package net.zootycoon.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;

import java.lang.reflect.Field;
import java.util.*;

public class SchematicScanner {

    private static Field PALETTES_FIELD = null;
    static {
        try {
            for (Field f : StructureTemplate.class.getDeclaredFields()) {
                if (f.getName().equals("palettes")) {
                    f.setAccessible(true);
                    PALETTES_FIELD = f;
                    break;
                }
            }
            if (PALETTES_FIELD == null) {
                for (Field f : StructureTemplate.class.getDeclaredFields()) {
                    if (List.class.isAssignableFrom(f.getType()) && !f.getName().toLowerCase().contains("entity")) {
                        f.setAccessible(true);
                        PALETTES_FIELD = f;
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            ZooTycoonMod.LOGGER.error("Failed to locate palettes field in StructureTemplate", t);
        }
    }

    public record ScanResult(
            Map<Item, Integer> requiredMaterials,
            List<StructureTemplate.StructureBlockInfo> sortedBlocksToPlace,
            int totalBlocks
    ) {}

    @SuppressWarnings("unchecked")
    public static List<StructureTemplate.StructureBlockInfo> getRawTemplateBlocks(StructureTemplate template) {
        if (template == null) return Collections.emptyList();
        if (PALETTES_FIELD != null) {
            try {
                List<StructureTemplate.Palette> pals = (List<StructureTemplate.Palette>) PALETTES_FIELD.get(template);
                if (pals != null && !pals.isEmpty()) {
                    return pals.get(0).blocks();
                }
            } catch (Throwable t) {
                ZooTycoonMod.LOGGER.error("Error reading palettes field from StructureTemplate", t);
            }
        }
        return Collections.emptyList();
    }

    public static ScanResult scan(StructureTemplate template) {
        return scan(template, Rotation.NONE);
    }

    public static ScanResult scan(StructureTemplate template, Rotation rotation) {
        Map<Item, Integer> materialCounts = new HashMap<>();
        List<StructureTemplate.StructureBlockInfo> blocks = new ArrayList<>();

        StructurePlaceSettings settings = new StructurePlaceSettings();
        if (rotation != null) {
            settings.setRotation(rotation);
        }

        List<StructureTemplate.StructureBlockInfo> rawList = getRawTemplateBlocks(template);
        for (StructureTemplate.StructureBlockInfo raw : rawList) {
            BlockState state = raw.state();
            if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.BARRIER)) {
                continue;
            }

            BlockPos rotPos = StructureTemplate.calculateRelativePosition(settings, raw.pos());
            BlockState rotState = state.rotate(rotation != null ? rotation : Rotation.NONE);
            StructureTemplate.StructureBlockInfo rotated = new StructureTemplate.StructureBlockInfo(rotPos, rotState, raw.nbt());
            blocks.add(rotated);

            Item item = state.getBlock().asItem();
            if (item != Items.AIR) {
                materialCounts.put(item, materialCounts.getOrDefault(item, 0) + 1);
            }
        }

        // Sort bottom-up: lowest Y first, then X and Z
        blocks.sort(Comparator.comparingInt((StructureTemplate.StructureBlockInfo b) -> b.pos().getY())
                .thenComparingInt(b -> b.pos().getX())
                .thenComparingInt(b -> b.pos().getZ())
        );

        return new ScanResult(materialCounts, blocks, blocks.size());
    }
}
