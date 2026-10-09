package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/**
 * Block entity for the Food Stall.
 * Stores up to 9 inventory slots of food items.
 * Tracks a per-unit sell price, lifetime revenue, and a visitor purchase queue.
 * Visitors queue here and wait for the player to confirm serving them.
 */
public class FoodStallBlockEntity extends BlockEntity implements Container, net.minecraft.world.MenuProvider {

    private static final int CONTAINER_SIZE = 9;
    /** Max ticks a visitor waits in queue before giving up. */
    public static final int QUEUE_TIMEOUT_TICKS = 600; // 30 s

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int foodPrice = 8_000;
    private long revenue = 0;

    // ── Per-item pricing ────────────────────────────────────────────────
    /** Per-food-item price map (itemId → Rp). */
    private final Map<String, Integer> itemPrices = new HashMap<>();
    static {
        // Default prices populated in constructor
    }
    public static final Map<String, Integer> DEFAULT_PRICES = Map.of(
            "indozoo:fd_hamburger", 8500,
            "indozoo:fd_chicken_sandwich", 7000,
            "indozoo:fd_hot_cocoa", 5000
    );

    // ── Visitor queue ────────────────────────────────────────────────────
    /** Ordered list of visitor UUIDs waiting to be served. */
    private final LinkedList<UUID> visitorQueue = new LinkedList<>();
    /** Visitors that have been served but whose AI hasn't been notified yet. */
    private final Set<UUID> servedVisitors = new HashSet<>();
    /** Per-visitor food requests (UUID -> itemId). */
    private final Map<UUID, String> visitorRequests = new HashMap<>();

    public FoodStallBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.FOOD_STALL_BE.get(), pos, state);
        // Initialize default per-item prices
        DEFAULT_PRICES.forEach((k, v) -> itemPrices.putIfAbsent(k, v));
    }

    // ── Per-item price API ───────────────────────────────────────────────

    public int getItemPrice(String itemId) {
        return itemPrices.getOrDefault(itemId, foodPrice);
    }

    public void setItemPrice(String itemId, int price) {
        itemPrices.put(itemId, Math.max(0, price));
        setChanged();
        syncToClient();
    }

    public Map<String, Integer> getItemPrices() {
        return itemPrices;
    }

    // ── Queue API ────────────────────────────────────────────────────────

    public synchronized boolean addToQueue(UUID uuid) {
        return addToQueue(uuid, "");
    }

    public synchronized boolean addToQueue(UUID uuid, String requestItemId) {
        if (visitorQueue.contains(uuid)) return false;
        visitorQueue.addLast(uuid);
        visitorRequests.put(uuid, requestItemId == null ? "" : requestItemId);
        return true;
    }

    public synchronized void removeFromQueue(UUID uuid) {
        visitorQueue.remove(uuid);
        visitorRequests.remove(uuid);
    }

    public synchronized int getQueueSize() { return visitorQueue.size(); }

    public synchronized boolean isInQueue(UUID uuid) { return visitorQueue.contains(uuid); }

    /** Pop and mark the first visitor as served. Returns null if queue/stock empty. */
    public synchronized UUID serveNext() {
        if (visitorQueue.isEmpty() || isStockEmpty()) return null;
        UUID uuid = visitorQueue.removeFirst();
        visitorRequests.remove(uuid);
        servedVisitors.add(uuid);
        return uuid;
    }

    /** Serve all queued visitors (up to stock count). Returns number served. */
    public synchronized int serveAll() {
        int count = 0;
        while (!visitorQueue.isEmpty() && !isStockEmpty()) {
            UUID uuid = visitorQueue.removeFirst();
            visitorRequests.remove(uuid);
            servedVisitors.add(uuid);
            count++;
        }
        return count;
    }

    /** Check if visitor was served (called from VisitorEntity AI). */
    public synchronized boolean wasServed(UUID uuid) { return servedVisitors.contains(uuid); }

    /** Called by VisitorEntity AI after processing the served state. */
    public synchronized void acknowledgeServed(UUID uuid) { servedVisitors.remove(uuid); }

    /** Returns the requested itemId for the next queued visitor, or empty if none. */
    public synchronized String getNextRequestItemId() {
        if (visitorQueue.isEmpty()) return "";
        UUID next = visitorQueue.getFirst();
        return visitorRequests.getOrDefault(next, "");
    }

    /** Returns the requested itemId for a specific visitor UUID. */
    public synchronized String getRequestForVisitor(UUID uuid) {
        return visitorRequests.getOrDefault(uuid, "");
    }

    /**
     * Pick a random stocked item id using smart market weighting.
     * Higher-priced items are requested less frequently.
     */
    public String pickRandomStockedItemId(RandomSource random) {
        if (random == null) return "";
        java.util.List<String> candidates = new java.util.ArrayList<>();
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            String id = Objects.toString(ForgeRegistries.ITEMS.getKey(stack.getItem()), "");
            if (!id.isEmpty() && !candidates.contains(id)) {
                candidates.add(id);
            }
        }
        if (candidates.isEmpty()) return "";
        if (candidates.size() == 1) return candidates.get(0);
        // Weighted random: weight = 10000 / price (cheaper → more requests)
        double totalWeight = 0;
        double[] weights = new double[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            int price = getItemPrice(candidates.get(i));
            weights[i] = (price > 0) ? 10000.0 / price : 1.0;
            totalWeight += weights[i];
        }
        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights[i];
            if (roll < cumulative) return candidates.get(i);
        }
        return candidates.get(candidates.size() - 1);
    }

    /**
     * Remove one unit of a SPECIFIC food item from inventory.
     * Returns true if the item was found and removed.
     */
    public boolean removeSpecificFood(String itemId) {
        if (itemId == null || itemId.isBlank()) return removeOneFood();
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(itemId);
        if (rl == null) return removeOneFood();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            String id = Objects.toString(ForgeRegistries.ITEMS.getKey(stack.getItem()), "");
            if (id.equals(itemId)) {
                stack.shrink(1);
                if (stack.isEmpty()) items.set(i, ItemStack.EMPTY);
                setChanged();
                syncToClient();
                return true;
            }
        }
        // Fallback: remove any food if specific not found
        return removeOneFood();
    }

    // ──────────────────────────────────────────────────────────────────────
    // Stock helpers
    // ──────────────────────────────────────────────────────────────────────

    /** Total number of items currently stocked (all slots combined). */
    public int getFoodStock() {
        int total = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) total += stack.getCount();
        }
        return total;
    }

    public boolean isStockEmpty() {
        return getFoodStock() <= 0;
    }

    /** First non-empty item (for display / visitor hand animation). */
    public ItemStack getDisplayFood() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                ItemStack d = stack.copy();
                d.setCount(1);
                return d;
            }
        }
        return ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> getAllItems() {
        return items;
    }

    /**
     * Remove one food item from the first non-empty slot.
     * Called by visitor purchase logic.
     */
    public boolean removeOneFood() {
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) items.set(i, ItemStack.EMPTY);
                setChanged();
                syncToClient();
                return true;
            }
        }
        return false;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Price & revenue
    // ──────────────────────────────────────────────────────────────────────

    public int getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(int price) {
        this.foodPrice = Math.max(0, price);
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

    // ──────────────────────────────────────────────────────────────────────
    // Sync / save
    // ──────────────────────────────────────────────────────────────────────

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("FoodPrice", foodPrice);
        tag.putLong("Revenue", revenue);
        // Per-item prices
        CompoundTag pricesTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : itemPrices.entrySet()) {
            pricesTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("ItemPrices", pricesTag);
        ListTag queueTag = new ListTag();
        for (UUID uuid : visitorQueue) queueTag.add(StringTag.valueOf(uuid.toString()));
        tag.put("VisitorQueue", queueTag);
        ListTag requestTag = new ListTag();
        for (UUID uuid : visitorQueue) {
            CompoundTag req = new CompoundTag();
            req.putUUID("Id", uuid);
            req.putString("Item", visitorRequests.getOrDefault(uuid, ""));
            requestTag.add(req);
        }
        tag.put("VisitorRequests", requestTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        foodPrice = tag.contains("FoodPrice") ? tag.getInt("FoodPrice") : 8_000;
        revenue = tag.getLong("Revenue");
        // Per-item prices
        if (tag.contains("ItemPrices")) {
            CompoundTag pricesTag = tag.getCompound("ItemPrices");
            for (String key : pricesTag.getAllKeys()) {
                itemPrices.put(key, pricesTag.getInt(key));
            }
        }
        DEFAULT_PRICES.forEach((k, v) -> itemPrices.putIfAbsent(k, v));
        visitorQueue.clear();
        if (tag.contains("VisitorQueue")) {
            ListTag queueTag = tag.getList("VisitorQueue", 8);
            for (int i = 0; i < queueTag.size(); i++) {
                try { visitorQueue.addLast(UUID.fromString(queueTag.getString(i))); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        if (tag.contains("VisitorRequests")) {
            ListTag reqTag = tag.getList("VisitorRequests", 10);
            for (int i = 0; i < reqTag.size(); i++) {
                CompoundTag req = reqTag.getCompound(i);
                if (!req.hasUUID("Id")) continue;
                visitorRequests.put(req.getUUID("Id"), req.getString("Item"));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ──────────────────────────────────────────────────────────────────────
    // MenuProvider
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.indozoo.food_stall");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new FoodStallMenu(id, playerInventory, this);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Container interface
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) { setChanged(); syncToClient(); }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
        syncToClient();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }
}
