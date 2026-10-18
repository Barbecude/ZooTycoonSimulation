package net.zootycoon.animal;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.InspectionPayloads;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZooInspectionService {

    private static final Map<UUID, Integer> ACTIVE_INSPECTORS = new ConcurrentHashMap<>();

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(InspectionPayloads.InspectStartPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> onStart(player, payload.entityId()));
        });

        ServerPlayNetworking.registerGlobalReceiver(InspectionPayloads.InspectStopPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> onStop(player.getUUID()));
        });
    }

    public static void onStart(ServerPlayer player, int targetEntityId) {
        if (player == null || !player.isAlive()) {
            return;
        }

        boolean holdingMagnifier = player.getMainHandItem().is(ZooItems.MAGNIFYING_GLASS)
                || player.getOffhandItem().is(ZooItems.MAGNIFYING_GLASS);
        if (!holdingMagnifier) {
            ACTIVE_INSPECTORS.remove(player.getUUID());
            return;
        }

        Entity entity = player.serverLevel().getEntity(targetEntityId);
        if (!(entity instanceof LivingEntity animal) || !animal.isAlive()) {
            ACTIVE_INSPECTORS.remove(player.getUUID());
            return;
        }

        if (!ZooAnimalHungerManager.isZooAnimalCandidate(animal) || !ZooAnimalHungerManager.isTagged(animal)) {
            ACTIVE_INSPECTORS.remove(player.getUUID());
            return;
        }

        if (player.distanceToSqr(animal) > 20.0 * 20.0) {
            ACTIVE_INSPECTORS.remove(player.getUUID());
            return;
        }

        ACTIVE_INSPECTORS.put(player.getUUID(), targetEntityId);
        sendSnapshot(player, animal);
    }

    public static void onStop(UUID playerUuid) {
        if (playerUuid != null) {
            ACTIVE_INSPECTORS.remove(playerUuid);
        }
    }

    public static void onPlayerDisconnect(ServerPlayer player) {
        if (player != null) {
            ACTIVE_INSPECTORS.remove(player.getUUID());
        }
    }

    public static void tickServer(MinecraftServer server) {
        if (server == null || server.getTickCount() % 10 != 0) {
            return;
        }
        if (ACTIVE_INSPECTORS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator = ACTIVE_INSPECTORS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            UUID playerUuid = entry.getKey();
            int targetEntityId = entry.getValue();

            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
            if (player == null || !player.isAlive() || player.hasDisconnected()) {
                iterator.remove();
                continue;
            }

            boolean holdingMagnifier = player.getMainHandItem().is(ZooItems.MAGNIFYING_GLASS)
                    || player.getOffhandItem().is(ZooItems.MAGNIFYING_GLASS);
            if (!holdingMagnifier) {
                iterator.remove();
                continue;
            }

            Entity entity = player.serverLevel().getEntity(targetEntityId);
            if (!(entity instanceof LivingEntity animal) || !animal.isAlive()) {
                iterator.remove();
                continue;
            }

            if (!ZooAnimalHungerManager.isZooAnimalCandidate(animal) || !ZooAnimalHungerManager.isTagged(animal)) {
                iterator.remove();
                continue;
            }

            if (player.distanceToSqr(animal) > 20.0 * 20.0) {
                iterator.remove();
                continue;
            }

            sendSnapshot(player, animal);
        }
    }

    private static void sendSnapshot(ServerPlayer player, LivingEntity animal) {
        boolean registered = ZooAnimalHungerManager.isTagged(animal);
        int hunger = -1;
        int thirst = -1;
        int cleanliness = -1;
        boolean isWet = false;

        if (registered) {
            hunger = ZooAnimalHungerManager.getHunger(animal);
            thirst = ZooAnimalHungerManager.getThirst(animal);
            cleanliness = ZooAnimalHungerManager.getCleanliness(animal);
            isWet = AnimalBathingManager.isWet(animal);
        }

        String season = ZooClimate.getCurrentSeason(animal.level());
        float temp = ZooClimate.getEffectiveTemperature(animal);
        String tempCat = ZooClimate.getCategory(temp).key;

        net.zootycoon.animal.habitat.HabitatEvaluator.HabitatResult habitat =
                net.zootycoon.animal.habitat.HabitatEvaluator.evaluate(player.serverLevel(), animal);
        String habitatStatus = habitat.status().name();
        String habitatNeeds = habitat.encodePayloadString();

        long timestamp = System.currentTimeMillis();
        InspectionPayloads.InspectionSnapshotPayload payload = new InspectionPayloads.InspectionSnapshotPayload(
                animal.getId(),
                registered,
                hunger,
                thirst,
                cleanliness,
                isWet,
                season,
                tempCat,
                habitatStatus,
                habitatNeeds,
                timestamp
        );

        ServerPlayNetworking.send(player, payload);
    }

    public static void notifyAnimalUpdated(LivingEntity animal) {
        if (animal == null || ACTIVE_INSPECTORS.isEmpty()) return;
        MinecraftServer server = animal.getServer();
        if (server == null) return;
        for (Map.Entry<UUID, Integer> entry : ACTIVE_INSPECTORS.entrySet()) {
            if (entry.getValue() == animal.getId()) {
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null && player.isAlive()) {
                    sendSnapshot(player, animal);
                }
            }
        }
    }

    public static void reset() {
        ACTIVE_INSPECTORS.clear();
    }
}

