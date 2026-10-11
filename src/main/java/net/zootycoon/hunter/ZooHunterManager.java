package net.zootycoon.hunter;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooHunterEntity;

import java.util.List;

public class ZooHunterManager {
    private static long lastCheckTick = 0L;

    public static void tickServer(ServerLevel level) {
        long currentTick = level.getGameTime();
        if (currentTick - lastCheckTick < 2400) { // Every 2 minutes
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

                    player.displayClientMessage(
                            Component.literal("[Peringatan] Pemburu liar terdeteksi menyusup ke area kebun binatang!").withStyle(ChatFormatting.RED),
                            true
                    );
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

            player.displayClientMessage(
                    Component.literal("[Spawn] Pemburu liar muncul di dekat Anda!").withStyle(ChatFormatting.RED),
                    true
            );
            level.playSound(null, player.blockPosition(), SoundEvents.ARROW_HIT, SoundSource.HOSTILE, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}

