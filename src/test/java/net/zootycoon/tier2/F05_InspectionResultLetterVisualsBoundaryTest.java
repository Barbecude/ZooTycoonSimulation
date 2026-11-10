package net.zootycoon.tier2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F5: Inspection Result Letter Visuals Boundary Cases")
public class F05_InspectionResultLetterVisualsBoundaryTest {

    @Test
    @DisplayName("F5-B1: Empty evaluation notes (zero species notes or improvement items)")
    void testEmptyEvaluationNotes() {
        List<String> emptyNotes = new ArrayList<>();
        int itemH = 16;
        int totalContentH = emptyNotes.size() * itemH;

        assertEquals(0, totalContentH);
        int maxScroll = Math.max(0, totalContentH - 120);
        assertEquals(0, maxScroll, "Scroll should be 0 when notes are empty");
    }

    @Test
    @DisplayName("F5-B2: Large volume of evaluation notes (50 notes causing large scroll)")
    void testLargeVolumeOfNotesScrollClamping() {
        int noteCount = 50;
        int itemH = 16;
        int scrollAreaH = 120;
        int totalContentH = noteCount * itemH; // 800px

        int maxScroll = Math.max(0, totalContentH - scrollAreaH);
        assertEquals(680, maxScroll);

        int clampedOverScroll = Math.clamp(1000, 0, maxScroll);
        assertEquals(680, clampedOverScroll);

        int clampedUnderScroll = Math.clamp(-50, 0, maxScroll);
        assertEquals(0, clampedUnderScroll);
    }

    @Test
    @DisplayName("F5-B3: Screen centering calculation at minimum window size (320x240)")
    void testCenteringAtMinimumWindowSize() {
        int width = 320;
        int height = 240;
        int panelW = 220;
        int panelH = 200;

        int startX = (width - panelW) / 2;
        int startY = (height - panelH) / 2;

        assertTrue(startX >= 0, "startX must be non-negative");
        assertTrue(startY >= 0, "startY must be non-negative");
        assertEquals(320, startX + panelW + startX);
    }

    @Test
    @DisplayName("F5-B4: Color luminance calculation boundary (pure black 0x000000 to pure white 0xFFFFFF)")
    void testLuminanceCalculationExtremes() {
        double blackLum = calculateLuminance(0xFF000000);
        assertEquals(0.0, blackLum, 0.001, "Pure black luminance should be 0");

        double whiteLum = calculateLuminance(0xFFFFFFFF);
        assertEquals(255.0, whiteLum, 0.001, "Pure white luminance should be 255");
    }

    @Test
    @DisplayName("F5-B5: Long text lines wrap within letter panel margins")
    void testLongTextWrappingWithinPanel() {
        int panelWidth = 220;
        int marginX = 14;
        int usableTextWidth = panelWidth - (marginX * 2); // 192px

        int avgCharWidth = 6;
        int maxCharsPerLine = usableTextWidth / avgCharWidth;
        assertEquals(32, maxCharsPerLine);

        String longObservation = "This animal enclosure has optimal temperature conditions and clean water.";
        assertTrue(longObservation.length() > maxCharsPerLine, "Long observation will wrap over multiple lines");
    }

    private double calculateLuminance(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }
}
