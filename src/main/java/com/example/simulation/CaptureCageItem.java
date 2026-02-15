package com.example.simulation;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CaptureCageItem extends Item {
    public CaptureCageItem() {
        super(new Item.Properties().stacksTo(1)); // Only 1 per slot
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
            InteractionHand hand) {
        Level level = player.level();

        // Check if cage is empty
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("CapturedEntity")) {
            return InteractionResult.PASS; // Cage full, do standard interaction (maybe hit?)
        }

        // Check size (Small mobs only: width <= 1.0 AND height <= 1.0)
        float width = target.getBbWidth();
        float height = target.getBbHeight();

        if (width > 1.0F || height > 1.0F) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("Mob terlalu besar! (Max 1x1 block)").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        // Capture logic
        CompoundTag entityTag = new CompoundTag();
        if (target.save(entityTag)) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
            if (id != null) {
                tag.putString("CapturedEntity", id.toString());
                tag.put("EntityData", entityTag);
                tag.putString("EntityName", target.getDisplayName().getString());

                // Remove from world
                target.discard();

                // Play sound
                level.playSound(null, player.blockPosition(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.PLAYERS, 1.0f,
                        1.0f);
                player.displayClientMessage(
                        Component.literal("Berhasil menangkap: " + target.getDisplayName().getString())
                                .withStyle(ChatFormatting.GREEN),
                        true);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("CapturedEntity")) {
            return InteractionResult.PASS; // Empty cage
        }

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        // Release logic
        String idStr = tag.getString("CapturedEntity");
        CompoundTag entityTag = tag.getCompound("EntityData");

        // Remove UUID to force new entity creation (prevents UUID conflict if original
        // still exists somehow)
        // Actually, we want to restore the SAME entity if possible, but discard()
        // invalidates it.
        // It uses a new UUID mostly.
        if (entityTag.contains("UUID")) {
            entityTag.remove("UUID");
        }

        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(idStr));
        if (type != null) {
            Entity entity = type.create(level);
            if (entity != null) {
                entity.load(entityTag); // Load captured data

                // Position (offset based on face)
                BlockPos spawnPos = pos.relative(face);
                entity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), 0);

                if (level.addFreshEntity(entity)) {
                    // Clear cage
                    stack.setTag(null);
                    level.playSound(null, spawnPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.displayClientMessage(Component.literal("Mob dilepaskan!").withStyle(ChatFormatting.GREEN),
                            true);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("EntityName")) {
            tooltip.add(Component.literal("Isi: " + tag.getString("EntityName")).withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.literal("Kosong (Klik kanan pada mob kecil)").withStyle(ChatFormatting.GRAY));
        }
    }
}
