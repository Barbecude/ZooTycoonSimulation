package net.zootycoon.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.entity.ZooInspectorEntity;

public class ZooInspectorModel extends HumanoidModel<ZooInspectorEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ZooTycoonMod.id("inspector"), "main");

    public ZooInspectorModel(ModelPart part) {
        super(part);
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ZooInspectorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ZooInspectorEntity.InspectorPhase phase = entity.getPhase();
        if (phase == ZooInspectorEntity.InspectorPhase.WRITING_NOTES) {
            // Left arm holds paper steady in front of torso
            this.leftArm.xRot = -0.95f;
            this.leftArm.yRot = 0.42f;
            this.leftArm.zRot = -0.10f;

            // Right arm scribbles notes across the paper with pen
            float writeSin = Mth.sin(ageInTicks * 0.85f);
            float writeCos = Mth.cos(ageInTicks * 0.85f);
            this.rightArm.xRot = -1.05f + writeSin * 0.15f;
            this.rightArm.yRot = -0.35f + writeCos * 0.22f;
            this.rightArm.zRot = 0.12f;

            // Head alternates looking down at notes and glancing at animal
            this.head.xRot = 0.32f + Mth.sin(ageInTicks * 0.20f) * 0.15f;
            this.hat.copyFrom(this.head);
        } else if (phase == ZooInspectorEntity.InspectorPhase.OBSERVING_ANIMAL) {
            // Observing posture: slight lean forward, scanning head, holding notes at side
            float scan = Mth.sin(ageInTicks * 0.22f);
            this.head.yRot += scan * 0.22f;
            this.head.xRot = -0.05f + Mth.cos(ageInTicks * 0.30f) * 0.06f;
            this.hat.copyFrom(this.head);

            this.leftArm.xRot = -0.60f;
            this.leftArm.yRot = 0.25f;
            this.rightArm.xRot = -1.25f + Mth.sin(ageInTicks * 0.15f) * 0.05f;
            this.rightArm.yRot = -0.30f;
        } else if (phase == ZooInspectorEntity.InspectorPhase.WAITING_CONFIRMATION
                || phase == ZooInspectorEntity.InspectorPhase.WAITING_RESULT_CLAIM) {
            // Polite official idle pose holding report/book
            float breath = Mth.sin(ageInTicks * 0.08f) * 0.03f;
            this.leftArm.xRot = -0.55f + breath;
            this.leftArm.yRot = 0.22f;
            this.rightArm.xRot = -0.35f - breath;
            this.rightArm.yRot = -0.15f;
        }
    }
}
