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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class AnimalFeederBlock extends BaseEntityBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);

    public AnimalFeederBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnimalFeederBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, IndoZooTycoon.ANIMAL_FEEDER_BE.get(), AnimalFeederBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AnimalFeederBlockEntity feeder)) return InteractionResult.PASS;

        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            player.sendSystemMessage(buildStatusMessage(feeder));
            return InteractionResult.CONSUME;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, feeder, pos);
        }
        return InteractionResult.CONSUME;
    }

    private Component buildStatusMessage(AnimalFeederBlockEntity feeder) {
        StringBuilder sb = new StringBuilder();
        sb.append("Feeder Stock: ").append(feeder.getFoodCount()).append("/64");
        if (!feeder.getDisplayFood().isEmpty()) {
            sb.append(" | Food: ").append(feeder.getDisplayFood().getHoverName().getString());
        }
        sb.append(" | Mode: universal");
        return Component.literal(sb.toString());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(LEVEL);
    }
}
