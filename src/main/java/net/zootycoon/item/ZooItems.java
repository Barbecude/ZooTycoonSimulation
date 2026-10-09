package net.zootycoon.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.zootycoon.ZooTycoonMod;

public class ZooItems {
    public static final Item BUILD_TOOL = new BuildToolItem(
            new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)
    );

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, ZooTycoonMod.id("build_tool"), BUILD_TOOL);
    }
}

