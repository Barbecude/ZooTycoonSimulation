package net.zootycoon.license;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.zootycoon.util.ZooTranslations;

import java.util.*;

public enum LicenseTier {
    TAMAN_MINI(
            1,
            "taman_mini",
            "zootycoon.license.tier.1",
            "Lisensi Taman Mini",
            0.0D,
            0xFF4ADE80,
            List.of(
                    new SpeciesEntry("mini_sheep", "Mini Sheep", List.of("wildernature:minisheep", "wildernature:mini_sheep")),
                    new SpeciesEntry("deer", "Deer", List.of("naturalist:deer", "wildernature:deer")),
                    new SpeciesEntry("raccoon", "Raccoon", List.of("alexsmobs:raccoon")),
                    new SpeciesEntry("capybara", "Capybara", List.of("naturalist:capybara")),
                    new SpeciesEntry("tortoise", "Giant Tortoise", List.of("naturalist:tortoise", "minecraft:turtle")),
                    new SpeciesEntry("capuchin_monkey", "Capuchin Monkey", List.of("alexsmobs:capuchin_monkey")),
                    new SpeciesEntry("kangaroo", "Kangaroo", List.of("alexsmobs:kangaroo")),
                    new SpeciesEntry("ostrich", "Ostrich", List.of("naturalist:ostrich", "alexsmobs:emu"))
            )
    ),
    KEBUN_BINATANG(
            2,
            "kebun_binatang",
            "zootycoon.license.tier.2",
            "Lisensi Kebun Binatang",
            3.0D,
            0xFF38BDF8,
            List.of(
                    new SpeciesEntry("zebra", "Zebra", List.of("naturalist:zebra", "minecraft:horse")),
                    new SpeciesEntry("camel", "Camel", List.of("minecraft:camel")),
                    new SpeciesEntry("penguin", "Penguin", List.of("naturalist:penguin", "ecologics:penguin")),
                    new SpeciesEntry("panda", "Panda", List.of("minecraft:panda")),
                    new SpeciesEntry("giant_anteater", "Giant Anteater", List.of("alexsmobs:anteater", "alexsmobs:giant_anteater")),
                    new SpeciesEntry("cassowary", "Cassowary", List.of("wildernature:cassowary")),
                    new SpeciesEntry("bald_eagle", "Bald Eagle", List.of("alexsmobs:bald_eagle")),
                    new SpeciesEntry("vulture", "Vulture", List.of("naturalist:vulture"))
            )
    ),
    SUAKA_MARGASATWA(
            3,
            "suaka_margasatwa",
            "zootycoon.license.tier.3",
            "Lisensi Suaka Margasatwa",
            3.5D,
            0xFFFBBF24,
            List.of(
                    new SpeciesEntry("giraffe", "Giraffe", List.of("naturalist:giraffe")),
                    new SpeciesEntry("hippo", "Hippo", List.of("naturalist:hippo")),
                    new SpeciesEntry("gorilla", "Gorilla", List.of("alexsmobs:gorilla")),
                    new SpeciesEntry("elephant", "Elephant", List.of("naturalist:elephant", "alexsmobs:elephant")),
                    new SpeciesEntry("alligator", "Alligator", List.of("naturalist:alligator", "alexsmobs:crocodile", "naturalist:crocodile", "alexsmobs:caiman")),
                    new SpeciesEntry("komodo_dragon", "Komodo Dragon", List.of("naturalist:komodo_dragon", "alexsmobs:komodo_dragon")),
                    new SpeciesEntry("anaconda", "Anaconda", List.of("alexsmobs:anaconda")),
                    new SpeciesEntry("mantis_shrimp", "Mantis Shrimp", List.of("alexsmobs:mantis_shrimp"))
            )
    ),
    CAGAR_ALAM(
            4,
            "cagar_alam",
            "zootycoon.license.tier.4",
            "Lisensi Cagar Alam",
            4.0D,
            0xFFF97316,
            List.of(
                    new SpeciesEntry("lion", "Lion", List.of("naturalist:lion", "alexsmobs:lion")),
                    new SpeciesEntry("tiger", "Tiger", List.of("naturalist:tiger", "alexsmobs:tiger")),
                    new SpeciesEntry("grizzly_bear", "Grizzly Bear", List.of("alexsmobs:grizzly_bear", "naturalist:bear")),
                    new SpeciesEntry("snow_leopard", "Snow Leopard", List.of("alexsmobs:snow_leopard", "naturalist:leopard")),
                    new SpeciesEntry("polar_bear", "Polar Bear", List.of("minecraft:polar_bear")),
                    new SpeciesEntry("mammoth", "Mammoth", List.of("naturalist:mammoth", "alexsmobs:mammoth")),
                    new SpeciesEntry("froststalker", "Froststalker", List.of("alexsmobs:froststalker")),
                    new SpeciesEntry("bunfungus", "Bunfungus", List.of("alexsmobs:bunfungus"))
            )
    ),
    KONSERVASI_DUNIA(
            5,
            "konservasi_dunia",
            "zootycoon.license.tier.5",
            "Lisensi Konservasi Dunia",
            4.5D,
            0xFFA855F7,
            List.of(
                    new SpeciesEntry("orca", "Orca", List.of("alexsmobs:orca")),
                    new SpeciesEntry("cachalot_whale", "Cachalot Whale", List.of("alexsmobs:cachalot_whale")),
                    new SpeciesEntry("great_white_shark", "Great White Shark", List.of("naturalist:great_white_shark", "alexsmobs:great_white_shark", "naturalist:shark")),
                    new SpeciesEntry("giant_squid", "Giant Squid", List.of("alexsmobs:giant_squid")),
                    new SpeciesEntry("mungus", "Mungus", List.of("alexsmobs:mungus")),
                    new SpeciesEntry("guster", "Guster", List.of("alexsmobs:guster")),
                    new SpeciesEntry("rocky_roller", "Rocky Roller", List.of("alexsmobs:rocky_roller")),
                    new SpeciesEntry("flutter", "Flutter", List.of("alexsmobs:flutter"))
            )
    );

    public record SpeciesEntry(String speciesKey, String defaultName, List<String> entityTypeIds) {
        public String getDisplayName() {
            if (defaultName != null && !defaultName.isEmpty()) {
                return defaultName;
            }
            EntityType<?> resolved = resolveEntityType();
            if (resolved != null) {
                return resolved.getDescription().getString();
            }
            return speciesKey;
        }

        public String resolvePrimaryEntityId() {
            for (String id : entityTypeIds) {
                ResourceLocation loc = ResourceLocation.tryParse(id);
                if (loc != null && BuiltInRegistries.ENTITY_TYPE.containsKey(loc)) {
                    return id;
                }
            }
            return entityTypeIds.isEmpty() ? "minecraft:pig" : entityTypeIds.get(0);
        }

        public EntityType<?> resolveEntityType() {
            for (String id : entityTypeIds) {
                ResourceLocation loc = ResourceLocation.tryParse(id);
                if (loc != null && BuiltInRegistries.ENTITY_TYPE.containsKey(loc)) {
                    return BuiltInRegistries.ENTITY_TYPE.get(loc);
                }
            }
            return null;
        }

        public boolean matches(String entityTypeId) {
            if (entityTypeId == null || entityTypeId.isEmpty()) return false;
            String normalized = entityTypeId.toLowerCase(Locale.ROOT);
            for (String candidate : entityTypeIds) {
                if (candidate.equalsIgnoreCase(normalized)) {
                    return true;
                }
            }
            return false;
        }

        public boolean matches(EntityType<?> type) {
            if (type == null) return false;
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            return key != null && matches(key.toString());
        }
    }

    private final int tierLevel;
    private final String id;
    private final String translationKey;
    private final String fallbackTitle;
    private final double requiredStars;
    private final int accentColor;
    private final List<SpeciesEntry> requiredSpecies;

    LicenseTier(int tierLevel, String id, String translationKey, String fallbackTitle, double requiredStars, int accentColor, List<SpeciesEntry> requiredSpecies) {
        this.tierLevel = tierLevel;
        this.id = id;
        this.translationKey = translationKey;
        this.fallbackTitle = fallbackTitle;
        this.requiredStars = requiredStars;
        this.accentColor = accentColor;
        this.requiredSpecies = requiredSpecies;
    }

    public int getTierLevel() {
        return tierLevel;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getDisplayName() {
        String t = ZooTranslations.get(translationKey);
        return (t == null || t.isEmpty() || t.equals(translationKey)) ? fallbackTitle : t;
    }

    public double getRequiredStars() {
        return requiredStars;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public List<SpeciesEntry> getRequiredSpecies() {
        return requiredSpecies;
    }

    public LicenseTier nextTier() {
        int nextOrd = this.ordinal() + 1;
        LicenseTier[] vals = values();
        return nextOrd < vals.length ? vals[nextOrd] : null;
    }

    public static LicenseTier byLevel(int level) {
        for (LicenseTier t : values()) {
            if (t.tierLevel == level) return t;
        }
        return TAMAN_MINI;
    }

    public static LicenseTier byOrdinal(int ord) {
        LicenseTier[] vals = values();
        if (ord < 0 || ord >= vals.length) return TAMAN_MINI;
        return vals[ord];
    }

    /**
     * Finds which LicenseTier requires the given entityTypeId, or null if it is not restricted by any tier.
     */
    public static LicenseTier findTierForEntity(String entityTypeId) {
        if (entityTypeId == null || entityTypeId.isEmpty()) return null;
        // Prefer exact primary match first
        for (LicenseTier tier : values()) {
            for (SpeciesEntry entry : tier.requiredSpecies) {
                if (!entry.entityTypeIds().isEmpty() && entry.entityTypeIds().get(0).equalsIgnoreCase(entityTypeId)) {
                    return tier;
                }
            }
        }
        for (LicenseTier tier : values()) {
            for (SpeciesEntry entry : tier.requiredSpecies) {
                if (entry.matches(entityTypeId)) {
                    return tier;
                }
            }
        }
        return null;
    }

    public static LicenseTier findTierForEntity(EntityType<?> type) {
        if (type == null) return null;
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key != null ? findTierForEntity(key.toString()) : null;
    }

    public static SpeciesEntry findSpeciesEntry(String speciesKeyOrEntityId) {
        if (speciesKeyOrEntityId == null) return null;
        for (LicenseTier tier : values()) {
            for (SpeciesEntry entry : tier.requiredSpecies) {
                if (entry.speciesKey().equalsIgnoreCase(speciesKeyOrEntityId) || entry.matches(speciesKeyOrEntityId)) {
                    return entry;
                }
            }
        }
        return null;
    }
}
