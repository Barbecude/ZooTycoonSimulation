package net.zootycoon.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
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
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bipedBody = partdefinition.getChild("body");

        // SlimColonies Toolbag with straps, ruler, and hammer (ported from ZooBuilderModel)
        PartDefinition toolbag = bipedBody.addOrReplaceChild("toolbag", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        toolbag.addOrReplaceChild("strapback_r1", CubeListBuilder.create().texOffs(84, 46).addBox(-0.5F, -14.3F, 0.0F, 1.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5665F, 9.9968F, 4.2551F, 0.0913F, 0.0015F, 0.7592F));
        toolbag.addOrReplaceChild("strapfrontb_r1", CubeListBuilder.create().texOffs(85, 46).addBox(-0.5F, -1.8F, 0.3F, 1.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4558F, -0.5558F, 1.1519F, -1.5708F, 0.0F, 0.7854F));
        toolbag.addOrReplaceChild("strapfrontb_r2", CubeListBuilder.create().texOffs(85, 51).addBox(-0.5F, -5.6F, 0.0F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7901F, 9.9854F, -4.2831F, -0.1448F, 0.0227F, 0.743F));
        toolbag.addOrReplaceChild("strapfronta_r1", CubeListBuilder.create().texOffs(88, 46).addBox(-0.5F, -4.5F, 0.0F, 1.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1004F, 2.7895F, -2.7598F, -0.1571F, 0.0F, 0.7854F));
        toolbag.addOrReplaceChild("bag_r1", CubeListBuilder.create().texOffs(68, 32).addBox(-1.9F, -3.0F, -4.0F, 3.0F, 6.0F, 8.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-5.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition ruler = toolbag.addOrReplaceChild("ruler", CubeListBuilder.create(), PartPose.offset(-5.5F, 10.95F, -1.5F));
        ruler.addOrReplaceChild("ruler_r1", CubeListBuilder.create().texOffs(78, 54).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 8.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.5F, -2.0F, 3.0F, -0.2182F, 0.0F, 0.0F));

        PartDefinition ruler2 = toolbag.addOrReplaceChild("ruler2", CubeListBuilder.create(), PartPose.offset(-5.5F, 10.95F, -1.5F));
        ruler2.addOrReplaceChild("ruler2_r1", CubeListBuilder.create().texOffs(72, 54).addBox(-1.1F, -4.0F, -0.7F, 1.0F, 8.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.5F, -2.0F, 3.0F, -0.6531F, -0.0806F, -0.0335F));

        toolbag.addOrReplaceChild("hammer", CubeListBuilder.create().texOffs(80, 46).addBox(-0.5F, -4.95F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(74, 49).addBox(-0.5F, -4.55F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-5.5F, 10.95F, -1.5F, 0.2618F, 0.0F, 0.1309F));

        return LayerDefinition.create(meshdefinition, 128, 64);
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
