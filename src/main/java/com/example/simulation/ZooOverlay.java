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
        // Use global ZooData from client level
        // Since we are on client, we need a way to get the synced data.
        // ZooData.get(level) works on server. On client, it returns a new instance
        // unless synced.
        // We typically sync balance via packets or partial data.
        // For simplicity in this "mock" context allowing direct access if SinglePlayer?
        // Or better, we can read from a ClientZooData singleton updated by packets.
        // BUT, since we just made ZooData.get(level) return new ZooData() on client,
        // it won't have the correct balance unless we sync it.

        // However, the user wants to remove "Computer" dependency.
        // Let's assume for now we use a simple placeholder or cached value if strictly
        // client-side.
        // Actually, we should check if we can access the server data in singleplayer
        // or if we have a packet handling mechanism.

        // Wait, OpenGuiPacket sends data to Menu.
        // To display on Overlay, we need a separate periodic sync packet (Clientbound).
        // OR we can just display "0" if not synced, or use a workaround.

        // Given constraints, I will implement a basic `ClientZooData` mechanism or just
        // try to read directly if logically possible (e.g. valid checks).

        // For now, let's use a static field in ZooData that is updated by packet?
        // Let's add that.

        int balance = ClientZooData.getBalance();

        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
        gfx.drawString(mc.font, "Rp " + nf.format(balance), x, y, 0xFF55FF55, true);

    }

}
