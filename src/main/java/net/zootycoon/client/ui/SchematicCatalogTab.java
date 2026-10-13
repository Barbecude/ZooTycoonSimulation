package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.render.PlacementPreviewRenderer;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.schematic.SchematicScanner;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;
import net.zootycoon.util.ZooTranslations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SchematicCatalogTab {
    private final MainManagementScreen parent;
    private String category;
    private List<ZooSchematic> schematics;
    private ZooSchematic selectedSchematic = null;
    private List<Map.Entry<Item, Integer>> cachedMaterials = Collections.emptyList();

    public SchematicCatalogTab(MainManagementScreen parent, String category) {
        this.parent = parent;
        this.category = category;
    }

    private void updateSelected(ZooSchematic schem) {
        this.selectedSchematic = schem;
        if (schem != null) {
            this.cachedMaterials = loadMaterials(schem);
        } else {
            this.cachedMaterials = Collections.emptyList();
        }
    }

    private List<Map.Entry<Item, Integer>> loadMaterials(ZooSchematic schem) {
        if (schem == null) return Collections.emptyList();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Collections.emptyList();
        Path nbtFile = ZooSchematicRegistry.getCustomSchematicsDir().resolve(schem.getStructureLocation().getPath() + ".nbt");
        if (Files.exists(nbtFile)) {
            try {
                CompoundTag tag = NbtIo.readCompressed(nbtFile, NbtAccounter.unlimitedHeap());
                StructureTemplate template = new StructureTemplate();
                template.load(mc.level.holderLookup(Registries.BLOCK), tag);
                var scan = SchematicScanner.scan(template);
                List<Map.Entry<Item, Integer>> list = new ArrayList<>(scan.requiredMaterials().entrySet());
                list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
                return list;
            } catch (Exception e) {
                ZooTycoonMod.LOGGER.error("Failed to load materials for " + schem.getName(), e);
            }
        }
        return Collections.emptyList();
    }

    public void init(MainManagementScreen screen) {
        this.schematics = ZooSchematicRegistry.getByCategory(category);
        if (selectedSchematic != null && !schematics.contains(selectedSchematic)) {
            selectedSchematic = null;
        }
        if (selectedSchematic == null && !schematics.isEmpty()) {
            updateSelected(schematics.get(0));
        } else if (selectedSchematic != null && cachedMaterials.isEmpty()) {
            this.cachedMaterials = loadMaterials(selectedSchematic);
        }

        int listWidth = 190;
        int listX = 20;

        int catBtnW = (listWidth - 4) / 2;
        int catY = 48;

        ZooButton kandangBtn = new ZooButton(listX, catY, catBtnW, 20, ZooTranslations.component("zootycoon.ui.catalog.tab_enclosures"), b -> {
            if (!"enclosures".equals(this.category)) {
                this.category = "enclosures";
                this.selectedSchematic = null;
                screen.setView(screen.getCurrentView());
            }
        });
        kandangBtn.setSelected("enclosures".equals(this.category));
        screen.addWidgetPublic(kandangBtn);

        ZooButton fasilitasBtn = new ZooButton(listX + catBtnW + 4, catY, catBtnW, 20, ZooTranslations.component("zootycoon.ui.catalog.tab_essentials"), b -> {
            if (!"essentials".equals(this.category)) {
                this.category = "essentials";
                this.selectedSchematic = null;
                screen.setView(screen.getCurrentView());
            }
        });
        fasilitasBtn.setSelected("essentials".equals(this.category));
        screen.addWidgetPublic(fasilitasBtn);

        int startY = catY + 24;

        screen.addWidgetPublic(new ZooButton(listX, startY, listWidth, 22, ZooTranslations.component("zootycoon.ui.catalog.btn_new_studio"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.EnterStudioPayload(category));
            screen.onClose();
        }).setCustomIcon(ZooTycoonMod.id("textures/gui/icons/add_enclosure.png")).asPrimary());

        int itemY = startY + 28;
        for (ZooSchematic schem : schematics) {
            String label = (schem == selectedSchematic ? "> " : "  ") + schem.getName();
            screen.addWidgetPublic(new ZooButton(listX, itemY, listWidth, 22, Component.literal(label), b -> {
                updateSelected(schem);
                screen.setView(screen.getCurrentView());
            }).setLeftAligned(true).setSelected(schem == selectedSchematic));
            itemY += 26;
        }

        if (selectedSchematic != null) {
            int detailX = listX + listWidth + 25;
            int detailWidth = parent.width - detailX - 25;
            int actionBtnY = screen.height - 42;

            int btnWidth1 = (int) (detailWidth * 0.44);
            int btnWidth2 = (int) (detailWidth * 0.34);
            int btnWidth3 = detailWidth - btnWidth1 - btnWidth2 - 8;

            screen.addWidgetPublic(new ZooButton(detailX, actionBtnY, btnWidth1, 24, ZooTranslations.component("zootycoon.ui.catalog.btn_place"), b -> {
                PlacementPreviewRenderer.setActiveSchematic(selectedSchematic);
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    boolean hasStick = false;
                    for (int i = 0; i < 9; i++) {
                        if (mc.player.getInventory().getItem(i).is(Items.STICK)) {
                            mc.player.getInventory().selected = i;
                            hasStick = true;
                            break;
                        }
                    }
                    if (!hasStick) {
                        for (int i = 9; i < mc.player.getInventory().getContainerSize(); i++) {
                            if (mc.player.getInventory().getItem(i).is(Items.STICK)) {
                                hasStick = true;
                                break;
                            }
                        }
                    }
                    if (!hasStick) {
                        ZooHudOverlay.showToast("zootycoon.toast.title.warning", "zootycoon.build.need_stick", ZooHudOverlay.TOAST_WARNING);
                        return;
                    }
                    ZooHudOverlay.showToast("zootycoon.toast.title.info", "zootycoon.build.place_guide", ZooHudOverlay.TOAST_INFO);
                }
                screen.onClose();
            }).setCustomIcon(ZooTycoonMod.id("textures/gui/icons/add_green.png")).asEmerald());

            screen.addWidgetPublic(new ZooButton(detailX + btnWidth1 + 4, actionBtnY, btnWidth2, 24, ZooTranslations.component("zootycoon.ui.catalog.btn_edit"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.EditStudioPayload(selectedSchematic.getId(), category));
                screen.onClose();
            }).setCustomIcon(ZooTycoonMod.id("textures/gui/icons/pencil.png")).asGold());

            screen.addWidgetPublic(new ZooButton(detailX + btnWidth1 + btnWidth2 + 8, actionBtnY, btnWidth3, 24, ZooTranslations.component("zootycoon.ui.catalog.btn_delete"), b -> {
                ZooSchematicRegistry.deleteCustomSchematic(selectedSchematic.getId());
                this.selectedSchematic = null;
                screen.setView(screen.getCurrentView());
            }).setCustomIcon(ZooTycoonMod.id("textures/gui/icons/trash_can.png")).asDanger());
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        int listWidth = 190;
        int listX = 20;
        int detailX = listX + listWidth + 25;
        int detailWidth = parent.width - detailX - 25;
        int panelStartY = 44;
        int detailHeight = parent.height - panelStartY - 48;

        int listTotalHeight = parent.height - panelStartY - 25;
        graphics.fill(listX - 5, panelStartY, listX + listWidth + 5, panelStartY + listTotalHeight, 0xF20F1219);
        graphics.fill(listX - 5, panelStartY, listX + listWidth + 5, panelStartY + 1, 0xFF242A38);
        graphics.fill(listX - 5, panelStartY + listTotalHeight - 1, listX + listWidth + 5, panelStartY + listTotalHeight, 0xFF242A38);
        graphics.fill(listX - 5, panelStartY, listX - 4, panelStartY + listTotalHeight, 0xFF242A38);
        graphics.fill(listX + listWidth + 4, panelStartY, listX + listWidth + 5, panelStartY + listTotalHeight, 0xFF242A38);

        graphics.fill(detailX, panelStartY, detailX + detailWidth, panelStartY + detailHeight, 0xF20F1219);
        graphics.fill(detailX, panelStartY, detailX + detailWidth, panelStartY + 1, 0xFF242A38);
        graphics.fill(detailX, panelStartY + detailHeight - 1, detailX + detailWidth, panelStartY + detailHeight, 0xFF242A38);
        graphics.fill(detailX, panelStartY, detailX + 1, panelStartY + detailHeight, 0xFF242A38);
        graphics.fill(detailX + detailWidth - 1, panelStartY, detailX + detailWidth, panelStartY + detailHeight, 0xFF242A38);

        int startY = panelStartY;

        if (selectedSchematic != null) {
            graphics.drawString(mc.font, Component.literal(selectedSchematic.getName()), detailX + 15, startY + 15, 0xFFFFFFFF, true);

            var dim = selectedSchematic.getDimensions();
            graphics.drawString(mc.font, ZooTranslations.component("zootycoon.ui.catalog.dimensions", dim.getX(), dim.getY(), dim.getZ(), (dim.getX() * dim.getY() * dim.getZ())), detailX + 15, startY + 30, 0xFF8B949E, false);

            graphics.drawString(mc.font, ZooTranslations.component("zootycoon.ui.catalog.builder_cost", selectedSchematic.getBuilderHiringCost()), detailX + 15, startY + 44, 0xFFD0D7DE, true);

            graphics.fill(detailX + 15, startY + 62, detailX + detailWidth - 15, startY + 63, 0xFF2A2E37);

            graphics.drawString(mc.font, ZooTranslations.component("zootycoon.ui.catalog.materials_header", cachedMaterials.size()), detailX + 15, startY + 74, 0xFFFFFFFF, true);

            int gridStartY = startY + 90;
            int slotSize = 22;
            int gap = 3;
            int step = slotSize + gap;
            int cols = Math.max(1, (detailWidth - 30) / step);

            if (cachedMaterials.isEmpty()) {
                graphics.drawString(mc.font, ZooTranslations.component("zootycoon.ui.catalog.no_materials"), detailX + 15, gridStartY + 10, 0xFFAAAAAA, true);
            } else {
                for (int i = 0; i < cachedMaterials.size(); i++) {
                    var entry = cachedMaterials.get(i);
                    int col = i % cols;
                    int row = i / cols;
                    int slotX = detailX + 15 + col * step;
                    int slotY = gridStartY + row * step;

                    if (slotY + slotSize > startY + detailHeight - 45) {
                        int remaining = cachedMaterials.size() - i;
                        graphics.drawString(mc.font, ZooTranslations.component("zootycoon.ui.catalog.more_materials", remaining), slotX, slotY + 6, 0xFF888888, true);
                        break;
                    }

                    graphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF22262E);
                    graphics.fill(slotX, slotY, slotX + slotSize, slotY + 1, 0xFF353B47);
                    graphics.fill(slotX, slotY + slotSize - 1, slotX + slotSize, slotY + slotSize, 0xFF353B47);
                    graphics.fill(slotX, slotY, slotX + 1, slotY + slotSize, 0xFF353B47);
                    graphics.fill(slotX + slotSize - 1, slotY, slotX + slotSize, slotY + slotSize, 0xFF353B47);

                    ItemStack stack = new ItemStack(entry.getKey());
                    graphics.renderItem(stack, slotX + 3, slotY + 3);
                    graphics.renderItemDecorations(mc.font, stack, slotX + 3, slotY + 3, String.valueOf(entry.getValue()));

                    if (mouseX >= slotX && mouseX <= slotX + slotSize && mouseY >= slotY && mouseY <= slotY + slotSize) {
                        graphics.renderTooltip(mc.font, stack, mouseX, mouseY);
                    }
                }
            }
        } else {
            graphics.drawString(mc.font, ZooTranslations.component("zootycoon.ui.catalog.empty_title"), detailX + 15, startY + 20, 0xFFFFFFFF, true);
            graphics.fill(detailX + 15, startY + 36, detailX + detailWidth - 15, startY + 37, 0xFF242A38);
            graphics.drawWordWrap(mc.font, ZooTranslations.component("zootycoon.ui.catalog.empty_help"), detailX + 15, startY + 48, detailWidth - 30, 0xFFCCCCCC);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
