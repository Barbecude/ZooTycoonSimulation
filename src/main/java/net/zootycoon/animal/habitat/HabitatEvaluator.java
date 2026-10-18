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

    public static final TagKey<Block> NATURAL_GROUND = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("zootycoon", "natural_ground"));

    public record HabitatResult(
            HabitatStatus status,
            String ecosystemName,
            boolean biomeOk,
            boolean waterRequired,
            boolean waterOk,
            boolean tempOk,
            boolean tooCold,
            boolean groundOk,
            boolean companionOk,
            List<String> unmetNeeds
    ) {
        public String encodePayloadString() {
            return "status=" + status.name()
                    + ";eco=" + ecosystemName
                    + ";biome=" + (biomeOk ? "OK" : "NO")
                    + ";water=" + (waterRequired ? (waterOk ? "OK" : "NO") : "NA")
                    + ";temp=" + (tempOk ? "OK" : (tooCold ? "HEATER" : "COOLER"))
                    + ";ground=" + (groundOk ? "OK" : "NO")
                    + ";mate=" + (companionOk ? "OK" : "NO");
        }

        public String formatHint() {
            if (unmetNeeds == null || unmetNeeds.isEmpty()) {
                return "";
            }
            if (unmetNeeds.size() == 1) {
                return "Butuh: " + unmetNeeds.getFirst();
            }
            return "Butuh: " + String.join(", ", unmetNeeds);
        }
    }

    /**
     * Evaluates the current habitat suitability for an animal.
     */
    public static HabitatResult evaluate(ServerLevel level, LivingEntity animal) {
        if (level == null || animal == null || !animal.isAlive()) {
            return new HabitatResult(HabitatStatus.NO_DATA, "-", false, false, false, false, false, false, false, Collections.emptyList());
        }

        HabitatProfile profile = HabitatProfileLoader.getProfile(animal);
        if (profile == null) {
            return new HabitatResult(HabitatStatus.NO_DATA, "-", false, false, false, false, false, false, false, Collections.emptyList());
        }

        // 1. Check surrounding blocks (radius 8, dy -4..4)
        Map<String, Integer> blockCounts = scanSurroundingBlocks(level, animal.blockPosition());

        // 2. Check Natural Ground (Point 6: coarse dirt, podzol, sand, etc.)
        int groundCount = blockCounts.getOrDefault("natural_ground", 0);
        boolean groundOk = groundCount >= 8;

        // 3. Check Water (Point 7: Pengecualian air dicek terpisah, hanya untuk hewan yang butuh air)
        boolean waterRequired = profile.requiresWater();
        int waterCount = blockCounts.getOrDefault("water", 0);
        boolean waterOk = !waterRequired || waterCount >= 4;

        // 4. Check Biome (Point 7: Biome terpenuhi = habitat terpenuhi)
        Holder<Biome> currentBiome = level.getBiome(animal.blockPosition());
        boolean biomeOk = profile.matchesBiome(currentBiome);

        // 5. Check Effective Temperature & Thermal Blocks (Point 8: Heater & Cooler Box)
        float effectiveTemp = ZooClimate.getEffectiveTemperature(level, animal.blockPosition());
        boolean tooCold = effectiveTemp < profile.minTemperature();
        boolean tooHot = effectiveTemp > profile.maxTemperature();
        boolean tempOk = !tooCold && !tooHot;

        // 6. Check Companion (living entity of same species in radius 16)
        boolean hasCompanion = !level.getEntitiesOfClass(animal.getClass(), animal.getBoundingBox().inflate(16.0), e -> e != animal && e.isAlive()).isEmpty();

        List<String> unmet = new ArrayList<>();
        if (!biomeOk) {
            unmet.add("Bioma: Perlu " + profile.displayName());
        }
        if (waterRequired && !waterOk) {
            unmet.add("Air: Butuh Air");
        }
        if (tooCold) {
            unmet.add("Suhu: Butuh Heater");
        } else if (tooHot) {
            unmet.add("Suhu: Butuh Cooler Box");
        }
        if (!groundOk) {
            unmet.add("Tanah: Butuh Tanah Alami");
        }
        if (!hasCompanion) {
            unmet.add("Pasangan: Butuh Teman");
        }

        // Rule 7: Biome terpenuhi = habitat terpenuhi, dan sebaliknya.
        // Pengecualian: air dicek terpisah, hanya untuk hewan yang butuh air.
        // Jika air tidak ada di sekitar, habitat tetap belum terpenuhi walau biome cocok.
        boolean isSuitable = biomeOk && tempOk && groundOk && waterOk;
        HabitatStatus status = isSuitable ? HabitatStatus.SUITABLE : HabitatStatus.UNSUITABLE;

        return new HabitatResult(status, profile.displayName(), biomeOk, waterRequired, waterOk, tempOk, tooCold, groundOk, hasCompanion, unmet);
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

                    // Natural Ground block tag (coarse dirt, podzol, sand, gravel, modded soil)
                    if (state.is(NATURAL_GROUND)) {
                        counts.merge("natural_ground", 1, Integer::sum);
                    }

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

