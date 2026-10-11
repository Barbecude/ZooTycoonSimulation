package net.zootycoon.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;

import java.util.*;

public class ZooBiomeApplier {

    public static boolean applySchematicBiomes(ServerLevel level, BlockPos origin, BoundingBox bb, ZooSchematic schematic, Rotation rotation) {
        if (level == null || origin == null || schematic == null) return false;

        CompoundTag rootTag = ZooSchematicRegistry.loadSchematicTag(level.getServer(), schematic);
        if (rootTag == null || !rootTag.contains("ZooBiomes")) {
            return false;
        }

        CompoundTag biomeTag = rootTag.getCompound("ZooBiomes");
        int qSizeX = biomeTag.getInt("sizeX");
        int qSizeY = biomeTag.getInt("sizeY");
        int qSizeZ = biomeTag.getInt("sizeZ");
        int[] data = biomeTag.getIntArray("data");

        if (qSizeX <= 0 || qSizeY <= 0 || qSizeZ <= 0 || data == null || data.length < qSizeX * qSizeY * qSizeZ) {
            return false;
        }

        // 1. Resolve palette
        ListTag palList = biomeTag.getList("palette", Tag.TAG_STRING);
        List<Holder<Biome>> paletteHolders = new ArrayList<>();
        var biomeRegistry = level.registryAccess().lookupOrThrow(Registries.BIOME);
        Holder.Reference<Biome> fallbackPlains = biomeRegistry.getOrThrow(Biomes.PLAINS);

        for (int i = 0; i < palList.size(); i++) {
            String idStr = palList.getString(i);
            if (idStr.isEmpty()) idStr = "minecraft:plains";
            try {
                ResourceLocation id = ResourceLocation.parse(idStr);
                var key = ResourceKey.create(Registries.BIOME, id);
                var holderOpt = biomeRegistry.get(key);
                paletteHolders.add(holderOpt.orElse(fallbackPlains));
            } catch (Exception e) {
                paletteHolders.add(fallbackPlains);
            }
        }

        if (paletteHolders.isEmpty()) {
            return false;
        }

        // 2. Map quart positions with rotation
        StructurePlaceSettings settings = new StructurePlaceSettings();
        if (rotation != null) {
            settings.setRotation(rotation);
        }

        Map<BlockPos, Holder<Biome>> quartBiomeMap = new HashMap<>();
        Set<ChunkPos> affectedChunks = new HashSet<>();

        for (int ly = 0; ly < qSizeY; ly++) {
            for (int lz = 0; lz < qSizeZ; lz++) {
                for (int lx = 0; lx < qSizeX; lx++) {
                    int index = (ly * qSizeZ + lz) * qSizeX + lx;
                    int palIndex = data[index];
                    if (palIndex < 0 || palIndex >= paletteHolders.size()) continue;

                    Holder<Biome> biomeHolder = paletteHolders.get(palIndex);

                    // Local block position relative to schematic min
                    BlockPos localBlock = new BlockPos(lx << 2, ly << 2, lz << 2);
                    BlockPos rotBlock = StructureTemplate.calculateRelativePosition(settings, localBlock);
                    BlockPos worldBlock = origin.offset(rotBlock);

                    int targetQx = QuartPos.fromBlock(worldBlock.getX());
                    int targetQy = QuartPos.fromBlock(worldBlock.getY());
                    int targetQz = QuartPos.fromBlock(worldBlock.getZ());

                    BlockPos targetQuartPos = new BlockPos(targetQx, targetQy, targetQz);
                    quartBiomeMap.put(targetQuartPos, biomeHolder);

                    int blockX = QuartPos.toBlock(targetQx);
                    int blockZ = QuartPos.toBlock(targetQz);
                    affectedChunks.add(new ChunkPos(blockX >> 4, blockZ >> 4));
                }
            }
        }

        if (quartBiomeMap.isEmpty()) {
            return false;
        }

        // 3. Fill biomes into affected chunks
        List<ChunkAccess> updatedChunks = new ArrayList<>();
        var sampler = level.getChunkSource().randomState().sampler();
        for (ChunkPos cp : affectedChunks) {
            LevelChunk chunk = level.getChunk(cp.x, cp.z);
            if (chunk == null) continue;

            BiomeResolver resolver = (int qx, int qy, int qz, Climate.Sampler sam) -> {
                Holder<Biome> target = quartBiomeMap.get(new BlockPos(qx, qy, qz));
                if (target != null) {
                    return target;
                }
                return chunk.getNoiseBiome(qx, qy, qz);
            };
            chunk.fillBiomesFromNoise(resolver, sampler);

            chunk.setUnsaved(true);
            updatedChunks.add(chunk);
        }

        // 4. Broadcast instant biome update to clients
        if (!updatedChunks.isEmpty()) {
            level.getChunkSource().chunkMap.resendBiomesForChunks(updatedChunks);
            ZooTycoonMod.LOGGER.info("Applied {} biome quarts across {} chunks for schematic {}",
                    quartBiomeMap.size(), updatedChunks.size(), schematic.getName());
            return true;
        }

        return false;
    }
}
