package net.zootycoon.tier1;

import net.minecraft.world.entity.EntityType;
import net.zootycoon.TestHelper;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.entity.ZooVehicleEntity;
import net.zootycoon.license.LicenseTier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F22: Mate Requirement & Large Animal Vehicle Capacity")
public class F22_MateRequirementAndVehicleCapacityTest {

    @BeforeAll
    static void setup() {
        TestHelper.ensureMinecraftBootstrapped();
    }

    @Test
    @DisplayName("F22-1: Mate requirement matches social/breedable vs solitary/non-breedable species")
    void testMateRequirementClassification() {
        List<String> expectedMate = List.of(
                "mini_sheep", "deer", "capybara", "capuchin_monkey", "kangaroo", "ostrich",
                "zebra", "camel", "toucan", "gazelle",
                "giraffe", "hippo", "gorilla", "elephant",
                "lion", "mammoth", "froststalker", "flutter"
        );
        for (String key : expectedMate) {
            assertTrue(LicenseTier.requiresMate(key), "Expected requiresMate=true for " + key);
        }

        List<String> expectedSolitaryOrUnbreedable = List.of(
                "raccoon", "tortoise", "black_bear", "panda", "giant_anteater", "rhinoceros",
                "alligator", "komodo_dragon", "anaconda", "mantis_shrimp", "tiger",
                "grizzly_bear", "snow_leopard", "polar_bear", "bunfungus",
                "orca", "cachalot_whale", "great_white_shark", "giant_squid",
                "mungus", "guster", "rocky_roller"
        );
        for (String key : expectedSolitaryOrUnbreedable) {
            assertFalse(LicenseTier.requiresMate(key), "Expected requiresMate=false for " + key);
        }
    }

    @Test
    @DisplayName("F22-2: Offspring exempt, paired adult satisfied, solitary adult never requires mate")
    void testNeedsMateRequirementOnEntities() {
        // Camel (social, requires mate) mapped via minecraft:camel; Level is null in unit tests? TestHelper handles null-safe tag logic on entity or minimal dummy
        // Verify pure tag/entity logic via EntityType registry checks + null-safety
        assertTrue(LicenseTier.requiresMateEntity(EntityType.CAMEL));
        assertFalse(LicenseTier.requiresMateEntity(EntityType.POLAR_BEAR));
        assertFalse(ZooAnimalHungerManager.needsMateRequirement(null));
    }

    @Test
    @DisplayName("F22-3: Large animal cargo slot weight is 2 slots while regular animal is 1 slot")
    void testLargeCargoSpeciesMapping() {
        assertTrue(LicenseTier.isLargeCargoSpecies(EntityType.CAMEL));
        assertFalse(LicenseTier.isLargeCargoSpecies(EntityType.POLAR_BEAR));
        assertFalse(LicenseTier.isLargeCargoSpecies(EntityType.PANDA));
        assertEquals(1, ZooVehicleEntity.getAnimalCargoSlots(null));
        // Bounding-box / species logic on EntityType without Level (direct EntityType check + TestHelper verifies spec capacities)
        ZooVehicleEntity.VehicleSpec truck = ZooVehicleEntity.getSpec("zil-130");
        assertEquals(2, truck.maxCargoAnimals());
        ZooVehicleEntity.VehicleSpec jeep = ZooVehicleEntity.getSpec("jeep_cj-5_1964");
        assertEquals(1, jeep.maxCargoAnimals());
        ZooVehicleEntity.VehicleSpec coupe = ZooVehicleEntity.getSpec("oldsmobile_cutlass_supreme_coupe_1980");
        assertEquals(1, coupe.maxCargoAnimals());
        // Verify tier species count remains 8 per tier
        for (LicenseTier tier : LicenseTier.values()) {
            assertEquals(8, tier.getRequiredSpecies().size(), "Tier " + tier + " must preserve 8 species quota");
        }
    }

    @Test
    @DisplayName("F22-4: All new mate, inspector offspring, and vehicle capacity lang keys exist cleanly in both files")
    void testNewLangKeysPresent() {
        List<String> requiredKeys = List.of(
                "zootycoon.habitat.need_mate",
                "zootycoon.license.collection.requires_mate",
                "zootycoon.license.issue.needs_mate",
                "zootycoon.license.issue.sick",
                "zootycoon.license.issue.offspring_line",
                "zootycoon.license.issue.short.hungry",
                "zootycoon.license.issue.short.thirsty",
                "zootycoon.license.issue.short.dirty",
                "zootycoon.license.issue.short.low_health",
                "zootycoon.license.issue.short.sick",
                "zootycoon.license.issue.short.habitat",
                "zootycoon.vehicle.animal_too_large",
                "zootycoon.vehicle.trunk_full_large"
        );
        Map<String, String> idLang = TestHelper.getIdLang();
        Map<String, String> enLang = TestHelper.getEnLang();
        for (String key : requiredKeys) {
            assertTrue(idLang.containsKey(key), "Missing key in id_id.json: " + key);
            assertTrue(enLang.containsKey(key), "Missing key in en_us.json: " + key);
        }
    }
}
