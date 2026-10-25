package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.animal.habitat.HabitatEvaluator;
import net.zootycoon.animal.habitat.HabitatProfile;
import net.zootycoon.animal.habitat.HabitatProfileLoader;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.client.inspect.HologramBarRenderer;
import net.zootycoon.client.render.ZooAnimalHologramRenderer;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalManagementTab {

    private static final ResourceLocation EXCLAMATION_ICON = ZooTycoonMod.id("textures/gui/icons/exclamation_point.png");
    private static final ResourceLocation TRASH_ICON = ZooTycoonMod.id("textures/gui/icons/trash_can.png");
    private static final ResourceLocation PENCIL_ICON = ZooTycoonMod.id("textures/gui/icons/pencil.png");

    private static final ResourceLocation THIRST_ICON = ZooTycoonMod.id("textures/gui/inspect/thirst.png");
    private static final ResourceLocation CLEANLINESS_ICON = ZooTycoonMod.id("textures/gui/inspect/cleanliness.png");
    private static final ResourceLocation THERMOMETER_ICON = ZooTycoonMod.id("textures/gui/inspect/thermometer.png");
    private static final ResourceLocation TEMP_HOT_ICON = ZooTycoonMod.id("textures/gui/icons/temp_fire.png");
    private static final ResourceLocation TEMP_COLD_ICON = ZooTycoonMod.id("textures/gui/icons/temp_ice.png");

    private static final ResourceLocation VANILLA_HEART_CONTAINER = ResourceLocation.withDefaultNamespace("hud/heart/container");
    private static final ResourceLocation VANILLA_HEART_FULL = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation VANILLA_HEART_HALF = ResourceLocation.withDefaultNamespace("hud/heart/half");

    private static final ResourceLocation VANILLA_FOOD_EMPTY = ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation VANILLA_FOOD_FULL = ResourceLocation.withDefaultNamespace("hud/food_full");
    private static final ResourceLocation VANILLA_FOOD_HALF = ResourceLocation.withDefaultNamespace("hud/food_half");

    private final MainManagementScreen screen;
    private ZooNetworkPayloads.AnimalSyncData selectedData = null;
    private int lastSelectedEntityId = -1;
    private final Map<String, LivingEntity> clientDummyCache = new HashMap<>();

    private EditBox nameInput;
    private ZooButton renameButton;
    private ZooButton removeButton;
    private ZooButton refreshButton;

    private boolean zookeeperDropdownOpen = false;
    private int zkDropdownX = 0;
    private int zkDropdownY = 0;
    private int zkDropdownW = 170;
    private int zkDropdownH = 0;
    private int zkScrollOffset = 0;
    private boolean isDraggingSidebarScrollbar = false;

    private int scrollOffset = 0;
    private int detailsScrollOffset = 0;
    private boolean isDraggingDetailsScrollbar = false;
    private int maxDetailsScroll = 0;
    private static final int ITEM_HEIGHT = 38;

    public AnimalManagementTab(MainManagementScreen screen) {
        this.screen = screen;
    }

    public void init() {
        clientDummyCache.clear();
        requestSync();

        int width = screen.width;

        int sidebarX = 14;
        int sidebarW = 160;
        int contentX = sidebarX + sidebarW + 16;
        int contentY = 48;
        int contentW = width - contentX - 14;

        refreshButton = new ZooButton(sidebarX, 48, sidebarW, 20, ZooTranslations.component("zootycoon.ui.animals.btn_refresh"), b -> {
            requestSync();
        });
        screen.addWidgetPublic(refreshButton);

        int previewX = contentX + 14;
        int previewW = 140;
        int infoX = previewX + previewW + 16;
        int infoW = contentW - (infoX - contentX) - 14;

        int inputY = contentY + 58;
        nameInput = new EditBox(screen.getFont(), infoX, inputY, infoW, 18, ZooTranslations.component("zootycoon.ui.animals.input_hint"));
        nameInput.setMaxLength(32);
        screen.addWidgetPublic(nameInput);

        int btnY = inputY + 22;
        int halfW = (infoW - 6) / 2;

        renameButton = new ZooButton(infoX, btnY, halfW, 18, ZooTranslations.component("zootycoon.ui.animals.btn_save"), b -> {
            if (selectedData != null && nameInput != null && !nameInput.getValue().trim().isEmpty()) {
                String newName = nameInput.getValue().trim();
                ClientPlayNetworking.send(new ZooNetworkPayloads.RenameAnimalPayload(selectedData.entityId(), newName));
                selectedData = new ZooNetworkPayloads.AnimalSyncData(
                        selectedData.entityId(), selectedData.entityTypeId(), newName,
                        selectedData.hunger(), selectedData.thirst(), selectedData.cleanliness(), selectedData.health(), selectedData.maxHealth(),
                        selectedData.hasZookeeper(), selectedData.zookeeperName(),
                        selectedData.habitatStatus(), selectedData.habitatNeeds(), selectedData.tempCategory()
                );
            }
        }).setCustomIcon(PENCIL_ICON, 12, 12).asPrimary();
        screen.addWidgetPublic(renameButton);

        removeButton = new ZooButton(infoX + halfW + 6, btnY, halfW, 18, ZooTranslations.component("zootycoon.ui.animals.btn_remove"), b -> {
            if (selectedData != null) {
                int idToRemove = selectedData.entityId();
                ClientPlayNetworking.send(new ZooNetworkPayloads.UnregisterAnimalPayload(idToRemove));
                ZooTycoonClient.clientAnimals.removeIf(a -> a.entityId() == idToRemove);
                selectedData = null;
                updateSelectedData(true);
            }
        }).setCustomIcon(TRASH_ICON, 12, 12).asDanger();
        screen.addWidgetPublic(removeButton);

        updateSelectedData(false);
    }

    public void requestSync() {
        ClientPlayNetworking.send(new ZooNetworkPayloads.RequestAnimalSyncPayload());
    }

    private List<ZooNetworkPayloads.AnimalSyncData> getValidAnimals() {
        return ZooTycoonClient.clientAnimals.stream()
                .filter(a -> {
                    String id = a.entityTypeId().toLowerCase();
                    return !id.equals("minecraft:bat") && !id.endsWith("_bat") && !id.contains(":bat");
                })
                .toList();
    }

    private void updateSelectedData(boolean forceTextReset) {
        List<ZooNetworkPayloads.AnimalSyncData> animals = getValidAnimals();
        if (selectedData == null && !animals.isEmpty()) {
            selectedData = animals.get(0);
            forceTextReset = true;
        } else if (selectedData != null) {
            boolean found = false;
            for (var a : animals) {
                if (a.entityId() == selectedData.entityId()) {
                    selectedData = a;
                    found = true;
                    break;
                }
            }
            if (!found && !animals.isEmpty()) {
                selectedData = animals.get(0);
                forceTextReset = true;
            } else if (!found) {
                selectedData = null;
                forceTextReset = true;
            }
        }

        if (nameInput != null) {
            if (selectedData != null) {
                nameInput.setVisible(true);
                renameButton.visible = true;
                removeButton.visible = true;

                if (forceTextReset || (selectedData.entityId() != lastSelectedEntityId && !nameInput.isFocused())) {
                    nameInput.setValue(selectedData.customName());
                    lastSelectedEntityId = selectedData.entityId();
                    detailsScrollOffset = 0;
                }
            } else {
                nameInput.setValue("");
                nameInput.setVisible(false);
                renameButton.visible = false;
                removeButton.visible = false;
                lastSelectedEntityId = -1;
            }
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateSelectedData(false);

        Minecraft mc = Minecraft.getInstance();
        Font font = screen.getFont();
        int width = screen.width;
        int height = screen.height;

        int sidebarX = 14;
        int sidebarW = 160;
        int sidebarY = 72;
        int sidebarH = height - sidebarY - 14;

        int contentX = sidebarX + sidebarW + 16;
        int contentY = 48;
        int contentW = width - contentX - 14;
        int contentH = height - contentY - 14;

        // 1. Sidebar Background & Borders
        graphics.fill(sidebarX, sidebarY, sidebarX + sidebarW, sidebarY + sidebarH, 0xD012151D);
        graphics.fill(sidebarX, sidebarY, sidebarX + sidebarW, sidebarY + 1, 0xFF2A3140);
        graphics.fill(sidebarX, sidebarY + sidebarH - 1, sidebarX + sidebarW, sidebarY + sidebarH, 0xFF2A3140);
        graphics.fill(sidebarX, sidebarY, sidebarX + 1, sidebarY + sidebarH, 0xFF2A3140);
        graphics.fill(sidebarX + sidebarW - 1, sidebarY, sidebarX + sidebarW, sidebarY + sidebarH, 0xFF2A3140);

        List<ZooNetworkPayloads.AnimalSyncData> animals = getValidAnimals();
        int totalContentH = animals.size() * ITEM_HEIGHT + 8;
        int maxScroll = Math.max(0, totalContentH - sidebarH);
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

        graphics.enableScissor(sidebarX + 1, sidebarY + 1, sidebarX + sidebarW - 1, sidebarY + sidebarH - 1);

        int itemRightMargin = maxScroll > 0 ? 8 : 3;
        int curY = sidebarY + 4 - scrollOffset;
        for (var a : animals) {
            if (curY + ITEM_HEIGHT >= sidebarY && curY <= sidebarY + sidebarH) {
                boolean isSelected = (selectedData != null && selectedData.entityId() == a.entityId());
                int itemBg = isSelected ? 0xFF243048 : 0xFF181C26;
                graphics.fill(sidebarX + 3, curY, sidebarX + sidebarW - itemRightMargin, curY + ITEM_HEIGHT - 2, itemBg);

                String displayName = a.customName().isEmpty() ? formatSpecies(a.entityTypeId()) : a.customName();
                graphics.drawString(font, Component.literal(displayName).withStyle(isSelected ? ChatFormatting.GOLD : ChatFormatting.WHITE), sidebarX + 8, curY + 6, 0xFFFFFFFF, false);

                graphics.blitSprite(VANILLA_HEART_FULL, sidebarX + 8, curY + 20, 9, 9);
                int hp = (int) Math.ceil(a.health());
                int maxHp = (int) Math.ceil(a.maxHealth());
                graphics.drawString(font, Component.literal(hp + "/" + maxHp), sidebarX + 20, curY + 21, 0xFFE06C75, false);

                // Colored hunger percentage (Red when <= 25%, Yellow when <= 60%, Green when > 60%)
                graphics.blitSprite(VANILLA_FOOD_FULL, sidebarX + 68, curY + 20, 9, 9);
                int tagColor = a.hunger() > 60 ? 0xFF2ECC71 : (a.hunger() > 25 ? 0xFFF1C40F : 0xFFE74C3C);
                graphics.drawString(font, Component.literal(a.hunger() + "%"), sidebarX + 80, curY + 21, tagColor, false);

                int cardRight = sidebarX + sidebarW - itemRightMargin;
                int badgeX = cardRight - 13;
                int badgeY = curY + 5;

                if (!a.hasZookeeper()) {
                    graphics.blit(EXCLAMATION_ICON, badgeX, badgeY, 0, 0, 10, 10, 64, 64);
                    if (mouseX >= badgeX && mouseX <= badgeX + 10 && mouseY >= badgeY && mouseY <= badgeY + 10) {
                        graphics.renderTooltip(font, ZooTranslations.component("zootycoon.ui.animals.no_zookeeper_tooltip").withStyle(ChatFormatting.RED), mouseX, mouseY);
                    }
                    badgeX -= 13;
                }

                if (a.hunger() <= 50) {
                    graphics.blitSprite(VANILLA_FOOD_FULL, badgeX, badgeY + 3, 9, 9);
                    if (mouseX >= badgeX && mouseX <= badgeX + 9 && mouseY >= badgeY + 3 && mouseY <= badgeY + 12) {
                        graphics.renderTooltip(font, ZooTranslations.component("zootycoon.ui.animals.hungry_tooltip", a.hunger()).withStyle(ChatFormatting.GOLD), mouseX, mouseY);
                    }
                }
            }
            curY += ITEM_HEIGHT;
        }

        graphics.disableScissor();

        if (maxScroll > 0) {
            int barX = sidebarX + sidebarW - 5;
            int barW = 3;
            graphics.fill(barX, sidebarY + 2, barX + barW, sidebarY + sidebarH - 2, 0xFF1C2230);
            float ratio = (float) sidebarH / (float) totalContentH;
            int thumbH = Math.max(16, (int) (sidebarH * ratio));
            int thumbY = sidebarY + 2 + (int) ((sidebarH - 4 - thumbH) * ((float) scrollOffset / (float) maxScroll));
            graphics.fill(barX, thumbY, barX + barW, thumbY + thumbH, 0xFF58A6FF);
        }

        // 2. Content Panel (Right Side) Background & Borders
        graphics.fill(contentX, contentY, contentX + contentW, contentY + contentH, 0xD012151D);
        graphics.fill(contentX, contentY, contentX + contentW, contentY + 1, 0xFF2A3140);
        graphics.fill(contentX, contentY + contentH - 1, contentX + contentW, contentY + contentH, 0xFF2A3140);
        graphics.fill(contentX, contentY, contentX + 1, contentY + contentH, 0xFF2A3140);
        graphics.fill(contentX + contentW - 1, contentY, contentX + contentW, contentY + contentH, 0xFF2A3140);

        if (selectedData == null) {
            // Normal font (No BOLD)
            graphics.drawCenteredString(font, ZooTranslations.component("zootycoon.ui.animals.empty1").withStyle(ChatFormatting.GOLD), contentX + contentW / 2, contentY + 50, 0xFFFFFFFF);
            graphics.drawCenteredString(font, ZooTranslations.component("zootycoon.ui.animals.empty2"), contentX + contentW / 2, contentY + 70, 0xFFAAAAAA);
            graphics.drawCenteredString(font, ZooTranslations.component("zootycoon.ui.animals.empty3"), contentX + contentW / 2, contentY + 84, 0xFFAAAAAA);
            return;
        }

        LivingEntity previewEntity = resolveLivingEntity(mc, selectedData);

        int previewW = 140;
        int previewH = 140;
        int previewX = contentX + 14;
        int previewY = contentY + 16;

        graphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0xFF141720);
        graphics.fill(previewX, previewY, previewX + previewW, previewY + 1, 0xFF242A38);
        graphics.fill(previewX, previewY + previewH - 1, previewX + previewW, previewY + previewH, 0xFF242A38);
        graphics.fill(previewX, previewY, previewX + 1, previewY + previewH, 0xFF242A38);
        graphics.fill(previewX + previewW - 1, previewY, previewX + previewW, previewY + previewH, 0xFF242A38);

        if (previewEntity != null) {
            ensureEntityId(previewEntity, selectedData.entityId());
            try {
                float bbH = Math.max(0.4f, previewEntity.getBbHeight());
                float bbW = Math.max(0.4f, previewEntity.getBbWidth());
                float maxDim = Math.max(bbH, bbW * 0.8f);
                int scale = (int) Math.clamp(82.0f / maxDim, 25, 78);
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics,
                        previewX, previewY, previewX + previewW, previewY + previewH,
                        scale, 0.0f, (float) mouseX, (float) mouseY, previewEntity
                );
            } catch (Throwable t) {
                graphics.drawCenteredString(font, ZooTranslations.component("zootycoon.ui.animals.preview_fallback").withStyle(ChatFormatting.DARK_GRAY), previewX + previewW / 2, previewY + previewH / 2, 0xFF888888);
            }
        }

        int infoX = previewX + previewW + 16;
        int infoW = contentW - (infoX - contentX) - 14;

        // Species Title (Normal font, no BOLD)
        String rawSpecies = formatSpecies(selectedData.entityTypeId());
        graphics.drawString(font, Component.literal(rawSpecies).withStyle(ChatFormatting.GOLD), infoX, contentY + 14, 0xFFFFFFFF, false);

        // Zookeeper Dropdown Selector Button (at red circled location)
        int zkBtnX = infoX;
        int zkBtnY = contentY + 26;
        int zkBtnW = Math.min(170, infoW);
        int zkBtnH = 18;

        boolean zkBtnHovered = mouseX >= zkBtnX && mouseX <= zkBtnX + zkBtnW && mouseY >= zkBtnY && mouseY <= zkBtnY + zkBtnH;
        int btnBg = zkBtnHovered ? 0xFF243048 : 0xFF181C26;
        int btnBorder = (zkBtnHovered || zookeeperDropdownOpen) ? 0xFF58A6FF : 0xFF2A3140;

        graphics.fill(zkBtnX, zkBtnY, zkBtnX + zkBtnW, zkBtnY + zkBtnH, btnBg);
        graphics.fill(zkBtnX, zkBtnY, zkBtnX + zkBtnW, zkBtnY + 1, btnBorder);
        graphics.fill(zkBtnX, zkBtnY + zkBtnH - 1, zkBtnX + zkBtnW, zkBtnY + zkBtnH, btnBorder);
        graphics.fill(zkBtnX, zkBtnY, zkBtnX + 1, zkBtnY + zkBtnH, btnBorder);
        graphics.fill(zkBtnX + zkBtnW - 1, zkBtnY, zkBtnX + zkBtnW, zkBtnY + zkBtnH, btnBorder);

        String keeperLabel;
        ChatFormatting keeperColor;
        if (selectedData.hasZookeeper()) {
            keeperLabel = "Zookeeper: " + selectedData.zookeeperName();
            keeperColor = ChatFormatting.GREEN;
        } else {
            keeperLabel = "Pilih Zookeeper...";
            keeperColor = ChatFormatting.GRAY;
        }

        int maxTextW = zkBtnW - 22;
        String trimmedLabel = font.plainSubstrByWidth(keeperLabel, maxTextW);
        graphics.drawString(font, Component.literal(trimmedLabel).withStyle(keeperColor), zkBtnX + 6, zkBtnY + 5, 0xFFFFFFFF, false);

        String arrow = zookeeperDropdownOpen ? "▲" : "▼";
        graphics.drawString(font, Component.literal(arrow).withStyle(ChatFormatting.GOLD), zkBtnX + zkBtnW - 12, zkBtnY + 5, 0xFFFFFFFF, false);

        if (!selectedData.hasZookeeper()) {
            int exX = zkBtnX + zkBtnW + 5;
            int exY = zkBtnY + 4;
            graphics.blit(EXCLAMATION_ICON, exX, exY, 0, 0, 10, 10, 64, 64);
            if (mouseX >= exX && mouseX <= exX + 10 && mouseY >= exY && mouseY <= exY + 10) {
                graphics.renderTooltip(font, ZooTranslations.component("zootycoon.ui.animals.no_zookeeper_tooltip").withStyle(ChatFormatting.RED), mouseX, mouseY);
            }
        }

        graphics.drawString(font, ZooTranslations.component("zootycoon.ui.animals.tag_label").withStyle(ChatFormatting.GRAY), infoX, contentY + 47, 0xFFFFFFFF, false);

        int scrollAreaX = infoX;
        int scrollAreaY = contentY + 102;
        int scrollAreaW = infoW;
        int scrollAreaH = contentH - 106;

        List<Component> redNeeds = getHabitatRedNeeds(previewEntity);
        List<ItemStack> foods = previewEntity != null ? AnimalDietRegistry.getFoodsForAnimal(previewEntity) : List.of();

        int slotSize = 22;
        int gap = 4;
        int availableW = infoW - 8;
        int colsPerRow = Math.max(1, (availableW + gap) / (slotSize + gap));
        int foodRows = (int) Math.ceil((double) foods.size() / colsPerRow);
        int foodGridH = foods.isEmpty() ? 0 : foodRows * (slotSize + gap);

        int habNeedsH = redNeeds.isEmpty() ? 13 : (11 + redNeeds.size() * 10 + 2);
        int detailsTotalContentH = 12 + 12 + 12 + 12 + 13 + habNeedsH + 14 + foodGridH + 8;

        this.maxDetailsScroll = Math.max(0, detailsTotalContentH - scrollAreaH);
        this.detailsScrollOffset = Math.clamp(this.detailsScrollOffset, 0, this.maxDetailsScroll);

        boolean inScrollArea = mouseX >= scrollAreaX && mouseX <= scrollAreaX + scrollAreaW && mouseY >= scrollAreaY && mouseY <= scrollAreaY + scrollAreaH;
        Component hoveredStatTooltip = null;

        // Scissor clipping so content never overflows
        graphics.enableScissor(contentX, scrollAreaY, contentX + contentW, scrollAreaY + scrollAreaH);

        int relY = 0;

        // 1. Health (Hearts)
        int healthY = scrollAreaY + relY - detailsScrollOffset;
        int totalHearts = Math.max(1, (int) Math.ceil(selectedData.maxHealth() / 2.0f));
        int heartsToDraw = Math.min(totalHearts, 10);

        for (int i = 0; i < heartsToDraw; i++) {
            int hX = infoX + i * 10;
            graphics.blitSprite(VANILLA_HEART_CONTAINER, hX, healthY, 9, 9);
            float hpForHeart = selectedData.health() - (i * 2.0f);
            if (hpForHeart >= 1.5f) {
                graphics.blitSprite(VANILLA_HEART_FULL, hX, healthY, 9, 9);
            } else if (hpForHeart >= 0.5f) {
                graphics.blitSprite(VANILLA_HEART_HALF, hX, healthY, 9, 9);
            }
        }
        int hp = (int) Math.ceil(selectedData.health());
        int maxHp = (int) Math.ceil(selectedData.maxHealth());
        int statTextX = infoX + 106;

        int hpPct = Math.round((selectedData.health() / Math.max(0.1f, selectedData.maxHealth())) * 100.0f);
        int hpColor = HologramBarRenderer.getStatusColor((float) hpPct);
        String hpStr = hp + "/" + maxHp + " HP (" + hpPct + "%)";
        graphics.drawString(font, Component.literal(hpStr), statTextX, healthY + 1, hpColor, false);
        relY += 12;

        // 2. Hunger (Food shanks + percentage)
        int hungerY = scrollAreaY + relY - detailsScrollOffset;
        int totalHalfShanks = 20;
        int filledHalfShanks = Math.clamp(Math.round((selectedData.hunger() / 100.0f) * totalHalfShanks), 0, totalHalfShanks);
        for (int i = 0; i < 10; i++) {
            int sX = infoX + i * 10;
            graphics.blitSprite(VANILLA_FOOD_EMPTY, sX, hungerY, 9, 9);
            int halfRemaining = filledHalfShanks - (i * 2);
            if (halfRemaining >= 2) {
                graphics.blitSprite(VANILLA_FOOD_FULL, sX, hungerY, 9, 9);
            } else if (halfRemaining == 1) {
                graphics.blitSprite(VANILLA_FOOD_HALF, sX, hungerY, 9, 9);
            }
        }
        int hungerColor = HologramBarRenderer.getStatusColor((float) selectedData.hunger());
        graphics.drawString(font, Component.literal(selectedData.hunger() + "%"), statTextX, hungerY + 1, hungerColor, false);
        if (inScrollArea && mouseX >= infoX && mouseX <= infoX + 100 && mouseY >= hungerY && mouseY <= hungerY + 10) {
            hoveredStatTooltip = Component.literal("Lapar: " + selectedData.hunger() + "%");
        }
        relY += 12;

        // 3. Thirst (Water droplets + percentage)
        int thirstY = scrollAreaY + relY - detailsScrollOffset;
        int filledDroplets = Math.clamp(Math.round(selectedData.thirst() / 10.0f), 0, 10);
        for (int i = 0; i < 10; i++) {
            int sX = infoX + i * 10;
            if (i < filledDroplets) {
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                graphics.setColor(0.35f, 0.40f, 0.48f, 0.45f);
            }
            graphics.blit(THIRST_ICON, sX, thirstY, 0, 0, 9, 9, 9, 9);
        }
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        int thirstColor = HologramBarRenderer.getStatusColor((float) selectedData.thirst());
        graphics.drawString(font, Component.literal(selectedData.thirst() + "%"), statTextX, thirstY + 1, thirstColor, false);
        if (inScrollArea && mouseX >= infoX && mouseX <= infoX + 100 && mouseY >= thirstY && mouseY <= thirstY + 10) {
            hoveredStatTooltip = Component.literal("Kehausan: " + selectedData.thirst() + "%");
        }
        relY += 12;

        // 4. Cleanliness (Sparkle icons + percentage)
        int cleanY = scrollAreaY + relY - detailsScrollOffset;
        int filledClean = Math.clamp(Math.round(selectedData.cleanliness() / 10.0f), 0, 10);
        for (int i = 0; i < 10; i++) {
            int sX = infoX + i * 10;
            if (i < filledClean) {
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                graphics.setColor(0.38f, 0.38f, 0.38f, 0.40f);
            }
            graphics.blit(CLEANLINESS_ICON, sX, cleanY, 0, 0, 9, 9, 9, 9);
        }
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        int cleanColor = HologramBarRenderer.getStatusColor((float) selectedData.cleanliness());
        graphics.drawString(font, Component.literal(selectedData.cleanliness() + "%"), statTextX, cleanY + 1, cleanColor, false);
        if (inScrollArea && mouseX >= infoX && mouseX <= infoX + 100 && mouseY >= cleanY && mouseY <= cleanY + 10) {
            hoveredStatTooltip = Component.literal("Kebersihan: " + selectedData.cleanliness() + "%");
        }
        relY += 12;

        // 5. Temperature (Thermometer icon + status)
        int tempY = scrollAreaY + relY - detailsScrollOffset;
        graphics.blit(THERMOMETER_ICON, infoX, tempY, 9, 9, 0, 0, 16, 16, 16, 16);
        int celsius = ZooAnimalHologramRenderer.parseCelsiusFromCategory(selectedData.tempCategory());
        String tempText = celsius + "°C ";
        graphics.drawString(font, Component.literal(tempText), infoX + 13, tempY + 1, 0xFFFFFFFF, false);
        int tempTextW = font.width(tempText);

        var habParsed = ZooAnimalHologramRenderer.HabitatParsedInfo.parse(selectedData.habitatNeeds(), selectedData.habitatStatus());
        if ("HEATER".equalsIgnoreCase(habParsed.tempStatus())) {
            graphics.drawString(font, Component.literal("(Butuh Heater)").withStyle(ChatFormatting.RED), infoX + 13 + tempTextW, tempY + 1, 0xFFFF5555, false);
            int labelW = font.width("(Butuh Heater) ");
            graphics.blit(TEMP_COLD_ICON, infoX + 13 + tempTextW + labelW, tempY, 9, 9, 0, 0, 16, 16, 16, 16);
        } else if ("COOLER".equalsIgnoreCase(habParsed.tempStatus())) {
            graphics.drawString(font, Component.literal("(Butuh Cooler Box)").withStyle(ChatFormatting.RED), infoX + 13 + tempTextW, tempY + 1, 0xFFFF5555, false);
            int labelW = font.width("(Butuh Cooler Box) ");
            graphics.blit(TEMP_HOT_ICON, infoX + 13 + tempTextW + labelW, tempY, 9, 9, 0, 0, 16, 16, 16, 16);
        } else {
            graphics.drawString(font, Component.literal("(Nyaman)").withStyle(ChatFormatting.GREEN), infoX + 13 + tempTextW, tempY + 1, 0xFF55FF55, false);
        }
        relY += 13;

        // 6. Habitat Needs (EXACTLY matching Hologram format)
        int habY = scrollAreaY + relY - detailsScrollOffset;
        if (redNeeds.isEmpty()) {
            graphics.drawString(font, Component.literal("Kebutuhan Habitat: Terpenuhi"), infoX, habY, 0xFF55FF55, false);
            relY += 13;
        } else {
            graphics.drawString(font, Component.literal("Kebutuhan Habitat:"), infoX, habY, 0xFFFFFF55, false);
            relY += 11;
            for (Component need : redNeeds) {
                int lineY = scrollAreaY + relY - detailsScrollOffset;
                graphics.drawString(font, need.copy(), infoX + 4, lineY, 0xFFFF5555, false);
                relY += 10;
            }
            relY += 2;
        }

        // 7. Favorite Food (Header + full grid with no limit)
        int gridY = scrollAreaY + relY - detailsScrollOffset;
        graphics.drawString(font, ZooTranslations.component("zootycoon.ui.animals.favorite_food").withStyle(ChatFormatting.YELLOW), infoX, gridY, 0xFFFFFFFF, false);
        relY += 14;

        int slotStartY = scrollAreaY + relY - detailsScrollOffset;
        ItemStack hoveredFoodStack = ItemStack.EMPTY;

        for (int i = 0; i < foods.size(); i++) {
            ItemStack foodStack = foods.get(i);
            int col = i % colsPerRow;
            int row = i / colsPerRow;
            int slotX = infoX + col * (slotSize + gap);
            int slotY = slotStartY + row * (slotSize + gap);

            boolean slotHovered = inScrollArea && mouseX >= slotX && mouseX <= slotX + slotSize && mouseY >= slotY && mouseY <= slotY + slotSize;
            int slotBg = slotHovered ? 0xFF1E2433 : 0xFF141720;
            int slotBorder = slotHovered ? 0xFF58A6FF : 0xFF2A3140;

            graphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, slotBg);
            graphics.fill(slotX, slotY, slotX + slotSize, slotY + 1, slotBorder);
            graphics.fill(slotX, slotY + slotSize - 1, slotX + slotSize, slotY + slotSize, slotBorder);
            graphics.fill(slotX, slotY, slotX + 1, slotY + slotSize, slotBorder);
            graphics.fill(slotX + slotSize - 1, slotY, slotX + slotSize, slotY + slotSize, slotBorder);

            graphics.renderItem(foodStack, slotX + 3, slotY + 3);

            if (slotHovered) {
                hoveredFoodStack = foodStack;
            }
        }

        graphics.disableScissor();

        // Render scrollbar if content overflows
        if (maxDetailsScroll > 0) {
            int sbX = contentX + contentW - 6;
            int sbW = 3;
            graphics.fill(sbX, scrollAreaY, sbX + sbW, scrollAreaY + scrollAreaH, 0xFF151922);
            int thumbH = Math.max(16, (int) ((float) scrollAreaH / detailsTotalContentH * scrollAreaH));
            int thumbY = scrollAreaY + (int) ((float) detailsScrollOffset / maxDetailsScroll * (scrollAreaH - thumbH));
            boolean sbHovered = mouseX >= sbX - 2 && mouseX <= sbX + sbW + 2 && mouseY >= scrollAreaY && mouseY <= scrollAreaY + scrollAreaH;
            int thumbColor = (sbHovered || isDraggingDetailsScrollbar) ? 0xFF58A6FF : 0xFF3A4459;
            graphics.fill(sbX, thumbY, sbX + sbW, thumbY + thumbH, thumbColor);
        }

        if (!hoveredFoodStack.isEmpty()) {
            graphics.renderTooltip(font, hoveredFoodStack.getHoverName(), mouseX, mouseY);
        } else if (hoveredStatTooltip != null && !zookeeperDropdownOpen) {
            graphics.renderTooltip(font, hoveredStatTooltip, mouseX, mouseY);
        }

        if (zookeeperDropdownOpen && selectedData != null) {
            renderZookeeperDropdown(graphics, mouseX, mouseY, font);
        }
    }

    private List<Component> getHabitatRedNeeds(LivingEntity previewEntity) {
        if (selectedData == null) return List.of();
        var habParsed = ZooAnimalHologramRenderer.HabitatParsedInfo.parse(selectedData.habitatNeeds(), selectedData.habitatStatus());
        List<Component> redNeeds = new ArrayList<>();
        if (!habParsed.serverUnmet().isEmpty()) {
            for (String item : habParsed.serverUnmet()) {
                if (item.startsWith("Bioma:") || item.startsWith("Butuh Heater") || item.startsWith("Butuh Cooler Box")) {
                    continue;
                }
                redNeeds.add(Component.literal("• " + item));
            }
        } else {
            if ("NO".equalsIgnoreCase(habParsed.waterStatus())) {
                redNeeds.add(Component.literal("• Butuh Air"));
            }
            if (!habParsed.groundOk() && previewEntity != null) {
                HabitatProfile profile = HabitatProfileLoader.getProfile(previewEntity);
                redNeeds.add(Component.literal("• Butuh " + HabitatEvaluator.getSpecificGroundLabel(profile)));
            }
        }

        if (!habParsed.biomeOk() && previewEntity != null) {
            HabitatProfile profile = HabitatProfileLoader.getProfile(previewEntity);
            List<String> biomes = ZooAnimalHologramRenderer.getBiomeListEnglish(profile);
            List<String> validBiomes = biomes.stream().filter(s -> !"Any".equalsIgnoreCase(s) && !s.isBlank()).toList();
            if (!validBiomes.isEmpty()) {
                redNeeds.add(Component.literal("• Bioma yang cocok:"));
                for (String b : validBiomes) {
                    redNeeds.add(Component.literal("  - " + b));
                }
            }
        }
        return redNeeds;
    }

    private LivingEntity resolveLivingEntity(Minecraft mc, ZooNetworkPayloads.AnimalSyncData data) {
        if (data == null) return null;

        if (mc.level != null) {
            Entity ent = mc.level.getEntity(data.entityId());
            if (ent instanceof LivingEntity living) {
                ensureEntityId(living, data.entityId());
                return living;
            }
        }

        if (clientDummyCache.containsKey(data.entityTypeId())) {
            LivingEntity cached = clientDummyCache.get(data.entityTypeId());
            if (cached != null) {
                ensureEntityId(cached, data.entityId());
                return cached;
            }
        }

        try {
            ResourceLocation id = ResourceLocation.parse(data.entityTypeId());
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            if (type != null && mc.level != null) {
                Entity dummy = type.create(mc.level);
                if (dummy instanceof LivingEntity living) {
                    ensureEntityId(living, data.entityId());
                    clientDummyCache.put(data.entityTypeId(), living);
                    return living;
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    private void ensureEntityId(LivingEntity entity, int desiredId) {
        if (entity == null) return;
        int idToSet = (desiredId != 0) ? desiredId : 999999;
        try {
            int currentId = entity.getId();
            if (currentId == 0) {
                entity.setId(idToSet);
            }
        } catch (Throwable e) {
            entity.setId(idToSet);
        }
    }

    private String formatSpecies(String rawId) {
        if (rawId == null || !rawId.contains(":")) return rawId;
        String path = rawId.substring(rawId.indexOf(':') + 1);
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            }
        }
        return sb.toString();
    }

    private List<ZooNetworkPayloads.ZookeeperSyncData> getAvailableKeepers() {
        List<ZooNetworkPayloads.ZookeeperSyncData> list = new ArrayList<>();
        if (selectedData == null) return list;

        for (var zk : ZooTycoonClient.clientZookeeperList) {
            boolean assignedToThisAnimal = (selectedData.hasZookeeper() && zk.name().equals(selectedData.zookeeperName()))
                    || (zk.isAssigned() && zk.assignedAnimalId() == selectedData.entityId());

            boolean assignedToOther = (zk.isAssigned() && zk.assignedAnimalId() != selectedData.entityId() && zk.assignedAnimalId() > 0)
                    || ZooTycoonClient.clientAnimals.stream().anyMatch(a -> a.entityId() != selectedData.entityId() && a.hasZookeeper() && a.zookeeperName().equals(zk.name()));

            // User requirement: "khusus untuk zookeper jangan di siabled tapi hilangkan saja option nya biar rapih"
            if (assignedToThisAnimal || !assignedToOther) {
                list.add(zk);
            }
        }
        return list;
    }

    private void renderZookeeperDropdown(GuiGraphics graphics, int mouseX, int mouseY, Font font) {
        var availableKeepers = getAvailableKeepers();
        boolean hasCurrentZk = selectedData.hasZookeeper();
        int totalRows = (hasCurrentZk ? 1 : 0) + (availableKeepers.isEmpty() && !hasCurrentZk ? 1 : availableKeepers.size());

        int itemH = 18;
        int maxVisibleRows = 5;
        int visibleRows = Math.min(Math.max(1, totalRows), maxVisibleRows);
        int headerH = 18;
        int listH = visibleRows * itemH;

        zkDropdownW = 170;
        zkDropdownH = headerH + listH + 4;

        int dx = zkDropdownX;
        int dy = zkDropdownY;
        if (dy + zkDropdownH > screen.height - 10) {
            dy = Math.max(10, zkDropdownY - zkDropdownH - 20);
        }

        int totalContentH = totalRows * itemH;
        int maxScroll = Math.max(0, totalContentH - listH);
        zkScrollOffset = Math.clamp(zkScrollOffset, 0, maxScroll);

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);

        // Border & background
        graphics.fill(dx - 1, dy - 1, dx + zkDropdownW + 1, dy + zkDropdownH + 1, 0xFF58A6FF);
        graphics.fill(dx, dy, dx + zkDropdownW, dy + zkDropdownH, 0xF5141720);

        // Header
        graphics.fill(dx, dy, dx + zkDropdownW, dy + headerH, 0xFF1C2230);
        graphics.drawString(font, Component.literal("Pilih Zookeeper:").withStyle(ChatFormatting.AQUA), dx + 8, dy + 5, 0xFFFFFFFF, false);

        int listStartY = dy + headerH + 2;
        graphics.enableScissor(dx + 1, listStartY, dx + zkDropdownW - 1, listStartY + listH);

        int curY = listStartY - zkScrollOffset;

        // Row 0: Unassign if has current zk
        if (hasCurrentZk) {
            if (curY + itemH >= listStartY && curY <= listStartY + listH) {
                boolean noneHovered = mouseX >= dx && mouseX <= dx + (maxScroll > 0 ? zkDropdownW - 5 : zkDropdownW)
                        && mouseY >= curY && mouseY <= curY + itemH && mouseY >= listStartY && mouseY <= listStartY + listH;
                if (noneHovered) {
                    graphics.fill(dx + 2, curY, dx + zkDropdownW - (maxScroll > 0 ? 7 : 2), curY + itemH, 0xFF243048);
                }
                ChatFormatting noneColor = noneHovered ? ChatFormatting.YELLOW : ChatFormatting.GRAY;
                graphics.drawString(font, Component.literal("— Lepas Zookeeper —").withStyle(noneColor), dx + 8, curY + 5, 0xFFFFFFFF, false);
            }
            curY += itemH;
        }

        if (availableKeepers.isEmpty() && !hasCurrentZk) {
            if (curY + itemH >= listStartY && curY <= listStartY + listH) {
                graphics.drawString(font, Component.literal("(Tidak ada yang bebas)").withStyle(ChatFormatting.DARK_GRAY), dx + 8, curY + 5, 0xFFFFFFFF, false);
            }
        } else {
            for (var zk : availableKeepers) {
                if (curY + itemH >= listStartY && curY <= listStartY + listH) {
                    boolean rowHovered = mouseX >= dx && mouseX <= dx + (maxScroll > 0 ? zkDropdownW - 5 : zkDropdownW)
                            && mouseY >= curY && mouseY <= curY + itemH && mouseY >= listStartY && mouseY <= listStartY + listH;
                    if (rowHovered) {
                        graphics.fill(dx + 2, curY, dx + zkDropdownW - (maxScroll > 0 ? 7 : 2), curY + itemH, 0xFF243048);
                    }
                    boolean isCurrent = selectedData.hasZookeeper() && zk.name().equals(selectedData.zookeeperName());
                    ChatFormatting nameColor = isCurrent ? ChatFormatting.GREEN : (rowHovered ? ChatFormatting.GOLD : ChatFormatting.WHITE);
                    String label = (isCurrent ? "✓ " : "• ") + zk.name();
                    String trimmedZkName = font.plainSubstrByWidth(label, zkDropdownW - (maxScroll > 0 ? 20 : 14));
                    graphics.drawString(font, Component.literal(trimmedZkName).withStyle(nameColor), dx + 8, curY + 5, 0xFFFFFFFF, false);
                }
                curY += itemH;
            }
        }

        graphics.disableScissor();

        // Scrollbar if needed
        if (maxScroll > 0) {
            int barX = dx + zkDropdownW - 5;
            int barY = listStartY;
            graphics.fill(barX, barY, barX + 3, barY + listH, 0xFF1C2230);
            float ratio = (float) listH / (float) totalContentH;
            int thumbH = Math.max(12, (int) (listH * ratio));
            int thumbY = barY + (int) ((listH - thumbH) * ((float) zkScrollOffset / (float) maxScroll));
            graphics.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFF58A6FF);
        }

        graphics.pose().popPose();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int width = screen.width;
        int sidebarX = 14;
        int sidebarW = 160;
        int contentX = sidebarX + sidebarW + 16;
        int contentY = 48;
        int contentW = width - contentX - 14;

        int previewX = contentX + 14;
        int previewW = 140;
        int infoX = previewX + previewW + 16;
        int infoW = contentW - (infoX - contentX) - 14;
        int zkBtnX = infoX;
        int zkBtnY = contentY + 26;
        int zkBtnW = Math.min(170, infoW);
        int zkBtnH = 18;

        if (zookeeperDropdownOpen && selectedData != null) {
            var availableKeepers = getAvailableKeepers();
            boolean hasCurrentZk = selectedData.hasZookeeper();
            int itemH = 18;
            int maxVisibleRows = 5;
            int totalRows = (hasCurrentZk ? 1 : 0) + (availableKeepers.isEmpty() && !hasCurrentZk ? 1 : availableKeepers.size());
            int visibleRows = Math.min(Math.max(1, totalRows), maxVisibleRows);
            int headerH = 18;
            int listH = visibleRows * itemH;
            int dx = zkBtnX;
            int dy = zkBtnY + zkBtnH + 2;
            if (dy + zkDropdownH > screen.height - 10) {
                dy = Math.max(10, zkBtnY - zkDropdownH - 2);
            }

            int listStartY = dy + headerH + 2;

            if (mouseX >= dx && mouseX <= dx + zkDropdownW && mouseY >= listStartY && mouseY <= listStartY + listH) {
                double relativeY = (mouseY - listStartY) + zkScrollOffset;
                int clickedIdx = (int) (relativeY / itemH);

                if (hasCurrentZk) {
                    if (clickedIdx == 0) {
                        // Clicked "Lepas Zookeeper"
                        ClientPlayNetworking.send(new ZooNetworkPayloads.AssignZookeeperPayload(selectedData.entityId(), null));
                        zookeeperDropdownOpen = false;
                        requestSync();
                        return true;
                    }
                    clickedIdx--;
                }

                if (clickedIdx >= 0 && clickedIdx < availableKeepers.size()) {
                    var zk = availableKeepers.get(clickedIdx);
                    ClientPlayNetworking.send(new ZooNetworkPayloads.AssignZookeeperPayload(selectedData.entityId(), zk.uuid()));
                    zookeeperDropdownOpen = false;
                    requestSync();
                    return true;
                }
                return true;
            } else if (mouseX >= zkBtnX && mouseX <= zkBtnX + zkBtnW && mouseY >= zkBtnY && mouseY <= zkBtnY + zkBtnH) {
                zookeeperDropdownOpen = false;
                return true;
            } else {
                zookeeperDropdownOpen = false;
            }
        }

        // Dropdown button clicked
        if (selectedData != null && mouseX >= zkBtnX && mouseX <= zkBtnX + zkBtnW && mouseY >= zkBtnY && mouseY <= zkBtnY + zkBtnH) {
            zookeeperDropdownOpen = !zookeeperDropdownOpen;
            zkDropdownX = zkBtnX;
            zkDropdownY = zkBtnY + zkBtnH + 2;
            zkScrollOffset = 0;
            return true;
        }

        int sidebarY = 72;
        int sidebarH = screen.height - sidebarY - 14;

        // Check if clicked sidebar scrollbar thumb/track
        if (mouseX >= sidebarX + sidebarW - 8 && mouseX <= sidebarX + sidebarW && mouseY >= sidebarY && mouseY <= sidebarY + sidebarH) {
            List<ZooNetworkPayloads.AnimalSyncData> animals = getValidAnimals();
            int totalContentH = animals.size() * ITEM_HEIGHT + 8;
            int maxScroll = Math.max(0, totalContentH - sidebarH);
            if (maxScroll > 0) {
                this.isDraggingSidebarScrollbar = true;
                double progress = Math.clamp((mouseY - sidebarY) / (double) sidebarH, 0.0, 1.0);
                this.scrollOffset = (int) (progress * maxScroll);
                return true;
            }
        }

        if (mouseX >= sidebarX && mouseX <= sidebarX + sidebarW && mouseY >= sidebarY && mouseY <= sidebarY + sidebarH) {
            List<ZooNetworkPayloads.AnimalSyncData> animals = getValidAnimals();
            double relativeY = (mouseY - (sidebarY + 4)) + scrollOffset;
            if (relativeY >= 0) {
                int clickedIdx = (int) (relativeY / ITEM_HEIGHT);
                if (clickedIdx >= 0 && clickedIdx < animals.size()) {
                    this.selectedData = animals.get(clickedIdx);
                    this.lastSelectedEntityId = selectedData.entityId();
                    this.detailsScrollOffset = 0;
                    if (nameInput != null) {
                        nameInput.setValue(selectedData.customName());
                    }
                    this.zookeeperDropdownOpen = false;
                    return true;
                }
            }
        }

        if (nameInput != null && nameInput.visible) {
            int ix = nameInput.getX();
            int iy = nameInput.getY();
            int iw = nameInput.getWidth();
            int ih = nameInput.getHeight();
            if (mouseX >= ix && mouseX <= ix + iw && mouseY >= iy && mouseY <= iy + ih) {
                screen.setFocused(nameInput);
                nameInput.setFocused(true);
                return false;
            }
        }

        if (selectedData != null) {
            int contentH = screen.height - contentY - 14;
            int scrollAreaY = contentY + 102;
            int scrollAreaH = contentH - 106;

            // Check scrollbar drag start
            if (maxDetailsScroll > 0) {
                int sbX = contentX + contentW - 6;
                if (mouseX >= sbX - 4 && mouseX <= sbX + 6 && mouseY >= scrollAreaY && mouseY <= scrollAreaY + scrollAreaH) {
                    this.isDraggingDetailsScrollbar = true;
                    Minecraft mc = Minecraft.getInstance();
                    LivingEntity previewEntity = resolveLivingEntity(mc, selectedData);
                    List<Component> redNeeds = getHabitatRedNeeds(previewEntity);
                    List<ItemStack> foods = previewEntity != null ? AnimalDietRegistry.getFoodsForAnimal(previewEntity) : List.of();
                    int slotSize = 22;
                    int gap = 4;
                    int availableW = infoW - 8;
                    int colsPerRow = Math.max(1, (availableW + gap) / (slotSize + gap));
                    int foodRows = (int) Math.ceil((double) foods.size() / colsPerRow);
                    int foodGridH = foods.isEmpty() ? 0 : foodRows * (slotSize + gap);
                    int habNeedsH = redNeeds.isEmpty() ? 13 : (11 + redNeeds.size() * 10 + 2);
                    int totalContentH = 12 + 12 + 12 + 12 + 13 + habNeedsH + 14 + foodGridH + 8;
                    int thumbH = Math.max(16, (int) ((float) scrollAreaH / totalContentH * scrollAreaH));
                    int availableTrack = scrollAreaH - thumbH;
                    if (availableTrack > 0) {
                        double progress = Math.clamp((mouseY - scrollAreaY - thumbH / 2.0) / availableTrack, 0.0, 1.0);
                        this.detailsScrollOffset = (int) (progress * maxDetailsScroll);
                    }
                    return true;
                }
            }

            Minecraft mc = Minecraft.getInstance();
            LivingEntity previewEntity = resolveLivingEntity(mc, selectedData);
            if (previewEntity != null) {
                List<ItemStack> foods = AnimalDietRegistry.getFoodsForAnimal(previewEntity);
                List<Component> redNeeds = getHabitatRedNeeds(previewEntity);
                int habNeedsH = redNeeds.isEmpty() ? 13 : (11 + redNeeds.size() * 10 + 2);
                int relY = 12 + 12 + 12 + 12 + 13 + habNeedsH + 14;
                int slotStartY = scrollAreaY + relY - detailsScrollOffset;
                int slotSize = 22;
                int gap = 4;
                int availableW = infoW - 8;
                int colsPerRow = Math.max(1, (availableW + gap) / (slotSize + gap));

                for (int i = 0; i < foods.size(); i++) {
                    int col = i % colsPerRow;
                    int row = i / colsPerRow;
                    int slotX = infoX + col * (slotSize + gap);
                    int slotY = slotStartY + row * (slotSize + gap);
                    if (mouseY >= scrollAreaY && mouseY <= scrollAreaY + scrollAreaH
                            && mouseX >= slotX && mouseX <= slotX + slotSize
                            && mouseY >= slotY && mouseY <= slotY + slotSize) {
                        ClientPlayNetworking.send(new ZooNetworkPayloads.FeedAnimalFromGuiPayload(selectedData.entityId()));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (zookeeperDropdownOpen && selectedData != null) {
            int width = screen.width;
            int sidebarX = 14;
            int sidebarW = 160;
            int contentX = sidebarX + sidebarW + 16;
            int contentY = 48;
            int contentW = width - contentX - 14;
            int previewX = contentX + 14;
            int previewW = 140;
            int infoX = previewX + previewW + 16;
            int infoW = contentW - (infoX - contentX) - 14;
            int zkBtnX = infoX;
            int zkBtnY = contentY + 26;

            int dx = zkBtnX;
            int dy = zkBtnY + 18 + 2;
            if (dy + zkDropdownH > screen.height - 10) {
                dy = Math.max(10, zkBtnY - zkDropdownH - 2);
            }
            if (mouseX >= dx && mouseX <= dx + zkDropdownW && mouseY >= dy && mouseY <= dy + zkDropdownH) {
                var availableKeepers = getAvailableKeepers();
                boolean hasCurrentZk = selectedData.hasZookeeper();
                int totalRows = (hasCurrentZk ? 1 : 0) + (availableKeepers.isEmpty() && !hasCurrentZk ? 1 : availableKeepers.size());
                int itemH = 18;
                int maxVisibleRows = 5;
                int visibleRows = Math.min(Math.max(1, totalRows), maxVisibleRows);
                int listH = visibleRows * itemH;
                int totalContentH = totalRows * itemH;
                int maxScroll = Math.max(0, totalContentH - listH);
                if (maxScroll > 0) {
                    this.zkScrollOffset = Math.clamp(this.zkScrollOffset - (int) (verticalAmount * 18), 0, maxScroll);
                    return true;
                }
            }
        }

        int sidebarX = 14;
        int sidebarW = 160;
        int sidebarY = 72;
        int sidebarH = screen.height - sidebarY - 14;

        if (mouseX >= sidebarX && mouseX <= sidebarX + sidebarW && mouseY >= sidebarY && mouseY <= sidebarY + sidebarH) {
            List<ZooNetworkPayloads.AnimalSyncData> animals = getValidAnimals();
            int totalContentH = animals.size() * ITEM_HEIGHT + 8;
            int maxScroll = Math.max(0, totalContentH - sidebarH);
            if (maxScroll > 0) {
                this.scrollOffset = Math.clamp(this.scrollOffset - (int) (verticalAmount * 24), 0, maxScroll);
                return true;
            }
        }

        int width = screen.width;
        int contentX = sidebarX + sidebarW + 16;
        int contentY = 48;
        int contentW = width - contentX - 14;
        int contentH = screen.height - contentY - 14;

        if (mouseX >= contentX && mouseX <= contentX + contentW && mouseY >= contentY && mouseY <= contentY + contentH) {
            if (maxDetailsScroll > 0) {
                this.detailsScrollOffset = Math.clamp(this.detailsScrollOffset - (int) (verticalAmount * 20), 0, maxDetailsScroll);
                return true;
            }
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingSidebarScrollbar) {
            int sidebarY = 72;
            int sidebarH = screen.height - sidebarY - 14;
            List<ZooNetworkPayloads.AnimalSyncData> animals = getValidAnimals();
            int totalContentH = animals.size() * ITEM_HEIGHT + 8;
            int maxScroll = Math.max(0, totalContentH - sidebarH);
            if (maxScroll > 0 && sidebarH > 0) {
                double progress = Math.clamp((mouseY - sidebarY) / (double) sidebarH, 0.0, 1.0);
                this.scrollOffset = (int) (progress * maxScroll);
                return true;
            }
        }

        if (isDraggingDetailsScrollbar && selectedData != null) {
            int sidebarX = 14;
            int sidebarW = 160;
            int contentX = sidebarX + sidebarW + 16;
            int contentY = 48;
            int contentW = screen.width - contentX - 14;
            int contentH = screen.height - contentY - 14;
            int previewX = contentX + 14;
            int previewW = 140;
            int infoW = contentW - (previewX + previewW + 16 - contentX) - 14;

            int scrollAreaY = contentY + 102;
            int scrollAreaH = contentH - 106;

            if (maxDetailsScroll > 0 && scrollAreaH > 0) {
                Minecraft mc = Minecraft.getInstance();
                LivingEntity previewEntity = resolveLivingEntity(mc, selectedData);
                List<Component> redNeeds = getHabitatRedNeeds(previewEntity);
                List<ItemStack> foods = previewEntity != null ? AnimalDietRegistry.getFoodsForAnimal(previewEntity) : List.of();
                int slotSize = 22;
                int gap = 4;
                int availableW = infoW - 8;
                int colsPerRow = Math.max(1, (availableW + gap) / (slotSize + gap));
                int foodRows = (int) Math.ceil((double) foods.size() / colsPerRow);
                int foodGridH = foods.isEmpty() ? 0 : foodRows * (slotSize + gap);
                int habNeedsH = redNeeds.isEmpty() ? 13 : (11 + redNeeds.size() * 10 + 2);
                int totalContentH = 12 + 12 + 12 + 12 + 13 + habNeedsH + 14 + foodGridH + 8;
                int thumbH = Math.max(16, (int) ((float) scrollAreaH / totalContentH * scrollAreaH));
                int availableTrack = scrollAreaH - thumbH;
                if (availableTrack > 0) {
                    double progress = Math.clamp((mouseY - scrollAreaY - thumbH / 2.0) / availableTrack, 0.0, 1.0);
                    this.detailsScrollOffset = (int) (progress * maxDetailsScroll);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDraggingSidebarScrollbar = false;
        this.isDraggingDetailsScrollbar = false;
        return false;
    }
}
