package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AnimalFeederBlockEntity extends BlockEntity {
    private int foodLevel = 0; // 0-100

    public AnimalFeederBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.ANIMAL_FEEDER_BE.get(), pos, state);
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public void addFood(int amount) {
        this.foodLevel = Math.min(100, this.foodLevel + amount);
        setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean isFull() {
        return foodLevel >= 100;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FoodLevel", foodLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        foodLevel = tag.getInt("FoodLevel");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
