package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.construction.ConstructionSite;
import net.zootycoon.economy.EconomyConfig;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.Map;
import java.util.UUID;

public class BuilderInspectionScreen extends Screen {
    private final UUID siteId;
    private final String schematicName;
    private final float progress;
    private final int placedCount;
    private final int totalCount;
    private final Map<Item, Integer> missingMaterials;
    private final String builderName;

    // Clearing land & tools phase
    private final int phase; // 0 = clearing, 1 = placing, 2 = gajian
    private final int obstructingCount;
    private final boolean needsShovel;
    private final boolean hasShovel;
    private final boolean needsPickaxe;
    private final boolean hasPickaxe;
    private final boolean needsAxe;
    private final boolean hasAxe;

    // Wage payment phase
    private final int wageDue;
    private final UUID builderUuid;

    // Material list scrolling
    private double scrollOffset = 0;
    private boolean isDraggingScrollbar = false;

    public BuilderInspectionScreen(
            UUID siteId, String schematicName, float progress, int placed, int total,
            Map<Item, Integer> missing, String builderName,
            int phase, int obstructingCount,
            boolean needsShovel, boolean hasShovel,
            boolean needsPickaxe, boolean hasPickaxe,
            boolean needsAxe, boolean hasAxe,
            int wageDue, UUID builderUuid
    ) {
        super(Component.literal("Kuli Bangunan: " + (builderName != null ? builderName : "Budi Santoso")));
        this.siteId = siteId;
        this.schematicName = schematicName;
        this.progress = progress;
        this.placedCount = placed;
        this.totalCount = total;
        this.missingMaterials = missing;
        this.builderName = builderName != null && !builderName.isEmpty() ? builderName : "Budi Santoso";
        this.phase = phase;
        this.obstructingCount = obstructingCount;
        this.needsShovel = needsShovel;
        this.hasShovel = hasShovel;
        this.needsPickaxe = needsPickaxe;
        this.hasPickaxe = hasPickaxe;
        this.needsAxe = needsAxe;
        this.hasAxe = hasAxe;
        this.wageDue = wageDue;
        this.builderUuid = builderUuid != null ? builderUuid : UUID.randomUUID();
    }

    public BuilderInspectionScreen(
            UUID siteId, String schematicName, float progress, int placed, int total,
            Map<Item, Integer> missing, String builderName,
            int phase, int obstructingCount,
            boolean needsShovel, boolean hasShovel,
            boolean needsPickaxe, boolean hasPickaxe,
            boolean needsAxe, boolean hasAxe
    ) {
        this(siteId, schematicName, progress, placed, total, missing, builderName, phase, obstructingCount, needsShovel, hasShovel, needsPickaxe, hasPickaxe, needsAxe, hasAxe, 0, UUID.randomUUID());
    }

    public BuilderInspectionScreen(UUID siteId, String schematicName, float progress, int placed, int total, Map<Item, Integer> missing, String builderName) {
        this(siteId, schematicName, progress, placed, total, missing, builderName, 1, 0, false, true, false, true, false, true, 0, UUID.randomUUID());
    }

    public BuilderInspectionScreen(UUID siteId, String schematicName, float progress, int placed, int total, Map<Item, Integer> missing) {
        this(siteId, schematicName, progress, placed, total, missing, "Budi Santoso");
    }

    public BuilderInspectionScreen(ConstructionSite site) {
        this(
                site.getSiteId(),
                site.getSchematic().getName(),
                site.getProgressPercentage(),
                site.getPlacedBlocksCount(),
                site.getTotalBlocks(),
                site.getMissingMaterials(),
                "Budi Santoso",
                site.isClearingPhase() ? 0 : 1,
                site.getObstructingBlocks().size(),
                site.needsShovel(), site.hasShovel(),
                site.needsPickaxe(), site.hasPickaxe(),
                site.needsAxe(), site.hasAxe(),
                0,
                site.getBuilderUuid() != null ? site.getBuilderUuid() : UUID.randomUUID()
        );
    }

    private int getPlayerEmeraldCount() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(Items.EMERALD)) {
                count += stack.getCount();
            } else if (stack.is(Items.EMERALD_BLOCK)) {
                count += stack.getCount() * 9;
            }
        }
        return count;
    }

    private void renderWrappedBubbleText(GuiGraphics graphics, String rawText, int bubbleX, int bubbleY, int bubbleW) {
        int maxTextWidth = bubbleW - 20;
        var lines = this.font.split(Component.literal(rawText), maxTextWidth);
        int textY = bubbleY + 8;
        for (var line : lines) {
            graphics.drawString(this.font, line, bubbleX + 10, textY, 0xFFE0E6ED, false);
            textY += 12;
        }
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = 380;
        int panelHeight = (phase == 2) ? 134 : 264;
        int startX = (this.width - panelWidth) / 2;
        int startY = (this.height - panelHeight) / 2;

        int footerHeight = 42;
        int footerY = startY + panelHeight - footerHeight;
        int btnY = footerY + 9;
        int btnHeight = 24;
        int twoBtnW = (panelWidth - 24 - 10) / 2;
        int btn1X = startX + 12;
        int btn2X = btn1X + twoBtnW + 10;

        boolean toolsNeeded = (needsShovel && !hasShovel) || (needsPickaxe && !hasPickaxe) || (needsAxe && !hasAxe);

        if (phase == 0) {
            // PHASE 0: PEMBERSIHAN LAHAN
            // Button 1: Kasih Alat
            String btn1Text = toolsNeeded ? "Kasih Alat" : "Alat Lengkap";
            var btn1 = new ZooButton(btn1X, btnY, twoBtnW, btnHeight, Component.literal(btn1Text), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.DepositToolsPayload(siteId));
                this.onClose();
            }).asPrimary();
            btn1.active = toolsNeeded;
            addRenderableWidget(btn1);

            // Button 2: Tutup Dulu
            addRenderableWidget(new ZooButton(btn2X, btnY, twoBtnW, btnHeight, Component.literal("Tutup Dulu"), b -> {
                this.onClose();
            }).asPrimary());
        } else if (phase == 1) {
            // PHASE 1: KONSTRUKSI BANGUNAN
            // Button 1: Kasih Bahan
            addRenderableWidget(new ZooButton(btn1X, btnY, twoBtnW, btnHeight, Component.literal("Kasih Bahan"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.DepositMaterialsPayload(siteId));
                this.onClose();
            }).asPrimary());

            // Button 2: Tutup Dulu
            addRenderableWidget(new ZooButton(btn2X, btnY, twoBtnW, btnHeight, Component.literal("Tutup Dulu"), b -> {
                this.onClose();
            }).asPrimary());
        } else {
            // PHASE 2: GAJIAN / NAGIH BAYARAN
            int pEmeralds = getPlayerEmeraldCount();
            boolean canPay = pEmeralds >= wageDue;

            // Button 1: Bayar Gaji (with custom emerald icon from Icons pack)
            String payText = "Bayar Gaji (" + wageDue + " Em.)";
            var payBtn = new ZooButton(btn1X, btnY, twoBtnW, btnHeight, Component.literal(payText), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.PayBuilderWagePayload(builderUuid));
                this.onClose();
            }).setCustomIcon(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/emerald.png")).asEmerald();
            payBtn.active = canPay;
            addRenderableWidget(payBtn);

            // Button 2: Nanti Dulu
            addRenderableWidget(new ZooButton(btn2X, btnY, twoBtnW, btnHeight, Component.literal("Nanti Dulu"), b -> {
                this.onClose();
            }).asPrimary());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int panelWidth = 380;
        int panelHeight = (phase == 2) ? 134 : 264;
        int startX = (this.width - panelWidth) / 2;
        int startY = (this.height - panelHeight) / 2;

        // 1. Main Essential-Mod Dark Slate Dialog Panel
        graphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xF212151D);
        graphics.fill(startX, startY, startX + panelWidth, startY + 1, 0xFF2A3140);
        graphics.fill(startX, startY + panelHeight - 1, startX + panelWidth, startY + panelHeight, 0xFF2A3140);
        graphics.fill(startX, startY, startX + 1, startY + panelHeight, 0xFF2A3140);
        graphics.fill(startX + panelWidth - 1, startY, startX + panelWidth, startY + panelHeight, 0xFF2A3140);

        // 2. Integrated Footer Background & Divider (Unifies buttons into the card)
        int footerHeight = 42;
        int footerY = startY + panelHeight - footerHeight;
        graphics.fill(startX + 1, footerY, startX + panelWidth - 1, startY + panelHeight - 1, 0xFF141720);
        graphics.fill(startX, footerY, startX + panelWidth, footerY + 1, 0xFF242A38);

        // 3. Worker Profile Header (Nama Kuli & Proyek)
        String nameHeader = builderName + "  (Kuli Proyek)";
        graphics.drawString(this.font, Component.literal(nameHeader), startX + 16, startY + 12, 0xFFFFFFFF, true);
        String projectHeader = "Proyek: " + schematicName;
        graphics.drawString(this.font, Component.literal(projectHeader), startX + 16, startY + 24, 0xFF8B949E, false);

        // 4. Speech Box (Dialogue Bubble) with dynamic text-wrap so text never overflows
        int bubbleX = startX + 14;
        int bubbleY = startY + 38;
        int bubbleW = panelWidth - 28;
        int bubbleH = 44;
        graphics.fill(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH, 0xFF181C26);
        graphics.fill(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + 1, 0xFF2C3342);
        graphics.fill(bubbleX, bubbleY + bubbleH - 1, bubbleX + bubbleW, bubbleY + bubbleH, 0xFF2C3342);
        graphics.fill(bubbleX, bubbleY, bubbleX + 1, bubbleY + bubbleH, 0xFF2C3342);
        graphics.fill(bubbleX + bubbleW - 1, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH, 0xFF2C3342);

        if (phase == 0) {
            // PHASE 0: Pembersihan Lahan (Ringkas & Wrapped)
            boolean toolsNeeded = (needsShovel && !hasShovel) || (needsPickaxe && !hasPickaxe) || (needsAxe && !hasAxe);
            String speech = toolsNeeded
                    ? "Lahannya masih kotor Bos, ada " + obstructingCount + " blok ngehalang. Bagi alat kerja dulu ya!"
                    : "Alat kerja udah mantap Bos! Lagi ane babat bersih dulu sebelum bangun.";
            renderWrappedBubbleText(graphics, speech, bubbleX, bubbleY, bubbleW);

            int sectionY = bubbleY + bubbleH + 12;
            graphics.drawString(this.font, Component.literal("PERALATAN YANG DIBUTUHKAN:"), startX + 16, sectionY, 0xFFD0D7DE, true);

            Minecraft mc = Minecraft.getInstance();
            boolean pHasShovel = false;
            boolean pHasPickaxe = false;
            boolean pHasAxe = false;
            if (mc.player != null) {
                for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                    ItemStack is = mc.player.getInventory().getItem(i);
                    if (is.is(ItemTags.SHOVELS)) pHasShovel = true;
                    if (is.is(ItemTags.PICKAXES)) pHasPickaxe = true;
                    if (is.is(ItemTags.AXES)) pHasAxe = true;
                }
            }

            int toolRowY = sectionY + 15;

            // Row 1: Sekop
            renderToolRow(graphics, startX + 14, toolRowY, panelWidth - 28, "Sekop (Shovel)", needsShovel, hasShovel, pHasShovel);
            toolRowY += 24;

            // Row 2: Beliung
            renderToolRow(graphics, startX + 14, toolRowY, panelWidth - 28, "Beliung (Pickaxe)", needsPickaxe, hasPickaxe, pHasPickaxe);
            toolRowY += 24;

            // Row 3: Kapak
            renderToolRow(graphics, startX + 14, toolRowY, panelWidth - 28, "Kapak (Axe)", needsAxe, hasAxe, pHasAxe);

        } else if (phase == 1) {
            // PHASE 1: Konstruksi Bangunan (Ringkas & Wrapped)
            String speech = missingMaterials.isEmpty()
                    ? "Bahan udah lengkap Bos, lagi dipasang nih. Santai aja ya!"
                    : "Lahan udah bersih Bos, tapi bahannya masih kurang. Oper bahannya biar bisa lanjut rakit!";
            renderWrappedBubbleText(graphics, speech, bubbleX, bubbleY, bubbleW);

            // Progress Bar
            int barX = startX + 14;
            int barY = bubbleY + bubbleH + 10;
            int barW = panelWidth - 28;
            int barH = 14;

            graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF1A1E27);
            int filledW = (int) (barW * Math.min(1.0f, Math.max(0.0f, progress / 100.0f)));
            graphics.fill(barX, barY, barX + filledW, barY + barH, 0xFF35445C);

            String progStr = String.format("%.1f%% Kelar (%d/%d Blok)", progress, placedCount, totalCount);
            graphics.drawCenteredString(this.font, Component.literal(progStr), this.width / 2, barY + 3, 0xFFFFFFFF);

            // Material Requirements Section
            int matY = barY + barH + 10;
            graphics.drawString(this.font, Component.literal("BAHAN-BAHAN YANG MASIH KURANG:"), startX + 16, matY, 0xFFD0D7DE, true);

            int itemStartY = matY + 14;
            if (missingMaterials.isEmpty()) {
                graphics.drawString(this.font, Component.literal("Bahan udah aman semua Bos. Ane lagi kerja nih!"), startX + 16, itemStartY + 6, 0xFF8B949E, false);
            } else {
                int clipTop = itemStartY - 2;
                int clipBottom = footerY - 4;
                int visibleHeight = clipBottom - clipTop;
                int totalListHeight = missingMaterials.size() * 22;
                double maxScroll = Math.max(0, totalListHeight - visibleHeight);
                this.scrollOffset = Math.clamp(this.scrollOffset, 0.0, maxScroll);

                // Scissor to clip scrolled items inside dialog body
                graphics.enableScissor(startX + 14, clipTop, startX + panelWidth - 14, clipBottom);

                int row = 0;
                for (var entry : missingMaterials.entrySet()) {
                    int curY = itemStartY + (row * 22) - (int) this.scrollOffset;
                    if (curY + 20 >= clipTop && curY <= clipBottom) {
                        int rowRight = startX + panelWidth - (maxScroll > 0 ? 22 : 14);
                        graphics.fill(startX + 14, curY - 2, rowRight, curY + 18, 0xFF161922);

                        ItemStack sample = new ItemStack(entry.getKey());
                        graphics.renderItem(sample, startX + 18, curY);

                        // Check if player has this in inventory
                        Minecraft mc = Minecraft.getInstance();
                        int playerHas = 0;
                        if (mc.player != null) {
                            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                                ItemStack pStack = mc.player.getInventory().getItem(i);
                                if (!pStack.isEmpty() && pStack.is(entry.getKey())) {
                                    playerHas += pStack.getCount();
                                }
                            }
                        }
                        String invStr = playerHas > 0 ? "(Punya: " + playerHas + ")" : "(Gak Ada)";
                        int invColor = playerHas > 0 ? 0xFFFFFFFF : 0xFF6E7681;
                        int invX = rowRight - font.width(invStr) - 6;
                        graphics.drawString(this.font, Component.literal(invStr), invX, curY + 4, invColor, false);

                        String itemName = sample.getHoverName().getString() + "  x" + entry.getValue();
                        int maxItemW = Math.max(20, (invX - (startX + 40)) - 6);
                        String fittedName = font.plainSubstrByWidth(itemName, maxItemW);
                        graphics.drawString(this.font, Component.literal(fittedName), startX + 40, curY + 4, 0xFFFFFFFF, false);
                    }
                    row++;
                }

                graphics.disableScissor();

                // Draw sleek scrollbar track & thumb when materials exceed visible area
                if (maxScroll > 0) {
                    int scrollTrackX = startX + panelWidth - 19;
                    int scrollTrackW = 4;
                    graphics.fill(scrollTrackX, clipTop, scrollTrackX + scrollTrackW, clipBottom, 0xFF181C26);
                    int thumbHeight = Math.max(14, (int) ((float) visibleHeight / totalListHeight * visibleHeight));
                    int thumbY = clipTop + (int) ((this.scrollOffset / maxScroll) * (visibleHeight - thumbHeight));
                    graphics.fill(scrollTrackX, thumbY, scrollTrackX + scrollTrackW, thumbY + thumbHeight, 0xFF3B4861);
                }
            }
        } else {
            // PHASE 2: Gajian / Nagih Bayaran (Cukup Dialog Saja & Tombol)
            String speech = "Alhamdulillah Bos! Proyek '" + schematicName + "' udah beres 100%. Bagi gajinya ya Bos!";
            renderWrappedBubbleText(graphics, speech, bubbleX, bubbleY, bubbleW);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (phase == 1 && !missingMaterials.isEmpty()) {
            int panelWidth = 380;
            int panelHeight = 264;
            int startX = (this.width - panelWidth) / 2;
            int startY = (this.height - panelHeight) / 2;
            int footerY = startY + panelHeight - 42;
            int itemStartY = startY + 140;
            int clipTop = itemStartY - 2;
            int clipBottom = footerY - 4;
            int visibleHeight = clipBottom - clipTop;
            int totalListHeight = missingMaterials.size() * 22;
            double maxScroll = Math.max(0, totalListHeight - visibleHeight);

            if (maxScroll > 0 && mouseX >= startX && mouseX <= startX + panelWidth && mouseY >= clipTop && mouseY <= footerY) {
                this.scrollOffset = Math.clamp(this.scrollOffset - (verticalAmount * 16.0), 0.0, maxScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (phase == 1 && !missingMaterials.isEmpty()) {
            int panelWidth = 380;
            int panelHeight = 264;
            int startX = (this.width - panelWidth) / 2;
            int startY = (this.height - panelHeight) / 2;
            int footerY = startY + panelHeight - 42;
            int itemStartY = startY + 140;
            int clipTop = itemStartY - 2;
            int clipBottom = footerY - 4;
            int scrollTrackX = startX + panelWidth - 22;

            if (mouseX >= scrollTrackX && mouseX <= scrollTrackX + 10 && mouseY >= clipTop && mouseY <= clipBottom) {
                isDraggingScrollbar = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingScrollbar && phase == 1 && !missingMaterials.isEmpty()) {
            int panelHeight = 264;
            int startY = (this.height - panelHeight) / 2;
            int footerY = startY + panelHeight - 42;
            int itemStartY = startY + 140;
            int clipTop = itemStartY - 2;
            int clipBottom = footerY - 4;
            int visibleHeight = clipBottom - clipTop;
            int totalListHeight = missingMaterials.size() * 22;
            double maxScroll = Math.max(0, totalListHeight - visibleHeight);

            if (maxScroll > 0 && visibleHeight > 0) {
                double scrollDelta = (dragY / visibleHeight) * totalListHeight;
                this.scrollOffset = Math.clamp(this.scrollOffset + scrollDelta, 0.0, maxScroll);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void renderToolRow(GuiGraphics graphics, int x, int y, int width, String toolName, boolean needed, boolean hasTool, boolean playerHasInInventory) {
        graphics.fill(x, y - 2, x + width, y + 20, 0xFF161922);
        graphics.fill(x, y - 2, x + width, y - 1, 0xFF242A38);
        graphics.fill(x, y + 19, x + width, y + 20, 0xFF242A38);
        graphics.fill(x, y - 2, x + 1, y + 20, 0xFF242A38);
        graphics.fill(x + width - 1, y - 2, x + width, y + 20, 0xFF242A38);

        graphics.drawString(this.font, Component.literal(toolName), x + 10, y + 4, 0xFFFFFFFF, false);

        String statusStr;
        int statusColor;
        if (!needed) {
            statusStr = "Gak Butuh";
            statusColor = 0xFF8B949E;
        } else if (hasTool) {
            statusStr = "Udah Siap";
            statusColor = 0xFF3FB950;
        } else {
            statusStr = "Butuh Nih";
            statusColor = 0xFFF85149;
        }

        String invInfo = playerHasInInventory ? "(Ada di Badan)" : "";
        int invInfoColor = 0xFF58A6FF;

        int statusX = x + width - font.width(statusStr) - 10;
        graphics.drawString(this.font, Component.literal(statusStr), statusX, y + 4, statusColor, true);

        if (!invInfo.isEmpty() && needed && !hasTool) {
            int invX = statusX - font.width(invInfo) - 8;
            graphics.drawString(this.font, Component.literal(invInfo), invX, y + 4, invInfoColor, false);
        }
    }
}
