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
}
