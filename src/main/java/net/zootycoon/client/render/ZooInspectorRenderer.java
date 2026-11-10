package net.zootycoon.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooInspectorModel;
import net.zootycoon.entity.ZooInspectorEntity;

public class ZooInspectorRenderer extends HumanoidMobRenderer<ZooInspectorEntity, ZooInspectorModel> {
    private static final ResourceLocation CUSTOM_TEXTURE =
            ZooTycoonMod.id("textures/entity/staff/inspector.png");
    private static final ResourceLocation FALLBACK_TEXTURE =
            ZooTycoonMod.id("textures/entity/staff/security.png");

    public ZooInspectorRenderer(EntityRendererProvider.Context context) {
        super(context, new ZooInspectorModel(context.bakeLayer(ZooInspectorModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ZooInspectorEntity entity) {
        if (Minecraft.getInstance().getResourceManager().getResource(CUSTOM_TEXTURE).isPresent()) {
            return CUSTOM_TEXTURE;
        }
        return FALLBACK_TEXTURE;
    }
}
