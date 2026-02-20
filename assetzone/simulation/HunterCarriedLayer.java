package com.example.simulation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public class HunterCarriedLayer extends RenderLayer<VisitorEntity, HumanoidModel<VisitorEntity>> {
    private final java.util.Map<Integer, CachedCarriedEntity> cache = new java.util.HashMap<>();

    public HunterCarriedLayer(RenderLayerParent<VisitorEntity, HumanoidModel<VisitorEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, VisitorEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isCarrying()) {
            cache.remove(entity.getId());
            return;
        }
        if (!entity.getPassengers().isEmpty()) {
            cache.remove(entity.getId());
            return;
        }

        Entity carried = getOrCreateCarriedEntity(entity);
        if (carried == null) return;

        poseStack.pushPose();
        HumanoidModel<VisitorEntity> model = this.getParentModel();
        model.body.translateAndRotate(poseStack);

        float width = carried.getBbWidth();
        float height = carried.getBbHeight();
        float maxDim = Math.max(width, height);
        if (maxDim < 0.1F) {
            maxDim = 0.1F;
        }
        float scale = 0.45F / maxDim;

        poseStack.translate(0.0, 0.75, -0.55);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, height * 0.5F, 0.0F);

        // Pastikan entitas tegak lurus
        if (carried instanceof net.minecraft.world.entity.LivingEntity le) {
            le.setXRot(0f);
            le.setYRot(0f);
            le.yBodyRot = 0f;
            le.yHeadRot = 0f;
        }

        // Render the actual entity
        Minecraft.getInstance().getEntityRenderDispatcher().render(carried, 0, 0, 0, 0, partialTick, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    private Entity getOrCreateCarriedEntity(VisitorEntity carrier) {
        CompoundTag data = carrier.getCarriedAnimalData();
        if (data == null || !data.contains("Type")) return null;

        String typeStr = data.getString("Type");
        CachedCarriedEntity cached = cache.get(carrier.getId());
        if (cached != null && typeStr.equals(cached.typeId) && cached.entity != null) {
            return cached.entity;
        }

        ResourceLocation id = ResourceLocation.tryParse(typeStr);
        if (id == null) return null;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type == null) return null;

        Entity entity = type.create(carrier.level());
        if (entity == null) return null;
        if (data.contains("Nbt")) {
            entity.load(data.getCompound("Nbt"));
        }
        entity.setPos(0, 0, 0);
        cache.put(carrier.getId(), new CachedCarriedEntity(typeStr, entity));
        return entity;
    }

    private record CachedCarriedEntity(String typeId, Entity entity) {}
}
