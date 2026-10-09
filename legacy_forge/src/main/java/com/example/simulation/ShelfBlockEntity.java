package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ShelfBlockEntity extends BlockEntity implements Container, net.minecraft.world.MenuProvider {
    private static final int CONTAINER_SIZE = 9; // 3x3 grid
    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    
    private int foodPrice = 5000;
    private int drinkPrice = 3000;
    private long revenue = 0;

    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.SHELF_BE.get(), pos, state);
    }

    // Food stock is total count of all items in slots
    public int getFoodStock() {
        int total = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public void setFoodStock(int amount) {
        // Legacy compatibility - not used with multi-slot
    }

    public boolean isStockEmpty() {
        return getFoodStock() <= 0;
    }

    public boolean isStockFull() {
        for (ItemStack stack : items) {
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    public void addFoodStock(int amount) {
        // Legacy compatibility - not used with multi-slot
    }

    public boolean removeFoodStock(int amount) {
        // Try to remove amount from slots
        int remaining = amount;
        for (int i = 0; i < items.size() && remaining > 0; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
                if (stack.isEmpty()) {
                    items.set(i, ItemStack.EMPTY);
                }
            }
        }
        if (remaining < amount) {
            setChanged();
            syncToClient();
            return true;
        }
        return false;
    }

    public boolean addFoodItem(ItemStack stack) {
        // Legacy compatibility - use Container methods instead
        return false;
    }

    public static boolean isValidShelfFood(ItemStack stack) {
        // Accept any non-empty item — the shelf stores whatever the player puts in.
        return !stack.isEmpty();
    }

    public ItemStack getDisplayFood() {
        // Return first non-empty item for compatibility
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                ItemStack display = stack.copy();
                display.setCount(1);
                return display;
            }
        }
        return ItemStack.EMPTY;
    }

    // Get all items for rendering
    public NonNullList<ItemStack> getAllItems() {
        return items;
    }

    // Drink-related API kept for compatibility with existing renderer/logic.
    public int getDrinkStock() {
        return 0;
    }

    public void setDrinkStock(int amount) {
        // Not used in debug shelf mode.
    }

    public void addDrinkStock(int amount) {
        // Not used in debug shelf mode.
    }

    public boolean removeDrinkStock(int amount) {
        return false;
    }

    public boolean addDrinkItem(ItemStack stack) {
        return false;
    }

    public ItemStack getDisplayDrink() {
        return ItemStack.EMPTY;
    }

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

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("FoodPrice", foodPrice);
        tag.putInt("DrinkPrice", drinkPrice);
        tag.putLong("Revenue", revenue);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        foodPrice = tag.contains("FoodPrice") ? tag.getInt("FoodPrice") : 5000;
        drinkPrice = tag.contains("DrinkPrice") ? tag.getInt("DrinkPrice") : 3000;
        revenue = tag.getLong("Revenue");
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

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ShelfMenu(syncId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
            syncToClient();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        setChanged();
        syncToClient();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.size()) {
            items.set(slot, stack);
            if (stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            setChanged();
            syncToClient();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this)
            return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !stack.isEmpty() && isValidShelfFood(stack);
    }

    @Override
    public void clearContent() {
        items.clear();
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            items.add(ItemStack.EMPTY);
        }
        setChanged();
        syncToClient();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}
