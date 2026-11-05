package net.zootycoon.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zootycoon.network.WarehousePayloads;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EmptyBarrelBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final MapCodec<EmptyBarrelBlock> CODEC = simpleCodec(EmptyBarrelBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    // Two barrels side by side join like a double chest (LEFT/RIGHT halves)
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;

    public static final Set<BlockPos> KNOWN_BARRELS = ConcurrentHashMap.newKeySet();

    private static final VoxelShape SHAPE = buildShape(null);
    // Shapes of a joined half, keyed by the side that opens into the other half
    private static final Map<Direction, VoxelShape> OPEN_SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            OPEN_SHAPES.put(dir, buildShape(dir));
        }
    }

    /** Open-top crate: floor plus four walls, leaving out the wall on the open side. */
    private static VoxelShape buildShape(@Nullable Direction open) {
        double minX = open == Direction.WEST ? 0 : 2;
        double maxX = open == Direction.EAST ? 16 : 14;
        double minZ = open == Direction.NORTH ? 0 : 2;
        double maxZ = open == Direction.SOUTH ? 16 : 14;
        VoxelShape shape = Block.box(minX, 0, minZ, maxX, 2, maxZ);
        if (open != Direction.WEST) shape = Shapes.or(shape, Block.box(0, 0, 0, 2, 16, 16));
        if (open != Direction.EAST) shape = Shapes.or(shape, Block.box(14, 0, 0, 16, 16, 16));
        if (open != Direction.NORTH) shape = Shapes.or(shape, Block.box(minX, 0, 0, maxX, 16, 2));
        if (open != Direction.SOUTH) shape = Shapes.or(shape, Block.box(minX, 0, 14, maxX, 16, 16));
        return shape;
    }

    /** Direction from this half toward the other half; same rule as the vanilla double chest. */
    public static Direction getConnectedDirection(BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(TYPE) == ChestType.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
    }

    public EmptyBarrelBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.5F)
                .sound(SoundType.WOOD)
                .noOcclusion());
    }

    public EmptyBarrelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(TYPE) == ChestType.SINGLE) {
            return SHAPE;
        }
        return OPEN_SHAPES.get(getConnectedDirection(state));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        ChestType type = ChestType.SINGLE;
        // Sneaking places a single barrel, like a chest
        if (!context.isSecondaryUseActive()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            if (canJoin(level.getBlockState(pos.relative(facing.getClockWise())), facing)) {
                type = ChestType.LEFT;
            } else if (canJoin(level.getBlockState(pos.relative(facing.getCounterClockWise())), facing)) {
                type = ChestType.RIGHT;
            }
        }
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(TYPE, type)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    private boolean canJoin(BlockState neighbor, Direction facing) {
        return neighbor.is(this) && neighbor.getValue(TYPE) == ChestType.SINGLE && neighbor.getValue(FACING) == facing;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (neighborState.is(this) && direction.getAxis().isHorizontal()) {
            ChestType neighborType = neighborState.getValue(TYPE);
            if (state.getValue(TYPE) == ChestType.SINGLE && neighborType != ChestType.SINGLE
                    && state.getValue(FACING) == neighborState.getValue(FACING)
                    && getConnectedDirection(neighborState) == direction.getOpposite()) {
                return state.setValue(TYPE, neighborType.getOpposite());
            }
        } else if (state.getValue(TYPE) != ChestType.SINGLE && getConnectedDirection(state) == direction) {
            // The other half is gone
            return state.setValue(TYPE, ChestType.SINGLE);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, WATERLOGGED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EmptyBarrelBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ZooBlocks.EMPTY_BARREL_BLOCK_ENTITY, EmptyBarrelBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer sp && level instanceof ServerLevel sLevel) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EmptyBarrelBlockEntity clicked) {
                // Both halves of a double barrel share one inventory, category and request list
                EmptyBarrelBlockEntity barrelBe = clicked.primary();
                if (barrelBe.getOwnerUuid() == null) {
                    barrelBe.setOwnerUuid(sp.getUUID());
                }
                if (!barrelBe.isCategoryAssigned()) {
                    level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.8F, 1.0F);
                    ServerPlayNetworking.send(sp, new WarehousePayloads.OpenBarrelCategoryScreenPayload(barrelBe.getBlockPos()));
                } else {
                    barrelBe.openChestForPlayer(sp, sLevel);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            KNOWN_BARRELS.add(pos.immutable());
            // Joining or splitting changes the slot layout, so open screens on either half are closed
            boolean typeChanged = oldState.is(this) && oldState.getValue(TYPE) != state.getValue(TYPE);
            if (typeChanged || state.getValue(TYPE) != ChestType.SINGLE) {
                closeNearbyBarrelScreens(level, pos);
            }
        }
    }

    private static void closeNearbyBarrelScreens(Level level, BlockPos pos) {
        for (Player player : level.players()) {
            if (player instanceof ServerPlayer sp
                    && player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu menu
                    && menu.getContainer() instanceof EmptyBarrelBlockEntity barrel
                    && barrel.getBlockPos().distManhattan(pos) <= 1) {
                sp.closeContainer();
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            KNOWN_BARRELS.remove(pos);
            if (!level.isClientSide()) {
                closeNearbyBarrelScreens(level, pos);
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EmptyBarrelBlockEntity barrelBe) {
                // Only this half's own slots; the other half keeps its items
                Containers.dropContents(level, pos, barrelBe.getOwnItems());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    public static void reset() {
        KNOWN_BARRELS.clear();
    }
}

