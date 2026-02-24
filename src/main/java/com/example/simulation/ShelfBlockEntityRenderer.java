package com.example.simulation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ShelfBlockEntityRenderer implements BlockEntityRenderer<ShelfBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ShelfBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ShelfBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        net.minecraft.core.NonNullList<ItemStack> items = be.getAllItems();
        if (items.isEmpty()) return;

        // Determine facing for item position rotation
        BlockState state = be.getBlockState();
        float rotY = 0f;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            // Shelf tangga naik dari X rendah ke X tinggi (model default facing=EAST)
            // Facing=EAST: display di X rendah (depan)
            // Facing=SOUTH: rotate 90°, display di Z rendah
            // Facing=WEST: rotate 180°, display di X tinggi
            // Facing=NORTH: rotate 270°, display di Z tinggi
            rotY = switch (facing) {
                case EAST  -> 0f;
                case SOUTH -> 90f;
                case WEST  -> 180f;
                case NORTH -> 270f;
                default    -> 0f;
            };
        }

        poseStack.pushPose();
        // Rotate item positions around block centre to match shelf facing
        poseStack.translate(0.5f, 0.0f, 0.5f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotY));
        poseStack.translate(-0.5f, 0.0f, -0.5f);

        // Item positions on each shelf step (model default facing=EAST, staircase rises toward +X)
        // Step 0 surface: Y=1.25/16≈0.08, X center ≈0.16 (from X=0 to X=5.25/16)
        // Step 1 surface: Y=6.75/16≈0.42, X center ≈0.49 (from X=5.25/16 to X=10.5/16)
        // Step 2 surface: Y=12/16=0.75, X center ≈0.75 (from X=10.5/16 to X=13.5/16)
        // Three items per step, spread across Z (0.25, 0.50, 0.75)
        float[][] positions = {
            // Step 0 (bottom)
            {0.16f, 0.13f, 0.25f}, {0.16f, 0.13f, 0.50f}, {0.16f, 0.13f, 0.75f},
            // Step 1 (middle)
            {0.49f, 0.47f, 0.25f}, {0.49f, 0.47f, 0.50f}, {0.49f, 0.47f, 0.75f},
            // Step 2 (top)
            {0.75f, 0.80f, 0.25f}, {0.75f, 0.80f, 0.50f}, {0.75f, 0.80f, 0.75f},
        };

        for (int i = 0; i < Math.min(items.size(), positions.length); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                renderItem(stack, poseStack, buffer, packedLight, packedOverlay,
                        positions[i][0], positions[i][1], positions[i][2]);
            }
        }
        poseStack.popPose();
    }

    private void renderItem(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
                            float x, float y, float z) {
        if (stack.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        // Rotate item to face outward from the shelf front face
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90F));
        poseStack.scale(0.50F, 0.50F, 0.50F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poseStack, buffer, null, 0);
        poseStack.popPose();
    }
}

