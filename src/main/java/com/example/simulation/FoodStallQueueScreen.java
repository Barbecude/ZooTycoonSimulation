package com.example.simulation;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Client-side popup screen shown when the player is within 8 blocks of a Food Stall
 * that has at least one visitor waiting in its queue.
 *
 * The player can:
 * - "Layani 1"    → send FoodStallServePacket(pos, false)
 * - "Layani Semua" → send FoodStallServePacket(pos, true)
 * - "Tutup"       → close the screen; the server will re-send if the queue persists
 */
public class FoodStallQueueScreen extends Screen {

    private static final int W = 200;
    private static final int H = 140;

    private final BlockPos stallPos;
    private int   queueSize;
    private String moodLabel;
    private String requestItemId;

    public FoodStallQueueScreen(BlockPos stallPos, int queueSize, String moodLabel, String requestItemId) {
        super(Component.translatable("screen.indozoo.food_stall_queue"));
        this.stallPos  = stallPos;
        this.queueSize = queueSize;
        this.moodLabel = moodLabel;
        this.requestItemId = requestItemId == null ? "" : requestItemId;
    }

    /** Update the screen data without closing/reopening it. */
    public void refresh(int newQueueSize, String newMoodLabel, String newRequestItemId) {
        this.queueSize  = newQueueSize;
        this.moodLabel  = newMoodLabel;
        this.requestItemId = newRequestItemId == null ? "" : newRequestItemId;
        if (queueSize == 0) onClose();
    }

    private int popupX() {
        return Math.max(10, (width - W) / 2 - 140);
    }

    @Override
    protected void init() {
        int cx = popupX();
        int cy = (height - H) / 2;

        // Layani 1
        addRenderableWidget(Button.builder(
                Component.literal("Layani 1"),
                btn -> {
                    PacketHandler.INSTANCE.sendToServer(new FoodStallServePacket(stallPos, false));
                    queueSize = Math.max(0, queueSize - 1);
                    if (queueSize == 0) onClose();
                })
                .pos(cx + 10, cy + H - 36)
                .size(80, 20)
                .build());

        // Layani Semua
        addRenderableWidget(Button.builder(
                Component.literal("Layani Semua"),
                btn -> {
                    PacketHandler.INSTANCE.sendToServer(new FoodStallServePacket(stallPos, true));
                    onClose();
                })
                .pos(cx + 98, cy + H - 36)
                .size(92, 20)
                .build());

        // Tutup
        addRenderableWidget(Button.builder(
                Component.literal("Tutup"),
                btn -> onClose())
                .pos(cx + W - 50, cy + 6)
                .size(44, 16)
                .build());
    }

    @Override
    public void render(GuiGraphics gfx, int mx, int my, float pt) {
        renderBackground(gfx);

        int cx = popupX();
        int cy = (height - H) / 2;

        // Panel background
        gfx.fill(cx, cy, cx + W, cy + H, 0xCC1A1A2E);
        gfx.fill(cx + 1, cy + 1, cx + W - 1, cy + 3, 0xFF4ECDC4);  // top accent line

        // Title
        gfx.drawCenteredString(font,
                Component.literal("\u26CF Food Stall"),
                cx + W / 2, cy + 8, 0xFF4ECDC4);

        // Queue info
        String queueStr = queueSize + " pengunjung menunggu dilayani";
        gfx.drawCenteredString(font, Component.literal(queueStr),
                cx + W / 2, cy + 34, 0xFFFFFFFF);

        // Mood info
        String moodStr = getMoodText(moodLabel);
        gfx.drawCenteredString(font, Component.literal(moodStr),
                cx + W / 2, cy + 56, getMoodColor(moodLabel));

        // Request info
        String requestName = resolveRequestName();
        gfx.drawCenteredString(font, Component.literal("Request: " + requestName),
            cx + W / 2, cy + 72, 0xFFDDDDDD);

        // Tip
        gfx.drawCenteredString(font,
            Component.literal("Pastikan stok tersedia sebelum melayani"),
            cx + W / 2, cy + 94, 0xFFAAAAAA);

        super.render(gfx, mx, my, pt);
    }

    private static String getMoodText(String mood) {
        return switch (mood) {
            case "HUNGRY"   -> "\uD83D\uDE22 Pengunjung lapar!";
            case "HAPPY"    -> "\uD83D\uDE00 Pengunjung senang!";
            case "BORED"    -> "\uD83D\uDE10 Pengunjung bosan";
            default         -> "Mood: " + mood;
        };
    }

    private static int getMoodColor(String mood) {
        return switch (mood) {
            case "HUNGRY" -> 0xFFFF6B6B;
            case "HAPPY"  -> 0xFF98FF98;
            default       -> 0xFFDDDDDD;
        };
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public BlockPos getStallPos() { return stallPos; }

    private String resolveRequestName() {
        if (requestItemId == null || requestItemId.isBlank()) return "-";
        ResourceLocation id = ResourceLocation.tryParse(requestItemId);
        if (id == null) return requestItemId;
        net.minecraft.world.item.Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == net.minecraft.world.item.Items.AIR) return requestItemId;
        return item.getDescription().getString();
    }
}
