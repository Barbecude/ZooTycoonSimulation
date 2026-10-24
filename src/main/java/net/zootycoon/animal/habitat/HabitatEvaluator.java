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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

        // 6. Companion requirement removed per user request
        boolean companionRequired = false;
        boolean hasCompanion = true;

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
                } else if ("mud".equals(type)) {
                    count = blockCounts.getOrDefault("mud", 0);
                } else if ("lily_pad".equals(type)) {
                    count = blockCounts.getOrDefault("lily_pad", 0);
                } else if ("ice".equals(type)) {
                    count = blockCounts.getOrDefault("ice", 0);
                } else if ("acacia".equals(type)) {
                    count = blockCounts.getOrDefault("acacia", 0);
                } else if ("spruce".equals(type)) {
                    count = blockCounts.getOrDefault("spruce", 0);
                } else if ("jungle_wood".equals(type)) {
                    count = blockCounts.getOrDefault("jungle_wood", 0);
                } else if ("vines".equals(type)) {
                    count = blockCounts.getOrDefault("vines", 0);
                } else if ("flowers".equals(type)) {
                    count = blockCounts.getOrDefault("flowers", 0);
                } else if ("hay".equals(type)) {
                    count = blockCounts.getOrDefault("hay", 0);
                } else if ("sweet_berries".equals(type)) {
                    count = blockCounts.getOrDefault("sweet_berries", 0);
                } else if ("terracotta".equals(type)) {
                    count = blockCounts.getOrDefault("terracotta", 0);
                } else if ("red_sand".equals(type)) {
                    count = blockCounts.getOrDefault("red_sand", 0);
                } else if ("sandstone".equals(type)) {
                    count = blockCounts.getOrDefault("sandstone", 0);
                } else if ("moss".equals(type)) {
                    count = blockCounts.getOrDefault("moss", 0);
                } else if ("podzol".equals(type)) {
                    count = blockCounts.getOrDefault("podzol", 0);
                } else if ("stone".equals(type)) {
                    count = blockCounts.getOrDefault("stone", 0);
                } else if ("mushrooms".equals(type)) {
                    count = blockCounts.getOrDefault("mushrooms", 0);
                } else if ("coral".equals(type)) {
                    count = blockCounts.getOrDefault("coral", 0);
                } else if ("dead_bush".equals(type)) {
                    count = blockCounts.getOrDefault("dead_bush", 0);
                } else if ("bone".equals(type)) {
                    count = blockCounts.getOrDefault("bone", 0);
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

        // 8. Check Predator Danger (Co-habitation conflict: radius 20, unblocked line-of-sight)
        String predatorThreat = findPredatorThreat(level, animal);
        boolean predatorSafe = (predatorThreat == null);
        if (!predatorSafe) {
            unmet.add("Bahaya: " + predatorThreat + " Sekandang");
        }

        if (!biomeOk) {
            unmet.add("Bioma: " + profile.displayName());
        }
        if (waterRequired && !waterOk && !unmet.contains("Butuh Air")) {
            unmet.add("Butuh Air");
        }
        if (!groundOk && !hasGroundRequirementInUnmet(unmet)) {
            String specificGround = getSpecificGroundLabel(profile);
            if (!unmet.contains("Butuh " + specificGround)) {
                unmet.add("Butuh " + specificGround);
            }
        }

        if (tooCold) {
            unmet.add("Butuh Heater");
        } else if (tooHot) {
            unmet.add("Butuh Cooler Box");
        }

        boolean isSuitable = biomeOk && tempOk && groundOk && waterOk && extraBlocksOk && predatorSafe;
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
                    if (state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS) || state.is(Blocks.PACKED_MUD)) {
                        counts.merge("mud", 1, Integer::sum);
                    }

                    // Lily Pad
                    if (state.is(Blocks.LILY_PAD)) {
                        counts.merge("lily_pad", 1, Integer::sum);
                    }

                    // Ice
                    if (state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.ICE)) {
                        counts.merge("ice", 1, Integer::sum);
                    }

                    // Acacia Wood / Leaves
                    if (state.is(Blocks.ACACIA_LOG) || state.is(Blocks.ACACIA_WOOD) || state.is(Blocks.ACACIA_LEAVES)
                            || state.is(Blocks.STRIPPED_ACACIA_LOG) || state.is(Blocks.STRIPPED_ACACIA_WOOD)) {
                        counts.merge("acacia", 1, Integer::sum);
                    }

                    // Spruce Wood / Leaves
                    if (state.is(Blocks.SPRUCE_LOG) || state.is(Blocks.SPRUCE_WOOD) || state.is(Blocks.SPRUCE_LEAVES)
                            || state.is(Blocks.STRIPPED_SPRUCE_LOG) || state.is(Blocks.STRIPPED_SPRUCE_WOOD)) {
                        counts.merge("spruce", 1, Integer::sum);
                    }

                    // Jungle Wood / Leaves
                    if (state.is(Blocks.JUNGLE_LOG) || state.is(Blocks.JUNGLE_WOOD) || state.is(Blocks.JUNGLE_LEAVES)
                            || state.is(Blocks.STRIPPED_JUNGLE_LOG) || state.is(Blocks.STRIPPED_JUNGLE_WOOD)) {
                        counts.merge("jungle_wood", 1, Integer::sum);
                    }

                    // Vines
                    if (state.is(Blocks.VINE) || state.is(Blocks.WEEPING_VINES) || state.is(Blocks.WEEPING_VINES_PLANT)
                            || state.is(Blocks.TWISTING_VINES) || state.is(Blocks.TWISTING_VINES_PLANT)
                            || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)) {
                        counts.merge("vines", 1, Integer::sum);
                    }

                    // Flowers
                    if (state.is(BlockTags.FLOWERS)) {
                        counts.merge("flowers", 1, Integer::sum);
                    }

                    // Hay Block
                    if (state.is(Blocks.HAY_BLOCK)) {
                        counts.merge("hay", 1, Integer::sum);
                    }

                    // Sweet Berries
                    if (state.is(Blocks.SWEET_BERRY_BUSH)) {
                        counts.merge("sweet_berries", 1, Integer::sum);
                    }

                    // Terracotta
                    if (state.is(BlockTags.TERRACOTTA)) {
                        counts.merge("terracotta", 1, Integer::sum);
                    }

                    // Red Sand
                    if (state.is(Blocks.RED_SAND) || state.is(Blocks.RED_SANDSTONE)
                            || state.is(Blocks.SMOOTH_RED_SANDSTONE) || state.is(Blocks.CUT_RED_SANDSTONE)) {
                        counts.merge("red_sand", 1, Integer::sum);
                    }

                    // Sandstone
                    if (state.is(Blocks.SANDSTONE) || state.is(Blocks.SMOOTH_SANDSTONE)
                            || state.is(Blocks.CHISELED_SANDSTONE) || state.is(Blocks.CUT_SANDSTONE)) {
                        counts.merge("sandstone", 1, Integer::sum);
                    }

                    // Moss
                    if (state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MOSS_CARPET)) {
                        counts.merge("moss", 1, Integer::sum);
                    }

                    // Podzol
                    if (state.is(Blocks.PODZOL)) {
                        counts.merge("podzol", 1, Integer::sum);
                    }

                    // Stone
                    if (state.is(Blocks.SMOOTH_STONE) || state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
                            || state.is(Blocks.MOSSY_COBBLESTONE) || state.is(Blocks.DRIPSTONE_BLOCK)) {
                        counts.merge("stone", 1, Integer::sum);
                    }

                    // Mushrooms & Fungi
                    if (state.is(Blocks.MYCELIUM) || state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                            || state.is(Blocks.MUSHROOM_STEM) || state.is(Blocks.SHROOMLIGHT)
                            || state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM)
                            || state.is(Blocks.SPORE_BLOSSOM)) {
                        counts.merge("mushrooms", 1, Integer::sum);
                    }

                    // Coral & Ocean Rocks
                    if (state.is(BlockTags.CORALS) || state.is(BlockTags.WALL_CORALS) || state.is(Blocks.SEA_PICKLE)
                            || state.is(Blocks.SEAGRASS) || state.is(Blocks.PRISMARINE) || state.is(Blocks.DARK_PRISMARINE)
                            || state.is(Blocks.PRISMARINE_BRICKS)) {
                        counts.merge("coral", 1, Integer::sum);
                    }

                    // Dead Bush
                    if (state.is(Blocks.DEAD_BUSH)) {
                        counts.merge("dead_bush", 1, Integer::sum);
                    }

                    // Bone Block
                    if (state.is(Blocks.BONE_BLOCK)) {
                        counts.merge("bone", 1, Integer::sum);
                    }
                }
            }
        }
        return counts;
    }

    /**
     * Scans for dangerous predators in radius 20 blocks with direct line of sight (no solid walls/glass partitions).
     * Returns localized predator name if a threat is present, or null if safe.
     */
    public static String findPredatorThreat(ServerLevel level, LivingEntity animal) {
        if (level == null || animal == null || !animal.isAlive()) return null;

        boolean isSelfPredator = isApexPredator(animal);
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                animal.getBoundingBox().inflate(20.0),
                e -> e != animal && e.isAlive() && isApexPredator(e)
        );

        for (LivingEntity predator : nearby) {
            // Predator territory conflict: two predators of different species cannot share an open space
            if (isSelfPredator && predator.getType().equals(animal.getType())) {
                continue; // Same predator species can live together
            }

            // Check if there is an unblocked line of sight (no glass/solid walls/fences between them)
            if (hasUnblockedLineOfSight(animal, predator)) {
                return getPredatorDisplayName(predator);
            }
        }
        return null;
    }

    public static boolean isApexPredator(LivingEntity entity) {
        if (entity == null || !entity.isAlive()) return false;
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().toLowerCase(Locale.ROOT);
        return path.contains("lion") || path.contains("tiger") || path.contains("snow_leopard")
                || path.contains("leopard") || path.contains("grizzly") || path.contains("polar_bear")
                || (path.contains("bear") && !path.contains("anteater")) || path.contains("alligator")
                || path.contains("crocodile") || path.contains("anaconda") || path.contains("komodo")
                || path.contains("froststalker") || path.contains("hyena") || path.contains("wolf");
    }

    public static String getPredatorDisplayName(LivingEntity predator) {
        if (predator == null) return "Predator";
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(predator.getType()).getPath().toLowerCase(Locale.ROOT);
        if (path.contains("lion")) return "Singa";
        if (path.contains("tiger")) return "Harimau";
        if (path.contains("snow_leopard")) return "Macan Salju";
        if (path.contains("leopard")) return "Macan Tutul";
        if (path.contains("polar_bear")) return "Beruang Kutub";
        if (path.contains("grizzly")) return "Beruang Grizzly";
        if (path.contains("bear") && !path.contains("anteater")) return "Beruang";
        if (path.contains("alligator")) return "Aligator";
        if (path.contains("crocodile")) return "Buaya";
        if (path.contains("anaconda")) return "Anakonda";
        if (path.contains("komodo")) return "Komodo";
        if (path.contains("froststalker")) return "Froststalker";
        if (path.contains("hyena")) return "Hiena";
        if (path.contains("wolf")) return "Serigala";
        return predator.getType().getDescription().getString();
    }

    public static boolean hasUnblockedLineOfSight(LivingEntity from, LivingEntity to) {
        if (from.level() != to.level()) return false;
        Vec3 eye1 = new Vec3(from.getX(), from.getEyeY(), from.getZ());
        Vec3 eye2 = new Vec3(to.getX(), to.getEyeY(), to.getZ());
        if (eye1.distanceToSqr(eye2) > 400.0) return false; // > 20 blocks

        HitResult hit = from.level().clip(new ClipContext(eye1, eye2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, from));
        return hit.getType() == HitResult.Type.MISS;
    }

    public static String getSpecificGroundLabel(HabitatProfile profile) {
        if (profile == null) return "Blok Rumput";
        String name = profile.name().toLowerCase(Locale.ROOT);
        if (name.contains("desert")) return "Blok Pasir";
        if (name.contains("snow") || name.contains("ice")) return "Blok Salju / Es";
        if (name.contains("swamp") || name.contains("wetland")) return "Blok Lumpur";
        if (name.contains("taiga") || name.contains("boreal") || name.contains("forest")) return "Blok Podzol";
        if (name.contains("badlands") || name.contains("arid")) return "Pasir Merah / Terracotta";
        if (name.contains("ocean") || name.contains("freshwater")) return "Dasar Pasir / Kerikil";
        if (name.contains("fantasy")) return "Blok Miselium / Podzol";
        return "Blok Rumput";
    }

    private static boolean hasGroundRequirementInUnmet(List<String> unmet) {
        if (unmet == null || unmet.isEmpty()) return false;
        for (String item : unmet) {
            String lower = item.toLowerCase(Locale.ROOT);
            if (lower.contains("rumput") || lower.contains("pasir") || lower.contains("salju")
                    || lower.contains("lumpur") || lower.contains("podzol") || lower.contains("miselium")
                    || lower.contains("terracotta") || lower.contains("es")) {
                return true;
            }
        }
        return false;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
