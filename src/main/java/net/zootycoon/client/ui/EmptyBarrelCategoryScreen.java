package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.block.EmptyBarrelBlockEntity;
import net.zootycoon.network.WarehousePayloads;
import net.zootycoon.util.ZooTranslations;

public class EmptyBarrelCategoryScreen extends Screen {

    private final BlockPos barrelPos;
    private final int panelWidth = 260;
    private final int panelHeight = 168;
    private int startX;
    private int startY;

    public EmptyBarrelCategoryScreen(BlockPos barrelPos) {
        super(ZooTranslations.component("zootycoon.warehouse.category.title"));
        this.barrelPos = barrelPos;
    }

    @Override
    protected void init() {
        super.init();
        this.startX = (this.width - panelWidth) / 2;
        this.startY = (this.height - panelHeight) / 2;

        int btnX = startX + 16;
        int btnW = panelWidth - 32;
        int btnH = 22;
        int gap = 6;
        int firstBtnY = startY + 46;

        ZooButton burgerBtn = new ZooButton(
                btnX, firstBtnY, btnW, btnH,
                ZooTranslations.component("zootycoon.warehouse.category.btn.burger"),
                b -> selectCategory(EmptyBarrelBlockEntity.CAT_BURGER)
        ).setItemIcon(new ItemStack(Items.BREAD)).asPrimary();
        addRenderableWidget(burgerBtn);

        ZooButton drinkBtn = new ZooButton(
                btnX, firstBtnY + btnH + gap, btnW, btnH,
                ZooTranslations.component("zootycoon.warehouse.category.btn.drink"),
                b -> selectCategory(EmptyBarrelBlockEntity.CAT_DRINK)
        ).setItemIcon(new ItemStack(Items.HONEY_BOTTLE)).asPrimary();
        addRenderableWidget(drinkBtn);

        ZooButton animalFoodBtn = new ZooButton(
                btnX, firstBtnY + (btnH + gap) * 2, btnW, btnH,
                ZooTranslations.component("zootycoon.warehouse.category.btn.animal_food"),
                b -> selectCategory(EmptyBarrelBlockEntity.CAT_ANIMAL_FOOD)
        ).setItemIcon(new ItemStack(Items.WHEAT)).asPrimary();
        addRenderableWidget(animalFoodBtn);

        ZooButton cancelBtn = new ZooButton(
                btnX, firstBtnY + (btnH + gap) * 3 + 4, btnW, 20,
                ZooTranslations.component("zootycoon.dialog.btn.close"),
                b -> onClose()
        ).asSecondary();
        addRenderableWidget(cancelBtn);
    }

    private void selectCategory(String category) {
        EmptyBarrelChestOverlay.prepareBarrelOpen(this.barrelPos, category);
        ClientPlayNetworking.send(new WarehousePayloads.SelectBarrelCategoryPayload(this.barrelPos, category));
    }

    @Override
    protected void renderBlurredBackground(float delta) {
        // Keep background crisp
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        // Main Slate Panel matching mod design language
        graphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xF212151D);
        graphics.renderOutline(startX, startY, panelWidth, panelHeight, 0xFF2A3140);

        // Header
        Component titleComp = ZooTranslations.component("zootycoon.warehouse.category.title");
        Component subComp = ZooTranslations.component("zootycoon.warehouse.category.subtitle");
        graphics.drawString(this.font, titleComp, startX + 16, startY + 12, 0xFFFFD700, false);
        graphics.drawString(this.font, subComp, startX + 16, startY + 24, 0xFFA0AAB8, false);

        // Divider
        graphics.fill(startX + 16, startY + 37, startX + panelWidth - 16, startY + 38, 0xFF252D3C);

        super.render(graphics, mouseX, mouseY, delta);
    }
}
