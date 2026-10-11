package net.zootycoon.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
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
}

