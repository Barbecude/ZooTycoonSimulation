package com.example.simulation;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * FeedAnimalGoal — Staff AI Prioritas 2
 *
 * Logika:
 * "Adakah hewan di radius 15 blok yang belum InLove?"
 * Jika Ada → Jalan ke hewan → Heal + Partikel hati
 * Jika Tidak → Idle (biarkan Wander goal ambil alih)
 */
public class FeedAnimalGoal extends Goal {

    private final StaffEntity staff;
    private Animal targetAnimal = null;
    private int feedCooldown = 0;

    private static final double SEARCH_RANGE = 15.0D;

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

        // Cari hewan terdekat yang belum InLove
        targetAnimal = findHungryAnimal();
        return targetAnimal != null;
    }

    @Override
    public boolean canContinueToUse() {
        return targetAnimal != null
                && targetAnimal.isAlive()
                && staff.hasFood()
                && staff.distanceToSqr(targetAnimal) < (SEARCH_RANGE * SEARCH_RANGE * 4);
    }

    @Override
    public void start() {
        if (targetAnimal != null) {
            staff.getNavigation().moveTo(targetAnimal, 1.0D);
        }
    }

    @Override
    public void tick() {
        if (targetAnimal == null || !targetAnimal.isAlive())
            return;

        staff.getLookControl().setLookAt(targetAnimal, 30.0F, 30.0F);

        double dist = staff.distanceToSqr(targetAnimal);

        // Sudah dekat hewan (≤ 2 blok)
        if (dist < 4.0) {
            // Beri makan → Heal hewan + Partikel hati
            staff.consumeOneFood();

            // Heal hewan
            targetAnimal.heal(4.0F);

            // Set InLove (partikel hati otomatis muncul)
            targetAnimal.setInLove(null);

            // Extra: spawn partikel hati di server
            if (staff.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        targetAnimal.getX(), targetAnimal.getY() + 1.0,
                        targetAnimal.getZ(), 5,
                        0.3, 0.3, 0.3, 0.0);
            }

            // Animasi tangan (swing arm)
            staff.swing(staff.getUsedItemHand());

            targetAnimal = null; // Cari hewan berikutnya
            feedCooldown = 40; // Tunggu 2 detik sebelum beri makan lagi
        } else {
            // Belum sampai, terus jalan
            staff.getNavigation().moveTo(targetAnimal, 1.0D);
        }
    }

    @Override
    public void stop() {
        targetAnimal = null;
        staff.getNavigation().stop();
    }

    /**
     * Cari hewan (Animal.class) di radius SEARCH_RANGE yang belum InLove.
     * Kompatibel dengan semua mod karena menggunakan Animal.class!
     */
    private Animal findHungryAnimal() {
        AABB searchBox = staff.getBoundingBox().inflate(SEARCH_RANGE);
        List<Animal> animals = staff.level().getEntitiesOfClass(Animal.class, searchBox);

        Animal nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Animal animal : animals) {
            if (animal.isInLove())
                continue; // Sudah kenyang / InLove
            double d = staff.distanceToSqr(animal);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = animal;
            }
        }
        return nearest;
    }
}
