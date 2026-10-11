package net.zootycoon.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.zootycoon.ZooTycoonMod;

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

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
        Registry.register(BuiltInRegistries.ITEM, VISITOR_SPAWN_EGG_KEY, VISITOR_SPAWN_EGG);

        ItemGroupEvents.modifyEntriesEvent(CREATIVE_TAB_KEY).register(entries -> {
            entries.accept(net.zootycoon.block.ZooBlocks.TOILET_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.TRASH_CAN_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.TRASH_ITEM);
            entries.accept(net.zootycoon.block.ZooBlocks.CONSTRUCTION_TAPE_ITEM);
            entries.accept(VISITOR_SPAWN_EGG);
        });
    }
}
