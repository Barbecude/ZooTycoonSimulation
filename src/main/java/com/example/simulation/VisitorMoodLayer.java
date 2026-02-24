package com.example.simulation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class VisitorMoodLayer extends RenderLayer<VisitorEntity, HumanoidModel<VisitorEntity>> {

        private static final ResourceLocation HAPPY = new ResourceLocation(IndoZooTycoon.MODID,
                        "textures/gui/moods/mood_happy.png");
        private static final ResourceLocation NEUTRAL = new ResourceLocation(IndoZooTycoon.MODID,
                        "textures/gui/moods/mood_neutral.png");
        private static final ResourceLocation TOILET = new ResourceLocation(IndoZooTycoon.MODID,
                        "textures/gui/moods/mood_toilet.png");
        private static final ResourceLocation AMAZED = new ResourceLocation(IndoZooTycoon.MODID,
                        "textures/gui/moods/mood_amazed.png");
        private static final ResourceLocation ADORED = new ResourceLocation(IndoZooTycoon.MODID,
                        "textures/gui/moods/mood_adored.png");
        private static final ResourceLocation HUNGRY = new ResourceLocation(IndoZooTycoon.MODID,
                        "textures/gui/moods/mood_hungry.png");
        private static final ResourceLocation THIRSTY = new ResourceLocation(IndoZooTycoon.MODID,
                        "textures/gui/moods/mood_thirsty.png");

        public VisitorMoodLayer(RenderLayerParent<VisitorEntity, HumanoidModel<VisitorEntity>> parent) {
                super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, VisitorEntity entity,
                        float limbSwing,
                        float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

                VisitorEntity.Mood mood = entity.getMood();
                if (mood == null || mood == VisitorEntity.Mood.NEUTRAL)
                        return;

                ResourceLocation tex;
                switch (mood) {
                        case HAPPY -> tex = HAPPY;
                        case TOILET -> tex = TOILET;
                        case AMAZED -> tex = AMAZED;
                        case ADORED -> tex = ADORED;
                        case HUNGRY -> tex = HUNGRY;
                        case THIRSTY -> tex = THIRSTY;
                        default -> {
                                return;
                        }
                }

                // Render a billboard above the entity's head.
                // Layer poseStack: Y is inverted due to renderer's scale(-1,-1,1).
                // Entity model root (L=0) is at the entity's TOP in world space.
                // To appear ABOVE the head: use negative Y offset (smaller = higher).
                poseStack.pushPose();

                // Place icon above entity head.
                // After LivingEntityRenderer applies scale(-1,-1,1) then translate(0,-1.501,0),
                // the model origin sits ~1.501 blocks above entity feet.
                // The child renderer further adds scale(0.6,0.6,0.6) before the translate, shrinking
                // the effective translate to 0.9 blocks.  In this space -Y moves the icon UP in world.
                // Adult head top ≈ model-origin + 0.5 = 2.0 blocks; adjust to be closer to head.
                // Child visually smaller (0.6×), head top ≈ 1.2 blocks.
                float headAbove = entity.isChildVisitor() ? -1.0f : -1.6f;
                poseStack.translate(0.0, headAbove, 0.0);

                // Counter the entity body rotation that the renderer baked in (180-yBodyRot)
                // then orient to face to camera
                net.minecraft.client.Camera camera = net.minecraft.client.Minecraft.getInstance().gameRenderer
                                .getMainCamera();
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(entity.yBodyRot - 180.0f));
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-camera.getYRot()));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(camera.getXRot()));

                // Optional: slight bob animation
                float bob = (float) Math.sin(ageInTicks * 0.15f) * 0.04f;
                poseStack.translate(0, -bob, 0);

                // Scale: 0.4 blocks wide (positive scale to render icon correctly)
                float sc = 0.4f;
                poseStack.scale(sc, sc, sc);

                renderIconStatic(poseStack, buffer, tex, packedLight, 255, 255, 255);

                poseStack.popPose();
        }

        public static void renderIconStatic(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation tex,
                        int packedLight,
                        int r,
                        int g, int b) {
                var vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));

                Matrix4f matrix = poseStack.last().pose();
                float size = 1.0f;
                float min = -size / 2;
                float max = size / 2;

                // Draw Quad with Color Tint
                vertexConsumer.vertex(matrix, min, max, 0).color(r, g, b, 255).uv(0, 1)
                                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, max, max, 0).color(r, g, b, 255).uv(1, 1)
                                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, max, min, 0).color(r, g, b, 255).uv(1, 0)
                                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, min, min, 0).color(r, g, b, 255).uv(0, 0)
                                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        }
}
