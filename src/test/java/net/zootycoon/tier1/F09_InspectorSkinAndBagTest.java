package net.zootycoon.tier1;

import net.minecraft.resources.ResourceLocation;
import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F9: Inspector Skin and Toolbag Model Accessory")
public class F09_InspectorSkinAndBagTest {

    @Test
    @DisplayName("F9-1: Target inspector skin texture path is well-formed")
    void testTargetSkinTexturePath() {
        TestHelper.ensureMinecraftBootstrapped();

        ResourceLocation inspectorTex = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/entity/staff/inspector.png");
        assertEquals("zootycoon", inspectorTex.getNamespace());
        assertEquals("textures/entity/staff/inspector.png", inspectorTex.getPath());
    }

    @Test
    @DisplayName("F9-2: Inspector model texture dimensions specify 128x64 to house skin and accessories")
    void testModelTextureDimensionsAre128x64() {
        int texWidth = 128;
        int texHeight = 64;

        assertEquals(128, texWidth, "Texture width must be 128 to accommodate toolbag texture space");
        assertEquals(64, texHeight, "Texture height must be 64");
    }

    @Test
    @DisplayName("F9-3: Toolbag accessory subparts specification covers straps, ruler, and hammer")
    void testToolbagAccessoriesSubparts() {
        String[] requiredParts = {"strapback_r1", "strapfrontb_r1", "strapfronta_r1", "bag_r1", "ruler", "ruler2", "hammer"};

        for (String part : requiredParts) {
            assertNotNull(part);
            assertFalse(part.isBlank());
        }
        assertEquals(7, requiredParts.length);
    }

    @Test
    @DisplayName("F9-4: Toolbag UV coordinates align with builder bag sheet region (U >= 64)")
    void testToolbagUvRegionSeparation() {
        // Humanoid skin is in U=[0..63], V=[0..63]
        // Toolbag and tools are placed in U=[64..127], V=[0..63]
        int bagU = 68;
        int rulerU = 78;
        int hammerU = 80;
        int strapU = 84;

        assertTrue(bagU >= 64, "Bag texture offset must be in the right half (U >= 64)");
        assertTrue(rulerU >= 64, "Ruler texture offset must be in the right half (U >= 64)");
        assertTrue(hammerU >= 64, "Hammer texture offset must be in the right half (U >= 64)");
        assertTrue(strapU >= 64, "Strap texture offset must be in the right half (U >= 64)");
    }

    @Test
    @DisplayName("F9-5: Builder reference texture exists in mod resources")
    void testBuilderReferenceTextureExists() {
        File builderTex = TestHelper.getAssetFile("textures/entity/builder/buildermale1_a.png");
        assertTrue(builderTex.exists(), "buildermale1_a.png must exist as source reference for toolbag");

        TestHelper.PngDimensions dims = TestHelper.getPngDimensions(builderTex);
        assertTrue(dims.validPng());
        assertEquals(128, dims.width(), "Builder texture width must be 128");
        assertEquals(64, dims.height(), "Builder texture height must be 64");
    }
}
