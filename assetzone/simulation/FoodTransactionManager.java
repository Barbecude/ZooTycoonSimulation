package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FoodTransactionManager {
    private static final int TRANSACTION_PROFIT_MARGIN = 70; // Zoo gets 70% of price
    private static final int INTERACTION_RADIUS = 6;

    /**
     * Process visitor purchase at shelf.
     * Returns true if transaction succeeds.
     */
    public static boolean processFoodPurchase(VisitorEntity visitor, ShelfBlockEntity shelf) {
        if (shelf == null || visitor.level().isClientSide) return false;

        if (!canServeAtShelf(visitor, shelf)) {
            visitor.playSound(SoundEvents.VILLAGER_NO, 1.0F, 0.9F);
            return false;
        }

        if (shelf.getFoodStock() < 1) {
            visitor.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return false;
        }

        shelf.removeFoodStock(1);
        int income = (shelf.getFoodPrice() * TRANSACTION_PROFIT_MARGIN) / 100;

        ZooData data = ZooData.get(visitor.level());
        data.addBalance(income);
        data.setRating(Math.min(100, data.getRating() + 1));
        shelf.addRevenue(income);

        visitor.setHunger(0);
        visitor.setMood(VisitorEntity.Mood.HAPPY, 140);
        visitor.setHasTrash(true);
        visitor.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

        return true;
    }

    /**
     * Kept for drink stall compatibility.
     */
    public static boolean processDrinkPurchase(VisitorEntity visitor, ShelfBlockEntity shelf) {
        if (shelf == null || visitor.level().isClientSide) return false;

        if (!canServeAtShelf(visitor, shelf)) {
            visitor.playSound(SoundEvents.VILLAGER_NO, 1.0F, 0.9F);
            return false;
        }

        if (shelf.getDrinkStock() < 1) {
            visitor.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return false;
        }

        shelf.removeDrinkStock(1);
        int income = (shelf.getDrinkPrice() * TRANSACTION_PROFIT_MARGIN) / 100;
        ZooData data = ZooData.get(visitor.level());
        data.addBalance(income);
        data.setRating(Math.min(100, data.getRating() + 1));
        shelf.addRevenue(income);

        visitor.setThirst(0);
        visitor.setMood(VisitorEntity.Mood.HAPPY, 120);
        visitor.setHasTrash(true);
        visitor.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

        return true;
    }

    public static BlockPos findNearestShelf(VisitorEntity visitor, String type) {
        BlockPos visitorPos = visitor.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                visitorPos.offset(-32, -5, -32),
                visitorPos.offset(32, 5, 32))) {

            BlockState state = visitor.level().getBlockState(pos);
            if ("food".equals(type) && ShelfBlock.isFoodShelfState(state)) {
                return pos.immutable();
            }
            if ("drink".equals(type) && ShelfBlock.isDrinkShelfState(state)) {
                return pos.immutable();
            }
        }

        return null;
    }

    public static boolean canServeAtShelf(VisitorEntity visitor, ShelfBlockEntity shelf) {
        BlockPos pos = shelf.getBlockPos();

        boolean cashierNearby = !visitor.level().getEntitiesOfClass(
                CashierEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(INTERACTION_RADIUS),
                e -> e.isAlive()).isEmpty();

        if (cashierNearby) return true;

        // Manual mode: player must stay near shelf to approve purchase.
        return !visitor.level().getEntitiesOfClass(
                Player.class,
                new net.minecraft.world.phys.AABB(pos).inflate(INTERACTION_RADIUS),
                e -> e.isAlive()).isEmpty();
    }
}
