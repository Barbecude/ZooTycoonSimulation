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
import java.util.ArrayList;
import java.util.List;
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

    public static class StaffWageRecord {
        public UUID staffUuid;
        public UUID ownerUuid;
        public String staffType;
        public String staffName;
        public int dailyWage;
        public int missedDays;
        public int owedAmount;
        public boolean resigned;

        public StaffWageRecord(UUID staffUuid, UUID ownerUuid, String staffType, String staffName,
                               int dailyWage, int missedDays, int owedAmount, boolean resigned) {
            this.staffUuid = staffUuid;
            this.ownerUuid = ownerUuid;
            this.staffType = staffType;
            this.staffName = staffName;
            this.dailyWage = dailyWage;
            this.missedDays = missedDays;
            this.owedAmount = owedAmount;
            this.resigned = resigned;
        }
    }

    public static int ZOOKEEPER_DAILY_WAGE = ZooEconomyEngine.ZOOKEEPER_DAILY_WAGE;
    public static int SECURITY_DAILY_WAGE = ZooEconomyEngine.SECURITY_DAILY_WAGE;
    public static int CASHIER_DAILY_WAGE = ZooEconomyEngine.CASHIER_DAILY_WAGE;
    public static int BUILDER_DAILY_WAGE = ZooEconomyEngine.BUILDER_DAILY_WAGE;

    private static ZooStaffManager instance;
    private final Map<UUID, StaffRecord> staffMap = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> staffOnStrike = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, Long> ownerStrikeEpoch = new ConcurrentHashMap<>();
    private final Map<UUID, Long> staffResolvedEpoch = new ConcurrentHashMap<>();
    private long strikeEpochCounter = 0L;
    private final Map<UUID, Long> lastSalaryDayMap = new ConcurrentHashMap<>();
    private final Map<UUID, StaffWageRecord> staffWages = new ConcurrentHashMap<>();
    private final Map<UUID, PeekSession> activePeeks = new ConcurrentHashMap<>();
    private MinecraftServer server;

    private record PeekSession(String staffType, UUID staffUuid) {}

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
            instance.ownerStrikeEpoch.clear();
            instance.staffResolvedEpoch.clear();
            instance.strikeEpochCounter = 0L;
            instance.lastSalaryDayMap.clear();
            instance.staffWages.clear();
            instance.activePeeks.clear();
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
            ownerStrikeEpoch.clear();
            staffResolvedEpoch.clear();
            strikeEpochCounter = root.contains("strike_epoch_counter") ? root.getLong("strike_epoch_counter") : 0L;
            if (root.contains("owner_strike_epochs", Tag.TAG_LIST)) {
                ListTag epList = root.getList("owner_strike_epochs", Tag.TAG_COMPOUND);
                for (int i = 0; i < epList.size(); i++) {
                    CompoundTag ct = epList.getCompound(i);
                    if (ct.hasUUID("owner")) {
                        long ep = ct.getLong("epoch");
                        ownerStrikeEpoch.put(ct.getUUID("owner"), ep);
                        strikeEpochCounter = Math.max(strikeEpochCounter, ep);
                    }
                }
            }
            if (root.contains("staff_resolved_epochs", Tag.TAG_LIST)) {
                ListTag resList = root.getList("staff_resolved_epochs", Tag.TAG_COMPOUND);
                for (int i = 0; i < resList.size(); i++) {
                    CompoundTag ct = resList.getCompound(i);
                    if (ct.hasUUID("staff")) {
                        staffResolvedEpoch.put(ct.getUUID("staff"), ct.getLong("epoch"));
                    }
                }
            }
            if (root.contains("staff_on_strike", Tag.TAG_LIST)) {
                ListTag strikeList = root.getList("staff_on_strike", Tag.TAG_COMPOUND);
                staffOnStrike.clear();
                for (int i = 0; i < strikeList.size(); i++) {
                    UUID ownerId = strikeList.getCompound(i).getUUID("player");
                    staffOnStrike.add(ownerId);
                    if (!ownerStrikeEpoch.containsKey(ownerId)) {
                        strikeEpochCounter = Math.max(1L, strikeEpochCounter + 1L);
                        ownerStrikeEpoch.put(ownerId, strikeEpochCounter);
                    }
                }
            }
            staffWages.clear();
            if (root.contains("staff_wages", Tag.TAG_LIST)) {
                ListTag wageList = root.getList("staff_wages", Tag.TAG_COMPOUND);
                for (int i = 0; i < wageList.size(); i++) {
                    CompoundTag wt = wageList.getCompound(i);
                    if (wt.hasUUID("staff_uuid") && wt.hasUUID("owner_uuid")) {
                        UUID sId = wt.getUUID("staff_uuid");
                        UUID oId = wt.getUUID("owner_uuid");
                        String st = wt.getString("staff_type");
                        String sn = wt.getString("staff_name");
                        int dw = wt.contains("daily_wage") ? wt.getInt("daily_wage") : getDefaultDailyWageForType(st);
                        int md = wt.getInt("missed_days");
                        int oa = wt.getInt("owed_amount");
                        boolean res = wt.getBoolean("resigned");
                        staffWages.put(sId, new StaffWageRecord(sId, oId, st, sn, dw, md, oa, res));
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

            root.putLong("strike_epoch_counter", strikeEpochCounter);
            ListTag epList = new ListTag();
            for (var epEntry : ownerStrikeEpoch.entrySet()) {
                CompoundTag ct = new CompoundTag();
                ct.putUUID("owner", epEntry.getKey());
                ct.putLong("epoch", epEntry.getValue());
                epList.add(ct);
            }
            root.put("owner_strike_epochs", epList);

            ListTag resList = new ListTag();
            for (var resEntry : staffResolvedEpoch.entrySet()) {
                CompoundTag ct = new CompoundTag();
                ct.putUUID("staff", resEntry.getKey());
                ct.putLong("epoch", resEntry.getValue());
                resList.add(ct);
            }
            root.put("staff_resolved_epochs", resList);

            ListTag wageList = new ListTag();
            for (StaffWageRecord w : staffWages.values()) {
                CompoundTag wt = new CompoundTag();
                wt.putUUID("staff_uuid", w.staffUuid);
                wt.putUUID("owner_uuid", w.ownerUuid);
                wt.putString("staff_type", w.staffType != null ? w.staffType : "");
                wt.putString("staff_name", w.staffName != null ? w.staffName : "");
                wt.putInt("daily_wage", w.dailyWage);
                wt.putInt("missed_days", w.missedDays);
                wt.putInt("owed_amount", w.owedAmount);
                wt.putBoolean("resigned", w.resigned);
                wageList.add(wt);
            }
            root.put("staff_wages", wageList);

            NbtIo.writeCompressed(root, path);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to save staff records", e);
        }
    }

    /** Returns true if this specific staff entity has an unresolved wage strike for its owner. */
    public boolean isStaffEntityOnStrike(UUID ownerUuid, UUID staffUuid) {
        if (staffUuid == null) return false;
        StaffWageRecord w = staffWages.get(staffUuid);
        if (w != null && !w.resigned && w.missedDays >= 3) {
            return true;
        }
        if (ownerUuid == null) return false;
        long activeEpoch = ownerStrikeEpoch.getOrDefault(ownerUuid, 0L);
        if (activeEpoch <= 0L) {
            return staffOnStrike.contains(ownerUuid);
        }
        return staffResolvedEpoch.getOrDefault(staffUuid, 0L) < activeEpoch;
    }

    public int getDefaultDailyWageForType(String staffType) {
        if (staffType == null) return 0;
        return switch (staffType.toLowerCase(java.util.Locale.ROOT)) {
            case "zookeeper" -> ZooEconomyEngine.ZOOKEEPER_DAILY_WAGE;
            case "security", "satpam" -> ZooEconomyEngine.SECURITY_DAILY_WAGE;
            case "cashier", "kasir" -> ZooEconomyEngine.CASHIER_DAILY_WAGE;
            default -> 0;
        };
    }

    public StaffWageRecord ensureActiveWageRecord(UUID staffUuid, UUID ownerUuid, String staffType, String staffName) {
        if (staffUuid == null || ownerUuid == null) return null;
        int wage = getDefaultDailyWageForType(staffType);
        StaffWageRecord rec = staffWages.computeIfAbsent(staffUuid, id -> new StaffWageRecord(
                id, ownerUuid, staffType, staffName != null ? staffName : "", wage, 0, 0, false
        ));
        if (!rec.resigned) {
            rec.ownerUuid = ownerUuid;
            rec.staffType = staffType;
            if (staffName != null && !staffName.isBlank()) {
                rec.staffName = staffName;
            }
            if (rec.dailyWage <= 0 && wage > 0) {
                rec.dailyWage = wage;
            }
        }
        return rec;
    }

    public StaffWageRecord getWageRecord(UUID staffUuid) {
        return staffUuid != null ? staffWages.get(staffUuid) : null;
    }

    public int getStaffDelayTier(UUID staffUuid) {
        StaffWageRecord w = getWageRecord(staffUuid);
        return w != null ? Math.max(0, w.missedDays) : 0;
    }

    public List<StaffWageRecord> getResignedWageRecords(UUID ownerUuid, String staffType) {
        List<StaffWageRecord> result = new ArrayList<>();
        if (ownerUuid == null) return result;
        for (StaffWageRecord w : staffWages.values()) {
            if (w.resigned && w.owedAmount > 0 && ownerUuid.equals(w.ownerUuid)) {
                if (staffType == null || staffType.equalsIgnoreCase(w.staffType)) {
                    result.add(w);
                }
            }
        }
        return result;
    }

    /** Marks a single staff entity as having its strike wage paid via dialog. */
    public void resolveStaffEntityStrike(UUID ownerUuid, UUID staffUuid) {
        if (ownerUuid == null || staffUuid == null) return;
        long activeEpoch = ownerStrikeEpoch.getOrDefault(ownerUuid, 0L);
        if (activeEpoch <= 0L && staffOnStrike.contains(ownerUuid)) {
            activeEpoch = ++strikeEpochCounter;
            ownerStrikeEpoch.put(ownerUuid, activeEpoch);
        }
        if (activeEpoch > 0L) {
            staffResolvedEpoch.put(staffUuid, activeEpoch);
        }
        saveToFile();
    }

    private void markNewlyHiredStaffResolved(UUID ownerUuid, UUID staffUuid) {
        if (ownerUuid == null || staffUuid == null) return;
        long activeEpoch = ownerStrikeEpoch.getOrDefault(ownerUuid, 0L);
        if (activeEpoch > 0L) {
            staffResolvedEpoch.put(staffUuid, activeEpoch);
        }
    }

    /** Returns true for staff roles that earn daily wages and can go on strike (excludes Builders). */
    public static boolean canStaffRoleStrike(String roleOrType) {
        if (roleOrType == null) return false;
        String lower = roleOrType.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("builder") || lower.contains("kuli")) {
            return false;
        }
        return lower.contains("zookeeper")
                || lower.contains("security") || lower.contains("satpam")
                || lower.contains("cashier") || lower.contains("kasir");
    }

    private boolean isOwnedStrikeEligibleStaffEntity(net.minecraft.world.entity.Entity ent, UUID ownerUuid) {
        if (ent == null || !ent.isAlive()) return false;
        if (ent instanceof net.zootycoon.entity.ZooZookeeperEntity zk) {
            return ownerUuid.equals(zk.getOwnerUuid());
        } else if (ent instanceof net.zootycoon.entity.ZooSecurityEntity sec) {
            return ownerUuid.equals(sec.getOwnerUuid());
        } else if (ent instanceof net.zootycoon.entity.ZooCashierEntity cash) {
            return ownerUuid.equals(cash.getOwnerUuid());
        }
        return false;
    }

    private String getStaffRoleLabel(net.minecraft.world.entity.Entity ent) {
        if (ent instanceof net.zootycoon.entity.ZooZookeeperEntity) return "Zookeeper";
        if (ent instanceof net.zootycoon.entity.ZooSecurityEntity) return "Security";
        if (ent instanceof net.zootycoon.entity.ZooCashierEntity) return "Kasir";
        if (ent instanceof net.zootycoon.entity.ZooBuilderEntity) return "Kuli";
        return "Staff";
    }

    /** Returns formatted "Role: Name" labels for every currently loaded staff entity on strike for {@code ownerUuid}. */
    public List<String> getStrikingStaffLabels(UUID ownerUuid) {
        List<String> labels = new ArrayList<>();
        if (ownerUuid == null || server == null) return labels;
        for (ServerLevel lvl : server.getAllLevels()) {
            for (net.minecraft.world.entity.Entity ent : lvl.getAllEntities()) {
                if (isOwnedStrikeEligibleStaffEntity(ent, ownerUuid) && isStaffEntityOnStrike(ownerUuid, ent.getUUID())) {
                    String name = ent.hasCustomName() ? ent.getCustomName().getString() : ent.getName().getString();
                    labels.add(getStaffRoleLabel(ent) + ": " + name);
                }
            }
        }
        return labels;
    }

    public boolean areStaffOnStrike(UUID playerUuid) {
        if (playerUuid == null) return false;
        for (StaffWageRecord w : staffWages.values()) {
            if (!w.resigned && w.missedDays >= 3 && playerUuid.equals(w.ownerUuid)) {
                return true;
            }
        }
        long activeEpoch = ownerStrikeEpoch.getOrDefault(playerUuid, 0L);
        if (activeEpoch <= 0L) {
            return staffOnStrike.contains(playerUuid);
        }
        if (server != null) {
            boolean foundAny = false;
            for (ServerLevel lvl : server.getAllLevels()) {
                for (net.minecraft.world.entity.Entity ent : lvl.getAllEntities()) {
                    if (isOwnedStrikeEligibleStaffEntity(ent, playerUuid)) {
                        foundAny = true;
                        if (isStaffEntityOnStrike(playerUuid, ent.getUUID())) {
                            return true;
                        }
                    }
                }
            }
            if (foundAny) {
                return false;
            }
        }
        return staffOnStrike.contains(playerUuid);
    }

    public void setStaffOnStrike(UUID playerUuid, boolean strike) {
        if (playerUuid == null) return;
        if (strike) {
            staffOnStrike.add(playerUuid);
            long nextEpoch = ++strikeEpochCounter;
            ownerStrikeEpoch.put(playerUuid, nextEpoch);
        } else {
            staffOnStrike.remove(playerUuid);
            ownerStrikeEpoch.remove(playerUuid);
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
        long currentDay = level.getDayTime() / 24000L;
        MinecraftServer srv = level.getServer();
        syncLoadedStaffWageRecords(srv);

        for (var entry : staffMap.entrySet()) {
            UUID playerUuid = entry.getKey();
            Long lastDay = lastSalaryDayMap.get(playerUuid);
            if (lastDay == null) {
                lastSalaryDayMap.put(playerUuid, currentDay);
                saveToFile();
                continue;
            }
            if (currentDay < lastDay) {
                lastSalaryDayMap.put(playerUuid, currentDay);
                saveToFile();
                continue;
            }
            long daysElapsed = currentDay - lastDay;
            if (daysElapsed <= 0L) {
                continue;
            }
            lastSalaryDayMap.put(playerUuid, currentDay);

            int addDays = (int) Math.min(daysElapsed, 365L);
            int pendingDailyTotal = 0;
            int newlyStruck = 0;
            int newlyTier2 = 0;
            List<String> newlyResignedNames = new ArrayList<>();
            List<String> newlyStruckLabels = new ArrayList<>();

            for (StaffWageRecord w : new ArrayList<>(staffWages.values())) {
                if (!playerUuid.equals(w.ownerUuid) || w.resigned) {
                    continue;
                }
                int wage = w.dailyWage > 0 ? w.dailyWage : getDefaultDailyWageForType(w.staffType);
                if (wage <= 0) continue;

                int prevMissed = w.missedDays;
                w.dailyWage = wage;
                w.missedDays += addDays;
                w.owedAmount += wage * addDays;
                pendingDailyTotal += wage * addDays;

                if (w.missedDays >= 5) {
                    handleStaffResignation(srv, playerUuid, w);
                    newlyResignedNames.add((w.staffName != null && !w.staffName.isBlank()) ? w.staffName : getRoleDisplayLabel(w.staffType));
                } else if (prevMissed < 3 && w.missedDays >= 3) {
                    newlyStruck++;
                    newlyStruckLabels.add(getRoleDisplayLabel(w.staffType) + ": " + w.staffName);
                } else if (prevMissed < 2 && w.missedDays == 2) {
                    newlyTier2++;
                }
            }

            if (pendingDailyTotal <= 0 && newlyResignedNames.isEmpty()) {
                continue;
            }

            saveToFile();
            ServerPlayer player = srv.getPlayerList().getPlayer(playerUuid);

            if (!newlyResignedNames.isEmpty()) {
                if (player != null) {
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.toast.salary.resigned", 3,
                            String.join(", ", newlyResignedNames));
                }
                net.zootycoon.log.ZooLogManager.addLog(playerUuid, "SALARY", "Staff Resign (Gaji 5x Tertunda)",
                        "Staff resign & keluar karena gaji tertunda 5 hari: " + String.join(", ", newlyResignedNames) + ". Hutang tetap wajib dilunasi.",
                        0, 3);
            }
            if (newlyStruck > 0) {
                net.zootycoon.log.ZooLogManager.recordStaffStrikeDetails(playerUuid, pendingDailyTotal, newlyStruckLabels);
                if (player != null) {
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.toast.salary.strike_3x", 3,
                            String.valueOf(newlyStruck));
                }
            } else if (newlyTier2 > 0 && player != null) {
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.toast.salary.delayed_2x", 2,
                        String.valueOf(newlyTier2));
            } else if (pendingDailyTotal > 0 && player != null) {
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.toast.salary.due_manual", 0,
                        String.valueOf(pendingDailyTotal));
            }

            if (player != null) {
                syncToPlayer(player);
                ZooNetworkPayloads.sendAnimalSync(player);
                net.zootycoon.log.ZooLogManager.sendSyncToPlayer(player);
            }
        }
    }

    private void syncLoadedStaffWageRecords(MinecraftServer srv) {
        if (srv == null) return;
        for (ServerLevel lvl : srv.getAllLevels()) {
            for (net.minecraft.world.entity.Entity ent : lvl.getAllEntities()) {
                if (ent == null || !ent.isAlive()) continue;
                if (ent instanceof net.zootycoon.entity.ZooZookeeperEntity zk && zk.getOwnerUuid() != null) {
                    ensureActiveWageRecord(zk.getUUID(), zk.getOwnerUuid(), "zookeeper", zk.getName().getString());
                } else if (ent instanceof net.zootycoon.entity.ZooSecurityEntity sec && sec.getOwnerUuid() != null) {
                    ensureActiveWageRecord(sec.getUUID(), sec.getOwnerUuid(), "security", sec.getName().getString());
                } else if (ent instanceof net.zootycoon.entity.ZooCashierEntity c && c.getOwnerUuid() != null) {
                    ensureActiveWageRecord(c.getUUID(), c.getOwnerUuid(), "cashier", c.getName().getString());
                }
            }
        }
    }

    private void handleStaffResignation(MinecraftServer srv, UUID ownerUuid, StaffWageRecord w) {
        w.resigned = true;
        StaffRecord ownerRec = getRecord(ownerUuid);
        String st = w.staffType != null ? w.staffType.toLowerCase(java.util.Locale.ROOT) : "";
        switch (st) {
            case "zookeeper" -> ownerRec.zookeepers = Math.max(0, ownerRec.zookeepers - 1);
            case "security" -> ownerRec.security = Math.max(0, ownerRec.security - 1);
            case "cashier" -> ownerRec.cashiers = Math.max(0, ownerRec.cashiers - 1);
            default -> {}
        }

        if (srv == null) return;
        for (ServerLevel lvl : srv.getAllLevels()) {
            net.minecraft.world.entity.Entity ent = lvl.getEntity(w.staffUuid);
            if (ent == null || !ent.isAlive()) continue;
            if (ent instanceof net.zootycoon.entity.ZooZookeeperEntity zk) {
                zk.setAssignedAnimalUuid(null);
                zk.dismiss(lvl);
            } else if (ent instanceof net.zootycoon.entity.ZooCashierEntity c) {
                net.minecraft.world.Containers.dropContents(lvl, c.blockPosition(), c.getStockInventory());
                lvl.sendParticles(ParticleTypes.POOF, c.getX(), c.getY() + 1.0D, c.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
                c.discard();
            } else {
                lvl.sendParticles(ParticleTypes.POOF, ent.getX(), ent.getY() + 1.0D, ent.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
                ent.discard();
            }
            break;
        }
    }

    private String getRoleDisplayLabel(String staffType) {
        if (staffType == null) return "Staff";
        return switch (staffType.toLowerCase(java.util.Locale.ROOT)) {
            case "zookeeper" -> "Zookeeper";
            case "security" -> "Security";
            case "cashier" -> "Kasir";
            case "builder" -> "Kuli";
            default -> "Staff";
        };
    }

    public boolean payIndividualWage(ServerPlayer player, UUID staffUuid) {
        if (player == null || staffUuid == null) return false;
        StaffWageRecord w = staffWages.get(staffUuid);
        if (w == null) {
            // Fallback for legacy entity strike payment if no record yet
            for (ServerLevel lvl : player.server.getAllLevels()) {
                net.minecraft.world.entity.Entity ent = lvl.getEntity(staffUuid);
                if (ent != null && ent.isAlive()) {
                    return tryPaySingleStaffStrikeSalary(player, ent);
                }
            }
            return false;
        }
        int owed = Math.max(0, w.owedAmount);
        if (owed <= 0 && w.missedDays <= 0) {
            return true;
        }

        if (owed > 0 && !player.isCreative()) {
            if (!TransactionHelper.hasEnoughEmeralds(player, owed) || !TransactionHelper.consumeEmeralds(player, owed)) {
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.toast.salary.unpaid", 2);
                player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0f, 1.0f);
                return false;
            }
        }

        boolean wasResigned = w.resigned;
        String roleLabel = getRoleDisplayLabel(w.staffType);
        String nameLabel = (w.staffName != null && !w.staffName.isBlank()) ? w.staffName : roleLabel;
        if (wasResigned) {
            staffWages.remove(staffUuid);
        } else {
            w.missedDays = 0;
            w.owedAmount = 0;
        }
        resolveStaffEntityStrike(w.ownerUuid, staffUuid);
        resolveStaffEntityStrike(player.getUUID(), staffUuid);
        saveToFile();

        net.zootycoon.log.ZooLogManager.recordIndividualSalaryPayment(w.ownerUuid, roleLabel, nameLabel, owed);
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success",
                wasResigned ? "zootycoon.toast.salary.debt_cleared" : "zootycoon.toast.salary.paid_manual",
                1, nameLabel, String.valueOf(owed));
        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);
        syncToPlayer(player);
        ZooNetworkPayloads.sendAnimalSync(player);
        net.zootycoon.log.ZooLogManager.sendSyncToPlayer(player);
        return true;
    }

    public void checkStrikeResolutionOnDeposit(UUID playerUuid, ServerPlayer player) {
        // Intentional no-op: when staff are on strike, players must visit and pay each staff member individually via dialog.
    }

    private void finishSingleStrikePayment(ServerPlayer player, UUID ownerId, net.minecraft.world.entity.Entity staffEntity, int wagePaid) {
        UUID staffUuid = staffEntity.getUUID();
        StaffWageRecord wageRec = staffWages.get(staffUuid);
        if (wageRec != null) {
            if (wageRec.resigned) {
                staffWages.remove(staffUuid);
            } else {
                wageRec.missedDays = 0;
                wageRec.owedAmount = 0;
            }
        }
        resolveStaffEntityStrike(ownerId, staffUuid);
        resolveStaffEntityStrike(player.getUUID(), staffUuid);
        String staffName = staffEntity.hasCustomName() ? staffEntity.getCustomName().getString() : staffEntity.getName().getString();
        net.zootycoon.log.ZooLogManager.recordIndividualSalaryPayment(ownerId, getStaffRoleLabel(staffEntity), staffName, wagePaid);
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.toast.salary.resolved", 1);
        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);
        syncToPlayer(player);
        ZooNetworkPayloads.sendAnimalSync(player);
        net.zootycoon.log.ZooLogManager.sendSyncToPlayer(player);
    }

    /** Pays the overdue salary for a single striking staff entity visited via dialog. */
    public boolean tryPaySingleStaffStrikeSalary(ServerPlayer player, net.minecraft.world.entity.Entity staffEntity) {
        if (player == null) return false;
        if (staffEntity == null) {
            return tryPayStrikeSalary(player, player.getUUID());
        }

        UUID ownerId = player.getUUID();
        int individualWage = 0;

        if (staffEntity instanceof net.zootycoon.entity.ZooZookeeperEntity zk) {
            if (zk.getOwnerUuid() != null) ownerId = zk.getOwnerUuid();
            individualWage = ZooEconomyEngine.ZOOKEEPER_DAILY_WAGE;
        } else if (staffEntity instanceof net.zootycoon.entity.ZooSecurityEntity sec) {
            if (sec.getOwnerUuid() != null) ownerId = sec.getOwnerUuid();
            individualWage = ZooEconomyEngine.SECURITY_DAILY_WAGE;
        } else if (staffEntity instanceof net.zootycoon.entity.ZooCashierEntity cash) {
            if (cash.getOwnerUuid() != null) ownerId = cash.getOwnerUuid();
            individualWage = ZooEconomyEngine.CASHIER_DAILY_WAGE;
        } else if (staffEntity instanceof net.zootycoon.entity.ZooBuilderEntity) {
            // Builders are paid per project and never go on daily-wage strike
            return true;
        } else {
            return tryPayStrikeSalary(player, ownerId);
        }

        UUID staffUuid = staffEntity.getUUID();
        if (!isStaffEntityOnStrike(ownerId, staffUuid) && !isStaffEntityOnStrike(player.getUUID(), staffUuid)) {
            return true;
        }

        StaffWageRecord rec = staffWages.get(staffUuid);
        if (rec != null && rec.owedAmount > 0) {
            individualWage = rec.owedAmount;
        }

        if (individualWage <= 0 || player.isCreative()) {
            finishSingleStrikePayment(player, ownerId, staffEntity, 0);
            return true;
        }

        if (TransactionHelper.hasEnoughEmeralds(player, individualWage) && TransactionHelper.consumeEmeralds(player, individualWage)) {
            finishSingleStrikePayment(player, ownerId, staffEntity, individualWage);
            return true;
        } else {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.toast.salary.unpaid", 2);
            player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0f, 1.0f);
            return false;
        }
    }

    public boolean tryPayStrikeSalary(ServerPlayer player, UUID targetOwnerUuid) {
        if (player == null) return false;
        UUID ownerId = targetOwnerUuid != null ? targetOwnerUuid : player.getUUID();
        if (!areStaffOnStrike(ownerId) && !areStaffOnStrike(player.getUUID())) {
            return true;
        }
        int totalWage = calculateDailyWage(ownerId);
        if (totalWage <= 0) {
            totalWage = calculateDailyWage(player.getUUID());
        }
        if (totalWage <= 0 || player.isCreative()) {
            setStaffOnStrike(ownerId, false);
            setStaffOnStrike(player.getUUID(), false);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.toast.salary.resolved", 1);
            player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);
            return true;
        }
        if (TransactionHelper.hasEnoughEmeralds(player, totalWage) && TransactionHelper.consumeEmeralds(player, totalWage)) {
            setStaffOnStrike(ownerId, false);
            setStaffOnStrike(player.getUUID(), false);
            int staffCount = getTotalStaffCount(ownerId);
            net.zootycoon.log.ZooLogManager.recordSalaryPayment(ownerId, totalWage, staffCount, true);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.toast.salary.resolved", 1);
            player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);
            return true;
        } else {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.toast.salary.unpaid", 2);
            player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0f, 1.0f);
            return false;
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
            case "builder", "kuli" -> {
                rec.builders++;
                var b = ZooEntities.BUILDER.create(sLevel);
                if (b != null) {
                    b.setPos(player.getX() + 0.5, player.getY(), player.getZ() + 0.5);
                    b.setPayerUuid(player.getUUID());
                    b.setPersistenceRequired();
                    markNewlyHiredStaffResolved(player.getUUID(), b.getUUID());
                    sLevel.addFreshEntity(b);
                    sLevel.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            b.getX(), b.getY() + 1.0, b.getZ(), 15, 0.4, 0.5, 0.4, 0.02
                    );
                    sLevel.playSound(null, b.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);
                }
            }
            case "zookeeper" -> {
                rec.zookeepers++;
                var zk = ZooEntities.ZOOKEEPER.create(sLevel);
                if (zk != null) {
                    zk.assignUniqueName(sLevel);
                    zk.setPos(player.getX() + 0.5, player.getY(), player.getZ() + 0.5);
                    zk.setOwnerUuid(player.getUUID());
                    markNewlyHiredStaffResolved(player.getUUID(), zk.getUUID());
                    ensureActiveWageRecord(zk.getUUID(), player.getUUID(), "zookeeper", zk.getName().getString());
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
                    markNewlyHiredStaffResolved(player.getUUID(), sec.getUUID());
                    ensureActiveWageRecord(sec.getUUID(), player.getUUID(), "security", sec.getName().getString());
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
                    cashier.setOwnerUuid(player.getUUID());
                    cashier.setFacingDirection(player.getDirection().getOpposite());
                    markNewlyHiredStaffResolved(player.getUUID(), cashier.getUUID());
                    ensureActiveWageRecord(cashier.getUUID(), player.getUUID(), "cashier", cashier.getName().getString());
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
        net.zootycoon.advancement.ZooAdvancements.grant(player, net.zootycoon.advancement.ZooAdvancements.HIRE_FIRST_STAFF);
        if (rec.builders >= 1 && rec.zookeepers >= 1 && rec.security >= 1 && rec.cashiers >= 1) {
            net.zootycoon.advancement.ZooAdvancements.grant(player, net.zootycoon.advancement.ZooAdvancements.HIRE_ALL_STAFF_TYPES);
        }
        return true;
    }

    private net.zootycoon.entity.ZooZookeeperEntity findOwnedZookeeper(ServerPlayer player, UUID zookeeperUuid) {
        if (zookeeperUuid == null || server == null) return null;
        for (ServerLevel lvl : server.getAllLevels()) {
            if (lvl.getEntity(zookeeperUuid) instanceof net.zootycoon.entity.ZooZookeeperEntity zk && zk.isAlive()) {
                UUID owner = zk.getOwnerUuid();
                if (owner == null || owner.equals(player.getUUID())) {
                    return zk;
                }
            }
        }
        return null;
    }

    /** Returns 0 on success, 1 if not found, 2 if the zookeeper is currently working (unless on strike). */
    public int callZookeeper(ServerPlayer player, UUID zookeeperUuid) {
        var zk = findOwnedZookeeper(player, zookeeperUuid);
        if (zk == null) return 1;
        if (zk.hasActiveAssignedAnimal() && !zk.isOnStrike()) return 2;
        zk.callToPlayer(player);
        return 0;
    }

    /** Returns 0 on success, 1 if not found. Allows firing even if staff is assigned/working. */
    public int fireZookeeper(ServerPlayer player, UUID zookeeperUuid) {
        var zk = findOwnedZookeeper(player, zookeeperUuid);
        if (zk == null) return 1;
        zk.setAssignedAnimalUuid(null);
        if (zk.level() instanceof ServerLevel zl) {
            zk.dismiss(zl);
        } else {
            zk.discard();
        }
        StaffRecord rec = getRecord(player.getUUID());
        rec.zookeepers = Math.max(0, rec.zookeepers - 1);
        cleanupWageOnManualFire(zookeeperUuid);
        saveToFile();
        syncToPlayer(player);
        return 0;
    }

    public int callStaff(ServerPlayer player, String staffType, UUID staffUuid) {
        if (staffUuid == null || server == null) return 1;
        for (ServerLevel lvl : server.getAllLevels()) {
            net.minecraft.world.entity.Entity ent = lvl.getEntity(staffUuid);
            if (ent != null && ent.isAlive()) {
                if ("builder".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooBuilderEntity b) {
                    if (b.hasActiveJob() && !b.isOnStrike()) return 2;
                    b.callToPlayer(player);
                    return 0;
                } else if ("security".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooSecurityEntity sec) {
                    sec.callToPlayer(player);
                    return 0;
                } else if ("cashier".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooCashierEntity c) {
                    c.callToPlayer(player);
                    return 0;
                } else if ("zookeeper".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooZookeeperEntity zk) {
                    if (zk.hasActiveAssignedAnimal() && !zk.isOnStrike()) return 2;
                    zk.callToPlayer(player);
                    return 0;
                }
            }
        }
        return 1;
    }

    public int fireStaff(ServerPlayer player, String staffType, UUID staffUuid) {
        if (staffUuid == null || server == null) return 1;
        for (ServerLevel lvl : server.getAllLevels()) {
            net.minecraft.world.entity.Entity ent = lvl.getEntity(staffUuid);
            if (ent != null && ent.isAlive()) {
                StaffRecord rec = getRecord(player.getUUID());
                if ("builder".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooBuilderEntity b) {
                    if (b.getSiteId() != null) {
                        net.zootycoon.construction.ConstructionManager.cancelAndRemoveSite(lvl, b.getSiteId());
                        b.setSiteId(null);
                    }
                    b.discard();
                    rec.builders = Math.max(0, rec.builders - 1);
                    saveToFile();
                    syncToPlayer(player);
                    ZooNetworkPayloads.sendAnimalSync(player);
                    return 0;
                } else if ("security".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooSecurityEntity sec) {
                    sec.discard();
                    rec.security = Math.max(0, rec.security - 1);
                    cleanupWageOnManualFire(staffUuid);
                    saveToFile();
                    syncToPlayer(player);
                    ZooNetworkPayloads.sendAnimalSync(player);
                    return 0;
                } else if ("cashier".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooCashierEntity c) {
                    net.minecraft.world.Containers.dropContents(lvl, c.blockPosition(), c.getStockInventory());
                    c.discard();
                    rec.cashiers = Math.max(0, rec.cashiers - 1);
                    cleanupWageOnManualFire(staffUuid);
                    saveToFile();
                    syncToPlayer(player);
                    ZooNetworkPayloads.sendAnimalSync(player);
                    return 0;
                } else if ("zookeeper".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooZookeeperEntity zk) {
                    return fireZookeeper(player, staffUuid);
                }
            }
        }
        return 1;
    }

    public int removeAllStaffForPlayer(ServerPlayer player) {
        if (player == null || server == null) return 0;
        UUID playerUuid = player.getUUID();
        int removedCount = 0;

        for (ServerLevel lvl : server.getAllLevels()) {
            List<net.minecraft.world.entity.Entity> toRemove = new ArrayList<>();
            for (net.minecraft.world.entity.Entity ent : lvl.getAllEntities()) {
                if (ent == null || !ent.isAlive()) continue;

                if (ent instanceof net.zootycoon.entity.ZooBuilderEntity b) {
                    if (playerUuid.equals(b.getOwnerUuid())) {
                        toRemove.add(b);
                    }
                } else if (ent instanceof net.zootycoon.entity.ZooSecurityEntity sec) {
                    if (playerUuid.equals(sec.getOwnerUuid())) {
                        toRemove.add(sec);
                    }
                } else if (ent instanceof net.zootycoon.entity.ZooCashierEntity c) {
                    if (playerUuid.equals(c.getOwnerUuid())) {
                        toRemove.add(c);
                    }
                } else if (ent instanceof net.zootycoon.entity.ZooZookeeperEntity zk) {
                    if (playerUuid.equals(zk.getOwnerUuid())) {
                        toRemove.add(zk);
                    }
                }
            }

            for (net.minecraft.world.entity.Entity ent : toRemove) {
                if (ent instanceof net.zootycoon.entity.ZooCashierEntity c) {
                    net.minecraft.world.Containers.dropContents(lvl, c.blockPosition(), c.getStockInventory());
                    c.discard();
                } else if (ent instanceof net.zootycoon.entity.ZooZookeeperEntity zk) {
                    zk.dismiss(lvl);
                } else {
                    ent.discard();
                }
                removedCount++;
            }
        }

        StaffRecord rec = getRecord(playerUuid);
        rec.builders = 0;
        rec.zookeepers = 0;
        rec.security = 0;
        rec.cashiers = 0;
        staffOnStrike.remove(playerUuid);
        ownerStrikeEpoch.remove(playerUuid);
        staffWages.values().removeIf(w -> playerUuid.equals(w.ownerUuid));

        saveToFile();
        syncToPlayer(player);
        ZooNetworkPayloads.sendAnimalSync(player);

        return removedCount;
    }

    private void cleanupWageOnManualFire(UUID staffUuid) {
        if (staffUuid == null) return;
        StaffWageRecord w = staffWages.get(staffUuid);
        if (w == null) return;
        if (w.owedAmount > 0) {
            w.resigned = true;
        } else {
            staffWages.remove(staffUuid);
        }
    }

    public void syncToPlayer(ServerPlayer player) {
        StaffRecord rec = getRecord(player.getUUID());
        int activeBuilders = ConstructionManager.getActiveSitesForPlayer(player.getUUID()).size();
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStaffPayload(
                rec.builders, activeBuilders, rec.zookeepers, rec.security, rec.cashiers
        ));
    }

    public void startPeek(ServerPlayer player, String staffType, UUID staffUuid) {
        if (player == null || staffUuid == null || server == null) return;
        net.minecraft.world.entity.Entity target = findPeekableStaff(player, staffType, staffUuid);
        if (target == null) {
            activePeeks.remove(player.getUUID());
            ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncPeekStaffPayload(false, -1, 0, 0, 0, ""));
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("zootycoon.staff.peek.unavailable"), true);
            return;
        }
        activePeeks.put(player.getUUID(), new PeekSession(staffType, staffUuid));
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncPeekStaffPayload(
                true,
                target.getId(),
                target.getX(),
                target.getY() + target.getEyeHeight() * 0.85,
                target.getZ(),
                target.getDisplayName().getString()
        ));
    }

    public void stopPeek(UUID playerUuid) {
        if (playerUuid == null) return;
        activePeeks.remove(playerUuid);
    }

    public void tickPeekUpdates(MinecraftServer srv) {
        if (srv == null || activePeeks.isEmpty()) return;
        if ((srv.getTickCount() & 1) != 0) return;

        var it = activePeeks.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            ServerPlayer player = srv.getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive()) {
                it.remove();
                continue;
            }
            PeekSession session = entry.getValue();
            net.minecraft.world.entity.Entity target = findPeekableStaff(player, session.staffType(), session.staffUuid());
            if (target == null) {
                it.remove();
                ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncPeekStaffPayload(false, -1, 0, 0, 0, ""));
                continue;
            }
            ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncPeekStaffPayload(
                    true,
                    target.getId(),
                    target.getX(),
                    target.getY() + target.getEyeHeight() * 0.85,
                    target.getZ(),
                    target.getDisplayName().getString()
            ));
        }
    }

    private net.minecraft.world.entity.Entity findPeekableStaff(ServerPlayer player, String staffType, UUID staffUuid) {
        if (player == null || staffUuid == null || server == null) return null;
        UUID ownerUuid = player.getUUID();
        for (ServerLevel lvl : server.getAllLevels()) {
            net.minecraft.world.entity.Entity ent = lvl.getEntity(staffUuid);
            if (ent == null || !ent.isAlive() || ent.isRemoved() || ent.level() != player.level()) continue;
            if ("builder".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooBuilderEntity b) {
                if (b.getOwnerUuid() == null || b.getOwnerUuid().equals(ownerUuid)) return b;
            } else if ("zookeeper".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooZookeeperEntity zk) {
                if (zk.getOwnerUuid() == null || zk.getOwnerUuid().equals(ownerUuid)) return zk;
            } else if ("security".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooSecurityEntity sec) {
                if (sec.getOwnerUuid() == null || sec.getOwnerUuid().equals(ownerUuid)) return sec;
            } else if ("cashier".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooCashierEntity c) {
                if (c.getOwnerUuid() == null || c.getOwnerUuid().equals(ownerUuid)) return c;
            }
        }
        return null;
    }
}
