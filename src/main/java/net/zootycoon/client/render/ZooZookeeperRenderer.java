package net.zootycoon.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooZookeeperModel;
import net.zootycoon.entity.ZooZookeeperEntity;

public class ZooZookeeperRenderer extends HumanoidMobRenderer<ZooZookeeperEntity, ZooZookeeperModel> {
    private static final ResourceLocation TEXTURE =
            ZooTycoonMod.id("textures/entity/staff/zookeeper.png");

    public ZooZookeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new ZooZookeeperModel(context.bakeLayer(ZooZookeeperModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ZooZookeeperEntity entity) {
        return TEXTURE;
    }
}
