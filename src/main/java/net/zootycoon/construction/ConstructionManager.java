package net.zootycoon.construction;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.schematic.SchematicScanner;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;
import net.zootycoon.util.ZooTranslations;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConstructionManager {
    private static final Map<UUID, ConstructionSite> ACTIVE_SITES = new ConcurrentHashMap<>();

    public static void reset() {
        ACTIVE_SITES.clear();
    }

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
                    String builderName = entity.getCustomName() != null ? entity.getCustomName().getString() : ZooTranslations.get("zootycoon.dialog.builder.default_name");
                    ItemStack held = player.getMainHandItem();
                    if (!held.isEmpty()) {
                        // Check if held item is a tool needed during land clearing phase
                        if (matchedSite.isClearingPhase()) {
                            if (matchedSite.needsShovel() && !matchedSite.hasShovel() && held.is(net.minecraft.tags.ItemTags.SHOVELS)) {
                                matchedSite.setEquippedShovel(held);
                                if (entity instanceof LivingEntity living) {
                                    living.setItemSlot(EquipmentSlot.MAINHAND, held.copyWithCount(1));
                                }
                                String toolName = held.getHoverName().getString();
                                held.shrink(1);
                                sLevel.playSound(null, entity.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                                ZooNetworkPayloads.sendToast(serverPlayer, "zootycoon.toast.title.construction", "zootycoon.builder.received_tool", 1, builderName, toolName);
                                if (entity instanceof net.zootycoon.entity.ZooBuilderEntity zb) zb.updateNeedsIndicator();
                                return InteractionResult.SUCCESS;
                            }
                            if (matchedSite.needsPickaxe() && !matchedSite.hasPickaxe() && held.is(net.minecraft.tags.ItemTags.PICKAXES)) {
                                matchedSite.setEquippedPickaxe(held);
                                if (entity instanceof LivingEntity living) {
                                    living.setItemSlot(EquipmentSlot.MAINHAND, held.copyWithCount(1));
                                }
                                String toolName = held.getHoverName().getString();
                                held.shrink(1);
                                sLevel.playSound(null, entity.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                                ZooNetworkPayloads.sendToast(serverPlayer, "zootycoon.toast.title.construction", "zootycoon.builder.received_tool", 1, builderName, toolName);
                                if (entity instanceof net.zootycoon.entity.ZooBuilderEntity zb) zb.updateNeedsIndicator();
                                return InteractionResult.SUCCESS;
                            }
                            if (matchedSite.needsAxe() && !matchedSite.hasAxe() && held.is(net.minecraft.tags.ItemTags.AXES)) {
                                matchedSite.setEquippedAxe(held);
                                if (entity instanceof LivingEntity living) {
                                    living.setItemSlot(EquipmentSlot.MAINHAND, held.copyWithCount(1));
                                }
                                String toolName = held.getHoverName().getString();
                                held.shrink(1);
                                sLevel.playSound(null, entity.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                                ZooNetworkPayloads.sendToast(serverPlayer, "zootycoon.toast.title.construction", "zootycoon.builder.received_tool", 1, builderName, toolName);
                                if (entity instanceof net.zootycoon.entity.ZooBuilderEntity zb) zb.updateNeedsIndicator();
                                return InteractionResult.SUCCESS;
                            }
                        }

                        int accepted = matchedSite.deliverMaterial(held);
                        if (accepted > 0) {
                            sLevel.playSound(null, entity.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                            ZooNetworkPayloads.sendToast(serverPlayer, "zootycoon.toast.title.construction", "zootycoon.builder.received_material_single", 1, builderName, accepted, held.getHoverName().getString());
                            if (entity instanceof net.zootycoon.entity.ZooBuilderEntity zb) zb.updateNeedsIndicator();
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
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.construction", "zootycoon.construction.must_be_in_claim", 3);
            return Optional.empty();
        }

        // Survival Mode: Check 1 Builder per Building limit
        int hiredBuilders = net.zootycoon.economy.ZooStaffManager.get(level).getBuilderCount(player.getUUID());
        int activeSites = getActiveSitesForPlayer(player.getUUID()).size();
        if (!player.isCreative() && activeSites >= hiredBuilders) {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.construction", "zootycoon.construction.all_builders_busy", 2, hiredBuilders);
            return Optional.empty();
        }

        // Survival Mode: Check hiring cost
        if (!player.isCreative()) {
            int hiringCost = schematic.getBuilderHiringCost();
            if (hiringCost > 0 && !TransactionHelper.hasEnoughEmeralds(player, hiringCost)) {
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.economy", "zootycoon.construction.not_enough_emerald_hire", 3, hiringCost);
                return Optional.empty();
            }

            if (hiringCost > 0 && !TransactionHelper.consumeEmeralds(player, hiringCost)) {
                return Optional.empty();
            }
        }

        // Scan template for bill of materials with rotation applied (entities are never counted as material needs!)
        var scan = SchematicScanner.scan(template, rotation);
        if (scan.sortedBlocksToPlace().isEmpty()) {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.construction", "zootycoon.construction.empty_schematic", 3);
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
        String builderName = ZooTranslations.get("zootycoon.dialog.builder.default_name");
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

        if (builder != null) {
            builder.updateNeedsIndicator();
        }

        ACTIVE_SITES.put(siteId, site);
        level.playSound(null, origin, SoundEvents.VILLAGER_WORK_MASON, SoundSource.BLOCKS, 1.0f, 1.0f);
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.construction", "zootycoon.construction.project_started", 1, schematic.getName(), builderName, site.getTotalBlocks());

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
                    boolean hasTool = site.hasToolFor(obsState);
                    if (!hasTool) {
                        // Kuli lacks the required tool! Pause and wait for player to provide it
                        site.flagToolNeeded(obsState);
                        if (builder != null) {
                            builder.setNeedsSomething(true);
                            if (owner != null && owner.distanceToSqr(builder) < 20 * 20) {
                                builder.getLookControl().setLookAt(owner.getX(), owner.getEyeY(), owner.getZ());
                            }
                        }
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
                    continue;
                }
            }

            // ==========================================
            // PHASE 2: BLOCK PLACEMENT (Land is clean!)
            // ==========================================
            int actionIdx = site.findNextPlaceableIndex(level, false);
            if (actionIdx == -1) {
                // WAITING STATE: No placeable block right now (waiting for materials)
                if (builder != null) {
                    builder.setNeedsSomething(!site.getMissingMaterials().isEmpty());
                    if (owner != null && owner.distanceToSqr(builder) < 20 * 20) {
                        builder.getLookControl().setLookAt(owner.getX(), owner.getEyeY(), owner.getZ());
                    }
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

                    if (site.getCooldown() % 8 == 0) {
                        builder.swing(InteractionHand.MAIN_HAND, true);
                        level.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, targetState),
                                targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                                3, 0.15, 0.15, 0.15, 0.05
                        );
                        level.playSound(null, targetPos, targetState.getSoundType().getHitSound(), SoundSource.BLOCKS, 0.5f, 1.0f);
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
                : ZooTranslations.get("zootycoon.dialog.builder.default_name");

        // 1. Re-verify and ensure biomes are fully set upon completion
        net.zootycoon.biome.ZooBiomeApplier.applySchematicBiomes(level, site.getOrigin(), site.getBounds(), site.getSchematic(), site.getRotation());

        // 2. Spawn entities (NPCs, Cashiers, Visitors, Animals) saved inside the StructureTemplate!
        StructureTemplate template = ZooSchematicRegistry.loadTemplate(level.getServer(), site.getSchematic());
        if (template != null) {
            spawnSchematicEntities(level, site, template);
        }

        // 3. Builder does NOT disappear yet! Sets awaitingPayment state and follows player!
        if (builderEntity != null) {
            builderEntity.setAwaitingPayment(site.getOwnerUuid(), site.getSchematic().getName(), totalBlocks, wageDue);
            level.playSound(null, builderEntity.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        if (player != null) {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.construction", "zootycoon.construction.completed_wage", 1, site.getSchematic().getName(), builderName, wageDue);
            level.playSound(null, site.getOrigin(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            net.zootycoon.economy.ZooStaffManager.get(level).syncToPlayer(player);
            ZooNetworkPayloads.sendAnimalSync(player);
        }
    }

    private static void spawnSchematicEntities(ServerLevel level, ConstructionSite site, StructureTemplate template) {
        try {
            CompoundTag saved = template.save(new CompoundTag());
            ListTag entitiesList = saved.getList("entities", 10);
            if (entitiesList.isEmpty()) return;

            BlockPos origin = site.getOrigin();
            Rotation rotation = site.getRotation();

            for (int i = 0; i < entitiesList.size(); i++) {
                CompoundTag entry = entitiesList.getCompound(i);
                ListTag posList = entry.getList("pos", 6); // 6 = TAG_Double
                if (posList.size() < 3) continue;

                double relX = posList.getDouble(0);
                double relY = posList.getDouble(1);
                double relZ = posList.getDouble(2);

                Vec3 rotatedOffset = StructureTemplate.transform(new Vec3(relX, relY, relZ), Mirror.NONE, rotation, BlockPos.ZERO);
                Vec3 worldPos = rotatedOffset.add(origin.getX(), origin.getY(), origin.getZ());

                CompoundTag entityNbt = entry.getCompound("nbt").copy();
                entityNbt.remove("UUID");

                // Skip spawning another builder entity if one was accidentally captured
                String idStr = entityNbt.getString("id");
                if ("zootycoon:builder".equals(idStr) || "minecraft:player".equals(idStr)) {
                    continue;
                }

                ListTag newPos = new ListTag();
                newPos.add(DoubleTag.valueOf(worldPos.x));
                newPos.add(DoubleTag.valueOf(worldPos.y));
                newPos.add(DoubleTag.valueOf(worldPos.z));
                entityNbt.put("Pos", newPos);

                Entity spawned = EntityType.loadEntityRecursive(entityNbt, level, ent -> {
                    float yRot = ent.rotate(rotation);
                    ent.moveTo(worldPos.x, worldPos.y, worldPos.z, yRot, ent.getXRot());
                    return ent;
                });

                if (spawned != null) {
                    if (spawned instanceof Mob mob) {
                        mob.setPersistenceRequired();
                    }
                    if (spawned instanceof LivingEntity living && ZooAnimalHungerManager.isZooAnimalCandidate(living)) {
                        ZooAnimalHungerManager.setTagged(living, true);
                        ZooAnimalHungerManager.setOwner(living, site.getOwnerUuid());
                        ZooAnimalHungerManager.setHunger(living, ZooAnimalHungerManager.MAX_HUNGER);
                    }
                    level.addFreshEntityWithPassengers(spawned);
                }
            }
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to spawn entities from schematic {}", site.getSchematic().getId(), e);
        }
    }

    public static Optional<ConstructionSite> getSite(UUID siteId) {
        return Optional.ofNullable(ACTIVE_SITES.get(siteId));
    }

    public static Collection<ConstructionSite> getActiveSites() {
        return Collections.unmodifiableCollection(ACTIVE_SITES.values());
    }
}
