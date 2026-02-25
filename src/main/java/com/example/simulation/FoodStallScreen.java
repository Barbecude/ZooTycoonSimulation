package com.example.simulation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * GUI screen for the Food Stall.
 * Left  : Queue overlay panel (when visitors are queued).
 * Center: 3x3 inventory grid (dispenser texture).
 * Right : "Price List" / "Bahan" tabs.
 *   - Price List : fixed 3 foods with per-item +/- price controls.
 *   - Bahan      : 6 raw materials from the 3 recipes.
 */
public class FoodStallScreen extends AbstractContainerScreen<FoodStallMenu> {

    private static final ResourceLocation TEXTURE = Objects.requireNonNull(
            ResourceLocation.tryParse("minecraft:textures/gui/container/dispenser.png"));

    private static final int PANEL_W      = 160;
    private static final int PANEL_GAP    = 4;
    private static final int ROW_H        = 30;
    private static final int CONTENT_TOP  = 24;

    // ---- Fixed food catalog (only 3 items) --------------------------------

    record CatalogEntry(String itemId, String name, int price) {}

    static final List<CatalogEntry> CATALOG = List.of(
        new CatalogEntry("indozoo:fd_hamburger",         "Burger",           8500),
        new CatalogEntry("indozoo:fd_chicken_sandwich",  "Chicken Sandwich", 7000),
        new CatalogEntry("indozoo:fd_hot_cocoa",         "Chocolate Milk",   5000)
    );

    static final Map<String, String> RECIPES = Map.of(
        "indozoo:fd_hamburger",         "Steak + Cabbage Leaf + Bread",
        "indozoo:fd_chicken_sandwich",  "Chicken + Bread + Cabbage Leaf",
        "indozoo:fd_hot_cocoa",         "Milk Bucket + Cocoa"
    );

    // ---- Fallback textures for items that may not be in the registry --------

    private static final java.util.Map<String, ResourceLocation> ITEM_TEX_FALLBACK = java.util.Map.of(
        "farmersdelight:cabbage_leaf",
            Objects.requireNonNull(ResourceLocation.tryParse("indozoo:textures/gui/items/cabbage_leaf.png"))
    );

    // ---- Bahan (ingredients for the 3 recipes) ----------------------------

    static final List<CatalogEntry> BAHAN_BAKU = List.of(
        new CatalogEntry("minecraft:bread",                   "Bread",          500),
        new CatalogEntry("minecraft:cooked_beef",             "Steak",          800),
        new CatalogEntry("minecraft:cooked_chicken",          "Cooked Chicken", 600),
        new CatalogEntry("minecraft:milk_bucket",             "Milk Bucket",    400),
        new CatalogEntry("minecraft:cocoa_beans",             "Cocoa",          350),
        new CatalogEntry("farmersdelight:cabbage_leaf",       "Cabbage Leaf",   300)
    );

    // ---- State -----------------------------------------------------------

    /** 0 = Price List, 1 = Bahan */
    private int    activeTab    = 0;
    private Button tabPriceListBtn;
    private Button tabBahanBtn;

    // Price List: +/- buttons for each food
    private final Button[] plusBtns  = new Button[3];
    private final Button[] minusBtns = new Button[3];

    // Bahan buy buttons
    private final List<Button> bahanBuyBtns = new ArrayList<>();

    // ---- Queue overlay state (updated by FoodStallQueueSyncPacket) --------
    private int    queueSize     = 0;
    private String moodLabel     = "";
    private String requestItemId = "";

    private Button layaniBtn;

    // -----------------------------------------------------------------------

    public FoodStallScreen(FoodStallMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth  = 176;
        this.imageHeight = 166;
    }

    /** Called from FoodStallQueueSyncPacket handler when this screen is open. */
    public void updateQueueInfo(int queueSize, String moodLabel, String requestItemId) {
        this.queueSize     = queueSize;
        this.moodLabel     = moodLabel;
        this.requestItemId = requestItemId == null ? "" : requestItemId;
    }

    // ---- Helpers ---------------------------------------------------------

    private int px() { return this.leftPos + this.imageWidth + PANEL_GAP; }
    private int py() { return this.topPos; }

    /** Left panel X position for queue overlay. */
    private int qx() { return Math.max(2, this.leftPos - 140); }

    private ItemStack stackFor(String itemId) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(itemId));
        return (item == null) ? ItemStack.EMPTY : new ItemStack(item, 1);
    }

    /** Check if the stall inventory contains at least one of the given item. */
    private boolean isStocked(String itemId) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                String id = Objects.toString(ForgeRegistries.ITEMS.getKey(stack.getItem()), "");
                if (id.equals(itemId)) return true;
            }
        }
        return false;
    }

    private int getStockCount(String itemId) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                String id = Objects.toString(ForgeRegistries.ITEMS.getKey(stack.getItem()), "");
                if (id.equals(itemId)) count += stack.getCount();
            }
        }
        return count;
    }

    private int getCurrentPrice(String itemId) {
        return menu.getItemPrice(itemId);
    }

    // ---- Init ------------------------------------------------------------

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        int px = px(), py = py();

        // Two tabs: Price List (80px), Bahan (78px)
        tabPriceListBtn = Button.builder(Component.literal("Price List"), b -> switchTab(0))
                .bounds(px, py, 80, 18).build();
        tabBahanBtn     = Button.builder(Component.literal("Bahan"),      b -> switchTab(1))
                .bounds(px + 82, py, 78, 18).build();
        addRenderableWidget(tabPriceListBtn);
        addRenderableWidget(tabBahanBtn);

        // Price List: +/- buttons for each food
        for (int i = 0; i < CATALOG.size(); i++) {
            final String foodId = CATALOG.get(i).itemId();
            plusBtns[i] = Button.builder(Component.literal("+"),
                    b -> adjustPrice(foodId, +500))
                    .bounds(px + PANEL_W - 42, py + CONTENT_TOP + i * ROW_H + 6, 18, 16).build();
            minusBtns[i] = Button.builder(Component.literal("\u2212"),
                    b -> adjustPrice(foodId, -500))
                    .bounds(px + PANEL_W - 22, py + CONTENT_TOP + i * ROW_H + 6, 18, 16).build();
            addRenderableWidget(plusBtns[i]);
            addRenderableWidget(minusBtns[i]);
        }

        // Bahan Baku buy buttons
        bahanBuyBtns.clear();
        for (int i = 0; i < BAHAN_BAKU.size(); i++) {
            final int row = i;
            Button btn = Button.builder(Component.literal("Ambil"), b -> onBuyBahan(row))
                    .bounds(px + PANEL_W - 44, py + CONTENT_TOP + i * ROW_H + 6, 42, 16).build();
            bahanBuyBtns.add(btn);
            addRenderableWidget(btn);
        }

        // Queue overlay buttons (left panel)
        int qx = qx();
        int qy = topPos;
        layaniBtn = Button.builder(Component.literal("Layani"),
                b -> {
                    BlockPos pos = menu.getStallPos();
                    if (pos != null) {
                        PacketHandler.INSTANCE.sendToServer(new FoodStallServePacket(pos, false));
                        queueSize = Math.max(0, queueSize - 1);
                        requestItemId = "";
                    }
                })
                .bounds(qx + 5, qy + 100, 125, 18).build();
        addRenderableWidget(layaniBtn);

        syncVisibility();
    }

    private void switchTab(int tab) {
        activeTab = tab;
        syncVisibility();
    }

    private void adjustPrice(String itemId, int delta) {
        BlockPos pos = menu.getStallPos();
        if (pos == null) return;
        int newPrice = Math.max(0, getCurrentPrice(itemId) + delta);
        PacketHandler.INSTANCE.sendToServer(new SetFoodStallPricePacket(pos, newPrice, itemId));
    }

    private void syncVisibility() {
        boolean pl    = (activeTab == 0);
        boolean bahan = (activeTab == 1);
        for (int i = 0; i < 3; i++) {
            plusBtns[i].visible  = pl;
            plusBtns[i].active   = pl;
            minusBtns[i].visible = pl;
            minusBtns[i].active  = pl;
        }
        for (Button b : bahanBuyBtns) { b.visible = bahan; b.active = bahan; }

        boolean hasQueue = queueSize > 0;
        layaniBtn.visible      = hasQueue;
        layaniBtn.active       = hasQueue;
    }

    private void onBuyBahan(int rowIndex) {
        BlockPos pos = menu.getStallPos();
        if (pos == null) return;
        if (rowIndex >= BAHAN_BAKU.size()) return;
        CatalogEntry e = BAHAN_BAKU.get(rowIndex);
        PacketHandler.INSTANCE.sendToServer(new FoodStallBuyItemPacket(pos, e.itemId(), 1, e.price(), true));
    }

    // ---- Rendering -------------------------------------------------------

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        renderPanel(g);
        renderQueueOverlay(g);
        renderTooltip(g, mx, my);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    private void renderPanel(GuiGraphics g) {
        int px = px(), py = py();
        int panelH = imageHeight + 30;
        g.fill(px - 2, py, px + PANEL_W + 2, py + panelH, 0xD0111118);
        g.fill(px - 2, py, px + PANEL_W + 2, py + 1, 0xFF4EC9B0);

        // Active tab underline
        if (activeTab == 0)
            g.fill(px, py + 16, px + 80, py + 18, 0xFF4EC9B0);
        else
            g.fill(px + 82, py + 16, px + 82 + 78, py + 18, 0xFF4EC9B0);

        if (activeTab == 0) renderPriceListTab(g, px, py);
        else                renderBahanTab(g, px, py);
    }

    // ---- Price List tab --------------------------------------------------

    private void renderPriceListTab(GuiGraphics g, int px, int py) {
        for (int i = 0; i < CATALOG.size(); i++) {
            CatalogEntry entry = CATALOG.get(i);
            int ry = py + CONTENT_TOP + i * ROW_H;
            boolean stocked = isStocked(entry.itemId());

            // Row background
            g.fill(px, ry, px + PANEL_W, ry + ROW_H - 2, stocked ? 0x55222250 : 0x22222230);

            // Item icon
            ItemStack stack = stackFor(entry.itemId());
            if (!stack.isEmpty()) {
                if (stocked) {
                    g.renderItem(stack, px + 2, ry + 6);
                } else {
                    g.setColor(1.0f, 1.0f, 1.0f, 0.3f);
                    g.renderItem(stack, px + 2, ry + 6);
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }

            // Name
            int nameColor = stocked ? 0xFFDDDDDD : 0x55999999;
            g.drawString(font, entry.name(), px + 22, ry + 3, nameColor, false);

            // Price
            int price = getCurrentPrice(entry.itemId());
            int priceColor = stocked ? 0xFFFFD700 : 0x55AA9900;
            g.drawString(font, "Rp " + String.format("%,d", price), px + 22, ry + 14, priceColor, false);


        }

    }

    // ---- Bahan tab -------------------------------------------------------

    private void renderBahanTab(GuiGraphics g, int px, int py) {


        for (int i = 0; i < BAHAN_BAKU.size(); i++) {
            CatalogEntry entry = BAHAN_BAKU.get(i);
            int ry = py + CONTENT_TOP + i * ROW_H;
            renderIngredientRow(g, px, ry, stackFor(entry.itemId()), entry.itemId(), entry.name(), entry.price());
        }
    }

    private void renderIngredientRow(GuiGraphics g, int px, int ry,
                                     ItemStack stack, String itemId, String name, int price) {
        g.fill(px, ry, px + PANEL_W, ry + ROW_H - 2, 0x55222240);
        if (!stack.isEmpty()) {
            g.renderItem(stack, px + 2, ry + 6);
        } else {
            // Try bundled fallback texture first
            ResourceLocation fallback = ITEM_TEX_FALLBACK.get(itemId);
            if (fallback != null) {
                g.blit(fallback, px + 2, ry + 6, 0, 0, 16, 16, 16, 16);
            } else {
                g.fill(px + 2, ry + 6, px + 18, ry + 22, 0xFF333355);
            }
        }
        g.drawString(font, name, px + 22, ry + 4, 0xFFDDDDDD, false);
        if (price > 0) {
            g.drawString(font, "Rp " + String.format("%,d", price), px + 22, ry + 15, 0xFFFFD700, false);
        }
    }

    // ---- Queue overlay (left panel) --------------------------------------

    private void renderQueueOverlay(GuiGraphics g) {
        syncVisibility();

        if (queueSize <= 0) return;

        int qx = qx();
        int qy = topPos;
        int qw = 135;
        int qh = 126;

        // Background
        g.fill(qx, qy, qx + qw, qy + qh, 0xCC1A1A2E);
        g.fill(qx, qy, qx + qw, qy + 2, 0xFF4ECDC4);

        // Title
        g.drawCenteredString(font, Component.literal("\u26CF Antrian"),
                qx + qw / 2, qy + 6, 0xFF4ECDC4);

        // Queue count
        g.drawCenteredString(font, queueSize + " pengunjung",
                qx + qw / 2, qy + 22, 0xFFFFFFFF);



        // Request
        String requestName = resolveRequestName();
        g.drawCenteredString(font, Component.literal("Minta: " + requestName),
                qx + qw / 2, qy + 54, 0xFFDDDDDD);

    }



    private static int getMoodColor(String mood) {
        return switch (mood) {
            case "HUNGRY" -> 0xFFFF6B6B;
            case "HAPPY"  -> 0xFF98FF98;
            default       -> 0xFFDDDDDD;
        };
    }

    private static String getMoodText(String mood) {
        return switch (mood) {
            case "HUNGRY" -> "Pengunjung lapar!";
            case "HAPPY"  -> "Pengunjung senang!";
            default       -> "Mood: " + mood;
        };
    }

    private String resolveRequestName() {
        if (requestItemId == null || requestItemId.isBlank()) return "-";
        ResourceLocation id = ResourceLocation.tryParse(requestItemId);
        if (id == null) return requestItemId;
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == net.minecraft.world.item.Items.AIR) return requestItemId;
        return item.getDescription().getString();
    }
}
