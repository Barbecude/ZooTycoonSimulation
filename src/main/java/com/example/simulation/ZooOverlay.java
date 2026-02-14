package com.example.simulation;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * ZooOverlay — HUD Overlay yang menampilkan saldo & info zoo
 * di pojok kiri atas layar pemain.
 *
 * Hanya muncul jika pemain berada di sekitar ZooComputerBlockEntity.
 */
public class ZooOverlay {

    private static final int HUD_SEARCH_RADIUS = 50;

    public static final IGuiOverlay HUD_OVERLAY = (gui, guiGraphics, partialTick, width, height) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        // Cari ZooComputer terdekat
        BlockPos playerPos = mc.player.blockPosition();
        ZooComputerBlockEntity nearestComputer = findNearestComputer(mc.level, playerPos);

        if (nearestComputer == null)
            return; // Tidak ada komputer dekat → sembunyikan HUD

        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));

        // ========== Render HUD ==========
        int x = 10;
        int y = 10;
        int lineHeight = 12;

        guiGraphics.drawString(mc.font,
                "🦁 IndoZoo Tycoon", x, y, 0xFFFFAA00);
        y += lineHeight;

        guiGraphics.drawString(mc.font,
                "💰 Saldo: Rp " + nf.format(nearestComputer.getBalance()),
                x, y, 0xFF55FF55);
        y += lineHeight;

        guiGraphics.drawString(mc.font,
                "🐾 Hewan: " + nearestComputer.getCachedAnimalCount()
                        + "  👷 Staff: " + nearestComputer.getCachedStaffCount()
                        + "  🧑 Visitor: " + nearestComputer.getCachedVisitorCount(),
                x, y, 0xFF55FFFF);
        y += lineHeight;

        guiGraphics.drawString(mc.font,
                "📡 Radius: " + nearestComputer.getScanRadius() + " blok",
                x, y, 0xFFAAAAAA);
    };

    /**
     * Mencari ZooComputerBlockEntity terdekat dari posisi pemain.
     * Scan terbatas agar tidak lag.
     */
    private static ZooComputerBlockEntity findNearestComputer(Level level, BlockPos center) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        double closest = Double.MAX_VALUE;
        ZooComputerBlockEntity found = null;

        // Scan area kecil (±HUD_SEARCH_RADIUS, ±5 Y) untuk cari komputer
        int r = HUD_SEARCH_RADIUS;
        for (int x = -r; x <= r; x += 4) { // Step 4 untuk performa
            for (int z = -r; z <= r; z += 4) {
                for (int y = -5; y <= 5; y++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockEntity be = level.getBlockEntity(mutable);
                    if (be instanceof ZooComputerBlockEntity comp) {
                        double d = center.distSqr(mutable);
                        if (d < closest) {
                            closest = d;
                            found = comp;
                        }
                    }
                }
            }
        }
        return found;
    }
}
