package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooZookeeperModel;
import net.zootycoon.entity.ZooZookeeperEntity;

public class ZooZookeeperRenderer extends HumanoidMobRenderer<ZooZookeeperEntity, ZooZookeeperModel> {
    private static final ResourceLocation TEXTURE_FEMALE =
            ZooTycoonMod.id("textures/entity/staff/zookeeper.png");
    private static final ResourceLocation TEXTURE_MALE =
            ZooTycoonMod.id("textures/entity/staff/zookeeper_men.png");

    public ZooZookeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new ZooZookeeperModel(context.bakeLayer(ZooZookeeperModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ZooZookeeperEntity entity) {
        return entity.isMale() ? TEXTURE_MALE : TEXTURE_FEMALE;
    }

    @Override
    protected boolean shouldShowName(ZooZookeeperEntity entity) {
        if (isSpectatorOrReplayCamera()) {
            return false;
        }
        return super.shouldShowName(entity);
    }

    @Override
    protected void renderNameTag(ZooZookeeperEntity entity, Component displayName, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int packedLight, float partialTick) {
        if (isSpectatorOrReplayCamera()) {
            return;
        }
        super.renderNameTag(entity, displayName, poseStack, bufferSource, packedLight, partialTick);
    }

    private static boolean isSpectatorOrReplayCamera() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return true;
        }
        if (mc.player.isSpectator()) {
            return true;
        }
        if (mc.getCameraEntity() != null && mc.getCameraEntity() != mc.player) {
            return true;
        }
        return mc.player.getClass().getName().contains("replaymod");
    }
}
