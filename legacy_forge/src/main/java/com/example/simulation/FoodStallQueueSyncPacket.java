package com.example.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S→C packet: notify a nearby player that a Food Stall has a visitor queue.
 * On the client this opens (or refreshes) the FoodStallQueueScreen popup.
 */
public class FoodStallQueueSyncPacket {

    private final BlockPos pos;
    private final int queueSize;
    private final String moodLabel; // e.g. "HUNGRY", "HAPPY"
    private final String requestItemId; // e.g. "indozoo:fd_hamburger"

    public FoodStallQueueSyncPacket(BlockPos pos, int queueSize, String moodLabel, String requestItemId) {
        this.pos = pos;
        this.queueSize = queueSize;
        this.moodLabel = moodLabel;
        this.requestItemId = requestItemId == null ? "" : requestItemId;
    }

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static void encode(FoodStallQueueSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.queueSize);
        buf.writeUtf(msg.moodLabel, 32);
        buf.writeUtf(msg.requestItemId, 128);
    }

    public static FoodStallQueueSyncPacket decode(FriendlyByteBuf buf) {
        return new FoodStallQueueSyncPacket(
                buf.readBlockPos(),
                buf.readVarInt(),
                buf.readUtf(32),
                buf.readUtf(128));
    }

    // ── Handler ───────────────────────────────────────────────────────────────

    public static void handle(FoodStallQueueSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openOrRefresh(msg))
        );
        ctx.get().setPacketHandled(true);
    }

    private static void openOrRefresh(FoodStallQueueSyncPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        // If the full Food Stall GUI is open, update the queue overlay there instead.
        if (mc.screen instanceof FoodStallScreen foodScreen) {
            foodScreen.updateQueueInfo(msg.queueSize, msg.moodLabel, msg.requestItemId);
            return;
        }
        // No standalone popup — queue info only shown inside FoodStallScreen
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public BlockPos getPos()      { return pos; }
    public int      getQueueSize(){ return queueSize; }
    public String   getMoodLabel(){ return moodLabel; }
    public String   getRequestItemId(){ return requestItemId; }
}
