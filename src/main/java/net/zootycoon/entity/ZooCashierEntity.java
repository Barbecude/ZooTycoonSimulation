package net.zootycoon.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
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

    private int serveCooldown = 0;

    public ZooCashierEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        if (!level.isClientSide()) {
            this.setCustomName(Component.literal(ZooTranslations.get("entity.zootycoon.cashier")));
            this.setCustomNameVisible(true);
            this.addTag("zootycoon_staff");
            this.addTag("zootycoon_cashier");
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

        // Every 2 seconds, serve nearby hungry or thirsty visitors within 6 blocks
        if (this.tickCount % 40 == 0 && serveCooldown <= 0 && this.level() instanceof ServerLevel sLevel) {
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
                customer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(this.random.nextBoolean() ? Items.COOKED_BEEF : Items.HONEY_BOTTLE));
                customer.updateMood();
                sLevel.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.2F);
                sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, customer.getX(), customer.getY() + 1.2D, customer.getZ(), 5, 0.25, 0.25, 0.25, 0.02);

                // Credit 1 Emerald to the claim owner if inside a claimed zoo
                BlockPos pos = this.blockPosition();
                for (ZooClaim claim : ZooClaimManager.get(sLevel).getAllClaims()) {
                    if (claim.contains(pos.getX(), pos.getZ())) {
                        UUID ownerUuid = claim.getOwnerUuid();
                        if (ownerUuid != null && sLevel.getServer() != null) {
                            ZooEconomyManager eco = ZooEconomyManager.get(sLevel.getServer());
                            ServerPlayer owner = sLevel.getServer().getPlayerList().getPlayer(ownerUuid);
                            eco.addBalance(ownerUuid, 1, owner);
                        }
                        break;
                    }
                }
                serveCooldown = 100;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sp) {
            this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.info", "zootycoon.cashier.greet", 0);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }
}
