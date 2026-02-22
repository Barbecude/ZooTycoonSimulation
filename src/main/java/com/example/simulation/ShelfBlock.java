package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
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
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ShelfBlockEntity shelf)) return InteractionResult.PASS;

        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            player.sendSystemMessage(buildStatusMessage(state, shelf));
            return InteractionResult.CONSUME;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, shelf, pos);
        }
        return InteractionResult.CONSUME;
    }

    private Component buildStatusMessage(BlockState state, ShelfBlockEntity shelf) {
        String mode = isFoodShelfState(state) ? "FOOD" : (isDrinkShelfState(state) ? "DRINK" : "SHELF");
        String txt = String.format("Shelf[%s] Stock: %d/64 | Item: cooked_beef | Price: %,d",
                mode, shelf.getFoodStock(), shelf.getFoodPrice());
        return Component.literal(txt);
    }

    public static boolean isFoodShelfState(BlockState state) {
        return state.is(IndoZooTycoon.FOOD_STALL_BLOCK.get())
                || state.is(IndoZooTycoon.OAK_SHELF_BLOCK.get())
                || state.is(IndoZooTycoon.OAK_STANDING_SHELF_BLOCK.get())
                || state.is(IndoZooTycoon.OAK_TOWER_SHELF_BLOCK.get());
    }

    public static boolean isDrinkShelfState(BlockState state) {
        return state.is(IndoZooTycoon.DRINK_STALL_BLOCK.get());
    }

    public static boolean isAnyShelfState(BlockState state) {
        return isFoodShelfState(state) || isDrinkShelfState(state);
    }
}
