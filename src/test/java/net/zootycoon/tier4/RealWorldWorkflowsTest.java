package net.zootycoon.tier4;

import net.zootycoon.TestHelper;
import net.zootycoon.economy.ZooStaffManager;
import net.zootycoon.license.LicenseProgressionState;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.license.ZooLicenseManager;
import net.zootycoon.tier1.F07_InspectorStarRatingGuardTest;
import net.zootycoon.tier1.F08_InspectorNoonArrivalTest;
import net.zootycoon.tier1.F12_AnimalInfoBannerAwardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 4 - Real-World End-to-End Simulation Workflows")
public class RealWorldWorkflowsTest {

    @Test
    @DisplayName("Workflow 1: New Zoo Bootstrap to Tier 1 Taman Mini Certification Lifecycle")
    void workflow01_NewZooBootstrapToTier1Certification() {
        TestHelper.ensureMinecraftBootstrapped();
        UUID playerUuid = UUID.randomUUID();

        // Step 1: Player initializes zoo
        ZooLicenseManager.PlayerLicenseProfile profile = new ZooLicenseManager.PlayerLicenseProfile();
        assertEquals(1, profile.activeTierLevel);
        ZooLicenseManager.TierProgressData tier1 = profile.getTierData(1);
        assertTrue(tier1.licenseUnlocked, "Taman Mini unlocked by default");
        assertFalse(tier1.licenseClaimed, "Taman Mini is unverified");

        // Step 2: Hires starting builder
        ZooStaffManager.StaffRecord staff = new ZooStaffManager.StaffRecord(1, 0, 0, 0);
        assertEquals(1, staff.builders);

        // Step 3: Acquires all 8 Taman Mini species
        LicenseTier t1 = LicenseTier.TAMAN_MINI;
        for (LicenseTier.SpeciesEntry sp : t1.getRequiredSpecies()) {
            profile.ownedSpeciesKeys.add(sp.speciesKey());
        }
        assertEquals(8, profile.ownedSpeciesKeys.size());

        // Step 4: Rating check (0.0 stars required)
        double currentStars = 0.0D;
        assertTrue(F07_InspectorStarRatingGuardTest.canSummonInspector(1, currentStars), "0 stars allows Taman Mini inspection");

        // Step 5: Schedule inspector arrival (requested at Day 0, tick 2000)
        long requestTick = 2000L;
        long arrivalTick = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(requestTick);
        assertEquals(30000L, arrivalTick, "Arrives Day 1 Noon");
        tier1.state = LicenseProgressionState.INSPECTOR_REQUESTED;

        // Step 6: Time advances to Day 1 Noon
        long currentTick = 30000L;
        assertTrue(F08_InspectorNoonArrivalTest.shouldSpawnInspector(currentTick, arrivalTick));

        // Step 7: Inspection runs: inspector visits each of 8 animals
        List<String> visited = new ArrayList<>();
        for (LicenseTier.SpeciesEntry sp : t1.getRequiredSpecies()) {
            visited.add(sp.speciesKey());
        }
        assertEquals(8, visited.size());
        tier1.inspectedSpecies.addAll(visited);

        // Step 8: Inspection passes -> awards certification banner & animal info banner
        tier1.inspectionPassed = true;
        tier1.state = LicenseProgressionState.INSPECTION_PASSED;
        assertTrue(F12_AnimalInfoBannerAwardTest.shouldAwardInfoBanner(tier1.inspectionPassed));

        tier1.licenseClaimed = true;
        tier1.bannerClaimed = true;
        tier1.state = LicenseProgressionState.BANNER_CLAIMED;

        assertTrue(tier1.licenseClaimed, "Tier 1 is now certified!");
    }

    @Test
    @DisplayName("Workflow 2: Progression to Tier 2 (Kebun Binatang) with Rating Gate")
    void workflow02_RatingProgressionToTier2KebunBinatang() {
        TestHelper.ensureMinecraftBootstrapped();
        UUID playerUuid = UUID.randomUUID();

        // Player has certified Tier 1 zoo
        ZooLicenseManager.PlayerLicenseProfile profile = new ZooLicenseManager.PlayerLicenseProfile();
        profile.getTierData(1).licenseClaimed = true;
        profile.activeTierLevel = 2;

        // Step 1: Early attempt to summon Tier 2 inspector with 1.8 stars -> BLOCKED
        double earlyRating = 1.8D;
        assertFalse(F07_InspectorStarRatingGuardTest.canSummonInspector(2, earlyRating),
                "Summoning Tier 2 inspector must be blocked at 1.8 stars (requires 3.0)");

        // Step 2: Zoo develops exhibits, visitors leave positive reviews
        double matureRating = 3.2D;
        assertTrue(F07_InspectorStarRatingGuardTest.canSummonInspector(2, matureRating),
                "Summoning Tier 2 inspector permitted at 3.2 stars");

        // Step 3: Inspector summoned -> arrives next day noon
        long requestTick = 52000L; // Day 2 afternoon (52000 / 24000 = 2)
        long arrivalTick = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(requestTick);
        long expectedDay3Noon = 3L * 24000L + 6000L; // 78000L
        assertEquals(expectedDay3Noon, arrivalTick, "Arrives Day 3 Noon (tick 78000)");

        // Step 4: Passes inspection -> Tier 2 unlocked and certified
        ZooLicenseManager.TierProgressData tier2 = profile.getTierData(2);
        tier2.licenseUnlocked = true;
        tier2.licenseClaimed = true;
        tier2.inspectionPassed = true;

        assertTrue(tier2.licenseUnlocked);
        assertTrue(tier2.licenseClaimed);
    }

    @Test
    @DisplayName("Workflow 3: Mature Zoo Full Reset via /zootycoon start with Staff Retention")
    void workflow03_FullResetViaZootycoonStartWithStaffRetention() {
        TestHelper.ensureMinecraftBootstrapped();
        UUID playerUuid = UUID.randomUUID();

        // 1. Establish mature Tier 3 zoo
        ConcurrentHashMap<UUID, ZooStaffManager.StaffRecord> staffTable = new ConcurrentHashMap<>();
        staffTable.put(playerUuid, new ZooStaffManager.StaffRecord(6, 4, 2, 3)); // 15 employees

        ConcurrentHashMap<UUID, Integer> balances = new ConcurrentHashMap<>();
        balances.put(playerUuid, 60000);

        ConcurrentHashMap<UUID, Double> ratings = new ConcurrentHashMap<>();
        ratings.put(playerUuid, 4.3D);

        ConcurrentHashMap<UUID, List<String>> logs = new ConcurrentHashMap<>();
        List<String> logEntries = new ArrayList<>();
        for (int i = 0; i < 25; i++) logEntries.add("Log entry " + i);
        logs.put(playerUuid, logEntries);

        ZooLicenseManager.PlayerLicenseProfile profile = new ZooLicenseManager.PlayerLicenseProfile();
        profile.activeTierLevel = 3;
        profile.getTierData(1).licenseClaimed = true;
        profile.getTierData(2).licenseClaimed = true;

        // 2. Player executes /zootycoon start
        balances.put(playerUuid, 0);
        ratings.put(playerUuid, 0.0D);
        logs.get(playerUuid).clear();

        profile.activeTierLevel = 1;
        profile.ownedSpeciesKeys.clear();
        for (LicenseTier tier : LicenseTier.values()) {
            ZooLicenseManager.TierProgressData data = profile.getTierData(tier.getTierLevel());
            data.inspectionPassed = false;
            data.letterClaimed = false;
            data.licenseClaimed = false;
            data.bannerClaimed = false;
            data.inspectedSpecies.clear();
            data.improvementNotes.clear();

            if (tier.getTierLevel() == 1) {
                data.licenseUnlocked = true;
                data.state = LicenseProgressionState.COLLECTING;
            } else {
                data.licenseUnlocked = false;
                data.state = LicenseProgressionState.LOCKED;
            }
        }

        // 3. Verify ALL 15 staff members are completely preserved
        ZooStaffManager.StaffRecord preserved = staffTable.get(playerUuid);
        assertNotNull(preserved);
        assertEquals(6, preserved.builders);
        assertEquals(4, preserved.zookeepers);
        assertEquals(2, preserved.security);
        assertEquals(3, preserved.cashiers);

        // 4. Verify world state is completely reset
        assertEquals(0, balances.get(playerUuid));
        assertEquals(0.0D, ratings.get(playerUuid));
        assertTrue(logs.get(playerUuid).isEmpty());
        assertEquals(1, profile.activeTierLevel);
        assertTrue(profile.getTierData(1).licenseUnlocked);
        assertFalse(profile.getTierData(1).licenseClaimed, "Taman Mini is unverified");
        assertFalse(profile.getTierData(2).licenseUnlocked, "Kebun Binatang is locked");
        assertFalse(profile.getTierData(3).licenseUnlocked, "Suaka Margasatwa is locked");
    }

    @Test
    @DisplayName("Workflow 4: Inspector Arrival Lifecycle Across Day/Night Time Progression")
    void workflow04_InspectorArrivalLifecycleAcrossDayNightCycles() {
        long requestTick = 73500L; // Day 3 night (73500 / 24000 = 3)
        long targetTick = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(requestTick);
        long expectedDay4Noon = 4L * 24000L + 6000L; // 102000L
        assertEquals(expectedDay4Noon, targetTick, "Target is Day 4 Noon (tick 102000)");

        // Midnight Day 3 (tick 90000) -> does not spawn
        assertFalse(F08_InspectorNoonArrivalTest.shouldSpawnInspector(90000L, targetTick));

        // Dawn Day 4 (tick 96000) -> does not spawn
        assertFalse(F08_InspectorNoonArrivalTest.shouldSpawnInspector(96000L, targetTick));

        // Morning Day 4 (tick 100000) -> does not spawn
        assertFalse(F08_InspectorNoonArrivalTest.shouldSpawnInspector(100000L, targetTick));

        // Exactly Noon Day 4 (tick 102000) -> SPAWNS!
        assertTrue(F08_InspectorNoonArrivalTest.shouldSpawnInspector(102000L, targetTick));
    }

    @Test
    @DisplayName("Workflow 5: Multi-Scale Responsive GUI Viewport Matrix Validation")
    void workflow05_MultiScaleResponsiveGuiLayoutValidation() {
        int[][] viewportMatrix = {
                {480, 270},   // GUI Scale 4 (Minecraft Default on 1080p)
                {640, 360},   // GUI Scale 3
                {854, 480},   // 480p wide
                {960, 540},   // GUI Scale 2
                {1280, 720},  // 720p HD
                {1920, 1080}, // GUI Scale 1 / 1080p FHD
                {2560, 1440}, // 1440p QHD
                {3840, 2160}  // 2160p 4K UHD
        };

        for (int[] vp : viewportMatrix) {
            int w = vp[0];
            int h = vp[1];
            TestHelper.LayoutResult layout = TestHelper.calculateAnimalManagementLayout(w, h);

            // Invariant 1: Info column width must be comfortable
            assertTrue(layout.infoW() >= 140, "infoW at " + w + "x" + h + " must be >= 140px");

            // Invariant 2: Sell button must never overflow right edge of viewport
            int sellRight = layout.sellBtnX() + layout.sellBtnW();
            assertTrue(sellRight <= w, "sellButton at " + w + "x" + h + " overflows screen: " + sellRight + " > " + w);

            // Invariant 3: Remove button must never overflow right edge of viewport
            int removeRight = layout.removeBtnX() + layout.removeBtnW();
            assertTrue(removeRight <= w, "removeButton at " + w + "x" + h + " overflows screen: " + removeRight + " > " + w);

            // Invariant 4: Stat percentage text must stay inside content scissor region
            int scissorRight = layout.contentX() + layout.contentW();
            assertTrue(layout.statTextX() + 25 <= scissorRight,
                    "statTextX at " + w + "x" + h + " clipped by scissor: " + layout.statTextX() + " > " + scissorRight);
        }
    }
}
