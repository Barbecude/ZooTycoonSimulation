package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.List;

public class StaffRecruitmentTab {
    private final MainManagementScreen parent;
    private static final ItemStack VANILLA_EMERALD_ITEM = new ItemStack(Items.EMERALD);

    private boolean isDropdownOpen = false;
    private ZooButton dropdownBtn;
    private int dropdownX;
    private int dropdownY;
    private int dropdownW;
    private int dropdownH;

    public StaffRecruitmentTab(MainManagementScreen parent) {
        this.parent = parent;
    }

    public void init(MainManagementScreen screen) {
        ClientPlayNetworking.send(new ZooNetworkPayloads.RequestStaffSyncPayload());
        this.isDropdownOpen = false;

        // 1. Dropdown Button at top right
        int emeralds = Minecraft.getInstance().player != null ? TransactionHelper.countEmeralds(Minecraft.getInstance().player) : 0;
        String balanceStr = String.valueOf(emeralds);
        int textW = screen.getFont().width(balanceStr);
        int pillW = 22 + textW + 10;
        int pillX = screen.width - pillW - 25;

        this.dropdownW = 118;
        this.dropdownH = 22;
        this.dropdownX = pillX - dropdownW - 8;
        this.dropdownY = 14;

        this.dropdownBtn = new ZooButton(dropdownX, dropdownY, dropdownW, dropdownH,
                ZooTranslations.component("zootycoon.ui.staff.btn_recruit_dropdown"), b -> {
            isDropdownOpen = !isDropdownOpen;
        }).asEmerald();
        screen.addWidgetPublic(this.dropdownBtn);

        // 2. 4 Staff Management Cards
        int totalWidth = Math.min(screen.width - 30, 840);
        int cardGap = 10;
        int cardWidth = (totalWidth - (cardGap * 3)) / 4;
        int cardHeight = Math.min(screen.height - 100, 240);
        int startX = (screen.width - totalWidth) / 2;
        int startY = 60;

        int btnHeight = 24;
        int btnY = startY + cardHeight - btnHeight - 12;

        // Card 1: Kelola Kuli Bangunan
        screen.addWidgetPublic(new ZooButton(startX + 12, btnY, cardWidth - 24, btnHeight,
                ZooTranslations.component("zootycoon.ui.staff.btn_manage_builder"), b -> {
            screen.setView(MainManagementScreen.ViewState.BUILDER_MANAGE);
        }).asPrimary());

        // Card 2: Kelola Zookeeper
        screen.addWidgetPublic(new ZooButton(startX + (cardWidth + cardGap) + 12, btnY, cardWidth - 24, btnHeight,
                ZooTranslations.component("zootycoon.ui.staff.btn_manage_zookeeper"), b -> {
            screen.setView(MainManagementScreen.ViewState.ZOOKEEPER_MANAGE);
        }).asPrimary());

        // Card 3: Kelola Security
        screen.addWidgetPublic(new ZooButton(startX + (cardWidth + cardGap) * 2 + 12, btnY, cardWidth - 24, btnHeight,
                ZooTranslations.component("zootycoon.ui.staff.btn_manage_security"), b -> {
            screen.setView(MainManagementScreen.ViewState.SECURITY_MANAGE);
        }).asPrimary());

        // Card 4: Kelola Kasir Zoo
        screen.addWidgetPublic(new ZooButton(startX + (cardWidth + cardGap) * 3 + 12, btnY, cardWidth - 24, btnHeight,
                ZooTranslations.component("zootycoon.ui.staff.btn_manage_cashier"), b -> {
            screen.setView(MainManagementScreen.ViewState.CASHIER_MANAGE);
        }).asPrimary());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isDropdownOpen && button == 0) {
            int itemH = 26;
            int menuW = 156;
            int menuH = 4 * itemH + 8;
            int menuX = dropdownX;
            int menuY = dropdownY + dropdownH + 4;

            if (mouseX >= menuX && mouseX <= menuX + menuW && mouseY >= menuY && mouseY <= menuY + menuH) {
                String[] roles = {"builder", "zookeeper", "security", "cashier"};
                int clickedIdx = (int) ((mouseY - (menuY + 4)) / itemH);
                if (clickedIdx >= 0 && clickedIdx < roles.length) {
                    Minecraft mc = Minecraft.getInstance();
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload(roles[clickedIdx]));
                    isDropdownOpen = false;
                    return true;
                }
            } else if (dropdownBtn != null && dropdownBtn.isMouseOver(mouseX, mouseY)) {
                return false;
            } else {
                // Consume the closing click so it does not reach widgets under the menu
                isDropdownOpen = false;
                return true;
            }
        }
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
                List.of(
                        ZooTranslations.get("zootycoon.ui.staff.cashier.stat1", ZooTycoonClient.clientCashiers),
                        ZooTranslations.get("zootycoon.ui.staff.cashier.stat2"),
                        ZooTranslations.get("zootycoon.ui.staff.cashier.stat3")
                )
        );
    }

    // Called by MainManagementScreen after widgets are drawn
    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (isDropdownOpen) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            renderDropdownMenu(graphics, Minecraft.getInstance(), mouseX, mouseY);
            graphics.pose().popPose();
        }
    }

    private void renderDropdownMenu(GuiGraphics graphics, Minecraft mc, int mouseX, int mouseY) {
        int itemH = 26;
        int menuW = 156;
        int menuH = 4 * itemH + 8;
        int menuX = dropdownX;
        int menuY = dropdownY + dropdownH + 4;

        graphics.fill(menuX, menuY, menuX + menuW, menuY + menuH, 0xF80F1219);
        graphics.fill(menuX, menuY, menuX + menuW, menuY + 1, 0xFF3A4456);
        graphics.fill(menuX, menuY + menuH - 1, menuX + menuW, menuY + menuH, 0xFF3A4456);
        graphics.fill(menuX, menuY, menuX + 1, menuY + menuH, 0xFF3A4456);
        graphics.fill(menuX + menuW - 1, menuY, menuX + menuW, menuY + menuH, 0xFF3A4456);

        String[] roles = {"builder", "zookeeper", "security", "cashier"};
        String[] titles = {
                ZooTranslations.get("zootycoon.ui.staff.builder.title"),
                ZooTranslations.get("zootycoon.ui.staff.zookeeper.title"),
                ZooTranslations.get("zootycoon.ui.staff.security.title"),
                ZooTranslations.get("zootycoon.ui.staff.cashier.title")
        };

        for (int i = 0; i < roles.length; i++) {
            int iy = menuY + 4 + i * itemH;
            boolean hovered = mouseX >= menuX && mouseX <= menuX + menuW && mouseY >= iy && mouseY < iy + itemH;
            if (hovered) {
                graphics.fill(menuX + 2, iy, menuX + menuW - 2, iy + itemH, 0xFF242C3D);
            }

            // Staff title
            graphics.drawString(mc.font, Component.literal(titles[i]), menuX + 8, iy + (itemH - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFE6EDF3, false);

            // Emerald icon + Cost
            int cost = net.zootycoon.economy.ZooEconomyEngine.getStaffHiringCost(roles[i]);
            String costStr = String.valueOf(cost);
            int costW = mc.font.width(costStr);
            int costX = menuX + menuW - 8 - costW;
            int iconX = costX - 18;
            graphics.renderItem(VANILLA_EMERALD_ITEM, iconX, iy + (itemH - 16) / 2);
            graphics.drawString(mc.font, Component.literal(costStr), costX, iy + (itemH - 8) / 2, 0xFF55FF55, true);
        }
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

        int statY = divY + 10;
        for (String stat : stats) {
            graphics.drawString(mc.font, Component.literal("• " + stat), x + 18, statY, 0xFFC9D1D9, false);
            statY += 15;
        }
    }
}
