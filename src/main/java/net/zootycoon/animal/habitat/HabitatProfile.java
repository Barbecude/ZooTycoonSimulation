package net.zootycoon.animal.habitat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.Collections;
import java.util.List;

/**
 * Represents an animal habitat requirement profile.
 * Can be derived from an ecosystem template or customized per species.
 */
public record HabitatProfile(
        ResourceLocation id,
        String name,
        String displayName,
        List<String> biomeTags,
        List<ResourceLocation> biomeIds,
        float minTemperature,
        float maxTemperature,
        List<BlockRequirement> requiredBlocks
) {
    public record BlockRequirement(
            String type,
            String tag,
            List<ResourceLocation> blocks,
            int minCount,
            String label
    ) {
        public BlockRequirement(String type, int minCount, String label) {
            this(type, null, Collections.emptyList(), minCount, label);
        }
    }

    /**
     * Checks if the given biome matches any of the profile's allowed biome tags or IDs.
     */
    public boolean matchesBiome(Holder<Biome> biomeHolder) {
        if (biomeHolder == null) return false;

        // If no biome restrictions specified, matches all
        boolean hasTags = biomeTags != null && !biomeTags.isEmpty();
        boolean hasIds = biomeIds != null && !biomeIds.isEmpty();
        if (!hasTags && !hasIds) {
            return true;
        }

        // Check biome tags
        if (hasTags) {
            for (String tagStr : biomeTags) {
                String cleanTag = tagStr.startsWith("#") ? tagStr.substring(1) : tagStr;
                ResourceLocation tagLoc = ResourceLocation.tryParse(cleanTag);
                if (tagLoc != null) {
                    TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, tagLoc);
                    if (biomeHolder.is(tagKey)) {
                        return true;
                    }
                }
            }
        }

        // Check specific biome IDs
        if (hasIds) {
            for (ResourceLocation biomeLoc : biomeIds) {
                ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, biomeLoc);
                if (biomeHolder.is(key)) {
                    return true;
                }
            }
        }

        return false;
    }
}

