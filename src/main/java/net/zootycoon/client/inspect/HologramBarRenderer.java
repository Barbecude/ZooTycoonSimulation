package net.zootycoon.client.inspect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Helper class for rendering in-world inspection hologram bars, background plates, and icons.
 * Follows Minecraft vanilla styling with clean quads, depth testing, and proportional layout.
 */
public class HologramBarRenderer {

    // Status icon textures (clean 9x9 pixel-art icons)
    public static final ResourceLocation THIRST_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/inspect/thirst.png");
    public static final ResourceLocation CLEANLINESS_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/inspect/cleanliness.png");
    public static final ResourceLocation HUNGER_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/inspect/hunger.png");
    public static final ResourceLocation HEART_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/inspect/heart.png");

    // Background plate color: semi-transparent dark slate (0xAA0B0E14)
    public static final int PLATE_BACKGROUND = 0xAA0B0E14;
    public static final int DARK_BORDER = 0xFF05070A;

    // Bar colors
    public static final int HEALTH_COLOR = 0xFFE02B2B;
    public static final int HEALTH_TRACK = 0xFF3A1010;

    public static final int HUNGER_COLOR = 0xFFD47A22;
    public static final int HUNGER_TRACK = 0xFF381F0B;

    public static final int THIRST_COLOR = 0xFF2B8CE0;
    public static final int THIRST_TRACK = 0xFF0E223D;

    public static final int CLEANLINESS_COLOR = 0xFF4CD8D8;
    public static final int CLEANLINESS_TRACK = 0xFF0E3033;

    // Value text threshold colors
    public static final int COLOR_NORMAL = 0xFFFFFFFF;   // > 50%
    public static final int COLOR_WARNING = 0xFFFFFF55;  // <= 50%
    public static final int COLOR_CRITICAL = 0xFFFF5555; // <= 15%
    public static final int COLOR_MUTED = 0xFFAAAAAA;    // '-' or unregistered

    /**
     * Calculates the text status color according to the requirement:
     * - White if normal (> 50%)
     * - Yellow if warning (<= 50%)
     * - Red if critical (<= 15%)
     */
    public static int getStatusColor(float percentage) {
        if (percentage <= 15.0f) {
            return COLOR_CRITICAL;
        } else if (percentage <= 50.0f) {
            return COLOR_WARNING;
        } else {
            return COLOR_NORMAL;
        }
    }

    /**
     * Draws an untextured 2D quad with color and lightmap on a depth-tested vertex consumer.
     */
    public static void drawRect(VertexConsumer consumer, Matrix4f matrix, float minX, float minY, float maxX, float maxY, float z, int argb, int light) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        consumer.addVertex(matrix, minX, minY, z).setColor(r, g, b, a).setLight(light);
        consumer.addVertex(matrix, minX, maxY, z).setColor(r, g, b, a).setLight(light);
        consumer.addVertex(matrix, maxX, maxY, z).setColor(r, g, b, a).setLight(light);
        consumer.addVertex(matrix, maxX, minY, z).setColor(r, g, b, a).setLight(light);
    }

    /**
     * Draws a rounded (1-pixel chamfered corner) semi-transparent background plate.
     */
    public static void drawBackgroundPlate(MultiBufferSource buffer, Matrix4f matrix, float minX, float minY, float maxX, float maxY, int light) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.textBackground());
        float z = 0.02f;

        // Top horizontal strip (omits outer corner pixels)
        drawRect(consumer, matrix, minX + 1.0f, minY, maxX - 1.0f, minY + 1.0f, z, PLATE_BACKGROUND, light);
        // Middle main body
        drawRect(consumer, matrix, minX, minY + 1.0f, maxX, maxY - 1.0f, z, PLATE_BACKGROUND, light);
        // Bottom horizontal strip (omits outer corner pixels)
        drawRect(consumer, matrix, minX + 1.0f, maxY - 1.0f, maxX - 1.0f, maxY, z, PLATE_BACKGROUND, light);
    }

    /**
     * Draws a status progress bar with a dark border, track background, filled region, and top highlight.
     */
    public static void drawBar(VertexConsumer consumer, Matrix4f matrix, float x, float y, float width, float height, float progress, int barColor, int trackColor, int light) {
        float borderZ = 0.015f;
        float fillZ = 0.01f;

        // 1. Dark outer border (1px padding around bar)
        drawRect(consumer, matrix, x - 1.0f, y - 1.0f, x + width + 1.0f, y + height + 1.0f, borderZ, DARK_BORDER, light);

        // 2. Empty track background
        drawRect(consumer, matrix, x, y, x + width, y + height, borderZ, trackColor, light);

        // 3. Filled bar area (only when progress > 0)
        if (progress > 0.0f) {
            float filledWidth = Math.max(1.0f, width * Math.min(1.0f, progress));
            drawRect(consumer, matrix, x, y, x + filledWidth, y + height, fillZ, barColor, light);

            // Subtle 1px lighter highlight along the top edge for classic pixel-art depth
            int r = Math.min(255, ((barColor >> 16) & 0xFF) + 40);
            int g = Math.min(255, ((barColor >> 8) & 0xFF) + 40);
            int b = Math.min(255, (barColor & 0xFF) + 40);
            int highlightColor = (0xFF << 24) | (r << 16) | (g << 8) | b;
            drawRect(consumer, matrix, x, y, x + filledWidth, y + 1.0f, fillZ, highlightColor, light);
        }
    }

    /**
     * Draws a textured 9x9 icon using RenderType.text to ensure depth testing and transparency.
     */
    public static void drawTexturedIcon(MultiBufferSource buffer, Matrix4f matrix, ResourceLocation texture, float x, float y, float width, float height, int light) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.text(texture));
        float z = 0.0f;
        int a = 255, r = 255, g = 255, b = 255;

        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(light);
        consumer.addVertex(matrix, x, y + height, z).setColor(r, g, b, a).setUv(0.0f, 1.0f).setLight(light);
        consumer.addVertex(matrix, x + width, y + height, z).setColor(r, g, b, a).setUv(1.0f, 1.0f).setLight(light);
        consumer.addVertex(matrix, x + width, y, z).setColor(r, g, b, a).setUv(1.0f, 0.0f).setLight(light);
    }

    /**
     * Draws the authentic vanilla heart icon '❤' using the Minecraft font glyph renderer.
     */
    public static void drawHeartIcon(Font font, MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light) {
        font.drawInBatch("❤", x, y, 0xFFFF3333, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, light);
    }
}

