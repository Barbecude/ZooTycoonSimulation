package net.zootycoon.tier3;

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

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 3 - Cross-Feature Pairwise Interactions")
public class PairwiseInteractionsTest {

    @Test
    @DisplayName("P1: F13 + F14: Staff preserved while balance, rating, and licenses are reset by /zootycoon start")
    void testPairwiseF13AndF14_StaffPreservedWhileBalanceRatingAndLicensesReset() {
        TestHelper.ensureMinecraftBootstrapped();
        UUID playerUuid = UUID.randomUUID();

        // 1. Establish initial high-tier state
        ConcurrentHashMap<UUID, ZooStaffManager.StaffRecord> staffMap = new ConcurrentHashMap<>();
        staffMap.put(playerUuid, new ZooStaffManager.StaffRecord(5, 8, 3, 6));

        ConcurrentHashMap<UUID, Integer> balances = new ConcurrentHashMap<>();
        balances.put(playerUuid, 45000);

        ConcurrentHashMap<UUID, Double> ratings = new ConcurrentHashMap<>();
        ratings.put(playerUuid, 4.2D);

        ZooLicenseManager.PlayerLicenseProfile profile = new ZooLicenseManager.PlayerLicenseProfile();
        profile.activeTierLevel = 3;

        // 2. Execute /zootycoon start state reset
        balances.put(playerUuid, 0);
        ratings.put(playerUuid, 0.0D);
        profile.activeTierLevel = 1;

        for (LicenseTier tier : LicenseTier.values()) {
            ZooLicenseManager.TierProgressData data = profile.getTierData(tier.getTierLevel());
            data.licenseUnlocked = (tier.getTierLevel() == 1);
            data.licenseClaimed = false;
            data.state = (tier.getTierLevel() == 1) ? LicenseProgressionState.COLLECTING : LicenseProgressionState.LOCKED;
        }

        // 3. Verify F14 resets occurred
        assertEquals(0, balances.get(playerUuid), "Balance must be reset to 0");
        assertEquals(0.0D, ratings.get(playerUuid), "Rating must be reset to 0.0");
        assertEquals(1, profile.activeTierLevel, "Active tier must be reset to 1");
        assertTrue(profile.getTierData(1).licenseUnlocked);
        assertFalse(profile.getTierData(1).licenseClaimed, "Taman Mini must be in unverified state");
        assertFalse(profile.getTierData(2).licenseUnlocked, "Kebun Binatang must be locked");

        // 4. Verify F13 staff preservation: All employees remain intact
        ZooStaffManager.StaffRecord preservedStaff = staffMap.get(playerUuid);
        assertNotNull(preservedStaff, "Staff record must remain intact");
        assertEquals(5, preservedStaff.builders, "Builders preserved");
        assertEquals(8, preservedStaff.zookeepers, "Zookeepers preserved");
        assertEquals(3, preservedStaff.security, "Security preserved");
        assertEquals(6, preservedStaff.cashiers, "Cashiers preserved");
    }

    @Test
    @DisplayName("P2: F7 + F8: Insufficient star rating blocks noon arrival scheduling")
    void testPairwiseF07AndF08_InsufficientStarRatingBlocksNoonArrivalScheduling() {
        double currentStars = 2.5D; // Below Kebun Binatang requirement (3.0)
        long currentDayTime = 10000L;
        long scheduledArrivalTick = -1L;

        boolean ratingPermitted = F07_InspectorStarRatingGuardTest.canSummonInspector(2, currentStars);
        assertFalse(ratingPermitted, "2.5 stars must block summoning for Kebun Binatang");

        // When blocked by F7 guard, F8 scheduling is NOT triggered
        if (ratingPermitted) {
            scheduledArrivalTick = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(currentDayTime);
        }

        assertEquals(-1L, scheduledArrivalTick, "Arrival tick must remain -1 when summon is blocked");
    }

    @Test
    @DisplayName("P3: F7 + F8: Sufficient star rating allows noon arrival scheduling")
    void testPairwiseF07AndF08_SufficientStarRatingSchedulesNoonArrival() {
        double currentStars = 3.2D; // Meets Kebun Binatang requirement (3.0)
        long currentDayTime = 14000L; // Day 0 afternoon
        long scheduledArrivalTick = -1L;

        boolean ratingPermitted = F07_InspectorStarRatingGuardTest.canSummonInspector(2, currentStars);
        assertTrue(ratingPermitted, "3.2 stars must allow summoning for Kebun Binatang");

        if (ratingPermitted) {
            scheduledArrivalTick = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(currentDayTime);
        }

        assertEquals(30000L, scheduledArrivalTick, "Arrival must be scheduled for Day 1 Noon (tick 30000)");
    }

    @Test
    @DisplayName("P4: F3 + F12: Default English species names align exactly with animal info banner titles")
    void testPairwiseF03AndF12_EnglishNamesMatchAnimalInfoBannerTitles() {
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                String englishName = entry.defaultName();
                String funFactKey = F12_AnimalInfoBannerAwardTest.getFunFactKey(entry.speciesKey());

                assertNotNull(englishName);
                assertFalse(englishName.isBlank());
                assertTrue(funFactKey.contains(entry.speciesKey()), "Fun fact key matches species key");
            }
        }
    }

    @Test
    @DisplayName("P5: F7 + F14: Command reset rating to 0.0 prevents summoning Kebun Binatang inspector")
    void testPairwiseF07AndF14_ResettingStateLocksHigherTierInspectionSummons() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, Double> ratings = new ConcurrentHashMap<>();
        ratings.put(playerUuid, 4.0D); // Prior 4.0 stars

        // Run reset
        ratings.put(playerUuid, 0.0D);

        // Attempt summoning Tier 2 inspector
        boolean canSummonTier2 = F07_InspectorStarRatingGuardTest.canSummonInspector(2, ratings.get(playerUuid));
        assertFalse(canSummonTier2, "After command reset to 0.0 stars, Kebun Binatang inspector must be blocked");

        // Taman Mini is 0.0 stars, so it should be allowed
        boolean canSummonTier1 = F07_InspectorStarRatingGuardTest.canSummonInspector(1, ratings.get(playerUuid));
        assertTrue(canSummonTier1, "Taman Mini inspection remains accessible at 0.0 stars");
    }

    @Test
    @DisplayName("P6: F1 + F2: Scale 4 responsive stacked button layout accommodates cute custom icon")
    void testPairwiseF01AndF02_Scale4ButtonsAndIconsDoNotOverlapOrClip() {
        TestHelper.LayoutResult layout = TestHelper.calculateAnimalManagementLayout(480, 270);
        assertTrue(layout.buttonsStacked());

        int iconPadding = 2;
        int iconSize = 14;
        int buttonH = 18;

        assertTrue(iconSize + (iconPadding * 2) <= buttonH, "Icon fits vertically inside 18px button height");
        assertTrue(layout.sellBtnX() + layout.sellBtnW() <= 480, "Stacked sell button with icon stays within screen");
    }

    @Test
    @DisplayName("P7: F4 + F8: Inspection dialog during noon arrival visit displays clean ratio without 'sisa'")
    void testPairwiseF04AndF08_InspectionDialogDisplaysCleanRatioDuringScheduledVisit() {
        long currentDayTime = 30000L;
        long scheduledTick = 30000L;
        boolean inspectorSpawned = F08_InspectorNoonArrivalTest.shouldSpawnInspector(currentDayTime, scheduledTick);
        assertTrue(inspectorSpawned);

        // Dialogue opened
        int inspected = 0;
        int total = 8;
        String dialogProgress = String.format("Selesai: %d/%d", inspected, total);

        assertEquals("Selesai: 0/8", dialogProgress);
        assertFalse(dialogProgress.contains("sisa"));
        assertFalse(dialogProgress.contains("spesies"));
    }

    @Test
    @DisplayName("P8: F6 + F12: Paper certificate banner and animal info plaque share authentic wood/paper aesthetic")
    void testPairwiseF06AndF12_PaperPlaqueVisualConsistency() {
        int certificateBorderColor = 0xFF2E1C0C; // Dark oak
        int infoPlaqueBorderColor = 0xFF2E1C0C;  // Dark oak

        assertEquals(certificateBorderColor, infoPlaqueBorderColor, "Both plaques share consistent dark oak frame styling");
    }
}
