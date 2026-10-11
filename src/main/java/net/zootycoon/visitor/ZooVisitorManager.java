package net.zootycoon.visitor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooVisitorEntity;

public class ZooVisitorManager {

    /**
     * Spawns a visitor at the given position and sets an optional gate position.
     * When the visitor's visit ends, they will walk back to the gate position.
     */
    public static ZooVisitorEntity spawnVisitor(ServerLevel level, Vec3 pos, BlockPos gatePos) {
        ZooVisitorEntity visitor = ZooEntities.VISITOR.create(level);
        if (visitor != null) {
            visitor.setPos(pos.x, pos.y, pos.z);
            if (gatePos != null) {
                visitor.setGatePos(gatePos);
            } else {
                visitor.setGatePos(BlockPos.containing(pos));
            }
            level.addFreshEntity(visitor);
            ZooTycoonMod.LOGGER.info("Spawned Zoo Visitor at {} with gate {}", pos, visitor.getGatePos());
        }
        return visitor;
    }
}
