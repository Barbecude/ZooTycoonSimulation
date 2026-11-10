package net.zootycoon.tier1;

import net.minecraft.resources.ResourceLocation;
import net.zootycoon.TestHelper;
import net.zootycoon.animal.ZooClimate;
import net.zootycoon.animal.habitat.HabitatProfile;
import net.zootycoon.animal.habitat.HabitatProfileLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F19: Biome Thermal Specialization (Hot-Only, Cold-Only & Season-Immune Temperate)")
public class F19_BiomeTemperatureSpecializationTest {

    @Test
    @DisplayName("Hot-only species stay comfortable in native biome during Spring/Summer/Fall and feel cold outside hot biomes or in unheated Winter")
    void testHotOnlySpecialistsThermalRules() {
        TestHelper.ensureMinecraftBootstrapped();
        HabitatProfile giraffe = HabitatProfileLoader.getDynamicFallbackProfile(ResourceLocation.parse("naturalist:giraffe"));
        assertTrue(ZooClimate.isHotSpecialist(giraffe));
        assertFalse(ZooClimate.isColdSpecialist(giraffe));

        // Even if modded savanna float temperature dips in Spring/Fall, native biome in Spring must NOT freeze
        var springNative = ZooClimate.evaluateThermalState(0.90f, 0.0f, "SPRING", true, giraffe);
        assertFalse(springNative.tooCold(), "Giraffe in native savanna during Spring must never feel cold");
        assertFalse(springNative.tooHot());

        var fallNative = ZooClimate.evaluateThermalState(0.95f, 0.0f, "FALL", true, giraffe);
        assertFalse(fallNative.tooCold(), "Giraffe in native savanna during Fall must never feel cold");
        assertFalse(fallNative.tooHot());

        // Outside native biome (e.g. Plains/Forest at 0.80f): Giraffe feels cold without Heater
        var outsidePlains = ZooClimate.evaluateThermalState(0.80f, 0.0f, "SPRING", false, giraffe);
        assertTrue(outsidePlains.tooCold(), "Giraffe outside hot biome without heater must feel cold");
        assertFalse(outsidePlains.tooHot());

        // Outside native biome WITH Heater warming temp >= 1.25f: comfortable
        var outsideHeated = ZooClimate.evaluateThermalState(1.55f, 0.75f, "SPRING", false, giraffe);
        assertFalse(outsideHeated.tooCold());
        assertFalse(outsideHeated.tooHot());

        // In Winter inside native biome: cold unless Heater is active
        var winterUnheated = ZooClimate.evaluateThermalState(0.95f, 0.0f, "WINTER", true, giraffe);
        assertTrue(winterUnheated.tooCold());
        var winterHeated = ZooClimate.evaluateThermalState(1.10f, 0.75f, "WINTER", true, giraffe);
        assertFalse(winterHeated.tooCold());
    }

    @Test
    @DisplayName("Cold-only species stay comfortable in native biome during Spring/Fall/Winter and feel hot outside cold biomes")
    void testColdOnlySpecialistsThermalRules() {
        TestHelper.ensureMinecraftBootstrapped();
        HabitatProfile polarBear = HabitatProfileLoader.getDynamicFallbackProfile(ResourceLocation.parse("minecraft:polar_bear"));
        assertTrue(ZooClimate.isColdSpecialist(polarBear));
        assertFalse(ZooClimate.isHotSpecialist(polarBear));

        // In native snow biome during Spring/Fall/Winter -> always comfortable, never overheats or freezes
        var springNative = ZooClimate.evaluateThermalState(0.40f, 0.0f, "SPRING", true, polarBear);
        assertFalse(springNative.tooHot(), "Polar bear in native snow biome during Spring must not overheat");
        assertFalse(springNative.tooCold());

        var winterNative = ZooClimate.evaluateThermalState(-0.50f, 0.0f, "WINTER", true, polarBear);
        assertFalse(winterNative.tooHot());
        assertFalse(winterNative.tooCold());

        // Outside cold biome (e.g. Plains 0.80f): Polar bear overheats unless cooled
        var outsidePlains = ZooClimate.evaluateThermalState(0.80f, 0.0f, "SPRING", false, polarBear);
        assertTrue(outsidePlains.tooHot(), "Polar bear outside cold biome without cooler must feel hot");
        assertFalse(outsidePlains.tooCold());

        // Outside cold biome WITH Cooler lowering temp <= 0.25f: comfortable
        var outsideCooled = ZooClimate.evaluateThermalState(-0.05f, -0.85f, "SPRING", false, polarBear);
        assertFalse(outsideCooled.tooHot());
        assertFalse(outsideCooled.tooCold());

        // In Summer inside native biome: overheats if uncooled, comfortable when Cooler is active
        var summerUncooled = ZooClimate.evaluateThermalState(0.65f, 0.0f, "SUMMER", true, polarBear);
        assertTrue(summerUncooled.tooHot());
        var summerCooled = ZooClimate.evaluateThermalState(0.40f, -0.85f, "SUMMER", true, polarBear);
        assertFalse(summerCooled.tooHot());
    }

    @Test
    @DisplayName("Moderate/temperate species from Alex's Mobs, Naturalist, WilderNature & Vanilla stay comfortable across all seasons")
    void testTemperateSpeciesUnaffectedBySeasons() {
        TestHelper.ensureMinecraftBootstrapped();
        String[] temperateIds = {
                "alexsmobs:tiger", "naturalist:tiger", "alexsmobs:gorilla", "alexsmobs:capuchin_monkey",
                "alexsmobs:toucan", "alexsmobs:grizzly_bear", "naturalist:black_bear", "naturalist:deer",
                "wildernature:deer", "wildernature:minisheep", "naturalist:capybara", "naturalist:tortoise",
                "alexsmobs:anaconda", "naturalist:alligator", "alexsmobs:orca", "minecraft:panda",
                "wildernature:cassowary", "alexsmobs:anteater"
        };
        String[] seasons = {"SPRING", "SUMMER", "FALL", "WINTER"};

        for (String entityId : temperateIds) {
            HabitatProfile p = HabitatProfileLoader.getProfileByIdString(entityId);
            assertNotNull(p, "Missing habitat profile for " + entityId);
            assertFalse(ZooClimate.isHotSpecialist(p), entityId + " must not be hot specialist");
            assertFalse(ZooClimate.isColdSpecialist(p), entityId + " must not be cold specialist");

            for (String season : seasons) {
                var coldWeather = ZooClimate.evaluateThermalState(-0.45f, 0.0f, season, false, p);
                var hotWeather = ZooClimate.evaluateThermalState(2.20f, 0.0f, season, false, p);
                assertFalse(coldWeather.tooCold(), entityId + " should not freeze in " + season);
                assertFalse(coldWeather.tooHot(), entityId + " should not overheat in " + season);
                assertFalse(hotWeather.tooCold(), entityId + " should not freeze in " + season);
                assertFalse(hotWeather.tooHot(), entityId + " should not overheat in " + season);
            }
        }
    }

    @Test
    @DisplayName("All hot-only and cold-only Alex's Mobs, Naturalist & WilderNature species map to proper thermal profiles")
    void testAllModdedSpecialistsClassification() {
        TestHelper.ensureMinecraftBootstrapped();
        String[] hotIds = {
                "naturalist:giraffe", "naturalist:elephant", "alexsmobs:elephant", "wildernature:elephant",
                "naturalist:lion", "alexsmobs:lion", "wildernature:lion", "wildernature:lioness",
                "naturalist:zebra", "naturalist:rhino", "alexsmobs:rhinoceros", "alexsmobs:gazelle",
                "alexsmobs:kangaroo", "naturalist:ostrich", "alexsmobs:emu", "naturalist:hyena",
                "naturalist:hippo", "wildernature:hippo", "minecraft:camel",
                "naturalist:komodo_dragon", "alexsmobs:komodo_dragon", "naturalist:vulture",
                "alexsmobs:roadrunner", "alexsmobs:rattlesnake", "naturalist:rattlesnake",
                "wildernature:scorpion", "naturalist:desert_scorpion", "alexsmobs:jerboa",
                "alexsmobs:tarantula_hawk", "alexsmobs:guster", "alexsmobs:rocky_roller"
        };
        for (String id : hotIds) {
            HabitatProfile p = HabitatProfileLoader.getProfileByIdString(id);
            assertNotNull(p, "Missing profile for hot species " + id);
            assertTrue(ZooClimate.isHotSpecialist(p), id + " must be classified as hot-only specialist");
        }

        String[] coldIds = {
                "minecraft:polar_bear", "alexsmobs:snow_leopard", "alexsmobs:mammoth", "naturalist:mammoth",
                "alexsmobs:froststalker", "alexsmobs:seal", "naturalist:penguin", "wildernature:penguin",
                "minecraft:goat"
        };
        for (String id : coldIds) {
            HabitatProfile p = HabitatProfileLoader.getProfileByIdString(id);
            assertNotNull(p, "Missing profile for cold species " + id);
            assertTrue(ZooClimate.isColdSpecialist(p), id + " must be classified as cold-only specialist");
        }
    }
}
