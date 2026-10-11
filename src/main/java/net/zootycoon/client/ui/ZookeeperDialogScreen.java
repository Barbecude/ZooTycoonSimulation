package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ZookeeperDialogScreen extends Screen {

    private final UUID keeperUuid;
    private final String keeperName;
    private final boolean hasAnimal;
    private final String animalName;
    private final int hunger;
    private final int foodStock;
    private final String dialogText;
    private final List<String> acceptedFoods;

    private final List<ItemStack> foodStacks = new ArrayList<>();

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
        super(Component.literal("Dialog Zookeeper: " + keeperName));
        this.keeperUuid = keeperUuid;
        this.keeperName = keeperName;
        this.hasAnimal = hasAnimal;
        this.animalName = animalName;
        this.hunger = hunger;
        this.foodStock = foodStock;
        this.dialogText = dialogText;
        this.acceptedFoods = acceptedFoods;

        for (String idStr : acceptedFoods) {
            try {
                ResourceLocation id = ResourceLocation.parse(idStr);
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != null && item != Items.AIR) {
                    foodStacks.add(new ItemStack(item));
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void init() {
        int panelW = 340;
        int panelH = 240;
        int startX = (this.width - panelW) / 2;
        int startY = (this.height - panelH) / 2;

        int btnY = startY + panelH - 34;
        int btnW = (panelW - 30) / 2;

        // Button: Serahkan Pakan dari Tas
        ZooButton giveFoodBtn = new ZooButton(startX + 10, btnY, btnW, 24, Component.literal("Serahkan Pakan"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.SupplyZookeeperFoodPayload(this.keeperUuid));
        }).asEmerald();
        if (!hasAnimal) {
            giveFoodBtn.active = false;
        }
        addRenderableWidget(giveFoodBtn);

        // Button: Tutup
        ZooButton closeBtn = new ZooButton(startX + 10 + btnW + 10, btnY, btnW, 24, Component.literal("Tutup"), b -> {
            this.onClose();
        });
        addRenderableWidget(closeBtn);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Semi-transparent darkened background
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        int panelW = 340;
        int panelH = 240;
        int startX = (this.width - panelW) / 2;
        int startY = (this.height - panelH) / 2;

        // 1. Panel Background & Borders
        graphics.fill(startX, startY, startX + panelW, startY + panelH, 0xD012151D);
        graphics.fill(startX, startY, startX + panelW, startY + 1, 0xFF2A3140);
        graphics.fill(startX, startY + panelH - 1, startX + panelW, startY + panelH, 0xFF2A3140);
        graphics.fill(startX, startY, startX + 1, startY + panelH, 0xFF2A3140);
        graphics.fill(startX + panelW - 1, startY, startX + panelW, startY + panelH, 0xFF2A3140);

        // 2. Header
        graphics.drawString(font, Component.literal(keeperName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), startX + 14, startY + 12, 0xFFFFFFFF, true);
        graphics.drawString(font, Component.literal("Pengasuh Satwa Kebun Binatang").withStyle(ChatFormatting.GRAY), startX + 14, startY + 24, 0xFFAAAAAA, true);

        // Top divider
        graphics.fill(startX + 14, startY + 36, startX + panelW - 14, startY + 37, 0xFF2A3140);

        // 3. NPC Speech Bubble / Dialog Box
        int bubbleX = startX + 12;
        int bubbleY = startY + 42;
        int bubbleW = panelW - 24;
        int bubbleH = 50;

        graphics.fill(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH, 0xFF181C26);
        graphics.fill(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + 1, 0xFF2A3140);
        graphics.fill(bubbleX, bubbleY + bubbleH - 1, bubbleX + bubbleW, bubbleY + bubbleH, 0xFF2A3140);
        graphics.fill(bubbleX, bubbleY, bubbleX + 1, bubbleY + bubbleH, 0xFF2A3140);
        graphics.fill(bubbleX + bubbleW - 1, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH, 0xFF2A3140);

        graphics.drawWordWrap(font, Component.literal(dialogText).withStyle(ChatFormatting.WHITE), bubbleX + 8, bubbleY + 6, bubbleW - 16, 0xFFDDDDDD);

        // 4. Animal & Care Status Section
        int statusY = bubbleY + bubbleH + 8;
        if (hasAnimal) {
            graphics.drawString(font, Component.literal("Satwa Binaan: ").withStyle(ChatFormatting.GRAY).append(Component.literal(animalName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)), startX + 14, statusY, 0xFFFFFFFF, true);

            int hungerColor = hunger > 50 ? 0xFF2ECC71 : (hunger > 20 ? 0xFFF1C40F : 0xFFE74C3C);
            graphics.drawString(font, Component.literal("Tingkat Kenyang: ").withStyle(ChatFormatting.GRAY).append(Component.literal(hunger + "%").withStyle(ChatFormatting.BOLD)), startX + 14, statusY + 12, hungerColor, true);

            int stockColor = foodStock > 3 ? 0xFF2ECC71 : (foodStock > 0 ? 0xFFF1C40F : 0xFFE74C3C);
            String stockTag = foodStock > 0 ? (foodStock + " buah") : "KOSONG (Perlu Isi!)";
            graphics.drawString(font, Component.literal("Stok di Kantong: ").withStyle(ChatFormatting.GRAY).append(Component.literal(stockTag).withStyle(ChatFormatting.BOLD)), startX + 14, statusY + 24, stockColor, true);

            // Accepted food grid header
            graphics.drawString(font, Component.literal("Pakan yang Disukai:").withStyle(ChatFormatting.YELLOW), startX + 14, statusY + 38, 0xFFFFFFFF, true);

            int slotSize = 22;
            int slotStartX = startX + 14;
            int slotStartY = statusY + 50;

            if (foodStacks.isEmpty()) {
                graphics.drawString(font, Component.literal("Belum ada data pakan khusus.").withStyle(ChatFormatting.GRAY), slotStartX, slotStartY + 4, 0xFF888888, true);
            } else {
                for (int i = 0; i < Math.min(foodStacks.size(), 12); i++) {
                    ItemStack st = foodStacks.get(i);
                    int sX = slotStartX + (i * (slotSize + 3));
                    int sY = slotStartY;

                    graphics.fill(sX, sY, sX + slotSize, sY + slotSize, 0xFF181C26);
                    graphics.fill(sX, sY, sX + slotSize, sY + 1, 0xFF2A3140);
                    graphics.fill(sX, sY + slotSize - 1, sX + slotSize, sY + slotSize, 0xFF2A3140);
                    graphics.fill(sX, sY, sX + 1, sY + slotSize, 0xFF2A3140);
                    graphics.fill(sX + slotSize - 1, sY, sX + slotSize, sY + slotSize, 0xFF2A3140);

                    graphics.renderItem(st, sX + 3, sY + 3);

                    if (mouseX >= sX && mouseX <= sX + slotSize && mouseY >= sY && mouseY <= sY + slotSize) {
                        graphics.fill(sX, sY, sX + slotSize, sY + slotSize, 0x40FFFFFF);
                        graphics.renderTooltip(font, st, mouseX, mouseY);
                    }
                }
            }
        } else {
            graphics.drawString(font, Component.literal("Status: Belum Ditugaskan").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), startX + 14, statusY + 10, 0xFFFFFFFF, true);
            graphics.drawWordWrap(font, Component.literal("Beri tag nama pada satwa liar di area kebun binatang (Shift Kanan) agar zookeeper ini otomatis terhubung merawatnya.").withStyle(ChatFormatting.GRAY), startX + 14, statusY + 26, panelW - 28, 0xFFAAAAAA);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

