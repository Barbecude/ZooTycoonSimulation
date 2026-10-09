package net.zootycoon.construction;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.economy.EconomyConfig;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.schematic.SchematicScanner;
import net.zootycoon.schematic.ZooSchematic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConstructionManager {
    private static final Map<UUID, ConstructionSite> ACTIVE_SITES = new ConcurrentHashMap<>();
    private static int tickCounter = 0;

    public static void initServerEvents() {
        ServerTickEvents.END_WORLD_TICK.register(ConstructionManager::onWorldTick);
    }

    public static Optional<ConstructionSite> startConstruction(ServerPlayer player, BlockPos origin, ZooSchematic schematic, StructureTemplate template) {
        ServerLevel level = player.serverLevel();
        ZooClaimManager claimManager = ZooClaimManager.get(level);

        // Verify placement is within player's claim
        var claimOpt = claimManager.getClaimAt(origin);
        if (claimOpt.isEmpty() || !claimOpt.get().getOwnerUuid().equals(player.getUUID())) {
            player.displayClientMessage(
                    Component.translatable("zootycoon.build.outside_claim").withStyle(ChatFormatting.RED),
                    true
            );
            return Optional.empty();
        }

        // Verify and deduct builder hire cost
        int hiringCost = schematic.getBuilderHiringCost();
        if (!TransactionHelper.hasEnoughEmeralds(player, hiringCost)) {
            player.displayClientMessage(
                    Component.literal("Emerald tidak cukup untuk menyewa Builder (Butuh " + hiringCost + " Emerald)!").withStyle(ChatFormatting.RED),
                    true
            );
            return Optional.empty();
        }

        if (!TransactionHelper.consumeEmeralds(player, hiringCost)) {
            return Optional.empty();
        }

        // Scan template for bill of materials
        var scan = SchematicScanner.scan(template);
        UUID siteId = UUID.randomUUID();
        ConstructionSite site = new ConstructionSite(
                siteId, player.getUUID(), origin, schematic, scan.requiredMaterials(), scan.sortedBlocksToPlace()
        );

        ACTIVE_SITES.put(siteId, site);
        player.displayClientMessage(
                Component.literal("Proyek '" + schematic.getName() + "' dimulai! Setor material ke builder untuk mulai membangun.").withStyle(ChatFormatting.GREEN),
                false
        );
        ZooTycoonMod.LOGGER.info("Started construction site {} for player {}", siteId, player.getName().getString());
        return Optional.of(site);
    }

    private static void onWorldTick(ServerLevel level) {
        tickCounter++;
        // Progress active construction sites every 10 ticks (0.5s per block)
        if (tickCounter % 10 != 0) return;

        List<UUID> toRemove = new ArrayList<>();

        for (ConstructionSite site : ACTIVE_SITES.values()) {
            if (site.isCompleted()) {
                toRemove.add(site.getSiteId());
                notifyCompletion(level, site);
                continue;
            }

            StructureTemplate.StructureBlockInfo nextBlock = site.getNextBlock();
            if (nextBlock == null) {
                site.markBlockPlaced();
                continue;
            }

            // Check if material is available
            if (site.hasAvailableItemFor(nextBlock)) {
                site.consumeItemFor(nextBlock);

                // Compute world coordinates
                BlockPos targetPos = site.getOrigin().offset(nextBlock.pos());
                BlockState targetState = nextBlock.state();

                // Place block in world
                level.setBlock(targetPos, targetState, 3);

                // Sound & particle effects
                level.playSound(null, targetPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, targetState),
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        10, 0.2, 0.2, 0.2, 0.05
                );

                site.markBlockPlaced();
            }
        }

        for (UUID id : toRemove) {
            ACTIVE_SITES.remove(id);
        }
    }

    private static void notifyCompletion(ServerLevel level, ConstructionSite site) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(site.getOwnerUuid());
        if (player != null) {
            player.displayClientMessage(
                    Component.literal("🎉 Bangunan '" + site.getSchematic().getName() + "' telah selesai dibangun 100%!").withStyle(ChatFormatting.GOLD),
                    false
            );
            level.playSound(null, site.getOrigin(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    public static Optional<ConstructionSite> getSite(UUID siteId) {
        return Optional.ofNullable(ACTIVE_SITES.get(siteId));
    }

    public static Collection<ConstructionSite> getActiveSites() {
        return Collections.unmodifiableCollection(ACTIVE_SITES.values());
    }
}

