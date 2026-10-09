package com.example.simulation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ToiletSeatRenderer extends EntityRenderer<ToiletSeatEntity> {
    public ToiletSeatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ToiletSeatEntity entity) {
        return new ResourceLocation("minecraft", "textures/misc/white.png");
    }

    @Override
    public boolean shouldRender(ToiletSeatEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public void render(ToiletSeatEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
    }
}
