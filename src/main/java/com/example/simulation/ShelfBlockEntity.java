package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ShelfBlockEntity extends BlockEntity implements Container, net.minecraft.world.MenuProvider {
    private static final int MAX_STOCK = 64;

    private ItemStack stockItem = ItemStack.EMPTY;
    private int foodPrice = 5000;
    private int drinkPrice = 3000;
    private long revenue = 0;

    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.SHELF_BE.get(), pos, state);
    }

    // Food stock is represented by the internal stack count.
    public int getFoodStock() {
        return stockItem.isEmpty() ? 0 : stockItem.getCount();
    }

    public void setFoodStock(int amount) {
        amount = Math.max(0, Math.min(MAX_STOCK, amount));
        if (amount <= 0) {
            stockItem = ItemStack.EMPTY;
        } else {
            if (stockItem.isEmpty()) {
                // No item assigned yet — default to cooked beef for backwards compat
                stockItem = new ItemStack(Items.COOKED_BEEF, amount);
            } else {
                stockItem.setCount(amount);
            }
        }
        setChanged();
        syncToClient();
    }

    public boolean isStockEmpty() {
        return getFoodStock() <= 0;
    }

    public boolean isStockFull() {
        return getFoodStock() >= MAX_STOCK;
    }

    public void addFoodStock(int amount) {
        if (amount <= 0)
            return;
        if (stockItem.isEmpty()) {
            stockItem = new ItemStack(Items.COOKED_BEEF, 0); // default fallback
        }
        stockItem.grow(Math.min(amount, MAX_STOCK - stockItem.getCount()));
        setChanged();
        syncToClient();
    }

    public boolean removeFoodStock(int amount) {
        if (amount <= 0)
            return false;
        if (getFoodStock() < amount)
            return false;

        stockItem.shrink(amount);
        if (stockItem.isEmpty()) {
            stockItem = ItemStack.EMPTY;
        }
        setChanged();
        syncToClient();
        return true;
    }

    public boolean addFoodItem(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (isStockFull())
            return false;

        if (stockItem.isEmpty()) {
            // First item placed: set this item as the stock type
            stockItem = stack.copy();
            stockItem.setCount(Math.min(stack.getCount(), MAX_STOCK));
            setChanged();
            syncToClient();
            return true;
        }

        // Already has an item — only accept the same type
        if (!ItemStack.isSameItemSameTags(stockItem, stack))
            return false;
        int add = Math.min(stack.getCount(), MAX_STOCK - stockItem.getCount());
        if (add <= 0)
            return false;

        stockItem.grow(add);
        setChanged();
        syncToClient();
        return true;
    }

    public static boolean isValidShelfFood(ItemStack stack) {
        // Accept any non-empty item — the shelf stores whatever the player puts in.
        return !stack.isEmpty();
    }

    public ItemStack getDisplayFood() {
        if (stockItem.isEmpty())
            return ItemStack.EMPTY;
        ItemStack display = stockItem.copy();
        display.setCount(1);
        return display;
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
        tag.putInt("FoodStock", getFoodStock());
        tag.putInt("DrinkStock", 0);
        tag.putInt("FoodPrice", foodPrice);
        tag.putInt("DrinkPrice", drinkPrice);
        tag.putLong("Revenue", revenue);
        if (!stockItem.isEmpty()) {
            tag.put("StockItem", stockItem.save(new CompoundTag()));
            tag.put("DisplayFood", stockItem.save(new CompoundTag())); // legacy mirror
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("StockItem")) {
            stockItem = ItemStack.of(tag.getCompound("StockItem"));
        } else if (tag.contains("DisplayFood")) {
            stockItem = ItemStack.of(tag.getCompound("DisplayFood"));
        } else {
            stockItem = ItemStack.EMPTY;
        }

        int legacyFoodStock = tag.getInt("FoodStock");
        if (!stockItem.isEmpty()) {
            // Keep whatever item is stored; just fix count if needed
            stockItem.setCount(
                    Math.max(1, Math.min(MAX_STOCK, legacyFoodStock > 0 ? legacyFoodStock : stockItem.getCount())));
        } else if (legacyFoodStock > 0) {
            // Legacy: no item tag but had a food stock count — default to cooked beef
            stockItem = new ItemStack(Items.COOKED_BEEF, Math.min(MAX_STOCK, legacyFoodStock));
        }

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
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stockItem.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? stockItem : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || amount <= 0 || stockItem.isEmpty())
            return ItemStack.EMPTY;
        ItemStack split = stockItem.split(amount);
        if (stockItem.isEmpty())
            stockItem = ItemStack.EMPTY;
        setChanged();
        syncToClient();
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0)
            return ItemStack.EMPTY;
        ItemStack out = stockItem;
        stockItem = ItemStack.EMPTY;
        setChanged();
        syncToClient();
        return out;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0)
            return;
        if (stack.isEmpty()) {
            stockItem = ItemStack.EMPTY;
        } else {
            ItemStack copy = stack.copy();
            copy.setCount(Math.min(MAX_STOCK, copy.getCount()));
            stockItem = copy;
        }
        setChanged();
        syncToClient();
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
        if (slot != 0)
            return false;
        if (stockItem.isEmpty())
            return !stack.isEmpty();
        // If slot has an item, only allow the same item type
        return ItemStack.isSameItemSameTags(stockItem, stack);
    }

    @Override
    public void clearContent() {
        stockItem = ItemStack.EMPTY;
        setChanged();
        syncToClient();
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STOCK;
    }
}
