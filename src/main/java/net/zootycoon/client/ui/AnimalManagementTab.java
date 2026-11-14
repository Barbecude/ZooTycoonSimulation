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
import net.zootycoon.license.LicenseTier;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private ZooButton sellButton;
    private final Set<String> expandedParents = new HashSet<>();
    // Expand arrow x per animal uuid from the last frame, so clicks hit exactly where the arrow is drawn
    private final Map<String, Integer> renderedArrowX = new HashMap<>();

    public static class AnimalListItem {
        public final ZooNetworkPayloads.AnimalSyncData data;
        public final int depth;
        public final boolean isChild;
        public final boolean hasChildren;
        public final boolean isExpanded;
        public final int childCount;

        public AnimalListItem(ZooNetworkPayloads.AnimalSyncData data, int depth, boolean hasChildren, boolean isExpanded, int childCount) {
            this.data = data;
            this.depth = depth;
            this.isChild = depth > 0;
            this.hasChildren = hasChildren;
            this.isExpanded = isExpanded;
            this.childCount = childCount;
        }
    }

    // Indent per tree level; capped so deep generations still leave room for the name
    private static final int TREE_INDENT = 7;
    private static final int MAX_TREE_INDENT_LEVELS = 3;

    private static int treeIndent(AnimalListItem item) {
        return Math.min(item.depth, MAX_TREE_INDENT_LEVELS) * TREE_INDENT;
    }

    public static class LayoutMetrics {
        public int margin;
        public int gap;
        public int topY;
        public int bottomMargin;

        public int sidebarX;
        public int sidebarW;
        public int sidebarY;
        public int sidebarH;

        public int contentX;
        public int contentY;
        public int contentW;
        public int contentH;

        public int innerPadding;
        public int innerGap;

        public int previewX;
        public int previewY;
        public int previewW;
        public int previewH;

        public int infoX;
        public int infoW;

        public int zkBtnX;
        public int zkBtnY;
        public int zkBtnW;
        public int zkBtnH;

        public int btnRowY;
        public int removeBtnX, removeBtnY, removeBtnW, removeBtnH;
        public int sellBtnX, sellBtnY, sellBtnW, sellBtnH;
        public boolean sellVisible;

        public int scrollAreaX, scrollAreaY, scrollAreaW, scrollAreaH;
    }

    private LayoutMetrics computeLayout(int width, int height, boolean hasSell) {
        LayoutMetrics m = new LayoutMetrics();
        m.margin = (width < 500) ? 10 : 14;
        m.gap = (width < 500) ? 10 : 16;
        m.topY = 48;
        m.bottomMargin = m.margin;

        m.sidebarW = (width < 520) ? 125 : (width < 700 ? 145 : 165);
        m.sidebarX = m.margin;
        m.sidebarY = m.topY;
        m.sidebarH = height - m.sidebarY - m.bottomMargin;

        m.contentX = m.sidebarX + m.sidebarW + m.gap;
        m.contentY = m.topY;
        m.contentW = width - m.contentX - m.margin;
        m.contentH = height - m.contentY - m.bottomMargin;

        m.innerPadding = (m.contentW < 340) ? 10 : 14;
        m.innerGap = (m.contentW < 340) ? 10 : 16;

        if (m.contentW < 330) {
            m.previewW = 100;
        } else if (m.contentW < 440) {
            m.previewW = 120;
        } else {
            m.previewW = 140;
        }
        m.previewH = Math.min(m.previewW, m.contentH - m.innerPadding * 2);
        m.previewW = m.previewH; // Keep 1:1 square

        m.previewX = m.contentX + m.innerPadding;
        m.previewY = m.contentY + m.innerPadding;

        m.infoX = m.previewX + m.previewW + m.innerGap;
        m.infoW = Math.max(80, m.contentX + m.contentW - m.innerPadding - m.infoX);

        m.zkBtnX = m.infoX;
        m.zkBtnY = m.contentY + 41;
        m.zkBtnW = Math.min(170, m.infoW - 14);
        m.zkBtnH = 18;

        m.btnRowY = m.contentY + 62;
        m.removeBtnH = 18;
        m.sellBtnH = 18;
        m.sellVisible = hasSell;

        if (hasSell) {
            if (m.infoW >= 170) {
                int btnGap = 4;
                m.removeBtnW = Math.max(68, Math.min(90, (int) ((m.infoW - btnGap) * 0.42f)));
                m.sellBtnW = m.infoW - m.removeBtnW - btnGap;
                m.removeBtnX = m.infoX;
                m.removeBtnY = m.btnRowY;
                m.sellBtnX = m.infoX + m.removeBtnW + btnGap;
                m.sellBtnY = m.btnRowY;
                m.scrollAreaY = m.contentY + 84;
            } else {
                m.removeBtnX = m.infoX;
                m.removeBtnY = m.btnRowY;
                m.removeBtnW = m.infoW;
                m.sellBtnX = m.infoX;
                m.sellBtnY = m.btnRowY + 20;
                m.sellBtnW = m.infoW;
                m.scrollAreaY = m.contentY + 104;
            }
        } else {
            m.removeBtnX = m.infoX;
            m.removeBtnY = m.btnRowY;
            m.removeBtnW = Math.min(110, m.infoW);
            m.sellBtnX = m.infoX;
            m.sellBtnY = m.btnRowY;
            m.sellBtnW = 0;
            m.scrollAreaY = m.contentY + 84;
        }

        m.scrollAreaX = m.infoX;
        m.scrollAreaW = m.infoW;
        m.scrollAreaH = m.contentH - (m.scrollAreaY - m.contentY) - m.innerPadding;

        return m;
    }

    private List<AnimalListItem> buildDisplayItems() {
        List<ZooNetworkPayloads.AnimalSyncData> all = getValidAnimals();
        List<AnimalListItem> items = new ArrayList<>();

        // Any listed animal can be a parent, so offspring that breed again nest one level deeper
        Set<String> listedUuids = new HashSet<>();
        for (var a : all) {
            listedUuids.add(a.uuid());
        }

        Map<String, List<ZooNetworkPayloads.AnimalSyncData>> childrenMap = new LinkedHashMap<>();
        for (var a : all) {
            if (hasListedParent(a, listedUuids)) {
                childrenMap.computeIfAbsent(a.parentUuid(), k -> new ArrayList<>()).add(a);
            }
        }

        Set<String> visited = new HashSet<>();
        for (var a : all) {
            if (!hasListedParent(a, listedUuids)) {
                appendTreeItems(a, 0, childrenMap, visited, items);
            }
        }
        // Animals caught in a parent cycle have no root; list them at the top level
        for (var a : all) {
            if (!visited.contains(a.uuid())) {
                appendTreeItems(a, 0, childrenMap, visited, items);
            }
        }

        return items;
    }

    private static boolean hasListedParent(ZooNetworkPayloads.AnimalSyncData a, Set<String> listedUuids) {
        return a.isOffspring() && !a.parentUuid().isEmpty()
                && !a.parentUuid().equals(a.uuid()) && listedUuids.contains(a.parentUuid());
    }

    private void appendTreeItems(ZooNetworkPayloads.AnimalSyncData node, int depth,
                                 Map<String, List<ZooNetworkPayloads.AnimalSyncData>> childrenMap,
                                 Set<String> visited, List<AnimalListItem> out) {
        if (!visited.add(node.uuid())) return;
        List<ZooNetworkPayloads.AnimalSyncData> kids = childrenMap.get(node.uuid());
        boolean hasKids = kids != null && !kids.isEmpty();
        boolean isExp = hasKids && expandedParents.contains(node.uuid());
        out.add(new AnimalListItem(node, depth, hasKids, isExp, hasKids ? kids.size() : 0));
        if (isExp) {
            for (var kid : kids) {
                appendTreeItems(kid, depth + 1, childrenMap, visited, out);
            }
        } else if (hasKids) {
            // Collapsed descendants still count as placed so they never surface as roots
            markSubtreeVisited(node.uuid(), childrenMap, visited);
        }
    }

    private static void markSubtreeVisited(String uuid, Map<String, List<ZooNetworkPayloads.AnimalSyncData>> childrenMap, Set<String> visited) {
        List<ZooNetworkPayloads.AnimalSyncData> kids = childrenMap.get(uuid);
        if (kids == null) return;
        for (var kid : kids) {
            if (visited.add(kid.uuid())) {
                markSubtreeVisited(kid.uuid(), childrenMap, visited);
            }
        }
    }
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

        boolean hasSell = (selectedData != null && selectedData.isOffspring());
        LayoutMetrics m = computeLayout(width, height, hasSell);

        removeButton = new ZooButton(m.removeBtnX, m.removeBtnY, m.removeBtnW, m.removeBtnH, ZooTranslations.component("zootycoon.ui.animals.btn_remove"), b -> {
            if (selectedData != null) {
                int idToRemove = selectedData.entityId();
                ClientPlayNetworking.send(new ZooNetworkPayloads.UnregisterAnimalPayload(idToRemove));
                ZooTycoonClient.clientAnimals.removeIf(a -> a.entityId() == idToRemove);
                selectedData = null;
                updateSelectedData(true);
            }
        }).setCustomIcon(TRASH_ICON, 12, 12).asDanger();
        screen.addWidgetPublic(removeButton);

        sellButton = new ZooButton(m.sellBtnX, m.sellBtnY, m.sellBtnW, m.sellBtnH, ZooTranslations.component("zootycoon.ui.animals.btn_sell"), b -> {
            if (selectedData != null && selectedData.isOffspring()) {
                int idToSell = selectedData.entityId();
                ClientPlayNetworking.send(new ZooNetworkPayloads.SellAnimalPayload(idToSell));
                ZooTycoonClient.clientAnimals.removeIf(a -> a.entityId() == idToSell);
                selectedData = null;
                updateSelectedData(true);
            }
        }).asEmerald();
        sellButton.visible = hasSell;
        screen.addWidgetPublic(sellButton);

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
                    selectedData.entityId(), selectedData.uuid(), selectedData.entityTypeId(), newName,
                    selectedData.hunger(), selectedData.thirst(), selectedData.cleanliness(), selectedData.health(), selectedData.maxHealth(),
                    selectedData.hasZookeeper(), selectedData.zookeeperName(),
                    selectedData.habitatStatus(), selectedData.habitatNeeds(), selectedData.tempCategory(),
                    selectedData.isOffspring(), selectedData.parentUuid(), selectedData.isSick()
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
        if (sellButton != null) sellButton.visible = false;
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
        if (sellButton != null) {
            sellButton.visible = (selectedData != null && selectedData.isOffspring());
        }
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
            if (sellButton != null && !renameModalOpen) {
                if (selectedData.isOffspring()) {
                    int price = net.zootycoon.economy.ZooEconomyEngine.getAnimalSellPrice(selectedData.entityTypeId());
                    sellButton.setMessage(Component.literal("Jual (" + price + " Emerald)"));
                    sellButton.visible = true;
                } else {
                    sellButton.visible = false;
                }
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
            if (sellButton != null) {
                sellButton.visible = false;
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

        boolean hasSell = (selectedData != null && selectedData.isOffspring());
        LayoutMetrics m = computeLayout(width, height, hasSell);

        // Dynamically update action buttons position and visibility
        if (removeButton != null) {
            removeButton.setX(m.removeBtnX);
            removeButton.setY(m.removeBtnY);
            removeButton.setWidth(m.removeBtnW);
            removeButton.setHeight(m.removeBtnH);
            removeButton.visible = (selectedData != null && !renameModalOpen);
        }
        if (sellButton != null) {
            sellButton.setX(m.sellBtnX);
            sellButton.setY(m.sellBtnY);
            sellButton.setWidth(m.sellBtnW);
            sellButton.setHeight(m.sellBtnH);
            sellButton.visible = (selectedData != null && selectedData.isOffspring() && !renameModalOpen);
        }

        int sidebarX = m.sidebarX;
        int sidebarW = m.sidebarW;
        int sidebarY = m.sidebarY;
        int sidebarH = m.sidebarH;

        int contentX = m.contentX;
        int contentY = m.contentY;
        int contentW = m.contentW;
        int contentH = m.contentH;

        // 1. Sidebar Background & Borders
        graphics.fill(sidebarX, sidebarY, sidebarX + sidebarW, sidebarY + sidebarH, 0xD012151D);
        graphics.fill(sidebarX, sidebarY, sidebarX + sidebarW, sidebarY + 1, 0xFF2A3140);
        graphics.fill(sidebarX, sidebarY + sidebarH - 1, sidebarX + sidebarW, sidebarY + sidebarH, 0xFF2A3140);
        graphics.fill(sidebarX, sidebarY, sidebarX + 1, sidebarY + sidebarH, 0xFF2A3140);
        graphics.fill(sidebarX + sidebarW - 1, sidebarY, sidebarX + sidebarW, sidebarY + sidebarH, 0xFF2A3140);

        List<AnimalListItem> displayItems = buildDisplayItems();
        int totalContentH = displayItems.size() * ITEM_HEIGHT + 8;
        int maxScroll = Math.max(0, totalContentH - sidebarH);
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

        List<Component> hoveredSidebarProblems = null;

        graphics.enableScissor(sidebarX + 1, sidebarY + 1, sidebarX + sidebarW - 1, sidebarY + sidebarH - 1);

        int itemRightMargin = maxScroll > 0 ? 8 : 3;
        int curY = sidebarY + 4 - scrollOffset;
        renderedArrowX.clear();
        for (var item : displayItems) {
            var a = item.data;
            if (curY + ITEM_HEIGHT >= sidebarY && curY <= sidebarY + sidebarH) {
                boolean isSelected = (selectedData != null && selectedData.entityId() == a.entityId());
                int itemBg = isSelected ? 0xFF243048 : 0xFF181C26;
                int indent = treeIndent(item);
                int cardRight = sidebarX + sidebarW - itemRightMargin;

                graphics.fill(sidebarX + 3 + indent, curY, cardRight, curY + ITEM_HEIGHT - 2, itemBg);
                if (isSelected) {
                    graphics.fill(sidebarX + 3 + indent, curY, sidebarX + 5 + indent, curY + ITEM_HEIGHT - 2, 0xFF58A6FF);
                }

                if (item.isChild) {
                    graphics.drawString(font, Component.literal("\u2514"), sidebarX + 3 + indent - 9, curY + 9, 0xFF8B949E, false);
                }

                // 1. Check problem conditions: no zookeeper OR stats / habitat problem
                boolean hasNoZk = !a.hasZookeeper();
                boolean isHpLow = (a.health() / Math.max(0.1f, a.maxHealth())) <= 0.5f;
                boolean isHungry = a.hunger() <= 50;
                boolean isThirsty = a.thirst() <= 50;
                boolean isDirty = a.cleanliness() <= 50
                        && !net.zootycoon.animal.ZooAnimalHungerManager.isPureAquatic(a.entityTypeId());
                var habParsed = ZooAnimalHologramRenderer.HabitatParsedInfo.parse(a.habitatNeeds(), a.habitatStatus());
                boolean isTempProblem = "HEATER".equalsIgnoreCase(habParsed.tempStatus()) || "COOLER".equalsIgnoreCase(habParsed.tempStatus());
                boolean isWaterMissing = "NO".equalsIgnoreCase(habParsed.waterStatus());
                boolean hasNonTempUnmet = habParsed.serverUnmet().stream()
                        .anyMatch(u -> !u.startsWith("Butuh Heater") && !u.startsWith("Butuh Cooler Box"));
                boolean isHabProblem = hasNonTempUnmet || isWaterMissing
                        || !habParsed.groundOk() || !habParsed.biomeOk();
                boolean hasProblem = hasNoZk || isHpLow || isHungry || isThirsty || isDirty || isTempProblem || isHabProblem;

                // 2. Mini 3D Animal Model (di samping kiri nama satwa)
                int modelSize = 22;
                int modelX = sidebarX + 6 + indent;
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
                int textX = sidebarX + 32 + indent;
                int textY = curY + 9;
                int exX = cardRight - 14;
                int exY = curY + 8;
                int arrowX = cardRight - (hasProblem ? 24 : 12);
                int maxNameW = Math.max(20, (item.hasChildren ? arrowX - 4 : (hasProblem ? exX - 4 : cardRight - 4)) - textX);
                String trimmedName = font.plainSubstrByWidth(displayName, maxNameW);
                graphics.drawString(font, Component.literal(trimmedName).withStyle(ChatFormatting.WHITE), textX, textY, 0xFFFFFFFF, false);

                // Dropdown indicator arrow for parent
                if (item.hasChildren) {
                    renderedArrowX.put(a.uuid(), arrowX);
                    String arrow = item.isExpanded ? "\u25BC" : "\u25B6";
                    graphics.drawString(font, Component.literal(arrow), arrowX, curY + 8, item.isExpanded ? 0xFF58A6FF : 0xFF8B949E, false);
                }

                // 4. Alert Exclamation Mark (align kanan)
                if (hasProblem) {
                    // Scale the whole 64x64 texture into a 10x10 icon
                    graphics.blit(EXCLAMATION_ICON, exX, exY, 10, 10, 0, 0, 64, 64, 64, 64);
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
                        if (isHabProblem) {
                            problems.add(Component.literal("• Kebutuhan habitat belum terpenuhi:").withStyle(ChatFormatting.RED));
                            List<String> unmetLines = new ArrayList<>();
                            for (String need : habParsed.serverUnmet()) {
                                // Temperature already has its own line above
                                if (need.startsWith("Butuh Heater") || need.startsWith("Butuh Cooler Box")) continue;
                                unmetLines.add(need);
                            }
                            if (unmetLines.isEmpty()) {
                                if (isWaterMissing) unmetLines.add("Butuh Air");
                                if (!habParsed.groundOk()) unmetLines.add("Jenis tanah belum sesuai");
                                if (!habParsed.biomeOk()) unmetLines.add("Bioma belum sesuai");
                            }
                            for (String line : unmetLines) {
                                problems.add(Component.literal("  - " + line).withStyle(ChatFormatting.GRAY));
                            }
                        }
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

        int previewW = m.previewW;
        int previewH = m.previewH;
        int previewX = m.previewX;
        int previewY = m.previewY;

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
                float baseScaleFactor = previewW * 0.585f;
                int minScale = (int) (25 * (previewW / 140.0f));
                int maxScale = (int) (78 * (previewW / 140.0f));
                int scale = (int) Math.clamp(baseScaleFactor / maxDim, minScale, maxScale);
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics,
                        previewX, previewY, previewX + previewW, previewY + previewH,
                        scale, 0.0f, (float) mouseX, (float) mouseY, previewEntity
                );
            } catch (Throwable t) {
                graphics.drawCenteredString(font, ZooTranslations.component("zootycoon.ui.animals.preview_fallback").withStyle(ChatFormatting.DARK_GRAY), previewX + previewW / 2, previewY + previewH / 2, 0xFF888888);
            }
        }

        int infoX = m.infoX;
        int infoW = m.infoW;

        // 1. Animal Name & Pencil Icon
        String displayName = selectedData.customName().isEmpty() ? formatSpecies(selectedData.entityTypeId()) : selectedData.customName();
        int maxNameW = Math.max(20, infoW - 18);
        String trimmedName = font.plainSubstrByWidth(displayName, maxNameW);
        graphics.drawString(font, Component.literal(trimmedName).withStyle(ChatFormatting.WHITE), infoX, contentY + 14, 0xFFFFFFFF, false);

        int nameW = font.width(trimmedName);
        int pencilX = infoX + nameW + 5;
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
        String trimmedDiet = font.plainSubstrByWidth(dietComp.getString(), infoW);
        graphics.drawString(font, Component.literal(trimmedDiet).withStyle(dietComp.getStyle()), infoX, dietY, 0xFFFFFFFF, false);

        // 3. Zookeeper Dropdown Selector Button
        int zkBtnX = m.zkBtnX;
        int zkBtnY = m.zkBtnY;
        int zkBtnW = m.zkBtnW;
        int zkBtnH = m.zkBtnH;

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

        int maxTextW = zkBtnW - 20;
        String trimmedLabel = font.plainSubstrByWidth(keeperLabel, maxTextW);
        graphics.drawString(font, Component.literal(trimmedLabel).withStyle(keeperColor), zkBtnX + 6, zkBtnY + 5, 0xFFFFFFFF, false);

        String arrow = zookeeperDropdownOpen ? "▲" : "▼";
        graphics.drawString(font, Component.literal(arrow).withStyle(ChatFormatting.GRAY), zkBtnX + zkBtnW - 12, zkBtnY + 5, 0xFFFFFFFF, false);

        if (!selectedData.hasZookeeper()) {
            int exX = zkBtnX + zkBtnW + 4;
            int exY = zkBtnY + 4;
            graphics.blit(EXCLAMATION_ICON, exX, exY, 10, 10, 0, 0, 64, 64, 64, 64);
            if (!renameModalOpen && !zookeeperDropdownOpen && mouseX >= exX && mouseX <= exX + 10 && mouseY >= exY && mouseY <= exY + 10) {
                graphics.renderTooltip(font, ZooTranslations.component("zootycoon.ui.animals.no_zookeeper_tooltip").withStyle(ChatFormatting.RED), mouseX, mouseY);
            }
        }

        int scrollAreaX = m.scrollAreaX;
        int scrollAreaY = m.scrollAreaY;
        int scrollAreaW = m.scrollAreaW;
        int scrollAreaH = m.scrollAreaH;

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

        // 1. Health (Normalized 10 hearts proportional to health/maxHealth so 100% fills all 10 hearts)
        int healthY = scrollAreaY + relY - detailsScrollOffset;
        float maxHpVal = Math.max(0.1f, selectedData.maxHealth());
        float hpRatio = selectedData.health() / maxHpVal;
        int filledHalfHearts = calculateHalfUnits(selectedData.health(), maxHpVal);
        drawHalfUnitIconBar(graphics, infoX, healthY, filledHalfHearts, VANILLA_HEART_CONTAINER, VANILLA_HEART_FULL, VANILLA_HEART_HALF);
        int statTextX = infoX + 104;
        int hpPct = Math.round(hpRatio * 100.0f);
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
        int filledHalfShanks = calculateHalfUnits((float) selectedData.hunger(), 100.0f);
        drawHalfUnitIconBar(graphics, infoX, hungerY, filledHalfShanks, VANILLA_FOOD_EMPTY, VANILLA_FOOD_FULL, VANILLA_FOOD_HALF);
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
            String heaterStr = (infoW < 160) ? "(Heater)" : "(Butuh Heater)";
            graphics.drawString(font, Component.literal(heaterStr).withStyle(ChatFormatting.RED), infoX + 13 + tempTextW, tempY + 1, 0xFFFF5555, false);
            int labelW = font.width(heaterStr + " ");
            graphics.blit(TEMP_COLD_ICON, infoX + 13 + tempTextW + labelW, tempY, 9, 9, 0, 0, 16, 16, 16, 16);
        } else if ("COOLER".equalsIgnoreCase(habParsed.tempStatus())) {
            String coolerStr = (infoW < 160) ? "(Cooler)" : "(Butuh Cooler Box)";
            graphics.drawString(font, Component.literal(coolerStr).withStyle(ChatFormatting.RED), infoX + 13 + tempTextW, tempY + 1, 0xFFFF5555, false);
            int labelW = font.width(coolerStr + " ");
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
                String trimmedNeed = font.plainSubstrByWidth(need.getString(), infoW - 8);
                graphics.drawString(font, Component.literal(trimmedNeed).withStyle(need.getStyle()), infoX + 4, lineY, 0xFFFF5555, false);
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

    /**
     * Draws a 10-slot stat bar where each icon represents 2 half-units (0-20 total),
     * keeping Health and Hunger bars uniformly scaled to 0-100%.
     */
    public static int calculateHalfUnits(float value, float maxValue) {
        return Math.clamp(Math.round((value / Math.max(0.1f, maxValue)) * 20.0f), 0, 20);
    }

    private static void drawHalfUnitIconBar(
            GuiGraphics graphics,
            int startX,
            int y,
            int filledHalves,
            ResourceLocation emptySprite,
            ResourceLocation fullSprite,
            ResourceLocation halfSprite
    ) {
        for (int i = 0; i < 10; i++) {
            int iconX = startX + i * 10;
            graphics.blitSprite(emptySprite, iconX, y, 9, 9);
            int halfRemaining = filledHalves - (i * 2);
            if (halfRemaining >= 2) {
                graphics.blitSprite(fullSprite, iconX, y, 9, 9);
            } else if (halfRemaining == 1) {
                graphics.blitSprite(halfSprite, iconX, y, 9, 9);
            }
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
        if (rawId == null || rawId.isEmpty()) return "";
        LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(rawId);
        if (entry != null) {
            return entry.getDisplayName();
        }
        if (!rawId.contains(":")) return rawId;
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

            boolean assignedToDifferentSpecies = ZooTycoonClient.clientAnimals.stream().anyMatch(
                    a -> a.entityId() != selectedData.entityId()
                            && a.hasZookeeper()
                            && a.zookeeperName().equals(zk.name())
                            && !a.entityTypeId().equalsIgnoreCase(selectedData.entityTypeId())
            );

            if (!assignedToThisAnimal && !assignedToDifferentSpecies) {
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

        boolean hasSell = (selectedData != null && selectedData.isOffspring());
        LayoutMetrics m = computeLayout(width, height, hasSell);

        int sidebarX = m.sidebarX;
        int sidebarW = m.sidebarW;
        int sidebarY = m.sidebarY;
        int sidebarH = m.sidebarH;

        int contentX = m.contentX;
        int contentY = m.contentY;
        int contentW = m.contentW;
        int contentH = m.contentH;

        int previewX = m.previewX;
        int previewW = m.previewW;
        int infoX = m.infoX;
        int infoW = m.infoW;
        int zkBtnX = m.zkBtnX;
        int zkBtnY = m.zkBtnY;
        int zkBtnW = m.zkBtnW;
        int zkBtnH = m.zkBtnH;

        // Pencil Icon clicked -> open rename modal
        if (selectedData != null && !zookeeperDropdownOpen) {
            String displayName = selectedData.customName().isEmpty() ? formatSpecies(selectedData.entityTypeId()) : selectedData.customName();
            int maxNameW = Math.max(20, infoW - 18);
            String trimmedName = screen.getFont().plainSubstrByWidth(displayName, maxNameW);
            int nameW = screen.getFont().width(trimmedName);
            int pencilX = infoX + nameW + 5;
            int pencilY = contentY + 12;
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

        // Check if clicked sidebar scrollbar thumb/track
        if (mouseX >= sidebarX + sidebarW - 8 && mouseX <= sidebarX + sidebarW && mouseY >= sidebarY && mouseY <= sidebarY + sidebarH) {
            List<AnimalListItem> displayItems = buildDisplayItems();
            int totalContentH = displayItems.size() * ITEM_HEIGHT + 8;
            int maxScroll = Math.max(0, totalContentH - sidebarH);
            if (maxScroll > 0) {
                this.isDraggingSidebarScrollbar = true;
                double progress = Math.clamp((mouseY - sidebarY) / (double) sidebarH, 0.0, 1.0);
                this.scrollOffset = (int) (progress * maxScroll);
                return true;
            }
        }

        if (mouseX >= sidebarX && mouseX <= sidebarX + sidebarW && mouseY >= sidebarY && mouseY <= sidebarY + sidebarH) {
            List<AnimalListItem> displayItems = buildDisplayItems();
            double relativeY = (mouseY - (sidebarY + 4)) + scrollOffset;
            if (relativeY >= 0) {
                int clickedIdx = (int) (relativeY / ITEM_HEIGHT);
                if (clickedIdx >= 0 && clickedIdx < displayItems.size()) {
                    var item = displayItems.get(clickedIdx);
                    Integer arrowX = renderedArrowX.get(item.data.uuid());

                    // If clicked on dropdown arrow area of parent (any tree level)
                    if (item.hasChildren && arrowX != null && mouseX >= arrowX - 4 && mouseX < arrowX + 9) {
                        if (expandedParents.contains(item.data.uuid())) {
                            expandedParents.remove(item.data.uuid());
                        } else {
                            expandedParents.add(item.data.uuid());
                        }
                        return true;
                    }

                    this.selectedData = item.data;
                    this.lastSelectedEntityId = selectedData.entityId();
                    this.detailsScrollOffset = 0;
                    this.zookeeperDropdownOpen = false;
                    if (item.hasChildren) {
                        expandedParents.add(item.data.uuid());
                    }
                    closeRenameModal();
                    return true;
                }
            }
        }

        if (selectedData != null) {
            int scrollAreaY = m.scrollAreaY;
            int scrollAreaH = m.scrollAreaH;

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

        boolean hasSell = (selectedData != null && selectedData.isOffspring());
        LayoutMetrics m = computeLayout(screen.width, screen.height, hasSell);

        if (zookeeperDropdownOpen && selectedData != null) {
            int dx = m.zkBtnX;
            int dy = m.zkBtnY + 18 + 2;
            if (dy + zkDropdownH > screen.height - 10) {
                dy = Math.max(10, m.zkBtnY - zkDropdownH - 2);
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

        if (mouseX >= m.sidebarX && mouseX <= m.sidebarX + m.sidebarW && mouseY >= m.sidebarY && mouseY <= m.sidebarY + m.sidebarH) {
            List<AnimalListItem> displayItems = buildDisplayItems();
            int totalContentH = displayItems.size() * ITEM_HEIGHT + 8;
            int maxScroll = Math.max(0, totalContentH - m.sidebarH);
            if (maxScroll > 0) {
                this.scrollOffset = Math.clamp(this.scrollOffset - (int) (verticalAmount * 24), 0, maxScroll);
                return true;
            }
        }

        if (mouseX >= m.contentX && mouseX <= m.contentX + m.contentW && mouseY >= m.contentY && mouseY <= m.contentY + m.contentH) {
            if (maxDetailsScroll > 0) {
                this.detailsScrollOffset = Math.clamp(this.detailsScrollOffset - (int) (verticalAmount * 20), 0, maxDetailsScroll);
                return true;
            }
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (renameModalOpen) return true;

        boolean hasSell = (selectedData != null && selectedData.isOffspring());
        LayoutMetrics m = computeLayout(screen.width, screen.height, hasSell);

        if (isDraggingSidebarScrollbar) {
            List<AnimalListItem> displayItems = buildDisplayItems();
            int totalContentH = displayItems.size() * ITEM_HEIGHT + 8;
            int maxScroll = Math.max(0, totalContentH - m.sidebarH);
            if (maxScroll > 0 && m.sidebarH > 0) {
                double progress = Math.clamp((mouseY - m.sidebarY) / (double) m.sidebarH, 0.0, 1.0);
                this.scrollOffset = (int) (progress * maxScroll);
                return true;
            }
        }

        if (isDraggingDetailsScrollbar && selectedData != null) {
            int scrollAreaY = m.scrollAreaY;
            int scrollAreaH = m.scrollAreaH;
            int infoW = m.infoW;

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
