package net.zootycoon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zootycoon.entity.ZooCashierEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ZooGachaMachineBlockEntity extends BlockEntity implements Clearable {

    public static final int SLOTS_COUNT = 5;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS_COUNT, ItemStack.EMPTY);
    private UUID ownerUuid = null;
    private static final Random RANDOM = new Random();

    public ZooGachaMachineBlockEntity(BlockPos pos, BlockState blockState) {
        super(ZooBlocks.GACHA_MACHINE_BLOCK_ENTITY, pos, blockState);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(@Nullable UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        setChanged();
    }

    public int getTotalStock() {
        int total = 0;
        for (ItemStack st : this.items) {
            if (!st.isEmpty()) {
                total += st.getCount();
            }
        }
        return total;
    }

    public boolean isEmpty() {
        return getTotalStock() == 0;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public boolean canAccept(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!ZooCashierEntity.isPlushieItem(stack.getItem())) return false;

        for (ItemStack slot : this.items) {
            if (slot.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < 64) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts as many plushies as possible from the provided stack.
     * Modifies stack directly and returns number of items successfully inserted.
     */
    public int insertPlushies(ItemStack stack) {
        if (!canAccept(stack)) return 0;

        int initialCount = stack.getCount();
        int remaining = initialCount;

        // Pass 1: Try merging with existing matching stacks up to 64
        for (int i = 0; i < SLOTS_COUNT && remaining > 0; i++) {
            ItemStack slot = this.items.get(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack)) {
                int space = 64 - slot.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, remaining);
                    slot.grow(toAdd);
                    remaining -= toAdd;
                }
            }
        }

        // Pass 2: Put in empty slots
        for (int i = 0; i < SLOTS_COUNT && remaining > 0; i++) {
            ItemStack slot = this.items.get(i);
            if (slot.isEmpty()) {
                int toAdd = Math.min(64, remaining);
                this.items.set(i, stack.copyWithCount(toAdd));
                remaining -= toAdd;
            }
        }

        int inserted = initialCount - remaining;
        if (inserted > 0) {
            stack.shrink(inserted);
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return inserted;
    }

    /**
     * Extracts 1 plushie from the first non-empty slot.
     */
    public ItemStack extractPlushie() {
        for (int i = 0; i < SLOTS_COUNT; i++) {
            ItemStack slot = this.items.get(i);
            if (!slot.isEmpty()) {
                ItemStack result = slot.split(1);
                if (slot.isEmpty()) {
                    this.items.set(i, ItemStack.EMPTY);
                }
                setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Rolls a random plushie from among the available non-empty slots.
     */
    public ItemStack rollRandomPlushie() {
        List<Integer> validSlots = new ArrayList<>();
        for (int i = 0; i < SLOTS_COUNT; i++) {
            if (!this.items.get(i).isEmpty()) {
                validSlots.add(i);
            }
        }
        if (validSlots.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int chosenSlot = validSlots.get(RANDOM.nextInt(validSlots.size()));
        ItemStack slot = this.items.get(chosenSlot);
        ItemStack result = slot.split(1);
        if (slot.isEmpty()) {
            this.items.set(chosenSlot, ItemStack.EMPTY);
        }
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public List<ItemStack> getSlotsList() {
        List<ItemStack> list = new ArrayList<>(SLOTS_COUNT);
        for (int i = 0; i < SLOTS_COUNT; i++) {
            list.add(this.items.get(i).copy());
        }
        return list;
    }

    public void dropInventory() {
        if (this.level != null && !this.level.isClientSide()) {
            for (ItemStack st : this.items) {
                if (!st.isEmpty()) {
                    Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), st);
                }
            }
            this.clearContent();
        }
    }

    @Override
    public void clearContent() {
        this.items.clear();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        if (this.ownerUuid != null) {
            tag.putUUID("OwnerUuid", this.ownerUuid);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
        if (tag.hasUUID("OwnerUuid")) {
            this.ownerUuid = tag.getUUID("OwnerUuid");
        } else {
            this.ownerUuid = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

