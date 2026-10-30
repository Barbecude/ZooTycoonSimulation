package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.List;

public class StaffRecruitmentTab {
    private final MainManagementScreen parent;
    private static final ItemStack VANILLA_EMERALD_ITEM = new ItemStack(Items.EMERALD);

    public StaffRecruitmentTab(MainManagementScreen parent) {
        this.parent = parent;
    }

    public void init(MainManagementScreen screen) {
        ClientPlayNetworking.send(new ZooNetworkPayloads.RequestStaffSyncPayload());

        int totalWidth = Math.min(screen.width - 30, 840);
        int cardGap = 10;
        int cardWidth = (totalWidth - (cardGap * 3)) / 4;
        int cardHeight = Math.min(screen.height - 100, 240);
        int startX = (screen.width - totalWidth) / 2;
        int startY = 60;

        int btnHeight = 24;
        int btnY = startY + cardHeight - btnHeight - 12;

        // Button 1: Rekrut Kuli Bangunan
        screen.addWidgetPublic(new ZooButton(startX + 12, btnY, cardWidth - 24, btnHeight, ZooTranslations.component("zootycoon.ui.staff.btn_hire_builder"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("builder"));
        }).asEmerald());

        // Button 2: Rekrut Zookeeper
        screen.addWidgetPublic(new ZooButton(startX + (cardWidth + cardGap) + 12, btnY, cardWidth - 24, btnHeight, ZooTranslations.component("zootycoon.ui.staff.btn_hire_zookeeper"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("zookeeper"));
        }).asEmerald());

        // Button 2b: Kelola Zookeeper (call / fire idle zookeepers)
        screen.addWidgetPublic(new ZooButton(startX + (cardWidth + cardGap) + 12, btnY - 40, cardWidth - 24, 20, ZooTranslations.component("zootycoon.ui.staff.btn_manage_zookeeper"), b -> {
            screen.setView(MainManagementScreen.ViewState.ZOOKEEPER_MANAGE);
        }).asPrimary());

        // Button 3: Rekrut Security
        screen.addWidgetPublic(new ZooButton(startX + (cardWidth + cardGap) * 2 + 12, btnY, cardWidth - 24, btnHeight, ZooTranslations.component("zootycoon.ui.staff.btn_hire_security"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("security"));
        }).asEmerald());

        // Button 4: Rekrut Kasir
        screen.addWidgetPublic(new ZooButton(startX + (cardWidth + cardGap) * 3 + 12, btnY, cardWidth - 24, btnHeight, ZooTranslations.component("zootycoon.ui.staff.btn_hire_cashier"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("cashier"));
        }).asEmerald());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();

        int totalWidth = Math.min(parent.width - 30, 840);
        int cardGap = 10;
        int cardWidth = (totalWidth - (cardGap * 3)) / 4;
        int cardHeight = Math.min(parent.height - 100, 240);
        int startX = (parent.width - totalWidth) / 2;
        int startY = 60;

        // Card 1: Kuli Bangunan (Builder)
        renderStaffCard(
                graphics, mc,
                startX, startY, cardWidth, cardHeight,
                mouseX, mouseY,
                ZooTycoonMod.id("textures/gui/staff/builder_large.png"),
                ZooTranslations.get("zootycoon.ui.staff.builder.title"),
                net.zootycoon.economy.ZooEconomyEngine.getStaffHiringCost("builder"),
                List.of(
                        ZooTranslations.get("zootycoon.ui.staff.builder.stat1", ZooTycoonClient.clientBuilders),
                        ZooTranslations.get("zootycoon.ui.staff.builder.stat2", ZooTycoonClient.clientActiveBuilders),
                        ZooTranslations.get("zootycoon.ui.staff.builder.stat3", Math.max(0, ZooTycoonClient.clientBuilders - ZooTycoonClient.clientActiveBuilders))
                )
        );

        // Card 2: Zookeeper (Penjaga Satwa)
        renderStaffCard(
                graphics, mc,
                startX + cardWidth + cardGap, startY, cardWidth, cardHeight,
                mouseX, mouseY,
                ZooTycoonMod.id("textures/gui/staff/zookeeper_large.png"),
                ZooTranslations.get("zootycoon.ui.staff.zookeeper.title"),
                net.zootycoon.economy.ZooEconomyEngine.getStaffHiringCost("zookeeper"),
                List.of(
                        ZooTranslations.get("zootycoon.ui.staff.zookeeper.stat1", ZooTycoonClient.clientZookeepers),
                        ZooTranslations.get("zootycoon.ui.staff.zookeeper.stat2"),
                        ZooTranslations.get("zootycoon.ui.staff.zookeeper.stat3")
                )
        );

        // Card 3: Petugas Keamanan (Security Guard)
        renderStaffCard(
                graphics, mc,
                startX + (cardWidth + cardGap) * 2, startY, cardWidth, cardHeight,
                mouseX, mouseY,
                ZooTycoonMod.id("textures/gui/staff/security_large.png"),
                ZooTranslations.get("zootycoon.ui.staff.security.title"),
                net.zootycoon.economy.ZooEconomyEngine.getStaffHiringCost("security"),
                List.of(
                        ZooTranslations.get("zootycoon.ui.staff.security.stat1", ZooTycoonClient.clientSecurity),
                        ZooTranslations.get("zootycoon.ui.staff.security.stat2"),
                        ZooTranslations.get("zootycoon.ui.staff.security.stat3")
                )
        );

        // Card 4: Kasir (Cashier)
        renderStaffCard(
                graphics, mc,
                startX + (cardWidth + cardGap) * 3, startY, cardWidth, cardHeight,
                mouseX, mouseY,
                ZooTycoonMod.id("textures/gui/staff/cashier_large.png"),
                ZooTranslations.get("zootycoon.ui.staff.cashier.title"),
                net.zootycoon.economy.ZooEconomyEngine.getStaffHiringCost("cashier"),
                List.of(
                        ZooTranslations.get("zootycoon.ui.staff.cashier.stat1", ZooTycoonClient.clientCashiers),
                        ZooTranslations.get("zootycoon.ui.staff.cashier.stat2"),
                        ZooTranslations.get("zootycoon.ui.staff.cashier.stat3")
                )
        );
    }

    private void renderStaffCard(
            GuiGraphics graphics, Minecraft mc,
            int x, int y, int width, int height,
            int mouseX, int mouseY,
            ResourceLocation iconTexture,
            String title,
            int cost,
            List<String> stats
    ) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        int bgColor = hovered ? 0xF8141924 : 0xF20F1219;
        int borderColor = hovered ? 0xFFFFFFFF : 0xFF242A38;

        graphics.fill(x, y, x + width, y + height, bgColor);

        graphics.fill(x, y, x + width, y + 1, borderColor);
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        graphics.fill(x, y + 1, x + 1, y + height, borderColor);
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor);

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

        int titleColor = hovered ? 0xFFFFFFFF : 0xFFE6EDF3;
        int titleY = imgY + imgH + 10;
        graphics.drawCenteredString(mc.font, Component.literal(title), x + (width / 2), titleY, titleColor);

        int divY = titleY + 14;
        graphics.fill(x + 14, divY, x + width - 14, divY + 1, 0xFF242A38);

        int statY = divY + 8;
        for (String stat : stats) {
            graphics.drawString(mc.font, Component.literal("• " + stat), x + 18, statY, 0xFFC9D1D9, false);
            statY += 14;
        }

        int costY = y + height - 48;
        String costText = String.valueOf(cost);
        int costTextW = mc.font.width(costText);
        int badgeTotalW = 16 + 4 + costTextW;
        int badgeStartX = x + (width - badgeTotalW) / 2;

        graphics.renderItem(VANILLA_EMERALD_ITEM, badgeStartX, costY - 4);
        graphics.drawString(mc.font, Component.literal(costText), badgeStartX + 18, costY, 0xFF55FF55, true);
    }
}
