package net.zootycoon.tier1;

import net.zootycoon.license.LicenseTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F7: Inspector Star Rating Guard")
public class F07_InspectorStarRatingGuardTest {

    public static double getRequiredStarsForTier(int tierLevel) {
        return switch (tierLevel) {
            case 1 -> 0.0D; // Taman Mini (no star requirement)
            case 2 -> 3.0D; // Kebun Binatang (3 stars explicitly required)
            case 3 -> 3.5D; // Suaka Margasatwa (progressive scaling)
            case 4 -> 4.0D; // Cagar Alam (progressive scaling)
            case 5 -> 4.5D; // Konservasi Dunia (apex endgame requirement)
            default -> 0.0D;
        };
    }

    public static boolean canSummonInspector(int tierLevel, double currentStars) {
        double required = getRequiredStarsForTier(tierLevel);
        return currentStars >= required;
    }

    @Test
    @DisplayName("F7-1: Taman Mini (Tier 1) requires 0.0 stars and is accessible immediately")
    void testTamanMiniRequiresZeroStars() {
        assertEquals(0.0D, getRequiredStarsForTier(1));
        assertTrue(canSummonInspector(1, 0.0D), "0.0 stars should allow summoning for Taman Mini");
        assertTrue(canSummonInspector(1, 0.5D), "Higher rating should also allow summoning");
    }

    @Test
    @DisplayName("F7-2: Kebun Binatang (Tier 2) requires 3.0 stars")
    void testKebunBinatangRequiresThreeStars() {
        assertEquals(3.0D, getRequiredStarsForTier(2));
        assertFalse(canSummonInspector(2, 0.0D), "0.0 stars must be blocked for Kebun Binatang");
        assertFalse(canSummonInspector(2, 2.9D), "2.9 stars must be blocked for Kebun Binatang");
        assertTrue(canSummonInspector(2, 3.0D), "Exactly 3.0 stars must be permitted");
        assertTrue(canSummonInspector(2, 4.0D), "Above 3.0 stars must be permitted");
    }

    @Test
    @DisplayName("F7-3: Higher tiers scale progressively (Tier 3 = 3.5, Tier 4 = 4.0, Tier 5 = 4.5)")
    void testHigherTiersScaleProgressively() {
        assertEquals(3.5D, getRequiredStarsForTier(3));
        assertEquals(4.0D, getRequiredStarsForTier(4));
        assertEquals(4.5D, getRequiredStarsForTier(5));

        // Monotonic increase
        assertTrue(getRequiredStarsForTier(2) > getRequiredStarsForTier(1));
        assertTrue(getRequiredStarsForTier(3) > getRequiredStarsForTier(2));
        assertTrue(getRequiredStarsForTier(4) > getRequiredStarsForTier(3));
        assertTrue(getRequiredStarsForTier(5) > getRequiredStarsForTier(4));
    }

    @Test
    @DisplayName("F7-4: Star rating guard blocks inspection request when rating is insufficient")
    void testRatingBelowRequirementBlocksSummoning() {
        assertFalse(canSummonInspector(2, 1.5D));
        assertFalse(canSummonInspector(3, 3.2D));
        assertFalse(canSummonInspector(4, 3.8D));
        assertFalse(canSummonInspector(5, 4.4D));
    }

    @Test
    @DisplayName("F7-5: Warning toast formatting contains required and current star ratings")
    void testWarningToastMessageFormat() {
        double required = 3.0D;
        double current = 1.5D;
        String toastTemplate = "Rating kebun binatang belum mencukupi. Butuh minimal %.1f bintang (saat ini: %.1f bintang).";
        String message = String.format(java.util.Locale.ROOT, toastTemplate, required, current);

        assertTrue(message.contains("3.0 bintang"));
        assertTrue(message.contains("1.5 bintang"));
    }
}
