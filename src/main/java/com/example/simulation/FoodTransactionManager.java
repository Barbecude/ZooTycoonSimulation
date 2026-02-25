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
        data.logTransaction("Pendapatan", "Penjualan makanan di shelf", income);

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
        data.logTransaction("Pendapatan", "Penjualan minuman di shelf", income);

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

    // ─────────────────────────────────────────────────────────────────────
    // Food Stall (new directional shop block)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Process a visitor buying one drink item (Chocolate Milk) from a Food Stall.
     * Revenue goes directly to the zoo balance (70 % of listed price).
     */
    public static boolean processDrinkStallPurchase(VisitorEntity visitor, FoodStallBlockEntity stall) {
        if (stall == null || visitor.level().isClientSide) return false;

        String requestId = stall.getRequestForVisitor(visitor.getUUID());
        if (requestId == null || requestId.isBlank()) requestId = "indozoo:fd_hot_cocoa";

        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(requestId);
        net.minecraft.world.item.Item requestItem = rl != null ? net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(rl) : null;
        net.minecraft.world.item.ItemStack peekItem = (requestItem != null && requestItem != net.minecraft.world.item.Items.AIR)
                ? new net.minecraft.world.item.ItemStack(requestItem, 1) : stall.getDisplayFood();

        boolean removed = stall.removeSpecificFood(requestId);
        if (!removed) {
            visitor.playSound(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return false;
        }

        int income = (stall.getItemPrice(requestId) * TRANSACTION_PROFIT_MARGIN) / 100;
        ZooData data = ZooData.get(visitor.level());
        data.addBalance(income);
        data.setRating(Math.min(100, data.getRating() + 1));
        stall.addRevenue(income);
        data.logTransaction("Pendapatan",
                "Penjualan Chocolate Milk di Food Stall @" + stall.getBlockPos().toShortString(), income);

        visitor.setThirst(0);
        visitor.setMood(VisitorEntity.Mood.HAPPY, 120);
        visitor.setHasTrash(true);
        visitor.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

        if (!peekItem.isEmpty()) {
            visitor.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, peekItem);
            visitor.eatTimer = 40 + visitor.getRandom().nextInt(80);
        }

        return true;
    }

    /**
     * Process a visitor buying one item from a Food Stall.
     * Revenue goes directly to the zoo balance (70 % of listed price).
     */
    public static boolean processFoodStallPurchase(VisitorEntity visitor, FoodStallBlockEntity stall) {
        if (stall == null || visitor.level().isClientSide) return false;

        if (stall.isStockEmpty()) {
            visitor.playSound(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return false;
        }

        // Peek at display item before removing (for visual hand-hold)
        // Use the visitor's specific request if available
        String requestId = stall.getRequestForVisitor(visitor.getUUID());
        net.minecraft.world.item.ItemStack peekItem;
        if (requestId != null && !requestId.isBlank()) {
            net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(requestId);
            net.minecraft.world.item.Item requestItem = rl != null ? net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(rl) : null;
            peekItem = (requestItem != null && requestItem != net.minecraft.world.item.Items.AIR)
                    ? new net.minecraft.world.item.ItemStack(requestItem, 1) : stall.getDisplayFood();
            stall.removeSpecificFood(requestId);
        } else {
            peekItem = stall.getDisplayFood();
            stall.removeOneFood();
        }

        int income = (stall.getItemPrice(requestId != null ? requestId : "") * TRANSACTION_PROFIT_MARGIN) / 100;
        ZooData data = ZooData.get(visitor.level());
        data.addBalance(income);
        data.setRating(Math.min(100, data.getRating() + 1));
        stall.addRevenue(income);
        data.logTransaction("Pendapatan",
                "Penjualan makanan di Food Stall @" + stall.getBlockPos().toShortString(), income);

        visitor.setHunger(0);
        visitor.setMood(VisitorEntity.Mood.HAPPY, 140);
        visitor.setHasTrash(true);
        visitor.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

        // Give visitor a held item so they visually eat/drink
        if (!peekItem.isEmpty()) {
            visitor.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, peekItem);
            visitor.eatTimer = 40 + visitor.getRandom().nextInt(80);
        }

        return true;
    }
}

