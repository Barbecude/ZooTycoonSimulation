package net.zootycoon.animal.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.mixin.MobAccessor;

import java.util.EnumSet;
import java.util.Set;

/**
 * AI Goal for grazing zoo animals (Zebra, Deer, Panda, Camel, Tortoise, Kangaroo, etc.)
 * to seek, navigate to, and graze upon natural vegetation blocks directly in their enclosure.
 */
public class AnimalGrazeNaturalBlockGoal extends MoveToBlockGoal {

    private final PathfinderMob mob;
    private int eatTicks = 0;
    private int cooldownTicks = 0;

    public AnimalGrazeNaturalBlockGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier, 16, 7);
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isAlive()) return false;
        if (!ZooAnimalHungerManager.isTagged(this.mob)) return false;
        if (ZooAnimalHungerManager.isSick(this.mob)) return false;

        if (AnimalDietRegistry.getFeedingType(this.mob) != AnimalDietRegistry.FeedingType.GRAZING) {
            return false;
        }

        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return false;
        }

        int hunger = ZooAnimalHungerManager.getHunger(this.mob);
        if (hunger >= ZooAnimalHungerManager.MAX_HUNGER || hunger > 90) {
            return false;
        }

        this.eatTicks = 0;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isAlive()) return false;
        if (ZooAnimalHungerManager.isSick(this.mob)) return false;
        if (ZooAnimalHungerManager.getHunger(this.mob) >= ZooAnimalHungerManager.MAX_HUNGER) return false;
        if (this.tryTicks > 200) return false;
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
        this.cooldownTicks = 80;
        this.mob.getNavigation().stop();
    }

    @Override
    public double acceptedDistance() {
        return Math.max(3.5D, (this.mob.getBbWidth() * 0.5D) + 2.2D);
    }

    @Override
    protected BlockPos getMoveToTarget() {
        int groundY = this.mob.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPos.getX(), this.blockPos.getZ());
        return new BlockPos(this.blockPos.getX(), groundY, this.blockPos.getZ());
    }

    @Override
    public void tick() {
        super.tick();

        double dx = (this.blockPos.getX() + 0.5D) - this.mob.getX();
        double dz = (this.blockPos.getZ() + 0.5D) - this.mob.getZ();
        double horizDistSq = dx * dx + dz * dz;
        double maxHorizDist = this.acceptedDistance();
        boolean verticalReach = this.blockPos.getY() >= this.mob.getY() - 1.5D
                && this.blockPos.getY() <= this.mob.getY() + this.mob.getBbHeight() + 1.8D;

        boolean reached = (horizDistSq <= maxHorizDist * maxHorizDist && verticalReach)
                || this.mob.blockPosition().closerThan(this.blockPos, maxHorizDist);

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

            if (this.eatTicks >= 20) {
                if (this.mob.level() instanceof ServerLevel sl) {
                    BlockState state = sl.getBlockState(this.blockPos);
                    Set<Block> validBlocks = AnimalDietRegistry.getNaturalFoodBlocks(this.mob);
                    if (validBlocks.contains(state.getBlock())) {
                        sl.playSound(null, this.blockPos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
                        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                                this.blockPos.getX() + 0.5, this.blockPos.getY() + 0.5, this.blockPos.getZ() + 0.5,
                                12, 0.2, 0.2, 0.2, 0.05);

                        sl.destroyBlock(this.blockPos, false);

                        ZooAnimalHungerManager.setHunger(this.mob, ZooAnimalHungerManager.MAX_HUNGER);
                        sl.sendParticles(ParticleTypes.HEART,
                                this.mob.getX(), this.mob.getY() + this.mob.getBbHeight() * 0.7, this.mob.getZ(),
                                4, 0.3, 0.3, 0.3, 0.02);
                    }
                }
                this.stop();
            }
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Set<Block> validBlocks = AnimalDietRegistry.getNaturalFoodBlocks(this.mob);
        return validBlocks.contains(state.getBlock());
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
                    if (wrapped.getGoal() instanceof AnimalGrazeNaturalBlockGoal) {
                        return;
                    }
                }
                selector.addGoal(3, new AnimalGrazeNaturalBlockGoal(mob, 1.15D));
            }
        }
    }
}
