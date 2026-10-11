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
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalManagementTab {

    private static final ResourceLocation EXCLAMATION_ICON = ZooTycoonMod.id("textures/gui/icons/exclamation_point.png");
    private static final ResourceLocation TRASH_ICON = ZooTycoonMod.id("textures/gui/icons/trash_can.png");
    private static final ResourceLocation PENCIL_ICON = ZooTycoonMod.id("textures/gui/icons/pencil.png");

    // Vanilla HUD sprites for authentic health & hunger rendering
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

    private int scrollOffset = 0;
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

        // Refresh button on top of sidebar
        refreshButton = new ZooButton(sidebarX, 48, sidebarW, 20, Component.literal("Segarkan Satwa"), b -> {
            requestSync();
        });
        screen.addWidgetPublic(refreshButton);

        // Precise geometry matching render()
        int previewX = contentX + 14;
        int previewW = 140;
        int infoX = previewX + previewW + 16;
        int infoW = contentW - (infoX - contentX) - 14;

        // EditBox for naming
        int inputY = contentY + 50;
        nameInput = new EditBox(screen.getFont(), infoX, inputY, infoW, 20, Component.literal("Nama Satwa"));
        nameInput.setMaxLength(32);
        screen.addWidgetPublic(nameInput);

        // Action buttons side-by-side
        int btnY = inputY + 24;
        int halfW = (infoW - 6) / 2;

        renameButton = new ZooButton(infoX, btnY, halfW, 20, Component.literal("Simpan"), b -> {
            if (selectedData != null && nameInput != null && !nameInput.getValue().trim().isEmpty()) {
                String newName = nameInput.getValue().trim();
                ClientPlayNetworking.send(new ZooNetworkPayloads.RenameAnimalPayload(selectedData.entityId(), newName));
                selectedData = new ZooNetworkPayloads.AnimalSyncData(
                        selectedData.entityId(), selectedData.entityTypeId(), newName,
                        selectedData.hunger(), selectedData.health(), selectedData.maxHealth(),
                        selectedData.hasZookeeper(), selectedData.zookeeperName()
                );
            }
        }).setCustomIcon(PENCIL_ICON, 12, 12).asPrimary();
        screen.addWidgetPublic(renameButton);

        removeButton = new ZooButton(infoX + halfW + 6, btnY, halfW, 20, Component.literal("Hapus Satwa"), b -> {
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

        // Enable scissor clipping inside sidebar
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

                // Health mini row
                graphics.blitSprite(VANILLA_HEART_FULL, sidebarX + 8, curY + 20, 9, 9);
                int hp = (int) Math.ceil(a.health());
                int maxHp = (int) Math.ceil(a.maxHealth());
                graphics.drawString(font, Component.literal(hp + "/" + maxHp), sidebarX + 20, curY + 21, 0xFFE06C75, false);

                // Hunger mini row
                graphics.blitSprite(VANILLA_FOOD_FULL, sidebarX + 68, curY + 20, 9, 9);
                int tagColor = a.hunger() > 50 ? 0xFF2ECC71 : (a.hunger() > 20 ? 0xFFF1C40F : 0xFFE74C3C);
                graphics.drawString(font, Component.literal(a.hunger() + "%"), sidebarX + 80, curY + 21, tagColor, false);

                // Top-right indicator badges
                int cardRight = sidebarX + sidebarW - itemRightMargin;
                int badgeX = cardRight - 13;
                int badgeY = curY + 5;

                if (!a.hasZookeeper()) {
                    // Exclamation mark for missing zookeeper
                    graphics.blit(EXCLAMATION_ICON, badgeX, badgeY, 0, 0, 9, 16, 9, 16);
                    if (mouseX >= badgeX && mouseX <= badgeX + 9 && mouseY >= badgeY && mouseY <= badgeY + 16) {
                        graphics.renderTooltip(font, Component.literal("Belum Ada Zookeeper!").withStyle(ChatFormatting.RED), mouseX, mouseY);
                    }
                    badgeX -= 13;
                }

                if (a.hunger() <= 50) {
                    // Hunger alert icon
                    graphics.blitSprite(VANILLA_FOOD_FULL, badgeX, badgeY + 3, 9, 9);
                    if (mouseX >= badgeX && mouseX <= badgeX + 9 && mouseY >= badgeY + 3 && mouseY <= badgeY + 12) {
                        graphics.renderTooltip(font, Component.literal("Satwa Sedang Lapar (" + a.hunger() + "%)").withStyle(ChatFormatting.GOLD), mouseX, mouseY);
                    }
                }
            }
            curY += ITEM_HEIGHT;
        }

        graphics.disableScissor();

        // Render sleek scrollbar if scrollable
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
            String empty1 = "Belum Ada Satwa Terdeteksi di Tanah Zoo Anda";
            String empty2 = "Gunakan Spyglass untuk memindai satwa liar agar terdaftar di Zoo,";
            String empty3 = "lalu rawat dan kelola kebutuhannya di sini!";
            graphics.drawCenteredString(font, Component.literal(empty1).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), contentX + contentW / 2, contentY + 50, 0xFFFFFFFF);
            graphics.drawCenteredString(font, Component.literal(empty2), contentX + contentW / 2, contentY + 70, 0xFFAAAAAA);
            graphics.drawCenteredString(font, Component.literal(empty3), contentX + contentW / 2, contentY + 84, 0xFFAAAAAA);
            return;
        }

        // Resolve LivingEntity for 3D Preview
        LivingEntity previewEntity = resolveLivingEntity(mc, selectedData);

        int previewW = 140;
        int previewH = 140;
        int previewX = contentX + 14;
        int previewY = contentY + 16;

        // 3D Preview Frame
        graphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0xFF141720);
        graphics.fill(previewX, previewY, previewX + previewW, previewY + 1, 0xFF242A38);
        graphics.fill(previewX, previewY + previewH - 1, previewX + previewW, previewY + previewH, 0xFF242A38);
        graphics.fill(previewX, previewY, previewX + 1, previewY + previewH, 0xFF242A38);
        graphics.fill(previewX + previewW - 1, previewY, previewX + previewW, previewY + previewH, 0xFF242A38);

        if (previewEntity != null) {
            ensureEntityId(previewEntity, selectedData.entityId());
            try {
                float bbH = Math.max(0.6f, previewEntity.getBbHeight());
                int scale = (int) Math.clamp(46.0f / bbH, 14, 52);
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics,
                        previewX, previewY, previewX + previewW, previewY + previewH,
                        scale, 0.0f, (float) mouseX, (float) mouseY, previewEntity
                );
            } catch (Throwable t) {
                ZooTycoonMod.LOGGER.warn("Could not render 3D preview for entity: " + selectedData.entityTypeId(), t);
                graphics.drawCenteredString(font, Component.literal("[3D Preview]").withStyle(ChatFormatting.DARK_GRAY), previewX + previewW / 2, previewY + previewH / 2, 0xFF888888);
            }
        }

        // Details Section (Strict Non-Overlapping Layout)
        int infoX = previewX + previewW + 16;
        int infoW = contentW - (infoX - contentX) - 14;

        // Y = contentY + 14 (62): Species Title
        String rawSpecies = formatSpecies(selectedData.entityTypeId());
        graphics.drawString(font, Component.literal(rawSpecies).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), infoX, contentY + 14, 0xFFFFFFFF, false);
        if (!selectedData.hasZookeeper()) {
            int titleW = font.width(rawSpecies);
            int exX = infoX + titleW + 6;
            int exY = contentY + 11;
            graphics.blit(EXCLAMATION_ICON, exX, exY, 0, 0, 9, 16, 9, 16);
            if (mouseX >= exX && mouseX <= exX + 9 && mouseY >= exY && mouseY <= exY + 16) {
                graphics.renderTooltip(font, Component.literal("Belum Ada Zookeeper Ditugaskan!").withStyle(ChatFormatting.RED), mouseX, mouseY);
            }
        }

        // Y = contentY + 26 (74): Subtitle if Zookeeper exists (No text when missing)
        if (selectedData.hasZookeeper()) {
            graphics.drawString(font, Component.literal("Zookeeper: " + selectedData.zookeeperName()).withStyle(ChatFormatting.GREEN), infoX, contentY + 26, 0xFFFFFFFF, false);
        }

        // Y = contentY + 38 (86): Tag Label
        graphics.drawString(font, Component.literal("Tag Nama:").withStyle(ChatFormatting.GRAY), infoX, contentY + 38, 0xFFFFFFFF, false);

        // (nameInput is at Y = contentY + 50 to 70)
        // (renameButton and removeButton are at Y = contentY + 74 to 94)

        // Status Satwa Section (Authentic Vanilla Health & Hunger Bars)
        int statusY = contentY + 102;

        // 1. Health Status: Vanilla Hearts based on THIS mob's individual max health (1 heart = 2 HP)
        int totalHearts = Math.max(1, (int) Math.ceil(selectedData.maxHealth() / 2.0f));
        int heartsToDraw = Math.min(totalHearts, 20); // Cap at 20 hearts (2 rows) to keep UI clean
        int rowsOfHearts = (int) Math.ceil(heartsToDraw / 10.0);

        for (int i = 0; i < heartsToDraw; i++) {
            int row = i / 10;
            int col = i % 10;
            int hX = infoX + col * 10;
            int hY = statusY + row * 11;

            // Draw empty container underneath
            graphics.blitSprite(VANILLA_HEART_CONTAINER, hX, hY, 9, 9);

            float hpForHeart = selectedData.health() - (i * 2.0f);
            if (hpForHeart >= 1.5f) {
                graphics.blitSprite(VANILLA_HEART_FULL, hX, hY, 9, 9);
            } else if (hpForHeart >= 0.5f) {
                graphics.blitSprite(VANILLA_HEART_HALF, hX, hY, 9, 9);
            }
        }
        int hp = (int) Math.ceil(selectedData.health());
        int maxHp = (int) Math.ceil(selectedData.maxHealth());
        int hpTextX = infoX + (Math.min(heartsToDraw, 10) * 10) + 6;
        graphics.drawString(font, Component.literal(hp + "/" + maxHp + " HP").withStyle(ChatFormatting.RED), hpTextX, statusY + 1, 0xFFFFFFFF, false);

        // 2. Hunger Status: 10 Vanilla Food Drumsticks (with halves) + Numeric %
        int hungerY = statusY + (rowsOfHearts * 11) + 3;
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
        ChatFormatting hungerStyle = selectedData.hunger() > 70 ? ChatFormatting.GREEN : (selectedData.hunger() > 30 ? ChatFormatting.YELLOW : ChatFormatting.RED);
        graphics.drawString(font, Component.literal(selectedData.hunger() + "%").withStyle(hungerStyle), infoX + 106, hungerY + 1, 0xFFFFFFFF, false);

        // 3. Grid of Accepted Food Items
        int gridY = hungerY + 16;
        graphics.drawString(font, Component.literal("Makanan yang Disukai:").withStyle(ChatFormatting.YELLOW), infoX, gridY, 0xFFFFFFFF, false);

        if (previewEntity != null) {
            var diet = AnimalDietRegistry.getDiet(previewEntity);
            List<Item> foods = diet.acceptedFoods;
            int slotSize = 22;
            int gap = 4;
            int slotStartY = gridY + 12;

            for (int i = 0; i < foods.size(); i++) {
                Item foodItem = foods.get(i);
                int slotX = infoX + i * (slotSize + gap);

                boolean slotHovered = mouseX >= slotX && mouseX <= slotX + slotSize && mouseY >= slotStartY && mouseY <= slotStartY + slotSize;
                int slotBg = slotHovered ? 0xFF1E2433 : 0xFF141720;
                int slotBorder = slotHovered ? 0xFF58A6FF : 0xFF2A3140;

                graphics.fill(slotX, slotStartY, slotX + slotSize, slotStartY + slotSize, slotBg);
                graphics.fill(slotX, slotStartY, slotX + slotSize, slotStartY + 1, slotBorder);
                graphics.fill(slotX, slotStartY + slotSize - 1, slotX + slotSize, slotStartY + slotSize, slotBorder);
                graphics.fill(slotX, slotStartY, slotX + 1, slotStartY + slotSize, slotBorder);
                graphics.fill(slotX + slotSize - 1, slotStartY, slotX + slotSize, slotStartY + slotSize, slotBorder);

                ItemStack foodStack = new ItemStack(foodItem);
                graphics.renderItem(foodStack, slotX + 3, slotStartY + 3);

                if (slotHovered) {
                    graphics.renderTooltip(font, foodStack.getHoverName(), mouseX, mouseY);
                }
            }
        }
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

        // Dummy cache fallback
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

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int sidebarX = 14;
        int sidebarW = 160;
        int sidebarY = 72;
        int sidebarH = screen.height - sidebarY - 14;

        // 1. Sidebar Animal Selection Click
        if (mouseX >= sidebarX && mouseX <= sidebarX + sidebarW && mouseY >= sidebarY && mouseY <= sidebarY + sidebarH) {
            List<ZooNetworkPayloads.AnimalSyncData> animals = getValidAnimals();
            double relativeY = (mouseY - (sidebarY + 4)) + scrollOffset;
            if (relativeY >= 0) {
                int clickedIdx = (int) (relativeY / ITEM_HEIGHT);
                if (clickedIdx >= 0 && clickedIdx < animals.size()) {
                    this.selectedData = animals.get(clickedIdx);
                    this.lastSelectedEntityId = selectedData.entityId();
                    if (nameInput != null) {
                        nameInput.setValue(selectedData.customName());
                    }
                    return true;
                }
            }
        }

        // 2. Direct Click on Name Input field -> set focus
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

        // 3. Click on Food Slots to feed animal if holding matching item
        if (selectedData != null) {
            Minecraft mc = Minecraft.getInstance();
            LivingEntity previewEntity = resolveLivingEntity(mc, selectedData);
            if (previewEntity != null) {
                var diet = AnimalDietRegistry.getDiet(previewEntity);
                List<Item> foods = diet.acceptedFoods;
                int previewW = 140;
                int contentX = sidebarX + sidebarW + 16;
                int infoX = contentX + 14 + previewW + 16;
                int slotStartY = 48 + 102 + 14 + 16 + 12; // Matches gridY + 12 in render
                int slotSize = 22;
                int gap = 4;

                for (int i = 0; i < foods.size(); i++) {
                    int slotX = infoX + i * (slotSize + gap);
                    if (mouseX >= slotX && mouseX <= slotX + slotSize && mouseY >= slotStartY && mouseY <= slotStartY + slotSize) {
                        // Attempt to feed from GUI
                        ClientPlayNetworking.send(new ZooNetworkPayloads.FeedAnimalFromGuiPayload(selectedData.entityId()));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
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
        return false;
    }
}
