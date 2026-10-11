package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooVisitorModel;
import net.zootycoon.entity.ZooVisitorEntity;

public class ZooVisitorMoodLayer extends RenderLayer<ZooVisitorEntity, ZooVisitorModel> {

    private static final ResourceLocation HAPPY =
            ZooTycoonMod.id("textures/gui/moods/mood_happy.png");
    private static final ResourceLocation NEUTRAL =
            ZooTycoonMod.id("textures/gui/moods/mood_neutral.png");
    private static final ResourceLocation TOILET =
            ZooTycoonMod.id("textures/gui/moods/mood_toilet.png");
    private static final ResourceLocation AMAZED =
            ZooTycoonMod.id("textures/gui/moods/mood_amazed.png");
    private static final ResourceLocation ADORED =
            ZooTycoonMod.id("textures/gui/moods/mood_adored.png");
    private static final ResourceLocation HUNGRY =
            ZooTycoonMod.id("textures/gui/moods/mood_hungry.png");
    private static final ResourceLocation THIRSTY =
            ZooTycoonMod.id("textures/gui/moods/mood_thirsty.png");

    public ZooVisitorMoodLayer(RenderLayerParent<ZooVisitorEntity, ZooVisitorModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       ZooVisitorEntity visitor, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ZooVisitorEntity.VisitorMood mood = visitor.getMood();
        if (mood == null || mood == ZooVisitorEntity.VisitorMood.NEUTRAL) {
            return;
        }

        ResourceLocation tex = switch (mood) {
            case HAPPY -> HAPPY;
            case TOILET -> TOILET;
            case AMAZED -> AMAZED;
            case ADORED -> ADORED;
            case HUNGRY -> HUNGRY;
            case THIRSTY -> THIRSTY;
            default -> null;
        };

        if (tex == null) {
            return;
        }

        poseStack.pushPose();

        // Place icon above entity head (adjusting for adult vs child)
        float headAbove = visitor.isChildVisitor() ? -0.5f : -1.1f;
        poseStack.translate(0.0f, headAbove, 0.0f);

        // Billboard: orient directly to face the camera
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        if (camera != null) {
            poseStack.mulPose(Axis.YP.rotationDegrees(visitor.yBodyRot - 180.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        }

        // Slight bobbing animation
        float bob = (float) Math.sin((ageInTicks + partialTick) * 0.15f) * 0.04f;
        poseStack.translate(0.0f, -bob, 0.0f);

        // Scale: 0.4 blocks wide
        float sc = 0.4f;
        poseStack.scale(sc, sc, sc);

        float size = 1.0f;
        float min = -size / 2.0f;
        float max = size / 2.0f;

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(tex));
        var pose = poseStack.last();

        consumer.addVertex(pose, min, max, 0.0f)
                .setColor(255, 255, 255, 255)
                .setUv(0.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        consumer.addVertex(pose, max, max, 0.0f)
                .setColor(255, 255, 255, 255)
                .setUv(1.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        consumer.addVertex(pose, max, min, 0.0f)
                .setColor(255, 255, 255, 255)
                .setUv(1.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        consumer.addVertex(pose, min, min, 0.0f)
                .setColor(255, 255, 255, 255)
                .setUv(0.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        poseStack.popPose();
    }
}
