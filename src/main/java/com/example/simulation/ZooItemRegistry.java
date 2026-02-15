package com.example.simulation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ZooItemRegistry {
    private static final Map<ResourceLocation, ItemData> ITEM_CATALOG = new LinkedHashMap<>();

    public static class ItemData {
        public final Item item;
        public final String displayName;
        public final int price;

        public ItemData(Item item, String name, int price) {
            this.item = item;
            this.displayName = name;
            this.price = price;
        }
    }

    public static void initialize() {
        // Register Basic Zoo Items
        registerItem("minecraft:oak_fence", "Oak Fence", 50);
        registerItem("minecraft:iron_bars", "Iron Bars", 150);
        registerItem("minecraft:glass", "Glass", 100);

        // Food
        registerItem("minecraft:apple", "Apple", 20);
        registerItem("minecraft:wheat", "Wheat", 15);
        registerItem("minecraft:beef", "Beef", 50);

        // Logic to detect ZAWA items if loaded
        if (ZAWAIntegration.isZAWALoaded()) {
            // Manual overrides first (specific prices)
            registerOptionalItem("zawa:zoo_kibble", "General Kibble", 100);
            registerOptionalItem("zawa:carnivore_kibble", "Carnivore Kibble", 150);
            // ... (keep manual ones if desired, or let auto handle them?)
            // Better to let auto handle rest, but keep manual for custom prices/names if
            // needed.
            // Actually, let's just use auto-registration for EVERYTHING not manually added.

            for (Map.Entry<net.minecraft.resources.ResourceKey<Item>, Item> entry : ForgeRegistries.ITEMS
                    .getEntries()) {
                ResourceLocation id = entry.getKey().location();
                if (id.getNamespace().equals("zawa")) {
                    if (ITEM_CATALOG.containsKey(id))
                        continue; // Skip if already registered manually

                    String path = id.getPath();
                    if (path.contains("spawn_egg"))
                        continue; // Skip spawn eggs (sold via Animal tab)
                    if (path.contains("admin"))
                        continue; // Skip admin items

                    String name = formatName(path);
                    int price = estimatePrice(path);

                    ITEM_CATALOG.put(id, new ItemData(entry.getValue(), name, price));
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

    private static int estimatePrice(String path) {
        if (path.contains("kibble") || path.contains("food") || path.contains("fruit") || path.contains("meat"))
            return 120;
        if (path.contains("fence") || path.contains("wall") || path.contains("bars"))
            return 200;
        if (path.contains("glass") || path.contains("pane"))
            return 150;
        if (path.contains("net") || path.contains("gun") || path.contains("rifle"))
            return 1500;
        if (path.contains("cart") || path.contains("vehicle") || path.contains("jeep") || path.contains("atv")
                || path.contains("car") || path.contains("rover") || path.contains("transport"))
            return 5000;
        if (path.contains("bench") || path.contains("trash") || path.contains("lamp"))
            return 500;
        if (path.contains("enrichment") || path.contains("toy") || path.contains("ball"))
            return 350;
        if (path.contains("plush"))
            return 50;
        return 500; // Default fallback
    }

    private static void registerItem(String idStr, String name, int price) {
        ResourceLocation id = ResourceLocation.tryParse(idStr);
        if (id != null && ForgeRegistries.ITEMS.containsKey(id)) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null) {
                ITEM_CATALOG.put(id, new ItemData(item, name, price));
            }
        }
    }

    private static void registerOptionalItem(String idStr, String name, int price) {
        registerItem(idStr, name, price);
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
