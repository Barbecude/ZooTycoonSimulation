package net.zootycoon.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooVisitorEntity;
import net.zootycoon.network.ZooNetworkPayloads;

public class ZooCommands {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Main /zootycoon command tree
            var root = Commands.literal("zootycoon")
                    .executes(ctx -> sendHelp(ctx.getSource()))
                    // 1. /zootycoon satwa [lapar|hunger] [amount]
                    .then(Commands.literal("satwa")
                            .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), 10))
                            .then(Commands.literal("lapar")
                                    .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), 10))
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                            .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))
                                    )
                            )
                            .then(Commands.literal("hunger")
                                    .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), 10))
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                            .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))
                                    )
                            )
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                    .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))
                            )
                    )
                    // 2. /zootycoon lapar [jumlah pengunjung]
                    .then(Commands.literal("lapar")
                            .executes(ctx -> spawnDebugVisitors(ctx.getSource(), ZooVisitorEntity.VisitorMood.HUNGRY, 1))
                            .then(Commands.argument("jumlah", IntegerArgumentType.integer(1, 100))
                                    .executes(ctx -> spawnDebugVisitors(ctx.getSource(), ZooVisitorEntity.VisitorMood.HUNGRY, IntegerArgumentType.getInteger(ctx, "jumlah")))
                            )
                    )
                    // 3. /zootycoon haus [jumlah pengunjung]
                    .then(Commands.literal("haus")
                            .executes(ctx -> spawnDebugVisitors(ctx.getSource(), ZooVisitorEntity.VisitorMood.THIRSTY, 1))
                            .then(Commands.argument("jumlah", IntegerArgumentType.integer(1, 100))
                                    .executes(ctx -> spawnDebugVisitors(ctx.getSource(), ZooVisitorEntity.VisitorMood.THIRSTY, IntegerArgumentType.getInteger(ctx, "jumlah")))
                            )
                    )
                    // 4. /zootycoon toilet [jumlah pengunjung]
                    .then(Commands.literal("toilet")
                            .executes(ctx -> spawnDebugVisitors(ctx.getSource(), ZooVisitorEntity.VisitorMood.TOILET, 1))
                            .then(Commands.argument("jumlah", IntegerArgumentType.integer(1, 100))
                                    .executes(ctx -> spawnDebugVisitors(ctx.getSource(), ZooVisitorEntity.VisitorMood.TOILET, IntegerArgumentType.getInteger(ctx, "jumlah")))
                            )
                    );

            dispatcher.register(root);

            // Legacy backward-compatibility alias: /satwa hunger [amount]
            dispatcher.register(Commands.literal("satwa")
                    .then(Commands.literal("hunger")
                            .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), 10))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                    .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))
                            )
                    )
                    .then(Commands.literal("lapar")
                            .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), 10))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                    .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))
                            )
                    )
            );
        });
    }

    private static int spawnDebugVisitors(CommandSourceStack source, ZooVisitorEntity.VisitorMood mood, int count) {
        ServerLevel level = source.getLevel();
        Vec3 origin = source.getPosition();
        BlockPos gatePos = BlockPos.containing(origin);

        for (int i = 0; i < count; i++) {
            ZooVisitorEntity visitor = ZooEntities.VISITOR.create(level);
            if (visitor != null) {
                double ox = i > 0 ? (level.random.nextDouble() - 0.5) * 1.5 : 0;
                double oz = i > 0 ? (level.random.nextDouble() - 0.5) * 1.5 : 0;
                visitor.setPos(origin.x + ox, origin.y, origin.z + oz);
                visitor.setGatePos(gatePos);
                visitor.configureDebugMood(mood);
                level.addFreshEntity(visitor);

                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        visitor.getX(), visitor.getY() + 1.2, visitor.getZ(),
                        8, 0.25, 0.3, 0.25, 0.05);
            }
        }

        String moodName = switch (mood) {
            case HUNGRY -> "lapar";
            case THIRSTY -> "haus";
            case TOILET -> "kebelet toilet";
            default -> mood.name().toLowerCase();
        };

        source.sendSuccess(() -> Component.literal("Berhasil memunculkan " + count + " pengunjung dengan mood " + moodName + " di lokasi Anda.").withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int sendHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Daftar Perintah Zoo Tycoon:").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("/zootycoon lapar [jumlah] - Spawn pengunjung dengan mood lapar").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("/zootycoon haus [jumlah] - Spawn pengunjung dengan mood haus").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("/zootycoon toilet [jumlah] - Spawn pengunjung dengan mood toilet").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("/zootycoon satwa lapar [0-100] - Atur tingkat kelaparan seluruh satwa zoo").withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    private static int setAllAnimalsHunger(CommandSourceStack source, int amount) {
        MinecraftServer server = source.getServer();
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity living && living.isAlive()) {
                    if (ZooAnimalHungerManager.isTagged(living)) {
                        ZooAnimalHungerManager.setHunger(living, amount);
                        count++;
                    }
                }
            }
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ZooNetworkPayloads.sendAnimalSync(player);
        }
        final int affected = count;
        source.sendSuccess(() -> Component.literal("Tingkat kelaparan " + affected + " satwa kebun binatang berhasil diatur ke " + amount + "%"), true);
        return count;
    }
}
