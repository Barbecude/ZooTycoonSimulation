package com.example.simulation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class AnimalRegistry {
    public enum Category {
        LAND, AQUATIC, MYTHICAL, BUG
    }

    public static class AnimalData {
        public final EntityType<?> entityType;
        public final String displayName;
        public final int price;
        public final Category category;

        public AnimalData(EntityType<?> type, String name, int price, Category category) {
            this.entityType = type;
            this.displayName = name;
            this.price = price;
            this.category = category;
        }
    }

    private static final Map<ResourceLocation, AnimalData> ANIMAL_CATALOG = new LinkedHashMap<>();

    public static void initialize() {
        ANIMAL_CATALOG.clear();

        // 1. LAND ANIMALS (Merged Core, Reptile, Real, Vanilla)
        // Prices in Millions (Jutaan)
        register("alexsmobs:elephant", "Elephant", 15_000_000, Category.LAND);
        register("alexsmobs:tiger", "Tiger", 25_000_000, Category.LAND);
        register("alexsmobs:grizzly_bear", "Grizzly Bear", 20_000_000, Category.LAND);
        register("alexsmobs:gorilla", "Gorilla", 22_000_000, Category.LAND);
        register("alexsmobs:kangaroo", "Kangaroo", 12_000_000, Category.LAND);
        register("alexsmobs:rocky_roller", "Rocky Roller", 18_000_000, Category.LAND);
        register("alexsmobs:komodo_dragon", "Komodo Dragon", 30_000_000, Category.LAND);
        register("alexsmobs:anaconda", "Anaconda", 28_000_000, Category.LAND);
        register("alexsmobs:crocodile", "Alligator", 25_000_000, Category.LAND);
        register("naturalist:giraffe", "Giraffe", 18_000_000, Category.LAND);
        register("naturalist:zebra", "Zebra", 15_000_000, Category.LAND);
        register("naturalist:hippo", "Hippo", 22_000_000, Category.LAND);
        register("naturalist:lion", "Lion", 26_000_000, Category.LAND);
        register("naturalist:rhino", "Rhino", 24_000_000, Category.LAND);
        register("minecraft:panda", "Panda", 35_000_000, Category.LAND);

        // 2. AQUATIC
        register("alexsmobs:mimic_octopus", "Mimic Octopus", 15_000_000, Category.AQUATIC);
        register("alexsmobs:hammerhead_shark", "Hammerhead Shark", 20_000_000, Category.AQUATIC);
        register("alexsmobs:giant_squid", "Giant Squid", 40_000_000, Category.AQUATIC);
        register("alexsmobs:cachalot_whale", "Cachalot Whale", 80_000_000, Category.AQUATIC); // Moved from Mythical if
                                                                                              // debatable, but user
                                                                                              // said keep Aquatic
                                                                                              // distinct

        // 3. MYTHICAL (Higher prices)
        register("alexsmobs:centipede_head", "Cave Centipede", 50_000_000, Category.MYTHICAL);
        register("alexsmobs:sea_bear", "Sea Bear", 55_000_000, Category.MYTHICAL);
        register("alexsmobs:comb_jelly", "Comb Jelly", 45_000_000, Category.MYTHICAL);
        register("alexsmobs:guster", "Guster", 60_000_000, Category.MYTHICAL);
        register("alexsmobs:tarantula_hawk", "Tarantula Hawk", 50_000_000, Category.MYTHICAL);
        register("alexsmobs:bone_serpent", "Bone Serpent", 75_000_000, Category.MYTHICAL);
        register("alexsmobs:void_worm", "Leviathan", 100_000_000, Category.MYTHICAL);
        register("alexsmobs:endergrade", "Endergrade", 90_000_000, Category.MYTHICAL);
        register("alexsmobs:enderiophage", "Enderiophage", 85_000_000, Category.MYTHICAL);

        // 4. BUGS (Cheaper but still significant)
        register("alexsmobs:cockroach", "Cockroach", 500_000, Category.BUG);
        register("naturalist:butterfly", "Butterfly", 750_000, Category.BUG);
        register("naturalist:caterpillar", "Caterpillar", 300_000, Category.BUG);
        register("minecraft:bee", "Bee", 1_000_000, Category.BUG);

        System.out.println("[IndoZoo] Registered " + ANIMAL_CATALOG.size() + " animals.");
    }

    private static void register(String idStr, String name, int price, Category cat) {
        ResourceLocation id = new ResourceLocation(idStr);
        if (ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
            ANIMAL_CATALOG.put(id, new AnimalData(type, name, price, cat));
        }
    }

    public static Map<ResourceLocation, AnimalData> getAllAnimals() {
        return Collections.unmodifiableMap(ANIMAL_CATALOG);
    }

    public static AnimalData getAnimal(ResourceLocation id) {
        return ANIMAL_CATALOG.get(id);
    }

    public static AnimalData getAnimal(String idStr) {
        ResourceLocation id = idStr.contains(":")
                ? ResourceLocation.tryParse(idStr)
                : ResourceLocation.fromNamespaceAndPath("minecraft", idStr);
        return ANIMAL_CATALOG.get(id);
    }

    public static void registerAnimal(ResourceLocation id, AnimalData data) {
        ANIMAL_CATALOG.put(id, data);
    }
}
