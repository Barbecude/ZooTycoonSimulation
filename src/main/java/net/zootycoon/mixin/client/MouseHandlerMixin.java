package net.zootycoon.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.zootycoon.client.render.ClaimBoundaryRenderer;
import net.zootycoon.client.render.PlacementPreviewRenderer;
import net.zootycoon.item.ZooItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true, require = 0)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) return;

        boolean holdingTool = mc.player.getMainHandItem().is(net.minecraft.world.item.Items.STICK)
                || mc.player.getOffhandItem().is(net.minecraft.world.item.Items.STICK);
        if (!holdingTool) return;

        boolean ctrl = InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RCONTROL);
        boolean alt = InputConstants.isKeyDown(window, InputConstants.KEY_LALT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RALT);
        boolean shift = InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);

        if (!ctrl && !alt && !shift) return;

        int step = vertical > 0 ? 1 : -1;

        // 1. If currently previewing schematic placement, rotate or shift building
        if (PlacementPreviewRenderer.getActiveSchematic() != null) {
            if (shift) {
                PlacementPreviewRenderer.rotate(step);
                ci.cancel();
                return;
            }
            if (ctrl) {
                float pitch = mc.player.getXRot();
                if (Math.abs(pitch) >= 30.0f) {
                    PlacementPreviewRenderer.shiftOffsetY(step);
                } else {
                    PlacementPreviewRenderer.shiftOffsetX(step);
                }
                ci.cancel();
                return;
            }
            if (alt) {
                PlacementPreviewRenderer.shiftOffsetZ(step);
                ci.cancel();
                return;
            }
        }

        // 2. If currently previewing land claim, shift claim boundary
        if (ClaimBoundaryRenderer.hasSelection()) {
            if (ctrl) {
                ClaimBoundaryRenderer.shiftOffsetX(step);
                ci.cancel();
                return;
            }
            if (alt) {
                ClaimBoundaryRenderer.shiftOffsetZ(step);
                ci.cancel();
                return;
            }
        }
    }
}

