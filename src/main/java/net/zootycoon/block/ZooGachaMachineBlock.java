package net.zootycoon.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zootycoon.economy.ZooEconomyManager;
import net.zootycoon.entity.ZooCashierEntity;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.sound.ZooSounds;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ZooGachaMachineBlock extends BaseEntityBlock {

    public static final MapCodec<ZooGachaMachineBlock> CODEC = simpleCodec(ZooGachaMachineBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape LOWER_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape UPPER_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 11.0D, 16.0D);

    // Global spatial cache of lower block positions for visitor pathfinding
    public static final Set<BlockPos> KNOWN_MACHINES = ConcurrentHashMap.newKeySet();

    public static void reset() {
        KNOWN_MACHINES.clear();
    }

    public ZooGachaMachineBlock() {
        this(BlockBehaviour.Properties.of()
                .strength(2.5f, 3.0f)
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    public ZooGachaMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE : LOWER_SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        if (!level.isClientSide()) {
            KNOWN_MACHINES.add(pos.immutable());
            if (placer instanceof Player player) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ZooGachaMachineBlockEntity gachaBe) {
                    gachaBe.setOwnerUuid(player.getUUID());
                }
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            KNOWN_MACHINES.add(pos.immutable());
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.LOWER && direction == Direction.UP) {
                if (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.UPPER) {
                    return Blocks.AIR.defaultBlockState();
                }
            } else if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN) {
                if (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }
        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            DoubleBlockHalf half = state.getValue(HALF);
            if (half == DoubleBlockHalf.UPPER) {
                BlockPos lowerPos = pos.below();
                BlockState lowerState = level.getBlockState(lowerPos);
                if (lowerState.is(this) && lowerState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    BlockState airState = lowerState.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    level.setBlock(lowerPos, airState, 35);
                    level.levelEvent(player, 2001, lowerPos, Block.getId(lowerState));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                KNOWN_MACHINES.remove(pos);
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ZooGachaMachineBlockEntity gachaBe) {
                    gachaBe.dropInventory();
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockEntity be = level.getBlockEntity(lowerPos);
        if (!(be instanceof ZooGachaMachineBlockEntity gachaBe)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 1. Refill with Plushies
        if (!stack.isEmpty() && ZooCashierEntity.isPlushieItem(stack.getItem())) {
            if (gachaBe.canAccept(stack)) {
                if (!level.isClientSide()) {
                    int inserted = gachaBe.insertPlushies(stack);
                    if (inserted > 0) {
                        level.playSound(null, lowerPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.2f);
                        player.displayClientMessage(Component.translatable("zootycoon.gacha.refilled", inserted, gachaBe.getTotalStock()), true);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            } else {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("zootycoon.gacha.full"), true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        // 2. Play Gacha with Emerald
        if (stack.is(Items.EMERALD)) {
            if (gachaBe.isEmpty()) {
                if (!level.isClientSide()) {
                    level.playSound(null, lowerPos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0f, 1.0f);
                    player.displayClientMessage(Component.translatable("zootycoon.gacha.empty"), true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide() && level instanceof ServerLevel sl) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                java.util.UUID ownerUuid = gachaBe.getOwnerUuid();
                if (ownerUuid == null) {
                    for (net.zootycoon.claim.ZooClaim claim : net.zootycoon.claim.ZooClaimManager.get(sl).getAllClaims()) {
                        if (claim.contains(lowerPos.getX(), lowerPos.getZ())) {
                            ownerUuid = claim.getOwnerUuid();
                            break;
                        }
                    }
                }
                if (ownerUuid != null && sl.getServer() != null) {
                    var eco = ZooEconomyManager.get(sl.getServer());
                    ServerPlayer owner = sl.getServer().getPlayerList().getPlayer(ownerUuid);
                    eco.addBalance(ownerUuid, 1, owner);
                }

                ItemStack wonPlushie = gachaBe.rollRandomPlushie();
                if (!wonPlushie.isEmpty()) {
                    if (!player.getInventory().add(wonPlushie)) {
                        player.drop(wonPlushie, false);
                    }
                }

                sl.playSound(null, lowerPos, ZooSounds.KACHING, SoundSource.BLOCKS, 1.0f, 1.0f);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, lowerPos.getX() + 0.5, lowerPos.getY() + 1.2, lowerPos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.05);
                sl.sendParticles(ParticleTypes.END_ROD, lowerPos.getX() + 0.5, lowerPos.getY() + 0.5, lowerPos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.03);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // 3. Shift-Click with empty hand: Withdraw 1 plushie
        if (player.isShiftKeyDown() && stack.isEmpty()) {
            if (!gachaBe.isEmpty()) {
                if (!level.isClientSide()) {
                    ItemStack withdrawn = gachaBe.extractPlushie();
                    if (!withdrawn.isEmpty()) {
                        if (!player.getInventory().add(withdrawn)) {
                            player.drop(withdrawn, false);
                        }
                        level.playSound(null, lowerPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.0f);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        // 4. Default interaction: Open Gacha Dialog Screen
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            ServerPlayNetworking.send(sp, new ZooNetworkPayloads.OpenGachaMachineScreenPayload(
                    lowerPos,
                    gachaBe.getTotalStock(),
                    gachaBe.getSlotsList()
            ));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockEntity be = level.getBlockEntity(lowerPos);
        if (be instanceof ZooGachaMachineBlockEntity gachaBe) {
            if (player.isShiftKeyDown() && !gachaBe.isEmpty()) {
                if (!level.isClientSide()) {
                    ItemStack withdrawn = gachaBe.extractPlushie();
                    if (!withdrawn.isEmpty()) {
                        if (!player.getInventory().add(withdrawn)) {
                            player.drop(withdrawn, false);
                        }
                        level.playSound(null, lowerPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.0f);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                ServerPlayNetworking.send(sp, new ZooNetworkPayloads.OpenGachaMachineScreenPayload(
                        lowerPos,
                        gachaBe.getTotalStock(),
                        gachaBe.getSlotsList()
                ));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return new ZooGachaMachineBlockEntity(pos, state);
        }
        return null;
    }
}

