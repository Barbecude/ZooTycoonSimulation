package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.network.chat.Component;
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

    @Override
    protected boolean shouldShowName(ZooInspectorEntity entity) {
        return false;
    }

    @Override
    protected void renderNameTag(ZooInspectorEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        // Intentionally suppressed: no nametag renders above the inspector
    }
}
