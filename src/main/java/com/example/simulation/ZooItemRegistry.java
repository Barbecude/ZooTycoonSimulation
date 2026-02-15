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
        DECOR("Dekorasi"),
        VEHICLE("Kendaraan"),
        FOOD("Makanan"),
        MISC("Lainnya");

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

        public ItemData(Item item, String name, int price, Category category) {
            this.item = item;
            this.displayName = name;
            this.price = price;
            this.category = category;
        }
    }

    public static void initialize() {
        // Register Basic Zoo Items
        registerItem("minecraft:oak_fence", "Oak Fence", 25, Category.BUILDING);
        registerItem("minecraft:iron_bars", "Iron Bars", 50, Category.BUILDING);
        registerItem("minecraft:glass", "Glass", 30, Category.BUILDING);
        registerItem("minecraft:torch", "Torch", 10, Category.UTILITY);

        // Food
        registerItem("minecraft:apple", "Apple", 10, Category.FOOD);
        registerItem("minecraft:wheat", "Wheat", 10, Category.FOOD);
        registerItem("minecraft:beef", "Beef", 20, Category.FOOD);

        // Auto-detect ZAWA Items
        if (ZAWAIntegration.isZAWALoaded()) {
            for (Map.Entry<net.minecraft.resources.ResourceKey<Item>, Item> entry : ForgeRegistries.ITEMS
                    .getEntries()) {
                ResourceLocation id = entry.getKey().location();

                // Only process ZAWA (or other mods if needed)
                if (id.getNamespace().equals("zawa")) {
                    if (ITEM_CATALOG.containsKey(id))
                        continue; // Skip if logical duplicate

                    String path = id.getPath();
                    if (path.contains("spawn_egg") || path.contains("admin"))
                        continue;

                    String name = formatName(path);
                    Category cat = determineCategory(path);
                    int price = estimatePrice(path, cat);

                    ITEM_CATALOG.put(id, new ItemData(entry.getValue(), name, price, cat));
                }
            }
        }

        System.out.println("[IndoZoo] Registered " + ITEM_CATALOG.size() + " shop items.");
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

    // Heuristic for Category
    private static Category determineCategory(String path) {
        if (path.contains("fence") || path.contains("wall") || path.contains("glass") || path.contains("pane")
                || path.contains("planks") || path.contains("concrete") || path.contains("wire"))
            return Category.BUILDING;

        if (path.contains("cart") || path.contains("vehicle") || path.contains("jeep") || path.contains("atv")
                || path.contains("car") || path.contains("rover") || path.contains("transport")
                || path.contains("scooter"))
            return Category.VEHICLE;

        if (path.contains("kibble") || path.contains("food") || path.contains("fruit") || path.contains("meat")
                || path.contains("fish") || path.contains("diet"))
            return Category.FOOD;

        if (path.contains("bench") || path.contains("trash") || path.contains("lamp") || path.contains("sign")
                || path.contains("painting") || path.contains("flower") || path.contains("plant")
                || path.contains("plush"))
            return Category.DECOR;

        if (path.contains("net") || path.contains("gun") || path.contains("rifle") || path.contains("tool")
                || path.contains("wand") || path.contains("bucket"))
            return Category.UTILITY;

        return Category.MISC;
    }

    // Heuristic for Price
    private static int estimatePrice(String path, Category cat) {
        // High priority override
        if (cat == Category.VEHICLE)
            return 5000;

        if (path.contains("net") || path.contains("gun") || path.contains("rifle"))
            return 1500;
        if (path.contains("bench"))
            return 150;
        if (path.contains("trash"))
            return 100;
        if (path.contains("lamp"))
            return 120;

        return switch (cat) {
            case BUILDING -> 25; // Cheap building blocks
            case FOOD -> 15; // Cheap food
            case DECOR -> 200; // Moderate decoration
            case UTILITY -> 300; // Tools
            case MISC -> 100;
            default -> 100;
        };
    }

    private static void registerItem(String idStr, String name, int price, Category cat) {
        ResourceLocation id = ResourceLocation.tryParse(idStr);
        if (id != null && ForgeRegistries.ITEMS.containsKey(id)) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null) {
                ITEM_CATALOG.put(id, new ItemData(item, name, price, cat));
            }
        }
    }

    public static ItemData getItem(String idStr) {
        ResourceLocation id;
        if (idStr.contains(":")) {
            id = ResourceLocation.tryParse(idStr);
        } else {
            id = ResourceLocation.tryParse("minecraft:" + idStr);
        }
        return ITEM_CATALOG.get(id);
    }

    public static Map<ResourceLocation, ItemData> getAllItems() {
        return Collections.unmodifiableMap(ITEM_CATALOG);
    }
}
