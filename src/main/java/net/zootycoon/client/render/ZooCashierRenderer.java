package net.zootycoon.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooVisitorModel;
import net.zootycoon.entity.ZooCashierEntity;

public class ZooCashierRenderer extends HumanoidMobRenderer<ZooCashierEntity, HumanoidModel<ZooCashierEntity>> {
    private static final ResourceLocation TEXTURE =
            ZooTycoonMod.id("textures/entity/visitor/visitor_0.png");

    public ZooCashierRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ZooVisitorModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ZooCashierEntity entity) {
        return TEXTURE;
    }
}

