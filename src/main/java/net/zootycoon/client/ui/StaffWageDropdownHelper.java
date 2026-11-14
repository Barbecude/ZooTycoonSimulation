package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.UUID;

/** Shared UI helper for individual staff salary indicator buttons and floating z-index payment dropdown. */
public class StaffWageDropdownHelper {
    public static final int BADGE_W = 84;
    private static final int BTN_H = 18;
    private static final int MENU_W = 196;
    private static final int MENU_H = 74;
    private static final ItemStack EMERALD_ICON = new ItemStack(Items.EMERALD);

    private UUID openStaffUuid = null;
    private int anchorX;
    private int anchorY;
    private int openDailyWage;
    private int openMissedDays;
    private int openOwedAmount;
    private boolean openIsResigned;

    public void close() {
        openStaffUuid = null;
    }

    public boolean isOpen() {
        return openStaffUuid != null;
    }

    public void drawIndicatorButton(
            GuiGraphics graphics, Minecraft mc, UUID staffUuid,
            int x, int y, int dailyWage, int missedDays, int owedAmount, boolean isResigned,
            int mouseX, int mouseY, int listTop, int listBottom
    ) {
        if (staffUuid.equals(openStaffUuid)) {
            this.anchorX = x;
            this.anchorY = y;
            this.openDailyWage = dailyWage;
            this.openMissedDays = missedDays;
            this.openOwedAmount = owedAmount;
            this.openIsResigned = isResigned;
        }

        boolean hovered = mouseX >= x && mouseX <= x + BADGE_W && mouseY >= y && mouseY <= y + BTN_H
                && mouseY >= listTop && mouseY <= listBottom;

        int bg;
        int border;
        int textCol;
        if (owedAmount <= 0) {
            bg = hovered ? 0xFF1E2532 : 0xFF14171F;
            border = hovered ? 0xFF6B778D : 0xFF3A4456;
            textCol = 0xFF55FF55;
        } else if (missedDays >= 3 || isResigned) {
            bg = hovered ? 0xFF34181C : 0xFF221318;
            border = hovered ? 0xFFF87171 : 0xFFE06C75;
            textCol = 0xFFFF7777;
        } else if (missedDays == 2) {
            bg = hovered ? 0xFF332112 : 0xFF211810;
            border = hovered ? 0xFFFB923C : 0xFFD97706;
            textCol = 0xFFFFAA33;
        } else {
            bg = hovered ? 0xFF2D2714 : 0xFF1D1B12;
            border = hovered ? 0xFFFACC15 : 0xFFCA8A04;
            textCol = 0xFFFFDD55;
        }

        if (staffUuid.equals(openStaffUuid)) {
            border = 0xFFFFFFFF;
        }

        graphics.fill(x, y, x + BADGE_W, y + BTN_H, bg);
        graphics.fill(x, y, x + BADGE_W, y + 1, border);
        graphics.fill(x, y + BTN_H - 1, x + BADGE_W, y + BTN_H, border);
        graphics.fill(x, y, x + 1, y + BTN_H, border);
        graphics.fill(x + BADGE_W - 1, y, x + BADGE_W, y + BTN_H, border);

        String label = owedAmount > 0
                ? missedDays + "x " + owedAmount
                : "0";
        String display = trimToFit(mc, label, BADGE_W - 24);
        int textW = mc.font.width(display);
        int totalW = textW + 2 + 16;
        int startX = x + Math.max(4, (BADGE_W - totalW) / 2);
        graphics.drawString(mc.font, Component.literal(display),
                startX, y + (BTN_H - 8) / 2, textCol, false);
        graphics.renderFakeItem(EMERALD_ICON, startX + textW + 2, y + (BTN_H - 16) / 2);
    }

    public boolean toggleIfClicked(
            UUID staffUuid, int x, int y,
            int dailyWage, int missedDays, int owedAmount, boolean isResigned,
            double mouseX, double mouseY
    ) {
        if (mouseX >= x && mouseX <= x + BADGE_W && mouseY >= y && mouseY <= y + BTN_H) {
            if (staffUuid.equals(openStaffUuid)) {
                close();
            } else {
                openStaffUuid = staffUuid;
                anchorX = x;
                anchorY = y;
                openDailyWage = dailyWage;
                openMissedDays = missedDays;
                openOwedAmount = owedAmount;
                openIsResigned = isResigned;
            }
            return true;
        }
        return false;
    }

    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (openStaffUuid == null) return;
        Minecraft mc = Minecraft.getInstance();
        int[] box = getMenuBounds(mc);
        int mx = box[0];
        int my = box[1];

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);

        graphics.fill(mx, my, mx + MENU_W, my + MENU_H, 0xFA0E121A);
        int border = 0xFF475569;
        graphics.fill(mx, my, mx + MENU_W, my + 1, border);
        graphics.fill(mx, my + MENU_H - 1, mx + MENU_W, my + MENU_H, border);
        graphics.fill(mx, my, mx + 1, my + MENU_H, border);
        graphics.fill(mx + MENU_W - 1, my, mx + MENU_W, my + MENU_H, border);

        String dailyLine = ZooTranslations.get("zootycoon.ui.staff.wage.daily", openDailyWage);
        String arrearsLine = ZooTranslations.get("zootycoon.ui.staff.wage.arrears", openMissedDays, openOwedAmount);
        String tierLine = getTierDescription(openMissedDays, openIsResigned);
        int tierCol = openIsResigned || openMissedDays >= 3 ? 0xFFFF6666 : (openMissedDays == 2 ? 0xFFFFAA33 : (openMissedDays == 1 ? 0xFFFFDD55 : 0xFF55FF55));

        graphics.drawString(mc.font, Component.literal(dailyLine), mx + 8, my + 7, 0xFFD0D7DE, false);
        graphics.drawString(mc.font, Component.literal(arrearsLine), mx + 8, my + 19, 0xFFE6EDF3, false);
        graphics.drawString(mc.font, Component.literal(trimToFit(mc, tierLine, MENU_W - 16)), mx + 8, my + 31, tierCol, false);

        int payX = mx + 8;
        int payY = my + MENU_H - 24;
        int payW = MENU_W - 16;
        int payH = 17;
        boolean canPay = openOwedAmount > 0;
        boolean hovered = canPay && mouseX >= payX && mouseX <= payX + payW && mouseY >= payY && mouseY <= payY + payH;

        int pBg = !canPay ? 0xFF141820 : (hovered ? 0xFF1F4D36 : 0xFF163828);
        int pBorder = !canPay ? 0xFF283040 : (hovered ? 0xFF4ADE80 : 0xFF22C55E);
        int pText = !canPay ? 0xFF6E7681 : 0xFFFFFFFF;

        graphics.fill(payX, payY, payX + payW, payY + payH, pBg);
        graphics.fill(payX, payY, payX + payW, payY + 1, pBorder);
        graphics.fill(payX, payY + payH - 1, payX + payW, payY + payH, pBorder);
        graphics.fill(payX, payY, payX + 1, payY + payH, pBorder);
        graphics.fill(payX + payW - 1, payY, payX + payW, payY + payH, pBorder);

        String payLabel = canPay
                ? ZooTranslations.get(openIsResigned ? "zootycoon.ui.staff.wage.btn_settle_resigned" : "zootycoon.ui.staff.wage.btn_pay", openOwedAmount)
                : ZooTranslations.get("zootycoon.ui.staff.wage.btn_paid_ok");
        graphics.drawString(mc.font, Component.literal(payLabel),
                payX + (payW - mc.font.width(payLabel)) / 2, payY + (payH - 8) / 2, pText, false);

        graphics.pose().popPose();
    }

    public boolean handleOverlayClick(double mouseX, double mouseY) {
        if (openStaffUuid == null) return false;
        Minecraft mc = Minecraft.getInstance();
        int[] box = getMenuBounds(mc);
        int mx = box[0];
        int my = box[1];

        if (mouseX >= mx && mouseX <= mx + MENU_W && mouseY >= my && mouseY <= my + MENU_H) {
            int payX = mx + 8;
            int payY = my + MENU_H - 24;
            int payW = MENU_W - 16;
            int payH = 17;
            if (openOwedAmount > 0 && mouseX >= payX && mouseX <= payX + payW && mouseY >= payY && mouseY <= payY + payH) {
                ClientPlayNetworking.send(new ZooNetworkPayloads.PayStaffWagePayload(openStaffUuid));
                close();
            }
            return true;
        }

        close();
        return true;
    }

    private int[] getMenuBounds(Minecraft mc) {
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int mx = Math.clamp(anchorX - (MENU_W - BADGE_W) / 2, 8, Math.max(8, sw - MENU_W - 8));
        int my = anchorY + BTN_H + 3;
        if (my + MENU_H + 8 > sh) {
            my = Math.max(8, anchorY - MENU_H - 3);
        }
        return new int[]{mx, my};
    }

    private static String getTierDescription(int missedDays, boolean isResigned) {
        if (isResigned || missedDays >= 5) return ZooTranslations.get("zootycoon.ui.staff.wage.tier_resigned");
        if (missedDays >= 3) return ZooTranslations.get("zootycoon.ui.staff.wage.tier_strike");
        if (missedDays == 2) return ZooTranslations.get("zootycoon.ui.staff.wage.tier_degraded");
        if (missedDays == 1) return ZooTranslations.get("zootycoon.ui.staff.wage.tier_due");
        return ZooTranslations.get("zootycoon.ui.staff.wage.tier_ok");
    }

    private static String trimToFit(Minecraft mc, String text, int maxW) {
        if (mc.font.width(text) <= maxW) return text;
        String suffix = "..";
        int limit = maxW - mc.font.width(suffix);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));
            if (mc.font.width(sb.toString()) > limit) {
                sb.setLength(Math.max(0, sb.length() - 1));
                break;
            }
        }
        return sb.append(suffix).toString();
    }
}
