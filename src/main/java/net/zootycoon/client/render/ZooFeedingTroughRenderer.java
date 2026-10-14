package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.zootycoon.block.ZooFeedingTroughBlockEntity;

public class ZooFeedingTroughRenderer implements BlockEntityRenderer<ZooFeedingTroughBlockEntity> {

    public ZooFeedingTroughRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ZooFeedingTroughBlockEntity trough, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // 1. Water rendering if trough contains water
        if (trough.hasWater()) {
            renderWater(trough, poseStack, buffer, packedLight);
            return;
        }

        // 2. Dynamic 3D Food Item rendering matching exact item inserted
        ItemStack stack = trough.getFoodStack();
        if (stack.isEmpty()) {
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack singleStack = stack.copyWithCount(1);
        int totalCount = stack.getCount();
        int layers = getRenderedLayers(totalCount);

        for (int i = 0; i < layers; i++) {
            poseStack.pushPose();

            // Elevation and slight staggered offsets per layer
            float yOffset = 0.12f + i * 0.045f;
            float rotY = -25.0f + (i * 18.0f);
            float xOffset = ((i % 2 == 0) ? -0.02f : 0.02f) + ((i % 3 - 1) * 0.02f);
            float zOffset = ((i % 2 != 0) ? -0.02f : 0.02f) + ((i % 4 - 1.5f) * 0.015f);

            poseStack.translate(0.5D + xOffset, yOffset, 0.5D + zOffset);
            poseStack.scale(0.72f, 0.72f, 0.72f);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f)); // Lay flat inside trough

            itemRenderer.renderStatic(
                    singleStack,
                    ItemDisplayContext.GROUND,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    trough.getLevel(),
                    0
            );

            poseStack.popPose();
        }
    }

    private int getRenderedLayers(int count) {
        if (count <= 1) return 1;
        if (count < 10) return 2;
        if (count < 20) return 3;
        if (count < 35) return 4;
        if (count < 50) return 5;
        return 6;
    }

    private void renderWater(ZooFeedingTroughBlockEntity trough, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float waterY = 0.12f + (trough.getWaterLevel() / 3.0f) * 0.32f;
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getParticleIcon(Fluids.WATER.defaultFluidState().createLegacyBlock());

        VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());
        var pose = poseStack.last();

        float minX = 0.0625f;
        float maxX = 0.9375f;
        float minZ = 0.0625f;
        float maxZ = 0.9375f;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        int r = 63, g = 118, b = 228, a = 200; // Water tint

        consumer.addVertex(pose, minX, waterY, minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, minX, waterY, maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, maxX, waterY, maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, maxX, waterY, minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 1, 0);
    }
}

