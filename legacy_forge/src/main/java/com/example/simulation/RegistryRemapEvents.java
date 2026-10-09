package com.example.simulation;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RegistryRemapEvents {
    private RegistryRemapEvents() {
    }

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        remapBlocks(event.getMappings(ForgeRegistries.Keys.BLOCKS, IndoZooTycoon.MODID));
        remapItems(event.getMappings(ForgeRegistries.Keys.ITEMS, IndoZooTycoon.MODID));
        remapBlockEntities(event.getMappings(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, IndoZooTycoon.MODID));
    }

    private static void remapBlocks(List<MissingMappingsEvent.Mapping<net.minecraft.world.level.block.Block>> mappings) {
        remap(mappings, "restroom", IndoZooTycoon.RESTROOM_BLOCK.get());
    }

    private static void remapItems(List<MissingMappingsEvent.Mapping<net.minecraft.world.item.Item>> mappings) {
        remap(mappings, "restroom", IndoZooTycoon.RESTROOM_ITEM.get());
    }

    private static void remapBlockEntities(
            List<MissingMappingsEvent.Mapping<net.minecraft.world.level.block.entity.BlockEntityType<?>>> mappings) {
        remap(mappings, "restroom", IndoZooTycoon.TOILET_BE.get());
    }

    private static <T> void remap(List<MissingMappingsEvent.Mapping<T>> mappings, String fromPath, T to) {
        for (MissingMappingsEvent.Mapping<T> mapping : mappings) {
            ResourceLocation key = mapping.getKey();
            if (key != null && fromPath.equals(key.getPath())) {
                mapping.remap(to);
            }
        }
    }
}
