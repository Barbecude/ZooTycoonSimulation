package net.zootycoon.animal.habitat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.zootycoon.animal.ZooClimate;

import java.util.*;

/**
 * Server-side evaluator for animal habitat suitability and ecosystem block requirements.
 */
public class HabitatEvaluator {

    public enum HabitatStatus {
        SUITABLE,
        UNSUITABLE,
        NO_DATA
    }

    public record HabitatResult(
            HabitatStatus status,
            String ecosystemName,
            List<String> unmetNeeds
    ) {
        /**
         * Formats a clear, concise hint line for unmet habitat needs.
         * Examples:
         * - "Butuh: Air, Savana"
         * - "Butuh: Air, Pepohonan"
         * - "Bioma: Perlu Savana"
         * - "Suhu: Terlalu Dingin"
         */
        public String formatHint() {
            if (unmetNeeds == null || unmetNeeds.isEmpty()) {
                return "";
            }

            // Single need cases
            if (unmetNeeds.size() == 1) {
                String single = unmetNeeds.getFirst();
                if (single.startsWith("Suhu:")) {
                    return single;
                }
                if (single.startsWith("Bioma:")) {
                    return single;
                }
                return "Butuh: " + single;
            }

            // Multiple needs: join into "Butuh: Item1, Item2"
            List<String> cleanList = new ArrayList<>(unmetNeeds.size());
            for (String item : unmetNeeds) {
                if (item.startsWith("Bioma: Perlu ")) {
                    cleanList.add(item.substring("Bioma: Perlu ".length()));
                } else if (item.startsWith("Bioma: ")) {
                    cleanList.add(item.substring("Bioma: ".length()));
                } else if (item.startsWith("Suhu: ")) {
                    cleanList.add(item);
                } else {
                    cleanList.add(item);
                }
            }
            return "Butuh: " + String.join(", ", cleanList);
        }
    }

    /**
     * Evaluates the current habitat suitability for an animal.
     */
    public static HabitatResult evaluate(ServerLevel level, LivingEntity animal) {
        if (level == null || animal == null || !animal.isAlive()) {
            return new HabitatResult(HabitatStatus.NO_DATA, "-", Collections.emptyList());
        }

        HabitatProfile profile = HabitatProfileLoader.getProfile(animal);
        if (profile == null) {
            return new HabitatResult(HabitatStatus.NO_DATA, "-", Collections.emptyList());
        }

        List<String> unmet = new ArrayList<>();

        // 1. Check Ecosystem Blocks (radius 8, dy -4..4)
        Map<String, Integer> blockCounts = scanSurroundingBlocks(level, animal.blockPosition());
        for (HabitatProfile.BlockRequirement req : profile.requiredBlocks()) {
            int count = getRequirementCount(level, blockCounts, req);
            if (count < req.minCount()) {
                unmet.add(req.label());
            }
        }

        // 2. Check Biome
        Holder<Biome> currentBiome = level.getBiome(animal.blockPosition());
        if (!profile.matchesBiome(currentBiome)) {
            unmet.add("Bioma: Perlu " + profile.displayName());
        }

        // 3. Check Effective Temperature
        float effectiveTemp = ZooClimate.getEffectiveTemperature(level, animal.blockPosition());
        if (effectiveTemp < profile.minTemperature()) {
            unmet.add("Suhu: Terlalu Dingin");
        } else if (effectiveTemp > profile.maxTemperature()) {
            unmet.add("Suhu: Terlalu Panas");
        }

        if (unmet.isEmpty()) {
            return new HabitatResult(HabitatStatus.SUITABLE, profile.displayName(), Collections.emptyList());
        } else {
            return new HabitatResult(HabitatStatus.UNSUITABLE, profile.displayName(), unmet);
        }
    }

    private static Map<String, Integer> scanSurroundingBlocks(ServerLevel level, BlockPos center) {
        Map<String, Integer> counts = new HashMap<>();
        int radius = 8;
        int minDy = -4;
        int maxDy = 4;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                for (int dy = minDy; dy <= maxDy; dy++) {
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;

                    // Water
                    if (state.getFluidState().is(FluidTags.WATER) || state.is(Blocks.WATER)) {
                        counts.merge("water", 1, Integer::sum);
                    }

                    // Trees: Leaves & Logs
                    if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)) {
                        counts.merge("trees", 1, Integer::sum);
                        counts.merge("leaves", 1, Integer::sum);
                    }

                    // Grass / Dirt
                    if (state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.DIRT)) {
                        counts.merge("grass", 1, Integer::sum);
                    }

                    // Sand
                    if (state.is(BlockTags.SAND)) {
                        counts.merge("sand", 1, Integer::sum);
                    }

                    // Snow & Ice
                    if (state.is(BlockTags.SNOW) || state.is(BlockTags.ICE)
                            || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW_BLOCK)) {
                        counts.merge("snow", 1, Integer::sum);
                    }

                    // Bamboo
                    if (state.is(Blocks.BAMBOO) || state.is(Blocks.BAMBOO_SAPLING)) {
                        counts.merge("bamboo", 1, Integer::sum);
                    }

                    // Mud
                    if (state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS)) {
                        counts.merge("mud", 1, Integer::sum);
                    }
                }
            }
        }
        return counts;
    }

    private static int getRequirementCount(ServerLevel level, Map<String, Integer> scannedCounts, HabitatProfile.BlockRequirement req) {
        if (req.type() != null) {
            String type = req.type().toLowerCase(Locale.ROOT);
            Integer count = scannedCounts.get(type);
            if (count != null) {
                return count;
            }
        }

        // Custom tag or blocks fallback
        if (req.tag() != null) {
            String clean = req.tag().startsWith("#") ? req.tag().substring(1) : req.tag();
            ResourceLocation tagLoc = ResourceLocation.tryParse(clean);
            if (tagLoc != null) {
                TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagLoc);
                return countBlocksMatching(level, tagKey);
            }
        }

        if (req.blocks() != null && !req.blocks().isEmpty()) {
            return countBlocksMatching(level, req.blocks());
        }

        return 0;
    }

    private static int countBlocksMatching(ServerLevel level, TagKey<Block> tagKey) {
        int count = 0;
        // Basic fallback count
        return count;
    }

    private static int countBlocksMatching(ServerLevel level, List<ResourceLocation> blockIds) {
        int count = 0;
        return count;
    }
}

