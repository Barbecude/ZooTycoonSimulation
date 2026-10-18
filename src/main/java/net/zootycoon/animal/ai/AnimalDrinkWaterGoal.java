package net.zootycoon.animal.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.block.ZooFeedingTroughBlockEntity;

import java.util.EnumSet;

/**
 * AI Goal for animals requiring water or experiencing thirst to seek, navigate to, and drink from
 * the nearest water source (fluid water blocks or filled water troughs) autonomously.
 * Modeled after Naturalist and Alex's Mobs drinking routines without requiring mixins.
 */
public class AnimalDrinkWaterGoal extends MoveToBlockGoal {

    private final PathfinderMob mob;
    private int drinkTicks = 0;

    public AnimalDrinkWaterGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier, 16, 4);
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!mob.isAlive()) return false;
        if (!ZooAnimalHungerManager.isTagged(mob)) return false;

        int thirst = ZooAnimalHungerManager.getThirst(mob);
        // Triggers when animal is thirsty (<= 70)
        if (thirst > 70) {
            return false;
        }

        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (!mob.isAlive()) return false;
        if (this.drinkTicks > 60) return false;
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.drinkTicks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.drinkTicks = 0;
    }

    @Override
    public double acceptedDistance() {
        return Math.max(2.0, (this.mob.getBbWidth() * 0.5) + 1.2);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isReachedTarget()) {
            this.mob.getNavigation().stop();
            this.mob.getLookControl().setLookAt(
                    this.blockPos.getX() + 0.5,
                    this.blockPos.getY() + 0.2,
                    this.blockPos.getZ() + 0.5
            );

            this.drinkTicks++;

            if (this.mob.level() instanceof ServerLevel sl) {
                // Play drinking effects every 10 ticks while at water
                if (this.drinkTicks % 10 == 0) {
                    sl.playSound(null, this.mob.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 0.7f, 1.0f);
                    sl.sendParticles(ParticleTypes.SPLASH,
                            this.mob.getX(), this.mob.getY() + 0.1, this.mob.getZ(),
                            6, 0.25, 0.1, 0.25, 0.05);
                }

                // After drinking for 40 ticks (~2 seconds), restore thirst to max!
                if (this.drinkTicks >= 40) {
                    ZooAnimalHungerManager.setThirst(this.mob, ZooAnimalHungerManager.MAX_THIRST);
                    sl.playSound(null, this.mob.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 0.5f, 1.2f);
                    this.stop();
                }
            }
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        // Water block
        BlockState state = level.getBlockState(pos);
        if (state.getFluidState().is(FluidTags.WATER) || state.is(Blocks.WATER)) {
            BlockState above = level.getBlockState(pos.above());
            return !above.isSolid();
        }

        // Water trough
        if (level.getBlockEntity(pos) instanceof ZooFeedingTroughBlockEntity trough && trough.hasWater()) {
            return true;
        }

        return false;
    }

    public static void attach(PathfinderMob mob) {
        if (mob == null) return;
        if (mob instanceof net.zootycoon.mixin.MobAccessor mobAccessor) {
            net.minecraft.world.entity.ai.goal.GoalSelector selector = mobAccessor.zootycoon$getGoalSelector();
            if (selector != null) {
                for (var wrapped : selector.getAvailableGoals()) {
                    if (wrapped.getGoal() instanceof AnimalDrinkWaterGoal) {
                        return;
                    }
                }
                selector.addGoal(3, new AnimalDrinkWaterGoal(mob, 1.1));
            }
        }
    }
}

