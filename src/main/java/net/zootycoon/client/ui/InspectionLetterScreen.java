package net.zootycoon.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.List;

public class InspectionLetterScreen extends Screen {

    private final String zooName;
    private final int tierLevel;
    private final boolean passed;
    private final List<String> inspectedSpecies;
    private final List<String> improvementNotes;

    private final int panelWidth = 310;
    private final int panelHeight = 236;
    private int startX;
    private int startY;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    public InspectionLetterScreen(String zooName, int tierLevel, boolean passed, List<String> inspectedSpecies, List<String> improvementNotes) {
        super(ZooTranslations.component("zootycoon.license.letter.title"));
        this.zooName = (zooName != null && !zooName.isEmpty()) ? zooName : "Player's Zoo";
        this.tierLevel = Math.clamp(tierLevel, 1, 5);
        this.passed = passed;
        this.inspectedSpecies = inspectedSpecies != null ? new ArrayList<>(inspectedSpecies) : new ArrayList<>();
        this.improvementNotes = improvementNotes != null ? new ArrayList<>(improvementNotes) : new ArrayList<>();
    }

    public static void openFromItem(ItemStack stack) {
        String zooName = "Player's Zoo";
        int tierLevel = 1;
        boolean passed = true;
        List<String> species = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            if (tag.contains("ZooName")) zooName = tag.getString("ZooName");
            if (tag.contains("TierLevel")) tierLevel = tag.getInt("TierLevel");
            if (tag.contains("Passed")) passed = tag.getBoolean("Passed");
            if (tag.contains("InspectedSpecies", Tag.TAG_LIST)) {
                ListTag l = tag.getList("InspectedSpecies", Tag.TAG_STRING);
                for (int i = 0; i < l.size(); i++) species.add(l.getString(i));
            }
            if (tag.contains("ImprovementNotes", Tag.TAG_LIST)) {
                ListTag l = tag.getList("ImprovementNotes", Tag.TAG_STRING);
                for (int i = 0; i < l.size(); i++) notes.add(l.getString(i));
            }
        }
        Minecraft.getInstance().setScreen(new InspectionLetterScreen(zooName, tierLevel, passed, species, notes));
    }

    @Override
    protected void init() {
        super.init();
        this.startX = (this.width - panelWidth) / 2;
        this.startY = (this.height - panelHeight) / 2;

        int btnW = 110;
        int btnX = startX + (panelWidth - btnW) / 2;
        int btnY = startY + panelHeight - 28;
        this.addRenderableWidget(new ZooButton(btnX, btnY, btnW, 20, ZooTranslations.component("zootycoon.dialog.btn.close"), b -> this.onClose()).asPrimary());
    }

    @Override
    protected void renderBlurredBackground(float delta) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        // Official Document Outer Frame
        graphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xFF141821);
        int borderColor = passed ? 0xFF2E7D32 : 0xFFC62828;
        graphics.fill(startX, startY, startX + panelWidth, startY + 2, borderColor);
        graphics.fill(startX, startY + panelHeight - 2, startX + panelWidth, startY + panelHeight, borderColor);
        graphics.fill(startX, startY, startX + 2, startY + panelHeight, borderColor);
        graphics.fill(startX + panelWidth - 2, startY, startX + panelWidth, startY + panelHeight, borderColor);

        // Document Header Area
        graphics.fill(startX + 6, startY + 6, startX + panelWidth - 6, startY + 58, 0xFF1C2230);
        graphics.drawCenteredString(this.font, ZooTranslations.get("zootycoon.license.letter.header"), startX + panelWidth / 2, startY + 11, 0xFFFFD700);

        String zooLabel = ZooTranslations.get("zootycoon.license.letter.zoo_name", zooName);
        graphics.drawCenteredString(this.font, zooLabel, startX + panelWidth / 2, startY + 23, 0xFFFFFFFF);

        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        String tierLabel = ZooTranslations.get("zootycoon.license.letter.tier_label", tier.getDisplayName());
        graphics.drawCenteredString(this.font, tierLabel, startX + panelWidth / 2, startY + 34, 0xFF81E6D9);

        // Status Stamp Pill
        String statusText = ZooTranslations.get(passed ? "zootycoon.license.status.passed" : "zootycoon.license.status.failed");
        int stampW = this.font.width(statusText) + 16;
        int stampX = startX + (panelWidth - stampW) / 2;
        int stampY = startY + 45;
        int stampBg = passed ? 0xFF1B4332 : 0xFF4A151B;
        int stampFg = passed ? 0xFF4ADE80 : 0xFFF87171;
        graphics.fill(stampX, stampY - 1, stampX + stampW, stampY + 10, stampBg);
        graphics.drawCenteredString(this.font, statusText, startX + panelWidth / 2, stampY, stampFg);

        // Scrollable Body Area
        int bodyX = startX + 12;
        int bodyY = startY + 64;
        int bodyW = panelWidth - 24;
        int bodyH = panelHeight - 98;

        graphics.fill(bodyX - 4, bodyY - 2, bodyX + bodyW + 4, bodyY + bodyH + 2, 0xFF0E1218);

        graphics.enableScissor(bodyX - 2, bodyY, bodyX + bodyW + 2, bodyY + bodyH);
        int curY = bodyY + 4 - scrollOffset;

        // 1. Inspected Species Section
        graphics.drawString(this.font, ZooTranslations.get("zootycoon.license.letter.species_section"), bodyX, curY, 0xFFFFD700, false);
        curY += 13;

        if (inspectedSpecies.isEmpty()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                graphics.drawString(this.font, "- " + entry.getDisplayName(), bodyX + 6, curY, 0xFFD1D5DB, false);
                curY += 11;
            }
        } else {
            for (String sp : inspectedSpecies) {
                graphics.drawString(this.font, "- " + sp, bodyX + 6, curY, 0xFF86EFAC, false);
                curY += 11;
            }
        }

        curY += 8;

        // 2. Evaluation / What Needs Improvement Section
        graphics.drawString(this.font, ZooTranslations.get("zootycoon.license.letter.notes_section"), bodyX, curY, 0xFFFFD700, false);
        curY += 13;

        if (passed && improvementNotes.isEmpty()) {
            String passNote = ZooTranslations.get("zootycoon.license.letter.pass_note", tier.getDisplayName());
            List<FormattedCharSequence> lines = this.font.split(Component.literal(passNote), bodyW - 8);
            for (FormattedCharSequence line : lines) {
                graphics.drawString(this.font, line, bodyX + 4, curY, 0xFF4ADE80, false);
                curY += 11;
            }
        } else {
            for (String issue : improvementNotes) {
                List<FormattedCharSequence> lines = this.font.split(Component.literal("- " + issue), bodyW - 8);
                for (FormattedCharSequence line : lines) {
                    graphics.drawString(this.font, line, bodyX + 4, curY, 0xFFF87171, false);
                    curY += 11;
                }
                curY += 2;
            }
        }

        graphics.disableScissor();

        int contentTotalH = (curY + scrollOffset) - bodyY + 6;
        this.maxScroll = Math.max(0, contentTotalH - bodyH);
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, this.maxScroll);

        if (this.maxScroll > 0) {
            int trackX = bodyX + bodyW + 1;
            graphics.fill(trackX, bodyY, trackX + 2, bodyY + bodyH, 0xFF222834);
            int thumbH = Math.max(14, (int) ((float) bodyH * bodyH / (float) contentTotalH));
            int thumbY = bodyY + (int) ((bodyH - thumbH) * ((float) scrollOffset / (float) maxScroll));
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFF60A5FA);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.maxScroll > 0) {
            this.scrollOffset = Math.clamp(this.scrollOffset - (int) (verticalAmount * 16), 0, this.maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
