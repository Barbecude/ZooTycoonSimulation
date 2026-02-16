package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class ZooBannerBlock extends Block {
    public ZooBannerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOL)
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .noCollission()); // Like a banner, no collision?
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            ZooData.get(level).addEntrance(pos);
            // Optional: Notify player
            // Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(),
            // 10, false);
            // if (player != null)
            // player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Zoo
            // Entrance Set!"));
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                ZooData.get(level).removeEntrance(pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
