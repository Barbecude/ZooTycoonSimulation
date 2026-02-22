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

public class ShelfMenu extends AbstractContainerMenu {
    private final Container shelfInventory;
    @Nullable
    private final ShelfBlockEntity shelf;
    private final ContainerLevelAccess access;

    private record ClientInitData(Container inventory, @Nullable ShelfBlockEntity shelf) {}

    public ShelfMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, buf.readBlockPos());
    }

    private ShelfMenu(int id, Inventory playerInventory, BlockPos pos) {
        this(id, playerInventory, resolveClientData(playerInventory, pos), ContainerLevelAccess.create(playerInventory.player.level(), pos));
    }

    public ShelfMenu(int id, Inventory playerInventory, ShelfBlockEntity shelf) {
        this(id, playerInventory, new ClientInitData(shelf, shelf), ContainerLevelAccess.create(shelf.getLevel(), shelf.getBlockPos()));
    }

    private ShelfMenu(int id, Inventory playerInventory, ClientInitData initData, ContainerLevelAccess access) {
        super(IndoZooTycoon.SHELF_MENU.get(), id);
        this.shelfInventory = initData.inventory();
        this.shelf = initData.shelf();
        this.access = access;

        checkContainerSize(this.shelfInventory, 1);
        this.shelfInventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(this.shelfInventory, 0, 80, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ShelfBlockEntity.isValidShelfFood(stack);
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
        if (playerInventory.player.level().getBlockEntity(pos) instanceof ShelfBlockEntity shelfEntity) {
            return new ClientInitData(shelfEntity, shelfEntity);
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
        if (shelf != null) return shelf.stillValid(player);
        return stillValid(access, player, IndoZooTycoon.FOOD_STALL_BLOCK.get())
                || stillValid(access, player, IndoZooTycoon.DRINK_STALL_BLOCK.get())
                || stillValid(access, player, IndoZooTycoon.OAK_SHELF_BLOCK.get())
                || stillValid(access, player, IndoZooTycoon.OAK_STANDING_SHELF_BLOCK.get())
                || stillValid(access, player, IndoZooTycoon.OAK_TOWER_SHELF_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getItem();
        newStack = originalStack.copy();

        int shelfSize = this.shelfInventory.getContainerSize();
        if (invSlot < shelfSize) {
            if (!this.moveItemStackTo(originalStack, shelfSize, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(originalStack, 0, shelfSize, false)) {
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
        this.shelfInventory.stopOpen(player);
    }

    public int getStock() {
        if (shelf != null) return shelf.getFoodStock();
        ItemStack slot = shelfInventory.getItem(0);
        return slot.isEmpty() ? 0 : slot.getCount();
    }

    public int getPrice() {
        return shelf != null ? shelf.getFoodPrice() : 5000;
    }

    public String getDisplayItemName() {
        ItemStack stack = shelf != null ? shelf.getDisplayFood() : shelfInventory.getItem(0);
        if (stack.isEmpty()) return "-";
        return stack.getHoverName().getString();
    }
}
