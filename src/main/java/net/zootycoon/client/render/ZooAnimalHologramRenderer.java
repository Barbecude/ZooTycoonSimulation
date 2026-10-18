package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
import java.util.Locale;

/**
 * World-space in-world billboarded inspection hologram renderer.
 * Formatted exactly according to the user wireframe mockup:
 * [Nama Hewan]
 * [Ikon Makanan 1] [Ikon Makanan 2] ...
 * Kesehatan: [heart] XX%
 * Lapar: [hunger] XX%
 * Haus: [thirst] XX%
 * Kebersihan: [clean] XX%
 * Temperature: [thermometer] XX°C (Status)
 * Kebutuhan Habitat:
 * 1. Air: Sesuai [v]
 * 2. Suhu: Sesuai [v]
 * 3. Tanah Alami: Sesuai [v]
 * 4. Pasangan: Ada [v]
 * Biome: ... [v]
 */
public class ZooAnimalHologramRenderer {

    public static LivingEntity currentTargetedAnimal = null;

    public record HabitatParsedInfo(
            String status,
            String eco,
            boolean biomeOk,
            String waterStatus, // "OK", "NO", "NA"
            String tempStatus,  // "OK", "HEATER", "COOLER"
            boolean groundOk,
            boolean mateOk
    ) {
        public static HabitatParsedInfo parse(String raw, String defaultEco) {
            String fallbackEco = (defaultEco != null && !defaultEco.isEmpty() && !"-".equals(defaultEco)) ? defaultEco : "Savana";
            if (raw == null || !raw.contains("status=")) {
                return new HabitatParsedInfo("SUITABLE", fallbackEco, true, "OK", "OK", true, true);
            }
            String status = "SUITABLE";
            String eco = fallbackEco;
            boolean biomeOk = true;
            String water = "NA";
            String temp = "OK";
            boolean ground = true;
            boolean mate = true;

            for (String part : raw.split(";")) {
                String[] kv = part.split("=", 2);
                if (kv.length != 2) continue;
                String k = kv[0].trim();
                String v = kv[1].trim();
                switch (k) {
                    case "status" -> status = v;
                    case "eco" -> eco = v;
                    case "biome" -> biomeOk = "OK".equalsIgnoreCase(v);
                    case "water" -> water = v.toUpperCase(Locale.ROOT);
                    case "temp" -> temp = v.toUpperCase(Locale.ROOT);
                    case "ground" -> ground = "OK".equalsIgnoreCase(v);
                    case "mate" -> mate = "OK".equalsIgnoreCase(v);
                }
            }
            return new HabitatParsedInfo(status, eco, biomeOk, water, temp, ground, mate);
        }
    }

    public static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            currentTargetedAnimal = null;
            return;
        }

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
        float scale = 0.0125f;
        poseStack.scale(scale, -scale, scale);

        // 1. Entity Health
        float currentHp = entity.getHealth();
        float maxHp = entity.getMaxHealth();
        int hpPct = Math.round((currentHp / Math.max(1.0f, maxHp)) * 100.0f);
        String hpText = hpPct + "%";
        int hpTextColor = HologramBarRenderer.getStatusColor((float) hpPct);

        // 2. Snapshot data from server
        InspectionClientCache.Snapshot snapshot = InspectionClientCache.getSnapshot(entity.getId());
        boolean isRegistered = snapshot != null && snapshot.registered();

        String hungerText;
        int hungerTextColor;
        if (isRegistered && snapshot.hunger() >= 0) {
            int hVal = snapshot.hunger();
            hungerText = hVal + "%";
            hungerTextColor = HologramBarRenderer.getStatusColor((float) hVal);
        } else {
            hungerText = "-";
            hungerTextColor = HologramBarRenderer.COLOR_MUTED;
        }

        String thirstText;
        int thirstTextColor;
        if (isRegistered && snapshot.thirst() >= 0) {
            int tVal = snapshot.thirst();
            thirstText = tVal + "%";
            thirstTextColor = HologramBarRenderer.getStatusColor((float) tVal);
        } else {
            thirstText = "-";
            thirstTextColor = HologramBarRenderer.COLOR_MUTED;
        }

        String cleanText;
        int cleanTextColor;
        boolean isWet = isRegistered && InspectionPayloads.isAnimalWet(entity.getId());
        if (isRegistered && snapshot.cleanliness() >= 0) {
            int cVal = snapshot.cleanliness();
            cleanText = cVal + "%" + (isWet ? " " + Component.translatable("zootycoon.inspect.cleanliness.wet").getString() : "");
            cleanTextColor = HologramBarRenderer.getStatusColor((float) cVal);
        } else {
            cleanText = "-";
            cleanTextColor = HologramBarRenderer.COLOR_MUTED;
        }

        // 3. Climate & Temperature
        float effectiveTemp = ZooClimate.getEffectiveTemperature(entity);
        int celsius = ZooClimate.toCelsius(effectiveTemp);

        // 4. Habitat parsed data
        InspectionPayloads.HabitatData habitatData = InspectionPayloads.getLatestHabitat(entity.getId());
        HabitatParsedInfo habitatInfo = HabitatParsedInfo.parse(
                habitatData != null ? habitatData.needs() : null,
                habitatData != null ? habitatData.status() : null
        );

        String tempStatusText;
        int tempTextColor;
        if ("HEATER".equalsIgnoreCase(habitatInfo.tempStatus())) {
            tempStatusText = Component.translatable("zootycoon.inspect.temp.need_heater").getString();
            tempTextColor = 0xFF55FFFF;
        } else if ("COOLER".equalsIgnoreCase(habitatInfo.tempStatus())) {
            tempStatusText = Component.translatable("zootycoon.inspect.temp.need_cooler").getString();
            tempTextColor = 0xFFFFAA00;
        } else {
            tempStatusText = Component.translatable("zootycoon.inspect.temp.comfortable").getString();
            tempTextColor = 0xFFFFFFFF;
        }
        String tempValueText = celsius + "°C (" + tempStatusText + ")";

        // 5. Stat Components
        Component hpLabel = Component.translatable("zootycoon.inspect.stat.health");
        Component hungerLabel = Component.translatable("zootycoon.inspect.stat.hunger");
        Component thirstLabel = Component.translatable("zootycoon.inspect.stat.thirst");
        Component cleanLabel = Component.translatable("zootycoon.inspect.stat.cleanliness");
        Component tempLabel = Component.translatable("zootycoon.inspect.stat.temperature");

        int maxLabelWidth = Math.max(font.width(hpLabel),
                Math.max(font.width(hungerLabel),
                Math.max(font.width(thirstLabel),
                Math.max(font.width(cleanLabel), font.width(tempLabel)))));

        int maxValueWidth = Math.max(font.width(hpText),
                Math.max(font.width(hungerText),
                Math.max(font.width(thirstText),
                Math.max(font.width(cleanText), font.width(tempValueText)))));

        // 6. Food items (icons only)
        List<ItemStack> foods = AnimalDietRegistry.getFoodsForAnimal(entity);
        int foodCount = Math.min(5, foods.size());
        float foodSpacing = 12.0f;
        float foodsRowWidth = foodCount * foodSpacing;

        // 7. Habitat checklist items
        Component habitatHeader = Component.translatable("zootycoon.inspect.habitat_needs_header");

        Component waterCheckComp;
        int waterCheckColor;
        if ("OK".equalsIgnoreCase(habitatInfo.waterStatus())) {
            waterCheckComp = Component.translatable("zootycoon.inspect.checklist.water.ok");
            waterCheckColor = 0xFF55FF55;
        } else if ("NO".equalsIgnoreCase(habitatInfo.waterStatus())) {
            waterCheckComp = Component.translatable("zootycoon.inspect.checklist.water.fail");
            waterCheckColor = 0xFFFF5555;
        } else {
            waterCheckComp = Component.translatable("zootycoon.inspect.checklist.water.na");
            waterCheckColor = 0xFFAAAAAA;
        }

        Component tempCheckComp;
        int tempCheckColor;
        if ("OK".equalsIgnoreCase(habitatInfo.tempStatus())) {
            tempCheckComp = Component.translatable("zootycoon.inspect.checklist.temp.ok");
            tempCheckColor = 0xFF55FF55;
        } else if ("HEATER".equalsIgnoreCase(habitatInfo.tempStatus())) {
            tempCheckComp = Component.translatable("zootycoon.inspect.checklist.temp.heater");
            tempCheckColor = 0xFFFF5555;
        } else {
            tempCheckComp = Component.translatable("zootycoon.inspect.checklist.temp.cooler");
            tempCheckColor = 0xFFFF5555;
        }

        Component groundCheckComp = habitatInfo.groundOk()
                ? Component.translatable("zootycoon.inspect.checklist.ground.ok")
                : Component.translatable("zootycoon.inspect.checklist.ground.fail");
        int groundCheckColor = habitatInfo.groundOk() ? 0xFF55FF55 : 0xFFFF5555;

        Component mateCheckComp = habitatInfo.mateOk()
                ? Component.translatable("zootycoon.inspect.checklist.mate.ok")
                : Component.translatable("zootycoon.inspect.checklist.mate.fail");
        int mateCheckColor = habitatInfo.mateOk() ? 0xFF55FF55 : 0xFFFFAA00;

        Component biomeLineComp = habitatInfo.biomeOk()
                ? Component.translatable("zootycoon.inspect.biome.ok", habitatInfo.eco())
                : Component.translatable("zootycoon.inspect.biome.fail", habitatInfo.eco());
        int biomeLineColor = habitatInfo.biomeOk() ? 0xFF55FF55 : 0xFFFF5555;

        // Calculate card layout geometry
        Component titleComp = entity.getDisplayName();
        int titleWidth = font.width(titleComp);

        float iconWidth = 9.0f;
        float gapLabelToIcon = 4.0f;
        float gapIconToValue = 4.0f;
        float statRowWidth = maxLabelWidth + gapLabelToIcon + iconWidth + gapIconToValue + maxValueWidth;

        float maxHabitatTextWidth = Math.max(font.width(habitatHeader),
                Math.max(font.width(waterCheckComp),
                Math.max(font.width(tempCheckComp),
                Math.max(font.width(groundCheckComp),
                Math.max(font.width(mateCheckComp), font.width(biomeLineComp))))));

        float boxWidth = Math.max(statRowWidth + 16.0f,
                Math.max(titleWidth + 18.0f,
                Math.max(foodsRowWidth + 16.0f, maxHabitatTextWidth + 18.0f)));

        float rowHeight = 11.0f;
        float headerHeight = 11.0f;
        float foodSectionHeight = (foodCount > 0) ? 14.0f : 0.0f;
        float statsSectionHeight = 5.0f * rowHeight;
        float habitatHeaderHeight = 11.0f;
        float checklistHeight = 4.0f * 10.0f;
        float biomeHeight = 10.0f;

        float contentHeight = headerHeight + foodSectionHeight + statsSectionHeight + 4.0f + habitatHeaderHeight + checklistHeight + 2.0f + biomeHeight;
        float totalBoxHeight = contentHeight + 10.0f; // 5px padding top & bottom

        float minX = -boxWidth / 2.0f;
        float maxX = boxWidth / 2.0f;
        float maxY = 0.0f;
        float startY = maxY - totalBoxHeight;

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource buffer = context.consumers();

        if (buffer != null) {
            // 1. Draw rounded dark slate background plate (0xAA0B0E14)
            HologramBarRenderer.drawBackgroundPlate(buffer, matrix, minX, startY, maxX, maxY, LightTexture.FULL_BRIGHT);

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch(RenderType.textBackground());
            }

            float curY = startY + 5.0f;

            // 2. Animal Title
            float titleX = -titleWidth / 2.0f;
            font.drawInBatch(titleComp, titleX, curY, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            curY += headerHeight;

            // 3. Favorite Food Icons (Icons only, no text labels)
            if (foodCount > 0) {
                float foodStartX = -foodsRowWidth / 2.0f + (foodSpacing / 2.0f);
                for (int i = 0; i < foodCount; i++) {
                    ItemStack foodStack = foods.get(i);
                    poseStack.pushPose();
                    poseStack.translate(foodStartX + (i * foodSpacing), curY + 5.0f, -0.005f);
                    poseStack.scale(8.0f, -8.0f, 8.0f);
                    mc.getItemRenderer().renderStatic(
                            foodStack,
                            ItemDisplayContext.GUI,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            buffer,
                            mc.level,
                            0
                    );
                    poseStack.popPose();
                }
                curY += foodSectionHeight;
            }

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }

            // 4. Stat rows layout
            float statStartX = -statRowWidth / 2.0f;
            float labelX = statStartX;
            float iconX = labelX + maxLabelWidth + gapLabelToIcon;
            float valueX = iconX + iconWidth + gapIconToValue;

            float r1Y = curY;
            float r2Y = curY + rowHeight;
            float r3Y = curY + (rowHeight * 2.0f);
            float r4Y = curY + (rowHeight * 3.0f);
            float r5Y = curY + (rowHeight * 4.0f);
            curY += (rowHeight * 5.0f) + 4.0f;

            // Render Stat Icons
            HologramBarRenderer.drawVanillaHeartIcon(mc, font, buffer, matrix, iconX, r1Y + 1.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawVanillaHungerIcon(mc, buffer, matrix, iconX, r2Y + 1.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawTexturedIcon(buffer, matrix, HologramBarRenderer.THIRST_ICON, iconX, r3Y + 1.0f, 9.0f, 9.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawTexturedIcon(buffer, matrix, HologramBarRenderer.CLEANLINESS_ICON, iconX, r4Y + 1.0f, 9.0f, 9.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawThermometerIcon(buffer, matrix, iconX, r5Y + 1.0f, LightTexture.FULL_BRIGHT);

            // Render Stat Labels
            font.drawInBatch(hpLabel, labelX, r1Y + 1.0f, 0xFFDDDDDD, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(hungerLabel, labelX, r2Y + 1.0f, 0xFFDDDDDD, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(thirstLabel, labelX, r3Y + 1.0f, 0xFFDDDDDD, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(cleanLabel, labelX, r4Y + 1.0f, 0xFFDDDDDD, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(tempLabel, labelX, r5Y + 1.0f, 0xFFDDDDDD, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            // Render Stat Values
            font.drawInBatch(hpText, valueX, r1Y + 1.0f, hpTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(hungerText, valueX, r2Y + 1.0f, hungerTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(thirstText, valueX, r3Y + 1.0f, thirstTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(cleanText, valueX, r4Y + 1.0f, cleanTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(tempValueText, valueX, r5Y + 1.0f, tempTextColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            // 5. Habitat Needs Section
            float checklistStartX = minX + 8.0f;

            font.drawInBatch(habitatHeader, checklistStartX, curY, 0xFFFFFF55, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            curY += habitatHeaderHeight;

            font.drawInBatch(waterCheckComp, checklistStartX + 4.0f, curY, waterCheckColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            curY += 10.0f;

            font.drawInBatch(tempCheckComp, checklistStartX + 4.0f, curY, tempCheckColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            curY += 10.0f;

            font.drawInBatch(groundCheckComp, checklistStartX + 4.0f, curY, groundCheckColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            curY += 10.0f;

            font.drawInBatch(mateCheckComp, checklistStartX + 4.0f, curY, mateCheckColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            curY += 12.0f;

            font.drawInBatch(biomeLineComp, checklistStartX, curY, biomeLineColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
        }

        poseStack.popPose();
    }
}
