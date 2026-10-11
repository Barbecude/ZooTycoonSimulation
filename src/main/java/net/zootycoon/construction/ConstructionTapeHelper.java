package net.zootycoon.construction;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.zootycoon.block.ZooBlocks;

import java.util.HashSet;
import java.util.Set;

public class ConstructionTapeHelper {

    public static Set<BlockPos> placePerimeterTape(ServerLevel level, BoundingBox box) {
        Set<BlockPos> placed = new HashSet<>();
        int minX = box.minX() - 1;
        int maxX = box.maxX() + 1;
        int minZ = box.minZ() - 1;
        int maxZ = box.maxZ() + 1;

        // North and South border lines
        for (int x = minX; x <= maxX; x++) {
            placeTapeAt(level, x, box.minY(), minZ, placed);
            placeTapeAt(level, x, box.minY(), maxZ, placed);
        }

        // West and East border lines
        for (int z = minZ; z <= maxZ; z++) {
            placeTapeAt(level, minX, box.minY(), z, placed);
            placeTapeAt(level, maxX, box.minY(), z, placed);
        }

        // Update connected states between adjacent tapes
        for (BlockPos pos : placed) {
            updateTapeState(level, pos);
        }

        return placed;
    }

    private static void placeTapeAt(ServerLevel level, int x, int baseY, int z, Set<BlockPos> placed) {
        for (int dy = 5; dy >= -10; dy--) {
            BlockPos check = new BlockPos(x, baseY + dy, z);
            BlockState belowState = level.getBlockState(check);
            BlockPos above = check.above();
            BlockState aboveState = level.getBlockState(above);

            if (belowState.isSolid() && (aboveState.isAir() || aboveState.canBeReplaced())) {
                level.setBlock(above, ZooBlocks.CONSTRUCTION_TAPE.defaultBlockState(), 2);
                placed.add(above);
                return;
            }
        }
    }

    private static void updateTapeState(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(ZooBlocks.CONSTRUCTION_TAPE)) return;

        boolean n = level.getBlockState(pos.north()).is(ZooBlocks.CONSTRUCTION_TAPE);
        boolean e = level.getBlockState(pos.east()).is(ZooBlocks.CONSTRUCTION_TAPE);
        boolean s = level.getBlockState(pos.south()).is(ZooBlocks.CONSTRUCTION_TAPE);
        boolean w = level.getBlockState(pos.west()).is(ZooBlocks.CONSTRUCTION_TAPE);

        BlockState updated = state
                .setValue(ConstructionTapeBlock.NORTH, n)
                .setValue(ConstructionTapeBlock.EAST, e)
                .setValue(ConstructionTapeBlock.SOUTH, s)
                .setValue(ConstructionTapeBlock.WEST, w);

        level.setBlock(pos, updated, 2);
    }

    public static void removePerimeterTape(ServerLevel level, Set<BlockPos> positions) {
        if (positions == null) return;
        for (BlockPos pos : positions) {
            if (level.getBlockState(pos).is(ZooBlocks.CONSTRUCTION_TAPE)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }
}

