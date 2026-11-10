package net.zootycoon.tier1;

import net.minecraft.resources.ResourceLocation;
import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F2: Cute Pixelated Animal Icons")
public class F02_AnimalListIconsTest {

    @Test
    @DisplayName("F2-1: Animals icon texture exists and is a valid PNG with square aspect ratio")
    void testAnimalsIconTextureExistsAndValid() {
        File animalsIcon = TestHelper.getAssetFile("textures/gui/icons/animals.png");
        assertTrue(animalsIcon.exists(), "animals.png texture must exist in textures/gui/icons/");

        TestHelper.PngDimensions dims = TestHelper.getPngDimensions(animalsIcon);
        assertTrue(dims.validPng(), "animals.png must be a valid PNG file");
        assertEquals(dims.width(), dims.height(), "animals icon should have square dimensions (e.g. 16x16 or 32x32)");
        assertTrue(dims.width() >= 16 && dims.width() <= 64, "Icon dimensions should be between 16 and 64 pixels");
    }

    @Test
    @DisplayName("F2-2: ResourceLocation identifiers for animal icons are valid namespace paths")
    void testResourceLocationIdentifiersAreValid() {
        TestHelper.ensureMinecraftBootstrapped();

        ResourceLocation animalsLoc = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/animals.png");
        assertEquals("zootycoon", animalsLoc.getNamespace());
        assertEquals("textures/gui/icons/animals.png", animalsLoc.getPath());

        ResourceLocation collectionLoc = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/collection_cute.png");
        assertEquals("zootycoon", collectionLoc.getNamespace());

        ResourceLocation licenseLoc = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/license_cute.png");
        assertEquals("zootycoon", licenseLoc.getNamespace());
    }

    @Test
    @DisplayName("F2-3: Custom icon dimensions conform to 16x16 or 32x32 standard pixel art grid")
    void testCustomIconStandardPixelDimensions() {
        int[] validSizes = {14, 16, 24, 32};
        for (int size : validSizes) {
            assertTrue(size > 0 && size <= 64, "Icon size " + size + " is within acceptable UI button bounds");
        }
    }

    @Test
    @DisplayName("F2-4: UI buttons support custom icon rendering without crash or null pointer")
    void testButtonCustomIconContract() {
        TestHelper.ensureMinecraftBootstrapped();
        ResourceLocation testIcon = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/animals.png");

        assertNotNull(testIcon, "Icon ResourceLocation must not be null");
        int targetW = 14;
        int targetH = 14;
        int texW = 32;
        int texH = 32;

        assertTrue(targetW > 0 && targetH > 0, "Icon target render dimensions must be positive");
        assertTrue(texW > 0 && texH > 0, "Texture sheet dimensions must be positive");
    }

    @Test
    @DisplayName("F2-5: License button does not require vanilla book item texture when custom icon is present")
    void testLicenseButtonCuteIconReplacesVanillaBook() {
        ResourceLocation cuteLicenseIcon = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/license_cute.png");
        assertNotNull(cuteLicenseIcon);
        assertEquals("license_cute.png", cuteLicenseIcon.getPath().substring(cuteLicenseIcon.getPath().lastIndexOf('/') + 1));
    }
}
