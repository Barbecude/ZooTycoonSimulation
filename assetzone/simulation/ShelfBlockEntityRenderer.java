package com.example.simulation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ShelfBlockEntityRenderer implements BlockEntityRenderer<ShelfBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ShelfBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ShelfBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        renderItem(be.getDisplayFood(), poseStack, buffer, packedLight, packedOverlay, 0.35F, 0.50F, 0.35F);
        renderItem(be.getDisplayDrink(), poseStack, buffer, packedLight, packedOverlay, 0.65F, 0.50F, 0.65F);
    }

    private void renderItem(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
                            float x, float y, float z) {
        if (stack.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(0.45F, 0.45F, 0.45F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, poseStack, buffer, null, 0);
        poseStack.popPose();
    }
}
