package net.zootycoon.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.client.render.ClaimBoundaryRenderer;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.util.ZooTranslations;

public class MainManagementScreen extends Screen {
    public enum ViewState {
        ROOT_GRID,
        ENCLOSURES_CATALOG,
        STAFF_RECRUITMENT,
        ZOOKEEPER_MANAGE,
        BUILDER_MANAGE,
        SECURITY_MANAGE,
        CASHIER_MANAGE,
        ANIMAL_MANAGEMENT
    }

    private ViewState currentView = ViewState.ROOT_GRID;
    private final SchematicCatalogTab enclosuresTab;
    private final StaffRecruitmentTab staffTab;
    private final ZookeeperManageTab zookeeperManageTab;
    private final BuilderManageTab builderManageTab;
    private final SecurityManageTab securityManageTab;
    private final CashierManageTab cashierManageTab;
    private final AnimalManagementTab animalTab;
    private final ZooLogStatsModal logStatsModal;
    private final AnimalCollectionModal animalCollectionModal;
    private boolean isLogStatsOpen = false;
    private boolean isAnimalCollectionOpen = false;
    private ZooButton logBtn;
    private ZooButton collectionBtn;
    private ZooButton inspectorBtn;
    private static final ResourceLocation LOG_HISTORY_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/log_history.png");
    private static final ResourceLocation ANIMAL_LIST_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/animal_list_cute.png");
    private static final ResourceLocation ANIMAL_COLLECTION_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/animal_collection_cute.png");
    private static final ResourceLocation LICENSE_ICON = ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/license_cute.png");

    public MainManagementScreen() {
        super(ZooTranslations.component("zootycoon.ui.title"));
        this.enclosuresTab = new SchematicCatalogTab(this, "enclosures");
        this.staffTab = new StaffRecruitmentTab(this);
        this.zookeeperManageTab = new ZookeeperManageTab(this);
        this.builderManageTab = new BuilderManageTab(this);
        this.securityManageTab = new SecurityManageTab(this);
        this.cashierManageTab = new CashierManageTab(this);
        this.animalTab = new AnimalManagementTab(this);
        this.logStatsModal = new ZooLogStatsModal(this);
        this.animalCollectionModal = new AnimalCollectionModal(this);
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

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new net.zootycoon.network.ZooNetworkPayloads.RequestLicenseSyncPayload()
        );

        // Small icon button at top-right: Log & Statistics
        int topBtnSize = 20;
        int topBtnY = 10;
        int logBtnX = this.width - topBtnSize - 12;

        this.logBtn = addRenderableWidget(new ZooButton(logBtnX, topBtnY, topBtnSize, topBtnSize, Component.empty(), b -> {
            this.isAnimalCollectionOpen = false;
            this.isLogStatsOpen = !this.isLogStatsOpen;
            if (this.isLogStatsOpen) {
                this.logStatsModal.init();
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        new net.zootycoon.network.ZooNetworkPayloads.RequestZooLogStatsPayload()
                );
            }
        }).setCustomIcon(LOG_HISTORY_ICON, 12, 12, 16, 16).asPrimary());

        this.collectionBtn = null;
        this.inspectorBtn = null;

        if (currentView == ViewState.ROOT_GRID) {
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

            // 1. Koleksi Satwa & Lisensi Card
            addRenderableWidget(new ZooGridCard(
                    col0X, row0Y, cardWidth, cardHeight,
                    ZooTranslations.get("zootycoon.ui.card.claim"),
                    card -> {
                        this.isLogStatsOpen = false;
                        this.isAnimalCollectionOpen = true;
                        this.animalCollectionModal.init();
                        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                                new net.zootycoon.network.ZooNetworkPayloads.RequestLicenseSyncPayload()
                        );
                    }
            ).withIcon(ANIMAL_COLLECTION_ICON, 24, 24, 24, 24));

            // 2. Buildings Card
            addRenderableWidget(new ZooGridCard(
                    col1X, row0Y, cardWidth, cardHeight,
                    ZooTranslations.get("zootycoon.ui.card.buildings"),
                    card -> setView(ViewState.ENCLOSURES_CATALOG)
            ).withIcon(ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/buildings.png"), 48, 48, 48, 48));

            // 3. Rekrut Staff Card
            addRenderableWidget(new ZooGridCard(
                    col0X, row1Y, cardWidth, cardHeight,
                    ZooTranslations.get("zootycoon.ui.card.staff"),
                    card -> setView(ViewState.STAFF_RECRUITMENT)
            ).withIcon(ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/staff.png"), 32, 32, 32, 32));

            // 4. Kelola Kebun Binatang Card
            addRenderableWidget(new ZooGridCard(
                    col1X, row1Y, cardWidth, cardHeight,
                    ZooTranslations.get("zootycoon.ui.card.animals"),
                    card -> setView(ViewState.ANIMAL_MANAGEMENT)
            ).withIcon(ANIMAL_LIST_ICON, 32, 32, 32, 32));
        } else {
            addRenderableWidget(new ZooButton(20, 14, 86, 22, ZooTranslations.component("zootycoon.ui.btn.back"), b -> {
                boolean isStaffSub = currentView == ViewState.ZOOKEEPER_MANAGE
                        || currentView == ViewState.BUILDER_MANAGE
                        || currentView == ViewState.SECURITY_MANAGE
                        || currentView == ViewState.CASHIER_MANAGE;
                setView(isStaffSub ? ViewState.STAFF_RECRUITMENT : ViewState.ROOT_GRID);
            }).setCustomIcon(ResourceLocation.fromNamespaceAndPath("zootycoon", "textures/gui/icons/back.png"), 17, 9, 17, 9).asPrimary());

            switch (currentView) {
                case ENCLOSURES_CATALOG -> enclosuresTab.init(this);
                case STAFF_RECRUITMENT -> staffTab.init(this);
                case ZOOKEEPER_MANAGE -> zookeeperManageTab.init(this);
                case BUILDER_MANAGE -> builderManageTab.init(this);
                case SECURITY_MANAGE -> securityManageTab.init(this);
                case CASHIER_MANAGE -> cashierManageTab.init(this);
                case ANIMAL_MANAGEMENT -> animalTab.init();
                default -> {}
            }
        }
    }

    @Override
    protected void renderBlurredBackground(float delta) {
        // Disable 1.21 menu background blur
    }

    private void updateInspectorButtonState() {
        if (this.inspectorBtn == null) return;
        Minecraft mc = Minecraft.getInstance();
        boolean creative = mc.player != null && mc.player.isCreative();
        int activeLvl = net.zootycoon.license.ZooLicenseManager.getClientActiveTierLevel();
        var tierData = net.zootycoon.license.ZooLicenseManager.getClientTierData(activeLvl);
        boolean pendingClaim = tierData.inspectionPassed && (!tierData.licenseClaimed || !tierData.letterClaimed);
        int missing = net.zootycoon.license.ZooLicenseManager.getClientMissingCountForActiveTier();
        this.inspectorBtn.active = creative || pendingClaim || missing == 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderTransparentBackground(graphics);
        updateInspectorButtonState();

        Minecraft mc = Minecraft.getInstance();

        if (currentView != ViewState.ROOT_GRID) {
            graphics.fill(10, 10, this.width - 10, 42, 0xF20F1219);
            graphics.fill(10, 41, this.width - 10, 42, 0xFF242A38);

            String subviewKey = switch (currentView) {
                case ENCLOSURES_CATALOG -> "zootycoon.ui.subview.catalog";
                case STAFF_RECRUITMENT -> "zootycoon.ui.subview.staff";
                case ZOOKEEPER_MANAGE -> "zootycoon.ui.subview.zookeeper_manage";
                case BUILDER_MANAGE -> "zootycoon.ui.subview.builder_manage";
                case SECURITY_MANAGE -> "zootycoon.ui.subview.security_manage";
                case CASHIER_MANAGE -> "zootycoon.ui.subview.cashier_manage";
                case ANIMAL_MANAGEMENT -> "zootycoon.ui.subview.animals";
                default -> "zootycoon.ui.title";
            };
            graphics.drawCenteredString(this.font, ZooTranslations.component(subviewKey), this.width / 2, 22, 0xFFFFFFFF);

            if (mc.player != null) {
                int emeralds = TransactionHelper.countEmeralds(mc.player);
                String balanceStr = String.valueOf(emeralds);
                int textW = font.width(balanceStr);
                int pillW = 22 + textW + 10;
                int pillX = this.width - pillW - 86;
                int pillY = 16;

                graphics.fill(pillX, pillY - 2, pillX + pillW, pillY + 16, 0xFF171B24);
                graphics.fill(pillX, pillY - 2, pillX + pillW, pillY - 1, 0xFF2B3342);
                graphics.fill(pillX, pillY + 15, pillX + pillW, pillY + 16, 0xFF2B3342);
                graphics.fill(pillX, pillY - 2, pillX + 1, pillY + 16, 0xFF2B3342);
                graphics.fill(pillX + pillW - 1, pillY - 2, pillX + pillW, pillY + 16, 0xFF2B3342);

                graphics.renderItem(new ItemStack(Items.EMERALD), pillX + 3, pillY - 2);
                graphics.drawString(this.font, Component.literal(balanceStr), pillX + 22, pillY + 2, 0xFF55FF55, false);
            }
        }

        switch (currentView) {
            case ENCLOSURES_CATALOG -> enclosuresTab.render(graphics, mouseX, mouseY, delta);
            case STAFF_RECRUITMENT -> staffTab.render(graphics, mouseX, mouseY, delta);
            case ZOOKEEPER_MANAGE -> zookeeperManageTab.render(graphics, mouseX, mouseY, delta);
            case BUILDER_MANAGE -> builderManageTab.render(graphics, mouseX, mouseY, delta);
            case SECURITY_MANAGE -> securityManageTab.render(graphics, mouseX, mouseY, delta);
            case CASHIER_MANAGE -> cashierManageTab.render(graphics, mouseX, mouseY, delta);
            case ANIMAL_MANAGEMENT -> animalTab.render(graphics, mouseX, mouseY, delta);
            default -> {}
        }

        if (isLogStatsOpen) {
            logStatsModal.render(graphics, mouseX, mouseY, delta);
        } else if (isAnimalCollectionOpen) {
            animalCollectionModal.render(graphics, mouseX, mouseY, delta);
        } else {
            super.render(graphics, mouseX, mouseY, delta);

            // Overlays drawn after widgets so nothing renders on top of them
            if (currentView == ViewState.STAFF_RECRUITMENT) {
                staffTab.renderOverlay(graphics, mouseX, mouseY, delta);
            }
            renderTopButtonsTooltips(graphics, mouseX, mouseY);
        }
    }

    private void renderTopButtonsTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.collectionBtn != null && isInside(this.collectionBtn, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, ZooTranslations.component("zootycoon.license.btn.collection_tooltip"), mouseX, mouseY);
        } else if (this.inspectorBtn != null && isInside(this.inspectorBtn, mouseX, mouseY)) {
            int activeLvl = net.zootycoon.license.ZooLicenseManager.getClientActiveTierLevel();
            net.zootycoon.license.LicenseTier tier = net.zootycoon.license.LicenseTier.byLevel(activeLvl);
            int missing = net.zootycoon.license.ZooLicenseManager.getClientMissingCountForActiveTier();
            Minecraft mc = Minecraft.getInstance();
            boolean creative = mc.player != null && mc.player.isCreative();
            var tierData = net.zootycoon.license.ZooLicenseManager.getClientTierData(activeLvl);
            boolean pendingClaim = tierData.inspectionPassed && (!tierData.licenseClaimed || !tierData.letterClaimed);

            java.util.List<Component> lines = new java.util.ArrayList<>();
            lines.add(ZooTranslations.component("zootycoon.license.btn.inspector_title"));
            if (pendingClaim || missing == 0) {
                lines.add(Component.literal(ZooTranslations.get("zootycoon.license.btn.inspector_ready", tier.getDisplayName())).withColor(0xFF4ADE80));
            } else if (creative) {
                lines.add(Component.literal(ZooTranslations.get("zootycoon.license.btn.inspector_creative", tier.getDisplayName())).withColor(0xFF38BDF8));
            } else {
                lines.add(Component.literal(ZooTranslations.get("zootycoon.license.btn.inspector_missing", missing, tier.getDisplayName())).withColor(0xFFF87171));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }

    private boolean isInside(ZooButton btn, double mouseX, double mouseY) {
        return mouseX >= btn.getX() && mouseX <= btn.getX() + btn.getWidth()
                && mouseY >= btn.getY() && mouseY <= btn.getY() + btn.getHeight();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isLogStatsOpen) {
            boolean handled = logStatsModal.mouseClicked(mouseX, mouseY, button);
            if (!handled) {
                // Clicked outside modal or on close button
                isLogStatsOpen = false;
            }
            return true;
        }
        if (isAnimalCollectionOpen) {
            boolean handled = animalCollectionModal.mouseClicked(mouseX, mouseY, button);
            if (!handled) {
                isAnimalCollectionOpen = false;
            }
            return true;
        }
        if (this.inspectorBtn != null && !this.inspectorBtn.active && isInside(this.inspectorBtn, mouseX, mouseY)) {
            int activeLvl = net.zootycoon.license.ZooLicenseManager.getClientActiveTierLevel();
            net.zootycoon.license.LicenseTier tier = net.zootycoon.license.LicenseTier.byLevel(activeLvl);
            int missing = net.zootycoon.license.ZooLicenseManager.getClientMissingCountForActiveTier();
            ZooHudOverlay.showToast(
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.missing_species",
                    ZooHudOverlay.TOAST_WARNING,
                    missing,
                    tier.getDisplayName()
            );
            return true;
        }
        switch (currentView) {
            case ENCLOSURES_CATALOG -> {
                if (enclosuresTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            case STAFF_RECRUITMENT -> {
                if (staffTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            case ZOOKEEPER_MANAGE -> {
                if (zookeeperManageTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            case BUILDER_MANAGE -> {
                if (builderManageTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            case SECURITY_MANAGE -> {
                if (securityManageTab.mouseClicked(mouseX, mouseY, button)) return true;
            }
            case CASHIER_MANAGE -> {
                if (cashierManageTab.mouseClicked(mouseX, mouseY, button)) return true;
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
        if (isLogStatsOpen) {
            if (logStatsModal.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }
        if (isAnimalCollectionOpen) {
            if (animalCollectionModal.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }
        if (currentView == ViewState.ENCLOSURES_CATALOG) {
            if (enclosuresTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }
        if (currentView == ViewState.ZOOKEEPER_MANAGE) {
            if (zookeeperManageTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }
        if (currentView == ViewState.BUILDER_MANAGE) {
            if (builderManageTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }
        if (currentView == ViewState.SECURITY_MANAGE) {
            if (securityManageTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }
        if (currentView == ViewState.CASHIER_MANAGE) {
            if (cashierManageTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }
        if (currentView == ViewState.ANIMAL_MANAGEMENT) {
            if (animalTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (currentView == ViewState.ANIMAL_MANAGEMENT) {
            if (animalTab.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (currentView == ViewState.ANIMAL_MANAGEMENT) {
            if (animalTab.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isLogStatsOpen) {
            if (keyCode == 256) { // GLFW_KEY_ESCAPE
                isLogStatsOpen = false;
                return true;
            }
        }
        if (isAnimalCollectionOpen) {
            if (keyCode == 256) { // GLFW_KEY_ESCAPE
                isAnimalCollectionOpen = false;
                return true;
            }
        }
        if (currentView == ViewState.ANIMAL_MANAGEMENT) {
            if (animalTab.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
