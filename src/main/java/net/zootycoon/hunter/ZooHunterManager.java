package net.zootycoon.hunter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    public static void tickServer(ServerLevel level) {
        // 1. Clean up temporary hunter dirt blocks after 10 seconds (200 ticks)
        tickTempDirtCleanup(level);

        // 2. Periodic hunter incursion check every 2 minutes
        long currentTick = level.getGameTime();
        if (currentTick - lastCheckTick < 2400) {
            return;
        }
        lastCheckTick = currentTick;

        for (ServerPlayer player : level.players()) {
            List<ZooClaim> claims = ZooClaimManager.get(level).getPlayerClaims(player.getUUID());
            if (claims.isEmpty()) continue;

            // 25% chance of hunter incursion if player has claimed zoo land
            if (level.getRandom().nextFloat() < 0.25f) {
                ZooClaim randomClaim = claims.get(level.getRandom().nextInt(claims.size()));
                var box = randomClaim.getBox();

                int spawnX = box.minX() + (level.getRandom().nextBoolean() ? -5 : (box.maxX() - box.minX() + 5));
                int spawnZ = box.minZ() + (level.getRandom().nextBoolean() ? -5 : (box.maxZ() - box.minZ() + 5));
                int spawnY = level.getHeight(Heightmap.Types.WORLD_SURFACE, spawnX, spawnZ);

                BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);
                ZooHunterEntity hunter = ZooEntities.HUNTER.create(level);
                if (hunter != null) {
                    hunter.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                    level.addFreshEntity(hunter);

                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.security", "zootycoon.hunter.incursion_warning", 3);
                    level.playSound(null, player.blockPosition(), SoundEvents.ARROW_HIT, SoundSource.HOSTILE, 1.0f, 0.8f);
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
        int spawnY = level.getHeight(Heightmap.Types.WORLD_SURFACE, spawnX, spawnZ);

        BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);
        ZooHunterEntity hunter = ZooEntities.HUNTER.create(level);
        if (hunter != null) {
            hunter.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            level.addFreshEntity(hunter);

            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.security", "zootycoon.hunter.spawned_near", 3);
            level.playSound(null, player.blockPosition(), SoundEvents.ARROW_HIT, SoundSource.HOSTILE, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}
