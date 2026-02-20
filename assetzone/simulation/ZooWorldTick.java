package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZooWorldTick {

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide)
            return;

        if (event.level instanceof ServerLevel level) {
            // Run logic every 10 seconds (200 ticks)
            if (level.getGameTime() % 200 == 0) {
                ZooData data = ZooData.get(level);
                java.util.List<BlockPos> entrances = data.getEntrances();

                if (entrances.isEmpty())
                    return;

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
                            visitor.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);

                            level.addFreshEntity(visitor);
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
}
