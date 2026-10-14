package net.zootycoon.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.util.ZooTranslations;

import java.util.Optional;

/**
 * Base dialog screen in the style of Easy NPC (BO's Easy NPC):
 * - Left column: Large borderless 3D interactive entity preview following mouse cursor
 * - Right column: Title Case NPC name and role, stylish speech bubble with typewriter animation
 * - Bottom row: Full action buttons
 */
public abstract class EasyNpcDialogScreen extends Screen {

    protected final int panelWidth = 380;
    protected final int panelHeight = 236;

    protected int startX;
    protected int startY;

    protected LivingEntity previewEntity;
    protected Component npcName;
    protected Component npcRole;
    protected Component dialogMessage;
    protected long dialogStartTime;

    public EasyNpcDialogScreen(Component screenTitle, Component npcName, Component npcRole, Component dialogMessage, LivingEntity previewEntity) {
        super(screenTitle);
        this.npcName = npcName;
        this.npcRole = npcRole;
        this.dialogMessage = dialogMessage;
        this.previewEntity = previewEntity;
        this.dialogStartTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        this.startX = (this.width - panelWidth) / 2;
        this.startY = (this.height - panelHeight) / 2;
        initDialogWidgets();
    }

    protected abstract void initDialogWidgets();

    @Override
    protected void renderBlurredBackground(float delta) {
        // Disabled 1.21 menu blur to keep NPC dialog crisp and readable
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Darkened overlay backdrop
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        // 1. Main Slate Dialog Panel (Easy NPC aesthetic)
        graphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xF212151D);
        graphics.fill(startX, startY, startX + panelWidth, startY + 1, 0xFF2A3140);
        graphics.fill(startX, startY + panelHeight - 1, startX + panelWidth, startY + panelHeight, 0xFF2A3140);
        graphics.fill(startX, startY, startX + 1, startY + panelHeight, 0xFF2A3140);
        graphics.fill(startX + panelWidth - 1, startY, startX + panelWidth, startY + panelHeight, 0xFF2A3140);

        // 2. Left Column: Large Borderless 3D Entity Preview (No inner box container)
        int previewX = startX + 6;
        int previewY = startY + 8;
        int previewW = 114;
        int previewH = 190;

        if (previewEntity != null) {
            try {
                float bbH = Math.max(0.6f, previewEntity.getBbHeight());
                int scale = (int) Math.clamp(142.0f / bbH, 50, 105);
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics,
                        previewX, previewY, previewX + previewW, previewY + previewH,
                        scale, 0.05f, (float) mouseX, (float) mouseY, previewEntity
                );
            } catch (Throwable ignored) {
                graphics.drawCenteredString(font, ZooTranslations.get("zootycoon.dialog.npc_fallback"), previewX + previewW / 2, previewY + previewH / 2, 0xFF718096);
            }
        }

        // 3. Right Column: Header (NPC Name & Role)
        int rightX = previewX + previewW + 8;
        int rightW = panelWidth - (rightX - startX) - 12;

        graphics.drawString(this.font, npcName, rightX, startY + 12, 0xFFFFD700, false);
        graphics.drawString(this.font, npcRole, rightX, startY + 23, 0xFFA0AAB8, false);

        // Divider under header
        graphics.fill(rightX, startY + 34, startX + panelWidth - 12, startY + 35, 0xFF252D3C);

        // 4. Speech Bubble (Dialogue Box)
        int bubbleX = rightX;
        int bubbleY = startY + 39;
        int bubbleW = rightW;
        int bubbleH = getSpeechBubbleHeight();

        // Speech bubble frame
        graphics.fill(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH, 0xFF19202D);
        graphics.fill(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + 1, 0xFF313B4E);
        graphics.fill(bubbleX, bubbleY + bubbleH - 1, bubbleX + bubbleW, bubbleY + bubbleH, 0xFF313B4E);
        graphics.fill(bubbleX, bubbleY, bubbleX + 1, bubbleY + bubbleH, 0xFF313B4E);
        graphics.fill(bubbleX + bubbleW - 1, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH, 0xFF313B4E);

        // Pointer triangle notch pointing to NPC on the left
        graphics.fill(bubbleX - 4, bubbleY + 14, bubbleX, bubbleY + 20, 0xFF19202D);
        graphics.fill(bubbleX - 4, bubbleY + 14, bubbleX - 3, bubbleY + 20, 0xFF313B4E);

        // Typewriter animated word-wrapped dialogue text
        int visibleChars = Math.max(1, (int) ((System.currentTimeMillis() - dialogStartTime) / 18L));
        Component typedComponent = truncateComponent(dialogMessage, visibleChars);
        var wrappedLines = this.font.split(typedComponent, bubbleW - 16);
        int textY = bubbleY + 8;
        for (var line : wrappedLines) {
            if (textY + 9 > bubbleY + bubbleH - 4) break;
            graphics.drawString(this.font, line, bubbleX + 8, textY, 0xFFE2E8F0, false);
            textY += 11;
        }

        // 5. Context / Content Area (Only used by BuilderInspectionScreen for missing tools/blocks)
        int contentX = rightX;
        int contentY = bubbleY + bubbleH + 6;
        int contentW = rightW;
        int contentH = (startY + panelHeight - 34) - contentY - 4;

        renderDialogContent(graphics, contentX, contentY, contentW, contentH, mouseX, mouseY, delta);

        // 6. Render widgets (buttons, etc.)
        super.render(graphics, mouseX, mouseY, delta);
    }

    private static Component truncateComponent(Component source, int maxChars) {
        if (source == null) return Component.empty();
        String plain = source.getString();
        if (maxChars >= plain.length()) return source;
        MutableComponent result = Component.empty();
        int[] remaining = { maxChars };
        source.visit((style, part) -> {
            if (remaining[0] <= 0) return Optional.of(Unit.INSTANCE);
            if (part.length() <= remaining[0]) {
                result.append(Component.literal(part).withStyle(style));
                remaining[0] -= part.length();
            } else {
                result.append(Component.literal(part.substring(0, remaining[0])).withStyle(style));
                remaining[0] = 0;
            }
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    protected int getSpeechBubbleHeight() {
        return 118;
    }

    protected abstract void renderDialogContent(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta);

    public void setPreviewEntity(LivingEntity entity) {
        this.previewEntity = entity;
    }

    public void setDialogMessage(Component message) {
        this.dialogMessage = message;
        this.dialogStartTime = System.currentTimeMillis();
    }
}
