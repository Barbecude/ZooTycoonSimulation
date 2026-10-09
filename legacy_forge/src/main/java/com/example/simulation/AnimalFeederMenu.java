package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AnimalFeederMenu extends AbstractContainerMenu {
    private final Container feederInventory;
    @Nullable
    private final AnimalFeederBlockEntity feeder;
    private final ContainerLevelAccess access;

    private record ClientInitData(Container inventory, @Nullable AnimalFeederBlockEntity feeder) {}

    public AnimalFeederMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, buf.readBlockPos());
    }

    private AnimalFeederMenu(int id, Inventory playerInventory, BlockPos pos) {
        this(id, playerInventory, resolveClientData(playerInventory, pos), ContainerLevelAccess.create(playerInventory.player.level(), pos));
    }

    public AnimalFeederMenu(int id, Inventory playerInventory, AnimalFeederBlockEntity feeder) {
        this(id, playerInventory, new ClientInitData(feeder, feeder), ContainerLevelAccess.create(feeder.getLevel(), feeder.getBlockPos()));
    }

    private AnimalFeederMenu(int id, Inventory playerInventory, ClientInitData initData, ContainerLevelAccess access) {
        super(IndoZooTycoon.ANIMAL_FEEDER_MENU.get(), id);
        this.feederInventory = initData.inventory();
        this.feeder = initData.feeder();
        this.access = access;

        checkContainerSize(this.feederInventory, 1);
        this.feederInventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(this.feederInventory, 0, 80, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (stack.isEmpty()) return false;
                if (AnimalFeederMenu.this.feeder == null) return true;
                return AnimalFeederMenu.this.feeder.canAcceptFood(stack.getItem(), "", stack.getCount());
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        drawPlayerInventory(playerInventory);
        drawHotbar(playerInventory);
    }

    private static ClientInitData resolveClientData(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof AnimalFeederBlockEntity feederEntity) {
            return new ClientInitData(feederEntity, feederEntity);
        }
        return new ClientInitData(new SimpleContainer(1), null);
    }

    private void drawPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void drawHotbar(Inventory playerInventory) {
        for (int row = 0; row < 9; ++row) {
            this.addSlot(new Slot(playerInventory, row, 8 + row * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (feeder != null) return feeder.stillValid(player);
        return stillValid(access, player, IndoZooTycoon.ANIMAL_FEEDER_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getItem();
        newStack = originalStack.copy();

        int feederSize = this.feederInventory.getContainerSize();
        if (invSlot < feederSize) {
            if (!this.moveItemStackTo(originalStack, feederSize, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(originalStack, 0, feederSize, false)) {
            return ItemStack.EMPTY;
        }

        if (originalStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return newStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.feederInventory.stopOpen(player);
    }

    public String getAssignedAnimalId() {
        return feeder != null ? feeder.getAssignedAnimalId() : "";
    }

    public int getFoodCount() {
        if (feeder != null) return feeder.getFoodCount();
        ItemStack slot = feederInventory.getItem(0);
        return slot.isEmpty() ? 0 : slot.getCount();
    }

    public String getDisplayFoodName() {
        ItemStack stack = feeder != null ? feeder.getDisplayFood() : feederInventory.getItem(0);
        if (stack.isEmpty()) return "-";
        return stack.getHoverName().getString();
    }
}
