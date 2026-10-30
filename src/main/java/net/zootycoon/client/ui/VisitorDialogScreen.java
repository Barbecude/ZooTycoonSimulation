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
    private final String complaintName;
    private final String feedbackText;

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
            boolean hasTrash,
            String complaintName,
            String feedbackText
    ) {
        super(
                ZooTranslations.component("zootycoon.dialog.visitor.title", variant),
                Component.literal(ZooTranslations.get("zootycoon.dialog.visitor.title", variant) + (isChild ? ZooTranslations.get("zootycoon.dialog.visitor.child_suffix") : "")),
                ZooTranslations.component("zootycoon.dialog.visitor.role"),
                resolveVisitorSpeech(moodName, complaintName, feedbackText),
                resolveVisitorEntity(entityId, visitorUuid, variant, isChild)
        );
        this.entityId = entityId;
        this.moodName = moodName != null ? moodName : "NEUTRAL";
        this.needsToilet = needsToilet;
        this.thirst = thirst;
        this.hunger = hunger;
        this.complaintName = complaintName != null ? complaintName : "NONE";
        this.feedbackText = feedbackText != null ? feedbackText : "";
    }

    private static Component resolveVisitorSpeech(String moodName, String complaintName, String feedbackText) {
        if (feedbackText != null && !feedbackText.isBlank()) {
            return Component.literal(feedbackText);
        }
        if (complaintName != null && !complaintName.equalsIgnoreCase("NONE")) {
            String key = "zootycoon.dialog.visitor.feedback." + complaintName.toLowerCase(java.util.Locale.ROOT);
            if (ZooTranslations.has(key)) {
                return ZooTranslations.component(key);
            }
        }
        return getVisitorDialogue(moodName);
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
        } else if ("SCARED".equalsIgnoreCase(moodName)) {
            return ZooTranslations.component("zootycoon.dialog.visitor.speech.scared");
        } else if ("CRYING".equalsIgnoreCase(moodName)) {
            return ZooTranslations.component("zootycoon.dialog.visitor.speech.crying");
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

        if (!"NONE".equalsIgnoreCase(complaintName)) {
            setupComplaintWidgets(btnY, btnW);
            return;
        }

        Component actionText;
        Component closeText;

        boolean isNeedFacility = "TOILET".equalsIgnoreCase(moodName)
                || "HUNGRY".equalsIgnoreCase(moodName)
                || "THIRSTY".equalsIgnoreCase(moodName);

        if (isNeedFacility) {
            actionText = ZooTranslations.component("zootycoon.dialog.visitor.btn.follow_me");
            closeText = ZooTranslations.component("zootycoon.dialog.visitor.btn.not_available");

            // Button 1: "Ikuti Saya" -> guide visitor to facility
            var followBtn = new ZooButton(startX + 12, btnY, btnW, 22, actionText, b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.GuideVisitorPayload(this.entityId, true));
                this.onClose();
            }).asPrimary();
            addRenderableWidget(followBtn);

            // Button 2: "Belum Ada" -> visitor disappointed
            var notAvailBtn = new ZooButton(startX + 12 + btnW + 8, btnY, btnW, 22, closeText, b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.GuideVisitorPayload(this.entityId, false));
                this.onClose();
            }).asPrimary();
            addRenderableWidget(notAvailBtn);
        } else {
            actionText = ZooTranslations.component("zootycoon.dialog.visitor.btn.greet");
            closeText = ZooTranslations.component("zootycoon.dialog.visitor.btn.bye");

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
    }

    private void setupComplaintWidgets(int btnY, int btnW) {
        int x1 = startX + 12;
        int x2 = startX + 12 + btnW + 8;
        String c = complaintName.toUpperCase(java.util.Locale.ROOT);

        Component btn1Text;
        Component btn2Text;

        switch (c) {
            case "THIRSTY":
            case "HUNGRY":
            case "TOILET":
                btn1Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.follow_me");
                btn2Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.not_available");
                addRenderableWidget(new ZooButton(x1, btnY, btnW, 22, btn1Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.GuideVisitorPayload(this.entityId, true));
                    this.onClose();
                }).asPrimary());
                addRenderableWidget(new ZooButton(x2, btnY, btnW, 22, btn2Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.GuideVisitorPayload(this.entityId, false));
                    this.onClose();
                }).asPrimary());
                break;

            case "NO_SEAT":
                btn1Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.guide_seat");
                btn2Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.apologize");
                addRenderableWidget(new ZooButton(x1, btnY, btnW, 22, btn1Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.GuideVisitorPayload(this.entityId, true));
                    this.onClose();
                }).asPrimary());
                addRenderableWidget(new ZooButton(x2, btnY, btnW, 22, btn2Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.ResolveVisitorComplaintPayload(this.entityId, "APOLOGIZE"));
                    this.onClose();
                }).asPrimary());
                break;

            case "OUT_OF_STOCK":
                btn1Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.restock");
                btn2Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.bye");
                addRenderableWidget(new ZooButton(x1, btnY, btnW, 22, btn1Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.ResolveVisitorComplaintPayload(this.entityId, "RESTOCK"));
                    this.onClose();
                }).asPrimary());
                addRenderableWidget(new ZooButton(x2, btnY, btnW, 22, btn2Text, b -> this.onClose()).asPrimary());
                break;

            case "LITTER":
                btn1Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.clean_trash");
                btn2Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.bye");
                addRenderableWidget(new ZooButton(x1, btnY, btnW, 22, btn1Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.ResolveVisitorComplaintPayload(this.entityId, "CLEAN"));
                    this.onClose();
                }).asPrimary());
                addRenderableWidget(new ZooButton(x2, btnY, btnW, 22, btn2Text, b -> this.onClose()).asPrimary());
                break;

            case "SICK_ANIMALS":
                btn1Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.call_zookeeper");
                btn2Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.bye");
                addRenderableWidget(new ZooButton(x1, btnY, btnW, 22, btn1Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.ResolveVisitorComplaintPayload(this.entityId, "CARE"));
                    this.onClose();
                }).asPrimary());
                addRenderableWidget(new ZooButton(x2, btnY, btnW, 22, btn2Text, b -> this.onClose()).asPrimary());
                break;

            case "TICKET_EXPENSIVE":
                btn1Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.review_price");
                btn2Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.bye");
                addRenderableWidget(new ZooButton(x1, btnY, btnW, 22, btn1Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.ResolveVisitorComplaintPayload(this.entityId, "PRICE"));
                    this.onClose();
                }).asPrimary());
                addRenderableWidget(new ZooButton(x2, btnY, btnW, 22, btn2Text, b -> this.onClose()).asPrimary());
                break;

            case "FEW_ANIMALS":
                btn1Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.more_animals");
                btn2Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.bye");
                addRenderableWidget(new ZooButton(x1, btnY, btnW, 22, btn1Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.ResolveVisitorComplaintPayload(this.entityId, "MORE_ANIMALS"));
                    this.onClose();
                }).asPrimary());
                addRenderableWidget(new ZooButton(x2, btnY, btnW, 22, btn2Text, b -> this.onClose()).asPrimary());
                break;

            case "LONG_QUEUE":
            default:
                btn1Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.apologize");
                btn2Text = ZooTranslations.component("zootycoon.dialog.visitor.btn.bye");
                addRenderableWidget(new ZooButton(x1, btnY, btnW, 22, btn1Text, b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.ResolveVisitorComplaintPayload(this.entityId, "APOLOGIZE"));
                    this.onClose();
                }).asPrimary());
                addRenderableWidget(new ZooButton(x2, btnY, btnW, 22, btn2Text, b -> this.onClose()).asPrimary());
                break;
        }
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
