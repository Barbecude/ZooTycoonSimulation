package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → Server packet: change the sell price of a Food Stall item.
 * If {@code itemId} is empty, the global price is set.
 * Convention: if {@code price == -1} the server will reset the stall's revenue counter instead.
 */
public class SetFoodStallPricePacket {

    private final BlockPos pos;
    private final int price;
    private final String itemId;

    public SetFoodStallPricePacket(BlockPos pos, int price, String itemId) {
        this.pos = pos;
        this.price = price;
        this.itemId = itemId == null ? "" : itemId;
    }

    public SetFoodStallPricePacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.price = buf.readInt();
        this.itemId = buf.readUtf(128);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(price);
        buf.writeUtf(itemId, 128);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            BlockEntity be = player.level().getBlockEntity(pos);
            if (!(be instanceof FoodStallBlockEntity stall)) return;

            if (price == -1) {
                stall.resetRevenue();
            } else if (price >= 0) {
                if (itemId.isEmpty()) {
                    stall.setFoodPrice(price);
                } else {
                    stall.setItemPrice(itemId, price);
                }
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
