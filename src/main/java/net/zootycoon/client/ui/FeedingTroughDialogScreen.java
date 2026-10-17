package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedingTroughDialogScreen extends Screen {

    private final BlockPos pos;
    private int assignedAnimalId;
    private String assignedAnimalUuid;
    private String assignedAnimalName;
    private String assignedAnimalSpecies;
    private String assignedFoodItemId;
    private int foodCount;
    private String foodItemKey;
    private int waterLevel;

    private final int panelWidth = 320;
    private final int panelHeight = 224;
    private int startX;
    private int startY;

    private List<ZooNetworkPayloads.AnimalSyncData> availableAnimals = List.of();
    private int scrollOffset = 0;

    private static final Map<String, LivingEntity> dummyAnimalCache = new HashMap<>();

    public FeedingTroughDialogScreen(
            BlockPos pos,
            int assignedAnimalId,
            String assignedAnimalUuid,
            String assignedAnimalName,
            String assignedAnimalSpecies,
            String assignedFoodItemId,
            int foodCount,
            String foodItemKey,
            int waterLevel
    ) {
        super(Component.literal("Tempat Pakan"));
        this.pos = pos;
        this.assignedAnimalId = assignedAnimalId;
        this.assignedAnimalUuid = assignedAnimalUuid;
        this.assignedAnimalName = assignedAnimalName;
        this.assignedAnimalSpecies = assignedAnimalSpecies;
        this.assignedFoodItemId = assignedFoodItemId;
        this.foodCount = foodCount;
        this.foodItemKey = foodItemKey;
        this.waterLevel = waterLevel;
    }

    @Override
    protected void init() {
        super.init();
        this.startX = (this.width - panelWidth) / 2;
        this.startY = (this.height - panelHeight) / 2;

        this.availableAnimals = ZooTycoonClient.clientAnimals.stream()
                .filter(a -> {
                    String id = a.entityTypeId().toLowerCase();
                    return !id.equals("minecraft:bat") && !id.endsWith("_bat") && !id.contains(":bat");
                })
                .toList();

        // Top action buttons if assigned
        if (isAssigned()) {
            ZooButton depositBtn = new ZooButton(startX + panelWidth - 110, startY + 36, 98, 18,
                    Component.literal("Isi Inventory"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.DepositTroughFoodPayload(this.pos));
                this.onClose();
            }).asPrimary();
            addRenderableWidget(depositBtn);

            ZooButton clearBtn = new ZooButton(startX + panelWidth - 110, startY + 56, 98, 18,
                    Component.literal("Lepas Satwa"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.ClearFeedingTroughAnimalPayload(this.pos));
                this.assignedAnimalUuid = "";
                this.assignedAnimalId = -1;
                this.assignedAnimalName = "";
                this.assignedAnimalSpecies = "";
                this.assignedFoodItemId = "";
                this.clearWidgets();
                this.init();
            }).asSecondary();
            addRenderableWidget(clearBtn);
        }

        // Bottom close button
        ZooButton closeBtn = new ZooButton(startX + 12, startY + panelHeight - 26, panelWidth - 24, 20,
                Component.literal("Tutup"), b -> this.onClose()).asSecondary();
        addRenderableWidget(closeBtn);
    }

    private boolean isAssigned() {
        return (assignedAnimalUuid != null && !assignedAnimalUuid.isEmpty())
                || (assignedAnimalSpecies != null && !assignedAnimalSpecies.isEmpty());
    }

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

    private static String formatSpecies(String entityTypeId) {
        if (entityTypeId == null || entityTypeId.isEmpty()) return "-";
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

    private static String getFoodDisplayName(String foodKey) {
        if (foodKey == null || foodKey.isEmpty()) return "Gandum";
        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(foodKey));
            if (item != null && item != Items.AIR) {
                return item.getDescription().getString();
            }
        } catch (Exception ignored) {}
        return formatSpecies(foodKey);
    }

    private static Item getPrimaryFood(String entityTypeId) {
        var diet = AnimalDietRegistry.getDiet(entityTypeId);
        if (diet != null && !diet.acceptedFoods.isEmpty()) {
            return diet.acceptedFoods.get(0);
        }
        return Items.WHEAT;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        // Panel background
        graphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xF0181F2A);
        graphics.renderOutline(startX, startY, panelWidth, panelHeight, 0xFF38BDF8);

        // Header Title
        String title = "Tempat Pakan (Feeding Trough)";
        graphics.drawString(this.font, Component.literal(title).withStyle(ChatFormatting.GOLD),
                startX + 14, startY + 12, 0xFFFFAA00, false);
        graphics.drawString(this.font, Component.literal("(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")").withStyle(ChatFormatting.GRAY),
                startX + 14, startY + 22, 0xFFAAAAAA, false);

        // Status Card
        int cardY = startY + 34;
        int cardH = 44;
        graphics.fill(startX + 12, cardY, startX + panelWidth - 12, cardY + cardH, 0xFF111827);
        graphics.renderOutline(startX + 12, cardY, panelWidth - 24, cardH, 0xFF374151);

        if (isAssigned()) {
            String anName = assignedAnimalName != null && !assignedAnimalName.isEmpty() ? assignedAnimalName : formatSpecies(assignedAnimalSpecies);
            graphics.drawString(this.font, Component.literal("Satwa: " + anName).withStyle(ChatFormatting.AQUA),
                    startX + 18, cardY + 6, 0xFF55FFFF, false);

            String foodName = getFoodDisplayName(assignedFoodItemId);
            graphics.drawString(this.font, Component.literal("Pakan: " + foodName).withStyle(ChatFormatting.YELLOW),
                    startX + 18, cardY + 18, 0xFFFFFF55, false);

            graphics.drawString(this.font, Component.literal("Isi: " + foodCount + "/64").withStyle(ChatFormatting.GREEN),
                    startX + 18, cardY + 30, 0xFF55FF55, false);
        } else {
            graphics.drawString(this.font, Component.literal("Belum ada satwa dipilih.").withStyle(ChatFormatting.WHITE),
                    startX + 18, cardY + 10, 0xFFFFFFFF, false);
            graphics.drawString(this.font, Component.literal("Pilih satwa di bawah agar Zookeeper otomatis mengisi pakan.").withStyle(ChatFormatting.GRAY),
                    startX + 18, cardY + 24, 0xFFAAAAAA, false);
        }

        // Subheader for animal list
        graphics.drawString(this.font, Component.literal("Daftar Satwa di Kebun Binatang:").withStyle(ChatFormatting.WHITE),
                startX + 14, startY + 84, 0xFFFFFFFF, false);

        // Animal list container
        int listX = startX + 12;
        int listY = startY + 96;
        int listW = panelWidth - 24;
        int listH = 96;

        graphics.fill(listX, listY, listX + listW, listY + listH, 0xFF0F172A);
        graphics.renderOutline(listX, listY, listW, listH, 0xFF334155);

        if (availableAnimals.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.literal("Tidak ada satwa terdaftar di kebun binatang.").withStyle(ChatFormatting.GRAY),
                    listX + listW / 2, listY + 40, 0xFFAAAAAA);
        } else {
            int itemH = 28;
            int totalH = availableAnimals.size() * itemH;
            int maxScroll = Math.max(0, totalH - listH);
            scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

            graphics.enableScissor(listX + 1, listY + 1, listX + listW - 1, listY + listH - 1);

            int curY = listY + 2 - scrollOffset;
            for (int i = 0; i < availableAnimals.size(); i++) {
                var animal = availableAnimals.get(i);
                if (curY + itemH >= listY && curY <= listY + listH) {
                    boolean isCurrentlyThis = (assignedAnimalId != -1 && assignedAnimalId == animal.entityId());
                    boolean hovered = mouseX >= listX && mouseX <= listX + listW - 8
                            && mouseY >= curY && mouseY <= curY + itemH && mouseY >= listY && mouseY <= listY + listH;

                    if (isCurrentlyThis) {
                        graphics.fill(listX + 2, curY, listX + listW - 8, curY + itemH, 0x5010B981);
                    } else if (hovered) {
                        graphics.fill(listX + 2, curY, listX + listW - 8, curY + itemH, 0xFF1E293B);
                    }

                    // Render small 3D animal
                    LivingEntity ent = resolveAnimalEntity(animal);
                    if (ent != null) {
                        float bbH = Math.max(0.4f, ent.getBbHeight());
                        float bbW = Math.max(0.4f, ent.getBbWidth());
                        float maxDim = Math.max(bbH, bbW * 0.8f);
                        int scale = (int) Math.clamp(12.0f / maxDim, 6, 14);
                        InventoryScreen.renderEntityInInventoryFollowsMouse(
                                graphics,
                                listX + 4, curY + 2, listX + 26, curY + 24,
                                scale, 0.0f, (float) mouseX, (float) mouseY, ent
                        );
                    }

                    String anName = animal.customName().isEmpty() ? formatSpecies(animal.entityTypeId()) : animal.customName();
                    Item food = getPrimaryFood(animal.entityTypeId());
                    String foodText = "Pakan: " + food.getDescription().getString();

                    graphics.drawString(this.font, Component.literal(anName).withStyle(isCurrentlyThis ? ChatFormatting.GREEN : ChatFormatting.WHITE),
                            listX + 30, curY + 4, 0xFFFFFFFF, false);
                    graphics.drawString(this.font, Component.literal(foodText).withStyle(ChatFormatting.YELLOW),
                            listX + 30, curY + 15, 0xFFFFFF55, false);

                    // Select button area
                    int btnX = listX + listW - 56;
                    int btnY = curY + 4;
                    int btnW = 46;
                    int btnRowH = 18;
                    boolean btnHovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnRowH;

                    if (isCurrentlyThis) {
                        graphics.fill(btnX, btnY, btnX + btnW, btnY + btnRowH, 0xFF059669);
                        graphics.drawString(this.font, Component.literal("Aktif").withStyle(ChatFormatting.WHITE),
                                btnX + 11, btnY + 5, 0xFFFFFFFF, false);
                    } else {
                        graphics.fill(btnX, btnY, btnX + btnW, btnY + btnRowH, btnHovered ? 0xFF0284C7 : 0xFF0369A1);
                        graphics.renderOutline(btnX, btnY, btnW, btnRowH, 0xFF38BDF8);
                        graphics.drawString(this.font, Component.literal("Pilih").withStyle(ChatFormatting.WHITE),
                                btnX + 12, btnY + 5, 0xFFFFFFFF, false);
                    }
                }
                curY += itemH;
            }

            graphics.disableScissor();

            // Scrollbar
            if (maxScroll > 0) {
                int barX = listX + listW - 6;
                int barY = listY + 2;
                int barH = listH - 4;
                graphics.fill(barX, barY, barX + 3, barY + barH, 0xFF020617);
                float ratio = (float) barH / (float) totalH;
                int thumbH = Math.max(12, (int) (barH * ratio));
                int thumbY = barY + (int) ((barH - thumbH) * ((float) scrollOffset / (float) maxScroll));
                graphics.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFF38BDF8);
            }
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !availableAnimals.isEmpty()) {
            int listX = startX + 12;
            int listY = startY + 96;
            int listW = panelWidth - 24;
            int listH = 96;

            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                int itemH = 28;
                int totalH = availableAnimals.size() * itemH;
                int maxScroll = Math.max(0, totalH - listH);
                scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

                int clickedIdx = (int) ((mouseY - (listY + 2) + scrollOffset) / itemH);
                if (clickedIdx >= 0 && clickedIdx < availableAnimals.size()) {
                    var selected = availableAnimals.get(clickedIdx);
                    Item food = getPrimaryFood(selected.entityTypeId());
                    String foodKey = BuiltInRegistries.ITEM.getKey(food).toString();
                    String animalName = selected.customName().isEmpty() ? formatSpecies(selected.entityTypeId()) : selected.customName();

                    ClientPlayNetworking.send(new ZooNetworkPayloads.SelectFeedingTroughAnimalPayload(
                            this.pos,
                            selected.entityId(),
                            "",
                            animalName,
                            selected.entityTypeId()
                    ));

                    this.assignedAnimalId = selected.entityId();
                    this.assignedAnimalName = animalName;
                    this.assignedAnimalSpecies = selected.entityTypeId();
                    this.assignedFoodItemId = foodKey;

                    this.clearWidgets();
                    this.init();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int listX = startX + 12;
        int listY = startY + 96;
        int listW = panelWidth - 24;
        int listH = 96;

        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            int itemH = 28;
            int totalH = availableAnimals.size() * itemH;
            int maxScroll = Math.max(0, totalH - listH);
            if (maxScroll > 0) {
                this.scrollOffset = Math.clamp(this.scrollOffset - (int) (verticalAmount * 28), 0, maxScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
