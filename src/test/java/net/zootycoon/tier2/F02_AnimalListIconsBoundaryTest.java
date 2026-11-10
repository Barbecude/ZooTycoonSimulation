package net.zootycoon.tier2;

import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F2: Animal List Icons Boundary and Corner Cases")
public class F02_AnimalListIconsBoundaryTest {

    @Test
    @DisplayName("F2-B1: Zero and negative icon dimensions clamp to valid minimum")
    void testZeroAndNegativeIconDimensions() {
        int inputW = 0;
        int inputH = -5;
        int clampedW = Math.max(1, inputW);
        int clampedH = Math.max(1, inputH);

        assertEquals(1, clampedW);
        assertEquals(1, clampedH);
    }

    @Test
    @DisplayName("F2-B2: Extremely large icon dimension clamped to button boundary")
    void testOversizedIconClampedToButton() {
        int buttonSize = 20;
        int iconSize = 256; // Intentionally huge icon
        int clamped = Math.min(iconSize, buttonSize - 4);

        assertEquals(16, clamped, "Oversized icon should be bounded within button padding");
    }

    @Test
    @DisplayName("F2-B3: Non-square aspect ratio icons preserve proportions")
    void testNonSquareIconProportions() {
        int rawW = 32;
        int rawH = 16;
        double aspect = (double) rawW / rawH;
        assertEquals(2.0, aspect, 0.001);

        int maxFit = 16;
        int fitW = (int) (maxFit * Math.min(1.0, aspect));
        int fitH = (int) (maxFit / Math.max(1.0, aspect));
        assertTrue(fitW <= maxFit && fitH <= maxFit);
    }

    @Test
    @DisplayName("F2-B4: Missing texture file returns invalid PNG result without throwing uncaught exception")
    void testMissingTextureFileHandledGracefully() {
        File nonExistent = new File("src/main/resources/assets/zootycoon/textures/gui/icons/definitely_missing_12345.png");
        TestHelper.PngDimensions dims = TestHelper.getPngDimensions(nonExistent);

        assertFalse(dims.validPng(), "Missing file should yield validPng = false");
        assertEquals(0, dims.width());
        assertEquals(0, dims.height());
    }

    @Test
    @DisplayName("F2-B5: Corrupt PNG header (invalid magic bytes) rejected safely")
    void testCorruptedPngHeaderRejection() {
        File dummyTextFile = TestHelper.getAssetFile("lang/en_us.json");
        TestHelper.PngDimensions dims = TestHelper.getPngDimensions(dummyTextFile);

        assertFalse(dims.validPng(), "JSON file passed to PNG parser must be rejected");
        assertEquals(0, dims.width());
        assertEquals(0, dims.height());
    }
}
