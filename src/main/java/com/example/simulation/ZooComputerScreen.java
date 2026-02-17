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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

public class ZooComputerScreen extends AbstractContainerScreen<ZooComputerMenu> {
        // --- COLORS & STYLES ---
        private static final int BG_COLOR = 0xCD000000; // Semi Transparent Black
        private static final int SIDEBAR_BG_COLOR = 0xAA1E1E1E; // Semi Transparent Sidebar
        
        private static final int ACTIVE_TAB_COLOR = 0xFF3D3D3D;
        private static final int CARD_BG_COLOR = 0x88252525;
        private static final int CARD_HOVER_COLOR = 0xAA353535;
        private static final int TEXT_PRIMARY = 0xFFFFFFFF;
        private static final int TEXT_SECONDARY = 0xFFAAAAAA;
        private static final int ACCENT_COLOR = 0xFF4A90E2; 
        private static final int GOLD_COLOR = 0xFFFFD700;
        
        private static final int SIDEBAR_WIDTH = 50; 
        private static final int TOP_PADDING = 20;
        private static final int CONTENT_PADDING = 15;

        private int panelWidth = 0;

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
                this.imageWidth = 400; 
                this.imageHeight = 256;
        }

        @Override
        protected void init() {
                this.height = this.minecraft.getWindow().getGuiScaledHeight();
                this.width = this.minecraft.getWindow().getGuiScaledWidth();
                
                // Full screen layout
                this.panelWidth = (int)(this.width * 0.9f); 
                this.imageWidth = this.panelWidth;
                this.imageHeight = this.height - 40;
                
                // Center - No Animation
                this.leftPos = (this.width - this.panelWidth) / 2;
                this.topPos = 20;

                super.init();
                refreshContent();
        }

        private void refreshContent() {
                this.clearWidgets();
                initSidebar();

                int contentStartX = leftPos + SIDEBAR_WIDTH + CONTENT_PADDING;
                int contentWidth = panelWidth - SIDEBAR_WIDTH - (CONTENT_PADDING * 2);

                switch (currentTab) {
                        case DASHBOARD -> {
                                this.scrollOffset = 0;
                                prepareDashboardCollection();
                                initDashboard(contentStartX, topPos + TOP_PADDING, contentWidth);
                        }
                        case ANIMALS -> initShopList("ANIMALS", contentStartX, topPos + TOP_PADDING, contentWidth);
                        case BUILDINGS -> initShopList("BUILDINGS", contentStartX, topPos + TOP_PADDING, contentWidth);
                        case FOOD -> initShopList("FOOD", contentStartX, topPos + TOP_PADDING, contentWidth);
                        case VEHICLES -> initShopList("VEHICLES", contentStartX, topPos + TOP_PADDING, contentWidth);
                }
        }

        private void initSidebar() {
                int x = leftPos;
                int y = topPos + 20;
                int btnSize = 30;
                int gap = 15;
                
                addRenderableWidget(createSidebarBtn(x + 10, y, "🏠", MainTab.DASHBOARD));
                addRenderableWidget(createSidebarBtn(x + 10, y + (btnSize + gap), "🦁", MainTab.ANIMALS));
                addRenderableWidget(createSidebarBtn(x + 10, y + (btnSize + gap) * 2, "🧱", MainTab.BUILDINGS));
                addRenderableWidget(createSidebarBtn(x + 10, y + (btnSize + gap) * 3, "🍎", MainTab.FOOD));
                addRenderableWidget(createSidebarBtn(x + 10, y + (btnSize + gap) * 4, "🚜", MainTab.VEHICLES));
        }

        private Button createSidebarBtn(int x, int y, String icon, MainTab tab) {
                return Button.builder(Component.literal(icon), b -> {
                        this.currentTab = tab;
                        this.currentSubFilter = "ALL";
                        this.scrollOffset = 0;
                        refreshContent();
                }).bounds(x, y, 30, 30)
                  .tooltip(Tooltip.create(Component.literal(tab.name())))
                  .build();
        }

        // ================= DASHBOARD =================
        private void initDashboard(int x, int y, int w) {
                int recruitY = y + 100;
                
                int btnWidth = 100;
                int btnGap = 10;
                
                // Recruit Buttons
                addRenderableWidget(Button.builder(Component.literal("Janitor"), b -> cmd("hire janitor"))
                        .bounds(x, recruitY + 25, btnWidth, 20)
                        .tooltip(Tooltip.create(Component.literal("§eJanitor\n§7Membersihkan sampah\n\n§aRp 2.000.000")))
                        .build());

                addRenderableWidget(Button.builder(Component.literal("Zookeeper"), b -> cmd("hire zookeeper"))
                        .bounds(x + btnWidth + btnGap, recruitY + 25, btnWidth, 20)
                        .tooltip(Tooltip.create(Component.literal("§eZookeeper\n§7Memberi makan hewan\n\n§aRp 2.000.000")))
                        .build());

                addRenderableWidget(Button.builder(Component.literal("Security"), b -> {}) 
                        .bounds(x + (btnWidth + btnGap) * 2, recruitY + 25, btnWidth, 20)
                        .tooltip(Tooltip.create(Component.literal("§cComing Soon")))
                        .build());

                // Animal Grid - Dashboard also 5 cols
                int animalsY = recruitY + 70;
                int colCount = 5;
                int cardGap = 10;
                int cardWidth = (w - (cardGap * (colCount - 1))) / colCount;
                if (cardWidth < 60) cardWidth = 60; 
                int cardHeight = 40;
                
                int maxRows = 3;
                
                List<ShopEntry> animals = currentDisplayList; 
                
                for(int i=0; i < animals.size(); i++) {
                    if (i >= colCount * maxRows) break;
                    
                    int row = i / colCount;
                    int col = i % colCount;
                    
                    int ax = x + col * (cardWidth + cardGap);
                    int ay = animalsY + 30 + row * (cardHeight + cardGap);
                    
                    if (ax + cardWidth > x + w) continue; 
                    
                    addRenderableWidget(new AnimalCard(ax, ay, cardWidth, cardHeight, animals.get(i)));
                }
                
                addRenderableWidget(Button.builder(Component.literal("🔄"),
                                b -> cmd("refresh")).bounds(leftPos + panelWidth - 30, topPos + imageHeight - 30, 20, 20)
                                .tooltip(Tooltip.create(Component.literal("Refresh Data")))
                                .build());
        }

        private void renderDashboardInfo(GuiGraphics g, int x, int y) {
                 // Balance
                String balanceText = formatPrice(ClientZooData.getBalance());
                g.pose().pushPose();
                g.pose().translate(x, y, 0);
                g.pose().scale(2.0f, 2.0f, 2.0f);
                g.drawString(font, balanceText, 0, 0, GOLD_COLOR);
                g.pose().popPose();

                // Stats
                int statsY = y + 35;
                drawStatBox(g, x, statsY, "Staff", String.valueOf(ClientZooData.getStaffCount()));
                drawStatBox(g, x + 60, statsY, "Pengunjung", String.valueOf(ClientZooData.getVisitorCount()));

                // Headers
                int recruitY = y + 100;
                g.drawString(font, "Rekrut", x, recruitY, TEXT_PRIMARY);

                int animalsY = recruitY + 70;
                g.drawString(font, "Hewan (" + currentDisplayList.size() + ")", x, animalsY, TEXT_PRIMARY);
        }

        private void drawStatBox(GuiGraphics g, int x, int y, String label, String value) {
                int size = 50;
                g.fill(x, y, x + size, y + size, CARD_BG_COLOR);
                
                int txtW = font.width(value);
                g.pose().pushPose();
                float scale = 1.5f;
                g.pose().translate(x + (size - txtW * scale)/2, y + 10, 0);
                g.pose().scale(scale, scale, scale);
                g.drawString(font, value, 0, 0, TEXT_PRIMARY, false);
                g.pose().popPose();

                int lblW = font.width(label);
                g.pose().pushPose();
                float scaleSmall = 0.7f;
                g.pose().translate(x + (size - lblW * scaleSmall)/2, y + 35, 0);
                g.pose().scale(scaleSmall, scaleSmall, scaleSmall);
                g.drawString(font, label, 0, 0, TEXT_SECONDARY, false);
                g.pose().popPose();
        }

        // ================= SHOP LIST =================
        private void initShopList(String type, int x, int y, int w) {
                prepareShopData(type);
                
                int listY = y + 40;
                
                List<String> filters = getFiltersForTab(type);
                int fx = x;
                for (String f : filters) {
                        int fw = font.width(f) + 20;
                        boolean isActive = currentSubFilter.equals(f);
                        addRenderableWidget(Button.builder(Component.literal(f), b -> {
                                this.currentSubFilter = f;
                                this.scrollOffset = 0;
                                refreshContent();
                        }).bounds(fx, y, fw, 20).build());
                        fx += fw + 5;
                }

                // Grid 5xN
                int colCount = 5;
                int gap = 10;
                int itemW = (w - (gap * (colCount - 1))) / colCount;
                int itemH = 35; 
                
                int maxRows = (imageHeight - (listY - topPos) - 10) / (itemH + gap);
                int totalItems = currentDisplayList.size();
                int totalRows = (totalItems + colCount - 1) / colCount;

                if (scrollOffset > Math.max(0, totalRows - maxRows)) {
                        scrollOffset = Math.max(0, totalRows - maxRows);
                }

                for (int r = 0; r < maxRows; r++) {
                        int rowIdx = scrollOffset + r;
                        if (rowIdx >= totalRows) break;

                        for (int c = 0; c < colCount; c++) {
                                int idx = rowIdx * colCount + c;
                                if (idx < totalItems) {
                                        ShopEntry entry = currentDisplayList.get(idx);
                                        int bx = x + c * (itemW + gap);
                                        int by = listY + r * (itemH + gap);
                                        
                                        String cmd = (type.equals("ANIMALS")) ? "buy " + entry.id
                                                        : "buyitem " + entry.id + " 1";
                                        
                                        addRenderableWidget(new ShopItemButton(bx, by, itemW, itemH, entry, b -> cmd(cmd)));
                                }
                        }
                }
                
                if (totalRows > maxRows) {
                     int px = leftPos + panelWidth - 25;
                     addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
                            if (scrollOffset > 0) { scrollOffset--; refreshContent(); }
                    }).bounds(px, listY, 20, 20).build());

                    addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
                            if (scrollOffset < totalRows - maxRows) { scrollOffset++; refreshContent(); }
                    }).bounds(px, listY + 25, 20, 20).build());
                }
        }

        // ================= MAIN RENDER =================
        @Override
        public void render(GuiGraphics g, int mx, int my, float partialTick) {
                this.renderBackground(g);
                super.render(g, mx, my, partialTick);
                renderTooltip(g, mx, my);
        }
        
        @Override
        protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
            // Semi Transparent BGs
            g.fill(leftPos, topPos, leftPos + panelWidth, topPos + imageHeight, BG_COLOR); 
            g.fill(leftPos, topPos, leftPos + SIDEBAR_WIDTH, topPos + imageHeight, SIDEBAR_BG_COLOR); 
            
            g.drawString(font, getTabTitle(), leftPos + SIDEBAR_WIDTH + CONTENT_PADDING, topPos + 5, TEXT_SECONDARY);

            if (currentTab == MainTab.DASHBOARD) {
                renderDashboardInfo(g, leftPos + SIDEBAR_WIDTH + CONTENT_PADDING, topPos + TOP_PADDING);
            }
        }
        
        @Override
        protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            // DIBIARKAN KOSONG
            // Ini sengaja di-override kosong agar tulisan default "Inventory" 
            // dan Judul Container tidak muncul menimpa UI kita.
        }

        private String getTabTitle() {
            return switch(currentTab) {
                case DASHBOARD -> "Dashboard";
                case ANIMALS -> "Toko Hewan";
                case BUILDINGS -> "Toko Bangunan";
                case FOOD -> "Toko Makanan";
                case VEHICLES -> "Kendaraan";
            };
        }

        // ================= CUSTOM WIDGETS =================
        
        private class ShopItemButton extends Button {
             private final ShopEntry entry;
             
             public ShopItemButton(int x, int y, int w, int h, ShopEntry entry, OnPress onPress) {
                 super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
                 this.entry = entry;
                 // Colorful & Simple Tooltip
                 this.setTooltip(Tooltip.create(Component.literal(
                     "§e§l" + entry.displayName + "\n" +
                     "§7" + (entry.category != null ? entry.category : "Item") + "\n\n" +
                     "§a" + formatPrice(entry.price)
                 )));
             }
             
             @Override
             public void renderWidget(GuiGraphics g, int mx, int my, float partialTick) {
                 boolean hovered = isHoveredOrFocused();
                 int bgColor = hovered ? CARD_HOVER_COLOR : CARD_BG_COLOR;
                 
                 g.fill(getX(), getY(), getX() + width, getY() + height, bgColor);
                 if (hovered) g.renderOutline(getX(), getY(), width, height, ACCENT_COLOR);
                 
                 // Clean Icon (No Background)
                 int iconSize = height - 4;
                 int iconX = getX() + 2;
                 int iconY = getY() + 2;
                 
                 if (entry.icon != null) {
                     g.pose().pushPose();
                     // Center the item
                     g.renderItem(entry.icon, iconX + (iconSize-16)/2, iconY + (iconSize-16)/2);
                     g.pose().popPose();
                 }
                 
                 // Name (Running Text)
                 int textX = iconX + iconSize + 5;
                 int textY = getY() + (height - 8) / 2;
                 int availW = width - (textX - getX()) - 5;
                 
                 String name = entry.displayName;
                 int strW = font.width(name);
                 
                 g.enableScissor(textX, getY(), getX() + width - 2, getY() + height);
                 g.pose().pushPose();
                 
                 if (strW > availW) {
                     long time = System.currentTimeMillis();
                     int speed = 30; 
                     int cycle = strW + 30;
                     int offset = (int)((time / speed) % cycle);
                     
                     g.drawString(font, name, textX - offset, textY, TEXT_PRIMARY);
                     if (offset > 0) g.drawString(font, name, textX - offset + cycle, textY, TEXT_PRIMARY);
                 } else {
                     g.drawString(font, name, textX, textY, TEXT_PRIMARY);
                 }
                 
                 g.pose().popPose();
                 g.disableScissor();
             }
        }

        private class AnimalCard extends Button {
             ShopEntry entry;
             public AnimalCard(int x, int y, int w, int h, ShopEntry entry) {
                 super(x, y, w, h, Component.empty(), b -> {}, DEFAULT_NARRATION);
                 this.active = false; 
                 this.entry = entry;
             }
             
             @Override
             public void renderWidget(GuiGraphics g, int mx, int my, float partialTick) {
                 g.fill(getX(), getY(), getX() + width, getY() + height, CARD_BG_COLOR);
                 
                 int iconSize = 20;
                 int iconX = getX() + 5;
                 int iconY = getY() + (height - iconSize)/2;
                 if (entry.icon != null) g.renderItem(entry.icon, iconX, iconY);
                 
                 int textX = iconX + iconSize + 5;
                 g.drawString(font, entry.displayName, textX, getY() + 5, TEXT_PRIMARY);
                 
                 String type = entry.category != null ? entry.category : "Animal";
                 g.pose().pushPose();
                 g.pose().translate(textX, getY() + 18, 0);
                 g.pose().scale(0.7f, 0.7f, 0.7f);
                 g.drawString(font, type, 0, 0, TEXT_SECONDARY);
                 g.pose().popPose();
             }
        }

        // ================= HELPERS & DATA =================
        private void cmd(String action) {
                if (action.equals("refresh")) { refreshContent(); return; }
                String c = "zoocmd " + action;
                var conn = Minecraft.getInstance().getConnection();
                if (conn != null) conn.sendCommand(c);
        }

        private String formatPrice(int price) {
                java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
                return "Rp " + nf.format(price);
        }
        
        private List<String> getFiltersForTab(String type) {
                if (type.equals("ANIMALS")) return Arrays.asList("ALL", "LAND", "AQUATIC", "MYTHICAL");
                if (type.equals("BUILDINGS")) return Arrays.asList("ALL", "BLOCKS", "NATURAL", "UTILITY");
                return Arrays.asList("ALL");
        }

        private void prepareDashboardCollection() {
                currentDisplayList.clear();
                net.minecraft.nbt.ListTag list = ClientZooData.getTaggedAnimals();
                for (int i = 0; i < list.size(); i++) {
                        net.minecraft.nbt.CompoundTag tag = list.getCompound(i);
                        String name = tag.getString("name");
                        int id = tag.getInt("id");
                        String type = tag.contains("type") ? tag.getString("type") : "Animal";
                        
                        ItemStack icon = new ItemStack(Items.NAME_TAG);
                         if (tag.contains("type")) {
                            ResourceLocation typeId = ResourceLocation.tryParse(tag.getString("type"));
                            if (typeId != null) {
                                if (ForgeRegistries.ITEMS.containsKey(new ResourceLocation(typeId + "_spawn_egg"))) {
                                    icon = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(typeId + "_spawn_egg")));
                                } else if (ForgeRegistries.ITEMS.containsKey(typeId)) { 
                                     icon = new ItemStack(ForgeRegistries.ITEMS.getValue(typeId));
                                }
                            }
                        }
                        
                        currentDisplayList.add(new ShopEntry(new ResourceLocation("indozoo", "animal_"+id), name, 0, icon, null, type));
                }
        }
        
        private void prepareShopData(String type) {
             currentDisplayList.clear();
             if (type.equals("ANIMALS")) {
                 addAnimalEntry("alexsmobs:elephant", "Elephant", 5000000, "LAND");
                 addAnimalEntry("alexsmobs:tiger", "Tiger", 4500000, "LAND");
                 addAnimalEntry("alexsmobs:grizzly_bear", "Grizzly Bear", 4000000, "LAND");
                 addAnimalEntry("alexsmobs:gorilla", "Gorilla", 3800000, "LAND");
                 addAnimalEntry("naturalist:lion", "Lion", 5000000, "LAND");
                 addAnimalEntry("naturalist:hippo", "Hippo", 4500000, "LAND");
                 addAnimalEntry("alexsmobs:hammerhead_shark", "Hammerhead Shark", 4500000, "AQUATIC");
                 addAnimalEntry("alexsmobs:giant_squid", "Giant Squid", 5000000, "AQUATIC");
                 addAnimalEntry("alexsmobs:void_worm", "Leviathan", 10000000, "MYTHICAL");
             } else if (type.equals("BUILDINGS")) {
                 addShopItem("minecraft:stone_bricks", "Stone Bricks", 500, "BLOCKS");
                 addShopItem("minecraft:oak_log", "Oak Log", 200, "NATURAL");
                 addShopItem("minecraft:iron_bars", "Iron Bars", 1500, "UTILITY");
                 addShopItem("indozoo:zoo_banner", "Zoo Banner", 0, "UTILITY");
                 addShopItem("indozoo:animal_tag", "Animal Tag", 0, "UTILITY");
             }
             
             if (!currentSubFilter.equals("ALL")) {
                 currentDisplayList = currentDisplayList.stream()
                     .filter(e -> e.category != null && (e.category.equalsIgnoreCase(currentSubFilter) || e.category.contains(currentSubFilter)))
                     .collect(Collectors.toList());
             }
        }
        
        private void addAnimalEntry(String id, String name, int price, String cat) {
            ResourceLocation rl = new ResourceLocation(id);
            ItemStack icon = new ItemStack(Items.NAME_TAG);
            if (ForgeRegistries.ITEMS.containsKey(new ResourceLocation(id + "_spawn_egg"))) {
                 icon = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id + "_spawn_egg")));
            }
            currentDisplayList.add(new ShopEntry(rl, name, price, icon, null, cat));
        }
        
        private void addShopItem(String id, String name, int price, String cat) {
             ResourceLocation rl = new ResourceLocation(id);
             ItemStack icon = new ItemStack(Items.BARRIER);
             if (ForgeRegistries.ITEMS.containsKey(rl)) {
                 icon = new ItemStack(ForgeRegistries.ITEMS.getValue(rl));
             }
             currentDisplayList.add(new ShopEntry(rl, name, price, icon, null, cat));
        }

        private static class ShopEntry {
                ResourceLocation id;
                String displayName;
                int price;
                ItemStack icon;
                LivingEntity entityModel; 
                String category;

                public ShopEntry(ResourceLocation id, String name, int price, ItemStack icon, LivingEntity entityModel, String category) {
                        this.id = id;
                        this.displayName = name;
                        this.price = price;
                        this.icon = icon;
                        this.entityModel = entityModel;
                        this.category = category;
                }
        }
}