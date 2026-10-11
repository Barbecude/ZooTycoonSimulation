package net.zootycoon.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
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
}
