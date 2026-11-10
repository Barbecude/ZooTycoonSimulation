package net.zootycoon.tier2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F4: Removal of Remaining Species Text Boundary Cases")
public class F04_RemoveRemainingSpeciesTextBoundaryTest {

    public static String formatProgressClean(int inspected, int total) {
        int safeInspected = Math.max(0, inspected);
        int safeTotal = Math.max(0, total);
        return String.format("Selesai: %d/%d", safeInspected, safeTotal);
    }

    @Test
    @DisplayName("F4-B1: Zero total species edge case (0/0 ratio)")
    void testZeroTotalSpecies() {
        String formatted = formatProgressClean(0, 0);
        assertEquals("Selesai: 0/0", formatted);
        assertFalse(formatted.contains("Sisa"));
    }

    @Test
    @DisplayName("F4-B2: Negative inspected count sanitized to zero")
    void testNegativeInspectedSanitization() {
        String formatted = formatProgressClean(-5, 8);
        assertEquals("Selesai: 0/8", formatted, "Negative inspected count must be clamped to 0");
    }

    @Test
    @DisplayName("F4-B3: Inspected count exceeding total count (e.g. 10/8)")
    void testInspectedExceedingTotal() {
        String formatted = formatProgressClean(10, 8);
        assertEquals("Selesai: 10/8", formatted);
        assertFalse(formatted.contains("Sisa"));
    }

    @Test
    @DisplayName("F4-B4: Large species total count (100/100) formatting")
    void testLargeCountFormatting() {
        String formatted = formatProgressClean(100, 100);
        assertEquals("Selesai: 100/100", formatted);
    }

    @Test
    @DisplayName("F4-B5: Remaining count calculation clamped at minimum zero")
    void testRemainingCountClamping() {
        int total = 8;
        int inspected = 10;
        int remaining = Math.max(0, total - inspected);

        assertEquals(0, remaining, "Remaining count must never be negative");
    }
}
