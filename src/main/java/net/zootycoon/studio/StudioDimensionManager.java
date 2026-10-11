package net.zootycoon.studio;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StudioDimensionManager {

    public record PlayerSurvivalSnapshot(
            ServerLevel originLevel,
            double x, double y, double z,
            float yRot, float xRot,
            GameType previousGameMode,
            String targetCategory,
            BlockPos plotCenter,
            String editingSchematicId
    ) {}

    private static final Map<UUID, PlayerSurvivalSnapshot> ACTIVE_STUDIO_SESSIONS = new ConcurrentHashMap<>();

    public static void initServerEvents() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("zoo")
                    .then(Commands.literal("exit").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null && isInStudio(player.getUUID())) {
                            exitStudio(player);
                            return 1;
                        } else if (player != null) {
                            player.sendSystemMessage(Component.literal("Anda tidak sedang berada di Studio.").withStyle(ChatFormatting.RED));
                        }
                        return 0;
                    }))
                    .then(Commands.literal("hunter").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            net.zootycoon.hunter.ZooHunterManager.spawnHunterNear(player);
                            return 1;
                        }
                        return 0;
                    }))
            );
            dispatcher.register(Commands.literal("zootycoon")
                    .then(Commands.literal("exit").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null && isInStudio(player.getUUID())) {
                            exitStudio(player);
                            return 1;
                        } else if (player != null) {
                            player.sendSystemMessage(Component.literal("Anda tidak sedang berada di Studio.").withStyle(ChatFormatting.RED));
                        }
                        return 0;
                    }))
                    .then(Commands.literal("hunter").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            net.zootycoon.hunter.ZooHunterManager.spawnHunterNear(player);
                            return 1;
                        }
                        return 0;
                    }))
            );
        });
    }

    public static BlockPos getPlotCenter(UUID playerUuid) {
        PlayerSurvivalSnapshot snapshot = ACTIVE_STUDIO_SESSIONS.get(playerUuid);
        return snapshot != null ? snapshot.plotCenter() : null;
    }

    public static String getEditingSchematicId(UUID playerUuid) {
        PlayerSurvivalSnapshot snapshot = ACTIVE_STUDIO_SESSIONS.get(playerUuid);
        return snapshot != null ? snapshot.editingSchematicId() : null;
    }

    public static void clearStudioPlot(ServerLevel level, BlockPos center) {
        int radius = 64;
        // 1. Clear any blocks above the floor up to height 80
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 1; dy <= 80; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 2. Rebuild clean floor
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos floorPos = center.offset(dx, 0, dz);
                if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) {
                    level.setBlock(floorPos, Blocks.GOLD_BLOCK.defaultBlockState(), 2);
                } else if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                    level.setBlock(floorPos, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(floorPos, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                }
                level.setBlock(floorPos.below(), Blocks.BEDROCK.defaultBlockState(), 2);
            }
        }
    }

    public static boolean enterStudio(ServerPlayer player, String targetCategory) {
        if (ACTIVE_STUDIO_SESSIONS.containsKey(player.getUUID())) {
            return false;
        }

        ServerLevel level = (ServerLevel) player.level();
        int plotOffset = (Math.abs(player.getUUID().hashCode()) % 500) * 300 + 10000;
        BlockPos center = new BlockPos(plotOffset, 100, plotOffset);

        // Guaranteed blank, clean plot for new designs
        clearStudioPlot(level, center);

        PlayerSurvivalSnapshot snapshot = new PlayerSurvivalSnapshot(
                level,
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot(),
                player.gameMode.getGameModeForPlayer(),
                targetCategory,
                center,
                null
        );
        ACTIVE_STUDIO_SESSIONS.put(player.getUUID(), snapshot);

        player.teleportTo(center.getX() + 0.5, 101.0, center.getZ() + 0.5);
        player.setGameMode(GameType.CREATIVE);
        giveStudioBiomeKit(player);

        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStudioStatePayload(true, targetCategory));

        player.sendSystemMessage(Component.literal("--------------------------------------------------").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("SELAMAT DATANG DI STUDIO DESAIN BLUEPRINT!").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Area luas 128x128 blok kosong, bebas Anda bangun di Creative mode.").withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("- Selesai? Klik Kanan Build Tool untuk SIMPAN atau KELUAR!").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("- Atau ketik '/zoo exit' untuk keluar.").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("--------------------------------------------------").withStyle(ChatFormatting.GOLD));

        ZooTycoonMod.LOGGER.info("Player {} entered Studio Dimension session for new design (Plot: {}).", player.getName().getString(), center);
        return true;
    }

    public static boolean editInStudio(ServerPlayer player, String schematicId, String targetCategory) {
        if (ACTIVE_STUDIO_SESSIONS.containsKey(player.getUUID())) {
            return false;
        }

        ServerLevel level = (ServerLevel) player.level();
        int plotOffset = (Math.abs(player.getUUID().hashCode()) % 500) * 300 + 10000;
        BlockPos center = new BlockPos(plotOffset, 100, plotOffset);

        // 1. Wipe plot completely
        clearStudioPlot(level, center);

        // 2. Load and place existing schematic onto the studio plot center
        var schemOpt = ZooSchematicRegistry.get(schematicId);
        if (schemOpt.isEmpty()) {
            ZooSchematicRegistry.loadCustomSchematics();
            schemOpt = ZooSchematicRegistry.get(schematicId);
        }

        if (schemOpt.isPresent()) {
            ZooSchematic schem = schemOpt.get();
            StructureTemplate template = ZooSchematicRegistry.loadTemplate(level.getServer(), schem);
            if (template != null) {
                var dim = schem.getDimensions();
                BlockPos placePos = center.offset(-dim.getX() / 2, 1, -dim.getZ() / 2);
                template.placeInWorld(level, placePos, placePos, new StructurePlaceSettings(), level.getRandom(), 2);
                var plotBb = new net.minecraft.world.level.levelgen.structure.BoundingBox(
                        placePos.getX(), placePos.getY(), placePos.getZ(),
                        placePos.getX() + dim.getX() - 1, placePos.getY() + dim.getY() - 1, placePos.getZ() + dim.getZ() - 1
                );
                net.zootycoon.biome.ZooBiomeApplier.applySchematicBiomes(level, placePos, plotBb, schem, Rotation.NONE);
            }
        }

        PlayerSurvivalSnapshot snapshot = new PlayerSurvivalSnapshot(
                level,
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot(),
                player.gameMode.getGameModeForPlayer(),
                targetCategory,
                center,
                schematicId
        );
        ACTIVE_STUDIO_SESSIONS.put(player.getUUID(), snapshot);

        player.teleportTo(center.getX() + 0.5, 101.0, center.getZ() + 0.5);
        player.setGameMode(GameType.CREATIVE);
        giveStudioBiomeKit(player);

        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStudioStatePayload(true, targetCategory));

        player.sendSystemMessage(Component.literal("--------------------------------------------------").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("MODE EDIT STUDIO: " + (schemOpt.isPresent() ? schemOpt.get().getName() : schematicId)).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Desain lama telah dimuat di Studio untuk Anda modifikasi.").withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("- Klik Kanan Build Tool untuk Simpan Pembaruan atau Keluar.").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("--------------------------------------------------").withStyle(ChatFormatting.GOLD));

        ZooTycoonMod.LOGGER.info("Player {} entered Studio Dimension to edit schematic {} (Plot: {}).", player.getName().getString(), schematicId, center);
        return true;
    }

    public static boolean exitStudio(ServerPlayer player) {
        PlayerSurvivalSnapshot snapshot = ACTIVE_STUDIO_SESSIONS.remove(player.getUUID());
        if (snapshot == null) return false;

        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStudioStatePayload(false, ""));

        player.teleportTo(snapshot.originLevel(), snapshot.x(), snapshot.y(), snapshot.z(), snapshot.yRot(), snapshot.xRot());
        player.setGameMode(snapshot.previousGameMode());

        player.sendSystemMessage(
                Component.literal("Kembali ke dunia Survival.").withStyle(ChatFormatting.YELLOW)
        );
        return true;
    }

    public static boolean isInStudio(UUID playerUuid) {
        return ACTIVE_STUDIO_SESSIONS.containsKey(playerUuid);
    }

    public static void giveStudioBiomeKit(ServerPlayer player) {
        if (player == null) return;
        // Always provide a Spyglass if missing
        if (!player.getInventory().contains(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPYGLASS))) {
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPYGLASS));
        }

        // Check if Biome Blends is loaded or any biome blend items exist in registry
        for (var entry : net.minecraft.core.registries.BuiltInRegistries.ITEM.entrySet()) {
            if ("biomeblends".equals(entry.getKey().location().getNamespace())) {
                String path = entry.getKey().location().getPath();
                if (path.equals("bland_blend") || path.endsWith("_blend")) {
                    net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(entry.getValue(), 16);
                    if (!player.getInventory().contains(stack)) {
                        player.getInventory().add(stack);
                    }
                }
            }
        }
    }
}
