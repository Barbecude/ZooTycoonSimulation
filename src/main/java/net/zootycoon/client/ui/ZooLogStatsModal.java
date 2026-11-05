package net.zootycoon.client.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modal overlay on MainManagementScreen displaying:
 * - Top Tab Bar: [Aktivitas & Log] and [Statistik Pengunjung]
 * - Tab 0: Chronological list of notifications (salary payments, ticket admissions, animal health warnings, staff requests)
 *          with precise real local machine date & time timestamps, clean color accents, and smooth scroll.
 * - Tab 1: Comprehensive visitor statistics and detailed analytics for today's visitors (tickets sold, free admissions,
 *          revenue, child vs adult demographics, live visitors in zoo, and satisfaction sentiment breakdown).
 */
public class ZooLogStatsModal {

    private final MainManagementScreen screen;
    private int activeTab = 0; // 0 = Logs, 1 = Stats

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private double logScrollOffset = 0;
    private int logTotalContentH = 0;

    private double statsScrollOffset = 0;
    private int statsTotalContentH = 0;

    public ZooLogStatsModal(MainManagementScreen screen) {
        this.screen = screen;
    }

    public void init() {
        this.logScrollOffset = 0;
        this.statsScrollOffset = 0;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int screenW = screen.width;
        int screenH = screen.height;

        // Dark dim backdrop over main screen
        graphics.fill(0, 0, screenW, screenH, 0x88000000);

        int modalW = Math.min(screenW - 40, 520);
        int modalH = Math.min(screenH - 30, 310);
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        Font font = screen.getFont();

        // 1. Sleek Slate Dialog Panel (Essential Minimalist theme)
        graphics.fill(modalX, modalY, modalX + modalW, modalY + modalH, 0xF212151D);
        graphics.fill(modalX, modalY, modalX + modalW, modalY + 1, 0xFF2A3140);
        graphics.fill(modalX, modalY + modalH - 1, modalX + modalW, modalY + modalH, 0xFF2A3140);
        graphics.fill(modalX, modalY, modalX + 1, modalY + modalH, 0xFF2A3140);
        graphics.fill(modalX + modalW - 1, modalY, modalX + modalW, modalY + modalH, 0xFF2A3140);

        // 2. Header Bar
        int headerH = 34;
        graphics.drawString(font, Component.literal("Riwayat Log & Statistik"), modalX + 14, modalY + 12, 0xFFFFD700, false);

        // Close 'x' button in top-right
        int closeBtnSize = 16;
        int closeBtnX = modalX + modalW - closeBtnSize - 10;
        int closeBtnY = modalY + 9;
        boolean closeHovered = mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnSize && mouseY >= closeBtnY && mouseY <= closeBtnY + closeBtnSize;
        graphics.fill(closeBtnX, closeBtnY, closeBtnX + closeBtnSize, closeBtnY + closeBtnSize, closeHovered ? 0xFF354157 : 0xFF1C222D);
        graphics.renderOutline(closeBtnX, closeBtnY, closeBtnSize, closeBtnSize, closeHovered ? 0xFF58A6FF : 0xFF2A3140);
        graphics.drawCenteredString(font, "x", closeBtnX + closeBtnSize / 2, closeBtnY + 3, closeHovered ? 0xFFFFFFFF : 0xFFA0AAB8);

        // Header Divider
        graphics.fill(modalX + 10, modalY + headerH, modalX + modalW - 10, modalY + headerH + 1, 0xFF252D3C);

        // 3. Sub Tabs: [Aktivitas & Log] [Statistik Pengunjung]
        int tabY = modalY + headerH + 6;
        int tabH = 18;
        int tabW = 120;
        int tab0X = modalX + 14;
        int tab1X = tab0X + tabW + 6;

        renderSubTab(graphics, font, tab0X, tabY, tabW, tabH, "Aktivitas & Log", activeTab == 0, mouseX, mouseY);
        renderSubTab(graphics, font, tab1X, tabY, tabW, tabH, "Statistik Pengunjung", activeTab == 1, mouseX, mouseY);

        // Content Area Container
        int contentX = modalX + 12;
        int contentY = tabY + tabH + 6;
        int contentW = modalW - 24;
        int contentH = (modalY + modalH - 10) - contentY;

        graphics.fill(contentX, contentY, contentX + contentW, contentY + contentH, 0xFF14171F);
        graphics.renderOutline(contentX, contentY, contentW, contentH, 0xFF252D3C);

        if (activeTab == 0) {
            renderLogsTab(graphics, font, contentX, contentY, contentW, contentH, mouseX, mouseY);
        } else {
            renderStatsTab(graphics, font, contentX, contentY, contentW, contentH, mouseX, mouseY);
        }
    }

    private void renderSubTab(GuiGraphics graphics, Font font, int x, int y, int w, int h, String text, boolean selected, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = selected ? 0xFF1F6FEB : (hovered ? 0xFF212631 : 0xFF161B22);
        int border = selected ? 0xFF58A6FF : (hovered ? 0xFF388BFD : 0xFF2B3342);
        int textCol = selected ? 0xFFFFFFFF : (hovered ? 0xFFE6EDF3 : 0xFF8B949E);

        graphics.fill(x, y, x + w, y + h, bg);
        graphics.renderOutline(x, y, w, h, border);
        graphics.drawCenteredString(font, text, x + w / 2, y + 5, textCol);
    }

    private void renderLogsTab(GuiGraphics graphics, Font font, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY) {
        List<ZooNetworkPayloads.ZooLogEntryDto> logs = ZooTycoonClient.clientLogs;

        if (logs.isEmpty()) {
            graphics.drawCenteredString(font, Component.literal("Belum ada log riwayat aktivitas tercatat."), contentX + contentW / 2, contentY + contentH / 2 - 4, 0xFF718096);
            return;
        }

        int listX = contentX + 4;
        int listY = contentY + 4;
        int listW = contentW - 8;
        int listH = contentH - 8;

        graphics.enableScissor(listX, listY, listX + listW, listY + listH);

        int curY = (int) (listY - logScrollOffset);
        int cardGap = 4;

        for (int i = 0; i < logs.size(); i++) {
            var log = logs.get(i);
            int textW = listW - 20;

            // Format date & time using player's local machine time
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(log.timestamp()), ZoneId.systemDefault());
            String timeStr = localTime.format(DATE_FORMATTER);

            var descLines = font.split(Component.literal(log.description()), textW);
            int cardH = 26 + descLines.size() * 10;

            if (curY + cardH >= listY && curY <= listY + listH) {
                int accentColor = switch (log.category()) {
                    case "SALARY" -> (log.urgency() >= 3 ? 0xFFE06C75 : 0xFF61AFEF);
                    case "TICKET" -> 0xFF98C379;
                    case "ANIMAL_HEALTH" -> (log.urgency() >= 3 ? 0xFFE06C75 : 0xFFE5C07B);
                    case "STAFF_REQUEST" -> 0xFFD19A66;
                    default -> 0xFF58A6FF;
                };

                int bgCol = (log.urgency() >= 3) ? 0xFF211417 : 0xFF171B24;
                int borderCol = (log.urgency() >= 3) ? 0xFF5C2329 : 0xFF252D3C;

                graphics.fill(listX, curY, listX + listW, curY + cardH, bgCol);
                graphics.renderOutline(listX, curY, listW, cardH, borderCol);

                // Left color accent bar
                graphics.fill(listX + 1, curY + 1, listX + 3, curY + cardH - 1, accentColor);

                // Title + Category tag
                graphics.drawString(font, Component.literal(log.title()), listX + 8, curY + 6, 0xFFFFFFFF, false);

                // Local timestamp on the right
                int timeW = font.width(timeStr);
                graphics.drawString(font, Component.literal(timeStr), listX + listW - timeW - 6, curY + 6, 0xFF8B949E, false);

                // Description lines
                int dy = curY + 18;
                for (var line : descLines) {
                    graphics.drawString(font, line, listX + 8, dy, 0xFFC9D1D9, false);
                    dy += 10;
                }
            }

            curY += cardH + cardGap;
        }

        this.logTotalContentH = curY - (int) (listY - logScrollOffset);
        graphics.disableScissor();

        // Scrollbar
        int maxScroll = Math.max(0, logTotalContentH - listH);
        if (maxScroll > 0) {
            int barW = 3;
            int barX = listX + listW - barW;
            graphics.fill(barX, listY, barX + barW, listY + listH, 0xFF14171F);
            float ratio = (float) listH / (float) logTotalContentH;
            int thumbH = Math.max(12, (int) (listH * ratio));
            int thumbY = listY + (int) ((listH - thumbH) * (logScrollOffset / maxScroll));
            graphics.fill(barX, thumbY, barX + barW, thumbY + thumbH, 0xFF58A6FF);
        }
    }

    private void renderStatsTab(GuiGraphics graphics, Font font, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY) {
        var stats = ZooTycoonClient.clientStats;

        int listX = contentX + 6;
        int listY = contentY + 6;
        int listW = contentW - 12;
        int listH = contentH - 12;

        graphics.enableScissor(listX, listY, listX + listW, listY + listH);

        int curY = (int) (listY - statsScrollOffset);

        // 1. Summary Cards Row: 3 metric boxes (Total Pengunjung Hari Ini, Total Tiket Terjual, Total Pendapatan Tiket)
        int metricBoxW = (listW - 8) / 3;
        int metricBoxH = 46;

        renderMetricBox(graphics, font, listX, curY, metricBoxW, metricBoxH, "Pengunjung Hari Ini", String.valueOf(stats.totalVisitorsToday()), "Orang Masuk", 0xFF58A6FF);
        renderMetricBox(graphics, font, listX + metricBoxW + 4, curY, metricBoxW, metricBoxH, "Tiket Terjual", String.valueOf(stats.ticketsSoldToday()), stats.freeVisitorsToday() > 0 ? ("+" + stats.freeVisitorsToday() + " Tiket Gratis") : "Tiket Loket", 0xFF98C379);
        renderMetricBox(graphics, font, listX + (metricBoxW + 4) * 2, curY, metricBoxW, metricBoxH, "Pendapatan Tiket", "+" + stats.ticketRevenueToday(), "Emerald", 0xFFE5C07B);

        curY += metricBoxH + 10;

        // 2. Section: Demografi Pengunjung Hari Ini
        graphics.drawString(font, Component.literal("Analisis Demografi Pengunjung:"), listX + 2, curY, 0xFFFFD700, false);
        curY += 13;

        int demoCardH = 44;
        graphics.fill(listX, curY, listX + listW, curY + demoCardH, 0xFF171B24);
        graphics.renderOutline(listX, curY, listW, demoCardH, 0xFF252D3C);

        int totalToday = Math.max(1, stats.totalVisitorsToday());
        int adultPct = Math.round(((float) stats.adultVisitorsToday() / (float) totalToday) * 100.0f);
        int childPct = Math.round(((float) stats.childVisitorsToday() / (float) totalToday) * 100.0f);

        graphics.drawString(font, Component.literal("• Pengunjung Dewasa: " + stats.adultVisitorsToday() + " orang (" + adultPct + "%)"), listX + 10, curY + 8, 0xFFE6EDF3, false);
        graphics.drawString(font, Component.literal("• Pengunjung Anak-Anak: " + stats.childVisitorsToday() + " orang (" + childPct + "%)"), listX + 10, curY + 22, 0xFFE6EDF3, false);

        curY += demoCardH + 10;

        // 3. Section: Status Pengunjung di Kebun Binatang Saat Ini
        graphics.drawString(font, Component.literal("Kondisi & Kepuasan Pengunjung Saat Ini:"), listX + 2, curY, 0xFFFFD700, false);
        curY += 13;

        int liveCardH = 68;
        graphics.fill(listX, curY, listX + listW, curY + liveCardH, 0xFF171B24);
        graphics.renderOutline(listX, curY, listW, liveCardH, 0xFF252D3C);

        int liveCount = stats.currentVisitorsInZoo();
        graphics.drawString(font, Component.literal("• Total Pengunjung Berada di Zoo: " + liveCount + " orang"), listX + 10, curY + 8, 0xFF58A6FF, false);

        int happy = stats.happyVisitors();
        int neutral = stats.neutralVisitors();
        int unhappy = stats.unhappyVisitors();
        int totalMood = Math.max(1, liveCount);
        int happyPct = Math.round(((float) happy / (float) totalMood) * 100.0f);
        int neutralPct = Math.round(((float) neutral / (float) totalMood) * 100.0f);
        int unhappyPct = Math.round(((float) unhappy / (float) totalMood) * 100.0f);

        graphics.drawString(font, Component.literal("• Senang / Terhibur: " + happy + " orang (" + happyPct + "%)"), listX + 10, curY + 22, 0xFF98C379, false);
        graphics.drawString(font, Component.literal("• Biasa / Berkeliling: " + neutral + " orang (" + neutralPct + "%)"), listX + 10, curY + 36, 0xFFA0AAB8, false);
        graphics.drawString(font, Component.literal("• Mengeluh / Kecewa: " + unhappy + " orang (" + unhappyPct + "%)"), listX + 10, curY + 50, unhappy > 0 ? 0xFFE06C75 : 0xFF718096, false);

        curY += liveCardH + 10;

        this.statsTotalContentH = curY - (int) (listY - statsScrollOffset);
        graphics.disableScissor();

        // Scrollbar
        int maxScroll = Math.max(0, statsTotalContentH - listH);
        if (maxScroll > 0) {
            int barW = 3;
            int barX = listX + listW - barW;
            graphics.fill(barX, listY, barX + barW, listY + listH, 0xFF14171F);
            float ratio = (float) listH / (float) statsTotalContentH;
            int thumbH = Math.max(12, (int) (listH * ratio));
            int thumbY = listY + (int) ((listH - thumbH) * (statsScrollOffset / maxScroll));
            graphics.fill(barX, thumbY, barX + barW, thumbY + thumbH, 0xFF58A6FF);
        }
    }

    private void renderMetricBox(GuiGraphics graphics, Font font, int x, int y, int w, int h, String title, String value, String sub, int valColor) {
        graphics.fill(x, y, x + w, y + h, 0xFF171B24);
        graphics.renderOutline(x, y, w, h, 0xFF252D3C);

        graphics.drawString(font, Component.literal(title), x + 8, y + 6, 0xFF8B949E, false);
        graphics.drawString(font, Component.literal(value), x + 8, y + 18, valColor, false);
        graphics.drawString(font, Component.literal(sub), x + 8, y + 32, 0xFF6E7681, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int screenW = screen.width;
        int screenH = screen.height;
        int modalW = Math.min(screenW - 40, 520);
        int modalH = Math.min(screenH - 30, 310);
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        int closeBtnSize = 16;
        int closeBtnX = modalX + modalW - closeBtnSize - 10;
        int closeBtnY = modalY + 9;
        if (mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnSize && mouseY >= closeBtnY && mouseY <= closeBtnY + closeBtnSize) {
            return false; // Close handled by caller
        }

        int headerH = 34;
        int tabY = modalY + headerH + 6;
        int tabH = 18;
        int tabW = 120;
        int tab0X = modalX + 14;
        int tab1X = tab0X + tabW + 6;

        if (mouseX >= tab0X && mouseX <= tab0X + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
            this.activeTab = 0;
            return true;
        }
        if (mouseX >= tab1X && mouseX <= tab1X + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
            this.activeTab = 1;
            return true;
        }

        return mouseX >= modalX && mouseX <= modalX + modalW && mouseY >= modalY && mouseY <= modalY + modalH;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int screenW = screen.width;
        int screenH = screen.height;
        int modalW = Math.min(screenW - 40, 520);
        int modalH = Math.min(screenH - 30, 310);
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        int headerH = 34;
        int tabY = modalY + headerH + 6;
        int tabH = 18;
        int contentX = modalX + 12;
        int contentY = tabY + tabH + 6;
        int contentW = modalW - 24;
        int contentH = (modalY + modalH - 10) - contentY;

        if (mouseX >= contentX && mouseX <= contentX + contentW && mouseY >= contentY && mouseY <= contentY + contentH) {
            if (activeTab == 0) {
                int maxScroll = Math.max(0, logTotalContentH - (contentH - 8));
                logScrollOffset = Math.clamp(logScrollOffset - verticalAmount * 18.0D, 0, maxScroll);
            } else {
                int maxScroll = Math.max(0, statsTotalContentH - (contentH - 12));
                statsScrollOffset = Math.clamp(statsScrollOffset - verticalAmount * 18.0D, 0, maxScroll);
            }
            return true;
        }
        return false;
    }
}

