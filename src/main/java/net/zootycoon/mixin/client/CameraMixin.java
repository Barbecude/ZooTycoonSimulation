package net.zootycoon.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.client.camera.StaffPeekCameraManager;
import net.zootycoon.client.camera.ZooVehicleCameraManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private boolean detached;
    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yRot, float xRot);
    @Shadow protected abstract void move(float zoom, float dy, float dx);
    @Shadow protected abstract float getMaxZoom(float maxZoom);

    @ModifyArg(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"),
            index = 0
    )
    private float zootycoon$modifyVehicleCameraDistance(float originalMaxZoom) {
        Minecraft mc = Minecraft.getInstance();
        if (ZooVehicleCameraManager.isVehicleThirdPersonBack(mc)) {
            float scale = originalMaxZoom > 0.01F ? (originalMaxZoom / 4.0F) : 1.0F;
            return ZooVehicleCameraManager.getDistance() * scale;
        }
        return originalMaxZoom;
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void zootycoon$applyStaffPeekCamera(BlockGetter level, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float partialTick, CallbackInfo ci) {
        if (!StaffPeekCameraManager.isPeeking()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        this.detached = true;
        Vec3 targetPos = null;
        int entityId = StaffPeekCameraManager.getTargetEntityId();
        if (entityId >= 0) {
            Entity target = mc.level.getEntity(entityId);
            if (target != null && target.isAlive()) {
                targetPos = target.getPosition(partialTick).add(0.0, target.getEyeHeight() * 0.85, 0.0);
            }
        }
        if (targetPos == null) {
            targetPos = new Vec3(
                    StaffPeekCameraManager.getTargetX(),
                    StaffPeekCameraManager.getTargetY(),
                    StaffPeekCameraManager.getTargetZ()
            );
        }

        this.setPosition(targetPos.x, targetPos.y, targetPos.z);
        this.setRotation(mc.player.getViewYRot(partialTick), mc.player.getViewXRot(partialTick));
        this.move(-this.getMaxZoom(StaffPeekCameraManager.getDistance()), 0.0F, 0.0F);
    }
}
