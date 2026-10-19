package net.zootycoon.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.block.ZooFeedingTroughBlockEntity;

/**
 * Watering Can Item ported and adapted from Dew Drop: Watering Cans by coolbot100s.
 * Supports continuous swinging/pouring animation when right-click is held down,
 * water refilling, farmland hydrating, crop bonemealing, mud creation,
 * fire dousing, feeding trough filling, and charged "Super" area watering when crouching.
 */
public class WateringCanItem extends Item {

    private final int cooldown;
    private final int requiredCharge;
    private final int maxSuperLevel;
    private final int chargePerLevel;
    private final boolean isNetherite;
    private static final int USE_DURATION = 72000;

    public WateringCanItem(Properties properties, int requiredCharge, int cooldown, int maxSuperLevel) {
        this(properties, requiredCharge, cooldown, maxSuperLevel, false);
    }

    public WateringCanItem(Properties properties, int requiredCharge, int cooldown, int maxSuperLevel, boolean isNetherite) {
        super(properties);
        this.requiredCharge = requiredCharge;
        this.cooldown = cooldown;
        this.maxSuperLevel = maxSuperLevel;
        this.chargePerLevel = maxSuperLevel >= 1 ? requiredCharge / maxSuperLevel : 0;
        this.isNetherite = isNetherite;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getMaxSuperLevel() {
        return maxSuperLevel;
    }

    public int getContinuousInterval() {
        if (isNetherite) return 4;
        if (maxSuperLevel >= 3) return 6; // diamond
        if (maxSuperLevel >= 2) return 8; // gold
        if (maxSuperLevel >= 1) return 10; // iron
        return 12; // copper
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Azure/blue water color
        return Mth.hsvToRgb(0.60F, 1.0F, 1.0F);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity livingEntity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Can't use while already using
        if (player.isUsingItem()) {
            return InteractionResultHolder.pass(stack);
        }

        // Can't use if on cooldown
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        // Raycast target block
        BlockPos pos = blockHighlightedOrNull(player);

        // Instant refill when clicking on water
        if (pos != null) {
            BlockState blockState = level.getBlockState(pos);
            if (blockState.getFluidState().is(FluidTags.WATER) || blockState.is(Blocks.WATER)) {
                if (!level.isClientSide()) {
                    stack.setDamageValue(0);
                    level.playSound(null, player.getOnPos(), SoundEvents.HONEY_BLOCK_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.getCooldowns().addCooldown(this, cooldown);
                }
                player.swing(hand);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        // Empty check
        if (stack.getDamageValue() >= stack.getMaxDamage() && !player.isCreative()) {
            if (!level.isClientSide()) {
                player.playNotifySound(SoundEvents.LANTERN_FALL, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.getCooldowns().addCooldown(this, cooldown);
            }
            return InteractionResultHolder.fail(stack);
        }

        // Start using so holding right-click enters a continuous smooth swaying/swinging animation
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int timeLeft) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        int useDuration = this.getUseDuration(stack, livingEntity) - timeLeft;

        // MODE 1: Crouch + Super Ability (Iron+)
        if (player.isCrouching() && maxSuperLevel >= 1) {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            // Sound cue on charging each super level
            if (chargePerLevel > 0 && (useDuration == chargePerLevel || (useDuration % chargePerLevel == 0 && useDuration <= chargePerLevel * maxSuperLevel && useDuration > 0))) {
                serverPlayer.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            }

            // Particle preview every 8 ticks
            if (useDuration % 8 != 0) {
                return;
            }

            int superLevelToUse = chargePerLevel > 0 ? Math.floorDiv(useDuration, chargePerLevel) : 0;
            if (superLevelToUse > maxSuperLevel) {
                superLevelToUse = maxSuperLevel;
            }

            if (superLevelToUse <= 0) {
                return;
            }

            BlockPos pos = blockHighlightedOrNull(player);
            if (pos == null) {
                return;
            }

            AABB area = getAreaForSuper(superLevelToUse, player, pos);
            ServerLevel serverLevel = (ServerLevel) level;

            BlockPos.betweenClosedStream(area).forEach(bp -> {
                double yoff;
                if (level.getBlockState(bp).isAir()) {
                    double shapeMax = level.getBlockState(bp.below()).getShape(level, bp.below()).isEmpty() ? 0.0 : level.getBlockState(bp.below()).getShape(level, bp.below()).max(Direction.Axis.Y);
                    yoff = -1.0 + shapeMax + 0.1;
                } else {
                    double shapeMax = level.getBlockState(bp).getShape(level, bp).isEmpty() ? 1.0 : level.getBlockState(bp).getShape(level, bp).max(Direction.Axis.Y);
                    yoff = shapeMax + 0.1;
                }
                serverLevel.sendParticles(ParticleTypes.COMPOSTER, bp.getX() + 0.5, bp.getY() + yoff, bp.getZ() + 0.5, 1, 0, 0, 0, 0);
            });
            return;
        }

        // MODE 2: Normal Continuous Watering (smooth swinging animation while holding right click)
        int interval = getContinuousInterval();
        if (useDuration == 1 || (useDuration > 1 && useDuration % interval == 0)) {
            // Looking downwards check
            if (player.getLookAngle().y > 0) {
                return;
            }

            BlockPos pos = blockHighlightedOrNull(player);
            if (pos == null) {
                return;
            }

            // Empty check
            if (stack.getDamageValue() >= stack.getMaxDamage() && !player.isCreative()) {
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.playNotifySound(SoundEvents.LANTERN_FALL, SoundSource.PLAYERS, 1.0f, 1.0f);
                    serverPlayer.getCooldowns().addCooldown(this, cooldown);
                }
                player.stopUsingItem();
                return;
            }

            // Obstructed by solid block above
            if (level.getBlockState(pos.above()).isSolidRender(level, pos.above())) {
                return;
            }

            BlockState blockState = level.getBlockState(pos);

            // Refill if pointing at water
            if (blockState.getFluidState().is(FluidTags.WATER) || blockState.is(Blocks.WATER)) {
                if (!level.isClientSide()) {
                    stack.setDamageValue(0);
                    level.playSound(null, player.getOnPos(), SoundEvents.HONEY_BLOCK_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return;
            }

            // Server-side pouring
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
                pourWater(serverLevel, serverPlayer, stack, pos, blockState, false);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int useDuration = this.getUseDuration(stack, entity) - timeLeft;

        // If player was crouching with a super capable can and charged at least 1 level:
        if (player.isCrouching() && maxSuperLevel >= 1 && chargePerLevel > 0 && useDuration >= chargePerLevel) {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (player.getLookAngle().y > 0) {
                return;
            }

            BlockPos pos = blockHighlightedOrNull(player);
            if (pos == null) {
                return;
            }

            int superLevelToUse = Math.min(Math.floorDiv(useDuration, chargePerLevel), maxSuperLevel);
            AABB area = getAreaForSuper(superLevelToUse, player, pos);

            int areaSize = (int) BlockPos.betweenClosedStream(area).count();
            int waterLeft = stack.getMaxDamage() - stack.getDamageValue();

            if (areaSize > waterLeft && !player.isCreative()) {
                serverPlayer.playNotifySound(SoundEvents.LANTERN_FALL, SoundSource.PLAYERS, 1.0f, 1.0f);
                serverPlayer.getCooldowns().addCooldown(this, cooldown);
                return;
            }

            ServerLevel serverLevel = (ServerLevel) level;
            BlockPos.betweenClosedStream(area).forEach(bp -> {
                BlockState state = level.getBlockState(bp);
                pourWater(serverLevel, serverPlayer, stack, bp, state, true);
            });

            if (!player.isCreative()) {
                stack.setDamageValue(stack.getDamageValue() + areaSize);
            }

            serverPlayer.getCooldowns().addCooldown(this, cooldown);
            return;
        }

        // Normal mode release: apply small cooldown so clicking doesn't flutter
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCooldowns().addCooldown(this, 5);
        }
    }

    public void pourWater(ServerLevel level, ServerPlayer player, ItemStack stack, BlockPos pos, BlockState state, boolean isSuper) {
        if (!isSuper) {
            if (!player.isCreative()) {
                stack.setDamageValue(stack.getDamageValue() + 1);
            }
        }

        // Nether check: only netherite watering can functions in the Nether
        if (level.dimension().equals(ServerLevel.NETHER) && !isNetherite) {
            double maxY = state.getShape(level, pos).isEmpty() ? 1.0 : state.getShape(level, pos).max(Direction.Axis.Y);
            for (int i = 0; i < 8; i++) {
                level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + maxY, pos.getZ() + 0.5, 0, 0, 0, 0, 1);
            }
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 3.0f);
            return;
        }

        if (isSuper && state.isAir()) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }

        // 1. Zoo Feeding Trough interaction
        if (level.getBlockEntity(pos) instanceof ZooFeedingTroughBlockEntity trough) {
            if (!trough.hasWater() && trough.isEmpty()) {
                trough.setWaterLevel(3);
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
        // 2. Farmland hydration
        else if (state.is(Blocks.FARMLAND)) {
            if (state.hasProperty(FarmBlock.MOISTURE) && state.getValue(FarmBlock.MOISTURE) < 7) {
                level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, 7), 3);
            }
        // 3. Crop on top of farmland
        } else if (state.is(BlockTags.CROPS) && level.getBlockState(pos.below()).is(Blocks.FARMLAND)) {
            BlockPos farmPos = pos.below();
            BlockState farmState = level.getBlockState(farmPos);
            if (farmState.hasProperty(FarmBlock.MOISTURE) && farmState.getValue(FarmBlock.MOISTURE) < 7) {
                level.setBlock(farmPos, farmState.setValue(FarmBlock.MOISTURE, 7), 3);
            }
        // 4. Dirt to Mud conversion (25% chance)
        } else if ((state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) && level.random.nextInt(100) < 25) {
            level.setBlock(pos, Blocks.MUD.defaultBlockState(), 3);
            level.playSound(null, pos, SoundEvents.MUD_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        // 5. Fire extinguishing
        } else if (state.is(BlockTags.CAMPFIRES) && state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 3);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
        } else if (state.is(BlockTags.FIRE)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        // Sound effect
        level.playSound(null, pos, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 1.0f, 3.0f);

        // Water rain particles
        double maxY = state.getShape(level, pos).isEmpty() ? 1.0 : state.getShape(level, pos).max(Direction.Axis.Y);
        for (int i = 0; i < 15; i++) {
            double offsetX = level.random.nextDouble() - 0.5;
            double offsetZ = level.random.nextDouble() - 0.5;
            level.sendParticles(ParticleTypes.RAIN, pos.getX() + 0.5 + offsetX, pos.getY() + maxY, pos.getZ() + 0.5 + offsetZ, 1, 0, 0, 0, 0);
        }

        // Bonemeal mechanic (25% chance)
        if (level.random.nextInt(100) < 25) {
            if (state.is(BlockTags.CROPS) && state.getBlock() instanceof CropBlock crop) {
                crop.performBonemeal(level, level.getRandom(), pos, state);
            } else {
                BlockPos above = pos.above();
                BlockState aboveState = level.getBlockState(above);
                if (aboveState.is(BlockTags.CROPS) && aboveState.getBlock() instanceof CropBlock crop) {
                    crop.performBonemeal(level, level.getRandom(), above, aboveState);
                }
            }
        }
    }

    public static AABB getAreaForSuper(int superLevelToUse, Player player, BlockPos pos) {
        int forward = 0;
        int back = 0;
        int left = 0;
        int right = 0;

        switch (superLevelToUse) {
            case 1 -> {
                forward = 2; back = 0; left = 0; right = 0;
            }
            case 2 -> {
                forward = 2; back = 0; left = 1; right = 1;
            }
            case 3 -> {
                forward = 4; back = 0; left = 1; right = 1;
            }
            case 4 -> {
                forward = 4; back = 0; left = 2; right = 2;
            }
            default -> {}
        }

        Direction facing = player.getDirection();
        BlockPos start = pos;
        BlockPos end = pos;

        switch (facing) {
            case NORTH -> {
                start = pos.offset(-left, 0, -forward);
                end = pos.offset(right, 0, back);
            }
            case SOUTH -> {
                start = pos.offset(-right, 0, -back);
                end = pos.offset(left, 0, forward);
            }
            case WEST -> {
                start = pos.offset(-forward, 0, right);
                end = pos.offset(back, 0, -left);
            }
            case EAST -> {
                start = pos.offset(forward, 0, -right);
                end = pos.offset(-back, 0, left);
            }
            default -> {}
        }

        return new AABB(Vec3.atLowerCornerOf(start), Vec3.atLowerCornerOf(end));
    }

    public static BlockPos blockHighlightedOrNull(Player player) {
        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        HitResult hitResult = player.pick(reach, 1.0F, true);
        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult bhr) {
            return bhr.getBlockPos();
        }
        return null;
    }
}
