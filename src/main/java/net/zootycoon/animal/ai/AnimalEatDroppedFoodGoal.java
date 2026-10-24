package net.zootycoon.animal.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.mixin.MobAccessor;

import java.util.EnumSet;
import java.util.List;

/**
 * AI Goal for zoo animals to detect, move toward, and consume dropped/thrown food items in their enclosure.
 * Supports carnivores (thrown meat/fish) as their primary feeding behavior and herbivores (thrown crops/fruits).
 */
public class AnimalEatDroppedFoodGoal extends Goal {

    private final PathfinderMob mob;
    private final double speedModifier;
    private ItemEntity targetItem;
    private int eatCooldown = 0;
    private int searchCooldown = 0;
    private int tryTicks = 0;

    public AnimalEatDroppedFoodGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isAlive()) return false;
        if (!ZooAnimalHungerManager.isTagged(this.mob)) return false;

        if (this.eatCooldown > 0) {
            this.eatCooldown--;
            return false;
        }

        if (this.searchCooldown > 0) {
            this.searchCooldown--;
            return false;
        }
        this.searchCooldown = 6; // Scan every 6 ticks (0.3s) for responsive pickup

        List<ItemEntity> items = this.mob.level().getEntitiesOfClass(
                ItemEntity.class,
                this.mob.getBoundingBox().inflate(16.0D, 4.0D, 16.0D)
        );

        ItemEntity closest = null;
        double minDistance = Double.MAX_VALUE;

        for (ItemEntity item : items) {
            if (!item.isAlive() || item.getItem().isEmpty()) continue;
            if (AnimalDietRegistry.isFoodValid(this.mob, item.getItem())) {
                double dist = this.mob.distanceToSqr(item);
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = item;
                }
            }
        }

        if (closest != null) {
            this.targetItem = closest;
            this.tryTicks = 0;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isAlive()) return false;
        if (this.targetItem == null || !this.targetItem.isAlive() || this.targetItem.getItem().isEmpty()) return false;
        if (this.mob.distanceToSqr(this.targetItem) > 256.0D) return false;
        if (this.tryTicks > 200) return false; // 10-second timeout if blocked
        return true;
    }

    @Override
    public void start() {
        this.tryTicks = 0;
        if (this.targetItem != null) {
            this.mob.getNavigation().moveTo(this.targetItem, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        this.tryTicks++;
        this.mob.getLookControl().setLookAt(this.targetItem.getX(), this.targetItem.getY() + 0.1D, this.targetItem.getZ(), 30.0F, 30.0F);

        double maxEatDist = (this.mob.getBbWidth() * 0.5D) + 1.25D;
        double distSq = this.mob.distanceToSqr(this.targetItem);

        if (distSq <= maxEatDist * maxEatDist) {
            this.mob.getNavigation().stop();
            ItemStack stack = this.targetItem.getItem();
            if (!stack.isEmpty() && AnimalDietRegistry.isFoodValid(this.mob, stack)) {
                boolean fed = ZooAnimalHungerManager.feedAnimal(this.mob, stack, null);
                if (fed) {
                    if (stack.isEmpty()) {
                        this.targetItem.discard();
                    } else {
                        this.targetItem.setItem(stack);
                    }
                    this.eatCooldown = 30; // 1.5-second cooldown
                    this.stop();
                }
            }
        } else {
            if (this.mob.getNavigation().isDone() || this.tryTicks % 15 == 0) {
                this.mob.getNavigation().moveTo(this.targetItem, this.speedModifier);
            }
        }
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
                    if (wrapped.getGoal() instanceof AnimalEatDroppedFoodGoal) {
                        return; // Already attached
                    }
                }
                selector.addGoal(2, new AnimalEatDroppedFoodGoal(mob, 1.25D));
            }
        }
    }
}
