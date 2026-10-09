package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.item.ZooItems;
import net.zootycoon.schematic.ZooSchematic;

public class PlacementPreviewRenderer {
    private static ZooSchematic activeSchematic = null;
    private static BlockPos targetPlacementPos = null;

    public static void setActiveSchematic(ZooSchematic schematic) {
        activeSchematic = schematic;
    }

    public static ZooSchematic getActiveSchematic() {
        return activeSchematic;
    }

    public static BlockPos getTargetPlacementPos() {
        return targetPlacementPos;
    }

    public static void clear() {
        activeSchematic = null;
        targetPlacementPos = null;
    }

    public static void render(PoseStack poseStack, Camera camera, float tickDelta) {
        if (activeSchematic == null) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        // Player must be holding Build Tool
        boolean holdingTool = player.getMainHandItem().is(ZooItems.BUILD_TOOL)
                || player.getOffhandItem().is(ZooItems.BUILD_TOOL);
        if (!holdingTool) {
            clear();
            return;
        }

        HitResult hit = player.pick(20.0D, tickDelta, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            targetPlacementPos = null;
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos basePos = blockHit.getBlockPos().above();
        targetPlacementPos = basePos;

        Vec3i dim = activeSchematic.getDimensions();
        Vec3 camPos = camera.getPosition();

        // Check if inside owned claim
        boolean isValidClaim = false;
        for (ZooClaim claim : ClaimBoundaryRenderer.CLIENT_CLAIMS) {
            if (claim.getOwnerUuid().equals(player.getUUID())) {
                if (claim.contains(basePos) && claim.contains(basePos.offset(dim.getX(), 0, dim.getZ()))) {
                    isValidClaim = true;
                    break;
                }
            }
        }

        // Sinusoidal pulsing glow (kedap-kedip)
        long gameTime = mc.level.getGameTime();
        float pulse = (float) (0.50f + 0.40f * Math.sin((gameTime + tickDelta) / 4.0));

        float r = isValidClaim ? 0.2f : 1.0f;
        float g = isValidClaim ? 1.0f : 0.2f;
        float b = isValidClaim ? 0.4f : 0.2f;

        VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        // Render main bounding box with glowing outline
        LevelRenderer.renderLineBox(
                poseStack, consumer,
                basePos.getX(), basePos.getY(), basePos.getZ(),
                basePos.getX() + dim.getX(), basePos.getY() + dim.getY(), basePos.getZ() + dim.getZ(),
                r, g, b, pulse
        );

        // Render grid base footprint on ground
        LevelRenderer.renderLineBox(
                poseStack, consumer,
                basePos.getX(), basePos.getY(), basePos.getZ(),
                basePos.getX() + dim.getX(), basePos.getY() + 0.1, basePos.getZ() + dim.getZ(),
                r, g, b, 0.9f
        );

        poseStack.popPose();
    }
}

