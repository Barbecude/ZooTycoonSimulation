package com.example.simulation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AnimalTagItem extends Item {
    public AnimalTagItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
            InteractionHand usedHand) {
        if (!player.level().isClientSide) {
            PacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new OpenNamingGuiPacket(interactionTarget.getId()));
        }
        return InteractionResult.SUCCESS;
    }
}
