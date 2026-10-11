package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.List;

public class StaffRecruitmentTab {
    private final MainManagementScreen parent;

    public StaffRecruitmentTab(MainManagementScreen parent) {
        this.parent = parent;
    }

    public void init(MainManagementScreen screen) {
        // Request latest staff count from server on opening
        ClientPlayNetworking.send(new ZooNetworkPayloads.RequestStaffSyncPayload());

        int totalWidth = Math.min(screen.width - 40, 720);
        int cardGap = 14;
        int cardWidth = (totalWidth - (cardGap * 2)) / 3;
        int cardHeight = Math.min(screen.height - 100, 240);
        int startX = (screen.width - totalWidth) / 2;
        int startY = 60;

        int btnHeight = 24;
        int btnY = startY + cardHeight - btnHeight - 12;

        // Button 1: Rekrut Kuli Bangunan (15 Emeralds)
        screen.addWidgetPublic(new ZooButton(startX + 12, btnY, cardWidth - 24, btnHeight, Component.literal("Rekrut Kuli (15 Em.)"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("builder"));
        }).asEmerald());

        // Button 2: Rekrut Zookeeper (25 Emeralds)
        screen.addWidgetPublic(new ZooButton(startX + cardWidth + cardGap + 12, btnY, cardWidth - 24, btnHeight, Component.literal("Rekrut Zookeeper (25 Em.)"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("zookeeper"));
        }).asEmerald());

        // Button 3: Rekrut Security (30 Emeralds)
        screen.addWidgetPublic(new ZooButton(startX + (cardWidth + cardGap) * 2 + 12, btnY, cardWidth - 24, btnHeight, Component.literal("Rekrut Security (30 Em.)"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("security"));
        }).asEmerald());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();

        int totalWidth = Math.min(parent.width - 40, 720);
        int cardGap = 14;
        int cardWidth = (totalWidth - (cardGap * 2)) / 3;
        int cardHeight = Math.min(parent.height - 100, 240);
        int startX = (parent.width - totalWidth) / 2;
        int startY = 60;

        // Card 1: Kuli Bangunan (Builder)
        renderStaffCard(
                graphics, mc,
                startX, startY, cardWidth, cardHeight,
                mouseX, mouseY,
                ZooTycoonMod.id("textures/gui/staff/builder_large.png"),
                "KULI BANGUNAN",
                List.of(
                        "Dimiliki: " + ZooTycoonClient.clientBuilders + " Orang",
                        "Aktif Bekerja: " + ZooTycoonClient.clientActiveBuilders + " Orang",
                        "Tersedia: " + Math.max(0, ZooTycoonClient.clientBuilders - ZooTycoonClient.clientActiveBuilders) + " Orang"
                )
        );

        // Card 2: Zookeeper (Penjaga Satwa)
        renderStaffCard(
                graphics, mc,
                startX + cardWidth + cardGap, startY, cardWidth, cardHeight,
                mouseX, mouseY,
                ZooTycoonMod.id("textures/gui/staff/zookeeper_large.png"),
                "ZOOKEEPER",
                List.of(
                        "Dimiliki: " + ZooTycoonClient.clientZookeepers + " Orang",
                        "Tugas: Merawat Satwa Zoo",
                        "Status: Siap Bertugas"
                )
        );

        // Card 3: Petugas Keamanan (Security Guard)
        renderStaffCard(
                graphics, mc,
                startX + (cardWidth + cardGap) * 2, startY, cardWidth, cardHeight,
                mouseX, mouseY,
                ZooTycoonMod.id("textures/gui/staff/security_large.png"),
                "SECURITY",
                List.of(
                        "Dimiliki: " + ZooTycoonClient.clientSecurity + " Orang",
                        "Tugas: Berpatroli & Keamanan",
                        "Status: Bersenjata"
                )
        );
    }

    private void renderStaffCard(
            GuiGraphics graphics, Minecraft mc,
            int x, int y, int width, int height,
            int mouseX, int mouseY,
            ResourceLocation iconTexture,
            String title,
            List<String> stats
    ) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        int bgColor = hovered ? 0xF8141924 : 0xF20F1219;
        int borderColor = hovered ? 0xFFFFFFFF : 0xFF242A38;

        // Card Background Fill
        graphics.fill(x, y, x + width, y + height, bgColor);

        // 1px Border
        graphics.fill(x, y, x + width, y + 1, borderColor);
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        graphics.fill(x, y + 1, x + 1, y + height, borderColor);
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor);

        // Large 100x48 Header Illustration
        int imgW = 100;
        int imgH = 48;
        int imgX = x + (width - imgW) / 2;
        int imgY = y + 14;

        graphics.fill(imgX - 2, imgY - 2, imgX + imgW + 2, imgY + imgH + 2, 0xFF141822);
        graphics.fill(imgX - 2, imgY - 2, imgX + imgW + 2, imgY - 1, 0xFF282E3B);
        graphics.fill(imgX - 2, imgY + imgH + 1, imgX + imgW + 2, imgY + imgH + 2, 0xFF282E3B);
        graphics.fill(imgX - 2, imgY - 2, imgX - 1, imgY + imgH + 2, 0xFF282E3B);
        graphics.fill(imgX + imgW + 1, imgY - 2, imgX + imgW + 2, imgY + imgH + 2, 0xFF282E3B);

        graphics.blit(iconTexture, imgX, imgY, 0, 0, imgW, imgH, imgW, imgH);

        // Title
        int titleColor = hovered ? 0xFFFFFFFF : 0xFFE6EDF3;
        int titleY = imgY + imgH + 10;
        graphics.drawCenteredString(mc.font, Component.literal(title), x + (width / 2), titleY, titleColor);

        // Divider
        int divY = titleY + 14;
        graphics.fill(x + 14, divY, x + width - 14, divY + 1, 0xFF242A38);

        // Stats section
        int statY = divY + 10;
        for (String stat : stats) {
            graphics.drawString(mc.font, Component.literal("• " + stat), x + 18, statY, 0xFFC9D1D9, false);
            statY += 15;
        }
    }
}
