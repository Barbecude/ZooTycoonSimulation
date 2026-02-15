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

                ResourceLocation tex = switch (mood) {
                        case HAPPY -> HAPPY;
                        case NEUTRAL -> NEUTRAL;
                        case TOILET -> TOILET;
                        case AMAZED -> AMAZED;
                        case ADORED -> ADORED;
                };

                poseStack.pushPose();
                // Position above head. Adjust based on scale/baby status.
                // Standard height is usually around 2.0 but depends on model.
                // We can attach to the head bone or just float above.
                // Floating above is easier.
                // Eye height + 0.5?

                // Translate to head position
                // If we attach to head, it follows head movement.
                this.getParentModel().getHead().translateAndRotate(poseStack);
                poseStack.translate(0, -0.6, 0); // Move UP (negative Y is up in model space?)
                // Actually, model y goes down. -0.6 is UP.
                // Scale it
                poseStack.scale(0.4F, 0.4F, 0.4F);

                // Billboard effect (Rotate to face camera)
                // Since we are attached to head, we are already rotated with head.
                // Taking camera rotation into account is tricky inside a layer attached to a
                // bone.
                // It's better to just generic rendering.
                // Let's try simple quad rendering.

                // Rotate 180 Y to face front?
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180)); // Flip vertical?

                // Render Quad
                renderIcon(poseStack, buffer, tex, packedLight);

                poseStack.popPose();
        }

        private void renderIcon(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation tex, int packedLight) {
                var vertexConsumer = buffer.getBuffer(RenderType.text(tex)); // Text is always bright? Or entityCutout?
                // Use entityCutoutNoCull for transparency
                vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));

                Matrix4f matrix = poseStack.last().pose();

                // Draw centered quad
                // -0.5 to 0.5
                float size = 1.0f;
                float min = -size / 2;
                float max = size / 2;

                // UV 0,0 to 1,1

                vertexConsumer.vertex(matrix, min, max, 0).color(255, 255, 255, 255).uv(0, 1)
                                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, max, max, 0).color(255, 255, 255, 255).uv(1, 1)
                                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, max, min, 0).color(255, 255, 255, 255).uv(1, 0)
                                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, min, min, 0).color(255, 255, 255, 255).uv(0, 0)
                                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        }
}
