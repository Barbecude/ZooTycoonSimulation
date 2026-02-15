package com.example.simulation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
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
        ANIMAL("Hewan"),
        MYTHICAL("Hewan Langka");

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
        registerItem("minecraft:wheat", "Wheat", 15, Category.FOOD);
        registerItem("minecraft:beef", "Beef", 20, Category.FOOD);
        registerItem("minecraft:carrot", "Golden Carrot", 5000, Category.FOOD);
        registerItem("minecraft:hay_block", "Hay Bale", 40, Category.FOOD);

        // --- DYNAMIC SCANNING ---
        for (Map.Entry<net.minecraft.resources.ResourceKey<Item>, Item> entry : ForgeRegistries.ITEMS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            Item item = entry.getValue();
            String path = id.getPath();
            String namespace = id.getNamespace();

            // 1. SPAWN EGGS (Animals)
            if (item instanceof SpawnEggItem egg) {
                EntityType<?> type = egg.getType(null);
                if (type.getCategory() == net.minecraft.world.entity.MobCategory.CREATURE ||
                        type.getCategory() == net.minecraft.world.entity.MobCategory.AMBIENT ||
                        type.getCategory() == net.minecraft.world.entity.MobCategory.WATER_CREATURE) {

                    // Check for Mythical (Alex's Mobs special handling)
                    Category cat = Category.ANIMAL;
                    if (namespace.equals("alexsmobs")) {
                        if (path.contains("mimicube") || path.contains("void_worm"))
                            continue; // Exclude boss
                        if (path.contains("dragon") || path.contains("serpent") || path.contains("guster")) {
                            cat = Category.MYTHICAL;
                        }
                    }

                    int price = (cat == Category.MYTHICAL) ? 5000 : 500;
                    ITEM_CATALOG.put(id, new ItemData(item, formatName(path.replace("_spawn_egg", "")), price, cat));
                }
                continue; // Done with this item
            }

            // 2. ZAWA Capture Cage & Others
            if (namespace.equals("zawa")) {
                if (path.equals("capture_cage") || path.equals("catching_net")) { // Use Zawa's cage
                    ITEM_CATALOG.put(id, new ItemData(item, "Capture Cage (Universal)", 1000, Category.UTILITY));
                } else if (path.equals("kibble") || path.contains("food")) {
                    ITEM_CATALOG.put(id, new ItemData(item, formatName(path), 50, Category.FOOD));
                }
            }

            // 3. VEHICLES (Simple Keyword Search)
            if (path.contains("cart") || path.contains("vehicle") || path.contains("jeep") || path.contains("atv")
                    || path.contains("scooter")) {
                ITEM_CATALOG.put(id, new ItemData(item, formatName(path), 3000, Category.VEHICLE));
            }

            // 4. FOOD BLOCKS/ITEMS
            if (path.contains("kibble") || path.contains("hay") || path.contains("carrot")) {
                if (!ITEM_CATALOG.containsKey(id)) {
                    ITEM_CATALOG.put(id, new ItemData(item, formatName(path), 20, Category.FOOD));
                }
            }
        }

        // Ensure OUR items are there
        if (IndoZooTycoon.CAPTURE_CAGE_ITEM.isPresent()) {
            // Fallback if ZAWA not present or user wants ours
            ITEM_CATALOG.put(IndoZooTycoon.CAPTURE_CAGE_ITEM.getId(), new ItemData(
                    IndoZooTycoon.CAPTURE_CAGE_ITEM.get(), "Capture Cage (IndoZoo)", 1000, Category.UTILITY));
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

    private static void registerItem(String idStr, String name, int price, Category cat) {
        ResourceLocation id = ResourceLocation.tryParse(idStr);
        if (id != null && ForgeRegistries.ITEMS.containsKey(id)) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null) {
                ITEM_CATALOG.put(id, new ItemData(item, name, price, cat));
            }
        }
    }

    // ... helper methods
    public static Map<ResourceLocation, ItemData> getAllItems() {
        return Collections.unmodifiableMap(ITEM_CATALOG);
    }

    public static ItemData getItem(String idStr) {
        ResourceLocation id = ResourceLocation.tryParse(idStr.contains(":") ? idStr : "minecraft:" + idStr);
        return ITEM_CATALOG.get(id);
    }
}
