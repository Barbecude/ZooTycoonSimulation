package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ZookeeperDialogScreen extends EasyNpcDialogScreen {

    private final UUID keeperUuid;
    private final boolean hasAnimal;
    private final String dialogText;
    private final int hunger;
    private final int foodStock;
    private boolean dropdownOpen = false;
    private List<ZooNetworkPayloads.AnimalSyncData> availableAnimals = List.of();
    private int dropdownX = 0;
    private int dropdownY = 0;
    private int dropdownW = 0;
    private int dropdownH = 0;

    private int animalScrollOffset = 0;

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
        this.dialogText = dialogText;
        this.hunger = hunger;
        this.foodStock = foodStock;
    }

    private static Component buildSpeechComponent(boolean hasAnimal, String animalName, int hunger, int foodStock, String dialogText) {
        if (dialogText != null && !dialogText.isEmpty() && !dialogText.equals("-")) {
            return Component.literal(dialogText);
        }
        if (hasAnimal) {
            ChatFormatting hungerColor = hunger <= 25 ? ChatFormatting.RED : (hunger <= 60 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
            String hungerColoredStr = hungerColor + String.valueOf(hunger) + "%" + ChatFormatting.RESET;
            if (foodStock <= 0) {
                return ZooTranslations.component("zootycoon.dialog.zookeeper.empty_stock", animalName, hungerColoredStr);
            }
            return ZooTranslations.component("zootycoon.dialog.zookeeper.speech_with_animal", animalName, hungerColoredStr, String.valueOf(foodStock));
        }
        return ZooTranslations.component("zootycoon.dialog.zookeeper.speech_no_animal");
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

        if (!hasAnimal) {
            this.availableAnimals = ZooTycoonClient.clientAnimals.stream()
                    .filter(a -> {
                        String id = a.entityTypeId().toLowerCase();
                        return !id.equals("minecraft:bat") && !id.endsWith("_bat") && !id.contains(":bat");
                    })
                    .toList();

            if (availableAnimals.isEmpty()) {
                // If zoo has no registered animals: single button "Tunggu, lagi di cari."
                ZooButton waitBtn = new ZooButton(startX + 12, btnY, panelWidth - 24, 22,
                        ZooTranslations.component("zootycoon.dialog.zookeeper.btn.waiting_search"), b -> {
                    this.onClose();
                }).asPrimary();
                addRenderableWidget(waitBtn);
            } else {
                // Dropdown button to select registered animal + Cancel button
                ZooButton selectBtn = new ZooButton(startX + 12, btnY, btnW, 22,
                        ZooTranslations.component("zootycoon.dialog.zookeeper.btn.select_animal"), b -> {
                    this.dropdownOpen = !this.dropdownOpen;
                    if (this.dropdownOpen) {
                        this.animalScrollOffset = 0;
                    }
                }).asPrimary();
                addRenderableWidget(selectBtn);

                ZooButton laterBtn = new ZooButton(startX + 12 + btnW + 8, btnY, btnW, 22,
                        ZooTranslations.component("zootycoon.dialog.zookeeper.btn.later"), b -> {
                    this.onClose();
                }).asPrimary();
                addRenderableWidget(laterBtn);

                this.dropdownX = startX + 12;
                this.dropdownW = panelWidth - 24;
                int count = Math.min(availableAnimals.size(), 5);
                this.dropdownH = count * 26 + 4;
                this.dropdownY = btnY - dropdownH - 4;
            }
        } else {
            // When hasAnimal: Exactly 2 buttons according to current need!
            int actionBtnX = startX + 12;
            int closeBtnX = startX + 12 + btnW + 8;

            boolean isCoalCondition = this.dialogText != null && (
                    this.dialogText.contains("Coal") || this.dialogText.contains("kedinginan")
            );
            boolean isIceCondition = this.dialogText != null && (
                    this.dialogText.contains("Ice") || this.dialogText.contains("Icebox") || this.dialogText.contains("kepanasan")
            );
            boolean isBathingCondition = this.dialogText != null && (
                    this.dialogText.contains("dimandikan")
                    || this.dialogText.contains("sikat")
                    || this.dialogText.contains("penyiram")
                    || this.dialogText.contains("alat mandi")
                    || this.dialogText.contains("bath")
            );

            if (isCoalCondition) {
                // Condition: Butuh Coal -> Show [Serahkan Coal] & [Semangat kerjanya!]
                ZooButton giveCoalBtn = new ZooButton(actionBtnX, btnY, btnW, 22,
                        ZooTranslations.component("zootycoon.dialog.zookeeper.btn.give_coal"), b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.SupplyZookeeperFuelPayload(this.keeperUuid, "COAL"));
                    this.onClose();
                }).asPrimary();
                addRenderableWidget(giveCoalBtn);
            } else if (isIceCondition) {
                // Condition: Butuh Ice -> Show [Serahkan Ice] & [Semangat kerjanya!]
                ZooButton giveIceBtn = new ZooButton(actionBtnX, btnY, btnW, 22,
                        ZooTranslations.component("zootycoon.dialog.zookeeper.btn.give_ice"), b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.SupplyZookeeperFuelPayload(this.keeperUuid, "ICE"));
                    this.onClose();
                }).asPrimary();
                addRenderableWidget(giveIceBtn);
            } else if (isBathingCondition) {
                // Condition: Butuh Alat Mandi -> Show [Serahkan Alat Mandi] & [Semangat kerjanya!]
                ZooButton giveToolsBtn = new ZooButton(actionBtnX, btnY, btnW, 22,
                        ZooTranslations.component("zootycoon.dialog.zookeeper.btn.give_bathing_tools"), b -> {
                    ClientPlayNetworking.send(new net.zootycoon.network.ZookeeperBathingPayloads.GiveBathingToolsPayload(this.keeperUuid));
                    this.onClose();
                }).asPrimary();
                giveToolsBtn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.literal(ZooTranslations.get("zootycoon.zookeeper.no_bathing_tools_toast"))
                ));
                addRenderableWidget(giveToolsBtn);
            } else {
                // Condition: Butuh Pakan / Regular -> Show [Serahkan Pakan] & [Semangat kerjanya!]
                ZooButton giveFoodBtn = new ZooButton(actionBtnX, btnY, btnW, 22,
                        ZooTranslations.component("zootycoon.dialog.zookeeper.btn.give_food"), b -> {
                    ClientPlayNetworking.send(new ZooNetworkPayloads.SupplyZookeeperFoodPayload(this.keeperUuid));
                    this.onClose();
                }).asPrimary();
                addRenderableWidget(giveFoodBtn);
            }

            // Close button: Semangat kerjanya!
            ZooButton workBtn = new ZooButton(closeBtnX, btnY, btnW, 22,
                    ZooTranslations.component("zootycoon.dialog.zookeeper.btn.working_well"), b -> {
                this.onClose();
            }).asPrimary();
            addRenderableWidget(workBtn);
        }
    }

    private static final Map<String, LivingEntity> dummyAnimalCache = new HashMap<>();

    private LivingEntity resolveAnimalEntity(ZooNetworkPayloads.AnimalSyncData animal) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            var ent = mc.level.getEntity(animal.entityId());
            if (ent instanceof LivingEntity living) return living;
        }
        if (dummyAnimalCache.containsKey(animal.entityTypeId())) {
            return dummyAnimalCache.get(animal.entityTypeId());
        }
        try {
            var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(animal.entityTypeId()));
            if (type != null && mc.level != null) {
                var dummy = type.create(mc.level);
                if (dummy instanceof LivingEntity living) {
                    dummyAnimalCache.put(animal.entityTypeId(), living);
                    return living;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        if (dropdownOpen && !availableAnimals.isEmpty()) {
            renderAnimalDropdown(graphics, mouseX, mouseY);
        }
    }

    private void renderAnimalDropdown(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);

        // Background box
        graphics.fill(dropdownX, dropdownY, dropdownX + dropdownW, dropdownY + dropdownH, 0xFA1E293B);
        // Border
        graphics.renderOutline(dropdownX, dropdownY, dropdownW, dropdownH, 0xFF475569);

        int itemH = 26;
        int totalContentH = availableAnimals.size() * itemH;
        int maxScroll = Math.max(0, totalContentH - (dropdownH - 4));
        animalScrollOffset = Math.clamp(animalScrollOffset, 0, maxScroll);

        graphics.enableScissor(dropdownX + 1, dropdownY + 2, dropdownX + dropdownW - 1, dropdownY + dropdownH - 2);

        int curY = dropdownY + 2 - animalScrollOffset;
        for (int i = 0; i < availableAnimals.size(); i++) {
            var animal = availableAnimals.get(i);
            if (curY + itemH >= dropdownY && curY <= dropdownY + dropdownH) {
                boolean isAlreadyAssigned = animal.hasZookeeper();
                boolean hovered = mouseX >= dropdownX && mouseX <= dropdownX + (maxScroll > 0 ? dropdownW - 6 : dropdownW)
                        && mouseY >= curY && mouseY <= curY + itemH && mouseY >= dropdownY + 2 && mouseY <= dropdownY + dropdownH - 2;

                if (hovered && !isAlreadyAssigned) {
                    graphics.fill(dropdownX + 2, curY, dropdownX + dropdownW - (maxScroll > 0 ? 8 : 2), curY + itemH, 0xFF334155);
                } else if (isAlreadyAssigned) {
                    graphics.fill(dropdownX + 2, curY, dropdownX + dropdownW - (maxScroll > 0 ? 8 : 2), curY + itemH, 0x4011151D);
                }

                // 3D Model of animal
                LivingEntity ent = resolveAnimalEntity(animal);
                if (ent != null) {
                    float bbH = Math.max(0.4f, ent.getBbHeight());
                    float bbW = Math.max(0.4f, ent.getBbWidth());
                    float maxDim = Math.max(bbH, bbW * 0.8f);
                    int scale = (int) Math.clamp(14.0f / maxDim, 6, 16);
                    InventoryScreen.renderEntityInInventoryFollowsMouse(
                            graphics,
                            dropdownX + 6, curY + 2, dropdownX + 28, curY + 24,
                            scale, 0.0f, (float) mouseX, (float) mouseY, ent
                    );
                }

                String displayName = animal.customName().isEmpty() ? formatSpecies(animal.entityTypeId()) : animal.customName();
                if (isAlreadyAssigned) {
                    // Disabled style: cannot be selected
                    String zkLabel = displayName + " (Sudah ada zookeeper)";
                    graphics.drawString(this.font, Component.literal(zkLabel).withStyle(ChatFormatting.DARK_GRAY),
                            dropdownX + 32, curY + 9, 0xFF888888, false);
                } else {
                    // Available: active style
                    graphics.drawString(this.font, Component.literal(displayName).withStyle(hovered ? ChatFormatting.GOLD : ChatFormatting.WHITE),
                            dropdownX + 32, curY + 9, 0xFFFFFFFF, false);
                }
            }
            curY += itemH;
        }

        graphics.disableScissor();

        // Scrollbar if needed
        if (maxScroll > 0) {
            int barX = dropdownX + dropdownW - 5;
            int barY = dropdownY + 2;
            int barH = dropdownH - 4;
            graphics.fill(barX, barY, barX + 3, barY + barH, 0xFF0F172A);
            float ratio = (float) barH / (float) totalContentH;
            int thumbH = Math.max(12, (int) (barH * ratio));
            int thumbY = barY + (int) ((barH - thumbH) * ((float) animalScrollOffset / (float) maxScroll));
            graphics.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFF38BDF8);
        }

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (dropdownOpen && !availableAnimals.isEmpty()) {
            if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownW && mouseY >= dropdownY && mouseY <= dropdownY + dropdownH) {
                double relativeY = (mouseY - (dropdownY + 2)) + animalScrollOffset;
                int clickedIdx = (int) (relativeY / 26);
                if (clickedIdx >= 0 && clickedIdx < availableAnimals.size()) {
                    var selected = availableAnimals.get(clickedIdx);
                    if (!selected.hasZookeeper()) {
                        ClientPlayNetworking.send(new ZooNetworkPayloads.AssignZookeeperPayload(selected.entityId(), this.keeperUuid));
                        dropdownOpen = false;
                        this.onClose();
                        return true;
                    }
                }
                return true;
            } else {
                dropdownOpen = false;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (dropdownOpen && !availableAnimals.isEmpty()) {
            if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownW && mouseY >= dropdownY && mouseY <= dropdownY + dropdownH) {
                int totalContentH = availableAnimals.size() * 26;
                int maxScroll = Math.max(0, totalContentH - (dropdownH - 4));
                if (maxScroll > 0) {
                    this.animalScrollOffset = Math.clamp(this.animalScrollOffset - (int) (verticalAmount * 26), 0, maxScroll);
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private static String formatSpecies(String entityTypeId) {
        String path = entityTypeId.contains(":") ? entityTypeId.substring(entityTypeId.indexOf(':') + 1) : entityTypeId;
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    protected int getSpeechBubbleHeight() {
        return 118;
    }

    @Override
    protected void renderDialogContent(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta) {
    }
}
