package net.zootycoon.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;
import net.zootycoon.block.entity.ZooThermalBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ZooHeaterBlock extends BaseEntityBlock {

    public static final MapCodec<ZooHeaterBlock> CODEC = simpleCodec(ZooHeaterBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(3, 16, 3, 13, 30, 13)
    );

    public ZooHeaterBlock(Properties properties) {
        super(properties.sound(SoundType.METAL).strength(3.5f).requiresCorrectToolForDrops().noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    public ZooHeaterBlock() {
        this(BlockBehaviour.Properties.of());
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZooThermalBlockEntity(pos, state, ZooThermalBlockEntity.ThermalType.HEATING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ZooBlocks.HEATER_BLOCK_ENTITY, ZooThermalBlockEntity::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        int fuelAmount = getFuelTicks(stack);
        if (fuelAmount > 0) {
            if (level.getBlockEntity(pos) instanceof ZooThermalBlockEntity thermal) {
                if (!level.isClientSide()) {
                    thermal.addFuel(fuelAmount);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                        if (stack.is(Items.LAVA_BUCKET)) {
                            player.addItem(new ItemStack(Items.BUCKET));
                        }
                    }
                    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    player.displayClientMessage(Component.translatable("zootycoon.heater.fueled", formatDuration(thermal.getFuelTime() / 20)), true);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof ZooThermalBlockEntity thermal) {
                int seconds = thermal.getFuelTime() / 20;
                if (seconds > 0) {
                    player.displayClientMessage(Component.translatable("zootycoon.heater.time_remaining", formatDuration(seconds)), true);
                } else {
                    player.displayClientMessage(Component.translatable("zootycoon.heater.empty"), true);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes > 0) {
            if (seconds > 0) {
                return minutes + " menit " + seconds + " detik";
            }
            return minutes + " menit";
        }
        return totalSeconds + " detik";
    }

    public static int getFuelTicks(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) return 2400; // 2 minutes (2400 ticks)
        if (stack.is(Items.COAL_BLOCK)) return 24000; // 20 minutes
        if (stack.is(Items.BLAZE_ROD)) return 3600; // 3 minutes
        if (stack.is(Items.LAVA_BUCKET)) return 24000; // 20 minutes
        if (stack.is(Items.STICK)) return 100;
        if (stack.is(net.minecraft.tags.ItemTags.PLANKS) || stack.is(net.minecraft.tags.ItemTags.LOGS)) return 600; // 30 seconds
        return 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            double x = pos.getX() + 0.5;
            double z = pos.getZ() + 0.5;
            if (random.nextDouble() < 0.25) {
                level.playLocalSound(x, pos.getY() + 0.5, z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5f, 1.0f, false);
            }
            // Smoke billowing from top of chimney
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, pos.getY() + 1.9, z, 0.0, 0.04, 0.0);
            // Grate fire and subtle smoke
            level.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5) * 0.2, pos.getY() + 0.45, z + (random.nextDouble() - 0.5) * 0.2, 0.0, 0.02, 0.0);
            level.addParticle(ParticleTypes.FLAME, x + (random.nextDouble() - 0.5) * 0.2, pos.getY() + 0.45, z + (random.nextDouble() - 0.5) * 0.2, 0.0, 0.01, 0.0);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }
}

