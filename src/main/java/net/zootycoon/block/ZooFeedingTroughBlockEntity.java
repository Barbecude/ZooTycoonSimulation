package net.zootycoon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ZooFeedingTroughBlockEntity extends BlockEntity implements Container {

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int waterLevel = 0; // 0: no water, 1-3: water level
    private long placedGameTime = 0;

    public ZooFeedingTroughBlockEntity(BlockPos pos, BlockState state) {
        super(ZooBlocks.FEEDING_TROUGH_BLOCK_ENTITY, pos, state);
    }

    public void setPlacedGameTime(long time) {
        this.placedGameTime = time;
    }

    public boolean isPlacementCooldown(long currentTime) {
        return currentTime - placedGameTime < 600L; // 30 seconds protection after being placed
    }

    public boolean hasWater() {
        return waterLevel > 0;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(int level) {
        this.waterLevel = Math.clamp(level, 0, 3);
        markUpdated();
    }

    public boolean hasFood() {
        return !items.get(0).isEmpty();
    }

    public ItemStack getFoodStack() {
        return items.get(0);
    }

    public boolean insertFood(ItemStack stack, int count) {
        if (stack.isEmpty() || hasWater()) return false;
        ItemStack current = items.get(0);

        int insertAmount = (count <= 0) ? stack.getCount() : Math.min(count, stack.getCount());
        if (current.isEmpty()) {
            ItemStack newStack = stack.copy();
            newStack.setCount(insertAmount);
            items.set(0, newStack);
            stack.shrink(insertAmount);
            markUpdated();
            return true;
        } else if (ItemStack.isSameItemSameComponents(current, stack)) {
            int space = 64 - current.getCount();
            if (space > 0) {
                int toAdd = Math.min(space, insertAmount);
                current.grow(toAdd);
                stack.shrink(toAdd);
                markUpdated();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractFood(int count) {
        ItemStack current = items.get(0);
        if (current.isEmpty()) return ItemStack.EMPTY;
        int toExtract = Math.min(count, current.getCount());
        ItemStack result = current.split(toExtract);
        if (current.isEmpty()) {
            items.set(0, ItemStack.EMPTY);
        }
        markUpdated();
        return result;
    }

    public void dropInventory() {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, worldPosition, this);
        }
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("WaterLevel", waterLevel);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        this.waterLevel = tag.getInt("WaterLevel");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Container Interface Methods
    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty() && waterLevel == 0;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? items.get(0) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack res = ContainerHelper.removeItem(items, slot, amount);
        if (!res.isEmpty()) {
            markUpdated();
        }
        return res;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack res = ContainerHelper.takeItem(items, slot);
        if (!res.isEmpty()) {
            markUpdated();
        }
        return res;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            items.set(0, stack);
            if (stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            markUpdated();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.set(0, ItemStack.EMPTY);
        this.waterLevel = 0;
        markUpdated();
    }
}

