package net.zootycoon.construction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
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
                    if (s.isAir() || isReplaceableGrassOrPlant(s) || s.is(net.minecraft.world.level.block.Blocks.BEDROCK) || s.is(net.minecraft.world.level.block.Blocks.BARRIER)) {
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

    public static boolean isReplaceableGrassOrPlant(net.minecraft.world.level.block.state.BlockState s) {
        if (s == null || s.isAir()) return true;
        if (s.canBeReplaced()) return true;
        if (s.is(net.minecraft.tags.BlockTags.REPLACEABLE)) return true;
        if (s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS)
                || s.is(net.minecraft.world.level.block.Blocks.TALL_GRASS)
                || s.is(net.minecraft.world.level.block.Blocks.FERN)
                || s.is(net.minecraft.world.level.block.Blocks.LARGE_FERN)
                || s.is(net.minecraft.world.level.block.Blocks.DEAD_BUSH)) {
            return true;
        }
        return false;
    }

    public static boolean isGrassOrFlowerItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item == Items.GRASS_BLOCK || item == Items.SHORT_GRASS || item == Items.FERN || item == Items.LARGE_FERN) return true;
        if (stack.is(net.minecraft.tags.ItemTags.FLOWERS)) return true;
        return false;
    }

    private void checkToolNeeded(net.minecraft.world.level.block.state.BlockState s) {
        if (isReplaceableGrassOrPlant(s)) return;
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

    public synchronized void fulfillAllTools() {
        if (needsShovel && !hasShovel()) {
            setEquippedShovel(new ItemStack(Items.DIAMOND_SHOVEL));
        }
        if (needsPickaxe && !hasPickaxe()) {
            setEquippedPickaxe(new ItemStack(Items.DIAMOND_PICKAXE));
        }
        if (needsAxe && !hasAxe()) {
            setEquippedAxe(new ItemStack(Items.DIAMOND_AXE));
        }
    }

    public synchronized void fulfillAllMaterials() {
        Map<Item, Integer> remaining = getRemainingRequiredMaterials();
        for (var entry : remaining.entrySet()) {
            deliveredMaterials.put(entry.getKey(), entry.getValue());
        }
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
            if (state.isAir() || isReplaceableGrassOrPlant(state)) {
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
        if (s.isAir() || isReplaceableGrassOrPlant(s)) return true;
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
        if (isReplaceableGrassOrPlant(s)) return;
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
            Item item = net.zootycoon.schematic.SchematicScanner.getRequiredItemForState(b.state());
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

        // 1. Direct match for required item
        int needed = remaining.getOrDefault(item, 0);
        int currentDelivered = deliveredMaterials.getOrDefault(item, 0);

        if (needed > currentDelivered) {
            int stillNeeded = needed - currentDelivered;
            int accepted = Math.min(stack.getCount(), stillNeeded);
            stack.shrink(accepted);
            deliveredMaterials.put(item, currentDelivered + accepted);
            return accepted;
        }

        // 2. Bonemeal substitution: If bonemeal is needed, accept bone_meal, bone, grass_block, short grass, fern, or flowers!
        int boneMealNeeded = remaining.getOrDefault(Items.BONE_MEAL, 0);
        int boneMealDelivered = deliveredMaterials.getOrDefault(Items.BONE_MEAL, 0);
        if (boneMealNeeded > boneMealDelivered) {
            int stillNeeded = boneMealNeeded - boneMealDelivered;
            if (item == Items.BONE_MEAL) {
                int accepted = Math.min(stack.getCount(), stillNeeded);
                stack.shrink(accepted);
                deliveredMaterials.put(Items.BONE_MEAL, boneMealDelivered + accepted);
                return accepted;
            } else if (item == Items.BONE) {
                // 1 bone = 3 bone meals
                int bonesToUse = (int) Math.ceil((double) stillNeeded / 3.0);
                int acceptedBones = Math.min(stack.getCount(), bonesToUse);
                stack.shrink(acceptedBones);
                deliveredMaterials.put(Items.BONE_MEAL, Math.min(boneMealNeeded, boneMealDelivered + (acceptedBones * 3)));
                return acceptedBones;
            } else if (isGrassOrFlowerItem(stack)) {
                int accepted = Math.min(stack.getCount(), stillNeeded);
                stack.shrink(accepted);
                deliveredMaterials.put(Items.BONE_MEAL, boneMealDelivered + accepted);
                return accepted;
            }
        }

        return 0;
    }

    public synchronized boolean hasAvailableItemFor(StructureTemplate.StructureBlockInfo blockInfo) {
        Item item = net.zootycoon.schematic.SchematicScanner.getRequiredItemForState(blockInfo.state());
        if (item == Items.AIR) return true;
        return deliveredMaterials.getOrDefault(item, 0) > 0;
    }

    public synchronized boolean consumeItemFor(StructureTemplate.StructureBlockInfo blockInfo) {
        Item item = net.zootycoon.schematic.SchematicScanner.getRequiredItemForState(blockInfo.state());
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

    public synchronized CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("SiteId", siteId);
        tag.putUUID("OwnerUuid", ownerUuid);
        tag.putLong("Origin", origin.asLong());
        tag.putString("Dimension", dimension.location().toString());
        tag.putString("SchematicId", schematic.getId());
        tag.putString("Rotation", rotation.name());
        if (builderUuid != null) {
            tag.putUUID("BuilderUuid", builderUuid);
        }
        tag.putInt("PlacedBlocksCount", placedBlocksCount);
        tag.putBoolean("Completed", completed);
        tag.putInt("Cooldown", cooldown);
        tag.putInt("BreakProgress", breakProgress);

        tag.putBoolean("NeedsShovel", needsShovel);
        tag.putBoolean("NeedsPickaxe", needsPickaxe);
        tag.putBoolean("NeedsAxe", needsAxe);

        if (!equippedShovel.isEmpty()) {
            tag.putString("EquippedShovel", BuiltInRegistries.ITEM.getKey(equippedShovel.getItem()).toString());
        }
        if (!equippedPickaxe.isEmpty()) {
            tag.putString("EquippedPickaxe", BuiltInRegistries.ITEM.getKey(equippedPickaxe.getItem()).toString());
        }
        if (!equippedAxe.isEmpty()) {
            tag.putString("EquippedAxe", BuiltInRegistries.ITEM.getKey(equippedAxe.getItem()).toString());
        }

        CompoundTag deliveredTag = new CompoundTag();
        for (var entry : deliveredMaterials.entrySet()) {
            deliveredTag.putInt(BuiltInRegistries.ITEM.getKey(entry.getKey()).toString(), entry.getValue());
        }
        tag.put("DeliveredMaterials", deliveredTag);

        tag.putLongArray("TapePositions", tapePositions.stream().mapToLong(BlockPos::asLong).toArray());

        return tag;
    }

    public static ConstructionSite fromNbt(CompoundTag tag, MinecraftServer server) {
        try {
            UUID siteId = tag.getUUID("SiteId");
            UUID ownerUuid = tag.getUUID("OwnerUuid");
            BlockPos origin = BlockPos.of(tag.getLong("Origin"));
            String dimStr = tag.getString("Dimension");
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
            String schemId = tag.getString("SchematicId");
            ZooSchematic schem = net.zootycoon.schematic.ZooSchematicRegistry.get(schemId).orElse(null);
            if (schem == null) {
                net.zootycoon.schematic.ZooSchematicRegistry.loadCustomSchematics();
                schem = net.zootycoon.schematic.ZooSchematicRegistry.get(schemId).orElse(null);
            }
            if (schem == null) {
                return null;
            }

            Rotation rot;
            try {
                rot = Rotation.valueOf(tag.getString("Rotation"));
            } catch (Exception e) {
                rot = Rotation.NONE;
            }

            StructureTemplate template = net.zootycoon.schematic.ZooSchematicRegistry.loadTemplate(server, schem);
            if (template == null) {
                return null;
            }

            var scan = net.zootycoon.schematic.SchematicScanner.scan(template, rot);
            ConstructionSite site = new ConstructionSite(
                    siteId, ownerUuid, origin, dimension, schem, scan.requiredMaterials(), scan.sortedBlocksToPlace()
            );
            site.setRotation(rot);
            if (tag.hasUUID("BuilderUuid")) {
                site.setBuilderUuid(tag.getUUID("BuilderUuid"));
            }
            site.setCooldown(tag.getInt("Cooldown"));
            site.breakProgress = tag.getInt("BreakProgress");
            site.completed = tag.getBoolean("Completed");

            site.needsShovel = tag.getBoolean("NeedsShovel");
            site.needsPickaxe = tag.getBoolean("NeedsPickaxe");
            site.needsAxe = tag.getBoolean("NeedsAxe");

            if (tag.contains("EquippedShovel")) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("EquippedShovel")));
                if (item != Items.AIR) site.setEquippedShovel(new ItemStack(item));
            }
            if (tag.contains("EquippedPickaxe")) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("EquippedPickaxe")));
                if (item != Items.AIR) site.setEquippedPickaxe(new ItemStack(item));
            }
            if (tag.contains("EquippedAxe")) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("EquippedAxe")));
                if (item != Items.AIR) site.setEquippedAxe(new ItemStack(item));
            }

            CompoundTag delTag = tag.getCompound("DeliveredMaterials");
            for (String key : delTag.getAllKeys()) {
                try {
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(key));
                    if (item != null && item != Items.AIR) {
                        site.deliveredMaterials.put(item, delTag.getInt(key));
                    }
                } catch (Exception ignored) {}
            }

            if (tag.contains("TapePositions")) {
                long[] tapes = tag.getLongArray("TapePositions");
                Set<BlockPos> tape = new HashSet<>();
                for (long l : tapes) {
                    tape.add(BlockPos.of(l));
                }
                site.addTapePositions(tape);
            }

            // Sync with actual world blocks if level is loaded
            net.minecraft.server.level.ServerLevel sLevel = server.getLevel(dimension);
            if (sLevel != null) {
                site.scanObstructions(sLevel);
            }

            return site;
        } catch (Exception e) {
            net.zootycoon.ZooTycoonMod.LOGGER.error("Failed to deserialize ConstructionSite from NBT", e);
            return null;
        }
    }
}

