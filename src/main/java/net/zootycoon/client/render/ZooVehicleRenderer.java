package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.entity.ZooVehicleEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZooVehicleRenderer extends EntityRenderer<ZooVehicleEntity> {

    private static final Map<Integer, ItemStack> MODEL_STACK_CACHE = new ConcurrentHashMap<>();

    public ZooVehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.4F;
    }

    private static ItemStack getModelStack(int customModelData) {
        return MODEL_STACK_CACHE.computeIfAbsent(customModelData, cmd -> {
            ItemStack stack = new ItemStack(Items.STRUCTURE_VOID);
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(cmd));
            return stack;
        });
    }

    @Override
    public void render(ZooVehicleEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ZooVehicleEntity.VehicleSpec spec = entity.getSpec();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float clearanceY = spec.groundClearanceY();
        float steerAngle = entity.getSteerAngle();
        float wheelSpin = entity.getWheelSpin();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));

        // 1. Vehicle Body
        if (spec.bodyCmd() > 0) {
            poseStack.pushPose();
            Vec3 bodyOff = spec.bodyOffset();
            poseStack.translate(bodyOff.x, clearanceY + bodyOff.y, bodyOff.z);
            float bScale = spec.bodyScale();
            poseStack.scale(bScale, bScale, bScale);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            itemRenderer.renderStatic(
                    getModelStack(spec.bodyCmd()),
                    ItemDisplayContext.NONE,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );
            poseStack.popPose();
        }

        // 2. Steering Wheel
        if (spec.steerCmd() > 0) {
            poseStack.pushPose();
            Vec3 steerOff = spec.steerOffset();
            poseStack.translate(steerOff.x, clearanceY + steerOff.y, steerOff.z);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-steerAngle * 2.5F));
            float sScale = spec.steerScale();
            poseStack.scale(sScale, sScale, sScale);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            itemRenderer.renderStatic(
                    getModelStack(spec.steerCmd()),
                    ItemDisplayContext.NONE,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );
            poseStack.popPose();
        }

        // 3. Wheels
        for (ZooVehicleEntity.WheelSpec wheel : spec.wheels()) {
            poseStack.pushPose();
            Vec3 wOff = wheel.offset();
            poseStack.translate(wOff.x, clearanceY + wOff.y, wOff.z);
            if (wheel.steered()) {
                poseStack.mulPose(Axis.YP.rotationDegrees(steerAngle));
            }
            poseStack.mulPose(Axis.XP.rotationDegrees(wheelSpin));
            float wScale = wheel.scale();
            poseStack.scale(wScale, wScale, wScale);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            itemRenderer.renderStatic(
                    getModelStack(wheel.cmd()),
                    ItemDisplayContext.NONE,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(ZooVehicleEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return frustum.isVisible(entity.getBoundingBox().inflate(4.5D));
    }

    @Override
    public ResourceLocation getTextureLocation(ZooVehicleEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
