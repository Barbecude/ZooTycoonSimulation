package net.zootycoon.economy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.client.ZooTycoonClient;

public class TransactionHelper {

    public static int countInventoryEmeralds(Player player) {
        if (player == null) return 0;
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.EMERALD)) {
                count += stack.getCount();
            } else if (stack.is(Items.EMERALD_BLOCK)) {
                count += stack.getCount() * 9;
            }
        }
        return count;
    }

    public static int countEmeralds(Player player) {
        if (player == null) return 0;
        if (player.isCreative()) return 99999;
        int virtual = 0;
        if (player instanceof ServerPlayer sp && sp.server != null) {
            virtual = ZooEconomyManager.get(sp.server).getBalance(sp.getUUID());
        } else {
            virtual = ZooTycoonClient.clientEmeraldBalance;
        }
        return virtual + countInventoryEmeralds(player);
    }

    public static boolean hasEnoughEmeralds(Player player, int amount) {
        if (player != null && player.isCreative()) return true;
        return countEmeralds(player) >= amount;
    }

    public static synchronized boolean consumeEmeralds(Player player, int amount) {
        if (player == null) return false;
        if (player.isCreative()) return true;
        if (!hasEnoughEmeralds(player, amount)) return false;

        if (player instanceof ServerPlayer sp && sp.server != null) {
            ZooEconomyManager eco = ZooEconomyManager.get(sp.server);
            int virtual = eco.getBalance(sp.getUUID());
            if (virtual >= amount) {
                return eco.deductBalance(sp.getUUID(), amount, sp);
            } else {
                int neededFromInv = amount - virtual;
                if (virtual > 0) {
                    eco.deductBalance(sp.getUUID(), virtual, sp);
                }
                // Deduct remaining from inventory
                int remaining = neededFromInv;
                for (int i = 0; i < sp.getInventory().getContainerSize() && remaining > 0; i++) {
                    ItemStack stack = sp.getInventory().getItem(i);
                    if (stack.is(Items.EMERALD)) {
                        int take = Math.min(remaining, stack.getCount());
                        stack.shrink(take);
                        remaining -= take;
                    }
                }
                // If still remaining, check emerald blocks
                if (remaining > 0) {
                    for (int i = 0; i < sp.getInventory().getContainerSize() && remaining > 0; i++) {
                        ItemStack stack = sp.getInventory().getItem(i);
                        if (stack.is(Items.EMERALD_BLOCK)) {
                            stack.shrink(1);
                            int change = 9 - remaining;
                            if (change > 0) {
                                ItemStack changeStack = new ItemStack(Items.EMERALD, change);
                                if (!sp.getInventory().add(changeStack)) {
                                    sp.drop(changeStack, false);
                                }
                            }
                            remaining = 0;
                            break;
                        }
                    }
                }
                return true;
            }
        }
        return countEmeralds(player) >= amount;
    }

    public static void giveEmeralds(Player player, int amount) {
        if (player == null || amount <= 0) return;
        if (player instanceof ServerPlayer sp && sp.server != null) {
            ZooEconomyManager.get(sp.server).addBalance(sp.getUUID(), amount, sp);
        }
    }
}
