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

        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        if (camera == null) return;

        poseStack.pushPose();

        // 1. Position cleanly above the visitor's head
        float headOffset = -visitor.getBbHeight() - 0.45f;
        float bob = (float) Math.sin((ageInTicks + partialTick) * 0.12f) * 0.03f;
        poseStack.translate(0.0f, headOffset - bob, 0.0f);

        // 2. Cancel entity body rotation to align with world axes
        poseStack.mulPose(Axis.YP.rotationDegrees(visitor.yBodyRot - 180.0f));

        // 3. True camera-facing billboard
        poseStack.mulPose(camera.rotation());

        // 4. Scale: compact, crisp icon
        float sc = 0.42f;
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
