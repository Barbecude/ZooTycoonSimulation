package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.construction.ConstructionSite;
import net.zootycoon.economy.EconomyConfig;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.Map;

public class BuilderInspectionScreen extends Screen {
    private final ConstructionSite site;

    public BuilderInspectionScreen(ConstructionSite site) {
        super(Component.literal("Status Pembangunan: " + site.getSchematic().getName()));
        this.site = site;
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = 320;
        int startX = (this.width - panelWidth) / 2;
        int btnY = this.height - 40;

        // Auto-Buy Missing Materials Button
        Map<Item, Integer> missing = site.getMissingMaterials();
        int missingCost = EconomyConfig.calculateMissingCost(missing);
        String btnText = missing.isEmpty()
                ? "Semua Material Tersedia!"
                : "Beli Sisa Material (" + missingCost + " Emeralds ❇)";

        var buyBtn = Button.builder(Component.literal(btnText), b -> {
            if (!missing.isEmpty()) {
                ClientPlayNetworking.send(new ZooNetworkPayloads.AutoBuyMissingPayload(site.getSiteId()));
                this.onClose();
            }
        }).bounds(startX, btnY, panelWidth, 22).build();

        buyBtn.active = !missing.isEmpty();
        addRenderableWidget(buyBtn);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int panelWidth = 320;
        int panelHeight = this.height - 60;
        int startX = (this.width - panelWidth) / 2;
        int startY = 30;

        // Panel background
        graphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xEE1E1E1E);
        graphics.renderOutline(startX, startY, panelWidth, panelHeight, 0xFF555555);

        // Header Title
        graphics.drawCenteredString(this.font, site.getSchematic().getName(), this.width / 2, startY + 12, 0xFFFFFF);

        // Progress Bar
        float progress = site.getProgressPercentage();
        int barWidth = panelWidth - 40;
        int barHeight = 14;
        int barX = startX + 20;
        int barY = startY + 32;

        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        int filledWidth = (int) (barWidth * (progress / 100.0f));
        graphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFF55FF55);
        graphics.renderOutline(barX, barY, barWidth, barHeight, 0xFF777777);

        String progressStr = String.format("%.1f%% Selesai (%d/%d Blok)", progress, site.getPlacedBlocksCount(), site.getTotalBlocks());
        graphics.drawCenteredString(this.font, progressStr, this.width / 2, barY + 3, 0x000000);

        // Missing materials section
        graphics.drawString(this.font, "Material Yang Dibutuhkan:", startX + 20, startY + 60, 0xFFD700, false);

        int itemY = startY + 76;
        Map<Item, Integer> missing = site.getMissingMaterials();
        if (missing.isEmpty()) {
            graphics.drawString(this.font, "Semua material sudah lengkap. Builder sedang bekerja!", startX + 20, itemY, 0x55FF55, false);
        } else {
            int count = 0;
            for (var entry : missing.entrySet()) {
                if (count >= 7) {
                    graphics.drawString(this.font, "... dan " + (missing.size() - count) + " jenis material lainnya", startX + 20, itemY, 0xAAAAAA, false);
                    break;
                }
                ItemStack sample = new ItemStack(entry.getKey());
                String itemName = sample.getHoverName().getString() + " x" + entry.getValue();
                graphics.renderItem(sample, startX + 20, itemY - 2);
                graphics.drawString(this.font, itemName, startX + 42, itemY + 2, 0xFFFFFF, false);
                itemY += 20;
                count++;
            }
        }

        super.render(graphics, mouseX, mouseY, delta);
    }
}
