package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.entity.ZooCashierEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CashierDialogScreen extends EasyNpcDialogScreen {

    private final int entityId;
    private final String currentRole;
    private final List<ItemStack> stockItems = new ArrayList<>(ZooCashierEntity.STOCK_SLOTS);
    private boolean isZooOpen;
    private int ticketPrice = net.zootycoon.economy.ZooEconomyEngine.DEFAULT_TICKET_PRICE;
    private ZooButton toggleZooBtn;
    private final List<ZooButton> tierButtons = new ArrayList<>();
    private static final ItemStack EMERALD_ITEM_ICON = new ItemStack(net.minecraft.world.item.Items.EMERALD);

    // Ticket Cashier Tabs: 0 = Settings, 1 = Visitor Feedback
    private int activeTicketTab = 0;
    private ZooButton tabSettingsBtn;
    private ZooButton tabFeedbackBtn;
    private ZooButton sortFeedbackBtn;
    private boolean sortByUrgency = true;
    private final List<ZooNetworkPayloads.VisitorFeedbackItemDto> visitorFeedbacks = new ArrayList<>();
    private double feedbackScrollOffset = 0;
    private int feedbackTotalContentH = 0;

    public record CashierPriceTooltipData(Component name, int price) implements net.minecraft.world.inventory.tooltip.TooltipComponent {}

    public static class CashierPriceClientTooltip implements net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent {
        private final Component name;
        private final int price;
        private static final ItemStack EMERALD_ICON = new ItemStack(net.minecraft.world.item.Items.EMERALD);

        public CashierPriceClientTooltip(CashierPriceTooltipData data) {
            this.name = data.name();
            this.price = data.price();
        }

        @Override
        public int getHeight() {
            return 26;
        }

        @Override
        public int getWidth(net.minecraft.client.gui.Font font) {
            int w1 = font.width(name);
            int w2 = font.width("Harga Jual: " + price) + 16;
            return Math.max(w1, w2);
        }

        @Override
        public void renderImage(net.minecraft.client.gui.Font font, int x, int y, GuiGraphics graphics) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0f, 0.0f, 400.0f);
            graphics.drawString(font, name, x, y + 2, 0xFFFFFFFF, true);

            String priceLabel = "Harga Jual: " + price;
            graphics.drawString(font, priceLabel, x, y + 14, 0xFF55FF55, true);
            int labelW = font.width(priceLabel);

            graphics.pose().translate(x + labelW + 3, y + 10, 0.0f);
            graphics.pose().scale(0.75f, 0.75f, 1.0f);
            graphics.renderFakeItem(EMERALD_ICON, 0, 0);
            graphics.pose().popPose();
        }
    }

    public static void register() {
        net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof CashierPriceTooltipData pData) {
                return new CashierPriceClientTooltip(pData);
            }
            return null;
        });
    }

    // Layout coordinates for the 5 cashier stock slots and 36 player inventory slots
    private int stockStartX;
    private int stockStartY;
    private static final int STOCK_SLOT_SIZE = 20;
    private static final int STOCK_SLOT_GAP = 6;

    private int invStartX;
    private int invStartY;
    private static final int INV_SLOT_SIZE = 18;
    private static final int INV_SLOT_GAP = 1;

    // Drag-and-drop state from player inventory to cashier slots
    private int draggedPlayerSlot = -1;
    private int dragButton = 0;
    private boolean isDraggingItem = false;
    private double dragOriginX = 0;
    private double dragOriginY = 0;

    public CashierDialogScreen(int entityId, String currentRole, String cashierName, List<ItemStack> initialStock, boolean isZooOpen, int ticketPrice, List<ZooNetworkPayloads.VisitorFeedbackItemDto> initialFeedbacks) {
        super(
                Component.literal(cashierName.isEmpty() ? ZooTranslations.get("zootycoon.dialog.cashier.default_name") : cashierName),
                Component.literal(cashierName.isEmpty() ? ZooTranslations.get("zootycoon.dialog.cashier.title") : cashierName),
                ZooTranslations.component("zootycoon.dialog.cashier.role"),
                buildSpeechComponent(currentRole, initialStock, isZooOpen, ticketPrice),
                resolveCashierEntity(entityId)
        );
        this.entityId = entityId;
        this.currentRole = currentRole != null ? currentRole : "UNASSIGNED";
        this.isZooOpen = isZooOpen;
        this.ticketPrice = Math.clamp(ticketPrice, net.zootycoon.economy.ZooEconomyEngine.MIN_TICKET_PRICE, net.zootycoon.economy.ZooEconomyEngine.MAX_TICKET_PRICE);
        updateStockItems(initialStock);
        updateFeedbacks(initialFeedbacks);
    }

    public CashierDialogScreen(int entityId, String currentRole, String cashierName, List<ItemStack> initialStock, boolean isZooOpen, int ticketPrice) {
        this(entityId, currentRole, cashierName, initialStock, isZooOpen, ticketPrice, List.of());
    }

    public CashierDialogScreen(int entityId, String currentRole, String cashierName, List<ItemStack> initialStock, boolean isZooOpen) {
        this(entityId, currentRole, cashierName, initialStock, isZooOpen, net.zootycoon.economy.ZooEconomyEngine.DEFAULT_TICKET_PRICE);
    }

    public CashierDialogScreen(int entityId, String currentRole, String cashierName, List<ItemStack> initialStock) {
        this(entityId, currentRole, cashierName, initialStock, false, net.zootycoon.economy.ZooEconomyEngine.DEFAULT_TICKET_PRICE);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getCurrentRole() {
        return this.currentRole;
    }

    public int getTicketPrice() {
        return this.ticketPrice;
    }

    public void updateFeedbacks(List<ZooNetworkPayloads.VisitorFeedbackItemDto> newFeedbacks) {
        this.visitorFeedbacks.clear();
        if (newFeedbacks != null) {
            this.visitorFeedbacks.addAll(newFeedbacks);
        }
        applyFeedbackSort();
    }

    private void applyFeedbackSort() {
        if (sortByUrgency) {
            this.visitorFeedbacks.sort((a, b) -> Integer.compare(b.urgencyLevel(), a.urgencyLevel()));
        }
    }

    public void updateTicketPrice(int price) {
        this.ticketPrice = Math.clamp(price, net.zootycoon.economy.ZooEconomyEngine.MIN_TICKET_PRICE, net.zootycoon.economy.ZooEconomyEngine.MAX_TICKET_PRICE);
        updateTierButtonsStyle();
        this.dialogMessage = buildSpeechComponent(this.currentRole, this.stockItems, this.isZooOpen, this.ticketPrice);
        this.dialogStartTime = System.currentTimeMillis();
    }

    private void setTicketPrice(int newPrice) {
        this.ticketPrice = Math.clamp(newPrice, net.zootycoon.economy.ZooEconomyEngine.MIN_TICKET_PRICE, net.zootycoon.economy.ZooEconomyEngine.MAX_TICKET_PRICE);
        ClientPlayNetworking.send(new ZooNetworkPayloads.SetTicketPricePayload(this.entityId, this.ticketPrice));
        updateTierButtonsStyle();
        this.dialogMessage = buildSpeechComponent(this.currentRole, this.stockItems, this.isZooOpen, this.ticketPrice);
        this.dialogStartTime = System.currentTimeMillis();
    }

    private void updateTierButtonsStyle() {
        int[] tiers = net.zootycoon.economy.ZooEconomyEngine.TICKET_PRICE_TIERS;
        for (int i = 0; i < tierButtons.size() && i < tiers.length; i++) {
            boolean isSelected = (tiers[i] == this.ticketPrice);
            tierButtons.get(i).setSelected(isSelected);
            if (isSelected) {
                tierButtons.get(i).asPrimary();
            } else {
                tierButtons.get(i).asSecondary();
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if ("TICKET".equalsIgnoreCase(currentRole) && activeTicketTab == 1) {
            int rightX = startX + 114 + 14;
            int rightW = panelWidth - (rightX - startX) - 12;
            int topY = startY + 60;
            int btnH = 20;
            int totalH = panelHeight - 70 - btnH - 6;
            int listY = topY + 4;
            int listH = totalH - 8;
            if (mouseX >= rightX && mouseX <= rightX + rightW && mouseY >= listY && mouseY <= listY + listH) {
                int maxScroll = Math.max(0, feedbackTotalContentH - listH);
                feedbackScrollOffset = Math.clamp(feedbackScrollOffset - verticalAmount * 16.0D, 0, maxScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public void updateZooOpenState(boolean isZooOpen) {
        this.isZooOpen = isZooOpen;
        if (this.toggleZooBtn != null) {
            this.toggleZooBtn.setMessage(getToggleZooComponent());
            if (this.isZooOpen) {
                this.toggleZooBtn.asDanger();
            } else {
                this.toggleZooBtn.asPrimary();
            }
        }
        this.dialogMessage = buildSpeechComponent(this.currentRole, this.stockItems, this.isZooOpen, this.ticketPrice);
        this.dialogStartTime = System.currentTimeMillis();
    }

    private Component getToggleZooComponent() {
        return this.isZooOpen
                ? ZooTranslations.component("zootycoon.dialog.cashier.btn.close_zoo")
                : ZooTranslations.component("zootycoon.dialog.cashier.btn.open_zoo");
    }

    public void updateStockItems(List<ItemStack> newStock) {
        this.stockItems.clear();
        for (int i = 0; i < ZooCashierEntity.STOCK_SLOTS; i++) {
            if (newStock != null && i < newStock.size() && newStock.get(i) != null) {
                this.stockItems.add(newStock.get(i));
            } else {
                this.stockItems.add(ItemStack.EMPTY);
            }
        }
        this.dialogMessage = buildSpeechComponent(this.currentRole, this.stockItems, this.isZooOpen, this.ticketPrice);
    }

    private boolean isStockRole() {
        return "BURGER".equalsIgnoreCase(currentRole)
                || "DRINK".equalsIgnoreCase(currentRole)
                || "ICE_CREAM".equalsIgnoreCase(currentRole);
    }

    private boolean isUnassigned() {
        return currentRole == null || currentRole.isEmpty() || "UNASSIGNED".equalsIgnoreCase(currentRole);
    }

    private static boolean hasAnyStock(List<ItemStack> items) {
        if (items == null) return false;
        for (ItemStack st : items) {
            if (st != null && !st.isEmpty()) return true;
        }
        return false;
    }

    private static Component buildSpeechComponent(String role, List<ItemStack> stock, boolean isZooOpen, int price) {
        if (role == null || "UNASSIGNED".equalsIgnoreCase(role)) {
            return ZooTranslations.component("zootycoon.dialog.cashier.speech");
        }
        if ("TICKET".equalsIgnoreCase(role)) {
            double starRating = net.zootycoon.client.ZooTycoonClient.getClientStarRating();
            return net.zootycoon.economy.ZooEconomyEngine.getTicketCashierSpeech(price, starRating, isZooOpen);
        }
        boolean stocked = hasAnyStock(stock);
        if ("BURGER".equalsIgnoreCase(role)) {
            return stocked
                    ? ZooTranslations.component("zootycoon.dialog.cashier.speech.burger_ready")
                    : ZooTranslations.component("zootycoon.dialog.cashier.speech.burger_empty");
        }
        if ("ICE_CREAM".equalsIgnoreCase(role)) {
            return stocked
                    ? ZooTranslations.component("zootycoon.dialog.cashier.speech.ice_cream_ready")
                    : ZooTranslations.component("zootycoon.dialog.cashier.speech.ice_cream_empty");
        }
        if ("DRINK".equalsIgnoreCase(role)) {
            return stocked
                    ? ZooTranslations.component("zootycoon.dialog.cashier.speech.drink_ready")
                    : ZooTranslations.component("zootycoon.dialog.cashier.speech.drink_empty");
        }
        return ZooTranslations.component("zootycoon.dialog.cashier.speech");
    }

    private static LivingEntity resolveCashierEntity(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            if (entityId != -1) {
                var ent = mc.level.getEntity(entityId);
                if (ent instanceof LivingEntity living) return living;
            }
            return ZooEntities.CASHIER.create(mc.level);
        }
        return null;
    }

    private static final net.minecraft.resources.ResourceLocation ROTATE_ICON =
            net.zootycoon.ZooTycoonMod.id("textures/gui/icons/rotate.png");

    @Override
    protected void initDialogWidgets() {
        int rightX = startX + 114 + 14;
        int rightW = panelWidth - (rightX - startX) - 12;

        // Small icon-only rotate button at the top-right corner of the dialog container (both before and after job assignment)
        int rotBtnSize = 16;
        int rotBtnX = startX + panelWidth - rotBtnSize - 10;
        int rotBtnY = startY + 10;
        var rotateBtn = new ZooButton(rotBtnX, rotBtnY, rotBtnSize, rotBtnSize, Component.empty(), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.RotateCashierPayload(this.entityId));
            if (this.previewEntity instanceof ZooCashierEntity cashier) {
                cashier.rotateClockwise();
            }
        }).setCustomIcon(ROTATE_ICON, 12, 12, 16, 16).asPrimary();
        addRenderableWidget(rotateBtn);

        // Small close button ('x') at the top right next to rotate button
        int closeIconSize = 16;
        int closeIconX = rotBtnX - closeIconSize - 4;
        var closeIconBtn = new ZooButton(closeIconX, rotBtnY, closeIconSize, closeIconSize, Component.literal("x"), b -> {
            this.onClose();
        }).asSecondary();
        addRenderableWidget(closeIconBtn);

        if (isUnassigned()) {
            int colW = (rightW - 6) / 2;
            int btnH = 22;
            int gap = 4;
            int startBtnY = startY + 88;

            var burgerBtn = new ZooButton(rightX, startBtnY, colW, btnH,
                    ZooTranslations.component("zootycoon.dialog.cashier.btn.burger"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "BURGER"));
            }).asPrimary();
            addRenderableWidget(burgerBtn);

            var drinkBtn = new ZooButton(rightX + colW + 6, startBtnY, colW, btnH,
                    ZooTranslations.component("zootycoon.dialog.cashier.btn.drink"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "DRINK"));
            }).asPrimary();
            addRenderableWidget(drinkBtn);

            var iceCreamBtn = new ZooButton(rightX, startBtnY + btnH + gap, colW, btnH,
                    ZooTranslations.component("zootycoon.dialog.cashier.btn.ice_cream"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "ICE_CREAM"));
            }).asPrimary();
            addRenderableWidget(iceCreamBtn);

            var ticketBtn = new ZooButton(rightX + colW + 6, startBtnY + btnH + gap, colW, btnH,
                    ZooTranslations.component("zootycoon.dialog.cashier.btn.ticket"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "TICKET"));
            }).asPrimary();
            addRenderableWidget(ticketBtn);

            var closeBtn = new ZooButton(rightX, startBtnY + (btnH + gap) * 2, rightW, btnH,
                    ZooTranslations.component("zootycoon.dialog.cashier.btn.later"), b -> {
                this.onClose();
            }).asPrimary();
            addRenderableWidget(closeBtn);
        } else if ("TICKET".equalsIgnoreCase(currentRole)) {
            // Tab Header Buttons: [Tab 1: Pengaturan Tiket] [Tab 2: Masukan Pengunjung]
            int tabY = startY + 38;
            int tabGap = 4;
            int tabW = (rightW - tabGap) / 2;
            int tabH = 18;
            this.tabSettingsBtn = new ZooButton(rightX, tabY, tabW, tabH,
                    ZooTranslations.component("zootycoon.dialog.cashier.tab.ticket"), b -> {
                this.activeTicketTab = 0;
                updateTicketTabVisibility();
            });
            this.tabFeedbackBtn = new ZooButton(rightX + tabW + tabGap, tabY, tabW, tabH,
                    ZooTranslations.component("zootycoon.dialog.cashier.tab.feedback"), b -> {
                this.activeTicketTab = 1;
                updateTicketTabVisibility();
            });
            addRenderableWidget(this.tabSettingsBtn);
            addRenderableWidget(this.tabFeedbackBtn);

            // Sort button for Feedback Tab (positioned at the bottom where toggle button sits)
            int btnH = 20;
            int sortY = startY + panelHeight - btnH - 10;
            this.sortFeedbackBtn = new ZooButton(rightX, sortY, rightW, btnH,
                    ZooTranslations.component(sortByUrgency ? "zootycoon.dialog.cashier.feedback.sort_urgency" : "zootycoon.dialog.cashier.feedback.sort_time"), b -> {
                this.sortByUrgency = !this.sortByUrgency;
                this.sortFeedbackBtn.setMessage(ZooTranslations.component(sortByUrgency ? "zootycoon.dialog.cashier.feedback.sort_urgency" : "zootycoon.dialog.cashier.feedback.sort_time"));
                applyFeedbackSort();
            }).asSecondary();
            addRenderableWidget(this.sortFeedbackBtn);

            int bubbleH = getSpeechBubbleHeight();
            int buttonsY = startY + 60 + bubbleH + 8;

            int[] tiers = net.zootycoon.economy.ZooEconomyEngine.TICKET_PRICE_TIERS;
            this.tierButtons.clear();
            int gap = 4;
            int btnCount = tiers.length;
            int gratisW = 48;
            int otherW = (rightW - gratisW - (btnCount - 1) * gap) / (btnCount - 1);

            int curBtnX = rightX;
            for (int i = 0; i < btnCount; i++) {
                int tierPrice = tiers[i];
                int w = (i == 0) ? gratisW : otherW;
                String label = (tierPrice == 0) ? "Gratis" : String.valueOf(tierPrice);
                var btn = new ZooButton(curBtnX, buttonsY, w, 20, Component.literal(label), b -> {
                    setTicketPrice(tierPrice);
                });
                if (tierPrice > 0) {
                    btn.setItemIcon(EMERALD_ITEM_ICON).setIconAfter(true);
                }
                boolean isSelected = (tierPrice == this.ticketPrice);
                btn.setSelected(isSelected);
                if (isSelected) {
                    btn.asPrimary();
                } else {
                    btn.asSecondary();
                }
                this.tierButtons.add(btn);
                addRenderableWidget(btn);
                curBtnX += w + gap;
            }

            // Role is TICKET: toggle open/close zoo button replaces "Selesai" button
            int closeY = startY + panelHeight - btnH - 10;
            this.toggleZooBtn = new ZooButton(rightX, closeY, rightW, btnH,
                    getToggleZooComponent(), b -> {
                this.isZooOpen = !this.isZooOpen;
                this.toggleZooBtn.setMessage(getToggleZooComponent());
                if (this.isZooOpen) {
                    this.toggleZooBtn.asDanger();
                } else {
                    this.toggleZooBtn.asPrimary();
                }
                this.dialogMessage = buildSpeechComponent(this.currentRole, this.stockItems, this.isZooOpen, this.ticketPrice);
                this.dialogStartTime = System.currentTimeMillis();
                ClientPlayNetworking.send(new ZooNetworkPayloads.ToggleZooOpenPayload(this.entityId));
            });
            if (this.isZooOpen) {
                this.toggleZooBtn.asDanger();
            } else {
                this.toggleZooBtn.asPrimary();
            }
            addRenderableWidget(this.toggleZooBtn);

            updateTicketTabVisibility();
        } else {
            // Role is permanently assigned (BURGER / DRINK): single Close button at bottom
            int btnH = 20;
            int closeY = startY + panelHeight - btnH - 8;
            var closeBtn = new ZooButton(rightX, closeY, rightW, btnH,
                    ZooTranslations.component("zootycoon.dialog.cashier.btn.done"), b -> {
                this.onClose();
            }).asPrimary();
            addRenderableWidget(closeBtn);
        }
    }

    private void updateTicketTabVisibility() {
        if (!"TICKET".equalsIgnoreCase(currentRole)) return;
        boolean inSettings = (activeTicketTab == 0);
        if (tabSettingsBtn != null) {
            tabSettingsBtn.setSelected(inSettings);
            if (inSettings) tabSettingsBtn.asPrimary(); else tabSettingsBtn.asSecondary();
        }
        if (tabFeedbackBtn != null) {
            tabFeedbackBtn.setSelected(!inSettings);
            if (!inSettings) tabFeedbackBtn.asPrimary(); else tabFeedbackBtn.asSecondary();
        }
        for (ZooButton btn : tierButtons) {
            btn.visible = inSettings;
        }
        if (toggleZooBtn != null) {
            toggleZooBtn.visible = inSettings;
        }
        if (sortFeedbackBtn != null) {
            sortFeedbackBtn.visible = !inSettings;
        }
    }

    @Override
    protected int getSpeechBubbleHeight() {
        if (isStockRole()) {
            return 32;
        }
        if ("TICKET".equalsIgnoreCase(currentRole)) {
            return 52;
        }
        return 44;
    }

    @Override
    protected int getContentHeight() {
        if (isStockRole()) {
            return 120;
        }
        if ("TICKET".equalsIgnoreCase(currentRole)) {
            return 56;
        }
        return 48;
    }


    private void renderFeedbackTab(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta) {
        int rightX = startX + 114 + 14;
        int rightW = panelWidth - (rightX - startX) - 12;

        // Render slate container for visitor feedback list starting below tab row, ending above bottom button
        int topY = startY + 60;
        int btnH = 20;
        int totalH = panelHeight - 70 - btnH - 6;
        graphics.fill(rightX - 2, topY, rightX + rightW + 2, topY + totalH, 0xFF14171F);
        graphics.renderOutline(rightX - 2, topY, rightW + 4, totalH, 0xFF2A3140);

        int listX = rightX + 2;
        int listY = topY + 4;
        int listW = rightW - 4;
        int listH = totalH - 8;

        if (this.visitorFeedbacks.isEmpty()) {
            graphics.drawCenteredString(this.font, ZooTranslations.component("zootycoon.dialog.cashier.feedback.empty"), listX + listW / 2, listY + listH / 2 - 4, 0xFF718096);
            return;
        }

        graphics.enableScissor(listX, listY, listX + listW, listY + listH);

        int cardGap = 4;
        int dismissBtnSize = 14;
        int curY = (int) (listY - feedbackScrollOffset);

        for (int i = 0; i < this.visitorFeedbacks.size(); i++) {
            var fb = this.visitorFeedbacks.get(i);
            int textW = listW - 12 - dismissBtnSize - 4;
            var wrappedLines = this.font.split(Component.literal(fb.message()), textW);
            int cardH = Math.max(22, 10 + wrappedLines.size() * 10);

            if (curY + cardH >= listY && curY <= listY + listH) {
                // Determine styling based on urgency
                int borderCol = (fb.urgencyLevel() >= 3) ? 0xFF8B0000 : (fb.urgencyLevel() == 2 ? 0xFF996515 : 0xFF2B3342);
                int bgCol = (fb.urgencyLevel() >= 3) ? 0xFF211315 : (fb.urgencyLevel() == 2 ? 0xFF211D13 : 0xFF181C24);

                graphics.fill(listX, curY, listX + listW, curY + cardH, bgCol);
                graphics.renderOutline(listX, curY, listW, cardH, borderCol);

                // Urgency indicator bar on left edge
                int barColor = (fb.urgencyLevel() >= 3) ? 0xFFE06C75 : (fb.urgencyLevel() == 2 ? 0xFFE5C07B : 0xFF61AFEF);
                graphics.fill(listX + 1, curY + 1, listX + 3, curY + cardH - 1, barColor);

                // Text lines
                int ty = curY + 5;
                for (var line : wrappedLines) {
                    graphics.drawString(this.font, line, listX + 8, ty, 0xFFE2E8F0, false);
                    ty += 10;
                }

                // Dismiss 'X' button
                int dismissX = listX + listW - dismissBtnSize - 4;
                int dismissY = curY + (cardH - dismissBtnSize) / 2;
                boolean xHovered = mouseX >= dismissX && mouseX <= dismissX + dismissBtnSize && mouseY >= dismissY && mouseY <= dismissY + dismissBtnSize && mouseY >= listY && mouseY <= listY + listH;

                graphics.fill(dismissX, dismissY, dismissX + dismissBtnSize, dismissY + dismissBtnSize, xHovered ? 0xFF4A2528 : 0xFF1C222D);
                graphics.renderOutline(dismissX, dismissY, dismissBtnSize, dismissBtnSize, xHovered ? 0xFFE06C75 : 0xFF354157);
                graphics.drawCenteredString(this.font, "x", dismissX + dismissBtnSize / 2, dismissY + (dismissBtnSize - 8) / 2, xHovered ? 0xFFFFFFFF : 0xFFA0AAB8);

                if (xHovered) {
                    graphics.renderTooltip(this.font, ZooTranslations.component("zootycoon.dialog.cashier.feedback.dismiss"), mouseX, mouseY);
                }
            }

            curY += cardH + cardGap;
        }

        this.feedbackTotalContentH = (curY - (int)(listY - feedbackScrollOffset));
        graphics.disableScissor();

        // Scrollbar if needed
        int maxScroll = Math.max(0, feedbackTotalContentH - listH);
        if (maxScroll > 0) {
            int barW = 3;
            int barX = listX + listW - barW;
            graphics.fill(barX, listY, barX + barW, listY + listH, 0xFF14171F);
            float ratio = (float) listH / (float) feedbackTotalContentH;
            int thumbH = Math.max(12, (int) (listH * ratio));
            int thumbY = listY + (int) ((listH - thumbH) * (feedbackScrollOffset / maxScroll));
            graphics.fill(barX, thumbY, barX + barW, thumbY + thumbH, 0xFF58A6FF);
        }
    }

    @Override
    protected int getSpeechBubbleY() {
        if ("TICKET".equalsIgnoreCase(currentRole)) {
            return startY + 60;
        }
        return super.getSpeechBubbleY();
    }

    @Override
    protected boolean shouldRenderSpeechBubble() {
        if ("TICKET".equalsIgnoreCase(currentRole)) {
            return activeTicketTab == 0;
        }
        return true;
    }

    @Override
    protected void renderDialogContent(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta) {
        if ("TICKET".equalsIgnoreCase(currentRole)) {
            if (activeTicketTab == 0) {
                int bubbleH = getSpeechBubbleHeight();
                int buttonsY = startY + 60 + bubbleH + 8;
                int labelY = buttonsY + 24;
                String tierLabel = net.zootycoon.economy.ZooEconomyEngine.getTicketPriceTierLabel(this.ticketPrice);
                graphics.drawString(this.font, Component.literal("Kategori: " + tierLabel), contentX, labelY, 0xFFBBBBBB, false);
            } else {
                renderFeedbackTab(graphics, contentX, contentY, contentW, contentH, mouseX, mouseY, delta);
            }
            return;
        }
        if ("TICKET".equalsIgnoreCase(currentRole)) {
            return;
        }

        if (!isStockRole()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Inventory playerInv = mc.player.getInventory();

        // 1. Render 5 Cashier Stock Slots (Row of 5 boxes)
        this.stockStartX = contentX;
        this.stockStartY = contentY + 2;

        ItemStack hoveredTooltipStack = ItemStack.EMPTY;

        for (int i = 0; i < ZooCashierEntity.STOCK_SLOTS; i++) {
            int sx = stockStartX + i * (STOCK_SLOT_SIZE + STOCK_SLOT_GAP);
            int sy = stockStartY;
            boolean hovered = mouseX >= sx && mouseX < sx + STOCK_SLOT_SIZE && mouseY >= sy && mouseY < sy + STOCK_SLOT_SIZE;

            int borderColor = hovered ? 0xFFECC94B : 0xFF4A5568;
            int bgColor = hovered ? 0xFF2D3748 : 0xFF1A202C;
            graphics.fill(sx, sy, sx + STOCK_SLOT_SIZE, sy + STOCK_SLOT_SIZE, borderColor);
            graphics.fill(sx + 1, sy + 1, sx + STOCK_SLOT_SIZE - 1, sy + STOCK_SLOT_SIZE - 1, bgColor);

            ItemStack stack = i < stockItems.size() ? stockItems.get(i) : ItemStack.EMPTY;
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, sx + 2, sy + 2);
                graphics.renderItemDecorations(this.font, stack, sx + 2, sy + 2);
                if (hovered) {
                    hoveredTooltipStack = stack;
                }
            }
        }

        // Price label next to the 5 stock slots depending on items currently in stock
        int priceLabelX = stockStartX + ZooCashierEntity.STOCK_SLOTS * (STOCK_SLOT_SIZE + STOCK_SLOT_GAP) + 6;
        int minPrice = Integer.MAX_VALUE;
        int maxPrice = Integer.MIN_VALUE;
        int validCount = 0;
        for (ItemStack st : this.stockItems) {
            if (st != null && !st.isEmpty() && ZooCashierEntity.isValidItemForRole(this.currentRole, st)) {
                int p = ZooCashierEntity.getItemPrice(this.currentRole, st);
                if (p < minPrice) minPrice = p;
                if (p > maxPrice) maxPrice = p;
                validCount++;
            }
        }

        if (validCount > 0) {
            String priceStr = (minPrice == maxPrice) ? ("Harga: " + minPrice) : ("Harga: " + minPrice + "-" + maxPrice);
            graphics.drawString(this.font, priceStr, priceLabelX, stockStartY + 6, 0xFF55FF55, false);
            int textW = this.font.width(priceStr);
            graphics.pose().pushPose();
            graphics.pose().translate(priceLabelX + textW + 2, stockStartY + 2, 0.0f);
            graphics.pose().scale(0.75f, 0.75f, 1.0f);
            graphics.renderFakeItem(EMERALD_ITEM_ICON, 0, 0);
            graphics.pose().popPose();
        } else {
            graphics.drawString(this.font, "Harga: - (Stok Kosong)", priceLabelX, stockStartY + 6, 0xFFA0AEC0, false);
        }

        // 2. Render Player Inventory Grid (3x9 main + 1x9 hotbar) so player can input items directly
        this.invStartX = contentX;
        this.invStartY = stockStartY + STOCK_SLOT_SIZE + 6;

        // 3 rows of main inventory (slots 9..35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIdx = 9 + row * 9 + col;
                int ix = invStartX + col * (INV_SLOT_SIZE + INV_SLOT_GAP);
                int iy = invStartY + row * (INV_SLOT_SIZE + INV_SLOT_GAP);
                ItemStack hovered = renderPlayerSlot(graphics, playerInv, slotIdx, ix, iy, mouseX, mouseY);
                if (!hovered.isEmpty()) {
                    hoveredTooltipStack = hovered;
                }
            }
        }

        // 1 row of hotbar (slots 0..8)
        int hotbarY = invStartY + 3 * (INV_SLOT_SIZE + INV_SLOT_GAP) + 3;
        for (int col = 0; col < 9; col++) {
            int slotIdx = col;
            int ix = invStartX + col * (INV_SLOT_SIZE + INV_SLOT_GAP);
            ItemStack hovered = renderPlayerSlot(graphics, playerInv, slotIdx, ix, hotbarY, mouseX, mouseY);
            if (!hovered.isEmpty()) {
                hoveredTooltipStack = hovered;
            }
        }

        // Render dragged item under cursor if dragging
        if (isDraggingItem && draggedPlayerSlot >= 0 && draggedPlayerSlot < playerInv.items.size()) {
            ItemStack dragStack = playerInv.getItem(draggedPlayerSlot);
            if (!dragStack.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 250.0F);
                graphics.renderItem(dragStack, mouseX - 8, mouseY - 8);
                graphics.renderItemDecorations(this.font, dragStack, mouseX - 8, mouseY - 8);
                graphics.pose().popPose();
            }
        } else if (!hoveredTooltipStack.isEmpty()) {
            if (ZooCashierEntity.isValidItemForRole(this.currentRole, hoveredTooltipStack)) {
                int unitPrice = ZooCashierEntity.getItemPrice(this.currentRole, hoveredTooltipStack);
                graphics.renderTooltip(
                        this.font,
                        List.of(),
                        Optional.of(new CashierPriceTooltipData(hoveredTooltipStack.getHoverName(), unitPrice)),
                        mouseX,
                        mouseY
                );
            } else {
                graphics.renderTooltip(this.font, hoveredTooltipStack, mouseX, mouseY);
            }
        }
    }

    private ItemStack renderPlayerSlot(GuiGraphics graphics, Inventory playerInv, int slotIdx, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + INV_SLOT_SIZE && mouseY >= y && mouseY < y + INV_SLOT_SIZE;
        ItemStack stack = playerInv.getItem(slotIdx);
        boolean isValid = !stack.isEmpty() && ZooCashierEntity.isValidItemForRole(currentRole, stack);

        int borderColor = hovered ? 0xFFECC94B : (isValid ? 0xFF2F855A : 0xFF2D3748);
        int bgColor = hovered ? 0xFF2D3748 : (isValid ? 0xFF142620 : 0xFF141821);

        graphics.fill(x, y, x + INV_SLOT_SIZE, y + INV_SLOT_SIZE, borderColor);
        graphics.fill(x + 1, y + 1, x + INV_SLOT_SIZE - 1, y + INV_SLOT_SIZE - 1, bgColor);

        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x + 1, y + 1);
            graphics.renderItemDecorations(this.font, stack, x + 1, y + 1);
            if (!isValid) {
                // Slightly dim non-matching items so matching burgers/drinks/plushies stand out clearly
                graphics.fill(x + 1, y + 1, x + INV_SLOT_SIZE - 1, y + INV_SLOT_SIZE - 1, 0x8810141C);
            }
            if (hovered) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private int getCashierSlotAt(double mouseX, double mouseY) {
        for (int i = 0; i < ZooCashierEntity.STOCK_SLOTS; i++) {
            int sx = stockStartX + i * (STOCK_SLOT_SIZE + STOCK_SLOT_GAP);
            int sy = stockStartY;
            if (mouseX >= sx && mouseX < sx + STOCK_SLOT_SIZE && mouseY >= sy && mouseY < sy + STOCK_SLOT_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private int getPlayerSlotAt(double mouseX, double mouseY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int ix = invStartX + col * (INV_SLOT_SIZE + INV_SLOT_GAP);
                int iy = invStartY + row * (INV_SLOT_SIZE + INV_SLOT_GAP);
                if (mouseX >= ix && mouseX < ix + INV_SLOT_SIZE && mouseY >= iy && mouseY < iy + INV_SLOT_SIZE) {
                    return 9 + row * 9 + col;
                }
            }
        }
        int hotbarY = invStartY + 3 * (INV_SLOT_SIZE + INV_SLOT_GAP) + 3;
        for (int col = 0; col < 9; col++) {
            int ix = invStartX + col * (INV_SLOT_SIZE + INV_SLOT_GAP);
            if (mouseX >= ix && mouseX < ix + INV_SLOT_SIZE && mouseY >= hotbarY && mouseY < hotbarY + INV_SLOT_SIZE) {
                return col;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isStockRole() && (button == 0 || button == 1)) {
            int cSlot = getCashierSlotAt(mouseX, mouseY);
            if (cSlot != -1) {
                ItemStack cStack = cSlot < stockItems.size() ? stockItems.get(cSlot) : ItemStack.EMPTY;
                if (!cStack.isEmpty()) {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.CashierSlotClickPayload(
                            this.entityId, 1, -1, cSlot, button == 1
                    ));
                    return true;
                }
            }

            int pSlot = getPlayerSlotAt(mouseX, mouseY);
            if (pSlot != -1) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && !mc.player.getInventory().getItem(pSlot).isEmpty()) {
                    this.draggedPlayerSlot = pSlot;
                    this.dragButton = button;
                    this.isDraggingItem = false;
                    this.dragOriginX = mouseX;
                    this.dragOriginY = mouseY;
                    return true;
                }
            }
        }
        if ("TICKET".equalsIgnoreCase(currentRole) && activeTicketTab == 1) {
            int rightX = startX + 114 + 14;
            int rightW = panelWidth - (rightX - startX) - 12;
            int topY = startY + 60;
            int btnH = 20;
            int totalH = panelHeight - 70 - btnH - 6;
            int listX = rightX + 2;
            int listY = topY + 4;
            int listW = rightW - 4;
            int listH = totalH - 8;

            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                int cardGap = 4;
                int dismissBtnSize = 14;
                int curY = (int) (listY - feedbackScrollOffset);

                for (int i = 0; i < this.visitorFeedbacks.size(); i++) {
                    var fb = this.visitorFeedbacks.get(i);
                    int textW = listW - 12 - dismissBtnSize - 4;
                    var wrappedLines = this.font.split(Component.literal(fb.message()), textW);
                    int cardH = Math.max(22, 10 + wrappedLines.size() * 10);

                    int dismissX = listX + listW - dismissBtnSize - 4;
                    int dismissY = curY + (cardH - dismissBtnSize) / 2;

                    if (mouseX >= dismissX && mouseX <= dismissX + dismissBtnSize && mouseY >= dismissY && mouseY <= dismissY + dismissBtnSize) {
                        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        ClientPlayNetworking.send(new ZooNetworkPayloads.DismissVisitorFeedbackPayload(this.entityId, fb.feedbackId()));
                        this.visitorFeedbacks.remove(i);
                        return true;
                    }
                    curY += cardH + cardGap;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggedPlayerSlot != -1) {
            double distSq = (mouseX - dragOriginX) * (mouseX - dragOriginX) + (mouseY - dragOriginY) * (mouseY - dragOriginY);
            if (distSq > 16.0D) {
                this.isDraggingItem = true;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.draggedPlayerSlot != -1) {
            int sourcePlayerSlot = this.draggedPlayerSlot;
            boolean single = (this.dragButton == 1);
            this.draggedPlayerSlot = -1;
            this.isDraggingItem = false;

            int targetCashierSlot = getCashierSlotAt(mouseX, mouseY);
            if (targetCashierSlot != -1) {
                // Dropped onto a specific cashier stock slot (0..4)
                ClientPlayNetworking.send(new ZooNetworkPayloads.CashierSlotClickPayload(
                        this.entityId, 0, sourcePlayerSlot, targetCashierSlot, single
                ));
                return true;
            }

            int releasePlayerSlot = getPlayerSlotAt(mouseX, mouseY);
            if (releasePlayerSlot == sourcePlayerSlot) {
                // Clicked directly on the player inventory slot: auto-insert into the 5 cashier stock slots
                ClientPlayNetworking.send(new ZooNetworkPayloads.CashierSlotClickPayload(
                        this.entityId, 0, sourcePlayerSlot, -1, single
                ));
                return true;
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
