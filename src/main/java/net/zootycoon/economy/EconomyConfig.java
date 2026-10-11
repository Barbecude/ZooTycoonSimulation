package net.zootycoon.economy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class EconomyConfig {
    private static int starterEmeralds = 32;
    private static double convenienceSurcharge = 1.25; // 25% extra for instant-buy
    private static int defaultBuilderHiringCost = 10;

    private static final Map<Item, Double> ITEM_PRICES = new HashMap<>();

    static {
        // Common construction blocks (20 blocks per emerald)
        registerPrice(Items.OAK_PLANKS, 0.05);
        registerPrice(Items.SPRUCE_PLANKS, 0.05);
        registerPrice(Items.BIRCH_PLANKS, 0.05);
        registerPrice(Items.COBBLESTONE, 0.05);
        registerPrice(Items.STONE, 0.05);
        registerPrice(Items.OAK_LOG, 0.1);
        registerPrice(Items.SPRUCE_LOG, 0.1);
        registerPrice(Items.GLASS, 0.1);
        registerPrice(Items.OAK_FENCE, 0.1);
        registerPrice(Items.IRON_BARS, 0.25);
        registerPrice(Items.STONE_BRICKS, 0.1);
        registerPrice(Items.BRICKS, 0.2);

        // Decorative & advanced
        registerPrice(Items.LANTERN, 0.5);
        registerPrice(Items.GLOWSTONE, 1.0);
        registerPrice(Items.SEA_LANTERN, 1.5);
        registerPrice(Items.QUARTZ_BLOCK, 1.0);
        registerPrice(Items.TINTED_GLASS, 2.0);
    }

    private static void registerPrice(Item item, double price) {
        ITEM_PRICES.put(item, price);
    }

    public static int getStarterEmeralds() {
        return starterEmeralds;
    }

    public static void setStarterEmeralds(int amount) {
        starterEmeralds = amount;
    }

    public static double getConvenienceSurcharge() {
        return convenienceSurcharge;
    }

    public static int getDefaultBuilderHiringCost() {
        return defaultBuilderHiringCost;
    }

    public static double getItemEmeraldPrice(Item item) {
        return ITEM_PRICES.getOrDefault(item, 0.1); // Fallback: 10 blocks per emerald
    }

    public static int calculateMissingCost(Map<Item, Integer> missingMaterials) {
        double rawCost = 0.0;
        for (var entry : missingMaterials.entrySet()) {
            double pricePerUnit = getItemEmeraldPrice(entry.getKey());
            rawCost += pricePerUnit * entry.getValue();
        }
        return (int) Math.ceil(rawCost * convenienceSurcharge);
    }

    public static int calculateBuilderWage(int totalBlocks) {
        if (totalBlocks <= 15) return 2;
        if (totalBlocks <= 30) return 4;
        if (totalBlocks <= 60) return 8;
        if (totalBlocks <= 120) return 18;
        if (totalBlocks <= 250) return 35;
        if (totalBlocks <= 500) return 75;
        return 75 + (int) Math.ceil((totalBlocks - 500) * 0.2);
    }
}

