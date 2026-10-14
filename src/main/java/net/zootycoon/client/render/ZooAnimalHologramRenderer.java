package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.compat.SereneSeasonsCompat;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.ZooNetworkPayloads;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ZooAnimalHologramRenderer {

    public static LivingEntity currentTargetedAnimal = null;

    public static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        boolean holdingMagnifier = mc.player.getMainHandItem().is(ZooItems.MAGNIFYING_GLASS)
                || mc.player.getOffhandItem().is(ZooItems.MAGNIFYING_GLASS);

        if (!holdingMagnifier) {
            currentTargetedAnimal = null;
            return;
        }

        // Raycast up to 20 blocks
        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = mc.player.getViewVector(1.0f);
        Vec3 reach = eye.add(look.scale(20.0));
        AABB searchBox = mc.player.getBoundingBox().expandTowards(look.scale(20.0)).inflate(2.0);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                mc.player, eye, reach, searchBox,
                e -> e instanceof LivingEntity && e.isAlive() && !e.equals(mc.player),
                0.25
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            currentTargetedAnimal = living;
        } else {
            currentTargetedAnimal = null;
            return;
        }

        renderHologram(context, mc, currentTargetedAnimal);
    }

    private static void renderHologram(WorldRenderContext context, Minecraft mc, LivingEntity entity) {
        PoseStack poseStack = context.matrixStack();
        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        Font font = mc.font;

        double entX = entity.getX() - camPos.x;
        double entY = entity.getY() + entity.getBbHeight() + 0.65 - camPos.y;
        double entZ = entity.getZ() - camPos.z;

        poseStack.pushPose();
        poseStack.translate(entX, entY, entZ);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-0.022f, -0.022f, 0.022f);

        // Gather Stats
        int hp = (int) entity.getHealth();
        int maxHp = (int) entity.getMaxHealth();

        ZooNetworkPayloads.AnimalSyncData sync = ZooTycoonClient.getAnimalData(entity.getId());
        int hunger = sync != null ? sync.hunger() : 80;
        int thirst = sync != null ? sync.thirst() : 80;

        var env = SereneSeasonsCompat.evaluateEnvironment(entity);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("§e§l🔍 " + entity.getDisplayName().getString()));
        lines.add(Component.literal("§cHealth: " + getHealthBar(hp, maxHp) + " §f(" + hp + "/" + maxHp + ")"));
        lines.add(Component.literal("§6Hunger: " + getBar(hunger, 100, "█", "░") + " §f" + hunger + "%"));
        lines.add(Component.literal("§bThirst: " + getBar(thirst, 100, "💧", "·") + " §f" + thirst + "%"));
        lines.add(Component.literal("§aIklim: §f" + env.tempCategory() + " §7| §b" + env.seasonName()));
        lines.add(Component.literal(env.comfortable() ? "§2✔ " + env.notes() : "§c⚠ " + env.notes()));

        int lineHeight = font.lineHeight + 2;
        int totalHeight = lines.size() * lineHeight + 6;
        int maxWidth = 0;
        for (Component c : lines) {
            maxWidth = Math.max(maxWidth, font.width(c));
        }
        int halfW = (maxWidth / 2) + 6;

        Matrix4f matrix = poseStack.last().pose();
        var buffer = context.consumers();

        // Draw slate background
        if (buffer != null) {
            float bgAlpha = 0.75f;
            int bgCol = (int) (bgAlpha * 255) << 24 | 0x0E131F;

            // Draw text lines
            int startY = -totalHeight / 2;
            for (int i = 0; i < lines.size(); i++) {
                Component line = lines.get(i);
                int lw = font.width(line);
                font.drawInBatch(
                        line,
                        -lw / 2.0f,
                        startY + (i * lineHeight) + 3,
                        0xFFFFFFFF,
                        false,
                        matrix,
                        buffer,
                        Font.DisplayMode.SEE_THROUGH,
                        bgCol,
                        15728880
                );
            }
        }

        poseStack.popPose();
    }

    private static String getHealthBar(int current, int max) {
        int hearts = Math.max(1, Math.min(10, (int) Math.ceil((current / (float) max) * 10)));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < hearts) sb.append("❤");
            else sb.append("♡");
        }
        return sb.toString();
    }

    private static String getBar(int val, int max, String fullChar, String emptyChar) {
        int filled = Math.max(0, Math.min(8, (int) Math.round((val / (float) max) * 8)));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i < filled) sb.append(fullChar);
            else sb.append(emptyChar);
        }
        return sb.toString();
    }
}

