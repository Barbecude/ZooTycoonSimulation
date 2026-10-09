package net.zootycoon.construction;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    private final ZooSchematic schematic;
    private final BoundingBox bounds;

    private final Map<Item, Integer> requiredMaterials = new ConcurrentHashMap<>();
    private final Map<Item, Integer> deliveredMaterials = new ConcurrentHashMap<>();
    private final List<StructureTemplate.StructureBlockInfo> pendingBlocks = new ArrayList<>();
    private final int totalBlocks;
    private int placedBlocksCount = 0;
    private boolean completed = false;

    public ConstructionSite(UUID siteId, UUID ownerUuid, BlockPos origin, ZooSchematic schematic,
                            Map<Item, Integer> required, List<StructureTemplate.StructureBlockInfo> blocks) {
        this.siteId = siteId;
        this.ownerUuid = ownerUuid;
        this.origin = origin;
        this.schematic = schematic;
        this.requiredMaterials.putAll(required);
        this.pendingBlocks.addAll(blocks);
        this.totalBlocks = blocks.size();

        var dim = schematic.getDimensions();
        this.bounds = new BoundingBox(
                origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + dim.getX(), origin.getY() + dim.getY(), origin.getZ() + dim.getZ()
        );
    }

    public UUID getSiteId() {
        return siteId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public ZooSchematic getSchematic() {
        return schematic;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public int getPlacedBlocksCount() {
        return placedBlocksCount;
    }

    public boolean isCompleted() {
        return completed || pendingBlocks.isEmpty();
    }

    public float getProgressPercentage() {
        if (totalBlocks == 0) return 100.0f;
        return ((float) placedBlocksCount / totalBlocks) * 100.0f;
    }

    public Map<Item, Integer> getMissingMaterials() {
        Map<Item, Integer> missing = new HashMap<>();
        for (var entry : requiredMaterials.entrySet()) {
            Item item = entry.getKey();
            int req = entry.getValue();
            int del = deliveredMaterials.getOrDefault(item, 0);
            if (del < req) {
                missing.put(item, req - del);
            }
        }
        return missing;
    }

    public synchronized int deliverMaterial(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        int req = requiredMaterials.getOrDefault(item, 0);
        int currentDelivered = deliveredMaterials.getOrDefault(item, 0);

        if (currentDelivered >= req) {
            return 0; // Not needed or already fulfilled
        }

        int needed = req - currentDelivered;
        int accepted = Math.min(stack.getCount(), needed);
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

    public synchronized StructureTemplate.StructureBlockInfo getNextBlock() {
        if (pendingBlocks.isEmpty()) return null;
        return pendingBlocks.get(0);
    }

    public synchronized void markBlockPlaced() {
        if (!pendingBlocks.isEmpty()) {
            pendingBlocks.remove(0);
            placedBlocksCount++;
            if (pendingBlocks.isEmpty()) {
                completed = true;
            }
        }
    }

    public synchronized boolean autoBuyMissing(ServerPlayer player) {
        Map<Item, Integer> missing = getMissingMaterials();
        if (missing.isEmpty()) return true;

        int cost = EconomyConfig.calculateMissingCost(missing);
        if (!TransactionHelper.hasEnoughEmeralds(player, cost)) {
            return false;
        }

        if (TransactionHelper.consumeEmeralds(player, cost)) {
            // Fulfill all delivered materials
            for (var entry : missing.entrySet()) {
                deliveredMaterials.put(entry.getKey(), requiredMaterials.get(entry.getKey()));
            }
            return true;
        }
        return false;
    }
}

