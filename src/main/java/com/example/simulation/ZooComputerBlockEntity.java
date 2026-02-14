package com.example.simulation;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Otak Mod — ZooComputerBlockEntity
 *
 * Setiap 400 tick (20 detik):
 * 1. Scan area → hitung hewan (Animal.class)
 * 2. Hitung staff, visitor, sampah
 * 3. Kalkulasi Profit = Income − Expense
 * 4. Spawn visitor jika memenuhi syarat
 * 5. Visitor buang sampah jika kurang tempat sampah (Composter)
 */
public class ZooComputerBlockEntity extends BlockEntity {

    // ========== Persistent Data ==========
    private long balance = 5000; // Saldo awal Rp 5.000
    private int scanRadius = 20; // Radius awal 20 blok
    private int tickCounter = 0;

    // ========== Transient cache (updated setiap cycle) ==========
    private int cachedAnimalCount = 0;
    private int cachedStaffCount = 0;
    private int cachedVisitorCount = 0;
    private int cachedTrashCount = 0;

    // ========== Ekonomi Constants ==========
    private static final int TICK_INTERVAL = 400; // 20 detik
    private static final long INCOME_PER_ANIMAL = 500;
    private static final long EXPENSE_PER_STAFF = 200;
    private static final long EXPENSE_PER_TRASH = 100;
    private static final long FOOD_COST = 100;

    // ========== Visitor / Trash Constants ==========
    private static final int MAX_VISITORS = 5;
    private static final int MIN_ANIMALS_FOR_VISITORS = 2;

    public ZooComputerBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.ZOO_COMPUTER_BE.get(), pos, state);
    }

    // ========== NBT Save / Load ==========

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Balance", balance);
        tag.putInt("ScanRadius", scanRadius);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        balance = tag.getLong("Balance");
        scanRadius = tag.getInt("ScanRadius");
        if (scanRadius <= 0)
            scanRadius = 20;
    }

    // ========== Server Tick ==========

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            ZooComputerBlockEntity be) {
        if (level.isClientSide())
            return;
        be.tickCounter++;
        if (be.tickCounter < TICK_INTERVAL)
            return;
        be.tickCounter = 0;

        ServerLevel serverLevel = (ServerLevel) level;
        AABB area = new AABB(pos).inflate(be.scanRadius);

        // --- 1. Scan Hewan (Universal Detection) ---
        List<Animal> animals = serverLevel.getEntitiesOfClass(Animal.class, area);
        be.cachedAnimalCount = animals.size();

        // --- 2. Scan Staff ---
        List<StaffEntity> staff = serverLevel.getEntitiesOfClass(StaffEntity.class, area);
        be.cachedStaffCount = staff.size();

        // --- 3. Scan Visitor ---
        List<VisitorEntity> visitors = serverLevel.getEntitiesOfClass(VisitorEntity.class, area);
        be.cachedVisitorCount = visitors.size();

        // --- 4. Scan Sampah (ItemEntity bernama "Sampah") ---
        List<ItemEntity> items = serverLevel.getEntitiesOfClass(ItemEntity.class, area);
        int trashCount = 0;
        for (ItemEntity ie : items) {
            if (ie.getItem().getItem() == Items.SLIME_BALL
                    && ie.getItem().hasCustomHoverName()
                    && ie.getItem().getHoverName().getString().equals("Sampah")) {
                trashCount++;
            }
        }
        be.cachedTrashCount = trashCount;

        // --- 5. Kalkulasi Ekonomi ---
        long income = be.cachedAnimalCount * INCOME_PER_ANIMAL;
        long expense = (be.cachedStaffCount * EXPENSE_PER_STAFF)
                + (be.cachedTrashCount * EXPENSE_PER_TRASH);
        long profit = income - expense;
        be.balance += profit;
        be.setChanged();

        // --- 6. Spawn Visitor ---
        if (be.cachedAnimalCount >= MIN_ANIMALS_FOR_VISITORS
                && be.cachedVisitorCount < MAX_VISITORS) {
            VisitorEntity visitor = IndoZooTycoon.VISITOR_ENTITY.get().create(serverLevel);
            if (visitor != null) {
                visitor.moveTo(pos.getX() + 2, pos.getY(), pos.getZ() + 2, 0, 0);
                serverLevel.addFreshEntity(visitor);
            }
        }

        // --- 7. Visitor buang sampah? ---
        // Hitung Composter di area sebagai tong sampah
        int composterCount = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int r = be.scanRadius;
        for (int x = pos.getX() - r; x <= pos.getX() + r; x++) {
            for (int z = pos.getZ() - r; z <= pos.getZ() + r; z++) {
                mutablePos.set(x, pos.getY(), z);
                if (serverLevel.getBlockState(mutablePos)
                        .getBlock() == net.minecraft.world.level.block.Blocks.COMPOSTER) {
                    composterCount++;
                }
            }
        }
        // Rumus: jika visitor > composter * 5, peluang buang sampah
        if (be.cachedVisitorCount > composterCount * 5) {
            for (VisitorEntity v : visitors) {
                if (serverLevel.random.nextFloat() < 0.3F) {
                    net.minecraft.world.item.ItemStack trashStack = new net.minecraft.world.item.ItemStack(
                            Items.SLIME_BALL);
                    trashStack.setHoverName(Component.literal("Sampah"));
                    ItemEntity trashEntity = new ItemEntity(serverLevel,
                            v.getX(), v.getY(), v.getZ(), trashStack);
                    trashEntity.setDefaultPickUpDelay();
                    serverLevel.addFreshEntity(trashEntity);
                }
            }
        }
    }

    // ========== Chat GUI ==========

    public void openChatMenu(ServerPlayer player) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("  🦁 IndoZoo Tycoon — Dashboard")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        // Status
        player.sendSystemMessage(Component.literal("  💰 Saldo: Rp " + nf.format(balance))
                .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("  🐾 Hewan: " + cachedAnimalCount
                + "  |  👷 Staff: " + cachedStaffCount
                + "  |  🧑 Visitor: " + cachedVisitorCount)
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("  🗑️ Sampah: " + cachedTrashCount
                + "  |  📡 Radius: " + scanRadius + " blok")
                .withStyle(ChatFormatting.GRAY));

        player.sendSystemMessage(Component.literal("───────────────────────────────")
                .withStyle(ChatFormatting.DARK_GRAY));

        // Clickable buttons
        String bpx = getBlockPos().getX() + " " + getBlockPos().getY() + " " + getBlockPos().getZ();

        MutableComponent btnStaff = Component.literal(" [Rekrut Staff Rp2000] ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/zoocmd hire " + bpx)));

        MutableComponent btnChicken = Component.literal(" [Beli Ayam Rp1000] ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/zoocmd buy chicken " + bpx)));

        MutableComponent btnCow = Component.literal(" [Beli Sapi Rp3000] ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/zoocmd buy cow " + bpx)));

        MutableComponent btnPig = Component.literal(" [Beli Babi Rp2000] ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/zoocmd buy pig " + bpx)));

        MutableComponent btnUpgrade = Component.literal(" [Upgrade Lahan Rp5000] ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/zoocmd upgrade " + bpx)));

        player.sendSystemMessage(Component.literal("  📋 Aksi:").withStyle(ChatFormatting.WHITE));
        player.sendSystemMessage(btnStaff);
        player.sendSystemMessage(btnChicken.append(btnCow).append(btnPig));
        player.sendSystemMessage(btnUpgrade);

        player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    // ========== Getters / Setters (dipakai oleh ZooCommand) ==========

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
        setChanged();
    }

    public void addBalance(long amount) {
        this.balance += amount;
        setChanged();
    }

    public int getScanRadius() {
        return scanRadius;
    }

    public void setScanRadius(int radius) {
        this.scanRadius = radius;
        setChanged();
    }

    public int getCachedAnimalCount() {
        return cachedAnimalCount;
    }

    public int getCachedStaffCount() {
        return cachedStaffCount;
    }

    public int getCachedVisitorCount() {
        return cachedVisitorCount;
    }

    public static long getFoodCost() {
        return FOOD_COST;
    }
}
