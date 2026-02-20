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

        public VisitorMoodLayer(RenderLayerParent<VisitorEntity, HumanoidModel<VisitorEntity>> parent) {
                super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, VisitorEntity entity,
                        float limbSwing,
                        float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

                VisitorEntity.Mood mood = entity.getMood();
                if (mood == null)
                        return;

                ResourceLocation tex;
                int r = 255;
                int g = 255;
                int b = 255;

                // Select Texture & Tint
                switch (mood) {
                        case HAPPY:
                                tex = HAPPY;
                                break;
                        case NEUTRAL:
                                return; // Don't render anything for neutral
                        case TOILET:
                                tex = TOILET;
                                break;
                        case AMAZED:
                                tex = AMAZED;
                                break;
                        case ADORED:
                                tex = ADORED;
                                break;
                        case HUNGRY:
                                tex = NEUTRAL; // Re-use Neutral
                                r = 255;
                                g = 100;
                                b = 100; // Red tint
                                break;
                        case THIRSTY:
                                tex = NEUTRAL; // Re-use Neutral
                                r = 100;
                                g = 100;
                                b = 255; // Blue tint
                                break;
                        default:
                                tex = NEUTRAL;
                                break;
                }

                poseStack.pushPose();

                // Attachment: Head
                this.getParentModel().getHead().translateAndRotate(poseStack);

                // Position Adjustment: Lowered slightly from -1.25
                poseStack.translate(0, -0.9, 0); 

                // Scale
                poseStack.scale(0.5F, 0.5F, 0.5F);

 


                renderIconStatic(poseStack, buffer, tex, packedLight, r, g, b);

                poseStack.popPose();
        }

        public static void renderIconStatic(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation tex, int packedLight,
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
