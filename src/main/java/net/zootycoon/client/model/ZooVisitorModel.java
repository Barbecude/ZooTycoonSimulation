package net.zootycoon.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.entity.ZooVisitorEntity;

public class ZooVisitorModel extends HumanoidModel<ZooVisitorEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ZooTycoonMod.id("visitor"), "main");

    public ZooVisitorModel(ModelPart part) {
        super(part);
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ZooVisitorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity.isConsuming()) {
            float bob = Mth.cos(ageInTicks * 1.35F) * 0.18F;
            this.rightArm.xRot = -1.35F + bob;
            this.rightArm.yRot = -0.38F;
            this.rightArm.zRot = Mth.sin(ageInTicks * 1.35F) * 0.05F;
            this.head.xRot += Mth.sin(ageInTicks * 1.35F) * 0.07F;
            this.hat.copyFrom(this.head);
        }
    }
}
