package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.client.inspect.HologramBarRenderer;
import net.zootycoon.client.inspect.InspectionClientCache;
import net.zootycoon.client.inspect.InspectionTargetTracker;
import org.joml.Matrix4f;

/**
 * World-space in-world billboarded inspection hologram renderer.
 * Displays species identity, visual health bar with vanilla heart icon, and
 * visual hunger, thirst, and cleanliness status bars.
 */
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

        // Retrieve actual entity health data
        float currentHp = entity.getHealth();
        float maxHp = entity.getMaxHealth();
        int hpInt = (int) Math.ceil(currentHp);
        int maxHpInt = (int) Math.ceil(maxHp);
        String hpText = hpInt + "/" + maxHpInt;
        float hpPct = (currentHp / Math.max(1.0f, maxHp)) * 100.0f;
        int hpTextColor = HologramBarRenderer.getStatusColor(hpPct);
        float hpProgress = Math.max(0.0f, Math.min(1.0f, currentHp / Math.max(1.0f, maxHp)));

        // Read snapshot from server sync cache
        InspectionClientCache.Snapshot snapshot = InspectionClientCache.getSnapshot(entity.getId());
        boolean isRegistered = snapshot != null && snapshot.registered();

        // Prepare hunger status
        String hungerText;
        int hungerTextColor;
        float hungerProgress;
        if (isRegistered && snapshot.hunger() >= 0) {
            int hVal = snapshot.hunger();
            hungerText = hVal + "%";
            hungerTextColor = HologramBarRenderer.getStatusColor((float) hVal);
            hungerProgress = Math.max(0.0f, Math.min(1.0f, hVal / 100.0f));
        } else {
            hungerText = "-";
            hungerTextColor = HologramBarRenderer.COLOR_MUTED;
            hungerProgress = 0.0f;
        }

        // Prepare thirst status
        String thirstText;
        int thirstTextColor;
        float thirstProgress;
        if (isRegistered && snapshot.thirst() >= 0) {
            int tVal = snapshot.thirst();
            thirstText = tVal + "%";
            thirstTextColor = HologramBarRenderer.getStatusColor((float) tVal);
            thirstProgress = Math.max(0.0f, Math.min(1.0f, tVal / 100.0f));
        } else {
            thirstText = "-";
            thirstTextColor = HologramBarRenderer.COLOR_MUTED;
            thirstProgress = 0.0f;
        }

        // Prepare cleanliness status
        String cleanText;
        int cleanTextColor;
        float cleanProgress;
        if (isRegistered && snapshot.cleanliness() >= 0) {
            int cVal = snapshot.cleanliness();
            cleanText = cVal + "%";
            cleanTextColor = HologramBarRenderer.getStatusColor((float) cVal);
            cleanProgress = Math.max(0.0f, Math.min(1.0f, cVal / 100.0f));
        } else {
            cleanText = "-";
            cleanTextColor = HologramBarRenderer.COLOR_MUTED;
            cleanProgress = 0.0f;
        }

        // Calculate layout geometry
        Component titleComponent = entity.getDisplayName();
        int titleWidth = font.width(titleComponent);
        Component unregComponent = Component.translatable("zootycoon.inspect.unregistered");
        int unregWidth = isRegistered ? 0 : font.width(unregComponent);
        int maxHeaderWidth = Math.max(titleWidth, unregWidth);

        float iconWidth = 9.0f;
        float gapIconToBar = 4.0f;
        float barWidth = 42.0f;
        float barHeight = 4.0f;
        float gapBarToText = 4.0f;

        int maxTextWidth = Math.max(font.width(hpText),
                Math.max(font.width(hungerText),
                Math.max(font.width(thirstText), font.width(cleanText))));

        float rowBlockWidth = iconWidth + gapIconToBar + barWidth + gapBarToText + Math.max(24.0f, (float) maxTextWidth);
        float boxWidth = Math.max(rowBlockWidth + 12.0f, maxHeaderWidth + 14.0f);

        float rowHeight = 11.0f;
        float headerHeight = isRegistered ? 13.0f : 23.0f;
        float contentHeight = headerHeight + (4.0f * rowHeight);
        float totalBoxHeight = contentHeight + 8.0f; // 4px padding top, 4px padding bottom

        float minX = -boxWidth / 2.0f;
        float maxX = boxWidth / 2.0f;
        float startY = -totalBoxHeight / 2.0f;
        float maxY = startY + totalBoxHeight;

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource buffer = context.consumers();

        if (buffer != null) {
            // 1. Draw rounded dark background plate (semi-transparent 0xAA0B0E14)
            HologramBarRenderer.drawBackgroundPlate(buffer, matrix, minX, startY, maxX, maxY, LightTexture.FULL_BRIGHT);

            // 2. Draw visual bars (solid quads with border and track)
            float rowStartX = -rowBlockWidth / 2.0f;
            float iconX = rowStartX;
            float barX = iconX + iconWidth + gapIconToBar;
            float textX = barX + barWidth + gapBarToText;
            float curY = startY + 4.0f;

            float titleX = -titleWidth / 2.0f;
            float titleY = curY;
            curY += 10.0f;

            float unregX = 0;
            float unregY = 0;
            if (!isRegistered) {
                unregX = -unregWidth / 2.0f;
                unregY = curY;
                curY += 10.0f;
            }

            curY += 2.0f; // Gap between header and bar rows

            float healthRowY = curY;
            float hungerRowY = curY + rowHeight;
            float thirstRowY = curY + (rowHeight * 2.0f);
            float cleanRowY = curY + (rowHeight * 3.0f);

            VertexConsumer barConsumer = buffer.getBuffer(RenderType.textBackground());
            HologramBarRenderer.drawBar(barConsumer, matrix, barX, healthRowY + 3.5f, barWidth, barHeight, hpProgress, HologramBarRenderer.HEALTH_COLOR, HologramBarRenderer.HEALTH_TRACK, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawBar(barConsumer, matrix, barX, hungerRowY + 3.5f, barWidth, barHeight, hungerProgress, HologramBarRenderer.HUNGER_COLOR, HologramBarRenderer.HUNGER_TRACK, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawBar(barConsumer, matrix, barX, thirstRowY + 3.5f, barWidth, barHeight, thirstProgress, HologramBarRenderer.THIRST_COLOR, HologramBarRenderer.THIRST_TRACK, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawBar(barConsumer, matrix, barX, cleanRowY + 3.5f, barWidth, barHeight, cleanProgress, HologramBarRenderer.CLEANLINESS_COLOR, HologramBarRenderer.CLEANLINESS_TRACK, LightTexture.FULL_BRIGHT);

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch(RenderType.textBackground());
            }

            // 3. Draw vanilla heart and hunger icons (using Minecraft vanilla GUI sprites)
            HologramBarRenderer.drawVanillaHeartIcon(mc, font, buffer, matrix, iconX, healthRowY + 1.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawVanillaHungerIcon(mc, buffer, matrix, iconX, hungerRowY + 1.0f, LightTexture.FULL_BRIGHT);

            // 4. Draw custom 9x9 status icons (thirst and cleanliness)
            HologramBarRenderer.drawTexturedIcon(buffer, matrix, HologramBarRenderer.THIRST_ICON, iconX, thirstRowY + 1.0f, 9.0f, 9.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawTexturedIcon(buffer, matrix, HologramBarRenderer.CLEANLINESS_ICON, iconX, cleanRowY + 1.0f, 9.0f, 9.0f, LightTexture.FULL_BRIGHT);

            // 5. Draw text components (clean font without shadow)
            font.drawInBatch(titleComponent, titleX, titleY, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            if (!isRegistered) {
                font.drawInBatch(unregComponent, unregX, unregY, HologramBarRenderer.COLOR_MUTED, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            }
            font.drawInBatch(hpText, textX, healthRowY + 1.0f, hpTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(hungerText, textX, hungerRowY + 1.0f, hungerTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(thirstText, textX, thirstRowY + 1.0f, thirstTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(cleanText, textX, cleanRowY + 1.0f, cleanTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
        }

        poseStack.popPose();
    }
}
