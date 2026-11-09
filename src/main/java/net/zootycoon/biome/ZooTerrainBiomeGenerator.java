package net.zootycoon.biome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;
import net.zootycoon.util.ZooTranslations;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates data-driven "Already Built Biome" terrain schematics in the "terrain" category.
 * Captures world generation palettes, centered elevated plateaus with natural falloff,
 * and 3D Biome NBT tags for instant habitat transformation.
 */
public class ZooTerrainBiomeGenerator {

    public static final int SIZE_X = 32;
    public static final int SIZE_Y = 22;
    public static final int SIZE_Z = 32;
    public static final int BASE_Y = 3;

    @FunctionalInterface
    public interface Decorator {
        void decorate(BlockState[][][] grid, int x, int ySurface, int z, int plateauHeight, boolean isPlateau, Random rand);
    }

    public record BiomePreset(
            String idKey,
            ResourceKey<Biome> biomeKey,
            String translationKey,
            String fallbackName,
            int plateauHeight,
            BlockState surfaceBlock,
            BlockState subsurfaceBlock,
            BlockState foundationBlock,
            Decorator decorator
    ) {}

    private static final List<BiomePreset> PRESETS = new ArrayList<>();

    static {
        // 1. Savanna Biome (Savanna & Savanna Plateau)
        PRESETS.add(new BiomePreset(
                "terrain_savanna",
                Biomes.SAVANNA,
                "zootycoon.terrain.biome.savanna",
                "Savanna Biome",
                5,
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    // Tree on central plateau
                    if (x == 16 && z == 16) {
                        buildAcaciaTree(grid, x, ySurface, z);
                    }
                    // Natural stone/granite rock outcrop on the slope
                    if (x >= 9 && x <= 11 && z >= 21 && z <= 23) {
                        setBlockSafe(grid, x, ySurface, z, Blocks.GRANITE.defaultBlockState());
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.STONE.defaultBlockState());
                    }
                    if (isPlateau) {
                        if (x == 14 && z == 18) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.ACACIA_SAPLING.defaultBlockState());
                        } else if (rand.nextFloat() < 0.12f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.SHORT_GRASS.defaultBlockState());
                        } else if (rand.nextFloat() < 0.04f) {
                            setDoublePlant(grid, x, ySurface + 1, z, Blocks.TALL_GRASS.defaultBlockState());
                        }
                    } else if (rand.nextFloat() < 0.06f) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.SHORT_GRASS.defaultBlockState());
                    }
                }
        ));

        // 2. Plains Biome (Plains & Meadow)
        PRESETS.add(new BiomePreset(
                "terrain_plains",
                Biomes.PLAINS,
                "zootycoon.terrain.biome.plains",
                "Plains Biome",
                4,
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 16 && z == 16) {
                        buildOakTree(grid, x, ySurface, z);
                    }
                    if (isPlateau) {
                        float r = rand.nextFloat();
                        if (r < 0.05f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.POPPY.defaultBlockState());
                        } else if (r < 0.10f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.DANDELION.defaultBlockState());
                        } else if (r < 0.14f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.AZURE_BLUET.defaultBlockState());
                        } else if (r < 0.18f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.CORNFLOWER.defaultBlockState());
                        } else if (r < 0.35f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.SHORT_GRASS.defaultBlockState());
                        }
                    } else if (rand.nextFloat() < 0.10f) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.SHORT_GRASS.defaultBlockState());
                    }
                }
        ));

        // 3. Desert Biome (Desert & Dunes)
        PRESETS.add(new BiomePreset(
                "terrain_desert",
                Biomes.DESERT,
                "zootycoon.terrain.biome.desert",
                "Desert Biome",
                3,
                Blocks.SAND.defaultBlockState(),
                Blocks.SAND.defaultBlockState(),
                Blocks.SANDSTONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    // Sandstone outcrop on plateau center
                    if (isPlateau && (x == 16 || x == 17) && (z == 16 || z == 17)) {
                        setBlockSafe(grid, x, ySurface, z, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.SANDSTONE_SLAB.defaultBlockState());
                    }
                    // Cacti
                    if ((x == 13 && z == 13) || (x == 20 && z == 16) || (x == 15 && z == 21)) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.CACTUS.defaultBlockState());
                        setBlockSafe(grid, x, ySurface + 2, z, Blocks.CACTUS.defaultBlockState());
                    } else if (isPlateau && rand.nextFloat() < 0.05f) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.DEAD_BUSH.defaultBlockState());
                    }
                    // Water oasis at lower edge
                    if (x >= 7 && x <= 10 && z >= 20 && z <= 23) {
                        setBlockSafe(grid, x, ySurface, z, Blocks.WATER.defaultBlockState());
                        if (x == 7 || z == 20) {
                            setBlockSafe(grid, x - 1, ySurface + 1, z, Blocks.SUGAR_CANE.defaultBlockState());
                        }
                    }
                }
        ));

        // 4. Badlands Biome (Mesa Strata)
        PRESETS.add(new BiomePreset(
                "terrain_badlands",
                Biomes.BADLANDS,
                "zootycoon.terrain.biome.badlands",
                "Badlands Biome",
                6,
                Blocks.RED_SAND.defaultBlockState(),
                Blocks.TERRACOTTA.defaultBlockState(),
                Blocks.RED_SANDSTONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    // Strata coloration
                    for (int y = 1; y < ySurface; y++) {
                        BlockState strata = switch (y % 6) {
                            case 0 -> Blocks.TERRACOTTA.defaultBlockState();
                            case 1 -> Blocks.ORANGE_TERRACOTTA.defaultBlockState();
                            case 2 -> Blocks.YELLOW_TERRACOTTA.defaultBlockState();
                            case 3 -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
                            case 4 -> Blocks.BROWN_TERRACOTTA.defaultBlockState();
                            default -> Blocks.RED_TERRACOTTA.defaultBlockState();
                        };
                        setBlockSafe(grid, x, y, z, strata);
                    }
                    if (isPlateau) {
                        setBlockSafe(grid, x, ySurface, z, Blocks.TERRACOTTA.defaultBlockState());
                        if (rand.nextFloat() < 0.08f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.DEAD_BUSH.defaultBlockState());
                        } else if (rand.nextFloat() < 0.03f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.CACTUS.defaultBlockState());
                        }
                    }
                }
        ));

        // 5. Jungle Biome (Rainforest)
        PRESETS.add(new BiomePreset(
                "terrain_jungle",
                Biomes.JUNGLE,
                "zootycoon.terrain.biome.jungle",
                "Jungle Biome",
                5,
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 16 && z == 16) {
                        buildJungleTree(grid, x, ySurface, z);
                    }
                    // Bamboo clusters
                    if ((x == 13 && z == 14) || (x == 19 && z == 17) || (x == 18 && z == 14)) {
                        for (int h = 1; h <= 4; h++) {
                            setBlockSafe(grid, x, ySurface + h, z, Blocks.BAMBOO.defaultBlockState());
                        }
                    }
                    // Melons
                    if ((x == 14 && z == 18) || (x == 18 && z == 18)) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.MELON.defaultBlockState());
                    }
                    if (isPlateau && rand.nextFloat() < 0.15f) {
                        setDoublePlant(grid, x, ySurface + 1, z, Blocks.LARGE_FERN.defaultBlockState());
                    }
                }
        ));

        // 6. Taiga Biome (Pine Forest)
        PRESETS.add(new BiomePreset(
                "terrain_taiga",
                Biomes.TAIGA,
                "zootycoon.terrain.biome.taiga",
                "Taiga Biome",
                4,
                Blocks.PODZOL.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 16 && z == 16) {
                        buildSpruceTree(grid, x, ySurface, z, false);
                    }
                    // Mossy cobblestone boulder
                    if (x >= 11 && x <= 13 && z >= 19 && z <= 20) {
                        setBlockSafe(grid, x, ySurface, z, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                    }
                    if (isPlateau) {
                        if (rand.nextFloat() < 0.08f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.SWEET_BERRY_BUSH.defaultBlockState());
                        } else if (rand.nextFloat() < 0.12f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.FERN.defaultBlockState());
                        }
                    }
                }
        ));

        // 7. Snowy Plains Biome
        PRESETS.add(new BiomePreset(
                "terrain_snowy_plains",
                Biomes.SNOWY_PLAINS,
                "zootycoon.terrain.biome.snowy_plains",
                "Snowy Plains Biome",
                4,
                Blocks.SNOW_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 16 && z == 16) {
                        buildSpruceTree(grid, x, ySurface, z, true);
                    }
                    // Ice spike
                    if (x == 12 && z == 19) {
                        for (int h = 1; h <= 4; h++) {
                            setBlockSafe(grid, x, ySurface + h, z, Blocks.PACKED_ICE.defaultBlockState());
                        }
                    }
                    // Snow layer on surface
                    setBlockSafe(grid, x, ySurface + 1, z, Blocks.SNOW.defaultBlockState().setValue(BlockStateProperties.LAYERS, 2));
                }
        ));

        // 8. Swamp Biome
        PRESETS.add(new BiomePreset(
                "terrain_swamp",
                Biomes.SWAMP,
                "zootycoon.terrain.biome.swamp",
                "Swamp Biome",
                3,
                Blocks.MUD.defaultBlockState(),
                Blocks.MUD.defaultBlockState(),
                Blocks.CLAY.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 16 && z == 16) {
                        buildOakTree(grid, x, ySurface, z);
                    }
                    // Shallow swamp pool
                    if (x >= 7 && x <= 11 && z >= 18 && z <= 22) {
                        setBlockSafe(grid, x, ySurface, z, Blocks.WATER.defaultBlockState());
                        if (rand.nextFloat() < 0.25f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.LILY_PAD.defaultBlockState());
                        }
                    }
                    if (isPlateau && rand.nextFloat() < 0.12f) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.BLUE_ORCHID.defaultBlockState());
                    }
                }
        ));

        // 9. Mangrove Swamp Biome
        PRESETS.add(new BiomePreset(
                "terrain_mangrove_swamp",
                Biomes.MANGROVE_SWAMP,
                "zootycoon.terrain.biome.mangrove_swamp",
                "Mangrove Swamp Biome",
                3,
                Blocks.MUD.defaultBlockState(),
                Blocks.MUD.defaultBlockState(),
                Blocks.CLAY.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 16 && z == 16) {
                        buildMangroveTree(grid, x, ySurface, z);
                    }
                    if (isPlateau && rand.nextFloat() < 0.15f) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
                    }
                }
        ));

        // 10. Forest Biome
        PRESETS.add(new BiomePreset(
                "terrain_forest",
                Biomes.FOREST,
                "zootycoon.terrain.biome.forest",
                "Forest Biome",
                4,
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 14 && z == 15) {
                        buildOakTree(grid, x, ySurface, z);
                    }
                    if (x == 18 && z == 17) {
                        buildBirchTree(grid, x, ySurface, z, false);
                    }
                    if (isPlateau && rand.nextFloat() < 0.08f) {
                        setDoublePlant(grid, x, ySurface + 1, z, Blocks.ROSE_BUSH.defaultBlockState());
                    }
                }
        ));

        // 11. Dark Forest Biome
        PRESETS.add(new BiomePreset(
                "terrain_dark_forest",
                Biomes.DARK_FOREST,
                "zootycoon.terrain.biome.dark_forest",
                "Dark Forest Biome",
                4,
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 15 && z == 15) {
                        buildDarkOakTree(grid, x, ySurface, z);
                    }
                    if (x == 12 && z == 20) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.RED_MUSHROOM.defaultBlockState());
                    }
                    if (x == 20 && z == 12) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.BROWN_MUSHROOM.defaultBlockState());
                    }
                }
        ));

        // 12. Meadow Biome
        PRESETS.add(new BiomePreset(
                "terrain_meadow",
                Biomes.MEADOW,
                "zootycoon.terrain.biome.meadow",
                "Meadow Biome",
                4,
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 16 && z == 16) {
                        buildBirchTree(grid, x, ySurface, z, true);
                    }
                    if (isPlateau) {
                        float r = rand.nextFloat();
                        if (r < 0.08f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.ALLIUM.defaultBlockState());
                        } else if (r < 0.16f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.CORNFLOWER.defaultBlockState());
                        } else if (r < 0.24f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.OXEYE_DAISY.defaultBlockState());
                        } else if (r < 0.35f) {
                            setBlockSafe(grid, x, ySurface + 1, z, Blocks.SHORT_GRASS.defaultBlockState());
                        }
                    }
                }
        ));

        // 13. Cherry Grove Biome
        PRESETS.add(new BiomePreset(
                "terrain_cherry_grove",
                Biomes.CHERRY_GROVE,
                "zootycoon.terrain.biome.cherry_grove",
                "Cherry Grove Biome",
                4,
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                (grid, x, ySurface, z, plateauHeight, isPlateau, rand) -> {
                    if (x == 16 && z == 16) {
                        buildCherryTree(grid, x, ySurface, z);
                    }
                    if (isPlateau && rand.nextFloat() < 0.25f) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.PINK_PETALS.defaultBlockState().setValue(BlockStateProperties.FLOWER_AMOUNT, 3));
                    }
                }
        ));
    }

    /**
     * Ensures all default "already builded biome" schematics exist on disk.
     */
    public static void generateDefaultsIfMissing() {
        Path baseDir = ZooSchematicRegistry.getCustomSchematicsDir();
        for (BiomePreset preset : PRESETS) {
            Path nbtPath = baseDir.resolve(preset.idKey() + ".nbt");
            Path jsonPath = baseDir.resolve(preset.idKey() + ".json");
            if (!Files.exists(nbtPath) || !Files.exists(jsonPath)) {
                try {
                    generateAndSavePreset(preset, "World Generator", "Natural biome terrain from world generation with centered plateau.");
                } catch (Exception e) {
                    ZooTycoonMod.LOGGER.error("Failed to generate default terrain schematic: " + preset.idKey(), e);
                }
            }
        }
    }

    /**
     * Checks the active world generation (including modded world generation such as Tectonic).
     * If Tectonic or modded biomes are present, extracts authentic biome data and ensures
     * all worldgen biomes are registered in the terrain category.
     */
    public static void generateForWorld(ServerLevel overworld) {
        if (overworld == null) return;
        boolean isTectonic = FabricLoader.getInstance().isModLoaded("tectonic");
        String author = isTectonic ? "Tectonic WorldGen" : "World Generator";
        String desc = isTectonic
                ? "Tectonic authentic world generation biome terrain with centered plateau."
                : "Natural biome terrain from world generation with centered plateau.";

        Path baseDir = ZooSchematicRegistry.getCustomSchematicsDir();

        // 1. Ensure core presets are up-to-date with world generator author
        for (BiomePreset preset : PRESETS) {
            Path nbtPath = baseDir.resolve(preset.idKey() + ".nbt");
            Path jsonPath = baseDir.resolve(preset.idKey() + ".json");
            if (!Files.exists(nbtPath) || !Files.exists(jsonPath) || isTectonic) {
                try {
                    generateAndSavePreset(preset, author, desc);
                } catch (Exception e) {
                    ZooTycoonMod.LOGGER.error("Failed to generate terrain schematic for: " + preset.idKey(), e);
                }
            }
        }

        // 2. Discover any modded biomes in overworld generator
        try {
            var biomeSource = overworld.getChunkSource().getGenerator().getBiomeSource();
            Set<Holder<Biome>> possible = biomeSource.possibleBiomes();
            for (Holder<Biome> holder : possible) {
                holder.unwrapKey().ifPresent(key -> {
                    ResourceLocation loc = key.location();
                    if (!"minecraft".equals(loc.getNamespace())) {
                        String modId = loc.getNamespace();
                        String safeName = "terrain_" + modId + "_" + loc.getPath();
                        Path modNbt = baseDir.resolve(safeName + ".nbt");
                        Path modJson = baseDir.resolve(safeName + ".json");
                        if (!Files.exists(modNbt) || !Files.exists(modJson)) {
                            generateModdedBiome(safeName, key, loc, modId + " WorldGen", desc);
                        }
                    }
                });
            }
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.warn("Could not scan modded biomes: " + e.getMessage());
        }

        ZooSchematicRegistry.loadCustomSchematics();
    }

    private static void generateAndSavePreset(BiomePreset preset, String author, String description) throws Exception {
        BlockState[][][] grid = new BlockState[SIZE_X][SIZE_Y][SIZE_Z];
        Random rand = new Random(preset.idKey().hashCode());

        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                int elevation = calculateElevation(x, z, preset.plateauHeight());
                int ySurface = BASE_Y + elevation;
                boolean isPlateau = elevation >= preset.plateauHeight();

                // Foundation rock layers (y = 0 to ySurface - 4)
                for (int y = 0; y <= Math.max(0, ySurface - 4); y++) {
                    grid[x][y][z] = preset.foundationBlock();
                }

                // Subsurface layers (y = ySurface - 3 to ySurface - 1)
                for (int y = Math.max(0, ySurface - 3); y < ySurface; y++) {
                    grid[x][y][z] = preset.subsurfaceBlock();
                }

                // Surface layer
                grid[x][ySurface][z] = preset.surfaceBlock();

                // Biome decoration
                if (preset.decorator() != null) {
                    preset.decorator().decorate(grid, x, ySurface, z, preset.plateauHeight(), isPlateau, rand);
                }
            }
        }

        String displayName = ZooTranslations.get(preset.translationKey());
        if (displayName == null || displayName.isEmpty() || displayName.equals(preset.translationKey())) {
            displayName = preset.fallbackName();
        }

        saveSchematic(preset.idKey(), displayName, preset.biomeKey().location(), grid, author, description);
    }

    private static void generateModdedBiome(String idKey, ResourceKey<Biome> key, ResourceLocation loc, String author, String desc) {
        try {
            BlockState[][][] grid = new BlockState[SIZE_X][SIZE_Y][SIZE_Z];
            Random rand = new Random(idKey.hashCode());
            int plateauHeight = 4;

            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    int elevation = calculateElevation(x, z, plateauHeight);
                    int ySurface = BASE_Y + elevation;
                    boolean isPlateau = elevation >= plateauHeight;

                    for (int y = 0; y <= Math.max(0, ySurface - 4); y++) {
                        grid[x][y][z] = Blocks.STONE.defaultBlockState();
                    }
                    for (int y = Math.max(0, ySurface - 3); y < ySurface; y++) {
                        grid[x][y][z] = Blocks.DIRT.defaultBlockState();
                    }
                    grid[x][ySurface][z] = Blocks.GRASS_BLOCK.defaultBlockState();

                    if (x == 16 && z == 16) {
                        buildOakTree(grid, x, ySurface, z);
                    }
                    if (isPlateau && rand.nextFloat() < 0.15f) {
                        setBlockSafe(grid, x, ySurface + 1, z, Blocks.SHORT_GRASS.defaultBlockState());
                    }
                }
            }

            String rawName = loc.getPath().replace('_', ' ');
            String capName = Character.toUpperCase(rawName.charAt(0)) + rawName.substring(1) + " Biome";
            saveSchematic(idKey, capName, loc, grid, author, desc);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to generate modded biome: " + idKey, e);
        }
    }

    /**
     * Calculates the surface elevation above BASE_Y.
     * Guarantees that:
     * 1. The high plateau ("lempengan dataran tinggi") is in the CENTER (dist <= 0.32).
     * 2. The outer perimeter (border distance <= 2, or dist >= 0.78) is strictly 0 elevation.
     * 3. A smoothstep gradient connects the center to the outer perimeter, preventing any cut-off cliffs.
     */
    public static int calculateElevation(int x, int z, int plateauHeight) {
        float cx = 15.5f;
        float cz = 15.5f;
        float dx = (x - cx) / 15.5f;
        float dz = (z - cz) / 15.5f;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        // Organic natural contour noise (prevents a sterile geometric cone)
        float noise = 0.12f * (float) Math.sin(x * 0.5f + 1.0f) * (float) Math.cos(z * 0.5f + 0.5f)
                + 0.06f * (float) Math.cos((x + z) * 0.35f);
        float effectiveDist = dist + noise;

        int borderDist = Math.min(Math.min(x, 31 - x), Math.min(z, 31 - z));

        // Perimeter rings: strictly 0 elevation so edges are 100% flush at BASE_Y
        if (borderDist <= 2 || effectiveDist >= 0.78f) {
            return 0;
        }

        // Central Plateau ("lempengan dataran tinggi muncul di tengah")
        if (effectiveDist <= 0.32f) {
            int elev = plateauHeight;
            if (effectiveDist < 0.20f && (x + z) % 4 == 0) {
                elev = Math.min(plateauHeight + 1, plateauHeight + 1);
            }
            return elev;
        }

        // Smoothstep transition slope from plateau down to border
        float t = (0.78f - effectiveDist) / (0.78f - 0.32f);
        t = Math.clamp(t, 0.0f, 1.0f);
        float smoothT = t * t * (3.0f - 2.0f * t);
        int elev = Math.round(smoothT * plateauHeight);

        // Natural boundary falloff to ensure completely level outer edges
        if (borderDist <= 4) {
            elev = Math.min(elev, borderDist - 2);
        }
        return Math.max(0, elev);
    }

    public static void saveSchematic(
            String idName,
            String displayName,
            ResourceLocation biomeId,
            BlockState[][][] grid,
            String author,
            String description
    ) throws Exception {
        CompoundTag root = new CompoundTag();

        // 1. Dimensions
        ListTag sizeTag = new ListTag();
        sizeTag.add(IntTag.valueOf(SIZE_X));
        sizeTag.add(IntTag.valueOf(SIZE_Y));
        sizeTag.add(IntTag.valueOf(SIZE_Z));
        root.put("size", sizeTag);

        // 2. Palette and Block coordinates
        List<BlockState> paletteList = new ArrayList<>();
        Map<BlockState, Integer> paletteMap = new HashMap<>();

        // Ensure AIR is at index 0
        BlockState airState = Blocks.AIR.defaultBlockState();
        paletteList.add(airState);
        paletteMap.put(airState, 0);

        ListTag blocksTag = new ListTag();

        for (int y = 0; y < SIZE_Y; y++) {
            for (int z = 0; z < SIZE_Z; z++) {
                for (int x = 0; x < SIZE_X; x++) {
                    BlockState state = grid[x][y][z];
                    if (state == null || state.isAir()) continue;

                    int stateIndex = paletteMap.computeIfAbsent(state, s -> {
                        paletteList.add(s);
                        return paletteList.size() - 1;
                    });

                    CompoundTag bTag = new CompoundTag();
                    ListTag posTag = new ListTag();
                    posTag.add(IntTag.valueOf(x));
                    posTag.add(IntTag.valueOf(y));
                    posTag.add(IntTag.valueOf(z));
                    bTag.put("pos", posTag);
                    bTag.putInt("state", stateIndex);
                    blocksTag.add(bTag);
                }
            }
        }

        ListTag paletteTag = new ListTag();
        for (BlockState st : paletteList) {
            paletteTag.add(net.minecraft.nbt.NbtUtils.writeBlockState(st));
        }

        root.put("palette", paletteTag);
        root.put("blocks", blocksTag);
        root.put("entities", new ListTag());
        root.putInt("DataVersion", 3955);

        // 3. ZooBiomes tag: 3D biomes across quarts
        int qSizeX = SIZE_X / 4;
        int qSizeY = SIZE_Y / 4;
        int qSizeZ = SIZE_Z / 4;

        CompoundTag biomeTag = new CompoundTag();
        biomeTag.putInt("sizeX", qSizeX);
        biomeTag.putInt("sizeY", qSizeY);
        biomeTag.putInt("sizeZ", qSizeZ);

        ListTag palBiomeList = new ListTag();
        palBiomeList.add(StringTag.valueOf(biomeId.toString()));
        biomeTag.put("palette", palBiomeList);

        int[] biomeData = new int[qSizeX * qSizeY * qSizeZ]; // All 0
        biomeTag.putIntArray("data", biomeData);

        root.put("ZooBiomes", biomeTag);

        // 4. Write .nbt file
        Path baseDir = ZooSchematicRegistry.getCustomSchematicsDir();
        Path nbtPath = baseDir.resolve(idName + ".nbt");
        NbtIo.writeCompressed(root, nbtPath);

        // 5. Write .json metadata
        Path jsonPath = baseDir.resolve(idName + ".json");
        JsonObject obj = new JsonObject();
        obj.addProperty("id", "zootycoon:" + idName);
        obj.addProperty("name", displayName);
        obj.addProperty("category", "terrain");
        obj.addProperty("subcategory", "worldgen");
        obj.addProperty("author", author);
        obj.addProperty("description", description);
        obj.addProperty("hasBiomes", true);
        obj.addProperty("builderHiringCost", 15);
        obj.addProperty("defaultPrice", 30);

        JsonObject dim = new JsonObject();
        dim.addProperty("x", SIZE_X);
        dim.addProperty("y", SIZE_Y);
        dim.addProperty("z", SIZE_Z);
        obj.add("dimensions", dim);

        try (FileWriter writer = new FileWriter(jsonPath.toFile())) {
            new GsonBuilder().setPrettyPrinting().create().toJson(obj, writer);
        }
    }

    // Helper block placement methods
    private static void setBlockSafe(BlockState[][][] grid, int x, int y, int z, BlockState state) {
        if (x >= 0 && x < SIZE_X && y >= 0 && y < SIZE_Y && z >= 0 && z < SIZE_Z) {
            grid[x][y][z] = state;
        }
    }

    private static void setDoublePlant(BlockState[][][] grid, int x, int y, int z, BlockState plantState) {
        setBlockSafe(grid, x, y, z, plantState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
        setBlockSafe(grid, x, y + 1, z, plantState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static void buildAcaciaTree(BlockState[][][] grid, int x, int yBase, int z) {
        BlockState log = Blocks.ACACIA_LOG.defaultBlockState();
        BlockState leaves = Blocks.ACACIA_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);

        // Trunk
        for (int y = 1; y <= 5; y++) {
            setBlockSafe(grid, x, yBase + y, z, log);
        }
        // Diagonal branches
        setBlockSafe(grid, x + 1, yBase + 4, z, log);
        setBlockSafe(grid, x + 2, yBase + 5, z, log);
        setBlockSafe(grid, x - 1, yBase + 4, z + 1, log);
        setBlockSafe(grid, x - 2, yBase + 5, z + 1, log);

        // Umbrella canopy (5x5)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 4) {
                    setBlockSafe(grid, x + dx, yBase + 5, z + dz, leaves);
                }
                if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) {
                    setBlockSafe(grid, x + dx, yBase + 6, z + dz, leaves);
                }
            }
        }
    }

    private static void buildOakTree(BlockState[][][] grid, int x, int yBase, int z) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);

        for (int y = 1; y <= 5; y++) {
            setBlockSafe(grid, x, yBase + y, z, log);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 4; dy <= 6; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) + (dy == 6 ? 1 : 0) <= 3) {
                        setBlockSafe(grid, x + dx, yBase + dy, z + dz, leaves);
                    }
                }
            }
        }
    }

    private static void buildSpruceTree(BlockState[][][] grid, int x, int yBase, int z, boolean snowy) {
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState leaves = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);

        for (int y = 1; y <= 7; y++) {
            setBlockSafe(grid, x, yBase + y, z, log);
        }
        // Conical leaves
        for (int r = 2; r >= 0; r--) {
            int dy = 3 + (2 - r) * 2;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= r + 1) {
                        setBlockSafe(grid, x + dx, yBase + dy, z + dz, leaves);
                        if (snowy) {
                            setBlockSafe(grid, x + dx, yBase + dy + 1, z + dz, Blocks.SNOW.defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    private static void buildJungleTree(BlockState[][][] grid, int x, int yBase, int z) {
        BlockState log = Blocks.JUNGLE_LOG.defaultBlockState();
        BlockState leaves = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);

        for (int y = 1; y <= 8; y++) {
            setBlockSafe(grid, x, yBase + y, z, log);
            if (y >= 2 && y <= 6) {
                setBlockSafe(grid, x, yBase + y, z + 1, Blocks.VINE.defaultBlockState().setValue(BlockStateProperties.NORTH, true));
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 7; dy <= 9; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 3) {
                        setBlockSafe(grid, x + dx, yBase + dy, z + dz, leaves);
                    }
                }
            }
        }
    }

    private static void buildDarkOakTree(BlockState[][][] grid, int x, int yBase, int z) {
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);

        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                for (int y = 1; y <= 6; y++) {
                    setBlockSafe(grid, x + dx, yBase + y, z + dz, log);
                }
            }
        }
        for (int dx = -3; dx <= 4; dx++) {
            for (int dz = -3; dz <= 4; dz++) {
                for (int dy = 5; dy <= 7; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 5) {
                        setBlockSafe(grid, x + dx, yBase + dy, z + dz, leaves);
                    }
                }
            }
        }
    }

    private static void buildBirchTree(BlockState[][][] grid, int x, int yBase, int z, boolean withBeeNest) {
        BlockState log = Blocks.BIRCH_LOG.defaultBlockState();
        BlockState leaves = Blocks.BIRCH_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);

        for (int y = 1; y <= 6; y++) {
            setBlockSafe(grid, x, yBase + y, z, log);
        }
        if (withBeeNest) {
            setBlockSafe(grid, x, yBase + 2, z + 1, Blocks.BEE_NEST.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 5; dy <= 7; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 3) {
                        setBlockSafe(grid, x + dx, yBase + dy, z + dz, leaves);
                    }
                }
            }
        }
    }

    private static void buildCherryTree(BlockState[][][] grid, int x, int yBase, int z) {
        BlockState log = Blocks.CHERRY_LOG.defaultBlockState();
        BlockState leaves = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);

        for (int y = 1; y <= 5; y++) {
            setBlockSafe(grid, x, yBase + y, z, log);
        }
        setBlockSafe(grid, x + 1, yBase + 4, z, log);
        setBlockSafe(grid, x + 2, yBase + 5, z, log);

        for (int dx = -3; dx <= 4; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 4; dy <= 6; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 4) {
                        setBlockSafe(grid, x + dx, yBase + dy, z + dz, leaves);
                    }
                }
            }
        }
    }

    private static void buildMangroveTree(BlockState[][][] grid, int x, int yBase, int z) {
        BlockState log = Blocks.MANGROVE_LOG.defaultBlockState();
        BlockState roots = Blocks.MANGROVE_ROOTS.defaultBlockState();
        BlockState leaves = Blocks.MANGROVE_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);

        // Roots
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int y = 0; y <= 2; y++) {
                    setBlockSafe(grid, x + dx, yBase + y, z + dz, roots);
                }
            }
        }
        // Trunk
        for (int y = 3; y <= 7; y++) {
            setBlockSafe(grid, x, yBase + y, z, log);
        }
        // Canopy
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 6; dy <= 8; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 3) {
                        setBlockSafe(grid, x + dx, yBase + dy, z + dz, leaves);
                    }
                }
            }
        }
    }
}
