package net.zootycoon.client.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.zootycoon.client.render.ClaimBoundaryRenderer;
import net.zootycoon.economy.TransactionHelper;

public class MainManagementScreen extends Screen {
    public enum ViewState {
        ROOT_GRID,
        ENCLOSURES_CATALOG,
        STAFF_RECRUITMENT,
        ANIMAL_MANAGEMENT
    }

    private ViewState currentView = ViewState.ROOT_GRID;
    private final SchematicCatalogTab enclosuresTab;
    private final StaffRecruitmentTab staffTab;
    private final AnimalManagementTab animalTab;

    public MainManagementScreen() {
        super(Component.literal("Zoo Tycoon Management"));
        this.enclosuresTab = new SchematicCatalogTab(this, "enclosures");
        this.staffTab = new StaffRecruitmentTab(this);
        this.animalTab = new AnimalManagementTab(this);
    }

    public <T extends GuiEventListener & Renderable & NarratableEntry> T addWidgetPublic(T widget) {
        return this.addRenderableWidget(widget);
    }

    public void setView(ViewState view) {
        this.currentView = view;
        this.clearWidgets();
        this.init();
    }

    public ViewState getCurrentView() {
        return currentView;
    }

    public Font getFont() {
        return this.font;
    }

    @Override
    protected void init() {
        super.init();

        if (currentView == ViewState.ROOT_GRID) {
            // ESSENTIAL-MOD MINIMALIST 4-GRID (2x2)
            int gridTotalWidth = Math.min(this.width - 40, 520);
            int cardGapX = 14;
            int cardGapY = 12;
            int cardWidth = (gridTotalWidth - cardGapX) / 2;
            int cardHeight = Math.min((this.height - 60 - cardGapY) / 2, 95);
            if (cardHeight < 72) {
                cardHeight = 72;
            }
            int totalGridHeight = cardHeight * 2 + cardGapY;
            int startX = (this.width - gridTotalWidth) / 2;
            int startY = (this.height - totalGridHeight) / 2;

            int col0X = startX;
            int col1X = startX + cardWidth + cardGapX;
            int row0Y = startY;
            int row1Y = startY + cardHeight + cardGapY;

            // 1. Beli Tanah Card -> DIRECTLY STARTS 2-POINT SELECTION AND CLOSES SCREEN IF PLAYER HAS STICK
            addRenderableWidget(new ZooGridCard(
                    col0X, row0Y, cardWidth, cardHeight,
                    "BELI TANAH",
                    card -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player != null) {
                            boolean hasStick = false;
                            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                                if (mc.player.getInventory().getItem(i).is(Items.STICK)) {
                                    hasStick = true;
                                    break;
                                }
                            }
                            if (!hasStick) {
                                mc.player.displayClientMessage(
                                        Component.literal("[Peringatan] Anda membutuhkan Stick (Tongkat Kayu) untuk membeli tanah!")
                                                .withStyle(ChatFormatting.RED),
                                        true
                                );
                                return;
                            }

                            this.onClose();
                            for (int i = 0; i < 9; i++) {
                                if (mc.player.getInventory().getItem(i).is(Items.STICK)) {
                                    mc.player.getInventory().selected = i;
                                    break;
                                }
                            }
                            ClaimBoundaryRenderer.clearClaimPreview();
                            mc.player.displayClientMessage(
                                    Component.literal("Mode Beli Tanah Aktif: Klik Kiri Blok Sudut 1 lalu Klik Kiri Blok Sudut 2!")
                                            .withStyle(ChatFormatting.AQUA),
                                    true
                            );
                        }
                    }
            ).withIcon(ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/claim_land.png"), 32, 32, 32, 32));

            // 2. Buildings Card -> OPENS BUILDINGS / ENCLOSURES CATALOG
            addRenderableWidget(new ZooGridCard(
                    col1X, row0Y, cardWidth, cardHeight,
                    "BANGUNAN",
                    card -> setView(ViewState.ENCLOSURES_CATALOG)
            ).withIcon(ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/buildings.png"), 48, 48, 48, 48));

            // 3. Rekrut Pegawai Card -> OPENS STAFF RECRUITMENT VIEW
            addRenderableWidget(new ZooGridCard(
                    col0X, row1Y, cardWidth, cardHeight,
                    "REKRUT PEGAWAI",
                    card -> setView(ViewState.STAFF_RECRUITMENT)
            ).withIcon(ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/staff.png"), 32, 32, 32, 32));

            // 4. Kelola Kebun Binatang Card -> OPENS ANIMAL MANAGEMENT VIEW
            addRenderableWidget(new ZooGridCard(
                    col1X, row1Y, cardWidth, cardHeight,
                    "KELOLA KEBUN BINATANG",
                    card -> setView(ViewState.ANIMAL_MANAGEMENT)
            ).withIcon(ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/animals.png"), 32, 32, 32, 32));
        } else {
            // Essential-Mod Sub-view Header: Sleek Pill Back Button at top-left
            addRenderableWidget(new ZooButton(20, 14, 86, 22, Component.literal("Kembali"), b -> {
                setView(ViewState.ROOT_GRID);
            }).setCustomIcon(ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/back.png"), 17, 9, 17, 9).asPrimary());

            // Initialize active subview
            switch (currentView) {
                case ENCLOSURES_CATALOG -> enclosuresTab.init(this);
                case STAFF_RECRUITMENT -> staffTab.init(this);
                case ANIMAL_MANAGEMENT -> animalTab.init();
                default -> {}
            }
        }
    }

    @Override
    protected void renderBlurredBackground(float delta) {
        // Disable 1.21 menu background blur so the GUI remains crisp and doesn't conflict with shaders
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderTransparentBackground(graphics);

        Minecraft mc = Minecraft.getInstance();

        // In ROOT_GRID, top bar is hidden.
        // In subviews, render top navigation bar & Emerald pill badge.
        if (currentView != ViewState.ROOT_GRID) {
            graphics.fill(10, 10, this.width - 10, 42, 0xF20F1219);
            graphics.fill(10, 41, this.width - 10, 42, 0xFF242A38);

            String subviewTitle = switch (currentView) {
                case ENCLOSURES_CATALOG -> "KATALOG BANGUNAN";
                case STAFF_RECRUITMENT -> "REKRUT & KELOLA PEGAWAI";
                case ANIMAL_MANAGEMENT -> "KELOLA SATWA KEBUN BINATANG";
                default -> "ZOO TYCOON";
            };
            graphics.drawCenteredString(this.font, Component.literal(subviewTitle), this.width / 2, 22, 0xFFFFFFFF);

            // Emerald balance on top right
            if (mc.player != null) {
                int emeralds = TransactionHelper.countEmeralds(mc.player);
                String balanceStr = String.valueOf(emeralds);
                int textW = font.width(balanceStr);
                int pillW = 20 + textW + 8;
                int pillX = this.width - pillW - 25;
                int pillY = 16;

                graphics.fill(pillX, pillY - 2, pillX + pillW, pillY + 14, 0xFF171B24);
                graphics.fill(pillX, pillY - 2, pillX + pillW, pillY - 1, 0xFF2B3342);
                graphics.fill(pillX, pillY + 13, pillX + pillW, pillY + 14, 0xFF2B3342);
                graphics.fill(pillX, pillY - 2, pillX + 1, pillY + 14, 0xFF2B3342);
                graphics.fill(pillX + pillW - 1, pillY - 2, pillX + pillW, pillY + 14, 0xFF2B3342);

                graphics.blit(ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/emerald.png"), pillX + 4, pillY - 1, 0, 0, 12, 12, 16, 16);
                graphics.drawString(this.font, Component.literal(balanceStr), pillX + 18, pillY + 2, 0xFF55FF55, false);
            }
        }

        // Render sub-detail view content if active
        switch (currentView) {
            case ENCLOSURES_CATALOG -> enclosuresTab.render(graphics, mouseX, mouseY, delta);
            case STAFF_RECRUITMENT -> staffTab.render(graphics, mouseX, mouseY, delta);
            case ANIMAL_MANAGEMENT -> animalTab.render(graphics, mouseX, mouseY, delta);
            default -> {}
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        switch (currentView) {
            case ENCLOSURES_CATALOG -> {
                if (enclosuresTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            case STAFF_RECRUITMENT -> {
                if (staffTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            case ANIMAL_MANAGEMENT -> {
                if (animalTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            default -> {}
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentView == ViewState.ANIMAL_MANAGEMENT) {
            if (animalTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
