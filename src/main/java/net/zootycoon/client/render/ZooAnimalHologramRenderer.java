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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
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
        double yOffset = entity.getBbHeight() + 0.5;
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

        // Retrieve favorite foods data
        List<ItemStack> favoriteStacks = AnimalDietRegistry.getFavoriteFoodStacks(entity);
        boolean isFallbackDiet = AnimalDietRegistry.isFallbackDiet(entity);
        Component foodHeaderComponent;
        if (isFallbackDiet) {
            foodHeaderComponent = Component.translatable("zootycoon.inspect.favorite_food")
                    .withStyle(ChatFormatting.WHITE)
                    .append(" ")
                    .append(Component.translatable("zootycoon.inspect.general_diet").withStyle(ChatFormatting.YELLOW));
        } else {
            foodHeaderComponent = Component.translatable("zootycoon.inspect.favorite_food")
                    .withStyle(ChatFormatting.WHITE);
        }
        int foodHeaderWidth = font.width(foodHeaderComponent);

        int totalFoods = favoriteStacks.size();
        int renderedFoodCount = Math.min(4, totalFoods);
        int extraFoods = totalFoods - renderedFoodCount;
        String extraFoodsText = extraFoods > 0 ? "+" + extraFoods : "";
        int extraFoodsWidth = extraFoods > 0 ? font.width(extraFoodsText) : 0;

        float itemSlotWidth = 14.0f;
        float itemsRowWidth = (renderedFoodCount * itemSlotWidth) + (extraFoods > 0 ? extraFoodsWidth + 3.0f : 0.0f);
        float foodSectionWidth = Math.max((float) foodHeaderWidth, itemsRowWidth);

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
        float boxWidth = Math.max(rowBlockWidth + 12.0f,
                Math.max(maxHeaderWidth + 14.0f,
                Math.max(foodSectionWidth + 14.0f,
                Math.max(climateWidth + 14.0f,
                Math.max(habitatStatusWidth + 14.0f, habitatHintWidth + 14.0f)))));

        float rowHeight = 11.0f;
        float headerHeight = isRegistered ? 13.0f : 23.0f;
        float barsHeight = 4.0f * rowHeight;
        float habitatGap = 3.0f;
        float habitatStatusHeight = 10.0f;
        float habitatHintHeight = hasHabitatHint ? 10.0f : 0.0f;
        float habitatSectionHeight = habitatStatusHeight + habitatHintHeight;
        float climateGap = 3.0f;
        float climateHeight = 10.0f;
        float foodGap = 4.0f;
        float foodHeaderHeight = 10.0f;
        float foodItemsHeight = renderedFoodCount > 0 ? 14.0f : 0.0f;
        float foodSectionHeight = foodHeaderHeight + foodItemsHeight;
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

            float foodItemsY = curY + 6.0f; // center Y of 3D items
            float itemsStartX = -(itemsRowWidth / 2.0f) + (itemSlotWidth / 2.0f);

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

            // 5. Render 3D favorite food item stacks with proportional scale
            ItemRenderer itemRenderer = mc.getItemRenderer();
            for (int i = 0; i < renderedFoodCount; i++) {
                ItemStack stack = favoriteStacks.get(i);
                float itemX = itemsStartX + (i * itemSlotWidth);

                poseStack.pushPose();
                poseStack.translate(itemX, foodItemsY, -0.5f);
                poseStack.scale(7.0f, -7.0f, 7.0f);

                itemRenderer.renderStatic(
                        stack,
                        ItemDisplayContext.GROUND,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        buffer,
                        entity.level(),
                        0
                );

                poseStack.popPose();
            }

            // 6. Draw text components (sharp font using POLYGON_OFFSET to prevent Z-fighting)
            font.drawInBatch(titleComponent, titleX, titleY, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
            if (!isRegistered) {
                font.drawInBatch(unregComponent, unregX, unregY, HologramBarRenderer.COLOR_MUTED, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
            }
            font.drawInBatch(hpText, textX, healthRowY + 1.0f, hpTextColor, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(hungerText, textX, hungerRowY + 1.0f, hungerTextColor, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(thirstText, textX, thirstRowY + 1.0f, thirstTextColor, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(cleanText, textX, cleanRowY + 1.0f, cleanTextColor, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);

            // Draw habitat status line (e.g. "Habitat: Sesuai (Ideal)" or "Habitat: Tidak Sesuai")
            font.drawInBatch(habitatStatusComp, habitatStatusX, habitatStatusY, habitatStatusColor, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);

            // Draw habitat hint line if unsuitable (e.g. "Butuh: Air, Savana")
            if (hasHabitatHint && habitatHintComp != null) {
                font.drawInBatch(habitatHintComp, habitatHintX, habitatHintY, 0xFFFFAA00, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
            }

            // Draw climate line (e.g. "Musim: Musim Semi | Suhu: Sejuk / Sedang")
            font.drawInBatch(climateComponent, climateX, climateY, HologramBarRenderer.COLOR_MUTED, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);

            // Draw Favorite Food header
            font.drawInBatch(foodHeaderComponent, foodHeaderX, foodHeaderY, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);

            // Draw +N if more than 4 foods
            if (extraFoods > 0) {
                float extraX = itemsStartX + (renderedFoodCount * itemSlotWidth) - (itemSlotWidth / 2.0f) + 3.0f;
                float extraY = foodItemsY - 4.0f;
                font.drawInBatch(extraFoodsText, extraX, extraY, 0xFFAAAAAA, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
            }

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
        }

        poseStack.popPose();
    }
}
