package com.example.simulation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AnimalFeederBlockEntityRenderer implements BlockEntityRenderer<AnimalFeederBlockEntity> {
    private final ItemRenderer itemRenderer;

    public AnimalFeederBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AnimalFeederBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        ItemStack stack = be.getDisplayFood();
        if (stack.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.2F, 0.5F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, buffer, null, 0);
        poseStack.popPose();
    }
}
