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
import net.zootycoon.animal.habitat.HabitatProfile;
import net.zootycoon.animal.habitat.HabitatProfileLoader;
import net.zootycoon.block.ZooFeedingTroughBlockEntity;

import java.util.EnumSet;

/**
 * AI Goal strictly for animals requiring water in their habitat to seek, navigate to,
 * and drink from the nearest water source (fluid water blocks or filled water troughs).
 * Includes strict timeout, thirst cooldown, and non-blocking recovery so mobs never get stuck.
 */
public class AnimalDrinkWaterGoal extends MoveToBlockGoal {

    private final PathfinderMob mob;
    private int drinkTicks = 0;
    private boolean finishedDrinking = false;
    private int cooldownTicks = 0;

    public AnimalDrinkWaterGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier, 24, 6);
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!mob.isAlive()) return false;
        if (!ZooAnimalHungerManager.isTagged(mob)) return false;

        // ONLY animals whose habitat specifically requires water (Rule 7 & 9)
        HabitatProfile profile = HabitatProfileLoader.getProfile(mob);
        if (profile == null || !profile.requiresWater()) {
            return false;
        }

        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return false;
        }

        int thirst = ZooAnimalHungerManager.getThirst(mob);
        // Triggers only when animal is actually thirsty (<= 40)
        if (thirst > 40) {
            return false;
        }

        this.drinkTicks = 0;
        this.finishedDrinking = false;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (!mob.isAlive()) return false;
        if (this.finishedDrinking) return false;
        // Navigation timeout: if unreachable within 10 seconds (200 ticks), abort so mob never gets stuck
        if (this.tryTicks > 200) return false;
        if (this.drinkTicks > 60) return false;
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.drinkTicks = 0;
        this.finishedDrinking = false;
    }

    @Override
    public void stop() {
        super.stop();
        this.drinkTicks = 0;
        this.finishedDrinking = true;
        // 5-minute cooldown (6000 ticks) before drinking again
        this.cooldownTicks = 6000;
        this.mob.getNavigation().stop();
    }

    @Override
    public double acceptedDistance() {
        return Math.max(2.0, (this.mob.getBbWidth() * 0.5) + 1.2);
    }

    @Override
    public void tick() {
        super.tick();

        // If mob is already submerged/swimming in water, consider reached
        boolean reached = this.isReachedTarget() || this.mob.isInWater();
        if (reached) {
            this.mob.getNavigation().stop();
            this.mob.getLookControl().setLookAt(
                    this.blockPos.getX() + 0.5,
                    this.blockPos.getY() + 0.2,
                    this.blockPos.getZ() + 0.5
            );

            this.drinkTicks++;

            if (this.mob.level() instanceof ServerLevel sl) {
                // Play drinking effects every 10 ticks while drinking
                if (this.drinkTicks % 10 == 0) {
                    sl.playSound(null, this.mob.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 0.7f, 1.0f);
                    sl.sendParticles(ParticleTypes.SPLASH,
                            this.mob.getX(), this.mob.getY() + 0.1, this.mob.getZ(),
                            6, 0.25, 0.1, 0.25, 0.05);
                }

                // After drinking for 40 ticks (~2 seconds), restore thirst to full and cleanly finish
                if (this.drinkTicks >= 40) {
                    ZooAnimalHungerManager.setThirst(this.mob, ZooAnimalHungerManager.MAX_THIRST);
                    sl.playSound(null, this.mob.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 0.5f, 1.2f);
                    this.finishedDrinking = true;
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
                HabitatProfile profile = HabitatProfileLoader.getProfile(mob);
                boolean requiresWater = profile != null && profile.requiresWater();

                for (var wrapped : selector.getAvailableGoals()) {
                    if (wrapped.getGoal() instanceof AnimalDrinkWaterGoal goal) {
                        if (!requiresWater) {
                            selector.removeGoal(goal);
                        }
                        return;
                    }
                }

                if (requiresWater) {
                    selector.addGoal(3, new AnimalDrinkWaterGoal(mob, 1.1));
                }
            }
        }
    }
}
