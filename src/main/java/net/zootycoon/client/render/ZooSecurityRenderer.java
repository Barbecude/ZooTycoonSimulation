package net.zootycoon.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooSecurityModel;
import net.zootycoon.entity.ZooSecurityEntity;

public class ZooSecurityRenderer extends HumanoidMobRenderer<ZooSecurityEntity, ZooSecurityModel> {
    private static final ResourceLocation TEXTURE =
            ZooTycoonMod.id("textures/entity/staff/security.png");

    public ZooSecurityRenderer(EntityRendererProvider.Context context) {
        super(context, new ZooSecurityModel(context.bakeLayer(ZooSecurityModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ZooSecurityEntity entity) {
        return TEXTURE;
    }
}
