package com.example.simulation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ZooCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zoocmd")
                .then(Commands.literal("buy")
                        .then(Commands
                                .argument("animal_id", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(ctx -> buyAnimal(ctx.getSource(),
                                                                net.minecraft.commands.arguments.ResourceLocationArgument
                                                                        .getId(ctx, "animal_id"),
                                                                IntegerArgumentType.getInteger(ctx, "x"),
                                                                IntegerArgumentType.getInteger(ctx, "y"),
                                                                IntegerArgumentType.getInteger(ctx, "z"))))))))
                .then(Commands.literal("buyitem")
                        .then(Commands
                                .argument("item_id", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                .executes(ctx -> buyItem(ctx.getSource(),
                                                                        net.minecraft.commands.arguments.ResourceLocationArgument
                                                                                .getId(ctx, "item_id"),
                                                                        IntegerArgumentType.getInteger(ctx, "amount"),
                                                                        IntegerArgumentType.getInteger(ctx, "x"),
                                                                        IntegerArgumentType.getInteger(ctx, "y"),
                                                                        IntegerArgumentType.getInteger(ctx, "z")))))))))
                .then(Commands.literal("hire")
                        .then(Commands.literal("janitor")
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(ctx -> hireStaff(ctx.getSource(), 0,
                                                                IntegerArgumentType.getInteger(ctx, "x"),
                                                                IntegerArgumentType.getInteger(ctx, "y"),
                                                                IntegerArgumentType.getInteger(ctx, "z")))))))
                        .then(Commands.literal("zookeeper")
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(ctx -> hireStaff(ctx.getSource(), 1,
                                                                IntegerArgumentType.getInteger(ctx, "x"),
                                                                IntegerArgumentType.getInteger(ctx, "y"),
                                                                IntegerArgumentType.getInteger(ctx, "z"))))))))
                .then(Commands.literal("upgrade")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> upgradeLand(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "x"),
                                                        IntegerArgumentType.getInteger(ctx, "y"),
                                                        IntegerArgumentType.getInteger(ctx, "z")))))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> resetZoo(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "x"),
                                                        IntegerArgumentType.getInteger(ctx, "y"),
                                                        IntegerArgumentType.getInteger(ctx, "z"))))))));
    }

    // Handlers

    private static int buyAnimal(CommandSourceStack src, ResourceLocation parsedId, int x, int y, int z) {
        // ... implementation reuse ...
        return buyEntity(src, parsedId, x, y, z);
    }

    private static int buyEntity(CommandSourceStack src, ResourceLocation id, int x, int y, int z) {
        ServerLevel level = src.getLevel();
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof ZooComputerBlockEntity comp)) {
            src.sendFailure(Component.literal("Komputer tidak ditemukan!"));
            return 0;
        }

        AnimalRegistry.AnimalData data = AnimalRegistry.getAnimal(id);
        if (data == null) {
            src.sendFailure(Component.literal("Hewan '" + id + "' tidak tersedia!"));
            return 0;
        }

        if (comp.getBalance() < data.price) {
            src.sendFailure(
                    Component.literal("Saldo tidak cukup! Butuh Rp " + data.price).withStyle(ChatFormatting.RED));
            return 0;
        }

        comp.addBalance(-data.price);
        Entity entity = data.entityType.create(level);
        if (entity != null) {
            entity.moveTo(x + 0.5, y + 2, z + 0.5, 0, 0);
            level.addFreshEntity(entity);
        }
        String msg = "Berhasil beli " + data.displayName + "! (Rp " + data.price + ")";
        if (data instanceof ZAWAIntegration.ZAWAAnimalData zawaData) {
            msg += " [" + zawaData.category.toUpperCase() + "]";
        }
        final String finalMsg = msg;
        src.sendSuccess(() -> Component.literal(finalMsg).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    // Replaced buyItem logic to use ResourceLocation
    private static int buyItem(CommandSourceStack src, ResourceLocation itemId, int amount, int x, int y, int z) {
        ServerLevel level = src.getLevel();
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof ZooComputerBlockEntity comp))
            return 0;

        ZooItemRegistry.ItemData data = ZooItemRegistry.getItem(itemId.toString());
        if (data == null) {
            src.sendFailure(Component.literal("Item '" + itemId + "' tidak tersedia!"));
            return 0;
        }

        int totalCost = data.price * amount;
        if (comp.getBalance() < totalCost) {
            src.sendFailure(
                    Component.literal("Saldo tidak cukup! Butuh Rp " + totalCost).withStyle(ChatFormatting.RED));
            return 0;
        }

        comp.addBalance(-totalCost);
        ItemStack stack = new ItemStack(data.item, amount);
        ItemEntity itemEntity = new ItemEntity(level, x + 0.5, y + 1.5, z + 0.5, stack);
        itemEntity.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(itemEntity);

        src.sendSuccess(() -> Component.literal("Berhasil beli " + amount + "x " + data.displayName)
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int hireStaff(CommandSourceStack src, int role, int x, int y, int z) {
        ServerLevel level = src.getLevel();
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof ZooComputerBlockEntity comp))
            return 0;

        int cost = 2000;
        if (comp.getBalance() < cost) {
            src.sendFailure(Component.literal("Saldo tidak cukup!").withStyle(ChatFormatting.RED));
            return 0;
        }
        comp.addBalance(-cost);
        StaffEntity staff = IndoZooTycoon.STAFF_ENTITY.get().create(level);
        if (staff != null) {
            staff.moveTo(x + 1.5, y, z + 1.5, 0, 0);
            staff.setRole(role); // 0 = Janitor, 1 = Zookeeper
            level.addFreshEntity(staff);
        }
        src.sendSuccess(() -> Component.literal(role == 0 ? "Janitor direkrut!" : "Zookeeper direkrut!")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int upgradeLand(CommandSourceStack src, int x, int y, int z) {
        ServerLevel level = src.getLevel();
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof ZooComputerBlockEntity comp))
            return 0;
        if (comp.getBalance() < 5000) {
            src.sendFailure(Component.literal("Saldo tidak cukup!").withStyle(ChatFormatting.RED));
            return 0;
        }
        comp.addBalance(-5000);
        int newR = comp.getScanRadius() + 10;
        comp.setScanRadius(newR);
        src.sendSuccess(() -> Component.literal("Lahan di-upgrade! Radius: " + newR).withStyle(ChatFormatting.GREEN),
                false);
        return 1;
    }

    private static int addMoney(CommandSourceStack src, int amount, int x, int y, int z) {
        ServerLevel level = src.getLevel();
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (be instanceof ZooComputerBlockEntity comp) {
            comp.addBalance(amount);
            src.sendSuccess(() -> Component.literal("Saldo ditambah: Rp " + amount).withStyle(ChatFormatting.GREEN),
                    false);
            return 1;
        }
        return 0;
    }

    private static int resetZoo(CommandSourceStack src, int x, int y, int z) {
        ServerLevel level = src.getLevel();
        BlockEntity be = level.getBlockEntity(new BlockPos(x, y, z));
        if (be instanceof ZooComputerBlockEntity computer) {
            computer.resetProgress();
            src.sendSuccess(() -> Component.literal("Progress di-reset!").withStyle(ChatFormatting.RED), true);
            return 1;
        }
        return 0;
    }
}
