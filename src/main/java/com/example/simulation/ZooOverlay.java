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

        // 1. Draw Balance (Original Logic)
        drawBalance(mc, gfx);

        // 2. Draw Mob Info if Looking at Animal
        if (mc.hitResult instanceof EntityHitResult ehr && ehr.getType() == HitResult.Type.ENTITY) {
            Entity target = ehr.getEntity();
            if (target instanceof Animal animal) {
                drawAnimalInfo(mc, gfx, animal, w, h);
            }
        }
    };

    private static void drawBalance(Minecraft mc, GuiGraphics gfx) {
        ZooComputerBlockEntity comp = findNearestComputer(mc.level, mc.player.blockPosition());
        if (comp == null)
            return;

        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
        int x = 10, y = 10;

        // Background
        gfx.fill(x - 5, y - 5, x + 150, y + 20, 0xAA000000); // 0xAA for semi-transparent
        gfx.fill(x - 5, y - 5, x + 150, y - 3, 0xFF6C63FF); // Header strip

        // Text
        gfx.drawString(mc.font, "💰 Saldo: Rp " + nf.format(comp.getBalance()), x, y + 2, 0xFF55FF55, true);
    }

    private static void drawAnimalInfo(Minecraft mc, GuiGraphics gfx, Animal animal, int w, int h) {
        int centerX = w / 2;
        int centerY = h / 2;
        int panelWidth = 140;
        int panelHeight = 50; // Dynamic height based on content
        int x = centerX + 20; // Offset significantly to the right of crosshair
        int y = centerY - 25;

        // Background Panel
        gfx.fill(x, y, x + panelWidth, y + panelHeight, 0xCC000000);

        // --- 1. Name & Category ---
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(animal.getType());
        String name = animal.getDisplayName().getString();
        String category = "Vanilla";
        int nameColor = 0xFFFFFF;

        // Check ZAWA Integration
        if (ZAWAIntegration.isZAWALoaded() && id != null && ZAWAIntegration.isZAWAAnimal(id)) {
            ZAWAIntegration.ZAWAAnimalData data = ZAWAIntegration.getZAWAAnimal(id);
            if (data != null) {
                category = data.category.toUpperCase();
                nameColor = 0xFFD700; // Gold for ZAWA animals
            }
        }

        // Draw Name
        gfx.drawString(mc.font, name, x + 5, y + 5, nameColor, true);

        // Draw Category Tag
        float scale = 0.7f;
        gfx.pose().pushPose();
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(mc.font, "[" + category + "]", (int) ((x + 5) / scale), (int) ((y + 15) / scale), 0xAAAAAA,
                true);
        gfx.pose().popPose();

        // --- 2. Health Bar ---
        float health = animal.getHealth();
        float maxHealth = animal.getMaxHealth();
        int healthPercentage = (int) ((health / maxHealth) * 100);

        int barX = x + 5;
        int barY = y + 25;
        int filledWidth = (int) ((health / maxHealth) * (panelWidth - 10));

        // Bar Background
        gfx.fill(barX, barY, barX + panelWidth - 10, barY + 5, 0xFF333333);
        // Bar Foreground (Color based on health)
        int healthColor = healthPercentage > 50 ? 0xFF00FF00 : (healthPercentage > 20 ? 0xFFFFFF00 : 0xFFFF0000);
        gfx.fill(barX, barY, barX + filledWidth, barY + 5, healthColor);

        // Health Text
        gfx.pose().pushPose();
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(mc.font, "HP: " + (int) health + "/" + (int) maxHealth, (int) ((barX) / scale),
                (int) ((barY + 7) / scale), 0xFFDDDD, true);
        gfx.pose().popPose();

        // --- ZAWA SPECIFIC STATS ---
        if (ZAWAIntegration.isZAWALoaded() && ZAWAIntegration.isZAWAAnimal(id)) {
            drawZAWAStatus(mc, gfx, animal, barX, barY + 15, scale);
        }

        // --- 3. Interaction Hint ---
        if (animal.isBaby()) {
            gfx.pose().pushPose();
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(mc.font, "(Baby)", (int) ((x + panelWidth - 30) / scale), (int) ((y + 5) / scale), 0xFFAAFF,
                    true);
            gfx.pose().popPose();
        }
    }

    private static void drawZAWAStatus(Minecraft mc, GuiGraphics gfx, Animal animal, int x, int y, float scale) {
        // Read NBT Data safely
        net.minecraft.nbt.CompoundTag nbt = new net.minecraft.nbt.CompoundTag();
        try {
            animal.saveWithoutId(nbt);
        } catch (Exception e) {
            return; // Fail safe
        }

        // Try to read common ZAWA tags
        // Hunger: usually 0-100 or 0-1000
        int hunger = nbt.contains("Hunger") ? nbt.getInt("Hunger")
                : (nbt.contains("hunger") ? nbt.getInt("hunger") : -1);
        // Enrichment: usually 0-100
        int enrichment = nbt.contains("Enrichment") ? nbt.getInt("Enrichment")
                : (nbt.contains("enrichment") ? nbt.getInt("enrichment") : -1);

        int yOffset = 0;

        if (hunger != -1) {
            // Normalize assuming max is 100 or check logic
            // Usually hunger goes UP as they get hungry, or standard Minecraft food logic
            // (20 max)
            // Let's assume standard 0-100 scale for modded animals
            int max = 100;
            int w = 80;
            int h = 4;

            gfx.fill(x, y + yOffset, x + w, y + yOffset + h, 0xFF330000);
            int fill = (int) ((hunger / (float) max) * w);
            // Red bar for hunger
            gfx.fill(x, y + yOffset, x + fill, y + yOffset + h, 0xFFFF5555);

            gfx.pose().pushPose();
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(mc.font, "Hunger", (int) ((x + w + 5) / scale), (int) ((y + yOffset) / scale), 0xFFFFFF,
                    true);
            gfx.pose().popPose();
            yOffset += 8;
        }

        if (enrichment != -1) {
            int max = 100;
            int w = 80;
            int h = 4;

            gfx.fill(x, y + yOffset, x + w, y + yOffset + h, 0xFF000033);
            int fill = (int) ((enrichment / (float) max) * w);
            // Blue/Purple bar for enrichment
            gfx.fill(x, y + yOffset, x + fill, y + yOffset + h, 0xFF5555FF);

            gfx.pose().pushPose();
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(mc.font, "Fun", (int) ((x + w + 5) / scale), (int) ((y + yOffset) / scale), 0xFFFFFF, true);
            gfx.pose().popPose();
        }
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
