package com.example.simulation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Comprehensive mapping of foods to animals across all mods.
 */
public class FoodAnimalRegistry {

    public static class FoodProfile {
        public final Item food;
        public final String displayName;
        private final List<String> animalConsumers; // "modid:entity_name"

        public FoodProfile(Item food, String displayName, String... animals) {
            this.food = food;
            this.displayName = displayName;
            this.animalConsumers = Arrays.asList(animals);
        }

        public List<String> getConsumerAnimals() {
            return new ArrayList<>(animalConsumers);
        }

        public String getTooltip() {
            if (animalConsumers.isEmpty()) return "No animals eat this";

            StringBuilder sb = new StringBuilder();
            sb.append("Eaten by:");
            Set<String> seen = new LinkedHashSet<>();
            for (String animalId : animalConsumers) {
                String line = "- " + getAnimalDisplayName(animalId);
                if (seen.add(line)) {
                    sb.append("\n").append(line);
                }
            }
            return sb.toString();
        }

        private String getAnimalDisplayName(String animalId) {
            if (animalId == null || animalId.isEmpty()) return "Unknown";

            String custom = ANIMAL_DISPLAY_OVERRIDES.get(animalId);
            if (custom != null) return custom;

            if (!animalId.contains(":")) return animalId;

            String entityName = animalId.substring(animalId.indexOf(':') + 1);
            String[] words = entityName.split("_");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (word.isEmpty()) continue;
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }

            return sb.toString();
        }
    }

    private static final Map<Item, FoodProfile> FOOD_PROFILES = new LinkedHashMap<>();

    private static final Map<String, String> MOD_DISPLAY_NAMES = new LinkedHashMap<>();
    private static final Map<String, String> ANIMAL_DISPLAY_OVERRIDES = new LinkedHashMap<>();

    static {
        MOD_DISPLAY_NAMES.put("alexsmobs", "Alex's Mobs");
        MOD_DISPLAY_NAMES.put("naturalist", "Naturalist");
        MOD_DISPLAY_NAMES.put("minecraft", "Vanilla");

        // REMOVED - Display only animal name without mod prefix
        // Previously had entries like "Gorilla (Alex's Mobs)" - now just "Gorilla"
    }

    public static void initialize() {
        FOOD_PROFILES.clear();

        // Alex's Mobs + Naturalist mapping based on requested zoo shop list.
        registerById("alexsmobs:banana", "Banana",
                "alexsmobs:gorilla", "alexsmobs:capuchin_monkey");

        register(Items.MELON_SLICE, "Melon Slice",
                "alexsmobs:elephant", "naturalist:elephant");

        register(Items.SALMON, "Raw Salmon",
                "alexsmobs:grizzly_bear", "alexsmobs:crocodile",
                "alexsmobs:alligator_snapping_turtle", "alexsmobs:seagull", "alexsmobs:bald_eagle",
                "naturalist:bear");

        register(Items.COD, "Raw Cod",
                "alexsmobs:grizzly_bear", "alexsmobs:crocodile",
                "alexsmobs:alligator_snapping_turtle", "alexsmobs:seagull", "alexsmobs:bald_eagle",
                "naturalist:bear");

        register(Items.HONEY_BOTTLE, "Honey Bottle",
                "alexsmobs:grizzly_bear");

        register(Items.BEEF, "Raw Beef",
                "alexsmobs:tiger", "alexsmobs:lion", "alexsmobs:komodo_dragon",
                "naturalist:lion", "naturalist:tiger", "naturalist:wolf");

        register(Items.PORKCHOP, "Raw Porkchop",
                "alexsmobs:tiger", "alexsmobs:komodo_dragon", "naturalist:wolf");

        register(Items.CHICKEN, "Raw Chicken",
                "alexsmobs:snake", "alexsmobs:rattlesnake", "alexsmobs:komodo_dragon", "naturalist:wolf");

        register(Items.WHEAT, "Wheat",
                "alexsmobs:kangaroo", "alexsmobs:moose",
                "naturalist:deer", "naturalist:zebra", "naturalist:rhino");

        register(Items.SWEET_BERRIES, "Sweet Berries",
                "alexsmobs:raccoon", "naturalist:fox");

        register(Items.ROTTEN_FLESH, "Rotten Flesh",
                "alexsmobs:cockroach", "alexsmobs:fly");

        register(Items.BONE_MEAL, "Bone Meal",
                "alexsmobs:hummingbird");

        register(Items.ACACIA_LEAVES, "Acacia Leaves",
                "naturalist:giraffe");

        register(Items.WHEAT_SEEDS, "Seeds",
                "naturalist:duck", "naturalist:canary", "naturalist:sparrow");

        // Naturalist interaction cases.
        register(Items.DANDELION, "Flower",
                "naturalist:butterfly");

        register(Items.GLOW_BERRIES, "Glow Berries",
                "naturalist:firefly");
    }

    private static void register(Item food, String displayName, String... animals) {
        FoodProfile profile = new FoodProfile(food, displayName, animals);
        FOOD_PROFILES.put(food, profile);
    }

    private static void registerById(String itemId, String displayName, String... animals) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) return;
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) return;
        register(item, displayName, animals);
    }

    public static FoodProfile getProfile(Item food) {
        return FOOD_PROFILES.get(food);
    }

    public static boolean isFoodRegistered(Item food) {
        return FOOD_PROFILES.containsKey(food);
    }

    public static Collection<FoodProfile> getAllFoods() {
        return FOOD_PROFILES.values();
    }

    public static List<String> getAnimalsEatingFood(Item food) {
        FoodProfile profile = getProfile(food);
        return profile != null ? profile.getConsumerAnimals() : Collections.emptyList();
    }

    public static String getFoodTooltip(Item food) {
        FoodProfile profile = getProfile(food);
        return profile != null ? profile.getTooltip() : "No information available";
    }

    public static List<Item> getFoodsForAnimal(String animalId) {
        List<Item> foods = new ArrayList<>();
        for (FoodProfile profile : FOOD_PROFILES.values()) {
            if (profile.getConsumerAnimals().contains(animalId)) {
                foods.add(profile.food);
            }
        }
        return foods;
    }

    public static boolean isValidFoodForAnimal(String animalId, Item item) {
        if (animalId == null || animalId.isEmpty() || item == null) return false;
        return getFoodsForAnimal(animalId).contains(item);
    }

    public static Map<Item, FoodProfile> getProfiles() {
        return Collections.unmodifiableMap(FOOD_PROFILES);
    }
}
