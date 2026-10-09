package com.example.simulation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TutorialScreen extends Screen {

    public TutorialScreen() {
        super(Component.literal("Tutorial UI"));
    }

    @Override
    protected void init() {
        // Method ini dipanggil saat layar dibuka atau di-resize
        
        // Hitung posisi tengah
        int btnWidth = 200;
        int btnHeight = 20;
        int x = (this.width - btnWidth) / 2;
        int y = (this.height - btnHeight) / 2;

        // 1. Tambah Tombol Sederhana
        addRenderableWidget(Button.builder(Component.literal("Klik Saya!"), button -> {
            // Aksi saat tombol diklik
            this.minecraft.player.sendSystemMessage(Component.literal("Halo dari Button!"));
        }).bounds(x, y, btnWidth, btnHeight).build());

        // 2. Tambah Tombol Close
        addRenderableWidget(Button.builder(Component.literal("Tutup"), button -> {
            this.onClose();
        }).bounds(x, y + 30, btnWidth, btnHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Gambar Background (Gelap transparan standar Minecraft)
        this.renderBackground(guiGraphics);

        // 2. Gambar Teks Judul
        // Format: (font, text, x, y, color)
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 0xFFD700); // Warna Emas
        
        guiGraphics.drawCenteredString(this.font, "Ini adalah layar custom pertamamu!", this.width / 2, 60, 0xFFFFFF);

        // 3. Gambar semua widget (tombol, dll) yang sudah di-add di init()
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false; // Set true jika ingin game pause saat UI dibuka
    }
}
