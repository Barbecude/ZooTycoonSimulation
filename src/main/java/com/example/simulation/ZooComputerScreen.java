package com.example.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.text.NumberFormat;
import java.util.*;

public class ZooComputerScreen extends AbstractContainerScreen<ZooComputerMenu> {

        private static final int BG = 0xEE0F0F1A;
        private static final int BORDER = 0xFF6C63FF;
        private static final int GOLD = 0xFFFFD700;
        private static final int GREEN = 0xFF00FF88;
        private static final int GRAY = 0xFFAAAAAA;

        private int scrollOffset = 0;
        private static final int ITEMS_PER_PAGE = 12; // 3 cols x 4 rows
        private List<Map.Entry<ResourceLocation, AnimalRegistry.AnimalData>> animalList;
        private List<Map.Entry<ResourceLocation, ZooItemRegistry.ItemData>> shopItemList;

        // 0 = Dashboard, 1 = Animals, 2 = Shop Items
        private int currentTab = 0;

        // Shop Filtering
        private ZooItemRegistry.Category currentCategory = null; // null = ALL
        private List<Map.Entry<ResourceLocation, ZooItemRegistry.ItemData>> displayItemList;

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

                updateFilteredList();

                refreshWidgets();
        }

        private void updateFilteredList() {
                if (currentCategory == null) {
                        displayItemList = new ArrayList<>(shopItemList);
                } else {
                        displayItemList = new ArrayList<>();
                        for (var entry : shopItemList) {
                                if (entry.getValue().category == currentCategory) {
                                        displayItemList.add(entry);
                                }
                        }
                }
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
                addRenderableWidget(
                                Button.builder(Component.literal("🧹 Rekrut Janitor (Rp2k)"), b -> cmd("hire janitor"))
                                                .bounds(x + 20, y + 80, 130, 20).build());
                addRenderableWidget(
                                Button.builder(Component.literal("🥩 Rekrut Keeper (Rp2k)"), b -> cmd("hire zookeeper"))
                                                .bounds(x + 160, y + 80, 130, 20).build());

                addRenderableWidget(Button.builder(Component.literal("📡 Upgrade Radius (Rp5k)"), b -> cmd("upgrade"))
                                .bounds(x + 20, y + 110, 130, 20).build());

                // Refresh button just forces update, no cost.
                addRenderableWidget(Button.builder(Component.literal("🔄 Refresh Data"), b -> cmd("refresh"))
                                .bounds(x + 160, y + 110, 130, 20).build());
        }

        private void initAnimalShop(int x, int y) {
                // Navigation

                renderGrid(x + 10, y + 60, animalList.size(), (i, btnX, btnY) -> {
                        var entry = animalList.get(i);
                        var data = entry.getValue();
                        String label = data.displayName + " (" + (data.price / 1000) + "k)";
                        // Quote the ID: "buy \"minecraft:cow\" ..."
                        addRenderableWidget(Button
                                        .builder(Component.literal(label), b -> cmd("buy " + entry.getKey()))
                                        .bounds(btnX, btnY, 95, 20).build());
                });
        }

        private void initItemShop(int x, int y) {
                // Category Filter Buttons
                int cx = x + 10;
                int cy = y + 55;
                int cw = 42;
                int ch = 15;

                // "ALL" button
                addRenderableWidget(Button.builder(Component.literal("ALL"), b -> setCategory(null))
                                .bounds(cx, cy, cw, ch)
                                .tooltip(Tooltip.create(Component.literal("Semua Item")))
                                .build());

                int i = 1;
                for (ZooItemRegistry.Category cat : ZooItemRegistry.Category.values()) {
                        addRenderableWidget(Button
                                        .builder(Component.literal(
                                                        cat.label.substring(0, Math.min(4, cat.label.length()))),
                                                        b -> setCategory(cat))
                                        .bounds(cx + (i * (cw + 2)), cy, cw, ch)
                                        .tooltip(Tooltip.create(Component.literal(cat.label)))
                                        .build());
                        i++;
                }

                renderGrid(x + 10, y + 75, displayItemList.size(), (idx, btnX, btnY) -> {
                        var entry = displayItemList.get(idx);
                        var data = entry.getValue();

                        // Render Item Icon Logic (Using a custom button or overlay)
                        // Since we can't easily add a complex widget here without a class, we will use
                        // a label with spacing
                        // and render the item manually in renderBg or similar.
                        // Actually, standard buttons don't support items easily.
                        // We'll trust the user can read text for now, but to fix "item gada gambar",
                        // we need to implement a render method that draws items over the buttons.
                        // See render method below.

                        String label = data.displayName;
                        addRenderableWidget(Button
                                        .builder(Component.literal(label), b -> cmd("buyitem " + entry.getKey() + " 1"))
                                        .bounds(btnX + 20, btnY, 75, 20) // Shift right for icon
                                        .tooltip(Tooltip.create(Component.literal(
                                                        "Price: Rp " + data.price + " [" + data.category.label + "]")))
                                        .build());
                });
        }

        private void setCategory(ZooItemRegistry.Category cat) {
                this.currentCategory = cat;
                this.scrollOffset = 0;
                updateFilteredList();
                refreshWidgets();
        }

        // Removed initMarketing

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

                // Show grid only if tab matches
                if (currentTab != 1 && currentTab != 2)
                        return;

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

                // Show Page Info
                // We can't render text here easily, so maybe tooltip on buttons?

                // Grid Items
                int startIdx = scrollOffset * ITEMS_PER_PAGE;
                int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, totalItems);

                for (int j = startIdx; j < endIdx; j++) {
                        int idx = j - startIdx;
                        int col = idx % cols;
                        int row = idx / cols;

                        int btnX = startX + col * (btnW + gapX);
                        int btnY = startY + row * (btnH + gapY);

                        renderer.renderItem(j, btnX, btnY);
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

                String c = "zoocmd " + action + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ();

                var conn = Minecraft.getInstance().getConnection();
                if (conn != null)
                        conn.sendCommand(c);

                if (action.contains("buy") || action.equals("upgrade") || action.contains("hire")) {
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
                        case 2 -> "Supply Shop (" + (scrollOffset + 1) + ") - "
                                        + (currentCategory == null ? "ALL" : currentCategory.label);
                        default -> "";
                };
                g.drawString(font, tabTitle, x + 15, y + 65, GRAY); // Render Tab Title OVER Buttons?
                // Ah, Y=65 might overlap with filters at Y=55.
                // Let's move Tab Title UP or buttons DOWN.
                // Title at 65 is fine if logic is initDashboard (y+80).
                // But initItemShop buttons are at y+55.
                // So Title overrides buttons.
                // Fix: Title renders at y+38 (below tabs)? Or use header.
                // Actually tabs are at y+25.
                // Let's render title at y+45. But wait, buttons are at y+55.

                // Let's just adjust render locations in initItemShop.

                // Render Item Icons for Shop Tab (Tab 2)
                if (currentTab == 2) {
                        renderItemIcons(g, x + 10, y + 75);
                }
        }

        private void renderItemIcons(GuiGraphics g, int startX, int startY) {
                int cols = 3;
                int btnW = 95;
                int btnH = 20;
                int gapX = 5;
                int gapY = 5;
                int startIdx = scrollOffset * ITEMS_PER_PAGE;
                int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, displayItemList.size());

                for (int i = startIdx; i < endIdx; i++) {
                        int idx = i - startIdx;
                        // int row = idx / cols;
                        // int col = idx % cols;
                        int col = idx % cols;
                        int row = idx / cols;

                        // FIX: Logic must match renderGrid

                        int btnX = startX + col * (btnW + gapX);
                        int btnY = startY + row * (btnH + gapY);

                        var entry = displayItemList.get(i);
                        var data = entry.getValue();

                        // Draw Item Icon
                        g.renderItem(new net.minecraft.world.item.ItemStack(data.item), btnX + 2, btnY + 2);
                }
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
