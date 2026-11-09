package net.zootycoon.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.zootycoon.client.camera.ZooVehicleCameraManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Camera.class)
public class CameraMixin {

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
}
