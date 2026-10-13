package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.List;
import java.util.UUID;

public class ZookeeperDialogScreen extends EasyNpcDialogScreen {

    private final UUID keeperUuid;
    private final boolean hasAnimal;

    public ZookeeperDialogScreen(
            UUID keeperUuid,
            String keeperName,
            boolean hasAnimal,
            String animalName,
            int hunger,
            int foodStock,
            String dialogText,
            List<String> acceptedFoods
    ) {
        super(
                Component.literal(keeperName != null && !keeperName.isEmpty() ? keeperName : ZooTranslations.get("zootycoon.dialog.zookeeper.default_name")),
                Component.literal(keeperName != null && !keeperName.isEmpty() ? keeperName : ZooTranslations.get("zootycoon.dialog.zookeeper.default_name")),
                ZooTranslations.component("zootycoon.dialog.zookeeper.role"),
                buildSpeechComponent(hasAnimal, animalName, hunger, foodStock, dialogText),
                resolveKeeperEntity(keeperUuid)
        );
        this.keeperUuid = keeperUuid;
        this.hasAnimal = hasAnimal;
    }

    private static Component buildSpeechComponent(boolean hasAnimal, String animalName, int hunger, int foodStock, String dialogText) {
        if (hasAnimal) {
            ChatFormatting hungerColor = hunger <= 25 ? ChatFormatting.RED : (hunger <= 60 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
            String hungerColoredStr = hungerColor + String.valueOf(hunger) + "%" + ChatFormatting.RESET;
            return ZooTranslations.component("zootycoon.dialog.zookeeper.speech_with_animal", animalName, hungerColoredStr, foodStock);
        }
        return ZooTranslations.component("zootycoon.dialog.zookeeper.speech_no_animal", foodStock);
    }

    private static LivingEntity resolveKeeperEntity(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            if (uuid != null) {
                for (var ent : mc.level.entitiesForRendering()) {
                    if (ent.getUUID().equals(uuid) && ent instanceof LivingEntity living) {
                        return living;
                    }
                }
            }
            return ZooEntities.ZOOKEEPER.create(mc.level);
        }
        return null;
    }

    @Override
    protected void initDialogWidgets() {
        int btnY = startY + panelHeight - 30;
        int btnW = (panelWidth - 24 - 8) / 2;

        // Button 1: Serahkan Pakan (No generic left icon)
        ZooButton giveFoodBtn = new ZooButton(startX + 12, btnY, btnW, 22, ZooTranslations.component("zootycoon.dialog.zookeeper.btn.give_food"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.SupplyZookeeperFoodPayload(this.keeperUuid));
            this.onClose();
        }).asPrimary();
        addRenderableWidget(giveFoodBtn);

        // Button 2: Nanti dulu ya / Okey
        String closeKey = hasAnimal ? "zootycoon.dialog.builder.btn.later" : "zootycoon.dialog.btn.okay";
        ZooButton closeBtn = new ZooButton(startX + 12 + btnW + 8, btnY, btnW, 22, ZooTranslations.component(closeKey), b -> {
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
