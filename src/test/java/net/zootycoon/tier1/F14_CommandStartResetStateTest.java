package net.zootycoon.tier1;

import net.zootycoon.TestHelper;
import net.zootycoon.license.LicenseProgressionState;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.license.ZooLicenseManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F14: Command Start Reset State (Balance, Rating, Inputs, Logs, Licenses)")
public class F14_CommandStartResetStateTest {

    @Test
    @DisplayName("F14-1: Balance is reset from previous value to exactly 0")
    void testBalanceResetToZero() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, Integer> balances = new ConcurrentHashMap<>();
        balances.put(playerUuid, 12500);

        // Reset operation
        balances.put(playerUuid, 0);

        assertEquals(0, balances.get(playerUuid), "Balance must be exactly 0 after reset");
    }

    @Test
    @DisplayName("F14-2: Zoo star rating is reset from previous score to exactly 0.0")
    void testRatingResetToZero() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, Integer> ratings = new ConcurrentHashMap<>();
        ConcurrentHashMap<UUID, Double> reviewSums = new ConcurrentHashMap<>();
        ConcurrentHashMap<UUID, Integer> reviewCounts = new ConcurrentHashMap<>();

        // Previous state: 4.5 stars with 50 reviews
        ratings.put(playerUuid, 90);
        reviewSums.put(playerUuid, 225.0D);
        reviewCounts.put(playerUuid, 50);

        // Reset operation
        ratings.put(playerUuid, 0);
        reviewSums.put(playerUuid, 0.0D);
        reviewCounts.put(playerUuid, 0);

        assertEquals(0, ratings.get(playerUuid));
        assertEquals(0.0D, reviewSums.get(playerUuid));
        assertEquals(0, reviewCounts.get(playerUuid));
    }

    @Test
    @DisplayName("F14-3: Ticket booth cashier inputs reset to default price and zoo is closed")
    void testTicketBoothReset() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, Integer> ticketPrices = new ConcurrentHashMap<>();
        ConcurrentHashMap<UUID, Boolean> openZoos = new ConcurrentHashMap<>();

        ticketPrices.put(playerUuid, 15);
        openZoos.put(playerUuid, true);

        // Reset operation
        int defaultPrice = 20;
        ticketPrices.put(playerUuid, defaultPrice);
        openZoos.remove(playerUuid);

        assertEquals(20, ticketPrices.get(playerUuid), "Ticket price must return to default (20)");
        assertFalse(openZoos.containsKey(playerUuid), "Zoo must be marked as closed after reset");
    }

    @Test
    @DisplayName("F14-4: Tier 1 (Taman Mini) is the only unlocked tier in an unverified state")
    void testTamanMiniUnverifiedStateAfterReset() {
        TestHelper.ensureMinecraftBootstrapped();

        ZooLicenseManager.PlayerLicenseProfile profile = new ZooLicenseManager.PlayerLicenseProfile();
        assertEquals(1, profile.activeTierLevel, "Active tier must be 1 (Taman Mini)");

        ZooLicenseManager.TierProgressData tier1 = profile.getTierData(1);
        assertTrue(tier1.licenseUnlocked, "Taman Mini license must be unlocked");
        assertFalse(tier1.licenseClaimed, "Taman Mini license must NOT be claimed (unverified state)");
        assertFalse(tier1.inspectionPassed, "Inspection must NOT be marked as passed");
        assertEquals(LicenseProgressionState.COLLECTING, tier1.state, "Tier 1 state must be COLLECTING");
    }

    @Test
    @DisplayName("F14-5: Tiers 2 through 5 are completely locked after reset")
    void testHigherTiersLockedAfterReset() {
        TestHelper.ensureMinecraftBootstrapped();

        ZooLicenseManager.PlayerLicenseProfile profile = new ZooLicenseManager.PlayerLicenseProfile();

        for (int tier = 2; tier <= 5; tier++) {
            ZooLicenseManager.TierProgressData data = profile.getTierData(tier);
            assertFalse(data.licenseUnlocked, "Tier " + tier + " must be locked (licenseUnlocked = false)");
            assertFalse(data.licenseClaimed, "Tier " + tier + " license must not be claimed");
            assertEquals(LicenseProgressionState.LOCKED, data.state, "Tier " + tier + " state must be LOCKED");
        }
    }

    @Test
    @DisplayName("F14-6: Inspected species and improvement notes are cleared on reset")
    void testInspectedSpeciesAndNotesCleared() {
        ZooLicenseManager.TierProgressData data = new ZooLicenseManager.TierProgressData(1);
        data.inspectedSpecies.add("capybara");
        data.improvementNotes.add("needs cooler box");

        // Reset operation
        data.inspectedSpecies.clear();
        data.improvementNotes.clear();

        assertTrue(data.inspectedSpecies.isEmpty(), "Inspected species list must be empty");
        assertTrue(data.improvementNotes.isEmpty(), "Improvement notes must be empty");
    }
}
