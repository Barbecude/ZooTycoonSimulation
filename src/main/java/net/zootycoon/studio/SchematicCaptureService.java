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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.network.ZooNetworkPayloads;
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
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            boolean foundAny = false;

            int radius = StudioDimensionManager.PLOT_RADIUS;
            int floorY = center.getY();

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dy = 1; dy <= StudioDimensionManager.MAX_BUILD_HEIGHT; dy++) {
                        BlockPos checkPos = new BlockPos(center.getX() + dx, floorY + dy, center.getZ() + dz);
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
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.studio.save_empty", 2);
                return false;
            }

            // Include +1 Y headroom so entities/NPCs standing on top of blocks are captured
            List<Entity> ents = level.getEntitiesOfClass(
                    Entity.class,
                    new AABB(minX, minY, minZ, maxX + 1, maxY + 3, maxZ + 1),
                    e -> !(e instanceof Player)
            );
            for (Entity e : ents) {
                maxY = Math.max(maxY, (int) Math.ceil(e.getBoundingBox().maxY));
            }

            min = new BlockPos(minX, minY, minZ);
            max = new BlockPos(maxX, maxY + 1, maxZ);
        } else {
            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());

            // If player selected two ground corners (shallow Y span), scan upward to capture full building & entities
            if (maxY - minY < 4) {
                int highestY = maxY;
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        for (int y = minY; y <= minY + 40; y++) {
                            if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                                highestY = Math.max(highestY, y);
                            }
                        }
                    }
                }
                List<Entity> ents = level.getEntitiesOfClass(
                        Entity.class,
                        new AABB(minX, minY, minZ, maxX + 1, minY + 40, maxZ + 1),
                        e -> !(e instanceof Player)
                );
                for (Entity e : ents) {
                    highestY = Math.max(highestY, (int) Math.ceil(e.getBoundingBox().maxY));
                }
                maxY = highestY + 2;
            } else {
                maxY += 2;
            }

            min = new BlockPos(minX, minY, minZ);
            max = new BlockPos(maxX, maxY, maxZ);
        }

        Vec3i size = new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);

        StructureTemplate template = new StructureTemplate();
        // includeEntities = true captures all NPCs & animals inside the schematic without counting them as block materials!
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

            // 2. Save .nbt with StructureTemplate blocks + entities + ZooBiomes
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

            // 4. Register into memory
            ZooSchematic customSchem = new ZooSchematic(
                    "zootycoon:" + safeId, schematicName, category, "custom",
                    player.getName().getString(), size, description,
                    obj.get("builderHiringCost").getAsInt(),
                    obj.get("defaultPrice").getAsInt(),
                    true, ZooTycoonMod.id(safeId)
            );
            ZooSchematicRegistry.register(customSchem);
            ZooSchematicRegistry.loadCustomSchematics();

            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.studio.save_success", 1, schematicName, category);
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);

            ZooTycoonMod.LOGGER.info("Captured and saved custom schematic: {} (Size: {}x{}x{})", schematicName, size.getX(), size.getY(), size.getZ());
            return true;
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to capture schematic", e);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.error", "zootycoon.studio.save_empty", 3);
            return false;
        }
    }
}
