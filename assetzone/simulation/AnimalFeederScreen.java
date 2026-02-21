package com.example.simulation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import java.util.Objects;

public class AnimalFeederScreen extends AbstractContainerScreen<AnimalFeederMenu> {
    private static final ResourceLocation TEXTURE = Objects.requireNonNull(
            ResourceLocation.tryParse("animal_feeding_trough:textures/gui/container/feeding_trough.png"));

    public AnimalFeederScreen(AnimalFeederMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderSideInfo(guiGraphics);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    private void renderSideInfo(GuiGraphics guiGraphics) {
        int panelX = Math.max(6, this.leftPos - 118);
        int panelY = this.topPos + 10;

        guiGraphics.fill(panelX - 4, panelY - 4, panelX + 108, panelY + 50, 0xA0000000);
        guiGraphics.drawString(font, "Animal Feeder", panelX, panelY, 0xFFE6D2A2, false);
        guiGraphics.drawString(font, "Mode: Universal", panelX, panelY + 12, 0xFF55AA55, false);
        guiGraphics.drawString(font, "Food: " + menu.getDisplayFoodName(), panelX, panelY + 24, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, "Stock: " + menu.getFoodCount() + "/64", panelX, panelY + 36, 0xFFAAAAAA, false);
    }
}
