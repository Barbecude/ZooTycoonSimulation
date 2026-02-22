package com.example.simulation;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CaptureCageItem extends Item {
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(IndoZooTycoon.MODID);

    private static final int MAX_CAPTURE = 1;
    private static final int RELEASE_COOLDOWN_TICKS = 20;
    private static final String KEY_ID = "CapturedEntity";
    private static final String KEY_DATA = "EntityData";
    private static final String KEY_NAME = "EntityName";

    public CaptureCageItem() {
        super(new Item.Properties().stacksTo(1)); // Only 1 per slot
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getTag();
            if (hasCaptured(tag)) {
                boolean ok = tryRelease(level, player, stack, null);
                return ok ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
            InteractionHand hand) {
        Level level = player.level();

        if (target instanceof Player || target instanceof StaffEntity || target instanceof VisitorEntity) {
            return InteractionResult.FAIL;
        }

        float width = target.getBbWidth();
        float height = target.getBbHeight();

        if (width > 0.9F || height > 2.0F) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("Mob terlalu besar! (Max 0.9x2.0 block)").withStyle(ChatFormatting.RED),
                        true);
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = stack.getTag();
        if (hasCaptured(tag)) {
            boolean ok = tryRelease(level, player, stack, null);
            return ok ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        CompoundTag newTag = stack.getOrCreateTag();
        migrateLegacyToSingle(newTag);
        int count = hasCaptured(newTag) ? 1 : 0;
        if (count >= MAX_CAPTURE) {
            player.displayClientMessage(Component.literal("Kandang sudah berisi 1 mob.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        CompoundTag entityTag = new CompoundTag();
        if (!target.save(entityTag)) {
            return InteractionResult.FAIL;
        }

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (id == null) {
            return InteractionResult.FAIL;
        }

        newTag.putString(KEY_ID, id.toString());
        newTag.put(KEY_DATA, entityTag);
        newTag.putString(KEY_NAME, target.getDisplayName().getString());

        target.discard();
        level.playSound(null, player.blockPosition(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.25, 0.25, 0.25, 0.01);
        }
        player.displayClientMessage(Component.literal("Berhasil menangkap: " + newTag.getString(KEY_NAME)).withStyle(ChatFormatting.GREEN), true);
        LOGGER.info("CaptureCage CAPTURE player={} mob={} pos={}", player.getGameProfile().getName(), id, player.blockPosition());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos().relative(face);

        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        CompoundTag tag = stack.getTag();
        if (!hasCaptured(tag)) return InteractionResult.PASS;
        boolean ok = tryRelease(level, player, stack, pos);
        return ok ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        migrateLegacyToSingle(tag);
        if (hasCaptured(tag)) {
            tooltip.add(Component.literal("Isi: 1 mob").withStyle(ChatFormatting.GOLD));
            if (tag != null && tag.contains(KEY_NAME)) {
                tooltip.add(Component.literal(tag.getString(KEY_NAME)).withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(Component.literal("Klik kanan untuk melepas").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.literal("Kosong (Klik kanan pada mob kecil)").withStyle(ChatFormatting.GRAY));
    }

    private static void migrateLegacyToSingle(CompoundTag tag) {
        if (tag == null) return;
        if (tag.contains(KEY_ID) && tag.contains(KEY_DATA)) return;
        if (tag.contains("CapturedEntity_0")) {
            tag.putString(KEY_ID, tag.getString("CapturedEntity_0"));
            tag.put(KEY_DATA, tag.getCompound("EntityData_0"));
            tag.putString(KEY_NAME, tag.getString("EntityName_0"));
            tag.remove("CapturedEntity_0");
            tag.remove("EntityData_0");
            tag.remove("EntityName_0");
            tag.remove("CapturedCount");
            return;
        }
        if (tag.contains("CapturedEntity")) {
            tag.putString(KEY_ID, tag.getString("CapturedEntity"));
            tag.put(KEY_DATA, tag.getCompound("EntityData"));
            tag.putString(KEY_NAME, tag.getString("EntityName"));
            tag.remove("CapturedEntity");
            tag.remove("EntityData");
            tag.remove("EntityName");
        }
    }

    private static boolean hasCaptured(@Nullable CompoundTag tag) {
        if (tag == null) return false;
        migrateLegacyToSingle(tag);
        return tag.contains(KEY_ID) && tag.contains(KEY_DATA);
    }

    private boolean tryRelease(Level level, Player player, ItemStack stack, @Nullable BlockPos preferredPos) {
        if (player.getCooldowns().isOnCooldown(this)) return false;
        player.getCooldowns().addCooldown(this, RELEASE_COOLDOWN_TICKS);

        CompoundTag tag = stack.getTag();
        if (!hasCaptured(tag)) return false;
        String idStr = tag.getString(KEY_ID);
        CompoundTag entityTag = tag.getCompound(KEY_DATA);
        if (entityTag.contains("UUID")) {
            entityTag.remove("UUID");
        }

        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(idStr));
        if (type == null) {
            player.displayClientMessage(Component.literal("Mob tidak ditemukan.").withStyle(ChatFormatting.RED), true);
            stack.setTag(null);
            return false;
        }

        Entity entity = type.create(level);
        if (entity == null) {
            player.displayClientMessage(Component.literal("Gagal membuat mob.").withStyle(ChatFormatting.RED), true);
            return false;
        }

        entity.load(entityTag);

        Vec3 base = preferredPos != null
                ? new Vec3(preferredPos.getX() + 0.5, preferredPos.getY(), preferredPos.getZ() + 0.5)
                : player.position().add(player.getLookAngle().normalize().scale(1.2));

        Vec3 spawn = findSafeSpawn(level, entity, base);
        if (spawn == null) {
            player.displayClientMessage(Component.literal("Area pelepasan terhalang.").withStyle(ChatFormatting.RED), true);
            return false;
        }

        entity.moveTo(spawn.x, spawn.y, spawn.z, player.getYRot(), 0);
        if (!level.addFreshEntity(entity)) {
            player.displayClientMessage(Component.literal("Gagal melepas mob.").withStyle(ChatFormatting.RED), true);
            return false;
        }

        stack.setTag(null);
        level.playSound(null, BlockPos.containing(spawn.x, spawn.y, spawn.z), SoundEvents.IRON_DOOR_OPEN, SoundSource.PLAYERS, 1.0f, 1.1f);
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.POOF, spawn.x, spawn.y + 0.2, spawn.z, 12, 0.25, 0.25, 0.25, 0.02);
        }
        player.displayClientMessage(Component.literal("Mob dilepaskan!").withStyle(ChatFormatting.GREEN), true);
        LOGGER.info("CaptureCage RELEASE player={} mob={} pos={}", player.getGameProfile().getName(), idStr, BlockPos.containing(spawn.x, spawn.y, spawn.z));
        return true;
    }

    private static Vec3 findSafeSpawn(Level level, Entity entity, Vec3 base) {
        double bx = Math.floor(base.x) + 0.5;
        double by = Math.floor(base.y);
        double bz = Math.floor(base.z) + 0.5;

        int[] dx = new int[] {0, 1, -1, 0, 0, 2, -2, 1, -1, 0};
        int[] dz = new int[] {0, 0, 0, 1, -1, 0, 0, 1, -1, 2};
        int[] dy = new int[] {0, 1, -1, 2, -2};

        for (int yOff : dy) {
            double y = by + yOff;
            for (int i = 0; i < dx.length; i++) {
                double x = bx + dx[i];
                double z = bz + dz[i];
                BlockPos pos = BlockPos.containing(x, y, z);

                if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                    continue;
                }
                if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                    continue;
                }
                entity.setPos(x, y, z);
                if (level.noCollision(entity)) {
                    return new Vec3(x, y, z);
                }
            }
        }
        return null;
    }
}
