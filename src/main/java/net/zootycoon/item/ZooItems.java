package net.zootycoon.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
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

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
        Registry.register(BuiltInRegistries.ITEM, VISITOR_SPAWN_EGG_KEY, VISITOR_SPAWN_EGG);
        Registry.register(BuiltInRegistries.ITEM, CASHIER_SPAWN_EGG_KEY, CASHIER_SPAWN_EGG);
        Registry.register(BuiltInRegistries.ITEM, MAGNIFYING_GLASS_KEY, MAGNIFYING_GLASS);
        Registry.register(BuiltInRegistries.ITEM, BROOM_KEY, BROOM);

        ItemGroupEvents.modifyEntriesEvent(CREATIVE_TAB_KEY).register(entries -> {
            entries.accept(net.zootycoon.block.ZooBlocks.FEEDING_TROUGH_ITEM);
            entries.accept(MAGNIFYING_GLASS);
            entries.accept(BROOM);
            entries.accept(net.zootycoon.block.ZooBlocks.TOILET_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.TRASH_CAN_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.TRASH_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.CONSTRUCTION_TAPE_ITEM);
            entries.accept(VISITOR_SPAWN_EGG);
            entries.accept(CASHIER_SPAWN_EGG);
            for (ItemStack jbtStack : createJakesBuildToolsItems()) {
                entries.accept(jbtStack);
            }
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            for (ItemStack jbtStack : createJakesBuildToolsItems()) {
                entries.accept(jbtStack);
            }
        });
    }
}
