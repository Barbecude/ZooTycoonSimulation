package com.example.simulation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncBalancePacket {
    private final int balance;
    private final net.minecraft.nbt.ListTag taggedAnimals;
    private final int animalCount;
    private final int staffCount;
    private final int visitorCount;

    public SyncBalancePacket(int balance, net.minecraft.nbt.ListTag taggedAnimals, int ac, int sc, int vc) {
        this.balance = balance;
        this.taggedAnimals = taggedAnimals;
        this.animalCount = ac;
        this.staffCount = sc;
        this.visitorCount = vc;
    }

    public SyncBalancePacket(FriendlyByteBuf buf) {
        this.balance = buf.readInt();
        this.taggedAnimals = buf.readNbt().getList("TaggedAnimals", 10);
        this.animalCount = buf.readInt();
        this.staffCount = buf.readInt();
        this.visitorCount = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(balance);
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.put("TaggedAnimals", taggedAnimals);
        buf.writeNbt(tag);
        buf.writeInt(animalCount);
        buf.writeInt(staffCount);
        buf.writeInt(visitorCount);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Client side handling
            ClientZooData.setBalance(balance);
            ClientZooData.setAnimalCount(animalCount);
            ClientZooData.setStaffCount(staffCount);
            ClientZooData.setVisitorCount(visitorCount);

            // Also sync to main client ZooData instance so GUI can read it
            ZooData data = ZooData.get(net.minecraft.client.Minecraft.getInstance().level);
            data.setBalance(balance);
            data.setAnimalCount(animalCount);
            data.setStaffCount(staffCount);
            data.setVisitorCount(visitorCount);
            // We need a setter for taggedAnimals or manipulation
            data.getTaggedAnimals().clear();
            data.getTaggedAnimals().addAll(taggedAnimals);
        });
        return true;
    }
}
