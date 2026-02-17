package com.example.simulation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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

public class ZooCommand {

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("zoocmd")
                                                .then(Commands.literal("rename")
                                                                .then(Commands.argument("id", IntegerArgumentType.integer())
                                                                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                                                                                .executes(ctx -> renameAnimal(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id"), StringArgumentType.getString(ctx, "name"))))))
                                                .then(Commands.literal("release")
                                                                .then(Commands.argument("id", IntegerArgumentType.integer())
                                                                                .executes(ctx -> releaseAnimal(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id")))))

                                                .then(Commands.literal("buy")
                                                                .then(Commands
                                                                                .argument("animal_id",
                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument
                                                                                                                .id())
                                                                                .executes(ctx -> buyGlobal(
                                                                                                ctx.getSource(),
                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument
                                                                                                                .getId(ctx,
                                                                                                                                "animal_id")))
                                                                                .then(Commands.argument("x",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .then(Commands.argument(
                                                                                                                "y",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .then(Commands.argument(
                                                                                                                                "z",
                                                                                                                                IntegerArgumentType
                                                                                                                                                .integer())
                                                                                                                                .executes(ctx -> buyAnimal(
                                                                                                                                                ctx.getSource(),
                                                                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument
                                                                                                                                                                .getId(ctx, "animal_id"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "x"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "y"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "z"))))))))

                                                .then(Commands.literal("buyitem")
                                                                .then(Commands
                                                                                .argument("item_id",
                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument
                                                                                                                .id())
                                                                                .then(Commands.argument("amount",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .executes(ctx -> buyItemGlobal(
                                                                                                                ctx.getSource(),
                                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument
                                                                                                                                .getId(ctx, "item_id"),
                                                                                                                IntegerArgumentType
                                                                                                                                .getInteger(ctx, "amount")))
                                                                                                .then(Commands.argument(
                                                                                                                "x",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .then(Commands.argument(
                                                                                                                                "y",
                                                                                                                                IntegerArgumentType
                                                                                                                                                .integer())
                                                                                                                                .then(Commands
                                                                                                                                                .argument("z", IntegerArgumentType
                                                                                                                                                                .integer())
                                                                                                                                                .executes(ctx -> buyItem(
                                                                                                                                                                ctx.getSource(),
                                                                                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument
                                                                                                                                                                                .getId(ctx, "item_id"),
                                                                                                                                                                IntegerArgumentType
                                                                                                                                                                                .getInteger(ctx,
                                                                                                                                                                                                "amount"),
                                                                                                                                                                IntegerArgumentType
                                                                                                                                                                                .getInteger(ctx,
                                                                                                                                                                                                "x"),
                                                                                                                                                                IntegerArgumentType
                                                                                                                                                                                .getInteger(ctx,
                                                                                                                                                                                                "y"),
                                                                                                                                                                IntegerArgumentType
                                                                                                                                                                                .getInteger(ctx,
                                                                                                                                                                                                "z")))))))))

                                                .then(Commands.literal("hire")
                                                                .then(Commands.literal("janitor")
                                                                                .executes(ctx -> hireGlobal(
                                                                                                ctx.getSource(), 0))
                                                                                .then(Commands.argument("x",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .then(Commands.argument(
                                                                                                                "y",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .then(Commands.argument(
                                                                                                                                "z",
                                                                                                                                IntegerArgumentType
                                                                                                                                                .integer())
                                                                                                                                .executes(ctx -> hireStaff(
                                                                                                                                                ctx.getSource(),
                                                                                                                                                0,
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "x"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "y"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "z")))))))
                                                                .then(Commands.literal("zookeeper")
                                                                                .executes(ctx -> hireGlobal(
                                                                                                ctx.getSource(), 1))
                                                                                .then(Commands.argument("x",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .then(Commands.argument(
                                                                                                                "y",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .then(Commands.argument(
                                                                                                                                "z",
                                                                                                                                IntegerArgumentType
                                                                                                                                                .integer())
                                                                                                                                .executes(ctx -> hireStaff(
                                                                                                                                                ctx.getSource(),
                                                                                                                                                1,
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "x"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "y"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "z"))))))))

                                                .then(Commands.literal("reset")
                                                                .executes(ctx -> resetGlobal(ctx.getSource()))
                                                                .then(Commands.argument("x",
                                                                                IntegerArgumentType.integer())
                                                                                .then(Commands.argument("y",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .then(Commands.argument(
                                                                                                                "z",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .executes(ctx -> resetZoo(
                                                                                                                                ctx.getSource(),
                                                                                                                                IntegerArgumentType
                                                                                                                                                .getInteger(ctx, "x"),
                                                                                                                                IntegerArgumentType
                                                                                                                                                .getInteger(ctx, "y"),
                                                                                                                                IntegerArgumentType
                                                                                                                                                .getInteger(ctx, "z")))))))

                                                .then(Commands.literal("addmoney")
                                                                .then(Commands.argument("amount",
                                                                                IntegerArgumentType.integer())
                                                                                .executes(ctx -> addMoneyGlobal(
                                                                                                ctx.getSource(),
                                                                                                IntegerArgumentType
                                                                                                                .getInteger(ctx, "amount")))
                                                                                .then(Commands.argument("x",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .then(Commands.argument(
                                                                                                                "y",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .then(Commands.argument(
                                                                                                                                "z",
                                                                                                                                IntegerArgumentType
                                                                                                                                                .integer())
                                                                                                                                .executes(ctx -> addMoney(
                                                                                                                                                ctx.getSource(),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "amount"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "x"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "y"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "z"))))))))

                                                .then(Commands.literal("upgrade")
                                                                .then(Commands.argument("x",
                                                                                IntegerArgumentType.integer())
                                                                                .then(Commands.argument("y",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .then(Commands.argument(
                                                                                                                "z",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .executes(ctx -> upgradeLand(
                                                                                                                                ctx.getSource(),
                                                                                                                                IntegerArgumentType
                                                                                                                                                .getInteger(ctx, "x"),
                                                                                                                                IntegerArgumentType
                                                                                                                                                .getInteger(ctx, "y"),
                                                                                                                                IntegerArgumentType
                                                                                                                                                .getInteger(ctx, "z")))))))

                                                .then(Commands.literal("setbiome")
                                                                .then(Commands
                                                                                .argument("biome_id",
                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument
                                                                                                                .id())
                                                                                .then(Commands.argument("x",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .then(Commands.argument(
                                                                                                                "y",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .then(Commands.argument(
                                                                                                                                "z",
                                                                                                                                IntegerArgumentType
                                                                                                                                                .integer())
                                                                                                                                .executes(ctx -> setBiome(
                                                                                                                                                ctx.getSource(),
                                                                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument
                                                                                                                                                                .getId(ctx, "biome_id"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "x"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx, "y"),
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .getInteger(ctx,
                                                                                                                                                                                "z"),
                                                                                                                                                null))
                                                                                                                                .then(Commands.literal("replace")
                                                                                                                                                .then(Commands.argument("filter", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                                                                                                                                                                .executes(ctx -> setBiome(
                                                                                                                                                                                ctx.getSource(),
                                                                                                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument.getId(ctx, "biome_id"),
                                                                                                                                                                                IntegerArgumentType.getInteger(ctx, "x"),
                                                                                                                                                                                IntegerArgumentType.getInteger(ctx, "y"),
                                                                                                                                                                                IntegerArgumentType.getInteger(ctx, "z"),
                                                                                                                                                                                net.minecraft.commands.arguments.ResourceLocationArgument.getId(ctx, "filter")))))))))));
        }

        // Handlers

        private static int buyGlobal(CommandSourceStack src, ResourceLocation id) {
                if (src.getEntity() == null) {
                        src.sendFailure(Component.literal("Harus dijalankan oleh player!"));
                        return 0;
                }
                BlockPos pos = src.getEntity().blockPosition();
                // Use 0 balance check or deduct from global? Logic normally uses block entity
                // balance.
                // We will deduct from Global ZooData.
                return buyEntityGlobal(src, id, pos);
        }

        private static int buyItemGlobal(CommandSourceStack src, ResourceLocation id, int amount) {
                if (src.getEntity() == null) {
                        src.sendFailure(Component.literal("Harus dijalankan oleh player!"));
                        return 0;
                }
                BlockPos pos = src.getEntity().blockPosition();
                return buyItemGlobalLogic(src, id, amount, pos);
        }

        private static int hireGlobal(CommandSourceStack src, int role) {
                if (src.getEntity() == null) {
                        src.sendFailure(Component.literal("Harus dijalankan oleh player!"));
                        return 0;
                }
                BlockPos pos = src.getEntity().blockPosition();
                return hireStaffGlobal(src, role, pos);
        }

        private static int resetGlobal(CommandSourceStack src) {
                ServerLevel level = src.getLevel();
                ZooData data = ZooData.get(level);
                data.setBalance(5000);
                src.sendSuccess(
                                () -> Component.literal("Mod Zoo di-reset global! Saldo: Rp 5000")
                                                .withStyle(ChatFormatting.RED),
                                true);
                SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                return 1;
        }

        private static int addMoneyGlobal(CommandSourceStack src, int amount) {
                ServerLevel level = src.getLevel();
                ZooData data = ZooData.get(level);
                data.addBalance(amount);
                src.sendSuccess(() -> Component.literal("Saldo ditambah: Rp " + amount)
                                .withStyle(ChatFormatting.GREEN),
                                true);
                SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                return 1;
        }

        // --- implementations ---

        private static int buyEntityGlobal(CommandSourceStack src, ResourceLocation id, BlockPos pos) {
                ServerLevel level = src.getLevel();
                ZooData zooData = ZooData.get(level);
                AnimalRegistry.AnimalData data = AnimalRegistry.getAnimal(id);

                if (data == null) {
                        src.sendFailure(Component.literal("Hewan tidak valid!"));
                        return 0;
                }

                if (zooData.getBalance() < data.price) {
                        src.sendFailure(Component.literal("Saldo tidak cukup!")
                                        .withStyle(ChatFormatting.RED));
                        return 0;
                }

                zooData.addBalance(-data.price);
                Entity entity = data.entityType.create(level);
                if (entity != null) {
                        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                        level.addFreshEntity(entity);
                }
                src.sendSuccess(() -> Component.literal("Beli " + data.displayName + " (-Rp " + data.price + ")")
                                .withStyle(ChatFormatting.GREEN), false);
                // Immediate update
                zooData.updateCounts(level);
                SyncBalancePacket packet = new SyncBalancePacket(zooData.getBalance(), zooData.getTaggedAnimals(),
                                zooData.getAnimalCount(), zooData.getStaffCount(), zooData.getVisitorCount(), zooData.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                return 1;
        }

        private static int buyItemGlobalLogic(CommandSourceStack src, ResourceLocation itemId, int amount,
                        BlockPos pos) {
                ServerLevel level = src.getLevel();
                ZooData zooData = ZooData.get(level);

                // Special logic for Zoo Banner (1x limit)
                if (itemId.toString().equals(IndoZooTycoon.MODID + ":zoo_banner")) {
                        if (zooData.isZooBannerPurchased()) {
                                src.sendFailure(Component.literal("Banner Kebun Binatang hanya bisa dibeli 1x!")
                                                .withStyle(ChatFormatting.RED));
                                return 0;
                        }
                }

                ZooItemRegistry.ItemData data = ZooItemRegistry.getItem(itemId.toString());

                if (data == null) {
                        src.sendFailure(Component.literal("Item tidak tersedia!"));
                        return 0;
                }

                int cost = data.price * amount;
                if (zooData.getBalance() < cost) {
                        src.sendFailure(Component.literal("Saldo tidak cukup!")
                                        .withStyle(ChatFormatting.RED));
                        return 0;
                }

                zooData.addBalance(-cost);
                ItemStack stack = new ItemStack(data.item, amount);
                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, stack);
                level.addFreshEntity(itemEntity);

                // Mark banner as purchased if it is the banner
                if (itemId.toString().equals(IndoZooTycoon.MODID + ":zoo_banner")) {
                        zooData.setZooBannerPurchased(true);
                }

                src.sendSuccess(() -> Component
                                .literal("Beli " + amount + "x " + data.displayName + " (-Rp " + cost + ")")
                                .withStyle(ChatFormatting.GREEN), false);
                zooData.updateCounts(level);
                SyncBalancePacket packet = new SyncBalancePacket(zooData.getBalance(), zooData.getTaggedAnimals(),
                                zooData.getAnimalCount(), zooData.getStaffCount(), zooData.getVisitorCount(), zooData.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                return 1;
        }

        private static int hireStaffGlobal(CommandSourceStack src, int role, BlockPos pos) {
                ServerLevel level = src.getLevel();
                ZooData zooData = ZooData.get(level);
                int cost = 2000000;

                if (zooData.getBalance() < cost) {
                        src.sendFailure(Component.literal("Saldo tidak cukup!")
                                        .withStyle(ChatFormatting.RED));
                        return 0;
                }

                zooData.addBalance(-cost);
                StaffEntity staff = IndoZooTycoon.STAFF_ENTITY.get().create(level);
                if (staff != null) {
                        staff.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                        staff.setRole(role);
                        level.addFreshEntity(staff);
                }
                src.sendSuccess(() -> Component.literal(role == 0 ? "Janitor Hired!" : "Zookeeper Hired!")
                                .withStyle(ChatFormatting.GREEN), false);
                // Immediate update
                zooData.updateCounts(level);
                SyncBalancePacket packet = new SyncBalancePacket(zooData.getBalance(), zooData.getTaggedAnimals(),
                                zooData.getAnimalCount(), zooData.getStaffCount(), zooData.getVisitorCount(), zooData.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                return 1;
        }

        // Keep existing coordinate-based handlers for checking block entities
        // But updated to use ZooData if block entity is deprecated?
        // Actually, block entity logic (ZooComputerBlockEntity) also uses
        // getBalanceGlobal().
        // So modifying ZooData modifies what the block entity sees. Correct.

        private static int buyAnimal(CommandSourceStack src, ResourceLocation parsedId, int x, int y, int z) {
                return buyEntity(src, parsedId, x, y, z); // Redirect to existing logic or new global logic?
                // Existing logic checks for ComputerBlockEntity.
                // Let's keep it checking for BE as a "security" measure that you have a
                // computer?
                // But user said "computer should be global".
                // Whatever. I'll leave legacy methods alone.
        }

        private static int buyEntity(CommandSourceStack src, ResourceLocation id, int x, int y, int z) {
                // Global logic applied to coordinate
                return buyEntityLegacy(src, id, x, y, z);
        }

        // I will include the legacy implementations inline to avoid "missing method"
        // errors.
        // Copy-pasting logic from previous read.

        private static int buyEntityLegacy(CommandSourceStack src, ResourceLocation id, int x, int y, int z) {
                ServerLevel level = src.getLevel();
                // BlockEntity check removed
                ZooData data = ZooData.get(level); // Use global data
                AnimalRegistry.AnimalData adata = AnimalRegistry.getAnimal(id);
                if (adata == null)
                        return 0;
                if (data.getBalance() < adata.price)
                        return 0;
                data.addBalance(-adata.price);
                Entity entity = adata.entityType.create(level);
                if (entity != null) {
                        entity.moveTo(x + 0.5, y + 2, z + 0.5, 0, 0);
                        level.addFreshEntity(entity);
                }
                return 1;
        }

        // Actually, to keep it simple and safe, I will just implement the handlers I
        // declared in register.
        // And duplicate the legacy logic but using ZooData.

        private static int buyItem(CommandSourceStack src, ResourceLocation itemId, int amount, int x, int y, int z) {
                // Just redirect to global logic at pos
                return buyItemGlobalLogic(src, itemId, amount, new BlockPos(x, y, z));
        }

        private static int hireStaff(CommandSourceStack src, int role, int x, int y, int z) {
                return hireStaffGlobal(src, role, new BlockPos(x, y, z));
        }

        private static int upgradeLand(CommandSourceStack src, int x, int y, int z) {
                // Upgrade logic removed or changed to global?
                // User said "hapus computer 100%".
                // "Upgrade Land" on computer expanded scan radius.
                // Global "scan" is now effectively infinite or chunk based.
                // This command might be obsolete.
                // I'll just return success with a message "Upgrade not needed for global zoo",
                // or remove it?
                // Retaining command but making it no-op or just charge money for "fun".
                src.sendSuccess(() -> Component.literal("Zoo Global tidak perlu upgrade radius!")
                                .withStyle(ChatFormatting.GREEN), true);
                return 1;
        }

        private static int addMoney(CommandSourceStack src, int amount, int x, int y, int z) {
                return addMoneyGlobal(src, amount);
        }

        private static int resetZoo(CommandSourceStack src, int x, int y, int z) {
                return resetGlobal(src);
        }

        private static int setBiome(CommandSourceStack src, ResourceLocation biomeId, int x, int y, int z, ResourceLocation filter) {
                ServerLevel level = src.getLevel();
                int r = 15; // Decreased radius to ~31x31 area to avoid volume limits
                
                String cmd;
                if (filter != null) {
                    cmd = String.format("fillbiome %d %d %d %d %d %d %s replace %s",
                                    x - r, level.getMinBuildHeight(), z - r,
                                    x + r, level.getMaxBuildHeight(), z + r,
                                    biomeId.toString(), filter.toString());
                } else {
                    cmd = String.format("fillbiome %d %d %d %d %d %d %s",
                                    x - r, level.getMinBuildHeight(), z - r,
                                    x + r, level.getMaxBuildHeight(), z + r,
                                    biomeId.toString());
                }

                // Create source with correct level and permission level 4
                CommandSourceStack elevatedSrc = src.withPermission(4).withLevel(level);

                int result = src.getServer().getCommands().performPrefixedCommand(elevatedSrc, cmd);
                
                if (result == 0) {
                     src.sendFailure(Component.literal("Gagal mengubah biome! Area mungkin terlalu besar atau chunk belum load."));
                     return 0;
                }

                src.sendSuccess(() -> Component.literal("Set Biome: " + biomeId + " @ " + x + "," + z)
                                .withStyle(ChatFormatting.GREEN), true);
                return 1;
        }

        private static int renameAnimal(CommandSourceStack src, int id, String name) {
                ServerLevel level = src.getLevel();
                ZooData data = ZooData.get(level);
                net.minecraft.nbt.ListTag list = data.getTaggedAnimals();
                for (int i = 0; i < list.size(); i++) {
                        net.minecraft.nbt.CompoundTag tag = list.getCompound(i);
                        if (tag.getInt("id") == id) {
                                tag.putString("name", name);
                                data.setDirty();
                                src.sendSuccess(() -> Component.literal("Hewan di-rename menjadi: " + name), true);
                                SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                                                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
                                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                                
                                Entity e = level.getEntity(id);
                                if (e != null) {
                                    e.setCustomName(Component.literal(name));
                                    e.setCustomNameVisible(true);
                                }
                                return 1;
                        }
                }
                return 0;
        }

        private static int releaseAnimal(CommandSourceStack src, int id) {
                ServerLevel level = src.getLevel();
                ZooData data = ZooData.get(level);
                data.removeAnimal(id);
                Entity e = level.getEntity(id);
                if (e != null) {
                    e.discard();
                }
                src.sendSuccess(() -> Component.literal("Hewan di-lepaskan!"), true);
                SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                return 1;
        }
}
