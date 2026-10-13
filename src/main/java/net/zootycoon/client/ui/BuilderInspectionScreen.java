package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.construction.ConstructionSite;
import net.zootycoon.economy.ZooEconomyManager;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BuilderInspectionScreen extends EasyNpcDialogScreen {

    private final UUID siteId;
    private final String schematicName;
    private final float progress;
    private final int placedCount;
    private final int totalCount;
    private final Map<Item, Integer> missingMaterials;
    private final String builderNameStr;

    private final int phase; // 0 = clearing, 1 = placing, 2 = gajian, 3 = idle
    private final int obstructingCount;
    private final boolean needsShovel;
    private final boolean hasShovel;
    private final boolean needsPickaxe;
    private final boolean hasPickaxe;
    private final boolean needsAxe;
    private final boolean hasAxe;

    private final int wageDue;
    private final UUID builderUuid;

    public BuilderInspectionScreen(
            UUID siteId, String schematicName, float progress, int placed, int total,
            Map<Item, Integer> missing, String builderName,
            int phase, int obstructingCount,
            boolean needsShovel, boolean hasShovel,
            boolean needsPickaxe, boolean hasPickaxe,
            boolean needsAxe, boolean hasAxe,
            int wageDue, UUID builderUuid
    ) {
        super(
                Component.literal(builderName != null && !builderName.isEmpty() ? builderName : ZooTranslations.get("zootycoon.dialog.builder.default_name")),
                Component.literal(builderName != null && !builderName.isEmpty() ? builderName : ZooTranslations.get("zootycoon.dialog.builder.default_name")),
                ZooTranslations.component("zootycoon.dialog.builder.role"),
                getInitialSpeech(phase, schematicName != null ? schematicName : ZooTranslations.get("zootycoon.dialog.builder.default_project"), wageDue),
                resolveBuilderEntity(builderUuid)
        );
        this.siteId = siteId;
        this.schematicName = schematicName != null ? schematicName : ZooTranslations.get("zootycoon.dialog.builder.default_project");
        this.progress = progress;
        this.placedCount = placed;
        this.totalCount = total;
        this.missingMaterials = missing != null ? missing : Map.of();
        this.builderNameStr = builderName != null && !builderName.isEmpty() ? builderName : ZooTranslations.get("zootycoon.dialog.builder.default_name");
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
        this(siteId, schematicName, progress, placed, total, missing, ZooTranslations.get("zootycoon.dialog.builder.default_name"));
    }

    public BuilderInspectionScreen(ConstructionSite site) {
        this(
                site.getSiteId(),
                site.getSchematic().getName(),
                site.getProgressPercentage(),
                site.getPlacedBlocksCount(),
                site.getTotalBlocks(),
                site.getMissingMaterials(),
                ZooTranslations.get("zootycoon.dialog.builder.default_name"),
                site.isClearingPhase() ? 0 : 1,
                site.getObstructingBlocks().size(),
                site.needsShovel(), site.hasShovel(),
                site.needsPickaxe(), site.hasPickaxe(),
                site.needsAxe(), site.hasAxe(),
                0,
                site.getBuilderUuid() != null ? site.getBuilderUuid() : UUID.randomUUID()
        );
    }

    private static Component getInitialSpeech(int phase, String schematicName, int wageDue) {
        if (phase == 0) {
            return ZooTranslations.component("zootycoon.dialog.builder.speech_phase0", schematicName);
        } else if (phase == 1) {
            return ZooTranslations.component("zootycoon.dialog.builder.speech_phase1", schematicName);
        } else if (phase == 2) {
            return ZooTranslations.component("zootycoon.dialog.builder.speech_phase2", schematicName, wageDue);
        } else {
            return ZooTranslations.component("zootycoon.dialog.builder.speech_idle");
        }
    }

    private static LivingEntity resolveBuilderEntity(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            if (uuid != null) {
                for (var ent : mc.level.entitiesForRendering()) {
                    if (ent.getUUID().equals(uuid) && ent instanceof LivingEntity living) {
                        return living;
                    }
                }
            }
            return ZooEntities.BUILDER.create(mc.level);
        }
        return null;
    }

    private int getAvailableEmeralds() {
        Minecraft mc = Minecraft.getInstance();
        int invEmeralds = 0;
        if (mc.player != null) {
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (stack.is(Items.EMERALD)) {
                    invEmeralds += stack.getCount();
                } else if (stack.is(Items.EMERALD_BLOCK)) {
                    invEmeralds += stack.getCount() * 9;
                }
            }
        }
        return Math.max(ZooEconomyManager.getClientEmeraldBalance(), invEmeralds);
    }

    @Override
    protected void initDialogWidgets() {
        int btnY = startY + panelHeight - 30;
        int btnW = (panelWidth - 24 - 8) / 2;

        boolean toolsNeeded = (needsShovel && !hasShovel) || (needsPickaxe && !hasPickaxe) || (needsAxe && !hasAxe);

        if (phase == 0) {
            // PHASE 0: PEMBERSIHAN LAHAN (No generic icon on Serahkan Alat)
            Component btn1Text = ZooTranslations.component(toolsNeeded ? "zootycoon.dialog.builder.btn.give_tools" : "zootycoon.dialog.builder.btn.tools_ready");
            var btn1 = new ZooButton(startX + 12, btnY, btnW, 22, btn1Text, b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.DepositToolsPayload(siteId));
                this.onClose();
            }).asPrimary();
            btn1.active = toolsNeeded;
            addRenderableWidget(btn1);

            var closeBtn = new ZooButton(startX + 12 + btnW + 8, btnY, btnW, 22, ZooTranslations.component("zootycoon.dialog.builder.btn.later"), b -> {
                this.onClose();
            }).asPrimary();
            addRenderableWidget(closeBtn);

        } else if (phase == 1) {
            // PHASE 1: KONSTRUKSI BANGUNAN (No generic icon on Serahkan Bahan)
            var depositBtn = new ZooButton(startX + 12, btnY, btnW, 22, ZooTranslations.component("zootycoon.dialog.builder.btn.give_materials"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.DepositMaterialsPayload(siteId));
                this.onClose();
            }).asPrimary();
            addRenderableWidget(depositBtn);

            var closeBtn = new ZooButton(startX + 12 + btnW + 8, btnY, btnW, 22, ZooTranslations.component("zootycoon.dialog.builder.btn.later"), b -> {
                this.onClose();
            }).asPrimary();
            addRenderableWidget(closeBtn);

        } else if (phase == 2) {
            // PHASE 2: GAJIAN / BAYAR UPAH (Authentic vanilla emerald icon for price display)
            int emeralds = getAvailableEmeralds();
            boolean canPay = emeralds >= wageDue;

            var payBtn = new ZooButton(startX + 12, btnY, btnW, 22, ZooTranslations.component("zootycoon.dialog.builder.btn.pay_wage", wageDue), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.PayBuilderWagePayload(builderUuid));
                this.onClose();
            }).setItemIcon(new ItemStack(Items.EMERALD)).asEmerald();
            payBtn.active = canPay;
            addRenderableWidget(payBtn);

            var laterBtn = new ZooButton(startX + 12 + btnW + 8, btnY, btnW, 22, ZooTranslations.component("zootycoon.dialog.builder.btn.later"), b -> {
                this.onClose();
            }).asPrimary();
            addRenderableWidget(laterBtn);

        } else {
            // IDLE: BELUM ADA PROYEK
            var closeBtn = new ZooButton(startX + 12, btnY, panelWidth - 24, 22, ZooTranslations.component("zootycoon.dialog.btn.okay"), b -> {
                this.onClose();
            }).asPrimary();
            addRenderableWidget(closeBtn);
        }
    }

    @Override
    protected int getSpeechBubbleHeight() {
        return (phase == 0 || phase == 1) ? 48 : 118;
    }

    @Override
    protected void renderDialogContent(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta) {
        if (phase == 0) {
            // Missing tools info (Kept below dialog as requested)
            graphics.drawString(this.font, ZooTranslations.component("zootycoon.dialog.builder.clearing_label").withStyle(ChatFormatting.GRAY)
                    .append(ZooTranslations.component("zootycoon.dialog.builder.blocks_count", obstructingCount).withStyle(ChatFormatting.YELLOW)), contentX, contentY, 0xFFFFFFFF, false);

            graphics.drawString(this.font, ZooTranslations.component("zootycoon.dialog.builder.tools_needed_header").withStyle(ChatFormatting.WHITE), contentX, contentY + 12, 0xFFFFFFFF, false);

            renderToolStatus(graphics, contentX, contentY + 24, ZooTranslations.get("zootycoon.dialog.builder.tool.shovel"), needsShovel, hasShovel, new ItemStack(Items.IRON_SHOVEL));
            renderToolStatus(graphics, contentX + 80, contentY + 24, ZooTranslations.get("zootycoon.dialog.builder.tool.pickaxe"), needsPickaxe, hasPickaxe, new ItemStack(Items.IRON_PICKAXE));
            renderToolStatus(graphics, contentX + 160, contentY + 24, ZooTranslations.get("zootycoon.dialog.builder.tool.axe"), needsAxe, hasAxe, new ItemStack(Items.IRON_AXE));

        } else if (phase == 1) {
            // Missing blocks info (Kept below dialog as requested)
            int progPercent = (int) progress;
            graphics.drawString(this.font, ZooTranslations.component("zootycoon.dialog.builder.progress_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(progPercent + "% (" + placedCount + "/" + totalCount + ")").withStyle(ChatFormatting.AQUA)), contentX, contentY, 0xFFFFFFFF, false);

            int barW = contentW - 4;
            int barH = 6;
            int barY = contentY + 12;
            graphics.fill(contentX, barY, contentX + barW, barY + barH, 0xFF141720);
            int fillW = (int) (barW * Math.clamp(progress / 100.0f, 0.0f, 1.0f));
            if (fillW > 0) {
                graphics.fill(contentX, barY, contentX + fillW, barY + barH, 0xFF388BFD);
            }
            graphics.fill(contentX, barY, contentX + barW, barY + 1, 0xFF2A3140);
            graphics.fill(contentX, barY + barH - 1, contentX + barW, barY + barH, 0xFF2A3140);

            graphics.drawString(this.font, ZooTranslations.component("zootycoon.dialog.builder.materials_needed_header").withStyle(ChatFormatting.YELLOW), contentX, barY + 10, 0xFFFFFFFF, false);

            int slotSize = 20;
            int slotX = contentX;
            int slotY = barY + 22;

            if (missingMaterials.isEmpty()) {
                graphics.drawString(this.font, ZooTranslations.component("zootycoon.dialog.builder.materials_complete").withStyle(ChatFormatting.GREEN), contentX, slotY + 4, 0xFF2ECC71, false);
            } else {
                List<Map.Entry<Item, Integer>> entryList = new ArrayList<>(missingMaterials.entrySet());
                ItemStack hoveredItem = null;
                for (int i = 0; i < Math.min(entryList.size(), 8); i++) {
                    var entry = entryList.get(i);
                    ItemStack stack = new ItemStack(entry.getKey());
                    int sx = slotX + (i * (slotSize + 4));

                    graphics.fill(sx, slotY, sx + slotSize, slotY + slotSize, 0xFF141720);
                    graphics.fill(sx, slotY, sx + slotSize, slotY + 1, 0xFF2B3344);
                    graphics.fill(sx, slotY + slotSize - 1, sx + slotSize, slotY + slotSize, 0xFF2B3344);
                    graphics.fill(sx, slotY + 1, sx + 1, slotY + slotSize, 0xFF2B3344);
                    graphics.fill(sx + slotSize - 1, slotY, sx + slotSize, slotY + slotSize, 0xFF2B3344);

                    graphics.renderFakeItem(stack, sx + 2, slotY + 2);
                    graphics.renderItemDecorations(this.font, stack, sx + 2, slotY + 2, String.valueOf(entry.getValue()));

                    if (mouseX >= sx && mouseX <= sx + slotSize && mouseY >= slotY && mouseY <= slotY + slotSize) {
                        hoveredItem = stack;
                    }
                }

                if (hoveredItem != null) {
                    graphics.renderTooltip(this.font, hoveredItem, mouseX, mouseY);
                }
            }
        }
    }

    private void renderToolStatus(GuiGraphics graphics, int x, int y, String name, boolean needed, boolean has, ItemStack icon) {
        graphics.renderFakeItem(icon, x, y);
        int textX = x + 18;
        graphics.drawString(this.font, name, textX, y + 1, 0xFFD0D7DE, false);

        String statusStr;
        int statusColor;
        if (!needed) {
            statusStr = ZooTranslations.get("zootycoon.dialog.builder.tool.not_needed");
            statusColor = 0xFF718096;
        } else if (has) {
            statusStr = ZooTranslations.get("zootycoon.dialog.builder.tool.ready");
            statusColor = 0xFF2ECC71;
        } else {
            statusStr = ZooTranslations.get("zootycoon.dialog.builder.tool.needed");
            statusColor = 0xFFE74C3C;
        }
        graphics.drawString(this.font, statusStr, textX, y + 10, statusColor, false);
    }
}
