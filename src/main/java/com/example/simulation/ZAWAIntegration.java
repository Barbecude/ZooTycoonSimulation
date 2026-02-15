package com.example.simulation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * ZAWA Integration Manager
 * Detects and manages ZAWA animals for the Zoo Tycoon simulation.
 * 
 * Features:
 * - Automatic detection of ZAWA animals
 * - Premium pricing for ZAWA animals
 * - Habitat requirements tracking
 * - Special handling for ZAWA-specific features
 */
public class ZAWAIntegration {
    private static final String ZAWA_MODID = "zawa";
    private static boolean zawaLoaded = false;
    private static final Map<ResourceLocation, ZAWAAnimalData> ZAWA_ANIMALS = new LinkedHashMap<>();

    public static class ZAWAAnimalData extends AnimalRegistry.AnimalData {
        public final String category; // e.g., "african", "asian", "aquatic"
        public final int maintenanceCost; // Daily upkeep cost
        public final int happinessRequirement; // Minimum habitat quality needed

        public ZAWAAnimalData(EntityType<? extends Animal> type, String name, int price,
                String category, int maintenanceCost, int happinessRequirement) {
            super(type, name, price);
            this.category = category;
            this.maintenanceCost = maintenanceCost;
            this.happinessRequirement = happinessRequirement;
        }
    }

    /**
     * Initialize ZAWA integration if the mod is loaded
     */
    public static void initialize() {
        zawaLoaded = ModList.get().isLoaded(ZAWA_MODID);

        if (!zawaLoaded) {
            System.out.println("[IndoZoo] ZAWA mod not detected. Running in standalone mode.");
            return;
        }

        System.out.println("[IndoZoo] ZAWA mod detected! Initializing integration...");
        discoverZAWAAnimals();
        System.out.println("[IndoZoo] Found " + ZAWA_ANIMALS.size() + " ZAWA animals!");
    }

    /**
     * Discover all ZAWA animals from the entity registry
     */
    private static void discoverZAWAAnimals() {
        ForgeRegistries.ENTITY_TYPES.forEach((entityType) -> {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
            if (id == null)
                return;

            // Check if this is a ZAWA animal
            if (ZAWA_MODID.equals(id.getNamespace()) && isAnimalType(entityType)) {
                @SuppressWarnings("unchecked")
                EntityType<? extends Animal> animalType = (EntityType<? extends Animal>) entityType;

                String displayName = formatZAWAName(id);
                String category = detectCategory(id);
                int price = calculateZAWAPrice(id, category);
                int maintenanceCost = calculateMaintenanceCost(price);
                int happinessReq = calculateHappinessRequirement(category);

                ZAWAAnimalData data = new ZAWAAnimalData(
                        animalType, displayName, price, category,
                        maintenanceCost, happinessReq);

                ZAWA_ANIMALS.put(id, data);

                // Also update the main registry with our enhanced data
                AnimalRegistry.registerAnimal(id, data);
            }
        });
    }

    private static boolean isAnimalType(EntityType<?> type) {
        try {
            Class<?> clazz = type.getBaseClass();
            return Animal.class.isAssignableFrom(clazz);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Format ZAWA animal names nicely
     * Example: zawa:african_elephant -> African Elephant
     */
    private static String formatZAWAName(ResourceLocation id) {
        String name = id.getPath();
        name = name.replace("_", " ");
        return capitalize(name);
    }

    /**
     * Detect animal category based on name patterns
     */
    private static String detectCategory(ResourceLocation id) {
        String path = id.getPath().toLowerCase();

        // African animals
        if (path.contains("african") || path.contains("lion") || path.contains("giraffe")
                || path.contains("zebra") || path.contains("rhino")) {
            return "african";
        }

        // Asian animals
        if (path.contains("asian") || path.contains("tiger") || path.contains("panda")
                || path.contains("orangutan")) {
            return "asian";
        }

        // Aquatic animals
        if (path.contains("penguin") || path.contains("seal") || path.contains("otter")
                || path.contains("dolphin") || path.contains("fish")) {
            return "aquatic";
        }

        // Arctic animals
        if (path.contains("polar") || path.contains("arctic") || path.contains("snow")) {
            return "arctic";
        }

        // Reptiles
        if (path.contains("snake") || path.contains("lizard") || path.contains("crocodile")
                || path.contains("alligator") || path.contains("tortoise")) {
            return "reptile";
        }

        // Birds
        if (path.contains("bird") || path.contains("parrot") || path.contains("eagle")
                || path.contains("flamingo") || path.contains("owl")) {
            return "avian";
        }

        return "general";
    }

    /**
     * Calculate premium pricing for ZAWA animals
     */
    private static int calculateZAWAPrice(ResourceLocation id, String category) {
        int basePrice = ZooConfig.ZAWA_ANIMAL_PRICE.get();

        // Premium categories get higher prices
        switch (category) {
            case "african":
                return (int) (basePrice * 1.5); // African animals are iconic
            case "asian":
                return (int) (basePrice * 1.4);
            case "aquatic":
                return (int) (basePrice * 1.3); // Need special habitats
            case "arctic":
                return (int) (basePrice * 1.6); // Expensive to maintain
            case "reptile":
                return (int) (basePrice * 1.2);
            case "avian":
                return (int) (basePrice * 1.1);
            default:
                return basePrice;
        }
    }

    /**
     * Calculate daily maintenance cost
     */
    private static int calculateMaintenanceCost(int price) {
        // Maintenance is roughly 5% of purchase price
        return Math.max(10, price / 20);
    }

    /**
     * Calculate minimum happiness requirement
     */
    private static int calculateHappinessRequirement(String category) {
        switch (category) {
            case "arctic":
            case "aquatic":
                return 80; // Need very specific conditions
            case "african":
            case "asian":
                return 70; // Need good space and enrichment
            case "reptile":
                return 60; // More tolerant
            default:
                return 50; // Standard requirement
        }
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

    // ========== Public API ==========

    public static boolean isZAWALoaded() {
        return zawaLoaded;
    }

    public static Map<ResourceLocation, ZAWAAnimalData> getZAWAAnimals() {
        return Collections.unmodifiableMap(ZAWA_ANIMALS);
    }

    public static ZAWAAnimalData getZAWAAnimal(ResourceLocation id) {
        return ZAWA_ANIMALS.get(id);
    }

    public static boolean isZAWAAnimal(ResourceLocation id) {
        return ZAWA_MODID.equals(id.getNamespace());
    }

    /**
     * Get all ZAWA animals by category
     */
    public static Map<String, List<ZAWAAnimalData>> getAnimalsByCategory() {
        Map<String, List<ZAWAAnimalData>> result = new HashMap<>();

        for (ZAWAAnimalData data : ZAWA_ANIMALS.values()) {
            result.computeIfAbsent(data.category, k -> new ArrayList<>()).add(data);
        }

        return result;
    }

    /**
     * Get total number of ZAWA animals discovered
     */
    public static int getTotalZAWAAnimals() {
        return ZAWA_ANIMALS.size();
    }
}
