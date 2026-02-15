package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class ZooComputerMenu extends AbstractContainerMenu {

    private final ContainerData data;
    private final BlockPos blockPos;

    public static final int DATA_BAL_HI = 0;
    public static final int DATA_BAL_LO = 1;
    public static final int DATA_ANIMALS = 2;
    public static final int DATA_STAFF = 3;
    public static final int DATA_VISITORS = 4;
    public static final int DATA_TRASH = 5;
    public static final int DATA_RADIUS = 6;
    // 0-1: Balance, 2: Animals, 3: Staff, 4: Visitors, 5: Trash, 6: Radius, 7:
    // Ticket, 8: Marketing
    private static final int DATA_COUNT = 9;

    // Server constructor
    public ZooComputerMenu(int id, Inventory inv, BlockPos pos, ContainerData data) {
        super(IndoZooTycoon.ZOO_COMPUTER_MENU.get(), id);
        this.blockPos = pos;
        this.data = data;
        addDataSlots(data);
    }

    // Client constructor (from network)
    public ZooComputerMenu(int id, Inventory inv, net.minecraft.network.FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos(), new net.minecraft.world.inventory.SimpleContainerData(DATA_COUNT));
    }

    // Client constructor (local)
    public ZooComputerMenu(int id, Inventory inv) {
        this(id, inv, BlockPos.ZERO, new net.minecraft.world.inventory.SimpleContainerData(DATA_COUNT));
    }

    public int getTicketPrice() {
        return this.data.get(7);
    }

    public int getMarketingLevel() {
        return this.data.get(8);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(blockPos.getX() + 0.5,
                blockPos.getY() + 0.5, blockPos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 99) {
            // Manual Refresh
            if (player.level().getBlockEntity(blockPos) instanceof ZooComputerBlockEntity be) {
                be.forceUpdate();
                return true;
            }
        }
        return super.clickMenuButton(player, id);
    }

    public int getBalance() {
        int hi = data.get(DATA_BAL_HI) & 0xFFFF;
        int lo = data.get(DATA_BAL_LO) & 0xFFFF;
        return (hi << 16) | lo;
    }

    public int getAnimalCount() {
        return data.get(DATA_ANIMALS);
    }

    public int getStaffCount() {
        return data.get(DATA_STAFF);
    }

    public int getVisitorCount() {
        return data.get(DATA_VISITORS);
    }

    public int getTrashCount() {
        return data.get(DATA_TRASH);
    }

    public int getScanRadius() {
        return data.get(DATA_RADIUS);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
