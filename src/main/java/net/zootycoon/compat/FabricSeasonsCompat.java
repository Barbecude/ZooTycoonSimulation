package net.zootycoon.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.zootycoon.ZooTycoonMod;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Soft dependency compatibility layer for Fabric Seasons mod (modid: "seasons").
 * Uses reflection to read the current season and seasonal biome temperature modifications.
 */
public class FabricSeasonsCompat {

    private static boolean checked = false;
    private static boolean isPresent = false;
    private static Method getCurrentSeasonWorldMethod = null;
    private static Method getCurrentSeasonClientMethod = null;
    private static Method injectBiomeTemperatureMethod = null;

    private static synchronized void checkInit() {
        if (checked) return;
        checked = true;
        try {
            if (FabricLoader.getInstance().isModLoaded("seasons")) {
                Class<?> seasonsClass = Class.forName("io.github.lucaargolo.seasons.FabricSeasons");
                for (Method m : seasonsClass.getMethods()) {
                    if (m.getName().equals("getCurrentSeason")) {
                        if (m.getParameterCount() == 1) {
                            getCurrentSeasonWorldMethod = m;
                        } else if (m.getParameterCount() == 0) {
                            getCurrentSeasonClientMethod = m;
                        }
                    } else if (m.getName().equals("injectBiomeTemperature") && m.getParameterCount() == 2) {
                        injectBiomeTemperatureMethod = m;
                    }
                }
                isPresent = (getCurrentSeasonWorldMethod != null || getCurrentSeasonClientMethod != null);
                if (isPresent) {
                    ZooTycoonMod.LOGGER.info("[ZooTycoon] Fabric Seasons integration active. Current season hook loaded.");
                }
            }
        } catch (Throwable t) {
            isPresent = false;
            ZooTycoonMod.LOGGER.warn("[ZooTycoon] Failed to initialize Fabric Seasons reflection: " + t.getMessage());
        }
    }

    public static boolean isLoaded() {
        checkInit();
        return isPresent;
    }

    /**
     * Reads the current season name from Fabric Seasons, normalized to SPRING, SUMMER, FALL, or WINTER.
     */
    public static String getCurrentSeason(Level level) {
        checkInit();
        if (!isPresent) {
            return "SPRING";
        }
        try {
            if (level != null && getCurrentSeasonWorldMethod != null) {
                Object seasonObj = getCurrentSeasonWorldMethod.invoke(null, level);
                if (seasonObj != null) {
                    return normalizeSeason(seasonObj.toString());
                }
            } else if (getCurrentSeasonClientMethod != null) {
                Object seasonObj = getCurrentSeasonClientMethod.invoke(null);
                if (seasonObj != null) {
                    return normalizeSeason(seasonObj.toString());
                }
            }
        } catch (Throwable t) {
            ZooTycoonMod.LOGGER.debug("[ZooTycoon] Error reading Fabric Seasons: " + t.getMessage());
        }
        return "SPRING";
    }

    private static String normalizeSeason(String raw) {
        if (raw == null) return "SPRING";
        String upper = raw.trim().toUpperCase(Locale.ROOT);
        if (upper.contains("SUMMER")) return "SUMMER";
        if (upper.contains("WINTER")) return "WINTER";
        if (upper.contains("FALL") || upper.contains("AUTUMN")) return "FALL";
        return "SPRING";
    }

    /**
     * Retrieves the season-modified biome temperature at the given position.
     * Fabric Seasons intercepts getBiome via WorldViewMixin and injects season offsets.
     */
    public static float getEffectiveTemperature(Level level, BlockPos pos) {
        checkInit();
        if (level == null || pos == null) return 0.5f;
        Holder<Biome> biomeHolder = level.getBiome(pos);
        if (isPresent && injectBiomeTemperatureMethod != null) {
            try {
                injectBiomeTemperatureMethod.invoke(null, biomeHolder, level);
            } catch (Throwable ignored) {}
        }
        float baseTemp = biomeHolder.value().getBaseTemperature();
        if (pos.getY() > 80) {
            float heightDrop = (pos.getY() - 80) * 0.05f / 40.0f;
            return baseTemp - heightDrop;
        }
        return baseTemp;
    }
}
