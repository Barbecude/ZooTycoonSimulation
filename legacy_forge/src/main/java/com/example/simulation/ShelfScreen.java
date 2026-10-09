package com.example.simulation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Objects;

public class ShelfScreen extends AbstractContainerScreen<ShelfMenu> {
    private static final ResourceLocation TEXTURE = Objects.requireNonNull(
            ResourceLocation.tryParse("minecraft:textures/gui/container/dispenser.png"));

    // Shop catalog
    private record ShopItem(net.minecraft.world.item.Item item, String name, int price) {}

    private static final List<ShopItem> FOOD_ITEMS = List.of(
            new ShopItem(Items.COOKED_BEEF,    "Burger",      8_000),
            new ShopItem(Items.BREAD,          "Roti",        5_000),
            new ShopItem(Items.APPLE,          "Apel",        3_000),
            new ShopItem(Items.COOKED_CHICKEN, "Ayam Goreng", 7_000)
    );

    private static final List<ShopItem> DRINK_ITEMS = List.of(
            new ShopItem(Items.HONEY_BOTTLE,   "Es Krim",     5_000),
            new ShopItem(Items.MILK_BUCKET,    "Susu",        6_000),
            new ShopItem(Items.POTION,         "Air Mineral", 3_000)
    );

    private static final int PANEL_W   = 136;
    private static final int PANEL_GAP = 4;
    private static final int ROW_H     = 22;
    private static final int MAX_ROWS  = 5;
    private static final int BUY_COUNT = 8;

    private int shopTab    = 0;   // 0 = Makanan, 1 = Minuman
    private int shopScroll = 0;

    private Button tabFood;
    private Button tabDrink;
    private final Button[] buyBtns = new Button[MAX_ROWS];

    public ShelfScreen(ShelfMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth  = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        int px = this.leftPos + this.imageWidth + PANEL_GAP;
        int py = this.topPos;

        tabFood = Button.builder(Component.literal("Makanan"), b -> {
            shopTab = 0;
            shopScroll = 0;
            refreshBuyButtons();
        }).bounds(px, py + 18, 65, 14).build();

        tabDrink = Button.builder(Component.literal("Minuman"), b -> {
            shopTab = 1;
            shopScroll = 0;
            refreshBuyButtons();
        }).bounds(px + 67, py + 18, 65, 14).build();

        this.addRenderableWidget(tabFood);
        this.addRenderableWidget(tabDrink);

        int rowStartY = py + 36;
        for (int i = 0; i < MAX_ROWS; i++) {
            final int rowIdx = i;
            buyBtns[i] = Button.builder(Component.literal("Beli " + BUY_COUNT), b -> onBuy(rowIdx))
                    .bounds(px + PANEL_W - 52, rowStartY + rowIdx * ROW_H + 3, 50, 14)
                    .build();
            this.addRenderableWidget(buyBtns[i]);
        }

        refreshBuyButtons();
    }

    private void refreshBuyButtons() {
        List<ShopItem> items = shopTab == 0 ? FOOD_ITEMS : DRINK_ITEMS;
        int rowStartY = this.topPos + 36;
        for (int i = 0; i < MAX_ROWS; i++) {
            int catalogIdx = shopScroll + i;
            boolean visible = catalogIdx < items.size();
            buyBtns[i].visible = visible;
            buyBtns[i].active  = visible;
            buyBtns[i].setY(rowStartY + i * ROW_H + 3);
        }
    }

    private void onBuy(int rowIdx) {
        List<ShopItem> items = shopTab == 0 ? FOOD_ITEMS : DRINK_ITEMS;
        int catalogIdx = shopScroll + rowIdx;
        if (catalogIdx >= items.size()) return;

        BlockPos pos = menu.getShelfPos();
        if (pos == null) return;

        ShopItem si = items.get(catalogIdx);
        String itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS
                .getKey(si.item()).toString();
        PacketHandler.INSTANCE.sendToServer(new ShelfBuyItemPacket(pos, itemId, BUY_COUNT));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, pt);
        renderSideInfo(g);
        renderShopPanel(g);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        g.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    private void renderSideInfo(GuiGraphics g) {
        int px = Math.max(6, this.leftPos - 118);
        int py = this.topPos + 10;

        g.fill(px - 4, py - 4, px + 108, py + 50, 0xA0000000);
        g.drawString(font, "Food Shelf",                         px, py,      0xFFE6D2A2, false);
        g.drawString(font, "Item: " + menu.getDisplayItemName(), px, py + 12, 0xFF55AA55, false);
        g.drawString(font, "Stock: " + menu.getStock() + "/64",  px, py + 24, 0xFFAAAAAA, false);
        g.drawString(font, "Price: Rp " + menu.getPrice(),       px, py + 36, 0xFFFFFFFF, false);
    }

    private void renderShopPanel(GuiGraphics g) {
        int px = this.leftPos + this.imageWidth + PANEL_GAP;
        int py = this.topPos;

        // Background
        g.fill(px - 2, py, px + PANEL_W + 2, py + imageHeight, 0xCC1A1A1A);
        g.renderOutline(px - 2, py, PANEL_W + 4, imageHeight, 0xFF4A90E2);

        g.drawString(font, "Toko Item", px + 4, py + 4, 0xFFFFD369, false);

        // Active tab highlight
        Button activeTabBtn = shopTab == 0 ? tabFood : tabDrink;
        if (activeTabBtn != null) {
            g.fill(activeTabBtn.getX() - 1, activeTabBtn.getY() - 1,
                    activeTabBtn.getX() + activeTabBtn.getWidth() + 1,
                    activeTabBtn.getY() + activeTabBtn.getHeight() + 1,
                    0x773D5FA0);
        }

        // Item rows
        List<ShopItem> items = shopTab == 0 ? FOOD_ITEMS : DRINK_ITEMS;
        int rowY = py + 36;
        for (int i = 0; i < MAX_ROWS; i++) {
            int catalogIdx = shopScroll + i;
            if (catalogIdx >= items.size()) break;

            ShopItem si = items.get(catalogIdx);
            int ry = rowY + i * ROW_H;
            g.fill(px, ry, px + PANEL_W, ry + ROW_H, i % 2 == 0 ? 0x22FFFFFF : 0x11FFFFFF);

            // Item icon (16x16)
            g.renderItem(new ItemStack(si.item()), px + 2, ry + 3);

            // Name + price
            g.drawString(font, si.name(),
                    px + 20, ry + 4, 0xFFDDDDDD, false);
            String totalPrice = "Rp " + String.format("%,d", (long) si.price() * BUY_COUNT);
            g.drawString(font, totalPrice,
                    px + 20, ry + 13, 0xFFAAAAAA, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int px = this.leftPos + this.imageWidth + PANEL_GAP;
        int py = this.topPos;
        if (mx >= px - 2 && mx <= px + PANEL_W + 2 && my >= py && my <= py + imageHeight) {
            List<ShopItem> items = shopTab == 0 ? FOOD_ITEMS : DRINK_ITEMS;
            int maxScroll = Math.max(0, items.size() - MAX_ROWS);
            shopScroll = (int) Math.max(0, Math.min(maxScroll, shopScroll + (delta > 0 ? -1 : 1)));
            refreshBuyButtons();
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }
}

