package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class CashierEntity extends PathfinderMob {
    public static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(CashierEntity.class,
            EntityDataSerializers.INT);

    private static final double CASHIER_BASE_SPEED = 0.24D;
    private static final int DETECT_RADIUS = 24;
    private static final int RESTOCK_BATCH = 8;
    private static final int RESTOCK_COST_PER_ITEM = 2_000;

    public CashierEntity(EntityType<? extends CashierEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        this.setVariant(this.getRandom().nextInt(20));
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WorkShelfGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, CASHIER_BASE_SPEED)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    static class WorkShelfGoal extends Goal {
        private final CashierEntity cashier;
        private BlockPos targetShelfPos;
        private int restockCooldown = 0;
        private int scanCooldown = 0;

        WorkShelfGoal(CashierEntity cashier) {
            this.cashier = cashier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (scanCooldown > 0) {
                scanCooldown--;
                return false;
            }
            targetShelfPos = findNearestShelf();
            scanCooldown = 20;
            return targetShelfPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return targetShelfPos != null && !cashier.level().isClientSide;
        }

        @Override
        public void tick() {
            if (restockCooldown > 0) restockCooldown--;

            if (targetShelfPos == null || !(cashier.level().getBlockEntity(targetShelfPos) instanceof ShelfBlockEntity shelf)) {
                targetShelfPos = findNearestShelf();
                return;
            }

            double dist = cashier.distanceToSqr(targetShelfPos.getX() + 0.5D, targetShelfPos.getY(), targetShelfPos.getZ() + 0.5D);
            if (dist > 4.0D) {
                cashier.getNavigation().moveTo(targetShelfPos.getX() + 0.5D, targetShelfPos.getY(), targetShelfPos.getZ() + 0.5D, 1.0D);
                return;
            }

            cashier.getNavigation().stop();
            cashier.getLookControl().setLookAt(targetShelfPos.getX() + 0.5D, targetShelfPos.getY() + 0.5D, targetShelfPos.getZ() + 0.5D);

            if (restockCooldown <= 0) {
                restockIfNeeded(shelf);
                restockCooldown = 40;
            }
        }

        private void restockIfNeeded(ShelfBlockEntity shelf) {
            int stock = shelf.getFoodStock();
            if (stock >= 56) return;

            int need = Math.min(RESTOCK_BATCH, 64 - stock);
            if (need <= 0) return;

            ZooData data = ZooData.get(cashier.level());
            int cost = need * RESTOCK_COST_PER_ITEM;
            if (data.getBalance() < cost) return;

            data.addBalance(-cost);
            shelf.addFoodStock(need);
        }

        private BlockPos findNearestShelf() {
            BlockPos origin = cashier.blockPosition();
            BlockPos nearest = null;
            double best = Double.MAX_VALUE;

            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-DETECT_RADIUS, -4, -DETECT_RADIUS),
                    origin.offset(DETECT_RADIUS, 4, DETECT_RADIUS))) {
                BlockState state = cashier.level().getBlockState(pos);
                if (!ShelfBlock.isFoodShelfState(state)) continue;

                BlockEntity be = cashier.level().getBlockEntity(pos);
                if (!(be instanceof ShelfBlockEntity)) continue;

                double d = origin.distSqr(pos);
                if (d < best) {
                    best = d;
                    nearest = pos.immutable();
                }
            }

            return nearest;
        }
    }
}
