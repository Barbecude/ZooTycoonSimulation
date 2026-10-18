package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.animal.ZooClimate;
import net.zootycoon.client.inspect.HologramBarRenderer;
import net.zootycoon.client.inspect.InspectionClientCache;
import net.zootycoon.client.inspect.InspectionTargetTracker;
import net.zootycoon.network.InspectionPayloads;
import org.joml.Matrix4f;

import java.util.List;

/**
 * World-space in-world billboarded inspection hologram renderer.
 * Displays species identity, visual health bar with vanilla heart icon,
 * visual hunger, thirst, and cleanliness status bars, and 3D favorite food items.
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

        if (target == null || !target.isAlive() || !InspectionTargetTracker.isZooRegisteredAnimal(target)) {
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
        double yOffset = entity.getBbHeight() + 0.15;
        if (entity.hasCustomName()) {
            yOffset += 0.25;
        }
        double entY = entity.getY() + yOffset - camPos.y;
        double entZ = entity.getZ() - camPos.z;

        poseStack.pushPose();
        poseStack.translate(entX, entY, entZ);
        poseStack.mulPose(camera.rotation());
        float scale = 0.0135f;
        poseStack.scale(scale, -scale, scale);

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
        boolean isWet = isRegistered && InspectionPayloads.isAnimalWet(entity.getId());
        if (isRegistered && snapshot.cleanliness() >= 0) {
            int cVal = snapshot.cleanliness();
            cleanText = cVal + "%" + (isWet ? " " + Component.translatable("zootycoon.inspect.cleanliness.wet").getString() : "");
            cleanTextColor = HologramBarRenderer.getStatusColor((float) cVal);
            cleanProgress = Math.max(0.0f, Math.min(1.0f, cVal / 100.0f));
        } else {
            cleanText = "-";
            cleanTextColor = HologramBarRenderer.COLOR_MUTED;
            cleanProgress = 0.0f;
        }

        // Status labels to display on the left of each bar
        Component hpLabel = Component.translatable("zootycoon.inspect.health");
        Component hungerLabel = Component.translatable("zootycoon.inspect.hunger");
        Component thirstLabel = Component.translatable("zootycoon.inspect.thirst");
        Component cleanLabel = Component.translatable("zootycoon.inspect.cleanliness");

        int maxLabelWidth = Math.max(font.width(hpLabel),
                Math.max(font.width(hungerLabel),
                Math.max(font.width(thirstLabel), font.width(cleanLabel))));

        // Retrieve foods data (dynamically scans all loaded mods)
        List<ItemStack> foods = AnimalDietRegistry.getFoodsForAnimal(entity);
        StringBuilder sb = new StringBuilder();
        int showCount = Math.min(3, foods.size());
        for (int i = 0; i < showCount; i++) {
            if (i > 0) sb.append(", ");
            sb.append(foods.get(i).getHoverName().getString());
        }
        if (foods.size() > showCount) {
            sb.append(" (+").append(foods.size() - showCount).append(")");
        }
        String foodNamesText = !foods.isEmpty() ? sb.toString() : "-";
        Component foodNamesComp = Component.literal(foodNamesText).withStyle(ChatFormatting.YELLOW);
        int foodNamesWidth = font.width(foodNamesComp);

        Component foodHeaderComponent = Component.translatable("zootycoon.inspect.favorite_food")
                .withStyle(ChatFormatting.WHITE);
        int foodHeaderWidth = font.width(foodHeaderComponent);
        float foodSectionWidth = Math.max((float) foodHeaderWidth, (float) foodNamesWidth);

        // Retrieve climate data (season & temperature category)
        InspectionPayloads.ClimateData climateData = InspectionPayloads.getLatestClimate(entity.getId());
        String currentSeason = climateData != null ? climateData.season() : ZooClimate.getCurrentSeason(entity.level());
        String currentTempCat = climateData != null ? climateData.tempCategory() : ZooClimate.getCategory(ZooClimate.getEffectiveTemperature(entity)).key;

        Component seasonComp = ZooClimate.getSeasonComponent(currentSeason);
        Component tempComp = ZooClimate.getTempCategoryComponent(currentTempCat);
        Component climateComponent = Component.translatable("zootycoon.inspect.climate", seasonComp, tempComp);
        int climateWidth = font.width(climateComponent);

        // Retrieve habitat data (status & unmet requirements hint)
        InspectionPayloads.HabitatData habitatData = InspectionPayloads.getLatestHabitat(entity.getId());
        Component habitatStatusComp;
        int habitatStatusColor;
        boolean hasHabitatHint = false;
        Component habitatHintComp = null;

        if (habitatData != null) {
            String hStatus = habitatData.status();
            String hNeeds = habitatData.needs();
            if ("SUITABLE".equalsIgnoreCase(hStatus)) {
                habitatStatusComp = Component.translatable("zootycoon.inspect.habitat",
                        Component.translatable("zootycoon.inspect.habitat.suitable"));
                habitatStatusColor = 0xFF55FF55;
            } else if ("UNSUITABLE".equalsIgnoreCase(hStatus)) {
                habitatStatusComp = Component.translatable("zootycoon.inspect.habitat",
                        Component.translatable("zootycoon.inspect.habitat.unsuitable"));
                habitatStatusColor = 0xFFFF5555;
                if (hNeeds != null && !hNeeds.isEmpty()) {
                    hasHabitatHint = true;
                    habitatHintComp = Component.literal(hNeeds);
                }
            } else {
                habitatStatusComp = Component.translatable("zootycoon.inspect.habitat",
                        Component.translatable("zootycoon.inspect.habitat.no_data"));
                habitatStatusColor = HologramBarRenderer.COLOR_MUTED;
            }
        } else {
            habitatStatusComp = Component.translatable("zootycoon.inspect.habitat",
                    Component.translatable("zootycoon.inspect.habitat.no_data"));
            habitatStatusColor = HologramBarRenderer.COLOR_MUTED;
        }

        int habitatStatusWidth = font.width(habitatStatusComp);
        int habitatHintWidth = (hasHabitatHint && habitatHintComp != null) ? font.width(habitatHintComp) : 0;

        // Calculate layout geometry
        Component titleComponent = entity.getDisplayName();
        int titleWidth = font.width(titleComponent);

        float iconWidth = 9.0f;
        float gapIconToLabel = 3.0f;
        float gapLabelToBar = 4.0f;
        float barWidth = 42.0f;
        float barHeight = 4.0f;
        float gapBarToText = 4.0f;

        int maxTextWidth = Math.max(font.width(hpText),
                Math.max(font.width(hungerText),
                Math.max(font.width(thirstText), font.width(cleanText))));

        float rowBlockWidth = iconWidth + gapIconToLabel + maxLabelWidth + gapLabelToBar + barWidth + gapBarToText + Math.max(20.0f, (float) maxTextWidth);
        float boxWidth = Math.max(rowBlockWidth + 14.0f,
                Math.max(titleWidth + 14.0f,
                Math.max(foodSectionWidth + 14.0f,
                Math.max(climateWidth + 14.0f,
                Math.max(habitatStatusWidth + 14.0f, habitatHintWidth + 14.0f)))));

        float rowHeight = 11.0f;
        float headerHeight = 13.0f;
        float barsHeight = 4.0f * rowHeight;
        float habitatGap = 3.0f;
        float habitatStatusHeight = 10.0f;
        float habitatHintHeight = hasHabitatHint ? 10.0f : 0.0f;
        float habitatSectionHeight = habitatStatusHeight + habitatHintHeight;
        float climateGap = 3.0f;
        float climateHeight = 10.0f;
        float foodGap = 4.0f;
        float foodHeaderHeight = 10.0f;
        float foodNamesHeight = 10.0f;
        float foodSectionHeight = foodHeaderHeight + foodNamesHeight;
        float contentHeight = headerHeight + barsHeight + habitatGap + habitatSectionHeight + climateGap + climateHeight + foodGap + foodSectionHeight;
        float totalBoxHeight = contentHeight + 8.0f; // 4px padding top, 4px padding bottom

        float minX = -boxWidth / 2.0f;
        float maxX = boxWidth / 2.0f;
        float maxY = 0.0f;
        float startY = maxY - totalBoxHeight;

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource buffer = context.consumers();

        if (buffer != null) {
            // 1. Draw rounded dark background plate (semi-transparent 0xAA0B0E14)
            HologramBarRenderer.drawBackgroundPlate(buffer, matrix, minX, startY, maxX, maxY, LightTexture.FULL_BRIGHT);

            // 2. Layout positions
            float rowStartX = -rowBlockWidth / 2.0f;
            float iconX = rowStartX;
            float labelX = iconX + iconWidth + gapIconToLabel;
            float barX = labelX + maxLabelWidth + gapLabelToBar;
            float textX = barX + barWidth + gapBarToText;
            float curY = startY + 4.0f;

            float titleX = -titleWidth / 2.0f;
            float titleY = curY;
            curY += 12.0f; // Gap between title and bar rows

            float healthRowY = curY;
            float hungerRowY = curY + rowHeight;
            float thirstRowY = curY + (rowHeight * 2.0f);
            float cleanRowY = curY + (rowHeight * 3.0f);
            curY += (rowHeight * 4.0f);

            curY += habitatGap;
            float habitatStatusX = -habitatStatusWidth / 2.0f;
            float habitatStatusY = curY;
            curY += habitatStatusHeight;

            float habitatHintX = 0;
            float habitatHintY = 0;
            if (hasHabitatHint && habitatHintComp != null) {
                habitatHintX = -habitatHintWidth / 2.0f;
                habitatHintY = curY;
                curY += habitatHintHeight;
            }

            curY += climateGap;
            float climateX = -climateWidth / 2.0f;
            float climateY = curY;
            curY += climateHeight;

            curY += foodGap;
            float foodHeaderX = -foodHeaderWidth / 2.0f;
            float foodHeaderY = curY;
            curY += foodHeaderHeight;
            float foodNamesX = -foodNamesWidth / 2.0f;
            float foodNamesY = curY;

            // 3. Draw visual bars (solid quads with border and track)
            VertexConsumer barConsumer = buffer.getBuffer(RenderType.textBackground());
            HologramBarRenderer.drawBar(barConsumer, matrix, barX, healthRowY + 3.5f, barWidth, barHeight, hpProgress, HologramBarRenderer.HEALTH_COLOR, HologramBarRenderer.HEALTH_TRACK, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawBar(barConsumer, matrix, barX, hungerRowY + 3.5f, barWidth, barHeight, hungerProgress, HologramBarRenderer.HUNGER_COLOR, HologramBarRenderer.HUNGER_TRACK, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawBar(barConsumer, matrix, barX, thirstRowY + 3.5f, barWidth, barHeight, thirstProgress, HologramBarRenderer.THIRST_COLOR, HologramBarRenderer.THIRST_TRACK, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawBar(barConsumer, matrix, barX, cleanRowY + 3.5f, barWidth, barHeight, cleanProgress, HologramBarRenderer.CLEANLINESS_COLOR, HologramBarRenderer.CLEANLINESS_TRACK, LightTexture.FULL_BRIGHT);

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch(RenderType.textBackground());
            }

            // 4. Draw icons (heart, hunger, thirst, cleanliness)
            HologramBarRenderer.drawVanillaHeartIcon(mc, font, buffer, matrix, iconX, healthRowY + 1.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawVanillaHungerIcon(mc, buffer, matrix, iconX, hungerRowY + 1.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawTexturedIcon(buffer, matrix, HologramBarRenderer.THIRST_ICON, iconX, thirstRowY + 1.0f, 9.0f, 9.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawTexturedIcon(buffer, matrix, HologramBarRenderer.CLEANLINESS_ICON, iconX, cleanRowY + 1.0f, 9.0f, 9.0f, LightTexture.FULL_BRIGHT);

            // 5. Draw labels on the left of each bar
            font.drawInBatch(hpLabel, labelX, healthRowY + 1.0f, 0xFFDDDDDD, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(hungerLabel, labelX, hungerRowY + 1.0f, 0xFFDDDDDD, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(thirstLabel, labelX, thirstRowY + 1.0f, 0xFFDDDDDD, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(cleanLabel, labelX, cleanRowY + 1.0f, 0xFFDDDDDD, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            // 6. Draw values on the right of each bar
            font.drawInBatch(hpText, textX, healthRowY + 1.0f, hpTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(hungerText, textX, hungerRowY + 1.0f, hungerTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(thirstText, textX, thirstRowY + 1.0f, thirstTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(cleanText, textX, cleanRowY + 1.0f, cleanTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            // 7. Draw title, habitat, climate, and food list
            font.drawInBatch(titleComponent, titleX, titleY, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            font.drawInBatch(habitatStatusComp, habitatStatusX, habitatStatusY, habitatStatusColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            if (hasHabitatHint && habitatHintComp != null) {
                font.drawInBatch(habitatHintComp, habitatHintX, habitatHintY, 0xFFFFAA00, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            }

            font.drawInBatch(climateComponent, climateX, climateY, HologramBarRenderer.COLOR_MUTED, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            font.drawInBatch(foodHeaderComponent, foodHeaderX, foodHeaderY, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(foodNamesComp, foodNamesX, foodNamesY, 0xFFFFFF55, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
        }

        poseStack.popPose();
    }
}
