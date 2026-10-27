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
    private final Map<UUID, Double> reviewStarsSum = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> reviewCounts = new ConcurrentHashMap<>();

    public static final int DEFAULT_RATING = 100;
    public static final int MIN_RATING = 20;
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
            CompoundTag sumTag = tag.getCompound("ReviewSums");
            CompoundTag countTag = tag.getCompound("ReviewCounts");

            ratings.clear();
            reviewStarsSum.clear();
            reviewCounts.clear();

            for (String key : rateTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int r = rateTag.getInt(key);
                    ratings.put(uuid, r);
                    if (countTag.contains(key)) {
                        reviewCounts.put(uuid, countTag.getInt(key));
                        reviewStarsSum.put(uuid, sumTag.getDouble(key));
                    } else {
                        // Migration for old saves: initialize 10 baseline reviews
                        int initialCount = 10;
                        double initialStar = Math.clamp(r / 20.0D, 1.0D, 5.0D);
                        reviewCounts.put(uuid, initialCount);
                        reviewStarsSum.put(uuid, initialStar * (double) initialCount);
                    }
                } catch (Exception ignored) {}
            }
            ZooTycoonMod.LOGGER.info("Loaded {} player zoo reviews & ratings.", reviewCounts.size());
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to load zoo rating data", e);
        }
    }

    public void saveToFile() {
        Path path = getSavePath();
        try {
            CompoundTag root = new CompoundTag();
            CompoundTag rateTag = new CompoundTag();
            CompoundTag sumTag = new CompoundTag();
            CompoundTag countTag = new CompoundTag();

            for (Map.Entry<UUID, Integer> entry : ratings.entrySet()) {
                rateTag.putInt(entry.getKey().toString(), entry.getValue());
            }
            for (Map.Entry<UUID, Double> entry : reviewStarsSum.entrySet()) {
                sumTag.putDouble(entry.getKey().toString(), entry.getValue());
            }
            for (Map.Entry<UUID, Integer> entry : reviewCounts.entrySet()) {
                countTag.putInt(entry.getKey().toString(), entry.getValue());
            }

            root.put("Ratings", rateTag);
            root.put("ReviewSums", sumTag);
            root.put("ReviewCounts", countTag);
            NbtIo.writeCompressed(root, path);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to save zoo rating data", e);
        }
    }

    public int getRating(UUID playerUuid) {
        if (playerUuid == null) return DEFAULT_RATING;
        return (int) Math.round(getStarRating(playerUuid) * 20.0D);
    }

    public double getStarRating(UUID playerUuid) {
        if (playerUuid == null) return 5.0D;
        int count = reviewCounts.getOrDefault(playerUuid, 0);
        if (count <= 0) {
            if (ratings.containsKey(playerUuid)) {
                return Math.clamp(ratings.get(playerUuid) / 20.0D, 1.0D, 5.0D);
            }
            return 5.0D;
        }
        double sum = reviewStarsSum.getOrDefault(playerUuid, (double) count * 5.0D);
        double avg = sum / (double) count;
        return Math.clamp(Math.round(avg * 10.0D) / 10.0D, 1.0D, 5.0D);
    }

    public int getReviewCount(UUID playerUuid) {
        if (playerUuid == null) return 0;
        return reviewCounts.getOrDefault(playerUuid, 0);
    }

    public void addReview(UUID playerUuid, double stars, ServerPlayer player) {
        if (playerUuid == null) return;
        double clampedStars = Math.clamp(stars, 1.0D, 5.0D);
        reviewStarsSum.merge(playerUuid, clampedStars, Double::sum);
        reviewCounts.merge(playerUuid, 1, Integer::sum);

        double newStar = getStarRating(playerUuid);
        ratings.put(playerUuid, (int) Math.round(newStar * 20.0D));
        saveToFile();
        syncToPlayer(player);
    }

    public void setStarRating(UUID playerUuid, double starRating, ServerPlayer player) {
        if (playerUuid == null) return;
        double clamped = Math.clamp(starRating, 1.0D, 5.0D);
        int baselineCount = Math.max(10, reviewCounts.getOrDefault(playerUuid, 10));
        reviewCounts.put(playerUuid, baselineCount);
        reviewStarsSum.put(playerUuid, clamped * baselineCount);
        ratings.put(playerUuid, (int) Math.round(clamped * 20.0D));
        saveToFile();
        syncToPlayer(player);
    }

    public void setRating(UUID playerUuid, int rating, ServerPlayer player) {
        if (playerUuid == null) return;
        int clamped = Math.clamp(rating, MIN_RATING, MAX_RATING);
        setStarRating(playerUuid, clamped / 20.0D, player);
    }

    public int addRating(UUID playerUuid, int amount, ServerPlayer player) {
        if (playerUuid == null || amount <= 0) return getRating(playerUuid);
        addReview(playerUuid, 5.0D, player);
        return getRating(playerUuid);
    }

    public int deductRating(UUID playerUuid, int amount, ServerPlayer player) {
        if (playerUuid == null || amount <= 0) return getRating(playerUuid);
        addReview(playerUuid, 1.0D, player);
        return getRating(playerUuid);
    }

    public void syncToPlayer(ServerPlayer player) {
        if (player == null) return;
        int rating = getRating(player.getUUID());
        int reviews = getReviewCount(player.getUUID());
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncRatingPayload(rating, reviews));
    }
}

