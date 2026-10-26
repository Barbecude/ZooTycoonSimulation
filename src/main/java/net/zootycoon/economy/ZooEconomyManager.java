package net.zootycoon.economy;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.network.ZooNetworkPayloads;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZooEconomyManager {
    private static ZooEconomyManager instance;

    private final MinecraftServer server;
    private final Map<UUID, Integer> balances = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastAbsorbTicks = new ConcurrentHashMap<>();

    private static int clientEmeraldBalance = 0;

    public static int getClientEmeraldBalance() {
        return clientEmeraldBalance;
    }

    public static void setClientEmeraldBalance(int balance) {
        clientEmeraldBalance = balance;
    }

    public ZooEconomyManager(MinecraftServer server) {
        this.server = server;
        loadFromFile();
    }

    public static ZooEconomyManager get(MinecraftServer server) {
        if (instance == null || instance.server != server) {
            instance = new ZooEconomyManager(server);
        }
        return instance;
    }

    public static synchronized void reset() {
        if (instance != null) {
            instance.lastAbsorbTicks.clear();
        }
        instance = null;
        clientEmeraldBalance = 0;
    }

    private Path getSavePath() {
        return server.getWorldPath(LevelResource.ROOT).resolve("zoo_economy.dat");
    }

    public void loadFromFile() {
        Path path = getSavePath();
        if (!Files.exists(path)) {
            return;
        }
        try {
            CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            CompoundTag balTag = tag.getCompound("Balances");
            balances.clear();
            for (String key : balTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    balances.put(uuid, balTag.getInt(key));
                } catch (Exception ignored) {}
            }
            ZooTycoonMod.LOGGER.info("Loaded {} player zoo economy balances.", balances.size());
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to load zoo economy data", e);
        }
    }

    public void saveToFile() {
        Path path = getSavePath();
        try {
            CompoundTag root = new CompoundTag();
            CompoundTag balTag = new CompoundTag();
            for (Map.Entry<UUID, Integer> entry : balances.entrySet()) {
                balTag.putInt(entry.getKey().toString(), entry.getValue());
            }
            root.put("Balances", balTag);
            NbtIo.writeCompressed(root, path);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to save zoo economy data", e);
        }
    }

    public int getBalance(UUID playerUuid) {
        if (playerUuid == null) return 0;
        return balances.getOrDefault(playerUuid, 0);
    }

    public void setBalance(UUID playerUuid, int amount) {
        if (playerUuid == null) return;
        balances.put(playerUuid, Math.max(0, amount));
        saveToFile();
    }

    public void addBalance(UUID playerUuid, int amount, ServerPlayer player) {
        if (playerUuid == null || amount <= 0) return;
        int current = getBalance(playerUuid);
        int newBalance = current + amount;
        balances.put(playerUuid, newBalance);
        saveToFile();
        if (player != null) {
            ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncEmeraldBalancePayload(newBalance, amount));
        }
    }

    public boolean deductBalance(UUID playerUuid, int amount, ServerPlayer player) {
        if (playerUuid == null || amount <= 0) return true;
        int current = getBalance(playerUuid);
        if (current < amount) {
            return false;
        }
        int newBalance = current - amount;
        balances.put(playerUuid, newBalance);
        saveToFile();
        if (player != null) {
            ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncEmeraldBalancePayload(newBalance, -amount));
        }
        return true;
    }

    public void syncToPlayer(ServerPlayer player) {
        if (player == null) return;
        UUID uuid = player.getUUID();
        // Give starter emerald balance on first join if not recorded
        if (!balances.containsKey(uuid)) {
            int starter = ZooEconomyEngine.getStarterEmeralds();
            balances.put(uuid, starter);
            saveToFile();
            ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncEmeraldBalancePayload(starter, starter));
            return;
        }
        int balance = getBalance(uuid);
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncEmeraldBalancePayload(balance, 0));
    }

    /**
     * Automatically absorbs any Emerald items or Emerald Blocks from player's inventory
     * directly into their registered Zoo Wallet balance!
     * Throttled to scan inventory only once every 20 ticks (1 second) per player.
     */
    public void absorbPlayerEmeralds(ServerPlayer player) {
        if (player == null || player.isCreative()) return;

        long currentTick = (this.server != null) ? this.server.getTickCount() : player.level().getGameTime();
        Long lastTick = lastAbsorbTicks.get(player.getUUID());
        if (lastTick != null && currentTick >= lastTick && (currentTick - lastTick < 20)) {
            return;
        }
        lastAbsorbTicks.put(player.getUUID(), currentTick);

        int gained = 0;
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.EMERALD)) {
                gained += stack.getCount();
                stack.setCount(0);
                player.getInventory().setItem(i, ItemStack.EMPTY);
            } else if (stack.is(Items.EMERALD_BLOCK)) {
                gained += stack.getCount() * 9;
                stack.setCount(0);
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
        if (gained > 0) {
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            addBalance(player.getUUID(), gained, player);
        }
    }
}

