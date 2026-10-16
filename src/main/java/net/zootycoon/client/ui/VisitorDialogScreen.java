package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooVisitorEntity;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.UUID;

public class VisitorDialogScreen extends EasyNpcDialogScreen {

    private final int entityId;
    private final String moodName;
    private final boolean needsToilet;
    private final int thirst;
    private final int hunger;

    public VisitorDialogScreen(
            int entityId,
            UUID visitorUuid,
            int variant,
            boolean isChild,
            String moodName,
            String moodLabel,
            int hunger,
            int thirst,
            boolean needsToilet,
            boolean hasTrash
    ) {
        super(
                ZooTranslations.component("zootycoon.dialog.visitor.title", variant),
                Component.literal(ZooTranslations.get("zootycoon.dialog.visitor.title", variant) + (isChild ? ZooTranslations.get("zootycoon.dialog.visitor.child_suffix") : "")),
                ZooTranslations.component("zootycoon.dialog.visitor.role"),
                getVisitorDialogue(moodName),
                resolveVisitorEntity(entityId, visitorUuid, variant, isChild)
        );
        this.entityId = entityId;
        this.moodName = moodName != null ? moodName : "NEUTRAL";
        this.needsToilet = needsToilet;
        this.thirst = thirst;
        this.hunger = hunger;
    }

    private static Component getVisitorDialogue(String moodName) {
        if ("TOILET".equalsIgnoreCase(moodName)) {
            return ZooTranslations.component("zootycoon.dialog.visitor.speech.toilet");
        } else if ("HUNGRY".equalsIgnoreCase(moodName)) {
            return ZooTranslations.component("zootycoon.dialog.visitor.speech.hungry");
        } else if ("THIRSTY".equalsIgnoreCase(moodName)) {
            return ZooTranslations.component("zootycoon.dialog.visitor.speech.thirsty");
        } else if ("AMAZED".equalsIgnoreCase(moodName)) {
            return ZooTranslations.component("zootycoon.dialog.visitor.speech.amazed");
        } else if ("ADORED".equalsIgnoreCase(moodName)) {
            return ZooTranslations.component("zootycoon.dialog.visitor.speech.adored");
        } else {
            return ZooTranslations.component("zootycoon.dialog.visitor.speech.neutral");
        }
    }

    private static LivingEntity resolveVisitorEntity(int entityId, UUID uuid, int variant, boolean isChild) {
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
            ZooVisitorEntity dummy = ZooEntities.VISITOR.create(mc.level);
            if (dummy != null) {
                dummy.setVariant(variant);
                dummy.setBaby(isChild);
                return dummy;
            }
        }
        return null;
    }

    @Override
    protected void initDialogWidgets() {
        int btnY = startY + panelHeight - 30;
        int btnW = (panelWidth - 24 - 8) / 2;

        Component actionText;
        Component closeText;

        if ("TOILET".equalsIgnoreCase(moodName)) {
            actionText = ZooTranslations.component("zootycoon.dialog.visitor.btn.toilet");
            closeText = ZooTranslations.component("zootycoon.dialog.visitor.btn.toilet_close");
        } else if ("HUNGRY".equalsIgnoreCase(moodName)) {
            actionText = ZooTranslations.component("zootycoon.dialog.visitor.btn.hungry");
            closeText = ZooTranslations.component("zootycoon.dialog.visitor.btn.hungry_close");
        } else if ("THIRSTY".equalsIgnoreCase(moodName)) {
            actionText = ZooTranslations.component("zootycoon.dialog.visitor.btn.thirsty");
            closeText = ZooTranslations.component("zootycoon.dialog.visitor.btn.thirsty_close");
        } else {
            actionText = ZooTranslations.component("zootycoon.dialog.visitor.btn.greet");
            closeText = ZooTranslations.component("zootycoon.dialog.visitor.btn.bye");
        }

        // Button 1: Contextual greeting or advice
        var greetBtn = new ZooButton(startX + 12, btnY, btnW, 22, actionText, b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.GreetVisitorPayload(this.entityId));
            this.onClose();
        }).asPrimary();
        addRenderableWidget(greetBtn);

        // Button 2: Friendly close
        var closeBtn = new ZooButton(startX + 12 + btnW + 8, btnY, btnW, 22, closeText, b -> {
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
