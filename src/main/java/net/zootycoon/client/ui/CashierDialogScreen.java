package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.entity.ZooCashierEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

public class CashierDialogScreen extends EasyNpcDialogScreen {

    private final int entityId;
    private final String currentRole;

    public CashierDialogScreen(int entityId, String currentRole, String cashierName) {
        super(
                Component.literal(cashierName.isEmpty() ? ZooTranslations.get("zootycoon.dialog.cashier.default_name") : cashierName),
                Component.literal(cashierName.isEmpty() ? ZooTranslations.get("zootycoon.dialog.cashier.title") : cashierName),
                ZooTranslations.component("zootycoon.dialog.cashier.role"),
                ZooTranslations.component("zootycoon.dialog.cashier.speech"),
                resolveCashierEntity(entityId)
        );
        this.entityId = entityId;
        this.currentRole = currentRole;
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

    @Override
    protected void initDialogWidgets() {
        int rightX = startX + 114 + 14;
        int rightW = panelWidth - (rightX - startX) - 14;
        int colW = (rightW - 6) / 2;
        int btnH = 22;
        int gap = 4;
        int startBtnY = startY + 86;

        boolean isBurger = "BURGER".equalsIgnoreCase(currentRole);
        boolean isDrink = "DRINK".equalsIgnoreCase(currentRole) || "ICE_CREAM".equalsIgnoreCase(currentRole);
        boolean isSouvenir = "SOUVENIR".equalsIgnoreCase(currentRole);
        boolean isTicket = "TICKET".equalsIgnoreCase(currentRole);

        // Row 1, Col 1: Stand Burger
        Component burgerText = isBurger ?
                ZooTranslations.component("zootycoon.dialog.cashier.btn.burger_active") :
                ZooTranslations.component("zootycoon.dialog.cashier.btn.burger");
        var burgerBtn = new ZooButton(rightX, startBtnY, colW, btnH, burgerText, b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "BURGER"));
            this.onClose();
        }).asPrimary();
        if (isBurger) burgerBtn.active = false;
        addRenderableWidget(burgerBtn);

        // Row 1, Col 2: Stand Minuman
        Component drinkText = isDrink ?
                ZooTranslations.component("zootycoon.dialog.cashier.btn.drink_active") :
                ZooTranslations.component("zootycoon.dialog.cashier.btn.drink");
        var drinkBtn = new ZooButton(rightX + colW + 6, startBtnY, colW, btnH, drinkText, b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "DRINK"));
            this.onClose();
        }).asPrimary();
        if (isDrink) drinkBtn.active = false;
        addRenderableWidget(drinkBtn);

        // Row 2, Col 1: Toko Souvenir
        Component souvText = isSouvenir ?
                ZooTranslations.component("zootycoon.dialog.cashier.btn.souvenir_active") :
                ZooTranslations.component("zootycoon.dialog.cashier.btn.souvenir");
        var souvenirBtn = new ZooButton(rightX, startBtnY + btnH + gap, colW, btnH, souvText, b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "SOUVENIR"));
            this.onClose();
        }).asPrimary();
        if (isSouvenir) souvenirBtn.active = false;
        addRenderableWidget(souvenirBtn);

        // Row 2, Col 2: Loket Tiket
        Component ticketText = isTicket ?
                ZooTranslations.component("zootycoon.dialog.cashier.btn.ticket_active") :
                ZooTranslations.component("zootycoon.dialog.cashier.btn.ticket");
        var ticketBtn = new ZooButton(rightX + colW + 6, startBtnY + btnH + gap, colW, btnH, ticketText, b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "TICKET"));
            this.onClose();
        }).asPrimary();
        if (isTicket) ticketBtn.active = false;
        addRenderableWidget(ticketBtn);

        // Row 3 (Full width): Nanti dulu yaa (Batal / Tutup)
        var closeBtn = new ZooButton(rightX, startBtnY + (btnH + gap) * 2, rightW, btnH,
                ZooTranslations.component("zootycoon.dialog.cashier.btn.later"), b -> {
            this.onClose();
        }).asPrimary();
        addRenderableWidget(closeBtn);
    }

    @Override
    protected int getContentHeight() {
        return 48;
    }

    @Override
    protected int getSpeechBubbleHeight() {
        return 44;
    }

    @Override
    protected void renderDialogContent(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta) {
    }
}

