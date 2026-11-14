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
import net.minecraft.world.item.Items;
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

    /** Biome names per species key, filled by the server (client has no biome spawn data). */
    public static final Map<String, List<String>> BIOME_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Set<String> REQUESTED_BIOMES = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final ItemStack EMERALD_ICON = new ItemStack(Items.EMERALD);
    private static final ItemStack MAGNIFYING_GLASS_ICON = new ItemStack(net.zootycoon.item.ZooItems.MAGNIFYING_GLASS);
    private static final net.minecraft.resources.ResourceLocation STAR_FILLED =
            net.zootycoon.ZooTycoonMod.id("textures/gui/icons/star_filled.png");
    private static final net.minecraft.resources.ResourceLocation STAR_HALF =
            net.zootycoon.ZooTycoonMod.id("textures/gui/icons/star_half.png");
    private static final net.minecraft.resources.ResourceLocation STAR_EMPTY =
            net.zootycoon.ZooTycoonMod.id("textures/gui/icons/star_empty.png");

    private final MainManagementScreen parent;
    private final Map<String, LivingEntity> dummyEntityCache = new HashMap<>();
    private LicenseTier.SpeciesEntry selectedEntry = LicenseTier.TAMAN_MINI.getRequiredSpecies().get(0);
    private LicenseTier selectedTier = LicenseTier.TAMAN_MINI;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    // Tooltip regions captured while rendering, drawn last so they stay on top
    private int[] biomeHoverRect = null;
    private List<String> biomeHoverList = null;
    private String infoBoardTooltip = null;
    private String summonTooltip = null;
    private final Map<Integer, int[]> summonButtonRects = new HashMap<>();

    public AnimalCollectionModal(MainManagementScreen parent) {
        this.parent = parent;
    }

    public void init() {
        this.scrollOffset = 0;
    }

    private record InspectorState(boolean canRequest, boolean pendingClaim, boolean creative, int missing, LicenseTier activeTier) {}

    private InspectorState inspectorState() {
        int activeTierLvl = ZooLicenseManager.getClientActiveTierLevel();
        LicenseTier activeTier = LicenseTier.byLevel(activeTierLvl);
        int missing = ZooLicenseManager.getClientMissingCountForActiveTier();
        Minecraft mc = Minecraft.getInstance();
        boolean creative = mc.player != null && mc.player.isCreative();
        var tierData = ZooLicenseManager.getClientTierData(activeTierLvl);
        boolean pendingClaim = tierData.inspectionPassed && (!tierData.licenseClaimed || !tierData.letterClaimed);
        return new InspectorState(creative || pendingClaim || missing == 0, pendingClaim, creative, missing, activeTier);
    }

    private int[] inspectorButtonRect(Font font, int modalX, int modalY, int modalW) {
        InspectorState st = inspectorState();
        String text = ZooTranslations.get(st.pendingClaim() ? "zootycoon.license.btn.claim_inspector" : "zootycoon.license.btn.call_inspector");
        int w = font.width(text) + 28;
        int closeX = modalX + modalW - 24;
        return new int[]{closeX - 6 - w, modalY + 5, w, 18};
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

        // Fully solid dark backdrop
        graphics.fill(0, 0, sw, sh, 0xF505070B);

        int modalW = Math.min(sw - 28, 540);
        int modalH = Math.min(sh - 24, 310);
        int modalX = (sw - modalW) / 2;
        int modalY = (sh - modalH) / 2;

        // Main Modal Frame (fully opaque)
        graphics.fill(modalX, modalY, modalX + modalW, modalY + modalH, 0xFF11151E);
        graphics.fill(modalX, modalY, modalX + modalW, modalY + 1, 0xFF2D3648);
        graphics.fill(modalX, modalY + modalH - 1, modalX + modalW, modalY + modalH, 0xFF2D3648);
        graphics.fill(modalX, modalY, modalX + 1, modalY + modalH, 0xFF2D3648);
        graphics.fill(modalX + modalW - 1, modalY, modalX + modalW, modalY + modalH, 0xFF2D3648);

        // Header Bar
        graphics.fill(modalX + 1, modalY + 1, modalX + modalW - 1, modalY + 28, 0xFF171D29);
        graphics.fill(modalX + 1, modalY + 28, modalX + modalW - 1, modalY + 29, 0xFF262F42);
        graphics.drawString(font, ZooTranslations.get("zootycoon.license.collection.title"), modalX + 12, modalY + 10, 0xFFF1F5F9, false);

        // Close [X] Button
        int closeX = modalX + modalW - 24;
        int closeY = modalY + 5;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18;
        graphics.fill(closeX, closeY, closeX + 18, closeY + 18, hoverClose ? 0xFFC53030 : 0xFF252D3D);
        graphics.drawCenteredString(font, "X", closeX + 9, closeY + 5, 0xFFFFFFFF);

        // Call Inspector button, directly beside the close button
        this.biomeHoverRect = null;
        this.biomeHoverList = null;
        this.infoBoardTooltip = null;
        this.summonTooltip = null;
        this.summonButtonRects.clear();
        InspectorState inspector = inspectorState();
        int[] ib = inspectorButtonRect(font, modalX, modalY, modalW);
        boolean hoverInspector = mouseX >= ib[0] && mouseX <= ib[0] + ib[2] && mouseY >= ib[1] && mouseY <= ib[1] + ib[3];
        drawSlateButtonBox(graphics, ib[0], ib[1], ib[2], ib[3], inspector.canRequest(), hoverInspector);
        String ibText = ZooTranslations.get(inspector.pendingClaim() ? "zootycoon.license.btn.claim_inspector" : "zootycoon.license.btn.call_inspector");
        int iconSize = 12;
        int totalContentW = iconSize + 4 + font.width(ibText);
        int contentStartX = ib[0] + (ib[2] - totalContentW) / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(contentStartX, ib[1] + 3, 0);
        graphics.pose().scale(0.75f, 0.75f, 1.0f);
        graphics.renderFakeItem(MAGNIFYING_GLASS_ICON, 0, 0);
        graphics.pose().popPose();
        graphics.drawString(font, ibText, contentStartX + iconSize + 4, ib[1] + 5, inspector.canRequest() ? 0xFFF1F5F9 : 0xFF64748B, false);

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

            // Tier Section Header (restrained slate hierarchy)
            graphics.fill(gridX, curY, gridX + gridW - 6, curY + 18, 0xFF18202E);
            graphics.fill(gridX, curY, gridX + 3, curY + 18, tierUnlocked ? 0xFF3B82F6 : 0xFF334155);

            String tierHeader = tier.getDisplayName();
            graphics.drawString(font, tierHeader, gridX + 8, curY + 5, tierUnlocked ? 0xFFF1F5F9 : 0xFF94A3B8, false);

            double reqStars = tier.getRequiredStars();
            int starStartX = gridX + 8 + font.width(tierHeader) + 6;
            int starY = curY + 5;
            for (int si = 0; si < 5; si++) {
                int sx = starStartX + si * 9;
                net.minecraft.resources.ResourceLocation starTex;
                if (reqStars >= si + 0.75D) {
                    starTex = STAR_FILLED;
                } else if (reqStars >= si + 0.25D) {
                    starTex = STAR_HALF;
                } else {
                    starTex = STAR_EMPTY;
                }
                graphics.blit(starTex, sx, starY, 8, 8, 0.0f, 0.0f, 16, 16, 16, 16);
            }
            String ratingStr = String.format(java.util.Locale.ROOT, "%.1f", reqStars);
            graphics.drawString(font, ratingStr, starStartX + 5 * 9 + 3, curY + 5, 0xFFF1C40F, false);

            String tierBadgeKey = !tierUnlocked ? "zootycoon.license.state.locked"
                    : (tierData.licenseClaimed ? "zootycoon.license.state.certified" : "zootycoon.license.state.unverified");
            String tierBadge = ZooTranslations.get(tierBadgeKey);
            int badgeColor = !tierUnlocked ? 0xFFFF5555 : (tierData.licenseClaimed ? 0xFF6EE7B7 : 0xFFFF5555);
            int badgeX = gridX + gridW - 12 - font.width(tierBadge);
            graphics.drawString(font, tierBadge, badgeX, curY + 5, badgeColor, false);

            // Creative-only per-tier Summon All button for testing
            if (inspector.creative()) {
                String summonLabel = ZooTranslations.get("zootycoon.license.btn.summon_tier");
                int sW = font.width(summonLabel) + 8;
                int sX = badgeX - 6 - sW;
                int sY = curY + 2;
                int sH = 14;
                if (sY + sH >= gridY && sY <= gridY + gridH) {
                    boolean hoverSummon = mouseY >= gridY && mouseY <= gridY + gridH
                            && mouseX >= sX && mouseX <= sX + sW && mouseY >= sY && mouseY <= sY + sH;
                    graphics.fill(sX, sY, sX + sW, sY + sH, hoverSummon ? 0xFF2563EB : 0xFF1D4ED8);
                    graphics.renderOutline(sX, sY, sW, sH, hoverSummon ? 0xFF60A5FA : 0xFF3B82F6);
                    graphics.drawCenteredString(font, summonLabel, sX + sW / 2, sY + 3, 0xFFFFFFFF);
                    this.summonButtonRects.put(tier.getTierLevel(), new int[]{sX, sY, sW, sH});
                    if (hoverSummon) {
                        this.summonTooltip = ZooTranslations.get("zootycoon.license.btn.summon_tier_tooltip", tier.getDisplayName());
                    }
                }
            }

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
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFF3B82F6);
        }

        // Right Detail Column
        int detailX = gridX + gridW + 6;
        int detailY = gridY;
        renderDetailPanel(graphics, font, detailX, detailY, detailW, gridH, mouseX, mouseY);

        // Tooltips last, so they sit above every other element of the modal
        if (hoverInspector) {
            graphics.renderTooltip(font, Component.literal(inspectorTooltip(inspector)), mouseX, mouseY);
        } else if (summonTooltip != null) {
            graphics.renderTooltip(font, Component.literal(summonTooltip), mouseX, mouseY);
        } else if (infoBoardTooltip != null) {
            graphics.renderTooltip(font, Component.literal(infoBoardTooltip), mouseX, mouseY);
        } else if (biomeHoverRect != null && biomeHoverList != null && !biomeHoverList.isEmpty()
                && mouseX >= biomeHoverRect[0] && mouseX <= biomeHoverRect[0] + biomeHoverRect[2]
                && mouseY >= biomeHoverRect[1] && mouseY <= biomeHoverRect[1] + biomeHoverRect[3]) {
            graphics.renderTooltip(font, font.split(Component.literal(String.join(", ", biomeHoverList)), 220), mouseX, mouseY);
        }
    }

    private String inspectorTooltip(InspectorState st) {
        if (st.pendingClaim() || st.missing() == 0) {
            return ZooTranslations.get("zootycoon.license.btn.inspector_ready", st.activeTier().getDisplayName());
        }
        if (st.creative()) {
            return ZooTranslations.get("zootycoon.license.btn.inspector_creative", st.activeTier().getDisplayName());
        }
        return ZooTranslations.get("zootycoon.license.btn.inspector_missing", st.missing(), st.activeTier().getDisplayName());
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

        int bg = isSelected ? 0xFF1E293B : (hovered ? 0xFF182030 : 0xFF121722);
        int border = isSelected ? 0xFFE2E8F0 : (owned ? 0xFF2C4A3E : (!tierUnlocked ? 0xFF252A36 : 0xFF283142));

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
        int nameColor = owned ? 0xFFF1F5F9 : (tierUnlocked ? 0xFFCBD5E1 : 0xFF64748B);
        graphics.drawCenteredString(font, name, x + w / 2, y + h - 21, nameColor);

        // Status Label
        String statusText;
        int statusColor;
        if (owned) {
            statusText = ZooTranslations.get("zootycoon.license.collection.owned");
            statusColor = 0xFF6EE7B7;
        } else if (tierUnlocked) {
            statusText = ZooTranslations.get("zootycoon.license.collection.unowned");
            statusColor = 0xFF94A3B8;
        } else {
            statusText = ZooTranslations.get("zootycoon.license.collection.locked");
            statusColor = 0xFF64748B;
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
        graphics.drawCenteredString(font, selectedEntry.getDisplayName(), x + w / 2, infoY, 0xFFF1F5F9);
        infoY += 13;

        // Ownership / Lock Status Badge
        String statusStr;
        int statusCol;
        if (owned) {
            statusStr = ZooTranslations.get("zootycoon.license.collection.owned");
            statusCol = 0xFF6EE7B7;
        } else if (tierUnlocked) {
            statusStr = ZooTranslations.get("zootycoon.license.collection.unowned");
            statusCol = 0xFF94A3B8;
        } else {
            statusStr = ZooTranslations.get("zootycoon.license.collection.locked");
            statusCol = 0xFF64748B;
        }
        graphics.drawCenteredString(font, statusStr, x + w / 2, infoY, statusCol);
        infoY += 15;

        if (LicenseTier.requiresMate(selectedEntry.speciesKey())) {
            graphics.drawCenteredString(
                    font,
                    ZooTranslations.get("zootycoon.license.collection.requires_mate"),
                    x + w / 2,
                    infoY - 2,
                    0xFFFBBF24
            );
            infoY += 12;
        }

        graphics.fill(x + 8, infoY - 3, x + w - 8, infoY - 2, 0xFF252E40);
        infoY += 3;

        // Buy Animal Information Board button at the bottom of the detail panel
        renderInfoBoardButton(graphics, font, x, y, w, h, mouseX, mouseY, owned && tierUnlocked);

        if (!tierUnlocked) {
            String lockNote = ZooTranslations.get("zootycoon.license.collection.locked_hint", selectedTier.getDisplayName());
            List<FormattedCharSequence> lines = font.split(Component.literal(lockNote), w - 16);
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, x + 8, infoY, 0xFF94A3B8, false);
                infoY += 11;
            }
            return;
        }

        // Additional Species Information (Diet & Favorite Food)
        if (dummy != null) {
            AnimalDietRegistry.AnimalDiet diet = AnimalDietRegistry.getDiet(dummy);
            if (diet != null) {
                graphics.drawString(font, ZooTranslations.get("zootycoon.license.collection.diet_label"), x + 8, infoY, 0xFF94A3B8, false);
                infoY += 11;
                String sub = font.plainSubstrByWidth(diet.getSubtitle(), w - 16);
                graphics.drawString(font, sub, x + 8, infoY, 0xFFE2E8F0, false);
                infoY += 14;
            }

            List<ItemStack> foods = AnimalDietRegistry.getFavoriteFoodStacks(dummy);
            if (!foods.isEmpty()) {
                graphics.drawString(font, ZooTranslations.get("zootycoon.ui.animals.favorite_food"), x + 8, infoY, 0xFF94A3B8, false);
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

        // Species spawn biomes (English), supplied by the server
        String speciesKey = selectedEntry.speciesKey();
        if (!BIOME_CACHE.containsKey(speciesKey) && REQUESTED_BIOMES.add(speciesKey)) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new net.zootycoon.network.ZooNetworkPayloads.RequestSpeciesBiomesPayload(speciesKey));
        }
        graphics.drawString(font, ZooTranslations.get("zootycoon.license.collection.spawns_in"), x + 8, infoY, 0xFF94A3B8, false);
        infoY += 11;
        List<String> biomes = BIOME_CACHE.get(speciesKey);
        int biomeAreaBottom = y + h - 20 - 8 - 6;
        int maxLines = Math.max(0, (biomeAreaBottom - infoY) / 10);
        if (biomes == null) {
            graphics.drawString(font, ZooTranslations.get("zootycoon.license.collection.biomes_loading"), x + 8, infoY, 0xFFCBD5E1, false);
        } else if (biomes.isEmpty()) {
            graphics.drawString(font, ZooTranslations.get("zootycoon.license.collection.no_spawn"), x + 8, infoY, 0xFFCBD5E1, false);
        } else {
            List<FormattedCharSequence> lines = font.split(Component.literal(String.join(", ", biomes)), w - 16);
            int shown = Math.min(lines.size(), maxLines);
            for (int i = 0; i < shown; i++) {
                graphics.drawString(font, lines.get(i), x + 8, infoY + i * 10, 0xFFCBD5E1, false);
            }
            if (shown < lines.size()) {
                this.biomeHoverRect = new int[]{x + 8, infoY, w - 16, Math.max(10, shown * 10)};
                this.biomeHoverList = biomes;
                graphics.drawString(font, "...", x + 8, infoY + shown * 10, 0xFF94A3B8, false);
            }
        }
    }

    private int[] infoBoardButtonRect(int detailX, int detailY, int detailW, int detailH) {
        return new int[]{detailX + 8, detailY + detailH - 20 - 8, detailW - 16, 20};
    }

    private static void drawSlateButtonBox(GuiGraphics graphics, int x, int y, int w, int h, boolean enabled, boolean hover) {
        int bg = !enabled ? 0xFF1A1F2B : (hover ? 0xFF2E3A50 : 0xFF232C3D);
        int border = !enabled ? 0xFF2D3545 : (hover ? 0xFF94A3B8 : 0xFF475569);
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.renderOutline(x, y, w, h, border);
    }

    private void renderInfoBoardButton(GuiGraphics graphics, Font font, int x, int y, int w, int h, int mouseX, int mouseY, boolean enabled) {
        int[] b = infoBoardButtonRect(x, y, w, h);
        boolean hover = mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3];
        drawSlateButtonBox(graphics, b[0], b[1], b[2], b[3], enabled, hover);
        Minecraft mc = Minecraft.getInstance();
        boolean creative = mc.player != null && mc.player.isCreative();
        int textColor = enabled ? 0xFFF1F5F9 : 0xFF64748B;
        if (creative) {
            String text = ZooTranslations.get("zootycoon.license.btn.buy_info_board_free");
            graphics.drawCenteredString(font, font.plainSubstrByWidth(text, b[2] - 6), b[0] + b[2] / 2, b[1] + 6, textColor);
        } else {
            String priceStr = String.valueOf(ZooLicenseManager.INFO_BOARD_PRICE);
            int iconW = 16;
            int gapAfterText = 3;
            int gapAfterIcon = 1;
            int priceW = font.width(priceStr);
            int maxLabelW = Math.max(20, b[2] - 8 - gapAfterText - iconW - gapAfterIcon - priceW);
            String label = font.plainSubstrByWidth(ZooTranslations.get("zootycoon.license.btn.buy_info_board"), maxLabelW);
            int labelW = font.width(label);
            int totalW = labelW + gapAfterText + iconW + gapAfterIcon + priceW;
            int startX = b[0] + (b[2] - totalW) / 2;
            graphics.drawString(font, label, startX, b[1] + 6, textColor, false);
            int iconX = startX + labelW + gapAfterText;
            graphics.renderItem(EMERALD_ICON, iconX, b[1] + 2);
            graphics.drawString(font, priceStr, iconX + iconW + gapAfterIcon, b[1] + 6, enabled ? 0xFF6EE7B7 : 0xFF64748B, false);
        }
        if (hover && !enabled) {
            this.infoBoardTooltip = ZooTranslations.get("zootycoon.license.btn.buy_info_board_locked");
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

        // Top-right Call Inspector button (beside Close [X] button)
        Font font = Minecraft.getInstance().font;
        int[] ib = inspectorButtonRect(font, modalX, modalY, modalW);
        if (mouseX >= ib[0] && mouseX <= ib[0] + ib[2] && mouseY >= ib[1] && mouseY <= ib[1] + ib[3]) {
            InspectorState inspector = inspectorState();
            if (inspector.canRequest()) {
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        new net.zootycoon.network.ZooNetworkPayloads.RequestZooInspectionPayload()
                );
                parent.onClose();
            } else {
                ZooHudOverlay.showToast(
                        "zootycoon.toast.title.inspector",
                        "zootycoon.license.toast.missing_species",
                        ZooHudOverlay.TOAST_WARNING,
                        inspector.missing(),
                        inspector.activeTier().getDisplayName()
                );
            }
            return true;
        }

        int detailW = 152;
        int gridX = modalX + 10;
        int gridY = modalY + 34;
        int gridW = modalW - detailW - 26;
        int gridH = modalH - 42;

        int detailX = gridX + gridW + 6;
        int detailY = gridY;
        int detailH = gridH;

        // Bottom-right Buy Animal Info Board button
        int[] b = infoBoardButtonRect(detailX, detailY, detailW, detailH);
        if (mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
            if (selectedEntry != null && selectedTier != null) {
                boolean tierUnlocked = ZooLicenseManager.isClientTierUnlocked(selectedTier.getTierLevel());
                boolean owned = ZooLicenseManager.isClientSpeciesOwned(selectedEntry.speciesKey());
                if (owned && tierUnlocked) {
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                            new net.zootycoon.network.ZooNetworkPayloads.BuyAnimalInfoBannerPayload(selectedEntry.speciesKey())
                    );
                } else {
                    ZooHudOverlay.showToast(
                            "zootycoon.toast.title.inspector",
                            "zootycoon.license.toast.info_board_not_owned",
                            ZooHudOverlay.TOAST_WARNING,
                            selectedEntry.getDisplayName()
                    );
                }
            }
            return true;
        }

        if (mouseX >= gridX && mouseX <= gridX + gridW && mouseY >= gridY && mouseY <= gridY + gridH) {
            for (var entry : summonButtonRects.entrySet()) {
                int[] r = entry.getValue();
                if (mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                            new net.zootycoon.network.ZooNetworkPayloads.SummonTierAnimalsPayload(entry.getKey())
                    );
                    parent.onClose();
                    return true;
                }
            }
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
