package net.zootycoon.economy;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TransactionHelper {

    public static int countEmeralds(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.EMERALD)) {
                count += stack.getCount();
            } else if (stack.is(Items.EMERALD_BLOCK)) {
                count += stack.getCount() * 9;
            }
        }
        return count;
    }

    public static boolean hasEnoughEmeralds(Player player, int amount) {
        return countEmeralds(player) >= amount;
    }

    public static synchronized boolean consumeEmeralds(Player player, int amount) {
        if (!hasEnoughEmeralds(player, amount)) {
            return false;
        }

        int remaining = amount;

        // 1. First consume loose emeralds
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.is(Items.EMERALD)) {
                int toTake = Math.min(stack.getCount(), remaining);
                stack.shrink(toTake);
                remaining -= toTake;
                if (remaining <= 0) break;
            }
        }

        // 2. If still needed, break emerald blocks and return change
        if (remaining > 0) {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack stack = player.getInventory().items.get(i);
                if (stack.is(Items.EMERALD_BLOCK)) {
                    while (stack.getCount() > 0 && remaining > 0) {
                        stack.shrink(1);
                        int value = 9;
                        int used = Math.min(value, remaining);
                        remaining -= used;
                        int change = value - used;

                        if (change > 0) {
                            ItemStack changeStack = new ItemStack(Items.EMERALD, change);
                            if (!player.getInventory().add(changeStack)) {
                                player.drop(changeStack, false);
                            }
                        }
                    }
                    if (remaining <= 0) break;
                }
            }
        }

        player.containerMenu.broadcastChanges();
        return true;
    }

    public static void giveEmeralds(Player player, int amount) {
        int blocks = amount / 9;
        int singles = amount % 9;

        if (blocks > 0) {
            ItemStack blockStack = new ItemStack(Items.EMERALD_BLOCK, blocks);
            if (!player.getInventory().add(blockStack)) {
                player.drop(blockStack, false);
            }
        }

        if (singles > 0) {
            ItemStack singleStack = new ItemStack(Items.EMERALD, singles);
            if (!player.getInventory().add(singleStack)) {
                player.drop(singleStack, false);
            }
        }

        player.containerMenu.broadcastChanges();
    }
}

