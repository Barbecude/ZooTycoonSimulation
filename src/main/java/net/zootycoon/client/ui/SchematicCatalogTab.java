package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.zootycoon.client.render.PlacementPreviewRenderer;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;

import java.util.List;

public class SchematicCatalogTab {
    private final MainManagementScreen parent;
    private final String category;
    private List<ZooSchematic> schematics;
    private ZooSchematic selectedSchematic = null;

    public SchematicCatalogTab(MainManagementScreen parent, String category) {
        this.parent = parent;
        this.category = category;
    }

    public void init(MainManagementScreen screen) {
        this.schematics = ZooSchematicRegistry.getByCategory(category);
        if (selectedSchematic == null && !schematics.isEmpty()) {
            selectedSchematic = schematics.get(0);
        }

        int listWidth = 180;
        int listX = 20;
        int startY = 60;

        // Button: + Add Schematic (Studio)
        screen.addRenderableWidget(Button.builder(Component.translatable("zootycoon.ui.btn.add_schematic"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.EnterStudioPayload(category));
            screen.onClose();
        }).bounds(listX, startY, listWidth, 20).build());

        // Buttons for schematics
        int itemY = startY + 26;
        for (ZooSchematic schem : schematics) {
            screen.addRenderableWidget(Button.builder(Component.literal(schem.getName()), b -> {
                this.selectedSchematic = schem;
            }).bounds(listX, itemY, listWidth, 22).build());
            itemY += 26;
        }

        // Action Button: Select Location to Build
        int detailX = listX + listWidth + 25;
        int actionBtnY = screen.height - 45;
        screen.addRenderableWidget(Button.builder(Component.literal("Pilih Lokasi Bangun & Preview In-World"), b -> {
            if (selectedSchematic != null) {
                PlacementPreviewRenderer.setActiveSchematic(selectedSchematic);
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.displayClientMessage(
                            Component.literal("Arahkan pandangan dan klik kanan blok dengan Build Tool untuk menaruh bangunan.")
                                    .withStyle(ChatFormatting.AQUA),
                            false
                    );
                }
                screen.onClose();
            }
        }).bounds(detailX, actionBtnY, 260, 24).build());
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        int listWidth = 180;
        int listX = 20;
        int detailX = listX + listWidth + 25;
        int detailWidth = parent.width - detailX - 25;
        int startY = 60;
        int detailHeight = parent.height - startY - 55;

        // Detail panel background
        graphics.fill(detailX, startY, detailX + detailWidth, startY + detailHeight, 0xAA222222);
        graphics.renderOutline(detailX, startY, detailWidth, detailHeight, 0xFF444444);

        if (selectedSchematic != null) {
            // Header
            graphics.drawString(mc.font, selectedSchematic.getName(), detailX + 15, startY + 15, 0xFFFFFF, false);
            graphics.drawString(mc.font, "Kategori: " + selectedSchematic.getCategory() + " (" + selectedSchematic.getSubcategory() + ")", detailX + 15, startY + 30, 0xAAAAAA, false);
            graphics.drawString(mc.font, "Arsitek: " + selectedSchematic.getAuthor(), detailX + 15, startY + 45, 0x888888, false);

            // Divider
            graphics.fill(detailX + 15, startY + 60, detailX + detailWidth - 15, startY + 61, 0xFF444444);

            // Dimensions
            var dim = selectedSchematic.getDimensions();
            String dimText = "Dimensi: " + dim.getX() + " x " + dim.getY() + " x " + dim.getZ() + " Blok (P x T x L)";
            graphics.drawString(mc.font, dimText, detailX + 15, startY + 70, 0x55FF55, false);

            // Builder cost
            String costText = "Biaya Sewa Builder: " + selectedSchematic.getBuilderHiringCost() + " Emeralds ❇";
            graphics.drawString(mc.font, costText, detailX + 15, startY + 85, 0x55FFFF, false);

            // Description
            graphics.drawString(mc.font, "Deskripsi:", detailX + 15, startY + 110, 0xFFFFFF, false);
            graphics.drawWordWrap(mc.font, Component.literal(selectedSchematic.getDescription()), detailX + 15, startY + 125, detailWidth - 30, 0xCCCCCC);

            // Workflow note
            graphics.drawString(mc.font, "Alur Pembangunan:", detailX + 15, startY + 165, 0xFFD700, false);
            String note = "1. Klik tombol di bawah.\n2. Arahkan ke lahan kebun binatang Anda (preview berpendar hijau).\n3. Klik kanan untuk memulai kontraktor!";
            graphics.drawWordWrap(mc.font, Component.literal(note), detailX + 15, startY + 180, detailWidth - 30, 0xAAAAAA);
        } else {
            graphics.drawString(mc.font, "Pilih salah satu bangunan di daftar sebelah kiri.", detailX + 15, startY + 15, 0xAAAAAA, false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
