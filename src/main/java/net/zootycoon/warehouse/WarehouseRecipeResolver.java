package net.zootycoon.warehouse;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.zootycoon.entity.ZooCashierEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class WarehouseRecipeResolver {

    private static List<Item> cachedBurgerItems = null;
    private static List<Item> cachedDrinkItems = null;

    public static void reset() {
        cachedBurgerItems = null;
        cachedDrinkItems = null;
    }

    public static List<Item> getAvailableMenuItems(ServerLevel level, String role) {
        if ("BURGER".equalsIgnoreCase(role)) {
            if (cachedBurgerItems == null || cachedBurgerItems.isEmpty()) {
                cachedBurgerItems = scanMenuItems(level, "BURGER");
            }
            return cachedBurgerItems;
        }
        if ("DRINK".equalsIgnoreCase(role) || "ICE_CREAM".equalsIgnoreCase(role)) {
            if (cachedDrinkItems == null || cachedDrinkItems.isEmpty()) {
                cachedDrinkItems = scanMenuItems(level, "DRINK");
            }
            return cachedDrinkItems;
        }
        return List.of();
    }

    private static List<Item> scanMenuItems(ServerLevel level, String role) {
        List<Item> withRecipes = new ArrayList<>();
        List<Item> allValid = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null) continue;
            String path = id.getPath().toLowerCase(Locale.ROOT);
            // Skip potions, splash potions, ominous bottles, bucket fluids
            if (path.contains("potion") || path.contains("ominous") || path.contains("spawn_egg")
                    || path.equals("water_bottle") || path.contains("bucket") || path.contains("bottle_of_")) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            if (ZooCashierEntity.isValidItemForRole(role, stack)) {
                allValid.add(item);
                if (findRecipeFor(level, item) != null) {
                    withRecipes.add(item);
                }
            }
        }

        if (!withRecipes.isEmpty()) {
            return withRecipes;
        }
        if (!allValid.isEmpty()) {
            return allValid;
        }
        if ("BURGER".equalsIgnoreCase(role)) {
            return List.of(Items.BREAD, Items.COOKED_BEEF);
        }
        return List.of(Items.HONEY_BOTTLE, Items.MILK_BUCKET);
    }

    public static List<ItemStack> pickRandomMenus(ServerLevel level, String role, Random random) {
        List<Item> pool = new ArrayList<>(getAvailableMenuItems(level, role));
        if (pool.isEmpty()) {
            return List.of();
        }
        // Maximum 2 menu variants per restock request
        int variantCount = (pool.size() >= 2 && random.nextBoolean()) ? 2 : 1;
        List<ItemStack> chosen = new ArrayList<>(variantCount);

        for (int i = 0; i < variantCount && !pool.isEmpty(); i++) {
            int idx = random.nextInt(pool.size());
            Item item = pool.remove(idx);
            // Total portions across the request is 8 to 16
            int portions = (variantCount == 1) ? (8 + random.nextInt(5)) : (5 + random.nextInt(4));
            chosen.add(new ItemStack(item, portions));
        }
        return chosen;
    }

    public static Map<Item, Integer> resolveRawIngredientsForMenus(ServerLevel level, String role, List<ItemStack> menus) {
        Map<Item, Integer> totalRaw = new LinkedHashMap<>();
        for (ItemStack menuStack : menus) {
            if (menuStack == null || menuStack.isEmpty()) continue;
            int portions = Math.max(1, menuStack.getCount());
            Map<Item, Integer> perOne = resolveRawIngredientsForSingleItem(level, role, menuStack.getItem());
            for (Map.Entry<Item, Integer> entry : perOne.entrySet()) {
                int needed = entry.getValue() * portions;
                totalRaw.merge(entry.getKey(), needed, Integer::sum);
            }
        }
        return totalRaw;
    }

    public static Map<Item, Integer> resolveRawIngredientsForSingleItem(ServerLevel level, String role, Item menuItem) {
        Map<Item, Integer> rawMap = new LinkedHashMap<>();
        RecipeHolder<?> recipeHolder = findRecipeFor(level, menuItem);

        if (recipeHolder != null) {
            ItemStack resultStack = recipeHolder.value().getResultItem(level.registryAccess());
            int outCount = (resultStack != null && !resultStack.isEmpty()) ? Math.max(1, resultStack.getCount()) : 1;

            for (Ingredient ingredient : recipeHolder.value().getIngredients()) {
                if (ingredient == null || ingredient.isEmpty()) continue;
                Item chosen = pickRepresentativeIngredient(ingredient);
                if (chosen != null && chosen != Items.AIR) {
                    Item rawItem = toRawIngredient(chosen);
                    if (rawItem != null && rawItem != Items.AIR && !isContainerRemainderOnly(rawItem, role)) {
                        rawMap.merge(rawItem, 1, Integer::sum);
                    }
                }
            }

            if (outCount > 1 && !rawMap.isEmpty()) {
                for (Map.Entry<Item, Integer> e : rawMap.entrySet()) {
                    e.setValue(Math.max(1, (int) Math.ceil((double) e.getValue() / outCount)));
                }
            }
        }

        if (rawMap.isEmpty()) {
            populateFallbackRawIngredients(menuItem, role, rawMap);
        }
        return rawMap;
    }

    private static boolean isContainerRemainderOnly(Item item, String role) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id != null) {
            String path = id.getPath().toLowerCase(Locale.ROOT);
            if (path.equals("food_press") || path.equals("frying_pan") || path.equals("cooking_pot")
                    || path.contains("knife") || path.equals("mortar_and_pestle") || path.equals("skillet")) {
                return true;
            }
        }
        if ("BURGER".equalsIgnoreCase(role)) {
            return item == Items.BOWL || item == Items.BUCKET || item == Items.GLASS_BOTTLE;
        }
        return item == Items.BUCKET;
    }

    private static RecipeHolder<?> findRecipeFor(ServerLevel level, Item targetItem) {
        if (level == null || targetItem == null || targetItem == Items.AIR) return null;
        RecipeHolder<?> fallback = null;
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            try {
                ItemStack result = holder.value().getResultItem(level.registryAccess());
                if (result != null && !result.isEmpty() && result.is(targetItem)) {
                    if (!holder.value().getIngredients().isEmpty()) {
                        return holder;
                    }
                    fallback = holder;
                }
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }

    private static Item pickRepresentativeIngredient(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        if (stacks == null || stacks.length == 0) return null;

        // Prefer vanilla or common farming mods so ingredients are familiar to gather (never pick croptopia unless no other option exists)
        for (String prefNs : new String[]{"minecraft", "farmersdelight", "farm_and_charm", "moredelight", "naturalist"}) {
            for (ItemStack st : stacks) {
                if (st != null && !st.isEmpty()) {
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(st.getItem());
                    if (id != null && prefNs.equals(id.getNamespace())) {
                        return st.getItem();
                    }
                }
            }
        }
        for (ItemStack st : stacks) {
            if (st != null && !st.isEmpty()) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(st.getItem());
                if (id != null && !"croptopia".equals(id.getNamespace())) {
                    return st.getItem();
                }
            }
        }
        return stacks[0].getItem();
    }

    public static Item toRawIngredient(Item item) {
        if (item == null || item == Items.AIR) return Items.AIR;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return item;
        String path = id.getPath().toLowerCase(Locale.ROOT);

        // Bread / buns / dough -> raw wheat
        if (item == Items.BREAD || path.contains("bread") || path.contains("bun") || path.contains("dough") || path.contains("toast")) {
            return Items.WHEAT;
        }
        // Beef / patty / minced beef -> raw beef
        if (item == Items.COOKED_BEEF || path.contains("beef_patty") || path.contains("minced_beef")
                || path.contains("cooked_beef") || path.contains("steak")) {
            return Items.BEEF;
        }
        // Pork / bacon / ham -> raw porkchop
        if (item == Items.COOKED_PORKCHOP || path.contains("bacon") || path.contains("cooked_pork") || path.contains("ham")) {
            return Items.PORKCHOP;
        }
        // Chicken -> raw chicken
        if (item == Items.COOKED_CHICKEN || path.contains("chicken_cuts") || path.contains("cooked_chicken") || path.contains("chicken_patty")) {
            return Items.CHICKEN;
        }
        // Mutton -> raw mutton
        if (item == Items.COOKED_MUTTON || path.contains("mutton_chops") || path.contains("cooked_mutton") || path.contains("lamb")) {
            return Items.MUTTON;
        }
        // Rabbit -> raw rabbit
        if (item == Items.COOKED_RABBIT || path.contains("cooked_rabbit")) {
            return Items.RABBIT;
        }
        // Fish -> raw cod / salmon
        if (item == Items.COOKED_COD || path.contains("cod_slice") || path.contains("cooked_cod")) {
            return Items.COD;
        }
        if (item == Items.COOKED_SALMON || path.contains("salmon_slice") || path.contains("cooked_salmon")) {
            return Items.SALMON;
        }
        // Fried egg -> raw egg
        if (path.contains("fried_egg") || path.contains("boiled_egg") || path.contains("cooked_egg")) {
            return Items.EGG;
        }
        // Cabbage leaf -> whole cabbage if present
        if (path.equals("cabbage_leaf")) {
            Item cabbage = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("farmersdelight", "cabbage"));
            if (cabbage != Items.AIR) return cabbage;
        }
        // Diced/baked potato -> raw potato
        if (item == Items.BAKED_POTATO || path.contains("diced_potato") || path.contains("potato_slice")) {
            return Items.POTATO;
        }
        // Milk bottle / bucket -> sugar or wheat/apple if bucket
        if (item == Items.MILK_BUCKET || path.contains("milk_bottle")) {
            Item milkBottle = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("farmersdelight", "milk_bottle"));
            if (milkBottle != Items.AIR && item == Items.MILK_BUCKET) {
                return milkBottle;
            }
        }
        // Potion / water bottle in tea recipes -> glass bottle
        if (item == Items.POTION || path.equals("water_bottle")) {
            return Items.GLASS_BOTTLE;
        }
        // Cheese -> zootycoon cheese or milk bottle
        if (path.contains("cheese") && !path.contains("cheeseburger")) {
            return net.zootycoon.item.ZooItems.CHEESE;
        }
        return item;
    }

    private static void populateFallbackRawIngredients(Item menuItem, String role, Map<Item, Integer> rawMap) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(menuItem);
        String path = id != null ? id.getPath().toLowerCase(Locale.ROOT) : "";

        if ("BURGER".equalsIgnoreCase(role)) {
            rawMap.put(Items.WHEAT, 1);
            if (path.contains("deluxe_cheeseburger")) {
                rawMap.put(Items.BEEF, 2);
                rawMap.put(Items.PORKCHOP, 1);
                rawMap.put(net.zootycoon.item.ZooItems.CHEESE, 1);
            } else if (path.contains("cheeseburger")) {
                rawMap.put(Items.BEEF, 1);
                rawMap.put(net.zootycoon.item.ZooItems.CHEESE, 1);
            } else if (path.contains("chicken")) {
                rawMap.put(Items.CHICKEN, 1);
            } else if (path.contains("pork") || path.contains("bacon") || path.contains("ham")) {
                rawMap.put(Items.PORKCHOP, 1);
            } else if (path.contains("fish") || path.contains("cod")) {
                rawMap.put(Items.COD, 1);
            } else if (path.contains("salmon")) {
                rawMap.put(Items.SALMON, 1);
            } else if (path.contains("mutton") || path.contains("lamb")) {
                rawMap.put(Items.MUTTON, 1);
            } else {
                rawMap.put(Items.BEEF, 1);
            }

            Item cabbage = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("farmersdelight", "cabbage"));
            Item tomato = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("farmersdelight", "tomato"));
            if (cabbage != Items.AIR) {
                rawMap.put(cabbage, 1);
            } else {
                rawMap.put(Items.CARROT, 1);
            }
            if (tomato != Items.AIR) {
                rawMap.put(tomato, 1);
            }
        } else {
            // Drink fallback
            rawMap.put(Items.GLASS_BOTTLE, 1);
            rawMap.put(Items.SUGAR, 1);
            if (path.contains("matcha") || path.contains("green_tea")) {
                rawMap.put(net.zootycoon.item.ZooItems.GREEN_TEA_LEAVES, 1);
            } else if (path.contains("melon")) {
                rawMap.put(Items.MELON_SLICE, 2);
            } else if (path.contains("glow")) {
                rawMap.put(Items.GLOW_BERRIES, 2);
            } else if (path.contains("berry") || path.contains("strawberry")) {
                rawMap.put(Items.SWEET_BERRIES, 2);
            } else if (path.contains("cocoa") || path.contains("chocolate") || path.contains("coffee")) {
                rawMap.put(Items.COCOA_BEANS, 2);
            } else if (path.contains("pumpkin")) {
                rawMap.put(Items.PUMPKIN, 1);
            } else {
                rawMap.put(Items.APPLE, 2);
            }
        }
    }
}

