package com.example.simulation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenGuiPacket {
    public OpenGuiPacket() {
    }

    public OpenGuiPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                // Open GUI with global data
                NetworkHooks.openScreen(player, new net.minecraft.world.MenuProvider() {
                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return net.minecraft.network.chat.Component.literal("IndoZoo Dashboard");
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id,
                            net.minecraft.world.entity.player.Inventory inv,
                            net.minecraft.world.entity.player.Player p) {

                        net.minecraft.world.inventory.SimpleContainerData data = new net.minecraft.world.inventory.SimpleContainerData(
                                9);
                        if (p.level() instanceof net.minecraft.server.level.ServerLevel level) {
                            ZooData zooData = ZooData.get(level);
                            int bal = zooData.getBalance();
                            data.set(0, (bal >> 16) & 0xFFFF);
                            data.set(1, bal & 0xFFFF);

                            // Count entities (Global radius 300)
                            net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(p.blockPosition())
                                    .inflate(300);
                            data.set(2, zooData.getAnimalCount());
                            data.set(3, zooData.getStaffCount());
                            data.set(4, zooData.getVisitorCount());
                            data.set(5, zooData.getTrashCount());
                            data.set(6, 0); // Radius
                            data.set(7, zooData.getTicketPrice());
                            data.set(8, zooData.getMarketingLevel());
                        }

                        return new ZooComputerMenu(id, inv, data);
                    }
                }, buf -> {
                    // No extra data needed for client constructor
                });
            }
        });
        return true;
    }
}
