package net.zootycoon.studio;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.zootycoon.ZooTycoonMod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StudioDimensionManager {

    public record PlayerSurvivalSnapshot(
            ServerLevel originLevel,
            double x, double y, double z,
            float yRot, float xRot,
            GameType previousGameMode
    ) {}

    private static final Map<UUID, PlayerSurvivalSnapshot> ACTIVE_STUDIO_SESSIONS = new ConcurrentHashMap<>();

    public static void initServerEvents() {
        // Lifecycle events if needed
    }

    public static boolean enterStudio(ServerPlayer player, String targetCategory) {
        if (ACTIVE_STUDIO_SESSIONS.containsKey(player.getUUID())) {
            return false; // Already in studio
        }

        ServerLevel level = player.serverLevel();
        PlayerSurvivalSnapshot snapshot = new PlayerSurvivalSnapshot(
                level,
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot(),
                player.gameMode.getGameModeForPlayer()
        );
        ACTIVE_STUDIO_SESSIONS.put(player.getUUID(), snapshot);

        // Teleport to isolated creative plot area (e.g. Y=120, offset by player hash)
        int plotOffset = (Math.abs(player.getUUID().hashCode()) % 500) * 100 + 10000;
        BlockPos center = new BlockPos(plotOffset, 100, plotOffset);

        // Build 32x32 flat smooth stone & bedrock building platform
        for (int dx = -16; dx <= 16; dx++) {
            for (int dz = -16; dz <= 16; dz++) {
                level.setBlock(center.offset(dx, -1, dz), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                level.setBlock(center.offset(dx, -2, dz), Blocks.BEDROCK.defaultBlockState(), 3);
            }
        }

        player.teleportTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
        player.setGameMode(GameType.CREATIVE);

        player.displayClientMessage(
                Component.literal("🎨 Masuk ke Studio Desain Schematic! Bangun sesuka Anda di Creative, lalu klik 'Confirm & Save' di Build Tool.").withStyle(net.minecraft.ChatFormatting.AQUA),
                false
        );
        ZooTycoonMod.LOGGER.info("Player {} entered Studio Dimension session.", player.getName().getString());
        return true;
    }

    public static boolean exitStudio(ServerPlayer player) {
        PlayerSurvivalSnapshot snapshot = ACTIVE_STUDIO_SESSIONS.remove(player.getUUID());
        if (snapshot == null) return false;

        player.teleportTo(snapshot.originLevel(), snapshot.x(), snapshot.y(), snapshot.z(), snapshot.yRot(), snapshot.xRot());
        player.setGameMode(snapshot.previousGameMode());

        player.displayClientMessage(
                Component.literal("Kembali ke dunia Survival.").withStyle(net.minecraft.ChatFormatting.YELLOW),
                false
        );
        return true;
    }

    public static boolean isInStudio(UUID playerUuid) {
        return ACTIVE_STUDIO_SESSIONS.containsKey(playerUuid);
    }
}

