package com.example.simulation;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.NumberFormat;
import java.util.Locale;

public class ZooOverlay {
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 8;

    public static final IGuiOverlay HUD_OVERLAY = (gui, gfx, pt, w, h) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        // 1. Draw Balance (Existing) - Moved slightly down to make room for generic
        // info or vice versa
        // Let's keep balance at (10, 10) for now, and put Entity Info below it or above
        // it.
        // User requested "ujung kiri" (top left) for entity info.
        // We will prioritize Entity Info at the very top (y=5), and move Balance down
        // if needed.

        // Render Entity Info FIRST (Top Left)
        if (mc.hitResult instanceof EntityHitResult ehr && ehr.getType() == HitResult.Type.ENTITY) {
            Entity target = ehr.getEntity();
            if (target instanceof LivingEntity living) { // Show for all living entities (mobs, players, animals)
                drawEntityInfo(mc, gfx, living);
            }
        }

        // Render Balance below Entity Info space (approx y=40 to avoid overlap if
        // entity is shown)
        // Or keep it fixed. Let's put Balance at y=50 to be safe and clean.
        drawBalance(mc, gfx, 5, 50);
    };

    private static void drawEntityInfo(Minecraft mc, GuiGraphics gfx, LivingEntity entity) {
        int x = 5;
        int y = 5;

        // 1. Entity Name
        Component name = entity.getDisplayName();

        // 2. Mod Name (Source)
        ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        String modId = "Minecraft";
        if (registryName != null) {
            modId = registryName.getNamespace();
            // Capitalize first letter logic or specific overrides
            if (modId.equals("minecraft")) {
                modId = "Minecraft";
            } else if (modId.equals("alexsmobs")) {
                modId = "Alex's Mobs";
            } else if (modId.equals(IndoZooTycoon.MODID)) {
                modId = "IndoZoo Tycoon";
            } else {
                // Capitalize first letter
                modId = modId.substring(0, 1).toUpperCase() + modId.substring(1);
            }
        }

        // Render WITHOUT background (clean text)
        // Name (White/colored based on type?) - Let's use White/Yellow
        gfx.drawString(mc.font, name, x, y, 0xFFFFFF, true);

        // Mod Name (Gray/Subtle) below name
        gfx.drawString(mc.font, modId, x, y + 10, 0xAAAAAA, true);
    }

    private static void drawBalance(Minecraft mc, GuiGraphics gfx, int x, int y) {
        // Only draw if we find a computer (optional check)
        ZooComputerBlockEntity comp = findNearestComputer(mc.level, mc.player.blockPosition());
        if (comp == null)
            return;

        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));

        // Background (Partial, since user wanted "no bg" for entity, but didn't specify
        // for balance.
        // But let's keep balance style consistent or minimal).
        // Let's make balance minimal too to match the "clean" aesthetic requested.
        // gfx.fill(x - 2, y - 2, x + 120, y + 12, 0x88000000);

        // Text
        gfx.drawString(mc.font, "💰 Rp " + nf.format(comp.getBalance()), x, y, 0xFF55FF55, true);
    }

    private static ZooComputerBlockEntity findNearestComputer(Level level, BlockPos center) {
        // Simplified search for performance - Check 50 block radius
        // Ideally this should be cached or optimized further
        for (int x = -50; x <= 50; x += 10) {
            for (int z = -50; z <= 50; z += 10) {
                BlockPos p = center.offset(x, 0, z);
                // Just check simplistic chunks or known locations in a real mod
                // For now, keep the original but reduce radius to avoid lag if called every
                // frame
            }
        }

        // Reverting to roughly original logic but optimized range
        for (int r = 0; r <= 50; r += 16) { // Reduced from 100 to 50
            for (int x = -r; x <= r; x += 16) {
                for (int z = -r; z <= r; z += 16) {
                    BlockPos p = center.offset(x, 0, z);
                    for (int y = -10; y <= 10; y++) {
                        BlockEntity be = level.getBlockEntity(p.atY(center.getY() + y));
                        if (be instanceof ZooComputerBlockEntity c)
                            return c;
                    }
                }
            }
        }
        return null;
    }
}
