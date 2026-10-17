package net.zootycoon.hunter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooHunterEntity;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZooHunterManager {
    private static long lastCheckTick = 0L;
    // Tracks temporary dirt blocks placed by stuck hunters -> expire gameTime tick (10s = 200 ticks)
    private static final Map<GlobalPos, Long> TEMP_DIRT_BLOCKS = new ConcurrentHashMap<>();

    public static void placeTempDirt(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) return;
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.canBeReplaced()) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.8f, 1.0f);
            GlobalPos gPos = GlobalPos.of(level.dimension(), pos.immutable());
            TEMP_DIRT_BLOCKS.put(gPos, level.getGameTime() + 200L); // 10 seconds
        }
    }

    private static void tickTempDirtCleanup(ServerLevel level) {
        if (TEMP_DIRT_BLOCKS.isEmpty()) return;
        long now = level.getGameTime();
        Iterator<Map.Entry<GlobalPos, Long>> it = TEMP_DIRT_BLOCKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<GlobalPos, Long> entry = it.next();
            GlobalPos gPos = entry.getKey();
            if (!gPos.dimension().equals(level.dimension())) continue;
            if (now >= entry.getValue()) {
                BlockPos pos = gPos.pos();
                if (level.getBlockState(pos).is(Blocks.DIRT)) {
                    level.levelEvent(2001, pos, Block.getId(Blocks.DIRT.defaultBlockState()));
                    level.removeBlock(pos, false);
                }
                it.remove();
            }
        }
    }

    public static void reset() {
        lastCheckTick = 0L;
        TEMP_DIRT_BLOCKS.clear();
    }

    public static boolean isMidnight(ServerLevel level) {
        if (level == null) return false;
        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= 18000L && dayTime <= 22000L;
    }

    public static void tickServer(ServerLevel level) {
        // 1. Clean up temporary hunter dirt blocks after 10 seconds (200 ticks)
        tickTempDirtCleanup(level);

        // Only tick on overworld
        if (level.getServer() == null || level != level.getServer().overworld()) {
            return;
        }

        // 2. Periodic hunter incursion check every 45 seconds (900 ticks)
        long currentTick = level.getGameTime();
        if (currentTick - lastCheckTick < 900) {
            return;
        }
        lastCheckTick = currentTick;

        // Hunters only spawn during midnight (18000 to 22000)
        if (!isMidnight(level)) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            List<ZooClaim> claims = ZooClaimManager.get(level).getPlayerClaims(player.getUUID());
            if (claims.isEmpty()) continue;

            // Cap existing alive hunters near player (max 4)
            List<ZooHunterEntity> existingHunters = level.getEntitiesOfClass(
                    ZooHunterEntity.class,
                    player.getBoundingBox().inflate(64.0, 32.0, 64.0),
                    Entity::isAlive
            );
            if (existingHunters.size() >= 4) {
                continue;
            }

            // 60% chance of hunter incursion if player has claimed zoo land
            if (level.getRandom().nextFloat() < 0.60f) {
                ZooClaim randomClaim = claims.get(level.getRandom().nextInt(claims.size()));
                var box = randomClaim.getBox();

                int minX = box.minX();
                int maxX = box.maxX();
                int minZ = box.minZ();
                int maxZ = box.maxZ();

                int spawnX;
                int spawnZ;
                int side = level.getRandom().nextInt(4);
                int offset = 6 + level.getRandom().nextInt(6);
                switch (side) {
                    case 0 -> {
                        spawnX = minX + level.getRandom().nextInt(Math.max(1, maxX - minX + 1));
                        spawnZ = minZ - offset;
                    }
                    case 1 -> {
                        spawnX = minX + level.getRandom().nextInt(Math.max(1, maxX - minX + 1));
                        spawnZ = maxZ + offset;
                    }
                    case 2 -> {
                        spawnX = minX - offset;
                        spawnZ = minZ + level.getRandom().nextInt(Math.max(1, maxZ - minZ + 1));
                    }
                    default -> {
                        spawnX = maxX + offset;
                        spawnZ = minZ + level.getRandom().nextInt(Math.max(1, maxZ - minZ + 1));
                    }
                }

                if (!level.hasChunk(spawnX >> 4, spawnZ >> 4)) {
                    spawnHunterNear(player);
                    continue;
                }

                int spawnY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);
                BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);

                if (spawnY > level.getMinBuildHeight() && !level.getBlockState(spawnPos.below()).isAir()) {
                    spawnHunterGroup(level, spawnPos, player, false);
                } else {
                    spawnHunterNear(player);
                }
            }
        }
    }

    public static boolean spawnHunterNear(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        double angle = level.getRandom().nextDouble() * Math.PI * 2;
        double dist = 12.0 + level.getRandom().nextDouble() * 8.0;
        int spawnX = (int) (player.getX() + Math.cos(angle) * dist);
        int spawnZ = (int) (player.getZ() + Math.sin(angle) * dist);
        int spawnY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);

        BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);
        return spawnHunterGroup(level, spawnPos, player, true);
    }

    public static boolean spawnHunterGroup(ServerLevel level, BlockPos spawnPos, ServerPlayer player, boolean isNear) {
        boolean isDuo = level.getRandom().nextBoolean(); // 50% Type 1 (Solo), 50% Type 2 (Duo)

        ZooHunterEntity thief = ZooEntities.HUNTER.create(level);
        if (thief == null) return false;

        thief.setHunterType(ZooHunterEntity.HunterType.THIEF);
        thief.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        level.addFreshEntity(thief);

        if (isDuo) {
            // Hunter Type 2: Duo - 1 Archer shooting security/zookeepers/players + 1 Thief stealing animals
            ZooHunterEntity archer = ZooEntities.HUNTER.create(level);
            if (archer != null) {
                archer.setHunterType(ZooHunterEntity.HunterType.ARCHER);
                archer.setPos(spawnPos.getX() + 1.5, spawnPos.getY(), spawnPos.getZ() + 1.5);
                level.addFreshEntity(archer);

                thief.setPartnerUuid(archer.getUUID());
                archer.setPartnerUuid(thief.getUUID());
            }

            if (player != null) {
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.security", "zootycoon.hunter.duo_warning", 3);
                level.playSound(null, player.blockPosition(), SoundEvents.ARROW_HIT, SoundSource.HOSTILE, 1.0f, 0.8f);
            }
        } else {
            // Hunter Type 1: Solo - 1 Thief carrying animal far outside the zoo
            if (player != null) {
                String toastKey = isNear ? "zootycoon.hunter.spawned_near" : "zootycoon.hunter.incursion_warning";
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.security", toastKey, 3);
                level.playSound(null, player.blockPosition(), SoundEvents.ARROW_HIT, SoundSource.HOSTILE, 1.0f, 1.0f);
            }
        }
        return true;
    }
}
