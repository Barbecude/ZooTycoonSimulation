package net.zootycoon.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zootycoon.entity.ToiletSeatEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooVisitorEntity;

import java.util.List;

public class ZooSeatHelper {

    /**
     * Checks if a block is a seatable object (chairs, benches, stools, sofas, seats from any mod).
     */
    public static boolean isSeatableBlock(Level level, BlockPos pos, BlockState state) {
        if (state == null || state.isAir()) return false;

        // Exclude toilets (toilets are handled by FindToiletGoal)
        if (state.is(net.zootycoon.block.ZooBlocks.TOILET)) return false;

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = blockId.getPath().toLowerCase();

        // 1. Check ID keywords (covering Another Furniture, Handcrafted, Adorn, Refurbished Furniture, Cluttered, etc.)
        if (path.contains("chair")
                || path.contains("bench")
                || path.contains("sofa")
                || path.contains("couch")
                || path.contains("stool")
                || path.contains("armchair")
                || path.contains("seat")
                || path.contains("lounger")
                || path.contains("throne")
                || path.contains("chaise")
                || path.contains("ottoman")
                || path.contains("pouffe")) {
            return isSeatNotOccupied(level, pos);
        }

        // 2. Check tags (e.g. #c:seats, #c:chairs, #furniture:seats, #another_furniture:chairs, etc.)
        boolean hasSeatTag = state.getTags().anyMatch(tag -> {
            String tagPath = tag.location().getPath().toLowerCase();
            return tagPath.contains("seat")
                    || tagPath.contains("chair")
                    || tagPath.contains("bench")
                    || tagPath.contains("sofa")
                    || tagPath.contains("couch")
                    || tagPath.contains("stool")
                    || tagPath.contains("sit");
        });
        if (hasSeatTag) {
            return isSeatNotOccupied(level, pos);
        }

        // 3. Check Class name
        String className = state.getBlock().getClass().getSimpleName().toLowerCase();
        if (className.contains("chair")
                || className.contains("seat")
                || className.contains("bench")
                || className.contains("sofa")
                || className.contains("couch")
                || className.contains("stool")) {
            return isSeatNotOccupied(level, pos);
        }

        // 4. Vanilla stairs (when used as seats, lower half)
        if (state.getBlock() instanceof StairBlock) {
            if (state.hasProperty(StairBlock.HALF) && state.getValue(StairBlock.HALF) == Half.BOTTOM) {
                if (level.getBlockState(pos.above()).isAir()) {
                    return isSeatNotOccupied(level, pos);
                }
            }
        }

        return false;
    }

    public static boolean isSeatNotOccupied(Level level, BlockPos pos) {
        // Space above the seat must be open (air or passable)
        BlockState above = level.getBlockState(pos.above());
        if (!above.isAir() && above.isSolid()) return false;

        // Check if any entity is already sitting on this seat
        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0).inflate(0.1);
        List<Entity> occupants = level.getEntitiesOfClass(Entity.class, box, e -> e.isPassenger() || e.hasPassenger(passenger -> true) || (e instanceof ToiletSeatEntity));
        return occupants.isEmpty();
    }

    /**
     * Calculates the true seat surface Y coordinate of any seatable block
     * (chairs, benches, stools, sofas, stairs, slabs) using the block's own definition
     * or collision shape, matching where a vanilla Minecraft player sits.
     */
    public static double getSeatSurfaceY(Level level, BlockPos pos, BlockState state) {
        // 1. Check if the block has seatHeight(BlockState) (e.g. Another Furniture SeatBlock, Refurbished Furniture)
        try {
            java.lang.reflect.Method m = state.getBlock().getClass().getMethod("seatHeight", BlockState.class);
            Object res = m.invoke(state.getBlock(), state);
            if (res instanceof Number num) {
                return pos.getY() + num.doubleValue();
            }
        } catch (Exception ignored) {
        }

        // 2. Vanilla stairs: the seat step is at half-block (0.5)
        if (state.getBlock() instanceof StairBlock) {
            return pos.getY() + 0.5D;
        }

        // 3. Slabs: bottom slab surface is at 0.5
        if (state.getBlock() instanceof net.minecraft.world.level.block.SlabBlock) {
            return pos.getY() + 0.5D;
        }

        // 4. Physical collision shape
        VoxelShape collision = state.getCollisionShape(level, pos);
        if (!collision.isEmpty()) {
            double maxY = collision.max(Direction.Axis.Y);
            // If it's a flat stool/bench/ottoman with no backrest (maxY between 0.2 and 0.75)
            if (maxY >= 0.2D && maxY <= 0.75D) {
                return pos.getY() + maxY;
            }

            // If the chair has a backrest (maxY > 0.75), search for the cushion top
            double bestCushionY = -1.0D;
            for (AABB box : collision.toAabbs()) {
                if (box.maxY >= 0.2D && box.maxY <= 0.75D) {
                    if (box.maxY > bestCushionY) {
                        bestCushionY = box.maxY;
                    }
                }
            }
            if (bestCushionY > 0) {
                return pos.getY() + bestCushionY;
            }
        }

        // Default standard chair sitting height
        return pos.getY() + 0.5D;
    }

    /**
     * Seats a visitor onto a seatable block or existing seat entity.
     */
    public static boolean seatVisitorAt(ServerLevel level, BlockPos pos, ZooVisitorEntity visitor) {
        if (!isSeatNotOccupied(level, pos)) return false;

        BlockState state = level.getBlockState(pos);

        // Determine rotation based on block facing
        float yRot = visitor.getYRot();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            yRot = state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
        } else if (state.hasProperty(BlockStateProperties.FACING)) {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            if (dir.getAxis().isHorizontal()) {
                yRot = dir.toYRot();
            }
        }

        // Determine seating height based on true seat surface (matching player source of truth)
        double seatY = getSeatSurfaceY(level, pos, state);

        // Create invisible seat entity
        ToiletSeatEntity seat = new ToiletSeatEntity(ZooEntities.TOILET_SEAT, level);
        seat.setSeatBlockPos(pos);
        seat.setPos(pos.getX() + 0.5D, seatY, pos.getZ() + 0.5D);
        seat.setYRot(yRot);
        level.addFreshEntity(seat);

        visitor.setYRot(yRot);
        visitor.setYHeadRot(yRot);
        visitor.setYBodyRot(yRot);
        visitor.startRiding(seat, true);

        level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.PLAYERS, 0.6F, 1.0F);
        return true;
    }
}
