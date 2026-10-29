package net.zootycoon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zootycoon.warehouse.WarehouseSupplyManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class EmptyBarrelBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int CONTAINER_SIZE = 27;

    public static final String CAT_UNASSIGNED = "UNASSIGNED";
    public static final String CAT_BURGER = "BURGER";
    public static final String CAT_DRINK = "DRINK";
    public static final String CAT_ANIMAL_FOOD = "ANIMAL_FOOD";
    public static final String CAT_ANIMAL_CARE = "ANIMAL_CARE";

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private String category = CAT_UNASSIGNED;
    private UUID ownerUuid = null;
    private final Set<UUID> viewingPlayers = new HashSet<>();

    public EmptyBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ZooBlocks.EMPTY_BARREL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level != null && !level.isClientSide()) {
            EmptyBarrelBlock.KNOWN_BARRELS.add(this.worldPosition.immutable());
        }
    }

    public String getCategory() {
        return category != null ? category : CAT_UNASSIGNED;
    }

    public boolean isCategoryAssigned() {
        return category != null && !category.isEmpty() && !CAT_UNASSIGNED.equalsIgnoreCase(category);
    }

    public void setCategoryAndOwner(String newCategory, UUID owner) {
        if (newCategory != null && !newCategory.isEmpty()) {
            this.category = newCategory.toUpperCase();
        }
        if (owner != null) {
            this.ownerUuid = owner;
        }
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        setChanged();
    }

    public void openChestForPlayer(ServerPlayer sp, ServerLevel sLevel) {
        sLevel.playSound(null, this.worldPosition, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.85F, 1.0F);
        viewingPlayers.add(sp.getUUID());
        sp.openMenu(this);
        WarehouseSupplyManager.syncRequestsToPlayer(sLevel, this, sp);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EmptyBarrelBlockEntity be) {
        if (!(level instanceof ServerLevel sLevel)) return;
        EmptyBarrelBlock.KNOWN_BARRELS.add(pos.immutable());

        if (!be.isCategoryAssigned()) return;

        // Evaluate and process staff supply requests every 5 ticks (0.25s)
        if (sLevel.getGameTime() % 5 == 0) {
            WarehouseSupplyManager.tickBarrel(sLevel, be);
        }

        // Sync active requests to any player currently viewing this barrel every 10 ticks
        if (!be.viewingPlayers.isEmpty() && sLevel.getGameTime() % 10 == 0) {
            be.syncToViewingPlayers(sLevel);
        }
    }

    public void syncToViewingPlayers(ServerLevel sLevel) {
        Iterator<UUID> it = viewingPlayers.iterator();
        while (it.hasNext()) {
            UUID id = it.next();
            ServerPlayer sp = sLevel.getServer().getPlayerList().getPlayer(id);
            if (sp == null || !(sp.containerMenu instanceof ChestMenu chestMenu) || chestMenu.getContainer() != this) {
                it.remove();
            } else {
                WarehouseSupplyManager.syncRequestsToPlayer(sLevel, this, sp);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return switch (getCategory()) {
            case CAT_BURGER -> Component.translatable("zootycoon.warehouse.barrel.title.burger");
            case CAT_DRINK -> Component.translatable("zootycoon.warehouse.barrel.title.drink");
            case CAT_ANIMAL_FOOD -> Component.translatable("zootycoon.warehouse.barrel.title.animal_food");
            case CAT_ANIMAL_CARE -> Component.translatable("zootycoon.warehouse.barrel.title.animal_care");
            default -> Component.translatable("block.zootycoon.empty_barrel");
        };
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return ChestMenu.threeRows(syncId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= this.items.size()) return ItemStack.EMPTY;
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.items.size()) return;
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level instanceof ServerLevel sLevel && isCategoryAssigned()) {
            WarehouseSupplyManager.tickBarrel(sLevel, this);
            syncToViewingPlayers(sLevel);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator() && this.level != null && !this.level.isClientSide()) {
            viewingPlayers.remove(player.getUUID());
            this.level.playSound(null, this.worldPosition, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.75F, 1.0F);
        }
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("BarrelCategory", getCategory());
        if (ownerUuid != null) {
            tag.putUUID("OwnerUuid", ownerUuid);
        }
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        if (tag.contains("BarrelCategory")) {
            this.category = tag.getString("BarrelCategory");
        }
        if (tag.hasUUID("OwnerUuid")) {
            this.ownerUuid = tag.getUUID("OwnerUuid");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

