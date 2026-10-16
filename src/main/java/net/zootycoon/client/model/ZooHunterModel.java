package net.zootycoon.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.entity.ZooHunterEntity;

public class ZooHunterModel extends HumanoidModel<ZooHunterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ZooTycoonMod.id("hunter"), "main");

    public ZooHunterModel(ModelPart part) {
        super(part);
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ZooHunterEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isArcher()) {
            if (entity.isUsingItem() || entity.isAggressive()) {
                this.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            } else {
                this.rightArmPose = HumanoidModel.ArmPose.ITEM;
            }
        } else {
            this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        }
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity.isVehicle()) {
            // Raise both arms overhead when carrying a kidnapped zoo animal
            this.rightArm.xRot = -2.85F;
            this.leftArm.xRot = -2.85F;
            this.rightArm.zRot = 0.15F;
            this.leftArm.zRot = -0.15F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
        }
    }
}
