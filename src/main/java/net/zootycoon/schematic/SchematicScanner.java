package net.zootycoon.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherSproutsBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.RootsBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
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

            Item item = getRequiredItemForState(state);
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

    private static final TagKey<Block> C_FLOWERS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "flowers"));
    private static final TagKey<Block> C_SMALL_FLOWERS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "small_flowers"));
    private static final TagKey<Block> C_TALL_FLOWERS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "tall_flowers"));
    private static final TagKey<Block> FORGE_FLOWERS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "flowers"));

    private static final Set<String> MODDED_FLOWER_NAMESPACES = Set.of(
            "bloomingnature", "farm_and_charm", "farmersdelight", "letsdo", "meadow", "vinery", "herbalbrews"
    );

    private static final List<String> FLOWER_KEYWORDS = List.of(
            "flower", "blossom", "petal", "clover", "bloom", "wildflower",
            "orchid", "daisy", "poppy", "tulip", "rose", "lily", "violet",
            "lavender", "peony", "hydrangea", "sunflower", "marigold", "dahlia",
            "lupine", "bluebell", "hibiscus", "begonia", "iris", "crocus",
            "foxglove", "delphinium", "anemone", "freesia", "hyssop", "laurel",
            "buttercup", "cornflower", "gladiolus", "golden_rod", "joe_pye",
            "bird_of_paradise", "bottlebrush", "pansy", "cardinal", "wild_"
    );

    private static final Set<String> NON_FLOWER_TOKENS = Set.of(
            "sapling", "saplings", "log", "logs", "wood", "plank", "planks",
            "leaves", "leaf", "slab", "slabs", "stair", "stairs", "fence", "fences",
            "wall", "walls", "door", "doors", "gate", "gates", "trapdoor", "trapdoors",
            "seed", "seeds", "pot", "potted", "box", "boxes", "basket", "baskets",
            "crate", "crates", "bag", "bags", "sack", "sacks", "carpet", "carpets",
            "bed", "beds", "rug", "rugs", "banner", "banners", "berry", "berries",
            "crop", "crops", "dough", "flour", "cauliflower", "paper", "brick",
            "bricks", "tile", "tiles", "stone", "cobblestone"
    );

    public static boolean isFlowerNeed(BlockState state) {
        if (state == null || state.isAir()) return false;
        if (state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SMALL_FLOWERS)
                || state.is(BlockTags.TALL_FLOWERS)
                || state.is(C_FLOWERS)
                || state.is(C_SMALL_FLOWERS)
                || state.is(C_TALL_FLOWERS)
                || state.is(FORGE_FLOWERS)) {
            return true;
        }

        Block block = state.getBlock();
        if (block instanceof FlowerBlock || block instanceof TallFlowerBlock || block instanceof PinkPetalsBlock) {
            return true;
        }

        if (block instanceof SaplingBlock
                || block instanceof CropBlock
                || block instanceof StemBlock
                || block instanceof AttachedStemBlock
                || block instanceof SweetBerryBushBlock
                || block instanceof MushroomBlock
                || block instanceof FungusBlock
                || block instanceof RootsBlock
                || block instanceof NetherSproutsBlock
                || block instanceof BambooSaplingBlock) {
            return false;
        }

        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);
        String namespace = blockKey != null ? blockKey.getNamespace() : "";
        String path = blockKey != null ? blockKey.getPath() : "";
        String className = block.getClass().getSimpleName();
        return matchesModdedFlowerRule(namespace, path, className, block instanceof BushBlock, block.defaultBlockState().canOcclude());
    }

    /**
     * Classifies modded/untagged flower blocks by registry namespace, ID keywords, and block class.
     */
    public static boolean matchesModdedFlowerRule(String namespace, String path, String className, boolean isBushBlock, boolean canOcclude) {
        String ns = namespace != null ? namespace.toLowerCase(Locale.ROOT) : "";
        String p = path != null ? path.toLowerCase(Locale.ROOT) : "";
        String cls = className != null ? className.toLowerCase(Locale.ROOT) : "";

        if (hasExcludedToken(p)) {
            return false;
        }
        if (isBushBlock) {
            if (MODDED_FLOWER_NAMESPACES.contains(ns)) {
                return true;
            }
            if (containsAny(p, FLOWER_KEYWORDS) || containsAny(cls, FLOWER_KEYWORDS)) {
                return true;
            }
        }
        return !canOcclude && (p.contains("flower") || cls.contains("flower"));
    }

    private static boolean hasExcludedToken(String path) {
        if (path == null || path.isEmpty()) return false;
        for (String token : path.split("_")) {
            if (NON_FLOWER_TOKENS.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAny(String value, List<String> tokens) {
        if (value == null || value.isEmpty()) return false;
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGrassOrPlantNeed(BlockState state) {
        if (state == null || state.isAir()) return false;
        if (state.is(Blocks.GRASS_BLOCK)) return true;
        if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)) return true;
        if (state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)) return true;
        return isFlowerNeed(state);
    }

    public static Item getRequiredItemForState(BlockState state) {
        if (isGrassOrPlantNeed(state)) {
            return Items.BONE_MEAL;
        }
        return state.getBlock().asItem();
    }
}
