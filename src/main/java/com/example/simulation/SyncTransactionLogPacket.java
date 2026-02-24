package com.example.simulation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncTransactionLogPacket {
    private final ListTag log;

    public SyncTransactionLogPacket(ListTag log) {
        this.log = log;
    }

    public SyncTransactionLogPacket(FriendlyByteBuf buf) {
        CompoundTag wrapper = buf.readNbt();
        this.log = (wrapper != null) ? wrapper.getList("Log", 10) : new ListTag();
    }

    public void toBytes(FriendlyByteBuf buf) {
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("Log", log);
        buf.writeNbt(wrapper);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientZooData.setTransactionLog(log));
        return true;
    }
}
