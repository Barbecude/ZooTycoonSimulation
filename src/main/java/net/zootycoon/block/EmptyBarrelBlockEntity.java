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
import net.minecraft.world.level.block.state.properties.ChestType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.zootycoon.network.WarehousePayloads;
import net.zootycoon.warehouse.WarehouseSupplyManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EmptyBarrelBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int CONTAINER_SIZE = 27;

    public static final String CAT_UNASSIGNED = "UNASSIGNED";
    public static final String CAT_BURGER = "BURGER";
    public static final String CAT_DRINK = "DRINK";
    public static final String CAT_ICE_CREAM = "ICE_CREAM";
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

    // ==========================================
    // DOUBLE BARREL
    // Two joined halves act as one barrel: slots 0-26 belong to the LEFT half, 27-53 to the RIGHT
    // half, and the LEFT half (the primary) owns the request logic, viewers and hologram.
    // ==========================================

    /** The joined other half, or null for a single barrel. */
    public @Nullable EmptyBarrelBlockEntity partner() {
        if (this.level == null || this.remove) return null;
        BlockState state = getBlockState();
        if (!state.hasProperty(EmptyBarrelBlock.TYPE) || state.getValue(EmptyBarrelBlock.TYPE) == ChestType.SINGLE) {
            return null;
        }
        BlockPos otherPos = this.worldPosition.relative(EmptyBarrelBlock.getConnectedDirection(state));
        if (!this.level.isLoaded(otherPos)) return null;
        BlockEntity other = this.level.getBlockEntity(otherPos);
        if (other instanceof EmptyBarrelBlockEntity barrel && barrel != this && !barrel.isRemoved()
                && barrel.connectsTo(this.worldPosition)) {
            return barrel;
        }
        return null;
    }

    private boolean connectsTo(BlockPos pos) {
        BlockState state = getBlockState();
        return state.hasProperty(EmptyBarrelBlock.TYPE)
                && state.getValue(EmptyBarrelBlock.TYPE) != ChestType.SINGLE
                && this.worldPosition.relative(EmptyBarrelBlock.getConnectedDirection(state)).equals(pos);
    }

    private boolean isLeftHalf() {
        BlockState state = getBlockState();
        return !state.hasProperty(EmptyBarrelBlock.TYPE) || state.getValue(EmptyBarrelBlock.TYPE) != ChestType.RIGHT;
    }

    /** The half that runs the shared logic: the LEFT half of a double barrel, otherwise this one. */
    public EmptyBarrelBlockEntity primary() {
        EmptyBarrelBlockEntity other = partner();
        return other != null && !isLeftHalf() ? other : this;
    }

    public boolean isSecondaryHalf() {
        return primary() != this;
    }

    /** Only the slots stored in this half, used when the block is broken. */
    public NonNullList<ItemStack> getOwnItems() {
        return this.items;
    }

    private boolean ownCategoryAssigned() {
        return category != null && !category.isEmpty() && !CAT_UNASSIGNED.equalsIgnoreCase(category);
    }

    public String getCategory() {
        EmptyBarrelBlockEntity other = partner();
        if (other != null) {
            // The primary half's category wins; fall back to the other half if only it is assigned
            EmptyBarrelBlockEntity first = isLeftHalf() ? this : other;
            EmptyBarrelBlockEntity second = first == this ? other : this;
            if (first.ownCategoryAssigned()) return first.category;
            if (second.ownCategoryAssigned()) return second.category;
        }
        return category != null ? category : CAT_UNASSIGNED;
    }

    public boolean isCategoryAssigned() {
        String cat = getCategory();
        return !cat.isEmpty() && !CAT_UNASSIGNED.equalsIgnoreCase(cat);
    }

    public void setCategoryAndOwner(String newCategory, UUID owner) {
        applyCategoryAndOwner(newCategory, owner);
        EmptyBarrelBlockEntity other = partner();
        if (other != null) {
            other.applyCategoryAndOwner(newCategory, owner);
        }
    }

    private void applyCategoryAndOwner(String newCategory, UUID owner) {
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
        if (ownerUuid == null) {
            EmptyBarrelBlockEntity other = partner();
            if (other != null) return other.ownerUuid;
        }
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        setChanged();
    }

    /** Copies the shared category and owner onto both halves so either keeps them after a split. */
    private void syncHalves() {
        EmptyBarrelBlockEntity other = partner();
        if (other == null) return;
        String shared = getCategory();
        UUID owner = getOwnerUuid();
        for (EmptyBarrelBlockEntity half : new EmptyBarrelBlockEntity[]{this, other}) {
            boolean changed = false;
            if (!shared.equalsIgnoreCase(half.category)) {
                half.category = shared;
                changed = true;
            }
            if (owner != null && !owner.equals(half.ownerUuid)) {
                half.ownerUuid = owner;
                changed = true;
            }
            if (changed) {
                half.markDirtyOnly();
                if (half.level != null) {
                    half.level.sendBlockUpdated(half.worldPosition, half.getBlockState(), half.getBlockState(), 3);
                }
            }
        }
    }

    private void markDirtyOnly() {
        super.setChanged();
    }

    /** Resolves a combined slot index to the half that stores it. */
    private EmptyBarrelBlockEntity halfForSlot(int slot) {
        EmptyBarrelBlockEntity other = partner();
        if (other == null) return this;
        EmptyBarrelBlockEntity left = isLeftHalf() ? this : other;
        EmptyBarrelBlockEntity right = left == this ? other : this;
        return slot < CONTAINER_SIZE ? left : right;
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

        // The secondary half of a double barrel is driven by its primary half
        if (be.isSecondaryHalf()) return;
        if (sLevel.getGameTime() % 20 == 0) {
            be.syncHalves();
        }

        if (!be.isCategoryAssigned()) return;

        // Evaluate and process staff supply requests every 5 ticks (0.25s)
        if (sLevel.getGameTime() % 5 == 0) {
            WarehouseSupplyManager.tickBarrel(sLevel, be);
        }

        // Sync active requests to any player currently viewing this barrel every 10 ticks
        if (!be.viewingPlayers.isEmpty() && sLevel.getGameTime() % 10 == 0) {
            be.syncToViewingPlayers(sLevel);
        }

        // Broadcast request progress for the world hologram once per second, staggered per barrel
        if ((sLevel.getGameTime() + (pos.asLong() & 0xF)) % 20 == 0) {
            be.broadcastHologram(sLevel);
        }
    }

    private static final double HOLOGRAM_SYNC_RANGE_SQ = 24.0D * 24.0D;

    private void broadcastHologram(ServerLevel sLevel) {
        List<ServerPlayer> nearby = new ArrayList<>();
        for (ServerPlayer sp : sLevel.players()) {
            if (sp.distanceToSqr(this.worldPosition.getCenter()) <= HOLOGRAM_SYNC_RANGE_SQ) {
                nearby.add(sp);
            }
        }
        if (nearby.isEmpty()) return;
        // An empty list is sent too, so the hologram disappears as soon as requests are fulfilled
        var payload = new WarehousePayloads.BarrelHologramPayload(this.worldPosition, getCategory(),
                WarehouseSupplyManager.buildRequestDtos(sLevel, this));
        for (ServerPlayer sp : nearby) {
            ServerPlayNetworking.send(sp, payload);
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
            case CAT_ICE_CREAM -> Component.translatable("zootycoon.warehouse.barrel.title.ice_cream");
            case CAT_ANIMAL_FOOD -> Component.translatable("zootycoon.warehouse.barrel.title.animal_food");
            case CAT_ANIMAL_CARE -> Component.translatable("zootycoon.warehouse.barrel.title.animal_care");
            default -> Component.translatable("block.zootycoon.empty_barrel");
        };
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return getContainerSize() > CONTAINER_SIZE
                ? ChestMenu.sixRows(syncId, playerInventory, this)
                : ChestMenu.threeRows(syncId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return partner() != null ? CONTAINER_SIZE * 2 : CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;
        return halfForSlot(slot).items.get(slot % CONTAINER_SIZE);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;
        EmptyBarrelBlockEntity half = halfForSlot(slot);
        ItemStack result = ContainerHelper.removeItem(half.items, slot % CONTAINER_SIZE, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;
        return ContainerHelper.takeItem(halfForSlot(slot).items, slot % CONTAINER_SIZE);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= getContainerSize()) return;
        halfForSlot(slot).items.set(slot % CONTAINER_SIZE, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        EmptyBarrelBlockEntity other = partner();
        if (other != null) {
            // Items may live in either half, so both get saved
            other.markDirtyOnly();
        }
        EmptyBarrelBlockEntity main = other != null ? primary() : this;
        if (this.level instanceof ServerLevel sLevel && main.isCategoryAssigned()) {
            WarehouseSupplyManager.tickBarrel(sLevel, main);
            main.syncToViewingPlayers(sLevel);
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
        tag.putString("BarrelCategory", category != null ? category : CAT_UNASSIGNED);
        if (ownerUuid != null) {
            tag.putUUID("OwnerUuid", ownerUuid);
        }
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
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

