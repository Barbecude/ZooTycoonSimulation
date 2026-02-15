package com.example.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.text.NumberFormat;
import java.util.*;

public class ZooComputerScreen extends AbstractContainerScreen<ZooComputerMenu> {

        private static final int BG = 0xEE0F0F1A;
        private static final int BORDER = 0xFF6C63FF;
        private static final int HEADER = 0xFF1A1A30;
        private static final int SECTION = 0x88222240;
        private static final int GOLD = 0xFFFFD700;
        private static final int GREEN = 0xFF00FF88;
        private static final int CYAN = 0xFF55FFFF;
        private static final int GRAY = 0xFFAAAAAA;
        private static final int WHITE = 0xFFE0E0E0;

        private int scrollOffset = 0;
        private static final int ITEMS_PER_PAGE = 12; // 3 cols x 4 rows
        private List<Map.Entry<ResourceLocation, AnimalRegistry.AnimalData>> animalList;
        private List<Map.Entry<ResourceLocation, ZooItemRegistry.ItemData>> shopItemList;

        // 0 = Dashboard, 1 = Animals, 2 = Shop Items
        private int currentTab = 0;

        public ZooComputerScreen(ZooComputerMenu menu, Inventory inv, Component title) {
                super(menu, inv, title);
                this.imageWidth = 320; // Sedikit lebih lebar
                this.imageHeight = 240;

                this.inventoryLabelY = 10000;
                this.titleLabelY = 10000;
        }

        @Override
        protected void init() {
                super.init();

                // Load Data Lists
                animalList = new ArrayList<>(AnimalRegistry.getAllAnimals().entrySet());
                animalList.sort(Comparator.comparingInt(e -> e.getValue().price));

                shopItemList = new ArrayList<>(ZooItemRegistry.getAllItems().entrySet());
                shopItemList.sort(Comparator.comparingInt(e -> e.getValue().price));

                refreshWidgets();
        }

        private void refreshWidgets() {
                this.clearWidgets();
                int x = this.leftPos;
                int y = this.topPos;

                // --- GLOBAL TABS ---
                addRenderableWidget(Button.builder(Component.literal("🏠 Dash"), b -> switchTab(0))
                                .bounds(x + 10, y + 25, 60, 20).build());
                addRenderableWidget(Button.builder(Component.literal("🦁 Hewan"), b -> switchTab(1))
                                .bounds(x + 75, y + 25, 60, 20).build());
                addRenderableWidget(Button.builder(Component.literal("📦 Toko"), b -> switchTab(2))
                                .bounds(x + 140, y + 25, 60, 20).build());

                // Global Info
                int balance = menu.getBalance(); // Get from menu container
                NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));

                // --- TAB CONTENT ---
                switch (currentTab) {
                        case 0:
                                initDashboard(x, y);
                                break;
                        case 1:
                                initAnimalShop(x, y);
                                break;
                        case 2:
                                initItemShop(x, y);
                                break;
                }
        }

        private void switchTab(int tab) {
                this.currentTab = tab;
                this.scrollOffset = 0; // Reset scroll saat ganti tab
                refreshWidgets();
        }

        private void initDashboard(int x, int y) {
                // Management Buttons
                addRenderableWidget(Button.builder(Component.literal("👔 Rekrut Staff (Rp2k)"), b -> cmd("hire"))
                                .bounds(x + 20, y + 80, 130, 20).build());
                addRenderableWidget(Button.builder(Component.literal("📡 Upgrade Radius (Rp5k)"), b -> cmd("upgrade"))
                                .bounds(x + 160, y + 80, 130, 20).build());
                addRenderableWidget(Button.builder(Component.literal("🔄 Refresh Data"), b -> cmd("refresh"))
                                .bounds(x + 20, y + 110, 130, 20).build());
        }

        private void initAnimalShop(int x, int y) {
                // Navigation

                renderGrid(x + 10, y + 60, animalList.size(), (i, btnX, btnY) -> {
                        var entry = animalList.get(i);
                        var data = entry.getValue();
                        String label = data.displayName + " (" + (data.price / 1000) + "k)";
                        addRenderableWidget(Button.builder(Component.literal(label), b -> cmd("buy " + entry.getKey()))
                                        .bounds(btnX, btnY, 95, 20).build());
                });
        }

        private void initItemShop(int x, int y) {
                renderGrid(x + 10, y + 60, shopItemList.size(), (i, btnX, btnY) -> {
                        var entry = shopItemList.get(i);
                        var data = entry.getValue();
                        String label = data.displayName + " (" + data.price + ")";
                        // Buy 1 item, amount = 1
                        addRenderableWidget(Button
                                        .builder(Component.literal(label), b -> cmd("buyitem " + entry.getKey() + " 1"))
                                        .bounds(btnX, btnY, 95, 20).build());
                });
        }

        private interface GridRenderer {
                void renderItem(int index, int x, int y);
        }

        private void renderGrid(int startX, int startY, int totalItems, GridRenderer renderer) {
                int cols = 3;
                int btnW = 95;
                int btnH = 20;
                int gapX = 5;
                int gapY = 5;

                // Pagination Controls
                int maxPages = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
                int currentPage = scrollOffset + 1;

                int navY = startY + (4 * (btnH + gapY)) + 5;
                addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> {
                        if (scrollOffset > 0) {
                                scrollOffset--;
                                refreshWidgets();
                        }
                }).bounds(startX, navY, 60, 20).build());

                addRenderableWidget(Button.builder(Component.literal("Next >"), b -> {
                        if (scrollOffset < maxPages - 1) {
                                scrollOffset++;
                                refreshWidgets();
                        }
                }).bounds(startX + 240, navY, 60, 20).build());

                // Grid Items
                int startIdx = scrollOffset * ITEMS_PER_PAGE;
                int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, totalItems);

                for (int i = startIdx; i < endIdx; i++) {
                        int idx = i - startIdx;
                        int row = idx / cols;
                        int col = idx % cols;

                        int btnX = startX + col * (btnW + gapX);
                        int btnY = startY + row * (btnH + gapY);

                        renderer.renderItem(i, btnX, btnY);
                }
        }

        private void cmd(String action) {
                var pos = menu.getBlockPos();
                if (action.equals("refresh")) {
                        if (this.minecraft != null && this.minecraft.gameMode != null) {
                                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 99);
                        }
                        return;
                }

                // Format command with coords
                String c = "zoocmd " + action;
                if (!action.contains("buyitem")) {
                        // Append coords for standard commands if not already handled
                        // buyitem already has arguments from button callback? NO.
                        // Wait, standard buy also needs coords.
                        // Let's standardise: append coords to ALL commands
                        c = c + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
                } else {
                        // buyitem <id> <amount> <x> <y> <z>
                        // Action passed is "buyitem id amount"
                        c = c + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
                }

                var conn = Minecraft.getInstance().getConnection();
                if (conn != null)
                        conn.sendCommand(c);

                // Don't close screen for shop actions to allow multiple buys
                if (action.contains("buy") || action.equals("upgrade")) {
                        // Stay open
                } else {
                        onClose();
                }
        }

        @Override
        protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
                int x = leftPos, y = topPos, w = imageWidth, h = imageHeight;

                // Background
                g.fill(x, y, x + w, y + h, BG);

                // Borders
                g.fill(x, y, x + w, y + 2, BORDER);
                g.fill(x, y + h - 2, x + w, y + h, BORDER);
                g.fill(x, y, x + 2, y + h, BORDER);
                g.fill(x + w - 2, y, x + w, y + h, BORDER);

                // Header
                g.drawCenteredString(font, "🦁 INDOZOO SYSTEM v2.0", x + w / 2, y + 8, GOLD);

                // Stats Bar (Always visible)
                int statsY = y + 50;
                NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
                String stats = "💰 Rp " + nf.format(menu.getBalance()) + " | 🐾 " + menu.getAnimalCount() + " | 👔 "
                                + menu.getStaffCount();
                g.drawCenteredString(font, stats, x + w / 2, statsY, GREEN);

                // Tab Title
                String tabTitle = switch (currentTab) {
                        case 0 -> "Dashboard Overview";
                        case 1 -> "Animal Market (" + (scrollOffset + 1) + ")";
                        case 2 -> "Supply Shop (" + (scrollOffset + 1) + ")";
                        default -> "";
                };
                g.drawString(font, tabTitle, x + 15, y + 65, GRAY);

                // Tooltip logic for items/animals could be added here
        }

        @Override
        protected void renderLabels(GuiGraphics g, int mx, int my) {
                // KEEP EMPTY
        }

        @Override
        public boolean isPauseScreen() {
                return false;
        }
}
