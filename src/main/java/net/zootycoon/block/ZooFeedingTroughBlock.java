package net.zootycoon.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zootycoon.animal.AnimalDietRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ZooFeedingTroughBlock extends BaseEntityBlock {

    public static final MapCodec<ZooFeedingTroughBlock> CODEC = simpleCodec(ZooFeedingTroughBlock::new);
    public static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D);

    // Global spatial cache of trough positions for blazing fast, lag-free animal pathfinding!
    public static final Set<BlockPos> KNOWN_TROUGHS = ConcurrentHashMap.newKeySet();

    public static void reset() {
        KNOWN_TROUGHS.clear();
    }

    public ZooFeedingTroughBlock() {
        this(BlockBehaviour.Properties.of()
                .strength(2.0f, 3.0f)
                .sound(SoundType.WOOD)
                .noOcclusion());
    }

    public ZooFeedingTroughBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ZooFeedingTroughBlockEntity trough) {
                trough.clearContent();
                trough.setPlacedGameTime(level.getGameTime());
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        KNOWN_TROUGHS.add(pos.immutable());
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ZooFeedingTroughBlockEntity trough) {
                trough.clearContent();
                trough.setPlacedGameTime(level.getGameTime());
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            KNOWN_TROUGHS.remove(pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ZooFeedingTroughBlockEntity troughBe) {
                troughBe.dropInventory();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ZooFeedingTroughBlockEntity trough)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 1. Water Bucket Interaction
        if (stack.is(Items.WATER_BUCKET)) {
            if (!trough.hasWater() && trough.isEmpty()) {
                if (!level.isClientSide()) {
                    trough.setWaterLevel(3);
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.1, 0.2, 0.05);
                    }
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        // 2. Empty Bucket on Water Trough
        if (stack.is(Items.BUCKET) && trough.hasWater()) {
            if (!level.isClientSide()) {
                trough.setWaterLevel(0);
                if (!player.isCreative()) {
                    stack.shrink(1);
                    ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
                    if (!player.getInventory().add(waterBucket)) {
                        player.drop(waterBucket, false);
                    }
                }
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // 3. Shift-Right-Click with food to insert directly
        if (player.isShiftKeyDown() && !stack.isEmpty() && !trough.hasWater()) {
            boolean isAnimalFood = AnimalDietRegistry.isAnyFood(stack) || AnimalDietRegistry.isAnyTroughFood(stack);
            if (isAnimalFood) {
                boolean inserted = trough.insertFood(stack, stack.getCount());
                if (inserted) {
                    if (!level.isClientSide()) {
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }

        // 4. Normal Right-Click: Open Feeding Trough Selection Dialog Screen
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            String foodKey = trough.hasFood() ? net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(trough.getFoodStack().getItem()).toString() : "";
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp, new net.zootycoon.network.ZooNetworkPayloads.OpenFeedingTroughScreenPayload(
                    pos,
                    trough.getAssignedAnimalId(),
                    trough.getAssignedAnimalUuid() != null ? trough.getAssignedAnimalUuid().toString() : "",
                    trough.getAssignedAnimalName(),
                    trough.getAssignedAnimalSpecies(),
                    trough.getAssignedFoodItemId(),
                    trough.getFoodStack().getCount(),
                    foodKey,
                    trough.getWaterLevel()
            ));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ZooFeedingTroughBlockEntity trough) {
            if (player.isShiftKeyDown() && !trough.isEmpty() && !trough.hasWater()) {
                if (!level.isClientSide()) {
                    ItemStack removed = trough.extractFood(64);
                    if (!removed.isEmpty()) {
                        if (!player.getInventory().add(removed)) {
                            player.drop(removed, false);
                        }
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.0f);
                    }
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                String foodKey = trough.hasFood() ? net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(trough.getFoodStack().getItem()).toString() : "";
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp, new net.zootycoon.network.ZooNetworkPayloads.OpenFeedingTroughScreenPayload(
                        pos,
                        trough.getAssignedAnimalId(),
                        trough.getAssignedAnimalUuid() != null ? trough.getAssignedAnimalUuid().toString() : "",
                        trough.getAssignedAnimalName(),
                        trough.getAssignedAnimalSpecies(),
                        trough.getAssignedFoodItemId(),
                        trough.getFoodStack().getCount(),
                        foodKey,
                        trough.getWaterLevel()
                ));
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide());
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZooFeedingTroughBlockEntity(pos, state);
    }
}

