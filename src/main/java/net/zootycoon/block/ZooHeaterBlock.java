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
import net.minecraft.world.phys.BlockHitResult;
import net.zootycoon.block.entity.ZooThermalBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ZooHeaterBlock extends BaseEntityBlock {

    public static final MapCodec<ZooHeaterBlock> CODEC = simpleCodec(ZooHeaterBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ZooHeaterBlock(Properties properties) {
        super(properties.sound(SoundType.METAL).strength(3.5f).requiresCorrectToolForDrops());
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
                    player.displayClientMessage(Component.translatable("zootycoon.heater.fueled", thermal.getFuelTime() / 20), true);
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
                    player.displayClientMessage(Component.translatable("zootycoon.heater.time_remaining", seconds), true);
                } else {
                    player.displayClientMessage(Component.translatable("zootycoon.heater.empty"), true);
                }
            }
        }
        return InteractionResult.SUCCESS;
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
            double y = pos.getY() + 0.85;
            double z = pos.getZ() + 0.5;
            if (random.nextDouble() < 0.25) {
                level.playLocalSound(x, y, z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5f, 1.0f, false);
            }
            level.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5) * 0.2, y, z + (random.nextDouble() - 0.5) * 0.2, 0.0, 0.05, 0.0);
            level.addParticle(ParticleTypes.FLAME, x + (random.nextDouble() - 0.5) * 0.2, y, z + (random.nextDouble() - 0.5) * 0.2, 0.0, 0.01, 0.0);
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

