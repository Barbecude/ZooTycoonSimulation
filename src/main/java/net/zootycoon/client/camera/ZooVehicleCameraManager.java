package net.zootycoon.client.camera;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.zootycoon.entity.ZooVehicleEntity;

public class ZooVehicleCameraManager {

    private static float distance = 5.5F;

    public static void onScroll(double vertical) {
        if (vertical > 0) {
            // Scroll UP: zoom closer to vehicle
            distance = Math.max(2.0F, distance - 0.5F);
        } else if (vertical < 0) {
            // Scroll DOWN: zoom further away from vehicle
            distance = Math.min(16.0F, distance + 0.5F);
        }
    }

    public static float getDistance() {
        return distance;
    }

    public static void setDistance(float newDist) {
        distance = Mth.clamp(newDist, 2.0F, 16.0F);
    }

    public static boolean isVehicleThirdPersonBack(Minecraft mc) {
        return mc != null
                && mc.player != null
                && mc.player.getVehicle() instanceof ZooVehicleEntity
                && mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK;
    }
}
