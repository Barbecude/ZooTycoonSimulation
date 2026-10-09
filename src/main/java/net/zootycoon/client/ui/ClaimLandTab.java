package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.zootycoon.claim.ClaimTier;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.network.ZooNetworkPayloads;

public class ClaimLandTab {
    private final MainManagementScreen parent;
    private ClaimTier selectedTier = ClaimTier.TIER_1_SMALL;

    public ClaimLandTab(MainManagementScreen parent) {
        this.parent = parent;
    }

    public void init(MainManagementScreen screen) {
        int cardWidth = 120;
        int cardHeight = 110;
        int gap = 15;
        int totalWidth = (cardWidth * 4) + (gap * 3);
        int startX = (screen.width - totalWidth) / 2;
        int startY = 70;

        ClaimTier[] tiers = ClaimTier.values();
        for (int i = 0; i < tiers.length; i++) {
            ClaimTier tier = tiers[i];
            int x = startX + i * (cardWidth + gap);
            int btnY = startY + cardHeight - 25;

            screen.addRenderableWidget(Button.builder(Component.literal("Pilih Tier"), b -> {
                this.selectedTier = tier;
            }).bounds(x + 10, btnY, cardWidth - 20, 20).build());
        }

        // Action Button: Confirm Claim Land
        int confirmY = startY + cardHeight + 40;
        screen.addRenderableWidget(Button.builder(Component.literal("Beli Lahan di Posisi Saat Ini (" + selectedTier.getCostInEmeralds() + " ❇)"), b -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ClientPlayNetworking.send(new ZooNetworkPayloads.ClaimLandPayload(
                        mc.player.blockPosition(), selectedTier.name()
                ));
                screen.onClose();
            }
        }).bounds((screen.width - 280) / 2, confirmY, 280, 24).build());
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int cardWidth = 120;
        int cardHeight = 110;
        int gap = 15;
        int totalWidth = (cardWidth * 4) + (gap * 3);
        int startX = (parent.width - totalWidth) / 2;
        int startY = 70;

        graphics.drawCenteredString(parent.getMinecraft().font, "Pilih Ukuran Lahan untuk Kebun Binatang", parent.width / 2, 50, 0xFFFFFF);

        ClaimTier[] tiers = ClaimTier.values();
        Minecraft mc = Minecraft.getInstance();
        int playerEmeralds = mc.player != null ? TransactionHelper.countEmeralds(mc.player) : 0;

        for (int i = 0; i < tiers.length; i++) {
            ClaimTier tier = tiers[i];
            int x = startX + i * (cardWidth + gap);

            boolean isSelected = (tier == selectedTier);
            int bgColor = isSelected ? 0xDD2A4A3A : 0xAA222222;
            int borderColor = isSelected ? 0xFF55FF55 : 0xFF555555;

            // Card background & border
            graphics.fill(x, startY, x + cardWidth, startY + cardHeight, bgColor);
            graphics.renderOutline(x, startY, cardWidth, cardHeight, borderColor);

            // Title
            graphics.drawCenteredString(mc.font, tier.getDisplayName(), x + cardWidth / 2, startY + 8, 0xFFFFFF);

            // Dimensions
            String dim = tier.getWidth() + "x" + tier.getLength() + " (" + tier.getArea() + " m²)";
            graphics.drawCenteredString(mc.font, dim, x + cardWidth / 2, startY + 24, 0xAAAAAA);

            // Price
            String cost = tier.getCostInEmeralds() + " Emeralds";
            int costColor = (playerEmeralds >= tier.getCostInEmeralds()) ? 0x55FF55 : 0xFF5555;
            graphics.drawCenteredString(mc.font, cost, x + cardWidth / 2, startY + 40, costColor);

            // Price per block
            String perUnit = String.format("%.3f Em/blok", tier.getPricePerBlock());
            graphics.drawCenteredString(mc.font, perUnit, x + cardWidth / 2, startY + 56, 0x888888);
        }

        // Instructions
        graphics.drawCenteredString(
                mc.font,
                "Lahan akan dipusatkan pada posisi berdiri Anda dan otomatis dilindungi dari pemain lain.",
                parent.width / 2, startY + cardHeight + 20, 0xDDDDDD
        );
    }
}
