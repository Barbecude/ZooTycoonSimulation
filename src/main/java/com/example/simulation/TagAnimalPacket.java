package com.example.simulation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TagAnimalPacket {
    public final int entityId;
    public final String customName;

    public TagAnimalPacket(int entityId, String name) {
        this.entityId = entityId;
        this.customName = name;
    }

    public TagAnimalPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.customName = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(customName);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isServer()) {
                Entity entity = ctx.getSender().level().getEntity(entityId);
                if (entity != null) {
                    entity.setCustomName(Component.literal(customName).withStyle(net.minecraft.ChatFormatting.GOLD));
                    entity.setCustomNameVisible(true);
                    entity.getPersistentData().putBoolean("ZooAnimal", true);

                    entity.getPersistentData().putBoolean("ZooAnimal", true);

                    // Add to stats
                    ZooData data = ZooData.get(ctx.getSender().level());
                    net.minecraft.resources.ResourceLocation typeId = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                            .getKey(entity.getType());
                    String typeStr = (typeId != null) ? typeId.toString() : "minecraft:pig";
                    data.addAnimal(entityId, customName, typeStr);

                    // Sync to client immediately
                    SyncBalancePacket sync = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                            data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
                    PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), sync);

                    ctx.getSender().sendSystemMessage(Component.literal("Animal Tagged: " + customName));
                }
            }
        });
        return true;
    }
}
