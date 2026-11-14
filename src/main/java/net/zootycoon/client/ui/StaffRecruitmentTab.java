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
import net.zootycoon.economy.ZooEconomyEngine;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class StaffRecruitmentTab {
    private final MainManagementScreen parent;
    public static final ItemStack VANILLA_EMERALD_ITEM = new ItemStack(Items.EMERALD);
    public static final ResourceLocation EXCLAMATION_ICON = ZooTycoonMod.id("textures/gui/icons/exclamation_point.png");

    public StaffRecruitmentTab(MainManagementScreen parent) {
        this.parent = parent;
    }

    public void init(MainManagementScreen screen) {
        ClientPlayNetworking.send(new ZooNetworkPayloads.RequestStaffSyncPayload());
        ClientPlayNetworking.send(new ZooNetworkPayloads.RequestAnimalSyncPayload());

        // 1. 4 Staff Management Cards
        int totalWidth = Math.min(screen.width - 30, 840);
        int cardGap = 10;
        int cardWidth = (totalWidth - (cardGap * 3)) / 4;
        int cardHeight = Math.min(screen.height - 100, 240);
        int startX = (screen.width - totalWidth) / 2;
        int startY = 60;

        int btnHeight = 20;
        int btnGap = 4;
        int kelolaY = startY + cardHeight - (btnHeight * 2 + btnGap) - 10;
        int rekrutY = kelolaY + btnHeight + btnGap;
        int btnW = cardWidth - 20;

        // Card 1: Kelola Kuli Bangunan
        int c1X = startX + 10;
        screen.addWidgetPublic(new ZooButton(c1X, kelolaY, btnW, btnHeight,
                ZooTranslations.component("zootycoon.ui.staff.btn_manage_builder"), b -> {
            screen.setView(MainManagementScreen.ViewState.BUILDER_MANAGE);
        }).asPrimary());

        int builderCost = net.zootycoon.economy.ZooEconomyEngine.getStaffHiringCost("builder");
        screen.addWidgetPublic(new ZooButton(c1X, rekrutY, btnW, btnHeight,
                Component.literal("Rekrut " + builderCost), b -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("builder"));
        }).setItemIcon(VANILLA_EMERALD_ITEM).setIconAfter(true).asEmerald());

        // Card 2: Kelola Zookeeper
        int c2X = startX + (cardWidth + cardGap) + 10;
        screen.addWidgetPublic(new ZooButton(c2X, kelolaY, btnW, btnHeight,
                ZooTranslations.component("zootycoon.ui.staff.btn_manage_zookeeper"), b -> {
            screen.setView(MainManagementScreen.ViewState.ZOOKEEPER_MANAGE);
        }).asPrimary());

        int zkCost = net.zootycoon.economy.ZooEconomyEngine.getStaffHiringCost("zookeeper");
        screen.addWidgetPublic(new ZooButton(c2X, rekrutY, btnW, btnHeight,
                Component.literal("Rekrut " + zkCost), b -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("zookeeper"));
        }).setItemIcon(VANILLA_EMERALD_ITEM).setIconAfter(true).asEmerald());

        // Card 3: Kelola Security
        int c3X = startX + (cardWidth + cardGap) * 2 + 10;
        screen.addWidgetPublic(new ZooButton(c3X, kelolaY, btnW, btnHeight,
                ZooTranslations.component("zootycoon.ui.staff.btn_manage_security"), b -> {
            screen.setView(MainManagementScreen.ViewState.SECURITY_MANAGE);
        }).asPrimary());

        int secCost = net.zootycoon.economy.ZooEconomyEngine.getStaffHiringCost("security");
        screen.addWidgetPublic(new ZooButton(c3X, rekrutY, btnW, btnHeight,
                Component.literal("Rekrut " + secCost), b -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("security"));
        }).setItemIcon(VANILLA_EMERALD_ITEM).setIconAfter(true).asEmerald());

        // Card 4: Kelola Kasir Zoo
        int c4X = startX + (cardWidth + cardGap) * 3 + 10;
        screen.addWidgetPublic(new ZooButton(c4X, kelolaY, btnW, btnHeight,
                ZooTranslations.component("zootycoon.ui.staff.btn_manage_cashier"), b -> {
            screen.setView(MainManagementScreen.ViewState.CASHIER_MANAGE);
        }).asPrimary());

        int cashierCost = net.zootycoon.economy.ZooEconomyEngine.getStaffHiringCost("cashier");
        screen.addWidgetPublic(new ZooButton(c4X, rekrutY, btnW, btnHeight,
                Component.literal("Rekrut " + cashierCost), b -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("cashier"));
        }).setItemIcon(VANILLA_EMERALD_ITEM).setIconAfter(true).asEmerald());
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
                "builder",
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
                "zookeeper",
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
                "security",
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
                "cashier",
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
    }

    private static List<String> getStrikingNames(String staffType) {
        List<String> names = new ArrayList<>();
        if (staffType == null) return names;
        switch (staffType.toLowerCase()) {
            case "builder" -> {
                // Kuli bangunan tidak ikut mogok gaji harian
            }
            case "zookeeper" -> {
                for (var zk : ZooTycoonClient.clientZookeeperList) {
                    if (zk.isOnStrike()) names.add(zk.name());
                }
            }
            case "security" -> {
                for (var s : ZooTycoonClient.clientSecurityList) {
                    if (s.isOnStrike()) names.add(s.name());
                }
            }
            case "cashier" -> {
                for (var c : ZooTycoonClient.clientCashierList) {
                    if (c.isOnStrike()) names.add(c.name());
                }
            }
        }
        return names;
    }

    private void renderStaffCard(
            GuiGraphics graphics, Minecraft mc,
            int x, int y, int width, int height,
            int mouseX, int mouseY,
            ResourceLocation iconTexture,
            String staffType,
            String title,
            List<String> stats
    ) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        List<String> strikingNames = getStrikingNames(staffType);
        boolean hasStrike = !strikingNames.isEmpty();

        int bgColor = hovered ? 0xF8141924 : 0xF20F1219;
        int borderColor = hovered ? 0xFFFFFFFF : (hasStrike ? 0xFFE06C75 : 0xFF242A38);

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

        if (hasStrike) {
            int exX = x + width - 18;
            int exY = y + 6;
            graphics.blit(EXCLAMATION_ICON, exX, exY, 12, 12, 0, 0, 64, 64, 64, 64);
            if (mouseX >= exX - 2 && mouseX <= exX + 14 && mouseY >= exY - 2 && mouseY <= exY + 14) {
                graphics.renderTooltip(mc.font,
                        Component.literal(ZooTranslations.get("zootycoon.ui.staff.strike_tooltip", String.join(", ", strikingNames)))
                                .withStyle(ChatFormatting.RED),
                        mouseX, mouseY);
            }
        }

        int titleColor = hovered ? 0xFFFFFFFF : 0xFFE6EDF3;
        int titleY = imgY + imgH + 10;
        graphics.drawCenteredString(mc.font, Component.literal(title), x + (width / 2), titleY, titleColor);

        int divY = titleY + 14;
        graphics.fill(x + 14, divY, x + width - 14, divY + 1, 0xFF242A38);

        int statY = divY + 10;
        for (String stat : stats) {
            graphics.drawString(mc.font, Component.literal("• " + stat), x + 18, statY, 0xFFC9D1D9, false);
            statY += 13;
        }
        renderStaffWageLine(graphics, mc, x + 18, statY, staffType, true);
    }

    /** Renders an exclamation mark icon inside an individual striking staff row. */
    public static void renderStrikeRowIcon(GuiGraphics graphics, int x, int y) {
        graphics.blit(EXCLAMATION_ICON, x, y, 10, 10, 0, 0, 64, 64, 64, 64);
    }

    /** Formats salary value text (never including the word "Emerald") for the given staff role. */
    public static String getWageValueText(String staffType) {
        int dailyWage = ZooEconomyEngine.getStaffDailyWage(staffType);
        return dailyWage > 0
                ? String.valueOf(dailyWage)
                : ZooTranslations.get("zootycoon.ui.staff.wage_per_project");
    }

    /** Renders the salary label followed by an emerald item icon and numeric/project value. */
    public static void renderStaffWageLine(GuiGraphics graphics, Minecraft mc, int x, int y, String staffType, boolean bullet) {
        int dailyWage = ZooEconomyEngine.getStaffDailyWage(staffType);
        String labelKey = dailyWage > 0 ? "zootycoon.ui.staff.wage_daily_label" : "zootycoon.ui.staff.wage_label";
        String prefix = (bullet ? "• " : "") + ZooTranslations.get(labelKey);
        String valueText = getWageValueText(staffType);

        graphics.drawString(mc.font, Component.literal(prefix), x, y, 0xFFC9D1D9, false);
        int iconX = x + mc.font.width(prefix) - 1;
        graphics.renderItem(VANILLA_EMERALD_ITEM, iconX, y - 4);
        graphics.drawString(mc.font, Component.literal(valueText), iconX + 14, y, 0xFF55FF55, false);
    }

    /** Right-aligns the salary line against {@code rightX} for manage sub-tab headers. */
    public static void renderWageRightAligned(GuiGraphics graphics, Minecraft mc, int rightX, int y, String staffType) {
        int dailyWage = ZooEconomyEngine.getStaffDailyWage(staffType);
        String labelKey = dailyWage > 0 ? "zootycoon.ui.staff.wage_daily_label" : "zootycoon.ui.staff.wage_label";
        String prefix = ZooTranslations.get(labelKey);
        String valueText = getWageValueText(staffType);
        int totalW = mc.font.width(prefix) + 13 + mc.font.width(valueText);
        renderStaffWageLine(graphics, mc, rightX - totalW, y, staffType, false);
    }
}
