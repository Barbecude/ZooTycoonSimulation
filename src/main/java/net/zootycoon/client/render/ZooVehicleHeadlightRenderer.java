package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.entity.ZooVehicleEntity;
import org.joml.Matrix4f;

public class ZooVehicleHeadlightRenderer {

    // Authentic MTS-style high-definition lens flare, volumetric beam, and ramp textures
    private static final ResourceLocation FLARE_TEXTURE =
            ZooTycoonMod.id("textures/vehicle/lensflare.png");
    private static final ResourceLocation BEAM_TEXTURE =
            ZooTycoonMod.id("textures/vehicle/lightbeam.png");

    // Geometric parameters matching MTS headlight_low / headlight_high specifications
    private static final float BEAM_LENGTH = 30.0F;
    private static final float CORE_LENGTH = 20.0F;
    private static final float DOWNWARD_TILT = -0.038F; // Realistic dipped headlight low-beam pitch
    private static final float FLARE_PULL = 0.14F;      // Pull flare slightly toward camera off lens face
    private static final float FLARE_SIZE = 0.95F;      // Corona bloom radius (~1.9m total diameter)

    private static final int CONE_SIDES = 16;
    private static final int CONE_RINGS = 8;
    private static final int CORE_SIDES = 12;
    private static final int CORE_RINGS = 6;

    public static void renderHeadlights(ZooVehicleEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        if (!entity.hasHeadlightsOn()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) {
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

        // 1. Render both left and right headlight units (corona flare & volumetric cone)
        renderSingleHeadlight(entity, camera, poseStack, buffer, leftX, hlY, hlZ);
        renderSingleHeadlight(entity, camera, poseStack, buffer, rightX, hlY, hlZ);

        // 2. Render road / terrain illumination pool ahead of vehicle
        renderRoadIllumination(entity, poseStack, buffer, hlZ);
    }

    private static void renderSingleHeadlight(
            ZooVehicleEntity entity,
            Camera camera,
            PoseStack poseStack,
            MultiBufferSource buffer,
            float localX,
            float localY,
            float localZ
    ) {
        poseStack.pushPose();
        poseStack.translate(localX, localY, localZ);

        // A. Lens Corona / Lens Flare (MTS Camera-Facing Billboard)
        renderCorona(entity, camera, poseStack, buffer, localX, localY, localZ);

        // B. Volumetric 3D Conical Light Beams (Inner High-Intensity Core + Outer Atmospheric Cone)
        renderVolumetricCones(poseStack, buffer);

        // C. Projected Spotlight on Obstacles / Walls ahead
        renderProjectedWallSpot(entity, poseStack, buffer, localX, localY, localZ);

        poseStack.popPose();
    }

    /**
     * Camera-facing corona billboard at the headlight lens, based on MTS DynamicLightCoronas.
     */
    private static void renderCorona(
            ZooVehicleEntity entity,
            Camera camera,
            PoseStack poseStack,
            MultiBufferSource buffer,
            float localX,
            float localY,
            float localZ
    ) {
        Vec3 localHl = new Vec3(localX, localY, localZ);
        float yawDeg = entity.getYRot();
        Vec3 worldHl = entity.position().add(ZooVehicleEntity.rotateLocalOffset(localHl, yawDeg));

        Vec3 camPos = camera.getPosition();
        Vec3 toCamWorld = camPos.subtract(worldHl);
        double dist = toCamWorld.length();
        if (dist < 0.05) {
            return;
        }

        // Pull flare slightly toward camera off the physical car body to prevent mesh clipping
        Vec3 camDirLocal = toCamWorld.normalize().yRot(yawDeg * Mth.DEG_TO_RAD);

        poseStack.pushPose();
        poseStack.translate(camDirLocal.x * FLARE_PULL, camDirLocal.y * FLARE_PULL, camDirLocal.z * FLARE_PULL);

        // Orient billboard directly facing the player's camera
        poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg));
        poseStack.mulPose(camera.rotation());

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(FLARE_TEXTURE));
        var pose = poseStack.last();
        Matrix4f mat = pose.pose();

        int r = 255;
        int g = 252;
        int b = 235;
        int a = 245;
        int light = 0x00F000F0;
        float s = FLARE_SIZE;

        // Front face
        consumer.addVertex(mat, -s, -s, 0.0F).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(mat, s, -s, 0.0F).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(mat, s, s, 0.0F).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(mat, -s, s, 0.0F).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);

        // Back face (double-sided)
        consumer.addVertex(mat, -s, -s, 0.0F).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        consumer.addVertex(mat, -s, s, 0.0F).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        consumer.addVertex(mat, s, s, 0.0F).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        consumer.addVertex(mat, s, -s, 0.0F).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);

        poseStack.popPose();
    }

    /**
     * 3D volumetric light cone geometry with MTS quadratic falloff and limb darkening.
     */
    private static void renderVolumetricCones(PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();

        VertexConsumer beamConsumer = buffer.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, true));
        var pose = poseStack.last();
        Matrix4f mat = pose.pose();

        int light = 0x00F000F0;
        int r = 255;
        int g = 252;
        int b = 230;

        // 1. Outer Atmospheric Volumetric Cone (Soft broad atmospheric illumination)
        renderConeMesh(mat, pose, beamConsumer, light, r, g, b, 78, 0.08F, 3.80F, BEAM_LENGTH, CONE_RINGS, CONE_SIDES);

        // 2. Inner Intense Beam Core (High-intensity piercing center beam)
        renderConeMesh(mat, pose, beamConsumer, light, r, g, b, 175, 0.05F, 1.85F, CORE_LENGTH, CORE_RINGS, CORE_SIDES);

        poseStack.popPose();
    }

    private static void renderConeMesh(
            Matrix4f mat,
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int r,
            int g,
            int b,
            int baseAlpha,
            float startRadius,
            float endRadius,
            float length,
            int rings,
            int sides
    ) {
        float[][] ringX = new float[rings + 1][sides];
        float[][] ringY = new float[rings + 1][sides];
        float[] ringZ = new float[rings + 1];
        int[] ringAlpha = new int[rings + 1];

        for (int i = 0; i <= rings; i++) {
            float s = (float) i / rings;
            float z = length * s;
            float radius = startRadius + (endRadius - startRadius) * s;
            float yCenter = DOWNWARD_TILT * z;
            float along = (1.0F - s) * (1.0F - s); // MTS quadratic falloff along beam distance

            ringZ[i] = z;
            ringAlpha[i] = Math.max(0, (int) (baseAlpha * along));

            for (int j = 0; j < sides; j++) {
                double angle = (j * 2.0 * Math.PI) / sides;
                ringX[i][j] = (float) (radius * Math.cos(angle));
                ringY[i][j] = (float) (yCenter + radius * Math.sin(angle));
            }
        }

        // Render quads between successive rings with double-sided windings
        for (int i = 0; i < rings; i++) {
            int a0 = ringAlpha[i];
            int a1 = ringAlpha[i + 1];
            if (a0 == 0 && a1 == 0) continue;

            float z0 = ringZ[i];
            float z1 = ringZ[i + 1];
            float v0 = (float) i / rings;
            float v1 = (float) (i + 1) / rings;

            for (int j = 0; j < sides; j++) {
                int nextJ = (j + 1) % sides;
                float u0 = (float) j / sides;
                float u1 = (float) (j + 1) / sides;

                float x00 = ringX[i][j];
                float y00 = ringY[i][j];
                float x01 = ringX[i][nextJ];
                float y01 = ringY[i][nextJ];

                float x10 = ringX[i + 1][j];
                float y10 = ringY[i + 1][j];
                float x11 = ringX[i + 1][nextJ];
                float y11 = ringY[i + 1][nextJ];

                // Forward winding (outside facing)
                consumer.addVertex(mat, x00, y00, z0).setColor(r, g, b, a0).setUv(u0, v0)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
                consumer.addVertex(mat, x01, y01, z0).setColor(r, g, b, a0).setUv(u1, v0)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
                consumer.addVertex(mat, x11, y11, z1).setColor(r, g, b, a1).setUv(u1, v1)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
                consumer.addVertex(mat, x10, y10, z1).setColor(r, g, b, a1).setUv(u0, v1)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);

                // Reverse winding (inside facing)
                consumer.addVertex(mat, x00, y00, z0).setColor(r, g, b, a0).setUv(u0, v0)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
                consumer.addVertex(mat, x10, y10, z1).setColor(r, g, b, a1).setUv(u0, v1)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
                consumer.addVertex(mat, x11, y11, z1).setColor(r, g, b, a1).setUv(u1, v1)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
                consumer.addVertex(mat, x01, y01, z0).setColor(r, g, b, a0).setUv(u1, v0)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
            }
        }
    }

    /**
     * Road / ground surface illumination pool cast by vehicle headlights.
     */
    private static void renderRoadIllumination(ZooVehicleEntity entity, PoseStack poseStack, MultiBufferSource buffer, float frontZ) {
        if (entity.level() == null) {
            return;
        }

        float yawDeg = entity.getYRot();
        double rad = Math.toRadians(yawDeg);
        double fwdX = -Math.sin(rad);
        double fwdZ = Math.cos(rad);

        VertexConsumer spotConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(FLARE_TEXTURE));
        int r = 255;
        int g = 252;
        int b = 230;
        int light = 0x00F000F0;

        // Render multiple connected ground illumination pools along the vehicle's forward travel path
        int segments = 5;
        for (int i = 1; i <= segments; i++) {
            float distForward = frontZ + 4.0F + i * 4.2F;
            float s = (float) i / segments;
            float along = (1.0F - s) * (1.0F - s);
            int alpha = (int) (130 * along);
            if (alpha < 6) continue;

            float halfWidth = 1.3F + s * 2.2F;
            float halfLength = 2.4F + s * 1.6F;

            // Sample ground at this forward point
            Vec3 sampleWorldPos = entity.position().add(fwdX * distForward, 0.5, fwdZ * distForward);
            ClipContext groundCheck = new ClipContext(
                    sampleWorldPos.add(0.0, 1.5, 0.0),
                    sampleWorldPos.add(0.0, -3.0, 0.0),
                    ClipContext.Block.VISUAL,
                    ClipContext.Fluid.NONE,
                    entity
            );
            BlockHitResult groundHit = entity.level().clip(groundCheck);
            if (groundHit.getType() == HitResult.Type.BLOCK && groundHit.getDirection() == Direction.UP) {
                Vec3 hitPos = groundHit.getLocation();
                Vec3 rel = hitPos.subtract(entity.position());
                Vec3 localPos = rel.yRot(yawDeg * Mth.DEG_TO_RAD);

                poseStack.pushPose();
                poseStack.translate(localPos.x, localPos.y + 0.035, localPos.z);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F)); // Align flat on ground

                var p = poseStack.last();
                Matrix4f m = p.pose();

                spotConsumer.addVertex(m, -halfWidth, -halfLength, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0.0F, 0.0F, 1.0F);
                spotConsumer.addVertex(m, halfWidth, -halfLength, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0.0F, 0.0F, 1.0F);
                spotConsumer.addVertex(m, halfWidth, halfLength, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0.0F, 0.0F, 1.0F);
                spotConsumer.addVertex(m, -halfWidth, halfLength, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0.0F, 0.0F, 1.0F);

                poseStack.popPose();
            }
        }
    }

    /**
     * Soft projected spotlight disc when headlights strike a vertical wall or obstacle.
     */
    private static void renderProjectedWallSpot(
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
        Vec3 localHl = new Vec3(localX, localY, localZ);
        Vec3 worldHl = entity.position().add(ZooVehicleEntity.rotateLocalOffset(localHl, yawDeg));

        double rad = Math.toRadians(yawDeg);
        double fwdX = -Math.sin(rad);
        double fwdZ = Math.cos(rad);
        Vec3 lookDir = new Vec3(fwdX, DOWNWARD_TILT, fwdZ).normalize();

        Vec3 endPos = worldHl.add(lookDir.scale(BEAM_LENGTH));
        ClipContext ctx = new ClipContext(worldHl, endPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity);
        BlockHitResult hit = entity.level().clip(ctx);

        if (hit.getType() == HitResult.Type.BLOCK && hit.getDirection() != Direction.UP && hit.getDirection() != Direction.DOWN) {
            Vec3 hitPos = hit.getLocation();
            double dist = worldHl.distanceTo(hitPos);
            if (dist > 1.0 && dist < BEAM_LENGTH) {
                float intensity = 1.0F - (float) (dist / BEAM_LENGTH);
                int alpha = (int) (175 * intensity * intensity);
                if (alpha < 10) return;

                float spotRadius = 0.95F + (float) (dist * 0.075F);
                Direction dir = hit.getDirection();

                Vec3 rel = hitPos.subtract(worldHl);
                Vec3 localHit = rel.yRot(yawDeg * Mth.DEG_TO_RAD);

                poseStack.pushPose();
                poseStack.translate(localHit.x, localHit.y, localHit.z);

                Vec3 normalLocal = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ()).yRot(yawDeg * Mth.DEG_TO_RAD);
                poseStack.translate(normalLocal.x * 0.035, normalLocal.y * 0.035, normalLocal.z * 0.035);

                if (dir == Direction.NORTH) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg));
                } else if (dir == Direction.SOUTH) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg + 180.0F));
                } else if (dir == Direction.WEST) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg - 90.0F));
                } else if (dir == Direction.EAST) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg + 90.0F));
                }

                VertexConsumer spotConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(FLARE_TEXTURE));
                var p = poseStack.last();
                Matrix4f m = p.pose();

                int r = 255;
                int g = 252;
                int b = 230;
                int light = 0x00F000F0;

                spotConsumer.addVertex(m, -spotRadius, -spotRadius, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0.0F, 0.0F, 1.0F);
                spotConsumer.addVertex(m, spotRadius, -spotRadius, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0.0F, 0.0F, 1.0F);
                spotConsumer.addVertex(m, spotRadius, spotRadius, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0.0F, 0.0F, 1.0F);
                spotConsumer.addVertex(m, -spotRadius, spotRadius, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0.0F, 0.0F, 1.0F);

                poseStack.popPose();
            }
        }
    }
}
