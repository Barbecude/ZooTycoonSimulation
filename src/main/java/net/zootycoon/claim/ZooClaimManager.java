package net.zootycoon.claim;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.zootycoon.economy.TransactionHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZooClaimManager extends SavedData {
    private static final String DATA_KEY = "zootycoon_claims";
    private final Map<UUID, ZooClaim> claims = new ConcurrentHashMap<>();

    public ZooClaimManager() {
    }

    public static ZooClaimManager get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new Factory<>(ZooClaimManager::new, ZooClaimManager::load, null),
                DATA_KEY
        );
    }

    public static ZooClaimManager load(CompoundTag tag, HolderLookup.Provider provider) {
        ZooClaimManager manager = new ZooClaimManager();
        ListTag list = tag.getList("claims", 10);
        for (int i = 0; i < list.size(); i++) {
            ZooClaim claim = ZooClaim.fromNbt(list.getCompound(i));
            manager.claims.put(claim.getClaimId(), claim);
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (ZooClaim claim : claims.values()) {
            list.add(claim.toNbt());
        }
        tag.put("claims", list);
        return tag;
    }

    public Collection<ZooClaim> getAllClaims() {
        return Collections.unmodifiableCollection(claims.values());
    }

    public List<ZooClaim> getClaimsForPlayer(UUID playerUuid) {
        List<ZooClaim> result = new ArrayList<>();
        for (ZooClaim claim : claims.values()) {
            if (claim.getOwnerUuid().equals(playerUuid)) {
                result.add(claim);
            }
        }
        return result;
    }

    public Optional<ZooClaim> getClaimAt(BlockPos pos) {
        for (ZooClaim claim : claims.values()) {
            if (claim.contains(pos)) {
                return Optional.of(claim);
            }
        }
        return Optional.empty();
    }

    public boolean canPlayerModify(Player player, BlockPos pos) {
        if (player.hasPermissions(2)) {
            return true; // Admin bypass
        }
        Optional<ZooClaim> claimOpt = getClaimAt(pos);
        if (claimOpt.isEmpty()) {
            return true; // Wilderness / unclaimed land is editable by default
        }
        return claimOpt.get().getOwnerUuid().equals(player.getUUID());
    }

    public boolean tryClaim(ServerPlayer player, BlockPos center, ClaimTier tier) {
        ZooClaim newClaim = ZooClaim.create(player.getUUID(), player.getName().getString(), center, tier);

        // Check overlapping claims
        for (ZooClaim existing : claims.values()) {
            if (existing.intersects(newClaim)) {
                player.displayClientMessage(
                        Component.translatable("zootycoon.claim.overlap").withStyle(ChatFormatting.RED),
                        true
                );
                return false;
            }
        }

        // Check and deduct emerald cost
        int cost = tier.getCostInEmeralds();
        if (!TransactionHelper.hasEnoughEmeralds(player, cost)) {
            player.displayClientMessage(
                    Component.translatable("zootycoon.claim.not_enough_emeralds").withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        if (!TransactionHelper.consumeEmeralds(player, cost)) {
            return false;
        }

        claims.put(newClaim.getClaimId(), newClaim);
        setDirty();

        player.displayClientMessage(
                Component.translatable("zootycoon.claim.success").withStyle(ChatFormatting.GREEN),
                true
        );
        return true;
    }

    public static void initProtectionHooks() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel) {
                ZooClaimManager manager = get(serverLevel);
                if (!manager.canPlayerModify(player, pos)) {
                    player.displayClientMessage(
                            Component.literal("Anda tidak memiliki izin di lahan kebun binatang ini!").withStyle(ChatFormatting.RED),
                            true
                    );
                    return false; // Prevent break
                }
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                ZooClaimManager manager = get(serverLevel);
                BlockPos pos = hitResult.getBlockPos();
                if (!manager.canPlayerModify(player, pos)) {
                    player.displayClientMessage(
                            Component.literal("Lahan ini dilindungi oleh pemilik zoo!").withStyle(ChatFormatting.RED),
                            true
                    );
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });
    }
}

