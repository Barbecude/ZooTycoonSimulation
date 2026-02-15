package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Zone Marker Block - Digunakan untuk menandai corner area zoo.
 * Taruh 2 marker di diagonal corners untuk define zone rectangle.
 */
public class ZoneMarkerBlock extends Block {
    public ZoneMarkerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .strength(1.0F)
                .sound(SoundType.METAL)
                .lightLevel(state -> 10) // Glow agar mudah dilihat
                .noOcclusion());
    }
}
