package net.zootycoon.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.client.animation.PlayerBrushAnimationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmAttackTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void zootycoon$applyFirstPersonBrush(
            AbstractClientPlayer player,
            float partialTicks,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack itemStack,
            float equipProgress,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            CallbackInfo ci
    ) {
        if (itemStack.is(Items.BRUSH) && PlayerBrushAnimationManager.isBrushing()) {
            HumanoidArm arm = (hand == InteractionHand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
            PlayerBrushAnimationManager.applyBrushTransform(poseStack, partialTicks, arm);
        }
    }
}
