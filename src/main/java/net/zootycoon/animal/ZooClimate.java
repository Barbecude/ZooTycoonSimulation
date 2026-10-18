package net.zootycoon.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.zootycoon.compat.FabricSeasonsCompat;
import net.zootycoon.compat.SereneSeasonsCompat;

import java.util.Locale;

/**
 * Utility for evaluating climate, season, and temperature categories for zoo animals.
 * Uses Fabric Seasons (or Serene Seasons fallback) and accurate Minecraft biome temperature calculations.
 */
public class ZooClimate {

    public enum TempCategory {
        FREEZING("freezing", "zootycoon.temperature.freezing"),
        COLD("cold", "zootycoon.temperature.cold"),
        TEMPERATE("temperate", "zootycoon.temperature.temperate"),
        WARM("warm", "zootycoon.temperature.warm"),
        HOT("hot", "zootycoon.temperature.hot");

        public final String key;
        public final String translationKey;

        TempCategory(String key, String translationKey) {
            this.key = key;
            this.translationKey = translationKey;
        }

        public static TempCategory fromKey(String key) {
            if (key == null) return TEMPERATE;
            for (TempCategory c : values()) {
                if (c.key.equalsIgnoreCase(key)) return c;
            }
            return TEMPERATE;
        }
    }

    /**
     * Determines temperature category based on the Minecraft effective float temperature:
     * - Freezing (< 0.15)
     * - Cold (< 0.45)
     * - Temperate / Moderate (< 0.85)
     * - Warm (< 1.25)
     * - Hot (>= 1.25)
     */
    public static TempCategory getCategory(float effectiveTemperature) {
        if (effectiveTemperature < 0.15f) {
            return TempCategory.FREEZING;
        } else if (effectiveTemperature < 0.45f) {
            return TempCategory.COLD;
        } else if (effectiveTemperature < 0.85f) {
            return TempCategory.TEMPERATE;
        } else if (effectiveTemperature < 1.25f) {
            return TempCategory.WARM;
        } else {
            return TempCategory.HOT;
        }
    }

    public static float getEffectiveTemperature(LivingEntity entity) {
        if (entity == null || entity.level() == null) return 0.5f;
        return getEffectiveTemperature(entity.level(), entity.blockPosition());
    }

    public static float getEffectiveTemperature(Level level, BlockPos pos) {
        if (level == null || pos == null) return 0.5f;
        if (FabricSeasonsCompat.isLoaded()) {
            return FabricSeasonsCompat.getEffectiveTemperature(level, pos);
        }
        float baseTemp = level.getBiome(pos).value().getBaseTemperature();
        if (pos.getY() > 80) {
            float heightDrop = (pos.getY() - 80) * 0.05f / 40.0f;
            return baseTemp - heightDrop;
        }
        return baseTemp;
    }

    public static String getCurrentSeason(Level level) {
        if (FabricSeasonsCompat.isLoaded()) {
            return FabricSeasonsCompat.getCurrentSeason(level);
        }
        if (SereneSeasonsCompat.isLoaded()) {
            return SereneSeasonsCompat.getCurrentSeason(level);
        }
        return "SPRING";
    }

    public static Component getSeasonComponent(String season) {
        if (season == null) return Component.translatable("zootycoon.season.spring");
        String s = season.toUpperCase(Locale.ROOT);
        return switch (s) {
            case "SUMMER" -> Component.translatable("zootycoon.season.summer");
            case "FALL", "AUTUMN" -> Component.translatable("zootycoon.season.fall");
            case "WINTER" -> Component.translatable("zootycoon.season.winter");
            default -> Component.translatable("zootycoon.season.spring");
        };
    }

    public static Component getTempCategoryComponent(String tempCategoryKey) {
        TempCategory category = TempCategory.fromKey(tempCategoryKey);
        return Component.translatable(category.translationKey);
    }
}
