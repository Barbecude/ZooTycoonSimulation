package net.zootycoon.animal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class AnimalDietRegistry {

    public enum FeedingType {
        TROUGH,         // Herbivora / Pemakan Palung Pakan
        THROWN_MEAT     // Karnivora / Predator (Dilempar Daging/Ikan)
    }

    public static class AnimalDiet {
        public final FeedingType feedingType;
        public final List<Item> acceptedFoods;
        public final String description;

        public AnimalDiet(FeedingType feedingType, String description, Item... foods) {
            this.feedingType = feedingType;
            this.description = description;
            this.acceptedFoods = Arrays.asList(foods);
        }
    }

    private static final Map<String, AnimalDiet> DIET_REGISTRY = new HashMap<>();

    static {
        initializeRegistry();
    }

    private static void initializeRegistry() {
        // ==========================================
        // 1. VANILLA MINECRAFT
        // ==========================================
        // Herbivores (Trough)
        register("minecraft:cow", FeedingType.TROUGH, "Herbivora (Gandum & Rumput)", Items.WHEAT, Items.HAY_BLOCK);
        register("minecraft:sheep", FeedingType.TROUGH, "Herbivora (Gandum)", Items.WHEAT);
        register("minecraft:pig", FeedingType.TROUGH, "Omnivora (Wortel & Kentang)", Items.CARROT, Items.POTATO, Items.BEETROOT);
        register("minecraft:chicken", FeedingType.TROUGH, "Granivora (Biji-bijian)", Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
        register("minecraft:horse", FeedingType.TROUGH, "Herbivora (Apel & Gandum)", Items.APPLE, Items.GOLDEN_APPLE, Items.WHEAT, Items.HAY_BLOCK, Items.CARROT);
        register("minecraft:donkey", FeedingType.TROUGH, "Herbivora (Apel & Gandum)", Items.APPLE, Items.WHEAT, Items.HAY_BLOCK, Items.CARROT);
        register("minecraft:mule", FeedingType.TROUGH, "Herbivora (Apel & Gandum)", Items.APPLE, Items.WHEAT, Items.HAY_BLOCK, Items.CARROT);
        register("minecraft:llama", FeedingType.TROUGH, "Herbivora (Jerami & Gandum)", Items.HAY_BLOCK, Items.WHEAT);
        register("minecraft:trader_llama", FeedingType.TROUGH, "Herbivora (Jerami)", Items.HAY_BLOCK, Items.WHEAT);
        register("minecraft:goat", FeedingType.TROUGH, "Herbivora (Gandum)", Items.WHEAT);
        register("minecraft:rabbit", FeedingType.TROUGH, "Herbivora (Wortel & Dandelion)", Items.CARROT, Items.GOLDEN_CARROT, Items.DANDELION);
        register("minecraft:panda", FeedingType.TROUGH, "Folivora (Bambu)", Items.BAMBOO);
        register("minecraft:sniffer", FeedingType.TROUGH, "Herbivora (Benih Torchflower)", Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);
        register("minecraft:camel", FeedingType.TROUGH, "Herbivora (Kaktus)", Items.CACTUS);
        register("minecraft:strider", FeedingType.TROUGH, "Pemakan Jamur Nether", Items.WARPED_FUNGUS);

        // Carnivores / Predators (Thrown Meat)
        register("minecraft:wolf", FeedingType.THROWN_MEAT, "Karnivora (Daging Segar)", Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.ROTTEN_FLESH);
        register("minecraft:ocelot", FeedingType.THROWN_MEAT, "Piscivora (Ikan Segar)", Items.COD, Items.SALMON);
        register("minecraft:cat", FeedingType.THROWN_MEAT, "Piscivora (Ikan Segar)", Items.COD, Items.SALMON);
        register("minecraft:fox", FeedingType.THROWN_MEAT, "Omnivora (Beri & Daging)", Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.CHICKEN, Items.RABBIT);
        register("minecraft:polar_bear", FeedingType.THROWN_MEAT, "Karnivora (Ikan Segar)", Items.SALMON, Items.COD);
        register("minecraft:dolphin", FeedingType.THROWN_MEAT, "Piscivora (Ikan Mentah)", Items.COD, Items.SALMON);

        // ==========================================
        // 2. ALEX'S MOBS
        // ==========================================
        // Herbivores / Frugivores / Grazers (Trough)
        register("alexsmobs:gorilla", FeedingType.TROUGH, "Frugivora (Pisang & Buah)", Items.MELON_SLICE, Items.APPLE, Items.SWEET_BERRIES);
        register("alexsmobs:capuchin_monkey", FeedingType.TROUGH, "Frugivora (Pisang & Buah)", Items.MELON_SLICE, Items.APPLE, Items.SWEET_BERRIES);
        register("alexsmobs:elephant", FeedingType.TROUGH, "Herbivora Besar (Melon & Jerami)", Items.MELON_SLICE, Items.HAY_BLOCK, Items.APPLE);
        register("alexsmobs:kangaroo", FeedingType.TROUGH, "Herbivora (Gandum & Rumput)", Items.WHEAT, Items.CARROT, Items.APPLE);
        register("alexsmobs:moose", FeedingType.TROUGH, "Herbivora (Gandum & Daun)", Items.WHEAT, Items.OAK_LEAVES, Items.APPLE);
        register("alexsmobs:gazelle", FeedingType.TROUGH, "Herbivora (Gandum & Rumput)", Items.WHEAT);
        register("alexsmobs:gelada_monkey", FeedingType.TROUGH, "Herbivora (Rumput & Biji)", Items.WHEAT, Items.WHEAT_SEEDS);
        register("alexsmobs:emu", FeedingType.TROUGH, "Granivora (Biji-bijian)", Items.WHEAT_SEEDS, Items.MELON_SEEDS);
        register("alexsmobs:platypus", FeedingType.TROUGH, "Pemakan Udang & Serangga", Items.COD, Items.SALMON);
        register("alexsmobs:roadrunner", FeedingType.TROUGH, "Pemakan Biji & Serangga)", Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);
        register("alexsmobs:hummingbird", FeedingType.TROUGH, "Nektar (Bunga)", Items.DANDELION, Items.POPPY);
        register("alexsmobs:raccoon", FeedingType.TROUGH, "Omnivora (Beri & Roti)", Items.SWEET_BERRIES, Items.BREAD, Items.EGG);

        // Carnivores / Predators (Thrown Meat)
        register("alexsmobs:tiger", FeedingType.THROWN_MEAT, "Predator Puncak (Daging Sapi & Babi)", Items.BEEF, Items.PORKCHOP, Items.MUTTON);
        register("alexsmobs:lion", FeedingType.THROWN_MEAT, "Predator Puncak (Daging Segar)", Items.BEEF, Items.PORKCHOP, Items.MUTTON);
        register("alexsmobs:crocodile", FeedingType.THROWN_MEAT, "Karnivora Air (Daging & Ikan)", Items.BEEF, Items.PORKCHOP, Items.COD, Items.SALMON);
        register("alexsmobs:alligator_snapping_turtle", FeedingType.THROWN_MEAT, "Karnivora (Ikan & Daging)", Items.COD, Items.SALMON, Items.BEEF);
        register("alexsmobs:grizzly_bear", FeedingType.THROWN_MEAT, "Omnivora Besar (Ikan & Daging)", Items.SALMON, Items.COD, Items.HONEY_BOTTLE, Items.SWEET_BERRIES);
        register("alexsmobs:komodo_dragon", FeedingType.THROWN_MEAT, "Karnivora Berbisa (Daging)", Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.ROTTEN_FLESH);
        register("alexsmobs:seagull", FeedingType.THROWN_MEAT, "Piscivora (Ikan)", Items.COD, Items.SALMON);
        register("alexsmobs:bald_eagle", FeedingType.THROWN_MEAT, "Pemangsa Udara (Ikan & Kelinci)", Items.SALMON, Items.COD, Items.RABBIT);
        register("alexsmobs:snake", FeedingType.THROWN_MEAT, "Karnivora (Ayam & Daging)", Items.CHICKEN, Items.RABBIT);
        register("alexsmobs:rattlesnake", FeedingType.THROWN_MEAT, "Karnivora (Daging Segar)", Items.CHICKEN, Items.RABBIT);
        register("alexsmobs:orca", FeedingType.THROWN_MEAT, "Predator Laut (Ikan & Daging)", Items.SALMON, Items.COD, Items.BEEF);

        // ==========================================
        // 3. NATURALIST
        // ==========================================
        // Herbivores (Trough)
        register("naturalist:elephant", FeedingType.TROUGH, "Herbivora Raksasa (Melon & Jerami)", Items.MELON_SLICE, Items.HAY_BLOCK, Items.APPLE);
        register("naturalist:giraffe", FeedingType.TROUGH, "Folivora (Daun Akasia)", Items.ACACIA_LEAVES, Items.OAK_LEAVES, Items.APPLE);
        register("naturalist:zebra", FeedingType.TROUGH, "Herbivora (Gandum & Rumput)", Items.WHEAT, Items.HAY_BLOCK, Items.CARROT);
        register("naturalist:deer", FeedingType.TROUGH, "Herbivora (Gandum & Apel)", Items.WHEAT, Items.APPLE, Items.CARROT);
        register("naturalist:rhino", FeedingType.TROUGH, "Herbivora (Gandum & Melon)", Items.WHEAT, Items.MELON_SLICE, Items.HAY_BLOCK);
        register("naturalist:hippo", FeedingType.TROUGH, "Herbivora (Melon & Gandum)", Items.MELON_SLICE, Items.WHEAT, Items.APPLE);
        register("naturalist:duck", FeedingType.TROUGH, "Granivora (Biji-bijian)", Items.WHEAT_SEEDS, Items.MELON_SEEDS);
        register("naturalist:canary", FeedingType.TROUGH, "Granivora (Biji-bijian)", Items.WHEAT_SEEDS);
        register("naturalist:sparrow", FeedingType.TROUGH, "Granivora (Biji-bijian)", Items.WHEAT_SEEDS);
        register("naturalist:tortoise", FeedingType.TROUGH, "Herbivora (Wortel & Bunga)", Items.CARROT, Items.DANDELION);

        // Carnivores / Predators (Thrown Meat)
        register("naturalist:lion", FeedingType.THROWN_MEAT, "Predator Savana (Daging Sapi & Babi)", Items.BEEF, Items.PORKCHOP, Items.MUTTON);
        register("naturalist:tiger", FeedingType.THROWN_MEAT, "Predator Rimba (Daging Sapi & Babi)", Items.BEEF, Items.PORKCHOP, Items.MUTTON);
        register("naturalist:bear", FeedingType.THROWN_MEAT, "Omnivora Besar (Ikan & Daging)", Items.SALMON, Items.COD, Items.HONEY_BOTTLE, Items.BEEF);
        register("naturalist:wolf", FeedingType.THROWN_MEAT, "Karnivora (Daging Segar)", Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON);
        register("naturalist:alligator", FeedingType.THROWN_MEAT, "Karnivora Rawa (Daging & Ikan)", Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.SALMON);
        register("naturalist:crocodile", FeedingType.THROWN_MEAT, "Karnivora Sungai (Daging & Ikan)", Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.SALMON);
        register("naturalist:leopard", FeedingType.THROWN_MEAT, "Karnivora Cepat (Daging Segar)", Items.BEEF, Items.PORKCHOP, Items.CHICKEN);
        register("naturalist:snake", FeedingType.THROWN_MEAT, "Karnivora (Daging Ayam & Kelinci)", Items.CHICKEN, Items.RABBIT);
        register("naturalist:boar", FeedingType.THROWN_MEAT, "Omnivora Liar (Wortel & Daging)", Items.CARROT, Items.POTATO, Items.BEEF);
        register("naturalist:hyena", FeedingType.THROWN_MEAT, "Karnivora (Daging Segar & Bangkai)", Items.BEEF, Items.PORKCHOP, Items.ROTTEN_FLESH);
    }

    public static void register(String entityTypeId, FeedingType feedingType, String description, Item... foods) {
        DIET_REGISTRY.put(entityTypeId.toLowerCase(Locale.ROOT), new AnimalDiet(feedingType, description, foods));
    }

    public static AnimalDiet getDiet(Entity entity) {
        if (entity == null) return getFallbackDiet(null);
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String key = id != null ? id.toString().toLowerCase(Locale.ROOT) : "";

        AnimalDiet diet = DIET_REGISTRY.get(key);
        if (diet != null) return diet;

        // Fallback check
        return getFallbackDiet(entity);
    }

    private static AnimalDiet getFallbackDiet(Entity entity) {
        if (entity instanceof Animal) {
            // Default peaceful animal -> Trough with Wheat, Carrots, Seeds
            return new AnimalDiet(FeedingType.TROUGH, "Herbivora Umum (Gandum & Sayur)", Items.WHEAT, Items.CARROT, Items.APPLE, Items.WHEAT_SEEDS);
        }
        // Hostile / Monster / Predator fallback -> Thrown Meat
        return new AnimalDiet(FeedingType.THROWN_MEAT, "Karnivora Umum (Daging Segar)", Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.ROTTEN_FLESH);
    }

    public static FeedingType getFeedingType(Entity entity) {
        return getDiet(entity).feedingType;
    }

    public static List<Item> getAcceptedFoods(Entity entity) {
        return getDiet(entity).acceptedFoods;
    }

    public static boolean isFoodValid(Entity entity, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        List<Item> accepted = getAcceptedFoods(entity);
        return accepted.contains(item);
    }

    public static String getDietDescription(Entity entity) {
        return getDiet(entity).description;
    }

    public static String getModSource(Entity entity) {
        if (entity == null) return "Vanilla";
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return "Vanilla";
        String ns = id.getNamespace();
        if (ns.equalsIgnoreCase("minecraft")) return "Vanilla";
        if (ns.equalsIgnoreCase("alexsmobs")) return "Alex's Mobs";
        if (ns.equalsIgnoreCase("naturalist")) return "Naturalist";
        return capitalize(ns);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

