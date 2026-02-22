package com.example.simulation;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.NumberFormat;
import java.util.Locale;

public class ZooOverlay {
    private static final ResourceLocation MONEY_BG = new ResourceLocation("indozoo", "textures/gui/money_bg.png");
    private static final ResourceLocation VANILLA_ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");
    private static Boolean hasVanillaFoodTexture;

    public static final IGuiOverlay HUD_OVERLAY = (gui, gfx, pt, w, h) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;
        drawBalance(mc, gfx, 5, 12);
        drawTargetAnimalInfo(mc, gfx, w, h);
    };

    private static void drawTargetAnimalInfo(Minecraft mc, GuiGraphics gfx, int screenWidth, int screenHeight) {
        HitResult hit = mc.hitResult;
        if (!(hit instanceof EntityHitResult entityHit))
            return;
        if (!(entityHit.getEntity() instanceof LivingEntity living))
            return;
        if (!ClientZooData.isTaggedAnimal(living))
            return;

        int hunger = ClientZooData.getTaggedAnimalHunger(living);
        if (hunger < 0)
            return;

        // Position: Top-right corner with small padding
        int panelWidth = 120;
        int panelHeight = 44;
        int panelX = screenWidth - panelWidth - 5;
        int panelY = 5;

        gfx.fill(panelX - 2, panelY - 2, panelX + panelWidth, panelY + panelHeight, 0x90000000);
        drawEntityInfo(mc, gfx, living, panelX, panelY);
        drawHungerBar(mc, gfx, panelX, panelY + 20, hunger);
    }

    private static void drawEntityInfo(Minecraft mc, GuiGraphics gfx, LivingEntity entity, int x, int y) {
        Component name = entity.getDisplayName();
        ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());

        // Get species name without prefix (alexsmobs:elephant -> Elephant,
        // minecraft:cow -> Cow)
        String speciesName = "";
        if (registryName != null) {
            speciesName = registryName.getPath();
            // Convert snake_case to Title Case
            String[] parts = speciesName.split("_");
            StringBuilder titleCase = new StringBuilder();
            for (String part : parts) {
                if (titleCase.length() > 0)
                    titleCase.append(" ");
                titleCase.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
            }
            speciesName = titleCase.toString();
        }

        gfx.drawString(mc.font, name, x, y, 0xFFF2EEDC, true);
        if (!speciesName.isEmpty()) {
            gfx.drawString(mc.font, speciesName, x, y + 10, 0xAAAAAA, true);
        }
    }

    private static void drawHungerBar(Minecraft mc, GuiGraphics gfx, int x, int y, int hunger) {
        // Always use Minecraft food icons (chicken leg) — same as dashboard AnimalCard
        // hunger: MAX_HUNGER=20 = full, 0 = starving
        final ResourceLocation ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");
        int filled = Math.max(0, Math.min(10, Math.round((hunger / (float) ZooAnimalHungerSystem.MAX_HUNGER) * 10.0F)));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int i = 0; i < 10; i++) {
            int drawX = x + (i * 9); // 9px spacing to avoid overlap
            // Empty icon (u=16, v=27)
            gfx.blit(ICONS, drawX, y, 16, 27, 9, 9, 256, 256);
            // Filled icon on top (u=52, v=27) — golden/yellow drumstick
            if (i < filled) {
                gfx.blit(ICONS, drawX, y, 52, 27, 9, 9, 256, 256);
            }
        }
        RenderSystem.disableBlend();
    }

    private static boolean hasVanillaFoodIcons(Minecraft mc) {
        if (hasVanillaFoodTexture == null) {
            hasVanillaFoodTexture = mc.getResourceManager().getResource(VANILLA_ICONS).isPresent();
        }
        return hasVanillaFoodTexture;
    }

    private static void drawBalance(Minecraft mc, GuiGraphics gfx, int x, int y) {
        int balance = ClientZooData.getBalance();
        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
        String text = "Rp " + nf.format(balance);

        int bgWidth = 100;
        int bgHeight = 20;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MONEY_BG);
        RenderSystem.enableBlend();

        int drawX = x + 5;
        int drawY = y;

        gfx.blit(MONEY_BG, drawX, drawY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        int textY = drawY + (bgHeight - 8) / 2;
        int textX = drawX + 10;

        gfx.drawString(mc.font, text, textX, textY, 0xFF4E342E, false);

        RenderSystem.disableBlend();
    }
}
