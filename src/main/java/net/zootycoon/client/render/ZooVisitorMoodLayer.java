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
    private static final ResourceLocation SCARED =
            ZooTycoonMod.id("textures/gui/moods/mood_scared.png");

    public ZooVisitorMoodLayer(RenderLayerParent<ZooVisitorEntity, ZooVisitorModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       ZooVisitorEntity visitor, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        // Handled cleanly in ZooWorldIndicatorRenderer with true world-space camera billboarding
    }
}
