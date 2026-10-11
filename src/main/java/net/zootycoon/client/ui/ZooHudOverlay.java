package net.zootycoon.client.ui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.zootycoon.client.render.ClaimBoundaryRenderer;
import net.zootycoon.client.render.PlacementPreviewRenderer;
import net.zootycoon.item.ZooItems;
import net.zootycoon.schematic.ZooSchematic;

public class ZooHudOverlay {

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        // 0. Permanent Emerald Balance Indicator on Screen (Top-Right)
        renderPermanentEmeraldHud(graphics, mc);

        // 1. Placement Preview Mode
        ZooSchematic schem = PlacementPreviewRenderer.getActiveSchematic();
        if (schem != null) {
            renderPlacementHud(graphics, mc.font, schem);
            return;
        }

        // 2. Claim Selection Mode
        boolean holdingTool = player.getMainHandItem().is(net.minecraft.world.item.Items.STICK)
                || player.getOffhandItem().is(net.minecraft.world.item.Items.STICK);
        if (holdingTool && ClaimBoundaryRenderer.hasSelection()) {
            renderClaimHud(graphics, mc.font);
        }
    }

    private static final net.minecraft.resources.ResourceLocation EMERALD_ICON = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/emerald.png");

    private static void renderPermanentEmeraldHud(GuiGraphics graphics, Minecraft mc) {
        Player player = mc.player;
        if (player == null) return;

        int emeraldCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(net.minecraft.world.item.Items.EMERALD)) {
                emeraldCount += stack.getCount();
            } else if (stack.is(net.minecraft.world.item.Items.EMERALD_BLOCK)) {
                emeraldCount += stack.getCount() * 9;
            }
        }

        Font font = mc.font;
        String text = String.valueOf(emeraldCount);
        int textW = font.width(text);
        int badgeW = 22 + textW + 10;
        int badgeH = 20;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int x = screenW - badgeW - 12;
        int y = 32; // Moved lower down

        // Sleek Minimalist Pill Card (Essential-mod style)
        graphics.fill(x, y, x + badgeW, y + badgeH, 0xD012151D);
        graphics.fill(x, y, x + badgeW, y + 1, 0xFF2A3140);
        graphics.fill(x, y + badgeH - 1, x + badgeW, y + badgeH, 0xFF2A3140);
        graphics.fill(x, y, x + 1, y + badgeH, 0xFF2A3140);
        graphics.fill(x + badgeW - 1, y, x + badgeW, y + badgeH, 0xFF2A3140);

        // Custom Emerald Icon from Icons v.1.14
        graphics.blit(EMERALD_ICON, x + 5, y + 3, 0, 0, 14, 14, 16, 16);

        // Emerald Count (Only the number)
        graphics.drawString(font, Component.literal(text), x + 23, y + 6, 0xFF55FF55, true);
    }

    private static void renderPlacementHud(GuiGraphics graphics, Font font, ZooSchematic schem) {
        int x = 12;
        int y = 12;
        int width = 230;
        int height = 138;

        // Semi-transparent dark background card & border
        graphics.fill(x, y, x + width, y + height, 0xD010141A);
        graphics.fill(x, y, x + width, y + 1, 0xFF2A2E37);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF2A2E37);
        graphics.fill(x, y, x + 1, y + height, 0xFF2A2E37);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF2A2E37);

        // Header Title
        graphics.drawString(font, Component.literal("PENEMPATAN BANGUNAN"), x + 8, y + 8, 0xFF00E5FF, true);

        // Details
        graphics.drawString(font, Component.literal("Desain: " + schem.getName()), x + 8, y + 22, 0xFFFFFFFF, true);

        boolean insideClaim = PlacementPreviewRenderer.isTargetInsideClaim();
        int statusColor = insideClaim ? 0xFF55FF55 : 0xFFFF5555;
        String statusText = insideClaim ? "Lokasi Valid di Zoo" : "Di Luar Wilayah Zoo";
        graphics.drawString(font, Component.literal("Status: " + statusText), x + 8, y + 34, statusColor, true);

        String rot = PlacementPreviewRenderer.getRotationDegreeString();
        int offX = PlacementPreviewRenderer.getPlacementOffsetX();
        int offY = PlacementPreviewRenderer.getPlacementOffsetY();
        int offZ = PlacementPreviewRenderer.getPlacementOffsetZ();
        String transformText = "Rotasi: " + rot + " | X:" + offX + " Y:" + offY + " Z:" + offZ;
        graphics.drawString(font, Component.literal(transformText), x + 8, y + 46, 0xFFAAAAAA, true);

        // Divider
        graphics.fill(x + 8, y + 59, x + width - 8, y + 60, 0xFF2A2E37);

        // Multi-line Controls Guidance
        graphics.drawString(font, Component.literal("Panduan Kontrol:"), x + 8, y + 64, 0xFFFFD700, true);
        graphics.drawString(font, Component.literal("- Klik Kanan : Bangun"), x + 8, y + 76, 0xFFFFFFFF, true);
        graphics.drawString(font, Component.literal("- R / Shift+Scroll : Putar Bangunan"), x + 8, y + 88, 0xFFFFFFFF, true);
        graphics.drawString(font, Component.literal("- Ctrl+Scroll : Geser X (atau Y jika lihat atas/bawah)"), x + 8, y + 100, 0xFFFFFFFF, true);
        graphics.drawString(font, Component.literal("- Alt+Scroll : Geser Sumbu Z"), x + 8, y + 112, 0xFFFFFFFF, true);
        graphics.drawString(font, Component.literal("- Shift+Klik Kanan : Batalkan"), x + 8, y + 124, 0xFFCCCCCC, true);
    }

    private static void renderClaimHud(GuiGraphics graphics, Font font) {
        var pos1 = ClaimBoundaryRenderer.getPos1();
        var pos2 = ClaimBoundaryRenderer.getPos2();
        var info = ClaimBoundaryRenderer.getSelectionInfo();

        int x = 12;
        int y = 12;
        int width = 230;
        int height = (pos2 != null && info != null) ? 144 : 96;

        graphics.fill(x, y, x + width, y + height, 0xD010141A);
        graphics.fill(x, y, x + width, y + 1, 0xFF2A2E37);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF2A2E37);
        graphics.fill(x, y, x + 1, y + height, 0xFF2A2E37);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF2A2E37);

        graphics.drawString(font, Component.literal("BELI TANAH ZOO"), x + 8, y + 8, 0xFF00FF88, true);
        graphics.drawString(font, Component.literal("Sudut 1: (" + pos1.getX() + ", " + pos1.getZ() + ")"), x + 8, y + 22, 0xFFFFFFFF, true);

        if (pos2 == null || info == null) {
            graphics.drawString(font, Component.literal("Sudut 2: Klik blok lain untuk sudut 2"), x + 8, y + 34, 0xFFFFFF55, true);
            graphics.fill(x + 8, y + 47, x + width - 8, y + 48, 0xFF2A2E37);
            graphics.drawString(font, Component.literal("Panduan Kontrol:"), x + 8, y + 52, 0xFFFFD700, true);
            graphics.drawString(font, Component.literal("- Klik Kiri: Tentukan Sudut 2"), x + 8, y + 64, 0xFFFFFFFF, true);
            graphics.drawString(font, Component.literal("- Shift+Klik Kanan: Batalkan"), x + 8, y + 76, 0xFFCCCCCC, true);
        } else {
            graphics.drawString(font, Component.literal("Sudut 2: (" + (pos2.getX() + ClaimBoundaryRenderer.getClaimOffsetX()) + ", " + (pos2.getZ() + ClaimBoundaryRenderer.getClaimOffsetZ()) + ")"), x + 8, y + 34, 0xFFFFFFFF, true);
            graphics.drawString(font, Component.literal("Ukuran: " + info.width() + "x" + info.length() + " (" + info.totalBlocks() + " blok)"), x + 8, y + 46, 0xFFAAAAAA, true);

            String costStr = (info.cost() == 0)
                    ? "Gratis (Semua sudah dimiliki)"
                    : info.cost() + " Emerald (" + info.newBlocks() + " blok baru)";
            int costCol = (info.cost() == 0) ? 0xFF55FF55 : (info.canAfford() ? 0xFF00E5FF : 0xFFFF5555);
            graphics.drawString(font, Component.literal("Biaya: " + costStr), x + 8, y + 58, costCol, true);

            if (info.conflictOther()) {
                graphics.drawString(font, Component.literal("Status: Tabrakan dengan Zoo Lain!"), x + 8, y + 70, 0xFFFF2222, true);
            }

            // Divider
            graphics.fill(x + 8, y + 83, x + width - 8, y + 84, 0xFF2A2E37);

            graphics.drawString(font, Component.literal("Panduan Kontrol:"), x + 8, y + 88, 0xFFFFD700, true);
            graphics.drawString(font, Component.literal("- Klik Kanan : Konfirmasi Beli Tanah"), x + 8, y + 100, 0xFFFFFFFF, true);
            graphics.drawString(font, Component.literal("- Ctrl/Alt+Scroll : Geser Area"), x + 8, y + 112, 0xFFFFFFFF, true);
            graphics.drawString(font, Component.literal("- Shift+Klik Kanan : Batalkan"), x + 8, y + 124, 0xFFCCCCCC, true);
        }
    }
}

