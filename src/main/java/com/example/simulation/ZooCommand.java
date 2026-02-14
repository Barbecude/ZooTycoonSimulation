package com.example.simulation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * ZooCommand — Handler untuk /zoocmd
 *
 * Sub-commands (dipanggil via Clickable Chat):
 * /zoocmd buy <animal> <x> <y> <z> — Beli hewan (spawn di atas komputer)
 * /zoocmd hire <x> <y> <z> — Rekrut Staff
 * /zoocmd upgrade <x> <y> <z> — Upgrade radius scan
 */
public class ZooCommand {

    private static final long HIRE_COST = 2000;
    private static final long UPGRADE_COST = 5000;
    private static final int UPGRADE_INCREMENT = 10;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zoocmd")
                // /zoocmd buy <animal> <x> <y> <z>
                .then(Commands.literal("buy")
                        .then(Commands.argument("animal", StringArgumentType.string())
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(ctx -> buyAnimal(
                                                                ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "animal"),
                                                                IntegerArgumentType.getInteger(ctx, "x"),
                                                                IntegerArgumentType.getInteger(ctx, "y"),
                                                                IntegerArgumentType.getInteger(ctx, "z"))))))))

                // /zoocmd hire <x> <y> <z>
                .then(Commands.literal("hire")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> hireStaff(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "x"),
                                                        IntegerArgumentType.getInteger(ctx, "y"),
                                                        IntegerArgumentType.getInteger(ctx, "z")))))))

                // /zoocmd upgrade <x> <y> <z>
                .then(Commands.literal("upgrade")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> upgradeLand(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "x"),
                                                        IntegerArgumentType.getInteger(ctx, "y"),
                                                        IntegerArgumentType.getInteger(ctx, "z"))))))));
    }

    // ========== Buy Animal ==========

    private static int buyAnimal(CommandSourceStack source, String animalName,
            int x, int y, int z) {
        ServerLevel level = source.getLevel();
        BlockPos pos = new BlockPos(x, y, z);
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof ZooComputerBlockEntity computer)) {
            source.sendFailure(Component.literal("❌ Komputer tidak ditemukan di posisi tersebut!"));
            return 0;
        }

        // Tentukan harga & tipe hewan
        long cost;
        EntityType<? extends Animal> entityType;
        switch (animalName.toLowerCase()) {
            case "chicken" -> {
                cost = 1000;
                entityType = EntityType.CHICKEN;
            }
            case "cow" -> {
                cost = 3000;
                entityType = EntityType.COW;
            }
            case "pig" -> {
                cost = 2000;
                entityType = EntityType.PIG;
            }
            case "sheep" -> {
                cost = 2500;
                entityType = EntityType.SHEEP;
            }
            case "horse" -> {
                cost = 8000;
                entityType = EntityType.HORSE;
            }
            default -> {
                source.sendFailure(Component.literal("❌ Hewan '" + animalName + "' tidak tersedia!"));
                return 0;
            }
        }

        // Cek saldo
        if (computer.getBalance() < cost) {
            source.sendFailure(Component.literal("❌ Saldo tidak cukup! Butuh Rp" + cost)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        // Kurangi saldo & spawn hewan di atas komputer (Y + 2)
        computer.addBalance(-cost);
        Animal animal = entityType.create(level);
        if (animal != null) {
            animal.moveTo(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, 0, 0);
            level.addFreshEntity(animal);
        }

        source.sendSuccess(() -> Component.literal("✅ " + capitalize(animalName)
                + " berhasil dikirim! (Rp" + cost + ")")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    // ========== Hire Staff ==========

    private static int hireStaff(CommandSourceStack source, int x, int y, int z) {
        ServerLevel level = source.getLevel();
        BlockPos pos = new BlockPos(x, y, z);
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof ZooComputerBlockEntity computer)) {
            source.sendFailure(Component.literal("❌ Komputer tidak ditemukan!"));
            return 0;
        }

        if (computer.getBalance() < HIRE_COST) {
            source.sendFailure(Component.literal("❌ Saldo tidak cukup! Butuh Rp" + HIRE_COST)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        computer.addBalance(-HIRE_COST);
        StaffEntity staff = IndoZooTycoon.STAFF_ENTITY.get().create(level);
        if (staff != null) {
            staff.moveTo(pos.getX() + 1.5, pos.getY(), pos.getZ() + 1.5, 0, 0);
            level.addFreshEntity(staff);
        }

        source.sendSuccess(() -> Component.literal("✅ Zookeeper berhasil direkrut! (Rp" + HIRE_COST + ")")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    // ========== Upgrade Land ==========

    private static int upgradeLand(CommandSourceStack source, int x, int y, int z) {
        ServerLevel level = source.getLevel();
        BlockPos pos = new BlockPos(x, y, z);
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof ZooComputerBlockEntity computer)) {
            source.sendFailure(Component.literal("❌ Komputer tidak ditemukan!"));
            return 0;
        }

        if (computer.getBalance() < UPGRADE_COST) {
            source.sendFailure(Component.literal("❌ Saldo tidak cukup! Butuh Rp" + UPGRADE_COST)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        computer.addBalance(-UPGRADE_COST);
        int newRadius = computer.getScanRadius() + UPGRADE_INCREMENT;
        computer.setScanRadius(newRadius);

        source.sendSuccess(() -> Component.literal("✅ Lahan di-upgrade! Radius: "
                + newRadius + " blok (Rp" + UPGRADE_COST + ")")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    // ========== Utility ==========

    private static String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
