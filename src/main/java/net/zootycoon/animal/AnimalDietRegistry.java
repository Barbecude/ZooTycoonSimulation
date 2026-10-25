package net.zootycoon.animal;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnimalDietRegistry {

    public static final TagKey<Item> RAW_MEAT_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("zootycoon", "animal_food/raw_meat"));
    public static final TagKey<Item> RAW_FISH_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("zootycoon", "animal_food/raw_fish"));
    public static final TagKey<Item> C_RAW_MEAT_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/raw_meat"));
    public static final TagKey<Item> C_RAW_MEATS_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/raw_meats"));
    public static final TagKey<Item> C_RAW_FISH_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/raw_fish"));
    public static final TagKey<Item> C_RAW_FISHES_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/raw_fishes"));
    public static final TagKey<Item> C_VEGETABLES_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/vegetable"));
    public static final TagKey<Item> C_FRUITS_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/fruit"));
    public static final TagKey<Item> C_BERRIES_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/berry"));
    public static final TagKey<Item> C_SEEDS_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "seeds"));

    private static final Set<String> SUPPORTED_FOOD_MODS = Set.of(
            "minecraft", "naturalist", "farmersdelight", "moredelight",
            "farm_and_charm", "alexsmobs", "wildernature"
    );

    private static final List<String> EXPLICIT_RAW_MEAT_IDS = List.of(
            "minecraft:beef", "minecraft:porkchop", "minecraft:chicken", "minecraft:mutton", "minecraft:rabbit",
            "farmersdelight:chicken_cuts", "farmersdelight:minced_beef", "farmersdelight:bacon",
            "farmersdelight:mutton_chops", "farmersdelight:ham",
            "moredelight:diced_ham", "moredelight:raw_B_bacon", "moredelight:chicken_ breast",
            "naturalist:venison", "naturalist:bushmeat", "naturalist:duck", "naturalist:drumstick",
            "naturalist:morsel", "naturalist:lizard_tail", "naturalist:mammoth_meat",
            "naturalist:crab_meat", "naturalist:clam_meat",
            "farm_and_charm:minced_beef", "farm_and_charm:chicken_parts", "farm_and_charm:lamb_ham", "farm_and_charm:bacon",
            "alexsmobs:kangaroo_meat", "alexsmobs:moose_ribs", "alexsmobs:raw_tendon",
            "wildernature:bison_meat", "wildernature:cassowary_meat", "wildernature:turkey_meat", "wildernature:venison"
    );

    private static final List<String> EXPLICIT_RAW_FISH_IDS = List.of(
            "minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", "minecraft:pufferfish",
            "farmersdelight:cod_slice", "farmersdelight:salmon_slice",
            "naturalist:catfish", "naturalist:bass", "naturalist:anglerfish", "naturalist:piranha", "naturalist:blobfish",
            "alexsmobs:raw_catfish", "alexsmobs:flying_fish", "alexsmobs:blobfish", "alexsmobs:lobster_tail"
    );

    private static final List<String> EXPLICIT_HERBIVORE_CROP_IDS = List.of(
            "farmersdelight:cabbage", "farmersdelight:cabbage_leaf", "farmersdelight:tomato",
            "farmersdelight:onion", "farmersdelight:rice_panicle", "farmersdelight:pumpkin_slice",
            "farmersdelight:wild_carrots", "farmersdelight:wild_potatoes", "farmersdelight:wild_beetroots",
            "farmersdelight:wild_cabbages", "farmersdelight:wild_tomatoes", "farmersdelight:wild_onions",
            "farmersdelight:straw", "farmersdelight:rice_bale", "farmersdelight:straw_bale",
            "moredelight:diced_potatoes", "moredelight:sliced_bread",
            "farm_and_charm:corn", "farm_and_charm:oat", "farm_and_charm:barley",
            "farm_and_charm:lettuce", "farm_and_charm:tomato", "farm_and_charm: onion", "farm_and_charm:strawberry",
            "alexsmobs:banana", "alexsmobs:acacia_blossom", "alexsmobs:gongylidia"
    );

    private static final List<String> EXPLICIT_SEED_IDS = List.of(
            "minecraft:wheat_seeds", "minecraft:melon_seeds", "minecraft:pumpkin_seeds",
            "minecraft:beetroot_seeds", "minecraft:torchflower_seeds", "minecraft:pitcher_pod",
            "farmersdelight:cabbage_seeds", "farmersdelight:tomato_seeds", "farmersdelight:rice",
            "farm_and_charm:kernels", "farm_and_charm:oat_seeds", "farm_and_charm:barley_seeds",
            "farm_and_charm:lettuce_seeds", "farm_and_charm:tomato_seeds", "farm_and_charm:strawberry_seeds"
    );

    private static final List<String> EXPLICIT_INSECT_IDS = List.of(
            "naturalist:Caterpillar", "naturalist:caterpillar", "naturalist:grub", "naturalist:snail",
            "alexsmobs:maggot", "alexsmobs:cockroach_wing", "alexsmobs: mosquito_larva"
    );

    public static boolean isRawMeat(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        String ns = id.getNamespace().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_FOOD_MODS.contains(ns)) return false;
        if (stack.is(RAW_MEAT_TAG) || stack.is(C_RAW_MEAT_TAG) || stack.is(C_RAW_MEATS_TAG)) return true;
        String full = id.toString().toLowerCase(Locale.ROOT);
        if (EXPLICIT_RAW_MEAT_IDS.contains(full)) return true;

        String path = id.getPath().toLowerCase(Locale.ROOT);
        if (isCookedOrPreparedDish(path)) return false;

        return path.contains("raw_meat") || path.contains("minced_beef") || path.contains("chicken_cuts")
                || path.contains("mutton_chops") || path.equals("bacon") || path.endsWith("_bacon")
                || path.equals("ham") || path.endsWith("_ham") || path.endsWith("_venison")
                || path.equals("venison") || path.equals("bushmeat") || path.equals("morsel")
                || path.equals("drumstick") || path.endsWith("_meat") || path.equals("lizard_tail")
                || path.contains("raw_beef") || path.contains("raw_pork") || path.contains("raw_chicken")
                || path.contains("raw_mutton") || path.contains("raw_rabbit") || path.contains("raw_duck");
    }

    public static boolean isRawFish(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        String ns = id.getNamespace().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_FOOD_MODS.contains(ns)) return false;
        if (stack.is(RAW_FISH_TAG) || stack.is(C_RAW_FISH_TAG) || stack.is(C_RAW_FISHES_TAG)) return true;
        String full = id.toString().toLowerCase(Locale.ROOT);
        if (EXPLICIT_RAW_FISH_IDS.contains(full)) return true;

        String path = id.getPath().toLowerCase(Locale.ROOT);
        if (isCookedOrPreparedDish(path)) return false;

        return path.equals("cod_slice") || path.equals("salmon_slice") || path.equals("catfish")
                || path.equals("raw_catfish") || path.equals("bass") || path.equals("anglerfish")
                || path.equals("piranha") || path.equals("blobfish") || path.equals("flying_fish")
                || path.equals("lobster_tail") || path.contains("raw_fish") || path.endsWith("_fillet");
    }

    public static boolean isModProduceOrCrop(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        String ns = id.getNamespace().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_FOOD_MODS.contains(ns)) return false;
        if (stack.is(C_VEGETABLES_TAG) || stack.is(C_FRUITS_TAG) || stack.is(C_BERRIES_TAG)) return true;
        String full = id.toString().toLowerCase(Locale.ROOT);
        if (EXPLICIT_HERBIVORE_CROP_IDS.contains(full)) return true;

        String path = id.getPath().toLowerCase(Locale.ROOT);
        if (isCookedOrPreparedDish(path)) return false;

        return path.equals("cabbage") || path.equals("cabbage_leaf") || path.equals("tomato")
                || path.equals("onion") || path.equals("pumpkin_slice") || path.equals("rice_panicle")
                || path.equals("diced_potatoes") || path.equals("banana") || path.endsWith("_berries")
                || path.equals("lettuce") || path.equals("strawberry") || path.equals("corn")
                || path.equals("oat") || path.equals("barley");
    }

    public static boolean isSeedOrGrain(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        String ns = id.getNamespace().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_FOOD_MODS.contains(ns)) return false;
        if (stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS) || stack.is(C_SEEDS_TAG)) return true;
        String full = id.toString().toLowerCase(Locale.ROOT);
        if (EXPLICIT_SEED_IDS.contains(full)) return true;

        String path = id.getPath().toLowerCase(Locale.ROOT);
        return path.endsWith("_seeds") || path.equals("rice") || path.equals("kernels");
    }

    private static boolean isCookedOrPreparedDish(String path) {
        return path.contains("cooked") || path.contains("roasted") || path.contains("fried")
                || path.contains("baked") || path.contains("stew") || path.contains("soup")
                || path.contains("sandwich") || path.contains("burger") || path.contains("pasta")
                || path.contains("noodle") || path.contains("pie") || path.contains("cake")
                || path.contains("cookie") || path.contains("roll") || path.contains("salad")
                || path.contains("skewer") || path.contains("toast") || path.contains("steak")
                || path.contains("barbecue") || path.contains("dumpling") || path.contains("stuffed")
                || path.contains("plate") || path.contains("bowl") || path.contains("juice")
                || path.contains("cider") || path.contains("crate") || path.contains("bag")
                || path.contains("knife") || path.contains("stove") || path.contains("pot")
                || path.contains("skillet") || path.contains("cabinet") || path.contains("tatami")
                || path.contains("spawn_egg") || path.contains("bucket") || path.contains("banner");
    }

    public enum FeedingType {
        TROUGH("Perlu Palung Makanan"),
        THROWN_MEAT("Lempar Makanan");

        private final String label;

        FeedingType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    public static class AnimalDiet {
        public final FeedingType feedingType;
        public final String dietCategory;
        public final List<Item> acceptedFoods;
        public final String description;
        public final boolean acceptsRawMeat;
        public final boolean acceptsRawFish;
        public final boolean isFallback;

        public AnimalDiet(FeedingType feedingType, String dietCategory, String description, boolean acceptsRawMeat, boolean acceptsRawFish, boolean isFallback, Item... foods) {
            this.feedingType = feedingType;
            this.dietCategory = (dietCategory != null && !dietCategory.isEmpty()) ? dietCategory : deduceCategory(feedingType, description);
            this.description = description;
            this.acceptsRawMeat = acceptsRawMeat;
            this.acceptsRawFish = acceptsRawFish;
            this.isFallback = isFallback;
            this.acceptedFoods = Arrays.asList(foods);
        }

        public AnimalDiet(FeedingType feedingType, String description, boolean acceptsRawMeat, boolean acceptsRawFish, boolean isFallback, Item... foods) {
            this(feedingType, deduceCategory(feedingType, description), description, acceptsRawMeat, acceptsRawFish, isFallback, foods);
        }

        public AnimalDiet(FeedingType feedingType, String description, boolean acceptsRawMeat, boolean acceptsRawFish, Item... foods) {
            this(feedingType, description, acceptsRawMeat, acceptsRawFish, false, foods);
        }

        public AnimalDiet(FeedingType feedingType, String description, Item... foods) {
            this(feedingType, description, autoDetectRawMeat(feedingType, foods), autoDetectRawFish(foods), false, foods);
        }

        public String getSubtitle() {
            return dietCategory + " (" + feedingType.getLabel() + ")";
        }
    }

    private static String deduceCategory(FeedingType feedingType, String description) {
        if (description == null || description.isEmpty()) {
            return feedingType == FeedingType.THROWN_MEAT ? "Karnivora" : "Herbivora";
        }
        String d = description.toLowerCase(Locale.ROOT);
        if (d.contains("granivora")) return "Granivora";
        if (d.contains("frugivora")) return "Frugivora";
        if (d.contains("insektivora") || d.contains("serangga")) return "Insektivora";
        if (d.contains("piscivora") || d.contains("ikan")) return "Piscivora";
        if (d.contains("jamur") || d.contains("spora") || d.contains("fungi")) return "Pemakan Jamur";
        if (d.contains("mineral") || d.contains("pasir")) return "Pemakan Mineral";
        if (d.contains("nektar")) return "Nektarivora";
        if (d.contains("omnivora")) return "Omnivora";
        if (d.contains("karnivora") || d.contains("predator") || d.contains("bangkai") || d.contains("pemangsa")) return "Karnivora";
        if (d.contains("herbivora") || d.contains("folivora")) return "Herbivora";
        return feedingType == FeedingType.THROWN_MEAT ? "Karnivora" : "Herbivora";
    }

    private static boolean autoDetectRawMeat(FeedingType feedingType, Item... foods) {
        if (foods == null) return false;
        for (Item f : foods) {
            if (f == Items.BEEF || f == Items.PORKCHOP || f == Items.CHICKEN || f == Items.MUTTON || f == Items.RABBIT) {
                return true;
            }
        }
        return false;
    }

    private static boolean autoDetectRawFish(Item... foods) {
        if (foods == null) return false;
        for (Item f : foods) {
            if (f == Items.COD || f == Items.SALMON || f == Items.TROPICAL_FISH || f == Items.PUFFERFISH) {
                return true;
            }
        }
        return false;
    }

    private static final Map<String, AnimalDiet> DIET_REGISTRY = new HashMap<>();

    static {
        initializeRegistry();
    }

    private static void initializeRegistry() {
        // ==========================================
        // 1. VANILLA MINECRAFT
        // ==========================================
        register("minecraft:cow", FeedingType.TROUGH, "Herbivora (Gandum & Sayuran)", Items.WHEAT, Items.HAY_BLOCK);
        register("minecraft:mooshroom", FeedingType.TROUGH, "Herbivora (Gandum & Jamur)", Items.WHEAT, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM);
        register("minecraft:sheep", FeedingType.TROUGH, "Herbivora (Gandum & Rumput)", Items.WHEAT, Items.HAY_BLOCK);
        register("minecraft:pig", FeedingType.TROUGH, "Omnivora (Wortel, Kentang & Sayur)", Items.CARROT, Items.POTATO, Items.BEETROOT, Items.APPLE);
        register("minecraft:chicken", FeedingType.TROUGH, "Granivora (Biji-bijian)", Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);
        register("minecraft:horse", FeedingType.TROUGH, "Herbivora (Apel, Wortel & Gandum)", Items.APPLE, Items.GOLDEN_APPLE, Items.WHEAT, Items.HAY_BLOCK, Items.CARROT, Items.SUGAR);
        register("minecraft:donkey", FeedingType.TROUGH, "Herbivora (Apel & Gandum)", Items.APPLE, Items.WHEAT, Items.HAY_BLOCK, Items.CARROT);
        register("minecraft:mule", FeedingType.TROUGH, "Herbivora (Apel & Gandum)", Items.APPLE, Items.WHEAT, Items.HAY_BLOCK, Items.CARROT);
        register("minecraft:llama", FeedingType.TROUGH, "Herbivora (Jerami & Gandum)", Items.HAY_BLOCK, Items.WHEAT);
        register("minecraft:trader_llama", FeedingType.TROUGH, "Herbivora (Jerami & Gandum)", Items.HAY_BLOCK, Items.WHEAT);
        register("minecraft:goat", FeedingType.TROUGH, "Herbivora (Gandum & Sayuran)", Items.WHEAT, Items.CARROT);
        register("minecraft:rabbit", FeedingType.TROUGH, "Herbivora (Wortel, Kubis & Dandelion)", Items.CARROT, Items.GOLDEN_CARROT, Items.DANDELION);
        register("minecraft:panda", FeedingType.TROUGH, "Folivora (Bambu & Kue)", Items.BAMBOO, Items.CAKE);
        register("minecraft:sniffer", FeedingType.TROUGH, "Herbivora (Benih Purba)", Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);
        register("minecraft:camel", FeedingType.TROUGH, "Herbivora (Kaktus, Jerami & Gandum)", Items.CACTUS, Items.HAY_BLOCK, Items.WHEAT);
        register("minecraft:strider", FeedingType.TROUGH, "Pemakan Jamur Nether", Items.WARPED_FUNGUS);
        register("minecraft:turtle", FeedingType.TROUGH, "Herbivora Laut (Lamun)", Items.SEAGRASS);
        register("minecraft:frog", FeedingType.TROUGH, "Insektivora (Slime & Serangga)", Items.SLIME_BALL);
        register("minecraft:armadillo", FeedingType.TROUGH, "Insektivora (Mata Laba-laba & Serangga)", Items.SPIDER_EYE);

        // Carnivores / Predators (Thrown Meat / Fish)
        register("minecraft:wolf", FeedingType.THROWN_MEAT, "Karnivora (Daging Segar)", true, false, Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT, Items.ROTTEN_FLESH);
        register("minecraft:ocelot", FeedingType.THROWN_MEAT, "Piscivora (Ikan Segar)", false, true, Items.COD, Items.SALMON, Items.TROPICAL_FISH);
        register("minecraft:cat", FeedingType.THROWN_MEAT, "Piscivora (Ikan Segar)", false, true, Items.COD, Items.SALMON);
        register("minecraft:fox", FeedingType.THROWN_MEAT, "Omnivora (Beri, Ayam & Daging)", true, false, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.CHICKEN, Items.RABBIT);
        register("minecraft:polar_bear", FeedingType.THROWN_MEAT, "Karnivora Kutub (Ikan & Daging)", true, true, Items.SALMON, Items.COD, Items.BEEF, Items.PORKCHOP);
        register("minecraft:dolphin", FeedingType.THROWN_MEAT, "Piscivora (Ikan Mentah)", false, true, Items.COD, Items.SALMON, Items.TROPICAL_FISH);
        register("minecraft:axolotl", FeedingType.THROWN_MEAT, "Piscivora (Ikan Tropis)", false, true, Items.TROPICAL_FISH, Items.COD, Items.SALMON);

        // ==========================================
        // 2. ALEX'S MOBS
        // ==========================================
        register("alexsmobs:gorilla", FeedingType.TROUGH, "Frugivora (Pisang & Buah)", Items.MELON_SLICE, Items.APPLE, Items.SWEET_BERRIES);
        register("alexsmobs:capuchin_monkey", FeedingType.TROUGH, "Frugivora (Pisang, Buah & Telur)", Items.MELON_SLICE, Items.APPLE, Items.SWEET_BERRIES, Items.EGG);
        register("alexsmobs:elephant", FeedingType.TROUGH, "Herbivora Besar (Melon, Jerami & Sayur)", Items.MELON_SLICE, Items.HAY_BLOCK, Items.APPLE, Items.CARROT);
        register("alexsmobs:kangaroo", FeedingType.TROUGH, "Herbivora (Gandum, Jerami, Wortel & Rumput)", Items.WHEAT, Items.HAY_BLOCK, Items.CARROT, Items.APPLE);
        register("alexsmobs:moose", FeedingType.TROUGH, "Herbivora (Gandum, Daun & Apel)", Items.WHEAT, Items.OAK_LEAVES, Items.APPLE, Items.DANDELION);
        register("alexsmobs:gazelle", FeedingType.TROUGH, "Herbivora (Gandum & Rumput)", Items.WHEAT, Items.HAY_BLOCK);
        register("alexsmobs:bison", FeedingType.TROUGH, "Herbivora (Gandum & Jerami)", Items.WHEAT, Items.HAY_BLOCK);
        register("alexsmobs:gelada_monkey", FeedingType.TROUGH, "Herbivora (Rumput & Biji)", Items.WHEAT, Items.WHEAT_SEEDS);
        register("alexsmobs:emu", FeedingType.TROUGH, "Granivora (Biji-bijian, Rumput & Buah)", Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.WHEAT, Items.SWEET_BERRIES, Items.APPLE);
        register("alexsmobs:platypus", FeedingType.TROUGH, "Pemakan Ikan & Serangga Air", false, true, Items.COD, Items.SALMON);
        register("alexsmobs:roadrunner", FeedingType.TROUGH, "Pemakan Biji & Serangga", Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);
        register("alexsmobs:hummingbird", FeedingType.TROUGH, "Nektar (Bunga & Gula)", Items.DANDELION, Items.POPPY, Items.SUGAR);
        register("alexsmobs:raccoon", FeedingType.TROUGH, "Omnivora (Beri, Roti & Telur)", Items.SWEET_BERRIES, Items.BREAD, Items.EGG, Items.APPLE);

        register("alexsmobs:tiger", FeedingType.THROWN_MEAT, "Predator Puncak (Daging Segar)", true, false, Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.CHICKEN);
        register("alexsmobs:lion", FeedingType.THROWN_MEAT, "Predator Puncak (Daging Segar)", true, false, Items.BEEF, Items.PORKCHOP, Items.MUTTON);
        register("alexsmobs:snow_leopard", FeedingType.THROWN_MEAT, "Predator Pegunungan (Daging Segar)", true, false, Items.BEEF, Items.MUTTON, Items.RABBIT, Items.CHICKEN);
        register("alexsmobs:crocodile", FeedingType.THROWN_MEAT, "Karnivora Air (Daging & Ikan)", true, true, Items.BEEF, Items.PORKCHOP, Items.COD, Items.SALMON);
        register("alexsmobs:caiman", FeedingType.THROWN_MEAT, "Karnivora Rawa (Ikan & Daging)", true, true, Items.COD, Items.SALMON, Items.CHICKEN, Items.BEEF);
        register("alexsmobs:alligator_snapping_turtle", FeedingType.THROWN_MEAT, "Karnivora (Ikan & Daging)", true, true, Items.COD, Items.SALMON, Items.BEEF);
        register("alexsmobs:grizzly_bear", FeedingType.THROWN_MEAT, "Omnivora Besar (Ikan, Madu & Daging)", true, true, Items.SALMON, Items.COD, Items.HONEY_BOTTLE, Items.SWEET_BERRIES, Items.BEEF);
        register("alexsmobs:komodo_dragon", FeedingType.THROWN_MEAT, "Karnivora Berbisa (Daging)", true, false, Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.ROTTEN_FLESH);
        register("alexsmobs:seagull", FeedingType.THROWN_MEAT, "Piscivora (Ikan)", false, true, Items.COD, Items.SALMON);
        register("alexsmobs:bald_eagle", FeedingType.THROWN_MEAT, "Pemangsa Udara (Ikan & Daging)", true, true, Items.SALMON, Items.COD, Items.RABBIT, Items.CHICKEN);
        register("alexsmobs:snake", FeedingType.THROWN_MEAT, "Karnivora (Ayam, Kelinci & Daging)", true, false, Items.CHICKEN, Items.RABBIT, Items.EGG);
        register("alexsmobs:rattlesnake", FeedingType.THROWN_MEAT, "Karnivora (Daging Segar)", true, false, Items.CHICKEN, Items.RABBIT);
        register("alexsmobs:anaconda", FeedingType.THROWN_MEAT, "Karnivora Besar (Daging & Ikan)", true, true, Items.CHICKEN, Items.BEEF, Items.PORKCHOP, Items.COD);
        register("alexsmobs:orca", FeedingType.THROWN_MEAT, "Predator Laut (Ikan & Daging)", true, true, Items.SALMON, Items.COD, Items.BEEF);
        register("alexsmobs:seal", FeedingType.THROWN_MEAT, "Piscivora (Ikan Segar)", false, true, Items.COD, Items.SALMON);

        // ==========================================
        // 3. NATURALIST
        // ==========================================
        register("naturalist:elephant", FeedingType.TROUGH, "Herbivora Raksasa (Melon, Jerami & Sayur)", Items.MELON_SLICE, Items.HAY_BLOCK, Items.APPLE, Items.CARROT, Items.WHEAT);
        register("naturalist:giraffe", FeedingType.TROUGH, "Folivora (Daun, Jerami, Apel & Sayur)", Items.ACACIA_LEAVES, Items.OAK_LEAVES, Items.APPLE, Items.CARROT, Items.HAY_BLOCK, Items.WHEAT);
        register("naturalist:zebra", FeedingType.TROUGH, "Herbivora (Gandum, Jerami & Wortel)", Items.WHEAT, Items.HAY_BLOCK, Items.CARROT, Items.APPLE);
        register("naturalist:deer", FeedingType.TROUGH, "Herbivora (Gandum, Jerami, Apel & Beri)", Items.WHEAT, Items.HAY_BLOCK, Items.APPLE, Items.CARROT, Items.SWEET_BERRIES);
        register("naturalist:rhino", FeedingType.TROUGH, "Herbivora (Gandum, Melon & Jerami)", Items.WHEAT, Items.MELON_SLICE, Items.HAY_BLOCK, Items.CARROT);
        register("naturalist:hippo", FeedingType.TROUGH, "Herbivora Air (Melon, Labu & Gandum)", Items.MELON_SLICE, Items.PUMPKIN, Items.WHEAT, Items.APPLE);
        register("naturalist:duck", FeedingType.TROUGH, "Granivora (Biji-bijian & Roti)", Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.BREAD, Items.WHEAT);
        register("naturalist:canary", FeedingType.TROUGH, "Granivora (Biji-bijian & Beri)", Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.SWEET_BERRIES);
        register("naturalist:cardinal", FeedingType.TROUGH, "Granivora (Biji-bijian & Beri)", Items.WHEAT_SEEDS, Items.SWEET_BERRIES);
        register("naturalist:bluejay", FeedingType.TROUGH, "Granivora (Biji-bijian & Beri)", Items.WHEAT_SEEDS, Items.SWEET_BERRIES);
        register("naturalist:finch", FeedingType.TROUGH, "Granivora (Biji-bijian)", Items.WHEAT_SEEDS, Items.MELON_SEEDS);
        register("naturalist:sparrow", FeedingType.TROUGH, "Granivora (Biji-bijian)", Items.WHEAT_SEEDS, Items.MELON_SEEDS);
        register("naturalist:tortoise", FeedingType.TROUGH, "Herbivora (Sayuran, Wortel, Apel & Bunga)", Items.CARROT, Items.DANDELION, Items.MELON_SLICE, Items.APPLE, Items.SWEET_BERRIES, Items.BEETROOT, Items.WHEAT);
        register("naturalist:butterfly", FeedingType.TROUGH, "Nektar (Bunga)", Items.DANDELION, Items.POPPY, Items.BLUE_ORCHID);
        register("naturalist:snail", FeedingType.TROUGH, "Herbivora Kecil (Jamur & Sayur)", Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.CARROT);

        // Carnivores / Predators / Omnivores (Thrown Meat & Fish)
        register("naturalist:lion", FeedingType.THROWN_MEAT, "Predator Savana (Daging Segar)", true, false, Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.CHICKEN, Items.RABBIT);
        register("naturalist:tiger", FeedingType.THROWN_MEAT, "Predator Jungle (Daging Segar)", true, false, Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.CHICKEN, Items.RABBIT);
        register("naturalist:bear", FeedingType.THROWN_MEAT, "Omnivora Besar (Ikan, Madu, Beri & Daging)", true, true, Items.SALMON, Items.COD, Items.HONEY_BOTTLE, Items.SWEET_BERRIES, Items.BEEF, Items.PORKCHOP);
        register("naturalist:wolf", FeedingType.THROWN_MEAT, "Karnivora (Daging Segar)", true, false, Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT);
        register("naturalist:alligator", FeedingType.THROWN_MEAT, "Karnivora Rawa (Daging & Ikan)", true, true, Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.SALMON, Items.COD);
        register("naturalist:crocodile", FeedingType.THROWN_MEAT, "Karnivora Sungai (Daging & Ikan)", true, true, Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.SALMON, Items.COD);
        register("naturalist:leopard", FeedingType.THROWN_MEAT, "Karnivora Cepat (Daging Segar)", true, false, Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT);
        register("naturalist:snake", FeedingType.THROWN_MEAT, "Karnivora (Ayam, Kelinci & Daging Kecil)", true, false, Items.CHICKEN, Items.RABBIT, Items.EGG);
        register("naturalist:coral_snake", FeedingType.THROWN_MEAT, "Karnivora (Ayam, Kelinci & Daging Kecil)", true, false, Items.CHICKEN, Items.RABBIT);
        register("naturalist:rattlesnake", FeedingType.THROWN_MEAT, "Karnivora (Ayam, Kelinci & Daging Kecil)", true, false, Items.CHICKEN, Items.RABBIT);
        register("naturalist:boar", FeedingType.TROUGH, "Omnivora Liar (Sayuran, Umbi & Daging)", true, false, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.APPLE, Items.BEEF);
        register("naturalist:hyena", FeedingType.THROWN_MEAT, "Karnivora (Daging Segar & Bangkai)", true, false, Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.CHICKEN, Items.ROTTEN_FLESH);
        register("naturalist:vulture", FeedingType.THROWN_MEAT, "Pemakan Bangkai & Daging", true, false, Items.ROTTEN_FLESH, Items.BEEF, Items.CHICKEN, Items.PORKCHOP);
        register("naturalist:lizard", FeedingType.TROUGH, "Insektivora & Buah Kecil", Items.SPIDER_EYE, Items.SWEET_BERRIES, Items.MELON_SLICE);
        register("naturalist:catfish", FeedingType.THROWN_MEAT, "Pemakan Ikan Kecil & Serangga", false, true, Items.COD, Items.TROPICAL_FISH, Items.SEAGRASS);
        register("naturalist:bass", FeedingType.THROWN_MEAT, "Pemakan Ikan Kecil", false, true, Items.COD, Items.SEAGRASS);
        // ==========================================
        // 4. WILDER NATURE & ADDITIONAL ALEX'S MOBS / NATURALIST
        // ==========================================
        register("wildernature:cassowary", FeedingType.TROUGH, "Omnivora", "Omnivora (Melon, Apel, Biji & Serangga)", Items.MELON_SLICE, Items.APPLE, Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.WHEAT, Items.SPIDER_EYE);
        register("wildernature:mini_sheep", FeedingType.TROUGH, "Herbivora", "Herbivora (Gandum & Rumput Segar)", Items.WHEAT, Items.HAY_BLOCK);
        register("naturalist:ostrich", FeedingType.TROUGH, "Granivora", "Granivora (Biji-bijian, Rumput, Apel & Wortel)", Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.CARROT, Items.WHEAT, Items.HAY_BLOCK, Items.APPLE);
        register("naturalist:capybara", FeedingType.TROUGH, "Herbivora", "Herbivora (Apel, Wortel, Rumput & Melon)", Items.APPLE, Items.CARROT, Items.MELON_SLICE, Items.WHEAT);
        register("alexsmobs:giant_anteater", FeedingType.TROUGH, "Insektivora", "Insektivora (Serangga & Telur)", Items.SPIDER_EYE, Items.SLIME_BALL, Items.EGG);
        register("alexsmobs:anteater", FeedingType.TROUGH, "Insektivora", "Insektivora (Serangga & Telur)", Items.SPIDER_EYE, Items.SLIME_BALL, Items.EGG);
        register("naturalist:komodo_dragon", FeedingType.THROWN_MEAT, "Karnivora", "Karnivora (Daging Segar & Bangkai)", true, false, Items.BEEF, Items.MUTTON, Items.ROTTEN_FLESH, Items.PORKCHOP, Items.CHICKEN);
        register("alexsmobs:cachalot_whale", FeedingType.THROWN_MEAT, "Karnivora", "Karnivora Laut (Kantong Tinta, Ikan & Daging)", true, true, Items.INK_SAC, Items.GLOW_INK_SAC, Items.COD, Items.SALMON, Items.BEEF);
        register("naturalist:great_white_shark", FeedingType.THROWN_MEAT, "Karnivora", "Karnivora Laut (Daging & Ikan)", true, true, Items.BEEF, Items.COD, Items.SALMON);
        register("alexsmobs:great_white_shark", FeedingType.THROWN_MEAT, "Karnivora", "Karnivora Laut (Daging & Ikan)", true, true, Items.BEEF, Items.COD, Items.SALMON);
        register("naturalist:shark", FeedingType.THROWN_MEAT, "Karnivora", "Karnivora Laut (Daging & Ikan)", true, true, Items.BEEF, Items.COD, Items.SALMON);
        register("naturalist:penguin", FeedingType.TROUGH, "Piscivora", "Piscivora (Ikan Segar)", false, true, Items.COD, Items.SALMON);
        register("ecologics:penguin", FeedingType.TROUGH, "Piscivora", "Piscivora (Ikan Segar)", false, true, Items.COD, Items.SALMON);
        register("alexsmobs:giant_squid", FeedingType.THROWN_MEAT, "Karnivora", "Karnivora Laut (Ikan & Daging)", true, true, Items.COD, Items.SALMON, Items.BEEF);
        register("alexsmobs:mantis_shrimp", FeedingType.TROUGH, "Karnivora", "Karnivora Laut (Krustasea & Ikan Kecil)", false, true, Items.COD, Items.SALMON, Items.TROPICAL_FISH);
        register("naturalist:mammoth", FeedingType.TROUGH, "Herbivora", "Herbivora Purba (Jerami, Gandum & Daun Spruce)", Items.HAY_BLOCK, Items.WHEAT, Items.SPRUCE_LEAVES, Items.APPLE);
        register("alexsmobs:mammoth", FeedingType.TROUGH, "Herbivora", "Herbivora Purba (Jerami, Gandum & Daun Spruce)", Items.HAY_BLOCK, Items.WHEAT, Items.SPRUCE_LEAVES, Items.APPLE);
        register("alexsmobs:froststalker", FeedingType.THROWN_MEAT, "Karnivora", "Karnivora Kutub (Daging Segar)", true, false, Items.MUTTON, Items.BEEF, Items.PORKCHOP);
        register("alexsmobs:guster", FeedingType.TROUGH, "Pemakan Mineral", "Pemakan Mineral (Pasir & Tulang)", Items.SAND, Items.BONE);
        register("alexsmobs:bunfungus", FeedingType.TROUGH, "Pemakan Jamur", "Pemakan Jamur (Jamur Merah & Cokelat)", Items.RED_MUSHROOM, Items.BROWN_MUSHROOM);
        register("alexsmobs:mungus", FeedingType.TROUGH, "Pemakan Jamur", "Pemakan Jamur (Jamur Cokelat, Crimson & Warped)", Items.BROWN_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS);
        register("alexsmobs:rocky_roller", FeedingType.TROUGH, "Pemakan Mineral", "Pemakan Mineral (Kerikil, Batu Bara & Rumput Kering)", Items.GRAVEL, Items.COAL, Items.WHEAT);
        register("alexsmobs:flutter", FeedingType.TROUGH, "Nektarivora", "Nektarivora (Daun Ceri, Spora & Azalea)", Items.CHERRY_LEAVES, Items.SPORE_BLOSSOM, Items.AZALEA, Items.FLOWERING_AZALEA);
    }

    public static void register(String entityTypeId, FeedingType feedingType, String description, Item... foods) {
        DIET_REGISTRY.put(entityTypeId.toLowerCase(Locale.ROOT), new AnimalDiet(feedingType, description, foods));
    }

    public static void register(String entityTypeId, FeedingType feedingType, String description, boolean acceptsRawMeat, boolean acceptsRawFish, Item... foods) {
        DIET_REGISTRY.put(entityTypeId.toLowerCase(Locale.ROOT), new AnimalDiet(feedingType, description, acceptsRawMeat, acceptsRawFish, false, foods));
    }

    public static void register(String entityTypeId, FeedingType feedingType, String dietCategory, String description, Item... foods) {
        DIET_REGISTRY.put(entityTypeId.toLowerCase(Locale.ROOT), new AnimalDiet(feedingType, dietCategory, description, autoDetectRawMeat(feedingType, foods), autoDetectRawFish(foods), false, foods));
    }

    public static void register(String entityTypeId, FeedingType feedingType, String dietCategory, String description, boolean acceptsRawMeat, boolean acceptsRawFish, Item... foods) {
        DIET_REGISTRY.put(entityTypeId.toLowerCase(Locale.ROOT), new AnimalDiet(feedingType, dietCategory, description, acceptsRawMeat, acceptsRawFish, false, foods));
    }

    public static final int COLOR_DIET_CATEGORY = 0xFFBD6DFF; // Soft Lavender Purple
    public static final int COLOR_FEEDING_METHOD = 0xFFAAAAAA; // Soft Gray

    public static Component getDietCategorySubtitleComponent(LivingEntity entity) {
        AnimalDiet diet = (entity != null) ? getDiet(entity) : null;
        String category = (diet != null && diet.dietCategory != null) ? diet.dietCategory : "Herbivora";
        String method = (diet != null && diet.feedingType != null) ? diet.feedingType.getLabel() : "Perlu Palung Makanan";

        return Component.literal(category)
                .withStyle(style -> style.withColor(COLOR_DIET_CATEGORY))
                .append(Component.literal(" (" + method + ")")
                        .withStyle(style -> style.withColor(COLOR_FEEDING_METHOD)));
    }

    public static String getDietCategorySubtitle(LivingEntity entity) {
        if (entity == null) return "Herbivora (Perlu Palung Makanan)";
        AnimalDiet diet = getDiet(entity);
        return diet != null ? diet.getSubtitle() : "Herbivora (Perlu Palung Makanan)";
    }

    public static String getDietCategorySubtitle(String entityTypeId) {
        if (entityTypeId == null || entityTypeId.isEmpty()) return "Herbivora (Perlu Palung Makanan)";
        AnimalDiet diet = getDiet(entityTypeId);
        return diet != null ? diet.getSubtitle() : "Herbivora (Perlu Palung Makanan)";
    }

    public static AnimalDiet getDiet(String entityTypeId) {
        if (entityTypeId == null || entityTypeId.isEmpty()) return getFallbackDiet(null);
        String key = entityTypeId.toLowerCase(Locale.ROOT);
        AnimalDiet diet = DIET_REGISTRY.get(key);
        if (diet != null) return diet;
        return getFallbackDiet(null);
    }

    public static AnimalDiet getDiet(Entity entity) {
        if (entity == null) return getFallbackDiet(null);
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String key = id != null ? id.toString().toLowerCase(Locale.ROOT) : "";

        AnimalDiet diet = DIET_REGISTRY.get(key);
        if (diet != null) return diet;

        return getFallbackDiet(entity);
    }

    public static final List<Item> COMMON_ANIMAL_FOOD_CANDIDATES = List.of(
            Items.WHEAT, Items.HAY_BLOCK, Items.CARROT, Items.GOLDEN_CARROT,
            Items.POTATO, Items.BEETROOT, Items.APPLE, Items.GOLDEN_APPLE,
            Items.MELON_SLICE, Items.PUMPKIN, Items.BREAD, Items.SUGAR,
            Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD,
            Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.BAMBOO, Items.CACTUS, Items.SEAGRASS,
            Items.ACACIA_LEAVES, Items.OAK_LEAVES, Items.DANDELION, Items.POPPY, Items.WARPED_FUNGUS,
            Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.HONEY_BOTTLE, Items.EGG, Items.SLIME_BALL, Items.SPIDER_EYE,
            Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT, Items.ROTTEN_FLESH,
            Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH
    );

    private static AnimalDiet getFallbackDiet(Entity entity) {
        if (entity instanceof Animal a) {
            List<Item> probed = new ArrayList<>();
            boolean wantsMeat = false;
            boolean wantsFish = false;
            for (Item candidate : COMMON_ANIMAL_FOOD_CANDIDATES) {
                try {
                    ItemStack st = new ItemStack(candidate);
                    if (a.isFood(st)) {
                        probed.add(candidate);
                        if (isRawMeat(st)) wantsMeat = true;
                        if (isRawFish(st)) wantsFish = true;
                    }
                } catch (Throwable ignored) {}
            }
            if (!probed.isEmpty()) {
                FeedingType fType = (wantsMeat || wantsFish) ? FeedingType.THROWN_MEAT : FeedingType.TROUGH;
                return new AnimalDiet(fType, "Pakan Alami", wantsMeat, wantsFish, true, probed.toArray(new Item[0]));
            }
            return new AnimalDiet(FeedingType.TROUGH, "Herbivora Umum (Gandum & Sayur)", false, false, true, Items.WHEAT, Items.CARROT, Items.APPLE);
        }
        return new AnimalDiet(FeedingType.THROWN_MEAT, "Karnivora Umum (Daging Segar)", true, false, true, Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.ROTTEN_FLESH);
    }

    public static boolean isFallbackDiet(Entity entity) {
        if (entity == null) return true;
        AnimalDiet diet = getDiet(entity);
        return diet == null || diet.isFallback;
    }

    public static FeedingType getFeedingType(Entity entity) {
        return getDiet(entity).feedingType;
    }

    /**
     * Returns all accepted Item instances using the single source of truth (getFoodsForAnimal).
     */
    public static List<Item> getAcceptedFoods(Entity entity) {
        if (entity instanceof LivingEntity living) {
            List<ItemStack> stacks = getFoodsForAnimal(living);
            List<Item> items = new ArrayList<>(stacks.size());
            for (ItemStack st : stacks) {
                if (!st.isEmpty()) items.add(st.getItem());
            }
            if (!items.isEmpty()) return Collections.unmodifiableList(items);
        }
        return getDiet(entity).acceptedFoods;
    }

    private static final Map<EntityType<?>, List<ItemStack>> FOOD_CACHE = new ConcurrentHashMap<>();

    public static void clearCache() {
        FOOD_CACHE.clear();
    }

    private static void addExplicitItemIds(List<String> ids, Set<Item> seen, List<ItemStack> list) {
        for (String idStr : ids) {
            try {
                ResourceLocation loc = ResourceLocation.parse(idStr.trim().toLowerCase(Locale.ROOT));
                if (BuiltInRegistries.ITEM.containsKey(loc)) {
                    Item item = BuiltInRegistries.ITEM.get(loc);
                    if (item != null && item != Items.AIR && seen.add(item)) {
                        list.add(new ItemStack(item));
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    private static boolean dietContainsAny(AnimalDiet diet, Item... targets) {
        if (diet == null || diet.acceptedFoods == null) return false;
        for (Item t : targets) {
            if (diet.acceptedFoods.contains(t)) return true;
        }
        return false;
    }

    /**
     * SINGLE SOURCE OF TRUTH for both In-World Hologram and Animal Management GUI.
     * Dynamically resolves all valid foods for an animal including Vanilla, Naturalist,
     * Farmer's Delight, More Delight, Farm & Charm, Alex's Mobs, and WilderNature.
     */
    public static List<ItemStack> getFoodsForAnimal(LivingEntity entity) {
        if (entity == null) return Collections.emptyList();
        return FOOD_CACHE.computeIfAbsent(entity.getType(), type -> buildFoodListForEntity(entity));
    }

    public static List<ItemStack> getFoodsForSpecies(String entityTypeId) {
        if (entityTypeId == null || entityTypeId.isEmpty()) return List.of(new ItemStack(Items.WHEAT));
        try {
            ResourceLocation id = ResourceLocation.parse(entityTypeId.toLowerCase(Locale.ROOT));
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
                List<ItemStack> cached = FOOD_CACHE.get(type);
                if (cached != null && !cached.isEmpty()) {
                    return cached;
                }
            }
        } catch (Throwable ignored) {}

        AnimalDiet diet = getDiet(entityTypeId);
        return buildFoodListFromDietOnly(diet);
    }

    private static List<ItemStack> buildFoodListForEntity(LivingEntity entity) {
        Set<Item> seen = new LinkedHashSet<>();
        List<ItemStack> list = new ArrayList<>();

        AnimalDiet diet = getDiet(entity);
        if (diet != null && diet.acceptedFoods != null) {
            for (Item item : diet.acceptedFoods) {
                if (item != null && item != Items.AIR && seen.add(item)) {
                    list.add(new ItemStack(item));
                }
            }
        }

        // Determine dietary profile traits so modded foods from Naturalist / Farmer's Delight / More Delight are included
        boolean eatsSeeds = dietContainsAny(diet, Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS);
        boolean eatsVegOrHay = dietContainsAny(diet, Items.WHEAT, Items.HAY_BLOCK, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.MELON_SLICE, Items.APPLE, Items.DANDELION);
        boolean eatsInsects = dietContainsAny(diet, Items.SPIDER_EYE, Items.SLIME_BALL);

        // 1. Probe Animal.isFood() on common vanilla candidates
        if (entity instanceof Animal a) {
            for (Item candidate : COMMON_ANIMAL_FOOD_CANDIDATES) {
                if (candidate != null && !seen.contains(candidate)) {
                    try {
                        ItemStack test = new ItemStack(candidate);
                        if (a.isFood(test) && seen.add(candidate)) {
                            list.add(test);
                            if (isSeedOrGrain(test)) eatsSeeds = true;
                            if (isModProduceOrCrop(test) || candidate == Items.WHEAT || candidate == Items.CARROT) eatsVegOrHay = true;
                        }
                    } catch (Throwable ignored) {}
                }
            }

            // 2. Probe all registered items for Animal.isFood()
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == null || item == Items.AIR || seen.contains(item)) continue;
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                if (itemId != null && "croptopia".equalsIgnoreCase(itemId.getNamespace())) continue;
                try {
                    ItemStack testStack = new ItemStack(item);
                    if (a.isFood(testStack) && seen.add(item)) {
                        list.add(testStack);
                    }
                } catch (Throwable ignored) {}
            }
        }

        // 3. Include explicit + registry-scanned Raw Meat items (Naturalist, Farmer's Delight, More Delight, etc.)
        if (diet != null && diet.acceptsRawMeat) {
            addExplicitItemIds(EXPLICIT_RAW_MEAT_IDS, seen, list);
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == null || item == Items.AIR || seen.contains(item)) continue;
                ItemStack st = new ItemStack(item);
                if (isRawMeat(st) && seen.add(item)) {
                    list.add(st);
                }
            }
        }

        // 4. Include explicit + registry-scanned Raw Fish items (Naturalist, Farmer's Delight, More Delight, etc.)
        if (diet != null && diet.acceptsRawFish) {
            addExplicitItemIds(EXPLICIT_RAW_FISH_IDS, seen, list);
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == null || item == Items.AIR || seen.contains(item)) continue;
                ItemStack st = new ItemStack(item);
                if (isRawFish(st) && seen.add(item)) {
                    list.add(st);
                }
            }
        }

        // 5. Include Modded Vegetables / Crops / Fruits for Herbivores & Omnivores (Farmer's Delight, More Delight, Naturalist)
        if (eatsVegOrHay) {
            addExplicitItemIds(EXPLICIT_HERBIVORE_CROP_IDS, seen, list);
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == null || item == Items.AIR || seen.contains(item)) continue;
                ItemStack st = new ItemStack(item);
                if (isModProduceOrCrop(st) && seen.add(item)) {
                    list.add(st);
                }
            }
        }

        // 6. Include Modded Seeds & Grains for Granivores (Birds, Chickens, Ducks, etc.)
        if (eatsSeeds) {
            addExplicitItemIds(EXPLICIT_SEED_IDS, seen, list);
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == null || item == Items.AIR || seen.contains(item)) continue;
                ItemStack st = new ItemStack(item);
                if (isSeedOrGrain(st) && seen.add(item)) {
                    list.add(st);
                }
            }
        }

        // 7. Include Insects / Larvae for Insectivores (Frogs, Lizards, Armadillos, Roadrunners)
        if (eatsInsects) {
            addExplicitItemIds(EXPLICIT_INSECT_IDS, seen, list);
        }

        if (list.isEmpty()) {
            list.add(new ItemStack(Items.WHEAT));
        }

        return Collections.unmodifiableList(list);
    }

    private static List<ItemStack> buildFoodListFromDietOnly(AnimalDiet diet) {
        Set<Item> seen = new LinkedHashSet<>();
        List<ItemStack> list = new ArrayList<>();
        if (diet != null) {
            for (Item item : diet.acceptedFoods) {
                if (item != null && item != Items.AIR && seen.add(item)) {
                    list.add(new ItemStack(item));
                }
            }
            if (diet.acceptsRawMeat) {
                addExplicitItemIds(EXPLICIT_RAW_MEAT_IDS, seen, list);
            }
            if (diet.acceptsRawFish) {
                addExplicitItemIds(EXPLICIT_RAW_FISH_IDS, seen, list);
            }
            if (dietContainsAny(diet, Items.WHEAT, Items.HAY_BLOCK, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.MELON_SLICE, Items.APPLE)) {
                addExplicitItemIds(EXPLICIT_HERBIVORE_CROP_IDS, seen, list);
            }
            if (dietContainsAny(diet, Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS)) {
                addExplicitItemIds(EXPLICIT_SEED_IDS, seen, list);
            }
        }
        if (list.isEmpty()) {
            list.add(new ItemStack(Items.WHEAT));
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Client-safe method to retrieve a list of favorite food ItemStacks for an entity.
     */
    public static List<ItemStack> getFavoriteFoodStacks(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return getFoodsForAnimal(living);
        }
        return Collections.emptyList();
    }

    public static boolean isFoodValid(Entity entity, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId != null && "croptopia".equalsIgnoreCase(itemId.getNamespace())) return false;
        if (entity instanceof Animal a) {
            try {
                if (a.isFood(stack)) return true;
            } catch (Throwable ignored) {}
        }
        if (entity instanceof LivingEntity living) {
            for (ItemStack valid : getFoodsForAnimal(living)) {
                if (valid.is(stack.getItem())) {
                    return true;
                }
            }
        }
        AnimalDiet diet = getDiet(entity);
        if (diet == null) return false;
        if (diet.acceptsRawMeat && isRawMeat(stack)) return true;
        if (diet.acceptsRawFish && isRawFish(stack)) return true;
        Item item = stack.getItem();
        return diet.acceptedFoods != null && diet.acceptedFoods.contains(item);
    }

    public static boolean isAnyFood(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.has(DataComponents.FOOD)) return true;
        if (isRawMeat(stack) || isRawFish(stack) || isModProduceOrCrop(stack) || isSeedOrGrain(stack)) return true;
        Item item = stack.getItem();
        for (AnimalDiet d : DIET_REGISTRY.values()) {
            if (d.acceptedFoods.contains(item)) return true;
        }
        return isAnyTroughFood(stack);
    }

    public static boolean isAnyTroughFood(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isModProduceOrCrop(stack) || isSeedOrGrain(stack)) return true;
        Item item = stack.getItem();
        for (AnimalDiet d : DIET_REGISTRY.values()) {
            if (d.feedingType == FeedingType.TROUGH && d.acceptedFoods.contains(item)) {
                return true;
            }
        }
        return stack.is(Items.WHEAT) || stack.is(Items.CARROT) || stack.is(Items.POTATO)
                || stack.is(Items.BEETROOT) || stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES)
                || stack.is(Items.HAY_BLOCK) || stack.is(Items.BREAD);
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
