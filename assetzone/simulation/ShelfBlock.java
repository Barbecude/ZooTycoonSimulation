package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ShelfBlock extends BaseEntityBlock {
    public ShelfBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .strength(2.5F)
                .noOcclusion());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShelfBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ShelfBlockEntity shelf)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        boolean isFoodStall = state.is(IndoZooTycoon.FOOD_STALL_BLOCK.get());
        boolean isDrinkStall = state.is(IndoZooTycoon.DRINK_STALL_BLOCK.get());

        if (!held.isEmpty()) {
            if (isFoodStall && shelf.addFoodItem(held)) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                return InteractionResult.SUCCESS;
            }
            if (isDrinkStall && shelf.addDrinkItem(held)) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }

        String msgText = String.format(
                "Shelf Stock: [Food: %d/64] [Drink: %d/64] | Prices: [Food: %,d] [Drink: %,d]",
                shelf.getFoodStock(), shelf.getDrinkStock(), shelf.getFoodPrice(), shelf.getDrinkPrice()
        );
        if (isFoodStall && !shelf.getDisplayFood().isEmpty()) {
            msgText += " | " + FoodAnimalRegistry.getFoodTooltip(shelf.getDisplayFood().getItem());
        }
        player.sendSystemMessage(Component.literal(msgText));
        return InteractionResult.SUCCESS;
    }
}
