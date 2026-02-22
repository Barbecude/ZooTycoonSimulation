package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.item.context.BlockPlaceContext;

public class OakShelfBlock extends ShelfBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    // WALL MOUNT VoxelShape for hanging shelf
    private static final VoxelShape WALL_SHAPE = Shapes.or(
        box(1, 8, 14, 15, 16, 15), // Back support piece
        box(2, 6, 13, 14, 8, 16),  // Main shelf shelf
        box(1, 5, 13.5, 2, 9, 15), // Left side support
        box(14, 5, 13.5, 15, 9, 15) // Right side support
    );

    public OakShelfBlock() {
        super();
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VARIANT, Variant.NORMAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, VARIANT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(VARIANT, Variant.NORMAL);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext ctx) {
        return WALL_SHAPE; // Wall-mounted shelf shape
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            updateVariantsAround(level, pos, state.getValue(FACING));
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide) {
            updateVariantsAround(level, pos, state.getValue(FACING));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            updateVariant(level, pos.relative(facing.getClockWise()));
            updateVariant(level, pos.relative(facing.getCounterClockWise()));
        }
    }

    private void updateVariantsAround(Level level, BlockPos pos, Direction facing) {
        updateVariant(level, pos);
        updateVariant(level, pos.relative(facing.getClockWise()));
        updateVariant(level, pos.relative(facing.getCounterClockWise()));
    }

    private void updateVariant(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof OakShelfBlock)) return;

        Direction facing = state.getValue(FACING);
        BlockPos leftPos = pos.relative(facing.getCounterClockWise());
        BlockPos rightPos = pos.relative(facing.getClockWise());

        boolean hasLeft = isConnectedShelf(level.getBlockState(leftPos), facing);
        boolean hasRight = isConnectedShelf(level.getBlockState(rightPos), facing);

        Variant variant = Variant.NORMAL;
        if (hasLeft && hasRight) {
            variant = Variant.CENTER;
        } else if (hasLeft) {
            variant = Variant.RIGHT;
        } else if (hasRight) {
            variant = Variant.LEFT;
        }

        if (state.getValue(VARIANT) != variant) {
            level.setBlock(pos, state.setValue(VARIANT, variant), 3);
        }
    }

    private boolean isConnectedShelf(BlockState state, Direction facing) {
        return state.getBlock() instanceof OakShelfBlock && state.getValue(FACING) == facing;
    }

    public enum Variant implements StringRepresentable {
        NORMAL("normal"),
        LEFT("left"),
        RIGHT("right"),
        CENTER("center");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
