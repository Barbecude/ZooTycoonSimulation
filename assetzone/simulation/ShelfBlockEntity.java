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

public class ShelfBlockEntity extends BlockEntity {
    private int foodStock = 0;      // 0-64
    private int drinkStock = 0;     // 0-64
    private int foodPrice = 5000;   // default price
    private int drinkPrice = 3000;  // default price
    private long revenue = 0;       // accumulated revenue
    private ItemStack displayFood = ItemStack.EMPTY;
    private ItemStack displayDrink = ItemStack.EMPTY;

    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.SHELF_BE.get(), pos, state);
    }

    // Food Management
    public int getFoodStock() {
        return foodStock;
    }

    public void setFoodStock(int amount) {
        this.foodStock = Math.max(0, Math.min(64, amount));
        setChanged();
        syncToClient();
    }

    public void addFoodStock(int amount) {
        setFoodStock(foodStock + amount);
    }

    public boolean removeFoodStock(int amount) {
        if (foodStock >= amount) {
            setFoodStock(foodStock - amount);
            if (foodStock <= 0) {
                displayFood = ItemStack.EMPTY;
                syncToClient();
            }
            return true;
        }
        return false;
    }

    public boolean addFoodItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (!FoodAnimalRegistry.isFoodRegistered(item)) return false;
        if (foodStock >= 64) return false;
        addFoodStock(1);
        if (displayFood.isEmpty()) {
            displayFood = new ItemStack(item);
            syncToClient();
        }
        return true;
    }

    public ItemStack getDisplayFood() {
        return displayFood.copy();
    }

    // Drink Management
    public int getDrinkStock() {
        return drinkStock;
    }

    public void setDrinkStock(int amount) {
        this.drinkStock = Math.max(0, Math.min(64, amount));
        setChanged();
        syncToClient();
    }

    public void addDrinkStock(int amount) {
        setDrinkStock(drinkStock + amount);
    }

    public boolean removeDrinkStock(int amount) {
        if (drinkStock >= amount) {
            setDrinkStock(drinkStock - amount);
            if (drinkStock <= 0) {
                displayDrink = ItemStack.EMPTY;
                syncToClient();
            }
            return true;
        }
        return false;
    }

    public boolean addDrinkItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (drinkStock >= 64) return false;
        addDrinkStock(1);
        if (displayDrink.isEmpty()) {
            displayDrink = new ItemStack(stack.getItem());
            syncToClient();
        }
        return true;
    }

    public ItemStack getDisplayDrink() {
        return displayDrink.copy();
    }

    // Pricing
    public int getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(int price) {
        this.foodPrice = Math.max(0, price);
        setChanged();
        syncToClient();
    }

    public int getDrinkPrice() {
        return drinkPrice;
    }

    public void setDrinkPrice(int price) {
        this.drinkPrice = Math.max(0, price);
        setChanged();
        syncToClient();
    }

    // Revenue
    public long getRevenue() {
        return revenue;
    }

    public void addRevenue(long amount) {
        this.revenue += amount;
        setChanged();
        syncToClient();
    }

    public void resetRevenue() {
        this.revenue = 0;
        setChanged();
        syncToClient();
    }

    // Sync
    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FoodStock", foodStock);
        tag.putInt("DrinkStock", drinkStock);
        tag.putInt("FoodPrice", foodPrice);
        tag.putInt("DrinkPrice", drinkPrice);
        tag.putLong("Revenue", revenue);
        if (!displayFood.isEmpty()) {
            tag.put("DisplayFood", displayFood.save(new CompoundTag()));
        }
        if (!displayDrink.isEmpty()) {
            tag.put("DisplayDrink", displayDrink.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        foodStock = tag.getInt("FoodStock");
        drinkStock = tag.getInt("DrinkStock");
        foodPrice = tag.getInt("FoodPrice");
        drinkPrice = tag.getInt("DrinkPrice");
        revenue = tag.getLong("Revenue");
        displayFood = tag.contains("DisplayFood") ? ItemStack.of(tag.getCompound("DisplayFood")) : ItemStack.EMPTY;
        displayDrink = tag.contains("DisplayDrink") ? ItemStack.of(tag.getCompound("DisplayDrink")) : ItemStack.EMPTY;
        if (displayFood.isEmpty() && foodStock > 0) displayFood = new ItemStack(Items.APPLE);
        if (displayDrink.isEmpty() && drinkStock > 0) displayDrink = new ItemStack(Items.POTION);
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
