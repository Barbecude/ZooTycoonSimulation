package net.zootycoon.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.zootycoon.client.worldgen.SeedPreviewController;
import net.zootycoon.client.worldgen.SeedPreviewWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    @Shadow
    @Final
    private HeaderAndFooterLayout layout;

    @Shadow
    @Final
    WorldCreationUiState uiState;

    @Shadow
    @Final
    private TabManager tabManager;

    @Shadow
    private TabNavigationBar tabNavigationBar;

    @Unique
    private SeedPreviewController zootycoon$previewController;

    @Unique
    private SeedPreviewWidget zootycoon$previewWidget;

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void zootycoon$initSeedPreview(CallbackInfo ci) {
        if (this.zootycoon$previewController == null) {
            this.zootycoon$previewController = new SeedPreviewController(this.uiState);
        }
        if (this.zootycoon$previewWidget != null) {
            this.zootycoon$previewWidget.close();
        }
        this.zootycoon$previewWidget = new SeedPreviewWidget(
                0, 0, 168, 184, this.zootycoon$previewController, true
        );
        this.addRenderableWidget(this.zootycoon$previewWidget);
        this.zootycoon$layoutPreviewAndTabs();
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void zootycoon$repositionSeedPreview(CallbackInfo ci) {
        this.zootycoon$layoutPreviewAndTabs();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void zootycoon$syncWorldTabSeedBox(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.zootycoon$previewController == null || this.tabManager == null) {
            return;
        }
        Tab currentTab = this.tabManager.getCurrentTab();
        if (currentTab != null && currentTab.getClass().getSimpleName().contains("WorldTab")) {
            currentTab.visitChildren(widget -> {
                if (widget instanceof EditBox editBox) {
                    this.zootycoon$previewController.attachSeedEditBox(editBox);
                    String uiSeed = this.uiState.getSeed();
                    if (!editBox.isFocused() && uiSeed != null && !uiSeed.equals(editBox.getValue())) {
                        editBox.setValue(uiSeed);
                    }
                }
            });
        }
    }

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void zootycoon$applyPreviewedSeedBeforeCreate(CallbackInfo ci) {
        if (this.zootycoon$previewController != null) {
            this.zootycoon$previewController.syncSeedBeforeWorldCreate();
        }
    }

    @Inject(method = "popScreen", at = @At("HEAD"))
    private void zootycoon$cleanupPreviewTexture(CallbackInfo ci) {
        if (this.zootycoon$previewWidget != null) {
            this.zootycoon$previewWidget.close();
            this.zootycoon$previewWidget = null;
        }
    }

    @Unique
    private void zootycoon$layoutPreviewAndTabs() {
        if (this.tabNavigationBar == null || this.zootycoon$previewWidget == null) {
            return;
        }

        int topY = this.tabNavigationBar.getRectangle().bottom();
        int contentHeight = this.height - topY - this.layout.getFooterHeight();

        // Reserve right-side dock for the interactive Seed & Biome Preview card
        int previewW = Math.min(184, Math.max(132, this.width / 3 - 8));
        int previewH = Math.min(194, Math.max(128, contentHeight - 12));

        int availableLeftWidth = this.width - previewW - 14;
        if (availableLeftWidth >= 312) {
            ScreenRectangle leftTabRect = new ScreenRectangle(0, topY, availableLeftWidth, contentHeight);
            this.tabManager.setTabArea(leftTabRect);

            int px = availableLeftWidth + 6;
            int py = topY + Math.max(4, (contentHeight - previewH) / 2);
            this.zootycoon$previewWidget.setX(px);
            this.zootycoon$previewWidget.setY(py);
            this.zootycoon$previewWidget.setWidth(previewW);
            this.zootycoon$previewWidget.setHeight(previewH);
            this.zootycoon$previewWidget.visible = true;
        } else {
            // Very small window fallback: keep compact preview anchored at bottom-right
            int compactW = Math.min(128, Math.max(96, this.width / 3));
            int compactH = Math.min(132, Math.max(96, contentHeight - 8));
            ScreenRectangle leftTabRect = new ScreenRectangle(0, topY, Math.max(220, this.width - compactW - 8), contentHeight);
            this.tabManager.setTabArea(leftTabRect);

            this.zootycoon$previewWidget.setX(this.width - compactW - 4);
            this.zootycoon$previewWidget.setY(topY + Math.max(2, (contentHeight - compactH) / 2));
            this.zootycoon$previewWidget.setWidth(compactW);
            this.zootycoon$previewWidget.setHeight(compactH);
            this.zootycoon$previewWidget.visible = true;
        }
    }
}
