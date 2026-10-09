package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZooWorldTick {

    // Staff salary per role (Rp per day)
    private static final int JANITOR_SALARY = 100_000;
    private static final int ZOOKEEPER_SALARY = 150_000;
    private static final int SECURITY_SALARY = 200_000;
    private static final int CASHIER_SALARY = 180_000;

    private static void deductStaffSalaries(ServerLevel level) {
        ZooData data = ZooData.get(level);
        int totalSalary = 0;

        // Count each staff type and calculate salary
        int janitorCount = 0;
        int zookeeperCount = 0;
        int securityCount = 0;
        int cashierCount = 0;

        for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
            if (entity instanceof StaffEntity staff && staff.isPermanent()) { // Only count permanent staff
                switch (staff.getRole()) {
                    case 0 -> janitorCount++;
                    case 1 -> zookeeperCount++;
                    case 2 -> securityCount++;
                }
            }
        }

        for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
            if (entity instanceof CashierEntity) {
                cashierCount++;
            }
        }

        totalSalary = (janitorCount * JANITOR_SALARY) +
                (zookeeperCount * ZOOKEEPER_SALARY) +
                (securityCount * SECURITY_SALARY) +
                (cashierCount * CASHIER_SALARY);

        if (totalSalary > 0) {
            data.addBalance(-totalSalary);

            // Log salary transaction info (can add to zoo info later)
            System.out.println("[IndoZoo] Staff Salary Deducted: Rp " + totalSalary +
                    " (Janitors: " + janitorCount + ", Zookeepers: " + zookeeperCount +
                    ", Security: " + securityCount + ", Cashiers: " + cashierCount + ")");

            data.logTransaction("Pengeluaran",
                    "Gaji staff: " + janitorCount + " janitor, " + zookeeperCount
                            + " zookeeper, " + securityCount + " security, "
                            + cashierCount + " cashier",
                    -totalSalary);

            // Notify players if balance is too low
            if (data.getBalance() < 0) {
                data.setBalance(0); // Prevent negative balance
                for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component
                                    .literal("§c⚠ Anggaran Zoo tidak cukup untuk membayar gaji! Jebol!"),
                            false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide)
            return;

        if (event.level instanceof ServerLevel level) {
            ZooAnimalHungerSystem.tickServer(level);

            // HUNTER PHASE: Check every 60 seconds (1200 ticks)
            if (level.getGameTime() % 1200 == 0) {
                tickHunterPhase(level);
            }

            // FOOD STALL QUEUE: Notify nearby players every second (20 ticks)
            if (level.getGameTime() % 20 == 0) {
                for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                    if (player.serverLevel() != level) continue;
                    BlockPos pp = player.blockPosition();
                    for (BlockPos bp : BlockPos.betweenClosed(
                            pp.offset(-8, -4, -8), pp.offset(8, 4, 8))) {
                        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(bp);
                        if (be instanceof FoodStallBlockEntity stall && stall.getQueueSize() > 0) {
                            PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                new FoodStallQueueSyncPacket(bp.immutable(), stall.getQueueSize(), "HUNGRY", stall.getNextRequestItemId()));
                            break; // one popup per player per tick cycle
                        }
                    }
                }
            }

            // SALARY DEDUCTION: Every 24000 ticks = 1 Minecraft day (20 minutes real time)
            if (level.getGameTime() % 24000 == 0) {
                deductStaffSalaries(level);
            }

            // Run logic every 10 seconds (200 ticks)
            if (level.getGameTime() % 200 == 0) {
                ZooData data = ZooData.get(level);
                java.util.List<BlockPos> entrances = data.getEntrances();

                // Auto-discover zoo banners as entrance fallback
                if (entrances.isEmpty()) {
                    autoDiscoverBannerEntrances(level, data);
                    entrances = data.getEntrances();
                }

                if (entrances.isEmpty()) {
                    if (level.getGameTime() % 2400 == 0) {
                        System.out.println("[IndoZoo] ⚠ Belum ada Zoo Banner dipasang! Pasang Zoo Banner dulu.");
                    }
                    return;
                }

                // Count visitor, staff, animals
                data.updateCounts(level);
                int vCount = data.getVisitorCount();

                // Cap visitor count
                // Base 30 + Marketing * 10
                int maxVisitors = 30 + (data.getMarketingLevel() * 10);

                if (vCount < maxVisitors) {
                    BlockPos spawnPos = data.getRandomEntrance();
                    if (spawnPos != null && level.isLoaded(spawnPos)) {
                        // Spawn 1 visitor
                        VisitorEntity visitor = IndoZooTycoon.VISITOR_ENTITY.get().create(level);
                        if (visitor != null) {
                            // Random offset around block
                            double xX = spawnPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 2.0;
                            double zZ = spawnPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 2.0;
                            visitor.moveTo(xX, spawnPos.getY(), zZ, level.random.nextFloat() * 360F, 0);

                            // Set home/banner pos as "gate" pos
                            visitor.setGatePos(spawnPos);

                            // Trigger finalizeSpawn to randomize Hunter/Variant/Age
                            visitor.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT,
                                    null, null);

                            level.addFreshEntity(visitor);
                            System.out.println("[IndoZoo] ✓ Visitor spawned at " + spawnPos + " (Visitor count: "
                                    + (vCount + 1) + "/" + maxVisitors + ")");
                        }
                    }
                }

                int sCount = data.getStaffCount();
                int aCount = data.getAnimalCount();

                // Sync Balance to all players
                // We do this every 200 ticks (10s), which is acceptable.
                // Or maybe more frequently if needed? 10s is fine for passive income/updates.
                // But for transactions, we might want instant sync.
                // Instant sync should be handled by the command that changes balance.
                // This is a "catch-up" sync.
                SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(), aCount,
                        sCount, vCount, data.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);

                SyncTransactionLogPacket logPacket = new SyncTransactionLogPacket(data.getTransactionLog());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), logPacket);
            }
        }
    }

    /**
     * Scans the world around each online player for ZooBanner / ZooWallBanner
     * blocks
     * and auto-registers them as entrances if none are registered yet.
     */
    private static void autoDiscoverBannerEntrances(ServerLevel level, ZooData data) {
        int radius = 200;
        java.util.Set<BlockPos> found = new java.util.LinkedHashSet<>();
        for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            BlockPos center = player.blockPosition();
            for (java.util.Iterator<BlockPos> it = BlockPos.betweenClosed(
                    center.offset(-radius, -16, -radius),
                    center.offset(radius, 16, radius)).iterator(); it.hasNext();) {
                BlockPos p = it.next().immutable();
                net.minecraft.world.level.block.state.BlockState st = level.getBlockState(p);
                if (st.is(IndoZooTycoon.ZOO_BANNER_BLOCK.get())
                        || st.is(IndoZooTycoon.ZOO_WALL_BANNER_BLOCK.get())) {
                    found.add(p);
                    if (found.size() >= 5)
                        break; // max 5 entrances
                }
            }
            if (found.size() >= 5)
                break;
        }
        for (BlockPos pos : found) {
            data.addEntrance(pos);
            System.out.println("[IndoZoo] ✓ Auto-discovered entrance at Zoo Banner: " + pos);
        }
    }

    // ── Hunter Phase System ──────────────────────────────────────────────────

    /**
     * Called every 60 seconds. Manages escalating hunter waves.
     * Phase 0 = calm. Every ~5 min (6000 ticks) there's a 15% chance to start phase 1.
     * Each phase lasts 5 min; if hunters are killed, phase escalates (+1). Max phase 5.
     * After phase 5 wave is cleared, resets to 0.
     */
    private static void tickHunterPhase(ServerLevel level) {
        ZooData data = ZooData.get(level);
        int phase = data.getHunterPhase();
        long now = level.getGameTime();

        // Count active hunters
        int hunterCount = 0;
        for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
            if (e instanceof VisitorEntity v && !e.isRemoved() && v.isHunter()) {
                hunterCount++;
            }
        }

        if (phase == 0) {
            // Calm: 15% chance every check (every 60s) to spawn a wave
            if (level.random.nextFloat() < 0.15F) {
                data.setHunterPhase(1);
                data.setHunterPhaseStartTick(now);
                spawnHunterWave(level, data);
                notifyPlayers(level, "§c⚠ Pemburu fase 1 muncul! " + data.getHunterWaveSize() + " pemburu mendekati zoo!");
            }
        } else {
            // Active phase: check if all hunters are dead
            if (hunterCount == 0) {
                // Wave cleared! Escalate or reset
                if (phase >= 5) {
                    data.setHunterPhase(0);
                    data.setHunterPhaseStartTick(0);
                    notifyPlayers(level, "§a✓ Semua gelombang pemburu telah dikalahkan! Zoo aman.");
                } else {
                    int newPhase = phase + 1;
                    data.setHunterPhase(newPhase);
                    data.setHunterPhaseStartTick(now);
                    spawnHunterWave(level, data);
                    notifyPlayers(level, "§c⚠ Pemburu fase " + newPhase + "! " + data.getHunterWaveSize() + " pemburu baru muncul!");
                }
            } else {
                // Timeout: if phase active for > 10 min (12000 ticks) with hunters still alive, don't escalate
                if (now - data.getHunterPhaseStartTick() > 12000) {
                    // Hunters still roaming, just keep current phase
                }
            }
        }
    }

    private static void spawnHunterWave(ServerLevel level, ZooData data) {
        int count = data.getHunterWaveSize();
        java.util.List<BlockPos> entrances = data.getEntrances();
        if (entrances.isEmpty()) return;

        for (int i = 0; i < count; i++) {
            BlockPos spawnPos = entrances.get(level.random.nextInt(entrances.size()));
            if (!level.isLoaded(spawnPos)) continue;

            VisitorEntity hunter = IndoZooTycoon.VISITOR_ENTITY.get().create(level);
            if (hunter != null) {
                double x = spawnPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 3.0;
                double z = spawnPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 3.0;
                hunter.moveTo(x, spawnPos.getY(), z, level.random.nextFloat() * 360F, 0);
                hunter.setGatePos(spawnPos);

                // Force hunter via dataTag
                net.minecraft.nbt.CompoundTag forceHunter = new net.minecraft.nbt.CompoundTag();
                forceHunter.putBoolean("IsHunter", true);
                // Higher phases have more archers
                float archerChance = 0.15F + (data.getHunterPhase() * 0.10F);
                int hunterMode = level.random.nextFloat() < archerChance ? 0 : 1; // 0=ARCHER, 1=KIDNAPPER
                forceHunter.putInt("HunterMode", hunterMode);

                hunter.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                        MobSpawnType.EVENT, null, forceHunter);
                level.addFreshEntity(hunter);
            }
        }
        System.out.println("[IndoZoo] Hunter wave spawned: phase " + data.getHunterPhase() + ", count " + count);
    }

    private static void notifyPlayers(ServerLevel level, String message) {
        for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(message), false);
        }
    }
}
