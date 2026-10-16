package net.zootycoon.visitor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.entity.ZooCashierEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooVisitorEntity;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.List;

public class ZooVisitorManager {

    private static long lastSpawnTick = 0L;
    private static long lastToastTick = 0L;

    public static void reset() {
        lastSpawnTick = 0L;
        lastToastTick = 0L;
    }

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

    /**
     * Periodic visitor lifecycle and spawning tick:
     * Evaluates active zoos, ticket stands, animal attractions, and naturally spawns
     * guests/visitors to explore the zoo and generate revenue!
     */
    public static void tickServer(ServerLevel level) {
        if (level.getServer() == null || level != level.getServer().overworld()) {
            return;
        }

        long currentTick = level.getGameTime();
        if (currentTick - lastSpawnTick < 200) { // Check every 10 seconds
            return;
        }
        lastSpawnTick = currentTick;

        ZooClaimManager claimManager = ZooClaimManager.get(level);

        for (ServerPlayer player : level.players()) {
            List<ZooClaim> claims = claimManager.getPlayerClaims(player.getUUID());
            if (claims.isEmpty()) continue;

            // Calculate bounding box covering player's zoo claims
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            for (ZooClaim c : claims) {
                var b = c.getBox();
                minX = Math.min(minX, b.minX());
                maxX = Math.max(maxX, b.maxX());
                minZ = Math.min(minZ, b.minZ());
                maxZ = Math.max(maxZ, b.maxZ());
            }

            AABB zooArea = new AABB(minX - 10, level.getMinBuildHeight(), minZ - 10,
                    maxX + 10, level.getMaxBuildHeight(), maxZ + 10);

            // Count existing visitors in this zoo
            List<ZooVisitorEntity> currentVisitors = level.getEntitiesOfClass(
                    ZooVisitorEntity.class, zooArea, Entity::isAlive
            );

            // Find Cashier NPCs in this zoo (especially Ticket booths)
            List<ZooCashierEntity> cashiers = level.getEntitiesOfClass(
                    ZooCashierEntity.class, zooArea, Entity::isAlive
            );

            // Calculate visitor capacity based on zoo size, animals, and cashiers
            int baseCapacity = 4;
            int cashierBonus = cashiers.size() * 3;
            int maxCapacity = Math.min(20, baseCapacity + cashierBonus);

            if (currentVisitors.size() >= maxCapacity) {
                continue;
            }

            // Find best entrance / spawn location:
            BlockPos spawnPos = null;

            // 1. Prefer spawning near a Ticket booth or Cashier
            for (ZooCashierEntity cashier : cashiers) {
                BlockPos cPos = cashier.blockPosition();
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos candidate = cPos.relative(dir, 2);
                    if (isValidWalkablePos(level, candidate)) {
                        spawnPos = candidate;
                        break;
                    }
                }
                if (spawnPos != null) break;
            }

            // 2. Fallback: find a fence gate or door along the claim edge
            if (spawnPos == null) {
                ZooClaim sampleClaim = claims.get(level.getRandom().nextInt(claims.size()));
                var b = sampleClaim.getBox();
                int checkY = (int) player.getY();

                // Sample perimeter points
                for (int attempt = 0; attempt < 8; attempt++) {
                    int px = b.minX() + level.getRandom().nextInt(Math.max(1, b.maxX() - b.minX() + 1));
                    int pz = (attempt % 2 == 0) ? b.minZ() : b.maxZ();
                    BlockPos p = new BlockPos(px, checkY, pz);
                    for (int dy = -3; dy <= 3; dy++) {
                        BlockPos test = p.above(dy);
                        BlockState st = level.getBlockState(test);
                        if (st.getBlock() instanceof FenceGateBlock || st.getBlock() instanceof DoorBlock) {
                            spawnPos = test;
                            break;
                        }
                    }
                    if (spawnPos != null) break;
                }
            }

            // 3. Fallback: spawn on a valid surface block near claim perimeter
            if (spawnPos == null) {
                ZooClaim sampleClaim = claims.get(level.getRandom().nextInt(claims.size()));
                var b = sampleClaim.getBox();
                int px = b.minX() + level.getRandom().nextInt(Math.max(1, b.maxX() - b.minX() + 1));
                int pz = b.minZ() + (level.getRandom().nextBoolean() ? 1 : Math.max(1, b.maxZ() - b.minZ() - 1));
                int py = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, px, pz);
                BlockPos candidate = new BlockPos(px, py, pz);
                if (isValidWalkablePos(level, candidate)) {
                    spawnPos = candidate;
                }
            }

            // 4. Final fallback: near player
            if (spawnPos == null) {
                double angle = level.getRandom().nextDouble() * Math.PI * 2;
                double dist = 10.0 + level.getRandom().nextDouble() * 6.0;
                int px = (int) (player.getX() + Math.cos(angle) * dist);
                int pz = (int) (player.getZ() + Math.sin(angle) * dist);
                int py = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, px, pz);
                spawnPos = new BlockPos(px, py, pz);
            }

            if (spawnPos != null && isValidWalkablePos(level, spawnPos)) {
                Vec3 spawnVec = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                ZooVisitorEntity visitor = spawnVisitor(level, spawnVec, spawnPos);

                // 30% chance to spawn an accompanying child visitor
                if (level.getRandom().nextFloat() < 0.30f && currentVisitors.size() + 1 < maxCapacity) {
                    Vec3 childVec = spawnVec.add(level.getRandom().nextDouble() - 0.5, 0, level.getRandom().nextDouble() - 0.5);
                    ZooVisitorEntity child = spawnVisitor(level, childVec, spawnPos);
                    if (child != null) {
                        child.setChildVisitor(true);
                    }
                }

                // Show notification occasionally (not more than once every 2 minutes)
                if (currentVisitors.isEmpty() && currentTick - lastToastTick > 2400) {
                    lastToastTick = currentTick;
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.visitor.spawned", 0);
                }
            }
        }
    }

    private static boolean isValidWalkablePos(ServerLevel level, BlockPos pos) {
        if (pos.getY() <= level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight() - 2) {
            return false;
        }
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());

        boolean airSpace = (feet.isAir() || feet.canBeReplaced()) && (head.isAir() || head.canBeReplaced());
        boolean solidGround = below.isSolid() && !below.isAir() && !below.is(net.minecraft.world.level.block.Blocks.LAVA) && !below.is(net.minecraft.world.level.block.Blocks.FIRE);

        return airSpace && solidGround;
    }
}
