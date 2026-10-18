package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.block.ZooGachaMachineBlock;
import net.zootycoon.block.ZooGachaMachineBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ZooGachaMachineRenderer implements BlockEntityRenderer<ZooGachaMachineBlockEntity> {

    private record PlushiePose(double x, double y, double z, float rotX, float rotY, float rotZ, float scale) {}

    private static final PlushiePose[] PLUSHIE_POSES = new PlushiePose[] {
            // 0: Bottom Center-Front
            new PlushiePose(0.50D, 0.74D, 0.42D, 5.0f, 10.0f, 0.0f, 0.36f),
            // 1: Bottom Left
            new PlushiePose(0.36D, 0.74D, 0.58D, 10.0f, -40.0f, -12.0f, 0.34f),
            // 2: Bottom Right
            new PlushiePose(0.64D, 0.74D, 0.56D, -10.0f, 45.0f, 15.0f, 0.35f),
            // 3: Bottom Back-Center
            new PlushiePose(0.50D, 0.76D, 0.70D, 15.0f, 175.0f, 5.0f, 0.34f),
            // 4: Mid-tier Left-Front
            new PlushiePose(0.42D, 0.95D, 0.48D, -12.0f, -25.0f, -20.0f, 0.33f),
            // 5: Mid-tier Right-Back
            new PlushiePose(0.58D, 0.96D, 0.62D, 18.0f, 65.0f, 15.0f, 0.33f),
            // 6: Top-tier Center
            new PlushiePose(0.50D, 1.14D, 0.55D, -8.0f, 110.0f, 8.0f, 0.32f)
    };

    public ZooGachaMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ZooGachaMachineBlockEntity machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        List<ItemStack> availablePlushies = new ArrayList<>();
        for (ItemStack slot : machine.getItems()) {
            if (!slot.isEmpty()) {
                availablePlushies.add(slot);
            }
        }

        if (availablePlushies.isEmpty()) {
            return;
        }

        int totalStock = machine.getTotalStock();
        int countToRender;
        if (totalStock <= 1) {
            countToRender = 1;
        } else if (totalStock <= 3) {
            countToRender = 2;
        } else if (totalStock <= 8) {
            countToRender = 3;
        } else if (totalStock <= 16) {
            countToRender = 4;
        } else if (totalStock <= 32) {
            countToRender = 5;
        } else if (totalStock <= 64) {
            countToRender = 6;
        } else {
            countToRender = 7;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        int lightAbove = machine.getLevel() != null
                ? LevelRenderer.getLightColor(machine.getLevel(), machine.getBlockPos().above())
                : packedLight;
        int light = Math.max(packedLight, lightAbove);

        Direction facing = Direction.NORTH;
        if (machine.getBlockState().hasProperty(ZooGachaMachineBlock.FACING)) {
            facing = machine.getBlockState().getValue(ZooGachaMachineBlock.FACING);
        }

        poseStack.pushPose();
        // Rotate around block center to align with machine facing
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - facing.toYRot()));
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        for (int i = 0; i < countToRender; i++) {
            PlushiePose pose = PLUSHIE_POSES[i];
            ItemStack stackToRender = availablePlushies.get(i % availablePlushies.size());

            poseStack.pushPose();
            poseStack.translate(pose.x(), pose.y(), pose.z());
            poseStack.scale(pose.scale(), pose.scale(), pose.scale());

            if (pose.rotY() != 0.0f) poseStack.mulPose(Axis.YP.rotationDegrees(pose.rotY()));
            if (pose.rotX() != 0.0f) poseStack.mulPose(Axis.XP.rotationDegrees(pose.rotX()));
            if (pose.rotZ() != 0.0f) poseStack.mulPose(Axis.ZP.rotationDegrees(pose.rotZ()));

            itemRenderer.renderStatic(
                    stackToRender,
                    ItemDisplayContext.FIXED,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    machine.getLevel(),
                    i
            );

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}

