package net.zootycoon.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.entity.ZooEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ZooItems {
    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ZooTycoonMod.id("main")
    );

    public static final CreativeModeTab CREATIVE_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(net.zootycoon.block.ZooBlocks.TOILET_ITEM))
            .title(Component.translatable("itemGroup.zootycoon.main"))
            .build();

    public static final ResourceKey<Item> VISITOR_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("visitor_spawn_egg")
    );

    public static final Item VISITOR_SPAWN_EGG = new ZooVisitorSpawnEggItem(
            new Item.Properties()
    );

    public static final ResourceKey<Item> CASHIER_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("cashier_spawn_egg")
    );

    public static final Item CASHIER_SPAWN_EGG = new SpawnEggItem(
            ZooEntities.CASHIER,
            0x8B4513,
            0xD4AF37,
            new Item.Properties()
    );

    public static final ResourceKey<Item> HUNTER_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("hunter_spawn_egg")
    );

    public static final Item HUNTER_SPAWN_EGG = new SpawnEggItem(
            ZooEntities.HUNTER,
            0x2C3E50,
            0xC0392B,
            new Item.Properties()
    );

    public static final ResourceKey<Item> MAGNIFYING_GLASS_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("magnifying_glass")
    );

    public static final Item MAGNIFYING_GLASS = new MagnifyingGlassItem(
            new Item.Properties()
    );

    public static final ResourceKey<Item> BROOM_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("broom")
    );

    public static final Item BROOM = new BroomItem(
            new Item.Properties()
    );

    public static final ResourceKey<Item> BUILDER_NOTE_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("builder_note")
    );

    public static final Item BUILDER_NOTE = new BuilderNoteItem(
            new Item.Properties().stacksTo(1)
    );

    public static final ResourceKey<Item> COPPER_WATERING_CAN_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("copper_watering_can")
    );
    public static final Item COPPER_WATERING_CAN = new WateringCanItem(
            new Item.Properties().rarity(net.minecraft.world.item.Rarity.COMMON).durability(12),
            60, 18, 0
    );

    public static final ResourceKey<Item> IRON_WATERING_CAN_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("iron_watering_can")
    );
    public static final Item IRON_WATERING_CAN = new WateringCanItem(
            new Item.Properties().rarity(net.minecraft.world.item.Rarity.COMMON).durability(24),
            45, 15, 1
    );

    public static final ResourceKey<Item> GOLD_WATERING_CAN_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("gold_watering_can")
    );
    public static final Item GOLD_WATERING_CAN = new WateringCanItem(
            new Item.Properties().rarity(net.minecraft.world.item.Rarity.COMMON).durability(54),
            45, 12, 2
    );

    public static final ResourceKey<Item> DIAMOND_WATERING_CAN_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("diamond_watering_can")
    );
    public static final Item DIAMOND_WATERING_CAN = new WateringCanItem(
            new Item.Properties().rarity(net.minecraft.world.item.Rarity.COMMON).durability(90),
            40, 12, 3
    );

    public static final ResourceKey<Item> NETHERITE_WATERING_CAN_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("netherite_watering_can")
    );
    public static final Item NETHERITE_WATERING_CAN = new WateringCanItem(
            new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).durability(120).fireResistant(),
            40, 5, 4, true
    );

    public static final ResourceKey<Item> GREEN_TEA_LEAVES_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("green_tea_leaves")
    );
    public static final Item GREEN_TEA_LEAVES = new Item(
            new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).build())
    );

    public static final ResourceKey<Item> MATCHA_LATTE_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("matcha_latte")
    );
    public static final Item MATCHA_LATTE = new MatchaLatteItem(
            new Item.Properties().stacksTo(16).food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.6f).build())
    );

    public static final ResourceKey<Item> CHEESE_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("cheese")
    );
    public static final Item CHEESE = new Item(
            new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.4f).build())
    );

    public static final ResourceKey<Item> CHEESEBURGER_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("cheeseburger")
    );
    public static final Item CHEESEBURGER = new Item(
            new Item.Properties().stacksTo(16).food(new FoodProperties.Builder().nutrition(10).saturationModifier(0.8f).build())
    );

    public static final ResourceKey<Item> DELUXE_CHEESEBURGER_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("deluxe_cheeseburger")
    );
    public static final Item DELUXE_CHEESEBURGER = new Item(
            new Item.Properties().stacksTo(16).food(new FoodProperties.Builder().nutrition(14).saturationModifier(0.9f).build())
    );

    public static List<ItemStack> createJakesBuildToolsItems() {
        List<ItemStack> list = new ArrayList<>();

        // 1. Iron Trowel
        list.add(makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:iron_trowel", "Iron Trowel", 102201, 750, "jbt_trowel", "jbt_trowel_iron"));
        // 2. Diamond Trowel
        list.add(makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:diamond_trowel", "Diamond Trowel", 102203, 4500, "jbt_trowel", "jbt_trowel_diamond"));
        // 3. Iron Chisel
        list.add(makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:iron_chisel", "Iron Chisel", 102202, 350, "jbt_chisel", "jbt_chisel_iron"));
        // 4. Diamond Chisel
        list.add(makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:diamond_chisel", "Diamond Chisel", 102204, 2100, "jbt_chisel", "jbt_chisel_diamond"));
        // 5. Block Wrench
        list.add(makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:wrench", "Block Wrench", 1022097, 5000, "jbt_wrench"));
        // 6. Hammer
        list.add(makeJbtTool(Items.IRON_PICKAXE, "jbt:hammer", "Hammer", 102211, 1800, "jbt_hammer"));
        // 7. Tape Measure
        ItemStack tape = makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:tape", "Tape Measure", 1022095, 0, "jbt_tape");
        CompoundTag tapeTag = new CompoundTag();
        tapeTag.putBoolean("jbt_tape", true);
        tapeTag.putBoolean("jbt_tape_open", false);
        tape.set(DataComponents.CUSTOM_DATA, CustomData.of(tapeTag));
        list.add(tape);
        // 8. Grass Starter
        list.add(makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:grass", "Grass Starter", 102209, 10, "jbt_grass"));
        // 9. Void Bucket
        list.add(makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:bucket", "Void Bucket", 102206, 0, "jbt_bucket"));
        // 10. Void Bundle
        ItemStack voidBundle = makeJbtTool(Items.BUNDLE, "jbt:bundle", "Void Bundle", 1022095, 0, "jbt_bundle");
        voidBundle.set(DataComponents.FOOD, new FoodProperties(0, 0.0f, true, 2147483648.0f, Optional.empty(), List.of()));
        list.add(voidBundle);
        // 11. Mob Bag
        list.add(makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:bag", "Mob Bag", 102207, 0, "jbt_bag"));
        // 12. Experience Flask
        list.add(makeJbtTool(Items.WARPED_FUNGUS_ON_A_STICK, "jbt:flask", "Experience Flask", 1022091, 0, "jbt_flask"));
        // 13. Rope Ladder
        list.add(makeJbtTool(Items.LADDER, "jbt:ladder", "Rope Ladder", 1022099, 0, "jbt_ladder"));
        // 14. Item Magnet
        ItemStack magnet = makeJbtTool(Items.RABBIT_FOOT, "jbt:magnet", "Item Magnet", 1022095, 0, "jbt_magnet");
        magnet.set(DataComponents.MAX_STACK_SIZE, 1);
        list.add(magnet);
        // 15. Hardhat (+2 reach)
        list.add(makeHardhat("jbt:hardhat", "Hardhat", 102205, 2.0));
        // 16. Foreman's Hardhat (+3 reach)
        list.add(makeHardhat("jbt:hardhat_rare", "Foreman's Hardhat", 102206, 3.0));

        return list;
    }

    public static List<ItemStack> createPhysicalVehiclesItems() {
        List<ItemStack> list = new ArrayList<>();
        // 1. Jeep CJ-5 1964
        list.add(makeVehicleSpawnEgg("phys_veh.jeep_cj-5_1964", "Jeep CJ-5 1964", 20002, "phys_veh.jeep_cj-5_1964"));
        // 2. Buhanka
        list.add(makeVehicleSpawnEgg("phys_veh.buhanka", "Buhanka", 20001, "phys_veh.buhanka"));
        // 3. Ecto-1A
        list.add(makeVehicleSpawnEgg("phys_veh.ecto-1a", "Ecto-1A", 20003, "phys_veh.ecto-1a"));
        // 4. Hammer Monster Truck
        list.add(makeVehicleSpawnEgg("phys_veh.hammer_monster", "Hammer Monster Truck", 20004, "phys_veh.hammer_monster"));
        // 5. Oldsmobile Cutlass Supreme Coupe (1980)
        list.add(makeVehicleSpawnEgg("phys_veh.oldsmobile_cutlass_supreme_coupe_1980", "Oldsmobile Cutlass Supreme Coupe (1980)", 20005, "phys_veh.oldsmobile_cutlass_supreme_coupe_1980"));
        // 6. NEFAZ-66052 Tanker Truck
        list.add(makeVehicleSpawnEgg("phys_veh.tt-66052", "NEFAZ-66052 Tanker Truck", 20006, "phys_veh.tt-66052"));
        // 7. Vanna
        list.add(makeVehicleSpawnEgg("phys_veh.vanna", "Vanna", 20007, "phys_veh.vanna"));
        // 8. ZIL-130
        list.add(makeVehicleSpawnEgg("phys_veh.zil-130", "ZIL-130", 20008, "phys_veh.zil-130"));
        // 9. Armored Truck 4x4 (Black)
        list.add(makeVehicleSpawnEgg("phys_veh.armored_truck_4x4", "Armored Truck 4x4 (Black)", 20009, "phys_veh.armored_truck_4x4", "black"));
        // 10. Armored Truck 4x4 (Gray-Red)
        list.add(makeVehicleSpawnEgg("phys_veh.armored_truck_4x4", "Armored Truck 4x4 (Gray-Red)", 20010, "phys_veh.armored_truck_4x4", "gray-red"));
        // 11. ZAZ-968A (Race 01)
        list.add(makeVehicleSpawnEgg("phys_veh.zaz-968a", "ZAZ-968A (Race 01)", 20011, "phys_veh.zaz-968a", "race_01", "stock"));
        // 12. ZAZ-968A (Race 02)
        list.add(makeVehicleSpawnEgg("phys_veh.zaz-968a", "ZAZ-968A (Race 02)", 20012, "phys_veh.zaz-968a", "race_02", "stock"));
        // 13. ZAZ-968A (Classic Tint)
        list.add(makeVehicleSpawnEgg("phys_veh.zaz-968a", "ZAZ-968A (Classic)", 20013, "phys_veh.zaz-968a", "tint", "stock"));
        // 14. ZAZ-968M (Futuristic)
        list.add(makeVehicleSpawnEgg("phys_veh.zaz-968m", "ZAZ-968M (Futuristic)", 20014, "phys_veh.zaz-968m", "futuristic", "stock"));
        // 15. ZAZ-968M (Race 01)
        list.add(makeVehicleSpawnEgg("phys_veh.zaz-968m", "ZAZ-968M (Race 01)", 20015, "phys_veh.zaz-968m", "race_01", "stock"));
        // 16. ZAZ-968M (Race 02)
        list.add(makeVehicleSpawnEgg("phys_veh.zaz-968m", "ZAZ-968M (Race 02)", 20016, "phys_veh.zaz-968m", "race_02", "stock"));
        // 17. ZAZ-968M (Classic Tint)
        list.add(makeVehicleSpawnEgg("phys_veh.zaz-968m", "ZAZ-968M (Classic)", 20017, "phys_veh.zaz-968m", "tint", "stock"));
        // 18. ZAZ-968T (Caterpillar Tracks)
        list.add(makeVehicleSpawnEgg("phys_veh.zaz-968t", "ZAZ-968T (Tracks)", 20018, "phys_veh.zaz-968m", "tint", "tracks"));
        // 19. Vehicles Remover
        list.add(makeVehicleSpawnEgg("phys_veh.remover", "Vehicles Remover", 20019, "phys_veh.remover"));
        // 20. Car Key
        list.add(makeVehicleTool("phys_veh.car_key", "Car Key", 30001, "phys_veh.car_key"));
        // 21. Toolbox
        list.add(makeVehicleTool("phys_veh.toolbox", "Toolbox", 30002, "phys_veh.toolbox"));
        // 22. Torque Wrench
        list.add(makeVehicleTool("phys_veh.torque_wrench", "Torque Wrench", 30003, "phys_veh.torque_wrench"));

        return list;
    }

    private static ItemStack makeVehicleSpawnEgg(String translateKey, String fallbackName, int customModelData, String vehicleTag, String... configModes) {
        ItemStack stack = new ItemStack(Items.DOLPHIN_SPAWN_EGG);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatableWithFallback(translateKey, fallbackName).withStyle(s -> s.withItalic(false)));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));

        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("id", "minecraft:pig");
        entityTag.putBoolean("Silent", true);
        entityTag.putBoolean("NoAI", true);
        entityTag.putBoolean("Invulnerable", true);
        entityTag.putString("DeathLootTable", "");

        ListTag tags = new ListTag();
        tags.add(StringTag.valueOf("phys_veh.spawn"));
        tags.add(StringTag.valueOf(vehicleTag));
        entityTag.put("Tags", tags);

        CompoundTag dataTag = new CompoundTag();
        CompoundTag physVehTag = new CompoundTag();
        if (configModes.length > 0) {
            CompoundTag cmdTag = new CompoundTag();
            ListTag stringsTag = new ListTag();
            stringsTag.add(StringTag.valueOf(configModes[0]));
            cmdTag.put("strings", stringsTag);
            physVehTag.put("minecraft:custom_model_data", cmdTag);
        }
        if (configModes.length > 1) {
            CompoundTag customDataTag = new CompoundTag();
            customDataTag.putString("config_mode", configModes[1]);
            physVehTag.put("minecraft:custom_data", customDataTag);
        }
        dataTag.put("phys_veh", physVehTag);
        entityTag.put("data", dataTag);

        stack.set(DataComponents.ENTITY_DATA, CustomData.of(entityTag));
        return stack;
    }

    private static ItemStack makeVehicleTool(String translateKey, String fallbackName, int customModelData, String customDataFlag) {
        ItemStack stack = new ItemStack(Items.POISONOUS_POTATO);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatableWithFallback(translateKey, fallbackName).withStyle(s -> s.withItalic(false)));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));
        stack.set(DataComponents.FOOD, new FoodProperties(0, 0.0f, true, 2147483648.0f, Optional.empty(), List.of()));
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(customDataFlag, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    private static ItemStack makeJbtTool(Item baseItem, String translateKey, String fallbackName, int customModelData, int maxDamage, String... customDataFlags) {
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatableWithFallback(translateKey, fallbackName).withStyle(s -> s.withItalic(false)));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));
        if (maxDamage > 0) {
            stack.set(DataComponents.MAX_DAMAGE, maxDamage);
        }
        CompoundTag tag = new CompoundTag();
        for (String flag : customDataFlags) {
            tag.putBoolean(flag, true);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    private static ItemStack makeHardhat(String translateKey, String fallbackName, int customModelData, double extraReach) {
        ItemStack stack = new ItemStack(Items.CARVED_PUMPKIN);
        stack.set(DataComponents.MAX_STACK_SIZE, 1);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatableWithFallback(translateKey, fallbackName).withStyle(s -> s.withItalic(false)));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(
                        Attributes.BLOCK_INTERACTION_RANGE,
                        new AttributeModifier(ResourceLocation.withDefaultNamespace("jbt_hardhat_reach"), extraReach, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD
                )
                .build();
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        return stack;
    }

    public static ItemStack createHardhat() {
        return makeHardhat("jbt:hardhat", "Hardhat", 102205, 2.0);
    }

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
        Registry.register(BuiltInRegistries.ITEM, VISITOR_SPAWN_EGG_KEY, VISITOR_SPAWN_EGG);
        Registry.register(BuiltInRegistries.ITEM, CASHIER_SPAWN_EGG_KEY, CASHIER_SPAWN_EGG);
        Registry.register(BuiltInRegistries.ITEM, HUNTER_SPAWN_EGG_KEY, HUNTER_SPAWN_EGG);
        Registry.register(BuiltInRegistries.ITEM, MAGNIFYING_GLASS_KEY, MAGNIFYING_GLASS);
        Registry.register(BuiltInRegistries.ITEM, BROOM_KEY, BROOM);
        Registry.register(BuiltInRegistries.ITEM, BUILDER_NOTE_KEY, BUILDER_NOTE);
        Registry.register(BuiltInRegistries.ITEM, COPPER_WATERING_CAN_KEY, COPPER_WATERING_CAN);
        Registry.register(BuiltInRegistries.ITEM, IRON_WATERING_CAN_KEY, IRON_WATERING_CAN);
        Registry.register(BuiltInRegistries.ITEM, GOLD_WATERING_CAN_KEY, GOLD_WATERING_CAN);
        Registry.register(BuiltInRegistries.ITEM, DIAMOND_WATERING_CAN_KEY, DIAMOND_WATERING_CAN);
        Registry.register(BuiltInRegistries.ITEM, NETHERITE_WATERING_CAN_KEY, NETHERITE_WATERING_CAN);
        Registry.register(BuiltInRegistries.ITEM, GREEN_TEA_LEAVES_KEY, GREEN_TEA_LEAVES);
        Registry.register(BuiltInRegistries.ITEM, MATCHA_LATTE_KEY, MATCHA_LATTE);
        Registry.register(BuiltInRegistries.ITEM, CHEESE_KEY, CHEESE);
        Registry.register(BuiltInRegistries.ITEM, CHEESEBURGER_KEY, CHEESEBURGER);
        Registry.register(BuiltInRegistries.ITEM, DELUXE_CHEESEBURGER_KEY, DELUXE_CHEESEBURGER);

        ItemGroupEvents.modifyEntriesEvent(CREATIVE_TAB_KEY).register(entries -> {
            entries.accept(net.zootycoon.block.ZooBlocks.FEEDING_TROUGH_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.EMPTY_BARREL_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.GACHA_MACHINE_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.HEATER_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.COOLER_ITEM);
            entries.accept(GREEN_TEA_LEAVES);
            entries.accept(MATCHA_LATTE);
            entries.accept(CHEESE);
            entries.accept(CHEESEBURGER);
            entries.accept(DELUXE_CHEESEBURGER);
            entries.accept(MAGNIFYING_GLASS);
            entries.accept(BROOM);
            entries.accept(BUILDER_NOTE);
            entries.accept(COPPER_WATERING_CAN);
            entries.accept(IRON_WATERING_CAN);
            entries.accept(GOLD_WATERING_CAN);
            entries.accept(DIAMOND_WATERING_CAN);
            entries.accept(NETHERITE_WATERING_CAN);
            entries.accept(net.zootycoon.block.ZooBlocks.TOILET_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.TRASH_CAN_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.TRASH_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.CONSTRUCTION_TAPE_ITEM);
            entries.accept(VISITOR_SPAWN_EGG);
            entries.accept(CASHIER_SPAWN_EGG);
            entries.accept(HUNTER_SPAWN_EGG);
            for (ItemStack jbtStack : createJakesBuildToolsItems()) {
                entries.accept(jbtStack);
            }
            for (ItemStack pvStack : createPhysicalVehiclesItems()) {
                entries.accept(pvStack);
            }
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(net.zootycoon.block.ZooBlocks.HEATER_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.COOLER_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.FEEDING_TROUGH_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.EMPTY_BARREL_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.GACHA_MACHINE_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.TOILET_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.TRASH_CAN_ITEM);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(COPPER_WATERING_CAN);
            entries.accept(IRON_WATERING_CAN);
            entries.accept(GOLD_WATERING_CAN);
            entries.accept(DIAMOND_WATERING_CAN);
            entries.accept(NETHERITE_WATERING_CAN);
            for (ItemStack jbtStack : createJakesBuildToolsItems()) {
                entries.accept(jbtStack);
            }
            for (ItemStack pvStack : createPhysicalVehiclesItems()) {
                entries.accept(pvStack);
            }
        });
    }
}
