package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World-space in-world billboarded inspection hologram renderer.
 * Formatted with authentic vanilla tooltip background, clean stat icons,
 * Indonesian stat labels, English biome list, and red-only habitat unmet needs.
 */
public class ZooAnimalHologramRenderer {

    private static final Component HP_LABEL = Component.literal("Kesehatan:");
    private static final Component HUNGER_LABEL = Component.literal("Lapar:");
    private static final Component THIRST_LABEL = Component.literal("Haus:");
    private static final Component CLEAN_LABEL = Component.literal("Kebersihan:");
    private static final Component TEMP_LABEL = Component.literal("Suhu:");

    private static final String HABITAT_OK_TEXT = "Kebutuhan Habitat: Terpenuhi";
    private static final String HABITAT_HEADER_TEXT = "Kebutuhan Habitat:";

    private static final float TITLE_SCALE = 1.25f;
    private static final float ROW_HEIGHT = 11.0f;
    private static final float HEADER_HEIGHT = 13.0f;
    private static final float FOOD_SPACING = 16.0f;
    private static final float FOOD_SIZE = 12.0f;
    private static final float ICON_WIDTH = 9.0f;
    private static final float GAP_LABEL_TO_ICON = 4.0f;
    private static final float GAP_ICON_TO_VALUE = 4.0f;

    public static class CachedSnapshotRenderData {
        public final int entityId;
        public final long snapshotTimestamp;
        public final boolean registered;
        public final int hpPct;
        public final int hunger;
        public final int thirst;
        public final int cleanliness;
        public final boolean isWet;
        public final String rawNeeds;
        public final String rawStatus;
        public final String tempCategory;

        public final Component titleComp;
        public final float scaledTitleWidth;

        public final String hpText;
        public final int hpTextColor;
        public final int hpTextWidth;

        public final String hungerText;
        public final int hungerTextColor;
        public final int hungerTextWidth;

        public final String thirstText;
        public final int thirstTextColor;
        public final int thirstTextWidth;

        public final String cleanText;
        public final int cleanTextColor;
        public final int cleanTextWidth;

        public final HabitatParsedInfo parsedInfo;
        public final int celsius;
        public final String tempBaseText;
        public final int tempBaseTextWidth;
        public final String tempStatusLabel;
        public final int tempStatusLabelWidth;
        public final int tempStatusColor;
        public final int tempTotalWidth;

        public final int maxLabelWidth;
        public final int maxValueWidth;
        public final float statRowWidth;

        public final List<ItemStack> foods;
        public final int foodCount;
        public final float foodsRowWidth;

        public final List<Component> redNeeds;
        public final float maxHabitatTextWidth;

        public final float headerHeight;
        public final float foodSectionHeight;
        public final float statsSectionHeight;
        public final float habitatSectionHeight;
        public final float contentHeight;
        public final float totalBoxHeight;
        public final float boxWidth;
        public final float minX;
        public final float maxX;
        public final float startY;
        public final float maxY;

        public CachedSnapshotRenderData(
                int entityId,
                long snapshotTimestamp,
                boolean registered,
                int hpPct,
                int hunger,
                int thirst,
                int cleanliness,
                boolean isWet,
                String rawNeeds,
                String rawStatus,
                String tempCategory,
                Component titleComp,
                float scaledTitleWidth,
                String hpText,
                int hpTextColor,
                int hpTextWidth,
                String hungerText,
                int hungerTextColor,
                int hungerTextWidth,
                String thirstText,
                int thirstTextColor,
                int thirstTextWidth,
                String cleanText,
                int cleanTextColor,
                int cleanTextWidth,
                HabitatParsedInfo parsedInfo,
                int celsius,
                String tempBaseText,
                int tempBaseTextWidth,
                String tempStatusLabel,
                int tempStatusLabelWidth,
                int tempStatusColor,
                int tempTotalWidth,
                int maxLabelWidth,
                int maxValueWidth,
                float statRowWidth,
                List<ItemStack> foods,
                int foodCount,
                float foodsRowWidth,
                List<Component> redNeeds,
                float maxHabitatTextWidth,
                float headerHeight,
                float foodSectionHeight,
                float statsSectionHeight,
                float habitatSectionHeight,
                float contentHeight,
                float totalBoxHeight,
                float boxWidth,
                float minX,
                float maxX,
                float startY,
                float maxY
        ) {
            this.entityId = entityId;
            this.snapshotTimestamp = snapshotTimestamp;
            this.registered = registered;
            this.hpPct = hpPct;
            this.hunger = hunger;
            this.thirst = thirst;
            this.cleanliness = cleanliness;
            this.isWet = isWet;
            this.rawNeeds = rawNeeds;
            this.rawStatus = rawStatus;
            this.tempCategory = tempCategory;
            this.titleComp = titleComp;
            this.scaledTitleWidth = scaledTitleWidth;
            this.hpText = hpText;
            this.hpTextColor = hpTextColor;
            this.hpTextWidth = hpTextWidth;
            this.hungerText = hungerText;
            this.hungerTextColor = hungerTextColor;
            this.hungerTextWidth = hungerTextWidth;
            this.thirstText = thirstText;
            this.thirstTextColor = thirstTextColor;
            this.thirstTextWidth = thirstTextWidth;
            this.cleanText = cleanText;
            this.cleanTextColor = cleanTextColor;
            this.cleanTextWidth = cleanTextWidth;
            this.parsedInfo = parsedInfo;
            this.celsius = celsius;
            this.tempBaseText = tempBaseText;
            this.tempBaseTextWidth = tempBaseTextWidth;
            this.tempStatusLabel = tempStatusLabel;
            this.tempStatusLabelWidth = tempStatusLabelWidth;
            this.tempStatusColor = tempStatusColor;
            this.tempTotalWidth = tempTotalWidth;
            this.maxLabelWidth = maxLabelWidth;
            this.maxValueWidth = maxValueWidth;
            this.statRowWidth = statRowWidth;
            this.foods = foods;
            this.foodCount = foodCount;
            this.foodsRowWidth = foodsRowWidth;
            this.redNeeds = redNeeds;
            this.maxHabitatTextWidth = maxHabitatTextWidth;
            this.headerHeight = headerHeight;
            this.foodSectionHeight = foodSectionHeight;
            this.statsSectionHeight = statsSectionHeight;
            this.habitatSectionHeight = habitatSectionHeight;
            this.contentHeight = contentHeight;
            this.totalBoxHeight = totalBoxHeight;
            this.boxWidth = boxWidth;
            this.minX = minX;
            this.maxX = maxX;
            this.startY = startY;
            this.maxY = maxY;
        }

        public static CachedSnapshotRenderData build(
                LivingEntity entity,
                Font font,
                int hpPct,
                long snapshotTimestamp,
                boolean isRegistered,
                int hunger,
                int thirst,
                int cleanliness,
                boolean isWet,
                String rawNeeds,
                String rawStatus,
                String tempCategory
        ) {
            HabitatParsedInfo habitatInfo = HabitatParsedInfo.parse(rawNeeds, rawStatus);
            int celsius = parseCelsiusFromCategory(tempCategory);
            String tempBaseText = celsius + "°C";

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

            int tempBaseTextWidth = font.width(tempBaseText);
            int tempStatusLabelWidth = font.width(tempStatusLabel);
            int tempTotalWidth = tempBaseTextWidth + tempStatusLabelWidth;

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
                boolean hasBiomes = false;
                for (String b : biomes) {
                    if (!"Any".equalsIgnoreCase(b) && !b.isBlank()) {
                        hasBiomes = true;
                        break;
                    }
                }
                if (hasBiomes) {
                    redNeeds.add(Component.literal("• Bioma yang cocok:"));
                    for (String b : biomes) {
                        if (!"Any".equalsIgnoreCase(b) && !b.isBlank()) {
                            redNeeds.add(Component.literal("  - " + b));
                        }
                    }
                }
            }

            float maxHabTextWidth;
            if (redNeeds.isEmpty()) {
                maxHabTextWidth = font.width(HABITAT_OK_TEXT);
            } else {
                maxHabTextWidth = font.width(HABITAT_HEADER_TEXT);
                for (Component c : redNeeds) {
                    maxHabTextWidth = Math.max(maxHabTextWidth, font.width(c));
                }
            }

            String hpText = hpPct + "%";
            int hpTextColor = HologramBarRenderer.getStatusColor((float) hpPct);
            int hpTextWidth = font.width(hpText);

            String hungerText;
            int hungerTextColor;
            if (isRegistered && hunger >= 0) {
                hungerText = hunger + "%";
                hungerTextColor = HologramBarRenderer.getStatusColor((float) hunger);
            } else {
                hungerText = "-";
                hungerTextColor = HologramBarRenderer.COLOR_MUTED;
            }
            int hungerTextWidth = font.width(hungerText);

            String thirstText;
            int thirstTextColor;
            if (isRegistered && thirst >= 0) {
                thirstText = thirst + "%";
                thirstTextColor = HologramBarRenderer.getStatusColor((float) thirst);
            } else {
                thirstText = "-";
                thirstTextColor = HologramBarRenderer.COLOR_MUTED;
            }
            int thirstTextWidth = font.width(thirstText);

            String cleanText;
            int cleanTextColor;
            if (isRegistered && cleanliness >= 0) {
                cleanText = cleanliness + "%" + (isWet ? " (Basah)" : "");
                cleanTextColor = HologramBarRenderer.getStatusColor((float) cleanliness);
            } else {
                cleanText = "-";
                cleanTextColor = HologramBarRenderer.COLOR_MUTED;
            }
            int cleanTextWidth = font.width(cleanText);

            int maxLabelWidth = Math.max(font.width(HP_LABEL),
                    Math.max(font.width(HUNGER_LABEL),
                    Math.max(font.width(THIRST_LABEL),
                    Math.max(font.width(CLEAN_LABEL), font.width(TEMP_LABEL)))));

            int maxValueWidth = Math.max(hpTextWidth,
                    Math.max(hungerTextWidth,
                    Math.max(thirstTextWidth,
                    Math.max(cleanTextWidth, tempTotalWidth))));

            List<ItemStack> allFoods = AnimalDietRegistry.getFoodsForAnimal(entity);
            int foodCount = Math.min(8, allFoods.size());
            List<ItemStack> foods = (foodCount > 0)
                    ? Collections.unmodifiableList(new ArrayList<>(allFoods.subList(0, foodCount)))
                    : Collections.emptyList();
            float foodsRowWidth = foodCount * FOOD_SPACING;

            Component titleComp = entity.getDisplayName();
            float scaledTitleWidth = font.width(titleComp) * TITLE_SCALE;

            float statRowWidth = maxLabelWidth + GAP_LABEL_TO_ICON + ICON_WIDTH + GAP_ICON_TO_VALUE + maxValueWidth;

            float boxWidth = Math.max(statRowWidth + 16.0f,
                    Math.max(scaledTitleWidth + 16.0f,
                    Math.max(foodsRowWidth + 16.0f, maxHabTextWidth + 18.0f)));

            float foodSectionHeight = (foodCount > 0) ? 16.0f : 0.0f;
            float statsSectionHeight = 5.0f * ROW_HEIGHT;
            float habitatSectionHeight = redNeeds.isEmpty() ? 12.0f : (12.0f + (redNeeds.size() * 10.0f));

            float contentHeight = HEADER_HEIGHT + foodSectionHeight + statsSectionHeight + 4.0f + habitatSectionHeight;
            float totalBoxHeight = contentHeight + 10.0f; // 5px padding top & bottom

            float minX = -boxWidth / 2.0f;
            float maxX = boxWidth / 2.0f;
            float startY = -totalBoxHeight;
            float maxY = 0.0f;

            return new CachedSnapshotRenderData(
                    entity.getId(),
                    snapshotTimestamp,
                    isRegistered,
                    hpPct,
                    hunger,
                    thirst,
                    cleanliness,
                    isWet,
                    rawNeeds,
                    rawStatus,
                    tempCategory,
                    titleComp,
                    scaledTitleWidth,
                    hpText,
                    hpTextColor,
                    hpTextWidth,
                    hungerText,
                    hungerTextColor,
                    hungerTextWidth,
                    thirstText,
                    thirstTextColor,
                    thirstTextWidth,
                    cleanText,
                    cleanTextColor,
                    cleanTextWidth,
                    habitatInfo,
                    celsius,
                    tempBaseText,
                    tempBaseTextWidth,
                    tempStatusLabel,
                    tempStatusLabelWidth,
                    tempStatusColor,
                    tempTotalWidth,
                    maxLabelWidth,
                    maxValueWidth,
                    statRowWidth,
                    foods,
                    foodCount,
                    foodsRowWidth,
                    Collections.unmodifiableList(redNeeds),
                    maxHabTextWidth,
                    HEADER_HEIGHT,
                    foodSectionHeight,
                    statsSectionHeight,
                    habitatSectionHeight,
                    contentHeight,
                    totalBoxHeight,
                    boxWidth,
                    minX,
                    maxX,
                    startY,
                    maxY
            );
        }
    }

    private static final Map<Integer, CachedSnapshotRenderData> SNAPSHOT_RENDER_CACHE = new ConcurrentHashMap<>();
    private static final Map<HabitatProfile, List<String>> BIOME_LIST_CACHE = new ConcurrentHashMap<>();

    public static LivingEntity currentTargetedAnimal = null;
    private static float lockProgress = 0.0f;
    private static LivingEntity lockedAnimal = null;
    private static float fadeAlpha = 0.0f;
    private static LivingEntity lastValidAnimal = null;
    private static long lastFrameTimeNano = 0L;

    public static void reset() {
        currentTargetedAnimal = null;
        lockedAnimal = null;
        lastValidAnimal = null;
        lockProgress = 0.0f;
        fadeAlpha = 0.0f;
        lastFrameTimeNano = 0L;
        SNAPSHOT_RENDER_CACHE.clear();
    }

    public static int parseCelsiusFromCategory(String tempCategory) {
        if (tempCategory == null || tempCategory.isEmpty()) return 20;
        try {
            float f = Float.parseFloat(tempCategory);
            if (f < 5.0f) {
                return ZooClimate.toCelsius(f);
            }
            return Math.round(f);
        } catch (NumberFormatException ignored) {}

        return switch (tempCategory.toLowerCase(Locale.ROOT)) {
            case "freezing" -> 3;
            case "cold" -> 11;
            case "warm" -> 30;
            case "hot" -> 40;
            default -> 20; // temperate
        };
    }

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
            reset();
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

        LivingEntity activeTarget = InspectionTargetTracker.getTarget();

        // Lock onto active target when right click is pressed
        if (isRightClickHeld) {
            if (lockedAnimal == null) {
                if (activeTarget != null && activeTarget.isAlive() && InspectionTargetTracker.isZooRegisteredAnimal(activeTarget)) {
                    lockedAnimal = activeTarget;
                } else if (mc.crosshairPickEntity instanceof LivingEntity picked && picked.isAlive() && InspectionTargetTracker.isZooRegisteredAnimal(picked)) {
                    lockedAnimal = picked;
                }
            }
        } else {
            // When released and animation finished easing out, clear lockedAnimal
            if (lockProgress <= 0.001f) {
                lockedAnimal = null;
            }
        }

        LivingEntity candidate = (lockedAnimal != null && lockedAnimal.isAlive()) ? lockedAnimal : activeTarget;
        boolean candidateValid = candidate != null && candidate.isAlive() && InspectionTargetTracker.isZooRegisteredAnimal(candidate);

        if (candidateValid) {
            lastValidAnimal = candidate;
        }

        // Target lock progress: 1.0f when locked in center, 0.0f when free in-world
        float targetProgress = (isRightClickHeld && candidateValid) ? 1.0f : 0.0f;

        // Smooth ease transition rate (~0.25 seconds for full ease)
        float transitionSpeed = 4.0f;
        if (lockProgress < targetProgress) {
            lockProgress = Math.min(targetProgress, lockProgress + dt * transitionSpeed);
        } else if (lockProgress > targetProgress) {
            lockProgress = Math.max(targetProgress, lockProgress - dt * transitionSpeed);
        }

        // Fade animation: 1.0f when inspecting, snappy fade-out (~0.18s) when lost
        float targetFade = candidateValid ? 1.0f : 0.0f;
        if (fadeAlpha < targetFade) {
            fadeAlpha = Math.min(targetFade, fadeAlpha + dt * 8.0f);
        } else if (fadeAlpha > targetFade) {
            fadeAlpha = Math.max(targetFade, fadeAlpha - dt * 5.5f);
        }

        if (fadeAlpha <= 0.001f && !candidateValid) {
            currentTargetedAnimal = null;
            lockedAnimal = null;
            lastValidAnimal = null;
            lockProgress = 0.0f;
            return;
        }

        LivingEntity entityToRender = candidateValid ? candidate : lastValidAnimal;
        if (entityToRender == null || !entityToRender.isAlive() || entityToRender.isRemoved()) {
            reset();
            return;
        }

        currentTargetedAnimal = candidateValid ? entityToRender : null;

        renderHologram(context, mc, entityToRender, lockProgress, fadeAlpha);
    }

    private static void renderHologram(WorldRenderContext context, Minecraft mc, LivingEntity entity, float lockProgress, float fadeAlpha) {
        PoseStack poseStack = context.matrixStack();
        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        Font font = mc.font;

        // 1. Natural vertical positioning anchored directly above mob's bounding box
        double eyeHeight = entity.getEyeHeight();
        double yOffset = Math.max(entity.getBbHeight() + 0.08, eyeHeight + 0.15);
        if (entity.hasCustomName()) {
            yOffset += 0.25;
        }
        double entX = entity.getX() - camPos.x;
        double entY = entity.getY() + yOffset - camPos.y;
        double entZ = entity.getZ() - camPos.z;

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

        // 1. Entity Health
        float currentHp = entity.getHealth();
        float maxHp = entity.getMaxHealth();
        int hpPct = Math.round((currentHp / Math.max(1.0f, maxHp)) * 100.0f);

        // 2. Snapshot data from server
        InspectionClientCache.Snapshot snapshot = InspectionClientCache.getSnapshot(entity.getId());
        boolean isRegistered = snapshot != null && snapshot.registered();
        int hunger = (snapshot != null) ? snapshot.hunger() : -1;
        int thirst = (snapshot != null) ? snapshot.thirst() : -1;
        int cleanliness = (snapshot != null) ? snapshot.cleanliness() : -1;
        long snapshotTimestamp = (snapshot != null) ? snapshot.timestamp() : -1L;
        boolean isWet = isRegistered && InspectionPayloads.isAnimalWet(entity.getId());

        // 3. Climate & Habitat cached snapshot data
        InspectionPayloads.HabitatData habitatData = InspectionPayloads.getLatestHabitat(entity.getId());
        InspectionPayloads.ClimateData climateData = InspectionPayloads.getLatestClimate(entity.getId());
        String rawNeeds = habitatData != null ? habitatData.needs() : "";
        String rawStatus = habitatData != null ? habitatData.status() : "";
        String tempCategory = climateData != null ? climateData.tempCategory() : "temperate";

        CachedSnapshotRenderData cached = SNAPSHOT_RENDER_CACHE.get(entity.getId());
        if (cached == null
                || cached.snapshotTimestamp != snapshotTimestamp
                || cached.registered != isRegistered
                || cached.hpPct != hpPct
                || cached.hunger != hunger
                || cached.thirst != thirst
                || cached.cleanliness != cleanliness
                || cached.isWet != isWet
                || !Objects.equals(cached.titleComp.getString(), entity.getDisplayName().getString())
                || !Objects.equals(cached.rawNeeds, rawNeeds)
                || !Objects.equals(cached.rawStatus, rawStatus)
                || !Objects.equals(cached.tempCategory, tempCategory)) {

            cached = CachedSnapshotRenderData.build(
                    entity,
                    font,
                    hpPct,
                    snapshotTimestamp,
                    isRegistered,
                    hunger,
                    thirst,
                    cleanliness,
                    isWet,
                    rawNeeds,
                    rawStatus,
                    tempCategory
            );
            SNAPSHOT_RENDER_CACHE.put(entity.getId(), cached);
        }

        // Smoothly interpolate scale: world scale (0.0110f) -> screen locked scale (0.0068f)
        float curScale = 0.0110f + (0.0068f - 0.0110f) * eased;
        float halfHWorld = (cached.totalBoxHeight / 2.0f) * curScale;
        float targetTransY = -halfHWorld; // Screen-center vertical alignment when locked

        // Smoothly interpolate between in-world mob position and screen-center locked position
        float transX = localPos.x + (0.0f - localPos.x) * eased;
        float transY = localPos.y + (targetTransY - localPos.y) * eased;
        float transZ = localPos.z + (-1.35f - localPos.z) * eased;

        poseStack.pushPose();
        poseStack.mulPose(camera.rotation());
        poseStack.translate(transX, transY, transZ);
        poseStack.scale(curScale, -curScale, curScale);

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource buffer = context.consumers();

        if (buffer != null) {
            // 1. Draw authentic vanilla tooltip background with fadeAlpha
            HologramBarRenderer.drawVanillaTooltipBackground(buffer, matrix, cached.minX, cached.startY, cached.maxX, cached.maxY, LightTexture.FULL_BRIGHT, fadeAlpha);

            float leftX = cached.minX + 8.0f;
            float curY = cached.startY + 5.0f;

            // 2. Animal Title (Enlarged & Left-aligned)
            poseStack.pushPose();
            poseStack.translate(leftX, curY, 0.0f);
            poseStack.scale(TITLE_SCALE, TITLE_SCALE, 1.0f);
            font.drawInBatch(cached.titleComp, 0.0f, 0.0f, HologramBarRenderer.applyAlpha(0xFFFFFFFF, fadeAlpha), false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
            curY += cached.headerHeight;

            // 3. Favorite Food Icons (3D Vanilla for Blocks, 2D Flat for Items) - Single Source of Truth (Max 8 in Hologram)
            if (cached.foodCount > 0) {
                for (int i = 0; i < cached.foodCount; i++) {
                    ItemStack foodStack = cached.foods.get(i);
                    float foodX = leftX + (i * FOOD_SPACING);
                    float foodY = curY + 2.0f;

                    BakedModel model = mc.getItemRenderer().getModel(foodStack, mc.level, null, 0);
                    if (model != null && model.isGui3d()) {
                        HologramBarRenderer.drawBlockIcon3D(mc, buffer, poseStack, foodStack, foodX, foodY, FOOD_SIZE, LightTexture.FULL_BRIGHT, fadeAlpha);
                    } else {
                        HologramBarRenderer.drawItemIcon2D(mc, buffer, matrix, foodStack, foodX, foodY, FOOD_SIZE, LightTexture.FULL_BRIGHT, fadeAlpha);
                    }
                }
                curY += cached.foodSectionHeight;
            }

            // 4. Stat rows layout (Left-aligned)
            float labelX = leftX;
            float iconX = labelX + cached.maxLabelWidth + GAP_LABEL_TO_ICON;
            float valueX = iconX + ICON_WIDTH + GAP_ICON_TO_VALUE;

            float r1Y = curY;
            float r2Y = curY + ROW_HEIGHT;
            float r3Y = curY + (ROW_HEIGHT * 2.0f);
            float r4Y = curY + (ROW_HEIGHT * 3.0f);
            float r5Y = curY + (ROW_HEIGHT * 4.0f);
            curY += (ROW_HEIGHT * 5.0f) + 4.0f;

            // Render Stat Icons (Two-sided & depth-tested with fadeAlpha)
            HologramBarRenderer.drawVanillaHeartIcon(mc, font, buffer, matrix, iconX, r1Y + 1.0f, LightTexture.FULL_BRIGHT, fadeAlpha);
            HologramBarRenderer.drawVanillaHungerIcon(mc, buffer, matrix, iconX, r2Y + 1.0f, LightTexture.FULL_BRIGHT, fadeAlpha);
            HologramBarRenderer.drawThirstIcon(buffer, matrix, iconX, r3Y + 1.0f, LightTexture.FULL_BRIGHT, fadeAlpha);
            HologramBarRenderer.drawCleanlinessIcon(buffer, matrix, iconX, r4Y + 1.0f, LightTexture.FULL_BRIGHT, fadeAlpha);
            HologramBarRenderer.drawThermometerIcon(buffer, matrix, iconX, r5Y + 1.0f, LightTexture.FULL_BRIGHT, fadeAlpha);

            // Render Stat Labels (Indonesian, Left-aligned with fadeAlpha)
            font.drawInBatch(HP_LABEL, labelX, r1Y + 1.0f, HologramBarRenderer.applyAlpha(0xFFDDDDDD, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(HUNGER_LABEL, labelX, r2Y + 1.0f, HologramBarRenderer.applyAlpha(0xFFDDDDDD, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(THIRST_LABEL, labelX, r3Y + 1.0f, HologramBarRenderer.applyAlpha(0xFFDDDDDD, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(CLEAN_LABEL, labelX, r4Y + 1.0f, HologramBarRenderer.applyAlpha(0xFFDDDDDD, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(TEMP_LABEL, labelX, r5Y + 1.0f, HologramBarRenderer.applyAlpha(0xFFDDDDDD, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            // Render Stat Values with fadeAlpha
            font.drawInBatch(cached.hpText, valueX, r1Y + 1.0f, HologramBarRenderer.applyAlpha(cached.hpTextColor, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(cached.hungerText, valueX, r2Y + 1.0f, HologramBarRenderer.applyAlpha(cached.hungerTextColor, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(cached.thirstText, valueX, r3Y + 1.0f, HologramBarRenderer.applyAlpha(cached.thirstTextColor, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(cached.cleanText, valueX, r4Y + 1.0f, HologramBarRenderer.applyAlpha(cached.cleanTextColor, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            // Temperature Value: Celsius (White) + Status (Green if comfortable, Red if needs heater/cooler)
            font.drawInBatch(cached.tempBaseText, valueX, r5Y + 1.0f, HologramBarRenderer.applyAlpha(0xFFFFFFFF, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            float statusX = valueX + cached.tempBaseTextWidth;
            font.drawInBatch(cached.tempStatusLabel, statusX, r5Y + 1.0f, HologramBarRenderer.applyAlpha(cached.tempStatusColor, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            // 5. Habitat Needs Section: ONLY RED ITEMS SHOWN (Left-aligned with fadeAlpha)
            float checklistStartX = leftX;
            if (cached.redNeeds.isEmpty()) {
                // All needs satisfied: single green line
                font.drawInBatch(HABITAT_OK_TEXT, checklistStartX, curY, HologramBarRenderer.applyAlpha(0xFF55FF55, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            } else {
                // Some needs missing: yellow header and red itemized checklist
                font.drawInBatch(HABITAT_HEADER_TEXT, checklistStartX, curY, HologramBarRenderer.applyAlpha(0xFFFFFF55, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                curY += 11.0f;
                for (Component need : cached.redNeeds) {
                    font.drawInBatch(need, checklistStartX + 4.0f, curY, HologramBarRenderer.applyAlpha(0xFFFF5555, fadeAlpha), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                    curY += 10.0f;
                }
            }

            // Single unified buffer flush at the end of card rendering
            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
        }

        poseStack.popPose();
    }

    public static List<String> getBiomeListEnglish(HabitatProfile profile) {
        if (profile == null) {
            return Collections.emptyList();
        }
        return BIOME_LIST_CACHE.computeIfAbsent(profile, p -> {
            List<String> names = new ArrayList<>();
            if (p.biomeIds() != null && !p.biomeIds().isEmpty()) {
                for (ResourceLocation loc : p.biomeIds()) {
                    String title = toEnglishBiomeName(loc.getPath());
                    if (!names.contains(title)) {
                        names.add(title);
                    }
                }
            }
            if (names.isEmpty() && p.name() != null && !p.name().equalsIgnoreCase("any")) {
                names.add(toEnglishBiomeName(p.name()));
            }
            return Collections.unmodifiableList(names);
        });
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
