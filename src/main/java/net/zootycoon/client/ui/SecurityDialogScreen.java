package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.UUID;

public class SecurityDialogScreen extends EasyNpcDialogScreen {

    private final int entityId;

    public SecurityDialogScreen(
            int entityId,
            UUID securityUuid,
            String securityName,
            boolean isAlert,
            String targetName,
            int health,
            int maxHealth
    ) {
        super(
                Component.literal(securityName != null && !securityName.isEmpty() ? securityName : ZooTranslations.get("zootycoon.dialog.security.default_name")),
                Component.literal(securityName != null && !securityName.isEmpty() ? securityName : ZooTranslations.get("zootycoon.dialog.security.default_name")),
                ZooTranslations.component("zootycoon.dialog.security.role"),
                ZooTranslations.component(isAlert ? "zootycoon.dialog.security.speech_alert" : "zootycoon.dialog.security.speech_safe"),
                resolveSecurityEntity(entityId, securityUuid)
        );
        this.entityId = entityId;
    }

    private static LivingEntity resolveSecurityEntity(int entityId, UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            if (entityId != -1) {
                var ent = mc.level.getEntity(entityId);
                if (ent instanceof LivingEntity living) return living;
            }
            if (uuid != null) {
                for (var ent : mc.level.entitiesForRendering()) {
                    if (ent.getUUID().equals(uuid) && ent instanceof LivingEntity living) {
                        return living;
                    }
                }
            }
            return ZooEntities.SECURITY.create(mc.level);
        }
        return null;
    }

    @Override
    protected void initDialogWidgets() {
        int btnY = startY + panelHeight - 30;
        int btnW = (panelWidth - 24 - 8) / 2;

        // Button 1: Patroli Area (No generic icon)
        var patrolBtn = new ZooButton(startX + 12, btnY, btnW, 22, ZooTranslations.component("zootycoon.dialog.security.btn.patrol"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.InteractSecurityPayload(this.entityId));
            this.onClose();
        }).asPrimary();
        addRenderableWidget(patrolBtn);

        // Button 2: Okey
        var closeBtn = new ZooButton(startX + 12 + btnW + 8, btnY, btnW, 22, ZooTranslations.component("zootycoon.dialog.btn.okay"), b -> {
            this.onClose();
        }).asPrimary();
        addRenderableWidget(closeBtn);
    }

    @Override
    protected int getSpeechBubbleHeight() {
        return 118;
    }

    @Override
    protected void renderDialogContent(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta) {
        // Technical info below dialog removed as requested
    }
}
