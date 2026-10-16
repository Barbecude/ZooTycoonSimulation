package net.zootycoon.claim;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.storage.LevelResource;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.network.ZooNetworkPayloads;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZooClaimManager {
    private static ZooClaimManager instance;
    private final Map<UUID, ZooClaim> claims = new ConcurrentHashMap<>();
    private MinecraftServer server;

    public ZooClaimManager() {
    }

    public static synchronized ZooClaimManager get(ServerLevel level) {
        if (instance == null || instance.server != level.getServer()) {
            instance = new ZooClaimManager();
            instance.server = level.getServer();
            instance.loadFromFile();
        }
        return instance;
    }

    public static synchronized void reset() {
        if (instance != null) {
            instance.claims.clear();
            instance.server = null;
            instance = null;
        }
    }

    private Path getSavePath() {
        return server.getWorldPath(LevelResource.ROOT).resolve("zoo_claims.dat");
    }

    public void loadFromFile() {
        if (server == null) return;
        Path path = getSavePath();
        if (!Files.exists(path)) return;

        try {
            CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            ListTag list = tag.getList("claims", Tag.TAG_COMPOUND);
            claims.clear();
            for (int i = 0; i < list.size(); i++) {
                ZooClaim claim = ZooClaim.fromNbt(list.getCompound(i));
                claims.put(claim.getClaimId(), claim);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToFile() {
        if (server == null) return;
        Path path = getSavePath();
        try {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (ZooClaim claim : claims.values()) {
                list.add(claim.toNbt());
            }
            tag.put("claims", list);
            NbtIo.writeCompressed(tag, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            if (claim.contains(pos.getX(), pos.getZ())) {
                return Optional.of(claim);
            }
        }
        return Optional.empty();
    }

    public boolean isClaimedBy(BlockPos pos, UUID playerUuid) {
        if (pos == null || playerUuid == null) return false;
        for (ZooClaim claim : claims.values()) {
            if (claim.getOwnerUuid().equals(playerUuid) && claim.contains(pos.getX(), pos.getZ())) {
                return true;
            }
        }
        return false;
    }

    public boolean isInsideAnyClaim(BlockPos pos) {
        if (pos == null) return false;
        for (ZooClaim claim : claims.values()) {
            if (claim.contains(pos.getX(), pos.getZ())) {
                return true;
            }
        }
        return false;
    }

    public boolean canPlayerModify(Player player, BlockPos pos) {
        MinecraftServer server = player.level().getServer();
        if (server != null && server.getPlayerList().isOp(player.getGameProfile())) {
            return true; // Admin bypass
        }
        Optional<ZooClaim> claimOpt = getClaimAt(pos);
        if (claimOpt.isEmpty()) {
            return true; // Wilderness / unclaimed land is editable by default
        }
        return claimOpt.get().getOwnerUuid().equals(player.getUUID());
    }

    public boolean tryClaimBox(ServerPlayer player, BlockPos pos1, BlockPos pos2) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        net.minecraft.world.level.levelgen.structure.BoundingBox box =
                new net.minecraft.world.level.levelgen.structure.BoundingBox(minX, -64, minZ, maxX, 320, maxZ);

        // 1. Check overlapping claims belonging to ANOTHER player
        for (ZooClaim existing : claims.values()) {
            if (!existing.getOwnerUuid().equals(player.getUUID())) {
                if (existing.intersects(box)) {
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.claim", "zootycoon.claim.overlap_owner", 3, existing.getOwnerName());
                    return false;
                }
            }
        }

        // 2. Calculate newly claimed horizontal blocks (overlapping owned claims are FREE!)
        List<ZooClaim> ownedClaims = getClaimsForPlayer(player.getUUID());
        int totalBlocks = (maxX - minX + 1) * (maxZ - minZ + 1);
        int newBlocks = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean alreadyOwned = false;
                for (ZooClaim c : ownedClaims) {
                    if (c.contains(x, z)) {
                        alreadyOwned = true;
                        break;
                    }
                }
                if (!alreadyOwned) {
                    newBlocks++;
                }
            }
        }

        // 3. 4 blocks = 1 emerald. If newBlocks == 0 (all inside owned claims) or creative mode, free!
        int cost = (newBlocks == 0) ? 0 : Math.max(1, (int) Math.ceil(newBlocks / 4.0));

        if (cost > 0 && !player.isCreative()) {
            if (!TransactionHelper.hasEnoughEmeralds(player, cost)) {
                int current = TransactionHelper.countEmeralds(player);
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.economy", "zootycoon.claim.not_enough_emeralds_detail", 3, cost, newBlocks, current);
                return false;
            }

            if (!TransactionHelper.consumeEmeralds(player, cost)) {
                return false;
            }
        }

        // 4. Merge overlapping/touching claims of this player so cubes NEVER stack!
        int finalMinX = minX;
        int finalMaxX = maxX;
        int finalMinZ = minZ;
        int finalMaxZ = maxZ;

        List<ZooClaim> toRemove = new ArrayList<>();
        for (ZooClaim existing : ownedClaims) {
            net.minecraft.world.level.levelgen.structure.BoundingBox eb = existing.getBox();
            if (existing.intersects(box) || !(eb.maxX() < minX - 1 || eb.minX() > maxX + 1 || eb.maxZ() < minZ - 1 || eb.minZ() > maxZ + 1)) {
                finalMinX = Math.min(finalMinX, eb.minX());
                finalMaxX = Math.max(finalMaxX, eb.maxX());
                finalMinZ = Math.min(finalMinZ, eb.minZ());
                finalMaxZ = Math.max(finalMaxZ, eb.maxZ());
                toRemove.add(existing);
            }
        }

        for (ZooClaim rem : toRemove) {
            claims.remove(rem.getClaimId());
        }

        net.minecraft.world.level.levelgen.structure.BoundingBox mergedBox =
                new net.minecraft.world.level.levelgen.structure.BoundingBox(finalMinX, -64, finalMinZ, finalMaxX, 320, finalMaxZ);
        ZooClaim newClaim = new ZooClaim(UUID.randomUUID(), player.getUUID(), player.getName().getString(), mergedBox, ClaimTier.TIER_1_SMALL, System.currentTimeMillis());
        claims.put(newClaim.getClaimId(), newClaim);
        saveToFile();
        syncToAll();

        if (cost == 0) {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.claim", "zootycoon.claim.expanded_free", 1);
        } else {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.claim", "zootycoon.claim.purchased_detail", 1, newBlocks, cost, totalBlocks - newBlocks);
        }
        return true;
    }

    public boolean tryClaim(ServerPlayer player, BlockPos center, ClaimTier tier) {
        ZooClaim newClaim = ZooClaim.create(player.getUUID(), player.getName().getString(), center, tier);

        // Check overlapping claims belonging to ANOTHER player
        for (ZooClaim existing : claims.values()) {
            if (!existing.getOwnerUuid().equals(player.getUUID()) && existing.intersects(newClaim)) {
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.claim", "zootycoon.claim.overlap", 3);
                return false;
            }
        }

        // Check and deduct emerald cost
        int cost = tier.getCostInEmeralds();
        if (!TransactionHelper.hasEnoughEmeralds(player, cost)) {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.economy", "zootycoon.claim.not_enough_emeralds", 3);
            return false;
        }

        if (!TransactionHelper.consumeEmeralds(player, cost)) {
            return false;
        }

        // Merge with existing touching/overlapping claims of this player
        List<ZooClaim> owned = getClaimsForPlayer(player.getUUID());
        var box = newClaim.getBox();
        int finalMinX = box.minX();
        int finalMaxX = box.maxX();
        int finalMinZ = box.minZ();
        int finalMaxZ = box.maxZ();

        List<ZooClaim> toRemove = new ArrayList<>();
        for (ZooClaim existing : owned) {
            var eb = existing.getBox();
            if (existing.intersects(box) || !(eb.maxX() < box.minX() - 1 || eb.minX() > box.maxX() + 1 || eb.maxZ() < box.minZ() - 1 || eb.minZ() > box.maxZ() + 1)) {
                finalMinX = Math.min(finalMinX, eb.minX());
                finalMaxX = Math.max(finalMaxX, eb.maxX());
                finalMinZ = Math.min(finalMinZ, eb.minZ());
                finalMaxZ = Math.max(finalMaxZ, eb.maxZ());
                toRemove.add(existing);
            }
        }

        for (ZooClaim rem : toRemove) {
            claims.remove(rem.getClaimId());
        }

        var mergedBox = new net.minecraft.world.level.levelgen.structure.BoundingBox(finalMinX, -64, finalMinZ, finalMaxX, 320, finalMaxZ);
        ZooClaim mergedClaim = new ZooClaim(UUID.randomUUID(), player.getUUID(), player.getName().getString(), mergedBox, tier, System.currentTimeMillis());
        claims.put(mergedClaim.getClaimId(), mergedClaim);
        saveToFile();
        syncToAll();

        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.claim", "zootycoon.claim.success", 1);
        return true;
    }

    public void syncToPlayer(ServerPlayer player) {
        List<ZooNetworkPayloads.ClaimData> list = new ArrayList<>();
        for (ZooClaim c : claims.values()) {
            list.add(new ZooNetworkPayloads.ClaimData(
                    c.getClaimId(), c.getOwnerUuid(), c.getOwnerName(),
                    c.getBox().minX(), c.getBox().minY(), c.getBox().minZ(),
                    c.getBox().maxX(), c.getBox().maxY(), c.getBox().maxZ(),
                    c.getTier().name()
            ));
        }
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncClaimsPayload(list));
    }

    public void syncToAll() {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }

    public List<ZooClaim> getPlayerClaims(UUID playerUuid) {
        return getClaimsForPlayer(playerUuid);
    }

    public static void initProtectionHooks() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel) {
                ZooClaimManager manager = get(serverLevel);
                if (!manager.canPlayerModify(player, pos)) {
                    if (player instanceof ServerPlayer sp) {
                        ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.claim", "zootycoon.claim.no_permission", 3);
                    }
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
                    if (player instanceof ServerPlayer sp) {
                        ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.claim", "zootycoon.claim.protected_area", 3);
                    }
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });

        // Block hostile monsters (Creeper, Zombie, Skeleton, Spider, etc.) from spawning inside any claimed zoo territory
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (isHostileMonster(entity)) {
                ZooClaimManager manager = get(world);
                if (manager.getClaimAt(entity.blockPosition()).isPresent()) {
                    entity.discard();
                }
            }
        });
    }

    public static boolean isHostileMonster(Entity entity) {
        if (entity == null) return false;
        if (entity instanceof net.zootycoon.entity.ZooHunterEntity) return false;
        if (entity.getTags().contains("zootycoon_staff")) return false;
        return entity instanceof Enemy || entity instanceof Monster;
    }

    /**
     * Ticked every second on the server: instantly banishes any hostile monsters that
     * somehow enter or wander into claimed zoo borders, keeping the zoo peaceful and safe!
     */
    public static void tickServer(ServerLevel level) {
        if (level.getGameTime() % 20 != 0) return; // Check every 1 second
        ZooClaimManager manager = get(level);
        for (ZooClaim claim : manager.getAllClaims()) {
            var box = claim.getBox();
            var aabb = new net.minecraft.world.phys.AABB(
                    box.minX(), box.minY(), box.minZ(),
                    box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1
            );
            List<LivingEntity> hostiles = level.getEntitiesOfClass(LivingEntity.class, aabb,
                    e -> isHostileMonster(e) && e.isAlive()
            );
            for (LivingEntity hostile : hostiles) {
                level.sendParticles(ParticleTypes.POOF, hostile.getX(), hostile.getY() + 0.5, hostile.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                level.playSound(null, hostile.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.6f, 1.2f);
                hostile.discard();
            }
        }
    }
}

