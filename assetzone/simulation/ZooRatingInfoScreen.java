package com.example.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ZooRatingInfoScreen extends Screen {

    private final Screen parent;
    private int scrollY = 0;
    private int maxScrollY = 0;
    private Button closeButton;

    public ZooRatingInfoScreen(Screen parent) {
        super(Component.literal("Cara Naikkan Rating"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int bgWidth = 340;
        int bgHeight = 250;
        int x = (this.width - bgWidth) / 2;
        int y = (this.height - bgHeight) / 2;
        int btnW = 60;
        int btnH = 20;
        int btnX = x + (bgWidth - btnW) / 2;
        int btnY = y + bgHeight - 30;

        this.closeButton = this.addRenderableWidget(Button.builder(Component.literal("Tutup"), b -> onClose())
                .bounds(btnX, btnY, btnW, btnH).build());
        this.scrollY = 0;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);

        int bgWidth = 340;
        int bgHeight = 250;
        int x = (this.width - bgWidth) / 2;
        int y = (this.height - bgHeight) / 2;

        int bg = 0xCC1A1A1A;
        int outline = 0xFF4A90E2;

        g.fill(x, y, x + bgWidth, y + bgHeight, bg);
        g.renderOutline(x, y, bgWidth, bgHeight, outline);

        String title = "Cara Naikkan Rating";
        int titleW = this.font.width(title);
        g.drawString(this.font, title, x + (bgWidth - titleW) / 2, y + 10, 0xFFFFFF);

        int viewX = x + 14;
        int viewY = y + 32;
        int viewW = bgWidth - 28;
        int viewH = bgHeight - 32 - 38;
        int lineGap = this.font.lineHeight + 2;
        int textMaxWidth = viewW - 10;

        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("Rating meningkat ketika:");
        appendWrappedLines(lines, "- Jumlah hewan di kebun binatang bertambah banyak", textMaxWidth);
        appendWrappedLines(lines, "- Jumlah sampah di area kebun binatang sedikit", textMaxWidth);
        lines.add("");
        lines.add("Rating menurun ketika:");
        appendWrappedLines(lines, "- Pengunjung tidak dapat menemukan tempat sampah", textMaxWidth);
        appendWrappedLines(lines, "- Pengunjung tidak dapat menemukan toilet", textMaxWidth);
        lines.add("");
        lines.add("Informasi tambahan:");
        appendWrappedLines(lines, "- Jumlah hewan yang sedikit bisa membuat pengunjung tidak datang atau hanya sedikit yang berkunjung", textMaxWidth);
        appendWrappedLines(lines, "- Pengunjung tidak selalu buang sampah dan tidak selalu ingin ke toilet", textMaxWidth);

        int totalHeight = lines.size() * lineGap;
        this.maxScrollY = Math.max(0, totalHeight - viewH);
        if (this.scrollY > this.maxScrollY) {
            this.scrollY = this.maxScrollY;
        }

        g.enableScissor(viewX, viewY, viewX + viewW, viewY + viewH);
        int drawY = viewY - this.scrollY;
        for (int i = 0; i < lines.size(); i++) {
            int yy = drawY + i * lineGap;
            if (yy + lineGap < viewY) continue;
            if (yy > viewY + viewH) break;
            int color = lines.get(i).endsWith(":") ? 0xFFFFFF : 0xDDDDDD;
            if (lines.get(i).isEmpty()) {
                continue;
            }
            g.drawString(this.font, lines.get(i), viewX, yy, color);
        }
        g.disableScissor();

        if (this.maxScrollY > 0) {
            int barWidth = 4;
            int barX = viewX + viewW - barWidth - 2;
            int barTop = viewY + 2;
            int barBottom = viewY + viewH - 2;
            int trackHeight = barBottom - barTop;
            int thumbHeight = Math.max(10, (int) (trackHeight * (viewH / (float) totalHeight)));
            int thumbOffset = (int) ((trackHeight - thumbHeight) * (this.scrollY / (float) this.maxScrollY));
            int thumbY = barTop + thumbOffset;
            g.fill(barX, barTop, barX + barWidth, barBottom, 0x55222222);
            g.fill(barX, thumbY, barX + barWidth, thumbY + thumbHeight, 0xFFAAAAAA);
        }

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int bgWidth = 340;
        int bgHeight = 250;
        int x = (this.width - bgWidth) / 2;
        int y = (this.height - bgHeight) / 2;
        int viewX = x + 14;
        int viewY = y + 32;
        int viewW = bgWidth - 28;
        int viewH = bgHeight - 32 - 38;
        boolean inside = mouseX >= viewX && mouseX <= viewX + viewW && mouseY >= viewY && mouseY <= viewY + viewH;

        if (inside && this.maxScrollY > 0) {
            int dir = delta > 0 ? -1 : 1;
            this.scrollY = Math.max(0, Math.min(this.maxScrollY, this.scrollY + dir * 10));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void appendWrappedLines(java.util.List<String> out, String text, int maxWidth) {
        String[] words = text.split(" ");
        java.util.List<String> lines = new java.util.ArrayList<>();

        String current = "";
        for (String w : words) {
            String candidate = current.isEmpty() ? w : current + " " + w;
            if (this.font.width(candidate) <= maxWidth) {
                current = candidate;
            } else {
                if (!current.isEmpty()) {
                    lines.add(current);
                }
                current = w;
            }
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }

        out.addAll(lines);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

