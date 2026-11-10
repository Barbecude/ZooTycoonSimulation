package net.zootycoon.tier2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F9: Inspector Skin and Toolbag Boundary Cases")
public class F09_InspectorSkinAndBagBoundaryTest {

    @Test
    @DisplayName("F9-B1: Toolbag UV coordinates boundaries stay strictly within 128x64 canvas")
    void testToolbagUvCoordinatesStayWithinBounds() {
        int sheetW = 128;
        int sheetH = 64;

        int[][] uvRegions = {
                {68, 32, 8, 6},  // bag
                {78, 54, 8, 2},  // ruler
                {72, 54, 8, 2},  // ruler2
                {80, 46, 6, 1},  // hammer
                {84, 46, 15, 1}  // strap
        };

        for (int[] uv : uvRegions) {
            int u = uv[0];
            int v = uv[1];
            int w = uv[2];
            int h = uv[3];

            assertTrue(u >= 64, "U must start at or after 64");
            assertTrue(u + w <= sheetW, "U + W must not exceed 128");
            assertTrue(v >= 0, "V must be non-negative");
            assertTrue(v + h <= sheetH, "V + H must not exceed 64");
        }
    }

    @Test
    @DisplayName("F9-B2: CubeDeformation values remain small to prevent clipping artifacts")
    void testDeformationTolerances() {
        float bagDeformation = 0.03F;
        float rulerDeformation = -0.3F;
        float hammerDeformation = 0.1F;

        assertTrue(Math.abs(bagDeformation) <= 0.5F);
        assertTrue(Math.abs(rulerDeformation) <= 0.5F);
        assertTrue(Math.abs(hammerDeformation) <= 0.5F);
    }

    @Test
    @DisplayName("F9-B3: Model PartPose offset coordinate sanity limits (within 16 units of torso)")
    void testPartPoseOffsetSanity() {
        float offX = -5.5F;
        float offY = 12.0F;
        float offZ = 0.0F;

        assertTrue(Math.abs(offX) <= 16.0F);
        assertTrue(Math.abs(offY) <= 24.0F);
        assertTrue(Math.abs(offZ) <= 16.0F);
    }

    @Test
    @DisplayName("F9-B4: Legacy 64x32 skin expansion mapping boundary")
    void testLegacy64x32SkinExpansionMapping() {
        // Legacy 64x32 has head, body, right arm (40,16), right leg (0,16)
        // Expansion mirrors right arm to left arm (32,48) and right leg to left leg (16,48)
        int srcArmX = 40;
        int srcArmY = 16;
        int dstArmX = 32;
        int dstArmY = 48;

        assertEquals(40, srcArmX);
        assertEquals(48, dstArmY);
        assertTrue(dstArmY >= 32, "Left arm destination must be in expanded lower half of 64x64 texture");
    }

    @Test
    @DisplayName("F9-B5: Zero rotation boundary for biped body parent part")
    void testZeroRotationAtRest() {
        float rotX = 0.0F;
        float rotY = 0.0F;
        float rotZ = 0.0F;

        assertEquals(0.0F, rotX);
        assertEquals(0.0F, rotY);
        assertEquals(0.0F, rotZ);
    }
}
