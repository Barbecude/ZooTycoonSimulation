package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.client.inspect.InspectionTargetTracker;
import org.joml.Matrix4f;

public class ZooAnimalHologramRenderer {

    public static LivingEntity currentTargetedAnimal = null;

    public static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            currentTargetedAnimal = null;
            return;
        }

        // Tick tracker on each render frame for zero-delay crosshair sensitivity
        InspectionTargetTracker.tick(mc);

        LivingEntity target = InspectionTargetTracker.getTarget();
        currentTargetedAnimal = target;

        if (target == null || !target.isAlive()) {
            return;
        }

        renderHologram(context, mc, target);
    }

    private static void renderHologram(WorldRenderContext context, Minecraft mc, LivingEntity entity) {
        PoseStack poseStack = context.matrixStack();
        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        Font font = mc.font;

        double entX = entity.getX() - camPos.x;
        double yOffset = entity.getBbHeight() + 0.65;
        if (entity.hasCustomName()) {
            yOffset += 0.3;
        }
        double entY = entity.getY() + yOffset - camPos.y;
        double entZ = entity.getZ() - camPos.z;

        poseStack.pushPose();
        poseStack.translate(entX, entY, entZ);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-0.022f, -0.022f, 0.022f);

        Component name = entity.getDisplayName();
        int width = font.width(name);
        float x = -width / 2.0f;
        float y = -font.lineHeight / 2.0f;

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource buffer = context.consumers();

        if (buffer != null) {
            float bgAlpha = 0.5f;
            int bgCol = (int) (bgAlpha * 255) << 24;

            font.drawInBatch(
                    name,
                    x,
                    y,
                    0xFFFFFFFF,
                    false,
                    matrix,
                    buffer,
                    Font.DisplayMode.NORMAL,
                    bgCol,
                    LightTexture.FULL_BRIGHT
            );

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
        }

        poseStack.popPose();
    }
}
