package net.zootycoon.animal.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.block.ZooFeedingTroughBlock;
import net.zootycoon.block.ZooFeedingTroughBlockEntity;
import net.zootycoon.mixin.MobAccessor;

import java.util.EnumSet;

/**
 * AI Goal for herbivorous and trough-feeding zoo animals to seek, navigate to,
 * and consume food from feeding troughs in their enclosure.
 * Works seamlessly across all hitbox sizes: small birds, ostriches, zebras, giraffes, and elephants.
 */
public class AnimalEatFromTroughGoal extends MoveToBlockGoal {

    private final PathfinderMob mob;
    private int eatTicks = 0;
    private int cooldownTicks = 0;

    public AnimalEatFromTroughGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier, 24, 6);
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isAlive()) return false;
        if (!ZooAnimalHungerManager.isTagged(this.mob)) return false;

        // Only for trough-feeding animals
        if (AnimalDietRegistry.getFeedingType(this.mob) != AnimalDietRegistry.FeedingType.TROUGH) {
            return false;
        }

        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return false;
        }

        int hunger = ZooAnimalHungerManager.getHunger(this.mob);
        // Triggers when animal hunger drops to 90 or below
        if (hunger > 90) {
            return false;
        }

        this.eatTicks = 0;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isAlive()) return false;
        if (this.tryTicks > 200) return false; // 10-second timeout if blocked
        if (this.eatTicks > 40) return false;
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.eatTicks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.eatTicks = 0;
        this.cooldownTicks = 100; // 5-second cooldown before eating from trough again
        this.mob.getNavigation().stop();
    }

    @Override
    public double acceptedDistance() {
        return Math.max(2.6D, (this.mob.getBbWidth() * 0.5D) + 1.6D);
    }

    @Override
    protected boolean findNearestBlock() {
        BlockPos mobPos = this.mob.blockPosition();
        BlockPos bestPos = null;
        double bestDistSq = Double.MAX_VALUE;

        for (BlockPos pos : ZooFeedingTroughBlock.KNOWN_TROUGHS) {
            double distSq = mobPos.distSqr(pos);
            if (distSq <= 32.0D * 32.0D && distSq < bestDistSq) {
                if (this.isValidTarget(this.mob.level(), pos)) {
                    bestDistSq = distSq;
                    bestPos = pos;
                }
            }
        }

        if (bestPos != null) {
            this.blockPos = bestPos;
            return true;
        }

        return super.findNearestBlock();
    }

    @Override
    public void tick() {
        super.tick();

        double dx = (this.blockPos.getX() + 0.5D) - this.mob.getX();
        double dz = (this.blockPos.getZ() + 0.5D) - this.mob.getZ();
        double horizDistSq = dx * dx + dz * dz;
        double vertDist = Math.abs((this.blockPos.getY() + 0.5D) - this.mob.getY());
        double maxDist = this.acceptedDistance();

        boolean reached = this.isReachedTarget()
                || this.mob.blockPosition().closerThan(this.blockPos, maxDist)
                || (horizDistSq <= maxDist * maxDist && vertDist <= 3.0D);

        if (reached) {
            this.mob.getNavigation().stop();
            this.mob.getLookControl().setLookAt(
                    this.blockPos.getX() + 0.5D,
                    this.blockPos.getY() + 0.2D,
                    this.blockPos.getZ() + 0.5D
            );

            this.eatTicks++;

            if (this.eatTicks % 10 == 0 && this.mob.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.mob.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.7f, 1.0f);
            }

            // After arriving and eating for 20 ticks (~1 second), consume 1 food
            if (this.eatTicks >= 20) {
                if (this.mob.level() instanceof ServerLevel sl) {
                    BlockEntity be = sl.getBlockEntity(this.blockPos);
                    if (be instanceof ZooFeedingTroughBlockEntity trough && trough.hasFood()) {
                        if (AnimalDietRegistry.isFoodValid(this.mob, trough.getFoodStack())) {
                            ItemStack food = trough.extractFood(1);
                            if (!food.isEmpty()) {
                                ZooAnimalHungerManager.feedAnimal(this.mob, food, null);
                                trough.setChanged();
                            }
                        }
                    }
                }
                this.stop();
            }
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ZooFeedingTroughBlockEntity trough && trough.hasFood()) {
            if (trough.isAssigned() && !trough.isAssignedTo(this.mob)) {
                return false;
            }
            return AnimalDietRegistry.isFoodValid(this.mob, trough.getFoodStack());
        }
        return false;
    }

    public static void attach(PathfinderMob mob) {
        if (mob == null || !mob.isAlive()) return;
        if (mob.getTags().contains("phys_veh.seat") || mob.getTags().contains("phys_veh.spawn")) {
            return;
        }

        if (mob instanceof MobAccessor mobAccessor) {
            var selector = mobAccessor.zootycoon$getGoalSelector();
            if (selector != null) {
                for (var wrapped : selector.getAvailableGoals()) {
                    if (wrapped.getGoal() instanceof AnimalEatFromTroughGoal) {
                        return; // Already attached
                    }
                }
                selector.addGoal(3, new AnimalEatFromTroughGoal(mob, 1.15D));
            }
        }
    }
}
