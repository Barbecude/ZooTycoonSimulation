package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ZooComputerBlockEntity extends BlockEntity {

    private int scanRadius = 10;
    private int progress = 0;

    public ZooComputerBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.ZOO_COMPUTER_BE.get(), pos, state);
    }

    public int getScanRadius() {
        return scanRadius;
    }

    public void setScanRadius(int scanRadius) {
        this.scanRadius = scanRadius;
        setChanged();
    }

    public void resetProgress() {
        this.progress = 0;
        setChanged();
    }
}
