package net.zootycoon.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooHunterModel;
import net.zootycoon.entity.ZooHunterEntity;

public class ZooHunterRenderer extends HumanoidMobRenderer<ZooHunterEntity, ZooHunterModel> {
    private static final ResourceLocation TEXTURE =
            ZooTycoonMod.id("textures/entity/hunter/hunter.png");

    public ZooHunterRenderer(EntityRendererProvider.Context context) {
        super(context, new ZooHunterModel(context.bakeLayer(ZooHunterModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ZooHunterEntity entity) {
        return TEXTURE;
    }
}
