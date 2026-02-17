package com.example.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BiomeChangerScreen extends Screen {

    private static final int BG_COLOR = 0xEE1A1A1A;
    private static final int ITEMS_PER_PAGE = 20; // 4 cols x 5 rows
    private final BlockPos targetPos;
    private int scrollOffset = 0;
    private boolean replaceOnly = false;
    private final String currentBiomeId;

    // Fixed list of "Main" biomes to avoid variants
    private static final List<String> MAIN_BIOMES = List.of(
            "minecraft:plains",
            "minecraft:desert",
            "minecraft:forest",
            "minecraft:taiga",
            "minecraft:swamp",
            "minecraft:jungle",
            "minecraft:savanna",
            "minecraft:badlands",
            "minecraft:snowy_plains",
            "minecraft:mushroom_fields",
            "minecraft:ocean",
            "minecraft:river",
            "minecraft:beach",
            "minecraft:meadow",
            "minecraft:cherry_grove",
            "minecraft:mangrove_swamp",
            "minecraft:deep_dark",
            "minecraft:dripstone_caves",
            "minecraft:lush_caves",
            "minecraft:birch_forest",
            "minecraft:dark_forest",
            "minecraft:sunflower_plains",
            "minecraft:flower_forest",
            "minecraft:ice_spikes");

    public BiomeChangerScreen(BlockPos pos) {
        super(Component.literal("Select Biome"));
        this.targetPos = pos;
        this.currentBiomeId = Minecraft.getInstance().level.getBiome(pos).unwrapKey().map(k -> k.location().toString()).orElse("minecraft:plains");
    }

    @Override
    protected void init() {
        refreshWidgets();
    }

    private void refreshWidgets() {
        this.clearWidgets();
        int x = this.width / 2 - 160;
        int y = this.height / 2 - 100;

        int cols = 4;
        int btnW = 75;
        int btnH = 20;
        int gap = 5;

        // Pagination
        int maxPages = (MAIN_BIOMES.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

        // Grid
        int startIdx = scrollOffset * ITEMS_PER_PAGE;
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, MAIN_BIOMES.size());

        for (int i = startIdx; i < endIdx; i++) {
            int idx = i - startIdx;
            int col = idx % cols;
            int row = idx / cols;

            int btnX = x + col * (btnW + gap);
            int btnY = y + row * (btnH + gap);

            String biomeId = MAIN_BIOMES.get(i);
            String name = formatName(biomeId);

            this.addRenderableWidget(Button.builder(Component.literal(name), b -> setBiome(biomeId))
                    .bounds(btnX, btnY, btnW, btnH)
                    .tooltip(Tooltip.create(Component.literal(biomeId)))
                    .build());
        }

        // Mode Toggle
        String modeLabel = replaceOnly ? "Mode: Replace " + formatName(currentBiomeId) : "Mode: Fill All";
        this.addRenderableWidget(Button.builder(Component.literal(modeLabel), b -> {
            replaceOnly = !replaceOnly;
            refreshWidgets();
        }).bounds(x + 50, y + 140, 220, 20).build());

        // Navigation Buttons
        this.addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> {
            if (scrollOffset > 0) {
                scrollOffset--;
                refreshWidgets();
            }
        }).bounds(x, y + 180, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Next >"), b -> {
            if (scrollOffset < maxPages - 1) {
                scrollOffset++;
                refreshWidgets();
            }
        }).bounds(x + 260, y + 180, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
                .bounds(x + 130, y + 180, 60, 20).build());

    }

    private void setBiome(String biomeId) {
        if (Minecraft.getInstance().getConnection() != null) {
            String cmd;
            if (replaceOnly) {
                cmd = String.format("zoocmd setbiome %s %d %d %d replace %s",
                        biomeId, targetPos.getX(), targetPos.getY(), targetPos.getZ(), currentBiomeId);
            } else {
                cmd = String.format("zoocmd setbiome %s %d %d %d",
                        biomeId, targetPos.getX(), targetPos.getY(), targetPos.getZ());
            }
            Minecraft.getInstance().getConnection().sendCommand(cmd);
        }
        this.onClose();
    }

    private String formatName(String id) {
        String path = id.contains(":") ? id.split(":")[1] : id;
        path = path.replace("_", " ");
        // Capitalize
        StringBuilder sb = new StringBuilder();
        for (String w : path.split(" ")) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1)
                    sb.append(w.substring(1));
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        int x = this.width / 2;
        int y = this.height / 2 - 115;

        g.drawCenteredString(this.font, "Select Target Biome", x, y, 0xFFFFFF);
        g.drawCenteredString(this.font, "Pos: " + targetPos.toShortString() + " (" + formatName(currentBiomeId) + ")", x, y + 10, 0xAAAAAA);

        super.render(g, mx, my, pt);
    }
}
