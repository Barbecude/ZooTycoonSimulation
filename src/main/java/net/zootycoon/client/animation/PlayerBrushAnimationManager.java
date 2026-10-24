package net.zootycoon.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class PlayerBrushAnimationManager {
    private static int brushingTicks = 0;
    private static final int ANIM_DURATION = 15;

    public static void startBrushing() {
        brushingTicks = ANIM_DURATION;
    }

    public static boolean isBrushing() {
        return brushingTicks > 0;
    }

    public static void clientTick() {
        if (brushingTicks > 0) {
            brushingTicks--;
        }
    }

    public static void applyBrushTransform(PoseStack poseStack, float partialTicks, HumanoidArm humanoidArm) {
        float f = (float) (brushingTicks % 10);
        float g = f - partialTicks + 1.0F;
        float h = 1.0F - g / 10.0F;
        float p = -15.0F + 75.0F * Mth.cos(h * 2.0F * (float) Math.PI);

        if (humanoidArm != HumanoidArm.RIGHT) {
            poseStack.translate(0.1, 0.83, 0.35);
            poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(p));
            poseStack.translate(-0.3, 0.22, 0.35);
        } else {
            poseStack.translate(-0.25, 0.22, 0.35);
            poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(0.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(p));
        }
    }
}
