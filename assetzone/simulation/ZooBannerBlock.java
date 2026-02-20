package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class ZooBannerBlock extends BannerBlock {
    public ZooBannerBlock() {
        super(DyeColor.WHITE, BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOL)
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noCollission());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new net.minecraft.world.level.block.entity.BannerBlockEntity(pos, state, DyeColor.WHITE);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            ZooData data = ZooData.get(level);
            java.util.List<BlockPos> existing = new java.util.ArrayList<>(data.getEntrances());
            for (BlockPos existingPos : existing) {
                if (existingPos.equals(pos)) continue;
                BlockState existingState = level.getBlockState(existingPos);
                if (existingState.is(IndoZooTycoon.ZOO_BANNER_BLOCK.get())
                        || existingState.is(IndoZooTycoon.ZOO_WALL_BANNER_BLOCK.get())) {
                    level.destroyBlock(existingPos, false);
                }
                data.removeEntrance(existingPos);
            }
            data.addEntrance(pos);
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
