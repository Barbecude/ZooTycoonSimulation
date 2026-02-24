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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class CashierEntity extends PathfinderMob {
    public static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(CashierEntity.class,
            EntityDataSerializers.INT);

    private static final double CASHIER_BASE_SPEED = 0.24D;
    private static final int DETECT_RADIUS = 24;

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
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, VisitorEntity.class, 8.0F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, CASHIER_BASE_SPEED)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    static class WorkShelfGoal extends Goal {
        // Cost to zoo per item when restocking (buying wholesale)
        private static final int MEAT_COST_PER_ITEM  = 8_000;  // cooked_beef (daging)
        private static final int DRINK_COST_PER_ITEM = 5_000;  // honey_bottle (coca cola)

        private final CashierEntity cashier;
        private BlockPos targetShelfPos;
        private int restockCooldown = 0;
        private int scanCooldown = 0;
        private final java.util.List<BlockPos> knownShelves = new java.util.ArrayList<>();
        private int shelfIndex = 0;

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
            scanAllShelves();
            scanCooldown = 40;
            if (knownShelves.isEmpty()) return false;

            // Rotate through all known shelves, pick the next one that needs restock
            for (int i = 0; i < knownShelves.size(); i++) {
                int idx = (shelfIndex + i) % knownShelves.size();
                BlockPos pos = knownShelves.get(idx);
                if (!(cashier.level().getBlockEntity(pos) instanceof ShelfBlockEntity shelf)) continue;
                if (!shelf.isStockFull()) {
                    targetShelfPos = pos;
                    shelfIndex = (idx + 1) % knownShelves.size();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return targetShelfPos != null && !cashier.level().isClientSide;
        }

        @Override
        public void tick() {
            if (restockCooldown > 0) restockCooldown--;

            if (targetShelfPos == null || !(cashier.level().getBlockEntity(targetShelfPos) instanceof ShelfBlockEntity shelf)) {
                targetShelfPos = null;
                return;
            }

            double dist = cashier.distanceToSqr(
                    targetShelfPos.getX() + 0.5D, targetShelfPos.getY(), targetShelfPos.getZ() + 0.5D);
            if (dist > 4.0D) {
                cashier.getNavigation().moveTo(
                        targetShelfPos.getX() + 0.5D, targetShelfPos.getY(), targetShelfPos.getZ() + 0.5D, 1.0D);
                return;
            }

            cashier.getNavigation().stop();
            cashier.getLookControl().setLookAt(
                    targetShelfPos.getX() + 0.5D, targetShelfPos.getY() + 0.5D, targetShelfPos.getZ() + 0.5D);

            if (restockCooldown <= 0) {
                doRestock(shelf);
                restockCooldown = 40;
                // Always move on after a restock attempt so the cashier rotates through all shelves.
                // canUse() will return to this shelf later if it still needs more stock.
                targetShelfPos = null;
            }
        }

        /**
         * Stocks the shelf with cooked_beef (food/daging) in slots 0-4
         * and honey_bottle (drink/coca cola) in slots 5-8.
         * Zoo balance is deducted per item added.
         */
        private void doRestock(ShelfBlockEntity shelf) {
            ZooData data = ZooData.get(cashier.level());
            for (int i = 0; i < shelf.getContainerSize(); i++) {
                ItemStack current = shelf.getItem(i);

                // Slots 0-4 = food (cooked_beef / daging), slots 5-8 = drink (honey_bottle / coca cola)
                boolean isFoodSlot = i < 5;
                net.minecraft.world.item.Item itemToStock = isFoodSlot ? Items.COOKED_BEEF : Items.HONEY_BOTTLE;
                int costPerItem = isFoodSlot ? MEAT_COST_PER_ITEM : DRINK_COST_PER_ITEM;

                // Skip if already adequately stocked with the correct item
                if (!current.isEmpty() && current.getItem() == itemToStock && current.getCount() >= 16) continue;

                int currentCount = (!current.isEmpty() && current.getItem() == itemToStock) ? current.getCount() : 0;
                int toAdd = 16 - currentCount;
                if (toAdd <= 0) continue;

                // Check if zoo can afford it; reduce batch if needed
                int affordable = (int) (data.getBalance() / costPerItem);
                if (affordable <= 0) return;
                toAdd = Math.min(toAdd, affordable);
                long cost = (long) toAdd * costPerItem;

                data.addBalance(-(int) cost);
                data.logTransaction("Pengeluaran",
                        "Cashier refill shelf: " + toAdd + "x "
                                + (isFoodSlot ? "cooked_beef" : "honey_bottle"),
                        -(int) cost);
                shelf.setItem(i, new ItemStack(itemToStock, currentCount + toAdd));
            }
        }

        /** Scans the area and rebuilds the list of all nearby shelves. */
        private void scanAllShelves() {
            knownShelves.clear();
            BlockPos origin = cashier.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(
                    origin.offset(-DETECT_RADIUS, -4, -DETECT_RADIUS),
                    origin.offset(DETECT_RADIUS, 4, DETECT_RADIUS))) {
                BlockState state = cashier.level().getBlockState(pos);
                if (!ShelfBlock.isFoodShelfState(state)) continue;
                if (!(cashier.level().getBlockEntity(pos) instanceof ShelfBlockEntity)) continue;
                knownShelves.add(pos.immutable());
            }
            if (!knownShelves.isEmpty() && shelfIndex >= knownShelves.size()) {
                shelfIndex = 0;
            }
        }
    }
}
