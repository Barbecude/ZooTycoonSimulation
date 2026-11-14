package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.List;

public class CustomLetterScreen extends Screen {

    private static final ResourceLocation PARCHMENT_TEX = ZooTycoonMod.id("textures/gui/letter_parchment.png");
    private static final ResourceLocation BUILDER_NOTE_TEX = ZooTycoonMod.id("textures/gui/builder_note.png");

    private static class LetterBlock {
        String type; // "heading", "list", "paragraph"
        String text;
        int renderedTopY = Integer.MIN_VALUE;
        int renderedBottomY = Integer.MIN_VALUE;

        LetterBlock(String type, String text) {
            this.type = normalizeType(type);
            this.text = text != null ? text : "";
        }

        static String normalizeType(String t) {
            if ("heading".equalsIgnoreCase(t) || "list".equalsIgnoreCase(t) || "paragraph".equalsIgnoreCase(t)) {
                return t.toLowerCase(java.util.Locale.ROOT);
            }
            return "paragraph";
        }
    }

    private final InteractionHand hand;
    private final String itemTitle;
    private String headerText;
    private String subHeaderText;
    private String stampText;
    private boolean stampGreen;
    private boolean locked;
    private final List<LetterBlock> blocks = new ArrayList<>();

    private final int panelWidth = 260;
    private final int panelHeight = 250;
    private final int editorWidth = 192;
    private int startX;
    private int startY;
    private int editorX;
    private int bodyBoxY = 75;
    private int bodyBoxH = 145;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private int selectedBlockIndex = 0;
    private boolean updatingEditorFields = false;

    private EditBox headerField;
    private EditBox subHeaderField;
    private EditBox stampField;
    private Button stampColorBtn;
    private Button blockTypeBtn;
    private Button moveUpBtn;
    private Button moveDownBtn;
    private Button deleteBlockBtn;
    private MultiLineEditBox blockTextField;

    private CustomLetterScreen(
            InteractionHand hand,
            String itemTitle,
            String headerText,
            String subHeaderText,
            String stampText,
            boolean stampGreen,
            boolean locked,
            List<LetterBlock> initialBlocks
    ) {
        super(Component.literal(itemTitle != null && !itemTitle.isBlank()
                ? itemTitle
                : ZooTranslations.get("zootycoon.custom_letter.default_title")));
        this.hand = hand != null ? hand : InteractionHand.MAIN_HAND;
        this.itemTitle = itemTitle != null ? itemTitle : ZooTranslations.get("zootycoon.custom_letter.default_title");
        this.headerText = headerText != null ? headerText : this.itemTitle;
        this.subHeaderText = subHeaderText != null ? subHeaderText : "";
        this.stampText = stampText != null ? stampText : "";
        this.stampGreen = stampGreen;
        this.locked = locked;
        if (initialBlocks != null && !initialBlocks.isEmpty()) {
            this.blocks.addAll(initialBlocks);
        } else {
            this.blocks.add(new LetterBlock("heading", ZooTranslations.get("zootycoon.custom_letter.sample_heading")));
            this.blocks.add(new LetterBlock("paragraph", ZooTranslations.get("zootycoon.custom_letter.sample_paragraph")));
        }
    }

    public static void openFromItem(ItemStack stack, InteractionHand hand) {
        String title = ZooTranslations.get("zootycoon.custom_letter.default_title");
        String header = title;
        String subHeader = "";
        String stamp = "";
        boolean stampGreen = false;
        boolean locked = false;
        List<LetterBlock> loaded = new ArrayList<>();

        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            if (tag.contains("LetterTitle")) title = tag.getString("LetterTitle");
            header = tag.contains("CustomHeader") ? tag.getString("CustomHeader") : title;
            if (tag.contains("CustomSubHeader")) subHeader = tag.getString("CustomSubHeader");
            if (tag.contains("CustomStamp")) stamp = tag.getString("CustomStamp");
            stampGreen = tag.getBoolean("CustomStampGreen");
            locked = tag.getBoolean("CustomLocked");
            if (tag.contains("CustomBlocks", Tag.TAG_LIST)) {
                ListTag list = tag.getList("CustomBlocks", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag b = list.getCompound(i);
                    loaded.add(new LetterBlock(b.getString("Type"), b.getString("Text")));
                }
            }
        }
        Minecraft.getInstance().setScreen(new CustomLetterScreen(hand, title, header, subHeader, stamp, stampGreen, locked, loaded));
    }

    @Override
    protected void init() {
        super.init();
        this.startY = (this.height - panelHeight) / 2;

        if (this.locked) {
            this.startX = (this.width - panelWidth) / 2;
            int btnW = 90;
            int btnH = 18;
            this.addRenderableWidget(
                    Button.builder(ZooTranslations.component("zootycoon.dialog.btn.close"), b -> this.onClose())
                            .bounds(startX + (panelWidth - btnW) / 2, startY + panelHeight - 24, btnW, btnH)
                            .build()
            );
            return;
        }

        int totalW = panelWidth + 8 + editorWidth;
        if (this.width >= totalW + 12) {
            this.startX = (this.width - totalW) / 2;
            this.editorX = this.startX + panelWidth + 8;
        } else {
            this.startX = Math.max(4, (this.width - totalW) / 2);
            this.editorX = Math.min(this.width - editorWidth - 4, this.startX + panelWidth + 4);
        }

        int ex = editorX + 8;
        int ew = editorWidth - 16;
        int ey = startY + 20;

        headerField = new EditBox(this.font, ex, ey, ew, 16, ZooTranslations.component("zootycoon.custom_letter.field.header"));
        headerField.setMaxLength(64);
        headerField.setValue(headerText);
        headerField.setResponder(v -> headerText = v);
        this.addRenderableWidget(headerField);
        ey += 28;

        subHeaderField = new EditBox(this.font, ex, ey, ew, 16, ZooTranslations.component("zootycoon.custom_letter.field.subheader"));
        subHeaderField.setMaxLength(80);
        subHeaderField.setValue(subHeaderText);
        subHeaderField.setResponder(v -> subHeaderText = v);
        this.addRenderableWidget(subHeaderField);
        ey += 28;

        int stampFieldW = ew - 54;
        stampField = new EditBox(this.font, ex, ey, stampFieldW, 16, ZooTranslations.component("zootycoon.custom_letter.field.stamp"));
        stampField.setMaxLength(32);
        stampField.setValue(stampText);
        stampField.setResponder(v -> stampText = v);
        this.addRenderableWidget(stampField);

        stampColorBtn = Button.builder(getStampColorLabel(), b -> {
            stampGreen = !stampGreen;
            b.setMessage(getStampColorLabel());
        }).bounds(ex + stampFieldW + 4, ey - 1, 50, 18).build();
        this.addRenderableWidget(stampColorBtn);
        ey += 28;

        int addBtnW = (ew - 8) / 3;
        this.addRenderableWidget(Button.builder(ZooTranslations.component("zootycoon.custom_letter.btn.add_heading"),
                b -> addNewBlock("heading")).bounds(ex, ey, addBtnW, 16).build());
        this.addRenderableWidget(Button.builder(ZooTranslations.component("zootycoon.custom_letter.btn.add_list"),
                b -> addNewBlock("list")).bounds(ex + addBtnW + 4, ey, addBtnW, 16).build());
        this.addRenderableWidget(Button.builder(ZooTranslations.component("zootycoon.custom_letter.btn.add_paragraph"),
                b -> addNewBlock("paragraph")).bounds(ex + (addBtnW + 4) * 2, ey, addBtnW, 16).build());
        ey += 22;

        int typeBtnW = ew - 64;
        blockTypeBtn = Button.builder(Component.empty(), b -> cycleSelectedBlockType())
                .bounds(ex, ey, typeBtnW, 16).build();
        this.addRenderableWidget(blockTypeBtn);

        moveUpBtn = Button.builder(Component.literal("^"), b -> moveSelectedBlock(-1))
                .bounds(ex + typeBtnW + 3, ey, 18, 16).build();
        this.addRenderableWidget(moveUpBtn);

        moveDownBtn = Button.builder(Component.literal("v"), b -> moveSelectedBlock(1))
                .bounds(ex + typeBtnW + 23, ey, 18, 16).build();
        this.addRenderableWidget(moveDownBtn);

        deleteBlockBtn = Button.builder(Component.literal("X"), b -> deleteSelectedBlock())
                .bounds(ex + typeBtnW + 43, ey, 21, 16).build();
        this.addRenderableWidget(deleteBlockBtn);
        ey += 20;

        int textBoxH = Math.max(44, (startY + panelHeight - 28) - ey);
        blockTextField = new MultiLineEditBox(
                this.font, ex, ey, ew, textBoxH,
                ZooTranslations.component("zootycoon.custom_letter.field.content_hint"),
                ZooTranslations.component("zootycoon.custom_letter.field.content_hint")
        );
        blockTextField.setCharacterLimit(1000);
        blockTextField.setValueListener(val -> {
            if (!updatingEditorFields && selectedBlockIndex >= 0 && selectedBlockIndex < blocks.size()) {
                blocks.get(selectedBlockIndex).text = val;
            }
        });
        this.addRenderableWidget(blockTextField);

        int bottomY = startY + panelHeight - 24;
        int doneW = 84;
        int closeW = 84;
        int gap = 8;
        int rowX = editorX + (editorWidth - (doneW + closeW + gap)) / 2;
        this.addRenderableWidget(
                Button.builder(ZooTranslations.component("zootycoon.custom_letter.btn.done"), b -> finishAndLock())
                        .bounds(rowX, bottomY, doneW, 18).build()
        );
        this.addRenderableWidget(
                Button.builder(ZooTranslations.component("zootycoon.dialog.btn.close"), b -> this.onClose())
                        .bounds(rowX + doneW + gap, bottomY, closeW, 18).build()
        );

        syncSelectedBlockToEditor();
    }

    private Component getStampColorLabel() {
        return ZooTranslations.component(stampGreen
                ? "zootycoon.custom_letter.stamp_green"
                : "zootycoon.custom_letter.stamp_red");
    }

    private void addNewBlock(String type) {
        String defaultText = switch (type) {
            case "heading" -> ZooTranslations.get("zootycoon.custom_letter.sample_heading");
            case "list" -> ZooTranslations.get("zootycoon.custom_letter.sample_list");
            default -> ZooTranslations.get("zootycoon.custom_letter.sample_paragraph");
        };
        int insertAt = blocks.isEmpty() ? 0 : Math.min(blocks.size(), selectedBlockIndex + 1);
        blocks.add(insertAt, new LetterBlock(type, defaultText));
        selectedBlockIndex = insertAt;
        syncSelectedBlockToEditor();
    }

    private void cycleSelectedBlockType() {
        if (selectedBlockIndex < 0 || selectedBlockIndex >= blocks.size()) return;
        LetterBlock block = blocks.get(selectedBlockIndex);
        block.type = switch (block.type) {
            case "heading" -> "list";
            case "list" -> "paragraph";
            default -> "heading";
        };
        syncSelectedBlockToEditor();
    }

    private void moveSelectedBlock(int delta) {
        int next = selectedBlockIndex + delta;
        if (selectedBlockIndex < 0 || selectedBlockIndex >= blocks.size() || next < 0 || next >= blocks.size()) return;
        LetterBlock moved = blocks.remove(selectedBlockIndex);
        blocks.add(next, moved);
        selectedBlockIndex = next;
        syncSelectedBlockToEditor();
    }

    private void deleteSelectedBlock() {
        if (blocks.size() <= 1 || selectedBlockIndex < 0 || selectedBlockIndex >= blocks.size()) return;
        blocks.remove(selectedBlockIndex);
        selectedBlockIndex = Math.clamp(selectedBlockIndex, 0, blocks.size() - 1);
        syncSelectedBlockToEditor();
    }

    private void syncSelectedBlockToEditor() {
        if (locked || blockTextField == null) return;
        if (blocks.isEmpty()) {
            blocks.add(new LetterBlock("paragraph", ""));
        }
        selectedBlockIndex = Math.clamp(selectedBlockIndex, 0, blocks.size() - 1);
        LetterBlock active = blocks.get(selectedBlockIndex);

        updatingEditorFields = true;
        blockTextField.setValue(active.text);
        updatingEditorFields = false;

        if (blockTypeBtn != null) {
            String key = switch (active.type) {
                case "heading" -> "zootycoon.custom_letter.type.heading";
                case "list" -> "zootycoon.custom_letter.type.list";
                default -> "zootycoon.custom_letter.type.paragraph";
            };
            blockTypeBtn.setMessage(ZooTranslations.component(key));
        }
        if (moveUpBtn != null) moveUpBtn.active = selectedBlockIndex > 0;
        if (moveDownBtn != null) moveDownBtn.active = selectedBlockIndex < blocks.size() - 1;
        if (deleteBlockBtn != null) deleteBlockBtn.active = blocks.size() > 1;
    }

    private void sendSavePayload(boolean lock) {
        List<ZooNetworkPayloads.CustomLetterBlockData> dtoList = new ArrayList<>(blocks.size());
        for (LetterBlock b : blocks) {
            dtoList.add(new ZooNetworkPayloads.CustomLetterBlockData(b.type, b.text));
        }
        ClientPlayNetworking.send(new ZooNetworkPayloads.SaveCustomLetterPayload(
                hand == InteractionHand.OFF_HAND,
                headerText,
                subHeaderText,
                stampText,
                stampGreen,
                lock,
                dtoList
        ));
    }

    private void finishAndLock() {
        if (this.locked) return;
        sendSavePayload(true);
        this.locked = true;
        this.rebuildWidgets();
    }

    @Override
    public void onClose() {
        if (!this.locked) {
            sendSavePayload(false);
        }
        super.onClose();
    }

    @Override
    protected void renderBlurredBackground(float delta) {
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
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        boolean hasCustom = Minecraft.getInstance().getResourceManager().getResource(PARCHMENT_TEX).isPresent();
        if (hasCustom) {
            graphics.blit(PARCHMENT_TEX, startX, startY, 0.0F, 0.0F, panelWidth, panelHeight, panelWidth, panelHeight);
        } else {
            graphics.blit(BUILDER_NOTE_TEX, startX, startY, panelWidth, panelHeight, 16.0F, 4.0F, 128, 168, 256, 256);
        }

        // Document Header Banner
        graphics.fill(startX + 18, startY + 12, startX + panelWidth - 18, startY + 25, 0x227A1C1C);
        String shownHeader = headerText.isBlank() ? itemTitle : headerText;
        drawInkCentered(graphics, shownHeader, startX + panelWidth / 2, startY + 15, 0xFF6B1414);
        graphics.fill(startX + 24, startY + 26, startX + panelWidth - 24, startY + 27, 0x803A2410);

        int cursorHeaderY = startY + 30;
        if (!subHeaderText.isBlank()) {
            drawInkCentered(graphics, subHeaderText, startX + panelWidth / 2, cursorHeaderY, 0xFF2A1708);
            cursorHeaderY += 12;
        }

        if (!stampText.isBlank()) {
            int stampBorder = stampGreen ? 0xFF1E5B26 : 0xFF8A1E1E;
            int stampBg = stampGreen ? 0x181E5B26 : 0x188A1E1E;
            int stampW = this.font.width(stampText) + 20;
            int stampH = 15;
            int stampX = startX + (panelWidth - stampW) / 2;
            int stampY = cursorHeaderY + 1;
            graphics.fill(stampX, stampY, stampX + stampW, stampY + stampH, stampBg);
            graphics.renderOutline(stampX, stampY, stampW, stampH, stampBorder);
            graphics.renderOutline(stampX + 2, stampY + 2, stampW - 4, stampH - 4, stampBorder);
            drawInkCentered(graphics, stampText, startX + panelWidth / 2, stampY + 4, stampBorder);
            cursorHeaderY += 20;
        }

        graphics.fill(startX + 20, cursorHeaderY, startX + panelWidth - 20, cursorHeaderY + 1, 0x403A2410);

        int bodyX = startX + 20;
        int bodyY = cursorHeaderY + 5;
        int bodyW = panelWidth - 40;
        int bodyBottom = locked ? (startY + panelHeight - 30) : (startY + panelHeight - 16);
        int bodyH = Math.max(40, bodyBottom - bodyY);
        this.bodyBoxY = bodyY;
        this.bodyBoxH = bodyH;

        graphics.enableScissor(bodyX - 2, bodyY, bodyX + bodyW + 4, bodyY + bodyH);
        int curY = bodyY + 2 - scrollOffset;

        for (int i = 0; i < blocks.size(); i++) {
            LetterBlock block = blocks.get(i);
            int blockStartY = curY;

            if ("heading".equals(block.type)) {
                String headingLine = block.text.replace('\n', ' ').trim();
                if (headingLine.isEmpty()) headingLine = "...";
                List<FormattedCharSequence> wrapped = this.font.split(Component.literal(headingLine), bodyW - 6);
                for (int li = 0; li < wrapped.size(); li++) {
                    graphics.drawString(this.font, wrapped.get(li), bodyX, curY, 0xFF2A1708, false);
                    if (li == wrapped.size() - 1) {
                        int lineW = this.font.width(wrapped.get(li));
                        if (lineW + 6 < bodyW) {
                            graphics.fill(bodyX + lineW + 6, curY + 4, bodyX + bodyW, curY + 5, 0x303A2410);
                        }
                    }
                    curY += 11;
                }
                curY += 2;
            } else if ("list".equals(block.type)) {
                String[] rawItems = block.text.split("\n", -1);
                for (String rawItem : rawItems) {
                    String itemText = rawItem.trim();
                    if (itemText.isEmpty() && rawItems.length > 1) continue;
                    List<FormattedCharSequence> wrapped = this.font.split(Component.literal("- " + itemText), bodyW - 10);
                    for (FormattedCharSequence line : wrapped) {
                        graphics.drawString(this.font, line, bodyX + 6, curY, 0xFF1F1206, false);
                        curY += 11;
                    }
                }
                curY += 2;
            } else {
                String[] rawLines = block.text.split("\n", -1);
                for (String rawLine : rawLines) {
                    if (rawLine.isEmpty()) {
                        curY += 6;
                        continue;
                    }
                    List<FormattedCharSequence> wrapped = this.font.split(Component.literal(rawLine), bodyW - 6);
                    for (FormattedCharSequence line : wrapped) {
                        graphics.drawString(this.font, line, bodyX + 2, curY, 0xFF1F1206, false);
                        curY += 11;
                    }
                }
                curY += 4;
            }

            block.renderedTopY = blockStartY;
            block.renderedBottomY = curY;

            if (!locked && i == selectedBlockIndex) {
                graphics.fill(bodyX - 2, blockStartY - 1, bodyX + bodyW + 2, curY - 1, 0x186B3E14);
                graphics.renderOutline(bodyX - 2, blockStartY - 1, bodyW + 4, Math.max(12, curY - blockStartY), 0x606B3E14);
            }
        }

        graphics.disableScissor();

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

        if (!this.locked) {
            renderEditorPanel(graphics);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    private void renderEditorPanel(GuiGraphics graphics) {
        int ex = editorX;
        int ey = startY;
        graphics.fill(ex, ey, ex + editorWidth, ey + panelHeight, 0xEC141821);
        graphics.renderOutline(ex, ey, editorWidth, panelHeight, 0xFF3A4456);

        int lx = ex + 8;
        graphics.drawString(this.font, ZooTranslations.component("zootycoon.custom_letter.editor_title"), lx, ey + 7, 0xFFE6EDF3, false);
        graphics.drawString(this.font, ZooTranslations.component("zootycoon.custom_letter.label.subheader"), lx, ey + 38, 0xFF9FB1C8, false);
        graphics.drawString(this.font, ZooTranslations.component("zootycoon.custom_letter.label.stamp"), lx, ey + 66, 0xFF9FB1C8, false);
        graphics.drawString(this.font, ZooTranslations.component("zootycoon.custom_letter.label.blocks"), lx, ey + 95, 0xFF9FB1C8, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!locked && button == 0) {
            int bodyX = startX + 18;
            int bodyRight = startX + panelWidth - 18;
            if (mouseX >= bodyX && mouseX <= bodyRight && mouseY >= bodyBoxY && mouseY <= bodyBoxY + bodyBoxH) {
                for (int i = 0; i < blocks.size(); i++) {
                    LetterBlock block = blocks.get(i);
                    if (mouseY >= block.renderedTopY && mouseY <= block.renderedBottomY) {
                        selectedBlockIndex = i;
                        syncSelectedBlockToEditor();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= startX && mouseX <= startX + panelWidth && this.maxScroll > 0) {
            this.scrollOffset = Math.clamp(this.scrollOffset - (int) (verticalAmount * 16), 0, this.maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
