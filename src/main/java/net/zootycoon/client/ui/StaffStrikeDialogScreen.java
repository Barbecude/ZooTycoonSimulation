package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.economy.ZooEconomyEngine;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.UUID;

public class StaffStrikeDialogScreen extends EasyNpcDialogScreen {

    private static final ItemStack VANILLA_EMERALD_ITEM = new ItemStack(Items.EMERALD);

    private final int entityId;
    private final UUID staffUuid;
    private final int dailyWage;

    public StaffStrikeDialogScreen(int entityId, UUID staffUuid, String staffName, String roleKey) {
        super(
                Component.literal(staffName),
                Component.literal(staffName),
                ZooTranslations.component(roleKey != null && !roleKey.isEmpty() ? roleKey : "zootycoon.dialog.zookeeper.role"),
                ZooTranslations.component("zootycoon.dialog.staff.on_strike"),
                resolveStaffEntity(entityId, staffUuid)
        );
        this.entityId = entityId;
        this.staffUuid = staffUuid;
        this.dailyWage = ZooEconomyEngine.getStaffDailyWage(roleKey);
    }

    private static LivingEntity resolveStaffEntity(int entityId, UUID staffUuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            if (entityId >= 0) {
                Entity ent = mc.level.getEntity(entityId);
                if (ent instanceof LivingEntity living) {
                    return living;
                }
            }
            if (staffUuid != null) {
                for (Entity ent : mc.level.entitiesForRendering()) {
                    if (staffUuid.equals(ent.getUUID()) && ent instanceof LivingEntity living) {
                        return living;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected int getContentHeight() {
        return 46;
    }

    @Override
    protected void initDialogWidgets() {
        int btnY = startY + panelHeight - 30;
        int btnW = (panelWidth - 24 - 8) / 2;

        Component payLabel = this.dailyWage > 0
                ? Component.literal(ZooTranslations.get("zootycoon.dialog.staff.btn.pay_salary") + "  " + this.dailyWage)
                : ZooTranslations.component("zootycoon.dialog.staff.btn.pay_salary");
        ZooButton payBtn = new ZooButton(
                startX + 12, btnY, btnW, 22,
                payLabel,
                b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.PayStrikeSalaryPayload(this.entityId));
                    this.onClose();
                }
        );
        if (this.dailyWage > 0) {
            payBtn.setItemIcon(VANILLA_EMERALD_ITEM).setIconAfter(true).asEmerald();
        } else {
            payBtn.asPrimary();
        }
        this.addRenderableWidget(payBtn);

        var closeBtn = new ZooButton(
                startX + 12 + btnW + 8, btnY, btnW, 22,
                ZooTranslations.component("zootycoon.dialog.staff.btn.close"),
                b -> this.onClose()
        ).asPrimary();
        this.addRenderableWidget(closeBtn);
    }

    @Override
    protected void renderDialogContent(GuiGraphics g, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float partialTick) {
        int cardY = contentY + 2;
        int cardH = 42;

        g.fill(contentX, cardY, contentX + contentW, cardY + cardH, 0xCC1E2229);
        g.fill(contentX, cardY, contentX + contentW, cardY + 1, 0xFFEF5350);
        g.fill(contentX, cardY + cardH - 1, contentX + contentW, cardY + cardH, 0xFFEF5350);
        g.fill(contentX, cardY, contentX + 1, cardY + cardH, 0xFFEF5350);
        g.fill(contentX + contentW - 1, cardY, contentX + contentW, cardY + cardH, 0xFFEF5350);

        g.drawString(
                this.font,
                ZooTranslations.get("zootycoon.dialog.staff.strike_status"),
                contentX + 8,
                cardY + 8,
                0xFFEF5350,
                true
        );

        g.drawString(
                this.font,
                ZooTranslations.get("zootycoon.dialog.staff.strike_hint"),
                contentX + 8,
                cardY + 23,
                0xFFB0BEC5,
                false
        );
    }
}
