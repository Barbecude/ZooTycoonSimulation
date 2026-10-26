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
        public int builders = 0; 
        public int zookeepers = 0;
        public int security = 0;
        public int cashiers = 0;

        public StaffRecord() {}

        public StaffRecord(int b, int z, int s, int c) {
            this.builders = Math.max(1, b);
            this.zookeepers = Math.max(0, z);
            this.security = Math.max(0, s);
            this.cashiers = Math.max(0, c);
        }
    }

    public static int ZOOKEEPER_DAILY_WAGE = ZooEconomyEngine.ZOOKEEPER_DAILY_WAGE;
    public static int SECURITY_DAILY_WAGE = ZooEconomyEngine.SECURITY_DAILY_WAGE;
    public static int CASHIER_DAILY_WAGE = ZooEconomyEngine.CASHIER_DAILY_WAGE;
    public static int BUILDER_DAILY_WAGE = ZooEconomyEngine.BUILDER_DAILY_WAGE;

    private static ZooStaffManager instance;
    private final Map<UUID, StaffRecord> staffMap = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> staffOnStrike = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, Long> lastSalaryDayMap = new ConcurrentHashMap<>();
    private MinecraftServer server;

    public static synchronized ZooStaffManager get(ServerLevel level) {
        if (instance == null || instance.server != level.getServer()) {
            instance = new ZooStaffManager();
            instance.server = level.getServer();
            instance.loadFromFile();
        }
        return instance;
    }

    public static synchronized ZooStaffManager get(MinecraftServer s) {
        if (instance == null || instance.server != s) {
            instance = new ZooStaffManager();
            instance.server = s;
            instance.loadFromFile();
        }
        return instance;
    }

    public static synchronized void reset() {
        if (instance != null) {
            instance.staffMap.clear();
            instance.staffOnStrike.clear();
            instance.lastSalaryDayMap.clear();
            instance.server = null;
            instance = null;
        }
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
                            int c = tag.getInt("cashiers");
                            staffMap.put(uuid, new StaffRecord(b, z, s, c));
                        } catch (Exception ignored) {}
                    }
                }
            }
            if (root.contains("staff_on_strike", Tag.TAG_LIST)) {
                ListTag strikeList = root.getList("staff_on_strike", Tag.TAG_COMPOUND);
                staffOnStrike.clear();
                for (int i = 0; i < strikeList.size(); i++) {
                    staffOnStrike.add(strikeList.getCompound(i).getUUID("player"));
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
                tag.putInt("cashiers", entry.getValue().cashiers);
                list.add(tag);
            }
            root.put("staff_records", list);

            ListTag strikeList = new ListTag();
            for (UUID id : staffOnStrike) {
                CompoundTag ct = new CompoundTag();
                ct.putUUID("player", id);
                strikeList.add(ct);
            }
            root.put("staff_on_strike", strikeList);

            NbtIo.writeCompressed(root, path);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to save staff records", e);
        }
    }

    public boolean areStaffOnStrike(UUID playerUuid) {
        if (playerUuid == null) return false;
        return staffOnStrike.contains(playerUuid);
    }

    public void setStaffOnStrike(UUID playerUuid, boolean strike) {
        if (playerUuid == null) return;
        if (strike) {
            staffOnStrike.add(playerUuid);
        } else {
            staffOnStrike.remove(playerUuid);
        }
        saveToFile();
    }

    public int calculateDailyWage(UUID playerUuid) {
        StaffRecord rec = getRecord(playerUuid);
        return ZooEconomyEngine.calculateDailyWages(rec.zookeepers, rec.security, rec.cashiers);
    }

    public int getTotalStaffCount(UUID playerUuid) {
        StaffRecord rec = getRecord(playerUuid);
        return rec.builders + rec.zookeepers + rec.security + rec.cashiers;
    }

    public void processDailyWages(ServerLevel level) {
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 1000L || dayTime > 1050L) {
            return;
        }

        long currentDay = level.getDayTime() / 24000L;
        ZooEconomyManager eco = ZooEconomyManager.get(level.getServer());

        for (var entry : staffMap.entrySet()) {
            UUID playerUuid = entry.getKey();
            Long lastDay = lastSalaryDayMap.get(playerUuid);
            if (lastDay != null && lastDay == currentDay) {
                continue;
            }
            lastSalaryDayMap.put(playerUuid, currentDay);

            int totalWage = calculateDailyWage(playerUuid);
            int staffCount = getTotalStaffCount(playerUuid);
            if (totalWage <= 0) {
                setStaffOnStrike(playerUuid, false);
                continue;
            }

            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUuid);
            int currentBal = eco.getBalance(playerUuid);

            if (currentBal >= totalWage) {
                eco.deductBalance(playerUuid, totalWage, player);
                setStaffOnStrike(playerUuid, false);
                if (player != null) {
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.toast.salary.paid", 0, String.valueOf(totalWage), String.valueOf(staffCount));
                }
            } else {
                setStaffOnStrike(playerUuid, true);
                if (player != null) {
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.toast.salary.unpaid", 2);
                }
            }
        }
    }

    public void checkStrikeResolutionOnDeposit(UUID playerUuid, ServerPlayer player) {
        if (playerUuid == null || !areStaffOnStrike(playerUuid)) {
            return;
        }
        int totalWage = calculateDailyWage(playerUuid);
        if (totalWage <= 0) {
            setStaffOnStrike(playerUuid, false);
            return;
        }
        if (server == null) return;
        ZooEconomyManager eco = ZooEconomyManager.get(server);
        int currentBal = eco.getBalance(playerUuid);
        if (currentBal >= totalWage) {
            eco.deductBalance(playerUuid, totalWage, player);
            setStaffOnStrike(playerUuid, false);
            if (player != null) {
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.toast.salary.resolved", 1);
            }
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

    public int getCashierCount(UUID playerUuid) {
        return getRecord(playerUuid).cashiers;
    }

    public boolean hireStaff(ServerPlayer player, String type) {
        StaffRecord rec = getRecord(player.getUUID());
        int cost = ZooEconomyEngine.getStaffHiringCost(type);

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
            case "cashier", "kasir" -> {
                rec.cashiers++;
                var cashier = ZooEntities.CASHIER.create(sLevel);
                if (cashier != null) {
                    cashier.setPos(player.getX() + 0.5, player.getY(), player.getZ() + 0.5);
                    cashier.setFacingDirection(player.getDirection().getOpposite());
                    sLevel.addFreshEntity(cashier);
                    sLevel.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            cashier.getX(), cashier.getY() + 1.0, cashier.getZ(), 15, 0.4, 0.5, 0.4, 0.02
                    );
                    sLevel.playSound(null, cashier.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);
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
                rec.builders, activeBuilders, rec.zookeepers, rec.security, rec.cashiers
        ));
    }
}
