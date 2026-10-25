package net.zootycoon.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.animal.ZooInspectionService;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooVisitorEntity;
import net.zootycoon.network.ZooNetworkPayloads;

public class ZooCommands {

    public enum AnimalStat {
        HUNGER,
        THIRST,
        CLEANLINESS,
        HEALTH,
        ALL
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Main /zootycoon command tree
            var root = Commands.literal("zootycoon")
                    .executes(ctx -> sendHelp(ctx.getSource()))
                    // 1. /zootycoon satwa ...
                    .then(buildSatwaCommand("satwa"))
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
                    )
                    // 5. /zootycoon takut [jumlah pengunjung]
                    .then(Commands.literal("takut")
                            .executes(ctx -> spawnDebugVisitors(ctx.getSource(), ZooVisitorEntity.VisitorMood.SCARED, 1))
                            .then(Commands.argument("jumlah", IntegerArgumentType.integer(1, 100))
                                    .executes(ctx -> spawnDebugVisitors(ctx.getSource(), ZooVisitorEntity.VisitorMood.SCARED, IntegerArgumentType.getInteger(ctx, "jumlah")))
                            )
                    );

            dispatcher.register(root);

            // Shortcut / alias command: /satwa ...
            dispatcher.register(buildSatwaCommand("satwa"));
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSatwaCommand(String literalName) {
        return Commands.literal(literalName)
                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HUNGER, 10, false, false))
                // Direct backward-compatible number: /satwa 10
                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HUNGER, IntegerArgumentType.getInteger(ctx, "amount"), false, false))
                )
                // lapar / hunger
                .then(createStatNode("lapar", AnimalStat.HUNGER, 10, 0, 100, false))
                .then(createStatNode("hunger", AnimalStat.HUNGER, 10, 0, 100, false))
                // haus / thirst
                .then(createStatNode("haus", AnimalStat.THIRST, 10, 0, 100, false))
                .then(createStatNode("thirst", AnimalStat.THIRST, 10, 0, 100, false))
                // bersih / cleanliness
                .then(createStatNode("bersih", AnimalStat.CLEANLINESS, 10, 0, 100, false))
                .then(createStatNode("cleanliness", AnimalStat.CLEANLINESS, 10, 0, 100, false))
                // darah / health
                .then(createStatNode("darah", AnimalStat.HEALTH, 2, 1, 1000, false))
                .then(createStatNode("health", AnimalStat.HEALTH, 2, 1, 1000, false))
                // kurangi / reduce
                .then(buildReduceBranch("kurangi", false))
                .then(buildReduceBranch("reduce", false))
                // semua / all (force all registered animals)
                .then(buildAllBranch("semua"))
                .then(buildAllBranch("all"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createStatNode(String name, AnimalStat stat, int defaultVal, int min, int max, boolean forceAll) {
        return Commands.literal(name)
                .executes(ctx -> modifyAnimalStat(ctx.getSource(), stat, defaultVal, false, forceAll))
                .then(Commands.argument("amount", IntegerArgumentType.integer(min, max))
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), stat, IntegerArgumentType.getInteger(ctx, "amount"), false, forceAll))
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildReduceBranch(String literalName, boolean forceAll) {
        return Commands.literal(literalName)
                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.ALL, 25, true, forceAll))
                .then(Commands.literal("lapar")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HUNGER, 25, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HUNGER, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))))
                .then(Commands.literal("hunger")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HUNGER, 25, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HUNGER, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))))
                .then(Commands.literal("haus")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.THIRST, 25, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.THIRST, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))))
                .then(Commands.literal("thirst")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.THIRST, 25, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.THIRST, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))))
                .then(Commands.literal("bersih")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.CLEANLINESS, 25, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.CLEANLINESS, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))))
                .then(Commands.literal("cleanliness")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.CLEANLINESS, 25, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.CLEANLINESS, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))))
                .then(Commands.literal("darah")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HEALTH, 4, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HEALTH, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))))
                .then(Commands.literal("health")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HEALTH, 4, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HEALTH, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))))
                .then(Commands.literal("semua")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.ALL, 25, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.ALL, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))))
                .then(Commands.literal("all")
                        .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.ALL, 25, true, forceAll))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.ALL, IntegerArgumentType.getInteger(ctx, "amount"), true, forceAll))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildAllBranch(String literalName) {
        return Commands.literal(literalName)
                .executes(ctx -> modifyAnimalStat(ctx.getSource(), AnimalStat.HUNGER, 10, false, true))
                .then(createStatNode("lapar", AnimalStat.HUNGER, 10, 0, 100, true))
                .then(createStatNode("hunger", AnimalStat.HUNGER, 10, 0, 100, true))
                .then(createStatNode("haus", AnimalStat.THIRST, 10, 0, 100, true))
                .then(createStatNode("thirst", AnimalStat.THIRST, 10, 0, 100, true))
                .then(createStatNode("bersih", AnimalStat.CLEANLINESS, 10, 0, 100, true))
                .then(createStatNode("cleanliness", AnimalStat.CLEANLINESS, 10, 0, 100, true))
                .then(createStatNode("darah", AnimalStat.HEALTH, 2, 1, 1000, true))
                .then(createStatNode("health", AnimalStat.HEALTH, 2, 1, 1000, true))
                .then(buildReduceBranch("kurangi", true))
                .then(buildReduceBranch("reduce", true));
    }

    private static LivingEntity getTargetedAnimal(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 reachVec = eyePos.add(lookVec.scale(16.0));
        AABB box = player.getBoundingBox().expandTowards(lookVec.scale(16.0)).inflate(2.0);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, eyePos, reachVec, box,
                e -> e instanceof LivingEntity living && living.isAlive() && ZooAnimalHungerManager.isZooAnimalCandidate(living),
                16.0 * 16.0
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    private static int modifyAnimalStat(CommandSourceStack source, AnimalStat stat, int value, boolean isReduce, boolean forceAll) {
        MinecraftServer server = source.getServer();
        LivingEntity targetAnimal = null;

        if (!forceAll && source.getEntity() instanceof ServerPlayer player) {
            targetAnimal = getTargetedAnimal(player);
        }

        if (targetAnimal != null) {
            // Apply to specific targeted animal in crosshair
            if (!ZooAnimalHungerManager.isTagged(targetAnimal)) {
                ZooAnimalHungerManager.setTagged(targetAnimal, true);
                if (source.getEntity() instanceof ServerPlayer sp) {
                    ZooAnimalHungerManager.setOwner(targetAnimal, sp.getUUID());
                }
            }

            applyStatToAnimal(targetAnimal, stat, value, isReduce);
            ZooInspectionService.notifyAnimalUpdated(targetAnimal);

            for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                ZooNetworkPayloads.sendAnimalSync(sp);
            }

            String name = targetAnimal.hasCustomName() ? targetAnimal.getCustomName().getString() : targetAnimal.getType().getDescription().getString();
            String statName = getStatName(stat);
            String action = isReduce ? "dikurangi sebesar " + value : "diatur ke " + value + (stat == AnimalStat.HEALTH ? " HP" : "%");

            source.sendSuccess(() -> Component.literal("Stat " + statName + " satwa " + name + " berhasil " + action + ". (Tatap udara untuk mengatur seluruh satwa zoo).").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        // Apply to all registered zoo animals
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity living && living.isAlive()) {
                    if (ZooAnimalHungerManager.isTagged(living)) {
                        applyStatToAnimal(living, stat, value, isReduce);
                        ZooInspectionService.notifyAnimalUpdated(living);
                        count++;
                    }
                }
            }
        }

        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            ZooNetworkPayloads.sendAnimalSync(sp);
        }

        final int affected = count;
        String statName = getStatName(stat);
        String action = isReduce ? "dikurangi sebesar " + value : "diatur ke " + value + (stat == AnimalStat.HEALTH ? " HP" : "%");
        source.sendSuccess(() -> Component.literal("Stat " + statName + " pada " + affected + " satwa kebun binatang berhasil " + action + ".").withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static void applyStatToAnimal(LivingEntity living, AnimalStat stat, int value, boolean isReduce) {
        if (stat == AnimalStat.HUNGER || stat == AnimalStat.ALL) {
            int cur = ZooAnimalHungerManager.getHunger(living);
            int target = isReduce ? Math.max(0, cur - value) : Math.clamp(value, 0, ZooAnimalHungerManager.MAX_HUNGER);
            ZooAnimalHungerManager.setHunger(living, target);
        }
        if (stat == AnimalStat.THIRST || stat == AnimalStat.ALL) {
            int cur = ZooAnimalHungerManager.getThirst(living);
            int target = isReduce ? Math.max(0, cur - value) : Math.clamp(value, 0, ZooAnimalHungerManager.MAX_THIRST);
            ZooAnimalHungerManager.setThirst(living, target);
        }
        if (stat == AnimalStat.CLEANLINESS || stat == AnimalStat.ALL) {
            int cur = ZooAnimalHungerManager.getCleanliness(living);
            int target = isReduce ? Math.max(0, cur - value) : Math.clamp(value, 0, ZooAnimalHungerManager.MAX_CLEANLINESS);
            ZooAnimalHungerManager.setCleanliness(living, target);
        }
        if (stat == AnimalStat.HEALTH || stat == AnimalStat.ALL) {
            float cur = living.getHealth();
            float target = isReduce ? Math.max(1.0f, cur - value) : Math.clamp((float) value, 1.0f, living.getMaxHealth());
            living.setHealth(target);
        }
    }

    private static String getStatName(AnimalStat stat) {
        return switch (stat) {
            case HUNGER -> "kelaparan";
            case THIRST -> "kehausan";
            case CLEANLINESS -> "kebersihan";
            case HEALTH -> "darah/kesehatan";
            case ALL -> "semua indikator kebutuhan";
        };
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
            case SCARED -> "takut/waspada";
            default -> mood.name().toLowerCase();
        };

        source.sendSuccess(() -> Component.literal("Berhasil memunculkan " + count + " pengunjung dengan mood " + moodName + " di lokasi Anda.").withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int sendHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Daftar Perintah Zoo Tycoon:").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("/satwa lapar [0-100] - Atur tingkat lapar satwa target / semua satwa").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("/satwa haus [0-100] - Atur tingkat haus satwa target / semua satwa").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("/satwa bersih [0-100] - Atur kebersihan satwa target / semua satwa").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("/satwa darah [1-HP] - Atur sisa darah satwa target / semua satwa").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("/satwa kurangi <lapar|haus|bersih|darah|semua> [jumlah] - Kurangi stat satwa").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("/satwa semua <lapar|haus|bersih|darah> [nilai] - Paksa ubah semua satwa zoo").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("/zootycoon lapar [jumlah] - Spawn pengunjung dengan mood lapar").withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("/zootycoon haus [jumlah] - Spawn pengunjung dengan mood haus").withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("/zootycoon toilet [jumlah] - Spawn pengunjung dengan mood toilet").withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("/zootycoon takut [jumlah] - Spawn pengunjung dengan mood takut").withStyle(ChatFormatting.GRAY), false);
        return 1;
    }
}
