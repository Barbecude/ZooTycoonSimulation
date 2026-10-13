package net.zootycoon.economy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.zootycoon.client.ZooTycoonClient;

public class TransactionHelper {

    public static int countEmeralds(Player player) {
        if (player == null) return 0;
        if (player.isCreative()) return 99999;
        if (player instanceof ServerPlayer sp && sp.server != null) {
            return ZooEconomyManager.get(sp.server).getBalance(sp.getUUID());
        }
        return ZooTycoonClient.clientEmeraldBalance;
    }

    public static boolean hasEnoughEmeralds(Player player, int amount) {
        if (player != null && player.isCreative()) return true;
        return countEmeralds(player) >= amount;
    }

    public static synchronized boolean consumeEmeralds(Player player, int amount) {
        if (player == null) return false;
        if (player.isCreative()) return true;
        if (player instanceof ServerPlayer sp && sp.server != null) {
            return ZooEconomyManager.get(sp.server).deductBalance(sp.getUUID(), amount, sp);
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
