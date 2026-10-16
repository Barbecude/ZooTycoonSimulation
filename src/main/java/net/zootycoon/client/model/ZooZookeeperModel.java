package net.zootycoon.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.entity.ZooZookeeperEntity;

public class ZooZookeeperModel extends HumanoidModel<ZooZookeeperEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ZooTycoonMod.id("zookeeper"), "main");

    public ZooZookeeperModel(ModelPart part) {
        super(part);
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ZooZookeeperEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (entity.isSweeping()) {
            // Sweeping animation: both arms sweep forward and across the floor
            float sweepCycle = Mth.sin(ageInTicks * 0.45f);
            float sweepCos = Mth.cos(ageInTicks * 0.45f);

            // Right arm holding broom sweeping side to side and angled down
            this.rightArm.xRot = -0.75f + sweepCycle * 0.25f;
            this.rightArm.yRot = -0.35f + sweepCos * 0.45f;
            this.rightArm.zRot = 0.15f;

            // Left arm assisting holding broom handle
            this.leftArm.xRot = -0.65f + sweepCycle * 0.20f;
            this.leftArm.yRot = 0.40f + sweepCos * 0.30f;
            this.leftArm.zRot = -0.15f;

            // Torso slight twist with the broom swing
            this.body.yRot = sweepCos * 0.18f;
        }
    }
}
