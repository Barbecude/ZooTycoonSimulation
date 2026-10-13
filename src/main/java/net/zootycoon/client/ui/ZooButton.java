package net.zootycoon.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ZooButton extends AbstractWidget {

    @FunctionalInterface
    public interface OnPress {
        void onPress(ZooButton button);
    }

    private final OnPress onPress;
    private boolean selected = false;
    private boolean leftAligned = false;
    private net.minecraft.resources.ResourceLocation customIcon = null;
    private int customIconW = 0;
    private int customIconH = 0;
    private int customTexW = 0;
    private int customTexH = 0;
    private ItemStack itemIcon = ItemStack.EMPTY;

    // Palette Colors (Essential-Mod Minimalist Slate Aesthetic)
    private int bgColor = 0xFF14171F;
    private int hoverBgColor = 0xFF202633;
    private int selectedBgColor = 0xFF1C2536;
    private int disabledBgColor = 0xFF101217;

    private int borderColor = 0xFF282E3B;
    private int hoverBorderColor = 0xFFFFFFFF; // Crisp white highlight on hover (Essential style)
    private int selectedBorderColor = 0xFF388BFD;
    private int disabledBorderColor = 0xFF1C1F26;

    private int textColor = 0xFFD0D7DE;
    private int hoverTextColor = 0xFFFFFFFF;
    private int selectedTextColor = 0xFFFFFFFF;
    private int disabledTextColor = 0xFF586069;

    public ZooButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    public ZooButton setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public boolean isSelected() {
        return selected;
    }

    public ZooButton setLeftAligned(boolean leftAligned) {
        this.leftAligned = leftAligned;
        return this;
    }

    public ZooButton setCustomIcon(net.minecraft.resources.ResourceLocation icon) {
        return setCustomIcon(icon, 16, 16, 16, 16);
    }

    public ZooButton setCustomIcon(net.minecraft.resources.ResourceLocation icon, int w, int h) {
        return setCustomIcon(icon, w, h, w, h);
    }

    public ZooButton setCustomIcon(net.minecraft.resources.ResourceLocation icon, int w, int h, int texW, int texH) {
        this.customIcon = icon;
        this.customIconW = w;
        this.customIconH = h;
        this.customTexW = texW;
        this.customTexH = texH;
        return this;
    }

    public ZooButton setItemIcon(ItemStack stack) {
        this.itemIcon = stack != null ? stack : ItemStack.EMPTY;
        return this;
    }

    public ZooButton setAccentColor(int normalBorder, int hoverBorder, int hoverText) {
        this.borderColor = normalBorder;
        this.hoverBorderColor = hoverBorder;
        this.hoverTextColor = hoverText;
        return this;
    }

    public ZooButton asPrimary() {
        this.borderColor = 0xFF3A4456;
        this.hoverBorderColor = 0xFFFFFFFF;
        this.hoverBgColor = 0xFF252D3D;
        this.hoverTextColor = 0xFFFFFFFF;
        return this;
    }

    public ZooButton asEmerald() {
        // Essential-Mod slate with clean white border hover
        this.borderColor = 0xFF3A4456;
        this.hoverBorderColor = 0xFFFFFFFF;
        this.hoverBgColor = 0xFF252D3D;
        this.hoverTextColor = 0xFFFFFFFF;
        return this;
    }

    public ZooButton asGold() {
        // Essential-Mod slate with clean white border hover
        this.borderColor = 0xFF3A4456;
        this.hoverBorderColor = 0xFFFFFFFF;
        this.hoverBgColor = 0xFF252D3D;
        this.hoverTextColor = 0xFFFFFFFF;
        return this;
    }

    public ZooButton asDanger() {
        this.borderColor = 0xFF4A2528;
        this.hoverBorderColor = 0xFFE06C75;
        this.hoverBgColor = 0xFF2B181B;
        this.hoverTextColor = 0xFFFFFFFF;
        return this;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        boolean hovered = isHoveredOrFocused();

        int bg = !this.active ? disabledBgColor : (selected ? (hovered ? 0xFF24324C : selectedBgColor) : (hovered ? hoverBgColor : bgColor));
        int border = !this.active ? disabledBorderColor : (selected ? selectedBorderColor : (hovered ? hoverBorderColor : borderColor));
        int text = !this.active ? disabledTextColor : (selected ? selectedTextColor : (hovered ? hoverTextColor : textColor));

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // 1. Render Background
        graphics.fill(x, y, x + w, y + h, bg);

        // 2. Render 1px Border
        graphics.fill(x, y, x + w, y + 1, border);
        graphics.fill(x, y + h - 1, x + w, y + h, border);
        graphics.fill(x, y, x + 1, y + h, border);
        graphics.fill(x + w - 1, y, x + w, y + h, border);
        if (selected && this.active) {
            graphics.fill(x + 1, y + h - 2, x + w - 1, y + h, 0xFF388BFD);
        }

        // 3. Optional Custom Icon or Item Icon
        Component msg = this.getMessage();
        int textY = y + (h - 8) / 2;

        if (!itemIcon.isEmpty()) {
            int iW = 16;
            int iH = 16;
            int iconY = y + (h - iH) / 2;
            if (leftAligned) {
                graphics.renderFakeItem(itemIcon, x + 6, iconY);
                graphics.drawString(mc.font, msg, x + 6 + iW + 4, textY, text, false);
            } else {
                int textW = mc.font.width(msg);
                int totalW = iW + 5 + textW;
                int startX = x + (w - totalW) / 2;
                graphics.renderFakeItem(itemIcon, startX, iconY);
                graphics.drawString(mc.font, msg, startX + iW + 5, textY, text, false);
            }
        } else if (customIcon != null) {
            int iW = customIconW > 0 ? customIconW : 16;
            int iH = customIconH > 0 ? customIconH : 16;
            int tW = customTexW > 0 ? customTexW : iW;
            int tH = customTexH > 0 ? customTexH : iH;
            int iconY = y + (h - iH) / 2;
            if (leftAligned) {
                graphics.blit(customIcon, x + 6, iconY, 0, 0, iW, iH, tW, tH);
                graphics.drawString(mc.font, msg, x + 6 + iW + 4, textY, text, false);
            } else {
                int textW = mc.font.width(msg);
                int totalW = iW + 5 + textW;
                int startX = x + (w - totalW) / 2;
                graphics.blit(customIcon, startX, iconY, 0, 0, iW, iH, tW, tH);
                graphics.drawString(mc.font, msg, startX + iW + 5, textY, text, false);
            }
        } else {
            if (leftAligned) {
                graphics.drawString(mc.font, msg, x + 8, textY, text, false);
            } else {
                int textW = mc.font.width(msg);
                int textX = x + (w - textW) / 2;
                graphics.drawString(mc.font, msg, textX, textY, text, false);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}

