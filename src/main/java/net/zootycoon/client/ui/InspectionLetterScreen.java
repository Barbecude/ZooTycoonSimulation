package net.zootycoon.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.List;

public class InspectionLetterScreen extends Screen {

    private static final ResourceLocation PARCHMENT_TEX = ZooTycoonMod.id("textures/gui/letter_parchment.png");
    private static final ResourceLocation BUILDER_NOTE_TEX = ZooTycoonMod.id("textures/gui/builder_note.png");

    private final String zooName;
    private final int tierLevel;
    private final boolean passed;
    private final List<String> inspectedSpecies;
    private final List<String> improvementNotes;

    private final int panelWidth = 260;
    private final int panelHeight = 250;
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

        int btnW = 90;
        int btnH = 18;
        int btnX = startX + (panelWidth - btnW) / 2;
        int btnY = startY + panelHeight - 24;

        this.addRenderableWidget(
                Button.builder(ZooTranslations.component("zootycoon.dialog.btn.close"), b -> this.onClose())
                        .bounds(btnX, btnY, btnW, btnH)
                        .build()
        );
    }

    @Override
    protected void renderBlurredBackground(float delta) {
        // Disabled 1.21 menu blur to keep parchment crisp and readable
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawInkCentered(GuiGraphics graphics, String text, int centerX, int y, int color) {
        int x = centerX - this.font.width(text) / 2;
        graphics.drawString(this.font, text, x, y, color, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // 1. Semi-transparent backdrop overlay without blur
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        // 2. Render authentic parchment paper
        boolean hasCustom = Minecraft.getInstance().getResourceManager().getResource(PARCHMENT_TEX).isPresent();
        if (hasCustom) {
            graphics.blit(PARCHMENT_TEX, startX, startY, 0.0F, 0.0F, panelWidth, panelHeight, panelWidth, panelHeight);
        } else {
            graphics.blit(BUILDER_NOTE_TEX, startX, startY, panelWidth, panelHeight, 16.0F, 4.0F, 128, 168, 256, 256);
        }

        // 3. Document Header in crisp high-contrast ink (no dropShadow smudging)
        graphics.fill(startX + 18, startY + 12, startX + panelWidth - 18, startY + 25, 0x227A1C1C);
        drawInkCentered(graphics, ZooTranslations.get("zootycoon.license.letter.header"), startX + panelWidth / 2, startY + 15, 0xFF6B1414);
        graphics.fill(startX + 24, startY + 26, startX + panelWidth - 24, startY + 27, 0x803A2410);

        String zooLabel = ZooTranslations.get("zootycoon.license.letter.zoo_name", zooName);
        drawInkCentered(graphics, zooLabel, startX + panelWidth / 2, startY + 29, 0xFF1F1206);

        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        String tierLabel = ZooTranslations.get("zootycoon.license.letter.tier_label", tier.getDisplayName());
        drawInkCentered(graphics, tierLabel, startX + panelWidth / 2, startY + 40, 0xFF2A1708);

        // 4. Official Double-Ruled Rubber/Wax Seal Stamp Box
        String rawStatus = ZooTranslations.get(passed ? "zootycoon.license.status.passed" : "zootycoon.license.status.failed");
        String stampText = (passed ? "★ " : "⚠ ") + rawStatus + (passed ? " ★" : " ⚠");
        int stampBorder = passed ? 0xFF1E5B26 : 0xFF8A1E1E;
        int stampBg = passed ? 0x181E5B26 : 0x188A1E1E;
        int stampW = this.font.width(stampText) + 20;
        int stampH = 15;
        int stampX = startX + (panelWidth - stampW) / 2;
        int stampY = startY + 52;

        graphics.fill(stampX, stampY, stampX + stampW, stampY + stampH, stampBg);
        graphics.renderOutline(stampX, stampY, stampW, stampH, stampBorder);
        graphics.renderOutline(stampX + 2, stampY + 2, stampW - 4, stampH - 4, stampBorder);
        drawInkCentered(graphics, stampText, startX + panelWidth / 2, stampY + 4, stampBorder);

        graphics.fill(startX + 20, startY + 71, startX + panelWidth - 20, startY + 72, 0x403A2410);

        // 5. Scrollable Content Area directly on parchment
        int bodyX = startX + 20;
        int bodyY = startY + 75;
        int bodyW = panelWidth - 40;
        int bodyH = panelHeight - 105;

        graphics.enableScissor(bodyX - 2, bodyY, bodyX + bodyW + 4, bodyY + bodyH);
        int curY = bodyY + 2 - scrollOffset;

        // Section A: Inspected Species
        graphics.drawString(this.font, ZooTranslations.get("zootycoon.license.letter.species_section"), bodyX, curY, 0xFF2A1708, false);
        int spTitleW = this.font.width(ZooTranslations.get("zootycoon.license.letter.species_section"));
        graphics.fill(bodyX + spTitleW + 6, curY + 4, bodyX + bodyW, curY + 5, 0x303A2410);
        curY += 12;

        for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
            boolean checked = inspectedSpecies.contains(entry.getDisplayName());
            graphics.drawString(this.font, (checked ? "✔ " : "- ") + entry.getDisplayName(), bodyX + 6, curY, checked ? 0xFF1E5B26 : 0xFF6B5847, false);
            curY += 11;
        }

        curY += 8;

        // Section B: Notes & Evaluation
        graphics.drawString(this.font, ZooTranslations.get("zootycoon.license.letter.notes_section"), bodyX, curY, 0xFF2A1708, false);
        int notesTitleW = this.font.width(ZooTranslations.get("zootycoon.license.letter.notes_section"));
        graphics.fill(bodyX + notesTitleW + 6, curY + 4, bodyX + bodyW, curY + 5, 0x303A2410);
        curY += 12;

        if (passed && improvementNotes.isEmpty()) {
            String passNote = ZooTranslations.get("zootycoon.license.letter.pass_note", tier.getDisplayName());
            List<FormattedCharSequence> lines = this.font.split(Component.literal(passNote), bodyW - 6);
            for (FormattedCharSequence line : lines) {
                graphics.drawString(this.font, line, bodyX + 4, curY, 0xFF1E5B26, false);
                curY += 11;
            }
        } else {
            for (String issue : improvementNotes) {
                List<FormattedCharSequence> lines = this.font.split(Component.literal("- " + issue), bodyW - 6);
                for (FormattedCharSequence line : lines) {
                    graphics.drawString(this.font, line, bodyX + 4, curY, 0xFF8A1E1E, false);
                    curY += 11;
                }
                curY += 2;
            }
        }

        graphics.disableScissor();

        // 6. Scrollbar on parchment
        int contentTotalH = (curY + scrollOffset) - bodyY + 4;
        this.maxScroll = Math.max(0, contentTotalH - bodyH);
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, this.maxScroll);

        if (this.maxScroll > 0) {
            int trackX = startX + panelWidth - 18;
            graphics.fill(trackX, bodyY, trackX + 2, bodyY + bodyH, 0x303A2410);
            int thumbH = Math.max(14, (int) ((float) bodyH * bodyH / (float) contentTotalH));
            int thumbY = bodyY + (int) ((bodyH - thumbH) * ((float) scrollOffset / (float) maxScroll));
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFF5A432D);
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
