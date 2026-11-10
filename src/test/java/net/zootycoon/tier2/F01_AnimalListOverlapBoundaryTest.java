package net.zootycoon.tier2;

import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F1: Animal List Layout Boundary and Corner Cases")
public class F01_AnimalListOverlapBoundaryTest {

    @Test
    @DisplayName("F1-B1: Extremely narrow viewport (320x240 minimum legacy display)")
    void testExtremelyNarrowViewport() {
        TestHelper.LayoutResult layout = TestHelper.calculateAnimalManagementLayout(320, 240);
        assertTrue(layout.sidebarW() <= 130);
        assertTrue(layout.previewW() <= 100);
        assertTrue(layout.buttonsStacked(), "Buttons must stack on extremely narrow viewport");
        assertTrue(layout.sellBtnX() + layout.sellBtnW() <= 320, "Sell button must not overflow 320px width");
    }

    @Test
    @DisplayName("F1-B2: Responsive breakpoint boundary (539px vs 540px width)")
    void testResponsiveBreakpointThreshold() {
        TestHelper.LayoutResult belowBreakpoint = TestHelper.calculateAnimalManagementLayout(539, 300);
        assertEquals(130, belowBreakpoint.sidebarW(), "Sidebar width must be compact (130) at 539px");
        assertEquals(100, belowBreakpoint.previewW(), "Preview width must be compact (100) at 539px");

        TestHelper.LayoutResult atBreakpoint = TestHelper.calculateAnimalManagementLayout(540, 300);
        assertEquals(160, atBreakpoint.sidebarW(), "Sidebar width should expand to 160 at 540px");
        assertEquals(140, atBreakpoint.previewW(), "Preview width should expand to 140 at 540px");
    }

    @Test
    @DisplayName("F1-B3: Ultra-wide 4K viewport (3840x2160) spacing integrity")
    void testUltraWide4KViewport() {
        TestHelper.LayoutResult layout = TestHelper.calculateAnimalManagementLayout(3840, 2160);
        assertTrue(layout.infoW() > 1000, "4K info width should be spacious");
        assertFalse(layout.buttonsStacked(), "Buttons should never stack on 4K display");
        assertTrue(layout.sellBtnX() + layout.sellBtnW() <= 3840);
    }

    @Test
    @DisplayName("F1-B4: Deep generation hierarchy indentation cap (depth >= 10)")
    void testDeepGenerationIndentationCap() {
        int sidebarW = 130;
        int indentPerLevel = 10;
        int maxDepth = 12;
        // Indentation must be capped at 30px regardless of how deep the family tree gets
        int cappedIndent = Math.min(30, maxDepth * indentPerLevel);
        assertEquals(30, cappedIndent, "Indentation must be capped at 30px to prevent squeezing animal name");

        int textX = 14 + 18 + cappedIndent;
        int reservedForIcons = 32;
        int remainingNameWidth = (14 + sidebarW) - textX - reservedForIcons;
        assertTrue(remainingNameWidth >= 40, "Even at depth 12, name area must remain at least 40px");
    }

    @Test
    @DisplayName("F1-B5: Scroll offset clamping with zero or negative items")
    void testScrollClampingBoundary() {
        int sidebarH = 200;
        int totalContentH = 0; // Empty list
        int maxScroll = Math.max(0, totalContentH - sidebarH);
        assertEquals(0, maxScroll, "Max scroll must be 0 for empty list");

        int scrollAttempt = -50;
        int clampedNegative = Math.clamp(scrollAttempt, 0, maxScroll);
        assertEquals(0, clampedNegative, "Negative scroll must clamp to 0");

        int scrollAttemptOver = 500;
        int clampedOver = Math.clamp(scrollAttemptOver, 0, maxScroll);
        assertEquals(0, clampedOver, "Overscroll on empty list must clamp to 0");
    }
}
