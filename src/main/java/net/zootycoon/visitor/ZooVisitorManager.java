package net.zootycoon.visitor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
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
     * Visitors ONLY spawn if the zoo has an active Ticket Booth cashier ("TICKET").
     * They spawn 5 blocks OUTSIDE the claimed zoo boundary near the Ticket Booth cashier,
     * then walk in to purchase a ticket at the Ticket Booth.
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

        // Do not spawn visitors during late night / approaching midnight (park closing)
        if (ZooVisitorEntity.isLateNight(level)) {
            return;
        }

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

            AABB zooArea = new AABB(minX - 16, level.getMinBuildHeight(), minZ - 16,
                    maxX + 16, level.getMaxBuildHeight(), maxZ + 16);

            // Find all Cashiers in this zoo
            List<ZooCashierEntity> allCashiers = level.getEntitiesOfClass(
                    ZooCashierEntity.class, zooArea, Entity::isAlive
            );

            // Filter specifically for Ticket Booth ("TICKET") cashiers
            List<ZooCashierEntity> ticketCashiers = allCashiers.stream()
                    .filter(c -> "TICKET".equalsIgnoreCase(c.getCashierRole()))
                    .toList();

            // Visitors ONLY spawn if there is at least one Ticket Booth cashier
            if (ticketCashiers.isEmpty()) {
                continue;
            }

            // Count existing visitors in this zoo
            List<ZooVisitorEntity> currentVisitors = level.getEntitiesOfClass(
                    ZooVisitorEntity.class, zooArea, Entity::isAlive
            );

            // Calculate visitor capacity based on zoo cashiers
            int baseCapacity = 4;
            int cashierBonus = allCashiers.size() * 3;
            int maxCapacity = Math.min(20, baseCapacity + cashierBonus);

            if (currentVisitors.size() >= maxCapacity) {
                continue;
            }

            // Pick a Ticket Booth cashier and compute spawn position 5 blocks OUTSIDE the claimed zoo area near it
            ZooCashierEntity ticketCashier = ticketCashiers.get(level.getRandom().nextInt(ticketCashiers.size()));
            BlockPos spawnPos = findOutsideSpawnNearTicketCashier(level, claims, ticketCashier);

            if (spawnPos != null) {
                Vec3 spawnVec = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                ZooVisitorEntity visitor = spawnVisitor(level, spawnVec, spawnPos);
                if (visitor != null) {
                    visitor.getNavigation().moveTo(ticketCashier, 1.05D);
                }

                // 30% chance to spawn an accompanying child visitor
                if (level.getRandom().nextFloat() < 0.30f && currentVisitors.size() + 1 < maxCapacity) {
                    Vec3 childVec = spawnVec.add(level.getRandom().nextDouble() - 0.5, 0, level.getRandom().nextDouble() - 0.5);
                    ZooVisitorEntity child = spawnVisitor(level, childVec, spawnPos);
                    if (child != null) {
                        child.setChildVisitor(true);
                        child.getNavigation().moveTo(ticketCashier, 1.05D);
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

    private static boolean isInsideClaims(List<ZooClaim> claims, int x, int z) {
        for (ZooClaim c : claims) {
            if (c.contains(x, z)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a walkable spawn position 5 blocks OUTSIDE the claimed zoo area,
     * aligned with the nearest border to the Ticket Booth cashier.
     */
    private static BlockPos findOutsideSpawnNearTicketCashier(
            ServerLevel level,
            List<ZooClaim> claims,
            ZooCashierEntity ticketCashier
    ) {
        BlockPos cPos = ticketCashier.blockPosition();
        int cx = cPos.getX();
        int cy = cPos.getY();
        int cz = cPos.getZ();

        Direction outwardDir = Direction.NORTH;
        BlockPos borderEdgePos = cPos;

        if (isInsideClaims(claims, cx, cz)) {
            // Cashier is inside the claimed zoo area: find the closest claim boundary edge in cardinal directions
            int minStepsToOutside = Integer.MAX_VALUE;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                for (int step = 1; step <= 64; step++) {
                    int tx = cx + dir.getStepX() * step;
                    int tz = cz + dir.getStepZ() * step;
                    if (!isInsideClaims(claims, tx, tz)) {
                        if (step < minStepsToOutside) {
                            minStepsToOutside = step;
                            outwardDir = dir;
                            borderEdgePos = new BlockPos(
                                    cx + dir.getStepX() * (step - 1),
                                    cy,
                                    cz + dir.getStepZ() * (step - 1)
                            );
                        }
                        break;
                    }
                }
            }
        } else {
            // Cashier is already just outside the claimed zoo border: find which direction the claim is, and step opposite
            int minStepsToClaim = Integer.MAX_VALUE;
            Direction inwardDir = Direction.SOUTH;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                for (int step = 1; step <= 32; step++) {
                    int tx = cx + dir.getStepX() * step;
                    int tz = cz + dir.getStepZ() * step;
                    if (isInsideClaims(claims, tx, tz)) {
                        if (step < minStepsToClaim) {
                            minStepsToClaim = step;
                            inwardDir = dir;
                        }
                        break;
                    }
                }
            }
            outwardDir = inwardDir.getOpposite();
            borderEdgePos = cPos;
        }

        // Target is 5 blocks outside the border edge in outwardDir
        Direction lateralDir = outwardDir.getClockWise();

        // Search around targetBase (distance 5, then 4..6, with small lateral offsets) for a valid walkable block outside the claim
        int[] distOffsets = {5, 6, 4, 7};
        int[] latOffsets = {0, 1, -1, 2, -2};

        for (int dist : distOffsets) {
            BlockPos baseAtDist = borderEdgePos.relative(outwardDir, dist);
            for (int lat : latOffsets) {
                int tx = baseAtDist.getX() + lateralDir.getStepX() * lat;
                int tz = baseAtDist.getZ() + lateralDir.getStepZ() * lat;

                // Must be OUTSIDE the claimed zoo area
                if (isInsideClaims(claims, tx, tz)) {
                    continue;
                }

                BlockPos groundPos = findWalkableAtColumn(level, tx, cy, tz);
                if (groundPos != null) {
                    return groundPos;
                }
            }
        }

        return null;
    }

    private static BlockPos findWalkableAtColumn(ServerLevel level, int x, int refY, int z) {
        // First check near the cashier's elevation
        int[] dyOrder = {0, 1, -1, 2, -2, 3, -3, 4, -4};
        for (int dy : dyOrder) {
            BlockPos candidate = new BlockPos(x, refY + dy, z);
            if (isValidWalkablePos(level, candidate)) {
                return candidate;
            }
        }
        // Fallback to surface heightmap
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos surfacePos = new BlockPos(x, surfaceY, z);
        if (isValidWalkablePos(level, surfacePos)) {
            return surfacePos;
        }
        return null;
    }

    private static boolean isValidWalkablePos(ServerLevel level, BlockPos pos) {
        if (pos.getY() <= level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight() - 2) {
            return false;
        }
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());

        boolean airSpace = (feet.isAir() || feet.canBeReplaced()) && (head.isAir() || head.canBeReplaced());
        boolean solidGround = below.isSolid() && !below.isAir()
                && !below.is(Blocks.LAVA) && !below.is(Blocks.FIRE) && !below.is(Blocks.WATER);

        return airSpace && solidGround;
    }
}
