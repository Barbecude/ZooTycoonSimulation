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
            instance.ownerStrikeEpoch.clear();
            instance.staffResolvedEpoch.clear();
            instance.strikeEpochCounter = 0L;
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

            NbtIo.writeCompressed(root, path);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to save staff records", e);
        }
    }

    /** Returns true if this specific staff entity has an unresolved wage strike for its owner. */
    public boolean isStaffEntityOnStrike(UUID ownerUuid, UUID staffUuid) {
        if (ownerUuid == null || staffUuid == null) return false;
        long activeEpoch = ownerStrikeEpoch.getOrDefault(ownerUuid, 0L);
        if (activeEpoch <= 0L) {
            return staffOnStrike.contains(ownerUuid);
        }
        return staffResolvedEpoch.getOrDefault(staffUuid, 0L) < activeEpoch;
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
            if (areStaffOnStrike(playerUuid)) {
                // Active strikes must be settled in person via each striking staff member's dialog
                if (player != null) {
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.toast.salary.unpaid", 2);
                }
                continue;
            }

            int currentBal = eco.getBalance(playerUuid);

            if (currentBal >= totalWage) {
                eco.deductBalance(playerUuid, totalWage, player);
                setStaffOnStrike(playerUuid, false);
                net.zootycoon.log.ZooLogManager.recordSalaryPayment(playerUuid, totalWage, staffCount, true);
                if (player != null) {
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.toast.salary.paid", 0, String.valueOf(totalWage), String.valueOf(staffCount));
                }
            } else {
                setStaffOnStrike(playerUuid, true);
                net.zootycoon.log.ZooLogManager.recordStaffStrikeDetails(playerUuid, totalWage, getStrikingStaffLabels(playerUuid));
                if (player != null) {
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.toast.salary.unpaid", 2);
                }
            }
        }
    }

    public void checkStrikeResolutionOnDeposit(UUID playerUuid, ServerPlayer player) {
        // Intentional no-op: when staff are on strike, players must visit and pay each staff member individually via dialog.
    }

    private void finishSingleStrikePayment(ServerPlayer player, UUID ownerId, net.minecraft.world.entity.Entity staffEntity, int wagePaid) {
        UUID staffUuid = staffEntity.getUUID();
        resolveStaffEntityStrike(ownerId, staffUuid);
        resolveStaffEntityStrike(player.getUUID(), staffUuid);
        String staffName = staffEntity.hasCustomName() ? staffEntity.getCustomName().getString() : staffEntity.getName().getString();
        net.zootycoon.log.ZooLogManager.recordIndividualSalaryPayment(ownerId, getStaffRoleLabel(staffEntity), staffName, wagePaid);
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.toast.salary.resolved", 1);
        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);
        ZooNetworkPayloads.sendAnimalSync(player);
        net.zootycoon.log.ZooLogManager.sendSyncToPlayer(player);
    }

    /** Pays the overdue daily salary for a single striking staff entity visited via dialog. */
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
                        net.zootycoon.construction.ConstructionManager.getSite(b.getSiteId()).ifPresent(s -> s.setBuilderUuid(null));
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
                    saveToFile();
                    syncToPlayer(player);
                    ZooNetworkPayloads.sendAnimalSync(player);
                    return 0;
                } else if ("cashier".equalsIgnoreCase(staffType) && ent instanceof net.zootycoon.entity.ZooCashierEntity c) {
                    net.minecraft.world.Containers.dropContents(lvl, c.blockPosition(), c.getStockInventory());
                    c.discard();
                    rec.cashiers = Math.max(0, rec.cashiers - 1);
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

        saveToFile();
        syncToPlayer(player);
        ZooNetworkPayloads.sendAnimalSync(player);

        return removedCount;
    }

    public void syncToPlayer(ServerPlayer player) {
        StaffRecord rec = getRecord(player.getUUID());
        int activeBuilders = ConstructionManager.getActiveSitesForPlayer(player.getUUID()).size();
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStaffPayload(
                rec.builders, activeBuilders, rec.zookeepers, rec.security, rec.cashiers
        ));
    }
}
