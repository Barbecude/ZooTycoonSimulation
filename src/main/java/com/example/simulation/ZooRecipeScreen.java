package com.example.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ZooRecipeScreen extends Screen {

    private final Screen parent;
    private int currentIndex = 0;
    private final List<RecipePage> pages = new ArrayList<>();
    private int descScroll = 0;
    private static final int BG_WIDTH = 220;
    private static final int BG_HEIGHT = 250;

    public ZooRecipeScreen(Screen parent) {
        super(Component.literal("Resep IndoZoo"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        pages.clear();

        pages.add(RecipePage.biomeChanger());
        pages.add(RecipePage.trashCan());
        pages.add(RecipePage.animalTag());
        pages.add(RecipePage.mobCage());
        pages.add(RecipePage.toilet());
        pages.add(RecipePage.zooBanner());

        int centerX = this.width / 2;
        int y = (this.height - BG_HEIGHT) / 2;
        int baseY = y + BG_HEIGHT - 30;

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (currentIndex > 0) {
                currentIndex--;
                descScroll = 0;
            }
        }).bounds(centerX - 80, baseY, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if (currentIndex < pages.size() - 1) {
                currentIndex++;
                descScroll = 0;
            }
        }).bounds(centerX + 60, baseY, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Tutup"), b -> onClose())
                .bounds(centerX - 30, baseY, 60, 20).build());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);

        int bgWidth = BG_WIDTH;
        int bgHeight = BG_HEIGHT;
        int x = (this.width - bgWidth) / 2;
        int y = (this.height - bgHeight) / 2;

        int bg = 0xCC1A1A1A;
        int outline = 0xFF4A90E2;
        int slotBg = 0xFF2A2A2A;
        int slotOutline = 0xFF444444;

        g.fill(x, y, x + bgWidth, y + bgHeight, bg);
        g.renderOutline(x, y, bgWidth, bgHeight, outline);

        if (!pages.isEmpty()) {
            RecipePage page = pages.get(currentIndex);
            page.render(g, x, y, slotBg, slotOutline);

            String title = page.title;
            int titleW = this.font.width(title);
            g.drawString(this.font, title, (this.width - titleW) / 2, y + 10, 0xFFFFFF);

            String pageText = (currentIndex + 1) + "/" + pages.size();
            int pageW = this.font.width(pageText);
            g.drawString(this.font, pageText, (this.width - pageW) / 2, y + bgHeight - 14, 0xAAAAAA);

            String desc = page.description;
            java.util.List<net.minecraft.util.FormattedCharSequence> lines = new java.util.ArrayList<>();
            String[] paragraphs = desc.split("\n", -1);
            for (String p : paragraphs) {
                if (p.isEmpty()) {
                    lines.add(net.minecraft.util.FormattedCharSequence.EMPTY);
                } else {
                    lines.addAll(this.font.split(Component.literal(p), bgWidth - 44));
                }
            }
            int lineHeight = this.font.lineHeight + 1;
            int totalLines = lines.size();

            int containerX = x + 16;
            int containerY = y + 120;
            int containerWidth = bgWidth - 32;
            int baseY = y + bgHeight - 30;
            int containerBottom = baseY - 6;
            int containerHeight = Math.max(24, containerBottom - containerY);
            int maxVisibleLines = Math.max(1, (containerHeight - 8) / lineHeight);
            int visibleLines = totalLines <= 0 ? 0 : Math.min(maxVisibleLines, totalLines);

            if (visibleLines > 0) {
                if (descScroll > totalLines - visibleLines) {
                    descScroll = Math.max(0, totalLines - visibleLines);
                }

                g.pose().pushPose();
                g.pose().translate(0, 0, 100);
                g.fill(containerX, containerY, containerX + containerWidth, containerY + containerHeight, 0xCC000000);
                g.renderOutline(containerX, containerY, containerWidth, containerHeight, 0xFF555555);

                int textX = containerX + 6;
                int textY = containerY + 4;
                for (int i = 0; i < visibleLines; i++) {
                    int idx = descScroll + i;
                    if (idx >= totalLines) break;
                    g.drawString(this.font, lines.get(idx), textX, textY, 0xDDDDDD);
                    textY += lineHeight;
                }

                if (totalLines > visibleLines) {
                    int barWidth = 4;
                    int barX = containerX + containerWidth - barWidth - 2;
                    int barTop = containerY + 2;
                    int barBottom = containerY + containerHeight - 2;
                    int trackHeight = barBottom - barTop;
                    int barHeight = Math.max(6, (int) (trackHeight * (visibleLines / (float) totalLines)));
                    int maxScroll = totalLines - visibleLines;
                    int barOffset = maxScroll == 0 ? 0 : (int) ((trackHeight - barHeight) * (descScroll / (float) maxScroll));
                    int barY = barTop + barOffset;
                    g.fill(barX, barTop, barX + barWidth, barBottom, 0x55222222);
                    g.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFFAAAAAA);
                }

                g.pose().popPose();
            }
        }

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (!pages.isEmpty()) {
            RecipePage page = pages.get(currentIndex);
            int bgWidth = BG_WIDTH;
            int bgHeight = BG_HEIGHT;
            int x = (this.width - bgWidth) / 2;
            int y = (this.height - bgHeight) / 2;

            int containerX = x + 16;
            int containerY = y + 120;
            int containerWidth = bgWidth - 32;

            java.util.List<net.minecraft.util.FormattedCharSequence> lines = new java.util.ArrayList<>();
            String[] paragraphs = page.description.split("\n", -1);
            for (String p : paragraphs) {
                if (p.isEmpty()) {
                    lines.add(net.minecraft.util.FormattedCharSequence.EMPTY);
                } else {
                    lines.addAll(this.font.split(Component.literal(p), bgWidth - 44));
                }
            }
            int lineHeight = this.font.lineHeight + 1;
            int totalLines = lines.size();
            int baseY = y + bgHeight - 30;
            int containerBottom = baseY - 6;
            int containerHeight = Math.max(24, containerBottom - containerY);
            int maxVisibleLines = Math.max(1, (containerHeight - 8) / lineHeight);
            int visibleLines = totalLines <= 0 ? 0 : Math.min(maxVisibleLines, totalLines);
            boolean inside = mx >= containerX && mx <= containerX + containerWidth && my >= containerY && my <= containerY + containerHeight;
            if (inside && totalLines > visibleLines) {
                int maxScroll = totalLines - visibleLines;
                int dir = delta > 0 ? -1 : 1;
                descScroll = Math.max(0, Math.min(maxScroll, descScroll + dir));
            }
        }
        return super.mouseScrolled(mx, my, delta);
    }

    private record RecipePage(String title, String description, ItemStack[][] grid, ItemStack result) {

        void render(GuiGraphics g, int x, int y, int slotBg, int slotOutline) {
            int startX = x + 26;
            int startY = y + 44;
            int slotSize = 20;

            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int sx = startX + col * slotSize;
                    int sy = startY + row * slotSize;
                    g.fill(sx, sy, sx + 18, sy + 18, slotBg);
                    g.renderOutline(sx, sy, 18, 18, slotOutline);
                }
            }

            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    ItemStack stack = grid[row][col];
                    if (stack != null && !stack.isEmpty()) {
                        int sx = startX + col * slotSize + 1;
                        int sy = startY + row * slotSize + 1;
                        g.renderItem(stack, sx, sy);
                    }
                }
            }

            int outX = x + 158;
            int outY = y + 64;
            g.drawString(Minecraft.getInstance().font, "→", x + 126, y + 70, 0xFFAAAAAA);
            g.fill(outX, outY, outX + 18, outY + 18, slotBg);
            g.renderOutline(outX, outY, 18, 18, 0xFF7A7A7A);
            if (result != null && !result.isEmpty()) {
                g.renderItem(result, outX + 1, outY + 1);
            }
        }

        static RecipePage biomeChanger() {
            ItemStack oak = new ItemStack(net.minecraft.world.item.Items.OAK_LOG);
            ItemStack spruce = new ItemStack(net.minecraft.world.item.Items.SPRUCE_LOG);
            ItemStack birch = new ItemStack(net.minecraft.world.item.Items.BIRCH_LOG);
            ItemStack jungle = new ItemStack(net.minecraft.world.item.Items.JUNGLE_LOG);
            ItemStack acacia = new ItemStack(net.minecraft.world.item.Items.ACACIA_LOG);
            ItemStack darkOak = new ItemStack(net.minecraft.world.item.Items.DARK_OAK_LOG);
            ItemStack mangrove = new ItemStack(net.minecraft.world.item.Items.MANGROVE_LOG);
            ItemStack cherry = new ItemStack(net.minecraft.world.item.Items.CHERRY_LOG);
            ItemStack grass = new ItemStack(net.minecraft.world.item.Items.GRASS_BLOCK);
            ItemStack[][] grid = new ItemStack[3][3];

            grid[0][0] = oak;
            grid[0][1] = spruce;
            grid[0][2] = birch;
            grid[1][0] = jungle;
            grid[1][1] = grass;
            grid[1][2] = acacia;
            grid[2][0] = darkOak;
            grid[2][1] = mangrove;
            grid[2][2] = cherry;

            ItemStack result = new ItemStack(IndoZooTycoon.BIOME_CHANGER_ITEM.get());
            String desc = "Mengubah biome area kandang agar cocok dengan hewan.\n"
                    + "Gunakan untuk membuat lingkungan lebih realistis.";
            return new RecipePage("Biome Changer", desc, grid, result);
        }

        static RecipePage trashCan() {
            ItemStack iron = new ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
            ItemStack[][] grid = new ItemStack[3][3];

            grid[0][0] = iron;
            grid[0][1] = iron;
            grid[0][2] = iron;
            grid[1][0] = iron;
            grid[1][2] = iron;
            grid[2][0] = iron;
            grid[2][1] = iron;
            grid[2][2] = iron;

            ItemStack result = new ItemStack(IndoZooTycoon.TRASH_CAN_ITEM.get());
            String desc = "Tempat sampah resmi kebun binatang.\n"
                    + "Membantu menjaga area bersih dan menjaga rating tetap tinggi.";
            return new RecipePage("Trash Can", desc, grid, result);
        }

        static RecipePage animalTag() {
            ItemStack iron = new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET);
            ItemStack paper = new ItemStack(net.minecraft.world.item.Items.PAPER);
            ItemStack[][] grid = new ItemStack[3][3];

            grid[0][1] = iron;
            grid[1][1] = paper;
            grid[2][1] = iron;

            ItemStack result = new ItemStack(IndoZooTycoon.ANIMAL_TAG_ITEM.get());
            String desc = "Menandai hewan agar terdaftar di sistem kebun binatang.\n"
                    + "Digunakan untuk menghitung jumlah hewan dan distribusi pengunjung.";
            return new RecipePage("Animal Tag", desc, grid, result);
        }

        static RecipePage mobCage() {
            ItemStack iron = new ItemStack(net.minecraft.world.item.Items.IRON_BARS);
            ItemStack chest = new ItemStack(net.minecraft.world.item.Items.CHEST);
            ItemStack[][] grid = new ItemStack[3][3];

            grid[0][0] = iron;
            grid[0][1] = iron;
            grid[0][2] = iron;
            grid[1][0] = iron;
            grid[1][1] = chest;
            grid[1][2] = iron;
            grid[2][0] = iron;
            grid[2][1] = iron;
            grid[2][2] = iron;

            ItemStack result = new ItemStack(IndoZooTycoon.CAPTURE_CAGE_ITEM.get());
            String desc = "Kandang portabel untuk menangkap dan memindahkan hewan.\n"
                    + "Sangat berguna saat merapikan tata letak kandang.";
            return new RecipePage("Mob Cage", desc, grid, result);
        }

        static RecipePage toilet() {
            ItemStack iron = new ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
            ItemStack quartz = new ItemStack(net.minecraft.world.item.Items.QUARTZ);
            ItemStack bucket = new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET);
            ItemStack[][] grid = new ItemStack[3][3];

            grid[0][0] = iron;
            grid[0][1] = quartz;
            grid[0][2] = iron;
            grid[1][0] = quartz;
            grid[1][1] = bucket;
            grid[1][2] = quartz;
            grid[2][0] = iron;
            grid[2][1] = quartz;
            grid[2][2] = iron;

            ItemStack result = new ItemStack(IndoZooTycoon.RESTROOM_ITEM.get());
            String desc = "Toilet tidak dijual di toko.\n"
                    + "Craft langsung untuk fasilitas pengunjung agar rating kebun binatang tetap stabil.";
            return new RecipePage("Toilet", desc, grid, result);
        }

        static RecipePage zooBanner() {
            ItemStack wool = new ItemStack(net.minecraft.world.item.Items.WHITE_WOOL);
            ItemStack gold = new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT);
            ItemStack stick = new ItemStack(net.minecraft.world.item.Items.STICK);
            ItemStack[][] grid = new ItemStack[3][3];

            grid[0][0] = wool;
            grid[0][1] = wool;
            grid[0][2] = wool;
            grid[1][0] = wool;
            grid[1][1] = gold;
            grid[1][2] = wool;
            grid[2][1] = stick;

            ItemStack result = new ItemStack(IndoZooTycoon.ZOO_BANNER_ITEM.get());
            String desc = "Banner kebun binatang sekarang hanya melalui crafting.\n"
                    + "Gunakan sebagai penanda visual area utama kebun binatang.";
            return new RecipePage("Zoo Banner", desc, grid, result);
        }
    }
}
