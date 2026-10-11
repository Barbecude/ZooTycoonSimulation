package net.zootycoon.client.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.entity.ToiletSeatEntity;

public class ToiletSeatRenderer extends EntityRenderer<ToiletSeatEntity> {
    public ToiletSeatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ToiletSeatEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRender(ToiletSeatEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return false;
    }
}
