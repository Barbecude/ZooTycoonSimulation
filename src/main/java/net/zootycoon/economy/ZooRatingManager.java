package net.zootycoon.economy;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.network.ZooNetworkPayloads;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZooRatingManager {
    private static ZooRatingManager instance;

    private final MinecraftServer server;
    private final Map<UUID, Integer> ratings = new ConcurrentHashMap<>();

    public static final int DEFAULT_RATING = 50;
    public static final int MIN_RATING = 0;
    public static final int MAX_RATING = 100;

    public ZooRatingManager(MinecraftServer server) {
        this.server = server;
        loadFromFile();
    }

    public static ZooRatingManager get(MinecraftServer server) {
        if (instance == null || instance.server != server) {
            instance = new ZooRatingManager(server);
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    private Path getSavePath() {
        return server.getWorldPath(LevelResource.ROOT).resolve("zoo_ratings.dat");
    }

    public void loadFromFile() {
        Path path = getSavePath();
        if (!Files.exists(path)) {
            return;
        }
        try {
            CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            CompoundTag rateTag = tag.getCompound("Ratings");
            ratings.clear();
            for (String key : rateTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    ratings.put(uuid, rateTag.getInt(key));
                } catch (Exception ignored) {}
            }
            ZooTycoonMod.LOGGER.info("Loaded {} player zoo ratings.", ratings.size());
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to load zoo rating data", e);
        }
    }

    public void saveToFile() {
        Path path = getSavePath();
        try {
            CompoundTag root = new CompoundTag();
            CompoundTag rateTag = new CompoundTag();
            for (Map.Entry<UUID, Integer> entry : ratings.entrySet()) {
                rateTag.putInt(entry.getKey().toString(), entry.getValue());
            }
            root.put("Ratings", rateTag);
            NbtIo.writeCompressed(root, path);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to save zoo rating data", e);
        }
    }

    public int getRating(UUID playerUuid) {
        if (playerUuid == null) return DEFAULT_RATING;
        return ratings.getOrDefault(playerUuid, DEFAULT_RATING);
    }

    public void setRating(UUID playerUuid, int rating, ServerPlayer player) {
        if (playerUuid == null) return;
        int clamped = Math.clamp(rating, MIN_RATING, MAX_RATING);
        ratings.put(playerUuid, clamped);
        saveToFile();
        syncToPlayer(player);
    }

    public int addRating(UUID playerUuid, int amount, ServerPlayer player) {
        if (playerUuid == null || amount <= 0) return getRating(playerUuid);
        int current = getRating(playerUuid);
        int newRating = Math.clamp(current + amount, MIN_RATING, MAX_RATING);
        ratings.put(playerUuid, newRating);
        saveToFile();
        syncToPlayer(player);
        return newRating;
    }

    public int deductRating(UUID playerUuid, int amount, ServerPlayer player) {
        if (playerUuid == null || amount <= 0) return getRating(playerUuid);
        int current = getRating(playerUuid);
        int newRating = Math.clamp(current - amount, MIN_RATING, MAX_RATING);
        ratings.put(playerUuid, newRating);
        saveToFile();
        syncToPlayer(player);
        return newRating;
    }

    public void syncToPlayer(ServerPlayer player) {
        if (player == null) return;
        int rating = getRating(player.getUUID());
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncRatingPayload(rating));
    }
}

