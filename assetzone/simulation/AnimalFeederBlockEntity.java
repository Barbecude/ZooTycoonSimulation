package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AnimalFeederBlockEntity extends BlockEntity {
    private int foodLevel = 0; // 0-100
    private ItemStack displayFood = ItemStack.EMPTY;
    private String assignedAnimalId = "";

    public AnimalFeederBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.ANIMAL_FEEDER_BE.get(), pos, state);
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public void addFood(int amount) {
        this.foodLevel = Math.min(100, this.foodLevel + amount);
        updateVisualLevel();
        setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean addFood(ItemStack stack, int amount, String targetAnimalId) {
        if (stack.isEmpty() || amount <= 0) return false;
        Item item = stack.getItem();
        String validateAnimalId = targetAnimalId == null ? "" : targetAnimalId;
        if (!validateAnimalId.isEmpty() && !FoodAnimalRegistry.isValidFoodForAnimal(validateAnimalId, item)) return false;
        this.assignedAnimalId = validateAnimalId;
        if (displayFood.isEmpty()) {
            this.displayFood = new ItemStack(item);
        }
        addFood(amount);
        return true;
    }

    public ItemStack getDisplayFood() {
        return displayFood.copy();
    }

    public String getAssignedAnimalId() {
        return assignedAnimalId;
    }

    public boolean isFull() {
        return foodLevel >= 100;
    }

    private void updateVisualLevel() {
        if (level == null) return;
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(AnimalFeederBlock.LEVEL)) return;
        int visualLevel = Math.min(4, Math.max(0, (int) Math.ceil(foodLevel / 25.0D)));
        if (state.getValue(AnimalFeederBlock.LEVEL) != visualLevel) {
            level.setBlock(worldPosition, state.setValue(AnimalFeederBlock.LEVEL, visualLevel), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FoodLevel", foodLevel);
        if (!displayFood.isEmpty()) {
            tag.put("DisplayFood", displayFood.save(new CompoundTag()));
        }
        if (!assignedAnimalId.isEmpty()) {
            tag.putString("AssignedAnimalId", assignedAnimalId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        foodLevel = tag.getInt("FoodLevel");
        displayFood = tag.contains("DisplayFood") ? ItemStack.of(tag.getCompound("DisplayFood")) : ItemStack.EMPTY;
        assignedAnimalId = tag.getString("AssignedAnimalId");
        if (displayFood.isEmpty() && foodLevel > 0) {
            displayFood = new ItemStack(Items.HAY_BLOCK);
        }
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
