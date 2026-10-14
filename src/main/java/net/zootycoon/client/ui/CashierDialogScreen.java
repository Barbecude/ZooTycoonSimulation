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
                Component.literal(cashierName.isEmpty() ? "Kasir Zoo" : cashierName),
                Component.literal(cashierName.isEmpty() ? "Kasir Kebun Binatang" : cashierName),
                Component.literal("Pegawai Layanan Pengunjung"),
                Component.literal("Halo Bos! Senang bisa bergabung di kebun binatang ini. Mau tugaskan saya jaga stand kasir yang mana nih?"),
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
        int btnH = 22;
        int gap = 4;
        int startBtnY = startY + 95;

        // Button 1: Kasir Stand Es Krim
        var iceCreamBtn = new ZooButton(rightX, startBtnY, rightW, btnH,
                Component.literal("🍦 Kasir Stand Es Krim"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "ICE_CREAM"));
            this.onClose();
        }).asPrimary();
        addRenderableWidget(iceCreamBtn);

        // Button 2: Kasir Toko Souvenir
        var souvenirBtn = new ZooButton(rightX, startBtnY + btnH + gap, rightW, btnH,
                Component.literal("🧸 Kasir Toko Souvenir"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "SOUVENIR"));
            this.onClose();
        }).asPrimary();
        addRenderableWidget(souvenirBtn);

        // Button 3: Kasir Loket Tiket
        var ticketBtn = new ZooButton(rightX, startBtnY + (btnH + gap) * 2, rightW, btnH,
                Component.literal("🎟️ Kasir Loket Tiket"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.SetCashierRolePayload(this.entityId, "TICKET"));
            this.onClose();
        }).asPrimary();
        addRenderableWidget(ticketBtn);
    }

    @Override
    protected int getSpeechBubbleHeight() {
        return 50;
    }

    @Override
    protected void renderDialogContent(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta) {
    }
}

