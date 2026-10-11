package net.zootycoon.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ZooGridCard extends AbstractWidget {
    @FunctionalInterface
    public interface OnPress {
        void onPress(ZooGridCard card);
    }

    private final String cardTitle;
    private final String cardSubtitle;
    private final OnPress onPress;
    private net.minecraft.resources.ResourceLocation iconTexture = null;
    private int iconWidth = 32;
    private int iconHeight = 32;
    private int textureWidth = 32;
    private int textureHeight = 32;

    public ZooGridCard(int x, int y, int width, int height,
                       String title, String subtitle,
                       OnPress onPress) {
        super(x, y, width, height, Component.literal(title));
        this.cardTitle = title;
        this.cardSubtitle = subtitle;
        this.onPress = onPress;
    }

    public ZooGridCard(int x, int y, int width, int height,
                       String title,
                       OnPress onPress) {
        this(x, y, width, height, title, null, onPress);
    }

    public ZooGridCard withIcon(net.minecraft.resources.ResourceLocation icon) {
        return withIcon(icon, 32, 32, 32, 32);
    }

    public ZooGridCard withIcon(net.minecraft.resources.ResourceLocation icon, int iconW, int iconH, int texW, int texH) {
        this.iconTexture = icon;
        this.iconWidth = iconW;
        this.iconHeight = iconH;
        this.textureWidth = texW;
        this.textureHeight = texH;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHovered();

        // Essential-Mod Obsidian Slate Aesthetic
        int bgColor = hovered ? 0xF8181F2B : 0xF20F1219;
        int borderColor = hovered ? 0xFFFFFFFF : 0xFF242A38;

        // 1. Card Background Fill
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);

        // 2. 1px Crisp Border (Bright white highlight on hover)
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, borderColor);
        graphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, borderColor);
        graphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, borderColor);
        graphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);

        Minecraft mc = Minecraft.getInstance();

        // 3. Center the Icon, Title and Subtitle vertically
        int titleColor = hovered ? 0xFFFFFFFF : 0xFFE6EDF3;
        if (iconTexture != null) {
            int gap = 12;
            int textH = (cardSubtitle != null && !cardSubtitle.isEmpty()) ? 24 : 10;
            int totalContentH = iconHeight + gap + textH;
            int startContentY = this.getY() + (this.height - totalContentH) / 2;

            int iconX = this.getX() + (this.width - iconWidth) / 2;
            graphics.blit(iconTexture, iconX, startContentY, 0, 0, iconWidth, iconHeight, textureWidth, textureHeight);

            int titleY = startContentY + iconHeight + gap;
            graphics.drawCenteredString(mc.font, Component.literal(cardTitle), this.getX() + (this.width / 2), titleY, titleColor);

            if (cardSubtitle != null && !cardSubtitle.isEmpty()) {
                graphics.drawCenteredString(mc.font, Component.literal(cardSubtitle), this.getX() + (this.width / 2), titleY + 14, 0xFF8B949E);
            }
        } else {
            int totalTextH = (cardSubtitle != null && !cardSubtitle.isEmpty()) ? 26 : 12;
            int centerY = this.getY() + (this.height - totalTextH) / 2;
            graphics.drawCenteredString(mc.font, Component.literal(cardTitle), this.getX() + (this.width / 2), centerY, titleColor);

            if (cardSubtitle != null && !cardSubtitle.isEmpty()) {
                graphics.drawCenteredString(mc.font, Component.literal(cardSubtitle), this.getX() + (this.width / 2), centerY + 14, 0xFF8B949E);
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
