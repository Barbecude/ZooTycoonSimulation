package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooVisitorModel;
import net.zootycoon.entity.ZooVisitorEntity;

public class ZooVisitorRenderer extends HumanoidMobRenderer<ZooVisitorEntity, ZooVisitorModel> {

    public ZooVisitorRenderer(EntityRendererProvider.Context context) {
        super(context, new ZooVisitorModel(context.bakeLayer(ZooVisitorModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new ZooVisitorMoodLayer(this));
    }

    @Override
    protected void scale(ZooVisitorEntity entity, PoseStack poseStack, float partialTickTime) {
        super.scale(entity, poseStack, partialTickTime);
        if (entity.isChildVisitor()) {
            poseStack.scale(0.6F, 0.6F, 0.6F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ZooVisitorEntity entity) {
        return ZooTycoonMod.id("textures/entity/visitor/visitor_" + entity.getVariant() + ".png");
    }
}
