package com.example.simulation;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class FeedAnimalGoal extends Goal {
    private final StaffEntity staff;
    private Animal targetAnimal = null;
    private int feedCooldown = 0;

    public FeedAnimalGoal(StaffEntity staff) {
        this.staff = staff;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!staff.hasFood())
            return false;
        if (feedCooldown > 0) {
            feedCooldown--;
            return false;
        }
        targetAnimal = findHungryAnimal();
        return targetAnimal != null;
    }

    @Override
    public void tick() {
        if (targetAnimal == null)
            return;
        staff.getNavigation().moveTo(targetAnimal, 1.2D);
        staff.getLookControl().setLookAt(targetAnimal, 30, 30);

        if (staff.distanceToSqr(targetAnimal) < 5.0) {
            staff.consumeOneFood();
            targetAnimal.heal(5.0F);

            // HANYA BIKIN LOVE JIKA BELUM INLOVE (AUTO SYNC DENGAN SEMUA MOD HEWAN)
            if (!targetAnimal.isInLove()) {
                targetAnimal.setInLove(null);
            }

            if (staff.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER, targetAnimal.getX(), targetAnimal.getY() + 1,
                        targetAnimal.getZ(), 10, 0.5, 0.5, 0.5, 0.05);
            }
            staff.swing(staff.getUsedItemHand());
            targetAnimal = null;
            feedCooldown = 200; // Cooldown 10 detik agar tidak spam!
        }
    }

    private Animal findHungryAnimal() {
        List<Animal> animals = staff.level().getEntitiesOfClass(Animal.class, staff.getBoundingBox().inflate(15));
        for (Animal a : animals) {
            // Syarat: Darah kurang, atau belum InLove, dan tidak sedang cooldown
            if (a.getHealth() < a.getMaxHealth() || !a.isInLove()) {
                return a;
            }
        }
        return null;
    }
}
