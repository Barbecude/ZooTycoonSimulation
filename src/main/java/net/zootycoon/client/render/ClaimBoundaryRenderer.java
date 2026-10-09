package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.item.ZooItems;

import java.util.ArrayList;
import java.util.List;

public class ClaimBoundaryRenderer {
    public static final List<ZooClaim> CLIENT_CLAIMS = new ArrayList<>();

    public static void render(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Only render when holding the Build Tool in either hand
        boolean holdingBuildTool = player.getMainHandItem().is(ZooItems.BUILD_TOOL)
                || player.getOffhandItem().is(ZooItems.BUILD_TOOL);
        if (!holdingBuildTool) return;

        Vec3 camPos = camera.getPosition();
        long time = mc.level != null ? mc.level.getGameTime() : 0;
        // Sinusoidal pulsing alpha between 0.35 and 0.85
        float pulse = (float) (0.55f + 0.30f * Math.sin((time + tickDelta) / 6.0));

        VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        for (ZooClaim claim : CLIENT_CLAIMS) {
            BoundingBox box = claim.getBox();
            boolean isOwner = claim.getOwnerUuid().equals(player.getUUID());

            float r = isOwner ? 0.2f : 0.9f;
            float g = isOwner ? 0.9f : 0.2f;
            float b = isOwner ? 0.8f : 0.2f;

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            LevelRenderer.renderLineBox(
                    poseStack, consumer,
                    box.minX(), player.getY() - 1, box.minZ(),
                    box.maxX() + 1, player.getY() + 3, box.maxZ() + 1,
                    r, g, b, pulse
            );

            poseStack.popPose();
        }
    }
}

