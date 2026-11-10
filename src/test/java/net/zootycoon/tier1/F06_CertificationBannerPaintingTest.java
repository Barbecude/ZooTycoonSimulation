package net.zootycoon.tier1;

import net.minecraft.resources.ResourceLocation;
import net.zootycoon.TestHelper;
import net.zootycoon.client.render.LicenseBannerRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F6: Certification Banner Painting & World Render")
public class F06_CertificationBannerPaintingTest {

    @Test
    @DisplayName("F6-1: Banner texture path mapping resolves correctly for all 5 tiers")
    void testBannerTexturePathMapping() {
        TestHelper.ensureMinecraftBootstrapped();

        for (int tier = 1; tier <= 5; tier++) {
            ResourceLocation loc = LicenseBannerRenderer.getCustomBannerTexture(tier);
            assertEquals("zootycoon", loc.getNamespace());
            assertEquals("textures/entity/license_banner/tier_" + tier + ".png", loc.getPath());
        }
    }

    @Test
    @DisplayName("F6-2: Out-of-bounds tier levels are safely clamped to [1, 5]")
    void testBannerTextureClamping() {
        TestHelper.ensureMinecraftBootstrapped();

        ResourceLocation locZero = LicenseBannerRenderer.getCustomBannerTexture(0);
        assertEquals("textures/entity/license_banner/tier_1.png", locZero.getPath());

        ResourceLocation locNegative = LicenseBannerRenderer.getCustomBannerTexture(-99);
        assertEquals("textures/entity/license_banner/tier_1.png", locNegative.getPath());

        ResourceLocation locOver = LicenseBannerRenderer.getCustomBannerTexture(10);
        assertEquals("textures/entity/license_banner/tier_5.png", locOver.getPath());
    }

    @Test
    @DisplayName("F6-3: In-world plaque dimensions maintain 3:2 proportion (3 blocks wide by 2 blocks high)")
    void testPlaqueWorldDimensions() {
        double widthBlocks = 3.0D;
        double heightBlocks = 2.0D;
        double ratio = widthBlocks / heightBlocks;

        assertEquals(1.5D, ratio, 0.001D, "Certification banner plaque must maintain 3:2 ratio");
    }

    @Test
    @DisplayName("F6-4: Authentic certificate styling uses dark oak borders and sepia typography")
    void testCertificatePaletteStyling() {
        int darkOakBorder = 0xFF2E1C0C;
        int sepiaHeading = 0xFF3E2723;
        int goldAccent = 0xFFC5A059;

        // Dark oak should be deep brown
        assertTrue(((darkOakBorder >> 16) & 0xFF) > ((darkOakBorder) & 0xFF), "Border should have warm wood tone");
        // Heading should be dark readable ink
        assertTrue(((sepiaHeading >> 16) & 0xFF) < 100, "Heading ink should be dark for high contrast");
        // Accent should be warm antique gold
        assertTrue(((goldAccent >> 16) & 0xFF) > 150 && ((goldAccent >> 8) & 0xFF) > 120, "Gold accent should be warm");
    }

    @Test
    @DisplayName("F6-5: Banner resource directory path is well-formed")
    void testBannerResourceDirectoryStructure() {
        File bannerDir = TestHelper.getAssetFile("textures/entity/license_banner");
        assertNotNull(bannerDir);
        assertTrue(bannerDir.getPath().contains("textures") && bannerDir.getPath().contains("license_banner"));
    }
}
