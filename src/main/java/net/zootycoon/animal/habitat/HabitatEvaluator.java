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
import net.zootycoon.block.ZooFeedingTroughBlockEntity;

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
            boolean companionRequired,
            boolean companionOk,
            List<String> unmetNeeds
    ) {
        public String encodePayloadString() {
            StringBuilder sb = new StringBuilder();
            sb.append("status=").append(status.name())
                    .append(";eco=").append(ecosystemName)
                    .append(";biome=").append(biomeOk ? "OK" : "NO")
                    .append(";water=").append(waterRequired ? (waterOk ? "OK" : "NO") : "NA")
                    .append(";temp=").append(tempOk ? "OK" : (tooCold ? "HEATER" : "COOLER"))
                    .append(";ground=").append(groundOk ? "OK" : "NO")
                    .append(";mate=").append(companionRequired ? (companionOk ? "OK" : "NO") : "NA");

            if (unmetNeeds != null && !unmetNeeds.isEmpty()) {
                sb.append(";unmet=").append(String.join("|", unmetNeeds));
            }
            return sb.toString();
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
     * Determines whether an animal is social (needs a herd/partner) or solitary (lives independently).
     * Solitary animals (like tigers, bears, crocodiles, snapping turtles, snakes) do not require a partner.
     */
    public static boolean isSocialAnimal(LivingEntity animal, HabitatProfile profile) {
        if (animal == null) return false;
        String typeStr = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).getPath().toLowerCase(Locale.ROOT);
        // Solitary species that live independently
        if (typeStr.contains("turtle") || typeStr.contains("alligator") || typeStr.contains("crocodile")
                || typeStr.contains("tiger") || typeStr.contains("bear") || typeStr.contains("leopard")
                || typeStr.contains("snake") || typeStr.contains("rattlesnake") || typeStr.contains("platypus")
                || typeStr.contains("komodo") || typeStr.contains("tortoise") || typeStr.contains("rhino")
                || typeStr.contains("moose")) {
            return false;
        }
        return true;
    }

    private record CachedScan(
            long tick,
            ResourceLocation dimension,
            double x,
            double y,
            double z,
            BlockPos pos,
            Map<String, Integer> counts
    ) {}

    private static final Map<Integer, CachedScan> SCAN_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static void reset() {
        SCAN_CACHE.clear();
    }

    /**
     * Evaluates the current habitat suitability for an animal.
     */
    public static HabitatResult evaluate(ServerLevel level, LivingEntity animal) {
        if (level == null || animal == null || !animal.isAlive()) {
            return new HabitatResult(HabitatStatus.NO_DATA, "-", false, false, false, false, false, false, false, false, Collections.emptyList());
        }

        HabitatProfile profile = HabitatProfileLoader.getProfile(animal);
        if (profile == null) {
            return new HabitatResult(HabitatStatus.NO_DATA, "-", false, false, false, false, false, false, false, false, Collections.emptyList());
        }

        // 1. Check surrounding blocks with extended range (radius 24, dy -6..6) with memoization cache
        long currentTick = level.getGameTime();
        BlockPos currentPos = animal.blockPosition();
        CachedScan cached = SCAN_CACHE.get(animal.getId());

        Map<String, Integer> blockCounts;
        if (cached != null
                && cached.dimension.equals(level.dimension().location())
                && (currentTick >= cached.tick)
                && (currentTick - cached.tick <= 40)
                && (animal.distanceToSqr(cached.x, cached.y, cached.z) <= 4.0 || currentPos.closerThan(cached.pos, 2.01))) {
            blockCounts = cached.counts;
        } else {
            blockCounts = scanSurroundingBlocks(level, currentPos);
            SCAN_CACHE.put(animal.getId(), new CachedScan(
                    currentTick,
                    level.dimension().location(),
                    animal.getX(),
                    animal.getY(),
                    animal.getZ(),
                    currentPos.immutable(),
                    blockCounts
            ));
        }

        // 2. Check Natural Ground (dirt, podzol, coarse dirt, sand, gravel, mud, snow, etc.)
        int groundCount = blockCounts.getOrDefault("natural_ground", 0);
        boolean groundOk = groundCount >= 4;

        // 3. Check Water: generous detection radius so animals in big enclosures don't lose water
        boolean waterRequired = profile.requiresWater();
        int waterCount = blockCounts.getOrDefault("water", 0);
        boolean waterOk = !waterRequired || waterCount >= 2;

        // 4. Check Biome (Biome match)
        Holder<Biome> currentBiome = level.getBiome(animal.blockPosition());
        boolean biomeOk = profile.matchesBiome(currentBiome);

        // 5. Check Effective Temperature & Thermal Blocks (Heater & Cooler Box only when season is unsuitable)
        float effectiveTemp = ZooClimate.getEffectiveTemperature(level, animal.blockPosition());
        boolean tooCold = effectiveTemp < profile.minTemperature();
        boolean tooHot = effectiveTemp > profile.maxTemperature();
        boolean tempOk = !tooCold && !tooHot;

        // 6. Check Companion (ONLY required for social animals, radius 24)
        boolean companionRequired = isSocialAnimal(animal, profile);
        boolean hasCompanion = !companionRequired || !level.getEntitiesOfClass(animal.getClass(), animal.getBoundingBox().inflate(24.0), e -> e != animal && e.isAlive()).isEmpty();

        // 7. Check Required Blocks (Trees, Bamboo, Snow, Sand, etc.)
        boolean extraBlocksOk = true;
        List<String> unmet = new ArrayList<>();

        if (profile.requiredBlocks() != null) {
            for (HabitatProfile.BlockRequirement req : profile.requiredBlocks()) {
                String type = req.type().toLowerCase(Locale.ROOT);
                int count;
                if ("water".equals(type)) {
                    count = waterCount;
                } else if ("trees".equals(type) || "tree".equals(type)) {
                    count = blockCounts.getOrDefault("trees", 0);
                } else if ("bamboo".equals(type)) {
                    count = blockCounts.getOrDefault("bamboo", 0);
                } else if ("sand".equals(type)) {
                    count = blockCounts.getOrDefault("sand", 0);
                } else if ("snow".equals(type)) {
                    count = blockCounts.getOrDefault("snow", 0);
                } else if ("grass".equals(type)) {
                    count = blockCounts.getOrDefault("grass", 0);
                } else {
                    count = blockCounts.getOrDefault(type, 0);
                }

                if (count < req.minCount()) {
                    extraBlocksOk = false;
                    String label = (req.label() != null && !req.label().isEmpty()) ? req.label() : capitalize(type);
                    if (!unmet.contains("Butuh " + label)) {
                        unmet.add("Butuh " + label);
                    }
                }
            }
        }

        if (!biomeOk) {
            unmet.add("Bioma: " + profile.displayName());
        }
        if (waterRequired && !waterOk && !unmet.contains("Butuh Air")) {
            unmet.add("Butuh Air");
        }
        if (!groundOk && !unmet.contains("Butuh Tanah Alami")) {
            unmet.add("Butuh Tanah Alami");
        }
        if (companionRequired && !hasCompanion && !unmet.contains("Butuh Pasangan")) {
            unmet.add("Butuh Pasangan");
        }
        if (tooCold) {
            unmet.add("Butuh Heater");
        } else if (tooHot) {
            unmet.add("Butuh Cooler Box");
        }

        boolean isSuitable = biomeOk && tempOk && groundOk && waterOk && extraBlocksOk && (!companionRequired || hasCompanion);
        HabitatStatus status = isSuitable ? HabitatStatus.SUITABLE : HabitatStatus.UNSUITABLE;

        return new HabitatResult(status, profile.displayName(), biomeOk, waterRequired, waterOk, tempOk, tooCold, groundOk, companionRequired, hasCompanion, unmet);
    }

    private static Map<String, Integer> scanSurroundingBlocks(ServerLevel level, BlockPos center) {
        Map<String, Integer> counts = new HashMap<>();
        int radius = 24;
        int minDy = -6;
        int maxDy = 6;

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

                    // Natural Ground: checks NATURAL_GROUND tag AND all common natural surface blocks
                    if (state.is(NATURAL_GROUND) || state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)
                            || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.GRAVEL)
                            || state.is(Blocks.MUD) || state.is(Blocks.PACKED_MUD) || state.is(Blocks.CLAY)
                            || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.COARSE_DIRT)
                            || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MYCELIUM) || state.is(BlockTags.SNOW)) {
                        counts.merge("natural_ground", 1, Integer::sum);
                    }

                    // Water block
                    if (state.getFluidState().is(FluidTags.WATER) || state.is(Blocks.WATER)) {
                        counts.merge("water", 1, Integer::sum);
                    }

                    // Water in Feeding Trough
                    if (level.getBlockEntity(pos) instanceof ZooFeedingTroughBlockEntity trough && trough.hasWater()) {
                        counts.merge("water", 4, Integer::sum);
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

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
