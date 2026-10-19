package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.entity.ZooZookeeperEntity;
import net.zootycoon.network.ZooNetworkPayloads;
import org.joml.Quaternionf;

public class ZooWorldIndicatorRenderer {

    private static final ResourceLocation HUNGER_TEXTURE =
            ZooTycoonMod.id("textures/gui/icons/hunger_indicator.png");
    private static final ResourceLocation EXCLAMATION_TEXTURE =
            ZooTycoonMod.id("textures/gui/icons/exclamation_point.png");

    private static final double MAX_INDICATOR_DISTANCE = 48.0;

    public static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = camera.rotation();
        PoseStack poseStack = context.matrixStack();
        MultiBufferSource buffer = context.consumers();
        if (buffer == null) return;

        double maxRenderDist = mc.options.getEffectiveRenderDistance() * 16.0;
        double maxDist = Math.min(MAX_INDICATOR_DISTANCE, maxRenderDist);
        double maxDistSqr = maxDist * maxDist;

        double now = System.currentTimeMillis();
        double bob = Math.sin(now / 300.0) * 0.04;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living instanceof Player || !living.isAlive()) {
                continue;
            }

            double ex = living.getX();
            double ey = living.getY();
            double ez = living.getZ();
            double dx = ex - camPos.x;
            double dy = (ey + living.getBbHeight()) - camPos.y;
            double dz = ez - camPos.z;
            double distSqr = dx * dx + dy * dy + dz * dz;

            // Early distance culling before PoseStack or sync data lookup
            if (distSqr > maxDistSqr) continue;

            // 1. Zookeeper indicator: orange exclamation mark if out of food stock
            if (living instanceof ZooZookeeperEntity keeper) {
                if (keeper.getFoodStock() <= 0) {
                    double renderY = (ey + keeper.getBbHeight() + 1.25D + bob) - camPos.y;
                    renderBillboard(
                            buffer, poseStack, RenderType.entityCutout(EXCLAMATION_TEXTURE),
                            dx, renderY, dz,
                            0.20f, 0.28f,
                            camRot,
                            255, 255, 255, 255
                    );
                }
                continue;
            }

            // 1b. Builder indicator: orange exclamation mark if needing tools, materials, or wage payment
            if (living instanceof net.zootycoon.entity.ZooBuilderEntity builder) {
                if (builder.needsSomething()) {
                    double renderY = (ey + builder.getBbHeight() + 1.25D + bob) - camPos.y;
                    renderBillboard(
                            buffer, poseStack, RenderType.entityCutout(EXCLAMATION_TEXTURE),
                            dx, renderY, dz,
                            0.20f, 0.28f,
                            camRot,
                            255, 255, 255, 255
                    );
                }
                continue;
            }

            // 1b-2. Cashier indicator: exclamation mark if stock is empty
            if (living instanceof net.zootycoon.entity.ZooCashierEntity cashier) {
                if (cashier.isOutOfStock()) {
                    double renderY = (ey + cashier.getBbHeight() + 1.25D + bob) - camPos.y;
                    renderBillboard(
                            buffer, poseStack, RenderType.entityCutout(EXCLAMATION_TEXTURE),
                            dx, renderY, dz,
                            0.20f, 0.28f,
                            camRot,
                            255, 255, 255, 255
                    );
                }
                continue;
            }

            // 1c. Visitor mood icon: cleanly billboarded to camera at natural height just above head (hidden when leaving)
            if (living instanceof net.zootycoon.entity.ZooVisitorEntity visitor) {
                if (visitor.isLeaving() || visitor.isTimeToLeave()) {
                    continue;
                }
                var mood = visitor.getMood();
                if (mood != null && mood != net.zootycoon.entity.ZooVisitorEntity.VisitorMood.NEUTRAL) {
                    ResourceLocation tex = switch (mood) {
                        case TOILET -> ZooTycoonMod.id("textures/gui/moods/mood_toilet.png");
                        case AMAZED -> ZooTycoonMod.id("textures/gui/moods/mood_amazed.png");
                        case ADORED -> ZooTycoonMod.id("textures/gui/moods/mood_adored.png");
                        case HUNGRY -> ZooTycoonMod.id("textures/gui/moods/mood_hungry.png");
                        case THIRSTY -> ZooTycoonMod.id("textures/gui/moods/mood_thirsty.png");
                        default -> null;
                    };
                    if (tex != null) {
                        double hatOffset = visitor.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty() ? 0.65D : 1.05D;
                        double renderY = (ey + visitor.getBbHeight() + hatOffset + bob) - camPos.y;
                        renderBillboard(
                                buffer, poseStack, RenderType.entityCutout(tex),
                                dx, renderY, dz,
                                0.20f, 0.20f,
                                camRot,
                                255, 255, 255, 255
                        );
                    }
                }
                continue;
            }

            // 2. Animal indicator: hunger bar with down arrow if hunger <= 50
            if (!ZooTycoonClient.clientAnimalMap.isEmpty()) {
                ZooNetworkPayloads.AnimalSyncData syncData = ZooTycoonClient.getAnimalData(living.getId());
                if (syncData != null && syncData.hunger() <= 50) {
                    double heightOffset = living.hasCustomName() ? 0.75D : 0.45D;
                    double renderY = (ey + living.getBbHeight() + heightOffset + bob) - camPos.y;

                    float halfSize = 0.22f;
                    int r = 255, g = 255, b = 255;
                    if (syncData.hunger() <= 15) {
                        float pulse = (float) (Math.sin(now / 150.0) * 0.04);
                        halfSize += pulse;
                        g = 210;
                        b = 210;
                    }

                    renderBillboard(
                            buffer, poseStack, RenderType.entityCutout(HUNGER_TEXTURE),
                            dx, renderY, dz,
                            halfSize, halfSize,
                            camRot,
                            r, g, b, 255
                    );
                }
            }
        }
    }

    private static void renderBillboard(
            MultiBufferSource buffer,
            PoseStack poseStack,
            RenderType renderType,
            double x, double y, double z,
            float halfW, float halfH,
            Quaternionf cameraRotation,
            int r, int g, int b, int a
    ) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(cameraRotation);

        VertexConsumer consumer = buffer.getBuffer(renderType);
        var pose = poseStack.last();

        consumer.addVertex(pose, -halfW, -halfH, 0.0f)
                .setColor(r, g, b, a)
                .setUv(0.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        consumer.addVertex(pose, halfW, -halfH, 0.0f)
                .setColor(r, g, b, a)
                .setUv(1.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        consumer.addVertex(pose, halfW, halfH, 0.0f)
                .setColor(r, g, b, a)
                .setUv(1.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        consumer.addVertex(pose, -halfW, halfH, 0.0f)
                .setColor(r, g, b, a)
                .setUv(0.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        poseStack.popPose();
    }
}
