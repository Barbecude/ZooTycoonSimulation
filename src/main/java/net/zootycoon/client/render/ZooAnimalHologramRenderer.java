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
import net.zootycoon.client.inspect.InspectionClientCache;
import net.zootycoon.client.inspect.InspectionTargetTracker;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

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

        List<Component> lines = new ArrayList<>();
        // Title (Clean, NO BOLD)
        lines.add(entity.getDisplayName());

        // Health
        int hp = (int) Math.ceil(entity.getHealth());
        int maxHp = (int) Math.ceil(entity.getMaxHealth());
        lines.add(Component.literal("Health: " + hp + "/" + maxHp));

        // Read snapshot from InspectionClientCache
        InspectionClientCache.Snapshot snapshot = InspectionClientCache.getSnapshot(entity.getId());
        if (snapshot == null || !snapshot.registered()) {
            lines.add(Component.literal("§eBelum terdaftar"));
            lines.add(Component.literal("Hunger: -"));
            lines.add(Component.literal("Thirst: -"));
            lines.add(Component.literal("Cleanliness: -"));
        } else {
            int hunger = snapshot.hunger();
            if (hunger >= 0) {
                String hungerCol = hunger <= 25 ? "§c" : (hunger <= 60 ? "§e" : "§f");
                lines.add(Component.literal(hungerCol + "Hunger: " + getBar(hunger, 100, "█", "░") + " " + hunger + "%"));
            } else {
                lines.add(Component.literal("Hunger: -"));
            }

            int thirst = snapshot.thirst();
            if (thirst >= 0) {
                String thirstCol = thirst <= 25 ? "§c" : (thirst <= 60 ? "§e" : "§f");
                lines.add(Component.literal(thirstCol + "Thirst: " + getBar(thirst, 100, "█", "░") + " " + thirst + "%"));
            } else {
                lines.add(Component.literal("Thirst: -"));
            }

            int cleanliness = snapshot.cleanliness();
            if (cleanliness >= 0) {
                String cleanCol = cleanliness <= 10 ? "§c" : (cleanliness <= 25 ? "§e" : "§f");
                lines.add(Component.literal(cleanCol + "Cleanliness: " + getBar(cleanliness, 100, "█", "░") + " " + cleanliness + "%"));
            } else {
                lines.add(Component.literal("Cleanliness: -"));
            }
        }

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource buffer = context.consumers();

        if (buffer != null) {
            float bgAlpha = 0.5f;
            int bgCol = (int) (bgAlpha * 255) << 24;

            int lineSpacing = 10;
            int startY = -(lines.size() * lineSpacing) / 2;

            for (int i = 0; i < lines.size(); i++) {
                Component line = lines.get(i);
                int width = font.width(line);
                float x = -width / 2.0f;
                float y = startY + (i * lineSpacing);

                font.drawInBatch(
                        line,
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
            }

            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
        }

        poseStack.popPose();
    }

    private static String getBar(int val, int max, String fullChar, String emptyChar) {
        int segments = 10;
        int filled = Math.max(0, Math.min(segments, Math.round((val / (float) max) * segments)));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments; i++) {
            if (i < filled) sb.append(fullChar);
            else sb.append(emptyChar);
        }
        return sb.toString();
    }
}
