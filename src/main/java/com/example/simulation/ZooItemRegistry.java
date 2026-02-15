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
        // Logic to detect ZAWA items if loaded
        if (ZAWAIntegration.isZAWALoaded()) {
            // Food
            registerOptionalItem("zawa:zoo_kibble", "General Kibble", 100);
            registerOptionalItem("zawa:carnivore_kibble", "Carnivore Kibble", 150);
            registerOptionalItem("zawa:herbivore_kibble", "Herbivore Kibble", 120);
            registerOptionalItem("zawa:insectivore_kibble", "Insectivore Kibble", 130);
            registerOptionalItem("zawa:piscivore_kibble", "Piscivore Kibble", 140);
            registerOptionalItem("zawa:hay", "Hay Bundle", 80);

            // Tools & Equipment
            registerOptionalItem("zawa:animal_net", "Animal Net", 500);
            registerOptionalItem("zawa:tranquilizer_gun", "Tranq Gun", 2500);
            registerOptionalItem("zawa:tranquilizer_dart", "Tranq Dart", 100);
            registerOptionalItem("zawa:containment_crate", "Small Crate", 400);

            // Enrichment & Toys
            registerOptionalItem("zawa:enrichment_ball", "Enrichment Ball", 300);
            registerOptionalItem("zawa:scratching_post", "Scratching Post", 450);
            registerOptionalItem("zawa:tire_swing", "Tire Swing", 600);
            registerOptionalItem("zawa:heater", "Heater", 2500);

            // Fences & Building
            registerOptionalItem("zawa:exhibit_glass", "Exhibit Glass", 200);
            registerOptionalItem("zawa:steel_bars", "Steel Bars", 250);
            registerOptionalItem("zawa:wired_fence", "Wired Fence", 180);
            registerOptionalItem("zawa:electrified_fence", "Electric Fence", 500);
        }

        System.out.println("[IndoZoo] Registered " + ITEM_CATALOG.size() + " shop items.");
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
