package com.example.simulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide)
            return;

        if (event.getEntity().getPersistentData().getBoolean("ZooAnimal")) {
            if (event.getEntity().level() instanceof ServerLevel serverLevel) {
                ZooData data = ZooData.get(serverLevel);
                data.removeAnimal(event.getEntity().getId());
                
                // Sync updated data
                SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                        data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
            }
        }
    }
}
