package net.zootycoon.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zootycoon.economy.TransactionHelper;

public class MainManagementScreen extends Screen {
    public enum Tab {
        CLAIM_LAND("🏞️ Claim Land"),
        ENCLOSURES("🦁 Enclosures"),
        ESSENTIALS("🏢 Essentials");

        private final String title;
        Tab(String title) { this.title = title; }
        public String getTitle() { return title; }
    }

    private Tab currentTab = Tab.CLAIM_LAND;
    private final ClaimLandTab claimLandTab;
    private final SchematicCatalogTab enclosuresTab;
    private final SchematicCatalogTab essentialsTab;

    public MainManagementScreen() {
        super(Component.translatable("zootycoon.ui.title"));
        this.claimLandTab = new ClaimLandTab(this);
        this.enclosuresTab = new SchematicCatalogTab(this, "enclosures");
        this.essentialsTab = new SchematicCatalogTab(this, "essentials");
    }

    @Override
    protected void init() {
        super.init();
        int tabWidth = 110;
        int tabHeight = 22;
        int startX = (this.width - (tabWidth * 3 + 10)) / 2;
        int tabY = 15;

        // Tab Buttons
        addRenderableWidget(Button.builder(Component.literal(Tab.CLAIM_LAND.getTitle()), b -> setTab(Tab.CLAIM_LAND))
                .bounds(startX, tabY, tabWidth, tabHeight).build());

        addRenderableWidget(Button.builder(Component.literal(Tab.ENCLOSURES.getTitle()), b -> setTab(Tab.ENCLOSURES))
                .bounds(startX + tabWidth + 5, tabY, tabWidth, tabHeight).build());

        addRenderableWidget(Button.builder(Component.literal(Tab.ESSENTIALS.getTitle()), b -> setTab(Tab.ESSENTIALS))
                .bounds(startX + (tabWidth + 5) * 2, tabY, tabWidth, tabHeight).build());

        // Initialize active tab widgets
        initCurrentTab();
    }

    public void setTab(Tab tab) {
        this.currentTab = tab;
        this.clearWidgets();
        this.init();
    }

    private void initCurrentTab() {
        switch (currentTab) {
            case CLAIM_LAND -> claimLandTab.init(this);
            case ENCLOSURES -> enclosuresTab.init(this);
            case ESSENTIALS -> essentialsTab.init(this);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        // Header background panel
        graphics.fill(10, 10, this.width - 10, 42, 0xAA1E1E1E);

        // Emerald balance on top right
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            int emeralds = TransactionHelper.countEmeralds(mc.player);
            String balanceStr = "Emeralds: " + emeralds + " ❇";
            graphics.drawString(this.font, balanceStr, this.width - font.width(balanceStr) - 25, 22, 0x55FF55, false);
        }

        // Render tab content
        switch (currentTab) {
            case CLAIM_LAND -> claimLandTab.render(graphics, mouseX, mouseY, delta);
            case ENCLOSURES -> enclosuresTab.render(graphics, mouseX, mouseY, delta);
            case ESSENTIALS -> essentialsTab.render(graphics, mouseX, mouseY, delta);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        switch (currentTab) {
            case ENCLOSURES -> {
                if (enclosuresTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            case ESSENTIALS -> {
                if (essentialsTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            default -> {}
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
