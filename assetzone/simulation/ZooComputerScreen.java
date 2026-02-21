package com.example.simulation;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class ZooComputerScreen extends AbstractContainerScreen<ZooComputerMenu> {
    // --- COLORS & STYLES ---
    private static final int SIDEBAR_BG_COLOR = 0x881E1E1E; 
    private static final int BG_COLOR = 0xBB1A1A1A;
    
    private static final int ACTIVE_TAB_COLOR = 0xFF3D3D3D;
    private static final int CARD_BG_COLOR = 0x88252525;
    private static final int CARD_HOVER_COLOR = 0xAA353535;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFAAAAAA;
    private static final int ACCENT_COLOR = 0xFF4A90E2; 
    
    private static final int SIDEBAR_WIDTH = 50; 
    private static final int TOP_PADDING = 20;
    private static final int CONTENT_PADDING = 15;

    private int panelWidth = 0;
    private final List<String> staffPanelLines = new ArrayList<>();
    private final Map<String, List<String>> zookeeperByAnimalType = new HashMap<>();

    // --- TABS ---
    private enum MainTab {
        DASHBOARD, ANIMALS, BUILDINGS, FOOD, VEHICLES
    }

    private MainTab currentTab = MainTab.DASHBOARD;
    private int scrollOffset = 0;
    private String currentSubFilter = "ALL";
    private List<ShopEntry> currentDisplayList = new ArrayList<>();

    private static MainTab LAST_TAB = MainTab.DASHBOARD;
    private static final EnumMap<MainTab, String> LAST_SUBFILTER_BY_TAB = new EnumMap<>(MainTab.class);
    private static final EnumMap<MainTab, Integer> LAST_SCROLL_BY_TAB = new EnumMap<>(MainTab.class);

    static {
        for (MainTab t : MainTab.values()) {
            LAST_SUBFILTER_BY_TAB.put(t, "ALL");
            LAST_SCROLL_BY_TAB.put(t, 0);
        }
    }

    // --- RENAME POPUP VARIABLES ---
    private boolean isRenaming = false;
    private int renamingId = -1;
    private String renamingUuid = "";
    private EditBox nameEditField;
    private Button saveNameButton;
    private Button cancelNameButton;
    private EditBox searchBox;

    private boolean tutorialMenuOpen = false;
    private int tutorialButtonX;
    private int tutorialButtonY;
    private int tutorialButtonWidth;
    private int tutorialButtonHeight;

    public ZooComputerScreen(ZooComputerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 400; 
        this.imageHeight = 256;
    }

    @Override
    protected void init() {
        this.height = this.minecraft.getWindow().getGuiScaledHeight();
        this.width = this.minecraft.getWindow().getGuiScaledWidth();
        this.panelWidth = (int)(this.width * 0.9f); 
        this.imageWidth = this.panelWidth;
        this.imageHeight = this.height - 40;
        this.leftPos = (this.width - this.panelWidth) / 2;
        this.topPos = 20;

        super.init();

        this.currentTab = LAST_TAB;
        this.currentSubFilter = LAST_SUBFILTER_BY_TAB.getOrDefault(this.currentTab, "ALL");
        this.scrollOffset = LAST_SCROLL_BY_TAB.getOrDefault(this.currentTab, 0);
        
        // Initialize Rename Popup Widgets
        int popupWidth = 200;
        int popupX = (this.width - popupWidth) / 2;
        int popupY = (this.height - 80) / 2;

        this.nameEditField = new EditBox(this.font, popupX + 10, popupY + 20, 180, 20, Component.literal("Name"));
        this.nameEditField.setMaxLength(32);
        this.nameEditField.setVisible(false);
        this.addRenderableWidget(this.nameEditField);

        this.saveNameButton = Button.builder(Component.literal("Simpan"), b -> saveRename())
                .bounds(popupX + 10, popupY + 50, 85, 20).build();
        this.saveNameButton.visible = false;
        this.addRenderableWidget(this.saveNameButton);

        this.cancelNameButton = Button.builder(Component.literal("Batal"), b -> closeRenamePopup())
                .bounds(popupX + 105, popupY + 50, 85, 20).build();
        this.cancelNameButton.visible = false;
        this.addRenderableWidget(this.cancelNameButton);

        this.searchBox = new EditBox(this.font, leftPos + panelWidth - 110, topPos + 22, 100, 15, Component.literal("Cari..."));
        this.searchBox.setHint(Component.literal("Cari..."));
        this.searchBox.setResponder(s -> {
            scrollOffset = 0;
            refreshContent();
        });
        this.addRenderableWidget(this.searchBox);

        refreshContent();
    }

    @Override
    public void removed() {
        LAST_TAB = currentTab;
        LAST_SUBFILTER_BY_TAB.put(currentTab, currentSubFilter);
        LAST_SCROLL_BY_TAB.put(currentTab, scrollOffset);
        super.removed();
    }

    private long lastDataUpdate = 0;

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.nameEditField != null) this.nameEditField.tick();
        if (this.searchBox != null) this.searchBox.tick();

        if (ClientZooData.lastUpdate > this.lastDataUpdate) {
            this.lastDataUpdate = ClientZooData.lastUpdate;
            refreshContent();
        }
    }

    private void refreshContent() {
        if (isRenaming) return;

        tutorialMenuOpen = false;
        collectStaffPanelData();

        // Simpan widget popup agar tidak hilang saat clear
        List<net.minecraft.client.gui.components.events.GuiEventListener> toKeep = new ArrayList<>();
        toKeep.add(nameEditField);
        toKeep.add(saveNameButton);
        toKeep.add(cancelNameButton);
        toKeep.add(searchBox);
        
        this.clearWidgets();
        
        for(var w : toKeep) { 
            if(w instanceof net.minecraft.client.gui.components.Renderable r) 
                addRenderableWidget((net.minecraft.client.gui.components.AbstractWidget)r); 
        }

        searchBox.visible = (currentTab != MainTab.DASHBOARD && !isRenaming);

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
        
        // Menggunakan SidebarButton custom
        addRenderableWidget(new SidebarButton(x + 10, y, new ItemStack(Items.BOOK), MainTab.DASHBOARD));
        addRenderableWidget(new SidebarButton(x + 10, y + (btnSize + gap), new ItemStack(Items.PIG_SPAWN_EGG), MainTab.ANIMALS));
        addRenderableWidget(new SidebarButton(x + 10, y + (btnSize + gap) * 2, new ItemStack(Items.OAK_PLANKS), MainTab.BUILDINGS));
        addRenderableWidget(new SidebarButton(x + 10, y + (btnSize + gap) * 3, new ItemStack(Items.APPLE), MainTab.FOOD));
        addRenderableWidget(new SidebarButton(x + 10, y + (btnSize + gap) * 4, new ItemStack(Items.MINECART), MainTab.VEHICLES));
    }

    // ================= RENAME LOGIC =================
    private void openRenamePopup(int id, String uuid, String currentName) {
        this.isRenaming = true;
        this.renamingId = id;
        this.renamingUuid = uuid != null ? uuid : "";
        this.nameEditField.setValue(currentName);
        this.nameEditField.setVisible(true);
        this.nameEditField.setFocused(true);
        this.saveNameButton.visible = true;
        this.cancelNameButton.visible = true;
    }

    private void closeRenamePopup() {
        this.isRenaming = false;
        this.renamingId = -1;
        this.renamingUuid = "";
        this.nameEditField.setVisible(false);
        this.saveNameButton.visible = false;
        this.cancelNameButton.visible = false;
    }

    private void saveRename() {
        if (renamingId != -1 || (renamingUuid != null && !renamingUuid.isEmpty())) {
            String newName = nameEditField.getValue();
            if (!newName.trim().isEmpty()) {
                if (renamingUuid != null && !renamingUuid.isEmpty()) {
                    cmd("renameuuid " + renamingUuid + " " + newName);
                    for (ShopEntry entry : currentDisplayList) {
                        if (renamingUuid.equals(entry.dbUuid)) entry.displayName = newName;
                    }
                } else {
                    cmd("rename " + renamingId + " " + newName);
                    for (ShopEntry entry : currentDisplayList) {
                        if (entry.dbId == renamingId) entry.displayName = newName;
                    }
                }
            }
        }
        closeRenamePopup();
    }

    // ================= DASHBOARD =================
    private void initDashboard(int x, int y, int w) {
        int recruitY = y + 108;
        int btnWidth = 112;
        int btnGap = 10;

        int tutWidth = 110;
        int tutHeight = 16;
        int tutX = leftPos + panelWidth - tutWidth - 10;
        int tutY = topPos + TOP_PADDING + 36;
        this.tutorialButtonX = tutX;
        this.tutorialButtonY = tutY;
        this.tutorialButtonWidth = tutWidth;
        this.tutorialButtonHeight = tutHeight;

        addRenderableWidget(new DropdownButton(tutX, tutY, tutWidth, tutHeight, Component.literal("Tutorial & Resep")));

        addRenderableWidget(Button.builder(Component.literal("Janitor"), b -> { if(!isRenaming) cmd("hire janitor"); })
                .bounds(x, recruitY + 25, btnWidth, 20)
                .tooltip(Tooltip.create(Component.literal("§fJanitor\n§7Membersihkan sampah\n\n§aRp 2.000.000")))
                .build());

        addRenderableWidget(Button.builder(Component.literal("Zookeeper"), b -> { if(!isRenaming) cmd("hire zookeeper"); })
                .bounds(x + btnWidth + btnGap, recruitY + 25, btnWidth, 20)
                .tooltip(Tooltip.create(Component.literal("§fZookeeper\n§7Memberi makan hewan\n\n§aRp 2.000.000")))
                .build());

     addRenderableWidget(Button.builder(Component.literal("Security"), b -> { if(!isRenaming) cmd("hire security"); }) 
        .bounds(x + (btnWidth + btnGap) * 2, recruitY + 25, btnWidth, 20)
        .tooltip(Tooltip.create(Component.literal("§9Security\n§7Mengusir Visitor Jahat\n\n§aRp 3.000.000")))
        .build());
        addRenderableWidget(Button.builder(Component.literal("Cashier"), b -> { if(!isRenaming) cmd("hire cashier"); })
                .bounds(x, recruitY + 50, btnWidth, 20)
                .tooltip(Tooltip.create(Component.literal("§eCashier\n§7Auto restock & transaksi shelf\n\n§aRp 2.500.000")))
                .build());

        int animalsY = recruitY + 82;
        int colCount = 5;
        int cardGap = 10;
        int cardWidth = (w - (cardGap * (colCount - 1))) / colCount;
        if (cardWidth < 60) cardWidth = 60; 
        int cardHeight = 74;
        
        int maxRows = 3;
        List<ShopEntry> animals = currentDisplayList; 
        
        for(int i=0; i < animals.size(); i++) {
            if (i >= colCount * maxRows) break;
            int row = i / colCount;
            int col = i % colCount;
            int ax = x + col * (cardWidth + cardGap);
            int ay = animalsY + 28 + row * (cardHeight + cardGap);
            if (ax + cardWidth > x + w) continue; 
            
            addRenderableWidget(new AnimalCard(ax, ay, cardWidth, cardHeight, animals.get(i)));
        }
        
        addRenderableWidget(Button.builder(Component.literal("R"),
                        b -> { if(!isRenaming) cmd("refresh"); }).bounds(leftPos + panelWidth - 30, topPos + imageHeight - 30, 20, 20)
                        .tooltip(Tooltip.create(Component.literal("Refresh Data")))
                        .build());
    }

    private void renderDashboardInfo(GuiGraphics g, int x, int y) {
        String balanceText = formatPrice(ClientZooData.getBalance());
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(1.9f, 1.9f, 1.9f);
        g.drawString(font, balanceText, 0, 0, 0xFF55FF55);
        g.pose().popPose();

        int statsY = y + 38;
        drawStatBox(g, x, statsY, "Staff", String.valueOf(ClientZooData.getStaffCount()));
        drawStatBox(g, x + 60, statsY, "Pengunjung", String.valueOf(ClientZooData.getVisitorCount()));
        drawStatBox(g, x + 120, statsY, "Hewan", String.valueOf(ClientZooData.getAnimalCount()));

        int recruitY = y + 108;
        g.drawString(font, "Rekrut staff", x, recruitY, TEXT_PRIMARY);

        int animalsY = recruitY + 82;
        g.drawString(font, "Hewan (" + currentDisplayList.size() + ")", x, animalsY, TEXT_PRIMARY);

        int panelX = x + 340;
        int panelY = y + 38;
        int panelW = Math.max(260, panelWidth - (panelX - leftPos) - 14);
        int panelH = 172;
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, CARD_BG_COLOR);
        g.renderOutline(panelX, panelY, panelW, panelH, ACCENT_COLOR);
        g.drawString(font, "Staff Operations Panel", panelX + 8, panelY + 8, TEXT_PRIMARY);
        int lineY = panelY + 24;
        if (staffPanelLines.isEmpty()) {
            g.drawString(font, "- Tidak ada data staff aktif", panelX + 8, lineY, TEXT_SECONDARY);
        } else {
            for (int i = 0; i < Math.min(9, staffPanelLines.size()); i++) {
                g.drawString(font, staffPanelLines.get(i), panelX + 8, lineY + (i * 16), TEXT_SECONDARY);
            }
        }
    }

    private void collectStaffPanelData() {
        staffPanelLines.clear();
        zookeeperByAnimalType.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof StaffEntity staff) {
                if (staff.getRole() == 1) {
                    String animal = staff.getAssignedAnimalId().isEmpty() ? "Belum ditugaskan" : prettifyAnimalType(staff.getAssignedAnimalId());
                    String food = staff.getAssignedFoodId().isEmpty() ? "Belum ditentukan" : prettifyAnimalType(staff.getAssignedFoodId());
                    staffPanelLines.add("Zookeeper " + staff.getStaffName() + " | Hewan: " + animal + " | Pakan: " + food);
                    if (!staff.getAssignedAnimalId().isEmpty()) {
                        zookeeperByAnimalType.computeIfAbsent(staff.getAssignedAnimalId(), k -> new ArrayList<>())
                                .add(staff.getStaffName());
                    }
                } else if (staff.getRole() == 2) {
                    staffPanelLines.add("Security Officer " + staff.getStaffName() + " | Status: Siaga");
                }
            } else if (e instanceof CashierEntity cashier) {
                String cashierName = cashier.getName() != null ? cashier.getName().getString() : "Cashier";
                staffPanelLines.add("Cashier " + cashierName + " | Status: Shelf Service Aktif");
            } else if (e instanceof ZookeeperEntity zk) {
                String animal = zk.getAssignedAnimal().isEmpty() ? "Belum ditugaskan" : prettifyAnimalType(zk.getAssignedAnimal());
                staffPanelLines.add("Zookeeper " + zk.getZookeeperName() + " | Hewan: " + animal);
                if (!zk.getAssignedAnimal().isEmpty()) {
                    zookeeperByAnimalType.computeIfAbsent(zk.getAssignedAnimal(), k -> new ArrayList<>())
                            .add(zk.getZookeeperName());
                }
            } else if (e instanceof SecurityEntity sec) {
                staffPanelLines.add("Security Officer " + sec.getSecurityName() + " | Level Alert: " + sec.getAlertLevel());
            }
        }

        // Keep unique lines while preserving first-seen order.
        LinkedHashSet<String> unique = new LinkedHashSet<>(staffPanelLines);
        staffPanelLines.clear();
        staffPanelLines.addAll(unique);
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
            // Menggunakan Custom FilterButton untuk active state
            addRenderableWidget(new FilterButton(fx, y, fw, 20, f));
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
                    
                    addRenderableWidget(new ShopItemButton(bx, by, itemW, itemH, entry, cmd));
                }
            }
        }
        
        if (totalRows > maxRows) {
                int px = leftPos + panelWidth - 25;
                addRenderableWidget(Button.builder(Component.literal("â–²"), b -> {
                    if (scrollOffset > 0 && !isRenaming) { scrollOffset--; refreshContent(); }
                }).bounds(px, listY, 20, 20).build());

            addRenderableWidget(Button.builder(Component.literal("â–¼"), b -> {
                    if (scrollOffset < totalRows - maxRows && !isRenaming) { scrollOffset++; refreshContent(); }
            }).bounds(px, listY + 25, 20, 20).build());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isRenaming) return false; 

        if (currentTab != MainTab.DASHBOARD) {
            if (delta > 0) {
                if (scrollOffset > 0) {
                    scrollOffset--;
                    refreshContent();
                    return true;
                }
            } else if (delta < 0) {
                int listY = topPos + TOP_PADDING + 40;
                int itemH = 35; 
                int gap = 10;
                int maxRows = (imageHeight - (listY - topPos) - 10) / (itemH + gap);
                int colCount = 5;
                int totalItems = currentDisplayList.size();
                int totalRows = (totalItems + colCount - 1) / colCount;
                
                if (scrollOffset < totalRows - maxRows) {
                    scrollOffset++;
                    refreshContent();
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (isRenaming) {
            return nameEditField.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
        }
        if (searchBox != null && searchBox.isFocused()) {
            return searchBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isRenaming) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeRenamePopup();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                saveRename();
                return true;
            }
            return nameEditField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        }
        
        if (searchBox != null && searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchBox.setFocused(false);
                return true;
            }
            return searchBox.keyPressed(keyCode, scanCode, modifiers);
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tutorialMenuOpen && currentTab == MainTab.DASHBOARD && !isRenaming) {
            int menuWidth = 150;
            int entryHeight = 18;
            int menuX = tutorialButtonX;
            int menuY = tutorialButtonY + tutorialButtonHeight + 4;
            int menuHeight = entryHeight * 2;

            boolean insideMenu = mouseX >= menuX && mouseX <= menuX + menuWidth
                    && mouseY >= menuY && mouseY <= menuY + menuHeight;

            if (insideMenu) {
                if (mouseY < menuY + entryHeight) {
                    tutorialMenuOpen = false;
                    if (!isRenaming) {
                        Minecraft.getInstance().setScreen(new ZooRecipeScreen(this));
                    }
                    return true;
                } else {
                    tutorialMenuOpen = false;
                    if (!isRenaming) {
                        Minecraft.getInstance().setScreen(new ZooRatingInfoScreen(this));
                    }
                    return true;
                }
            } else {
                tutorialMenuOpen = false;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        this.renderBackground(g);
        if (currentTab == MainTab.DASHBOARD) {
             drawDashboardRating(g, leftPos + SIDEBAR_WIDTH + CONTENT_PADDING, topPos + TOP_PADDING, panelWidth - SIDEBAR_WIDTH - CONTENT_PADDING * 2);
        }
        super.render(g, mx, my, partialTick);
        
        if (isRenaming) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 600); 
            g.fill(0, 0, width, height, 0x88000000);
            
            int w = 200;
            int h = 80;
            int x = (width - w) / 2;
            int y = (height - h) / 2;
            
            g.fill(x, y, x + w, y + h, 0xFF2A2A2A);
            g.renderOutline(x, y, w, h, 0xFFFFFFFF);
            g.drawCenteredString(font, "Edit Nama Hewan", x + w/2, y + 5, 0xFFFFFFFF);
            
            this.nameEditField.render(g, mx, my, partialTick);
            this.saveNameButton.render(g, mx, my, partialTick);
            this.cancelNameButton.render(g, mx, my, partialTick);
            g.pose().popPose();
        } else {
            if (tutorialMenuOpen && currentTab == MainTab.DASHBOARD) {
                int menuWidth = 150;
                int entryHeight = 22;
                int menuX = tutorialButtonX;
                int menuY = tutorialButtonY + tutorialButtonHeight + 4;
                int menuHeight = entryHeight * 2;

                g.pose().pushPose();
                g.pose().translate(0, 0, 300);

                int bgColor = 0xFF2A2A2A;
                int borderColor = 0xFF555555;
                int hoverColor = 0xFF3A3A3A;

                boolean hoverFirst = mx >= menuX && mx <= menuX + menuWidth && my >= menuY && my <= menuY + entryHeight;
                boolean hoverSecond = mx >= menuX && mx <= menuX + menuWidth && my >= menuY + entryHeight && my <= menuY + menuHeight;

                g.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, bgColor);
                g.renderOutline(menuX, menuY, menuWidth, menuHeight, borderColor);

                if (hoverFirst) {
                    g.fill(menuX + 1, menuY + 1, menuX + menuWidth - 1, menuY + entryHeight - 1, hoverColor);
                }
                if (hoverSecond) {
                    g.fill(menuX + 1, menuY + entryHeight + 1, menuX + menuWidth - 1, menuY + menuHeight - 1, hoverColor);
                }

                String opt1 = "Recipe";
                String opt2 = "Cara Naikkan Rating";
                int textBaseY = menuY + (entryHeight - font.lineHeight) / 2;
                int textX = menuX + 12;
                g.drawString(font, opt1, textX, textBaseY, 0xFFFFFFFF);
                g.drawString(font, opt2, textX, textBaseY + entryHeight, 0xFFFFFFFF);

                g.pose().popPose();
            }
            renderTooltip(g, mx, my);
        }
    }

    private class DropdownButton extends AbstractButton {
        private final Component label;

        public DropdownButton(int x, int y, int width, int height, Component label) {
            super(x, y, width, height, label);
            this.label = label;
        }

        @Override
        public void onPress() {
            if (!isRenaming) {
                tutorialMenuOpen = !tutorialMenuOpen;
            }
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            int w = this.width;
            int h = this.height;

            int bgColor = isHoveredOrFocused() ? 0xFF3A3A3A : 0xFF2A2A2A;
            int borderColor = 0xFF555555;

            g.fill(x, y, x + w, y + h, bgColor);
            g.renderOutline(x, y, w, h, borderColor);

            int textPaddingX = 12;
            int textPaddingY = 8;
            int textY = y + (h - font.lineHeight) / 2;
            int textX = x + textPaddingX;
            g.drawString(font, label.getString(), textX, textY, 0xFFFFFFFF);

            String chevron = tutorialMenuOpen ? "^" : "v";
            int chevronWidth = font.width(chevron);
            int chevronX = x + w - chevronWidth - textPaddingX;
            g.drawString(font, chevron, chevronX, textY, 0xFFAAAAAA);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }
    }
    
    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        g.fill(leftPos, topPos, leftPos + panelWidth, topPos + imageHeight, BG_COLOR); 
        g.fill(leftPos, topPos, leftPos + SIDEBAR_WIDTH, topPos + imageHeight, SIDEBAR_BG_COLOR); 
        g.renderOutline(leftPos, topPos, panelWidth, imageHeight, 0xFF3D3D3D);
        
        g.drawString(font, getTabTitle(), leftPos + SIDEBAR_WIDTH + CONTENT_PADDING, topPos + 5, TEXT_SECONDARY);

        if (currentTab == MainTab.DASHBOARD) {
            renderDashboardInfo(g, leftPos + SIDEBAR_WIDTH + CONTENT_PADDING, topPos + TOP_PADDING);
        }
    }

    private void drawDashboardRating(GuiGraphics g, int x, int y, int w) {
        int rating = ClientZooData.getRating();
        String text = "Rating " + rating + "/100";
        int strW = font.width(text);
        
        // Right align text
        int textX = x + w - strW;
        int textY = y;
        
        g.drawString(font, text, textX, textY, TEXT_PRIMARY);
        
        // Draw stars to the left of text or below? User said "Judul Rating 100/100"
        // I'll put stars below the text
        int starSize = 16;
        int starsW = 5 * (starSize - 2);
        int starsX = x + w - starsW; // Align right
        int starsY = textY + 12;
        
        for (int i = 0; i < 5; i++) {
            int threshold = (i + 1) * 20;
            boolean active = rating >= threshold;
            float alpha = active ? 1.0F : 0.5F;
            
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            
            g.renderItem(new ItemStack(Items.NETHER_STAR), starsX + (i * (starSize - 2)), starsY);
            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }

    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
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

    private void cmd(String action) {
        String c = "zoocmd " + action;
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) conn.sendCommand(c);
    }
    
    private String formatPrice(int price) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
        return "Rp " + nf.format(price);
    }

    private String prettifyAnimalType(String rawId) {
        if (rawId == null || rawId.isEmpty()) return "Unknown";
        String path = rawId;
        if (rawId.contains(":")) {
            path = rawId.substring(rawId.indexOf(':') + 1);
        }
        return formatName(path);
    }

    private String getKeeperNamesForAnimal(String animalTypeId) {
        if (animalTypeId == null || animalTypeId.isEmpty()) return "Belum ada zookeeper";
        List<String> names = zookeeperByAnimalType.get(animalTypeId);
        if (names == null || names.isEmpty()) return "Belum ada zookeeper";
        LinkedHashSet<String> unique = new LinkedHashSet<>(names);
        return String.join(", ", unique);
    }

    private static int hungerToPercent(int hungerValue) {
        int safe = Math.max(0, Math.min(20, hungerValue));
        return ((20 - safe) * 100) / 20;
    }

private List<String> getFiltersForTab(String type) {
        // Ubah tulisan di sini jadi "Land", "Aquatic" (bukan "LAND")
        if (type.equals("ANIMALS")) return Arrays.asList("Land", "Aquatic", "Bugs", "Mythical");
        if (type.equals("BUILDINGS")) return Arrays.asList("Block", "Natural", "Item");
        return Arrays.asList("All");
    }

    private void prepareDashboardCollection() {
        currentDisplayList.clear();
        try {
            net.minecraft.nbt.ListTag list = ClientZooData.getTaggedAnimals();
            for (int i = 0; i < list.size(); i++) {
                net.minecraft.nbt.CompoundTag tag = list.getCompound(i);
                String name = tag.contains("name") ? tag.getString("name") : "Unnamed";
                int id = tag.getInt("id"); 
                String uuid = tag.contains("uuid") ? tag.getString("uuid") : "";
                String typeRaw = tag.contains("type") ? tag.getString("type") : "animal";
                String typeDisplay = prettifyAnimalType(typeRaw);
                int hunger = tag.contains("hunger") ? Math.max(0, Math.min(20, tag.getInt("hunger"))) : 20;
                String keeperName = getKeeperNamesForAnimal(typeRaw);

                ShopEntry entry = new ShopEntry(new ResourceLocation("indozoo", "animal_" + id), name, 0, ItemStack.EMPTY, null, typeDisplay);
                entry.dbId = id; 
                entry.dbUuid = uuid;
                entry.originalName = typeDisplay;
                entry.hunger = hunger;
                entry.zookeeperName = keeperName;
                currentDisplayList.add(entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
private void prepareShopData(String type) {
    currentDisplayList.clear();

    // Default sub filter
    if (currentSubFilter.equals("ALL") || currentSubFilter.isEmpty()) {
        if (type.equals("ANIMALS")) currentSubFilter = "LAND";
        else if (type.equals("BUILDINGS")) currentSubFilter = "BLOCK";
        else if (type.equals("FOOD")) currentSubFilter = "ALL";
    }

    /* =======================
       ANIMALS (Zoo Tycoon)
       NPC Ticket = 20.000
       ======================= */
    if (type.equals("ANIMALS")) {
        for (var e : AnimalRegistry.getAllAnimals().entrySet()) {
            AnimalRegistry.AnimalData ad = e.getValue();
            String cat = switch (ad.category) {
                case LAND -> "LAND";
                case AQUATIC -> "AQUATIC";
                case MYTHICAL -> "MYTHICAL";
                case BUG -> "BUGS";
            };
            addAnim(e.getKey().toString(), ad.displayName, ad.price, cat);
        }
    }

    /* =======================
       BUILDINGS
       ======================= */
    else if (type.equals("BUILDINGS")) {
        for (var e : ZooItemRegistry.getAllItems().entrySet()) {
            ZooItemRegistry.ItemData d = e.getValue();
            String cat = switch (d.category) {
                case BUILDING -> "BLOCK";
                case NATURAL -> "NATURAL";
                case UTILITY -> "ITEM";
                default -> null;
            };
            if (cat == null) continue;
            currentDisplayList.add(new ShopEntry(e.getKey(), d.displayName, d.price, new ItemStack(d.item), null, cat));
        }
    }

    /* =======================
       FOOD (Consumable)
       ======================= */
    else if (type.equals("FOOD")) {
        for (var e : ZooItemRegistry.getAllItems().entrySet()) {
            ZooItemRegistry.ItemData d = e.getValue();
            if (d.category != ZooItemRegistry.Category.FOOD) continue;
            currentDisplayList.add(new ShopEntry(e.getKey(), d.displayName, d.price, new ItemStack(d.item), null, "FOOD"));
        }
    }
    else if (type.equals("VEHICLES")) {
        for (var e : ZooItemRegistry.getAllItems().entrySet()) {
            ZooItemRegistry.ItemData d = e.getValue();
            if (d.category != ZooItemRegistry.Category.VEHICLE) continue;
            currentDisplayList.add(new ShopEntry(e.getKey(), d.displayName, d.price, new ItemStack(d.item), null, "VEHICLE"));
        }
    }

    /* =======================
       FILTER & SEARCH
       ======================= */
    if (!currentSubFilter.equals("ALL")) {
        currentDisplayList = currentDisplayList.stream()
                .filter(e -> e.category != null &&
                        (e.category.equalsIgnoreCase(currentSubFilter)
                                || e.category.contains(currentSubFilter)))
                .collect(Collectors.toList());
    }

    if (searchBox != null && !searchBox.getValue().isEmpty()) {
        String q = searchBox.getValue().toLowerCase();
        currentDisplayList = currentDisplayList.stream()
                .filter(e -> e.displayName.toLowerCase().contains(q))
                .collect(Collectors.toList());
    }
}

    
    private void addAnim(String id, String name, int price, String cat) {
        String ns = "minecraft";
        String path = id;
        if (id.contains(":")) {
            String[] s = id.split(":");
            ns = s[0];
            path = s[1];
        }
        ResourceLocation rl = new ResourceLocation(ns, path);
        ItemStack icon = new ItemStack(Items.FOX_SPAWN_EGG); 
        
        List<ResourceLocation> potentials = new ArrayList<>();
        potentials.add(new ResourceLocation(ns, path + "_spawn_egg"));
        potentials.add(new ResourceLocation(ns, "spawn_egg_" + path));
        
        for (ResourceLocation eggId : potentials) {
            if (ForgeRegistries.ITEMS.containsKey(eggId)) {
                icon = new ItemStack(ForgeRegistries.ITEMS.getValue(eggId));
                break;
            }
        }
        
        LivingEntity model = null;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);
        if (type != null) {
            try {
                Entity e = type.create(Minecraft.getInstance().level);
                if (e instanceof LivingEntity le) {
                    model = le;
                }
            } catch (Exception e) {}
        }
        
        currentDisplayList.add(new ShopEntry(rl, name, price, icon, model, cat));
    }

    private void addItem(String id, String name, int price, String cat) {
        String ns = "minecraft";
        String path = id;
        if (id.contains(":")) {
            String[] s = id.split(":");
            ns = s[0];
            path = s[1];
        }
        ResourceLocation rl = new ResourceLocation(ns, path);
        if (ForgeRegistries.ITEMS.containsKey(rl)) {
            currentDisplayList.add(new ShopEntry(rl, name, price, new ItemStack(ForgeRegistries.ITEMS.getValue(rl)), null, cat));
        }
    }
    
    private void addBlockGroup(String sub, String cat) {
        String[] forbidden = {"copper", "blackstone", "deepslate", "granite", "diorite", "wooden", "tool", "axe", "pickaxe", "shovel", "hoe", "sword"};
        for (ResourceLocation loc : ForgeRegistries.ITEMS.getKeys()) {
            if (loc.getPath().contains(sub)) {
                boolean skip = false;
                for (String f : forbidden) if (loc.getPath().contains(f)) skip = true;
                if (skip) continue;
                
                addItem(loc.toString(), formatName(loc.getPath()), 100, cat);
            }
        }
    }
    
    private String formatName(String s) {
        String clean = s.replace("_", " ");
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String part : clean.split(" ")) {
            if (!part.isEmpty()) {
                if (!first) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1).toLowerCase());
                first = false;
            }
        }
        return sb.toString();
    }

    private void addFood(String id, String name, int price, String... entities) {
        String ns = "minecraft";
        String path = id;
        if (id.contains(":")) {
            String[] s = id.split(":");
            ns = s[0];
            path = s[1];
        }
        ResourceLocation rl = new ResourceLocation(ns, path);
        
        if (ForgeRegistries.ITEMS.containsKey(rl)) {
            currentDisplayList.add(new ShopEntry(rl, name, price, new ItemStack(ForgeRegistries.ITEMS.getValue(rl)), null, "FOOD"));
        }
    }
    
    // --- INNER CLASSES ---

    // 1. Sidebar Tab Button
    private class SidebarButton extends AbstractButton {
        private final MainTab tab;
        private final ItemStack iconStack;

        public SidebarButton(int x, int y, ItemStack stack, MainTab tab) {
            super(x, y, 30, 30, Component.empty());
            this.tab = tab;
            this.iconStack = stack;
            this.setTooltip(Tooltip.create(Component.literal(tab.name())));
        }

        @Override
        public void onPress() {
            if (isRenaming) return;
            LAST_TAB = currentTab;
            LAST_SUBFILTER_BY_TAB.put(currentTab, currentSubFilter);
            LAST_SCROLL_BY_TAB.put(currentTab, scrollOffset);
            currentTab = tab;
            currentSubFilter = LAST_SUBFILTER_BY_TAB.getOrDefault(currentTab, "ALL");
            scrollOffset = LAST_SCROLL_BY_TAB.getOrDefault(currentTab, 0);
            refreshContent();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {}

        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float partialTick) {
            boolean isActive = (currentTab == tab);
            boolean hovered = isHoveredOrFocused() && !isRenaming;

            int fillColor = isActive ? ACTIVE_TAB_COLOR : (hovered ? CARD_HOVER_COLOR : SIDEBAR_BG_COLOR);
            g.fill(getX(), getY(), getX() + width, getY() + height, fillColor);

            if (isActive || hovered) {
                g.renderOutline(getX(), getY(), width, height, ACCENT_COLOR);
            }

            if (!iconStack.isEmpty()) {
                g.renderItem(iconStack, getX() + (width - 16) / 2, getY() + (height - 16) / 2);
            }
        }
    }

    // 2. Shop Filter Button (NEW)
    private class FilterButton extends AbstractButton {
        private final String filterName;

        public FilterButton(int x, int y, int w, int h, String filterName) {
            super(x, y, w, h, Component.literal(filterName));
            this.filterName = filterName;
        }

        @Override
        public void onPress() {
            if (isRenaming) return;
            currentSubFilter = filterName.toUpperCase(Locale.ROOT);
            LAST_SUBFILTER_BY_TAB.put(currentTab, currentSubFilter);
            scrollOffset = 0;
            LAST_SCROLL_BY_TAB.put(currentTab, scrollOffset);
            refreshContent();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {}

        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float partialTick) {
            boolean isActive = currentSubFilter.equalsIgnoreCase(filterName);
            boolean hovered = isHoveredOrFocused() && !isRenaming;

            int color = isActive ? ACCENT_COLOR : (hovered ? CARD_HOVER_COLOR : CARD_BG_COLOR);

            g.fill(getX(), getY(), getX() + width, getY() + height, color);
            if (isActive) {
                g.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
            }

            int textColor =  0xFFFFFFFF; 
            int strW = font.width(getMessage());
            g.drawString(font, getMessage(), getX() + (width - strW) / 2, getY() + (height - 8) / 2, textColor);
        }
    }
    
    // 3. Shop Item Button
    private class ShopItemButton extends AbstractButton {
         private final ShopEntry entry;
         private final String command;
         
         public ShopItemButton(int x, int y, int w, int h, ShopEntry entry, String command) {
             super(x, y, w, h, Component.empty());
             this.entry = entry;
             this.command = command;
             String tooltip = entry.displayName + "\n§a" + formatPrice(entry.price);
            if ("FOOD".equalsIgnoreCase(entry.category) && entry.icon != null && !entry.icon.isEmpty()) {
                 tooltip += "\n" + FoodAnimalRegistry.getFoodTooltip(entry.icon.getItem());
             }
             this.setTooltip(Tooltip.create(Component.literal(tooltip)));
         }
         
         @Override
         public void onPress() {
             if (!isRenaming) cmd(this.command);
         }
         
         @Override
         protected void updateWidgetNarration(NarrationElementOutput output) {
         }

         @Override
         public void renderWidget(GuiGraphics g, int mx, int my, float partialTick) {
             boolean hovered = isHoveredOrFocused() && !isRenaming;
             int bgColor = hovered ? CARD_HOVER_COLOR : CARD_BG_COLOR;
             g.fill(getX(), getY(), getX() + width, getY() + height, bgColor);
             if (hovered) g.renderOutline(getX(), getY(), width, height, ACCENT_COLOR);
             
             int iconSize = height - 4; 
             int iconX = getX() + 2;
             int iconY = getY() + 2;
             
             if (entry.icon != null) {
                 g.pose().pushPose();
                 g.renderItem(entry.icon, iconX + (iconSize-16)/2, iconY + (iconSize-16)/2);
                 g.pose().popPose();
             }
             
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
             
             if (hovered) {
                 if (entry.entityModel != null) {
                     renderEntityTooltip(g, mx, my, entry.entityModel);
                 }
             }
         }

        private void renderEntityTooltip(GuiGraphics g, int mx, int my, LivingEntity entity) {
              int boxSize = 100; 
              int tx = leftPos + panelWidth - boxSize - 20;
              int ty = topPos + imageHeight - boxSize - 20;
              
              g.pose().pushPose();
              g.pose().translate(0, 0, 500);
              
              g.fill(tx, ty, tx + boxSize, ty + boxSize, 0xEE000000);
              g.renderOutline(tx, ty, boxSize, boxSize, ACCENT_COLOR);
              
              try {
                  float scale = 40f;
                  if (entity.getBbHeight() > 2.0) scale = 25f;
                  if (entity.getBbHeight() < 0.5) scale = 60f;
                  
                  // Render entity facing slightly forward, no mouse follow
                  InventoryScreen.renderEntityInInventoryFollowsMouse(g, tx + boxSize/2, ty + boxSize - 15, (int)scale, 40, 0, entity);
              } catch (Exception e) {}
              
              g.pose().popPose();
        }
    }

    // 4. Animal Card (Overview)
    private class AnimalCard extends AbstractButton {
        private static final int ACTION_WIDTH = 34;
        private static final int ACTION_HEIGHT = 12;

        private final ShopEntry entry;
        private boolean isHoveringEdit = false;
        private boolean isHoveringDelete = false;

        public AnimalCard(int x, int y, int w, int h, ShopEntry entry) {
            super(x, y, w, h, Component.empty());
            this.entry = entry;
        }

        @Override
        public void onPress() {
            if (isHoveringEdit && !isRenaming && (entry.dbId != -1 || (entry.dbUuid != null && !entry.dbUuid.isEmpty()))) {
                openRenamePopup(entry.dbId, entry.dbUuid, entry.displayName);
            } else if (isHoveringDelete && !isRenaming && (entry.dbId != -1 || (entry.dbUuid != null && !entry.dbUuid.isEmpty()))) {
                if (entry.dbUuid != null && !entry.dbUuid.isEmpty()) {
                    cmd("releaseuuid " + entry.dbUuid);
                } else {
                    cmd("release " + entry.dbId);
                }
                refreshContent();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }

        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float partialTick) {
            boolean hovered = isHoveredOrFocused() && !isRenaming;
            int cardColor = hovered ? CARD_HOVER_COLOR : CARD_BG_COLOR;

            g.fill(getX(), getY(), getX() + width, getY() + height, cardColor);
            if (hovered) {
                g.renderOutline(getX(), getY(), width, height, ACCENT_COLOR);
            }

            int textX = getX() + 6;
            int textW = width - 12;

            drawInfoLine(g, "Tag", entry.displayName, textX, getY() + 6, textW, 0xFFFFD369);
            drawInfoLine(g, "Spesies", entry.originalName, textX, getY() + 17, textW, TEXT_PRIMARY);
            drawInfoLine(g, "Zookeeper", entry.zookeeperName, textX, getY() + 28, textW, TEXT_SECONDARY);

            int hungryPercent = hungerToPercent(entry.hunger);
            int barX = textX;
            int barY = getY() + 41;
            int barW = textW;
            int barH = 6;
            renderHungryBar(g, barX, barY, barW, barH, hungryPercent);
            g.drawString(font, "Hungry: " + hungryPercent + "%", textX, getY() + 50, 0xFFE5E5E5, false);

            if (hovered) {
                renderActionButtons(g, mx, my);
                renderHoverTooltip(g, mx, my, hungryPercent);
            } else {
                isHoveringEdit = false;
                isHoveringDelete = false;
            }
        }

        private void renderActionButtons(GuiGraphics g, int mx, int my) {
            int actionY = getY() + height - ACTION_HEIGHT - 4;
            int editX = getX() + width - (ACTION_WIDTH * 2) - 7;
            int deleteX = getX() + width - ACTION_WIDTH - 4;

            isHoveringEdit = mx >= editX && mx <= editX + ACTION_WIDTH && my >= actionY && my <= actionY + ACTION_HEIGHT;
            isHoveringDelete = mx >= deleteX && mx <= deleteX + ACTION_WIDTH && my >= actionY && my <= actionY + ACTION_HEIGHT;

            int editBg = isHoveringEdit ? 0xFF3C7BFF : 0x882D2D2D;
            int deleteBg = isHoveringDelete ? 0xFFD14D4D : 0x882D2D2D;
            g.fill(editX, actionY, editX + ACTION_WIDTH, actionY + ACTION_HEIGHT, editBg);
            g.fill(deleteX, actionY, deleteX + ACTION_WIDTH, actionY + ACTION_HEIGHT, deleteBg);
            g.renderOutline(editX, actionY, ACTION_WIDTH, ACTION_HEIGHT, 0xFF666666);
            g.renderOutline(deleteX, actionY, ACTION_WIDTH, ACTION_HEIGHT, 0xFF666666);

            g.drawString(font, "Edit", editX + 8, actionY + 2, 0xFFFFFFFF, false);
            g.drawString(font, "Hapus", deleteX + 5, actionY + 2, 0xFFFFFFFF, false);
        }

        private void renderHoverTooltip(GuiGraphics g, int mx, int my, int hungryPercent) {
            int tooltipW = 185;
            int tooltipH = 54;
            int tx = mx + 12;
            int ty = my + 10;
            if (tx + tooltipW > ZooComputerScreen.this.width - 4) tx = mx - tooltipW - 12;
            if (ty + tooltipH > ZooComputerScreen.this.height - 4) ty = my - tooltipH - 10;
            tx = Math.max(4, tx);
            ty = Math.max(4, ty);

            g.fill(tx, ty, tx + tooltipW, ty + tooltipH, 0xE0101010);
            g.renderOutline(tx, ty, tooltipW, tooltipH, ACCENT_COLOR);
            g.drawString(font, "Aksi: Klik tombol Edit/Hapus", tx + 6, ty + 6, 0xFFDFDFDF, false);
            g.drawString(font, "Hungry: " + hungryPercent + "%", tx + 6, ty + 18, 0xFFDFDFDF, false);
            String keeperLine = "Zookeeper: " + (entry.zookeeperName == null ? "-" : entry.zookeeperName);
            g.drawString(font, trimToWidth(keeperLine, tooltipW - 10), tx + 6, ty + 30, 0xFFBFBFBF, false);
            g.drawString(font, "Spesies: " + trimToWidth(entry.originalName, tooltipW - 58), tx + 6, ty + 42, 0xFFBFBFBF, false);
        }

        private void drawInfoLine(GuiGraphics g, String label, String value, int x, int y, int maxWidth, int color) {
            String line = label + ": " + (value == null || value.isEmpty() ? "-" : value);
            g.drawString(font, trimToWidth(line, maxWidth), x, y, color, false);
        }

        private void renderHungryBar(GuiGraphics g, int x, int y, int w, int h, int hungryPercent) {
            g.fill(x, y, x + w, y + h, 0xFF2A2A2A);
            int fill = Math.max(0, Math.min(w, (w * hungryPercent) / 100));
            int color;
            if (hungryPercent >= 65) {
                color = 0xFFD94B4B;
            } else if (hungryPercent >= 35) {
                color = 0xFFE6C14C;
            } else {
                color = 0xFF5CCF6B;
            }
            g.fill(x, y, x + fill, y + h, color);
            g.renderOutline(x, y, w, h, 0xFF555555);
        }

        private String trimToWidth(String value, int maxWidth) {
            if (value == null || value.isEmpty()) return "";
            if (font.width(value) <= maxWidth) return value;
            String suffix = "...";
            int end = value.length();
            while (end > 0 && font.width(value.substring(0, end) + suffix) > maxWidth) {
                end--;
            }
            if (end <= 0) return suffix;
            return value.substring(0, end) + suffix;
        }
    }

    private static class ShopEntry {
        ResourceLocation id;
        String displayName;
        int price;
        ItemStack icon;
        LivingEntity entityModel;
        String category;
        List<EntityType<?>> targets;
        int dbId = -1;
        String dbUuid = "";
        String originalName = "";
        int hunger = 20;
        String zookeeperName = "Belum ada zookeeper";

        public ShopEntry(ResourceLocation id, String name, int price, ItemStack icon, LivingEntity entityModel, String category) {
            this.id = id;
            this.displayName = name;
            this.price = price;
            this.icon = icon;
            this.entityModel = entityModel;
            this.category = category;
            this.targets = null;
            this.originalName = name;
        }
    }
}
