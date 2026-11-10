package net.zootycoon.tier1;

import net.zootycoon.TestHelper;
import net.zootycoon.license.LicenseTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F17: Banner Fun Fact Heading Removal & Progressive Certification Plaques")
public class F17_BannerPolishAndFunFactHeadingTest {

    @Test
    @DisplayName("All 5 tier plaque textures are 384x256 (3:2) and all 5 item icons are 16x16 PNGs")
    void testProgressiveBannerTextureAndItemDimensions() throws Exception {
        for (int tier = 1; tier <= 5; tier++) {
            File plaque = TestHelper.getAssetFile("textures/entity/license_banner/tier_" + tier + ".png");
            assertTrue(plaque.exists(), "Missing tier plaque texture: tier_" + tier + ".png");
            TestHelper.PngDimensions plaqueDims = TestHelper.getPngDimensions(plaque);
            assertTrue(plaqueDims.validPng());
            assertEquals(384, plaqueDims.width());
            assertEquals(256, plaqueDims.height());

            File icon = TestHelper.getAssetFile("textures/item/license_banner_tier_" + tier + ".png");
            assertTrue(icon.exists(), "Missing tier item icon: license_banner_tier_" + tier + ".png");
            TestHelper.PngDimensions iconDims = TestHelper.getPngDimensions(icon);
            assertTrue(iconDims.validPng());
            assertEquals(16, iconDims.width());
            assertEquals(16, iconDims.height());
        }
    }

    @Test
    @DisplayName("LicenseBannerRenderer omits the red fun_fact_title heading while retaining fun_fact description")
    void testFunFactRedTitleRemovedButParagraphPreserved() throws Exception {
        String source = Files.readString(Path.of("src/main/java/net/zootycoon/client/render/LicenseBannerRenderer.java"));
        assertFalse(source.contains("fun_fact_title"),
                "Red 'Fakta Unik Satwa' heading key must not be rendered on the animal info board");
        assertFalse(source.contains("0xFF7D1B1B"),
                "Red heading color 0xFF7D1B1B must be removed from LicenseBannerRenderer");
        assertTrue(source.contains("zootycoon.species.\" + speciesKey + \".fun_fact"),
                "Wrapped species fun fact paragraph must remain rendered");

        Map<String, String> en = TestHelper.getEnLang();
        Map<String, String> id = TestHelper.getIdLang();
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                String key = "zootycoon.species." + entry.speciesKey() + ".fun_fact";
                assertTrue(en.containsKey(key) && !en.get(key).isBlank(), "Missing en fun fact: " + key);
                assertTrue(id.containsKey(key) && !id.get(key).isBlank(), "Missing id fun fact: " + key);
            }
        }
    }

    @Test
    @DisplayName("Certification plaque localization keys exist for both en_us and id_id")
    void testCertificateLocalizationKeysExist() {
        Map<String, String> en = TestHelper.getEnLang();
        Map<String, String> id = TestHelper.getIdLang();
        String[] keys = {
                "zootycoon.license.banner.header",
                "zootycoon.license.banner.awarded_to",
                "zootycoon.license.banner.species_verified",
                "zootycoon.license.banner.certified",
                "zootycoon.license.banner.verified"
        };
        for (String key : keys) {
            assertTrue(en.containsKey(key) && !en.get(key).isBlank(), "Missing en_us key: " + key);
            assertTrue(id.containsKey(key) && !id.get(key).isBlank(), "Missing id_id key: " + key);
        }
    }
}
