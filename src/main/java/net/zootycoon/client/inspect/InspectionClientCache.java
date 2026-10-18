package net.zootycoon.client.inspect;

import net.minecraft.client.Minecraft;
import net.zootycoon.network.InspectionPayloads;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InspectionClientCache {

    public record Snapshot(
            int entityId,
            boolean registered,
            int hunger,
            int thirst,
            int cleanliness,
            long timestamp,
            long receiveGameTime
    ) {
        public boolean isExpired(long currentGameTime) {
            // Retain snapshot for up to 30 seconds (600 ticks) to avoid flickering
            return (currentGameTime - receiveGameTime) > 600L;
        }
    }

    private static final Map<Integer, Snapshot> CACHE = new ConcurrentHashMap<>();

    public static void update(InspectionPayloads.InspectionSnapshotPayload payload) {
        long gameTime = 0L;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            gameTime = mc.level.getGameTime();
        }
        CACHE.put(payload.entityId(), new Snapshot(
                payload.entityId(),
                payload.registered(),
                payload.hunger(),
                payload.thirst(),
                payload.cleanliness(),
                payload.timestamp(),
                gameTime
        ));
    }

    public static Snapshot getSnapshot(int entityId) {
        Snapshot snapshot = CACHE.get(entityId);
        if (snapshot == null) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            long currentGameTime = mc.level.getGameTime();
            if (snapshot.isExpired(currentGameTime)) {
                CACHE.remove(entityId);
                return null;
            }
        }
        return snapshot;
    }

    public static void clear() {
        CACHE.clear();
    }
}

