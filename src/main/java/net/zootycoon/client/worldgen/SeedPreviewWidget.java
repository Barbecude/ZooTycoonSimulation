package net.zootycoon.client.worldgen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.zootycoon.ZooTycoonMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interactive 2D Biome & Terrain Seed Preview Widget with built-in Randomize Seed,
 * Zoom +/-, Reset Spawn, drag-to-pan, scroll-to-zoom, and English Biome hover tooltip.
 */
public class SeedPreviewWidget extends AbstractWidget implements AutoCloseable {

    private final SeedPreviewController controller;
    private final boolean showToolbarButtons;
    private DynamicTexture dynamicTexture;
    private ResourceLocation textureLocation;

    private boolean draggingMap = false;
    private double dragAccumX = 0.0D;
    private double dragAccumY = 0.0D;

    // Toolbar button bounds (computed during render)
    private int btnRandomX, btnRandomY, btnRandomW, btnRandomH;
    private int btnZoomInX, btnZoomInY, btnZoomInW, btnZoomInH;
    private int btnZoomOutX, btnZoomOutY, btnZoomOutW, btnZoomOutH;
    private int btnSpawnX, btnSpawnY, btnSpawnW, btnSpawnH;
    private int mapX, mapY, mapW, mapH;

    public SeedPreviewWidget(int x, int y, int width, int height, SeedPreviewController controller, boolean showToolbarButtons) {
        super(x, y, width, height, Component.translatable("zootycoon.seed_preview.title"));
        this.controller = controller;
        this.showToolbarButtons = showToolbarButtons;
        initTexture();
    }

    private void initTexture() {
        int size = SeedPreviewController.MAP_SIZE;
        this.dynamicTexture = new DynamicTexture(size, size, false);
        this.textureLocation = Minecraft.getInstance()
                .getTextureManager()
                .register(ZooTycoonMod.MOD_ID + "_seed_preview", this.dynamicTexture);
        uploadPixelsToTexture();
    }

    private void uploadPixelsToTexture() {
        if (this.dynamicTexture == null || this.dynamicTexture.getPixels() == null) {
            return;
        }
        NativeImage img = this.dynamicTexture.getPixels();
        int size = SeedPreviewController.MAP_SIZE;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int argb = this.controller.getPixelArgb(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                img.setPixelRGBA(x, y, abgr);
            }
        }
        this.dynamicTexture.upload();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.controller.consumeTextureDirty()) {
            uploadPixelsToTexture();
        }

        Font font = Minecraft.getInstance().font;
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Outer card background & border
        graphics.fill(x, y, x + w, y + h, 0xE610161D);
        graphics.renderOutline(x, y, w, h, 0xFF3A4B5E);

        int headerH = 18;
        int footerH = showToolbarButtons ? 22 : 14;

        // Header bar
        graphics.fill(x + 1, y + 1, x + w - 1, y + headerH, 0xFF18222D);
        String headerTitle = Component.translatable("zootycoon.seed_preview.title").getString();
        graphics.drawString(font, headerTitle, x + 5, y + 5, 0xFFE2E8F0, false);

        // Fast WorldGen 80% badge on top right of header
        String fastBadge = Component.translatable("zootycoon.seed_preview.fast_badge").getString();
        int badgeW = font.width(fastBadge) + 6;
        int badgeX = x + w - badgeW - 4;
        if (badgeX > x + font.width(headerTitle) + 10) {
            graphics.fill(badgeX, y + 3, badgeX + badgeW, y + headerH - 2, 0xFF143D29);
            graphics.renderOutline(badgeX, y + 3, badgeW, headerH - 5, 0xFF2EA043);
            graphics.drawString(font, fastBadge, badgeX + 3, y + 5, 0xFF7EE787, false);
        }

        // Map viewport bounds
        this.mapX = x + 4;
        this.mapY = y + headerH + 2;
        this.mapW = Math.max(32, w - 8);
        this.mapH = Math.max(32, h - headerH - footerH - 4);

        // Map border & texture blit
        graphics.fill(mapX - 1, mapY - 1, mapX + mapW + 1, mapY + mapH + 1, 0xFF2B3846);
        if (this.textureLocation != null) {
            RenderSystem.enableBlend();
            graphics.blit(
                    this.textureLocation,
                    mapX,
                    mapY,
                    mapW,
                    mapH,
                    0.0F,
                    0.0F,
                    SeedPreviewController.MAP_SIZE,
                    SeedPreviewController.MAP_SIZE,
                    SeedPreviewController.MAP_SIZE,
                    SeedPreviewController.MAP_SIZE
            );
            RenderSystem.disableBlend();
        }

        // Draw Spawn Marker (Crosshair + Diamond) if within map viewport
        BlockPos spawn = controller.getEstimatedSpawn();
        if (spawn != null) {
            int scale = controller.getBlocksPerPixel();
            double relPxX = (spawn.getX() - controller.getCenterX()) / (double) scale + (SeedPreviewController.MAP_SIZE / 2.0D);
            double relPxZ = (spawn.getZ() - controller.getCenterZ()) / (double) scale + (SeedPreviewController.MAP_SIZE / 2.0D);
            int screenSpawnX = mapX + (int) Math.round((relPxX / SeedPreviewController.MAP_SIZE) * mapW);
            int screenSpawnY = mapY + (int) Math.round((relPxZ / SeedPreviewController.MAP_SIZE) * mapH);

            if (screenSpawnX >= mapX + 3 && screenSpawnX <= mapX + mapW - 3
                    && screenSpawnY >= mapY + 3 && screenSpawnY <= mapY + mapH - 3) {
                graphics.fill(screenSpawnX - 4, screenSpawnY - 1, screenSpawnX + 5, screenSpawnY + 2, 0xFF000000);
                graphics.fill(screenSpawnX - 1, screenSpawnY - 4, screenSpawnX + 2, screenSpawnY + 5, 0xFF000000);
                graphics.fill(screenSpawnX - 3, screenSpawnY, screenSpawnX + 4, screenSpawnY + 1, 0xFFFF4444);
                graphics.fill(screenSpawnX, screenSpawnY - 3, screenSpawnX + 1, screenSpawnY + 4, 0xFFFF4444);
                graphics.fill(screenSpawnX - 1, screenSpawnY - 1, screenSpawnX + 2, screenSpawnY + 2, 0xFFFFD700);
            }
        }

        // Subtle top-left overlay inside map: active seed & scale
        String seedText = "Seed: " + controller.getActiveResolvedSeed();
        String trimmedSeed = font.plainSubstrByWidth(seedText, mapW - 8);
        graphics.fill(mapX + 2, mapY + 2, mapX + font.width(trimmedSeed) + 6, mapY + 13, 0xAA0D1117);
        graphics.drawString(font, trimmedSeed, mapX + 4, mapY + 4, 0xFFE6EDF3, false);

        String scaleTag = "1:" + controller.getBlocksPerPixel() + " (" + controller.getActivePresetLabel() + ")";
        String trimmedScale = font.plainSubstrByWidth(scaleTag, mapW - 8);
        int scaleW = font.width(trimmedScale) + 6;
        graphics.fill(mapX + 2, mapY + mapH - 13, mapX + scaleW + 2, mapY + mapH - 2, 0xAA0D1117);
        graphics.drawString(font, trimmedScale, mapX + 4, mapY + mapH - 11, 0xFF9ECBFF, false);

        if (controller.isRendering()) {
            String loadingTag = "...";
            int lw = font.width(loadingTag) + 6;
            graphics.fill(mapX + mapW - lw - 2, mapY + 2, mapX + mapW - 2, mapY + 13, 0xAA0D1117);
            graphics.drawString(font, loadingTag, mapX + mapW - lw + 1, mapY + 4, 0xFFF2CC60, false);
        }

        // Bottom toolbar buttons
        if (showToolbarButtons) {
            int btnY = y + h - footerH + 2;
            int btnH = footerH - 4;
            int availW = w - 8;

            this.btnZoomOutW = 18;
            this.btnZoomInW = 18;
            this.btnSpawnW = 42;
            int gap = 3;
            this.btnRandomW = Math.max(54, availW - btnZoomOutW - btnZoomInW - btnSpawnW - gap * 3);

            this.btnRandomX = x + 4;
            this.btnRandomY = btnY;
            this.btnRandomH = btnH;

            this.btnZoomOutX = btnRandomX + btnRandomW + gap;
            this.btnZoomOutY = btnY;
            this.btnZoomOutH = btnH;

            this.btnZoomInX = btnZoomOutX + btnZoomOutW + gap;
            this.btnZoomInY = btnY;
            this.btnZoomInH = btnH;

            this.btnSpawnX = btnZoomInX + btnZoomInW + gap;
            this.btnSpawnY = btnY;
            this.btnSpawnH = btnH;

            drawMiniButton(graphics, font, btnRandomX, btnRandomY, btnRandomW, btnRandomH,
                    Component.translatable("zootycoon.seed_preview.btn.randomize").getString(),
                    mouseX, mouseY, true);
            drawMiniButton(graphics, font, btnZoomInX, btnZoomInY, btnZoomInW, btnZoomInH, "+", mouseX, mouseY, false);
            drawMiniButton(graphics, font, btnZoomOutX, btnZoomOutY, btnZoomOutW, btnZoomOutH, "-", mouseX, mouseY, false);
            drawMiniButton(graphics, font, btnSpawnX, btnSpawnY, btnSpawnW, btnSpawnH, "Spawn", mouseX, mouseY, false);
        }

        // Hover Tooltip over map showing Biome name in English + Coordinates
        if (isMouseOverMap(mouseX, mouseY)) {
            int texX = Mth.clamp((int) (((mouseX - mapX) / (double) mapW) * SeedPreviewController.MAP_SIZE), 0, SeedPreviewController.MAP_SIZE - 1);
            int texY = Mth.clamp((int) (((mouseY - mapY) / (double) mapH) * SeedPreviewController.MAP_SIZE), 0, SeedPreviewController.MAP_SIZE - 1);

            String biomeName = controller.getBiomeEnglishNameAtPixel(texX, texY);
            int worldX = controller.pixelToBlockX(texX);
            int worldZ = controller.pixelToBlockZ(texY);

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("Biome: " + biomeName));
            tooltip.add(Component.literal("X: " + worldX + ", Z: " + worldZ));
            tooltip.add(Component.translatable("zootycoon.seed_preview.tooltip.controls"));
            graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    private void drawMiniButton(
            GuiGraphics graphics,
            Font font,
            int bx,
            int by,
            int bw,
            int bh,
            String label,
            int mouseX,
            int mouseY,
            boolean accent
    ) {
        boolean hovered = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
        int bg = accent
                ? (hovered ? 0xFF236B3E : 0xFF194D2C)
                : (hovered ? 0xFF2D3C4E : 0xFF1E2936);
        int border = accent
                ? (hovered ? 0xFF4ADE80 : 0xFF2EA043)
                : (hovered ? 0xFF79C0FF : 0xFF44566C);
        graphics.fill(bx, by, bx + bw, by + bh, bg);
        graphics.renderOutline(bx, by, bw, bh, border);
        String fit = font.plainSubstrByWidth(label, bw - 4);
        int tx = bx + (bw - font.width(fit)) / 2;
        int ty = by + (bh - 8) / 2;
        graphics.drawString(font, fit, tx, ty, 0xFFF0F6FC, false);
    }

    private boolean isMouseOverMap(double mouseX, double mouseY) {
        return mouseX >= mapX && mouseX < mapX + mapW && mouseY >= mapY && mouseY < mapY + mapH;
    }

    private boolean isInside(double mouseX, double mouseY, int bx, int by, int bw, int bh) {
        return mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || button != 0) {
            return false;
        }

        if (showToolbarButtons) {
            if (isInside(mouseX, mouseY, btnRandomX, btnRandomY, btnRandomW, btnRandomH)) {
                playClickSound();
                controller.randomizeSeed();
                return true;
            }
            if (isInside(mouseX, mouseY, btnZoomInX, btnZoomInY, btnZoomInW, btnZoomInH)) {
                playClickSound();
                controller.zoomIn();
                return true;
            }
            if (isInside(mouseX, mouseY, btnZoomOutX, btnZoomOutY, btnZoomOutW, btnZoomOutH)) {
                playClickSound();
                controller.zoomOut();
                return true;
            }
            if (isInside(mouseX, mouseY, btnSpawnX, btnSpawnY, btnSpawnW, btnSpawnH)) {
                playClickSound();
                controller.resetToSpawn();
                return true;
            }
        }

        if (isMouseOverMap(mouseX, mouseY)) {
            this.draggingMap = true;
            this.dragAccumX = 0.0D;
            this.dragAccumY = 0.0D;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingMap && button == 0 && mapW > 0 && mapH > 0) {
            double scaleX = (double) SeedPreviewController.MAP_SIZE / mapW;
            double scaleY = (double) SeedPreviewController.MAP_SIZE / mapH;
            this.dragAccumX += dragX * scaleX;
            this.dragAccumY += dragY * scaleY;

            int stepX = (int) this.dragAccumX;
            int stepY = (int) this.dragAccumY;
            if (stepX != 0 || stepY != 0) {
                this.dragAccumX -= stepX;
                this.dragAccumY -= stepY;
                controller.panByPixels(stepX, stepY);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingMap) {
            this.draggingMap = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.visible && isMouseOverMap(mouseX, mouseY)) {
            if (scrollY > 0) {
                controller.zoomIn();
            } else if (scrollY < 0) {
                controller.zoomOut();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }

    @Override
    public void close() {
        if (this.dynamicTexture != null) {
            this.dynamicTexture.close();
            this.dynamicTexture = null;
        }
    }
}
