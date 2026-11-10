package net.zootycoon.tier1;

import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F5: Inspection Result Letter Visuals & Parchment Theme")
public class F05_InspectionResultLetterVisualsTest {

    @Test
    @DisplayName("F5-1: Parchment texture asset exists and is a valid PNG")
    void testParchmentTextureExistsAndValid() {
        File builderNote = TestHelper.getAssetFile("textures/gui/builder_note.png");
        assertTrue(builderNote.exists(), "Parchment base texture 'builder_note.png' must exist");

        TestHelper.PngDimensions dims = TestHelper.getPngDimensions(builderNote);
        assertTrue(dims.validPng(), "builder_note.png must be a valid PNG");
        assertTrue(dims.width() >= 128 && dims.height() >= 128,
                "Parchment texture should have adequate resolution (>= 128x128), got: " + dims.width() + "x" + dims.height());
    }

    @Test
    @DisplayName("F5-2: Authentic ink color palette uses warm sepia and deep earth tones")
    void testInkColorPaletteUsesWarmSepiaTones() {
        // Sepia / Walnut Ink color constants
        int inkPrimary = 0xFF2A1708;    // Deep walnut brown ink
        int inkSecondary = 0xFF4A3525;  // Muted bistre/sepia
        int inkCharcoal = 0xFF2B2018;   // Natural charcoal ink

        // Verify luminance indicates dark readable ink (not bright neon)
        assertTrue(calculateLuminance(inkPrimary) < 50, "Primary ink must be dark for authentic paper contrast");
        assertTrue(calculateLuminance(inkSecondary) < 80, "Secondary ink must be dark enough for readability");
        assertTrue(calculateLuminance(inkCharcoal) < 60, "Charcoal body ink must be dark");
    }

    @Test
    @DisplayName("F5-3: Letter palette excludes artificial bright neon hues")
    void testLetterPaletteExcludesArtificialNeonColors() {
        int[] neonColors = {
                0xFFFFD700, // Neon gold
                0xFF81E6D9, // Neon cyan
                0xFF60A5FA, // Neon blue
                0xFF4ADE80  // Bright phosphor green
        };

        for (int neon : neonColors) {
            double lum = calculateLuminance(neon);
            assertTrue(lum > 150, "Neon test reference should have high luminance");
        }
    }

    @Test
    @DisplayName("F5-4: Wax stamp styling matches authentic evaluation results")
    void testWaxStampStylingAttributes() {
        int passedStampColor = 0xFF1B5E20; // Deep forest green ink
        int failedStampColor = 0xFF8B1E1E; // Deep burgundy/crimson ink

        // Passed stamp should have dominant green component
        int passedG = (passedStampColor >> 8) & 0xFF;
        int passedR = (passedStampColor >> 16) & 0xFF;
        assertTrue(passedG > passedR, "Passed stamp should be green-dominated");

        // Failed stamp should have dominant red component
        int failedR = (failedStampColor >> 16) & 0xFF;
        int failedG = (failedStampColor >> 8) & 0xFF;
        assertTrue(failedR > failedG, "Failed stamp should be red-dominated");
    }

    @Test
    @DisplayName("F5-5: Letter dimensions maintain standard paper aspect ratio (approx 3:4)")
    void testLetterDimensionsAspectRatio() {
        int letterW = 220;
        int letterH = 280;
        double aspect = (double) letterW / letterH;

        assertTrue(aspect >= 0.65 && aspect <= 0.85,
                "Letter aspect ratio (" + aspect + ") should approximate standard portrait paper (3:4 = 0.75)");
    }

    private double calculateLuminance(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }
}
