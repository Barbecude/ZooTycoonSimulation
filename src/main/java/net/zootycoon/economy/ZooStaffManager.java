package net.zootycoon.economy;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.storage.LevelResource;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.construction.ConstructionManager;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.network.ZooNetworkPayloads;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZooStaffManager {
    public static class StaffRecord {
        public int builders = 1; // 1 free starter builder for every player
        public int zookeepers = 0;
        public int security = 0;

        public StaffRecord() {}

        public StaffRecord(int b, int z, int s) {
            this.builders = Math.max(1, b);
            this.zookeepers = Math.max(0, z);
            this.security = Math.max(0, s);
        }
    }

    private static ZooStaffManager instance;
    private final Map<UUID, StaffRecord> staffMap = new ConcurrentHashMap<>();
    private MinecraftServer server;

    public static synchronized ZooStaffManager get(ServerLevel level) {
        if (instance == null) {
            instance = new ZooStaffManager();
            instance.server = level.getServer();
            instance.loadFromFile();
        }
        return instance;
    }

    public static synchronized ZooStaffManager get(MinecraftServer s) {
        if (instance == null) {
            instance = new ZooStaffManager();
            instance.server = s;
            instance.loadFromFile();
        }
        return instance;
    }

    private Path getSavePath() {
        return server.getWorldPath(LevelResource.ROOT).resolve("zoo_staff.dat");
    }

    public void loadFromFile() {
        if (server == null) return;
        Path path = getSavePath();
        if (!Files.exists(path)) return;

        try {
            CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            ListTag list = root.getList("staff_records", Tag.TAG_COMPOUND);
            staffMap.clear();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                if (tag != null) {
                    String uStr = tag.getString("player_uuid");
                    if (!uStr.isEmpty()) {
                        try {
                            UUID uuid = UUID.fromString(uStr);
                            int b = tag.contains("builders") ? tag.getInt("builders") : 1;
                            int z = tag.getInt("zookeepers");
                            int s = tag.contains("security") ? tag.getInt("security") : (tag.contains("veterinarians") ? tag.getInt("veterinarians") : 0);
                            staffMap.put(uuid, new StaffRecord(b, z, s));
                        } catch (Exception ignored) {}
                    }
                }
            }
            ZooTycoonMod.LOGGER.info("Loaded {} staff records from file.", staffMap.size());
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to load staff records", e);
        }
    }

    public void saveToFile() {
        if (server == null) return;
        Path path = getSavePath();
        try {
            CompoundTag root = new CompoundTag();
            ListTag list = new ListTag();
            for (var entry : staffMap.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("player_uuid", entry.getKey().toString());
                tag.putInt("builders", entry.getValue().builders);
                tag.putInt("zookeepers", entry.getValue().zookeepers);
                tag.putInt("security", entry.getValue().security);
                list.add(tag);
            }
            root.put("staff_records", list);
            NbtIo.writeCompressed(root, path);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to save staff records", e);
        }
    }

    public StaffRecord getRecord(UUID playerUuid) {
        return staffMap.computeIfAbsent(playerUuid, u -> new StaffRecord());
    }

    public int getBuilderCount(UUID playerUuid) {
        return getRecord(playerUuid).builders;
    }

    public int getZookeeperCount(UUID playerUuid) {
        return getRecord(playerUuid).zookeepers;
    }

    public int getSecurityCount(UUID playerUuid) {
        return getRecord(playerUuid).security;
    }

    public boolean hireStaff(ServerPlayer player, String type) {
        StaffRecord rec = getRecord(player.getUUID());
        int cost = switch (type.toLowerCase()) {
            case "builder", "kuli" -> 15;
            case "zookeeper" -> 25;
            case "security", "satpam" -> 30;
            default -> 0;
        };

        if (cost <= 0) return false;

        if (!player.isCreative()) {
            if (!TransactionHelper.hasEnoughEmeralds(player, cost)) {
                return false;
            }
            if (!TransactionHelper.consumeEmeralds(player, cost)) {
                return false;
            }
        }

        ServerLevel sLevel = (ServerLevel) player.level();
        switch (type.toLowerCase()) {
            case "builder", "kuli" -> rec.builders++;
            case "zookeeper" -> {
                rec.zookeepers++;
                var zk = ZooEntities.ZOOKEEPER.create(sLevel);
                if (zk != null) {
                    zk.setPos(player.getX() + 0.5, player.getY(), player.getZ() + 0.5);
                    zk.setOwnerUuid(player.getUUID());
                    sLevel.addFreshEntity(zk);
                    sLevel.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            zk.getX(), zk.getY() + 1.0, zk.getZ(), 15, 0.4, 0.5, 0.4, 0.02
                    );
                    sLevel.playSound(null, zk.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);
                }
            }
            case "security", "satpam" -> {
                rec.security++;
                var sec = ZooEntities.SECURITY.create(sLevel);
                if (sec != null) {
                    sec.setPos(player.getX() + 0.5, player.getY(), player.getZ() + 0.5);
                    sec.setOwnerUuid(player.getUUID());
                    sLevel.addFreshEntity(sec);
                    sLevel.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            sec.getX(), sec.getY() + 1.0, sec.getZ(), 15, 0.4, 0.5, 0.4, 0.02
                    );
                    sLevel.playSound(null, sec.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                }
            }
        }

        saveToFile();
        syncToPlayer(player);
        return true;
    }

    public void syncToPlayer(ServerPlayer player) {
        StaffRecord rec = getRecord(player.getUUID());
        int activeBuilders = ConstructionManager.getActiveSitesForPlayer(player.getUUID()).size();
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStaffPayload(
                rec.builders, activeBuilders, rec.zookeepers, rec.security
        ));
    }
}
