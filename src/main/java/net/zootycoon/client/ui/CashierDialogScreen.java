package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

public class CashierDialogScreen extends EasyNpcDialogScreen {

    private final int entityId;
    private final String currentRole;
    private final List<ItemStack> stockItems = new ArrayList<>(ZooCashierEntity.STOCK_SLOTS);
    private boolean isZooOpen;
    private ZooButton toggleZooBtn;

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

    public CashierDialogScreen(int entityId, String currentRole, String cashierName, List<ItemStack> initialStock, boolean isZooOpen) {
        super(
                Component.literal(cashierName.isEmpty() ? ZooTranslations.get("zootycoon.dialog.cashier.default_name") : cashierName),
                Component.literal(cashierName.isEmpty() ? ZooTranslations.get("zootycoon.dialog.cashier.title") : cashierName),
                ZooTranslations.component("zootycoon.dialog.cashier.role"),
                buildSpeechComponent(currentRole, initialStock, isZooOpen),
                resolveCashierEntity(entityId)
        );
        this.entityId = entityId;
        this.currentRole = currentRole != null ? currentRole : "UNASSIGNED";
        this.isZooOpen = isZooOpen;
        updateStockItems(initialStock);
    }

    public CashierDialogScreen(int entityId, String currentRole, String cashierName, List<ItemStack> initialStock) {
        this(entityId, currentRole, cashierName, initialStock, false);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getCurrentRole() {
        return this.currentRole;
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
        this.dialogMessage = buildSpeechComponent(this.currentRole, this.stockItems, this.isZooOpen);
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
        this.dialogMessage = buildSpeechComponent(this.currentRole, this.stockItems, this.isZooOpen);
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

    private static Component buildSpeechComponent(String role, List<ItemStack> stock, boolean isZooOpen) {
        if (role == null || "UNASSIGNED".equalsIgnoreCase(role)) {
            return ZooTranslations.component("zootycoon.dialog.cashier.speech");
        }
        if ("TICKET".equalsIgnoreCase(role)) {
            return isZooOpen
                    ? ZooTranslations.component("zootycoon.dialog.cashier.speech.ticket_open")
                    : ZooTranslations.component("zootycoon.dialog.cashier.speech.ticket_closed");
        }
        boolean stocked = hasAnyStock(stock);
        if ("BURGER".equalsIgnoreCase(role)) {
            return stocked
                    ? ZooTranslations.component("zootycoon.dialog.cashier.speech.burger_ready")
                    : ZooTranslations.component("zootycoon.dialog.cashier.speech.burger_empty");
        }
        if ("DRINK".equalsIgnoreCase(role) || "ICE_CREAM".equalsIgnoreCase(role)) {
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

            var ticketBtn = new ZooButton(rightX, startBtnY + btnH + gap, rightW, btnH,
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
            // Role is TICKET: toggle open/close zoo button replaces "Selesai" button
            int btnH = 20;
            int closeY = startY + panelHeight - btnH - 8;
            this.toggleZooBtn = new ZooButton(rightX, closeY, rightW, btnH,
                    getToggleZooComponent(), b -> {
                this.isZooOpen = !this.isZooOpen;
                this.toggleZooBtn.setMessage(getToggleZooComponent());
                if (this.isZooOpen) {
                    this.toggleZooBtn.asDanger();
                } else {
                    this.toggleZooBtn.asPrimary();
                }
                this.dialogMessage = buildSpeechComponent(this.currentRole, this.stockItems, this.isZooOpen);
                this.dialogStartTime = System.currentTimeMillis();
                ClientPlayNetworking.send(new ZooNetworkPayloads.ToggleZooOpenPayload(this.entityId));
            });
            if (this.isZooOpen) {
                this.toggleZooBtn.asDanger();
            } else {
                this.toggleZooBtn.asPrimary();
            }
            addRenderableWidget(this.toggleZooBtn);
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

    @Override
    protected int getSpeechBubbleHeight() {
        if (isStockRole()) {
            return 32;
        }
        return 44;
    }

    @Override
    protected int getContentHeight() {
        if (isStockRole()) {
            return 120;
        }
        return 48;
    }

    @Override
    protected void renderDialogContent(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta) {
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

        // Price label next to the 5 stock slots
        int priceLabelX = stockStartX + ZooCashierEntity.STOCK_SLOTS * (STOCK_SLOT_SIZE + STOCK_SLOT_GAP) + 4;
        int basePrice = ZooCashierEntity.getItemPrice(currentRole, ItemStack.EMPTY);
        String priceStr = ZooTranslations.get("zootycoon.dialog.cashier.price_label", basePrice);
        graphics.drawString(this.font, priceStr, priceLabelX, stockStartY + 6, 0xFFA0AEC0, false);

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
            graphics.renderTooltip(this.font, hoveredTooltipStack, mouseX, mouseY);
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
