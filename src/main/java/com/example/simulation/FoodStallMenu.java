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

/**
 * Container menu for the Food Stall.
 * Layout: 3×3 stall inventory (slots 0-8) + player inventory (9-35) + hotbar (36-44).
 */
public class FoodStallMenu extends AbstractContainerMenu {

    private final Container stallInventory;
    @Nullable
    private final FoodStallBlockEntity stall;
    private final ContainerLevelAccess access;

    private record ClientInitData(Container inventory, @Nullable FoodStallBlockEntity stall) {}

    // ── Network constructor (client-side, BlockPos lookup) ──────────────────
    public FoodStallMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, buf.readBlockPos());
    }

    private FoodStallMenu(int id, Inventory playerInventory, BlockPos pos) {
        this(id, playerInventory, resolveClientData(playerInventory, pos),
                ContainerLevelAccess.create(playerInventory.player.level(), pos));
    }

    // ── Server constructor (opened via NetworkHooks.openScreen) ─────────────
    public FoodStallMenu(int id, Inventory playerInventory, FoodStallBlockEntity stall) {
        this(id, playerInventory, new ClientInitData(stall, stall),
                ContainerLevelAccess.create(stall.getLevel(), stall.getBlockPos()));
    }

    private FoodStallMenu(int id, Inventory playerInventory, ClientInitData initData, ContainerLevelAccess access) {
        super(IndoZooTycoon.FOOD_STALL_MENU.get(), id);
        this.stallInventory = initData.inventory();
        this.stall = initData.stall();
        this.access = access;

        checkContainerSize(this.stallInventory, 9);
        this.stallInventory.startOpen(playerInventory.player);

        // 3×3 stall inventory grid (centered, slot indices 0-8)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(this.stallInventory, col + row * 3,
                        62 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory (slots 9-35)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 84 + row * 18));
            }
        }

        // Hot-bar (slots 36-44)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private static ClientInitData resolveClientData(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof FoodStallBlockEntity stallEntity) {
            return new ClientInitData(stallEntity, stallEntity);
        }
        return new ClientInitData(new SimpleContainer(9), null);
    }

    // ── Getters for screen rendering ────────────────────────────────────────

    public int getStock() {
        return stall != null ? stall.getFoodStock() : 0;
    }

    public int getPrice() {
        return stall != null ? stall.getFoodPrice() : 8_000;
    }

    public String getDisplayItemName() {
        if (stall == null) return "-";
        ItemStack display = stall.getDisplayFood();
        return display.isEmpty() ? "Kosong" : display.getHoverName().getString();
    }

    public long getRevenue() {
        return stall != null ? stall.getRevenue() : 0;
    }

    public int getQueueSize() {
        return stall != null ? stall.getQueueSize() : 0;
    }

    public int getItemPrice(String itemId) {
        return stall != null ? stall.getItemPrice(itemId) : 0;
    }

    public java.util.Map<String, Integer> getItemPrices() {
        return stall != null ? stall.getItemPrices() : java.util.Collections.emptyMap();
    }

    @Nullable
    public BlockPos getStallPos() {
        return stall != null ? stall.getBlockPos() : null;
    }

    // ── Menu contract ───────────────────────────────────────────────────────

    @Override
    public boolean stillValid(Player player) {
        if (stall != null) return stall.stillValid(player);
        return stillValid(access, player, IndoZooTycoon.FOOD_STALL_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getItem();
        newStack = originalStack.copy();

        int stallSize = this.stallInventory.getContainerSize(); // 9
        if (invSlot < stallSize) {
            if (!this.moveItemStackTo(originalStack, stallSize, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(originalStack, 0, stallSize, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (originalStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return newStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.stallInventory.stopOpen(player);
    }
}
