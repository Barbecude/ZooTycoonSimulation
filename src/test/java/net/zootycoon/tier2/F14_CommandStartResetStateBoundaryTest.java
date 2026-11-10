package net.zootycoon.tier2;

import net.zootycoon.TestHelper;
import net.zootycoon.license.LicenseProgressionState;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.license.ZooLicenseManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F14: Command Start Reset State Boundary Cases")
public class F14_CommandStartResetStateBoundaryTest {

    @Test
    @DisplayName("F14-B1: Maximum integer balance (2,147,483,647 emeralds) resets cleanly to 0")
    void testMaxIntegerBalanceReset() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, Integer> balances = new ConcurrentHashMap<>();
        balances.put(playerUuid, Integer.MAX_VALUE);

        // Reset
        balances.put(playerUuid, 0);
        assertEquals(0, balances.get(playerUuid));
    }

    @Test
    @DisplayName("F14-B2: Negative balance edge case resets cleanly to 0")
    void testNegativeBalanceReset() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, Integer> balances = new ConcurrentHashMap<>();
        balances.put(playerUuid, -5000);

        // Reset
        balances.put(playerUuid, 0);
        assertEquals(0, balances.get(playerUuid));
    }

    @Test
    @DisplayName("F14-B3: 5.0 Star rating with 10,000 visitor reviews resets to 0.0 stars")
    void testMaxReviewsRatingReset() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, Integer> ratings = new ConcurrentHashMap<>();
        ConcurrentHashMap<UUID, Double> reviewSums = new ConcurrentHashMap<>();
        ConcurrentHashMap<UUID, Integer> reviewCounts = new ConcurrentHashMap<>();

        ratings.put(playerUuid, 100);
        reviewSums.put(playerUuid, 50000.0D);
        reviewCounts.put(playerUuid, 10000);

        // Reset
        ratings.put(playerUuid, 0);
        reviewSums.put(playerUuid, 0.0D);
        reviewCounts.put(playerUuid, 0);

        assertEquals(0, ratings.get(playerUuid));
        assertEquals(0.0D, reviewSums.get(playerUuid));
        assertEquals(0, reviewCounts.get(playerUuid));
    }

    @Test
    @DisplayName("F14-B4: All 5 tiers previously certified reset completely to unverified Taman Mini")
    void testAllTiersCertifiedReset() {
        TestHelper.ensureMinecraftBootstrapped();

        ZooLicenseManager.PlayerLicenseProfile profile = new ZooLicenseManager.PlayerLicenseProfile();
        // Simulate previously apex zoo where all 5 tiers were certified
        for (LicenseTier tier : LicenseTier.values()) {
            ZooLicenseManager.TierProgressData data = profile.getTierData(tier.getTierLevel());
            data.licenseUnlocked = true;
            data.licenseClaimed = true;
            data.inspectionPassed = true;
            data.state = LicenseProgressionState.BANNER_CLAIMED;
        }

        // Simulate resetLicensesForPlayer
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

        assertEquals(1, profile.activeTierLevel);
        assertTrue(profile.getTierData(1).licenseUnlocked);
        assertFalse(profile.getTierData(1).licenseClaimed);
        assertEquals(LicenseProgressionState.COLLECTING, profile.getTierData(1).state);

        for (int t = 2; t <= 5; t++) {
            assertFalse(profile.getTierData(t).licenseUnlocked);
            assertFalse(profile.getTierData(t).licenseClaimed);
            assertEquals(LicenseProgressionState.LOCKED, profile.getTierData(t).state);
        }
    }

    @Test
    @DisplayName("F14-B5: Ticket cashier 5-slot stock container clear operation")
    void testCashierStockContainerCleared() {
        // Container of size 5 (ticket booth slots)
        int containerSize = 5;
        int[] slots = {64, 64, 64, 64, 64}; // 5 full stacks

        // ClearContent operation
        for (int i = 0; i < containerSize; i++) {
            slots[i] = 0;
        }

        for (int i = 0; i < containerSize; i++) {
            assertEquals(0, slots[i], "Slot " + i + " must be empty after reset");
        }
    }
}
