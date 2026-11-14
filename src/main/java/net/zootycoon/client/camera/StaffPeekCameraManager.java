package net.zootycoon.client.camera;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.UUID;

public class StaffPeekCameraManager {

    private static boolean peeking = false;
    private static int targetEntityId = -1;
    private static double targetX = 0.0;
    private static double targetY = 0.0;
    private static double targetZ = 0.0;
    private static String staffName = "";
    private static float distance = 12.0F;

    public static void onServerSync(boolean active, int entityId, double x, double y, double z, String name) {
        if (!active) {
            stopLocal();
            return;
        }
        boolean newlyStarted = !peeking;
        peeking = true;
        targetEntityId = entityId;
        targetX = x;
        targetY = y;
        targetZ = z;
        staffName = name != null ? name : "";
        if (newlyStarted) {
            distance = 12.0F;
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null) {
                mc.setScreen(null);
            }
        }
    }

    public static void startRequest(String staffType, UUID staffUuid) {
        if (staffUuid == null) return;
        ClientPlayNetworking.send(new ZooNetworkPayloads.PeekStaffPayload(staffType, staffUuid, false));
    }

    public static void stopAndNotifyServer() {
        if (!peeking) return;
        stopLocal();
        ClientPlayNetworking.send(new ZooNetworkPayloads.PeekStaffPayload("", new UUID(0L, 0L), true));
    }

    public static void stopLocal() {
        boolean wasPeeking = peeking;
        peeking = false;
        targetEntityId = -1;
        staffName = "";
        if (wasPeeking) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui != null) {
                mc.gui.setOverlayMessage(Component.empty(), false);
            }
        }
    }

    public static void onScroll(double vertical) {
        if (vertical > 0) {
            distance = Math.max(3.0F, distance - 1.5F);
        } else if (vertical < 0) {
            distance = Math.min(48.0F, distance + 1.5F);
        }
    }

    public static void tick(Minecraft mc) {
        if (!peeking) return;
        if (mc.player == null || mc.level == null) {
            stopLocal();
            return;
        }
        if (mc.options.keyShift.isDown() || mc.screen != null) {
            stopAndNotifyServer();
            return;
        }
        String distText = String.format(java.util.Locale.ROOT, "%.0f", distance);
        mc.gui.setOverlayMessage(
                Component.literal(ZooTranslations.get("zootycoon.staff.peek.hud", staffName, distText)),
                false
        );
    }

    public static boolean isPeeking() {
        return peeking;
    }

    public static int getTargetEntityId() {
        return targetEntityId;
    }

    public static double getTargetX() {
        return targetX;
    }

    public static double getTargetY() {
        return targetY;
    }

    public static double getTargetZ() {
        return targetZ;
    }

    public static float getDistance() {
        return distance;
    }

    public static void setDistance(float newDist) {
        distance = Mth.clamp(newDist, 3.0F, 48.0F);
    }
}
