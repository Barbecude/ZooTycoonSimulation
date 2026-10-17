package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.ArrayList;
import java.util.List;

public class GachaMachineDialogScreen extends Screen {

    private final BlockPos pos;
    private int totalStock;
    private List<ItemStack> slots;

    private final int panelWidth = 260;
    private final int panelHeight = 170;
    private int startX;
    private int startY;

    public GachaMachineDialogScreen(BlockPos pos, int totalStock, List<ItemStack> slots) {
        super(Component.literal("Mesin Gacha Souvenir"));
        this.pos = pos;
        this.totalStock = totalStock;
        this.slots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
    }

    public void updateData(int newTotalStock, List<ItemStack> newSlots) {
        this.totalStock = newTotalStock;
        this.slots = newSlots != null ? new ArrayList<>(newSlots) : new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();
        this.startX = (this.width - panelWidth) / 2;
        this.startY = (this.height - panelHeight) / 2;

        int btnY = startY + 130;
        int btnH = 20;

        // Button: Refill all plushies from player inventory
        ZooButton refillBtn = new ZooButton(startX + 14, btnY, 110, btnH,
                Component.literal("Isi Dari Tas"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.RefillGachaMachinePayload(this.pos));
        }).asPrimary();
        addRenderableWidget(refillBtn);

        // Button: Extract 1 plushie into player inventory
        ZooButton withdrawBtn = new ZooButton(startX + 130, btnY, 60, btnH,
                Component.literal("Ambil 1"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.ExtractGachaPlushiePayload(this.pos));
        }).asSecondary();
        addRenderableWidget(withdrawBtn);

        // Button: Close dialog
        ZooButton closeBtn = new ZooButton(startX + 196, btnY, 50, btnH,
                Component.literal("Tutup"), b -> {
            this.onClose();
        }).asSecondary();
        addRenderableWidget(closeBtn);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        // Panel Background
        graphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xF00F172A);
        graphics.renderOutline(startX, startY, panelWidth, panelHeight, 0xFF334155);

        // Header Divider
        graphics.fill(startX + 1, startY + 36, startX + panelWidth - 1, startY + 37, 0xFF1E293B);

        // Title & Stock Subtitle
        graphics.drawString(this.font, Component.literal("Mesin Gacha Souvenir").withStyle(ChatFormatting.GOLD),
                startX + 14, startY + 12, 0xFFFFAA00, false);

        String stockText = "Stok: " + totalStock + " / 320 Boneka Plushie";
        graphics.drawString(this.font, Component.literal(stockText).withStyle(totalStock > 0 ? ChatFormatting.GREEN : ChatFormatting.RED),
                startX + 14, startY + 23, totalStock > 0 ? 0xFF55FF55 : 0xFFFF5555, false);

        // Help text
        graphics.drawString(this.font, Component.literal("Slot Penyimpanan Boneka:").withStyle(ChatFormatting.GRAY),
                startX + 14, startY + 44, 0xFFAAAAAA, false);

        // 5 Inventory Slots
        int slotSize = 40;
        int slotSpacing = 6;
        int totalSlotsW = (5 * slotSize) + (4 * slotSpacing);
        int slotsStartX = startX + (panelWidth - totalSlotsW) / 2;
        int slotY = startY + 58;

        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int i = 0; i < 5; i++) {
            int slotX = slotsStartX + i * (slotSize + slotSpacing);
            boolean isHovered = mouseX >= slotX && mouseX <= slotX + slotSize && mouseY >= slotY && mouseY <= slotY + slotSize;

            graphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, isHovered ? 0xFF1E293B : 0xFF0F172A);
            graphics.renderOutline(slotX, slotY, slotSize, slotSize, isHovered ? 0xFF38BDF8 : 0xFF334155);

            if (i < this.slots.size()) {
                ItemStack stack = this.slots.get(i);
                if (!stack.isEmpty()) {
                    int itemX = slotX + (slotSize - 16) / 2;
                    int itemY = slotY + (slotSize - 16) / 2;
                    graphics.renderItem(stack, itemX, itemY);
                    graphics.renderItemDecorations(this.font, stack, itemX, itemY);

                    if (isHovered) {
                        hoveredStack = stack;
                    }
                } else {
                    graphics.drawCenteredString(this.font, "-", slotX + slotSize / 2, slotY + (slotSize - 8) / 2, 0xFF475569);
                }
            } else {
                graphics.drawCenteredString(this.font, "-", slotX + slotSize / 2, slotY + (slotSize - 8) / 2, 0xFF475569);
            }
        }

        // Subtext above buttons
        graphics.drawString(this.font, Component.literal("Klik kanan dengan Emerald untuk bermain gacha.").withStyle(ChatFormatting.DARK_GRAY),
                startX + 14, startY + 110, 0xFF64748B, false);

        super.render(graphics, mouseX, mouseY, delta);

        if (!hoveredStack.isEmpty()) {
            graphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

