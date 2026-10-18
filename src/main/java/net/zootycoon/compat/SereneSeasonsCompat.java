package net.zootycoon.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.lang.reflect.Method;

public class SereneSeasonsCompat {

    private static boolean checked = false;
    private static boolean isPresent = false;
    private static Method getSeasonStateMethod = null;
    private static Method getSeasonMethod = null;

    private static void checkInit() {
        if (checked) return;
        checked = true;
        try {
            if (FabricLoader.getInstance().isModLoaded("sereneseasons")) {
                Class<?> helperClass = Class.forName("sereneseasons.api.season.SeasonHelper");
                getSeasonStateMethod = helperClass.getMethod("getSeasonState", Level.class);
                Class<?> stateClass = Class.forName("sereneseasons.api.season.ISeasonState");
                getSeasonMethod = stateClass.getMethod("getSeason");
                isPresent = true;
            }
        } catch (Throwable t) {
            isPresent = false;
        }
    }

    public static boolean isLoaded() {
        checkInit();
        return isPresent || FabricSeasonsCompat.isLoaded();
    }

    public static String getCurrentSeason(Level level) {
        checkInit();
        if (isPresent && level != null && getSeasonStateMethod != null && getSeasonMethod != null) {
            try {
                Object seasonState = getSeasonStateMethod.invoke(null, level);
                if (seasonState != null) {
                    Object seasonEnum = getSeasonMethod.invoke(seasonState);
                    if (seasonEnum != null) {
                        return seasonEnum.toString().toUpperCase(java.util.Locale.ROOT); // "SPRING", "SUMMER", "AUTUMN", "WINTER"
                    }
                }
            } catch (Throwable ignored) {}
        }
        if (FabricSeasonsCompat.isLoaded()) {
            return FabricSeasonsCompat.getCurrentSeason(level);
        }
        return "SPRING";
    }

    public static String getSeasonDisplayName(Level level) {
        String s = getCurrentSeason(level);
        return switch (s.toUpperCase(java.util.Locale.ROOT)) {
            case "SUMMER" -> "Musim Panas (Summer)";
            case "AUTUMN", "FALL" -> "Musim Gugur (Autumn)";
            case "WINTER" -> "Musim Dingin (Winter)";
            default -> "Musim Semi (Spring)";
        };
    }

    public record AnimalEnvironmentStatus(
            float temperature,
            String tempCategory,
            String seasonName,
            boolean comfortable,
            String notes
    ) {}

    public static AnimalEnvironmentStatus evaluateEnvironment(LivingEntity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        Biome biome = level.getBiome(pos).value();
        float baseTemp = biome.getBaseTemperature();

        String season = getCurrentSeason(level);
        float effectiveTemp = baseTemp;
        if ("WINTER".equalsIgnoreCase(season)) {
            effectiveTemp -= 0.35f;
        } else if ("SUMMER".equalsIgnoreCase(season)) {
            effectiveTemp += 0.30f;
        }

        String tempCat;
        if (effectiveTemp < 0.15f) {
            tempCat = "Beku / Salju";
        } else if (effectiveTemp < 0.45f) {
            tempCat = "Sejuk / Dingin";
        } else if (effectiveTemp < 0.85f) {
            tempCat = "Sedang / Nyaman";
        } else if (effectiveTemp < 1.25f) {
            tempCat = "Hangat";
        } else {
            tempCat = "Panas Terik";
        }

        // Animal compatibility detection based on type and tags
        String entityTypeStr = entity.getType().toString().toLowerCase();
        boolean isColdAnimal = entityTypeStr.contains("polar") || entityTypeStr.contains("snow") || entityTypeStr.contains("penguin") || entityTypeStr.contains("wolf") || entityTypeStr.contains("fox");
        boolean isDesertWarmAnimal = entityTypeStr.contains("camel") || entityTypeStr.contains("lion") || entityTypeStr.contains("elephant") || entityTypeStr.contains("giraffe") || entityTypeStr.contains("parrot");

        boolean comfortable = true;
        String notes = "Kondisi lingkungan ideal!";

        if (isColdAnimal) {
            if ("SUMMER".equalsIgnoreCase(season) || effectiveTemp > 0.9f) {
                comfortable = false;
                notes = "Kepanasan! Butuh salju / naungan dingin.";
            } else {
                notes = "Sangat suka udara sejuk!";
            }
        } else if (isDesertWarmAnimal) {
            if ("WINTER".equalsIgnoreCase(season) || effectiveTemp < 0.2f) {
                comfortable = false;
                notes = "Kedinginan! Butuh pemanas / kandang tertutup.";
            } else {
                notes = "Sangat cocok dengan suhu hangat!";
            }
        }

        return new AnimalEnvironmentStatus(effectiveTemp, tempCat, getSeasonDisplayName(level), comfortable, notes);
    }
}

