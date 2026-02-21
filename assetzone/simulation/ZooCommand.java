package com.example.simulation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ZooCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> zoocmd = Commands.literal("zoocmd");

        // Rename
        zoocmd.then(Commands.literal("rename")
                .then(Commands.argument("id", IntegerArgumentType.integer())
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> renameAnimal(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id"), StringArgumentType.getString(ctx, "name"))))));

        zoocmd.then(Commands.literal("renameuuid")
                .then(Commands.argument("uuid", StringArgumentType.word())
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> renameAnimalUuid(ctx.getSource(), StringArgumentType.getString(ctx, "uuid"), StringArgumentType.getString(ctx, "name"))))));

        // Release
        zoocmd.then(Commands.literal("release")
                .then(Commands.argument("id", IntegerArgumentType.integer())
                        .executes(ctx -> releaseAnimal(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id")))));

        zoocmd.then(Commands.literal("releaseuuid")
                .then(Commands.argument("uuid", StringArgumentType.word())
                        .executes(ctx -> releaseAnimalUuid(ctx.getSource(), StringArgumentType.getString(ctx, "uuid")))));

        // Buy Animal
        zoocmd.then(Commands.literal("buy")
                .then(Commands.argument("animal_id", ResourceLocationArgument.id())
                        .executes(ctx -> buyGlobal(ctx.getSource(), ResourceLocationArgument.getId(ctx, "animal_id")))
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> buyAnimal(ctx.getSource(), ResourceLocationArgument.getId(ctx, "animal_id"),
                                                        IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z"))))))));

        // Buy Item
        zoocmd.then(Commands.literal("buyitem")
                .then(Commands.argument("item_id", ResourceLocationArgument.id())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> buyItemGlobal(ctx.getSource(), ResourceLocationArgument.getId(ctx, "item_id"), IntegerArgumentType.getInteger(ctx, "amount")))
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(ctx -> buyItem(ctx.getSource(), ResourceLocationArgument.getId(ctx, "item_id"), IntegerArgumentType.getInteger(ctx, "amount"),
                                                                IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z")))))))));

        // Hire
        zoocmd.then(Commands.literal("hire")
                .then(Commands.literal("janitor")
                        .executes(ctx -> hireGlobal(ctx.getSource(), 0))
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> hireStaff(ctx.getSource(), 0, IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z")))))))
                .then(Commands.literal("zookeeper")
                        .executes(ctx -> hireGlobal(ctx.getSource(), 1))
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> hireStaff(ctx.getSource(), 1, IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z")))))))
                .then(Commands.literal("security")
                        .executes(ctx -> hireGlobal(ctx.getSource(), 2))
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> hireStaff(ctx.getSource(), 2, IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z")))))))
                .then(Commands.literal("cashier")
                        .executes(ctx -> hireCashierGlobal(ctx.getSource(), srcOrPlayerPos(ctx.getSource())))
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> hireCashier(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z"))))))));

        // Reset
        zoocmd.then(Commands.literal("reset")
                .executes(ctx -> resetGlobal(ctx.getSource()))
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(ctx -> resetZoo(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z")))))));

        // Add Money
        zoocmd.then(Commands.literal("addmoney")
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(ctx -> addMoneyGlobal(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> addMoney(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount"), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z"))))))));

        // Upgrade
        zoocmd.then(Commands.literal("upgrade")
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(ctx -> upgradeLand(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z")))))));

        // Set Rating
        zoocmd.then(Commands.literal("setrating")
                .then(Commands.argument("rating", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> setRatingGlobal(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "rating")))));

        // Set Biome
        zoocmd.then(Commands.literal("setbiome")
                .then(Commands.argument("biome_id", ResourceLocationArgument.id())
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> setBiome(ctx.getSource(), ResourceLocationArgument.getId(ctx, "biome_id"), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z"), null))
                                                .then(Commands.literal("replace")
                                                        .then(Commands.argument("filter", ResourceLocationArgument.id())
                                                                .executes(ctx -> setBiome(ctx.getSource(), ResourceLocationArgument.getId(ctx, "biome_id"), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z"), ResourceLocationArgument.getId(ctx, "filter"))))))))));

        // Debug Kill / Cleanup
        zoocmd.then(Commands.literal("killallhunter")
                .executes(ctx -> killAllVisitors(ctx.getSource(), true)));
        zoocmd.then(Commands.literal("killallvisitor")
                .executes(ctx -> killAllVisitors(ctx.getSource(), false)));
        zoocmd.then(Commands.literal("killallsecurity")
                .executes(ctx -> killAllStaffByRole(ctx.getSource(), 2)));
        zoocmd.then(Commands.literal("killallzookeeper")
                .executes(ctx -> killAllStaffByRole(ctx.getSource(), 1)));
        zoocmd.then(Commands.literal("killalljanitor")
                .executes(ctx -> killAllStaffByRole(ctx.getSource(), 0)));
        zoocmd.then(Commands.literal("removetrash")
                .executes(ctx -> removeAllTrash(ctx.getSource())));

        dispatcher.register(zoocmd);
    }

    // Handlers

    private static int buyGlobal(CommandSourceStack src, ResourceLocation id) {
        if (src.getEntity() == null) {
            src.sendFailure(Component.literal("Harus dijalankan oleh player!"));
            return 0;
        }
        BlockPos pos = src.getEntity().blockPosition();
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

    private static BlockPos srcOrPlayerPos(CommandSourceStack src) {
        if (src.getEntity() != null) return src.getEntity().blockPosition();
        return BlockPos.containing(src.getPosition());
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

    private static int killAllVisitors(CommandSourceStack src, boolean huntersOnly) {
        ServerLevel level = src.getLevel();
        final int[] removed = new int[]{0};
        for (Entity e : level.getAllEntities()) {
            if (e instanceof VisitorEntity v) {
                if (huntersOnly && !v.isHunter()) continue;
                e.remove(Entity.RemovalReason.KILLED);
                removed[0]++;
            }
        }
        ZooData data = ZooData.get(level);
        data.updateCounts(level);
        SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
        src.sendSuccess(() -> Component.literal("Menghapus " + removed[0] + " visitor" + (huntersOnly ? " (hunter)" : "")), true);
        return removed[0];
    }

    private static int killAllStaffByRole(CommandSourceStack src, int role) {
        ServerLevel level = src.getLevel();
        final int[] removed = new int[]{0};
        for (Entity e : level.getAllEntities()) {
            if (e instanceof StaffEntity s && s.getRole() == role) {
                e.remove(Entity.RemovalReason.KILLED);
                removed[0]++;
            }
        }
        ZooData data = ZooData.get(level);
        data.updateCounts(level);
        SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
        String roleName = switch (role) {
            case 0 -> "janitor";
            case 1 -> "zookeeper";
            case 2 -> "security";
            default -> "staff";
        };
        src.sendSuccess(() -> Component.literal("Menghapus " + removed[0] + " " + roleName), true);
        return removed[0];
    }

    private static int removeAllTrash(CommandSourceStack src) {
        ServerLevel level = src.getLevel();
        final int[] removed = new int[]{0};
        BlockPos playerPos = src.getEntity() != null ? src.getEntity().blockPosition() : BlockPos.ZERO;
        int radius = 128;
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-radius, -16, -radius), playerPos.offset(radius, 16, radius))) {
            if (level.getBlockState(pos).is(IndoZooTycoon.TRASH_BLOCK.get())) {
                level.destroyBlock(pos, false);
                removed[0]++;
            }
        }
        ZooData data = ZooData.get(level);
        data.setTrashCount(0);
        SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
        src.sendSuccess(() -> Component.literal("Menghapus " + removed[0] + " blok sampah"), true);
        return removed[0];
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
        int cost = (role == 2) ? 3000000 : 2000000;

        if (zooData.getBalance() < cost) {
            src.sendFailure(Component.literal("Saldo tidak cukup!")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        zooData.addBalance(-cost);
        StaffEntity staff = IndoZooTycoon.STAFF_ENTITY.get().create(level);
        if (staff != null) {
            BlockPos spawnPos = pos;
            if (!zooData.getEntrances().isEmpty()) {
                spawnPos = zooData.getRandomEntrance();
            }
            staff.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
            staff.setRole(role);
            staff.setGatePos(spawnPos);
            level.addFreshEntity(staff);
        }
        src.sendSuccess(() -> Component.literal(role == 0 ? "Janitor Hired!" : role == 1 ? "Zookeeper Hired!" : "Security Hired!")
                .withStyle(ChatFormatting.GREEN), false);
        // Immediate update
        zooData.updateCounts(level);
        SyncBalancePacket packet = new SyncBalancePacket(zooData.getBalance(), zooData.getTaggedAnimals(),
                zooData.getAnimalCount(), zooData.getStaffCount(), zooData.getVisitorCount(), zooData.getRating());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
        return 1;
    }

    private static int hireCashierGlobal(CommandSourceStack src, BlockPos pos) {
        ServerLevel level = src.getLevel();
        ZooData zooData = ZooData.get(level);
        int cost = 2_500_000;

        if (zooData.getBalance() < cost) {
            src.sendFailure(Component.literal("Saldo tidak cukup!")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        zooData.addBalance(-cost);
        CashierEntity cashier = IndoZooTycoon.CASHIER_ENTITY.get().create(level);
        if (cashier != null) {
            BlockPos spawnPos = pos;
            if (!zooData.getEntrances().isEmpty()) {
                spawnPos = zooData.getRandomEntrance();
            }
            cashier.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
            level.addFreshEntity(cashier);
        }

        src.sendSuccess(() -> Component.literal("Cashier Hired!")
                .withStyle(ChatFormatting.GREEN), false);
        zooData.updateCounts(level);
        SyncBalancePacket packet = new SyncBalancePacket(zooData.getBalance(), zooData.getTaggedAnimals(),
                zooData.getAnimalCount(), zooData.getStaffCount(), zooData.getVisitorCount(), zooData.getRating());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
        return 1;
    }

    private static int buyAnimal(CommandSourceStack src, ResourceLocation parsedId, int x, int y, int z) {
        return buyEntity(src, parsedId, x, y, z);
    }

    private static int buyEntity(CommandSourceStack src, ResourceLocation id, int x, int y, int z) {
        return buyEntityLegacy(src, id, x, y, z);
    }

    private static int buyEntityLegacy(CommandSourceStack src, ResourceLocation id, int x, int y, int z) {
        ServerLevel level = src.getLevel();
        ZooData data = ZooData.get(level);
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

    private static int buyItem(CommandSourceStack src, ResourceLocation itemId, int amount, int x, int y, int z) {
        return buyItemGlobalLogic(src, itemId, amount, new BlockPos(x, y, z));
    }

    private static int hireStaff(CommandSourceStack src, int role, int x, int y, int z) {
        return hireStaffGlobal(src, role, new BlockPos(x, y, z));
    }

    private static int hireCashier(CommandSourceStack src, int x, int y, int z) {
        return hireCashierGlobal(src, new BlockPos(x, y, z));
    }

    private static int upgradeLand(CommandSourceStack src, int x, int y, int z) {
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
        int r = 15;

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

                Entity e = null;
                if (tag.contains("uuid")) {
                    e = findEntityByUuid(src.getServer(), tag.getString("uuid"));
                    if (e != null && e.getId() != id) {
                        tag.putInt("id", e.getId());
                        data.setDirty();
                    }
                }
                if (e == null) e = level.getEntity(id);
                if (e == null) {
                    e = findEntityForTaggedEntry(src.getServer(), tag);
                    if (e != null) {
                        tag.putString("uuid", e.getUUID().toString());
                        tag.putInt("id", e.getId());
                        data.setDirty();
                    }
                }
                if (e != null) {
                    e.setCustomName(Component.literal(name).withStyle(ChatFormatting.GOLD));
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
        Entity e = level.getEntity(id);
        if (e == null) {
            net.minecraft.nbt.ListTag list = data.getTaggedAnimals();
            for (int i = 0; i < list.size(); i++) {
                net.minecraft.nbt.CompoundTag tag = list.getCompound(i);
                if (tag.getInt("id") == id && tag.contains("uuid")) {
                    e = findEntityByUuid(src.getServer(), tag.getString("uuid"));
                    if (e != null) break;
                }
                if (tag.getInt("id") == id && e == null) {
                    e = findEntityForTaggedEntry(src.getServer(), tag);
                    if (e != null) {
                        tag.putString("uuid", e.getUUID().toString());
                        tag.putInt("id", e.getId());
                        data.setDirty();
                        break;
                    }
                }
            }
        }
        data.removeAnimal(id);
        src.sendSuccess(() -> Component.literal("Hewan di-lepaskan!"), true);
        SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
        return 1;
    }

    private static int renameAnimalUuid(CommandSourceStack src, String uuidStr, String name) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (Exception e) {
            return 0;
        }
        ServerLevel level = src.getLevel();
        ZooData data = ZooData.get(level);
        net.minecraft.nbt.ListTag list = data.getTaggedAnimals();
        for (int i = 0; i < list.size(); i++) {
            net.minecraft.nbt.CompoundTag tag = list.getCompound(i);
            if (uuidStr.equals(tag.getString("uuid"))) {
                tag.putString("name", name);
                data.setDirty();
                Entity e = findEntityByUuid(src.getServer(), uuid);
                if (e != null) {
                    tag.putInt("id", e.getId());
                    data.setDirty();
                    e.setCustomName(Component.literal(name).withStyle(ChatFormatting.GOLD));
                    e.setCustomNameVisible(true);
                }
                SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                        data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                return 1;
            }
        }
        return 0;
    }

    private static int releaseAnimalUuid(CommandSourceStack src, String uuidStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (Exception e) {
            return 0;
        }
        ServerLevel level = src.getLevel();
        ZooData data = ZooData.get(level);
        data.removeAnimalByUuid(uuidStr);
        SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
        return 1;
    }

    private static Entity findEntityByUuid(MinecraftServer server, String uuidStr) {
        try {
            return findEntityByUuid(server, UUID.fromString(uuidStr));
        } catch (Exception e) {
            return null;
        }
    }

    private static Entity findEntityByUuid(MinecraftServer server, UUID uuid) {
        if (server == null || uuid == null) return null;
        for (ServerLevel sl : server.getAllLevels()) {
            Entity e = sl.getEntity(uuid);
            if (e != null) return e;
        }
        return null;
    }

    private static Entity findEntityForTaggedEntry(MinecraftServer server, net.minecraft.nbt.CompoundTag tagged) {
        if (server == null || tagged == null) return null;
        String typeStr = tagged.contains("type") ? tagged.getString("type") : "";
        String nameStr = tagged.contains("name") ? tagged.getString("name") : "";
        for (ServerLevel sl : server.getAllLevels()) {
            for (Entity e : sl.getAllEntities()) {
                if (e.isRemoved()) continue;
                if (!e.getPersistentData().getBoolean("ZooAnimal")) continue;
                if (!typeStr.isEmpty()) {
                    ResourceLocation typeId = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
                    if (typeId == null || !typeStr.equals(typeId.toString())) continue;
                }
                if (!nameStr.isEmpty()) {
                    Component cn = e.getCustomName();
                    if (cn == null || !nameStr.equals(cn.getString())) continue;
                }
                return e;
            }
        }
        return null;
    }

    private static int setRatingGlobal(CommandSourceStack src, int rating) {
        ServerLevel level = src.getLevel();
        ZooData data = ZooData.get(level);
        data.setRating(rating);
        src.sendSuccess(() -> Component.literal("Rating di-set menjadi: " + rating), true);
        SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
        return 1;
    }
}
