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
            return "status=" + status.name()
                    + ";eco=" + ecosystemName
                    + ";biome=" + (biomeOk ? "OK" : "NO")
                    + ";water=" + (waterRequired ? (waterOk ? "OK" : "NO") : "NA")
                    + ";temp=" + (tempOk ? "OK" : (tooCold ? "HEATER" : "COOLER"))
                    + ";ground=" + (groundOk ? "OK" : "NO")
                    + ";mate=" + (companionRequired ? (companionOk ? "OK" : "NO") : "NA");
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

        // 1. Check surrounding blocks with extended range (radius 24, dy -6..6)
        Map<String, Integer> blockCounts = scanSurroundingBlocks(level, animal.blockPosition());

        // 2. Check Natural Ground (coarse dirt, podzol, sand, gravel, etc.)
        int groundCount = blockCounts.getOrDefault("natural_ground", 0);
        boolean groundOk = groundCount >= 8;

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
        if (companionRequired && !hasCompanion) {
            unmet.add("Pasangan: Butuh Teman");
        }

        boolean isSuitable = biomeOk && tempOk && groundOk && waterOk && (!companionRequired || hasCompanion);
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

                    // Natural Ground block tag (coarse dirt, podzol, sand, gravel, modded soil)
                    if (state.is(NATURAL_GROUND)) {
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
}
