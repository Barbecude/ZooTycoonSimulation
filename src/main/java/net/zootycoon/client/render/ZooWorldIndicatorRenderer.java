package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.animal.AnimalBathingManager;
import net.zootycoon.animal.ZooClimate;
import net.zootycoon.animal.habitat.HabitatProfile;
import net.zootycoon.animal.habitat.HabitatProfileLoader;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.entity.ZooBuilderEntity;
import net.zootycoon.entity.ZooCashierEntity;
import net.zootycoon.entity.ZooVisitorEntity;
import net.zootycoon.entity.ZooZookeeperEntity;
import net.zootycoon.network.ZooNetworkPayloads;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZooWorldIndicatorRenderer {

    private static final ResourceLocation HEALTH_TEXTURE =
            ZooTycoonMod.id("textures/gui/icons/heart.png");
    private static final ResourceLocation HUNGER_TEXTURE =
            ZooTycoonMod.id("textures/gui/icons/hunger_indicator.png");
    private static final ResourceLocation THIRST_TEXTURE =
            ZooTycoonMod.id("textures/gui/inspect/thirst.png");
    private static final ResourceLocation CLEAN_TEXTURE =
            ZooTycoonMod.id("textures/gui/inspect/cleanliness.png");
    private static final ResourceLocation TEMP_HOT_TEXTURE =
            ZooTycoonMod.id("textures/gui/icons/temp_fire.png");
    private static final ResourceLocation TEMP_COLD_TEXTURE =
            ZooTycoonMod.id("textures/gui/icons/temp_ice.png");
    private static final ResourceLocation EXCLAMATION_TEXTURE =
            ZooTycoonMod.id("textures/gui/icons/exclamation_point.png");

    private record AnimalAlert(
            ResourceLocation texture,
            float halfW,
            float halfH,
            float shiverX,
            float shiverY,
            int r, int g, int b
    ) {}

    private record CachedThermalStatus(long lastCheckTick, boolean tooHot, boolean tooCold) {}
    private static final Map<Integer, CachedThermalStatus> THERMAL_CACHE = new HashMap<>();

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
            // Do NOT show exclamation mark if unassigned to an animal or currently cleaning/sweeping/bathing
            if (living instanceof ZooZookeeperEntity keeper) {
                boolean isCleaning = keeper.isSweeping()
                        || keeper.getBathingAction() != 0
                        || keeper.getMainHandItem().is(Items.BRUSH)
                        || AnimalBathingManager.isWateringCan(keeper.getMainHandItem());

                if (!keeper.hasAssignedAnimal() || isCleaning) {
                    continue;
                }

                if (keeper.getFoodStock() <= 0) {
                    double renderY = (ey + keeper.getBbHeight() + 1.25D + bob) - camPos.y;
                    renderBillboard(
                            buffer, poseStack, EXCLAMATION_TEXTURE,
                            dx, renderY, dz,
                            0.20f, 0.28f,
                            camRot,
                            255, 255, 255, 255
                    );
                }
                continue;
            }

            // 1b. Builder indicator: orange exclamation mark if needing tools, materials, or wage payment
            if (living instanceof ZooBuilderEntity builder) {
                if (builder.needsSomething()) {
                    double renderY = (ey + builder.getBbHeight() + 1.25D + bob) - camPos.y;
                    renderBillboard(
                            buffer, poseStack, EXCLAMATION_TEXTURE,
                            dx, renderY, dz,
                            0.20f, 0.28f,
                            camRot,
                            255, 255, 255, 255
                    );
                }
                continue;
            }

            // 1b-2. Cashier indicator: exclamation mark if stock is empty
            if (living instanceof ZooCashierEntity cashier) {
                if (cashier.isOutOfStock()) {
                    double renderY = (ey + cashier.getBbHeight() + 1.25D + bob) - camPos.y;
                    renderBillboard(
                            buffer, poseStack, EXCLAMATION_TEXTURE,
                            dx, renderY, dz,
                            0.20f, 0.28f,
                            camRot,
                            255, 255, 255, 255
                    );
                }
                continue;
            }

            // 1c. Visitor mood icon: cleanly billboarded to camera at natural height just above head (hidden when leaving)
            if (living instanceof ZooVisitorEntity visitor) {
                if (visitor.isLeaving() || visitor.isTimeToLeave()) {
                    continue;
                }
                var mood = visitor.getMood();
                if (mood != null && mood != ZooVisitorEntity.VisitorMood.NEUTRAL) {
                    ResourceLocation tex = switch (mood) {
                        case TOILET -> ZooTycoonMod.id("textures/gui/moods/mood_toilet.png");
                        case AMAZED -> ZooTycoonMod.id("textures/gui/moods/mood_amazed.png");
                        case ADORED -> ZooTycoonMod.id("textures/gui/moods/mood_adored.png");
                        case HUNGRY -> ZooTycoonMod.id("textures/gui/moods/mood_hungry.png");
                        case THIRSTY -> ZooTycoonMod.id("textures/gui/moods/mood_thirsty.png");
                        case SCARED -> ZooTycoonMod.id("textures/gui/moods/mood_scared.png");
                        default -> null;
                    };
                    if (tex != null) {
                        double hatOffset = visitor.getItemBySlot(EquipmentSlot.HEAD).isEmpty() ? 0.65D : 1.05D;
                        double renderY = (ey + visitor.getBbHeight() + hatOffset + bob) - camPos.y;
                        renderBillboard(
                                buffer, poseStack, tex,
                                dx, renderY, dz,
                                0.20f, 0.20f,
                                camRot,
                                255, 255, 255, 255
                        );
                    }
                }
                continue;
            }

            // 2. Animal indicators: Health, Hunger, Thirst, Cleanliness, Temperature (Side-by-side)
            if (!ZooTycoonClient.clientAnimalMap.isEmpty()) {
                ZooNetworkPayloads.AnimalSyncData syncData = ZooTycoonClient.getAnimalData(living.getId());
                if (syncData != null) {
                    List<AnimalAlert> alerts = new ArrayList<>(5);

                    // 2a. Health: alert if <= 50%
                    float maxHp = syncData.maxHealth() > 0 ? syncData.maxHealth() : living.getMaxHealth();
                    if (syncData.health() <= (maxHp * 0.5f)) {
                        boolean critHp = syncData.health() <= (maxHp * 0.2f);
                        float hpSize = critHp ? (0.20f + (float) (Math.sin(now / 150.0) * 0.03)) : 0.18f;
                        alerts.add(new AnimalAlert(HEALTH_TEXTURE, hpSize, hpSize, 0.0f, 0.0f, 255, critHp ? 180 : 255, critHp ? 180 : 255));
                    }

                    // 2b. Hunger: alert if <= 50%
                    if (syncData.hunger() <= 50) {
                        boolean critHunger = syncData.hunger() <= 15;
                        float hungerSize = critHunger ? (0.22f + (float) (Math.sin(now / 150.0) * 0.04)) : 0.20f;
                        int g = critHunger ? 210 : 255;
                        int b = critHunger ? 210 : 255;
                        alerts.add(new AnimalAlert(HUNGER_TEXTURE, hungerSize, hungerSize, 0.0f, 0.0f, 255, g, b));
                    }

                    // 2c. Thirst: alert if <= 50%
                    if (syncData.thirst() <= 50) {
                        boolean critThirst = syncData.thirst() <= 15;
                        float thirstSize = critThirst ? (0.22f + (float) (Math.sin(now / 150.0) * 0.03)) : 0.20f;
                        alerts.add(new AnimalAlert(THIRST_TEXTURE, thirstSize, thirstSize, 0.0f, 0.0f, 255, critThirst ? 210 : 255, critThirst ? 210 : 255));
                    }

                    // 2d. Cleanliness: alert if <= 50%
                    if (syncData.cleanliness() <= 50) {
                        boolean critClean = syncData.cleanliness() <= 15;
                        float cleanSize = critClean ? (0.22f + (float) (Math.sin(now / 150.0) * 0.03)) : 0.20f;
                        alerts.add(new AnimalAlert(CLEAN_TEXTURE, cleanSize, cleanSize, 0.0f, 0.0f, 255, critClean ? 210 : 255, critClean ? 210 : 255));
                    }

                    // 2e. Temperature: Hot (Fire) / Cold (Ice with shivering vibration)
                    long gameTime = living.level().getGameTime();
                    CachedThermalStatus thermal = THERMAL_CACHE.get(living.getId());
                    if (thermal == null || (gameTime - thermal.lastCheckTick > 20)) {
                        HabitatProfile profile = HabitatProfileLoader.getProfile(living);
                        boolean hot = false;
                        boolean cold = false;
                        if (profile != null) {
                            float effTemp = ZooClimate.getEffectiveTemperature(living);
                            cold = effTemp < profile.minTemperature();
                            hot = effTemp > profile.maxTemperature();
                        }
                        thermal = new CachedThermalStatus(gameTime, hot, cold);
                        THERMAL_CACHE.put(living.getId(), thermal);
                        if (THERMAL_CACHE.size() > 200) {
                            THERMAL_CACHE.clear();
                        }
                    }

                    if (thermal.tooHot()) {
                        float firePulse = (float) (Math.sin(now / 120.0) * 0.02);
                        alerts.add(new AnimalAlert(TEMP_HOT_TEXTURE, 0.22f + firePulse, 0.22f + firePulse, 0.0f, 0.0f, 255, 255, 255));
                    } else if (thermal.tooCold()) {
                        float shiverX = (float) (Math.sin(now / 35.0) * 0.035);
                        float shiverY = (float) (Math.cos(now / 25.0) * 0.015);
                        alerts.add(new AnimalAlert(TEMP_COLD_TEXTURE, 0.22f, 0.22f, shiverX, shiverY, 255, 255, 255));
                    }

                    // Render all active alerts side-by-side horizontally
                    int count = alerts.size();
                    if (count > 0) {
                        double heightOffset = living.hasCustomName() ? 0.75D : 0.45D;
                        double renderY = (ey + living.getBbHeight() + heightOffset + bob) - camPos.y;
                        float spacing = 0.42f;
                        float startX = -((count - 1) * spacing) / 2.0f;

                        for (int i = 0; i < count; i++) {
                            AnimalAlert alert = alerts.get(i);
                            float localX = startX + (i * spacing) + alert.shiverX();
                            float localY = alert.shiverY();

                            renderBillboard(
                                    buffer, poseStack, alert.texture(),
                                    dx, renderY, dz,
                                    localX, localY,
                                    alert.halfW(), alert.halfH(),
                                    camRot,
                                    alert.r(), alert.g(), alert.b(), 255
                            );
                        }
                    }
                }
            }
        }

        // Flush see-through render type immediately during world render pass
        if (buffer instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }
    }

    private static void renderBillboard(
            MultiBufferSource buffer,
            PoseStack poseStack,
            ResourceLocation texture,
            double x, double y, double z,
            float localX, float localY,
            float halfW, float halfH,
            Quaternionf cameraRotation,
            int r, int g, int b, int a
    ) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(cameraRotation);
        if (localX != 0.0f || localY != 0.0f) {
            poseStack.translate(localX, localY, 0.0f);
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.textSeeThrough(texture));
        var pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        // Front face (facing camera)
        consumer.addVertex(matrix, -halfW, -halfH, 0.0f).setColor(r, g, b, a).setUv(0.0f, 1.0f).setLight(0xF000F0);
        consumer.addVertex(matrix, halfW, -halfH, 0.0f).setColor(r, g, b, a).setUv(1.0f, 1.0f).setLight(0xF000F0);
        consumer.addVertex(matrix, halfW, halfH, 0.0f).setColor(r, g, b, a).setUv(1.0f, 0.0f).setLight(0xF000F0);
        consumer.addVertex(matrix, -halfW, halfH, 0.0f).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(0xF000F0);

        // Back face (so it never culls regardless of winding)
        consumer.addVertex(matrix, -halfW, -halfH, 0.0f).setColor(r, g, b, a).setUv(0.0f, 1.0f).setLight(0xF000F0);
        consumer.addVertex(matrix, -halfW, halfH, 0.0f).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(0xF000F0);
        consumer.addVertex(matrix, halfW, halfH, 0.0f).setColor(r, g, b, a).setUv(1.0f, 0.0f).setLight(0xF000F0);
        consumer.addVertex(matrix, halfW, -halfH, 0.0f).setColor(r, g, b, a).setUv(1.0f, 1.0f).setLight(0xF000F0);

        poseStack.popPose();
    }

    private static void renderBillboard(
            MultiBufferSource buffer,
            PoseStack poseStack,
            ResourceLocation texture,
            double x, double y, double z,
            float halfW, float halfH,
            Quaternionf cameraRotation,
            int r, int g, int b, int a
    ) {
        renderBillboard(buffer, poseStack, texture, x, y, z, 0.0f, 0.0f, halfW, halfH, cameraRotation, r, g, b, a);
    }
}
