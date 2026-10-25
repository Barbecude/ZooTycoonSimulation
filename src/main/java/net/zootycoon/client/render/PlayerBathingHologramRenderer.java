package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.client.bathing.PlayerBathingClientController;
import net.zootycoon.client.inspect.HologramBarRenderer;
import net.zootycoon.util.ZooTranslations;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class PlayerBathingHologramRenderer {

    private static final float HOLO_SCALE = 0.015f;
    private static final ItemStack BRUSH_STACK = new ItemStack(Items.BRUSH);

    public static void render(WorldRenderContext context) {
        if (!PlayerBathingClientController.isBathing()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        LivingEntity target = PlayerBathingClientController.getTargetAnimal();
        if (target == null || !target.isAlive() || PlayerBathingClientController.isAnimalClean(target)) {
            return;
        }

        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = camera.rotation();
        PoseStack poseStack = context.matrixStack();
        MultiBufferSource buffer = context.consumers();
        Font font = mc.font;

        float partialTick = context.tickCounter().getGameTimeDeltaPartialTick(true);
        double ex = Mth.lerp(partialTick, target.xOld, target.getX());
        double ey = Mth.lerp(partialTick, target.yOld, target.getY());
        double ez = Mth.lerp(partialTick, target.zOld, target.getZ());

        double renderY = ey + target.getBbHeight() + 0.45D;

        float progress = PlayerBathingClientController.getProgress();
        int stage = PlayerBathingClientController.getCurrentStage();

        String title;
        int barColor;

        if (stage == PlayerBathingClientController.STAGE_WATERING) {
            title = ZooTranslations.get("zootycoon.bathing.progress.watering");
            if (title == null || title.equals("zootycoon.bathing.progress.watering")) {
                title = "Menyiram...";
            }
            barColor = 0xFF29B6F6;
        } else {
            title = ZooTranslations.get("zootycoon.bathing.progress.brushing");
            if (title == null || title.equals("zootycoon.bathing.progress.brushing")) {
                title = "Menyikat...";
            }
            barColor = 0xFF26A69A;
        }

        String percentText = (int) (progress * 100.0f) + "%";

        poseStack.pushPose();
        poseStack.translate(ex - camPos.x, renderY - camPos.y, ez - camPos.z);
        poseStack.mulPose(camRot);
        poseStack.scale(HOLO_SCALE, -HOLO_SCALE, HOLO_SCALE);

        Matrix4f matrix = poseStack.last().pose();
        int light = 0xF000F0; // Full brightness

        float boxWidth = 92.0f;
        float boxHeight = 26.0f;
        float minX = -boxWidth / 2.0f;
        float maxX = boxWidth / 2.0f;
        float minY = -boxHeight / 2.0f;
        float maxY = boxHeight / 2.0f;

        // 1. Dark Chamfered Background Plate
        VertexConsumer bgConsumer = buffer.getBuffer(RenderType.textBackground());
        HologramBarRenderer.drawRect(bgConsumer, matrix, minX + 1.0f, minY, maxX - 1.0f, maxY, -0.02f, 0xDD0B0E14, light);
        HologramBarRenderer.drawRect(bgConsumer, matrix, minX, minY + 1.0f, maxX, maxY - 1.0f, -0.02f, 0xDD0B0E14, light);

        // 1b. Border Frame (Dark Slate)
        HologramBarRenderer.drawRect(bgConsumer, matrix, minX, minY + 1.0f, minX + 1.0f, maxY - 1.0f, -0.016f, 0xFF2A2D36, light);
        HologramBarRenderer.drawRect(bgConsumer, matrix, maxX - 1.0f, minY + 1.0f, maxX, maxY - 1.0f, -0.016f, 0xFF2A2D36, light);
        HologramBarRenderer.drawRect(bgConsumer, matrix, minX + 1.0f, minY, maxX - 1.0f, minY + 1.0f, -0.016f, 0xFF2A2D36, light);
        HologramBarRenderer.drawRect(bgConsumer, matrix, minX + 1.0f, maxY - 1.0f, maxX - 1.0f, maxY, -0.016f, 0xFF2A2D36, light);

        // 2. Icon (Droplet or Minecraft Brush item)
        float iconX = minX + 5.0f;
        float iconY = minY + 4.0f;
        if (stage == PlayerBathingClientController.STAGE_WATERING) {
            HologramBarRenderer.drawThirstIcon(buffer, matrix, iconX, iconY, light);
        } else {
            HologramBarRenderer.drawItemIcon2D(mc, buffer, matrix, BRUSH_STACK, iconX, iconY - 0.5f, 10.0f, light, 1.0f);
        }

        // 3. Title text
        font.drawInBatch(
                title,
                iconX + 12.0f,
                iconY + 1.0f,
                0xFFFFFFFF,
                false,
                matrix,
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                light
        );

        // 4. Percentage text (aligned right)
        float pctWidth = font.width(percentText);
        font.drawInBatch(
                percentText,
                maxX - 5.0f - pctWidth,
                iconY + 1.0f,
                stage == PlayerBathingClientController.STAGE_WATERING ? 0xFF55FFFF : 0xFF55FF55,
                false,
                matrix,
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                light
        );

        // 5. Progress Bar Track
        float trackX = minX + 5.0f;
        float trackW = boxWidth - 10.0f;
        float trackY = minY + 16.0f;
        float trackH = 5.0f;

        // Dark track border & background
        HologramBarRenderer.drawRect(bgConsumer, matrix, trackX - 1.0f, trackY - 1.0f, trackX + trackW + 1.0f, trackY + trackH + 1.0f, -0.015f, 0xFF05070A, light);
        HologramBarRenderer.drawRect(bgConsumer, matrix, trackX, trackY, trackX + trackW, trackY + trackH, -0.014f, 0xFF151820, light);

        // Progress Fill
        float fillW = trackW * progress;
        if (fillW > 0.5f) {
            HologramBarRenderer.drawRect(bgConsumer, matrix, trackX, trackY, trackX + fillW, trackY + trackH, -0.010f, barColor, light);
            // Subtle 1px top highlight
            HologramBarRenderer.drawRect(bgConsumer, matrix, trackX, trackY, trackX + fillW, trackY + 1.0f, -0.008f, 0x80FFFFFF, light);
        }

        // 6. Explicitly flush buffer so the hologram is rendered immediately to GPU!
        if (buffer instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        poseStack.popPose();
    }
}
