package com.example.simulation;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Lightweight animal diet database (vanilla baseline) used by UI/AI validation helpers.
 */
public class AnimalDietDatabase {

    public static class Profile {
        private final EntityType<?> entityType;
        private final String indonesianName;
        private final String dietType;
        private final Set<Item> foods;
        private final String feedingBehavior;
        private final String habitat;
        private final String specialRequirements;

        public Profile(EntityType<?> entityType, String indonesianName, String dietType,
                       Set<Item> foods, String feedingBehavior, String habitat, String specialRequirements) {
            this.entityType = entityType;
            this.indonesianName = indonesianName;
            this.dietType = dietType;
            this.foods = Set.copyOf(foods);
            this.feedingBehavior = feedingBehavior;
            this.habitat = habitat;
            this.specialRequirements = specialRequirements;
        }

        public EntityType<?> entityType() { return entityType; }
        public String indonesianName() { return indonesianName; }
        public String dietType() { return dietType; }
        public Set<Item> foods() { return foods; }
        public String feedingBehavior() { return feedingBehavior; }
        public String habitat() { return habitat; }
        public String specialRequirements() { return specialRequirements; }
    }

    private static final Map<EntityType<?>, Profile> ANIMAL_PROFILES = new HashMap<>();

    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        addProfile(new Profile(
                EntityType.WOLF, "Serigala", "Karnivora",
                Set.of(Items.BEEF, Items.PORKCHOP, Items.CHICKEN),
                "Daging mentah", "Hutan", "Berburu berkelompok"
        ));

        addProfile(new Profile(
                EntityType.FOX, "Rubah", "Omnivora",
                Set.of(Items.SWEET_BERRIES, Items.CHICKEN),
                "Buah dan hewan kecil", "Hutan", "Aktif malam"
        ));

        addProfile(new Profile(
                EntityType.PANDA, "Panda", "Herbivora",
                Set.of(Items.BAMBOO),
                "Bambu segar", "Hutan bambu", "Sensitif pada habitat"
        ));

        addProfile(new Profile(
                EntityType.POLAR_BEAR, "Beruang Kutub", "Karnivora",
                Set.of(Items.SALMON, Items.COD),
                "Ikan segar", "Bioma dingin", "Membutuhkan area dingin"
        ));

        addProfile(new Profile(
                EntityType.PARROT, "Burung", "Omnivora",
                Set.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS),
                "Biji-bijian", "Hutan", "Makan dalam porsi kecil"
        ));
    }

    private static void addProfile(Profile profile) {
        ANIMAL_PROFILES.put(profile.entityType(), profile);
    }

    public static Profile getProfile(EntityType<?> entityType) {
        return ANIMAL_PROFILES.get(entityType);
    }

    public static boolean isValidFood(EntityType<?> entityType, ItemStack food) {
        if (food.isEmpty()) return false;
        Profile profile = getProfile(entityType);
        return profile != null && profile.foods().contains(food.getItem());
    }

    public static List<Profile> getAnimalsByFood(Item food) {
        List<Profile> animals = new ArrayList<>();
        for (Profile profile : ANIMAL_PROFILES.values()) {
            if (profile.foods().contains(food)) animals.add(profile);
        }
        return animals;
    }

    public static List<Profile> getAnimalsByDiet(String dietType) {
        List<Profile> animals = new ArrayList<>();
        for (Profile profile : ANIMAL_PROFILES.values()) {
            if (profile.dietType().equalsIgnoreCase(dietType)) animals.add(profile);
        }
        return animals;
    }

    public static List<Profile> getAnimalsByHabitat(String habitat) {
        List<Profile> animals = new ArrayList<>();
        for (Profile profile : ANIMAL_PROFILES.values()) {
            if (profile.habitat().equalsIgnoreCase(habitat)) animals.add(profile);
        }
        return animals;
    }

    public static Set<Item> getAllAnimalFoods() {
        Set<Item> foods = new HashSet<>();
        for (Profile profile : ANIMAL_PROFILES.values()) foods.addAll(profile.foods());
        foods.addAll(FoodAnimalRegistry.getProfiles().keySet());
        return foods;
    }

    public static String getFeedingRecommendation(EntityType<?> entityType) {
        Profile profile = getProfile(entityType);
        if (profile == null) return "Makanan umum";
        return profile.indonesianName() + " (" + profile.dietType() + ") - " + profile.feedingBehavior();
    }

    public static String getSpecialRequirements(EntityType<?> entityType) {
        Profile profile = getProfile(entityType);
        return profile == null ? "Tidak ada kebutuhan khusus" : profile.specialRequirements();
    }

    public static Collection<Profile> getAllProfiles() {
        return ANIMAL_PROFILES.values();
    }

    public static boolean hasProfile(EntityType<?> entityType) {
        return ANIMAL_PROFILES.containsKey(entityType);
    }

    public static Item getRandomFood(EntityType<?> entityType) {
        Profile profile = getProfile(entityType);
        if (profile == null || profile.foods().isEmpty()) return Items.WHEAT;
        List<Item> foods = new ArrayList<>(profile.foods());
        return foods.get(new Random().nextInt(foods.size()));
    }

    public static List<Item> getRecommendedShopFoods() {
        return new ArrayList<>(FoodAnimalRegistry.getProfiles().keySet());
    }

    public static String generateFoodTooltip(Item food) {
        return FoodAnimalRegistry.getFoodTooltip(food);
    }
}
