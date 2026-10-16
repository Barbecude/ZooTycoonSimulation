package net.zootycoon.studio;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static final int STUDIO_Y = 200;
    public static final int PLOT_RADIUS = 24; // 49x49 building platform
    public static final int MAX_BUILD_HEIGHT = 35; // y=201 to y=235

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
            // 1. /studio command tree
            dispatcher.register(Commands.literal("studio")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            if (isInStudio(player.getUUID())) {
                                String cat = getTargetCategory(player.getUUID());
                                ServerPlayNetworking.send(player, new ZooNetworkPayloads.RequestClientSelectionSavePayload("", cat));
                            } else {
                                enterStudio(player, "enclosures");
                            }
                            return 1;
                        }
                        return 0;
                    })
                    .then(Commands.literal("enter")
                            .executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayer();
                                if (player != null) {
                                    enterStudio(player, "enclosures");
                                    return 1;
                                }
                                return 0;
                            })
                            .then(Commands.argument("kategori", StringArgumentType.word()).executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayer();
                                if (player != null) {
                                    String cat = StringArgumentType.getString(ctx, "kategori");
                                    enterStudio(player, cat);
                                    return 1;
                                }
                                return 0;
                            }))
                    )
                    .then(Commands.literal("exit").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            if (isInStudio(player.getUUID())) {
                                exitStudio(player);
                            } else {
                                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.studio.not_in_studio", 2);
                            }
                            return 1;
                        }
                        return 0;
                    }))
                    .then(Commands.literal("save")
                            .then(Commands.argument("nama", StringArgumentType.greedyString()).executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayer();
                                if (player != null) {
                                    String name = StringArgumentType.getString(ctx, "nama").trim();
                                    String cat = getTargetCategory(player.getUUID());
                                    ServerPlayNetworking.send(player, new ZooNetworkPayloads.RequestClientSelectionSavePayload(name, cat));
                                    return 1;
                                }
                                return 0;
                            }))
                    )
            );

            // 2. /zoo command tree
            dispatcher.register(Commands.literal("zoo")
                    .then(Commands.literal("studio").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            enterStudio(player, "enclosures");
                            return 1;
                        }
                        return 0;
                    }))
                    .then(Commands.literal("exit").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            if (isInStudio(player.getUUID())) {
                                exitStudio(player);
                            } else {
                                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.studio.not_in_studio", 2);
                            }
                            return 1;
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

            // 3. /zootycoon command tree
            dispatcher.register(Commands.literal("zootycoon")
                    .then(Commands.literal("studio").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            enterStudio(player, "enclosures");
                            return 1;
                        }
                        return 0;
                    }))
                    .then(Commands.literal("exit").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        if (player != null) {
                            if (isInStudio(player.getUUID())) {
                                exitStudio(player);
                            } else {
                                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.studio.not_in_studio", 2);
                            }
                            return 1;
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

            // 4. Command: /kandang [nama kandang] -> Save design into "enclosures"
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

            // 5. Command: /fasilitas [nama fasilitas] -> Save design into "essentials"
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

    public static String getTargetCategory(UUID playerUuid) {
        PlayerSurvivalSnapshot snapshot = ACTIVE_STUDIO_SESSIONS.get(playerUuid);
        return snapshot != null && snapshot.targetCategory() != null ? snapshot.targetCategory() : "enclosures";
    }

    public static void clearStudioPlot(ServerLevel level, BlockPos center) {
        // 0. Discard any non-player entities on the studio plot
        List<Entity> plotEntities = level.getEntitiesOfClass(
                Entity.class,
                new AABB(center.getX() - PLOT_RADIUS, STUDIO_Y - 1, center.getZ() - PLOT_RADIUS,
                        center.getX() + PLOT_RADIUS + 1, STUDIO_Y + MAX_BUILD_HEIGHT + 2, center.getZ() + PLOT_RADIUS + 1),
                e -> !(e instanceof Player)
        );
        for (Entity ent : plotEntities) {
            ent.discard();
        }

        // 1. Clear any blocks above the floor up to MAX_BUILD_HEIGHT
        for (int dx = -PLOT_RADIUS; dx <= PLOT_RADIUS; dx++) {
            for (int dz = -PLOT_RADIUS; dz <= PLOT_RADIUS; dz++) {
                for (int dy = 1; dy <= MAX_BUILD_HEIGHT; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 2. Rebuild clean platform floor at STUDIO_Y
        for (int dx = -PLOT_RADIUS; dx <= PLOT_RADIUS; dx++) {
            for (int dz = -PLOT_RADIUS; dz <= PLOT_RADIUS; dz++) {
                BlockPos floorPos = center.offset(dx, 0, dz);
                if (dx == 0 && dz == 0) {
                    level.setBlock(floorPos, Blocks.GLOWSTONE.defaultBlockState(), 2);
                } else if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) {
                    level.setBlock(floorPos, Blocks.GOLD_BLOCK.defaultBlockState(), 2);
                } else if (Math.abs(dx) == PLOT_RADIUS && Math.abs(dz) == PLOT_RADIUS) {
                    level.setBlock(floorPos, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                } else if (Math.abs(dx) == PLOT_RADIUS || Math.abs(dz) == PLOT_RADIUS) {
                    level.setBlock(floorPos, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(floorPos, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                }
                level.setBlock(floorPos.below(), Blocks.BEDROCK.defaultBlockState(), 2);
            }
        }
    }

    public static boolean enterStudio(ServerPlayer player, String targetCategory) {
        ServerLevel level = (ServerLevel) player.level();
        int plotOffset = (Math.abs(player.getUUID().hashCode()) % 500) * 200 + 10000;
        BlockPos center = new BlockPos(plotOffset, STUDIO_Y, plotOffset);

        // If player already in session, re-synchronize and teleport to plot center
        if (isInStudio(player.getUUID())) {
            player.teleportTo(center.getX() + 0.5, STUDIO_Y + 1.0, center.getZ() + 0.5);
            player.setGameMode(GameType.CREATIVE);
            giveStudioBiomeKit(player);
            ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStudioStatePayload(true, targetCategory));
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.studio.entered", 0);
            return true;
        }

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
        saveSessionToDisk(player.server, player.getUUID(), snapshot);

        player.teleportTo(center.getX() + 0.5, STUDIO_Y + 1.0, center.getZ() + 0.5);
        player.setGameMode(GameType.CREATIVE);
        giveStudioBiomeKit(player);

        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStudioStatePayload(true, targetCategory));
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.studio.entered", 0);

        ZooTycoonMod.LOGGER.info("Player {} entered Studio session for new design (Plot: {}).", player.getName().getString(), center);
        return true;
    }

    public static boolean editInStudio(ServerPlayer player, String schematicId, String targetCategory) {
        ServerLevel level = (ServerLevel) player.level();
        int plotOffset = (Math.abs(player.getUUID().hashCode()) % 500) * 200 + 10000;
        BlockPos center = new BlockPos(plotOffset, STUDIO_Y, plotOffset);

        if (isInStudio(player.getUUID())) {
            exitStudio(player);
        }

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
        saveSessionToDisk(player.server, player.getUUID(), snapshot);

        player.teleportTo(center.getX() + 0.5, STUDIO_Y + 1.0, center.getZ() + 0.5);
        player.setGameMode(GameType.CREATIVE);
        giveStudioBiomeKit(player);

        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStudioStatePayload(true, targetCategory));
        String editName = schemOpt.isPresent() ? schemOpt.get().getName() : schematicId;
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.studio.editing", 0, editName);

        ZooTycoonMod.LOGGER.info("Player {} entered Studio to edit schematic {} (Plot: {}).", player.getName().getString(), schematicId, center);
        return true;
    }

    public static boolean exitStudio(ServerPlayer player) {
        PlayerSurvivalSnapshot snapshot = ACTIVE_STUDIO_SESSIONS.remove(player.getUUID());
        if (snapshot == null) {
            snapshot = loadSessionFromDisk(player.server, player.getUUID());
        }
        if (snapshot == null) {
            // Failsafe: if no snapshot exists, teleport to world spawn in survival
            ServerLevel overworld = player.server.overworld();
            BlockPos spawn = overworld.getSharedSpawnPos();
            player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY() + 1.0, spawn.getZ() + 0.5, java.util.Set.of(), 0, 0);
            player.setGameMode(GameType.SURVIVAL);
            clearSessionFromDisk(player.server, player.getUUID());
            ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStudioStatePayload(false, ""));
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.studio.exited", 0);
            return true;
        }

        clearSessionFromDisk(player.server, player.getUUID());
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.SyncStudioStatePayload(false, ""));

        player.teleportTo(snapshot.originLevel(), snapshot.x(), snapshot.y(), snapshot.z(), java.util.Set.of(), snapshot.yRot(), snapshot.xRot());
        player.setGameMode(snapshot.previousGameMode());

        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.studio.exited", 0);
        return true;
    }

    public static boolean isInStudio(UUID playerUuid) {
        return ACTIVE_STUDIO_SESSIONS.containsKey(playerUuid);
    }

    private static Path getSessionSavePath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("zoo_studio_sessions.dat");
    }

    private static synchronized void saveSessionToDisk(MinecraftServer server, UUID playerUuid, PlayerSurvivalSnapshot snapshot) {
        if (server == null) return;
        try {
            Path path = getSessionSavePath(server);
            CompoundTag root = Files.exists(path) ? NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap()) : new CompoundTag();
            CompoundTag tag = new CompoundTag();
            tag.putString("Dimension", snapshot.originLevel().dimension().location().toString());
            tag.putDouble("X", snapshot.x());
            tag.putDouble("Y", snapshot.y());
            tag.putDouble("Z", snapshot.z());
            tag.putFloat("YRot", snapshot.yRot());
            tag.putFloat("XRot", snapshot.xRot());
            tag.putString("GameMode", snapshot.previousGameMode().getName());
            tag.putString("Category", snapshot.targetCategory() != null ? snapshot.targetCategory() : "enclosures");
            tag.putInt("CenterX", snapshot.plotCenter().getX());
            tag.putInt("CenterY", snapshot.plotCenter().getY());
            tag.putInt("CenterZ", snapshot.plotCenter().getZ());
            if (snapshot.editingSchematicId() != null) {
                tag.putString("EditingId", snapshot.editingSchematicId());
            }
            root.put(playerUuid.toString(), tag);
            NbtIo.writeCompressed(root, path);
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to save studio session", e);
        }
    }

    private static synchronized PlayerSurvivalSnapshot loadSessionFromDisk(MinecraftServer server, UUID playerUuid) {
        if (server == null) return null;
        try {
            Path path = getSessionSavePath(server);
            if (!Files.exists(path)) return null;
            CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            if (!root.contains(playerUuid.toString())) return null;
            CompoundTag tag = root.getCompound(playerUuid.toString());

            ResourceLocation dimLoc = ResourceLocation.parse(tag.getString("Dimension"));
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLoc);
            ServerLevel originLevel = server.getLevel(dimKey);
            if (originLevel == null) originLevel = server.overworld();

            double x = tag.getDouble("X");
            double y = tag.getDouble("Y");
            double z = tag.getDouble("Z");
            float yRot = tag.getFloat("YRot");
            float xRot = tag.getFloat("XRot");
            GameType gm = GameType.byName(tag.getString("GameMode"), GameType.SURVIVAL);
            String cat = tag.getString("Category");
            BlockPos center = new BlockPos(tag.getInt("CenterX"), tag.getInt("CenterY"), tag.getInt("CenterZ"));
            String editingId = tag.contains("EditingId") ? tag.getString("EditingId") : null;

            return new PlayerSurvivalSnapshot(originLevel, x, y, z, yRot, xRot, gm, cat, center, editingId);
        } catch (Exception e) {
            return null;
        }
    }

    private static synchronized void clearSessionFromDisk(MinecraftServer server, UUID playerUuid) {
        if (server == null) return;
        try {
            Path path = getSessionSavePath(server);
            if (Files.exists(path)) {
                CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
                root.remove(playerUuid.toString());
                NbtIo.writeCompressed(root, path);
            }
        } catch (Exception ignored) {}
    }

    public static void giveStudioBiomeKit(ServerPlayer player) {
        if (player == null) return;

        // 1. Give Studio Management Stick in slot 0
        ItemStack stick = new ItemStack(Items.STICK);
        stick.set(DataComponents.CUSTOM_NAME, Component.literal("Tongkat Studio").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.getInventory().setItem(0, stick);

        // 2. Give Spyglass in slot 1
        ItemStack spyglass = new ItemStack(Items.SPYGLASS);
        player.getInventory().setItem(1, spyglass);

        // 3. Give Biome Blends if BiomeBlends mod is present
        for (var entry : net.minecraft.core.registries.BuiltInRegistries.ITEM.entrySet()) {
            if ("biomeblends".equals(entry.getKey().location().getNamespace())) {
                String path = entry.getKey().location().getPath();
                if (path.equals("bland_blend") || path.endsWith("_blend")) {
                    ItemStack stack = new ItemStack(entry.getValue(), 16);
                    if (!player.getInventory().contains(stack)) {
                        player.getInventory().add(stack);
                    }
                }
            }
        }
    }
}
