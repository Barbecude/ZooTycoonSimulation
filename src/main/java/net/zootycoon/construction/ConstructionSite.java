package net.zootycoon.construction;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.economy.EconomyConfig;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.schematic.ZooSchematic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConstructionSite {
    private final UUID siteId;
    private final UUID ownerUuid;
    private final BlockPos origin;
    private final ResourceKey<Level> dimension;
    private final ZooSchematic schematic;
    private final BoundingBox bounds;

    private final Map<Item, Integer> initialRequiredMaterials = new ConcurrentHashMap<>();
    private final Map<Item, Integer> deliveredMaterials = new ConcurrentHashMap<>();
    private final List<StructureTemplate.StructureBlockInfo> pendingBlocks = new ArrayList<>();
    private final Set<BlockPos> tapePositions = new HashSet<>();
    private UUID builderUuid = null;
    private final int totalBlocks;
    private int placedBlocksCount = 0;
    private boolean completed = false;
    private int cooldown = 0;
    private net.minecraft.world.level.block.Rotation rotation = net.minecraft.world.level.block.Rotation.NONE;

    private final List<BlockPos> obstructingBlocks = new ArrayList<>();
    private boolean needsShovel = false;
    private boolean needsPickaxe = false;
    private boolean needsAxe = false;
    private ItemStack equippedShovel = ItemStack.EMPTY;
    private ItemStack equippedPickaxe = ItemStack.EMPTY;
    private ItemStack equippedAxe = ItemStack.EMPTY;
    private int breakProgress = 0;

    public ConstructionSite(UUID siteId, UUID ownerUuid, BlockPos origin, ResourceKey<Level> dimension,
                            ZooSchematic schematic, Map<Item, Integer> required, List<StructureTemplate.StructureBlockInfo> blocks) {
        this.siteId = siteId;
        this.ownerUuid = ownerUuid;
        this.origin = origin;
        this.dimension = dimension;
        this.schematic = schematic;
        this.initialRequiredMaterials.putAll(required);
        this.pendingBlocks.addAll(blocks);
        this.totalBlocks = blocks.size();

        var dim = schematic.getDimensions();
        this.bounds = new BoundingBox(
                origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + dim.getX() - 1, origin.getY() + dim.getY() - 1, origin.getZ() + dim.getZ() - 1
        );
    }

    public synchronized void scanObstructions(net.minecraft.server.level.ServerLevel level) {
        obstructingBlocks.clear();
        needsShovel = false;
        needsPickaxe = false;
        needsAxe = false;

        // Map expected blocks from schematic
        Map<BlockPos, net.minecraft.world.level.block.state.BlockState> expectedStates = new HashMap<>();
        for (var b : pendingBlocks) {
            expectedStates.put(origin.offset(b.pos()), b.state());
        }

        // Check if any pending blocks are already placed
        Iterator<StructureTemplate.StructureBlockInfo> it = pendingBlocks.iterator();
        while (it.hasNext()) {
            var b = it.next();
            BlockPos p = origin.offset(b.pos());
            net.minecraft.world.level.block.state.BlockState s = level.getBlockState(p);
            if (s.equals(b.state())) {
                it.remove();
                placedBlocksCount++;
            }
        }

        if (pendingBlocks.isEmpty()) {
            completed = true;
        }

        // Scan full 3D bounding box volume for any obstructing blocks (along X, Z, and Y axis)
        var dim = schematic.getDimensions();
        int minX = origin.getX();
        int maxX = origin.getX() + dim.getX() - 1;
        int minY = origin.getY();
        int maxY = origin.getY() + dim.getY() - 1;
        int minZ = origin.getZ();
        int maxZ = origin.getZ() + dim.getZ() - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = maxY; y >= minY; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    var s = level.getBlockState(p);
                    if (s.isAir() || s.is(net.minecraft.world.level.block.Blocks.BEDROCK) || s.is(net.minecraft.world.level.block.Blocks.BARRIER)) {
                        continue;
                    }
                    var expected = expectedStates.get(p);
                    if (expected != null && s.equals(expected)) {
                        continue;
                    }
                    // Obstructing block that must be cleared
                    obstructingBlocks.add(p);
                    checkToolNeeded(s);
                }
            }
        }

        // Sort descending by Y so kuli clears from highest block downwards
        obstructingBlocks.sort((p1, p2) -> Integer.compare(p2.getY(), p1.getY()));
    }

    private void checkToolNeeded(net.minecraft.world.level.block.state.BlockState s) {
        if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE) || s.is(net.minecraft.tags.BlockTags.LOGS) || s.is(net.minecraft.tags.BlockTags.LEAVES)) {
            needsAxe = true;
        } else if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL) || s.is(net.minecraft.tags.BlockTags.DIRT) || s.is(net.minecraft.tags.BlockTags.SAND)) {
            needsShovel = true;
        } else if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE) || !s.isAir()) {
            needsPickaxe = true;
        }
    }

    public synchronized boolean depositTools(ServerPlayer player) {
        boolean any = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (needsShovel && !hasShovel() && stack.is(net.minecraft.tags.ItemTags.SHOVELS)) {
                    setEquippedShovel(stack);
                    stack.shrink(1);
                    any = true;
                } else if (needsPickaxe && !hasPickaxe() && stack.is(net.minecraft.tags.ItemTags.PICKAXES)) {
                    setEquippedPickaxe(stack);
                    stack.shrink(1);
                    any = true;
                } else if (needsAxe && !hasAxe() && stack.is(net.minecraft.tags.ItemTags.AXES)) {
                    setEquippedAxe(stack);
                    stack.shrink(1);
                    any = true;
                }
            }
        }
        return any;
    }

    public synchronized List<BlockPos> getObstructingBlocks() {
        return obstructingBlocks;
    }

    public synchronized boolean isClearingPhase() {
        return !obstructingBlocks.isEmpty();
    }

    public synchronized BlockPos getNextObstructingBlock(net.minecraft.server.level.ServerLevel level) {
        while (!obstructingBlocks.isEmpty()) {
            BlockPos p = obstructingBlocks.get(0);
            var state = level.getBlockState(p);
            if (state.isAir()) {
                obstructingBlocks.remove(0);
            } else {
                return p;
            }
        }
        return null;
    }

    public synchronized void removeObstructingBlock(BlockPos pos) {
        obstructingBlocks.remove(pos);
    }

    public boolean needsShovel() { return needsShovel; }
    public boolean needsPickaxe() { return needsPickaxe; }
    public boolean needsAxe() { return needsAxe; }
    public boolean hasShovel() { return !equippedShovel.isEmpty(); }
    public boolean hasPickaxe() { return !equippedPickaxe.isEmpty(); }
    public boolean hasAxe() { return !equippedAxe.isEmpty(); }
    public ItemStack getEquippedShovel() { return equippedShovel; }
    public ItemStack getEquippedPickaxe() { return equippedPickaxe; }
    public ItemStack getEquippedAxe() { return equippedAxe; }
    public void setEquippedShovel(ItemStack s) { this.equippedShovel = s.copyWithCount(1); }
    public void setEquippedPickaxe(ItemStack s) { this.equippedPickaxe = s.copyWithCount(1); }
    public void setEquippedAxe(ItemStack s) { this.equippedAxe = s.copyWithCount(1); }

    public boolean needsAnyTool() {
        return (needsShovel && !hasShovel()) || (needsPickaxe && !hasPickaxe()) || (needsAxe && !hasAxe());
    }

    public boolean hasAllRequiredTools() {
        return (!needsShovel || hasShovel()) && (!needsPickaxe || hasPickaxe()) && (!needsAxe || hasAxe());
    }

    public boolean hasToolFor(net.minecraft.world.level.block.state.BlockState s) {
        if (s.isAir() || s.canBeReplaced()) return true;
        if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE) || s.is(net.minecraft.tags.BlockTags.LOGS) || s.is(net.minecraft.tags.BlockTags.LEAVES)) {
            return hasAxe();
        } else if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL) || s.is(net.minecraft.tags.BlockTags.DIRT) || s.is(net.minecraft.tags.BlockTags.SAND)) {
            return hasShovel();
        } else {
            return hasPickaxe();
        }
    }

    public ItemStack getToolStackFor(net.minecraft.world.level.block.state.BlockState s) {
        if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE) || s.is(net.minecraft.tags.BlockTags.LOGS) || s.is(net.minecraft.tags.BlockTags.LEAVES)) {
            return !equippedAxe.isEmpty() ? equippedAxe : new ItemStack(Items.IRON_AXE);
        } else if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL) || s.is(net.minecraft.tags.BlockTags.DIRT) || s.is(net.minecraft.tags.BlockTags.SAND)) {
            return !equippedShovel.isEmpty() ? equippedShovel : new ItemStack(Items.IRON_SHOVEL);
        } else {
            return !equippedPickaxe.isEmpty() ? equippedPickaxe : new ItemStack(Items.IRON_PICKAXE);
        }
    }

    public void flagToolNeeded(net.minecraft.world.level.block.state.BlockState s) {
        if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE) || s.is(net.minecraft.tags.BlockTags.LOGS) || s.is(net.minecraft.tags.BlockTags.LEAVES)) {
            needsAxe = true;
        } else if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL) || s.is(net.minecraft.tags.BlockTags.DIRT) || s.is(net.minecraft.tags.BlockTags.SAND)) {
            needsShovel = true;
        } else {
            needsPickaxe = true;
        }
    }

    public int getBreakProgress() { return breakProgress; }
    public void incrementBreakProgress() { this.breakProgress++; }
    public void resetBreakProgress() { this.breakProgress = 0; }

    public UUID getSiteId() {
        return siteId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public ZooSchematic getSchematic() {
        return schematic;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public net.minecraft.world.level.block.Rotation getRotation() {
        return rotation;
    }

    public void setRotation(net.minecraft.world.level.block.Rotation rotation) {
        this.rotation = rotation != null ? rotation : net.minecraft.world.level.block.Rotation.NONE;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public int getPlacedBlocksCount() {
        return placedBlocksCount;
    }

    public boolean isCompleted() {
        return completed || (totalBlocks > 0 && placedBlocksCount >= totalBlocks);
    }

    public void setCompleted(boolean val) {
        this.completed = val;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cd) {
        this.cooldown = cd;
    }

    public void decrementCooldown() {
        if (this.cooldown > 0) {
            this.cooldown--;
        }
    }

    public Set<BlockPos> getTapePositions() {
        return tapePositions;
    }

    public void addTapePositions(Collection<BlockPos> pos) {
        tapePositions.addAll(pos);
    }

    public UUID getBuilderUuid() {
        return builderUuid;
    }

    public void setBuilderUuid(UUID id) {
        this.builderUuid = id;
    }

    public float getProgressPercentage() {
        if (totalBlocks == 0) return 100.0f;
        return ((float) placedBlocksCount / totalBlocks) * 100.0f;
    }

    public synchronized Map<Item, Integer> getRemainingRequiredMaterials() {
        Map<Item, Integer> remaining = new HashMap<>();
        for (var b : pendingBlocks) {
            Item item = b.state().getBlock().asItem();
            if (item != Items.AIR) {
                remaining.put(item, remaining.getOrDefault(item, 0) + 1);
            }
        }
        return remaining;
    }

    public synchronized Map<Item, Integer> getMissingMaterials() {
        Map<Item, Integer> remaining = getRemainingRequiredMaterials();
        Map<Item, Integer> missing = new HashMap<>();
        for (var entry : remaining.entrySet()) {
            Item item = entry.getKey();
            int needed = entry.getValue();
            int del = deliveredMaterials.getOrDefault(item, 0);
            if (del < needed) {
                missing.put(item, needed - del);
            }
        }
        return missing;
    }

    public synchronized Map<Item, Integer> getRequiredMaterials() {
        return getRemainingRequiredMaterials();
    }

    public Map<Item, Integer> getInitialRequiredMaterials() {
        return Collections.unmodifiableMap(initialRequiredMaterials);
    }

    public synchronized int deliverMaterial(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        Map<Item, Integer> remaining = getRemainingRequiredMaterials();
        int needed = remaining.getOrDefault(item, 0);
        int currentDelivered = deliveredMaterials.getOrDefault(item, 0);

        if (currentDelivered >= needed) {
            return 0; // Not needed or already fulfilled for remaining unplaced blocks
        }

        int stillNeeded = needed - currentDelivered;
        int accepted = Math.min(stack.getCount(), stillNeeded);
        stack.shrink(accepted);
        deliveredMaterials.put(item, currentDelivered + accepted);
        return accepted;
    }

    public synchronized boolean hasAvailableItemFor(StructureTemplate.StructureBlockInfo blockInfo) {
        Item item = blockInfo.state().getBlock().asItem();
        if (item == Items.AIR) return true;
        return deliveredMaterials.getOrDefault(item, 0) > 0;
    }

    public synchronized boolean consumeItemFor(StructureTemplate.StructureBlockInfo blockInfo) {
        Item item = blockInfo.state().getBlock().asItem();
        if (item == Items.AIR) return true;
        int count = deliveredMaterials.getOrDefault(item, 0);
        if (count > 0) {
            deliveredMaterials.put(item, count - 1);
            return true;
        }
        return false;
    }

    public synchronized List<StructureTemplate.StructureBlockInfo> getPendingBlocks() {
        return pendingBlocks;
    }

    public synchronized StructureTemplate.StructureBlockInfo getNextBlock() {
        if (pendingBlocks.isEmpty()) return null;
        return pendingBlocks.get(0);
    }

    public synchronized void markBlockPlaced() {
        markSpecificBlockPlaced(0);
    }

    public synchronized void markSpecificBlockPlaced(int index) {
        if (index >= 0 && index < pendingBlocks.size()) {
            pendingBlocks.remove(index);
            placedBlocksCount++;
            if (pendingBlocks.isEmpty()) {
                completed = true;
            }
        }
    }

    public synchronized int findNextPlaceableIndex(net.minecraft.server.level.ServerLevel level, boolean isCreative) {
        // Clean already placed matching blocks from head if any
        while (!pendingBlocks.isEmpty()) {
            var head = pendingBlocks.get(0);
            BlockPos hp = origin.offset(head.pos());
            if (level.getBlockState(hp).equals(head.state())) {
                pendingBlocks.remove(0);
                placedBlocksCount++;
                if (pendingBlocks.isEmpty()) {
                    completed = true;
                    return -1;
                }
            } else {
                break;
            }
        }

        for (int i = 0; i < pendingBlocks.size(); i++) {
            var info = pendingBlocks.get(i);
            if (!isCreative && !hasAvailableItemFor(info)) {
                continue;
            }
            BlockPos pos = origin.offset(info.pos());
            net.minecraft.world.level.block.state.BlockState cur = level.getBlockState(pos);
            if (cur.equals(info.state())) {
                markSpecificBlockPlaced(i);
                i--;
                continue;
            }
            return i;
        }

        return -1;
    }

    public synchronized int findNextActionableIndex(net.minecraft.server.level.ServerLevel level, boolean isCreative) {
        return findNextPlaceableIndex(level, isCreative);
    }
}

