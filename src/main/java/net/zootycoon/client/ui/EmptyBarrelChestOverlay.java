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

    private static final Map<UUID, LivingEntity> dummyStaffCache = new HashMap<>();

    private static final int CHEST_WIDTH = 176;
    private static final int CHEST_HEIGHT = 166;
    private static final int CARD_HEIGHT = 68;
    private static final int CARD_GAP = 5;

    public record RawIngredientsTooltipData(
            String recipeName,
            int totalEmeraldReward,
            List<ItemStack> ingredients
    ) implements TooltipComponent {
    }

    public static class RawIngredientsClientTooltip implements ClientTooltipComponent {
        private final String recipeName;
        private final int totalEmeraldReward;
        private final List<ItemStack> ingredients;
        private static final int ROW_HEIGHT = 14;
        private static final int HEADER_HEIGHT = 16;
        private static final ItemStack EMERALD_STACK = new ItemStack(net.minecraft.world.item.Items.EMERALD);

        public RawIngredientsClientTooltip(RawIngredientsTooltipData data) {
            this.recipeName = data.recipeName();
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
                String rewardStr = "+" + totalEmeraldReward;
                int headerW = font.width(recipeName) + 6 + font.width(rewardStr) + 2 + 13;
                maxW = Math.max(maxW, headerW);
            }
            for (ItemStack raw : ingredients) {
                String text = raw.getCount() + "x " + raw.getHoverName().getString();
                maxW = Math.max(maxW, 14 + font.width(text));
            }
            return maxW;
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
            int curY = y;
            if (recipeName != null && !recipeName.isEmpty()) {
                // Recipe Name
                graphics.pose().pushPose();
                graphics.pose().translate(0.0f, 0.0f, 400.0f);
                graphics.drawString(font, recipeName, x, curY + 2, 0xFFFFFFFF, true);

                // +Emerald Reward Text
                int nameW = font.width(recipeName);
                String rewardStr = "+" + totalEmeraldReward;
                graphics.drawString(font, rewardStr, x + nameW + 5, curY + 2, 0xFF55FF55, true);
                graphics.pose().popPose();

                // Emerald Item Icon
                int rewardW = font.width(rewardStr);
                int emX = x + nameW + 5 + rewardW + 2;
                graphics.pose().pushPose();
                graphics.pose().translate(emX, curY, 400.0f);
                graphics.pose().scale(0.75f, 0.75f, 1.0f);
                graphics.renderFakeItem(EMERALD_STACK, 0, 0);
                graphics.pose().popPose();

                curY += HEADER_HEIGHT;
            }

            for (ItemStack raw : ingredients) {
                graphics.pose().pushPose();
                graphics.pose().translate(x, curY - 1, 400.0f);
                graphics.pose().scale(0.75f, 0.75f, 1.0f);
                graphics.renderFakeItem(raw, 0, 0);
                graphics.pose().popPose();

                String text = raw.getCount() + "x " + raw.getHoverName().getString();
                graphics.pose().pushPose();
                graphics.pose().translate(0.0f, 0.0f, 400.0f);
                graphics.drawString(font, text, x + 14, curY + 1, 0xFFAAAAAA, true);
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
    }

    public static void register() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof RawIngredientsTooltipData rawData) {
                return new RawIngredientsClientTooltip(rawData);
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
                || title.equals(ZooTranslations.get("zootycoon.warehouse.barrel.title.animal_food"));
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
        return (screen.height - CHEST_HEIGHT) / 2;
    }

    private static final int ZOOKEEPER_FOOD_COLS = 5;

    private static int getCardHeight(WarehousePayloads.StaffRequestDto req, int cardW) {
        if ("CASHIER".equalsIgnoreCase(req.staffType())) {
            return CARD_HEIGHT;
        }
        int slotSize = 18;
        int slotGap = 2;
        int colsPerRow = ZOOKEEPER_FOOD_COLS;
        List<ItemStack> targets = req.targetItems();
        boolean hasMultipleFoodRows = targets.size() > colsPerRow;
        boolean isExpanded = hasMultipleFoodRows && expandedFoodCards.contains(req.staffUuid());
        int displayedCount = (isExpanded || !hasMultipleFoodRows) ? targets.size() : colsPerRow;
        int foodRows = Math.max(1, (int) Math.ceil((double) Math.max(1, displayedCount) / colsPerRow));
        int foodGridH = foodRows * slotSize + Math.max(0, foodRows - 1) * slotGap;
        int toggleBtnH = 15;
        int actionBtnYOffset = hasMultipleFoodRows ? (25 + foodGridH + 3 + toggleBtnH + 3) : (25 + foodGridH + 3);
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
        int panelH = CHEST_HEIGHT;

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
                int maxVariants = Math.min(2, targets.size());
                int curX = infoX;
                for (int v = 0; v < maxVariants; v++) {
                    ItemStack st = targets.get(v);
                    graphics.fill(curX, slotY, curX + slotSize, slotY + slotSize, 0xFF12151D);
                    graphics.renderOutline(curX, slotY, slotSize, slotSize, 0xFF334155);
                    graphics.renderFakeItem(st, curX + 1, slotY + 1);
                    graphics.renderItemDecorations(font, st, curX + 1, slotY + 1, String.valueOf(st.getCount()));

                    if (mouseX >= curX && mouseX <= curX + slotSize && mouseY >= slotY && mouseY <= slotY + slotSize
                            && mouseY >= listY && mouseY <= listY + listH) {
                        hoveredStack = st;
                        hoveredDto = req;
                    }

                    if (maxVariants == 1) {
                        String itemName = st.getHoverName().getString();
                        int nameMaxW = infoW - slotSize - 4;
                        if (font.width(itemName) > nameMaxW) {
                            itemName = font.plainSubstrByWidth(itemName, Math.max(8, nameMaxW - font.width(".."))) + "..";
                        }
                        graphics.drawString(font, itemName, curX + slotSize + 4, slotY + 5, 0xFFE2E8F0, false);
                    }
                    curX += slotSize + 4;
                }
            } else {
                int colsPerRow = ZOOKEEPER_FOOD_COLS;
                boolean hasMultipleFoodRows = targets.size() > colsPerRow;
                boolean isExpanded = hasMultipleFoodRows && expandedFoodCards.contains(req.staffUuid());
                int displayedCount = (isExpanded || !hasMultipleFoodRows) ? targets.size() : colsPerRow;

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

                int foodRows = Math.max(1, (int) Math.ceil((double) Math.max(1, displayedCount) / colsPerRow));
                int foodGridH = foodRows * slotSize + Math.max(0, foodRows - 1) * slotGap;

                if (hasMultipleFoodRows) {
                    int toggleBtnX = infoX;
                    int toggleBtnY = slotY + foodGridH + 3;
                    int toggleBtnW = infoW;
                    int toggleBtnH = 15;
                    boolean toggleHovered = mouseX >= toggleBtnX && mouseX <= toggleBtnX + toggleBtnW
                            && mouseY >= toggleBtnY && mouseY <= toggleBtnY + toggleBtnH
                            && mouseY >= listY && mouseY <= listY + listH;

                    int tBtnBg = toggleHovered ? 0xFF263045 : 0xFF19202D;
                    int tBtnBorder = toggleHovered ? 0xFF58A6FF : 0xFF354157;
                    graphics.fill(toggleBtnX, toggleBtnY, toggleBtnX + toggleBtnW, toggleBtnY + toggleBtnH, tBtnBg);
                    graphics.renderOutline(toggleBtnX, toggleBtnY, toggleBtnW, toggleBtnH, tBtnBorder);

                    String toggleText = isExpanded ? "▲ Sembunyikan" : "▼ Lihat Semua";
                    int textW = font.width(toggleText);
                    graphics.drawString(
                            font,
                            toggleText,
                            toggleBtnX + (toggleBtnW - textW) / 2,
                            toggleBtnY + (toggleBtnH - 8) / 2,
                            toggleHovered ? 0xFFFFFFFF : 0xFFC0CAD8,
                            false
                    );

                    actionBtnY = toggleBtnY + toggleBtnH + 3;
                } else {
                    actionBtnY = slotY + foodGridH + 3;
                }
            }

            // Row 4 / 5: Ambil Resep / Ambil Catatan Button
            int btnX = infoX;
            int btnY = actionBtnY;
            int btnW = infoW;
            int btnH = 17;

            boolean btnHovered = mouseX >= btnX && mouseX <= btnX + btnW
                    && mouseY >= btnY && mouseY <= btnY + btnH
                    && mouseY >= listY && mouseY <= listY + listH;

            int bgColor = btnHovered ? 0xFF252D3D : 0xFF14171F;
            int borderColor = btnHovered ? 0xFFFFFFFF : 0xFF3A4456;
            int txtColor = btnHovered ? 0xFFFFFFFF : 0xFFD0D7DE;

            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, bgColor);
            graphics.renderOutline(btnX, btnY, btnW, btnH, borderColor);

            String btnLabel = "CASHIER".equalsIgnoreCase(req.staffType())
                    ? ZooTranslations.get("zootycoon.warehouse.btn.take_recipe")
                    : ZooTranslations.get("zootycoon.warehouse.btn.take_food_note");
            int lblW = font.width(btnLabel);
            graphics.drawString(font, btnLabel, btnX + (btnW - lblW) / 2, btnY + (btnH - 8) / 2, txtColor, false);
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

        // 5. Vanilla Tooltip with real food icons inline via TooltipComponent
        if (hoveredStack != null && hoveredDto != null) {
            if ("CASHIER".equalsIgnoreCase(hoveredDto.staffType())) {
                String menuName = hoveredStack.getHoverName().getString();
                String role = (activeCategory != null && !activeCategory.isEmpty())
                        ? activeCategory
                        : (hoveredDto.subtitle().toLowerCase().contains("burger") ? "BURGER" : "DRINK");
                int unitPrice = net.zootycoon.economy.ZooEconomyEngine.getFoodDrinkPrice(role, hoveredStack);
                int totalUnits = Math.max(1, hoveredStack.getCount());
                int totalReward = unitPrice * totalUnits;

                graphics.renderTooltip(
                        font,
                        List.of(),
                        Optional.of(new RawIngredientsTooltipData(menuName, totalReward, hoveredDto.rawIngredients())),
                        mouseX,
                        mouseY
                );
            } else {
                List<Component> lines = new ArrayList<>();
                lines.add(hoveredStack.getHoverName());
                lines.add(Component.literal(ZooTranslations.get("zootycoon.warehouse.tooltip.zookeeper_food")).withStyle(ChatFormatting.GRAY));
                graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private static boolean handleMouseClicked(ContainerScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int panelW = getPanelWidth(screen);
        int panelX = getPanelX(screen);
        int panelY = getPanelY(screen);
        int panelH = CHEST_HEIGHT;

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

                if (!"CASHIER".equalsIgnoreCase(req.staffType())) {
                    List<ItemStack> targets = req.targetItems();
                    int colsPerRow = ZOOKEEPER_FOOD_COLS;
                    boolean hasMultipleFoodRows = targets.size() > colsPerRow;
                    boolean isExpanded = hasMultipleFoodRows && expandedFoodCards.contains(req.staffUuid());
                    int displayedCount = (isExpanded || !hasMultipleFoodRows) ? targets.size() : colsPerRow;
                    int foodRows = Math.max(1, (int) Math.ceil((double) Math.max(1, displayedCount) / colsPerRow));
                    int foodGridH = foodRows * slotSize + Math.max(0, foodRows - 1) * slotGap;

                    if (hasMultipleFoodRows) {
                        int toggleBtnX = infoX;
                        int toggleBtnY = slotY + foodGridH + 3;
                        int toggleBtnW = infoW;
                        int toggleBtnH = 15;

                        if (mouseX >= toggleBtnX && mouseX <= toggleBtnX + toggleBtnW && mouseY >= toggleBtnY && mouseY <= toggleBtnY + toggleBtnH) {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (isExpanded) {
                                expandedFoodCards.remove(req.staffUuid());
                            } else {
                                expandedFoodCards.add(req.staffUuid());
                            }
                            return true;
                        }
                        actionBtnY = toggleBtnY + toggleBtnH + 3;
                    } else {
                        actionBtnY = slotY + foodGridH + 3;
                    }
                }

                int btnX = infoX;
                int btnY = actionBtnY;
                int btnW = infoW;
                int btnH = 17;

                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
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
        int panelW = getPanelWidth(screen);
        int panelX = getPanelX(screen);
        int panelY = getPanelY(screen);
        int panelH = CHEST_HEIGHT;

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
}
