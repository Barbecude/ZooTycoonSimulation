package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

public class StudioControlScreen extends Screen {

    private final String initialCategory;
    private String selectedCategory;
    private EditBox nameField;
    private ZooButton encBtn;
    private ZooButton essBtn;

    public StudioControlScreen(String initialCategory) {
        super(ZooTranslations.component("zootycoon.ui.studio.title"));
        this.initialCategory = (initialCategory == null || initialCategory.isEmpty()) ? "enclosures" : initialCategory;
        this.selectedCategory = this.initialCategory;
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = 320;
        int panelHeight = 220;
        int startX = (this.width - panelWidth) / 2;
        int startY = (this.height - panelHeight) / 2;

        String defaultName = ZooTranslations.get(selectedCategory.equals("enclosures") ? "zootycoon.ui.studio.default_enclosure" : "zootycoon.ui.studio.default_facility");
        this.nameField = new EditBox(this.font, startX + 20, startY + 55, panelWidth - 40, 20, ZooTranslations.component("zootycoon.ui.studio.name_label"));
        this.nameField.setValue(defaultName);
        this.addRenderableWidget(this.nameField);

        int catY = startY + 100;
        int btnW = (panelWidth - 50) / 2;

        this.encBtn = new ZooButton(startX + 20, catY, btnW, 22, ZooTranslations.component("zootycoon.ui.studio.btn_enclosures"), b -> {
            this.selectedCategory = "enclosures";
            this.encBtn.setSelected(true);
            this.essBtn.setSelected(false);
        }).asEmerald();
        this.encBtn.setSelected(selectedCategory.equals("enclosures"));
        this.addRenderableWidget(this.encBtn);

        this.essBtn = new ZooButton(startX + 20 + btnW + 10, catY, btnW, 22, ZooTranslations.component("zootycoon.ui.studio.btn_essentials"), b -> {
            this.selectedCategory = "essentials";
            this.encBtn.setSelected(false);
            this.essBtn.setSelected(true);
        }).asGold();
        this.essBtn.setSelected(selectedCategory.equals("essentials"));
        this.addRenderableWidget(this.essBtn);

        int actionY = startY + panelHeight - 45;

        this.addRenderableWidget(new ZooButton(startX + 20, actionY, (panelWidth - 50) / 2, 24, ZooTranslations.component("zootycoon.ui.studio.btn_save"), b -> {
            String name = nameField.getValue().trim();
            if (name.isEmpty()) name = defaultName;
            ClientPlayNetworking.send(new ZooNetworkPayloads.SaveCustomSchematicPayload(
                    BlockPos.ZERO, BlockPos.ZERO, name, selectedCategory, ZooTranslations.get("zootycoon.ui.studio.default_desc")
            ));
            this.onClose();
        }).asEmerald());

        this.addRenderableWidget(new ZooButton(startX + 20 + ((panelWidth - 50) / 2) + 10, actionY, (panelWidth - 50) / 2, 24, ZooTranslations.component("zootycoon.ui.studio.btn_cancel"), b -> {
            ClientPlayNetworking.send(new ZooNetworkPayloads.ExitStudioPayload());
            this.onClose();
        }).asDanger());
    }

    @Override
    protected void renderBlurredBackground(float delta) {
        // Disable 1.21 menu background blur
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderTransparentBackground(graphics);

        int panelWidth = 320;
        int panelHeight = 220;
        int startX = (this.width - panelWidth) / 2;
        int startY = (this.height - panelHeight) / 2;

        graphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xF0181B22);
        graphics.fill(startX, startY, startX + panelWidth, startY + 1, 0xFF2A2E37);
        graphics.fill(startX, startY + panelHeight - 1, startX + panelWidth, startY + panelHeight, 0xFF2A2E37);
        graphics.fill(startX, startY, startX + 1, startY + panelHeight, 0xFF2A2E37);
        graphics.fill(startX + panelWidth - 1, startY, startX + panelWidth, startY + panelHeight, 0xFF2A2E37);

        graphics.drawString(this.font, ZooTranslations.component("zootycoon.ui.studio.title"), startX + 20, startY + 15, 0xFF00E5FF, true);
        graphics.drawString(this.font, ZooTranslations.component("zootycoon.ui.studio.subtitle"), startX + 20, startY + 38, 0xFFAAAAAA, true);
        graphics.drawString(this.font, ZooTranslations.component("zootycoon.ui.studio.category_header"), startX + 20, startY + 86, 0xFFDDDDDD, true);

        super.render(graphics, mouseX, mouseY, delta);
    }
}
