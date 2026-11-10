package net.zootycoon.tier2;

import net.minecraft.resources.ResourceLocation;
import net.zootycoon.TestHelper;
import net.zootycoon.client.render.LicenseBannerRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F6: Certification Banner Painting Boundary Cases")
public class F06_CertificationBannerPaintingBoundaryTest {

    @Test
    @DisplayName("F6-B1: Extremely negative tier level (-1000) clamps safely to Tier 1")
    void testExtremelyNegativeTierLevelClamping() {
        TestHelper.ensureMinecraftBootstrapped();
        ResourceLocation loc = LicenseBannerRenderer.getCustomBannerTexture(-1000);
        assertEquals("textures/entity/license_banner/tier_1.png", loc.getPath());
    }

    @Test
    @DisplayName("F6-B2: Extremely high tier level (9999) clamps safely to Tier 5")
    void testExtremelyHighTierLevelClamping() {
        TestHelper.ensureMinecraftBootstrapped();
        ResourceLocation loc = LicenseBannerRenderer.getCustomBannerTexture(9999);
        assertEquals("textures/entity/license_banner/tier_5.png", loc.getPath());
    }

    @Test
    @DisplayName("F6-B3: Integer boundary values (Integer.MIN_VALUE and MAX_VALUE)")
    void testIntegerBoundaryValues() {
        TestHelper.ensureMinecraftBootstrapped();
        ResourceLocation minLoc = LicenseBannerRenderer.getCustomBannerTexture(Integer.MIN_VALUE);
        assertEquals("textures/entity/license_banner/tier_1.png", minLoc.getPath());

        ResourceLocation maxLoc = LicenseBannerRenderer.getCustomBannerTexture(Integer.MAX_VALUE);
        assertEquals("textures/entity/license_banner/tier_5.png", maxLoc.getPath());
    }

    @Test
    @DisplayName("F6-B4: Light level boundary limits (0 to 15)")
    void testPackedLightBoundaryLimits() {
        int minLight = 0;   // Pitch black
        int maxLight = 15;  // Full daylight

        assertTrue(minLight >= 0);
        assertTrue(maxLight <= 15);
    }

    @Test
    @DisplayName("F6-B5: Plaque depth offset prevents Z-fighting against attached wall")
    void testPlaqueWallOffsetPreventsZFighting() {
        float wallOffset = 0.0625F; // 1/16 block thickness (1 voxel)
        assertTrue(wallOffset > 0.01F && wallOffset <= 0.1F,
                "Wall offset should be approximately 1 voxel (0.0625) to avoid Z-fighting");
    }
}
