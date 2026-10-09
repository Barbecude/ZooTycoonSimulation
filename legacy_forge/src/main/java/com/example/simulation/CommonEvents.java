package com.example.simulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

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

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        handleTagPriority(event.getEntity(), event.getTarget(), event.getItemStack(), event);
        if (!event.isCanceled()) {
            handleTaggedAnimalFeeding(event.getEntity(), event.getTarget(), event.getItemStack(), event);
        }
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handleTagPriority(event.getEntity(), event.getTarget(), event.getItemStack(), event);
        if (!event.isCanceled()) {
            handleTaggedAnimalFeeding(event.getEntity(), event.getTarget(), event.getItemStack(), event);
        }
    }

    private static void handleTagPriority(Player player, net.minecraft.world.entity.Entity target, net.minecraft.world.item.ItemStack stack, net.minecraftforge.eventbus.api.Event evt) {
        if (player == null || target == null) return;
        if (player.level().isClientSide) return;
        if (!(stack.getItem() instanceof AnimalTagItem)) return;
        if (!(target instanceof LivingEntity le)) return;
        if (le instanceof Player) return;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp)) return;

        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new OpenNamingGuiPacket(le.getId()));

        if (evt instanceof PlayerInteractEvent.EntityInteract e) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.SUCCESS);
        } else if (evt instanceof PlayerInteractEvent.EntityInteractSpecific e) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static void handleTaggedAnimalFeeding(Player player, Entity target, ItemStack stack, net.minecraftforge.eventbus.api.Event evt) {
        if (player == null || target == null || stack.isEmpty()) return;
        if (player.level().isClientSide) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        long now = serverLevel.getGameTime();
        if (player.getPersistentData().getLong("indozoo_last_feed_tick") == now) return;
        if (!(target instanceof LivingEntity living)) return;
        if (living instanceof Player) return;
        if (!ZooAnimalHungerSystem.canEatFood(serverLevel, living, stack.getItem())) return;
        if (!ZooAnimalHungerSystem.feedTaggedAnimal(serverLevel, living, stack.getItem(), player)) return;

        player.getPersistentData().putLong("indozoo_last_feed_tick", now);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (evt instanceof PlayerInteractEvent.EntityInteract e) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.SUCCESS);
        } else if (evt instanceof PlayerInteractEvent.EntityInteractSpecific e) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(IndoZooTycoon.CASHIER_ENTITY.get(), CashierEntity.createAttributes().build());
        event.put(IndoZooTycoon.ZOOKEEPER_ENTITY.get(), ZookeeperEntity.createAttributes().build());
        event.put(IndoZooTycoon.SECURITY_ENTITY.get(), SecurityEntity.createAttributes().build());
    }
}
