package net.zootycoon.construction;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.schematic.SchematicScanner;
import net.zootycoon.schematic.ZooSchematic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConstructionManager {
    private static final Map<UUID, ConstructionSite> ACTIVE_SITES = new ConcurrentHashMap<>();

    public static void initServerEvents() {
        ServerTickEvents.END_WORLD_TICK.register(ConstructionManager::onWorldTick);

        // Player interaction with Builder NPC: deliver materials or open inspection screen
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != InteractionHand.MAIN_HAND || world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (entity instanceof net.zootycoon.entity.ZooBuilderEntity b && b.isAwaitingPayment()) {
                return InteractionResult.PASS;
            }

            if (entity.getTags().contains("zootycoon_builder") || entity instanceof net.zootycoon.entity.ZooBuilderEntity) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                ServerLevel sLevel = (ServerLevel) world;

                ConstructionSite matchedSite = null;
                for (ConstructionSite s : ACTIVE_SITES.values()) {
                    if (entity.getUUID().equals(s.getBuilderUuid()) || entity.getTags().contains(s.getSiteId().toString())) {
                        matchedSite = s;
                        break;
                    }
                }

                if (matchedSite != null) {
                    String builderName = entity.getCustomName() != null ? entity.getCustomName().getString() : "Budi Santoso";
                    ItemStack held = player.getMainHandItem();
                    if (!held.isEmpty()) {
                        // Check if held item is a tool needed during land clearing phase
                        if (matchedSite.isClearingPhase()) {
                            if (matchedSite.needsShovel() && !matchedSite.hasShovel() && held.is(net.minecraft.tags.ItemTags.SHOVELS)) {
                                matchedSite.setEquippedShovel(held);
                                if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                                    living.setItemSlot(EquipmentSlot.MAINHAND, held.copyWithCount(1));
                                }
                                String toolName = held.getHoverName().getString();
                                held.shrink(1);
                                sLevel.playSound(null, entity.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                                player.sendSystemMessage(Component.literal("[" + builderName + "] Menerima " + toolName + "! Mantap Bos, langsung saya pakai untuk menggali tanah.").withStyle(ChatFormatting.GREEN));
                                return InteractionResult.SUCCESS;
                            }
                            if (matchedSite.needsPickaxe() && !matchedSite.hasPickaxe() && held.is(net.minecraft.tags.ItemTags.PICKAXES)) {
                                matchedSite.setEquippedPickaxe(held);
                                if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                                    living.setItemSlot(EquipmentSlot.MAINHAND, held.copyWithCount(1));
                                }
                                String toolName = held.getHoverName().getString();
                                held.shrink(1);
                                sLevel.playSound(null, entity.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                                player.sendSystemMessage(Component.literal("[" + builderName + "] Menerima " + toolName + "! Mantap Bos, langsung saya pakai untuk menghancurkan batu.").withStyle(ChatFormatting.GREEN));
                                return InteractionResult.SUCCESS;
                            }
                            if (matchedSite.needsAxe() && !matchedSite.hasAxe() && held.is(net.minecraft.tags.ItemTags.AXES)) {
                                matchedSite.setEquippedAxe(held);
                                if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                                    living.setItemSlot(EquipmentSlot.MAINHAND, held.copyWithCount(1));
                                }
                                String toolName = held.getHoverName().getString();
                                held.shrink(1);
                                sLevel.playSound(null, entity.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                                player.sendSystemMessage(Component.literal("[" + builderName + "] Menerima " + toolName + "! Mantap Bos, langsung saya pakai untuk menebang kayu.").withStyle(ChatFormatting.GREEN));
                                return InteractionResult.SUCCESS;
                            }
                        }

                        int accepted = matchedSite.deliverMaterial(held);
                        if (accepted > 0) {
                            sLevel.playSound(null, entity.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                            sLevel.playSound(null, entity.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.NEUTRAL, 0.8f, 1.2f);
                            player.sendSystemMessage(
                                    Component.literal("[" + builderName + "] Menerima " + accepted + "x " + held.getHoverName().getString() + ". Terima kasih Bos!").withStyle(ChatFormatting.GREEN)
                            );
                            return InteractionResult.SUCCESS;
                        }
                    }

                    // Send S2C packet to open Builder dialogue / inspection screen
                    List<ZooNetworkPayloads.ItemEntry> missingList = new ArrayList<>();
                    for (var entry : matchedSite.getMissingMaterials().entrySet()) {
                        ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.getKey());
                        missingList.add(new ZooNetworkPayloads.ItemEntry(key.toString(), entry.getValue()));
                    }

                    int phase = matchedSite.isClearingPhase() ? 0 : 1;
                    int obsCount = matchedSite.getObstructingBlocks().size();

                    ServerPlayNetworking.send(serverPlayer, new ZooNetworkPayloads.OpenBuilderScreenPayload(
                            matchedSite.getSiteId(),
                            matchedSite.getSchematic().getName(),
                            matchedSite.getProgressPercentage(),
                            matchedSite.getPlacedBlocksCount(),
                            matchedSite.getTotalBlocks(),
                            missingList,
                            builderName,
                            phase,
                            obsCount,
                            matchedSite.needsShovel(), matchedSite.hasShovel(),
                            matchedSite.needsPickaxe(), matchedSite.hasPickaxe(),
                            matchedSite.needsAxe(), matchedSite.hasAxe(),
                            0,
                            matchedSite.getBuilderUuid() != null ? matchedSite.getBuilderUuid() : entity.getUUID()
                    ));
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }

    public static Optional<ConstructionSite> startConstruction(ServerPlayer player, BlockPos origin, ZooSchematic schematic, StructureTemplate template) {
        return startConstruction(player, origin, schematic, template, Rotation.NONE);
    }

    public static Optional<ConstructionSite> startConstruction(ServerPlayer player, BlockPos origin, ZooSchematic schematic, StructureTemplate template, Rotation rotation) {
        if (rotation == null) rotation = Rotation.NONE;
        ServerLevel level = (ServerLevel) player.level();
        ZooClaimManager claimManager = ZooClaimManager.get(level);

        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
        BoundingBox bb = template.getBoundingBox(settings, origin);

        // Verify placement is within player's claim (horizontal X, Z with unlimited Y)
        var claimOpt = claimManager.getClaimAt(origin);
        boolean hasValidClaim = claimOpt.isPresent() && claimOpt.get().getOwnerUuid().equals(player.getUUID());
        if (!hasValidClaim) {
            var centerOpt = claimManager.getClaimAt(new BlockPos(bb.minX() + bb.getXSpan() / 2, origin.getY(), bb.minZ() + bb.getZSpan() / 2));
            if (centerOpt.isPresent() && centerOpt.get().getOwnerUuid().equals(player.getUUID())) {
                hasValidClaim = true;
            }
        }

        if (!hasValidClaim) {
            player.displayClientMessage(
                    Component.literal("Bangunan harus ditempatkan di dalam area kebun binatang Anda!").withStyle(ChatFormatting.RED),
                    true
            );
            return Optional.empty();
        }

        // Survival Mode: Check 1 Builder per Building limit
        int hiredBuilders = net.zootycoon.economy.ZooStaffManager.get(level).getBuilderCount(player.getUUID());
        int activeSites = getActiveSitesForPlayer(player.getUUID()).size();
        if (!player.isCreative() && activeSites >= hiredBuilders) {
            player.displayClientMessage(
                    Component.literal("Semua kuli Bos (" + hiredBuilders + ") lagi sibuk! Rekrut kuli baru di menu Pegawai atau tunggu proyek kelar ya.").withStyle(ChatFormatting.RED),
                    true
            );
            return Optional.empty();
        }

        // Survival Mode: Check hiring cost
        if (!player.isCreative()) {
            int hiringCost = schematic.getBuilderHiringCost();
            if (hiringCost > 0 && !TransactionHelper.hasEnoughEmeralds(player, hiringCost)) {
                player.displayClientMessage(
                        Component.literal("Waduh Bos, Emeraldnya kurang buat sewa kuli (Butuh " + hiringCost + " Emerald)!").withStyle(ChatFormatting.RED),
                        true
                );
                return Optional.empty();
            }

            if (hiringCost > 0 && !TransactionHelper.consumeEmeralds(player, hiringCost)) {
                return Optional.empty();
            }
        }

        // Scan template for bill of materials with rotation applied
        var scan = SchematicScanner.scan(template, rotation);
        if (scan.sortedBlocksToPlace().isEmpty()) {
            player.displayClientMessage(
                    Component.literal("Skematik ini tidak memiliki blok untuk dibangun!").withStyle(ChatFormatting.RED),
                    true
            );
            return Optional.empty();
        }

        UUID siteId = UUID.randomUUID();
        ConstructionSite site = new ConstructionSite(
                siteId, player.getUUID(), origin, level.dimension(), schematic, scan.requiredMaterials(), scan.sortedBlocksToPlace()
        );
        site.setRotation(rotation);
        site.setCooldown(20);

        // Scan existing blocks for obstruction clearing
        site.scanObstructions(level);
        if (player.isCreative()) {
            site.setEquippedShovel(new ItemStack(Items.DIAMOND_SHOVEL));
            site.setEquippedPickaxe(new ItemStack(Items.DIAMOND_PICKAXE));
            site.setEquippedAxe(new ItemStack(Items.DIAMOND_AXE));
        }

        // 1. Place COC-style perimeter barrier tape
        Set<BlockPos> tape = ConstructionTapeHelper.placePerimeterTape(level, bb);
        site.addTapePositions(tape);

        // Apply schematic biomes immediately so player sees visual transformation right away!
        net.zootycoon.biome.ZooBiomeApplier.applySchematicBiomes(level, origin, bb, schematic, rotation);

        // 2. Spawn dedicated Humanoid Builder NPC at the entrance
        BlockPos entrance = new BlockPos(bb.minX() + bb.getXSpan() / 2, origin.getY(), bb.minZ() - 1);
        for (int dy = 3; dy >= -8; dy--) {
            BlockPos check = entrance.offset(0, dy, 0);
            if (level.getBlockState(check).isSolid()) {
                entrance = check.above();
                break;
            }
        }

        net.zootycoon.entity.ZooBuilderEntity builder = net.zootycoon.entity.ZooEntities.BUILDER.create(level);
        String builderName = "Budi Santoso";
        if (builder != null) {
            builder.setPos(entrance.getX() + 0.5, entrance.getY(), entrance.getZ() + 0.5);
            builder.setSiteId(siteId);
            builder.setPersistenceRequired();
            builder.addTag("zootycoon_builder");
            builder.addTag(siteId.toString());
            level.addFreshEntity(builder);
            site.setBuilderUuid(builder.getUUID());
            if (builder.getCustomName() != null) {
                builderName = builder.getCustomName().getString();
            }
        }

        // Auto-deposit matching materials player is currently carrying so builder starts immediately
        int autoDelivered = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                int accepted = site.deliverMaterial(stack);
                autoDelivered += accepted;
            }
        }

        ACTIVE_SITES.put(siteId, site);
        level.playSound(null, origin, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        player.sendSystemMessage(
                Component.literal("Proyek '" + schematic.getName() + "' dimulai! Kuli " + builderName + " bersiap bekerja (" + site.getTotalBlocks() + " blok).").withStyle(ChatFormatting.GREEN)
        );
        if (autoDelivered > 0) {
            player.sendSystemMessage(
                    Component.literal("Kuli mengambil " + autoDelivered + " material dari Anda dan langsung mulai membangun!").withStyle(ChatFormatting.AQUA)
            );
        } else if (!player.isCreative()) {
            player.sendSystemMessage(
                    Component.literal("Klik kuli " + builderName + " untuk memeriksa kebutuhan material atau menyerahkan peralatan.").withStyle(ChatFormatting.AQUA)
            );
        }
        net.zootycoon.economy.ZooStaffManager.get(level).syncToPlayer(player);
        ZooTycoonMod.LOGGER.info("Started construction site {} for player {} ({} blocks)", siteId, player.getName().getString(), site.getTotalBlocks());
        return Optional.of(site);
    }

    private static void onWorldTick(ServerLevel level) {
        List<UUID> toRemove = new ArrayList<>();

        for (ConstructionSite site : ACTIVE_SITES.values()) {
            // ONLY TICK FOR THE LEVEL MATCHING THIS SITE'S DIMENSION!
            if (!site.getDimension().equals(level.dimension())) {
                continue;
            }

            if (site.isCompleted()) {
                toRemove.add(site.getSiteId());
                // Remove COC perimeter tape
                ConstructionTapeHelper.removePerimeterTape(level, site.getTapePositions());
                notifyCompletion(level, site);
                continue;
            }

            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(site.getOwnerUuid());
            boolean isOwnerCreative = owner != null && owner.isCreative();

            Entity ent = site.getBuilderUuid() != null ? level.getEntity(site.getBuilderUuid()) : null;
            net.zootycoon.entity.ZooBuilderEntity builder = (ent instanceof net.zootycoon.entity.ZooBuilderEntity b) ? b : null;

            // ==========================================
            // PHASE 1: LAND CLEARING (Pembersihan Lahan)
            // Kuli will NOT build if the site is not clean!
            // ==========================================
            if (site.isClearingPhase()) {
                BlockPos obsPos = site.getNextObstructingBlock(level);
                if (obsPos != null) {
                    BlockState obsState = level.getBlockState(obsPos);
                    boolean hasTool = isOwnerCreative || site.hasToolFor(obsState);
                    if (!hasTool) {
                        // Kuli lacks the required tool! Pause and wait for player to provide it
                        site.flagToolNeeded(obsState);
                        if (builder != null && owner != null && owner.distanceToSqr(builder) < 20 * 20) {
                            builder.getLookControl().setLookAt(owner.getX(), owner.getEyeY(), owner.getZ());
                        }
                        // PAUSE! Do not clear and DO NOT BUILD!
                        continue;
                    }

                    // Kuli has the tool -> Proceed with clearing!
                    ItemStack toolStack = site.getToolStackFor(obsState);
                    if (builder != null) {
                        builder.setItemSlot(EquipmentSlot.MAINHAND, toolStack);
                        BlockPos walkTarget = new BlockPos(obsPos.getX(), builder.blockPosition().getY(), obsPos.getZ());
                        builder.getNavigation().moveTo(walkTarget.getX() + 0.5, walkTarget.getY(), walkTarget.getZ() + 0.5, 1.0);
                        builder.getLookControl().setLookAt(obsPos.getX() + 0.5, obsPos.getY() + 0.5, obsPos.getZ() + 0.5);

                        builder.swing(InteractionHand.MAIN_HAND, true);
                        level.playSound(null, obsPos, obsState.getSoundType().getHitSound(), SoundSource.BLOCKS, 0.7f, 1.0f);
                    }

                    int breakProg = site.getBreakProgress();
                    site.incrementBreakProgress();
                    level.destroyBlockProgress(site.getSiteId().hashCode(), obsPos, breakProg);

                    if (breakProg >= 9) {
                        level.destroyBlockProgress(site.getSiteId().hashCode(), obsPos, -1);
                        level.destroyBlock(obsPos, true);
                        site.resetBreakProgress();
                        site.removeObstructingBlock(obsPos);
                        site.setCooldown(isOwnerCreative ? 2 : 5);
                    }
                    // CRITICAL: NEVER BUILD ANY BLOCK WHILE CLEARING!
                    continue;
                }
            }

            // ==========================================
            // PHASE 2: BLOCK PLACEMENT (Land is clean!)
            // ==========================================
            int actionIdx = site.findNextPlaceableIndex(level, isOwnerCreative);
            if (actionIdx == -1) {
                // WAITING STATE: No placeable block right now (waiting for materials)
                if (builder != null && owner != null && owner.distanceToSqr(builder) < 20 * 20) {
                    builder.getLookControl().setLookAt(owner.getX(), owner.getEyeY(), owner.getZ());
                }
                continue;
            }

            var targetBlock = site.getPendingBlocks().get(actionIdx);
            BlockPos targetPos = site.getOrigin().offset(targetBlock.pos());
            BlockState targetState = targetBlock.state();

            int blockDelay = isOwnerCreative ? 15 : 25;
            if (site.getCooldown() > 0) {
                site.decrementCooldown();
                if (builder != null) {
                    Item blockItem = targetState.getBlock().asItem();
                    if (blockItem != Items.AIR) {
                        builder.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(blockItem));
                    }
                    BlockPos walkTarget = new BlockPos(targetPos.getX(), builder.blockPosition().getY(), targetPos.getZ());
                    builder.getNavigation().moveTo(walkTarget.getX() + 0.5, walkTarget.getY(), walkTarget.getZ() + 0.5, 1.0);
                    builder.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

                    if (site.getCooldown() % 6 == 0) {
                        builder.swing(InteractionHand.MAIN_HAND, true);
                        level.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, targetState),
                                targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                                3, 0.15, 0.15, 0.15, 0.05
                        );
                        level.playSound(null, targetPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.35f, 1.7f);
                    }
                }
                continue;
            }

            // Cooldown reached 0 -> Place block now!
            if (!isOwnerCreative) {
                site.consumeItemFor(targetBlock);
            }
            level.setBlock(targetPos, targetState, 3);
            level.playSound(null, targetPos, targetState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, targetState),
                    targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                    8, 0.2, 0.2, 0.2, 0.05
            );
            site.markSpecificBlockPlaced(actionIdx);
            site.setCooldown(blockDelay);
        }

        for (UUID id : toRemove) {
            ACTIVE_SITES.remove(id);
        }
    }

    public static List<ConstructionSite> getActiveSitesForPlayer(UUID playerUuid) {
        List<ConstructionSite> list = new ArrayList<>();
        for (ConstructionSite s : ACTIVE_SITES.values()) {
            if (s.getOwnerUuid().equals(playerUuid)) {
                list.add(s);
            }
        }
        return list;
    }

    private static void notifyCompletion(ServerLevel level, ConstructionSite site) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(site.getOwnerUuid());
        int totalBlocks = site.getTotalBlocks();
        int wageDue = net.zootycoon.economy.EconomyConfig.calculateBuilderWage(totalBlocks);

        net.zootycoon.entity.ZooBuilderEntity builderEntity = null;
        if (site.getBuilderUuid() != null) {
            Entity ent = level.getEntity(site.getBuilderUuid());
            if (ent instanceof net.zootycoon.entity.ZooBuilderEntity b) {
                builderEntity = b;
            }
        }

        String builderName = (builderEntity != null && builderEntity.getCustomName() != null)
                ? builderEntity.getCustomName().getString()
                : "Budi Santoso";

        // Re-verify and ensure biomes are fully set upon completion
        net.zootycoon.biome.ZooBiomeApplier.applySchematicBiomes(level, site.getOrigin(), site.getBounds(), site.getSchematic(), site.getRotation());

        // Builder does NOT disappear yet! Sets awaitingPayment state and follows player!
        if (builderEntity != null) {
            builderEntity.setAwaitingPayment(site.getOwnerUuid(), site.getSchematic().getName(), totalBlocks, wageDue);
            level.playSound(null, builderEntity.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        if (player != null) {
            player.sendSystemMessage(
                    Component.literal("Beres Bos! Bangunan '" + site.getSchematic().getName() + "' udah kelar dibangun 100%!").withStyle(ChatFormatting.GOLD)
            );
            player.sendSystemMessage(
                    Component.literal("[" + builderName + "] Bos, kerjaan udah beres nih (" + totalBlocks + " blok). Minta upah " + wageDue + " Emerald ya Bos!").withStyle(ChatFormatting.YELLOW)
            );
            level.playSound(null, site.getOrigin(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            net.zootycoon.economy.ZooStaffManager.get(level).syncToPlayer(player);
        }
    }

    public static Optional<ConstructionSite> getSite(UUID siteId) {
        return Optional.ofNullable(ACTIVE_SITES.get(siteId));
    }

    public static Collection<ConstructionSite> getActiveSites() {
        return Collections.unmodifiableCollection(ACTIVE_SITES.values());
    }
}
