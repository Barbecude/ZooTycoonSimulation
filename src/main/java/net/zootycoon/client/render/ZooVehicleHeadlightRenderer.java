package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.entity.ZooVehicleEntity;
import org.joml.Matrix4f;

public class ZooVehicleHeadlightRenderer {

    private static final ResourceLocation BEAM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("physical_vehicles", "textures/other/light/headlights.png");
    private static final ResourceLocation FLARE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("physical_vehicles", "textures/other/light/marker.png");
    private static final ResourceLocation SPOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("physical_vehicles", "textures/other/light/headlights2.png");

    private static final float BEAM_LENGTH = 22.0F;
    private static final float START_RADIUS = 0.12F;
    private static final float END_RADIUS = 2.40F;
    private static final float DOWNWARD_TILT = -0.035F;

    public static void renderHeadlights(ZooVehicleEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        if (!entity.hasHeadlightsOn()) {
            return;
        }

        ZooVehicleEntity.VehicleSpec spec = entity.getSpec();
        Vec3 hlOff = spec.headlightOffset();
        if (hlOff == null) {
            hlOff = new Vec3(0.60, 0.40, 2.0);
        }

        float clearanceY = spec.groundClearanceY();
        float leftX = (float) hlOff.x;
        float rightX = -leftX;
        float hlY = clearanceY + (float) hlOff.y;
        float hlZ = (float) hlOff.z;

        // Render both headlights: left and right
        renderSingleHeadlight(entity, poseStack, buffer, leftX, hlY, hlZ);
        renderSingleHeadlight(entity, poseStack, buffer, rightX, hlY, hlZ);
    }

    private static void renderSingleHeadlight(
            ZooVehicleEntity entity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            float localX,
            float localY,
            float localZ
    ) {
        poseStack.pushPose();
        poseStack.translate(localX, localY, localZ);

        // 1. Headlight Lens Flare (Bright glowing billboard at the car's headlight bulb)
        renderFlare(poseStack, buffer);

        // 2. 3D Volumetric Light Cone Beam
        renderVolumetricBeam(poseStack, buffer);

        // 3. Projected Ground/Wall Spotlight Decal
        renderProjectedSpot(entity, poseStack, buffer, localX, localY, localZ);

        poseStack.popPose();
    }

    private static void renderFlare(PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        float flareSize = 0.22F;

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(FLARE_TEXTURE));
        var pose = poseStack.last();
        Matrix4f mat = pose.pose();

        int r = 255;
        int g = 255;
        int b = 230;
        int a = 240;
        int light = 0x00F000F0;

        // Facing forward (+Z)
        consumer.addVertex(mat, -flareSize, -flareSize, 0.02F).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(mat, flareSize, -flareSize, 0.02F).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(mat, flareSize, flareSize, 0.02F).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(mat, -flareSize, flareSize, 0.02F).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);

        // Double-sided so visible from side/behind angles
        consumer.addVertex(mat, -flareSize, -flareSize, 0.02F).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        consumer.addVertex(mat, -flareSize, flareSize, 0.02F).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        consumer.addVertex(mat, flareSize, flareSize, 0.02F).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        consumer.addVertex(mat, flareSize, -flareSize, 0.02F).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);

        poseStack.popPose();
    }

    private static void renderVolumetricBeam(PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();

        VertexConsumer beamConsumer = buffer.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, true));
        var pose = poseStack.last();
        Matrix4f mat = pose.pose();

        int r = 255;
        int g = 255;
        int b = 225;
        int light = 0x00F000F0;

        float zEnd = BEAM_LENGTH;
        float yEnd = DOWNWARD_TILT * zEnd;

        // A. 4 Crossed double-sided planes rotated around Z axis (0, 45, 90, 135 deg)
        for (int i = 0; i < 4; i++) {
            float angleDeg = i * 45.0F;
            float cos = Mth.cos(angleDeg * Mth.DEG_TO_RAD);
            float sin = Mth.sin(angleDeg * Mth.DEG_TO_RAD);

            float x0a = -START_RADIUS * cos;
            float y0a = -START_RADIUS * sin;
            float x0b = START_RADIUS * cos;
            float y0b = START_RADIUS * sin;

            float x1a = -END_RADIUS * cos;
            float y1a = yEnd - END_RADIUS * sin;
            float x1b = END_RADIUS * cos;
            float y1b = yEnd + END_RADIUS * sin;

            // Forward quad
            beamConsumer.addVertex(mat, x0a, y0a, 0.05F).setColor(r, g, b, 175).setUv(0.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
            beamConsumer.addVertex(mat, x0b, y0b, 0.05F).setColor(r, g, b, 175).setUv(1.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
            beamConsumer.addVertex(mat, x1b, y1b, zEnd).setColor(r, g, b, 0).setUv(1.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
            beamConsumer.addVertex(mat, x1a, y1a, zEnd).setColor(r, g, b, 0).setUv(0.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);

            // Backface quad
            beamConsumer.addVertex(mat, x0a, y0a, 0.05F).setColor(r, g, b, 175).setUv(0.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
            beamConsumer.addVertex(mat, x1a, y1a, zEnd).setColor(r, g, b, 0).setUv(0.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
            beamConsumer.addVertex(mat, x1b, y1b, zEnd).setColor(r, g, b, 0).setUv(1.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
            beamConsumer.addVertex(mat, x0b, y0b, 0.05F).setColor(r, g, b, 175).setUv(1.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
        }

        // B. 8-faceted conical envelope for volumetric depth
        int segments = 8;
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (i * 2 * Math.PI / segments);
            float a1 = (float) ((i + 1) * 2 * Math.PI / segments);

            float cx0 = Mth.cos(a0);
            float sy0 = Mth.sin(a0);
            float cx1 = Mth.cos(a1);
            float sy1 = Mth.sin(a1);

            float sx0 = START_RADIUS * cx0;
            float sy00 = START_RADIUS * sy0;
            float sx1 = START_RADIUS * cx1;
            float sy11 = START_RADIUS * sy1;

            float ex0 = END_RADIUS * cx0;
            float ey0 = yEnd + END_RADIUS * sy0;
            float ex1 = END_RADIUS * cx1;
            float ey1 = yEnd + END_RADIUS * sy1;

            float u0 = (float) i / segments;
            float u1 = (float) (i + 1) / segments;

            beamConsumer.addVertex(mat, sx0, sy00, 0.05F).setColor(r, g, b, 95).setUv(u0, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, cx0, sy0, 0.0F);
            beamConsumer.addVertex(mat, sx1, sy11, 0.05F).setColor(r, g, b, 95).setUv(u1, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, cx1, sy1, 0.0F);
            beamConsumer.addVertex(mat, ex1, ey1, zEnd).setColor(r, g, b, 0).setUv(u1, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, cx1, sy1, 0.0F);
            beamConsumer.addVertex(mat, ex0, ey0, zEnd).setColor(r, g, b, 0).setUv(u0, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, cx0, sy0, 0.0F);
        }

        poseStack.popPose();
    }

    private static void renderProjectedSpot(
            ZooVehicleEntity entity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            float localX,
            float localY,
            float localZ
    ) {
        if (entity.level() == null) {
            return;
        }

        float yawDeg = entity.getYRot();
        Vec3 localHeadlightPos = new Vec3(localX, localY, localZ);
        Vec3 worldHeadlightPos = entity.position().add(ZooVehicleEntity.rotateLocalOffset(localHeadlightPos, yawDeg));

        // Forward direction in world coordinates with slight downward tilt
        double rad = Math.toRadians(yawDeg);
        double fwdX = -Math.sin(rad);
        double fwdZ = Math.cos(rad);
        double fwdY = DOWNWARD_TILT;
        Vec3 lookDir = new Vec3(fwdX, fwdY, fwdZ).normalize();

        Vec3 endPos = worldHeadlightPos.add(lookDir.scale(BEAM_LENGTH));

        ClipContext context = new ClipContext(
                worldHeadlightPos,
                endPos,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                entity
        );
        BlockHitResult hit = entity.level().clip(context);

        if (hit.getType() == HitResult.Type.BLOCK) {
            Vec3 hitPos = hit.getLocation();
            double dist = worldHeadlightPos.distanceTo(hitPos);
            if (dist > 1.2 && dist < BEAM_LENGTH) {
                float intensity = 1.0F - (float) (dist / BEAM_LENGTH);
                int alpha = (int) (165 * intensity * intensity);
                if (alpha < 8) return;

                float spotRadius = 0.85F + (float) (dist * 0.08F);
                Direction normalDir = hit.getDirection();

                // Compute local hit relative to this headlight in vehicle coordinates
                Vec3 relWorld = hitPos.subtract(worldHeadlightPos);
                // Inverse rotate relative vector back to vehicle local orientation
                Vec3 localHit = relWorld.yRot(yawDeg * Mth.DEG_TO_RAD);

                poseStack.pushPose();
                poseStack.translate(localHit.x, localHit.y, localHit.z);

                // Offset slightly outward from hit face to prevent z-fighting
                Vec3 normalLocal = new Vec3(normalDir.getStepX(), normalDir.getStepY(), normalDir.getStepZ()).yRot(yawDeg * Mth.DEG_TO_RAD);
                poseStack.translate(normalLocal.x * 0.03, normalLocal.y * 0.03, normalLocal.z * 0.03);

                // Rotate quad according to hit face direction
                if (normalDir == Direction.UP) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                } else if (normalDir == Direction.DOWN) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                } else if (normalDir == Direction.NORTH) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg));
                } else if (normalDir == Direction.SOUTH) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg + 180.0F));
                } else if (normalDir == Direction.WEST) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg - 90.0F));
                } else if (normalDir == Direction.EAST) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg + 90.0F));
                }

                VertexConsumer spotConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(SPOT_TEXTURE));
                var spotPose = poseStack.last();
                Matrix4f spotMat = spotPose.pose();

                int r = 255;
                int g = 255;
                int b = 230;
                int light = 0x00F000F0;

                spotConsumer.addVertex(spotMat, -spotRadius, -spotRadius, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(spotPose, 0.0F, 0.0F, 1.0F);
                spotConsumer.addVertex(spotMat, spotRadius, -spotRadius, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(spotPose, 0.0F, 0.0F, 1.0F);
                spotConsumer.addVertex(spotMat, spotRadius, spotRadius, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(spotPose, 0.0F, 0.0F, 1.0F);
                spotConsumer.addVertex(spotMat, -spotRadius, spotRadius, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(spotPose, 0.0F, 0.0F, 1.0F);

                poseStack.popPose();
            }
        }
    }
}
