package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.inspect.HologramBarRenderer;
import net.zootycoon.network.WarehousePayloads;
import net.zootycoon.util.ZooTranslations;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Billboarded hologram above an empty barrel listing the staff requests it can fulfil.
 * Data arrives through {@link WarehousePayloads.BarrelHologramPayload}; layout is computed once per
 * payload so the per-frame path only draws.
 */
public class EmptyBarrelHologramRenderer {

    private static final ResourceLocation EXCLAMATION_TEXTURE = ZooTycoonMod.id("textures/gui/icons/exclamation_point.png");

    private static final float HOLO_SCALE = 0.0125f;
    private static final double ANCHOR_Y = 1.2D;
    private static final double MAX_DISTANCE = 16.0D;
    private static final double FADE_START_DISTANCE = 12.0D;
    private static final long STALE_TIMEOUT_MS = 3000L;
    private static final float FADE_IN_MS = 200.0f;
    private static final float FADE_OUT_MS = 250.0f;

    private static final int MAX_REQUESTS = 3;
    private static final int MAX_ICONS = 5;
    private static final float PADDING = 6.0f;
    private static final float TITLE_H = 12.0f;
    private static final float LINE_H = 10.0f;
    private static final float ICON_SIZE = 10.0f;
    private static final float ICON_SPACING = 12.0f;
    private static final float BAR_H = 3.0f;
    private static final float REQUEST_GAP = 5.0f;
    private static final float MIN_WIDTH = 120.0f;
    private static final float MAX_TEXT_WIDTH = 170.0f;

    private static final int COLOR_TITLE = 0xFFFFFFFF;
    private static final int COLOR_NAME = 0xFFE6EDF3;
    private static final int COLOR_MUTED = 0xFFAAAAAA;
    private static final int COLOR_PROGRESS = 0xFFFFFF55;
    private static final int COLOR_READY = 0xFF55FF55;
    private static final int COLOR_DIVIDER = 0x40FFFFFF;
    private static final int COLOR_TRACK = 0xFF151820;

    private record RequestLayout(String name, String subtitle, String countText, float countWidth,
                                 List<ItemStack> icons, float progress, boolean ready) {}

    private record PanelLayout(String title, List<RequestLayout> requests, String moreText,
                               float width, float height) {}

    private static final class Entry {
        ResourceKey<Level> dimension;
        PanelLayout layout;
        long lastUpdateMs;
        long shownSinceMs;
        long hideSinceMs = -1L;
    }

    private static final Map<BlockPos, Entry> ENTRIES = new HashMap<>();

    public static void reset() {
        ENTRIES.clear();
    }

    public static void onPayload(WarehousePayloads.BarrelHologramPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long now = System.currentTimeMillis();
        BlockPos pos = payload.pos().immutable();
        Entry entry = ENTRIES.get(pos);

        if (payload.requests().isEmpty()) {
            // Keep the last layout briefly so the panel fades out instead of popping
            if (entry != null && entry.hideSinceMs < 0L) {
                entry.hideSinceMs = now;
            }
            return;
        }

        if (entry == null || entry.hideSinceMs >= 0L) {
            entry = new Entry();
            entry.shownSinceMs = now;
            ENTRIES.put(pos, entry);
        }
        entry.dimension = mc.level.dimension();
        entry.layout = buildLayout(mc.font, payload);
        entry.lastUpdateMs = now;
        entry.hideSinceMs = -1L;
    }

    private static PanelLayout buildLayout(Font font, WarehousePayloads.BarrelHologramPayload payload) {
        String title = ZooTranslations.get("zootycoon.hologram.barrel.title." + payload.category().toLowerCase(Locale.ROOT));
        String readyText = ZooTranslations.get("zootycoon.hologram.barrel.ready");

        float contentW = font.width(title) + 12.0f;
        List<RequestLayout> rows = new ArrayList<>();
        int shown = Math.min(MAX_REQUESTS, payload.requests().size());
        for (int i = 0; i < shown; i++) {
            var dto = payload.requests().get(i);
            // allReadyToFulfill also covers partial delivery, so "ready" here means the full amount is in the barrel
            boolean ready = dto.requiredCount() > 0
                    ? dto.currentCount() >= dto.requiredCount()
                    : dto.allReadyToFulfill();
            String countText = ready
                    ? readyText
                    : dto.currentCount() + "/" + dto.requiredCount();
            float countW = font.width(countText);
            String name = font.plainSubstrByWidth(dto.staffName(), (int) (MAX_TEXT_WIDTH - countW - 6.0f));
            String subtitle = font.plainSubstrByWidth(dto.subtitle(), (int) MAX_TEXT_WIDTH);

            List<ItemStack> icons = new ArrayList<>();
            for (ItemStack st : dto.targetItems()) {
                if (icons.size() >= MAX_ICONS) break;
                if (!st.isEmpty()) icons.add(st.copyWithCount(1));
            }

            float progress = dto.requiredCount() > 0
                    ? Math.min(1.0f, Math.max(0.0f, dto.currentCount() / (float) dto.requiredCount()))
                    : 0.0f;
            if (ready) progress = 1.0f;

            contentW = Math.max(contentW, font.width(name) + 6.0f + countW);
            contentW = Math.max(contentW, font.width(subtitle));
            contentW = Math.max(contentW, icons.size() * ICON_SPACING);
            rows.add(new RequestLayout(name, subtitle, countText, countW, icons, progress, ready));
        }

        int hidden = payload.requests().size() - shown;
        String moreText = hidden > 0 ? ZooTranslations.get("zootycoon.hologram.barrel.more", hidden) : null;
        if (moreText != null) {
            contentW = Math.max(contentW, font.width(moreText));
        }

        float width = Math.max(MIN_WIDTH, contentW + PADDING * 2.0f);
        float height = PADDING + TITLE_H + 3.0f;
        for (RequestLayout row : rows) {
            height += requestHeight(row.icons().isEmpty());
        }
        height -= REQUEST_GAP;
        if (moreText != null) {
            height += LINE_H;
        }
        height += PADDING;
        return new PanelLayout(title, rows, moreText, width, height);
    }

    private static float requestHeight(boolean noIcons) {
        return LINE_H + LINE_H + (noIcons ? 0.0f : ICON_SPACING) + BAR_H + REQUEST_GAP;
    }

    public static void render(WorldRenderContext context) {
        if (ENTRIES.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        long now = System.currentTimeMillis();
        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = context.matrixStack();
        MultiBufferSource buffer = context.consumers();
        ResourceKey<Level> dim = mc.level.dimension();
        boolean drewAny = false;

        Iterator<Map.Entry<BlockPos, Entry>> it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Entry> mapEntry = it.next();
            Entry entry = mapEntry.getValue();

            // No update for a while means the barrel is out of sync range or gone
            if (entry.hideSinceMs < 0L && now - entry.lastUpdateMs > STALE_TIMEOUT_MS) {
                entry.hideSinceMs = now;
            }
            float alpha = Math.min(1.0f, (now - entry.shownSinceMs) / FADE_IN_MS);
            if (entry.hideSinceMs >= 0L) {
                float out = 1.0f - (now - entry.hideSinceMs) / FADE_OUT_MS;
                if (out <= 0.0f) {
                    it.remove();
                    continue;
                }
                alpha = Math.min(alpha, out);
            }
            if (entry.dimension != dim || entry.layout == null) continue;

            BlockPos pos = mapEntry.getKey();
            double wx = pos.getX() + 0.5D;
            double wy = pos.getY() + ANCHOR_Y;
            double wz = pos.getZ() + 0.5D;
            double dist = Math.sqrt(camPos.distanceToSqr(wx, wy, wz));
            if (dist > MAX_DISTANCE) continue;
            if (dist > FADE_START_DISTANCE) {
                alpha *= (float) ((MAX_DISTANCE - dist) / (MAX_DISTANCE - FADE_START_DISTANCE));
            }
            // Font treats near-zero alpha as opaque, so skip nearly invisible frames
            if (alpha <= 0.05f) continue;

            poseStack.pushPose();
            poseStack.translate(wx - camPos.x, wy - camPos.y, wz - camPos.z);
            poseStack.mulPose(camera.rotation());
            poseStack.scale(HOLO_SCALE, -HOLO_SCALE, HOLO_SCALE);
            drawPanel(mc, poseStack, buffer, entry.layout, alpha);
            poseStack.popPose();
            drewAny = true;
        }

        if (drewAny && buffer instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }
    }

    private static void drawPanel(Minecraft mc, PoseStack poseStack, MultiBufferSource buffer, PanelLayout layout, float alpha) {
        Font font = mc.font;
        Matrix4f matrix = poseStack.last().pose();
        int light = LightTexture.FULL_BRIGHT;

        // Bottom-anchored: the panel grows upward from the anchor point
        float minX = -layout.width() / 2.0f;
        float maxX = layout.width() / 2.0f;
        float minY = -layout.height();
        float maxY = 0.0f;
        HologramBarRenderer.drawVanillaTooltipBackground(buffer, matrix, minX, minY, maxX, maxY, light, alpha);

        float leftX = minX + PADDING;
        float rightX = maxX - PADDING;
        float curY = minY + PADDING;

        // Title row with the shared exclamation icon
        HologramBarRenderer.drawTexturedIcon(buffer, matrix, EXCLAMATION_TEXTURE, leftX, curY - 1.0f, 9.0f, 9.0f, light, alpha);
        drawText(font, layout.title(), leftX + 12.0f, curY, COLOR_TITLE, matrix, buffer, light, alpha);
        curY += TITLE_H;

        VertexConsumer bg = buffer.getBuffer(RenderType.textBackground());
        HologramBarRenderer.drawRect(bg, matrix, leftX, curY - 2.0f, rightX, curY - 1.0f, -0.015f,
                HologramBarRenderer.applyAlpha(COLOR_DIVIDER, alpha), light);
        curY += 3.0f;

        for (RequestLayout row : layout.requests()) {
            int accent = row.ready() ? COLOR_READY : COLOR_PROGRESS;

            drawText(font, row.name(), leftX, curY, COLOR_NAME, matrix, buffer, light, alpha);
            drawText(font, row.countText(), rightX - row.countWidth(), curY, accent, matrix, buffer, light, alpha);
            curY += LINE_H;

            drawText(font, row.subtitle(), leftX, curY, COLOR_MUTED, matrix, buffer, light, alpha);
            curY += LINE_H;

            if (!row.icons().isEmpty()) {
                for (int i = 0; i < row.icons().size(); i++) {
                    ItemStack icon = row.icons().get(i);
                    float iconX = leftX + i * ICON_SPACING;
                    BakedModel model = mc.getItemRenderer().getModel(icon, mc.level, null, 0);
                    if (model != null && model.isGui3d()) {
                        HologramBarRenderer.drawBlockIcon3D(mc, buffer, poseStack, icon, iconX, curY, ICON_SIZE, light, alpha);
                    } else {
                        HologramBarRenderer.drawItemIcon2D(mc, buffer, matrix, icon, iconX, curY, ICON_SIZE, light, alpha);
                    }
                }
                curY += ICON_SPACING;
            }

            // Progress bar
            HologramBarRenderer.drawRect(bg, matrix, leftX, curY, rightX, curY + BAR_H, -0.014f,
                    HologramBarRenderer.applyAlpha(COLOR_TRACK, alpha), light);
            float fillW = (rightX - leftX) * row.progress();
            if (fillW > 0.5f) {
                HologramBarRenderer.drawRect(bg, matrix, leftX, curY, leftX + fillW, curY + BAR_H, -0.010f,
                        HologramBarRenderer.applyAlpha(accent, alpha), light);
            }
            curY += BAR_H + REQUEST_GAP;
        }

        if (layout.moreText() != null) {
            drawText(font, layout.moreText(), leftX, curY, COLOR_MUTED, matrix, buffer, light, alpha);
        }
    }

    private static void drawText(Font font, String text, float x, float y, int color, Matrix4f matrix,
                                 MultiBufferSource buffer, int light, float alpha) {
        font.drawInBatch(text, x, y, HologramBarRenderer.applyAlpha(color, alpha), false, matrix, buffer,
                Font.DisplayMode.NORMAL, 0, light);
    }
}
