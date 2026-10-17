package net.zootycoon.entity;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.economy.ZooEconomyManager;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ZooCashierEntity extends PathfinderMob {

    public static final int STOCK_SLOTS = 5;

    private static final EntityDataAccessor<String> CASHIER_ROLE =
            SynchedEntityData.defineId(ZooCashierEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> OUT_OF_STOCK =
            SynchedEntityData.defineId(ZooCashierEntity.class, EntityDataSerializers.BOOLEAN);

    private final SimpleContainer stockInventory = new SimpleContainer(STOCK_SLOTS);
    private int serveCooldown = 0;

    public ZooCashierEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        if (!level.isClientSide()) {
            this.setCustomName(Component.literal("Kasir Kebun Binatang"));
            this.setCustomNameVisible(true);
            this.addTag("zootycoon_staff");
            this.addTag("zootycoon_cashier");
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CASHIER_ROLE, "UNASSIGNED");
        builder.define(OUT_OF_STOCK, false);
    }

    public String getCashierRole() {
        return this.entityData.get(CASHIER_ROLE);
    }

    public boolean isRoleAssigned() {
        String role = getCashierRole();
        return role != null && !role.isEmpty() && !"UNASSIGNED".equalsIgnoreCase(role);
    }

    public boolean tryAssignRole(String role) {
        if (isRoleAssigned()) {
            return false;
        }
        setCashierRole(role);
        return true;
    }

    public void setCashierRole(String role) {
        if ("ICE_CREAM".equalsIgnoreCase(role)) {
            role = "DRINK";
        }
        this.entityData.set(CASHIER_ROLE, role);
        switch (role) {
            case "BURGER" -> this.setCustomName(Component.literal("Kasir Stand Burger"));
            case "DRINK" -> this.setCustomName(Component.literal("Kasir Stand Minuman"));
            case "SOUVENIR" -> this.setCustomName(Component.literal("Kasir Toko Souvenir"));
            case "TICKET" -> this.setCustomName(Component.literal("Kasir Loket Tiket"));
            default -> this.setCustomName(Component.literal("Kasir Zoo"));
        }
        updateStockStatus();
    }

    public boolean isOutOfStock() {
        return this.entityData.get(OUT_OF_STOCK);
    }

    public SimpleContainer getStockInventory() {
        return this.stockInventory;
    }

    public List<ItemStack> getStockItemsList() {
        List<ItemStack> list = new ArrayList<>(STOCK_SLOTS);
        for (int i = 0; i < STOCK_SLOTS; i++) {
            list.add(this.stockInventory.getItem(i).copy());
        }
        return list;
    }

    public int getTotalStockCount() {
        String role = getCashierRole();
        int total = 0;
        for (int i = 0; i < STOCK_SLOTS; i++) {
            ItemStack stack = this.stockInventory.getItem(i);
            if (!stack.isEmpty() && isValidItemForRole(role, stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public void updateStockStatus() {
        if (this.level().isClientSide()) return;
        String role = getCashierRole();
        if ("TICKET".equalsIgnoreCase(role)) {
            this.entityData.set(OUT_OF_STOCK, false);
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.PAPER));
            return;
        }
        if ("BURGER".equalsIgnoreCase(role) || "DRINK".equalsIgnoreCase(role) || "SOUVENIR".equalsIgnoreCase(role)) {
            ItemStack firstValid = ItemStack.EMPTY;
            for (int i = 0; i < STOCK_SLOTS; i++) {
                ItemStack st = this.stockInventory.getItem(i);
                if (!st.isEmpty() && isValidItemForRole(role, st)) {
                    firstValid = st;
                    break;
                }
            }
            if (!firstValid.isEmpty()) {
                this.entityData.set(OUT_OF_STOCK, false);
                this.setItemSlot(EquipmentSlot.MAINHAND, firstValid.copyWithCount(1));
            } else {
                this.entityData.set(OUT_OF_STOCK, true);
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            return;
        }
        this.entityData.set(OUT_OF_STOCK, false);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    public static boolean isValidItemForRole(String role, ItemStack stack) {
        if (stack == null || stack.isEmpty() || role == null) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        String namespace = id.getNamespace().toLowerCase(Locale.ROOT);
        String path = id.getPath().toLowerCase(Locale.ROOT);

        if ("BURGER".equalsIgnoreCase(role)) {
            if (path.contains("burger")) {
                return true;
            }
            return stack.getTags().anyMatch(t -> t.location().getPath().toLowerCase(Locale.ROOT).contains("burger"));
        }

        if ("DRINK".equalsIgnoreCase(role) || "ICE_CREAM".equalsIgnoreCase(role)) {
            if (path.contains("oil") || path.contains("sauce") || path.contains("molasses")
                    || path.contains("vinegar") || path.contains("bucket") || path.contains("dough")) {
                return false;
            }
            if (stack.getUseAnimation() == UseAnim.DRINK) {
                return true;
            }
            if (path.contains("juice") || path.contains("smoothie") || path.contains("tea")
                    || path.contains("coffee") || path.contains("latte") || path.contains("milk")
                    || path.contains("soda") || path.contains("lemonade") || path.contains("limeade")
                    || path.contains("horchata") || path.contains("cider") || path.contains("cocoa")
                    || path.contains("wine") || path.contains("beer") || path.contains("rum")
                    || path.contains("mead") || path.contains("water_bottle") || path.contains("drink")
                    || path.contains("beverage")) {
                return true;
            }
            return stack.getTags().anyMatch(t -> {
                String p = t.location().getPath().toLowerCase(Locale.ROOT);
                return p.contains("drink") || p.contains("juice") || p.contains("smoothie") || p.contains("tea") || p.contains("beverage");
            });
        }

        if ("SOUVENIR".equalsIgnoreCase(role)) {
            if ("plushie_buddies".equals(namespace) && !path.contains("workbench")) {
                return true;
            }
            return path.contains("plushie");
        }

        return false;
    }

    public static int getItemPrice(String role, ItemStack stack) {
        if ("BURGER".equalsIgnoreCase(role)) {
            FoodProperties food = stack != null ? stack.get(DataComponents.FOOD) : null;
            if (food != null && food.nutrition() >= 10) {
                return 4;
            }
            return 3;
        }
        if ("DRINK".equalsIgnoreCase(role) || "ICE_CREAM".equalsIgnoreCase(role)) {
            if (stack != null && !stack.isEmpty()) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (id != null) {
                    String path = id.getPath().toLowerCase(Locale.ROOT);
                    if (path.contains("smoothie") || path.contains("milkshake") || path.contains("latte")
                            || path.contains("wine") || path.contains("mead") || path.contains("beer")
                            || path.contains("rum") || path.contains("cider") || path.contains("cocoa")) {
                        return 3;
                    }
                }
            }
            return 2;
        }
        if ("SOUVENIR".equalsIgnoreCase(role)) {
            return 5;
        }
        if ("TICKET".equalsIgnoreCase(role)) {
            return 2;
        }
        return 2;
    }

    public ItemStack consumeOneStockItem() {
        String role = getCashierRole();
        for (int i = 0; i < STOCK_SLOTS; i++) {
            ItemStack stack = this.stockInventory.getItem(i);
            if (!stack.isEmpty() && isValidItemForRole(role, stack)) {
                ItemStack taken = stack.split(1);
                if (stack.isEmpty()) {
                    this.stockInventory.setItem(i, ItemStack.EMPTY);
                }
                updateStockStatus();
                return taken;
            }
        }
        updateStockStatus();
        return ItemStack.EMPTY;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, ZooVisitorEntity.class, 8.0F));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        if (serveCooldown > 0) serveCooldown--;

        if (this.tickCount % 20 == 0) {
            updateStockStatus();
        }

        // Serve nearby visitors based on assigned role
        if (this.tickCount % 30 == 0 && serveCooldown <= 0 && this.level() instanceof ServerLevel sLevel) {
            String role = getCashierRole();

            if ("BURGER".equals(role)) {
                if (isOutOfStock()) return;

                List<ZooVisitorEntity> visitors = sLevel.getEntitiesOfClass(
                        ZooVisitorEntity.class,
                        this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        v -> v.isAlive() && !v.isConsuming() && (v.getHunger() >= 35 || v.getMood() == ZooVisitorEntity.VisitorMood.HUNGRY || (v.getGuidedPlayerUuid() != null && v.getHunger() >= 15))
                );
                if (!visitors.isEmpty()) {
                    ZooVisitorEntity customer = visitors.get(0);
                    ItemStack soldBurger = consumeOneStockItem();
                    if (!soldBurger.isEmpty()) {
                        int price = getItemPrice(role, soldBurger);
                        this.getLookControl().setLookAt(customer, 30.0F, 30.0F);
                        customer.setHunger(0);
                        customer.startConsuming(soldBurger, false);
                        customer.updateMood();
                        creditOwner(sLevel, price);
                        sLevel.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.2F);
                        sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 5, 0.25, 0.25, 0.25, 0.02);
                        if (customer.getGuidedPlayerUuid() != null) {
                            customer.completeGuideReward(sLevel, "BURGER");
                        }
                        serveCooldown = 60;
                    }
                }
            } else if ("DRINK".equals(role)) {
                if (isOutOfStock()) return;

                List<ZooVisitorEntity> visitors = sLevel.getEntitiesOfClass(
                        ZooVisitorEntity.class,
                        this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        v -> v.isAlive() && !v.isConsuming() && (v.getThirst() >= 25 || v.getMood() == ZooVisitorEntity.VisitorMood.THIRSTY || (v.getGuidedPlayerUuid() != null && v.getThirst() >= 15))
                );
                if (!visitors.isEmpty()) {
                    ZooVisitorEntity customer = visitors.get(0);
                    ItemStack soldDrink = consumeOneStockItem();
                    if (!soldDrink.isEmpty()) {
                        int price = getItemPrice(role, soldDrink);
                        this.getLookControl().setLookAt(customer, 30.0F, 30.0F);
                        customer.setThirst(0);
                        customer.startConsuming(soldDrink, true);
                        customer.updateMood();
                        creditOwner(sLevel, price);
                        sLevel.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.2F);
                        sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 5, 0.25, 0.25, 0.25, 0.02);
                        if (customer.getGuidedPlayerUuid() != null) {
                            customer.completeGuideReward(sLevel, "DRINK");
                        }
                        serveCooldown = 60;
                    }
                }
            } else if ("SOUVENIR".equals(role)) {
                if (isOutOfStock()) return;

                List<ZooVisitorEntity> visitors = sLevel.getEntitiesOfClass(
                        ZooVisitorEntity.class,
                        this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        v -> v.isAlive() && v.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && !v.needsToilet()
                );
                if (!visitors.isEmpty()) {
                    ZooVisitorEntity customer = visitors.get(0);
                    ItemStack soldPlushie = consumeOneStockItem();
                    if (!soldPlushie.isEmpty()) {
                        int price = getItemPrice(role, soldPlushie);
                        this.getLookControl().setLookAt(customer, 30.0F, 30.0F);
                        customer.setItemSlot(EquipmentSlot.HEAD, soldPlushie.copyWithCount(1));
                        customer.setDropChance(EquipmentSlot.HEAD, 0.0F);
                        customer.setMood(ZooVisitorEntity.VisitorMood.ADORED);
                        creditOwner(sLevel, price);
                        sLevel.playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.NEUTRAL, 0.8F, 1.4F);
                        sLevel.sendParticles(ParticleTypes.HEART, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 5, 0.25, 0.25, 0.25, 0.02);
                        serveCooldown = 100;
                    }
                } else if (this.tickCount % 150 == 0) {
                    // Summon a random wandering visitor without a plushie hat to visit the souvenir shop
                    List<ZooVisitorEntity> wanderers = sLevel.getEntitiesOfClass(
                            ZooVisitorEntity.class,
                            this.getBoundingBox().inflate(20.0D, 4.0D, 20.0D),
                            v -> v.isAlive() && v.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && !v.needsToilet() && v.getGuidedPlayerUuid() == null && v.getHunger() < 50 && v.getThirst() < 50
                    );
                    if (!wanderers.isEmpty()) {
                        ZooVisitorEntity wanderer = wanderers.get(this.random.nextInt(wanderers.size()));
                        wanderer.getNavigation().moveTo(this, 1.0D);
                    }
                }
            } else if ("TICKET".equals(role)) {
                List<ZooVisitorEntity> visitors = sLevel.getEntitiesOfClass(
                        ZooVisitorEntity.class,
                        this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        v -> v.isAlive() && !v.getTags().contains("ticket_purchased")
                );
                if (!visitors.isEmpty()) {
                    ZooVisitorEntity customer = visitors.get(0);
                    this.getLookControl().setLookAt(customer, 30.0F, 30.0F);
                    customer.addTag("ticket_purchased");
                    customer.updateMood();
                    creditOwner(sLevel, getItemPrice("TICKET", ItemStack.EMPTY));
                    sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8F, 1.1F);
                    sLevel.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.2F);
                    sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 6, 0.25, 0.25, 0.25, 0.02);
                    serveCooldown = 40;
                }
            }
        }
    }

    private void creditOwner(ServerLevel sLevel, int amount) {
        BlockPos pos = this.blockPosition();
        ZooClaim targetClaim = null;
        double bestDist = Double.MAX_VALUE;
        for (ZooClaim claim : ZooClaimManager.get(sLevel).getAllClaims()) {
            if (claim.contains(pos.getX(), pos.getZ())) {
                targetClaim = claim;
                break;
            }
            var box = claim.getBox();
            int cx = Math.clamp(pos.getX(), box.minX(), box.maxX());
            int cz = Math.clamp(pos.getZ(), box.minZ(), box.maxZ());
            double d = (pos.getX() - cx) * (pos.getX() - cx) + (pos.getZ() - cz) * (pos.getZ() - cz);
            if (d < bestDist && d <= 256.0D) {
                bestDist = d;
                targetClaim = claim;
            }
        }
        if (targetClaim != null && targetClaim.getOwnerUuid() != null && sLevel.getServer() != null) {
            UUID ownerUuid = targetClaim.getOwnerUuid();
            ZooEconomyManager eco = ZooEconomyManager.get(sLevel.getServer());
            ServerPlayer owner = sLevel.getServer().getPlayerList().getPlayer(ownerUuid);
            eco.addBalance(ownerUuid, amount, owner);
        }
    }

    public void sendScreenToPlayer(ServerPlayer sp) {
        ServerPlayNetworking.send(sp, new ZooNetworkPayloads.OpenCashierScreenPayload(
                this.getId(),
                this.getCashierRole(),
                this.getName().getString(),
                this.getStockItemsList()
        ));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sp) {
            updateStockStatus();
            sendScreenToPlayer(sp);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        Containers.dropContents(level, this, this.stockInventory);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("CashierRole", getCashierRole());
        ListTag itemsTag = new ListTag();
        for (int i = 0; i < STOCK_SLOTS; i++) {
            ItemStack stack = this.stockInventory.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                itemsTag.add(stack.save(this.registryAccess(), slotTag));
            }
        }
        tag.put("StockItems", itemsTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.stockInventory.clearContent();
        if (tag.contains("StockItems", Tag.TAG_LIST)) {
            ListTag itemsTag = tag.getList("StockItems", Tag.TAG_COMPOUND);
            for (int i = 0; i < itemsTag.size(); i++) {
                CompoundTag slotTag = itemsTag.getCompound(i);
                int slot = slotTag.getByte("Slot") & 255;
                if (slot < STOCK_SLOTS) {
                    this.stockInventory.setItem(slot, ItemStack.parseOptional(this.registryAccess(), slotTag));
                }
            }
        }
        if (tag.contains("CashierRole")) {
            setCashierRole(tag.getString("CashierRole"));
        }
    }
}
