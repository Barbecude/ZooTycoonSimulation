package com.example.simulation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public class OpenNamingGuiPacket {
    public final int entityId;

    public OpenNamingGuiPacket(int entityId) {
        this.entityId = entityId;
    }

    public OpenNamingGuiPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Client side open GUI
            // Avoid class loading issues on server
            if (ctx.getDirection().getReceptionSide().isClient()) {
                Minecraft.getInstance().setScreen(new AnimalNamingScreen(entityId));
            }
        });
        return true;
    }
}
