package net.zootycoon.studio;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;

import java.util.List;
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
                            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.studio.not_in_studio", 2);
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
                            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.studio.not_in_studio", 2);
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

            // Command: /kandang [nama kandang] -> Save design into "enclosures"
            dispatcher.register(Commands.literal("kandang")
                    .then(Commands.argument("nama", StringArgumentType.greedyString()).executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            String name = StringArgumentType.getString(ctx, "nama").trim();
                            ServerPlayNetworking.send(player, new ZooNetworkPayloads.RequestClientSelectionSavePayload(name, "enclosures"));
                            return 1;
                        }
                        return 0;
                    }))
            );

            // Command: /fasilitas [nama fasilitas] -> Save design into "essentials"
            dispatcher.register(Commands.literal("fasilitas")
                    .then(Commands.argument("nama", StringArgumentType.greedyString()).executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            String name = StringArgumentType.getString(ctx, "nama").trim();
                            ServerPlayNetworking.send(player, new ZooNetworkPayloads.RequestClientSelectionSavePayload(name, "essentials"));
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
        // 0. Discard any non-player entities on the studio plot
        List<Entity> plotEntities = level.getEntitiesOfClass(
                Entity.class,
                new AABB(center.getX() - radius, 99, center.getZ() - radius, center.getX() + radius + 1, 185, center.getZ() + radius + 1),
                e -> !(e instanceof Player)
        );
        for (Entity ent : plotEntities) {
            ent.discard();
        }

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
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.studio.entered", 0);

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

        clearStudioPlot(level, center);

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
        String editName = schemOpt.isPresent() ? schemOpt.get().getName() : schematicId;
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.studio.editing", 0, editName);

        ZooTycoonMod.LOGGER.info("Player {} entered Studio Dimension to edit schematic {} (Plot: {}).", player.getName().getString(), schematicId, center);
        return true;
    }

    public static boolean exitStudio(ServerPlayer player) {
        PlayerSurvivalSnapshot snapshot = ACTIVE_STUDIO_SESSIONS.remove(player.getUUID());
        if (snapshot == null) return false;

        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStudioStatePayload(false, ""));

        player.teleportTo(snapshot.originLevel(), snapshot.x(), snapshot.y(), snapshot.z(), snapshot.yRot(), snapshot.xRot());
        player.setGameMode(snapshot.previousGameMode());

        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.studio.exited", 0);
        return true;
    }

    public static boolean isInStudio(UUID playerUuid) {
        return ACTIVE_STUDIO_SESSIONS.containsKey(playerUuid);
    }

    public static void giveStudioBiomeKit(ServerPlayer player) {
        if (player == null) return;
        if (!player.getInventory().contains(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPYGLASS))) {
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPYGLASS));
        }

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
