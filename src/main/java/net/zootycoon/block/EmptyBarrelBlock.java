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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EmptyBarrelBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final MapCodec<EmptyBarrelBlock> CODEC = simpleCodec(EmptyBarrelBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final Set<BlockPos> KNOWN_BARRELS = ConcurrentHashMap.newKeySet();

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 2, 16, 16),
            Block.box(2, 0, 2, 14, 2, 14),
            Block.box(2, 0, 0, 14, 16, 2),
            Block.box(14, 0, 0, 16, 16, 16),
            Block.box(2, 0, 14, 14, 16, 16)
    );

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
        return SHAPE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
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
            if (be instanceof EmptyBarrelBlockEntity barrelBe) {
                if (barrelBe.getOwnerUuid() == null) {
                    barrelBe.setOwnerUuid(sp.getUUID());
                }
                if (!barrelBe.isCategoryAssigned()) {
                    level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.8F, 1.0F);
                    ServerPlayNetworking.send(sp, new WarehousePayloads.OpenBarrelCategoryScreenPayload(pos));
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
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            KNOWN_BARRELS.remove(pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EmptyBarrelBlockEntity barrelBe) {
                Containers.dropContents(level, pos, barrelBe);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    public static void reset() {
        KNOWN_BARRELS.clear();
    }
}

