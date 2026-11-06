package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.network.WarehousePayloads;
import net.zootycoon.util.ZooTranslations;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class EmptyBarrelChestOverlay {

    private static BlockPos activeBarrelPos = null;
    private static String activeCategory = "";
    private static List<WarehousePayloads.StaffRequestDto> activeRequests = new ArrayList<>();
    private static final Set<UUID> expandedFoodCards = new HashSet<>();
    private static float scrollOffset = 0.0f;
    private static int maxScroll = 0;
    private static boolean isDraggingScrollbar = false;

    private static WarehousePayloads.StaffRequestDto activeFoodSelectionReq = null;
    private static int foodModalScrollOffset = 0;
    private static int foodModalMaxScroll = 0;

    private static final Map<UUID, LivingEntity> dummyStaffCache = new HashMap<>();

    private static final int CHEST_WIDTH = 176;
    private static final int CHEST_HEIGHT = 166;
    private static final int CARD_HEIGHT = 68;
    private static final int CARD_GAP = 5;
    private static final ItemStack EMERALD_STACK = new ItemStack(net.minecraft.world.item.Items.EMERALD);

    public record FoodPurchaseTooltipData(
            String itemName,
            int totalCost,
            int unitPrice,
            String qualityLabel,
            int qualityColor,
            boolean canAfford
    ) implements TooltipComponent {}

    public static class FoodPurchaseClientTooltip implements ClientTooltipComponent {
        private final String itemName;
        private final int totalCost;
        private final int unitPrice;
        private final String qualityLabel;
        private final int qualityColor;
        private final boolean canAfford;

        public FoodPurchaseClientTooltip(FoodPurchaseTooltipData data) {
            this.itemName = data.itemName();
            this.totalCost = data.totalCost();
            this.unitPrice = data.unitPrice();
            this.qualityLabel = data.qualityLabel();
            this.qualityColor = data.qualityColor();
            this.canAfford = data.canAfford();
        }

        @Override
        public int getHeight() {
            return canAfford ? 38 : 50;
        }

        @Override
        public int getWidth(Font font) {
            int w1 = font.width(itemName);
            int w2 = font.width(String.valueOf(totalCost)) + 14;
            int w3 = font.width(qualityLabel);
            int w4 = canAfford ? 0 : font.width("Saldo emerald tidak cukup");
            return Math.max(Math.max(w1, w2), Math.max(w3, w4)) + 4;
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
            // Line 1: Item Name
            graphics.drawString(font, itemName, x, y + 1, 0xFFFFFFFF, true);

            // Line 2: [totalCost] [Emerald Icon]
            String costStr = String.valueOf(totalCost);
            graphics.drawString(font, costStr, x, y + 13, 0xFF55FF55, true);
            int costW = font.width(costStr);

            graphics.pose().pushPose();
            graphics.pose().translate(x + costW + 3, y + 10, 400.0f);
            graphics.pose().scale(0.75f, 0.75f, 1.0f);
            graphics.renderFakeItem(EMERALD_STACK, 0, 0);
            graphics.pose().popPose();

            // Line 3: "Kualitas: [Tier]"
            graphics.drawString(font, qualityLabel, x, y + 25, qualityColor, true);

            // Line 4: Not enough emerald warning
            if (!canAfford) {
                graphics.drawString(font, "Saldo emerald tidak cukup", x, y + 37, 0xFFFF5555, true);
            }
        }
    }

    public record SimpleItemPriceTooltipData(
            String itemName,
            int price,
            boolean canAfford
    ) implements TooltipComponent {}

    public static class SimpleItemPriceClientTooltip implements ClientTooltipComponent {
        private final String itemName;
        private final int price;
        private final boolean canAfford;
        private static final ItemStack EMERALD_ICON = new ItemStack(net.minecraft.world.item.Items.EMERALD);

        public SimpleItemPriceClientTooltip(SimpleItemPriceTooltipData data) {
            this.itemName = data.itemName();
            this.price = data.price();
            this.canAfford = data.canAfford();
        }

        @Override
        public int getHeight() {
            return canAfford ? 26 : 38;
        }

        @Override
        public int getWidth(Font font) {
            int w1 = font.width(itemName);
            int w2 = font.width("Harga: " + price) + 16;
            int w3 = canAfford ? 0 : font.width("Saldo emerald tidak cukup");
            return Math.max(Math.max(w1, w2), w3) + 4;
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0f, 0.0f, 400.0f);
            graphics.drawString(font, itemName, x, y + 1, 0xFFFFFFFF, true);

            String priceStr = "Harga: " + price;
            graphics.drawString(font, priceStr, x, y + 13, 0xFF55FF55, true);
            int pW = font.width(priceStr);

            graphics.pose().pushPose();
            graphics.pose().translate(x + pW + 3, y + 10, 400.0f);
            graphics.pose().scale(0.75f, 0.75f, 1.0f);
            graphics.renderFakeItem(EMERALD_ICON, 0, 0);
            graphics.pose().popPose();

            if (!canAfford) {
                graphics.drawString(font, "Saldo emerald tidak cukup", x, y + 25, 0xFFE06C75, true);
            }
            graphics.pose().popPose();
        }
    }

    public record RawIngredientsTooltipData(
            String recipeName,
            int rawCost,
            int totalEmeraldReward,
            List<WarehousePayloads.RawIngredientItemDto> ingredients
    ) implements TooltipComponent {
    }

    public static class RawIngredientsClientTooltip implements ClientTooltipComponent {
        private final String recipeName;
        private final int rawCost;
        private final int totalEmeraldReward;
        private final List<WarehousePayloads.RawIngredientItemDto> ingredients;
        private static final int ROW_HEIGHT = 14;
        private static final int HEADER_HEIGHT = 38;
        private static final ItemStack EMERALD_STACK = new ItemStack(net.minecraft.world.item.Items.EMERALD);

        public RawIngredientsClientTooltip(RawIngredientsTooltipData data) {
            this.recipeName = data.recipeName();
            this.rawCost = data.rawCost();
            this.totalEmeraldReward = data.totalEmeraldReward();
            this.ingredients = data.ingredients();
        }

        @Override
        public int getHeight() {
            int headerH = (recipeName != null && !recipeName.isEmpty()) ? HEADER_HEIGHT : 0;
            return headerH + ingredients.size() * ROW_HEIGHT + 2;
        }

        @Override
        public int getWidth(Font font) {
            int maxW = 0;
            if (recipeName != null && !recipeName.isEmpty()) {
                String rawCostPrefix = ZooTranslations.get("zootycoon.warehouse.tooltip.raw_cost_prefix", "Harga Bahan Mentah: ");
                String profitPrefix = ZooTranslations.get("zootycoon.warehouse.tooltip.profit_prefix", "Keuntungan: ");
                String costStr = String.valueOf(rawCost);
                String rewardStr = "+" + totalEmeraldReward;
                int line1W = font.width(recipeName);
                int line2W = font.width(rawCostPrefix) + font.width(costStr) + 2 + 13;
                int line3W = font.width(profitPrefix) + font.width(rewardStr) + 2 + 13;
                maxW = Math.max(line1W, Math.max(line2W, line3W));
            }
            for (WarehousePayloads.RawIngredientItemDto raw : ingredients) {
                String text = raw.stack().getCount() + "x " + raw.stack().getHoverName().getString();
                int rowW = 14 + font.width(text) + (raw.inBarrel() ? 10 : 0);
                maxW = Math.max(maxW, rowW);
            }
            return maxW;
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
            int curY = y;
            if (recipeName != null && !recipeName.isEmpty()) {
                // Line 1: Recipe Name
                graphics.pose().pushPose();
                graphics.pose().translate(0.0f, 0.0f, 400.0f);
                graphics.drawString(font, recipeName, x, curY + 1, 0xFFFFFFFF, true);

                // Line 2: "Harga Bahan Mentah: " + rawCost + Emerald icon
                int line2Y = curY + 13;
                String rawCostPrefix = ZooTranslations.get("zootycoon.warehouse.tooltip.raw_cost_prefix", "Harga Bahan Mentah: ");
                String costStr = String.valueOf(rawCost);
                graphics.drawString(font, rawCostPrefix, x, line2Y, 0xFFECC94B, true);
                int costPrefixW = font.width(rawCostPrefix);
                graphics.drawString(font, costStr, x + costPrefixW, line2Y, 0xFFFFFFFF, true);
                graphics.pose().popPose();

                int costValW = font.width(costStr);
                int costEmX = x + costPrefixW + costValW + 2;
                graphics.pose().pushPose();
                graphics.pose().translate(costEmX, line2Y - 2, 400.0f);
                graphics.pose().scale(0.75f, 0.75f, 1.0f);
                graphics.renderFakeItem(EMERALD_STACK, 0, 0);
                graphics.pose().popPose();

                // Line 3: "Keuntungan: " + "+totalEmeraldReward" + Emerald Item Icon
                int line3Y = curY + 25;
                String profitPrefix = ZooTranslations.get("zootycoon.warehouse.tooltip.profit_prefix", "Keuntungan: ");
                String rewardStr = "+" + totalEmeraldReward;
                graphics.pose().pushPose();
                graphics.pose().translate(0.0f, 0.0f, 400.0f);
                graphics.drawString(font, profitPrefix, x, line3Y, 0xFFE2E8F0, true);
                int prefixW = font.width(profitPrefix);
                graphics.drawString(font, rewardStr, x + prefixW, line3Y, 0xFF55FF55, true);
                graphics.pose().popPose();

                int rewardW = font.width(rewardStr);
                int emX = x + prefixW + rewardW + 2;
                graphics.pose().pushPose();
                graphics.pose().translate(emX, line3Y - 2, 400.0f);
                graphics.pose().scale(0.75f, 0.75f, 1.0f);
                graphics.renderFakeItem(EMERALD_STACK, 0, 0);
                graphics.pose().popPose();

                curY += HEADER_HEIGHT;
            }

            for (WarehousePayloads.RawIngredientItemDto raw : ingredients) {
                graphics.pose().pushPose();
                graphics.pose().translate(x, curY - 1, 400.0f);
                graphics.pose().scale(0.75f, 0.75f, 1.0f);
                graphics.renderFakeItem(raw.stack(), 0, 0);
                graphics.pose().popPose();

                String text = raw.stack().getCount() + "x " + raw.stack().getHoverName().getString();
                int textW = font.width(text);
                graphics.pose().pushPose();
                graphics.pose().translate(0.0f, 0.0f, 400.0f);
                int textColor = raw.inBarrel() ? 0xFFFFFFFF : 0xFFAAAAAA;
                graphics.drawString(font, text, x + 14, curY + 1, textColor, true);

                // Pixelated green checkmark if ingredient is already in the empty barrel
                if (raw.inBarrel()) {
                    int chkX = x + 14 + textW + 4;
                    int chkY = curY + 2;
                    graphics.fill(chkX + 0, chkY + 2, chkX + 1, chkY + 4, 0xFF55FF55);
                    graphics.fill(chkX + 1, chkY + 3, chkX + 2, chkY + 5, 0xFF55FF55);
                    graphics.fill(chkX + 2, chkY + 2, chkX + 3, chkY + 4, 0xFF55FF55);
                    graphics.fill(chkX + 3, chkY + 1, chkX + 4, chkY + 3, 0xFF55FF55);
                    graphics.fill(chkX + 4, chkY + 0, chkX + 5, chkY + 2, 0xFF55FF55);
                }
                graphics.pose().popPose();
                curY += ROW_HEIGHT;
            }
        }
    }

    public static void prepareBarrelOpen(BlockPos pos, String category) {
        activeBarrelPos = pos;
        activeCategory = category != null ? category : "";
        activeRequests.clear();
        expandedFoodCards.clear();
        scrollOffset = 0.0f;
        isDraggingScrollbar = false;
        activeFoodSelectionReq = null;
        foodModalScrollOffset = 0;
    }

    public static void onSyncRequests(WarehousePayloads.SyncBarrelRequestsPayload payload) {
        activeBarrelPos = payload.pos();
        activeCategory = payload.category() != null ? payload.category() : "";
        activeRequests = new ArrayList<>(payload.requests());
    }

    public static void reset() {
        activeBarrelPos = null;
        activeCategory = "";
        activeRequests.clear();
        expandedFoodCards.clear();
        dummyStaffCache.clear();
        scrollOffset = 0.0f;
        maxScroll = 0;
        isDraggingScrollbar = false;
        activeFoodSelectionReq = null;
        foodModalScrollOffset = 0;
    }

    public static void register() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof RawIngredientsTooltipData rawData) {
                return new RawIngredientsClientTooltip(rawData);
            }
            if (data instanceof FoodPurchaseTooltipData foodData) {
                return new FoodPurchaseClientTooltip(foodData);
            }
            if (data instanceof SimpleItemPriceTooltipData priceData) {
                return new SimpleItemPriceClientTooltip(priceData);
            }
            return null;
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ContainerScreen) {
                ScreenEvents.afterRender(screen).register((scr, graphics, mouseX, mouseY, tickDelta) -> {
                    ContainerScreen cs = (ContainerScreen) scr;
                    if (isBarrelContainer(cs)) {
                        renderLeftRequestPanel(cs, graphics, mouseX, mouseY);
                    }
                });

                ScreenMouseEvents.allowMouseClick(screen).register((scr, mouseX, mouseY, button) -> {
                    ContainerScreen cs = (ContainerScreen) scr;
                    if (!isBarrelContainer(cs)) return true;
                    return !handleMouseClicked(cs, mouseX, mouseY, button);
                });

                ScreenMouseEvents.allowMouseScroll(screen).register((scr, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
                    ContainerScreen cs = (ContainerScreen) scr;
                    if (!isBarrelContainer(cs)) return true;
                    return !handleMouseScrolled(cs, mouseX, mouseY, verticalAmount);
                });

                ScreenEvents.remove(screen).register(scr -> {
                    ContainerScreen cs = (ContainerScreen) scr;
                    if (isBarrelContainer(cs)) {
                        activeBarrelPos = null;
                        activeRequests.clear();
                        scrollOffset = 0.0f;
                        isDraggingScrollbar = false;
                        activeFoodSelectionReq = null;
                        foodModalScrollOffset = 0;
                    }
                });
            }
        });
    }

    public static boolean onGlobalMouseScroll(double verticalAmount) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ContainerScreen cs) || !isBarrelContainer(cs)) {
            return false;
        }
        double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();
        return handleMouseScrolled(cs, mouseX, mouseY, verticalAmount);
    }

    public static boolean isBarrelContainer(ContainerScreen screen) {
        if (activeBarrelPos != null) {
            return true;
        }
        String title = screen.getTitle().getString();
        return title.equals(ZooTranslations.get("zootycoon.warehouse.barrel.title.burger"))
                || title.equals(ZooTranslations.get("zootycoon.warehouse.barrel.title.drink"))
                || title.equals(ZooTranslations.get("zootycoon.warehouse.barrel.title.ice_cream"))
                || title.equals(ZooTranslations.get("zootycoon.warehouse.barrel.title.animal_food"))
                || title.equals(ZooTranslations.get("zootycoon.warehouse.barrel.title.animal_care"));
    }

    private static int getPanelWidth(Screen screen) {
        int chestLeft = (screen.width - CHEST_WIDTH) / 2;
        return Math.min(176, Math.max(126, chestLeft - 10));
    }

    private static int getPanelX(Screen screen) {
        int chestLeft = (screen.width - CHEST_WIDTH) / 2;
        int panelW = getPanelWidth(screen);
        return Math.max(4, chestLeft - panelW - 6);
    }

    private static int getPanelY(Screen screen) {
        return (screen.height - getChestHeight(screen)) / 2;
    }

    // A double barrel opens a six-row chest screen, which is three rows (54px) taller
    private static int getChestHeight(Screen screen) {
        if (screen instanceof ContainerScreen cs) {
            return CHEST_HEIGHT + (cs.getMenu().getRowCount() - 3) * 18;
        }
        return CHEST_HEIGHT;
    }

    private static final int ZOOKEEPER_FOOD_COLS = 5;

    private static int getCardHeight(WarehousePayloads.StaffRequestDto req, int cardW) {
        int slotSize = 18;
        int slotGap = 2;
        if ("CASHIER".equalsIgnoreCase(req.staffType())) {
            int infoXOffset = 3 + 34 + 5;
            int infoW = cardW - infoXOffset - 4;
            int colsPerRow = Math.max(1, (infoW + slotGap) / (slotSize + slotGap));
            int variantCount = req.targetItems().size();
            int rows = Math.max(1, (int) Math.ceil((double) Math.max(1, variantCount) / colsPerRow));
            int gridH = rows * slotSize + Math.max(0, rows - 1) * slotGap;
            int actionBtnYOffset = 25 + gridH + 3;
            int actionBtnH = 17;
            return Math.max(CARD_HEIGHT, actionBtnYOffset + actionBtnH + 4);
        }
        int colsPerRow = 4;
        List<ItemStack> targets = req.targetItems();
        boolean isExpanded = expandedFoodCards.contains(req.staffUuid()) && targets.size() > colsPerRow;
        int displayedCount = isExpanded ? targets.size() : Math.min(colsPerRow, targets.size());
        int foodRows = Math.max(1, (int) Math.ceil((double) displayedCount / colsPerRow));
        int foodGridH = foodRows * slotSize + Math.max(0, foodRows - 1) * slotGap;
        int actionBtnYOffset = 25 + foodGridH + 3;
        int actionBtnH = 17;
        return Math.max(CARD_HEIGHT, actionBtnYOffset + actionBtnH + 4);
    }

    private static int computeTotalContentHeight(int colsCardW, int actualCardW) {
        int total = 0;
        for (int i = 0; i < activeRequests.size(); i++) {
            total += getCardHeight(activeRequests.get(i), colsCardW);
            if (i < activeRequests.size() - 1) {
                total += CARD_GAP;
            }
        }
        return total;
    }

    private static void renderLeftRequestPanel(ContainerScreen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        int panelW = getPanelWidth(screen);
        int panelX = getPanelX(screen);
        int panelY = getPanelY(screen);
        int panelH = getChestHeight(screen);

        // Handle active scrollbar dragging
        if (isDraggingScrollbar) {
            long window = mc.getWindow().getWindow();
            if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
                isDraggingScrollbar = false;
            } else if (maxScroll > 0) {
                int listY = panelY + 29;
                int listH = panelH - 33;
                float rel = (float) (mouseY - listY) / (float) Math.max(1, listH);
                scrollOffset = Math.clamp(rel * maxScroll, 0.0f, (float) maxScroll);
            }
        }

        // 1. Main Slate Frame (matching EasyNpcDialogScreen & MainManagementScreen)
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF212151D);
        graphics.renderOutline(panelX, panelY, panelW, panelH, 0xFF2A3140);

        // 2. Top Header Bar
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 25, 0xFF19202D);
        String headerTitle = ZooTranslations.get("zootycoon.warehouse.panel.header");
        graphics.drawString(font, headerTitle, panelX + 8, panelY + 5, 0xFFFFD700, false);

        String countLabel = activeRequests.size() + " " + ZooTranslations.get("zootycoon.warehouse.panel.requests_count");
        graphics.drawString(font, countLabel, panelX + 8, panelY + 15, 0xFFA0AAB8, false);
        graphics.fill(panelX + 1, panelY + 25, panelX + panelW - 1, panelY + 26, 0xFF252D3C);

        // 3. Scrollable Vertical Request Card Area
        int listX = panelX + 5;
        int listY = panelY + 29;
        int listH = panelH - 33;
        int listW = panelW - 10;

        if (activeRequests.isEmpty()) {
            String empty1 = ZooTranslations.get("zootycoon.warehouse.panel.empty1");
            String empty2 = ZooTranslations.get("zootycoon.warehouse.panel.empty2");
            int w1 = font.width(empty1);
            int w2 = font.width(empty2);
            graphics.drawString(font, empty1, panelX + (panelW - w1) / 2, listY + 46, 0xFF94A3B8, false);
            graphics.drawString(font, empty2, panelX + (panelW - w2) / 2, listY + 58, 0xFF64748B, false);
            return;
        }

        int baseCardW = listW - 6;
        int totalContentH = computeTotalContentHeight(baseCardW, baseCardW);
        int cardW = totalContentH > listH ? (listW - 6) : listW;
        maxScroll = Math.max(0, totalContentH - listH);
        scrollOffset = Math.clamp(scrollOffset, 0.0f, (float) maxScroll);

        ItemStack hoveredStack = null;
        WarehousePayloads.StaffRequestDto hoveredDto = null;
        int hoveredVariantIndex = -1;
        WarehousePayloads.StaffRequestDto hoveredCheckDto = null;
        WarehousePayloads.StaffRequestDto hoveredBuyDto = null;

        graphics.enableScissor(panelX + 2, listY, panelX + panelW - 2, listY + listH);

        int curCardY = listY - (int) scrollOffset;
        for (int i = 0; i < activeRequests.size(); i++) {
            WarehousePayloads.StaffRequestDto req = activeRequests.get(i);
            int cardY = curCardY;
            int cardH = getCardHeight(req, baseCardW);
            curCardY += cardH + CARD_GAP;

            if (cardY + cardH < listY || cardY > listY + listH) {
                continue;
            }

            // Card container
            graphics.fill(listX, cardY, listX + cardW, cardY + cardH, 0xFF19202D);
            graphics.renderOutline(listX, cardY, cardW, cardH, 0xFF2A364B);

            // A. Left Box: 3D Staff Skin Preview (Cashier or Zookeeper)
            int previewX = listX + 3;
            int previewY = cardY + 3;
            int previewW = 34;
            int previewH = Math.min(cardH - 6, 79);

            graphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0xFF12151D);
            graphics.renderOutline(previewX, previewY, previewW, previewH, 0xFF252D3C);

            LivingEntity staffEntity = resolveStaffEntity(req);
            if (staffEntity != null) {
                try {
                    InventoryScreen.renderEntityInInventoryFollowsMouse(
                            graphics,
                            previewX + 1, previewY + 2,
                            previewX + previewW - 1, previewY + previewH - 2,
                            24, 0.05f, (float) mouseX, (float) mouseY, staffEntity
                    );
                } catch (Throwable ignored) {
                }
            }

            // B. Right Column: Staff Name, Subtitle, Target Finished Food Icons, Toggle Button, and Ambil Resep Button
            int infoX = previewX + previewW + 5;
            int infoW = cardW - (infoX - listX) - 4;
            int fixedColsW = baseCardW - (infoX - listX) - 4;

            // Row 1: Staff Name
            String staffName = req.staffName();
            if (font.width(staffName) > infoW) {
                staffName = font.plainSubstrByWidth(staffName, infoW - font.width("..")) + "..";
            }
            graphics.drawString(font, staffName, infoX, cardY + 4, 0xFFF8FAFC, false);

            // Row 2: Subtitle / Progress
            String subText = req.subtitle();
            if ("ZOOKEEPER".equalsIgnoreCase(req.staffType())) {
                subText = req.subtitle() + " (" + req.currentCount() + "/" + req.requiredCount() + ")";
            }
            if (font.width(subText) > infoW) {
                subText = font.plainSubstrByWidth(subText, infoW - font.width("..")) + "..";
            }
            graphics.drawString(font, subText, infoX, cardY + 14, 0xFF94A3B8, false);

            // Row 3: Target Menu / Food Items
            int slotY = cardY + 25;
            int slotSize = 18;
            int slotGap = 2;
            List<ItemStack> targets = req.targetItems();
            int actionBtnY = cardY + 46;

            if ("CASHIER".equalsIgnoreCase(req.staffType())) {
                int totalVariants = targets.size();
                int colsPerRow = Math.max(1, (infoW + slotGap) / (slotSize + slotGap));
                int rows = Math.max(1, (int) Math.ceil((double) Math.max(1, totalVariants) / colsPerRow));
                int gridH = rows * slotSize + Math.max(0, rows - 1) * slotGap;
                actionBtnY = slotY + gridH + 3;

                for (int v = 0; v < totalVariants; v++) {
                    ItemStack st = targets.get(v);
                    int col = v % colsPerRow;
                    int row = v / colsPerRow;
                    int curX = infoX + col * (slotSize + slotGap);
                    int curY = slotY + row * (slotSize + slotGap);

                    graphics.fill(curX, curY, curX + slotSize, curY + slotSize, 0xFF12151D);
                    graphics.renderOutline(curX, curY, slotSize, slotSize, 0xFF334155);
                    graphics.renderFakeItem(st, curX + 1, curY + 1);
                    graphics.renderItemDecorations(font, st, curX + 1, curY + 1, String.valueOf(st.getCount()));

                    // Check badge on top-right of slot if that menu item's ingredients are complete in the barrel!
                    boolean itemReady = (v < req.menuItems().size()) && req.menuItems().get(v).isReadyInBarrel();
                    if (itemReady) {
                        int bX = curX + slotSize - 6;
                        int bY = curY - 1;
                        graphics.fill(bX, bY, bX + 6, bY + 6, 0xFF163821);
                        graphics.renderOutline(bX, bY, 6, 6, 0xFF38A169);
                        graphics.fill(bX + 1, bY + 2, bX + 2, bY + 4, 0xFF55FF55);
                        graphics.fill(bX + 2, bY + 3, bX + 3, bY + 5, 0xFF55FF55);
                        graphics.fill(bX + 3, bY + 2, bX + 4, bY + 4, 0xFF55FF55);
                        graphics.fill(bX + 4, bY + 1, bX + 5, bY + 3, 0xFF55FF55);
                    }

                    if (mouseX >= curX && mouseX <= curX + slotSize && mouseY >= curY && mouseY <= curY + slotSize
                            && mouseY >= listY && mouseY <= listY + listH) {
                        hoveredStack = st;
                        hoveredDto = req;
                        hoveredVariantIndex = v;
                    }

                    if (totalVariants == 1) {
                        String itemName = st.getHoverName().getString();
                        int nameMaxW = infoW - slotSize - 4;
                        if (font.width(itemName) > nameMaxW) {
                            itemName = font.plainSubstrByWidth(itemName, Math.max(8, nameMaxW - font.width(".."))) + "..";
                        }
                        graphics.drawString(font, itemName, curX + slotSize + 4, curY + 5, 0xFFE2E8F0, false);
                    }
                }
            } else {
                int colsPerRow = 4;
                boolean isExpanded = expandedFoodCards.contains(req.staffUuid()) && targets.size() > colsPerRow;
                int displayedCount = isExpanded ? targets.size() : Math.min(colsPerRow, targets.size());

                for (int k = 0; k < displayedCount; k++) {
                    ItemStack st = targets.get(k);
                    int col = k % colsPerRow;
                    int row = k / colsPerRow;
                    int sx = infoX + col * (slotSize + slotGap);
                    int sy = slotY + row * (slotSize + slotGap);

                    boolean slotHovered = mouseX >= sx && mouseX <= sx + slotSize
                            && mouseY >= sy && mouseY <= sy + slotSize
                            && mouseY >= listY && mouseY <= listY + listH;

                    graphics.fill(sx, sy, sx + slotSize, sy + slotSize, slotHovered ? 0xFF1E2433 : 0xFF12151D);
                    graphics.renderOutline(sx, sy, slotSize, slotSize, slotHovered ? 0xFF58A6FF : 0xFF334155);
                    graphics.renderFakeItem(st, sx + 1, sy + 1);

                    if (slotHovered) {
                        hoveredStack = st;
                        hoveredDto = req;
                    }
                }

                String sub = req.subtitle() != null ? req.subtitle().toLowerCase(java.util.Locale.ROOT) : "";
                boolean isCare = sub.contains("pemanas") || sub.contains("pendingin") || sub.contains("alat mandi") || sub.contains("kotor") || sub.contains("kepanasan") || sub.contains("kedinginan");

                // Dropdown chevron button right next to the 4th item (for animal food with > 4 items)
                if (!isCare && targets.size() > colsPerRow) {
                    int chevronX = infoX + 4 * (slotSize + slotGap);
                    int chevronY = slotY;
                    int chevronSize = 18;
                    boolean chevronHovered = mouseX >= chevronX && mouseX <= chevronX + chevronSize
                            && mouseY >= chevronY && mouseY <= chevronY + chevronSize
                            && mouseY >= listY && mouseY <= listY + listH;

                    int chBg = chevronHovered ? 0xFF2A344A : 0xFF161B26;
                    int chBorder = chevronHovered ? 0xFF58A6FF : 0xFF354157;
                    graphics.fill(chevronX, chevronY, chevronX + chevronSize, chevronY + chevronSize, chBg);
                    graphics.renderOutline(chevronX, chevronY, chevronSize, chevronSize, chBorder);

                    String chevronText = isExpanded ? "▲" : "▼";
                    int chTextW = font.width(chevronText);
                    graphics.drawString(
                            font,
                            chevronText,
                            chevronX + (chevronSize - chTextW) / 2,
                            chevronY + (chevronSize - 8) / 2,
                            chevronHovered ? 0xFFFFFFFF : 0xFF9FB0C7,
                            false
                    );
                }

                int foodRows = Math.max(1, (int) Math.ceil((double) displayedCount / colsPerRow));
                int foodGridH = foodRows * slotSize + Math.max(0, foodRows - 1) * slotGap;
                actionBtnY = slotY + foodGridH + 3;
            }

            // Row 4 / 5: Ambil Resep / Ambil Catatan Button + Square Checklist Delivery Button
            int btnY = actionBtnY;
            int btnH = 17;
            int checkBtnSize = 17; // STRICTLY SQUARE 17x17
            int btnGap = 3;
            int ambilResepW = infoW - checkBtnSize - btnGap;
            int ambilResepX = infoX;
            int checkBtnX = infoX + ambilResepW + btnGap;

            boolean ambilHovered = mouseX >= ambilResepX && mouseX <= ambilResepX + ambilResepW
                    && mouseY >= btnY && mouseY <= btnY + btnH
                    && mouseY >= listY && mouseY <= listY + listH;

            int ambilBg = ambilHovered ? 0xFF252D3D : 0xFF14171F;
            int ambilBorder = ambilHovered ? 0xFFFFFFFF : 0xFF3A4456;
            int ambilTxt = ambilHovered ? 0xFFFFFFFF : 0xFFD0D7DE;

            graphics.fill(ambilResepX, btnY, ambilResepX + ambilResepW, btnY + btnH, ambilBg);
            graphics.renderOutline(ambilResepX, btnY, ambilResepW, btnH, ambilBorder);

            String btnLabel = "CASHIER".equalsIgnoreCase(req.staffType())
                    ? ZooTranslations.get("zootycoon.warehouse.btn.take_recipe")
                    : ZooTranslations.get("zootycoon.warehouse.btn.take_food_note");
            int lblW = font.width(btnLabel);
            graphics.drawString(font, btnLabel, ambilResepX + (ambilResepW - lblW) / 2, btnY + (btnH - 8) / 2, ambilTxt, false);

            // Square Checklist Button [✓]
            boolean checkHovered = mouseX >= checkBtnX && mouseX <= checkBtnX + checkBtnSize
                    && mouseY >= btnY && mouseY <= btnY + btnH
                    && mouseY >= listY && mouseY <= listY + listH;

            if (checkHovered) {
                hoveredCheckDto = req;
            }

            boolean isReady = isCardReadyToFulfill(req);
            int checkBg = isReady ? (checkHovered ? 0xFF1F4E2E : 0xFF163821) : (checkHovered ? 0xFF1A1F29 : 0xFF14171F);
            int checkBorder = isReady ? (checkHovered ? 0xFF58D68D : 0xFF38A169) : (checkHovered ? 0xFF3F4A5C : 0xFF2D3748);
            int checkColor = isReady ? (checkHovered ? 0xFFFFFFFF : 0xFF48BB78) : 0xFF4A5568;

            graphics.fill(checkBtnX, btnY, checkBtnX + checkBtnSize, btnY + btnH, checkBg);
            graphics.renderOutline(checkBtnX, btnY, checkBtnSize, btnH, checkBorder);

            // Pixelated vanilla-style checkmark centered in 17x17 square
            int cx = checkBtnX + 5;
            int cy = btnY + 5;
            graphics.fill(cx + 0, cy + 3, cx + 2, cy + 5, checkColor);
            graphics.fill(cx + 1, cy + 4, cx + 3, cy + 6, checkColor);
            graphics.fill(cx + 2, cy + 5, cx + 4, cy + 7, checkColor);
            graphics.fill(cx + 3, cy + 4, cx + 5, cy + 6, checkColor);
            graphics.fill(cx + 4, cy + 2, cx + 6, cy + 4, checkColor);
            graphics.fill(cx + 5, cy + 0, cx + 7, cy + 2, checkColor);
        }

        graphics.disableScissor();

        // 4. Scrollbar if cards overflow
        if (maxScroll > 0) {
            int trackX = panelX + panelW - 7;
            int trackY = listY;
            int trackH = listH;
            graphics.fill(trackX, trackY, trackX + 3, trackY + trackH, 0xFF12151D);
            int thumbH = Math.max(18, (int) ((float) listH / totalContentH * trackH));
            int thumbY = trackY + (int) ((scrollOffset / maxScroll) * (trackH - thumbH));
            boolean scrollHover = mouseX >= trackX - 2 && mouseX <= trackX + 5 && mouseY >= trackY && mouseY <= trackY + trackH;
            graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, (scrollHover || isDraggingScrollbar) ? 0xFF94A3B8 : 0xFF475569);
        }

        // 5. Tooltips
        if (hoveredBuyDto != null) {
            int cost;
            List<Component> tipLines = new ArrayList<>();
            if ("CASHIER".equalsIgnoreCase(hoveredBuyDto.staffType())) {
                String role = (activeCategory != null && !activeCategory.isEmpty())
                        ? activeCategory
                        : (hoveredBuyDto.subtitle().toLowerCase().contains("burger") ? "BURGER" : "DRINK");
                net.zootycoon.economy.ZooEconomyEngine.ProfitMarginInfo margin =
                        net.zootycoon.economy.ZooEconomyEngine.getProfitMarginInfo(role, hoveredBuyDto);
                cost = margin.rawCost();
                String marginLabel = "Keuntungan: +" + margin.netProfit() + " Emerald";
                tipLines.add(Component.literal(marginLabel).withStyle(ChatFormatting.GREEN));
            } else {
                cost = net.zootycoon.economy.ZooEconomyEngine.calculateZookeeperSupplyCost(hoveredBuyDto);
                tipLines.add(Component.literal("Biaya: " + cost + " Emerald").withStyle(ChatFormatting.WHITE));
            }

            boolean canAfford = net.zootycoon.economy.TransactionHelper.hasEnoughEmeralds(mc.player, cost);
            if (!canAfford) {
                String notEnough = ZooTranslations.get("zootycoon.warehouse.tooltip.not_enough_emeralds");
                if (notEnough.equals("zootycoon.warehouse.tooltip.not_enough_emeralds") || notEnough.contains("%d")) {
                    notEnough = "Saldo emerald tidak cukup";
                }
                tipLines.add(Component.literal(notEnough).withStyle(ChatFormatting.RED));
            }
            graphics.renderTooltip(font, tipLines, Optional.empty(), mouseX, mouseY);
        } else if (hoveredCheckDto != null) {
            String checkTip;
            if (isCardReadyToFulfill(hoveredCheckDto)) {
                if ("CASHIER".equalsIgnoreCase(hoveredCheckDto.staffType())) {
                    boolean allReady = hoveredCheckDto.menuItems().isEmpty()
                            || hoveredCheckDto.menuItems().stream().allMatch(WarehousePayloads.MenuItemRequestDto::isReadyInBarrel);
                    checkTip = allReady ? "Kirim Semua Pesanan ke Kasir" : "Kirim Menu yang Siap ke Kasir";
                } else if (hoveredCheckDto.subtitle().toLowerCase().contains("pendingin")) {
                    checkTip = "Kirim Pendingin ke Zookeeper";
                } else if (hoveredCheckDto.subtitle().toLowerCase().contains("pemanas")) {
                    checkTip = "Kirim Pemanas ke Zookeeper";
                } else if (hoveredCheckDto.subtitle().toLowerCase().contains("mandi")) {
                    checkTip = "Kirim Alat Mandi ke Zookeeper";
                } else {
                    checkTip = "Kirim Pakan ke Zookeeper";
                }
            } else {
                if ("CASHIER".equalsIgnoreCase(hoveredCheckDto.staffType())) {
                    checkTip = "Bahan Mentah di Barrel Belum Lengkap";
                } else if (hoveredCheckDto.subtitle().toLowerCase().contains("pendingin")
                        || hoveredCheckDto.subtitle().toLowerCase().contains("pemanas")
                        || hoveredCheckDto.subtitle().toLowerCase().contains("mandi")) {
                    checkTip = "Alat Perawatan di Barrel Belum Lengkap";
                } else {
                    checkTip = "Pakan di Barrel Belum Lengkap";
                }
            }
            graphics.renderTooltip(font, Component.literal(checkTip), mouseX, mouseY);
        } else if (hoveredStack != null && hoveredDto != null) {
            if ("CASHIER".equalsIgnoreCase(hoveredDto.staffType())) {
                String menuName = hoveredStack.getHoverName().getString();
                String role = (activeCategory != null && !activeCategory.isEmpty())
                        ? activeCategory
                        : (hoveredDto.subtitle().toLowerCase().contains("burger") ? "BURGER" : "DRINK");
                int unitPrice = net.zootycoon.economy.ZooEconomyEngine.getFoodDrinkPrice(role, hoveredStack);
                int totalUnits = Math.max(1, hoveredStack.getCount());
                int totalReward = unitPrice * totalUnits;

                List<WarehousePayloads.RawIngredientItemDto> raws = List.of();
                if (hoveredVariantIndex >= 0 && hoveredVariantIndex < hoveredDto.menuItems().size()) {
                    raws = hoveredDto.menuItems().get(hoveredVariantIndex).rawIngredients();
                }

                int rawSum = 0;
                for (var raw : raws) {
                    rawSum += net.zootycoon.economy.ZooEconomyEngine.getRawIngredientUnitPrice(raw.stack().getItem()) * raw.stack().getCount();
                }
                int minCost = (int) Math.round(totalReward * 0.32D);
                int maxCost = (int) Math.round(totalReward * 0.44D);
                int rawCost = rawSum > 0 ? Math.clamp(rawSum, Math.max(1, minCost), Math.max(1, maxCost)) : (int) Math.round(totalReward * 0.38D);
                rawCost = Math.max(1, rawCost);
                int profit = Math.max(1, totalReward - rawCost);

                graphics.renderTooltip(
                        font,
                        List.of(),
                        Optional.of(new RawIngredientsTooltipData(menuName, rawCost, profit, raws)),
                        mouseX,
                        mouseY
                );
            } else {
                String sub = hoveredDto != null && hoveredDto.subtitle() != null ? hoveredDto.subtitle().toLowerCase(java.util.Locale.ROOT) : "";
                boolean isCare = sub.contains("pemanas") || sub.contains("pendingin") || sub.contains("alat mandi") || sub.contains("kotor") || sub.contains("kepanasan") || sub.contains("kedinginan") || "ANIMAL_CARE".equalsIgnoreCase(activeCategory);

                if (isCare || net.zootycoon.block.ZooCoolerBlock.getCoolingTicks(hoveredStack) > 0
                        || net.zootycoon.block.ZooHeaterBlock.getFuelTicks(hoveredStack) > 0
                        || net.zootycoon.animal.AnimalBathingManager.isWateringCan(hoveredStack)
                        || hoveredStack.is(net.minecraft.world.item.Items.BRUSH)) {
                    int price = 16;
                    if (net.zootycoon.animal.AnimalBathingManager.isWateringCan(hoveredStack)) price = 6;
                    else if (hoveredStack.is(net.minecraft.world.item.Items.BRUSH)) price = 4;
                    else if (net.zootycoon.block.ZooHeaterBlock.getFuelTicks(hoveredStack) > 0) price = 16;
                    else if (net.zootycoon.block.ZooCoolerBlock.getCoolingTicks(hoveredStack) > 0) price = 16;

                    boolean canAfford = net.zootycoon.economy.TransactionHelper.hasEnoughEmeralds(mc.player, price);
                    graphics.renderTooltip(
                            font,
                            List.of(),
                            Optional.of(new SimpleItemPriceTooltipData(hoveredStack.getHoverName().getString(), price, canAfford)),
                            mouseX,
                            mouseY
                    );
                } else {
                    int neededCount = hoveredDto != null ? Math.max(16, hoveredDto.requiredCount() - hoveredDto.currentCount()) : 64;
                    var quality = net.zootycoon.economy.ZooEconomyEngine.getFoodQualityInfo(hoveredStack);
                    int totalCost = quality.unitPrice() * neededCount;
                    boolean canAfford = net.zootycoon.economy.TransactionHelper.hasEnoughEmeralds(mc.player, totalCost);
                    String qualityLabel = "Kualitas: " + quality.tierName();

                    graphics.renderTooltip(
                            font,
                            List.of(),
                            Optional.of(new FoodPurchaseTooltipData(
                                    hoveredStack.getHoverName().getString(),
                                    totalCost,
                                    quality.unitPrice(),
                                    qualityLabel,
                                    quality.tierColor(),
                                    canAfford
                            )),
                            mouseX,
                            mouseY
                    );
                }
            }
        }

        // 6. Food Selection Popup Modal (renders on top of everything if active)
        if (activeFoodSelectionReq != null) {
            renderFoodSelectionModal(screen, graphics, mouseX, mouseY);
        }
    }

    private static void renderFoodSelectionModal(ContainerScreen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (activeFoodSelectionReq == null) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 400.0f);

        // 1. Dark Backdrop covering entire screen
        graphics.fill(0, 0, screen.width, screen.height, 0x99000000);

        // 2. Modal Dimensions & Slate Panel
        int modalW = 230;
        int modalH = 176;
        int modalX = (screen.width - modalW) / 2;
        int modalY = (screen.height - modalH) / 2;

        graphics.fill(modalX, modalY, modalX + modalW, modalY + modalH, 0xF212151D);
        graphics.renderOutline(modalX, modalY, modalW, modalH, 0xFF2A3140);

        // 3. Header Bar
        graphics.fill(modalX + 1, modalY + 1, modalX + modalW - 1, modalY + 25, 0xFF19202D);
        graphics.fill(modalX + 1, modalY + 25, modalX + modalW - 1, modalY + 26, 0xFF252D3C);

        String title = "Pilih Pakan: " + activeFoodSelectionReq.subtitle().replace("Satwa: ", "");
        if (font.width(title) > modalW - 32) {
            title = font.plainSubstrByWidth(title, modalW - 32 - font.width("..")) + "..";
        }
        graphics.drawString(font, title, modalX + 8, modalY + 5, 0xFFFFD700, false);

        int neededCount = Math.max(16, activeFoodSelectionReq.requiredCount() - activeFoodSelectionReq.currentCount());
        String sub = "Kebutuhan: " + neededCount + " item";
        graphics.drawString(font, sub, modalX + 8, modalY + 15, 0xFFA0AAB8, false);

        // Close button [X]
        int closeBtnX = modalX + modalW - 19;
        int closeBtnY = modalY + 5;
        int closeBtnSize = 14;
        boolean closeHovered = mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnSize && mouseY >= closeBtnY && mouseY <= closeBtnY + closeBtnSize;
        graphics.fill(closeBtnX, closeBtnY, closeBtnX + closeBtnSize, closeBtnY + closeBtnSize, closeHovered ? 0xFF8B0000 : 0xFF252D3C);
        graphics.renderOutline(closeBtnX, closeBtnY, closeBtnSize, closeBtnSize, closeHovered ? 0xFFFF5555 : 0xFF354157);
        graphics.drawString(font, "✕", closeBtnX + 3, closeBtnY + 3, closeHovered ? 0xFFFFFFFF : 0xFF9FB0C7, false);

        // 4. Food Grid Area
        List<ItemStack> foods = activeFoodSelectionReq.targetItems();
        if (foods == null) foods = List.of();

        int gridCols = 6;
        int slotSize = 18;
        int slotGap = 4;
        int gridTotalW = gridCols * slotSize + (gridCols - 1) * slotGap;
        int gridStartX = modalX + (modalW - gridTotalW) / 2;
        int gridStartY = modalY + 32;
        int gridAreaH = modalH - 38;

        int totalRows = Math.max(1, (int) Math.ceil((double) foods.size() / gridCols));
        int totalContentH = totalRows * slotSize + (totalRows - 1) * slotGap;
        foodModalMaxScroll = Math.max(0, totalContentH - gridAreaH);
        foodModalScrollOffset = Math.clamp(foodModalScrollOffset, 0, foodModalMaxScroll);

        ItemStack hoveredFood = null;

        graphics.enableScissor(modalX + 4, gridStartY, modalX + modalW - 4, gridStartY + gridAreaH);

        for (int i = 0; i < foods.size(); i++) {
            ItemStack st = foods.get(i);
            int col = i % gridCols;
            int row = i / gridCols;
            int sx = gridStartX + col * (slotSize + slotGap);
            int sy = gridStartY + row * (slotSize + slotGap) - foodModalScrollOffset;

            if (sy + slotSize < gridStartY || sy > gridStartY + gridAreaH) continue;

            boolean slotHovered = mouseX >= sx && mouseX <= sx + slotSize && mouseY >= sy && mouseY <= sy + slotSize
                    && mouseY >= gridStartY && mouseY <= gridStartY + gridAreaH;

            graphics.fill(sx, sy, sx + slotSize, sy + slotSize, slotHovered ? 0xFF1E2433 : 0xFF161B26);
            graphics.renderOutline(sx, sy, slotSize, slotSize, slotHovered ? 0xFF58A6FF : 0xFF334155);
            graphics.renderFakeItem(st, sx + 1, sy + 1);

            if (slotHovered) {
                hoveredFood = st;
            }
        }

        graphics.disableScissor();

        // Scrollbar if needed
        if (foodModalMaxScroll > 0) {
            int trackX = modalX + modalW - 8;
            int trackY = gridStartY;
            int trackH = gridAreaH;
            graphics.fill(trackX, trackY, trackX + 3, trackY + trackH, 0xFF12151D);
            int thumbH = Math.max(16, (int) ((float) gridAreaH / totalContentH * trackH));
            int thumbY = trackY + (int) (((float) foodModalScrollOffset / foodModalMaxScroll) * (trackH - thumbH));
            graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFF475569);
        }

        // Tooltip rendering for hovered food
        if (hoveredFood != null) {
            var quality = net.zootycoon.economy.ZooEconomyEngine.getFoodQualityInfo(hoveredFood);
            int totalCost = quality.unitPrice() * neededCount;
            boolean canAfford = net.zootycoon.economy.TransactionHelper.hasEnoughEmeralds(mc.player, totalCost);
            String qualityLabel = "Kualitas: " + quality.tierName();

            graphics.renderTooltip(
                    font,
                    List.of(),
                    Optional.of(new FoodPurchaseTooltipData(
                            hoveredFood.getHoverName().getString(),
                            totalCost,
                            quality.unitPrice(),
                            qualityLabel,
                            quality.tierColor(),
                            canAfford
                    )),
                    mouseX,
                    mouseY
            );
        }

        graphics.pose().popPose();
    }

    private static boolean handleMouseClicked(ContainerScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        if (activeFoodSelectionReq != null) {
            int modalW = 230;
            int modalH = 176;
            int modalX = (screen.width - modalW) / 2;
            int modalY = (screen.height - modalH) / 2;

            // Close button [X]
            int closeBtnX = modalX + modalW - 19;
            int closeBtnY = modalY + 5;
            int closeBtnSize = 14;
            if (mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnSize && mouseY >= closeBtnY && mouseY <= closeBtnY + closeBtnSize) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                activeFoodSelectionReq = null;
                return true;
            }

            // Click outside modal closes it
            if (mouseX < modalX || mouseX > modalX + modalW || mouseY < modalY || mouseY > modalY + modalH) {
                activeFoodSelectionReq = null;
                return true;
            }

            // Food slot clicks
            List<ItemStack> foods = activeFoodSelectionReq.targetItems();
            if (foods != null && !foods.isEmpty()) {
                int gridCols = 6;
                int slotSize = 18;
                int slotGap = 4;
                int gridTotalW = gridCols * slotSize + (gridCols - 1) * slotGap;
                int gridStartX = modalX + (modalW - gridTotalW) / 2;
                int gridStartY = modalY + 32;
                int gridAreaH = modalH - 38;

                if (mouseY >= gridStartY && mouseY <= gridStartY + gridAreaH) {
                    for (int i = 0; i < foods.size(); i++) {
                        int col = i % gridCols;
                        int row = i / gridCols;
                        int sx = gridStartX + col * (slotSize + slotGap);
                        int sy = gridStartY + row * (slotSize + slotGap) - foodModalScrollOffset;

                        if (mouseX >= sx && mouseX <= sx + slotSize && mouseY >= sy && mouseY <= sy + slotSize) {
                            ItemStack chosen = foods.get(i);
                            int neededCount = Math.max(16, activeFoodSelectionReq.requiredCount() - activeFoodSelectionReq.currentCount());
                            var quality = net.zootycoon.economy.ZooEconomyEngine.getFoodQualityInfo(chosen);
                            int totalCost = quality.unitPrice() * neededCount;

                            if (net.zootycoon.economy.TransactionHelper.hasEnoughEmeralds(Minecraft.getInstance().player, totalCost)) {
                                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                                BlockPos posToSend = activeBarrelPos != null ? activeBarrelPos : BlockPos.ZERO;
                                ClientPlayNetworking.send(new WarehousePayloads.BuyZookeeperFoodPayload(
                                        posToSend,
                                        activeFoodSelectionReq.staffUuid(),
                                        chosen,
                                        neededCount
                                ));
                                activeFoodSelectionReq = null;
                            } else {
                                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DISPENSER_FAIL, 1.2F));
                            }
                            return true;
                        }
                    }
                }
            }

            return true;
        }

        int panelW = getPanelWidth(screen);
        int panelX = getPanelX(screen);
        int panelY = getPanelY(screen);
        int panelH = getChestHeight(screen);

        if (mouseX < panelX || mouseX > panelX + panelW || mouseY < panelY || mouseY > panelY + panelH) {
            return false;
        }

        int listX = panelX + 5;
        int listY = panelY + 29;
        int listH = panelH - 33;
        int listW = panelW - 10;
        int baseCardW = listW - 6;
        int totalContentH = computeTotalContentHeight(baseCardW, baseCardW);
        int cardW = totalContentH > listH ? (listW - 6) : listW;

        // Check click on scrollbar track
        if (maxScroll > 0 && mouseX >= panelX + panelW - 10 && mouseX <= panelX + panelW && mouseY >= listY && mouseY <= listY + listH) {
            isDraggingScrollbar = true;
            float rel = (float) (mouseY - listY) / (float) Math.max(1, listH);
            scrollOffset = Math.clamp(rel * maxScroll, 0.0f, (float) maxScroll);
            return true;
        }

        if (mouseY >= listY && mouseY <= listY + listH) {
            int curCardY = listY - (int) scrollOffset;
            for (int i = 0; i < activeRequests.size(); i++) {
                WarehousePayloads.StaffRequestDto req = activeRequests.get(i);
                int cardY = curCardY;
                int cardH = getCardHeight(req, baseCardW);
                curCardY += cardH + CARD_GAP;

                if (cardY + cardH < listY || cardY > listY + listH) continue;

                int previewW = 34;
                int infoX = listX + 3 + previewW + 5;
                int infoW = cardW - (infoX - listX) - 4;
                int fixedColsW = baseCardW - (infoX - listX) - 4;
                int slotY = cardY + 25;
                int slotSize = 18;
                int slotGap = 2;
                int actionBtnY = cardY + 46;

                if ("CASHIER".equalsIgnoreCase(req.staffType())) {
                    int totalVariants = req.targetItems().size();
                    int colsPerRow = Math.max(1, (infoW + slotGap) / (slotSize + slotGap));
                    int rows = Math.max(1, (int) Math.ceil((double) Math.max(1, totalVariants) / colsPerRow));
                    int gridH = rows * slotSize + Math.max(0, rows - 1) * slotGap;
                    actionBtnY = slotY + gridH + 3;

                    for (int v = 0; v < totalVariants; v++) {
                        int col = v % colsPerRow;
                        int row = v / colsPerRow;
                        int curX = infoX + col * (slotSize + slotGap);
                        int curY = slotY + row * (slotSize + slotGap);
                        if (mouseX >= curX && mouseX <= curX + slotSize && mouseY >= curY && mouseY <= curY + slotSize) {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            BlockPos posToSend = activeBarrelPos != null ? activeBarrelPos : BlockPos.ZERO;
                            ItemStack clickedStack = req.targetItems().get(v);
                            ClientPlayNetworking.send(new WarehousePayloads.BuyCardItemToBarrelPayload(posToSend, req.staffUuid(), clickedStack, clickedStack.getCount()));
                            return true;
                        }
                    }
                } else {
                    String sub = req.subtitle() != null ? req.subtitle().toLowerCase(java.util.Locale.ROOT) : "";
                    boolean isCareReq = sub.contains("pemanas") || sub.contains("pendingin") || sub.contains("alat mandi") || sub.contains("kotor") || sub.contains("kepanasan") || sub.contains("kedinginan");

                    if (!isCareReq && req.targetItems().size() > 4) {
                        int chevronX = infoX + 4 * (slotSize + slotGap);
                        int chevronY = slotY;
                        int chevronSize = 18;

                        // Click on dropdown chevron button [▼] / [▲] -> EXPAND / COLLAPSE GRID!
                        if (mouseX >= chevronX && mouseX <= chevronX + chevronSize && mouseY >= chevronY && mouseY <= chevronY + chevronSize) {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (expandedFoodCards.contains(req.staffUuid())) {
                                expandedFoodCards.remove(req.staffUuid());
                            } else {
                                expandedFoodCards.add(req.staffUuid());
                            }
                            return true;
                        }
                    }

                    boolean isExpanded = expandedFoodCards.contains(req.staffUuid()) && req.targetItems().size() > 4;
                    int displayedCount = isExpanded ? req.targetItems().size() : Math.min(4, req.targetItems().size());
                    int foodRows = Math.max(1, (int) Math.ceil((double) displayedCount / 4));
                    int foodGridH = foodRows * slotSize + Math.max(0, foodRows - 1) * slotGap;
                    actionBtnY = slotY + foodGridH + 3;

                    for (int k = 0; k < displayedCount; k++) {
                        int col = k % 4;
                        int row = k / 4;
                        int sx = infoX + col * (slotSize + slotGap);
                        int sy = slotY + row * (slotSize + slotGap);
                        if (mouseX >= sx && mouseX <= sx + slotSize && mouseY >= sy && mouseY <= sy + slotSize) {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            BlockPos posToSend = activeBarrelPos != null ? activeBarrelPos : BlockPos.ZERO;
                            ItemStack clickedStack = req.targetItems().get(k);
                            int buyCount = isCareReq ? (clickedStack.getMaxStackSize() > 1 ? 16 : 1) : 16;
                            ClientPlayNetworking.send(new WarehousePayloads.BuyCardItemToBarrelPayload(posToSend, req.staffUuid(), clickedStack, buyCount));
                            return true;
                        }
                    }
                }

                int btnY = actionBtnY;
                int btnH = 17;
                int checkBtnSize = 17;
                int btnGap = 3;
                int ambilResepW = infoW - checkBtnSize - btnGap;
                int ambilResepX = infoX;
                int checkBtnX = infoX + ambilResepW + btnGap;

                // Square Checklist Delivery Button [✓]
                if (mouseX >= checkBtnX && mouseX <= checkBtnX + checkBtnSize && mouseY >= btnY && mouseY <= btnY + btnH) {
                    if (isCardReadyToFulfill(req)) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        BlockPos posToSend = activeBarrelPos != null ? activeBarrelPos : BlockPos.ZERO;
                        ClientPlayNetworking.send(new WarehousePayloads.FulfillStaffRequestPayload(posToSend, req.staffUuid()));
                        boolean allItemsReady = req.menuItems().isEmpty()
                                || req.menuItems().stream().allMatch(WarehousePayloads.MenuItemRequestDto::isReadyInBarrel);
                        if (allItemsReady) {
                            activeRequests.removeIf(r -> r.staffUuid().equals(req.staffUuid()));
                        }
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DISPENSER_FAIL, 1.2F));
                    }
                    return true;
                }

                // Ambil Resep / Ambil Catatan Button
                if (mouseX >= ambilResepX && mouseX <= ambilResepX + ambilResepW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    BlockPos posToSend = activeBarrelPos != null ? activeBarrelPos : BlockPos.ZERO;
                    ClientPlayNetworking.send(new WarehousePayloads.RequestBarrelRecipeNotePayload(posToSend, req.staffUuid()));
                    return true;
                }
            }
        }

        return true;
    }

    private static boolean handleMouseScrolled(ContainerScreen screen, double mouseX, double mouseY, double verticalAmount) {
        if (activeFoodSelectionReq != null) {
            foodModalScrollOffset = Math.clamp(foodModalScrollOffset - (int) (verticalAmount * 18), 0, foodModalMaxScroll);
            return true;
        }

        int panelW = getPanelWidth(screen);
        int panelX = getPanelX(screen);
        int panelY = getPanelY(screen);
        int panelH = getChestHeight(screen);

        if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) {
            if (maxScroll > 0) {
                scrollOffset = Math.clamp(scrollOffset - (float) verticalAmount * 22.0f, 0.0f, (float) maxScroll);
            }
            return true;
        }
        return false;
    }

    private static LivingEntity resolveStaffEntity(WarehousePayloads.StaffRequestDto req) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        if (req.staffEntityId() != -1) {
            Entity ent = mc.level.getEntity(req.staffEntityId());
            if (ent instanceof LivingEntity living) {
                return living;
            }
        }
        for (Entity ent : mc.level.entitiesForRendering()) {
            if (ent.getUUID().equals(req.staffUuid()) && ent instanceof LivingEntity living) {
                return living;
            }
        }
        if (dummyStaffCache.containsKey(req.staffUuid())) {
            return dummyStaffCache.get(req.staffUuid());
        }
        LivingEntity dummy = "CASHIER".equalsIgnoreCase(req.staffType())
                ? ZooEntities.CASHIER.create(mc.level)
                : ZooEntities.ZOOKEEPER.create(mc.level);
        if (dummy != null) {
            dummyStaffCache.put(req.staffUuid(), dummy);
        }
        return dummy;
    }

    private static boolean isCardReadyToFulfill(WarehousePayloads.StaffRequestDto req) {
        if (req == null) return false;
        if (req.allReadyToFulfill()) return true;
        if ("CASHIER".equalsIgnoreCase(req.staffType())) {
            return !req.menuItems().isEmpty() && req.menuItems().stream().anyMatch(WarehousePayloads.MenuItemRequestDto::isReadyInBarrel);
        }
        return false;
    }
}
