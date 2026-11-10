package net.zootycoon.tier2;

import net.zootycoon.tier1.F07_InspectorStarRatingGuardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F7: Inspector Star Rating Guard Boundary and Corner Cases")
public class F07_InspectorStarRatingGuardBoundaryTest {

    @Test
    @DisplayName("F7-B1: Kebun Binatang floating point boundary (2.999 vs 3.000 vs 3.001)")
    void testKebunBinatangPrecisionBoundary() {
        assertFalse(F07_InspectorStarRatingGuardTest.canSummonInspector(2, 2.999D), "2.999 stars must be blocked");
        assertTrue(F07_InspectorStarRatingGuardTest.canSummonInspector(2, 3.000D), "3.000 stars must be permitted");
        assertTrue(F07_InspectorStarRatingGuardTest.canSummonInspector(2, 3.001D), "3.001 stars must be permitted");
    }

    @Test
    @DisplayName("F7-B2: Suaka Margasatwa floating point boundary (3.499 vs 3.500)")
    void testSuakaMargasatwaPrecisionBoundary() {
        assertFalse(F07_InspectorStarRatingGuardTest.canSummonInspector(3, 3.499D), "3.499 stars must be blocked");
        assertTrue(F07_InspectorStarRatingGuardTest.canSummonInspector(3, 3.500D), "3.500 stars must be permitted");
    }

    @Test
    @DisplayName("F7-B3: Cagar Alam floating point boundary (3.999 vs 4.000)")
    void testCagarAlamPrecisionBoundary() {
        assertFalse(F07_InspectorStarRatingGuardTest.canSummonInspector(4, 3.999D), "3.999 stars must be blocked");
        assertTrue(F07_InspectorStarRatingGuardTest.canSummonInspector(4, 4.000D), "4.000 stars must be permitted");
    }

    @Test
    @DisplayName("F7-B4: Konservasi Dunia floating point boundary (4.499 vs 4.500 vs 5.000 max)")
    void testKonservasiDuniaPrecisionBoundary() {
        assertFalse(F07_InspectorStarRatingGuardTest.canSummonInspector(5, 4.499D), "4.499 stars must be blocked");
        assertTrue(F07_InspectorStarRatingGuardTest.canSummonInspector(5, 4.500D), "4.500 stars must be permitted");
        assertTrue(F07_InspectorStarRatingGuardTest.canSummonInspector(5, 5.000D), "5.000 max stars must be permitted");
    }

    @Test
    @DisplayName("F7-B5: Negative rating clamp handling (e.g. -1.0 stars clamped to 0.0)")
    void testNegativeRatingClampedToZero() {
        double clamped = Math.clamp(-1.0D, 0.0D, 5.0D);
        assertEquals(0.0D, clamped);
        assertTrue(F07_InspectorStarRatingGuardTest.canSummonInspector(1, clamped),
                "Clamped 0.0 stars satisfies Taman Mini requirement");
    }
}
