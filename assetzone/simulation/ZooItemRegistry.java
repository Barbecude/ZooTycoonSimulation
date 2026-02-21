package com.example.simulation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ZooItemRegistry {
    private static final Map<ResourceLocation, ItemData> ITEM_CATALOG = new LinkedHashMap<>();

    public enum Category {
        UTILITY("Utilitas"),
        BUILDING("Bangunan"),
        NATURAL("Alam"),
        FOOD("Makanan"),
        VEHICLE("Kendaraan");

        public final String label;

        Category(String label) {
            this.label = label;
        }
    }

    public static class ItemData {
        public final Item item;
        public final String displayName;
        public final int price;
        public final Category category;
        public final String subCategory; // For filtering: BLOCKS, FUNCTIONAL, NATURAL, UTILITY

        public ItemData(Item item, String name, int price, Category category) {
            this(item, name, price, category, null);
        }
        
        public ItemData(Item item, String name, int price, Category category, String subCategory) {
            this.item = item;
            this.displayName = name;
            this.price = price;
            this.category = category;
            this.subCategory = subCategory != null ? subCategory : "BLOCKS"; // Default to BLOCKS if not specified
        }
    }

    public static void initialize() {
        ITEM_CATALOG.clear();

        // 1. ITEMS (Utility) -> 5k-50k
        register("minecraft:scaffolding", "Scaffolding", 15_000, Category.UTILITY);
        register("minecraft:lead", "Lead", 25_000, Category.UTILITY);
        register("minecraft:bone_meal", "Bone Meal", 5_000, Category.UTILITY);
        register("minecraft:ladder", "Ladder", 15_000, Category.UTILITY);
        registerDyes();

        // 2. FUNCTIONAL BLOCKS -> 50k-200k (with FUNCTIONAL subcategory)
        register("minecraft:lantern", "Lantern", 50_000, Category.UTILITY, "FUNCTIONAL");
        register("minecraft:flower_pot", "Pot", 15_000, Category.UTILITY, "FUNCTIONAL");
        register("minecraft:composter", "Composter", 35_000, Category.UTILITY, "FUNCTIONAL");
        register("minecraft:campfire", "Campfire", 25_000, Category.UTILITY, "FUNCTIONAL");
        register(IndoZooTycoon.MODID + ":oak_shelf", "Oak Shelf", 250_000, Category.BUILDING, "FUNCTIONAL");
        register(IndoZooTycoon.MODID + ":standing_oak_shelf", "Standing Oak Shelf", 275_000, Category.BUILDING, "FUNCTIONAL");
        register(IndoZooTycoon.MODID + ":tower_oak_shelf", "Tower Oak Shelf", 300_000, Category.BUILDING, "FUNCTIONAL");
        register(IndoZooTycoon.MODID + ":animal_feeder", "Animal Feeder", 225_000, Category.BUILDING, "FUNCTIONAL");

        // 3. BLOCKS -> 10k-50k per block
        registerWoodVariants();
        registerStoneVariants();
        registerGlassVariants();
        addMacawsMods();


        register("minecraft:minecart", "Minecart", 10_000_000, Category.VEHICLE);
        register("minecraft:oak_boat", "Boat", 5_000_000, Category.VEHICLE);

        // 4. NATURAL BLOCKS -> 10k-100k
        register("minecraft:podzol", "Podzol", 20_000, Category.NATURAL);
        register("minecraft:gravel", "Gravel", 10_000, Category.NATURAL);
        register("minecraft:sand", "Sand", 10_000, Category.NATURAL);
        register("minecraft:coarse_dirt", "Coarse Dirt", 10_000, Category.NATURAL);
        register("minecraft:fern", "Fern", 15_000, Category.NATURAL);
        register("minecraft:dead_bush", "Dead Bush", 5_000, Category.NATURAL);
        register("minecraft:lily_pad", "Lily Pad", 15_000, Category.NATURAL);
        register("minecraft:kelp", "Kelp", 10_000, Category.NATURAL);
        register("minecraft:moss_block", "Moss Block", 25_000, Category.NATURAL);
        register("minecraft:moss_carpet", "Moss Carpet", 15_000, Category.NATURAL);
        register("minecraft:spore_blossom", "Spore Blossom", 150_000, Category.NATURAL);
        register("minecraft:azalea", "Azalea", 50_000, Category.NATURAL);
        register("minecraft:flowering_azalea", "Flowering Azalea", 60_000, Category.NATURAL);
        register("minecraft:amethyst_block", "Amethyst Block", 500_000, Category.NATURAL);
        register("minecraft:budding_amethyst", "Budding Amethyst", 1_000_000, Category.NATURAL);
        register("minecraft:amethyst_cluster", "Amethyst Cluster", 250_000, Category.NATURAL);
        register("minecraft:oak_leaves", "Oak Leaves", 10_000, Category.NATURAL);
        register("minecraft:spruce_leaves", "Spruce Leaves", 10_000, Category.NATURAL);
        register("minecraft:birch_leaves", "Birch Leaves", 10_000, Category.NATURAL);
        register("minecraft:jungle_leaves", "Jungle Leaves", 10_000, Category.NATURAL);
        register("minecraft:acacia_leaves", "Acacia Leaves", 10_000, Category.NATURAL);
        register("minecraft:dark_oak_leaves", "Dark Oak Leaves", 10_000, Category.NATURAL);
        register("minecraft:mangrove_leaves", "Mangrove Leaves", 10_000, Category.NATURAL);
        register("minecraft:cherry_leaves", "Cherry Leaves", 10_000, Category.NATURAL);
        register("minecraft:azalea_leaves", "Azalea Leaves", 10_000, Category.NATURAL);
        register("minecraft:flowering_azalea_leaves", "Flowering Azalea Leaves", 10_000, Category.NATURAL);

        // 5. FOOD: mandatory zoo diet list from FoodAnimalRegistry
        registerMandatoryFoods();

        // 6. VEHICLES -> 10M+
        register("minecraft:minecart", "Minecart", 10_000_000, Category.VEHICLE);
        register("minecraft:oak_boat", "Boat", 5_000_000, Category.VEHICLE); // Wooden boat cheaper

        System.out.println("[IndoZoo] Registered " + ITEM_CATALOG.size() + " factory items.");
    }

    private static void registerMandatoryFoods() {
        // Price table keyed by food display name.
        Map<String, Integer> prices = new HashMap<>();
        prices.put("Banana", 50_000);
        prices.put("Melon Slice", 10_000);
        prices.put("Raw Salmon", 20_000);
        prices.put("Raw Cod", 20_000);
        prices.put("Honey Bottle", 30_000);
        prices.put("Raw Beef", 60_000);
        prices.put("Raw Porkchop", 45_000);
        prices.put("Wheat", 5_000);
        prices.put("Sweet Berries", 5_000);
        prices.put("Raw Chicken", 25_000);
        prices.put("Rotten Flesh", 3_000);
        prices.put("Bone Meal", 5_000);
        prices.put("Acacia Leaves", 10_000);
        prices.put("Seeds", 2_000);
        prices.put("Flower", 4_000);
        prices.put("Glow Berries", 8_000);

        for (FoodAnimalRegistry.FoodProfile profile : FoodAnimalRegistry.getAllFoods()) {
            if (profile == null || profile.food == null) continue;
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(profile.food);
            if (id == null) continue;
            int price = prices.getOrDefault(profile.displayName, 10_000);
            ITEM_CATALOG.put(id, new ItemData(profile.food, profile.displayName, price, Category.FOOD, "FOOD"));
        }
    }
    private static void addMacawsMods() {
        // Kata kunci umum item Macaw's
        String[] keywords = {
            "fence", "bridge", "roof", "door", "window", "furniture", 
            "path", "light", "trapdoor", "gutter"
        };
        
        // Namespace Macaw's (mcw_*)
        String[] modIds = {
            "mcw_fences", "mcw_bridges", "mcw_doors", "mcw_trapdoors", 
            "mcw_windows", "mcw_roofs", "mcw_furniture", "mcw_lights", "mcw_paths"
        };

        for (ResourceLocation loc : ForgeRegistries.ITEMS.getKeys()) {
            String ns = loc.getNamespace();
            String path = loc.getPath();
            
            // Cek apakah item berasal dari mod Macaw's
            boolean isMacaw = false;
            for (String id : modIds) {
                if (ns.equals(id)) {
                    isMacaw = true;
                    break;
                }
            }
            
            // Atau jika path mengandung keyword bangunan umum
            if (!isMacaw) {
                 for (String k : keywords) {
                     if (path.contains(k)) {
                         // Filter item yang tidak diinginkan (opsional)
                         isMacaw = true; 
                         break;
                     }
                 }
            }

            if (isMacaw) {
                // Tentukan kategori. Furniture masuk ITEM/UTILITY, sisanya BLOCK
                String subCat = "BLOCK";
                if (path.contains("furniture") || ns.contains("furniture")) subCat = "ITEM";
                
                // Hindari duplikat jika sudah diregister
                if (!ITEM_CATALOG.containsKey(loc)) {
                    int price = 8_000;
                    if (path.contains("log") || path.contains("beam") || path.contains("pillar")) price = 15_000;
                    if (path.contains("glass") || path.contains("window")) price = 12_000;
                    if (path.contains("fence_gate") || path.contains("gate")) price = 10_000;
                    if (path.contains("door") || path.contains("trapdoor")) price = 12_000;
                    if (path.contains("roof")) price = 9_000;
                    if (subCat.equals("ITEM")) price = 15_000;
                    register(loc.toString(), formatName(path), price, Category.BUILDING, subCat);
                }
            }
        }
    }

    private static void registerWoodVariants() {
        String[] woods = { "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "crimson",
                "warped" };
        String[] types = { "planks", "fence", "fence_gate", "stairs", "slab", "log" }; // removed wood/stem distinction
                                                                                       // for simplicity or add if
                                                                                       // needed

        for (String w : woods) {
            String logName = w + "_log";
            if (w.equals("crimson") || w.equals("warped"))
                logName = w + "_stem";

            for (String t : types) {
                String name = w + "_" + t;
                if (t.equals("log"))
                    name = logName;

                // Price variants
                int price = 5_000;
                if (t.equals("log"))
                    price = 15_000;
                else if (t.equals("slab"))
                    price = 3_000;
                else if (t.equals("stairs"))
                    price = 6_000;
                else if (t.equals("fence"))
                    price = 7_000;
                else if (t.equals("fence_gate"))
                    price = 8_000;

                register("minecraft:" + name, formatName(name), price, Category.BUILDING);
            }
        }
        // Bamboo special
        register("minecraft:bamboo_planks", "Bamboo Planks", 5_000, Category.BUILDING);
        register("minecraft:bamboo_fence", "Bamboo Fence", 7_000, Category.BUILDING);
        register("minecraft:bamboo_fence_gate", "Bamboo Fence Gate", 8_000, Category.BUILDING);
        register("minecraft:bamboo_stairs", "Bamboo Stairs", 6_000, Category.BUILDING);
        register("minecraft:bamboo_slab", "Bamboo Slab", 3_000, Category.BUILDING);
        register("minecraft:bamboo_block", "Bamboo Block", 15_000, Category.BUILDING);
    }

    private static void registerStoneVariants() {
        String[] stones = { "stone", "cobblestone", "stone_bricks", "mossy_cobblestone", "mossy_stone_bricks",
                "smooth_stone", "sandstone", "red_sandstone", "brick" };
        // Note: "brick" is item "brick" but block "bricks". "brick_stairs" etc.
        // Actually "bricks" block.

        for (String s : stones) {
            String base = s;
            if (s.equals("brick"))
                base = "bricks";

            register("minecraft:" + base, formatName(base), 10_000, Category.BUILDING);
            register("minecraft:" + base + "_stairs", formatName(base + " Stairs"), 12_000, Category.BUILDING);
            register("minecraft:" + base + "_slab", formatName(base + " Slab"), 6_000, Category.BUILDING);
            register("minecraft:" + base + "_wall", formatName(base + " Wall"), 9_000, Category.BUILDING);
        }
    }

    private static void registerGlassVariants() {
        register("minecraft:glass", "Glass", 12_000, Category.BUILDING);
        register("minecraft:glass_pane", "Glass Pane", 5_000, Category.BUILDING);
    }

    private static void registerDyes() {
        register("minecraft:white_dye", "White Dye", 5_000, Category.UTILITY);
        register("minecraft:orange_dye", "Orange Dye", 5_000, Category.UTILITY);
        register("minecraft:magenta_dye", "Magenta Dye", 5_000, Category.UTILITY);
        register("minecraft:light_blue_dye", "Light Blue Dye", 5_000, Category.UTILITY);
        register("minecraft:yellow_dye", "Yellow Dye", 5_000, Category.UTILITY);
        register("minecraft:lime_dye", "Lime Dye", 5_000, Category.UTILITY);
        register("minecraft:pink_dye", "Pink Dye", 5_000, Category.UTILITY);
        register("minecraft:gray_dye", "Gray Dye", 5_000, Category.UTILITY);
        register("minecraft:light_gray_dye", "Light Gray Dye", 5_000, Category.UTILITY);
        register("minecraft:cyan_dye", "Cyan Dye", 5_000, Category.UTILITY);
        register("minecraft:purple_dye", "Purple Dye", 5_000, Category.UTILITY);
        register("minecraft:blue_dye", "Blue Dye", 5_000, Category.UTILITY);
        register("minecraft:brown_dye", "Brown Dye", 5_000, Category.UTILITY);
        register("minecraft:green_dye", "Green Dye", 5_000, Category.UTILITY);
        register("minecraft:red_dye", "Red Dye", 5_000, Category.UTILITY);
        register("minecraft:black_dye", "Black Dye", 5_000, Category.UTILITY);
    }

    private static void register(String idStr, String name, int price, Category cat) {
        register(idStr, name, price, cat, null);
    }
    
    private static void register(String idStr, String name, int price, Category cat, String subCategory) {
        ResourceLocation id = new ResourceLocation(idStr);
        if (ForgeRegistries.ITEMS.containsKey(id)) {
            if (shouldSkipBuildingItem(id, cat)) {
                return;
            }
            Item item = ForgeRegistries.ITEMS.getValue(id);
            ITEM_CATALOG.put(id, new ItemData(item, name, price, cat, subCategory));
        }
    }

    private static boolean shouldSkipBuildingItem(ResourceLocation id, Category cat) {
        if (cat != Category.BUILDING) return false;
        String path = id.getPath();
        if (path.contains("shulker_box")) return true;
        if (path.endsWith("_bed") || path.equals("bed")) return true;
        if (path.contains("candle")) return true;
        if (path.endsWith("_banner") || path.equals("banner")) return true;
        String[] colors = {
                "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        };
        String[] suffixes = {
                "_wool", "_carpet", "_concrete", "_concrete_powder", "_terracotta",
                "_glazed_terracotta", "_stained_glass", "_stained_glass_pane", "_shulker_box"
        };
        for (String color : colors) {
            for (String suffix : suffixes) {
                if (path.equals(color + suffix)) return true;
            }
        }
        return false;
    }

    private static String formatName(String path) {
        String[] words = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty())
                continue;
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1)
                sb.append(w.substring(1));
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static Map<ResourceLocation, ItemData> getAllItems() {
        return Collections.unmodifiableMap(ITEM_CATALOG);
    }

    public static ItemData getItem(String idStr) {
        ResourceLocation id = ResourceLocation.tryParse(idStr.contains(":") ? idStr : "minecraft:" + idStr);
        return ITEM_CATALOG.get(id);
    }
}

