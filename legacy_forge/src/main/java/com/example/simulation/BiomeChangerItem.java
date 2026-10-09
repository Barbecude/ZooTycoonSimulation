package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;

public class BiomeChangerItem extends Item {
    public BiomeChangerItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openScreen(context.getClickedPos()));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            BlockPos pos = player.blockPosition();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openScreen(pos));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private void openScreen(BlockPos pos) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new BiomeChangerScreen(pos));
    }
}
