package net.zootycoon.animal.habitat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side lookup of the biomes a species lives in. The client does not receive biome mob spawn data,
 * so results are computed here and sent over the network. All names are English, derived from biome ids.
 */
public final class SpeciesBiomeResolver {

    private static final Map<String, List<String>> SPAWN_CACHE = new ConcurrentHashMap<>();

    private SpeciesBiomeResolver() {
    }

    public static void reset() {
        SPAWN_CACHE.clear();
    }

    /** Every biome whose natural mob spawn table contains the entity type (modded biomes included). */
    public static List<String> resolveSpawnBiomes(ServerLevel level, EntityType<?> type) {
        if (level == null || type == null) return List.of();
        ResourceLocation typeId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type);
        String cacheKey = typeId.toString();
        List<String> cached = SPAWN_CACHE.get(cacheKey);
        if (cached != null) return cached;

        TreeSet<String> names = new TreeSet<>();
        var biomeRegistry = level.registryAccess().lookupOrThrow(Registries.BIOME);
        biomeRegistry.listElements().forEach(ref -> {
            Biome biome = ref.value();
            for (MobCategory category : MobCategory.values()) {
                boolean found = biome.getMobSettings().getMobs(category).unwrap().stream()
                        .anyMatch(data -> data.type == type);
                if (found) {
                    names.add(toEnglishBiomeName(ref.key().location()));
                    break;
                }
            }
        });
        List<String> result = List.copyOf(names);
        SPAWN_CACHE.put(cacheKey, result);
        return result;
    }

    /** Biomes accepted by a habitat profile, used when the species has no natural spawn or for inspection notes. */
    public static List<String> resolveProfileBiomes(ServerLevel level, HabitatProfile profile) {
        if (level == null || profile == null) return List.of();
        TreeSet<String> names = new TreeSet<>();
        var biomeRegistry = level.registryAccess().lookupOrThrow(Registries.BIOME);
        biomeRegistry.listElements().forEach((Holder.Reference<Biome> ref) -> {
            if (profile.matchesBiome(ref)) {
                names.add(toEnglishBiomeName(ref.key().location()));
            }
        });
        return new ArrayList<>(names);
    }

    /** minecraft:dark_forest becomes "Dark Forest". */
    public static String toEnglishBiomeName(ResourceLocation id) {
        if (id == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String part : id.getPath().split("[_/]")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return sb.toString();
    }
}
