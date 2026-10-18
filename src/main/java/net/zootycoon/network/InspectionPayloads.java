package net.zootycoon.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.zootycoon.ZooTycoonMod;

public class InspectionPayloads {

    // C2S: Start inspecting an entity
    public record InspectStartPayload(int entityId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<InspectStartPayload> TYPE =
                new CustomPacketPayload.Type<>(ZooTycoonMod.id("inspect_start"));
        public static final StreamCodec<FriendlyByteBuf, InspectStartPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeInt(payload.entityId),
                buf -> new InspectStartPayload(buf.readInt())
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // C2S: Stop inspecting
    public record InspectStopPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<InspectStopPayload> TYPE =
                new CustomPacketPayload.Type<>(ZooTycoonMod.id("inspect_stop"));
        public static final StreamCodec<FriendlyByteBuf, InspectStopPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {},
                buf -> new InspectStopPayload()
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // S2C: Live snapshot of animal inspection data
    public record InspectionSnapshotPayload(
            int entityId,
            boolean registered,
            int hunger,
            int thirst,
            int cleanliness,
            boolean isWet,
            String season,
            String tempCategory,
            String habitatStatus,
            String habitatNeeds,
            long timestamp
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<InspectionSnapshotPayload> TYPE =
                new CustomPacketPayload.Type<>(ZooTycoonMod.id("inspection_snapshot"));
        public static final StreamCodec<FriendlyByteBuf, InspectionSnapshotPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.entityId);
                    buf.writeBoolean(payload.registered);
                    buf.writeInt(payload.hunger);
                    buf.writeInt(payload.thirst);
                    buf.writeInt(payload.cleanliness);
                    buf.writeBoolean(payload.isWet);
                    buf.writeUtf(payload.season);
                    buf.writeUtf(payload.tempCategory);
                    buf.writeUtf(payload.habitatStatus != null ? payload.habitatStatus : "NO_DATA");
                    buf.writeUtf(payload.habitatNeeds != null ? payload.habitatNeeds : "");
                    buf.writeLong(payload.timestamp);
                },
                buf -> {
                    InspectionSnapshotPayload p = new InspectionSnapshotPayload(
                            buf.readInt(),
                            buf.readBoolean(),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readBoolean(),
                            buf.readUtf(),
                            buf.readUtf(),
                            buf.readUtf(),
                            buf.readUtf(),
                            buf.readLong()
                    );
                    onSnapshotReceived(p);
                    return p;
                }
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private static final java.util.Map<Integer, ClimateData> LATEST_CLIMATE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Integer, HabitatData> LATEST_HABITAT = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Integer, Boolean> LATEST_WET = new java.util.concurrent.ConcurrentHashMap<>();

    public record ClimateData(String season, String tempCategory) {}
    public record HabitatData(String status, String needs) {}

    public static void onSnapshotReceived(InspectionSnapshotPayload payload) {
        if (payload != null) {
            LATEST_CLIMATE.put(payload.entityId(), new ClimateData(payload.season(), payload.tempCategory()));
            LATEST_HABITAT.put(payload.entityId(), new HabitatData(payload.habitatStatus(), payload.habitatNeeds()));
            LATEST_WET.put(payload.entityId(), payload.isWet());
        }
    }

    public static ClimateData getLatestClimate(int entityId) {
        return LATEST_CLIMATE.get(entityId);
    }

    public static HabitatData getLatestHabitat(int entityId) {
        return LATEST_HABITAT.get(entityId);
    }

    public static boolean isAnimalWet(int entityId) {
        return Boolean.TRUE.equals(LATEST_WET.get(entityId));
    }

    public static void registerCommon() {
        PayloadTypeRegistry.playC2S().register(InspectStartPayload.TYPE, InspectStartPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(InspectStopPayload.TYPE, InspectStopPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(InspectionSnapshotPayload.TYPE, InspectionSnapshotPayload.CODEC);
    }
}

