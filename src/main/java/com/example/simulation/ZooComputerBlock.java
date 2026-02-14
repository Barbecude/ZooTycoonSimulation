package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Zoo Computer Block — saat pemain klik kanan blok ini,
 * server mengirim menu chat interaktif (clickable chat GUI).
 */
public class ZooComputerBlock extends BaseEntityBlock {

    public ZooComputerBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5F)
                .requiresCorrectToolForDrops());
    }

    // ========== Block Entity ==========

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZooComputerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide())
            return null;
        return createTickerHelper(type, IndoZooTycoon.ZOO_COMPUTER_BE.get(),
                ZooComputerBlockEntity::serverTick);
    }

    // ========== Render sebagai blok normal (bukan invisible) ==========

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ========== Interaksi klik kanan → buka Chat GUI ==========

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ZooComputerBlockEntity computer) {
                computer.openChatMenu(serverPlayer);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
