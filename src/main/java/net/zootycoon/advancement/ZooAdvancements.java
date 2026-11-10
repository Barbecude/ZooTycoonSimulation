package net.zootycoon.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.zootycoon.ZooTycoonMod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ZooAdvancements {

    public static final String ROOT = "root";
    public static final String FIRST_CLAIM = "first_claim";
    public static final String GRAND_OPENING = "grand_opening";
    public static final String TIER_1 = "tier_1_taman_mini";
    public static final String TIER_2 = "tier_2_kebun_binatang";
    public static final String TIER_3 = "tier_3_suaka_margasatwa";
    public static final String TIER_4 = "tier_4_cagar_alam";
    public static final String TIER_5 = "tier_5_konservasi_dunia";
    public static final String HIRE_FIRST_STAFF = "hire_first_staff";
    public static final String HIRE_ALL_STAFF_TYPES = "hire_all_staff_types";
    public static final String LOGISTIC_MASTER = "logistic_master";
    public static final String DEFEAT_HUNTER = "defeat_hunter";
    public static final String PLACE_BANNER = "place_banner";
    public static final String FIVE_STAR_ZOO = "five_star_zoo";

    public static final int LOGISTIC_MASTER_TARGET = 10;
    private static final Map<UUID, Integer> SUPPLY_FULFILL_COUNT = new ConcurrentHashMap<>();

    private ZooAdvancements() {
    }

    public static void grant(ServerPlayer player, String advancementPath) {
        if (player == null || player.server == null || advancementPath == null || advancementPath.isEmpty()) {
            return;
        }
        try {
            if (!ROOT.equals(advancementPath)) {
                grantSingle(player, ROOT);
            }
            grantSingle(player, advancementPath);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to grant advancement {} to {}", advancementPath, player.getName().getString(), e);
        }
    }

    private static void grantSingle(ServerPlayer player, String advancementPath) {
        ResourceLocation id = ZooTycoonMod.id(advancementPath);
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(holder, criterion);
                }
            }
        }
    }

    public static void grantForLicenseTier(ServerPlayer player, int tierLevel) {
        if (player == null) return;
        String adv = switch (Math.clamp(tierLevel, 1, 5)) {
            case 1 -> TIER_1;
            case 2 -> TIER_2;
            case 3 -> TIER_3;
            case 4 -> TIER_4;
            default -> TIER_5;
        };
        grant(player, adv);
    }

    public static void onSupplyRequestFulfilled(ServerPlayer player) {
        if (player == null) return;
        int count = SUPPLY_FULFILL_COUNT.merge(player.getUUID(), 1, Integer::sum);
        if (count >= LOGISTIC_MASTER_TARGET) {
            grant(player, LOGISTIC_MASTER);
        }
    }
}
