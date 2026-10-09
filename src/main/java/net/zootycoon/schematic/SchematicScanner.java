package net.zootycoon.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.*;

public class SchematicScanner {

    public record ScanResult(
            Map<Item, Integer> requiredMaterials,
            List<StructureTemplate.StructureBlockInfo> sortedBlocksToPlace,
            int totalBlocks
    ) {}

    public static ScanResult scan(StructureTemplate template) {
        Map<Item, Integer> materialCounts = new HashMap<>();
        List<StructureTemplate.StructureBlockInfo> blocks = new ArrayList<>();

        List<StructureTemplate.StructureBlockInfo> allBlocks = template.palettes.isEmpty()
                ? Collections.emptyList()
                : template.palettes.get(0).blocks();

        for (StructureTemplate.StructureBlockInfo info : allBlocks) {
            BlockState state = info.state();
            if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.BARRIER)) {
                continue; // Ignore void / indestructible blocks
            }

            blocks.add(info);

            Item item = state.getBlock().asItem();
            if (item != Items.AIR) {
                materialCounts.put(item, materialCounts.getOrDefault(item, 0) + 1);
            }
        }

        // Sort bottom-up: lowest Y first, then spiral or X/Z order
        blocks.sort(Comparator.comparingInt((StructureTemplate.StructureBlockInfo b) -> b.pos().getY())
                .thenComparingInt(b -> b.pos().getX())
                .thenComparingInt(b -> b.pos().getZ())
        );

        return new ScanResult(materialCounts, blocks, blocks.size());
    }
}

