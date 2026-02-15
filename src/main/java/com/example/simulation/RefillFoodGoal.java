package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * RefillFoodGoal — Staff AI Prioritas 1
 *
 * Logika:
 * "Apakah aku bawa makanan?"
 * Jika TIDAK → Cari ZooComputerBlockEntity terdekat
 * → Jalan ke sana → Kurangi saldo bos Rp100 → Isi foodStock staff jadi 5
 */
public class RefillFoodGoal extends Goal {

    private final StaffEntity staff;
    private BlockPos targetComputerPos = null;
    private int cooldown = 0;

    private static final int SEARCH_RADIUS = 30;
    private static final int REFILL_AMOUNT = 5;

    public RefillFoodGoal(StaffEntity staff) {
        this.staff = staff;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Hanya aktif jika staff tidak punya makanan
        if (staff.hasFood())
            return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        // Cari ZooComputer terdekat
        targetComputerPos = findNearestComputer();
        return targetComputerPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !staff.hasFood() && targetComputerPos != null;
    }

    @Override
    public void start() {
        if (targetComputerPos != null) {
            staff.getNavigation().moveTo(
                    targetComputerPos.getX() + 0.5,
                    targetComputerPos.getY(),
                    targetComputerPos.getZ() + 0.5,
                    1.0D);
        }
    }

    @Override
    public void tick() {
        if (targetComputerPos == null)
            return;

        double dist = staff.distanceToSqr(
                targetComputerPos.getX() + 0.5,
                targetComputerPos.getY() + 0.5,
                targetComputerPos.getZ() + 0.5);

        // Sudah dekat komputer (≤ 2.5 blok)
        if (dist < 6.25) {
            BlockEntity be = staff.level().getBlockEntity(targetComputerPos);
            if (be instanceof ZooComputerBlockEntity computer) {
                int cost = ZooComputerBlockEntity.getFoodCost();
                if (computer.getBalance() >= cost) {
                    computer.addBalance(-cost); // Kurangi saldo bos
                    staff.setFoodStock(REFILL_AMOUNT);

                    // Efek visual: partikel
                    staff.level().broadcastEntityEvent(staff, (byte) 7);
                }
            }
            targetComputerPos = null; // Selesai
        }
    }

    @Override
    public void stop() {
        targetComputerPos = null;
        cooldown = 60; // Tunggu 3 detik sebelum cek ulang
    }

    /**
     * Scan area di sekitar staff untuk menemukan ZooComputerBlockEntity.
     */
    private BlockPos findNearestComputer() {
        BlockPos staffPos = staff.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        double closest = Double.MAX_VALUE;
        BlockPos found = null;

        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    mutable.set(staffPos.getX() + x, staffPos.getY() + y, staffPos.getZ() + z);
                    BlockEntity be = staff.level().getBlockEntity(mutable);
                    if (be instanceof ZooComputerBlockEntity) {
                        double d = staffPos.distSqr(mutable);
                        if (d < closest) {
                            closest = d;
                            found = mutable.immutable();
                        }
                    }
                }
            }
        }
        return found;
    }
}
