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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.animal.ZooClimate;
import net.zootycoon.animal.habitat.HabitatProfile;
import net.zootycoon.animal.habitat.HabitatProfileLoader;
import net.zootycoon.client.inspect.HologramBarRenderer;
import net.zootycoon.client.inspect.InspectionClientCache;
import net.zootycoon.client.inspect.InspectionTargetTracker;
import net.zootycoon.network.InspectionPayloads;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * World-space in-world billboarded inspection hologram renderer.
 * Formatted with authentic vanilla tooltip background, clean stat icons,
 * Indonesian stat labels, English biome list, and red-only habitat unmet needs.
 */
public class ZooAnimalHologramRenderer {

    public static LivingEntity currentTargetedAnimal = null;
    private static float lockProgress = 0.0f;
    private static LivingEntity lockedAnimal = null;
    private static long lastFrameTimeNano = 0L;

    public static float smoothEase(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    public record HabitatParsedInfo(
            String status,
            String eco,
            boolean biomeOk,
            String waterStatus, // "OK", "NO", "NA"
            String tempStatus,  // "OK", "HEATER", "COOLER"
            boolean groundOk,
            boolean mateOk,
            List<String> serverUnmet
    ) {
        public static HabitatParsedInfo parse(String raw, String defaultEco) {
            String fallbackEco = (defaultEco != null && !defaultEco.isEmpty() && !"-".equals(defaultEco)) ? defaultEco : "Savanna";
            if (raw == null || !raw.contains("status=")) {
                return new HabitatParsedInfo("SUITABLE", fallbackEco, true, "OK", "OK", true, true, Collections.emptyList());
            }
            String status = "SUITABLE";
            String eco = fallbackEco;
            boolean biomeOk = true;
            String water = "NA";
            String temp = "OK";
            boolean ground = true;
            boolean mate = true;
            List<String> serverUnmet = new ArrayList<>();

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
                    case "mate" -> mate = !"NO".equalsIgnoreCase(v);
                    case "unmet" -> {
                        if (!v.isEmpty()) {
                            for (String item : v.split("\\|")) {
                                String trimmed = item.trim();
                                if (!trimmed.isEmpty()) {
                                    serverUnmet.add(trimmed);
                                }
                            }
                        }
                    }
                }
            }
            return new HabitatParsedInfo(status, eco, biomeOk, water, temp, ground, mate, serverUnmet);
        }
    }

    public static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            currentTargetedAnimal = null;
            lockedAnimal = null;
            lockProgress = 0.0f;
            lastFrameTimeNano = 0L;
            return;
        }

        long now = System.nanoTime();
        float dt = (lastFrameTimeNano == 0L) ? 0.016f : (float) ((now - lastFrameTimeNano) / 1_000_000_000.0);
        lastFrameTimeNano = now;
        dt = Math.min(dt, 0.05f);

        net.minecraft.world.entity.player.Player player = mc.player;
        boolean holdingMagnifier = player != null && (player.getMainHandItem().is(net.zootycoon.item.ZooItems.MAGNIFYING_GLASS)
                || player.getOffhandItem().is(net.zootycoon.item.ZooItems.MAGNIFYING_GLASS));
        boolean isRightClickHeld = holdingMagnifier && player.isUsingItem() && player.getUseItem().is(net.zootycoon.item.ZooItems.MAGNIFYING_GLASS);

        InspectionTargetTracker.tick(mc);

        LivingEntity activeTarget = InspectionTargetTracker.getTarget();

        // Lock onto active target when right click is pressed
        if (isRightClickHeld) {
            if (lockedAnimal == null && activeTarget != null && activeTarget.isAlive() && InspectionTargetTracker.isZooRegisteredAnimal(activeTarget)) {
                lockedAnimal = activeTarget;
            }
        } else {
            // When released and animation finished easing out, clear lockedAnimal
            if (lockProgress <= 0.001f) {
                lockedAnimal = null;
            }
        }

        // Entity to render: locked animal if still active, otherwise currently hovered target
        LivingEntity entityToRender = (lockedAnimal != null && lockedAnimal.isAlive()) ? lockedAnimal : activeTarget;
        currentTargetedAnimal = entityToRender;

        boolean shouldDisplay = entityToRender != null && entityToRender.isAlive() && InspectionTargetTracker.isZooRegisteredAnimal(entityToRender);

        // Target lock progress: 1.0f when locked in center, 0.0f when free in-world
        float targetProgress = (isRightClickHeld && shouldDisplay) ? 1.0f : 0.0f;

        // Smooth ease transition rate (~0.25 seconds for full ease)
        float transitionSpeed = 4.0f;
        if (lockProgress < targetProgress) {
            lockProgress = Math.min(targetProgress, lockProgress + dt * transitionSpeed);
        } else if (lockProgress > targetProgress) {
            lockProgress = Math.max(targetProgress, lockProgress - dt * transitionSpeed);
        }

        if (!shouldDisplay && lockProgress <= 0.001f) {
            return;
        }

        renderHologram(context, mc, entityToRender, lockProgress);
    }

    private static void renderHologram(WorldRenderContext context, Minecraft mc, LivingEntity entity, float lockProgress) {
        PoseStack poseStack = context.matrixStack();
        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        Font font = mc.font;

        // 1. Natural eye-level vertical positioning (fixes issue #1: hologram previously too high)
        double eyeHeight = entity.getEyeHeight();
        double yOffset = Math.max(0.65, eyeHeight + 0.15);
        if (entity.hasCustomName()) {
            yOffset += 0.25;
        }
        double entX = entity.getX() - camPos.x;
        double entY = entity.getY() + yOffset - camPos.y;
        double entZ = entity.getZ() - camPos.z;

        // Offset forward towards camera so large bodies, horns, ears, snouts don't clip the hologram
        double dx = camPos.x - entity.getX();
        double dz = camPos.z - entity.getZ();
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        if (distXZ > 0.001) {
            double forwardOffset = Math.min(distXZ * 0.45, Math.max(0.35, entity.getBbWidth() * 0.55));
            entX += (dx / distXZ) * forwardOffset;
            entZ += (dz / distXZ) * forwardOffset;
        }

        // Convert camera-relative world coordinates into camera-local space
        Quaternionf camRot = camera.rotation();
        Quaternionf invRot = new Quaternionf(camRot).conjugate();
        Vector3f localPos = new Vector3f((float) entX, (float) entY, (float) entZ);
        invRot.transform(localPos);

        // Clamp localPos.z in front of camera so it never clips behind view during fast turns
        if (localPos.z > -0.8f) {
            localPos.z = -0.8f;
        }

        float eased = smoothEase(lockProgress);

        // Smoothly interpolate between in-world mob position and screen-center locked position (0, 0, -1.35)
        float transX = localPos.x + (0.0f - localPos.x) * eased;
        float transY = localPos.y + (0.0f - localPos.y) * eased;
        float transZ = localPos.z + (-1.35f - localPos.z) * eased;

        // Smoothly interpolate scale: world scale (0.0110f) -> screen locked scale (0.0068f)
        float curScale = 0.0110f + (0.0068f - 0.0110f) * eased;

        poseStack.pushPose();
        poseStack.mulPose(camera.rotation());
        poseStack.translate(transX, transY, transZ);
        poseStack.scale(curScale, -curScale, curScale);

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
            cleanText = cVal + "%" + (isWet ? " (Basah)" : "");
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

        // Temperature status formatting (Green if comfortable, Red if needs heater/cooler)
        String tempStatusLabel;
        int tempStatusColor;
        if ("HEATER".equalsIgnoreCase(habitatInfo.tempStatus())) {
            tempStatusLabel = " (Butuh Heater)";
            tempStatusColor = 0xFFFF5555; // Red
        } else if ("COOLER".equalsIgnoreCase(habitatInfo.tempStatus())) {
            tempStatusLabel = " (Butuh Cooler Box)";
            tempStatusColor = 0xFFFF5555; // Red
        } else {
            tempStatusLabel = " (Nyaman)";
            tempStatusColor = 0xFF55FF55; // Green
        }
        String tempBaseText = celsius + "°C";

        // 5. Stat Labels (Indonesian)
        Component hpLabel = Component.literal("Kesehatan:");
        Component hungerLabel = Component.literal("Lapar:");
        Component thirstLabel = Component.literal("Haus:");
        Component cleanLabel = Component.literal("Kebersihan:");
        Component tempLabel = Component.literal("Suhu:");

        int maxLabelWidth = Math.max(font.width(hpLabel),
                Math.max(font.width(hungerLabel),
                Math.max(font.width(thirstLabel),
                Math.max(font.width(cleanLabel), font.width(tempLabel)))));

        int tempTotalWidth = font.width(tempBaseText) + font.width(tempStatusLabel);
        int maxValueWidth = Math.max(font.width(hpText),
                Math.max(font.width(hungerText),
                Math.max(font.width(thirstText),
                Math.max(font.width(cleanText), tempTotalWidth))));

        // 6. Food items (icons only)
        List<ItemStack> foods = AnimalDietRegistry.getFoodsForAnimal(entity);
        int foodCount = Math.min(5, foods.size());
        float foodSpacing = 16.0f;
        float foodsRowWidth = foodCount * foodSpacing;

        // 7. Habitat Needs Checklist: ONLY RED / UNMET NEEDS
        HabitatProfile profile = HabitatProfileLoader.getProfile(entity);
        List<Component> redNeeds = new ArrayList<>();

        if (!habitatInfo.serverUnmet().isEmpty()) {
            for (String item : habitatInfo.serverUnmet()) {
                if (item.startsWith("Bioma:")) {
                    continue; // Biome is handled below with English bullet points
                }
                if (item.startsWith("Butuh Heater") || item.startsWith("Butuh Cooler Box")) {
                    continue; // Temperature status is displayed in Suhu row
                }
                redNeeds.add(Component.literal("• " + item));
            }
        } else {
            if ("NO".equalsIgnoreCase(habitatInfo.waterStatus())) {
                redNeeds.add(Component.literal("• Butuh Air"));
            }
            if (!habitatInfo.groundOk()) {
                redNeeds.add(Component.literal("• Butuh Tanah Alami"));
            }
            if (!habitatInfo.mateOk()) {
                redNeeds.add(Component.literal("• Butuh Pasangan"));
            }
        }

        if (!habitatInfo.biomeOk()) {
            List<String> biomes = getBiomeListEnglish(profile);
            biomes.removeIf(b -> "Any".equalsIgnoreCase(b) || b.isBlank());
            if (!biomes.isEmpty()) {
                redNeeds.add(Component.literal("• Bioma yang cocok:"));
                for (String b : biomes) {
                    redNeeds.add(Component.literal("  - " + b));
                }
            }
        }

        // Layout measurements
        Component titleComp = entity.getDisplayName();
        float titleScale = 1.25f;
        float scaledTitleWidth = font.width(titleComp) * titleScale;

        float iconWidth = 9.0f;
        float gapLabelToIcon = 4.0f;
        float gapIconToValue = 4.0f;
        float statRowWidth = maxLabelWidth + gapLabelToIcon + iconWidth + gapIconToValue + maxValueWidth;

        float maxHabitatTextWidth = 0.0f;
        if (redNeeds.isEmpty()) {
            maxHabitatTextWidth = font.width("Kebutuhan Habitat: Terpenuhi");
        } else {
            maxHabitatTextWidth = font.width("Kebutuhan Habitat:");
            for (Component c : redNeeds) {
                maxHabitatTextWidth = Math.max(maxHabitatTextWidth, font.width(c));
            }
        }

        float boxWidth = Math.max(statRowWidth + 16.0f,
                Math.max(scaledTitleWidth + 16.0f,
                Math.max(foodsRowWidth + 16.0f, maxHabitatTextWidth + 18.0f)));

        float rowHeight = 11.0f;
        float headerHeight = 13.0f;
        float foodSectionHeight = (foodCount > 0) ? 18.0f : 0.0f;
        float statsSectionHeight = 5.0f * rowHeight;
        float habitatSectionHeight = redNeeds.isEmpty() ? 12.0f : (12.0f + (redNeeds.size() * 10.0f));

        float contentHeight = headerHeight + foodSectionHeight + statsSectionHeight + 4.0f + habitatSectionHeight;
        float totalBoxHeight = contentHeight + 10.0f; // 5px padding top & bottom

        float minX = -boxWidth / 2.0f;
        float maxX = boxWidth / 2.0f;
        float halfH = totalBoxHeight / 2.0f;
        float startY = -halfH;
        float maxY = halfH;

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource buffer = context.consumers();

        if (buffer != null) {
            // 1. Draw authentic vanilla tooltip background
            HologramBarRenderer.drawVanillaTooltipBackground(buffer, matrix, minX, startY, maxX, maxY, LightTexture.FULL_BRIGHT);

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch(RenderType.textBackground());
            }

            float leftX = minX + 8.0f;
            float curY = startY + 5.0f;

            // 2. Animal Title (Enlarged & Left-aligned)
            poseStack.pushPose();
            poseStack.translate(leftX, curY, 0.0f);
            poseStack.scale(titleScale, titleScale, 1.0f);
            font.drawInBatch(titleComp, 0.0f, 0.0f, 0xFFFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
            curY += headerHeight;

            // 3. Favorite Food Icons (Enlarged & Left-aligned)
            if (foodCount > 0) {
                float foodStartX = leftX + (foodSpacing / 2.0f);
                for (int i = 0; i < foodCount; i++) {
                    ItemStack foodStack = foods.get(i);
                    poseStack.pushPose();
                    poseStack.translate(foodStartX + (i * foodSpacing), curY + 6.0f, -0.005f);
                    poseStack.scale(12.0f, -12.0f, 12.0f);
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

            // 4. Stat rows layout (Left-aligned)
            float labelX = leftX;
            float iconX = labelX + maxLabelWidth + gapLabelToIcon;
            float valueX = iconX + iconWidth + gapIconToValue;

            float r1Y = curY;
            float r2Y = curY + rowHeight;
            float r3Y = curY + (rowHeight * 2.0f);
            float r4Y = curY + (rowHeight * 3.0f);
            float r5Y = curY + (rowHeight * 4.0f);
            curY += (rowHeight * 5.0f) + 4.0f;

            // Render Stat Icons (Two-sided & depth-tested)
            HologramBarRenderer.drawVanillaHeartIcon(mc, font, buffer, matrix, iconX, r1Y + 1.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawVanillaHungerIcon(mc, buffer, matrix, iconX, r2Y + 1.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawThirstIcon(buffer, matrix, iconX, r3Y + 1.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawCleanlinessIcon(buffer, matrix, iconX, r4Y + 1.0f, LightTexture.FULL_BRIGHT);
            HologramBarRenderer.drawThermometerIcon(buffer, matrix, iconX, r5Y + 1.0f, LightTexture.FULL_BRIGHT);

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }

            // Render Stat Labels (Indonesian, Left-aligned)
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

            // Temperature Value: Celsius (White) + Status (Green if comfortable, Red if needs heater/cooler)
            font.drawInBatch(tempBaseText, valueX, r5Y + 1.0f, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            float statusX = valueX + font.width(tempBaseText);
            font.drawInBatch(tempStatusLabel, statusX, r5Y + 1.0f, tempStatusColor, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            // 5. Habitat Needs Section: ONLY RED ITEMS SHOWN (Left-aligned)
            float checklistStartX = leftX;
            if (redNeeds.isEmpty()) {
                // All needs satisfied: single green line
                font.drawInBatch("Kebutuhan Habitat: Terpenuhi", checklistStartX, curY, 0xFF55FF55, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            } else {
                // Some needs missing: yellow header and red itemized checklist
                font.drawInBatch("Kebutuhan Habitat:", checklistStartX, curY, 0xFFFFFF55, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                curY += 11.0f;
                for (Component need : redNeeds) {
                    font.drawInBatch(need, checklistStartX + 4.0f, curY, 0xFFFF5555, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                    curY += 10.0f;
                }
            }

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
        }

        poseStack.popPose();
    }

    public static List<String> getBiomeListEnglish(HabitatProfile profile) {
        List<String> names = new ArrayList<>();
        if (profile == null) {
            return names;
        }
        if (profile.biomeIds() != null && !profile.biomeIds().isEmpty()) {
            for (ResourceLocation loc : profile.biomeIds()) {
                String title = toEnglishBiomeName(loc.getPath());
                if (!names.contains(title)) {
                    names.add(title);
                }
            }
        }
        if (names.isEmpty() && profile.name() != null && !profile.name().equalsIgnoreCase("any")) {
            names.add(toEnglishBiomeName(profile.name()));
        }
        return names;
    }

    private static String toEnglishBiomeName(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String[] parts = raw.replace('_', ' ').split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
