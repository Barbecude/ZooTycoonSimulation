package net.zootycoon.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.zootycoon.block.ZooBlocks;
import net.zootycoon.util.ZooSeatHelper;

public class ZooWalkNodeEvaluator extends WalkNodeEvaluator {

    @Override
    public PathType getPathType(PathfindingContext context, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = context.getBlockState(pos);

        if (state != null && !state.isAir()) {
            // 1. Facility & Seatable blocks are treated as BLOCKED obstacles
            // Prevents zookeepers and visitors from treating chairs, toilets, and gacha machines
            // as walkable steps and getting stuck jumping repeatedly on them.
            if (state.is(ZooBlocks.TOILET)
                    || state.is(ZooBlocks.GACHA_MACHINE)
                    || ZooSeatHelper.isSeatableBlockState(state)) {
                return PathType.BLOCKED;
            }

            // 2. Closed wooden doors and trapdoors: evaluate as passable when entity can open/pass them
            if (state.getBlock() instanceof DoorBlock && this.canOpenDoors()) {
                return PathType.DOOR_WOOD_CLOSED;
            }
            if (state.getBlock() instanceof TrapDoorBlock && this.canPassDoors()) {
                return PathType.TRAPDOOR;
            }
        }

        return super.getPathType(context, x, y, z);
    }
}
