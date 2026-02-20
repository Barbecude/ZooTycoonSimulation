package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FoodTransactionManager {
    private static final int FOOD_PRICE = 5000;
    private static final int DRINK_PRICE = 3000;
    private static final int TRANSACTION_PROFIT_MARGIN = 70; // Zoo gets 70% of price

    /**
     * Process visitor purchase at shelf
     * @return true if transaction successful
     */
    public static boolean processFoodPurchase(VisitorEntity visitor, ShelfBlockEntity shelf) {
        if (shelf == null || visitor.level().isClientSide) return false;

        // Check stock
        if (shelf.getFoodStock() < 1) {
            visitor.playSound(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return false;
        }

        // Process payment & stock
        shelf.removeFoodStock(1);
        int income = (shelf.getFoodPrice() * TRANSACTION_PROFIT_MARGIN) / 100;
        ZooData.get(visitor.level()).addBalance(income);
        shelf.addRevenue(income);

        // Satisfy visitor
        visitor.setHunger(0);
        visitor.setMood(VisitorEntity.Mood.HAPPY, 100);
        visitor.setHasTrash(true);
        visitor.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

        return true;
    }

    /**
     * Process visitor purchase at drink shelf
     * @return true if transaction successful
     */
    public static boolean processDrinkPurchase(VisitorEntity visitor, ShelfBlockEntity shelf) {
        if (shelf == null || visitor.level().isClientSide) return false;

        // Check stock
        if (shelf.getDrinkStock() < 1) {
            visitor.playSound(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return false;
        }

        // Process payment & stock
        shelf.removeDrinkStock(1);
        int income = (shelf.getDrinkPrice() * TRANSACTION_PROFIT_MARGIN) / 100;
        ZooData.get(visitor.level()).addBalance(income);
        shelf.addRevenue(income);

        // Satisfy visitor
        visitor.setThirst(0);
        visitor.setMood(VisitorEntity.Mood.HAPPY, 100);
        visitor.setHasTrash(true);
        visitor.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

        return true;
    }

    /**
     * Find nearby shelf block
     */
    public static BlockPos findNearestShelf(VisitorEntity visitor, String type) {
        BlockPos visitorPos = visitor.blockPosition();
        
        for (BlockPos pos : BlockPos.betweenClosed(
                visitorPos.offset(-32, -5, -32), 
                visitorPos.offset(32, 5, 32))) {
            
            BlockState state = visitor.level().getBlockState(pos);
            
            if (type.equals("food") && state.is(IndoZooTycoon.FOOD_STALL_BLOCK.get())) {
                return pos.immutable();
            } else if (type.equals("drink") && state.is(IndoZooTycoon.DRINK_STALL_BLOCK.get())) {
                return pos.immutable();
            }
        }
        
        return null;
    }
}
