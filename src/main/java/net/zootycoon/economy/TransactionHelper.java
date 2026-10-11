package net.zootycoon.economy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TransactionHelper {

    public static int countEmeralds(Player player) {
        if (player.isCreative()) return 99999;
        int count = 0;
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.EMERALD)) {
                count += stack.getCount();
            } else if (stack.is(Items.EMERALD_BLOCK)) {
                count += stack.getCount() * 9;
            }
        }
        return count;
    }

    public static boolean hasEnoughEmeralds(Player player, int amount) {
        if (player.isCreative()) return true;
        return countEmeralds(player) >= amount;
    }

    public static synchronized boolean consumeEmeralds(Player player, int amount) {
        if (player.isCreative()) return true;
        if (!hasEnoughEmeralds(player, amount)) {
            return false;
        }

        int remaining = amount;
        int size = player.getInventory().getContainerSize();

        // 1. First consume loose emeralds
        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.EMERALD)) {
                int toTake = Math.min(stack.getCount(), remaining);
                stack.shrink(toTake);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                remaining -= toTake;
                if (remaining <= 0) break;
            }
        }

        // 2. If still needed, break emerald blocks and return change
        if (remaining > 0) {
            for (int i = 0; i < size; i++) {
                ItemStack stack = player.getInventory().getItem(i);
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
                                if (player instanceof ServerPlayer sp) {
                                    sp.drop(changeStack, false);
                                }
                            }
                        }
                    }
                    if (remaining <= 0) break;
                }
            }
        }

        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        return true;
    }

    public static void giveEmeralds(Player player, int amount) {
        int blocks = amount / 9;
        int singles = amount % 9;

        if (blocks > 0) {
            ItemStack blockStack = new ItemStack(Items.EMERALD_BLOCK, blocks);
            if (!player.getInventory().add(blockStack)) {
                if (player instanceof ServerPlayer sp) {
                    sp.drop(blockStack, false);
                }
            }
        }

        if (singles > 0) {
            ItemStack singleStack = new ItemStack(Items.EMERALD, singles);
            if (!player.getInventory().add(singleStack)) {
                if (player instanceof ServerPlayer sp) {
                    sp.drop(singleStack, false);
                }
            }
        }

        player.containerMenu.broadcastChanges();
    }
}

