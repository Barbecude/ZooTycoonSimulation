package com.example.simulation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
                if (entity instanceof LivingEntity le && !(le instanceof net.minecraft.world.entity.player.Player)) {
                    entity.setCustomName(Component.literal(customName).withStyle(net.minecraft.ChatFormatting.GOLD));
                    entity.setCustomNameVisible(true);
                    entity.getPersistentData().putBoolean("ZooAnimal", true);

                    // Add to stats
                    ZooData data = ZooData.get(ctx.getSender().level());
                    net.minecraft.resources.ResourceLocation typeId = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                            .getKey(entity.getType());
                    String typeStr = (typeId != null) ? typeId.toString() : "minecraft:pig";
                    data.addAnimal(entityId, entity.getUUID().toString(), customName, typeStr);

                    Level level = ctx.getSender().level();
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + (entity.getBbHeight() * 0.6), entity.getZ(), 12, 0.35, 0.35, 0.35, 0.02);
                        sl.sendParticles(ParticleTypes.ENCHANT, entity.getX(), entity.getY() + (entity.getBbHeight() * 0.8), entity.getZ(), 20, 0.45, 0.45, 0.45, 0.02);
                        sl.playSound(null, entity.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.1f);
                    }

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
