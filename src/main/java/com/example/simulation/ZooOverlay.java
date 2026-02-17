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
        drawRating(mc, gfx, 5, 70); 
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
        int balance = ClientZooData.getBalance();
        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
        String text = "Rp " + nf.format(balance);

        int bgWidth = 100; // Adjust based on texture
        int bgHeight = 20; // Adjust based on texture
        
        RenderSystem.setShaderTexture(0, new ResourceLocation("indozoo", "textures/gui/money_bg.png"));
        RenderSystem.enableBlend();
        
        int drawX = x + 5;
        int drawY = y;
        
        gfx.blit(new ResourceLocation("indozoo", "textures/gui/money_bg.png"), drawX, drawY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        int textY = drawY + (bgHeight - 8) / 2;
        int textX = drawX + 10; 
        
        gfx.drawString(mc.font, text, textX, textY, 0xFFFFFFFF, false);
        
        RenderSystem.disableBlend();
    }
}
