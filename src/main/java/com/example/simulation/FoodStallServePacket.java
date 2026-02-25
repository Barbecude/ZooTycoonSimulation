package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * C→S packet: player clicks "Layani 1" or "Layani Semua" in FoodStallQueueScreen.
 * The server pops visitors off the queue, processes the transaction, and marks them served.
 */
public class FoodStallServePacket {

    private final BlockPos pos;
    private final boolean serveAll;

    public FoodStallServePacket(BlockPos pos, boolean serveAll) {
        this.pos = pos;
        this.serveAll = serveAll;
    }

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static void encode(FoodStallServePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.serveAll);
    }

    public static FoodStallServePacket decode(FriendlyByteBuf buf) {
        return new FoodStallServePacket(buf.readBlockPos(), buf.readBoolean());
    }

    // ── Handler ───────────────────────────────────────────────────────────────

    public static void handle(FoodStallServePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            ServerLevel level = sender.serverLevel();
            if (!(level.getBlockEntity(msg.pos) instanceof FoodStallBlockEntity stall)) return;

            if (msg.serveAll) {
                stall.serveAll();
            } else {
                stall.serveNext();
            }

            // Immediately send updated queue info to the player
            PacketHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sender),
                new FoodStallQueueSyncPacket(
                    msg.pos, stall.getQueueSize(), "HUNGRY", stall.getNextRequestItemId()));
        });
        ctx.get().setPacketHandled(true);
    }
}
