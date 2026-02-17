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
        register(IndoZooTycoon.MODID + ":zoo_banner", "Banner Kebun Binatang", 0, Category.UTILITY);
        register(IndoZooTycoon.MODID + ":animal_tag", "Animal Tag", 5_000, Category.UTILITY);
        register("minecraft:scaffolding", "Scaffolding", 15_000, Category.UTILITY);
        register("minecraft:lead", "Lead", 25_000, Category.UTILITY);
        register("minecraft:bone_meal", "Bone Meal", 5_000, Category.UTILITY);
        register("minecraft:white_dye", "Dye", 5_000, Category.UTILITY);
        register("minecraft:ladder", "Ladder", 15_000, Category.UTILITY);

        // 2. FUNCTIONAL BLOCKS -> 50k-200k (with FUNCTIONAL subcategory)
        register("minecraft:lantern", "Lantern", 50_000, Category.UTILITY, "FUNCTIONAL");
        register("minecraft:flower_pot", "Pot", 15_000, Category.UTILITY, "FUNCTIONAL");
        register("minecraft:composter", "Composter", 35_000, Category.UTILITY, "FUNCTIONAL");
        register("minecraft:campfire", "Campfire", 25_000, Category.UTILITY, "FUNCTIONAL");
        if (IndoZooTycoon.CAPTURE_CAGE_ITEM.isPresent()) {
            register(IndoZooTycoon.CAPTURE_CAGE_ITEM.getId().toString(), "Mob Cage", 2_000_000, Category.UTILITY, "FUNCTIONAL");
        }

        // 3. BLOCKS -> 10k-50k per block
        registerWoodVariants();
        registerStoneVariants();
        registerGlassVariants();

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

        // Custom Biome Changer
        register("indozoo:biome_changer", "Biome Changer", 15_000_000, Category.NATURAL);

        // 5. FOOD -> 5k-50k
        register("minecraft:wheat_seeds", "Seeds", 2_000, Category.FOOD);
        register("minecraft:carrot", "Carrot", 5_000, Category.FOOD);
        register("minecraft:wheat", "Wheat", 5_000, Category.FOOD);
        register("minecraft:apple", "Apple", 5_000, Category.FOOD);
        register("minecraft:golden_carrot", "Golden Carrot", 150_000, Category.FOOD);
        register("minecraft:sweet_berries", "Sweet Berries", 5_000, Category.FOOD);
        register("minecraft:bamboo", "Bamboo", 15_000, Category.FOOD);
        register("minecraft:seagrass", "Seagrass", 5_000, Category.FOOD);
        register("minecraft:cod", "Raw Cod", 20_000, Category.FOOD);
        register("minecraft:salmon", "Raw Salmon", 20_000, Category.FOOD);
        
        // Bug Spawn Eggs (Environmental Mobs) -> 50k-200k
        register("alexsmobs:cockroach_spawn_egg", "Cockroach Spawn Egg", 50_000, Category.FOOD);
        register("alexsmobs:flutter_spawn_egg", "Butterfly Spawn Egg", 100_000, Category.FOOD);
        register("alexsmobs:caterpillar_spawn_egg", "Caterpillar Spawn Egg", 75_000, Category.FOOD);
        register("minecraft:bee_spawn_egg", "Bee Spawn Egg", 150_000, Category.FOOD);

        // 6. VEHICLES -> 10M+
        register("minecraft:minecart", "Minecart", 10_000_000, Category.VEHICLE);
        register("minecraft:oak_boat", "Boat", 5_000_000, Category.VEHICLE); // Wooden boat cheaper

        System.out.println("[IndoZoo] Registered " + ITEM_CATALOG.size() + " factory items.");
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
                int price = 2_000;
                if (t.equals("log"))
                    price = 5_000;
                else if (t.equals("fence"))
                    price = 3_000;

                register("minecraft:" + name, formatName(name), price, Category.BUILDING);
            }
        }
        // Bamboo special
        register("minecraft:bamboo_planks", "Bamboo Planks", 2_000, Category.BUILDING);
        register("minecraft:bamboo_fence", "Bamboo Fence", 3_000, Category.BUILDING);
        register("minecraft:bamboo_fence_gate", "Bamboo Fence Gate", 4_000, Category.BUILDING);
        register("minecraft:bamboo_stairs", "Bamboo Stairs", 2_000, Category.BUILDING);
        register("minecraft:bamboo_slab", "Bamboo Slab", 1_000, Category.BUILDING);
        register("minecraft:bamboo_block", "Bamboo Block", 5_000, Category.BUILDING);
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

            register("minecraft:" + base, formatName(base), 5_000, Category.BUILDING);
            register("minecraft:" + base + "_stairs", formatName(base + " Stairs"), 5_000, Category.BUILDING);
            register("minecraft:" + base + "_slab", formatName(base + " Slab"), 2_500, Category.BUILDING);
            register("minecraft:" + base + "_wall", formatName(base + " Wall"), 4_000, Category.BUILDING);
        }
    }

    private static void registerGlassVariants() {
        register("minecraft:glass", "Glass", 2_500, Category.BUILDING);
        register("minecraft:glass_pane", "Glass Pane", 1_000, Category.BUILDING);
    }

    private static void register(String idStr, String name, int price, Category cat) {
        register(idStr, name, price, cat, null);
    }
    
    private static void register(String idStr, String name, int price, Category cat, String subCategory) {
        ResourceLocation id = new ResourceLocation(idStr);
        if (ForgeRegistries.ITEMS.containsKey(id)) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            ITEM_CATALOG.put(id, new ItemData(item, name, price, cat, subCategory));
        }
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
