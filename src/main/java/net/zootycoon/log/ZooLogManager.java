package net.zootycoon.log;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.zootycoon.network.ZooNetworkPayloads;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages persistent activity logs and visitor statistics per zoo owner.
 * Categories strictly tracked:
 * - SALARY: Staff wage payments or unpaid strike warnings
 * - TICKET: Visitor ticket purchases and revenue
 * - ANIMAL_HEALTH: Health warnings or animal stat problems (hunger, thirst, dirt, temperature, low HP)
 * - STAFF_REQUEST: Requests for animal food, heater/cooler supplies, or kitchen raw ingredients
 */
public class ZooLogManager {

    private static final Map<UUID, List<ZooNetworkPayloads.ZooLogEntryDto>> OWNER_LOGS = new ConcurrentHashMap<>();
    private static final Map<UUID, OwnerDailyStats> OWNER_STATS = new ConcurrentHashMap<>();

    private static final int MAX_LOGS_PER_OWNER = 120;

    public static class OwnerDailyStats {
        public String dateKey; // "YYYY-MM-DD" in local machine time
        public int totalVisitorsToday;
        public int ticketsSoldToday;
        public int ticketRevenueToday;
        public int freeVisitorsToday;
        public int childVisitorsToday;
        public int adultVisitorsToday;

        public OwnerDailyStats(String dateKey) {
            this.dateKey = dateKey;
        }

        public void checkDateRollover() {
            String today = LocalDate.now(ZoneId.systemDefault()).toString();
            if (!today.equals(this.dateKey)) {
                this.dateKey = today;
                this.totalVisitorsToday = 0;
                this.ticketsSoldToday = 0;
                this.ticketRevenueToday = 0;
                this.freeVisitorsToday = 0;
                this.childVisitorsToday = 0;
                this.adultVisitorsToday = 0;
            }
        }
    }

    private static OwnerDailyStats getOrCreateStats(UUID ownerUuid) {
        String today = LocalDate.now(ZoneId.systemDefault()).toString();
        OwnerDailyStats stats = OWNER_STATS.computeIfAbsent(ownerUuid, u -> new OwnerDailyStats(today));
        stats.checkDateRollover();
        return stats;
    }

    public static List<ZooNetworkPayloads.ZooLogEntryDto> getLogsForOwner(UUID ownerUuid) {
        if (ownerUuid == null) return List.of();
        List<ZooNetworkPayloads.ZooLogEntryDto> list = OWNER_LOGS.get(ownerUuid);
        if (list == null) return List.of();
        return new ArrayList<>(list);
    }

    public static ZooNetworkPayloads.ZooStatsDto getStatsForOwner(ServerLevel level, UUID ownerUuid) {
        if (ownerUuid == null) {
            return new ZooNetworkPayloads.ZooStatsDto(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        OwnerDailyStats st = getOrCreateStats(ownerUuid);

        // Calculate live visitors in owner's claims
        int currentVisitorsInZoo = 0;
        int happyVisitors = 0;
        int neutralVisitors = 0;
        int unhappyVisitors = 0;

        var claims = net.zootycoon.claim.ZooClaimManager.get(level).getClaimsForPlayer(ownerUuid);
        if (!claims.isEmpty()) {
            net.minecraft.world.phys.AABB combinedBox = null;
            for (var c : claims) {
                var b = c.getBox();
                var aabb = new net.minecraft.world.phys.AABB(b.minX(), b.minY() - 4, b.minZ(), b.maxX() + 1, b.maxY() + 8, b.maxZ() + 1);
                combinedBox = (combinedBox == null) ? aabb : combinedBox.minmax(aabb);
            }
            if (combinedBox != null) {
                List<net.zootycoon.entity.ZooVisitorEntity> visitors = level.getEntitiesOfClass(
                        net.zootycoon.entity.ZooVisitorEntity.class, combinedBox, net.minecraft.world.entity.Entity::isAlive
                );
                currentVisitorsInZoo = visitors.size();
                for (var v : visitors) {
                    var mood = v.getMood();
                    if (mood == net.zootycoon.entity.ZooVisitorEntity.VisitorMood.ADORED
                            || mood == net.zootycoon.entity.ZooVisitorEntity.VisitorMood.AMAZED) {
                        happyVisitors++;
                    } else if (mood == net.zootycoon.entity.ZooVisitorEntity.VisitorMood.ANGRY
                            || mood == net.zootycoon.entity.ZooVisitorEntity.VisitorMood.CRYING
                            || mood == net.zootycoon.entity.ZooVisitorEntity.VisitorMood.SCARED) {
                        unhappyVisitors++;
                    } else {
                        neutralVisitors++;
                    }
                }
            }
        }

        return new ZooNetworkPayloads.ZooStatsDto(
                st.totalVisitorsToday,
                st.ticketsSoldToday,
                st.ticketRevenueToday,
                st.freeVisitorsToday,
                st.childVisitorsToday,
                st.adultVisitorsToday,
                currentVisitorsInZoo,
                happyVisitors,
                neutralVisitors,
                unhappyVisitors
        );
    }

    public static void addLog(UUID ownerUuid, String category, String title, String description, int emeraldDelta, int urgency) {
        if (ownerUuid == null) return;
        List<ZooNetworkPayloads.ZooLogEntryDto> list = OWNER_LOGS.computeIfAbsent(ownerUuid, u -> new CopyOnWriteArrayList<>());

        String id = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        // Avoid spamming identical logs within 30 seconds
        for (var existing : list) {
            if (existing.title().equals(title) && existing.description().equals(description)
                    && (timestamp - existing.timestamp()) < 30_000L) {
                return;
            }
        }

        list.add(0, new ZooNetworkPayloads.ZooLogEntryDto(id, timestamp, category, title, description, emeraldDelta, urgency));
        while (list.size() > MAX_LOGS_PER_OWNER) {
            list.remove(list.size() - 1);
        }
    }

    public static void recordTicketPurchase(UUID ownerUuid, int ticketPrice, boolean isChild) {
        if (ownerUuid == null) return;
        OwnerDailyStats st = getOrCreateStats(ownerUuid);
        st.totalVisitorsToday++;
        if (isChild) {
            st.childVisitorsToday++;
        } else {
            st.adultVisitorsToday++;
        }

        if (ticketPrice > 0) {
            st.ticketsSoldToday++;
            st.ticketRevenueToday += ticketPrice;
            addLog(ownerUuid, "TICKET", "Pembelian Tiket Pengunjung",
                    (isChild ? "Pengunjung anak-anak" : "Pengunjung") + " membeli tiket masuk seharga " + ticketPrice + " Emerald.",
                    ticketPrice, 1);
        } else {
            st.freeVisitorsToday++;
            addLog(ownerUuid, "TICKET", "Pengunjung Masuk (Gratis)",
                    (isChild ? "Pengunjung anak-anak" : "Pengunjung") + " memasuki kebun binatang dengan tiket gratis ramah warga.",
                    0, 1);
        }
    }

    public static void recordSalaryPayment(UUID ownerUuid, int totalWage, int staffCount, boolean success) {
        if (ownerUuid == null) return;
        if (success) {
            addLog(ownerUuid, "SALARY", "Pembayaran Gaji Staff",
                    "Gaji harian berhasil dibayarkan sebesar " + totalWage + " Emerald untuk " + staffCount + " staff.",
                    -totalWage, 1);
        } else {
            addLog(ownerUuid, "SALARY", "Gaji Staff Belum Terbayar",
                    "Saldo emerald tidak mencukupi untuk membayar gaji (" + totalWage + " Emerald). Seluruh staff mogok kerja!",
                    0, 3);
        }
    }

    public static void recordStaffRequest(UUID ownerUuid, String staffType, String staffName, String detail) {
        if (ownerUuid == null) return;
        String typeLabel = "ZOOKEEPER".equalsIgnoreCase(staffType) ? "Zookeeper" : ("CASHIER".equalsIgnoreCase(staffType) ? "Kasir" : "Staff");
        addLog(ownerUuid, "STAFF_REQUEST", "Permintaan Suplai " + typeLabel,
                staffName + " memerlukan suplai: " + detail,
                0, 2);
    }

    public static void recordAnimalHealthIssue(UUID ownerUuid, String animalName, String problemDetail, int urgency) {
        if (ownerUuid == null) return;
        addLog(ownerUuid, "ANIMAL_HEALTH", "Kondisi Satwa: " + animalName,
                animalName + " mengalami masalah: " + problemDetail,
                0, urgency);
    }

    public static void resetLogsAndStats(ServerLevel level, UUID ownerUuid) {
        if (ownerUuid == null) return;
        List<ZooNetworkPayloads.ZooLogEntryDto> list = OWNER_LOGS.get(ownerUuid);
        if (list != null) {
            list.clear();
        }
        OwnerDailyStats st = OWNER_STATS.get(ownerUuid);
        if (st != null) {
            String today = LocalDate.now(ZoneId.systemDefault()).toString();
            st.dateKey = today;
            st.totalVisitorsToday = 0;
            st.ticketsSoldToday = 0;
            st.ticketRevenueToday = 0;
            st.freeVisitorsToday = 0;
            st.childVisitorsToday = 0;
            st.adultVisitorsToday = 0;
        }
        if (level != null) {
            net.zootycoon.claim.ZooClaimManager.get(level).saveToFile();
        }
    }

    public static void sendSyncToPlayer(ServerPlayer player) {
        if (player == null || !(player.level() instanceof ServerLevel sLevel)) return;
        UUID ownerUuid = player.getUUID();
        List<ZooNetworkPayloads.ZooLogEntryDto> logs = getLogsForOwner(ownerUuid);
        ZooNetworkPayloads.ZooStatsDto stats = getStatsForOwner(sLevel, ownerUuid);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncZooLogStatsPayload(logs, stats));
    }

    // NBT Persistence
    public static CompoundTag saveToNbt() {
        CompoundTag root = new CompoundTag();

        // Save logs
        ListTag ownersList = new ListTag();
        for (var entry : OWNER_LOGS.entrySet()) {
            CompoundTag ownerTag = new CompoundTag();
            ownerTag.putUUID("OwnerUuid", entry.getKey());
            ListTag logList = new ListTag();
            for (var log : entry.getValue()) {
                CompoundTag lTag = new CompoundTag();
                lTag.putString("Id", log.id());
                lTag.putLong("Time", log.timestamp());
                lTag.putString("Cat", log.category());
                lTag.putString("Title", log.title());
                lTag.putString("Desc", log.description());
                lTag.putInt("Delta", log.emeraldDelta());
                lTag.putInt("Urg", log.urgency());
                logList.add(lTag);
            }
            ownerTag.put("Logs", logList);
            ownersList.add(ownerTag);
        }
        root.put("Owners", ownersList);

        // Save stats
        ListTag statsList = new ListTag();
        for (var entry : OWNER_STATS.entrySet()) {
            CompoundTag sTag = new CompoundTag();
            sTag.putUUID("OwnerUuid", entry.getKey());
            var st = entry.getValue();
            sTag.putString("Date", st.dateKey != null ? st.dateKey : "");
            sTag.putInt("TotalVis", st.totalVisitorsToday);
            sTag.putInt("Sold", st.ticketsSoldToday);
            sTag.putInt("Rev", st.ticketRevenueToday);
            sTag.putInt("Free", st.freeVisitorsToday);
            sTag.putInt("Child", st.childVisitorsToday);
            sTag.putInt("Adult", st.adultVisitorsToday);
            statsList.add(sTag);
        }
        root.put("Stats", statsList);

        return root;
    }

    public static void loadFromNbt(CompoundTag root) {
        OWNER_LOGS.clear();
        OWNER_STATS.clear();
        if (root == null) return;

        if (root.contains("Owners", Tag.TAG_LIST)) {
            ListTag ownersList = root.getList("Owners", Tag.TAG_COMPOUND);
            for (int i = 0; i < ownersList.size(); i++) {
                CompoundTag ownerTag = ownersList.getCompound(i);
                if (ownerTag.hasUUID("OwnerUuid")) {
                    UUID u = ownerTag.getUUID("OwnerUuid");
                    List<ZooNetworkPayloads.ZooLogEntryDto> list = new CopyOnWriteArrayList<>();
                    if (ownerTag.contains("Logs", Tag.TAG_LIST)) {
                        ListTag logList = ownerTag.getList("Logs", Tag.TAG_COMPOUND);
                        for (int j = 0; j < logList.size(); j++) {
                            CompoundTag lTag = logList.getCompound(j);
                            list.add(new ZooNetworkPayloads.ZooLogEntryDto(
                                    lTag.getString("Id"),
                                    lTag.getLong("Time"),
                                    lTag.getString("Cat"),
                                    lTag.getString("Title"),
                                    lTag.getString("Desc"),
                                    lTag.getInt("Delta"),
                                    lTag.getInt("Urg")
                            ));
                        }
                    }
                    OWNER_LOGS.put(u, list);
                }
            }
        }

        if (root.contains("Stats", Tag.TAG_LIST)) {
            ListTag statsList = root.getList("Stats", Tag.TAG_COMPOUND);
            for (int i = 0; i < statsList.size(); i++) {
                CompoundTag sTag = statsList.getCompound(i);
                if (sTag.hasUUID("OwnerUuid")) {
                    UUID u = sTag.getUUID("OwnerUuid");
                    OwnerDailyStats st = new OwnerDailyStats(sTag.getString("Date"));
                    st.totalVisitorsToday = sTag.getInt("TotalVis");
                    st.ticketsSoldToday = sTag.getInt("Sold");
                    st.ticketRevenueToday = sTag.getInt("Rev");
                    st.freeVisitorsToday = sTag.getInt("Free");
                    st.childVisitorsToday = sTag.getInt("Child");
                    st.adultVisitorsToday = sTag.getInt("Adult");
                    st.checkDateRollover();
                    OWNER_STATS.put(u, st);
                }
            }
        }
    }
}
