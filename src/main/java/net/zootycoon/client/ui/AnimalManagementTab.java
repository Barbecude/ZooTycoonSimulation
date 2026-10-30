package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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

    private EditBox modalNameInput;
    private ZooButton modalSaveButton;
    private ZooButton modalCancelButton;
    private ZooButton removeButton;
    private boolean renameModalOpen = false;

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
    private boolean showAllFoods = false;
    private static final int ITEM_HEIGHT = 28;

    public AnimalManagementTab(MainManagementScreen screen) {
        this.screen = screen;
    }

    public void init() {
        clientDummyCache.clear();
        requestSync();

        renameModalOpen = false;
        zookeeperDropdownOpen = false;

        int width = screen.width;
        int height = screen.height;

        int sidebarX = 14;
        int sidebarW = 160;
        int contentX = sidebarX + sidebarW + 16;
        int contentY = 48;
        int contentW = width - contentX - 14;

        int previewX = contentX + 14;
        int previewW = 140;
        int infoX = previewX + previewW + 16;
        int infoW = contentW - (infoX - contentX) - 14;

        int removeBtnY = contentY + 65;
        int removeBtnW = 100;
        removeButton = new ZooButton(infoX, removeBtnY, removeBtnW, 18, ZooTranslations.component("zootycoon.ui.animals.btn_remove"), b -> {
            if (selectedData != null) {
                int idToRemove = selectedData.entityId();
                ClientPlayNetworking.send(new ZooNetworkPayloads.UnregisterAnimalPayload(idToRemove));
                ZooTycoonClient.clientAnimals.removeIf(a -> a.entityId() == idToRemove);
                selectedData = null;
                updateSelectedData(true);
            }
        }).setCustomIcon(TRASH_ICON, 12, 12).asDanger();
        screen.addWidgetPublic(removeButton);

        // Rename modal widgets
        int modalW = 220;
        int modalH = 96;
        int modalX = (width - modalW) / 2;
        int modalY = (height - modalH) / 2;

        modalNameInput = new EditBox(screen.getFont(), modalX + 14, modalY + 32, modalW - 28, 18, ZooTranslations.component("zootycoon.ui.animals.input_hint"));
        modalNameInput.setMaxLength(32);
        modalNameInput.setVisible(false);
        screen.addWidgetPublic(modalNameInput);

        int modalBtnW = 88;
        modalSaveButton = new ZooButton(modalX + 14, modalY + 62, modalBtnW, 20, ZooTranslations.component("zootycoon.ui.animals.btn_save"), b -> {
            commitRename();
        }).setCustomIcon(PENCIL_ICON, 12, 12).asPrimary();
        modalSaveButton.visible = false;
        screen.addWidgetPublic(modalSaveButton);

        modalCancelButton = new ZooButton(modalX + modalW - 14 - modalBtnW, modalY + 62, modalBtnW, 20, Component.literal("Batal"), b -> {
            closeRenameModal();
        }).asSecondary();
        modalCancelButton.visible = false;
        screen.addWidgetPublic(modalCancelButton);

        updateSelectedData(false);
    }

    private void commitRename() {
        if (selectedData != null && modalNameInput != null) {
            String newName = modalNameInput.getValue().trim();
            ClientPlayNetworking.send(new ZooNetworkPayloads.RenameAnimalPayload(selectedData.entityId(), newName));
            selectedData = new ZooNetworkPayloads.AnimalSyncData(
                    selectedData.entityId(), selectedData.entityTypeId(), newName,
                    selectedData.hunger(), selectedData.thirst(), selectedData.cleanliness(), selectedData.health(), selectedData.maxHealth(),
                    selectedData.hasZookeeper(), selectedData.zookeeperName(),
                    selectedData.habitatStatus(), selectedData.habitatNeeds(), selectedData.tempCategory()
            );
            closeRenameModal();
            requestSync();
        }
    }

    private void openRenameModal() {
        if (selectedData == null) return;
        this.renameModalOpen = true;
        this.zookeeperDropdownOpen = false;
        if (modalNameInput != null) {
            modalNameInput.setValue(selectedData.customName());
            modalNameInput.setVisible(true);
            modalNameInput.setFocused(true);
            screen.setFocused(modalNameInput);
        }
        if (modalSaveButton != null) modalSaveButton.visible = true;
        if (modalCancelButton != null) modalCancelButton.visible = true;
        if (removeButton != null) removeButton.visible = false;
    }

    private void closeRenameModal() {
        this.renameModalOpen = false;
        if (modalNameInput != null) {
            modalNameInput.setVisible(false);
            modalNameInput.setFocused(false);
        }
        if (modalSaveButton != null) modalSaveButton.visible = false;
        if (modalCancelButton != null) modalCancelButton.visible = false;
        if (removeButton != null) removeButton.visible = (selectedData != null);
        screen.setFocused(null);
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

        if (selectedData != null) {
            if (removeButton != null && !renameModalOpen) {
                removeButton.visible = true;
            }
            if (forceTextReset || selectedData.entityId() != lastSelectedEntityId) {
                lastSelectedEntityId = selectedData.entityId();
                detailsScrollOffset = 0;
                showAllFoods = false;
            }
        } else {
            if (removeButton != null) {
                removeButton.visible = false;
            }
            lastSelectedEntityId = -1;
            closeRenameModal();
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
        int sidebarY = 48;
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

        List<Component> hoveredSidebarProblems = null;

        graphics.enableScissor(sidebarX + 1, sidebarY + 1, sidebarX + sidebarW - 1, sidebarY + sidebarH - 1);

        int itemRightMargin = maxScroll > 0 ? 8 : 3;
        int curY = sidebarY + 4 - scrollOffset;
        for (var a : animals) {
            if (curY + ITEM_HEIGHT >= sidebarY && curY <= sidebarY + sidebarH) {
                boolean isSelected = (selectedData != null && selectedData.entityId() == a.entityId());
                int itemBg = isSelected ? 0xFF243048 : 0xFF181C26;
                graphics.fill(sidebarX + 3, curY, sidebarX + sidebarW - itemRightMargin, curY + ITEM_HEIGHT - 2, itemBg);
                if (isSelected) {
                    graphics.fill(sidebarX + 3, curY, sidebarX + 5, curY + ITEM_HEIGHT - 2, 0xFF58A6FF);
                }

                int cardRight = sidebarX + sidebarW - itemRightMargin;

                // 1. Check problem conditions: no zookeeper OR stats / habitat problem
                boolean hasNoZk = !a.hasZookeeper();
                boolean isHpLow = (a.health() / Math.max(0.1f, a.maxHealth())) <= 0.5f;
                boolean isHungry = a.hunger() <= 50;
                boolean isThirsty = a.thirst() <= 50;
                boolean isDirty = a.cleanliness() <= 50;
                var habParsed = ZooAnimalHologramRenderer.HabitatParsedInfo.parse(a.habitatNeeds(), a.habitatStatus());
                boolean isTempProblem = "HEATER".equalsIgnoreCase(habParsed.tempStatus()) || "COOLER".equalsIgnoreCase(habParsed.tempStatus());
                boolean isHabProblem = !habParsed.serverUnmet().isEmpty();
                boolean hasProblem = hasNoZk || isHpLow || isHungry || isThirsty || isDirty || isTempProblem || isHabProblem;

                // 2. Mini 3D Animal Model (di samping kiri nama satwa)
                int modelSize = 22;
                int modelX = sidebarX + 6;
                int modelY = curY + 2;

                LivingEntity miniEntity = resolveLivingEntity(mc, a);
                if (miniEntity != null) {
                    try {
                        float bbH = Math.max(0.4f, miniEntity.getBbHeight());
                        float bbW = Math.max(0.4f, miniEntity.getBbWidth());
                        float maxDim = Math.max(bbH, bbW * 0.8f);
                        int miniScale = (int) Math.clamp(13.0f / maxDim, 5, 16);
                        if (modelY >= sidebarY && modelY + modelSize <= sidebarY + sidebarH) {
                            InventoryScreen.renderEntityInInventoryFollowsMouse(
                                    graphics,
                                    modelX, modelY, modelX + modelSize, modelY + modelSize,
                                    miniScale, 0.0f, (float) mouseX, (float) mouseY, miniEntity
                            );
                        }
                    } catch (Throwable ignored) {}
                }

                // 3. Animal Name in normal white (di samping kanan model 3D)
                String displayName = a.customName().isEmpty() ? formatSpecies(a.entityTypeId()) : a.customName();
                int textX = sidebarX + 32;
                int textY = curY + 9;
                int exX = cardRight - 14;
                int exY = curY + 8;
                int maxNameW = Math.max(20, (hasProblem ? exX - 4 : cardRight - 4) - textX);
                String trimmedName = font.plainSubstrByWidth(displayName, maxNameW);
                graphics.drawString(font, Component.literal(trimmedName).withStyle(ChatFormatting.WHITE), textX, textY, 0xFFFFFFFF, false);

                // 4. Alert Exclamation Mark (align kanan)
                if (hasProblem) {
                    graphics.blit(EXCLAMATION_ICON, exX, exY, 0, 0, 10, 10, 64, 64);
                    if (mouseX >= exX && mouseX <= exX + 10 && mouseY >= exY && mouseY <= exY + 10) {
                        List<Component> problems = new ArrayList<>();
                        problems.add(Component.literal(displayName).withStyle(ChatFormatting.GOLD));
                        if (hasNoZk) problems.add(Component.literal("• Belum ada Zookeeper").withStyle(ChatFormatting.RED));
                        if (isHpLow) problems.add(Component.literal("• Darah rendah (" + Math.round((a.health() / Math.max(0.1f, a.maxHealth())) * 100.0f) + "%)").withStyle(ChatFormatting.RED));
                        if (isHungry) problems.add(Component.literal("• Lapar (" + a.hunger() + "%)").withStyle(ChatFormatting.GOLD));
                        if (isThirsty) problems.add(Component.literal("• Kehausan (" + a.thirst() + "%)").withStyle(ChatFormatting.AQUA));
                        if (isDirty) problems.add(Component.literal("• Kotor (" + a.cleanliness() + "%)").withStyle(ChatFormatting.YELLOW));
                        if (isTempProblem) {
                            String tempReason = "HEATER".equalsIgnoreCase(habParsed.tempStatus()) ? "Butuh Heater" : "Butuh Cooler Box";
                            problems.add(Component.literal("• Suhu tidak nyaman (" + tempReason + ")").withStyle(ChatFormatting.RED));
                        }
                        if (isHabProblem) problems.add(Component.literal("• Kebutuhan habitat bermasalah").withStyle(ChatFormatting.RED));
                        hoveredSidebarProblems = problems;
                    }
                }
            }
            curY += ITEM_HEIGHT;
        }

        graphics.disableScissor();

        if (hoveredSidebarProblems != null && !renameModalOpen && !zookeeperDropdownOpen) {
            graphics.renderComponentTooltip(font, hoveredSidebarProblems, mouseX, mouseY);
        }

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

        // 1. Animal Name & Pencil Icon
        String displayName = selectedData.customName().isEmpty() ? formatSpecies(selectedData.entityTypeId()) : selectedData.customName();
        graphics.drawString(font, Component.literal(displayName).withStyle(ChatFormatting.WHITE), infoX, contentY + 14, 0xFFFFFFFF, false);

        int nameW = font.width(displayName);
        int pencilX = infoX + nameW + 6;
        int pencilY = contentY + 12;
        int pencilSize = 12;
        boolean pencilHovered = !renameModalOpen && !zookeeperDropdownOpen && mouseX >= pencilX - 2 && mouseX <= pencilX + pencilSize + 2 && mouseY >= pencilY - 2 && mouseY <= pencilY + pencilSize + 2;
        if (pencilHovered) {
            graphics.fill(pencilX - 2, pencilY - 2, pencilX + pencilSize + 2, pencilY + pencilSize + 2, 0x4058A6FF);
        }
        graphics.blit(PENCIL_ICON, pencilX, pencilY, 12, 12, 0.0f, 0.0f, 16, 16, 16, 16);

        // 2. Jenis Hewan / Diet Category Subtitle (Warna ungu sama seperti hologram)
        int dietY = contentY + 28;
        Component dietComp = AnimalDietRegistry.getDietCategorySubtitleComponent(previewEntity);
        graphics.drawString(font, dietComp, infoX, dietY, 0xFFFFFFFF, false);

        // 3. Zookeeper Dropdown Selector Button (at red circled location)
        int zkBtnX = infoX;
        int zkBtnY = contentY + 43;
        int zkBtnW = Math.min(170, infoW);
        int zkBtnH = 18;

        boolean zkBtnHovered = mouseX >= zkBtnX && mouseX <= zkBtnX + zkBtnW && mouseY >= zkBtnY && mouseY <= zkBtnY + zkBtnH;
        int btnBg = zkBtnHovered ? 0xFF222B3A : 0xFF181C26;
        int btnBorder = (zkBtnHovered || zookeeperDropdownOpen) ? 0xFF4E5D78 : 0xFF2A3140;

        graphics.fill(zkBtnX, zkBtnY, zkBtnX + zkBtnW, zkBtnY + zkBtnH, btnBg);
        graphics.fill(zkBtnX, zkBtnY, zkBtnX + zkBtnW, zkBtnY + 1, btnBorder);
        graphics.fill(zkBtnX, zkBtnY + zkBtnH - 1, zkBtnX + zkBtnW, zkBtnY + zkBtnH, btnBorder);
        graphics.fill(zkBtnX, zkBtnY, zkBtnX + 1, zkBtnY + zkBtnH, btnBorder);
        graphics.fill(zkBtnX + zkBtnW - 1, zkBtnY, zkBtnX + zkBtnW, zkBtnY + zkBtnH, btnBorder);

        String keeperLabel;
        ChatFormatting keeperColor;
        if (selectedData.hasZookeeper()) {
            keeperLabel = "Zookeeper: " + selectedData.zookeeperName();
            keeperColor = ChatFormatting.WHITE;
        } else {
            keeperLabel = "Pilih Zookeeper...";
            keeperColor = ChatFormatting.GRAY;
        }

        int maxTextW = zkBtnW - 22;
        String trimmedLabel = font.plainSubstrByWidth(keeperLabel, maxTextW);
        graphics.drawString(font, Component.literal(trimmedLabel).withStyle(keeperColor), zkBtnX + 6, zkBtnY + 5, 0xFFFFFFFF, false);

        String arrow = zookeeperDropdownOpen ? "▲" : "▼";
        graphics.drawString(font, Component.literal(arrow).withStyle(ChatFormatting.GRAY), zkBtnX + zkBtnW - 12, zkBtnY + 5, 0xFFFFFFFF, false);

        if (!selectedData.hasZookeeper()) {
            int exX = zkBtnX + zkBtnW + 5;
            int exY = zkBtnY + 4;
            graphics.blit(EXCLAMATION_ICON, exX, exY, 0, 0, 10, 10, 64, 64);
            if (!renameModalOpen && !zookeeperDropdownOpen && mouseX >= exX && mouseX <= exX + 10 && mouseY >= exY && mouseY <= exY + 10) {
                graphics.renderTooltip(font, ZooTranslations.component("zootycoon.ui.animals.no_zookeeper_tooltip").withStyle(ChatFormatting.RED), mouseX, mouseY);
            }
        }

        int scrollAreaX = infoX;
        int scrollAreaY = contentY + 87;
        int scrollAreaW = infoW;
        int scrollAreaH = contentH - 91;

        List<Component> redNeeds = getHabitatRedNeeds(previewEntity);
        List<ItemStack> foods = previewEntity != null ? AnimalDietRegistry.getFoodsForAnimal(previewEntity) : List.of();

        int slotSize = 22;
        int gap = 4;
        int availableW = infoW - 8;
        int colsPerRow = Math.max(1, (availableW + gap) / (slotSize + gap));
        boolean hasMultipleFoodRows = foods.size() > colsPerRow;
        int displayedFoodCount = (showAllFoods || !hasMultipleFoodRows) ? foods.size() : colsPerRow;
        int foodRows = (int) Math.ceil((double) displayedFoodCount / colsPerRow);
        int foodGridH = foods.isEmpty() ? 0 : foodRows * (slotSize + gap);
        int foodToggleBtnH = hasMultipleFoodRows ? 20 : 0;

        boolean isAquatic = net.zootycoon.animal.ZooAnimalHungerManager.isPureAquatic(selectedData.entityTypeId());
        int cleanRowH = isAquatic ? 0 : 12;
        int habNeedsH = redNeeds.isEmpty() ? 13 : (11 + redNeeds.size() * 10 + 2);
        int detailsTotalContentH = 12 + 12 + 12 + cleanRowH + 13 + habNeedsH + 14 + foodGridH + foodToggleBtnH + 8;

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
        int statTextX = infoX + 106;
        int hpPct = Math.round((selectedData.health() / Math.max(0.1f, selectedData.maxHealth())) * 100.0f);
        int hpColor = HologramBarRenderer.getStatusColor((float) hpPct);
        graphics.drawString(font, Component.literal(hpPct + "%"), statTextX, healthY + 1, hpColor, false);
        if (inScrollArea && mouseX >= infoX && mouseX <= infoX + 100 && mouseY >= healthY && mouseY <= healthY + 10) {
            int hp = (int) Math.ceil(selectedData.health());
            int maxHp = (int) Math.ceil(selectedData.maxHealth());
            hoveredStatTooltip = Component.literal("Darah: " + hp + "/" + maxHp + " HP (" + hpPct + "%)");
        }
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

        // 4. Cleanliness (Sparkle icons + percentage) - Hidden for pure aquatic animals
        if (!isAquatic) {
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
        }

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

        for (int i = 0; i < displayedFoodCount; i++) {
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

        int foodRowsRendered = (int) Math.ceil((double) displayedFoodCount / colsPerRow);
        relY += foodRowsRendered * (slotSize + gap);

        if (hasMultipleFoodRows) {
            int toggleBtnX = infoX;
            int toggleBtnY = scrollAreaY + relY - detailsScrollOffset;
            int toggleBtnW = Math.min(150, availableW);
            int toggleBtnH = 16;
            boolean toggleHovered = inScrollArea && mouseX >= toggleBtnX && mouseX <= toggleBtnX + toggleBtnW && mouseY >= toggleBtnY && mouseY <= toggleBtnY + toggleBtnH;
            int tBtnBg = toggleHovered ? 0xFF263045 : 0xFF19202D;
            int tBtnBorder = toggleHovered ? 0xFF58A6FF : 0xFF354157;
            graphics.fill(toggleBtnX, toggleBtnY, toggleBtnX + toggleBtnW, toggleBtnY + toggleBtnH, tBtnBg);
            graphics.fill(toggleBtnX, toggleBtnY, toggleBtnX + toggleBtnW, toggleBtnY + 1, tBtnBorder);
            graphics.fill(toggleBtnX, toggleBtnY + toggleBtnH - 1, toggleBtnX + toggleBtnW, toggleBtnY + toggleBtnH, tBtnBorder);
            graphics.fill(toggleBtnX, toggleBtnY + 1, toggleBtnX + 1, toggleBtnY + toggleBtnH, tBtnBorder);
            graphics.fill(toggleBtnX + toggleBtnW - 1, toggleBtnY, toggleBtnX + toggleBtnW, toggleBtnY + toggleBtnH, tBtnBorder);

            String toggleText = showAllFoods ? "▲ Sembunyikan" : "▼ Lihat Semua Makanan (" + foods.size() + ")";
            int textW = font.width(toggleText);
            graphics.drawString(font, toggleText, toggleBtnX + (toggleBtnW - textW) / 2, toggleBtnY + (toggleBtnH - 8) / 2, toggleHovered ? 0xFFFFFFFF : 0xFFC0CAD8, false);
            relY += 20;
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

        if (!hoveredFoodStack.isEmpty() && !renameModalOpen && !zookeeperDropdownOpen) {
            graphics.renderTooltip(font, hoveredFoodStack.getHoverName(), mouseX, mouseY);
        } else if (hoveredStatTooltip != null && !zookeeperDropdownOpen && !renameModalOpen) {
            graphics.renderTooltip(font, hoveredStatTooltip, mouseX, mouseY);
        }

        if (zookeeperDropdownOpen && selectedData != null && !renameModalOpen) {
            renderZookeeperDropdown(graphics, mouseX, mouseY, font);
        }

        if (renameModalOpen) {
            renderRenameModal(graphics, mouseX, mouseY, font, width, height);
        }
    }

    private void renderRenameModal(GuiGraphics graphics, int mouseX, int mouseY, Font font, int width, int height) {
        if (!renameModalOpen) return;

        // Dark dim backdrop covering full screen
        graphics.fill(0, 0, width, height, 0x90000000);

        int modalW = 220;
        int modalH = 96;
        int modalX = (width - modalW) / 2;
        int modalY = (height - modalH) / 2;

        // Modal Box Background & 1px Borders
        graphics.fill(modalX, modalY, modalX + modalW, modalY + modalH, 0xF5121620);
        graphics.fill(modalX, modalY, modalX + modalW, modalY + 1, 0xFF384357);
        graphics.fill(modalX, modalY + modalH - 1, modalX + modalW, modalY + modalH, 0xFF384357);
        graphics.fill(modalX, modalY + 1, modalX + 1, modalY + modalH, 0xFF384357);
        graphics.fill(modalX + modalW - 1, modalY, modalX + modalW, modalY + modalH, 0xFF384357);

        // Modal Title
        graphics.drawString(font, Component.literal("Ubah Nama Satwa").withStyle(ChatFormatting.GOLD), modalX + 14, modalY + 14, 0xFFFFFFFF, false);
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

        if (mc.level != null && data.entityId() != 0) {
            Entity ent = mc.level.getEntity(data.entityId());
            if (ent instanceof LivingEntity living) {
                return living;
            }
        }

        if (clientDummyCache.containsKey(data.entityTypeId())) {
            LivingEntity cached = clientDummyCache.get(data.entityTypeId());
            if (cached != null) {
                return cached;
            }
        }

        try {
            ResourceLocation id = ResourceLocation.parse(data.entityTypeId());
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            if (type != null && mc.level != null) {
                Entity dummy = type.create(mc.level);
                if (dummy instanceof LivingEntity living) {
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

            // User requirement: "jangan munculkan zookeper yg sudah dipilih atau sudah punya hewan yg dibina"
            if (!assignedToThisAnimal && !assignedToOther) {
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
        graphics.fill(dx - 1, dy - 1, dx + zkDropdownW + 1, dy + zkDropdownH + 1, 0xFF354157);
        graphics.fill(dx, dy, dx + zkDropdownW, dy + zkDropdownH, 0xF5141720);

        // Header
        graphics.fill(dx, dy, dx + zkDropdownW, dy + headerH, 0xFF181D27);
        graphics.fill(dx, dy + headerH - 1, dx + zkDropdownW, dy + headerH, 0xFF2A3444);
        graphics.drawString(font, Component.literal("Pilih Zookeeper:").withStyle(ChatFormatting.GRAY), dx + 8, dy + 5, 0xFFFFFFFF, false);

        int listStartY = dy + headerH + 2;
        graphics.enableScissor(dx + 1, listStartY, dx + zkDropdownW - 1, listStartY + listH);

        int curY = listStartY - zkScrollOffset;

        // Row 0: Unassign if has current zk
        if (hasCurrentZk) {
            if (curY + itemH >= listStartY && curY <= listStartY + listH) {
                boolean noneHovered = mouseX >= dx && mouseX <= dx + (maxScroll > 0 ? zkDropdownW - 5 : zkDropdownW)
                        && mouseY >= curY && mouseY <= curY + itemH && mouseY >= listStartY && mouseY <= listStartY + listH;
                if (noneHovered) {
                    graphics.fill(dx + 2, curY, dx + zkDropdownW - (maxScroll > 0 ? 7 : 2), curY + itemH, 0xFF222B3A);
                }
                ChatFormatting noneColor = noneHovered ? ChatFormatting.WHITE : ChatFormatting.GRAY;
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
                        graphics.fill(dx + 2, curY, dx + zkDropdownW - (maxScroll > 0 ? 7 : 2), curY + itemH, 0xFF222B3A);
                    }
                    boolean isCurrent = selectedData.hasZookeeper() && zk.name().equals(selectedData.zookeeperName());
                    String label = (isCurrent ? "✓ " : "• ") + zk.name();
                    String trimmedZkName = font.plainSubstrByWidth(label, zkDropdownW - (maxScroll > 0 ? 20 : 14));
                    graphics.drawString(font, Component.literal(trimmedZkName).withStyle(ChatFormatting.WHITE), dx + 8, curY + 5, 0xFFFFFFFF, false);
                }
                curY += itemH;
            }
        }

        graphics.disableScissor();

        // Scrollbar if needed
        if (maxScroll > 0) {
            int barX = dx + zkDropdownW - 5;
            int barY = listStartY;
            graphics.fill(barX, barY, barX + 3, barY + listH, 0xFF161B24);
            float ratio = (float) listH / (float) totalContentH;
            int thumbH = Math.max(12, (int) (listH * ratio));
            int thumbY = barY + (int) ((listH - thumbH) * ((float) zkScrollOffset / (float) maxScroll));
            graphics.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFF4E5D78);
        }

        graphics.pose().popPose();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int width = screen.width;
        int height = screen.height;

        if (renameModalOpen) {
            int modalW = 220;
            int modalH = 96;
            int modalX = (width - modalW) / 2;
            int modalY = (height - modalH) / 2;

            if (modalSaveButton != null && modalSaveButton.visible && modalSaveButton.mouseClicked(mouseX, mouseY, button)) return true;
            if (modalCancelButton != null && modalCancelButton.visible && modalCancelButton.mouseClicked(mouseX, mouseY, button)) return true;
            if (modalNameInput != null && modalNameInput.visible && modalNameInput.mouseClicked(mouseX, mouseY, button)) {
                screen.setFocused(modalNameInput);
                modalNameInput.setFocused(true);
                return true;
            }

            if (mouseX < modalX || mouseX > modalX + modalW || mouseY < modalY || mouseY > modalY + modalH) {
                closeRenameModal();
            }
            return true;
        }

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
        int zkBtnY = contentY + 43;
        int zkBtnW = Math.min(170, infoW);
        int zkBtnH = 18;

        // Pencil Icon clicked -> open rename modal
        if (selectedData != null && !zookeeperDropdownOpen) {
            String displayName = selectedData.customName().isEmpty() ? formatSpecies(selectedData.entityTypeId()) : selectedData.customName();
            int nameW = screen.getFont().width(displayName);
            int pencilX = infoX + nameW + 6;
            int pencilY = contentY + 13;
            int pencilSize = 12;
            if (mouseX >= pencilX - 2 && mouseX <= pencilX + pencilSize + 2 && mouseY >= pencilY - 2 && mouseY <= pencilY + pencilSize + 2) {
                openRenameModal();
                return true;
            }
        }

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

        int sidebarY = 48;
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
                    this.zookeeperDropdownOpen = false;
                    closeRenameModal();
                    return true;
                }
            }
        }

        if (selectedData != null) {
            int contentH = screen.height - contentY - 14;
            int scrollAreaY = contentY + 87;
            int scrollAreaH = contentH - 91;

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
                boolean isAquatic = net.zootycoon.animal.ZooAnimalHungerManager.isPureAquatic(selectedData.entityTypeId());
                int cleanRowH = isAquatic ? 0 : 12;
                int relY = 12 + 12 + 12 + cleanRowH + 13 + habNeedsH + 14;
                int slotStartY = scrollAreaY + relY - detailsScrollOffset;
                int slotSize = 22;
                int gap = 4;
                int availableW = infoW - 8;
                int colsPerRow = Math.max(1, (availableW + gap) / (slotSize + gap));
                boolean hasMultipleFoodRows = foods.size() > colsPerRow;
                int displayedFoodCount = (showAllFoods || !hasMultipleFoodRows) ? foods.size() : colsPerRow;

                for (int i = 0; i < displayedFoodCount; i++) {
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

                if (hasMultipleFoodRows) {
                    int foodRowsRendered = (int) Math.ceil((double) displayedFoodCount / colsPerRow);
                    int toggleBtnY = slotStartY + foodRowsRendered * (slotSize + gap);
                    int toggleBtnX = infoX;
                    int toggleBtnW = Math.min(150, availableW);
                    int toggleBtnH = 16;
                    if (mouseY >= scrollAreaY && mouseY <= scrollAreaY + scrollAreaH
                            && mouseX >= toggleBtnX && mouseX <= toggleBtnX + toggleBtnW
                            && mouseY >= toggleBtnY && mouseY <= toggleBtnY + toggleBtnH) {
                        showAllFoods = !showAllFoods;
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (renameModalOpen) return true;

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
            int zkBtnY = contentY + 43;

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
        int sidebarY = 48;
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
        if (renameModalOpen) return true;

        if (isDraggingSidebarScrollbar) {
            int sidebarY = 48;
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

            int scrollAreaY = contentY + 87;
            int scrollAreaH = contentH - 91;

            if (maxDetailsScroll > 0 && scrollAreaH > 0) {
                Minecraft mc = Minecraft.getInstance();
                LivingEntity previewEntity = resolveLivingEntity(mc, selectedData);
                List<Component> redNeeds = getHabitatRedNeeds(previewEntity);
                List<ItemStack> foods = previewEntity != null ? AnimalDietRegistry.getFoodsForAnimal(previewEntity) : List.of();
                int slotSize = 22;
                int gap = 4;
                int availableW = infoW - 8;
                int colsPerRow = Math.max(1, (availableW + gap) / (slotSize + gap));
                boolean hasMultipleFoodRows = foods.size() > colsPerRow;
                int displayedFoodCount = (showAllFoods || !hasMultipleFoodRows) ? foods.size() : colsPerRow;
                int foodRows = (int) Math.ceil((double) displayedFoodCount / colsPerRow);
                int foodGridH = foods.isEmpty() ? 0 : foodRows * (slotSize + gap);
                int foodToggleBtnH = hasMultipleFoodRows ? 20 : 0;
                int habNeedsH = redNeeds.isEmpty() ? 13 : (11 + redNeeds.size() * 10 + 2);
                int totalContentH = 12 + 12 + 12 + 12 + 13 + habNeedsH + 14 + foodGridH + foodToggleBtnH + 8;
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

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (renameModalOpen) {
            if (keyCode == 256) { // GLFW_KEY_ESCAPE
                closeRenameModal();
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // GLFW_KEY_ENTER / NUMPAD_ENTER
                commitRename();
                return true;
            }
            if (modalNameInput != null && modalNameInput.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return true; // Block other inputs while modal is active
        }
        return false;
    }
}
