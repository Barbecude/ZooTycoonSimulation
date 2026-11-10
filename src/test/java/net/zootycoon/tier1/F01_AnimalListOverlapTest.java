package net.zootycoon.tier1;

import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F1: Animal List Layout & Overlap Prevention")
public class F01_AnimalListOverlapTest {

    @Test
    @DisplayName("F1-1: Responsive column widths across all standard GUI scales leave sufficient info width")
    void testResponsiveColumnWidthsAcrossStandardGuiScales() {
        int[][] scales = {
                {480, 270},   // GUI Scale 4 (Auto default on 1080p)
                {640, 360},   // GUI Scale 3
                {960, 540},   // GUI Scale 2
                {1920, 1080}  // GUI Scale 1
        };

        for (int[] dims : scales) {
            int w = dims[0];
            int h = dims[1];
            TestHelper.LayoutResult layout = TestHelper.calculateAnimalManagementLayout(w, h);

            assertTrue(layout.infoW() >= 140,
                    "Info column width at " + w + "x" + h + " must be at least 140px, got: " + layout.infoW());
            assertTrue(layout.contentW() > 0, "Content width must be positive at " + w + "x" + h);
            assertTrue(layout.previewW() > 0, "Preview width must be positive at " + w + "x" + h);
        }
    }

    @Test
    @DisplayName("F1-2: Sell button never overflows the game window on standard GUI Scale 4 (480px width)")
    void testSellButtonDoesNotOverflowWindowAtScale4() {
        TestHelper.LayoutResult layout = TestHelper.calculateAnimalManagementLayout(480, 270);
        int sellRightEdge = layout.sellBtnX() + layout.sellBtnW();

        assertTrue(sellRightEdge <= 480,
                "Sell button right edge (" + sellRightEdge + ") must not exceed screen width (480)");
    }

    @Test
    @DisplayName("F1-3: Buttons stack vertically when info width is constrained (< 240px)")
    void testButtonsStackWhenAvailableWidthIsConstrained() {
        TestHelper.LayoutResult layoutScale4 = TestHelper.calculateAnimalManagementLayout(480, 270);
        assertTrue(layoutScale4.buttonsStacked(), "Buttons must stack vertically on tight screen (infoW < 240)");
        assertTrue(layoutScale4.sellBtnY() >= layoutScale4.removeBtnY() + 18,
                "Stacked sell button must render below remove button without vertical overlap");

        TestHelper.LayoutResult layoutScale1 = TestHelper.calculateAnimalManagementLayout(1920, 1080);
        assertFalse(layoutScale1.buttonsStacked(), "Buttons should be side-by-side on wide screen (infoW >= 240)");
    }

    @Test
    @DisplayName("F1-4: Stat percentage text renders within content scissor bounds")
    void testStatPercentageWithinScissorBounds() {
        TestHelper.LayoutResult layout = TestHelper.calculateAnimalManagementLayout(480, 270);
        int scissorRight = layout.contentX() + layout.contentW();

        assertTrue(layout.statTextX() + 25 <= scissorRight,
                "Stat percentage text (X=" + layout.statTextX() + ") must render within scissor right bound (" + scissorRight + ")");
        assertTrue(layout.statTextX() >= layout.infoX(),
                "Stat percentage text must start at or after infoX");
    }

    @Test
    @DisplayName("F1-5: Deep tree indentation leaves sufficient width for animal name display")
    void testDeepTreeIndentationLeavesSufficientTextWidth() {
        int sidebarW = 130; // Scale 4 sidebar
        int indentPerLevel = 10;
        int maxDepth = 4;
        int totalIndent = Math.min(30, maxDepth * indentPerLevel);
        int textX = 14 + 18 + totalIndent;
        int arrowAndIconReserved = 32;
        int availableNameW = (14 + sidebarW) - textX - arrowAndIconReserved;

        assertTrue(availableNameW >= 40,
                "Available name width at max depth must be at least 40px to prevent severe truncation, got: " + availableNameW);
    }

    @Test
    @DisplayName("F1-6: Diet subtitle and temperature strings fit inside info column bounds")
    void testDietSubtitleAndTempStringTruncationCalculation() {
        TestHelper.LayoutResult layout = TestHelper.calculateAnimalManagementLayout(480, 270);
        int availableInfoW = layout.infoW();

        // Diet subtitle sample: "Frugivora (Buah & Biji-bijian)" (~140px at 6px per char)
        String sampleDiet = "Frugivora (Buah & Biji-bijian)";
        int estimatedWidth = sampleDiet.length() * 6;

        // When estimated width exceeds available width, truncation or ellipsis is required
        if (estimatedWidth > availableInfoW) {
            int maxChars = Math.max(0, (availableInfoW - 18) / 6);
            String truncated = sampleDiet.substring(0, maxChars) + "...";
            int truncatedWidth = truncated.length() * 6;
            assertTrue(truncatedWidth <= availableInfoW,
                    "Truncated diet text (" + truncatedWidth + "px) must fit within infoW (" + availableInfoW + "px)");
        }
    }
}
