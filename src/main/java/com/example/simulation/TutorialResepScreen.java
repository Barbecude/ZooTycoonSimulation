package com.example.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Tutorial & Resep screen that replaces the old "Zoo Info" button.
 *
 * Three tabs:
 * - Resep : crafting recipes (delegates to ZooRecipeScreen pages inline)
 * - Pengeluaran : scrollable cost/income breakdown
 * - Tips : zoo rating tips
 */
public class TutorialResepScreen extends Screen {

    private final Screen parent;
    private int activeTab = 0; // 0=Resep, 1=Pengeluaran, 2=Tips

    // Resep sub-screen
    private ZooRecipeScreen recipeDelegate;
    private net.minecraft.client.gui.components.Button recipeNavLeft;
    private net.minecraft.client.gui.components.Button recipeNavRight;

    // Scroll state for Pengeluaran and Tips
    private int pengeluaranScroll = 0;
    private int tipsScroll = 0;

    private static final int BG_WIDTH = 320;
    private static final int BG_HEIGHT = 280;

    private static final int TAB_COUNT = 3;
    private static final String[] TAB_LABELS = { "Resep", "Pengeluaran", "Tips" };

    // Build pengeluaran lines once
    private final List<String[]> pengeluaranRows = new ArrayList<>();
    private final List<String> tipLines = new ArrayList<>();

    public TutorialResepScreen(Screen parent) {
        super(Component.literal("Tutorial & Resep"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        pengeluaranRows.clear();
        tipLines.clear();

        // ---- Pengeluaran Data ---- built dynamically in renderPengeluaran()

        // ---- Tips Data ----
        tipLines.add("== Tips Meningkatkan Rating Zoo ==");
        tipLines.add("");
        tipLines.add("1. Pastikan ada hewan yang terdaftar (Animal Tag).");
        tipLines.add("   Semakin banyak hewan unik, semakin tinggi rating.");
        tipLines.add("");
        tipLines.add("2. Jaga kadar lapar hewan. Hire Zookeeper dan assign");
        tipLines.add("   ke jenis hewan tertentu. Zookeeper akan otomatis");
        tipLines.add("   mengisi Animal Feeder.");
        tipLines.add("");
        tipLines.add("3. Pasang shelf (Oak Shelf / Standing Oak Shelf) dan");
        tipLines.add("   isi dengan makanan (Burger / Es Krim). Visitor lapar");
        tipLines.add("   akan datang membeli. Pendapatan naik, rating ikut naik.");
        tipLines.add("");
        tipLines.add("4. Pasang Toilet agar pengunjung dengan mood TOILET");
        tipLines.add("   tidak pergi & tidak menurunkan rating.");
        tipLines.add("");
        tipLines.add("5. Hire Janitor untuk membersihkan sampah yang ditinggal");
        tipLines.add("   pengunjung. Sampah menurunkan rating.");
        tipLines.add("");
        tipLines.add("6. Hire Security untuk mengusir Hunter (kidnapper/archer)");
        tipLines.add("   yang mencoba mencuri atau menembak hewan.");
        tipLines.add("");
        tipLines.add("7. Rating max 100. Tiap kondisi buruk (hewan lapar,");
        tipLines.add("   terlalu banyak sampah, pengunjung kecewa) akan");
        tipLines.add("   mengurangi rating secara bertahap.");

        int bx = (this.width - BG_WIDTH) / 2;
        int by = (this.height - BG_HEIGHT) / 2;
        int baseY = by + BG_HEIGHT - 28;

        // Tab buttons
        int tabW = BG_WIDTH / TAB_COUNT;
        for (int i = 0; i < TAB_COUNT; i++) {
            final int idx = i;
            this.addRenderableWidget(Button.builder(Component.literal(TAB_LABELS[i]), b -> {
                activeTab = idx;
                pengeluaranScroll = 0;
                tipsScroll = 0;
                if (recipeNavLeft != null)  recipeNavLeft.visible  = (activeTab == 0);
                if (recipeNavRight != null) recipeNavRight.visible = (activeTab == 0);
            }).bounds(bx + i * tabW, by + 2, tabW - 2, 16).build());
        }

        // Close button (only 1 — delegate mode prevents ZooRecipeScreen adding its own)
        this.addRenderableWidget(Button.builder(Component.literal("Tutup"), b -> onClose())
                .bounds(bx + BG_WIDTH / 2 - 30, baseY, 60, 18).build());

        // Recipe navigation buttons — live in TutorialResepScreen, control the delegate
        recipeNavLeft = Button.builder(Component.literal("<"), b -> {
            if (recipeDelegate != null) recipeDelegate.prevPage();
        }).bounds(bx + BG_WIDTH / 2 - 80, baseY, 20, 18).build();
        recipeNavRight = Button.builder(Component.literal(">"), b -> {
            if (recipeDelegate != null) recipeDelegate.nextPage();
        }).bounds(bx + BG_WIDTH / 2 + 60, baseY, 20, 18).build();
        recipeNavLeft.visible  = (activeTab == 0);
        recipeNavRight.visible = (activeTab == 0);
        this.addRenderableWidget(recipeNavLeft);
        this.addRenderableWidget(recipeNavRight);

        // Init delegate in delegate mode: it won't add its own buttons or background
        recipeDelegate = new ZooRecipeScreen(this);
        recipeDelegate.setDelegateMode(true);
        recipeDelegate.init(Minecraft.getInstance(), this.width, this.height);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);

        int bx = (this.width - BG_WIDTH) / 2;
        int by = (this.height - BG_HEIGHT) / 2;

        // Background
        g.fill(bx, by, bx + BG_WIDTH, by + BG_HEIGHT, 0xCC1A1A1A);
        g.renderOutline(bx, by, BG_WIDTH, BG_HEIGHT, 0xFF4A90E2);

        // Title
        String title = "Tutorial & Resep";
        g.drawCenteredString(this.font, title, this.width / 2, by - 12, 0xFFFFFFFF);

        // Sync recipe nav button visibility each frame
        if (recipeNavLeft != null)  recipeNavLeft.visible  = (activeTab == 0);
        if (recipeNavRight != null) recipeNavRight.visible = (activeTab == 0);

        // Tab highlight
        int tabW = BG_WIDTH / TAB_COUNT;
        for (int i = 0; i < TAB_COUNT; i++) {
            int tx = bx + i * tabW;
            int ty = by + 2;
            if (i == activeTab) {
                g.fill(tx, ty, tx + tabW - 2, ty + 16, 0xFF3D5FA0);
                g.renderOutline(tx, ty, tabW - 2, 16, 0xFF7AB4FF);
            }
        }

        int contentX = bx + 10;
        int contentY = by + 24;
        int contentW = BG_WIDTH - 20;
        int contentH = BG_HEIGHT - 52;

        g.fill(contentX - 2, contentY - 2, contentX + contentW + 2, contentY + contentH + 2, 0x88000000);
        g.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);

        switch (activeTab) {
            case 0 -> renderResep(g, contentX, contentY, contentW, contentH, mx, my, pt);
            case 1 -> renderPengeluaran(g, contentX, contentY, contentW, contentH);
            case 2 -> renderTips(g, contentX, contentY, contentW, contentH);
        }

        g.disableScissor();

        super.render(g, mx, my, pt);
    }

    private void renderResep(GuiGraphics g, int x, int y, int w, int h, int mx, int my, float pt) {
        // Delegate rendering to ZooRecipeScreen (it renders relative to screen center)
        recipeDelegate.render(g, mx, my, pt);
    }

    private void renderPengeluaran(GuiGraphics g, int x, int y, int w, int h) {
        // Rebuild live rows from ClientZooData every call so the tab stays current
        pengeluaranRows.clear();
        net.minecraft.nbt.ListTag log = ClientZooData.getTransactionLog();
        if (log.isEmpty()) {
            pengeluaranRows.add(new String[] { "KATEGORI", "KETERANGAN", "JUMLAH" });
            pengeluaranRows.add(new String[] { "", "(Belum ada transaksi)", "" });
        } else {
            pengeluaranRows.add(new String[] { "KATEGORI", "KETERANGAN", "JUMLAH" });
            // Newest first
            for (int i = log.size() - 1; i >= 0; i--) {
                net.minecraft.nbt.CompoundTag entry = log.getCompound(i);
                String cat = entry.getString("cat");
                String desc = entry.getString("desc");
                int amount = entry.getInt("amount");
                String amountStr = (amount >= 0 ? "+" : "") + "Rp " + String.format("%,d", amount);
                pengeluaranRows.add(new String[] { cat, desc, amountStr });
            }
        }

        int lineH = this.font.lineHeight + 3;
        int maxVisible = h / lineH;
        int total = pengeluaranRows.size();
        pengeluaranScroll = Math.max(0, Math.min(pengeluaranScroll, Math.max(0, total - maxVisible)));

        int[] colX = { x, x + 90, x + w - 130 };
        int[] colW = { 85, w - 135 - 90, 125 };

        for (int i = 0; i < maxVisible; i++) {
            int idx = pengeluaranScroll + i;
            if (idx >= total)
                break;
            String[] row = pengeluaranRows.get(idx);
            int ry = y + i * lineH;

            if (row[0].startsWith("---")) {
                // Section header
                g.fill(x, ry - 1, x + w, ry + lineH - 1, 0x44336699);
                g.drawString(this.font, row[0], x + 2, ry + 1, 0xFF88BBFF, false);
            } else if (row[0].equals("KATEGORI")) {
                // Table header
                g.fill(x, ry - 1, x + w, ry + lineH - 1, 0x55444444);
                for (int c = 0; c < 3; c++) {
                    g.drawString(this.font, row[c], colX[c], ry + 1, 0xFFFFD369, false);
                }
            } else {
                // Colour income green, expense red
                int rowColor;
                if (row[0].equals("Pendapatan")) {
                    rowColor = 0xFF5BFF5B;
                } else if (row[0].equals("Pengeluaran")) {
                    rowColor = 0xFFFF7070;
                } else {
                    rowColor = idx % 2 == 0 ? 0xFFDDDDDD : 0xFFBBBBBB;
                }
                for (int c = 0; c < 3; c++) {
                    String text = trimTo(row[c], colW[c]);
                    g.drawString(this.font, text, colX[c], ry + 1, rowColor, false);
                }
            }
        }

        // Scrollbar
        if (total > maxVisible) {
            int barX = x + w + 2;
            int barH = h;
            int thumbH = Math.max(8, (int) (barH * (maxVisible / (float) total)));
            int maxScroll = total - maxVisible;
            int thumbY = y + (int) ((barH - thumbH) * (pengeluaranScroll / (float) maxScroll));
            g.fill(barX, y, barX + 4, y + barH, 0x55222222);
            g.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    private void renderTips(GuiGraphics g, int x, int y, int w, int h) {
        int lineH = this.font.lineHeight + 2;
        int maxVisible = h / lineH;
        int total = tipLines.size();
        tipsScroll = Math.max(0, Math.min(tipsScroll, Math.max(0, total - maxVisible)));

        for (int i = 0; i < maxVisible; i++) {
            int idx = tipsScroll + i;
            if (idx >= total)
                break;
            String line = tipLines.get(idx);
            int ly = y + i * lineH;
            int color = line.startsWith("==") ? 0xFF88BBFF : (line.isEmpty() ? 0xFF444444 : 0xFFDDDDDD);
            g.drawString(this.font, line, x + 2, ly + 1, color, false);
        }

        // Scrollbar
        if (total > maxVisible) {
            int barX = x + w + 2;
            int barH = h;
            int thumbH = Math.max(8, (int) (barH * (maxVisible / (float) total)));
            int maxScroll = total - maxVisible;
            int thumbY = y + (int) ((barH - thumbH) * (tipsScroll / (float) maxScroll));
            g.fill(barX, y, barX + 4, y + barH, 0x55222222);
            g.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int dir = delta > 0 ? -1 : 1;
        if (activeTab == 1)
            pengeluaranScroll = Math.max(0, pengeluaranScroll + dir);
        if (activeTab == 2)
            tipsScroll = Math.max(0, tipsScroll + dir);
        if (activeTab == 0)
            recipeDelegate.mouseScrolled(mx, my, delta);
        return true;
    }

    private String trimTo(String s, int maxW) {
        if (s == null)
            return "";
        if (this.font.width(s) <= maxW)
            return s;
        while (s.length() > 0 && this.font.width(s + "…") > maxW)
            s = s.substring(0, s.length() - 1);
        return s + "…";
    }
}
