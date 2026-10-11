package net.zootycoon.studio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;

public class SchematicCaptureService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean captureAndSave(ServerPlayer player, BlockPos pos1, BlockPos pos2,
                                         String schematicName, String category, String description) {
        ServerLevel level = (ServerLevel) player.level();

        BlockPos center = StudioDimensionManager.getPlotCenter(player.getUUID());
        if (center == null) {
            center = player.blockPosition();
        }

        BlockPos min;
        BlockPos max;

        if (pos1.equals(BlockPos.ZERO) && pos2.equals(BlockPos.ZERO)) {
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = 101, maxY = 101;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            boolean foundAny = false;

            int radius = 64;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dy = 1; dy <= 80; dy++) {
                        BlockPos checkPos = new BlockPos(center.getX() + dx, 100 + dy, center.getZ() + dz);
                        if (!level.getBlockState(checkPos).isAir()) {
                            foundAny = true;
                            minX = Math.min(minX, checkPos.getX());
                            maxX = Math.max(maxX, checkPos.getX());
                            minY = Math.min(minY, checkPos.getY());
                            maxY = Math.max(maxY, checkPos.getY());
                            minZ = Math.min(minZ, checkPos.getZ());
                            maxZ = Math.max(maxZ, checkPos.getZ());
                        }
                    }
                }
            }

            if (!foundAny) {
                min = new BlockPos(center.getX() - 8, 100, center.getZ() - 8);
                max = new BlockPos(center.getX() + 8, 108, center.getZ() + 8);
            } else {
                min = new BlockPos(minX, minY, minZ);
                max = new BlockPos(maxX, maxY, maxZ);
            }
        } else {
            min = new BlockPos(
                    Math.min(pos1.getX(), pos2.getX()),
                    Math.min(pos1.getY(), pos2.getY()),
                    Math.min(pos1.getZ(), pos2.getZ())
            );
            max = new BlockPos(
                    Math.max(pos1.getX(), pos2.getX()),
                    Math.max(pos1.getY(), pos2.getY()),
                    Math.max(pos1.getZ(), pos2.getZ())
            );
        }

        Vec3i size = new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);

        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, min, size, true, Blocks.BEDROCK);

        String safeId = "custom_" + schematicName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        Path baseDir = ZooSchematicRegistry.getCustomSchematicsDir();

        try {
            // 1. Capture 3D Biome noise data across the schematic volume
            int minQx = net.minecraft.core.QuartPos.fromBlock(min.getX());
            int minQy = net.minecraft.core.QuartPos.fromBlock(min.getY());
            int minQz = net.minecraft.core.QuartPos.fromBlock(min.getZ());
            int maxQx = net.minecraft.core.QuartPos.fromBlock(max.getX());
            int maxQy = net.minecraft.core.QuartPos.fromBlock(max.getY());
            int maxQz = net.minecraft.core.QuartPos.fromBlock(max.getZ());

            int qSizeX = maxQx - minQx + 1;
            int qSizeY = maxQy - minQy + 1;
            int qSizeZ = maxQz - minQz + 1;

            List<String> palette = new java.util.ArrayList<>();
            java.util.Map<String, Integer> paletteIndices = new java.util.HashMap<>();
            int[] biomeIndices = new int[qSizeX * qSizeY * qSizeZ];

            for (int ly = 0; ly < qSizeY; ly++) {
                for (int lz = 0; lz < qSizeZ; lz++) {
                    for (int lx = 0; lx < qSizeX; lx++) {
                        int qx = minQx + lx;
                        int qy = minQy + ly;
                        int qz = minQz + lz;
                        var holder = level.getNoiseBiome(qx, qy, qz);
                        String biomeId = holder.unwrapKey().map(k -> k.location().toString()).orElse("minecraft:plains");

                        int palIdx = paletteIndices.computeIfAbsent(biomeId, id -> {
                            palette.add(id);
                            return palette.size() - 1;
                        });

                        int flatIndex = (ly * qSizeZ + lz) * qSizeX + lx;
                        biomeIndices[flatIndex] = palIdx;
                    }
                }
            }

            net.minecraft.nbt.CompoundTag biomeTag = new net.minecraft.nbt.CompoundTag();
            biomeTag.putInt("sizeX", qSizeX);
            biomeTag.putInt("sizeY", qSizeY);
            biomeTag.putInt("sizeZ", qSizeZ);
            net.minecraft.nbt.ListTag paletteList = new net.minecraft.nbt.ListTag();
            for (String pid : palette) {
                paletteList.add(net.minecraft.nbt.StringTag.valueOf(pid));
            }
            biomeTag.put("palette", paletteList);
            biomeTag.putIntArray("data", biomeIndices);

            // 2. Save .nbt with StructureTemplate blocks + ZooBiomes
            File nbtFile = baseDir.resolve(safeId + ".nbt").toFile();
            CompoundTag nbtData = template.save(new CompoundTag());
            nbtData.put("ZooBiomes", biomeTag);
            NbtIo.writeCompressed(nbtData, nbtFile.toPath());

            // 3. Save .json metadata
            File jsonFile = baseDir.resolve(safeId + ".json").toFile();
            JsonObject obj = new JsonObject();
            obj.addProperty("id", "zootycoon:" + safeId);
            obj.addProperty("name", schematicName);
            obj.addProperty("category", category);
            obj.addProperty("subcategory", "custom");
            obj.addProperty("author", player.getName().getString());
            obj.addProperty("description", description);
            obj.addProperty("hasBiomes", true);
            obj.addProperty("builderHiringCost", Math.max(10, (size.getX() * size.getZ()) / 20));
            obj.addProperty("defaultPrice", Math.max(15, (size.getX() * size.getZ()) / 10));

            JsonObject dim = new JsonObject();
            dim.addProperty("x", size.getX());
            dim.addProperty("y", size.getY());
            dim.addProperty("z", size.getZ());
            obj.add("dimensions", dim);

            try (FileWriter writer = new FileWriter(jsonFile)) {
                GSON.toJson(obj, writer);
            }

            // 3. Register into memory
            ZooSchematic customSchem = new ZooSchematic(
                    "zootycoon:" + safeId, schematicName, category, "custom",
                    player.getName().getString(), size, description,
                    obj.get("builderHiringCost").getAsInt(),
                    obj.get("defaultPrice").getAsInt(),
                    true, ZooTycoonMod.id(safeId)
            );
            ZooSchematicRegistry.register(customSchem);

            ZooTycoonMod.LOGGER.info("Captured and saved custom schematic: {} (Size: {}x{}x{})", schematicName, size.getX(), size.getY(), size.getZ());
            return true;
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to capture schematic", e);
            return false;
        }
    }
}

