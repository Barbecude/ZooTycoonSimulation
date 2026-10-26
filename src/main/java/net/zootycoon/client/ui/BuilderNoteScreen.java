package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.List;

public class BuilderNoteScreen extends Screen {

    private static final ResourceLocation NOTE_TEXTURE = ZooTycoonMod.id("textures/gui/builder_note.png");

    // 10-Step progressive color gradient from Red to Green for material sufficiency
    private static final int[] RATIO_COLORS = {
            0xFFB21818, // 0%   Pure Crimson Red
            0xFFC6281E, // 10%  Rust Red
            0xFFD24114, // 20%  Red-Orange
            0xFFCC5500, // 30%  Deep Orange
            0xFFB96900, // 40%  Warm Amber
            0xFF9B7300, // 50%  Golden Ochre
            0xFF737D0A, // 60%  Olive Chartreuse
            0xFF4B821E, // 70%  Olive Green
            0xFF287D2D, // 80%  Medium Forest Green
            0xFF166E23, // 90%  Deep Leaf Green
            0xFF0F5F1E  // 100%+ Full Emerald Green
    };

    public static int getRatioColor(int inInv, int needed) {
        if (needed <= 0 || inInv >= needed) {
            return RATIO_COLORS[10];
        }
        if (inInv <= 0) {
            return RATIO_COLORS[0];
        }
        float frac = (float) inInv / (float) needed;
        int idx = Math.clamp((int) (frac * 10.0f), 1, 9);
        return RATIO_COLORS[idx];
    }

    private final ItemStack noteStack;
    private final InteractionHand hand;

    private String schematicName = "Proyek Konstruksi";
    private String builderName = "Kuli Bangunan";
    private final List<NoteEntry> entries = new ArrayList<>();

    private final int panelWidth = 224;
    private final int panelHeight = 252;
    private int startX;
    private int startY;

    private float scrollOffset = 0.0f;
    private int maxScroll = 0;

    public interface NoteDisplayRow {
        int getHeight();
    }

    public static class SectionHeaderRow implements NoteDisplayRow {
        public final String title;
        public SectionHeaderRow(String title) {
            this.title = title;
        }
        @Override
        public int getHeight() {
            return 16;
        }
    }

    public static class ItemRow implements NoteDisplayRow {
        public final NoteEntry entry;
        public final int originalIndex;
        public ItemRow(NoteEntry entry, int originalIndex) {
            this.entry = entry;
            this.originalIndex = originalIndex;
        }
        @Override
        public int getHeight() {
            return 22;
        }
    }

    public static class NoteEntry {
        public final Item item;
        public final String itemId;
        public final int needed;
        public boolean checked;

        public NoteEntry(Item item, String itemId, int needed, boolean checked) {
            this.item = item;
            this.itemId = itemId;
            this.needed = needed;
            this.checked = checked;
        }
    }

    public static void open(ItemStack stack, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new BuilderNoteScreen(stack, hand));
    }

    private final List<NoteDisplayRow> displayRows = new ArrayList<>();

    public BuilderNoteScreen(ItemStack noteStack, InteractionHand hand) {
        super(Component.translatable("zootycoon.note.title"));
        this.noteStack = noteStack;
        this.hand = hand;
        loadFromStack();
    }

    private void loadFromStack() {
        entries.clear();
        displayRows.clear();
        CustomData cd = noteStack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            if (tag.contains("SchematicName")) {
                this.schematicName = tag.getString("SchematicName");
            }
            if (tag.contains("BuilderName")) {
                this.builderName = tag.getString("BuilderName");
            }
            if (tag.contains("Materials", 9)) {
                ListTag list = tag.getList("Materials", 10);
                String lastSection = null;
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag itemTag = list.getCompound(i);
                    String idStr = itemTag.getString("id");
                    int needed = itemTag.getInt("needed");
                    boolean checked = itemTag.getBoolean("checked");
                    String section = itemTag.contains("section") ? itemTag.getString("section") : "";

                    ResourceLocation key = ResourceLocation.tryParse(idStr);
                    Item item = (key != null) ? BuiltInRegistries.ITEM.get(key) : Items.AIR;
                    if (item != null && item != Items.AIR) {
                        if (!section.isEmpty() && !section.equals(lastSection)) {
                            displayRows.add(new SectionHeaderRow(section));
                            lastSection = section;
                        }
                        NoteEntry entry = new NoteEntry(item, idStr, needed, checked);
                        entries.add(entry);
                        displayRows.add(new ItemRow(entry, i));
                    }
                }
            }
        }
    }

    private void saveAndSync(int entryIndex, boolean checked) {
        CustomData cd = noteStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = cd.copyTag();
        if (tag.contains("Materials", 9)) {
            ListTag list = tag.getList("Materials", 10);
            if (entryIndex >= 0 && entryIndex < list.size()) {
                CompoundTag itemTag = list.getCompound(entryIndex);
                itemTag.putBoolean("checked", checked);
                noteStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
        ClientPlayNetworking.send(new ZooNetworkPayloads.UpdateNoteChecklistPayload(hand == InteractionHand.MAIN_HAND, entryIndex, checked));
    }

    @Override
    protected void init() {
        super.init();
        this.startX = (this.width - panelWidth) / 2;
        this.startY = (this.height - panelHeight) / 2;

        int btnW = 70;
        int btnH = 18;
        int btnX = startX + (panelWidth - btnW) / 2;
        int btnY = startY + panelHeight - 24;

        Button closeBtn = Button.builder(Component.translatable("zootycoon.note.close"), b -> onClose())
                .bounds(btnX, btnY, btnW, btnH)
                .build();
        addRenderableWidget(closeBtn);
    }

    private int countPlayerItem(Item item) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return 0;
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (st.is(item)) {
                total += st.getCount();
            }
        }
        return total;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0) {
            scrollOffset = (float) Math.clamp(scrollOffset - (float) verticalAmount * 24.0f, 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private int computeTotalContentHeight() {
        int total = 0;
        for (NoteDisplayRow row : displayRows) {
            total += row.getHeight();
        }
        return total;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int listY = startY + 38;
            int listH = panelHeight - 66;

            if (maxScroll > 0 && mouseX >= startX + panelWidth - 22 && mouseX <= startX + panelWidth - 14 && mouseY >= listY && mouseY <= listY + listH) {
                float rel = (float) (mouseY - listY) / (float) listH;
                scrollOffset = Math.clamp(rel * maxScroll, 0, maxScroll);
                return true;
            }

            if (mouseX >= startX + 16 && mouseX <= startX + panelWidth - 18 && mouseY >= listY && mouseY <= listY + listH) {
                int curRowY = listY - (int) scrollOffset;
                for (NoteDisplayRow dRow : displayRows) {
                    int rowH = dRow.getHeight();
                    if (mouseY >= curRowY && mouseY <= curRowY + rowH) {
                        if (dRow instanceof ItemRow itemRow) {
                            itemRow.entry.checked = !itemRow.entry.checked;
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                            saveAndSync(itemRow.originalIndex, itemRow.entry.checked);
                            return true;
                        }
                    }
                    curRowY += rowH;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && maxScroll > 0) {
            int listY = startY + 38;
            int listH = panelHeight - 66;
            if (mouseY >= listY && mouseY <= listY + listH) {
                float rel = (float) (mouseY - listY) / (float) listH;
                scrollOffset = Math.clamp(rel * maxScroll, 0, maxScroll);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderBlurredBackground(float delta) {
        // Disabled 1.21 menu blur to keep notes screen crisp and clear
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Darkened overlay backdrop without blur
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        // 2. Render parchment texture from builder_note.png (scaled cleanly to panel dimensions)
        graphics.blit(NOTE_TEXTURE, startX, startY, panelWidth, panelHeight, 16.0f, 4.0f, 128, 168, 256, 256);

        // 3. Header title & subtitle in natural brown ink
        Component titleComp = Component.translatable("zootycoon.note.title");
        int titleW = font.width(titleComp);
        graphics.drawString(this.font, titleComp, startX + (panelWidth - titleW) / 2, startY + 12, 0xFF2A1708, false);

        String subStr = schematicName != null && !schematicName.isEmpty() ? schematicName : "Proyek Konstruksi";
        int subW = font.width(subStr);
        graphics.drawString(this.font, subStr, startX + (panelWidth - subW) / 2, startY + 23, 0xFF5C4028, false);

        // Subtle decorative divider
        graphics.fill(startX + 22, startY + 34, startX + panelWidth - 22, startY + 35, 0x403A2410);

        // 4. Checklist content area
        int listY = startY + 38;
        int listH = panelHeight - 66;

        int totalContentH = computeTotalContentHeight();
        maxScroll = Math.max(0, totalContentH - listH);
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

        ItemStack hoveredItem = null;

        graphics.enableScissor(startX + 16, listY, startX + panelWidth - 18, listY + listH);

        if (displayRows.isEmpty()) {
            String emptyText = "Tidak ada daftar kebutuhan bahan.";
            int emptyW = font.width(emptyText);
            graphics.drawString(this.font, emptyText, startX + (panelWidth - emptyW) / 2, listY + 30, 0xFF7A6B5B, false);
        }

        int curRowY = listY - (int) scrollOffset;
        for (int r = 0; r < displayRows.size(); r++) {
            NoteDisplayRow dRow = displayRows.get(r);
            int rowH = dRow.getHeight();

            if (curRowY + rowH >= listY && curRowY <= listY + listH) {
                if (dRow instanceof SectionHeaderRow header) {
                    int headerY = curRowY + 3;
                    graphics.drawString(this.font, header.title, startX + 20, headerY, 0xFF5C381E, false);
                    int textW = this.font.width(header.title);
                    int lineX = startX + 22 + textW + 4;
                    int lineEnd = startX + panelWidth - 22;
                    if (lineEnd > lineX) {
                        graphics.fill(lineX, headerY + 4, lineEnd, headerY + 5, 0x403A2410);
                    }
                } else if (dRow instanceof ItemRow itemRow) {
                    NoteEntry entry = itemRow.entry;
                    boolean isHovered = mouseX >= startX + 16 && mouseX <= startX + panelWidth - 20 && mouseY >= curRowY && mouseY <= curRowY + rowH;
                    if (isHovered) {
                        graphics.fill(startX + 18, curRowY, startX + panelWidth - 22, curRowY + rowH - 1, 0x14000000);
                    }

                    // A. Checkbox box
                    int boxX = startX + 20;
                    int boxY = curRowY + 5;
                    int boxSize = 12;

                    graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xFF5A432D);
                    graphics.fill(boxX + 1, boxY + 1, boxX + boxSize - 1, boxY + boxSize - 1, entry.checked ? 0xFF2ECC71 : 0xFFF5EEDB);

                    if (entry.checked) {
                        graphics.fill(boxX + 3, boxY + 5, boxX + 5, boxY + 7, 0xFFFFFFFF);
                        graphics.fill(boxX + 4, boxY + 6, boxX + 6, boxY + 8, 0xFFFFFFFF);
                        graphics.fill(boxX + 5, boxY + 7, boxX + 7, boxY + 9, 0xFFFFFFFF);
                        graphics.fill(boxX + 6, boxY + 5, boxX + 8, boxY + 7, 0xFFFFFFFF);
                        graphics.fill(boxX + 7, boxY + 3, boxX + 9, boxY + 5, 0xFFFFFFFF);
                    }

                    // B. 3D Item / Block icon
                    ItemStack iconStack = new ItemStack(entry.item);
                    int iconX = startX + 36;
                    int iconY = curRowY + 3;

                    graphics.renderFakeItem(iconStack, iconX, iconY);
                    graphics.renderItemDecorations(this.font, iconStack, iconX, iconY, "");

                    if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                        hoveredItem = iconStack;
                    }

                    // C. Item Name & Ratio (e.g. Oak Planks — 19/8)
                    int textX = startX + 56;
                    int textY = curRowY + 7;
                    int maxW = panelWidth - 56 - 22; // 146 px available

                    int inInv = countPlayerItem(entry.item);
                    int invColor = getRatioColor(inInv, entry.needed);
                    int titleColor = entry.checked ? 0xFF8C7B6B : 0xFF2B1909;

                    String countStr = inInv + "/" + entry.needed;
                    String dash = " — ";

                    int countW = this.font.width(countStr);
                    int dashW = this.font.width(dash);
                    int maxNameW = maxW - countW - dashW;

                    String displayName = entry.item.getDescription().getString();
                    if (this.font.width(displayName) > maxNameW) {
                        displayName = this.font.plainSubstrByWidth(displayName, maxNameW - this.font.width("...")) + "...";
                    }

                    graphics.drawString(this.font, displayName + dash, textX, textY, titleColor, false);
                    int countX = textX + this.font.width(displayName + dash);
                    graphics.drawString(this.font, countStr, countX, textY, invColor, false);
                }
            }
            curRowY += rowH;
        }

        graphics.disableScissor();

        // 5. Scrollbar indicator if material list overflows
        if (maxScroll > 0) {
            int scrollTrackX = startX + panelWidth - 20;
            int scrollTrackY = listY;
            int scrollTrackH = listH;

            graphics.fill(scrollTrackX, scrollTrackY, scrollTrackX + 2, scrollTrackY + scrollTrackH, 0x303A2410);
            int thumbH = Math.max(16, (int) ((float) listH / totalContentH * scrollTrackH));
            int thumbY = scrollTrackY + (int) ((scrollOffset / maxScroll) * (scrollTrackH - thumbH));
            graphics.fill(scrollTrackX, thumbY, scrollTrackX + 2, thumbY + thumbH, 0xFF5A432D);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        // 6. Tooltip when hovering over block item icon
        if (hoveredItem != null) {
            graphics.renderTooltip(this.font, hoveredItem, mouseX, mouseY);
        }
    }
}
