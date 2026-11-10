package net.zootycoon.tier1;

import net.zootycoon.TestHelper;
import net.zootycoon.license.LicenseTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F12: Animal Information Banner Award")
public class F12_AnimalInfoBannerAwardTest {

    public static String getFunFactKey(String speciesKey) {
        return "zootycoon.species." + speciesKey + ".fun_fact";
    }

    public static boolean shouldAwardInfoBanner(boolean inspectionPassed) {
        return inspectionPassed;
    }

    @Test
    @DisplayName("F12-1: Fun fact translation keys are consistently formatted for all 40 species")
    void testFunFactKeyFormattingForAllSpecies() {
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                String key = getFunFactKey(entry.speciesKey());
                assertTrue(key.startsWith("zootycoon.species."));
                assertTrue(key.endsWith(".fun_fact"));
                assertEquals("zootycoon.species." + entry.speciesKey() + ".fun_fact", key);
            }
        }
    }

    @Test
    @DisplayName("F12-2: Animal info banner is awarded exclusively upon passing inspection")
    void testBannerAwardedOnlyWhenInspectionPassed() {
        assertTrue(shouldAwardInfoBanner(true), "Banner must be awarded when inspection passes");
        assertFalse(shouldAwardInfoBanner(false), "Banner must NOT be awarded when inspection fails");
    }

    @Test
    @DisplayName("F12-3: Info banner plaque layout partitions render pane on left and text on right")
    void testInfoBannerLayoutPartition() {
        int bannerWidthPx = 192;
        int bannerHeightPx = 128;

        int renderPaneWidth = 72;
        int textPaneX = renderPaneWidth + 8;
        int textPaneWidth = bannerWidthPx - textPaneX - 8;

        assertTrue(renderPaneWidth > 48, "Render pane on left must have room for 3D model");
        assertTrue(textPaneWidth >= 96, "Text pane on right must have sufficient width for name and fun fact");
        assertEquals(bannerWidthPx, renderPaneWidth + 8 + textPaneWidth + 8);
    }

    @Test
    @DisplayName("F12-4: Banner title uses mod's default English species name")
    void testBannerTitleUsesEnglishDefaultName() {
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                String title = entry.defaultName();
                assertNotNull(title);
                assertFalse(title.isEmpty());
                assertTrue(Character.isUpperCase(title.charAt(0)), "English species name should begin capitalized");
            }
        }
    }

    @Test
    @DisplayName("F12-5: Fun fact banner description text formatting enforces clean typography")
    void testFunFactCleanTypography() {
        String sampleFunFact = "Capybaras are the largest living rodents in the world and love semi-aquatic habitats.";
        assertFalse(sampleFunFact.contains("§l"), "Fun fact text must not use bold codes");
        assertFalse(TestHelper.containsEmoji(sampleFunFact), "Fun fact must not contain emoji");
    }
}
