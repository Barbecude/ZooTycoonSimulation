package com.example.simulation;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

public class ZooComputerScreen extends AbstractContainerScreen<ZooComputerMenu> {
        // --- COLORS & STYLES ---
        private static final int PANEL_WIDTH = 260; // Wider
        private static final int BG_COLOR = 0xAA000000; // Transparent
        private static final int SIDEBAR_COLOR = 0xAA1E1E24; // Transparent Sidebar
        private static final int ACCENT_COLOR = 0xFF6C63FF;
        private static final int TEXT_COLOR = 0xFFE0E0E0;
        private static final int GOLD_COLOR = 0xFFFFD700;
        private static final int ROW_HEIGHT = 28;

        // --- TABS ---
        private enum MainTab {
                DASHBOARD, ANIMALS, BUILDINGS, FOOD, VEHICLES
        }

        private MainTab currentTab = MainTab.DASHBOARD;
        private int scrollOffset = 0;
        private String currentSubFilter = "ALL";
        private List<ShopEntry> currentDisplayList = new ArrayList<>();

        public ZooComputerScreen(ZooComputerMenu menu, Inventory inv, Component title) {
                super(menu, inv, title);
                this.imageWidth = PANEL_WIDTH;
                this.imageHeight = 256;
        }

        @Override
        protected void init() {
                this.height = this.minecraft.getWindow().getGuiScaledHeight();
                this.imageHeight = this.height;
                this.leftPos = this.width - PANEL_WIDTH;
                this.topPos = 0;
                super.init();
                refreshContent();
        }

        private void refreshContent() {
                this.clearWidgets();
                // Keep scroll offset ONLY if staying on same tab? No, reset for safety or keep
                // if logical.
                // this.scrollOffset = 0; // Uncomment to reset on refresh

                initSidebar();

                switch (currentTab) {
                        case DASHBOARD -> {
                                this.scrollOffset = 0;
                                this.scrollOffset = 0;
                                initDashboard();
                                prepareDashboardCollection();
                        }
                        case ANIMALS -> initShopList("ANIMALS");
                        case BUILDINGS -> initShopList("BUILDINGS");
                        case FOOD -> initShopList("FOOD");
                        case VEHICLES -> initShopList("VEHICLES");

                }
        }

        private void initSidebar() {
                int x = leftPos + 5;
                int y = topPos + 40;
                int btnSize = 24;
                int gap = 8;
                addRenderableWidget(createTabBtn(x, y, "🏠", "Dashboard", MainTab.DASHBOARD));
                addRenderableWidget(createTabBtn(x, y + (btnSize + gap), "🦁", "Toko Hewan", MainTab.ANIMALS));
                addRenderableWidget(createTabBtn(x, y + (btnSize + gap) * 2, "🧱", "Toko Bangunan", MainTab.BUILDINGS));
                addRenderableWidget(createTabBtn(x, y + (btnSize + gap) * 3, "🍎", "Pangan", MainTab.FOOD));
                addRenderableWidget(createTabBtn(x, y + (btnSize + gap) * 4, "🚜", "Kendaraan", MainTab.VEHICLES));
        }

        private Button createTabBtn(int x, int y, String icon, String label, MainTab tab) {
                return Button.builder(Component.literal(icon), b -> {
                        this.currentTab = tab;
                        this.currentSubFilter = "ALL";
                        this.scrollOffset = 0;
                        refreshContent();
                }).bounds(x, y, 24, 24).tooltip(Tooltip.create(Component.literal(label))).build();
        }

        private void initDashboard() {
                int contentX = leftPos + 40;
                int contentY = topPos + 40;
                int w = PANEL_WIDTH - 50;

                addRenderableWidget(Button.builder(Component.literal("Rekrut Janitor"),
                                b -> cmd("hire janitor"))
                                .bounds(contentX, contentY + 80, w, 20)
                                .tooltip(Tooltip.create(Component.literal("Rp 2.000.000")))
                                .build());

                addRenderableWidget(Button.builder(Component.literal("Reykrut Keeper"),
                                b -> cmd("hire zookeeper"))
                                .bounds(contentX, contentY + 105, w, 20)
                                .tooltip(Tooltip.create(Component.literal("Rp 2.000.000")))
                                .build());

                addRenderableWidget(Button.builder(Component.literal("Beli Banner Zoo"),
                                b -> cmd("buyitem indozoo:zoo_banner 1"))
                                .bounds(contentX, contentY + 130, w, 20)
                                .tooltip(Tooltip.create(Component.literal("Gratis / Penanda Lokasi")))
                                .build());

                addRenderableWidget(Button.builder(Component.literal("🔄 Refresh Data"),
                                b -> cmd("refresh")).bounds(contentX, this.height - 30, w, 20).build());

                // Collection List on Dashboard (Bottom / Side)
                // Let's render it below buttons
                initCollectionList(contentX, contentY + 160, w);
        }

        // --- LIST VIEW LOGIC ---

        private void initShopList(String type) {
                int contentX = leftPos + 40;
                int contentY = topPos + 10;
                int w = PANEL_WIDTH - 50;

                prepareShopData(type);

                // Filter Buttons
                List<String> filters = getFiltersForTab(type);
                int fx = contentX;
                for (String f : filters) {
                        int fw = font.width(f) + 12;
                        addRenderableWidget(Button.builder(Component.literal(f), b -> {
                                this.currentSubFilter = f;
                                this.scrollOffset = 0;
                                refreshContent();
                        }).bounds(fx, contentY, fw, 16).build());
                        fx += fw + 4;
                }

                // List Items (Grid 2 Columns)
                int listStartY = contentY + 25;
                // List Items (Grid 4 Columns)
                listStartY = contentY + 25;
                int maxRows = (this.height - listStartY - 40) / ROW_HEIGHT;
                int totalItems = currentDisplayList.size();
                int totalRows = (totalItems + 3) / 4; // Ceiling for 4 columns

                // Clamp scroll
                if (scrollOffset > Math.max(0, totalRows - maxRows)) {
                        scrollOffset = Math.max(0, totalRows - maxRows);
                }

                int colGap = 4;
                int itemW = (w - (colGap * 3)) / 4;

                for (int r = 0; r < maxRows; r++) {
                        int rowIdx = scrollOffset + r;
                        if (rowIdx >= totalRows)
                                break;

                        int y = listStartY + (r * ROW_HEIGHT);

                        for (int c = 0; c < 4; c++) {
                                int idx = rowIdx * 4 + c;
                                if (idx < totalItems) {
                                        ShopEntry entry = currentDisplayList.get(idx);
                                        String cmd = (type.equals("ANIMALS")) ? "buy " + entry.id
                                                        : "buyitem " + entry.id + " 1";
                                        addRenderableWidget(Button.builder(Component.empty(), b -> cmd(cmd))
                                                        .bounds(contentX + (itemW + colGap) * c, y, itemW,
                                                                        ROW_HEIGHT - 2)
                                                        .tooltip(Tooltip.create(
                                                                        Component.literal("§a"
                                                                                        + formatPrice(entry.price))))
                                                        .build());
                                }
                        }
                }

                // Pagination (Scroll by Row)
                if (totalRows > maxRows) {
                        addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
                                if (scrollOffset > 0) {
                                        scrollOffset--;
                                        refreshContent();
                                }
                        }).bounds(contentX + w + 2, listStartY, 15, 20).build());

                        addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
                                if (scrollOffset < totalRows - maxRows) {
                                        scrollOffset++;
                                        refreshContent();
                                }
                        }).bounds(contentX + w + 2, this.height - 40, 15, 20).build());
                }
        }

        private List<String> getFiltersForTab(String type) {
                if (type.equals("ANIMALS"))
                        return List.of("ALL", "Land", "Aquatic", "Mythical", "Bug");
                if (type.equals("BUILDINGS"))
                        return List.of("ALL", "Building", "Natural", "Utility");
                return List.of("ALL");
        }

        private void prepareShopData(String type) {
                currentDisplayList.clear();

                if (type.equals("ANIMALS")) {
                        for (AnimalRegistry.AnimalData data : AnimalRegistry.getAllAnimals().values()) {
                                if (!matchesFilter(data.category.name()))
                                        continue;

                                ItemStack icon = resolveAnimalIcon(data.entityType, data.displayName);

                                LivingEntity model = null;
                                Entity e = data.entityType.create(Minecraft.getInstance().level);
                                if (e instanceof LivingEntity le) {
                                        model = le;
                                }

                                currentDisplayList
                                                .add(new ShopEntry(ForgeRegistries.ENTITY_TYPES.getKey(data.entityType),
                                                                data.displayName, data.price, icon, model));
                        }
                } else {
                        // ZooItemRegistry
                        for (ZooItemRegistry.ItemData data : ZooItemRegistry.getAllItems().values()) {
                                String catName = data.category.name();
                                boolean typeMatch = false;
                                if (type.equals("BUILDINGS") && (catName.equals("BUILDING") || catName.equals("NATURAL")
                                                || catName.equals("UTILITY")))
                                        typeMatch = true;
                                else if (type.equals("FOOD") && catName.equals("FOOD"))
                                        typeMatch = true;
                                else if (type.equals("VEHICLES")) {
                                        // Disable vehicles, show nothing or handled specially?
                                        // The loop iterates items. If we want to show "Coming Soon", we must do it
                                        // outside the loop or filter.
                                        // Let's just NOT match anything here, and handle the "Coming Soon" entry
                                        // separately.
                                        typeMatch = false;
                                }

                                if (typeMatch && matchesFilter(catName)) {
                                        currentDisplayList.add(new ShopEntry(ForgeRegistries.ITEMS.getKey(data.item),
                                                        data.displayName, data.price, new ItemStack(data.item), null));
                                }
                        }
                }

                if (type.equals("VEHICLES")) {
                        currentDisplayList.add(new ShopEntry(new ResourceLocation("minecraft:barrier"), "Coming Soon",
                                        0, new ItemStack(Items.BARRIER), null));
                }

                currentDisplayList.sort(Comparator.comparingInt(e -> e.price));
        }

        private void initCollectionList() {
                currentDisplayList.clear();
                // Client side fetch ZooData
                ZooData data = ZooData.get(Minecraft.getInstance().level);
                net.minecraft.nbt.ListTag list = data.getTaggedAnimals();

                for (int i = 0; i < list.size(); i++) {
                        net.minecraft.nbt.CompoundTag tag = list.getCompound(i);
                        String name = tag.getString("name");
                        String typeStr = tag.getString("type");
                        int id = tag.getInt("id");

                        // Create dummy entity for rendering
                        // We don't have exact entity NBT, just type.
                        // Ideally we'd store entity type ID.
                        // For now, let's assume valid ID is somehow resolvable or just show Name Tag
                        // Item if fail.

                        // Wait, typeStr? I need to store ResourceLocation of type in TagAnimalPacket.
                        // I will assume typeStr IS ResourceLocation string.

                        ItemStack icon = new ItemStack(Items.NAME_TAG);
                        LivingEntity model = null;

                        // Try to parse entity type
                        // We need to update TagAnimalPacket to save entity TYPE.
                        // But assuming valid type string:

                        // If we can't find type, it will be null model.
                }

                // Wait, I need to update TagAnimalPacket first to store Type as
                // ResourceLocation string?
                // ZooData currently stores "type" string. I will ensure TagAnimalPacket puts
                // ResourceLocation there.

                // Re-reading ZooData from TagAnimalPacket step...
                // TagAnimalPacket stores "type". Simple string.

                // Back to filtering.
                // We can't easily iterate all entities to find what "type" means if it's just
                // general string.
                // But let's assume I fix TagAnimalPacket to save
                // `EntityType.getKey(entity.getType()).toString()`.

                // For now, mock implementation for list display
                // We will iterate and render.
        }

        private boolean matchesFilter(String catName) {
                if (currentSubFilter.equals("ALL"))
                        return true;
                return currentSubFilter.equalsIgnoreCase(catName);
        }

        private ItemStack resolveAnimalIcon(net.minecraft.world.entity.EntityType<?> type, String name) {
                ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
                if (id == null)
                        return new ItemStack(Items.BARRIER);

                // 1. Try standard pattern [mod]:[mob]_spawn_egg
                ResourceLocation egg1 = new ResourceLocation(id.getNamespace(), id.getPath() + "_spawn_egg");
                if (ForgeRegistries.ITEMS.containsKey(egg1))
                        return new ItemStack(ForgeRegistries.ITEMS.getValue(egg1));

                // 2. Try alexsmobs pattern [mod]:spawn_egg_[mob]
                ResourceLocation egg2 = new ResourceLocation(id.getNamespace(), "spawn_egg_" + id.getPath());
                if (ForgeRegistries.ITEMS.containsKey(egg2))
                        return new ItemStack(ForgeRegistries.ITEMS.getValue(egg2));

                // 3. Try generic lookup or fallback
                // Using a generic meaningful item if egg not found
                if (name.toLowerCase().contains("bear"))
                        return new ItemStack(Items.COD); // Just a placeholder example, better than Name Tag?

                // Fallback to name tag but user dislikes it. Let's try to just return a
                // structure void or something invisible?
                // Or just return the item that matches the ID if it exists (some entities have
                // same item ID)
                if (ForgeRegistries.ITEMS.containsKey(id))
                        return new ItemStack(ForgeRegistries.ITEMS.getValue(id));

                return new ItemStack(Items.NAME_TAG); // Ultimate fallback
        }

        @Override
        public void render(GuiGraphics g, int mx, int my, float partialTick) {
                this.mouseX = mx;
                this.mouseY = my;
                renderBg(g, partialTick, mx, my);
                super.render(g, mx, my, partialTick);
                renderForeground(g, mx, my);
                renderTooltip(g, mx, my);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
                if (delta != 0) {
                        int listStartY = topPos + 40;
                        if (currentTab != MainTab.DASHBOARD)
                                listStartY = topPos + 35; // Adjust based on logic

                        int maxRows = (this.height - listStartY - 40) / ROW_HEIGHT;
                        int totalItems = currentDisplayList.size();
                        int totalRows = (totalItems + 3) / 4;

                        if (totalRows > maxRows) {
                                if (delta > 0 && scrollOffset > 0) {
                                        scrollOffset--;
                                        refreshContent();
                                        return true;
                                } else if (delta < 0 && scrollOffset < totalRows - maxRows) {
                                        scrollOffset++;
                                        refreshContent();
                                        return true;
                                }
                        }
                }
                return super.mouseScrolled(mouseX, mouseY, delta);
        }

        @Override
        protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
                RenderSystem.enableBlend();
                g.fill(leftPos, 0, width, height, BG_COLOR);
                g.fill(leftPos, 0, leftPos + 35, height, SIDEBAR_COLOR);
                g.fill(leftPos + 35, 0, leftPos + 36, height, 0xFF333333);
        }

        private void renderForeground(GuiGraphics g, int mx, int my) {
                int contentX = leftPos + 40;
                g.drawString(font, "ZOO MANAGEMENT", leftPos + 10, 10, ACCENT_COLOR, false);

                if (currentTab == MainTab.DASHBOARD) {
                        renderDashboardStats(g, contentX, topPos + 40);
                        // Render list below stats
                        renderShopList(g, contentX, topPos + 180); // Adjusted Y
                } else {
                        renderShopList(g, contentX, topPos + 35);
                }
        }

        private void renderDashboardStats(GuiGraphics g, int x, int y) {
                g.pose().pushPose();
                g.pose().scale(2.0f, 2.0f, 2.0f);
                g.drawString(font, formatPrice(ClientZooData.getBalance()), (x / 2), (y / 2), GOLD_COLOR);
                g.pose().popPose();

                g.pose().popPose();

                y += 20; // Reduced gap
                drawStatBox(g, x, y, "Hewan", ClientZooData.getAnimalCount() + "", 0xFF4CAF50);
                drawStatBox(g, x + 80, y, "Staff", ClientZooData.getStaffCount() + "", 0xFF2196F3);
                drawStatBox(g, x + 80 + 80, y, "Pengunjung", ClientZooData.getVisitorCount() + "", 0xFFFF9800);
        }

        private void initCollectionList(int x, int y, int w) {
                // We can't use buttons effectively inside a scrollable area if the whole page
                // isn't scrollable.
                // But existing list logic uses scrollOffset.
                // Let's reuse renderShopList logic but for collection?
                // Or just simple text list for now?
                // "grid kategori usahkan ukurannya relative"

                // Let's populate currentDisplayList with collection and render it using the
                // main render loop if Tab is Dashboard?
                // But Dashboard has buttons too.
                // Complex mixed layout.

                // Proposal:
                // Dashboard Top: Stats & Buttons
                // Dashboard Bottom: Collection Grid

                // I will populate `currentDisplayList` with Collection items when enter
                // Dashboard.
                // And `renderDashboardStats` will render stats, then `renderShopList` (renamed
                // to renderGrid) will render the list below.
        }

        private void prepareDashboardCollection() {
                currentDisplayList.clear();
                ZooData data = ZooData.get(Minecraft.getInstance().level);
                net.minecraft.nbt.ListTag list = data.getTaggedAnimals();

                for (int i = 0; i < list.size(); i++) {
                        net.minecraft.nbt.CompoundTag tag = list.getCompound(i);
                        String name = tag.getString("name");
                        String typeStr = tag.getString("type");
                        int id = tag.getInt("id");

                        ItemStack icon = new ItemStack(Items.NAME_TAG);
                        LivingEntity model = null;

                        ResourceLocation typeId = ResourceLocation.tryParse(typeStr);
                        if (typeId != null && ForgeRegistries.ENTITY_TYPES.containsKey(typeId)) {
                                net.minecraft.world.entity.EntityType<?> et = ForgeRegistries.ENTITY_TYPES
                                                .getValue(typeId);
                                if (et != null) {
                                        Entity e = et.create(Minecraft.getInstance().level);
                                        if (e instanceof LivingEntity le) {
                                                model = le;
                                                model.setCustomName(Component.literal(name));
                                                model.setCustomNameVisible(true);
                                        }
                                        // Also try to resolve icon
                                        icon = resolveAnimalIcon(et, name);
                                }
                        }

                        // Use ID as price 0 for now
                        currentDisplayList.add(new ShopEntry(new ResourceLocation("indozoo", "animal_" + id), name, 0,
                                        icon, model));
                }
        }

        private void drawStatBox(GuiGraphics g, int x, int y, String label, String value, int color) {
                g.fill(x, y, x + 70, y + 40, 0xFF2A2A35);
                g.drawCenteredString(font, value, x + 35, y + 10, color);
                g.drawCenteredString(font, label, x + 35, y + 25, 0xFFAAAAAA);
        }

        private void renderShopList(GuiGraphics g, int x, int y) {
                int listStartY = y;
                int maxRows = (this.height - listStartY - 40) / ROW_HEIGHT;
                int totalItems = currentDisplayList.size();
                int totalRows = (totalItems + 3) / 4;

                int w = PANEL_WIDTH - 50;
                int colGap = 4;
                int itemW = (w - (colGap * 3)) / 4;

                for (int r = 0; r < maxRows; r++) {
                        int rowIdx = scrollOffset + r;
                        if (rowIdx >= totalRows)
                                break;

                        int rowY = listStartY + (r * ROW_HEIGHT);

                        for (int c = 0; c < 4; c++) {
                                int idx = rowIdx * 4 + c;
                                if (idx < totalItems) {
                                        renderShopItemAt(g, idx, x + (itemW + colGap) * c, rowY, itemW);
                                }
                        }
                }

                String pageInfo = "Items: " + currentDisplayList.size();
                g.drawString(font, pageInfo, x, this.height - 15, 0xFF555555);
        }

        private void renderShopItemAt(GuiGraphics g, int idx, int x, int y, int w) {
                ShopEntry entry = currentDisplayList.get(idx);

                // BG
                boolean hovered = isHovering(x, y, w, ROW_HEIGHT - 2, mouseX, mouseY);
                int color = hovered ? 0xFF3E3E44 : 0xFF2A2A35;
                g.fill(x, y, x + w, y + ROW_HEIGHT - 2, color);

                // Icon
                // Icon / Entity
                if (entry.entityModel != null) {
                        // Render Entity
                        // 15 is scale, x+15 center of icon area, y+24 bottom of icon area
                        try {
                                InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 15, y + 24, 13,
                                                (float) (x + 15) - (float) mouseX,
                                                (float) (y + 24 - 15) - (float) mouseY, entry.entityModel);
                        } catch (Exception e) {
                                // Fallback in case of render error
                                g.renderItem(entry.icon, x + 4, y + 5);
                        }
                } else {
                        g.renderItem(entry.icon, x + 4, y + 5);
                }

                // Name
                // Truncate name if too long for itemW?
                String name = entry.displayName;
                if (font.width(name) > w - 24) {
                        name = font.plainSubstrByWidth(name, w - 24 - 5) + "...";
                }
                g.drawString(font, name, x + 24, y + 10, TEXT_COLOR);
        }

        private String formatPrice(int price) {
                java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
                return "Rp " + nf.format(price);
        }

        private double mouseX, mouseY;

        private void cmd(String action) {
                // Global command dispatch
                if (action.equals("refresh")) {
                        refreshContent();
                        return; // No server command needed for refresh if it just redraws UI
                        // But wait, refresh might need to sync data?
                        // Data is synced via ContainerData continuously.
                }

                String c = "zoocmd " + action;
                var conn = Minecraft.getInstance().getConnection();
                if (conn != null) {
                        conn.sendCommand(c);
                }
        }

        @Override
        protected void renderLabels(GuiGraphics g, int mx, int my) {
        }

        private static class ShopEntry {
                ResourceLocation id;
                String displayName;
                int price;
                ItemStack icon;
                LivingEntity entityModel;

                public ShopEntry(ResourceLocation id, String name, int price, ItemStack icon,
                                LivingEntity entityModel) {
                        this.id = id;
                        this.displayName = name;
                        this.price = price;
                        this.icon = icon;
                        this.entityModel = entityModel;
                }
        }
}