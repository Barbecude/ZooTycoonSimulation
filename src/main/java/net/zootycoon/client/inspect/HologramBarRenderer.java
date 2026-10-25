package net.zootycoon.client.inspect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Helper class for rendering in-world inspection hologram bars, background plates, and icons.
 * Follows Minecraft vanilla styling with clean quads, depth testing, and proportional layout.
 */
public class HologramBarRenderer {

    // Vanilla Minecraft GUI sprites for heart and hunger
    public static final ResourceLocation VANILLA_HEART_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/full");
    public static final ResourceLocation VANILLA_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_full");

    public static final ResourceLocation VANILLA_HEART_CONTAINER = ResourceLocation.withDefaultNamespace("hud/heart/container");
    public static final ResourceLocation VANILLA_HEART_FULL = ResourceLocation.withDefaultNamespace("hud/heart/full");
    public static final ResourceLocation VANILLA_HEART_HALF = ResourceLocation.withDefaultNamespace("hud/heart/half");

    public static final ResourceLocation VANILLA_FOOD_EMPTY = ResourceLocation.withDefaultNamespace("hud/food_empty");
    public static final ResourceLocation VANILLA_FOOD_FULL = ResourceLocation.withDefaultNamespace("hud/food_full");
    public static final ResourceLocation VANILLA_FOOD_HALF = ResourceLocation.withDefaultNamespace("hud/food_half");

    // Custom Zoo Tycoon 9x9 pixel-art icons for mechanics not present in vanilla Minecraft
    public static final ResourceLocation THIRST_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/inspect/thirst.png");
    public static final ResourceLocation CLEANLINESS_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/inspect/cleanliness.png");
    public static final ResourceLocation THERMOMETER_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/inspect/thermometer.png");

    // Vanilla Tooltip Colors: dark translucent background and gradient border (Top 0x505000FF to Bottom 0x5028007F)
    public static final int TOOLTIP_BG = 0xF0100010;
    public static final int TOOLTIP_BORDER_TOP = 0x505000FF;
    public static final int TOOLTIP_BORDER_BOTTOM = 0x5028007F;

    // Value text threshold colors
    public static final int COLOR_NORMAL = 0xFFFFFFFF;   // > 50%
    public static final int COLOR_WARNING = 0xFFFFFF55;  // <= 50%
    public static final int COLOR_CRITICAL = 0xFFFF5555; // <= 15%
    public static final int COLOR_MUTED = 0xFFAAAAAA;    // '-' or unregistered

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
     * Draws an untextured 2D quad with two-sided vertices so it is never culled by OpenGL.
     */
    public static void drawRect(VertexConsumer consumer, Matrix4f matrix, float minX, float minY, float maxX, float maxY, float z, int argb, int light) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        // Front face (negative Y scale compliant)
        consumer.addVertex(matrix, minX, minY, z).setColor(r, g, b, a).setLight(light);
        consumer.addVertex(matrix, minX, maxY, z).setColor(r, g, b, a).setLight(light);
        consumer.addVertex(matrix, maxX, maxY, z).setColor(r, g, b, a).setLight(light);
        consumer.addVertex(matrix, maxX, minY, z).setColor(r, g, b, a).setLight(light);

        // Back face (positive Y scale compliant)
        consumer.addVertex(matrix, minX, minY, z).setColor(r, g, b, a).setLight(light);
        consumer.addVertex(matrix, maxX, minY, z).setColor(r, g, b, a).setLight(light);
        consumer.addVertex(matrix, maxX, maxY, z).setColor(r, g, b, a).setLight(light);
        consumer.addVertex(matrix, minX, maxY, z).setColor(r, g, b, a).setLight(light);
    }

    /**
     * Draws a vertical gradient quad (two-sided).
     */
    public static void drawVerticalGradientRect(VertexConsumer consumer, Matrix4f matrix, float minX, float minY, float maxX, float maxY, float z, int topColor, int bottomColor, int light) {
        int aTop = (topColor >> 24) & 0xFF;
        int rTop = (topColor >> 16) & 0xFF;
        int gTop = (topColor >> 8) & 0xFF;
        int bTop = topColor & 0xFF;

        int aBot = (bottomColor >> 24) & 0xFF;
        int rBot = (bottomColor >> 16) & 0xFF;
        int gBot = (bottomColor >> 8) & 0xFF;
        int bBot = bottomColor & 0xFF;

        // Front face
        consumer.addVertex(matrix, minX, minY, z).setColor(rTop, gTop, bTop, aTop).setLight(light);
        consumer.addVertex(matrix, minX, maxY, z).setColor(rBot, gBot, bBot, aBot).setLight(light);
        consumer.addVertex(matrix, maxX, maxY, z).setColor(rBot, gBot, bBot, aBot).setLight(light);
        consumer.addVertex(matrix, maxX, minY, z).setColor(rTop, gTop, bTop, aTop).setLight(light);

        // Back face
        consumer.addVertex(matrix, minX, minY, z).setColor(rTop, gTop, bTop, aTop).setLight(light);
        consumer.addVertex(matrix, maxX, minY, z).setColor(rTop, gTop, bTop, aTop).setLight(light);
        consumer.addVertex(matrix, maxX, maxY, z).setColor(rBot, gBot, bBot, aBot).setLight(light);
        consumer.addVertex(matrix, minX, maxY, z).setColor(rBot, gBot, bBot, aBot).setLight(light);
    }

    /**
     * Helper to multiply an ARGB color's alpha channel by an alphaFactor [0.0f, 1.0f].
     */
    public static int applyAlpha(int argb, float alphaFactor) {
        int origAlpha = (argb >> 24) & 0xFF;
        if (origAlpha == 0) {
            origAlpha = 255;
        }
        int newAlpha = Math.max(0, Math.min(255, Math.round(origAlpha * Math.max(0.0f, Math.min(1.0f, alphaFactor)))));
        return (newAlpha << 24) | (argb & 0x00FFFFFF);
    }

    /**
     * Draws an authentic vanilla Minecraft tooltip background box with the iconic purple gradient border.
     */
    public static void drawVanillaTooltipBackground(MultiBufferSource buffer, Matrix4f matrix, float minX, float minY, float maxX, float maxY, int light) {
        drawVanillaTooltipBackground(buffer, matrix, minX, minY, maxX, maxY, light, 1.0f);
    }

    public static void drawVanillaTooltipBackground(MultiBufferSource buffer, Matrix4f matrix, float minX, float minY, float maxX, float maxY, int light, float alphaFactor) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.textBackground());
        float z = -0.02f;

        int bg = applyAlpha(TOOLTIP_BG, alphaFactor);
        int borderTop = applyAlpha(TOOLTIP_BORDER_TOP, alphaFactor);
        int borderBottom = applyAlpha(TOOLTIP_BORDER_BOTTOM, alphaFactor);

        // 1. Center background fill
        drawRect(consumer, matrix, minX + 1.0f, minY + 1.0f, maxX - 1.0f, maxY - 1.0f, z, bg, light);
        // Outer 1-pixel chamfered extensions
        drawRect(consumer, matrix, minX + 2.0f, minY, maxX - 2.0f, minY + 1.0f, z, bg, light);
        drawRect(consumer, matrix, minX + 2.0f, maxY - 1.0f, maxX - 2.0f, maxY, z, bg, light);
        drawRect(consumer, matrix, minX, minY + 2.0f, minX + 1.0f, maxY - 2.0f, z, bg, light);
        drawRect(consumer, matrix, maxX - 1.0f, minY + 2.0f, maxX, maxY - 2.0f, z, bg, light);

        // 2. Vanilla tooltip border frame (Top 0xF05000FF to Bottom 0xF028007F)
        float borderZ = z + 0.005f;
        drawRect(consumer, matrix, minX + 1.0f, minY + 1.0f, maxX - 1.0f, minY + 2.0f, borderZ, borderTop, light);
        drawRect(consumer, matrix, minX + 1.0f, maxY - 2.0f, maxX - 1.0f, maxY - 1.0f, borderZ, borderBottom, light);
        drawVerticalGradientRect(consumer, matrix, minX + 1.0f, minY + 2.0f, minX + 2.0f, maxY - 2.0f, borderZ, borderTop, borderBottom, light);
        drawVerticalGradientRect(consumer, matrix, maxX - 2.0f, minY + 2.0f, maxX - 1.0f, maxY - 2.0f, borderZ, borderTop, borderBottom, light);
    }

    /**
     * Draws a textured 9x9 icon using RenderType.textSeeThrough with two-sided quads so it is always visible.
     */
    public static void drawTexturedIcon(MultiBufferSource buffer, Matrix4f matrix, ResourceLocation texture, float x, float y, float width, float height, int light) {
        drawTexturedIcon(buffer, matrix, texture, x, y, width, height, light, 1.0f);
    }

    public static void drawTexturedIcon(MultiBufferSource buffer, Matrix4f matrix, ResourceLocation texture, float x, float y, float width, float height, int light, float alphaFactor) {
        drawTexturedIcon(buffer, matrix, texture, x, y, width, height, light, alphaFactor, 1.0f, 1.0f, 1.0f);
    }

    public static void drawTexturedIcon(MultiBufferSource buffer, Matrix4f matrix, ResourceLocation texture, float x, float y, float width, float height, int light, float alphaFactor, float red, float green, float blue) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.text(texture));
        float z = -0.005f;
        int a = Math.max(0, Math.min(255, Math.round(255 * Math.max(0.0f, Math.min(1.0f, alphaFactor)))));
        int r = Math.max(0, Math.min(255, Math.round(255 * red)));
        int g = Math.max(0, Math.min(255, Math.round(255 * green)));
        int b = Math.max(0, Math.min(255, Math.round(255 * blue)));

        // Front face (negative Y scale compliant)
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(light);
        consumer.addVertex(matrix, x, y + height, z).setColor(r, g, b, a).setUv(0.0f, 1.0f).setLight(light);
        consumer.addVertex(matrix, x + width, y + height, z).setColor(r, g, b, a).setUv(1.0f, 1.0f).setLight(light);
        consumer.addVertex(matrix, x + width, y, z).setColor(r, g, b, a).setUv(1.0f, 0.0f).setLight(light);

        // Back face (positive Y scale compliant)
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(light);
        consumer.addVertex(matrix, x + width, y, z).setColor(r, g, b, a).setUv(1.0f, 0.0f).setLight(light);
        consumer.addVertex(matrix, x + width, y + height, z).setColor(r, g, b, a).setUv(1.0f, 1.0f).setLight(light);
        consumer.addVertex(matrix, x, y + height, z).setColor(r, g, b, a).setUv(0.0f, 1.0f).setLight(light);
    }

    /**
     * Draws a vanilla GUI atlas sprite with two-sided quads using RenderType.textSeeThrough.
     */
    public static void drawAtlasSprite(MultiBufferSource buffer, Matrix4f matrix, TextureAtlasSprite sprite, float x, float y, float width, float height, int light) {
        drawAtlasSprite(buffer, matrix, sprite, x, y, width, height, 0.0f, light, 1.0f);
    }

    public static void drawAtlasSprite(MultiBufferSource buffer, Matrix4f matrix, TextureAtlasSprite sprite, float x, float y, float width, float height, int light, float alphaFactor) {
        drawAtlasSprite(buffer, matrix, sprite, x, y, width, height, 0.0f, light, alphaFactor);
    }

    public static void drawAtlasSprite(MultiBufferSource buffer, Matrix4f matrix, TextureAtlasSprite sprite, float x, float y, float width, float height, float zOffset, int light, float alphaFactor) {
        if (sprite == null) return;
        VertexConsumer consumer = buffer.getBuffer(RenderType.text(sprite.atlasLocation()));
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float z = -0.005f + zOffset;
        int a = Math.max(0, Math.min(255, Math.round(255 * Math.max(0.0f, Math.min(1.0f, alphaFactor)))));
        int r = 255, g = 255, b = 255;

        // Front face
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(u0, v0).setLight(light);
        consumer.addVertex(matrix, x, y + height, z).setColor(r, g, b, a).setUv(u0, v1).setLight(light);
        consumer.addVertex(matrix, x + width, y + height, z).setColor(r, g, b, a).setUv(u1, v1).setLight(light);
        consumer.addVertex(matrix, x + width, y, z).setColor(r, g, b, a).setUv(u1, v0).setLight(light);

        // Back face
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(u0, v0).setLight(light);
        consumer.addVertex(matrix, x + width, y, z).setColor(r, g, b, a).setUv(u1, v0).setLight(light);
        consumer.addVertex(matrix, x + width, y + height, z).setColor(r, g, b, a).setUv(u1, v1).setLight(light);
        consumer.addVertex(matrix, x, y + height, z).setColor(r, g, b, a).setUv(u0, v1).setLight(light);
    }

    /**
     * Draws the 10-heart health row matching the Animal Management Tab format.
     */
    public static void drawHeartRow(Minecraft mc, MultiBufferSource buffer, Matrix4f matrix, float startX, float y, float health, float maxHealth, int light, float alphaFactor) {
        if (mc == null || mc.getGuiSprites() == null) return;
        TextureAtlasSprite containerSprite = mc.getGuiSprites().getSprite(VANILLA_HEART_CONTAINER);
        TextureAtlasSprite fullSprite = mc.getGuiSprites().getSprite(VANILLA_HEART_FULL);
        TextureAtlasSprite halfSprite = mc.getGuiSprites().getSprite(VANILLA_HEART_HALF);

        float safeMax = Math.max(0.1f, maxHealth);
        float hpRatio = Math.clamp(health / safeMax, 0.0f, 1.0f);
        float scaledHp = hpRatio * 20.0f;

        for (int i = 0; i < 10; i++) {
            float hX = startX + (i * 10.0f);
            drawAtlasSprite(buffer, matrix, containerSprite, hX, y, 9.0f, 9.0f, 0.0f, light, alphaFactor);
            float hpForHeart = scaledHp - (i * 2.0f);
            if (hpForHeart >= 1.5f) {
                drawAtlasSprite(buffer, matrix, fullSprite, hX, y, 9.0f, 9.0f, -0.001f, light, alphaFactor);
            } else if (hpForHeart >= 0.5f) {
                drawAtlasSprite(buffer, matrix, halfSprite, hX, y, 9.0f, 9.0f, -0.001f, light, alphaFactor);
            }
        }
    }

    /**
     * Draws the 10-shank hunger row matching the Animal Management Tab format.
     */
    public static void drawHungerRow(Minecraft mc, MultiBufferSource buffer, Matrix4f matrix, float startX, float y, int hunger, boolean registered, int light, float alphaFactor) {
        if (mc == null || mc.getGuiSprites() == null) return;
        TextureAtlasSprite emptySprite = mc.getGuiSprites().getSprite(VANILLA_FOOD_EMPTY);
        TextureAtlasSprite fullSprite = mc.getGuiSprites().getSprite(VANILLA_FOOD_FULL);
        TextureAtlasSprite halfSprite = mc.getGuiSprites().getSprite(VANILLA_FOOD_HALF);

        int totalHalfShanks = 20;
        int safeHunger = (registered && hunger >= 0) ? Math.clamp(hunger, 0, 100) : 0;
        int filledHalfShanks = Math.clamp(Math.round((safeHunger / 100.0f) * totalHalfShanks), 0, totalHalfShanks);

        for (int i = 0; i < 10; i++) {
            float sX = startX + (i * 10.0f);
            drawAtlasSprite(buffer, matrix, emptySprite, sX, y, 9.0f, 9.0f, 0.0f, light, alphaFactor);
            if (registered && hunger >= 0) {
                int halfRemaining = filledHalfShanks - (i * 2);
                if (halfRemaining >= 2) {
                    drawAtlasSprite(buffer, matrix, fullSprite, sX, y, 9.0f, 9.0f, -0.001f, light, alphaFactor);
                } else if (halfRemaining == 1) {
                    drawAtlasSprite(buffer, matrix, halfSprite, sX, y, 9.0f, 9.0f, -0.001f, light, alphaFactor);
                }
            }
        }
    }

    /**
     * Draws the 10-droplet thirst row matching the Animal Management Tab format.
     */
    public static void drawThirstRow(MultiBufferSource buffer, Matrix4f matrix, float startX, float y, int thirst, boolean registered, int light, float alphaFactor) {
        int safeThirst = (registered && thirst >= 0) ? Math.clamp(thirst, 0, 100) : 0;
        int filledDroplets = Math.clamp(Math.round(safeThirst / 10.0f), 0, 10);

        for (int i = 0; i < 10; i++) {
            float sX = startX + (i * 10.0f);
            if (registered && thirst >= 0 && i < filledDroplets) {
                drawTexturedIcon(buffer, matrix, THIRST_ICON, sX, y, 9.0f, 9.0f, light, alphaFactor, 1.0f, 1.0f, 1.0f);
            } else {
                drawTexturedIcon(buffer, matrix, THIRST_ICON, sX, y, 9.0f, 9.0f, light, alphaFactor * 0.45f, 0.35f, 0.40f, 0.48f);
            }
        }
    }

    /**
     * Draws the 10-sponge cleanliness row matching the Animal Management Tab format.
     */
    public static void drawCleanlinessRow(MultiBufferSource buffer, Matrix4f matrix, float startX, float y, int cleanliness, boolean registered, int light, float alphaFactor) {
        int safeClean = (registered && cleanliness >= 0) ? Math.clamp(cleanliness, 0, 100) : 0;
        int filledClean = Math.clamp(Math.round(safeClean / 10.0f), 0, 10);

        for (int i = 0; i < 10; i++) {
            float sX = startX + (i * 10.0f);
            if (registered && cleanliness >= 0 && i < filledClean) {
                drawTexturedIcon(buffer, matrix, CLEANLINESS_ICON, sX, y, 9.0f, 9.0f, light, alphaFactor, 1.0f, 1.0f, 1.0f);
            } else {
                drawTexturedIcon(buffer, matrix, CLEANLINESS_ICON, sX, y, 9.0f, 9.0f, light, alphaFactor * 0.40f, 0.38f, 0.38f, 0.38f);
            }
        }
    }

    /**
     * Draws the vanilla heart icon using the official vanilla GUI sprite hud/heart/full,
     * falling back to the font glyph '❤' if the sprite manager is unavailable.
     */
    public static void drawVanillaHeartIcon(Minecraft mc, Font font, MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light) {
        drawVanillaHeartIcon(mc, font, buffer, matrix, x, y, light, 1.0f);
    }

    public static void drawVanillaHeartIcon(Minecraft mc, Font font, MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light, float alphaFactor) {
        if (mc != null && mc.getGuiSprites() != null) {
            TextureAtlasSprite sprite = mc.getGuiSprites().getSprite(VANILLA_HEART_SPRITE);
            if (sprite != null) {
                drawAtlasSprite(buffer, matrix, sprite, x, y, 9.0f, 9.0f, light, alphaFactor);
                return;
            }
        }
        font.drawInBatch("❤", x + 1.0f, y, applyAlpha(0xFFFF2222, alphaFactor), false, matrix, buffer, Font.DisplayMode.NORMAL, 0, light);
    }

    /**
     * Draws the vanilla hunger icon.
     */
    public static void drawVanillaHungerIcon(Minecraft mc, MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light) {
        drawVanillaHungerIcon(mc, buffer, matrix, x, y, light, 1.0f);
    }

    public static void drawVanillaHungerIcon(Minecraft mc, MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light, float alphaFactor) {
        if (mc != null && mc.getGuiSprites() != null) {
            TextureAtlasSprite sprite = mc.getGuiSprites().getSprite(VANILLA_HUNGER_SPRITE);
            if (sprite != null) {
                drawAtlasSprite(buffer, matrix, sprite, x, y, 9.0f, 9.0f, light, alphaFactor);
                return;
            }
        }
        // Fallback: draw textured icon or colored square
        drawRect(buffer.getBuffer(RenderType.textBackground()), matrix, x, y, x + 8.0f, y + 8.0f, -0.005f, applyAlpha(0xFFD47A22, alphaFactor), light);
    }

    /**
     * Draws the custom water droplet thirst icon.
     */
    public static void drawThirstIcon(MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light) {
        drawThirstIcon(buffer, matrix, x, y, light, 1.0f);
    }

    public static void drawThirstIcon(MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light, float alphaFactor) {
        drawTexturedIcon(buffer, matrix, THIRST_ICON, x, y, 9.0f, 9.0f, light, alphaFactor);
    }

    /**
     * Draws the custom sponge/bubble cleanliness icon.
     */
    public static void drawCleanlinessIcon(MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light) {
        drawCleanlinessIcon(buffer, matrix, x, y, light, 1.0f);
    }

    public static void drawCleanlinessIcon(MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light, float alphaFactor) {
        drawTexturedIcon(buffer, matrix, CLEANLINESS_ICON, x, y, 9.0f, 9.0f, light, alphaFactor);
    }

    /**
     * Draws the Legendary Survival Overhaul thermometer icon for temperature status.
     */
    public static void drawThermometerIcon(MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light) {
        drawThermometerIcon(buffer, matrix, x, y, light, 1.0f);
    }

    public static void drawThermometerIcon(MultiBufferSource buffer, Matrix4f matrix, float x, float y, int light, float alphaFactor) {
        drawTexturedIcon(buffer, matrix, THERMOMETER_ICON, x, y, 9.0f, 9.0f, light, alphaFactor);
    }

    /**
     * Draws a 2D flat unshaded item icon from the item model sprite with no 3D extrusion, no shading, and no cropping.
     */
    public static void drawItemIcon2D(Minecraft mc, MultiBufferSource buffer, Matrix4f matrix, ItemStack stack, float x, float y, float size, int light, float alphaFactor) {
        if (stack == null || stack.isEmpty() || mc == null) return;
        BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, null, 0);
        if (model == null) return;
        TextureAtlasSprite sprite = model.getParticleIcon();
        if (sprite == null) return;

        int a = Math.max(0, Math.min(255, Math.round(255 * Math.max(0.0f, Math.min(1.0f, alphaFactor)))));
        int r = 255, g = 255, b = 255;

        VertexConsumer consumer = buffer.getBuffer(RenderType.text(sprite.atlasLocation()));
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float z = -0.005f;

        // Front face (aligned with billboard matrix)
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(u0, v0).setLight(light);
        consumer.addVertex(matrix, x, y + size, z).setColor(r, g, b, a).setUv(u0, v1).setLight(light);
        consumer.addVertex(matrix, x + size, y + size, z).setColor(r, g, b, a).setUv(u1, v1).setLight(light);
        consumer.addVertex(matrix, x + size, y, z).setColor(r, g, b, a).setUv(u1, v0).setLight(light);

        // Back face (two-sided for safety)
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(u0, v0).setLight(light);
        consumer.addVertex(matrix, x + size, y, z).setColor(r, g, b, a).setUv(u1, v0).setLight(light);
        consumer.addVertex(matrix, x + size, y + size, z).setColor(r, g, b, a).setUv(u1, v1).setLight(light);
        consumer.addVertex(matrix, x, y + size, z).setColor(r, g, b, a).setUv(u0, v1).setLight(light);
    }

    /**
     * Draws a 3D isometric block icon using vanilla Minecraft ItemRenderer for blocks (like Hay Bale)
     * positioned cleanly in front of the hologram background with no clipping.
     */
    public static void drawBlockIcon3D(Minecraft mc, MultiBufferSource buffer, PoseStack poseStack, ItemStack stack, float x, float y, float size, int light, float alphaFactor) {
        if (stack == null || stack.isEmpty() || mc == null) return;
        if (alphaFactor <= 0.05f) return;

        poseStack.pushPose();
        // Translate to icon center and bring forward on Z (+8.0f) so 3D depth never clips behind background plate
        poseStack.translate(x + (size / 2.0f), y + (size / 2.0f), 8.0f);
        float renderScale = size * Math.min(1.0f, Math.max(0.01f, alphaFactor));
        poseStack.scale(renderScale, -renderScale, renderScale);

        mc.getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.GUI,
                light,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                mc.level,
                0
        );
        poseStack.popPose();
    }
}
