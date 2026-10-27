package net.zootycoon.client.ui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.client.render.ClaimBoundaryRenderer;
import net.zootycoon.client.render.PlacementPreviewRenderer;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.util.ZooTranslations;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ZooHudOverlay {

    public static final int TOAST_INFO = 0;
    public static final int TOAST_SUCCESS = 1;
    public static final int TOAST_WARNING = 2;
    public static final int TOAST_ERROR = 3;

    public static class FloatingDelta {
        public final int amount;
        public final long startTime;

        public FloatingDelta(int amount) {
            this.amount = amount;
            this.startTime = System.currentTimeMillis();
        }
    }

    public static class EssentialToast {
        public final String title;
        public final String message;
        public final int type;
        public long startTime;
        public static final long DURATION_MS = 3800L;

        public EssentialToast(String title, String message, int type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.startTime = System.currentTimeMillis();
        }
    }

    private static final List<FloatingDelta> floatingDeltas = new CopyOnWriteArrayList<>();
    private static final List<EssentialToast> activeToasts = new CopyOnWriteArrayList<>();
    private static final ItemStack VANILLA_EMERALD_ITEM = new ItemStack(Items.EMERALD);

    private static final net.minecraft.resources.ResourceLocation STAR_FILLED =
            net.zootycoon.ZooTycoonMod.id("textures/gui/icons/star_filled.png");
    private static final net.minecraft.resources.ResourceLocation STAR_HALF =
            net.zootycoon.ZooTycoonMod.id("textures/gui/icons/star_half.png");
    private static final net.minecraft.resources.ResourceLocation STAR_EMPTY =
            net.zootycoon.ZooTycoonMod.id("textures/gui/icons/star_empty.png");

    public static void addDelta(int delta) {
        if (delta != 0) {
            floatingDeltas.add(new FloatingDelta(delta));
        }
    }

    public static void showToast(String titleKey, String msgKey, int type, Object... args) {
        String title = ZooTranslations.get(titleKey);
        String msg = ZooTranslations.get(msgKey, args);
        showToastDirect(title, msg, type);
    }

    public static void showToastDirect(String title, String message, int type) {
        for (EssentialToast existing : activeToasts) {
            if (existing.message.equals(message)) {
                existing.startTime = System.currentTimeMillis();
                return;
            }
        }
        while (activeToasts.size() >= 4) {
            activeToasts.remove(0);
        }
        activeToasts.add(new EssentialToast(title, message, type));
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        // 0a. Permanent 5-Star Zoo Rating Indicator on Screen (Top-Right)
        renderPermanentRatingHud(graphics, mc);

        // 0b. Permanent Emerald Balance Indicator on Screen (Top-Right)
        renderPermanentEmeraldHud(graphics, mc);

        // 0c. Essential-Mod Style Toast Notifications (Top-Right below Indicators)
        renderEssentialToasts(graphics, mc);

        // 1. Placement Preview Mode
        ZooSchematic schem = PlacementPreviewRenderer.getActiveSchematic();
        if (schem != null) {
            renderPlacementHud(graphics, mc.font, schem);
            return;
        }

        // 1b. Studio Mode HUD
        if (ZooTycoonClient.inStudio) {
            renderStudioHud(graphics, mc.font);
            return;
        }

        // 2. Claim Selection Mode
        boolean holdingTool = player.getMainHandItem().is(Items.STICK)
                || player.getOffhandItem().is(Items.STICK);
        if (holdingTool && ClaimBoundaryRenderer.hasSelection()) {
            renderClaimHud(graphics, mc.font);
        }
    }

    private static void renderEssentialToasts(GuiGraphics graphics, Minecraft mc) {
        if (activeToasts.isEmpty()) return;

        Font font = mc.font;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int toastW = 210;
        int curY = 56;
        long now = System.currentTimeMillis();

        for (EssentialToast toast : activeToasts) {
            long elapsed = now - toast.startTime;
            if (elapsed >= EssentialToast.DURATION_MS) {
                activeToasts.remove(toast);
                continue;
            }

            // Slide-in (first 260ms) and Slide-out (last 340ms) with cubic easing
            float slideFactor = 0.0f;
            if (elapsed < 260L) {
                float t = 1.0f - (elapsed / 260.0f);
                slideFactor = t * t * t;
            } else if (elapsed > EssentialToast.DURATION_MS - 340L) {
                float t = (elapsed - (EssentialToast.DURATION_MS - 340L)) / 340.0f;
                slideFactor = t * t * t;
            }

            List<FormattedCharSequence> lines = font.split(Component.literal(toast.message), toastW - 22);
            int toastH = 22 + (lines.size() * 10);

            int baseX = screenW - toastW - 12;
            int x = baseX + (int) (slideFactor * (toastW + 20));
            int y = curY;

            int accentColor = switch (toast.type) {
                case TOAST_SUCCESS -> 0xFF2ECC71;
                case TOAST_WARNING -> 0xFFF1C40F;
                case TOAST_ERROR -> 0xFFE74C3C;
                default -> 0xFF388BFD;
            };

            // Essential Mod Dark Obsidian Card Background & Crisp 1px Border
            graphics.fill(x, y, x + toastW, y + toastH, 0xF212151D);
            graphics.fill(x, y, x + toastW, y + 1, 0xFF282E3B);
            graphics.fill(x, y + toastH - 1, x + toastW, y + toastH, 0xFF282E3B);
            graphics.fill(x + toastW - 1, y, x + toastW, y + toastH, 0xFF282E3B);

            // Left 3px Status Accent Bar (Essential Friend Online style)
            graphics.fill(x, y, x + 3, y + toastH, accentColor);

            // Status Indicator Dot + Title (Normal font, no bold)
            graphics.fill(x + 9, y + 8, x + 13, y + 12, accentColor);
            graphics.drawString(font, Component.literal(toast.title), x + 18, y + 6, 0xFFFFFFFF, false);

            // Wrapped Message Lines
            int lineY = y + 18;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, x + 9, lineY, 0xFFC9D1D9, false);
                lineY += 10;
            }

            curY += toastH + 6;
        }
    }

    private static void renderPermanentRatingHud(GuiGraphics graphics, Minecraft mc) {
        Player player = mc.player;
        if (player == null) return;

        double starRating = ZooTycoonClient.getClientStarRating();
        int reviewCount = ZooTycoonClient.getClientReviewCount();
        String ratingText = (reviewCount > 0)
                ? String.format(java.util.Locale.ROOT, "%.1f (%d)", starRating, reviewCount)
                : String.format(java.util.Locale.ROOT, "%.1f", starRating);

        Font font = mc.font;
        int textW = font.width(ratingText);
        int starsW = 5 * 11 + 4 * 2; // 63px
        int badgeW = 6 + starsW + 6 + textW + 6;
        int badgeH = 18;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int x = screenW - badgeW - 12;
        int y = 8;

        // Sleek Minimalist Pill Card
        graphics.fill(x, y, x + badgeW, y + badgeH, 0xD012151D);
        graphics.fill(x, y, x + badgeW, y + 1, 0xFF2A3140);
        graphics.fill(x, y + badgeH - 1, x + badgeW, y + badgeH, 0xFF2A3140);
        graphics.fill(x, y + 1, x + 1, y + badgeH, 0xFF2A3140);
        graphics.fill(x + badgeW - 1, y, x + badgeW, y + badgeH, 0xFF2A3140);

        // Render 5 stars
        int starStartX = x + 6;
        int starY = y + 3;
        for (int i = 0; i < 5; i++) {
            int sx = starStartX + i * 13;
            double starFloor = (double) i;
            net.minecraft.resources.ResourceLocation starTex;
            if (starRating >= starFloor + 0.75D) {
                starTex = STAR_FILLED;
            } else if (starRating >= starFloor + 0.25D) {
                starTex = STAR_HALF;
            } else {
                starTex = STAR_EMPTY;
            }
            graphics.blit(starTex, sx, starY, 11, 11, 0.0f, 0.0f, 16, 16, 16, 16);
        }

        // Render rating text (Clean golden-yellow, normal font, no bold)
        int textX = starStartX + starsW + 6;
        int textY = y + 5;
        graphics.drawString(font, Component.literal(ratingText), textX, textY, 0xFFF1C40F, false);
    }

    private static void renderPermanentEmeraldHud(GuiGraphics graphics, Minecraft mc) {
        Player player = mc.player;
        if (player == null) return;

        int emeraldCount = ZooTycoonClient.clientEmeraldBalance;

        Font font = mc.font;
        String text = String.valueOf(emeraldCount);
        int textW = font.width(text);
        int badgeW = 22 + textW + 10;
        int badgeH = 20;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int x = screenW - badgeW - 12;
        int y = 29;

        // Sleek Minimalist Pill Card
        graphics.fill(x, y, x + badgeW, y + badgeH, 0xD012151D);
        graphics.fill(x, y, x + badgeW, y + 1, 0xFF2A3140);
        graphics.fill(x, y + badgeH - 1, x + badgeW, y + badgeH, 0xFF2A3140);
        graphics.fill(x, y + 1, x + 1, y + badgeH, 0xFF2A3140);
        graphics.fill(x + badgeW - 1, y, x + badgeW, y + badgeH, 0xFF2A3140);

        // Vanilla Emerald Item Render
        graphics.renderItem(VANILLA_EMERALD_ITEM, x + 3, y + 2);

        // Emerald Count (Normal text, no bold)
        graphics.drawString(font, Component.literal(text), x + 23, y + 6, 0xFF55FF55, true);

        // Render Floating Emerald Deltas (+X floating up, -X descending down and fading)
        long now = System.currentTimeMillis();
        for (FloatingDelta fd : floatingDeltas) {
            long elapsed = now - fd.startTime;
            if (elapsed > 1500) {
                floatingDeltas.remove(fd);
                continue;
            }

            float progress = elapsed / 1500f;
            int alpha = (int) ((1.0f - progress) * 255);
            if (alpha < 0) alpha = 0;

            if (fd.amount < 0) {
                float yOffset = progress * 20.0f;
                int color = (alpha << 24) | 0xFF4444;
                String deltaText = String.valueOf(fd.amount);
                graphics.drawString(font, Component.literal(deltaText), x + badgeW - font.width(deltaText), (int) (y + badgeH + 4 + yOffset), color, true);
            } else {
                float yOffset = -progress * 14.0f;
                int color = (alpha << 24) | 0x55FF55;
                String deltaText = "+" + fd.amount;
                graphics.drawString(font, Component.literal(deltaText), x + badgeW - font.width(deltaText), (int) (y + badgeH + 4 + yOffset), color, true);
            }
        }
    }

    private static void renderPlacementHud(GuiGraphics graphics, Font font, ZooSchematic schem) {
        int x = 12;
        int y = 12;
        int width = 230;
        int height = 138;

        graphics.fill(x, y, x + width, y + height, 0xD010141A);
        graphics.fill(x, y, x + width, y + 1, 0xFF2A2E37);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF2A2E37);
        graphics.fill(x, y + 1, x + 1, y + height, 0xFF2A2E37);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF2A2E37);

        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.placement.title"), x + 8, y + 8, 0xFF00E5FF, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.placement.design", schem.getName()), x + 8, y + 22, 0xFFFFFFFF, true);

        boolean insideClaim = PlacementPreviewRenderer.isTargetInsideClaim();
        int statusColor = insideClaim ? 0xFF55FF55 : 0xFFFF5555;
        String statusKey = insideClaim ? "zootycoon.hud.placement.status_valid" : "zootycoon.hud.placement.status_outside";
        graphics.drawString(font, ZooTranslations.component(statusKey), x + 8, y + 34, statusColor, true);

        String rot = PlacementPreviewRenderer.getRotationDegreeString();
        int offX = PlacementPreviewRenderer.getPlacementOffsetX();
        int offY = PlacementPreviewRenderer.getPlacementOffsetY();
        int offZ = PlacementPreviewRenderer.getPlacementOffsetZ();
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.placement.transform", rot, offX, offY, offZ), x + 8, y + 46, 0xFFAAAAAA, true);

        graphics.fill(x + 8, y + 59, x + width - 8, y + 60, 0xFF2A2E37);

        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.controls.title"), x + 8, y + 64, 0xFFFFD700, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.placement.ctrl_build"), x + 8, y + 76, 0xFFFFFFFF, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.placement.ctrl_rotate"), x + 8, y + 88, 0xFFFFFFFF, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.placement.ctrl_offset_xy"), x + 8, y + 100, 0xFFFFFFFF, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.placement.ctrl_offset_z"), x + 8, y + 112, 0xFFFFFFFF, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.ctrl_cancel"), x + 8, y + 124, 0xFFCCCCCC, true);
    }

    private static void renderClaimHud(GuiGraphics graphics, Font font) {
        var pos1 = ClaimBoundaryRenderer.getPos1();
        var pos2 = ClaimBoundaryRenderer.getPos2();
        var info = ClaimBoundaryRenderer.getSelectionInfo();

        int x = 12;
        int y = 12;
        int width = 230;
        int height = (pos2 != null && info != null) ? 144 : 96;

        graphics.fill(x, y, x + width, y + height, 0xD010141A);
        graphics.fill(x, y, x + width, y + 1, 0xFF2A2E37);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF2A2E37);
        graphics.fill(x, y + 1, x + 1, y + height, 0xFF2A2E37);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF2A2E37);

        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.claim.title"), x + 8, y + 8, 0xFF00FF88, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.claim.corner1", pos1.getX(), pos1.getZ()), x + 8, y + 22, 0xFFFFFFFF, true);

        if (pos2 == null || info == null) {
            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.claim.corner2_prompt"), x + 8, y + 34, 0xFFFFFF55, true);
            graphics.fill(x + 8, y + 47, x + width - 8, y + 48, 0xFF2A2E37);
            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.controls.title"), x + 8, y + 52, 0xFFFFD700, true);
            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.claim.ctrl_corner2"), x + 8, y + 64, 0xFFFFFFFF, true);
            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.ctrl_cancel"), x + 8, y + 76, 0xFFCCCCCC, true);
        } else {
            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.claim.corner2", pos2.getX() + ClaimBoundaryRenderer.getClaimOffsetX(), pos2.getZ() + ClaimBoundaryRenderer.getClaimOffsetZ()), x + 8, y + 34, 0xFFFFFFFF, true);
            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.claim.size", info.width(), info.length(), info.totalBlocks()), x + 8, y + 46, 0xFFAAAAAA, true);

            Component costComp = (info.cost() == 0)
                    ? ZooTranslations.component("zootycoon.hud.claim.cost_free")
                    : ZooTranslations.component("zootycoon.hud.claim.cost_emerald", info.cost(), info.newBlocks());
            int costCol = (info.cost() == 0) ? 0xFF55FF55 : (info.canAfford() ? 0xFF00E5FF : 0xFFFF5555);
            graphics.drawString(font, costComp, x + 8, y + 58, costCol, true);

            if (info.conflictOther()) {
                graphics.drawString(font, ZooTranslations.component("zootycoon.hud.claim.conflict"), x + 8, y + 70, 0xFFFF2222, true);
            }

            graphics.fill(x + 8, y + 83, x + width - 8, y + 84, 0xFF2A2E37);

            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.controls.title"), x + 8, y + 88, 0xFFFFD700, true);
            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.claim.ctrl_confirm"), x + 8, y + 100, 0xFFFFFFFF, true);
            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.claim.ctrl_move"), x + 8, y + 112, 0xFFFFFFFF, true);
            graphics.drawString(font, ZooTranslations.component("zootycoon.hud.ctrl_cancel"), x + 8, y + 124, 0xFFCCCCCC, true);
        }
    }

    private static void renderStudioHud(GuiGraphics graphics, Font font) {
        int x = 12;
        int y = 12;
        int width = 240;
        int height = 108;

        graphics.fill(x, y, x + width, y + height, 0xD010141A);
        graphics.fill(x, y, x + width, y + 1, 0xFF2A2E37);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF2A2E37);
        graphics.fill(x, y + 1, x + 1, y + height, 0xFF2A2E37);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF2A2E37);

        String catName = "enclosures".equalsIgnoreCase(ZooTycoonClient.studioCategory) ? "Kandang Satwa" : "Fasilitas Pengunjung";
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.studio.title"), x + 8, y + 8, 0xFF00E5FF, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.studio.category", catName), x + 8, y + 20, 0xFFFFFFFF, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.studio.plot_info"), x + 8, y + 32, 0xFFAAAAAA, true);

        graphics.fill(x + 8, y + 43, x + width - 8, y + 44, 0xFF2A2E37);

        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.controls.title"), x + 8, y + 47, 0xFFFFD700, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.studio.ctrl_stick"), x + 8, y + 59, 0xFF55FF55, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.studio.ctrl_save_enc"), x + 8, y + 71, 0xFFDDDDDD, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.studio.ctrl_save_ess"), x + 8, y + 83, 0xFFDDDDDD, true);
        graphics.drawString(font, ZooTranslations.component("zootycoon.hud.studio.ctrl_exit"), x + 8, y + 95, 0xFFFF5555, true);
    }
}
