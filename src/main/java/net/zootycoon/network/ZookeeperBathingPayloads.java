package net.zootycoon.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.entity.ZooZookeeperEntity;

import java.util.UUID;

/**
 * Network payload and receiver registration for Zookeeper bathing mechanics.
 * Handles transferring bathing tools (watering can + brush) from player to Zookeeper.
 */
public class ZookeeperBathingPayloads {

    // C2S: Player hands over bathing tools (1 watering can + 1 brush) to Zookeeper
    public record GiveBathingToolsPayload(UUID keeperUuid) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<GiveBathingToolsPayload> TYPE =
                new CustomPacketPayload.Type<>(ZooTycoonMod.id("give_bathing_tools"));
        public static final StreamCodec<FriendlyByteBuf, GiveBathingToolsPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeUUID(payload.keeperUuid),
                buf -> new GiveBathingToolsPayload(buf.readUUID())
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private static boolean registered = false;

    public static synchronized void register() {
        if (registered) return;
        registered = true;

        PayloadTypeRegistry.playC2S().register(GiveBathingToolsPayload.TYPE, GiveBathingToolsPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GiveBathingToolsPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                if (player == null || player.level() == null) return;
                ServerLevel level = (ServerLevel) player.level();
                Entity ent = level.getEntity(payload.keeperUuid());
                if (ent instanceof ZooZookeeperEntity keeper) {
                    keeper.handleBathingToolsTransfer(player);
                }
            });
        });
    }
}

