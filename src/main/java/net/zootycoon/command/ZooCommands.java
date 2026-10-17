package net.zootycoon.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.network.ZooNetworkPayloads;

public class ZooCommands {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("satwa")
                    .then(Commands.literal("hunger")
                            .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), 10))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                    .executes(ctx -> setAllAnimalsHunger(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))
                            )
                    )
            );
        });
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

