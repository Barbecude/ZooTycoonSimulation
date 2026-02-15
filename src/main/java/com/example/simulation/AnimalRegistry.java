package com.example.simulation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class AnimalRegistry {
    private static final Map<ResourceLocation, AnimalData> ANIMAL_CATALOG = new LinkedHashMap<>();
    private static boolean initialized = false;

    public static class AnimalData {
        public final EntityType<? extends Animal> entityType;
        public final String displayName;
        public final int price;

        public AnimalData(EntityType<? extends Animal> type, String name, int price) {
            this.entityType = type;
            this.displayName = name;
            this.price = price;
        }
    }

    /**
     * Scan semua EntityType yang ada di registry dan filter yang Animal.
     * Pricing otomatis berdasarkan MAX_HEALTH.
     */
    public static void initialize() {
        if (initialized)
            return;
        initialized = true;

        // Iterasi menggunakan iterator yang kompatibel
        ForgeRegistries.ENTITY_TYPES.forEach((entityType) -> {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
            if (id == null)
                return;

            // Cek apakah entity ini adalah Animal
            if (isAnimalType(entityType)) {
                @SuppressWarnings("unchecked")
                EntityType<? extends Animal> animalType = (EntityType<? extends Animal>) entityType;

                String displayName = formatName(id);
                int price = calculatePrice(animalType);

                ANIMAL_CATALOG.put(id, new AnimalData(animalType, displayName, price));
            }
        });

        System.out.println("[IndoZoo] Found " + ANIMAL_CATALOG.size() + " animals for sale!");
    }

    private static boolean isAnimalType(EntityType<?> type) {
        try {
            // Coba spawn dummy entity untuk cek class-nya
            Class<?> clazz = type.getBaseClass();
            return Animal.class.isAssignableFrom(clazz);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hitung harga berdasarkan kategori.
     * Vanilla animals lebih murah, mod animals lebih mahal.
     */
    private static int calculatePrice(EntityType<? extends Animal> type) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id == null)
            return ZooConfig.MOD_ANIMAL_PRICE.get();

        // Vanilla minecraft animals = cheaper
        if ("minecraft".equals(id.getNamespace())) {
            return ZooConfig.VANILLA_ANIMAL_PRICE.get();
        }
        // Mod animals = more expensive
        return ZooConfig.MOD_ANIMAL_PRICE.get();
    }

    /**
     * Convert ResourceLocation ke nama yang bagus.
     * Contoh: minecraft:polar_bear -> Polar Bear
     */
    private static String formatName(ResourceLocation id) {
        String name = id.getPath();
        name = name.replace("_", " ");
        return capitalize(name);
    }

    private static String capitalize(String str) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    public static void registerAnimal(ResourceLocation id, AnimalData data) {
        ANIMAL_CATALOG.put(id, data);
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
}
