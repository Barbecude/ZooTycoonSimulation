package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class AnimalFeederBlockEntity extends BlockEntity implements Container, net.minecraft.world.MenuProvider {
    private static final int MAX_FOOD_COUNT = 64;

    private ItemStack storedFood = ItemStack.EMPTY;
    private String assignedAnimalId = "";

    public AnimalFeederBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.ANIMAL_FEEDER_BE.get(), pos, state);
    }

    public int getFoodCount() {
        return storedFood.isEmpty() ? 0 : storedFood.getCount();
    }

    public int getFoodLevel() {
        return (int) Math.round((getFoodCount() * 100.0D) / MAX_FOOD_COUNT);
    }

    public boolean canAcceptFood(Item item, String targetAnimalId, int amount) {
        if (item == null || item == Items.AIR || amount <= 0) return false;
        if (!FoodAnimalRegistry.isFoodRegistered(item)) return false;

        String requestedAnimal = targetAnimalId == null ? "" : targetAnimalId;

        if (!requestedAnimal.isEmpty() && !FoodAnimalRegistry.isValidFoodForAnimal(requestedAnimal, item)) {
            return false;
        }

        if (!storedFood.isEmpty() && storedFood.getItem() != item) {
            return false;
        }

        return getFoodCount() < MAX_FOOD_COUNT;
    }

    public boolean addFood(ItemStack stack, int amount, String targetAnimalId) {
        if (stack.isEmpty() || amount <= 0) return false;
        Item item = stack.getItem();
        String requestedAnimal = targetAnimalId == null ? "" : targetAnimalId;

        if (!canAcceptFood(item, requestedAnimal, amount)) return false;

        if (storedFood.isEmpty()) {
            storedFood = new ItemStack(item, 0);
        }

        int add = Math.min(amount, MAX_FOOD_COUNT - getFoodCount());
        if (add <= 0) return false;
        storedFood.grow(add);

        syncState();
        return true;
    }

    public ItemStack getDisplayFood() {
        if (storedFood.isEmpty()) return ItemStack.EMPTY;
        ItemStack display = storedFood.copy();
        display.setCount(1);
        return display;
    }

    public String getAssignedAnimalId() {
        return assignedAnimalId;
    }

    public void setAssignedAnimalId(String animalId) {
        assignedAnimalId = animalId == null ? "" : animalId;
        syncState();
    }

    public boolean isFull() {
        return getFoodCount() >= MAX_FOOD_COUNT;
    }

    public boolean isEmpty() {
        return getFoodCount() <= 0;
    }

    public Item getStoredFoodItem() {
        return storedFood.isEmpty() ? Items.AIR : storedFood.getItem();
    }

    public boolean hasFoodForAnimal(String animalId) {
        if (storedFood.isEmpty()) return false;
        String id = animalId == null ? "" : animalId;
        if (id.isEmpty()) return false;
        return FoodAnimalRegistry.isValidFoodForAnimal(id, storedFood.getItem());
    }

    public boolean consumeOneFoodForAnimal(String animalId) {
        if (!hasFoodForAnimal(animalId)) return false;
        storedFood.shrink(1);
        if (storedFood.isEmpty()) {
            storedFood = ItemStack.EMPTY;
        }
        syncState();
        return true;
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, AnimalFeederBlockEntity feeder) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getGameTime() % 20 != 0) return;
        feeder.tickHungryAnimals(serverLevel, pos);
    }

    private void tickHungryAnimals(ServerLevel level, BlockPos pos) {
        if (isEmpty() || storedFood.isEmpty()) return;

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        java.util.List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos).inflate(8.0D, 3.0D, 8.0D),
                living -> ZooAnimalHungerSystem.isHungry(level, living)
                        && ZooAnimalHungerSystem.canEatFood(level, living, storedFood.getItem()));

        nearby.sort(java.util.Comparator.comparingDouble(living -> living.distanceToSqr(centerX, centerY, centerZ)));

        for (LivingEntity living : nearby) {
            if (isEmpty() || storedFood.isEmpty()) return;
            if (!(living.level() instanceof ServerLevel serverLevel)) continue;

            if (living instanceof PathfinderMob mob && living.distanceToSqr(centerX, centerY, centerZ) > 4.0D) {
                mob.getNavigation().moveTo(centerX, centerY, centerZ, 1.0D);
                continue;
            }

            if (living.distanceToSqr(centerX, centerY, centerZ) > 4.0D) continue;

            String animalId = ZooAnimalHungerSystem.getTaggedAnimalType(serverLevel, living);
            if (animalId.isEmpty()) continue;
            Item food = getStoredFoodItem();
            if (food == Items.AIR) continue;
            if (!hasFoodForAnimal(animalId)) continue;
            if (!ZooAnimalHungerSystem.feedTaggedAnimal(serverLevel, living, food, null)) continue;
            consumeOneFoodForAnimal(animalId);
        }
    }

    private void syncState() {
        updateVisualLevel();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void updateVisualLevel() {
        if (level == null) return;
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(AnimalFeederBlock.LEVEL)) return;

        int count = getFoodCount();
        int visualLevel = 0;
        if (count > 0) {
            visualLevel = Math.min(4, (int) Math.floor(count / 16.0D) + 1);
        }

        if (state.getValue(AnimalFeederBlock.LEVEL) != visualLevel) {
            level.setBlock(worldPosition, state.setValue(AnimalFeederBlock.LEVEL, visualLevel), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FoodCount", getFoodCount());
        if (!storedFood.isEmpty()) {
            tag.put("StoredFood", storedFood.save(new CompoundTag()));
        }
        if (!assignedAnimalId.isEmpty()) {
            tag.putString("AssignedAnimalId", assignedAnimalId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("StoredFood")) {
            storedFood = ItemStack.of(tag.getCompound("StoredFood"));
        } else if (tag.contains("DisplayFood")) {
            // Legacy compatibility from old feeder format.
            storedFood = ItemStack.of(tag.getCompound("DisplayFood"));
        } else {
            storedFood = ItemStack.EMPTY;
        }

        int count = tag.contains("FoodCount") ? tag.getInt("FoodCount") : -1;
        if (count < 0 && tag.contains("FoodLevel")) {
            // Legacy compatibility: convert 0-100 to 0-64.
            count = (int) Math.ceil(tag.getInt("FoodLevel") * (MAX_FOOD_COUNT / 100.0D));
        }
        count = Math.max(0, Math.min(MAX_FOOD_COUNT, count));

        if (!storedFood.isEmpty()) {
            storedFood.setCount(count > 0 ? count : Math.max(1, storedFood.getCount()));
        } else if (count > 0) {
            storedFood = new ItemStack(Items.HAY_BLOCK, count);
        }

        assignedAnimalId = tag.getString("AssignedAnimalId");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateVisualLevel();
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
        return new AnimalFeederMenu(syncId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? storedFood : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || storedFood.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack result = storedFood.split(amount);
        if (storedFood.isEmpty()) {
            storedFood = ItemStack.EMPTY;
        }
        syncState();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack old = storedFood;
        storedFood = ItemStack.EMPTY;
        syncState();
        return old;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) return;

        if (stack.isEmpty()) {
            storedFood = ItemStack.EMPTY;
            syncState();
            return;
        }

        ItemStack copy = stack.copy();
        copy.setCount(Math.min(copy.getCount(), getMaxStackSize()));
        storedFood = copy;
        syncState();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot != 0 || stack.isEmpty()) return false;
        return canAcceptFood(stack.getItem(), "", stack.getCount());
    }

    @Override
    public void clearContent() {
        storedFood = ItemStack.EMPTY;
        syncState();
    }

    @Override
    public int getMaxStackSize() {
        return MAX_FOOD_COUNT;
    }
}
