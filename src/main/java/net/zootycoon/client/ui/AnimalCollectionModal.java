package net.zootycoon.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.animal.habitat.HabitatProfile;
import net.zootycoon.animal.habitat.HabitatProfileLoader;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.license.ZooLicenseManager;
import net.zootycoon.util.ZooTranslations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalCollectionModal {

    private final MainManagementScreen parent;
    private final Map<String, LivingEntity> dummyEntityCache = new HashMap<>();
    private LicenseTier.SpeciesEntry selectedEntry = LicenseTier.TAMAN_MINI.getRequiredSpecies().get(0);
    private LicenseTier selectedTier = LicenseTier.TAMAN_MINI;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    public AnimalCollectionModal(MainManagementScreen parent) {
        this.parent = parent;
    }

    public void init() {
        this.scrollOffset = 0;
    }

    private LivingEntity getOrCreateDummy(String entityTypeId, EntityType<?> type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || type == null) return null;
        return dummyEntityCache.computeIfAbsent(entityTypeId, k -> {
            try {
                Entity e = type.create(mc.level);
                if (e instanceof LivingEntity le) {
                    return le;
                }
            } catch (Throwable ignored) {}
            return null;
        });
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int sw = parent.width;
        int sh = parent.height;
        Font font = parent.getFont();

        // Dark backdrop
        graphics.fill(0, 0, sw, sh, 0xB805070B);

        int modalW = Math.min(sw - 28, 540);
        int modalH = Math.min(sh - 24, 310);
        int modalX = (sw - modalW) / 2;
        int modalY = (sh - modalH) / 2;

        // Main Modal Frame
        graphics.fill(modalX, modalY, modalX + modalW, modalY + modalH, 0xF511151E);
        graphics.fill(modalX, modalY, modalX + modalW, modalY + 1, 0xFF2D3648);
        graphics.fill(modalX, modalY + modalH - 1, modalX + modalW, modalY + modalH, 0xFF2D3648);
        graphics.fill(modalX, modalY, modalX + 1, modalY + modalH, 0xFF2D3648);
        graphics.fill(modalX + modalW - 1, modalY, modalX + modalW, modalY + modalH, 0xFF2D3648);

        // Header Bar
        graphics.fill(modalX + 1, modalY + 1, modalX + modalW - 1, modalY + 28, 0xFF171D29);
        graphics.fill(modalX + 1, modalY + 28, modalX + modalW - 1, modalY + 29, 0xFF262F42);
        graphics.drawString(font, ZooTranslations.get("zootycoon.license.collection.title"), modalX + 12, modalY + 10, 0xFFFFD700, false);

        // Active License Summary in Header
        int activeTierLvl = ZooLicenseManager.getClientActiveTierLevel();
        LicenseTier activeTier = LicenseTier.byLevel(activeTierLvl);
        int missing = ZooLicenseManager.getClientMissingCountForActiveTier();
        int ownedInActive = activeTier.getRequiredSpecies().size() - missing;
        String activeSummary = activeTier.getDisplayName() + " (" + ownedInActive + "/" + activeTier.getRequiredSpecies().size() + ")";
        int sumW = font.width(activeSummary);
        graphics.drawString(font, activeSummary, modalX + modalW - sumW - 34, modalY + 10, activeTier.getAccentColor(), false);

        // Close [X] Button
        int closeX = modalX + modalW - 24;
        int closeY = modalY + 5;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18;
        graphics.fill(closeX, closeY, closeX + 18, closeY + 18, hoverClose ? 0xFFC53030 : 0xFF252D3D);
        graphics.drawCenteredString(font, "X", closeX + 9, closeY + 5, 0xFFFFFFFF);

        // Left Grid Area vs Right Detail Panel
        int detailW = 152;
        int gridX = modalX + 10;
        int gridY = modalY + 34;
        int gridW = modalW - detailW - 26;
        int gridH = modalH - 42;

        int cols = 4;
        int cardGap = 6;
        int cardW = (gridW - (cardGap * (cols - 1)) - 6) / cols;
        int cardH = 74;

        graphics.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);

        int curY = gridY - scrollOffset;
        for (LicenseTier tier : LicenseTier.values()) {
            boolean tierUnlocked = ZooLicenseManager.isClientTierUnlocked(tier.getTierLevel());
            ZooLicenseManager.TierProgressData tierData = ZooLicenseManager.getClientTierData(tier.getTierLevel());

            // Tier Section Header
            graphics.fill(gridX, curY, gridX + gridW - 6, curY + 18, 0xFF1A2130);
            graphics.fill(gridX, curY, gridX + 3, curY + 18, tier.getAccentColor());

            int ownedCount = 0;
            for (LicenseTier.SpeciesEntry sp : tier.getRequiredSpecies()) {
                if (ZooLicenseManager.isClientSpeciesOwned(sp.speciesKey())) {
                    ownedCount++;
                }
            }

            String tierHeader = "Tier " + tier.getTierLevel() + ": " + tier.getDisplayName() + " (" + ownedCount + "/" + tier.getRequiredSpecies().size() + ")";
            graphics.drawString(font, tierHeader, gridX + 8, curY + 5, tierUnlocked ? 0xFFFFFFFF : 0xFF9CA3AF, false);

            String tierBadgeKey = !tierUnlocked ? "zootycoon.license.state.locked"
                    : (tierData.licenseClaimed ? "zootycoon.license.state.certified" : "zootycoon.license.state.unverified");
            String tierBadge = ZooTranslations.get(tierBadgeKey);
            int badgeColor = !tierUnlocked ? 0xFFF87171 : (tierData.licenseClaimed ? 0xFF4ADE80 : 0xFFFBBF24);
            graphics.drawString(font, tierBadge, gridX + gridW - 12 - font.width(tierBadge), curY + 5, badgeColor, false);

            curY += 22;

            List<LicenseTier.SpeciesEntry> speciesList = tier.getRequiredSpecies();
            for (int i = 0; i < speciesList.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int cx = gridX + col * (cardW + cardGap);
                int cy = curY + row * (cardH + cardGap);

                if (cy + cardH >= gridY && cy <= gridY + gridH) {
                    renderSpeciesCard(graphics, font, tier, speciesList.get(i), tierUnlocked, cx, cy, cardW, cardH, mouseX, mouseY);
                }
            }

            int rows = (speciesList.size() + cols - 1) / cols;
            curY += rows * (cardH + cardGap) + 6;
        }

        graphics.disableScissor();

        int totalContentH = (curY + scrollOffset) - gridY;
        this.maxScroll = Math.max(0, totalContentH - gridH);
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, this.maxScroll);

        if (this.maxScroll > 0) {
            int trackX = gridX + gridW - 3;
            graphics.fill(trackX, gridY, trackX + 2, gridY + gridH, 0xFF1F2633);
            int thumbH = Math.max(18, (int) ((float) gridH * gridH / (float) totalContentH));
            int thumbY = gridY + (int) ((gridH - thumbH) * ((float) scrollOffset / (float) maxScroll));
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFF38BDF8);
        }

        // Right Detail Column
        int detailX = gridX + gridW + 6;
        int detailY = gridY;
        renderDetailPanel(graphics, font, detailX, detailY, detailW, gridH, mouseX, mouseY);
    }

    private void renderSpeciesCard(
            GuiGraphics graphics,
            Font font,
            LicenseTier tier,
            LicenseTier.SpeciesEntry entry,
            boolean tierUnlocked,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ) {
        boolean owned = ZooLicenseManager.isClientSpeciesOwned(entry.speciesKey());
        boolean isSelected = selectedEntry != null && selectedEntry.speciesKey().equals(entry.speciesKey());
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        int bg = isSelected ? 0xFF1F293D : (hovered ? 0xFF1A2232 : 0xFF141923);
        int border = isSelected ? 0xFF38BDF8 : (owned ? 0xFF22543D : (!tierUnlocked ? 0xFF3B1D24 : 0xFF252D3D));

        graphics.fill(x, y, x + w, y + h, bg);
        graphics.fill(x, y, x + w, y + 1, border);
        graphics.fill(x, y + h - 1, x + w, y + h, border);
        graphics.fill(x, y, x + 1, y + h, border);
        graphics.fill(x + w - 1, y, x + w, y + h, border);

        // 3D Entity Preview inside card
        String primaryId = entry.resolvePrimaryEntityId();
        EntityType<?> entityType = entry.resolveEntityType();
        LivingEntity dummy = getOrCreateDummy(primaryId, entityType);

        if (dummy != null) {
            try {
                float bbMax = Math.max(0.65f, Math.max(dummy.getBbHeight(), dummy.getBbWidth()));
                int scale = (int) Math.clamp(24.0f / bbMax, 8, 22);

                if (owned) {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                } else if (tierUnlocked) {
                    RenderSystem.setShaderColor(0.58F, 0.64F, 0.72F, 0.72F);
                } else {
                    RenderSystem.setShaderColor(0.10F, 0.12F, 0.16F, 0.55F);
                }

                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics,
                        x + 4, y + 3, x + w - 4, y + h - 24,
                        scale, 0.05F, x + w / 2.0F + 12, y + 20, dummy
                );
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } catch (Throwable ignored) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        // Translucent dimming overlay for unowned / locked states
        if (!owned) {
            int veilColor = tierUnlocked ? 0x440D1118 : 0x8807090E;
            graphics.fill(x + 1, y + 1, x + w - 1, y + h - 23, veilColor);
        }

        // Species Name
        String name = font.plainSubstrByWidth(entry.getDisplayName(), w - 6);
        int nameColor = owned ? 0xFFFFFFFF : (tierUnlocked ? 0xFFD1D5DB : 0xFF6B7280);
        graphics.drawCenteredString(font, name, x + w / 2, y + h - 21, nameColor);

        // Status Label
        String statusText;
        int statusColor;
        if (owned) {
            statusText = ZooTranslations.get("zootycoon.license.collection.owned");
            statusColor = 0xFF4ADE80;
        } else if (tierUnlocked) {
            statusText = ZooTranslations.get("zootycoon.license.collection.unowned");
            statusColor = 0xFFFBBF24;
        } else {
            statusText = ZooTranslations.get("zootycoon.license.collection.locked");
            statusColor = 0xFFF87171;
        }
        String clippedStatus = font.plainSubstrByWidth(statusText, w - 4);
        graphics.drawCenteredString(font, clippedStatus, x + w / 2, y + h - 10, statusColor);
    }

    private void renderDetailPanel(GuiGraphics graphics, Font font, int x, int y, int w, int h, int mouseX, int mouseY) {
        graphics.fill(x, y, x + w, y + h, 0xFF151A25);
        graphics.fill(x, y, x + w, y + 1, 0xFF283246);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF283246);
        graphics.fill(x, y, x + 1, y + h, 0xFF283246);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFF283246);

        if (selectedEntry == null) return;

        boolean tierUnlocked = ZooLicenseManager.isClientTierUnlocked(selectedTier.getTierLevel());
        boolean owned = ZooLicenseManager.isClientSpeciesOwned(selectedEntry.speciesKey());

        // Large 3D Preview Box
        int previewBoxH = 78;
        graphics.fill(x + 6, y + 6, x + w - 6, y + 6 + previewBoxH, 0xFF0E121A);

        String primaryId = selectedEntry.resolvePrimaryEntityId();
        EntityType<?> entityType = selectedEntry.resolveEntityType();
        LivingEntity dummy = getOrCreateDummy(primaryId, entityType);
        if (dummy != null) {
            try {
                float bbMax = Math.max(0.7f, Math.max(dummy.getBbHeight(), dummy.getBbWidth()));
                int scale = (int) Math.clamp(38.0f / bbMax, 12, 34);
                if (!owned && !tierUnlocked) {
                    RenderSystem.setShaderColor(0.14F, 0.16F, 0.22F, 0.65F);
                } else if (!owned) {
                    RenderSystem.setShaderColor(0.70F, 0.75F, 0.82F, 0.85F);
                }
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics,
                        x + 8, y + 8, x + w - 8, y + 4 + previewBoxH,
                        scale, 0.05F, mouseX, mouseY, dummy
                );
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } catch (Throwable ignored) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        int infoY = y + previewBoxH + 12;
        graphics.drawCenteredString(font, selectedEntry.getDisplayName(), x + w / 2, infoY, 0xFFFFFFFF);
        infoY += 13;

        // Ownership / Lock Status Badge
        String statusStr;
        int statusCol;
        if (owned) {
            statusStr = ZooTranslations.get("zootycoon.license.collection.owned");
            statusCol = 0xFF4ADE80;
        } else if (tierUnlocked) {
            statusStr = ZooTranslations.get("zootycoon.license.collection.unowned");
            statusCol = 0xFFFBBF24;
        } else {
            statusStr = ZooTranslations.get("zootycoon.license.collection.locked");
            statusCol = 0xFFF87171;
        }
        graphics.drawCenteredString(font, statusStr, x + w / 2, infoY, statusCol);
        infoY += 15;

        // Required License Tier Info
        graphics.fill(x + 8, infoY - 3, x + w - 8, infoY - 2, 0xFF252E40);
        graphics.drawString(font, ZooTranslations.get("zootycoon.license.collection.req_tier"), x + 8, infoY + 2, 0xFF9CA3AF, false);
        infoY += 12;
        String tierName = font.plainSubstrByWidth(selectedTier.getDisplayName(), w - 16);
        graphics.drawString(font, tierName, x + 8, infoY, selectedTier.getAccentColor(), false);
        infoY += 15;

        if (!tierUnlocked) {
            String lockNote = ZooTranslations.get("zootycoon.license.collection.locked_hint", selectedTier.getDisplayName());
            List<FormattedCharSequence> lines = font.split(Component.literal(lockNote), w - 16);
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, x + 8, infoY, 0xFFF87171, false);
                infoY += 11;
            }
            return;
        }

        // Additional Species Information (Diet & Favorite Food)
        if (dummy != null) {
            AnimalDietRegistry.AnimalDiet diet = AnimalDietRegistry.getDiet(dummy);
            if (diet != null) {
                graphics.drawString(font, ZooTranslations.get("zootycoon.license.collection.diet_label"), x + 8, infoY, 0xFF9CA3AF, false);
                infoY += 11;
                String sub = font.plainSubstrByWidth(diet.getSubtitle(), w - 16);
                graphics.drawString(font, sub, x + 8, infoY, 0xFFC084FC, false);
                infoY += 14;
            }

            List<ItemStack> foods = AnimalDietRegistry.getFavoriteFoodStacks(dummy);
            if (!foods.isEmpty()) {
                graphics.drawString(font, ZooTranslations.get("zootycoon.ui.animals.favorite_food"), x + 8, infoY, 0xFF9CA3AF, false);
                infoY += 12;
                int fx = x + 8;
                int maxIcons = Math.min(5, foods.size());
                for (int i = 0; i < maxIcons; i++) {
                    graphics.renderItem(foods.get(i), fx, infoY);
                    fx += 20;
                }
                infoY += 20;
            }
        }

        if (dummy != null) {
            HabitatProfile profile = HabitatProfileLoader.getProfile(dummy);
            if (profile != null && profile.displayName() != null && !profile.displayName().isEmpty()) {
                graphics.drawString(font, "Habitat: " + font.plainSubstrByWidth(profile.displayName(), w - 52), x + 8, infoY, 0xFF81E6D9, false);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int sw = parent.width;
        int sh = parent.height;
        int modalW = Math.min(sw - 28, 540);
        int modalH = Math.min(sh - 24, 310);
        int modalX = (sw - modalW) / 2;
        int modalY = (sh - modalH) / 2;

        if (mouseX < modalX || mouseX > modalX + modalW || mouseY < modalY || mouseY > modalY + modalH) {
            return false; // Clicked outside -> close modal
        }

        int closeX = modalX + modalW - 24;
        int closeY = modalY + 5;
        if (mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18) {
            return false; // Close button clicked -> close modal
        }

        int detailW = 152;
        int gridX = modalX + 10;
        int gridY = modalY + 34;
        int gridW = modalW - detailW - 26;
        int gridH = modalH - 42;

        if (mouseX >= gridX && mouseX <= gridX + gridW && mouseY >= gridY && mouseY <= gridY + gridH) {
            int cols = 4;
            int cardGap = 6;
            int cardW = (gridW - (cardGap * (cols - 1)) - 6) / cols;
            int cardH = 74;

            int curY = gridY - scrollOffset;
            for (LicenseTier tier : LicenseTier.values()) {
                curY += 22;
                List<LicenseTier.SpeciesEntry> speciesList = tier.getRequiredSpecies();
                for (int i = 0; i < speciesList.size(); i++) {
                    int col = i % cols;
                    int row = i / cols;
                    int cx = gridX + col * (cardW + cardGap);
                    int cy = curY + row * (cardH + cardGap);

                    if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + cardH) {
                        this.selectedTier = tier;
                        this.selectedEntry = speciesList.get(i);
                        return true;
                    }
                }
                int rows = (speciesList.size() + cols - 1) / cols;
                curY += rows * (cardH + cardGap) + 6;
            }
        }

        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.maxScroll > 0) {
            this.scrollOffset = Math.clamp(this.scrollOffset - (int) (verticalAmount * 28), 0, this.maxScroll);
            return true;
        }
        return false;
    }
}
