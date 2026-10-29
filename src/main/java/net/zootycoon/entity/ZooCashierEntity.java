package net.zootycoon.entity;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.economy.ZooEconomyManager;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ZooCashierEntity extends PathfinderMob {

    public static final int STOCK_SLOTS = 5;

    private static final EntityDataAccessor<String> CASHIER_ROLE =
            SynchedEntityData.defineId(ZooCashierEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> OUT_OF_STOCK =
            SynchedEntityData.defineId(ZooCashierEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> FACING_DIR =
            SynchedEntityData.defineId(ZooCashierEntity.class, EntityDataSerializers.INT);

    private final SimpleContainer stockInventory = new SimpleContainer(STOCK_SLOTS);
    private final List<UUID> customerQueue = new ArrayList<>();
    private int serveCooldown = 0;
    private int fomoCooldown = 0;
    private UUID servingCustomerUuid = null;
    private int serviceTicks = 0;
    private long lastVisitorSpawnTick = -1L;
    private static long lastTicketToastTick = 0L;

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
        builder.define(FACING_DIR, Direction.SOUTH.get2DDataValue());
    }

    public Direction getFacingDirection() {
        return Direction.from2DDataValue(Math.floorMod(this.entityData.get(FACING_DIR), 4));
    }

    public void setFacingDirection(Direction dir) {
        if (dir == null || dir.getAxis().isVertical()) {
            dir = Direction.SOUTH;
        }
        this.entityData.set(FACING_DIR, dir.get2DDataValue());
        float yaw = dir.toYRot();
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.yBodyRot = yaw;
        this.yBodyRotO = yaw;
        this.yHeadRot = yaw;
        this.yHeadRotO = yaw;
    }

    public void rotateClockwise() {
        setFacingDirection(getFacingDirection().getClockWise());
    }

    public int enqueueOrGetIndex(UUID visitorUuid) {
        if (visitorUuid == null) return -1;
        int idx = customerQueue.indexOf(visitorUuid);
        if (idx == -1) {
            customerQueue.add(visitorUuid);
            idx = customerQueue.size() - 1;
            if (customerQueue.size() == 1 && fomoCooldown <= 0) {
                tryTriggerFomoEffect(visitorUuid);
            }
        }
        return idx;
    }

    private void tryTriggerFomoEffect(UUID triggerVisitorUuid) {
        if (this.level().isClientSide()) return;
        if (!isRoleAssigned() || isOutOfStock()) return;

        // 25% chance as specified
        if (this.random.nextFloat() >= 0.25f) {
            return;
        }

        // Set cooldown on cashier (1200 ticks = 60s) to prevent cascading loops
        this.fomoCooldown = 1200;

        String role = getCashierRole();
        List<ZooVisitorEntity> candidates = new java.util.ArrayList<>();
        if (this.level() instanceof ServerLevel sLevel) {
            for (Entity entity : sLevel.getAllEntities()) {
                if (entity instanceof ZooVisitorEntity v && v.isAlive()) {
                    if (!v.getUUID().equals(triggerVisitorUuid)
                            && !v.isTimeToLeave()
                            && !v.isLeaving()
                            && !v.isFomoTarget()
                            && !customerQueue.contains(v.getUUID())
                            && !v.needsToilet()
                            && !v.isConsuming()
                            && v.getGuidingPlayerUuid() == null
                            && ("TICKET".equalsIgnoreCase(role)
                                ? !v.getTags().contains("ticket_purchased")
                                : (v.getTags().contains("ticket_purchased") && (
                                    ("BURGER".equalsIgnoreCase(role) && v.canEat()) ||
                                    ("DRINK".equalsIgnoreCase(role) && v.canDrink())
                                  )))) {
                        candidates.add(v);
                    }
                }
            }
        }

        if (candidates.isEmpty()) return;

        java.util.Collections.shuffle(candidates);
        int take = Math.min(4, candidates.size());
        for (int i = 0; i < take; i++) {
            candidates.get(i).triggerFomoForCashier(this);
        }
    }

    public void removeFromQueue(UUID visitorUuid) {
        if (visitorUuid != null) {
            customerQueue.remove(visitorUuid);
        }
    }

    public boolean isInQueue(UUID visitorUuid) {
        return visitorUuid != null && customerQueue.contains(visitorUuid);
    }

    public int getQueueSize() {
        return customerQueue.size();
    }

    public long getLastVisitorSpawnTick() {
        return lastVisitorSpawnTick;
    }

    public void setLastVisitorSpawnTick(long tick) {
        this.lastVisitorSpawnTick = tick;
    }

    /**
     * Queue index 0 is 2 blocks in front of the cashier's face.
     * Queue index k (k >= 1) is 2 + k blocks in front of the cashier's face (1 block behind visitor k - 1).
     */
    public BlockPos getQueueSlotPos(int queueIndex) {
        Direction facing = getFacingDirection();
        int dist = 2 + Math.max(0, queueIndex);
        BlockPos raw = this.blockPosition().relative(facing, dist);
        for (int dy : new int[]{0, -1, 1, -2, 2}) {
            BlockPos p = raw.offset(0, dy, 0);
            if (!this.level().getBlockState(p.below()).isAir()
                    && this.level().getBlockState(p).getCollisionShape(this.level(), p).isEmpty()) {
                return p;
            }
        }
        return raw;
    }

    public Vec3 getQueueSlotVec(int queueIndex) {
        BlockPos p = getQueueSlotPos(queueIndex);
        return new Vec3(p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D);
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
        if ("BURGER".equalsIgnoreCase(role) || "DRINK".equalsIgnoreCase(role)) {
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

        // Croptopia rule: only juice from croptopia is accepted (for DRINK); reject all other croptopia items
        if ("croptopia".equals(namespace)) {
            if ("DRINK".equalsIgnoreCase(role) || "ICE_CREAM".equalsIgnoreCase(role)) {
                return path.endsWith("_juice");
            }
            return false;
        }

        if ("BURGER".equalsIgnoreCase(role)) {
            if (stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                return false;
            }
            if (path.contains("burger")) {
                return true;
            }
            return stack.getTags().anyMatch(t -> t.location().getPath().toLowerCase(Locale.ROOT).contains("burger"));
        }

        if ("DRINK".equalsIgnoreCase(role) || "ICE_CREAM".equalsIgnoreCase(role)) {
            if (stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                return false;
            }
            if (path.contains("oil") || path.contains("sauce") || path.contains("molasses")
                    || path.contains("vinegar") || path.contains("bucket") || path.contains("dough")
                    || path.contains("potion") || path.contains("ominous") || path.contains("spawn_egg")
                    || path.equals("water_bottle") || path.equals("milk_bottle") || path.contains("bottle_of")
                    || path.contains("steamed") || path.contains("steak") || path.contains("stew")
                    || path.contains("soup") || path.contains("broth") || path.contains("curry")
                    || path.contains("leaves") || path.contains("seed") || path.contains("beans")
                    || path.contains("powder") || path.contains("cup") || path.contains("sandwich")
                    || path.contains("burger") || path.contains("pie") || path.contains("cake")) {
                return false;
            }
            String[] tokens = path.split("_");
            for (String token : tokens) {
                if (token.equals("juice") || token.equals("smoothie") || token.equals("tea")
                        || token.equals("coffee") || token.equals("latte") || token.equals("soda")
                        || token.equals("lemonade") || token.equals("limeade") || token.equals("horchata")
                        || token.equals("cider") || token.equals("cocoa") || token.equals("milkshake")
                        || token.equals("wine") || token.equals("beer") || token.equals("mead")) {
                    return true;
                }
            }
            if (stack.getUseAnimation() == UseAnim.DRINK && stack.has(DataComponents.FOOD)) {
                return true;
            }
            return false;
        }

        return false;
    }

    public static boolean isPlushieItem(Item item) {
        if (item == null) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return false;
        String namespace = id.getNamespace().toLowerCase(Locale.ROOT);
        String path = id.getPath().toLowerCase(Locale.ROOT);
        if ("plushie_buddies".equals(namespace) && !path.contains("workbench")) {
            return true;
        }
        return path.contains("plushie");
    }

    public int getTicketPrice() {
        if (this.level() instanceof ServerLevel sLevel) {
            ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
            var targetClaim = claimManager.getClaimAt(this.blockPosition());
            UUID ownerUuid = (targetClaim.isPresent() && targetClaim.get().getOwnerUuid() != null)
                    ? targetClaim.get().getOwnerUuid()
                    : null;
            if (ownerUuid != null) {
                return claimManager.getTicketPrice(ownerUuid);
            }
        }
        return 2;
    }

    public boolean isOnStrike() {
        if (this.level() instanceof ServerLevel sLevel) {
            ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
            var targetClaim = claimManager.getClaimAt(this.blockPosition());
            UUID ownerUuid = (targetClaim.isPresent() && targetClaim.get().getOwnerUuid() != null)
                    ? targetClaim.get().getOwnerUuid()
                    : null;
            if (ownerUuid != null) {
                return net.zootycoon.economy.ZooStaffManager.get(sLevel).areStaffOnStrike(ownerUuid);
            }
        }
        return false;
    }

    public static int getItemPrice(String role, ItemStack stack) {
        return net.zootycoon.economy.ZooEconomyEngine.getFoodDrinkPrice(role, stack);
    }

    /**
     * Randomly selects a non-empty valid slot among the 5 stock slots so visitors purchase varied items.
     */
    public ItemStack consumeOneStockItem() {
        String role = getCashierRole();
        List<Integer> validSlots = new ArrayList<>(STOCK_SLOTS);
        for (int i = 0; i < STOCK_SLOTS; i++) {
            ItemStack stack = this.stockInventory.getItem(i);
            if (!stack.isEmpty() && isValidItemForRole(role, stack)) {
                validSlots.add(i);
            }
        }
        if (validSlots.isEmpty()) {
            updateStockStatus();
            return ItemStack.EMPTY;
        }
        int chosenSlot = validSlots.get(this.random.nextInt(validSlots.size()));
        ItemStack stack = this.stockInventory.getItem(chosenSlot);
        ItemStack taken = stack.split(1);
        if (stack.isEmpty()) {
            this.stockInventory.setItem(chosenSlot, ItemStack.EMPTY);
        }
        updateStockStatus();
        return taken;
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
    }

    @Override
    public void aiStep() {
        super.aiStep();
        float yaw = getFacingDirection().toYRot();
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.yBodyRot = yaw;
        this.yBodyRotO = yaw;
        this.yHeadRot = yaw;
        this.yHeadRotO = yaw;
        this.setXRot(0.0F);
    }

    private boolean isVisitorEligibleForRole(ZooVisitorEntity v, String role) {
        if (v == null || !v.isAlive() || v.isTimeToLeave() || v.isConsuming() || v.hasCashierCooldown()) {
            return false;
        }
        if (v.isFomoTarget() && v.getFomoCashier() == this) {
            return true;
        }
        return switch (role) {
            case "TICKET" -> !v.getTags().contains("ticket_purchased");
            case "BURGER" -> v.hasWatchedAnimal() && v.canEat() && (v.getHunger() >= 35 || v.getMood() == ZooVisitorEntity.VisitorMood.HUNGRY
                    || (v.getGuidedPlayerUuid() != null && v.getHunger() >= 15));
            case "DRINK" -> v.hasWatchedAnimal() && v.canDrink() && (v.getThirst() >= 25 || v.getMood() == ZooVisitorEntity.VisitorMood.THIRSTY
                    || (v.getGuidedPlayerUuid() != null && v.getThirst() >= 15));
            default -> false;
        };
    }

    private boolean isCustomerReadyAtFront(ZooVisitorEntity customer) {
        if (customer == null) return false;
        Vec3 slot0 = getQueueSlotVec(0);
        double distToSlot0Sq = customer.distanceToSqr(slot0.x, slot0.y, slot0.z);
        double distToCashierSq = customer.distanceToSqr(this);
        return distToSlot0Sq <= 4.0D || distToCashierSq <= 12.25D;
    }

    private ZooVisitorEntity resolveNextReadyCustomer(ServerLevel sLevel, String role) {
        // 1. Prune invalid entries from customerQueue
        Iterator<UUID> it = customerQueue.iterator();
        while (it.hasNext()) {
            UUID id = it.next();
            if (!(sLevel.getEntity(id) instanceof ZooVisitorEntity v)
                    || !isVisitorEligibleForRole(v, role)
                    || v.distanceToSqr(this) > 32.0D * 32.0D) {
                it.remove();
            }
        }

        // 2. Enqueue any nearby eligible visitors within 8 blocks who are not yet in queue
        List<ZooVisitorEntity> nearby = sLevel.getEntitiesOfClass(
                ZooVisitorEntity.class,
                this.getBoundingBox().inflate(8.0D, 3.0D, 8.0D),
                v -> isVisitorEligibleForRole(v, role)
        );
        for (ZooVisitorEntity v : nearby) {
            enqueueOrGetIndex(v.getUUID());
        }

        if (customerQueue.isEmpty()) {
            return null;
        }

        // 3. Check if index 0 is ready at the front
        UUID firstId = customerQueue.get(0);
        if (sLevel.getEntity(firstId) instanceof ZooVisitorEntity front && isCustomerReadyAtFront(front)) {
            return front;
        }

        // 4. If index 0 is not ready at the front, check if any other queued visitor is standing ready at the front
        for (UUID id : customerQueue) {
            if (sLevel.getEntity(id) instanceof ZooVisitorEntity v && isCustomerReadyAtFront(v)) {
                customerQueue.remove(id);
                customerQueue.add(0, id);
                return v;
            }
        }

        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        if (serveCooldown > 0) serveCooldown--;
        if (fomoCooldown > 0) fomoCooldown--;

        if (isOnStrike()) {
            if (this.tickCount % 60 == 0 && this.level() instanceof ServerLevel sLevel) {
                sLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
            }
            servingCustomerUuid = null;
            serviceTicks = 0;
            return;
        }

        if (this.tickCount % 20 == 0) {
            updateStockStatus();
        }

        if (this.level() instanceof ServerLevel sLevel) {
            String role = getCashierRole();
            if (isRoleAssigned() && this.tickCount % 10 == 0) {
                // Keep queue clean every 10 ticks
                customerQueue.removeIf(id -> !(sLevel.getEntity(id) instanceof ZooVisitorEntity v)
                        || !isVisitorEligibleForRole(v, role)
                        || v.distanceToSqr(this) > 32.0D * 32.0D);
            }

            // 1. Ongoing service: process active customer transaction
            if (servingCustomerUuid != null) {
                Entity ent = sLevel.getEntity(servingCustomerUuid);
                if (ent instanceof ZooVisitorEntity customer && customer.isAlive()
                        && (customer.distanceToSqr(getQueueSlotVec(0)) <= 36.0D || customer.distanceToSqr(this) <= 25.0D)) {
                    serviceTicks--;
                    this.getLookControl().setLookAt(customer, 30.0F, 30.0F);

                    if ("TICKET".equals(role)) {
                        if (serviceTicks == 20) {
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_WORK_LIBRARIAN, SoundSource.NEUTRAL, 0.6F, 1.1F);
                        }
                        if (serviceTicks <= 0) {
                            removeFromQueue(customer.getUUID());
                            customer.addTag("ticket_purchased");
                            int ticketPrice = getTicketPrice();
                            if (ticketPrice > 0) {
                                creditOwner(sLevel, ticketPrice);
                            }
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8F, 1.1F);
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.2F);
                            sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 6, 0.25, 0.25, 0.25, 0.02);
                            customer.onTransactionComplete(this.blockPosition(), "TICKET", ItemStack.EMPTY, false);
                            notifyOwnerVisitorEntered(sLevel);
                            servingCustomerUuid = null;
                            serviceTicks = 0;
                            serveCooldown = 4;
                        }
                    } else if ("BURGER".equals(role)) {
                        // Sizzling / cooking burger sounds and smoke particles
                        if (serviceTicks % 20 == 0) {
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.CAMPFIRE_CRACKLE, SoundSource.NEUTRAL, 0.7F, 1.1F);
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_WORK_BUTCHER, SoundSource.NEUTRAL, 0.6F, 1.0F);
                            sLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.1D, this.getZ(), 3, 0.15, 0.1, 0.15, 0.01);
                        }
                        if (serviceTicks <= 0) {
                            ItemStack soldBurger = consumeOneStockItem();
                            if (!soldBurger.isEmpty()) {
                                removeFromQueue(customer.getUUID());
                                int price = getItemPrice(role, soldBurger);
                                creditOwner(sLevel, price);
                                sLevel.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.2F);
                                sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.7F, 1.1F);
                                sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 5, 0.25, 0.25, 0.25, 0.02);
                                if (customer.getGuidedPlayerUuid() != null) {
                                    customer.completeGuideReward(sLevel, "BURGER");
                                }
                                customer.onTransactionComplete(this.blockPosition(), "BURGER", soldBurger, false);
                            }
                            servingCustomerUuid = null;
                            serviceTicks = 0;
                            serveCooldown = 15;
                        }
                    } else if ("DRINK".equals(role)) {
                        // Pouring and bubbling drink sounds
                        if (serviceTicks % 20 == 0) {
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 0.7F, 1.0F);
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_WORK_CLERIC, SoundSource.NEUTRAL, 0.5F, 1.1F);
                            sLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.1D, this.getZ(), 4, 0.15, 0.1, 0.15, 0.02);
                        }
                        if (serviceTicks <= 0) {
                            ItemStack soldDrink = consumeOneStockItem();
                            if (!soldDrink.isEmpty()) {
                                removeFromQueue(customer.getUUID());
                                int price = getItemPrice(role, soldDrink);
                                creditOwner(sLevel, price);
                                sLevel.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.2F);
                                sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.7F, 1.1F);
                                sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 5, 0.25, 0.25, 0.25, 0.02);
                                if (customer.getGuidedPlayerUuid() != null) {
                                    customer.completeGuideReward(sLevel, "DRINK");
                                }
                                customer.onTransactionComplete(this.blockPosition(), "DRINK", soldDrink, true);
                            }
                            servingCustomerUuid = null;
                            serviceTicks = 0;
                            serveCooldown = 15;
                        }
                    }
                } else {
                    servingCustomerUuid = null;
                    serviceTicks = 0;
                }
            }

            // 2. Start new service for queued visitors when cashier is ready
            if (this.tickCount % 5 == 0 && serveCooldown <= 0 && servingCustomerUuid == null) {
                if ("BURGER".equals(role)) {
                    if (!isOutOfStock()) {
                        ZooVisitorEntity customer = resolveNextReadyCustomer(sLevel, role);
                        if (customer != null) {
                            servingCustomerUuid = customer.getUUID();
                            serviceTicks = 80; // 4 seconds to prepare burger
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_WORK_BUTCHER, SoundSource.NEUTRAL, 0.7F, 1.0F);
                        }
                    }
                } else if ("DRINK".equals(role)) {
                    if (!isOutOfStock()) {
                        ZooVisitorEntity customer = resolveNextReadyCustomer(sLevel, role);
                        if (customer != null) {
                            servingCustomerUuid = customer.getUUID();
                            serviceTicks = 80; // 4 seconds to prepare drink
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 0.7F, 1.0F);
                        }
                    }
                } else if ("TICKET".equals(role)) {
                    ZooVisitorEntity customer = resolveNextReadyCustomer(sLevel, role);
                    if (customer != null) {
                        servingCustomerUuid = customer.getUUID();
                        serviceTicks = 25; // Snappy ticket purchase (1.25 seconds)
                        sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_WORK_LIBRARIAN, SoundSource.NEUTRAL, 0.7F, 1.0F);
                    }
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

    private void notifyOwnerVisitorEntered(ServerLevel sLevel) {
        long currentTick = sLevel.getGameTime();
        if (currentTick - lastTicketToastTick < 2400) return; // at most once every 2 minutes

        BlockPos pos = this.blockPosition();
        for (ZooClaim claim : ZooClaimManager.get(sLevel).getAllClaims()) {
            if (claim.contains(pos.getX(), pos.getZ()) && claim.getOwnerUuid() != null) {
                ServerPlayer ownerPlayer = sLevel.getServer().getPlayerList().getPlayer(claim.getOwnerUuid());
                if (ownerPlayer != null) {
                    lastTicketToastTick = currentTick;
                    net.zootycoon.network.ZooNetworkPayloads.sendToast(ownerPlayer, "zootycoon.toast.title.info", "zootycoon.visitor.spawned", 0);
                }
                break;
            }
        }
    }

    public void sendScreenToPlayer(ServerPlayer sp) {
        boolean open = false;
        int ticketPrice = net.zootycoon.economy.ZooEconomyEngine.DEFAULT_TICKET_PRICE;
        if (this.level() instanceof ServerLevel sLevel) {
            ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
            var targetClaim = claimManager.getClaimAt(this.blockPosition());
            UUID ownerUuid = (targetClaim.isPresent() && targetClaim.get().getOwnerUuid() != null)
                    ? targetClaim.get().getOwnerUuid()
                    : sp.getUUID();
            open = claimManager.isZooOpen(ownerUuid);
            ticketPrice = claimManager.getTicketPrice(ownerUuid);
        }
        ServerPlayNetworking.send(sp, new ZooNetworkPayloads.OpenCashierScreenPayload(
                this.getId(),
                this.getCashierRole(),
                this.getName().getString(),
                this.getStockItemsList(),
                open,
                ticketPrice
        ));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sp) {
            if (player.isShiftKeyDown()) {
                rotateClockwise();
                this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.NEUTRAL, 0.9F, 1.1F);
                return InteractionResult.SUCCESS;
            }
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
        tag.putInt("FacingDir", this.entityData.get(FACING_DIR));
        tag.putLong("LastVisitorSpawnTick", lastVisitorSpawnTick);
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
        if (tag.contains("LastVisitorSpawnTick")) {
            lastVisitorSpawnTick = tag.getLong("LastVisitorSpawnTick");
        }
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
        if (tag.contains("FacingDir")) {
            setFacingDirection(Direction.from2DDataValue(tag.getInt("FacingDir")));
        }
        if (tag.contains("CashierRole")) {
            setCashierRole(tag.getString("CashierRole"));
        }
    }
}
