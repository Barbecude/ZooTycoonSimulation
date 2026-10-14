package net.zootycoon.entity;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.economy.ZooEconomyManager;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.List;
import java.util.UUID;

public class ZooCashierEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> CASHIER_ROLE =
            SynchedEntityData.defineId(ZooCashierEntity.class, EntityDataSerializers.STRING);

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
    }

    public String getCashierRole() {
        return this.entityData.get(CASHIER_ROLE);
    }

    public void setCashierRole(String role) {
        this.entityData.set(CASHIER_ROLE, role);
        switch (role) {
            case "ICE_CREAM" -> {
                this.setCustomName(Component.literal("Kasir Stand Es Krim"));
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SNOWBALL));
            }
            case "SOUVENIR" -> {
                this.setCustomName(Component.literal("Kasir Toko Souvenir"));
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.RABBIT_FOOT));
            }
            case "TICKET" -> {
                this.setCustomName(Component.literal("Kasir Loket Tiket"));
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.PAPER));
            }
            default -> {
                this.setCustomName(Component.literal("Kasir Zoo"));
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }
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

        // Every 2 seconds, serve nearby visitors based on assigned role
        if (this.tickCount % 40 == 0 && serveCooldown <= 0 && this.level() instanceof ServerLevel sLevel) {
            String role = getCashierRole();

            if ("ICE_CREAM".equals(role)) {
                // Serve thirsty visitors
                List<ZooVisitorEntity> visitors = sLevel.getEntitiesOfClass(
                        ZooVisitorEntity.class,
                        this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        v -> v.isAlive() && (v.getThirst() >= 25 || v.getHunger() >= 35)
                );
                if (!visitors.isEmpty()) {
                    ZooVisitorEntity customer = visitors.get(0);
                    this.getLookControl().setLookAt(customer, 30.0F, 30.0F);
                    customer.setThirst(0);
                    customer.setHunger(Math.max(0, customer.getHunger() - 25));
                    customer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.POTION));
                    customer.updateMood();
                    creditOwner(sLevel, 1);
                    sLevel.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.2F);
                    sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 5, 0.25, 0.25, 0.25, 0.02);
                    serveCooldown = 80;
                }
            } else if ("SOUVENIR".equals(role)) {
                // Serve visitors wanting souvenirs
                List<ZooVisitorEntity> visitors = sLevel.getEntitiesOfClass(
                        ZooVisitorEntity.class,
                        this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        v -> v.isAlive() && v.getMood() != ZooVisitorEntity.VisitorMood.ADORED
                );
                if (!visitors.isEmpty()) {
                    ZooVisitorEntity customer = visitors.get(0);
                    this.getLookControl().setLookAt(customer, 30.0F, 30.0F);
                    customer.setMood(ZooVisitorEntity.VisitorMood.ADORED);
                    customer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.RABBIT_FOOT));
                    creditOwner(sLevel, 2);
                    sLevel.playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.NEUTRAL, 0.8F, 1.4F);
                    sLevel.sendParticles(ParticleTypes.HEART, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 4, 0.25, 0.25, 0.25, 0.02);
                    serveCooldown = 120;
                }
            } else if ("TICKET".equals(role)) {
                // Serve newly arriving visitors
                List<ZooVisitorEntity> visitors = sLevel.getEntitiesOfClass(
                        ZooVisitorEntity.class,
                        this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        v -> v.isAlive() && !v.getTags().contains("ticket_purchased")
                );
                if (!visitors.isEmpty()) {
                    ZooVisitorEntity customer = visitors.get(0);
                    this.getLookControl().setLookAt(customer, 30.0F, 30.0F);
                    customer.addTag("ticket_purchased");
                    customer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.PAPER));
                    customer.updateMood();
                    creditOwner(sLevel, 2);
                    sLevel.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8F, 1.1F);
                    sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 6, 0.25, 0.25, 0.25, 0.02);
                    serveCooldown = 60;
                }
            } else {
                // Fallback unassigned serving
                List<ZooVisitorEntity> visitors = sLevel.getEntitiesOfClass(
                        ZooVisitorEntity.class,
                        this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        v -> v.isAlive() && (v.getHunger() >= 30 || v.getThirst() >= 30)
                );
                if (!visitors.isEmpty()) {
                    ZooVisitorEntity customer = visitors.get(0);
                    this.getLookControl().setLookAt(customer, 30.0F, 30.0F);
                    customer.setHunger(0);
                    customer.setThirst(0);
                    customer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.COOKED_BEEF));
                    customer.updateMood();
                    creditOwner(sLevel, 1);
                    sLevel.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.2F);
                    serveCooldown = 100;
                }
            }
        }
    }

    private void creditOwner(ServerLevel sLevel, int amount) {
        BlockPos pos = this.blockPosition();
        for (ZooClaim claim : ZooClaimManager.get(sLevel).getAllClaims()) {
            if (claim.contains(pos.getX(), pos.getZ())) {
                UUID ownerUuid = claim.getOwnerUuid();
                if (ownerUuid != null && sLevel.getServer() != null) {
                    ZooEconomyManager eco = ZooEconomyManager.get(sLevel.getServer());
                    ServerPlayer owner = sLevel.getServer().getPlayerList().getPlayer(ownerUuid);
                    eco.addBalance(ownerUuid, amount, owner);
                }
                break;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sp) {
            ServerPlayNetworking.send(sp, new ZooNetworkPayloads.OpenCashierScreenPayload(
                    this.getId(),
                    this.getCashierRole(),
                    this.getName().getString()
            ));
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("CashierRole", getCashierRole());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CashierRole")) {
            setCashierRole(tag.getString("CashierRole"));
        }
    }
}
