package net.zootycoon.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooBuilderModel;
import net.zootycoon.entity.ZooBuilderEntity;

public class ZooBuilderRenderer extends HumanoidMobRenderer<ZooBuilderEntity, ZooBuilderModel> {
    private static final ResourceLocation[] SKINS = new ResourceLocation[]{
            ZooTycoonMod.id("textures/entity/builder/buildermale1_a.png"),
            ZooTycoonMod.id("textures/entity/builder/buildermale1_b.png"),
            ZooTycoonMod.id("textures/entity/builder/buildermale1_d.png"),
            ZooTycoonMod.id("textures/entity/builder/buildermale1_w.png")
    };

    public ZooBuilderRenderer(EntityRendererProvider.Context context) {
        super(context, new ZooBuilderModel(context.bakeLayer(ZooBuilderModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ZooBuilderEntity entity) {
        int idx = Math.abs(entity.getSkinVariant()) % SKINS.length;
        return SKINS[idx];
    }
}
