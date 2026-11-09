package net.zootycoon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.construction.ConstructionManager;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.schematic.ZooSchematicRegistry;
import net.zootycoon.studio.StudioDimensionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZooTycoonMod implements ModInitializer {
    public static final String MOD_ID = "zootycoon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Zoo Tycoon Simulation for Fabric 26.3...");

        // 1. Register items, blocks, sounds and entities
        ZooItems.register();
        net.zootycoon.sound.ZooSounds.register();
        net.zootycoon.block.ZooBlocks.register();
        net.zootycoon.entity.ZooEntities.register();

        // 2. Initialize schematic registry & scan default catalog
        ZooSchematicRegistry.init();

        // 3. Register server networking payloads & handlers
        ZooNetworkPayloads.registerServerReceivers();
        net.zootycoon.network.WarehousePayloads.registerCommon();
        net.zootycoon.network.InspectionPayloads.registerCommon();
        net.zootycoon.animal.ZooInspectionService.registerServerReceivers();

        // 3b. Register data-driven habitat profile reload listener
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.server.packs.PackType.SERVER_DATA)
                .registerReloadListener(new net.zootycoon.animal.habitat.HabitatProfileLoader());

        // 4. Initialize Claim Manager & Protection hooks
        ZooClaimManager.initProtectionHooks();

        // 4b. Initialize Zoo Commands (/satwa hunger, etc.)
        net.zootycoon.command.ZooCommands.init();

        // 5. Initialize Construction Manager ticks
        ConstructionManager.initServerEvents();

        // 5b. Initialize Zoo Animal Hunger, Hunter incursions, Visitor arrivals, Hostile Mob Claims Barrier, and Economy absorption
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (level.getServer() != null && level == level.getServer().overworld()) {
                net.zootycoon.claim.ZooClaimManager.tickServer(level);
                net.zootycoon.hunter.ZooHunterManager.tickServer(level);
                net.zootycoon.visitor.ZooVisitorManager.tickServer(level);
                net.zootycoon.economy.ZooStaffManager.get(level).processDailyWages(level);
            }
            net.zootycoon.animal.ZooAnimalHungerManager.tickServer(level);
            if (level.getServer() != null) {
                var eco = net.zootycoon.economy.ZooEconomyManager.get(level.getServer());
                var staffMgr = net.zootycoon.economy.ZooStaffManager.get(level.getServer());
                for (var player : level.players()) {
                    eco.absorbPlayerEmeralds(player);
                    staffMgr.checkStrikeResolutionOnDeposit(player.getUUID(), player);
                }
            }
        });

        // 5bb. Reset static manager state across world loads to prevent cross-world state leakage
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            net.zootycoon.construction.ConstructionManager.saveToFile(server);
            net.zootycoon.claim.ZooClaimManager.reset();
            net.zootycoon.economy.ZooStaffManager.reset();
            net.zootycoon.economy.ZooEconomyManager.reset();
            net.zootycoon.economy.ZooRatingManager.reset();
            net.zootycoon.construction.ConstructionManager.reset();
            net.zootycoon.block.ZooFeedingTroughBlock.reset();
            net.zootycoon.block.EmptyBarrelBlock.reset();
            net.zootycoon.warehouse.WarehouseSupplyManager.reset();
            net.zootycoon.block.ZooGachaMachineBlock.reset();
            net.zootycoon.block.ToiletBlockEntity.reset();
            net.zootycoon.hunter.ZooHunterManager.reset();
            net.zootycoon.visitor.ZooVisitorManager.reset();
            net.zootycoon.animal.ZooInspectionService.reset();
            net.zootycoon.animal.AnimalBathingManager.reset();
            net.zootycoon.animal.habitat.HabitatEvaluator.reset();
            net.zootycoon.block.entity.ZooThermalBlockEntity.reset();
        });

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            net.zootycoon.claim.ZooClaimManager.reset();
            net.zootycoon.economy.ZooStaffManager.reset();
            net.zootycoon.economy.ZooEconomyManager.reset();
            net.zootycoon.economy.ZooRatingManager.reset();
            net.zootycoon.construction.ConstructionManager.reset();
            net.zootycoon.block.ZooFeedingTroughBlock.reset();
            net.zootycoon.block.EmptyBarrelBlock.reset();
            net.zootycoon.warehouse.WarehouseSupplyManager.reset();
            net.zootycoon.block.ZooGachaMachineBlock.reset();
            net.zootycoon.block.ToiletBlockEntity.reset();
            net.zootycoon.hunter.ZooHunterManager.reset();
            net.zootycoon.visitor.ZooVisitorManager.reset();
            net.zootycoon.animal.ZooInspectionService.reset();
            net.zootycoon.animal.AnimalBathingManager.reset();
            net.zootycoon.animal.habitat.HabitatEvaluator.reset();
            net.zootycoon.block.entity.ZooThermalBlockEntity.reset();
            net.zootycoon.construction.ConstructionManager.loadFromFile(server);
        });

        // 5c. Spyglass/Field Guide right-click animal registration, watering/brushing & direct player feeding
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                net.minecraft.world.item.ItemStack held = player.getItemInHand(hand);
                if (net.zootycoon.animal.ZooAnimalHungerManager.isScanningItem(held)) {
                    // Do not swing arm or cancel item use; allow normal scoping/zooming
                    return net.minecraft.world.InteractionResult.PASS;
                } else if (net.zootycoon.animal.AnimalBathingManager.isWateringCan(held)) {
                    if (net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(living)) {
                        int clean = net.zootycoon.animal.ZooAnimalHungerManager.getCleanliness(living);
                        if (clean >= 100) {
                            // Already 100% clean: do nothing!
                            return net.minecraft.world.InteractionResult.SUCCESS;
                        }
                        if (net.zootycoon.animal.AnimalBathingManager.isWet(living)) {
                            ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.animal", "zootycoon.bathing.already_watered", 0);
                        }
                        // Consumed: Bathing is driven by holding right-click with continuous progress bar
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                } else if (held.is(net.minecraft.world.item.Items.BRUSH)) {
                    if (net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(living)) {
                        int currentClean = net.zootycoon.animal.ZooAnimalHungerManager.getCleanliness(living);
                        if (currentClean >= 100) {
                            // Already 100% clean: do nothing!
                            return net.minecraft.world.InteractionResult.SUCCESS;
                        }
                        if (!net.zootycoon.animal.AnimalBathingManager.isWet(living)) {
                            sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("zootycoon.bathing.need_water"), true);
                        }
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                } else if (net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(living)
                        && net.zootycoon.animal.AnimalDietRegistry.isFoodValid(living, held)) {
                    boolean fed = net.zootycoon.animal.ZooAnimalHungerManager.feedAnimal(living, held, sp);
                    if (fed) {
                        int hungerAfter = net.zootycoon.animal.ZooAnimalHungerManager.getHunger(living);
                        ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.animal", "zootycoon.animal.fed", 1, living.getName().getString(), hungerAfter);
                        ZooNetworkPayloads.sendAnimalSync(sp);
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                }
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        // 6. Initialize Creative Studio Dimension management
        StudioDimensionManager.initServerEvents();

        // 6b. Physical Vehicles Placement Interceptor: Spawns custom ZooVehicleEntity facing player
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
            String vehicleType = net.zootycoon.entity.ZooVehicleEntity.resolveVehicleTypeFromEgg(stack);
            if (vehicleType != null) {
                if (!world.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    net.minecraft.server.level.ServerLevel sl = sp.serverLevel();
                    net.minecraft.world.phys.Vec3 spawnPos = hitResult.getBlockPos().relative(hitResult.getDirection()).getBottomCenter();

                    if ("remover".equals(vehicleType)) {
                        removeNearbyVehicles(sl, spawnPos, sp);
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }

                    net.zootycoon.entity.ZooVehicleEntity vehicle = net.zootycoon.entity.ZooEntities.VEHICLE.create(sl);
                    if (vehicle != null) {
                        vehicle.setVehicleType(vehicleType);
                        vehicle.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, sp.getYRot(), 0.0f);
                        sl.addFreshEntity(vehicle);
                    }

                    if (!sp.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    sl.playSound(null, net.minecraft.core.BlockPos.containing(spawnPos), net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(world.isClientSide());
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, world, hand) -> {
            net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
            String vehicleType = net.zootycoon.entity.ZooVehicleEntity.resolveVehicleTypeFromEgg(stack);
            if ("remover".equals(vehicleType)) {
                if (!world.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    removeNearbyVehicles(sp.serverLevel(), sp.position(), sp);
                }
                return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
            }
            return net.minecraft.world.InteractionResultHolder.pass(stack);
        });

        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity.getVehicle() instanceof net.zootycoon.entity.ZooVehicleEntity veh && !(entity instanceof net.minecraft.world.entity.player.Player)) {
                if (player.isShiftKeyDown()) {
                    if (!world.isClientSide()) {
                        veh.unloadCargoAnimals(player);
                    }
                    return net.minecraft.world.InteractionResult.sidedSuccess(world.isClientSide());
                }
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        // 6c. Convert any legacy physical_vehicle entities into ZooVehicleEntity and clean up broken llama/display parts
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, serverLevel) -> {
            if (entity instanceof net.zootycoon.entity.ZooVehicleEntity) {
                return;
            }
            if (entity.getTags().contains("phys_veh.spawn") || entity.getTags().contains("phys_veh.main")) {
                String resolvedType = null;
                for (String tag : entity.getTags()) {
                    if (tag.startsWith("phys_veh.") && !"phys_veh.spawn".equals(tag) && !"phys_veh.main".equals(tag) && !"phys_veh.car".equals(tag)) {
                        String candidate = tag.substring("phys_veh.".length());
                        if ("armored_truck_4x4".equals(candidate)) candidate = "armored_truck_4x4_black";
                        else if ("zaz-968a".equals(candidate)) candidate = "zaz-968a_tint";
                        else if ("zaz-968m".equals(candidate)) candidate = "zaz-968m_tint";
                        if (net.zootycoon.entity.ZooVehicleEntity.getSpec(candidate).typeId().equals(candidate)) {
                            resolvedType = candidate;
                            break;
                        }
                    }
                }
                if (resolvedType != null) {
                    net.zootycoon.entity.ZooVehicleEntity vehicle = net.zootycoon.entity.ZooEntities.VEHICLE.create(serverLevel);
                    if (vehicle != null) {
                        vehicle.setVehicleType(resolvedType);
                        vehicle.moveTo(entity.getX(), Math.floor(entity.getY()), entity.getZ(), entity.getYRot(), 0.0f);
                        serverLevel.addFreshEntity(vehicle);
                    }
                }
                entity.discard();
            } else if (entity.getTags().contains("physical_vehicle") || entity.getTags().contains("phys_veh.seat")) {
                entity.discard();
            }
        });

        // 6d. Tick server inspection service
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            net.zootycoon.animal.ZooInspectionService.tickServer(server);
        });

        // 7. Player Onboarding: First-join Starter Pack
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            String starterTag = "zootycoon_starter_received";
            if (!player.getTags().contains(starterTag)) {
                player.addTag(starterTag);

                // Give Starter Sticks for Zoo Management
                ItemStack starterSticks = new ItemStack(Items.STICK, 4);
                if (!player.getInventory().add(starterSticks)) {
                    player.drop(starterSticks, false);
                }

                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.welcome.message", 0);
                LOGGER.info("Gave starter pack to player {}", player.getName().getString());
            }

            // Sync claims, staff, emerald wallet balance and zoo rating to player
            ZooClaimManager.get((net.minecraft.server.level.ServerLevel) player.level()).syncToPlayer(player);
            net.zootycoon.economy.ZooStaffManager.get((net.minecraft.server.level.ServerLevel) player.level()).syncToPlayer(player);
            net.zootycoon.economy.ZooEconomyManager.get(server).syncToPlayer(player);
            net.zootycoon.economy.ZooRatingManager.get(server).syncToPlayer(player);

            // Unlock Jake's Build Tools recipes if present
            try {
                var funcOpt = server.getFunctions().get(ResourceLocation.fromNamespaceAndPath("jbt", "give_recipes"));
                funcOpt.ifPresent(func -> server.getFunctions().execute(func, player.createCommandSourceStack().withSuppressedOutput().withPermission(2)));
            } catch (Exception ignored) {}

            // Unlock Zoo Tycoon recipes for recipe book
            try {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withSuppressedOutput().withPermission(2),
                    "recipe give @s zootycoon:*"
                );
            } catch (Exception ignored) {}
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            net.zootycoon.animal.ZooInspectionService.onPlayerDisconnect(handler.getPlayer());
        });

        // Auto-release assigned zookeeper when a zoo animal dies
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity != null && !entity.level().isClientSide() && entity.level().getServer() != null) {
                if (net.zootycoon.animal.ZooAnimalHungerManager.isTagged(entity)) {
                    java.util.UUID zkUuid = net.zootycoon.animal.ZooAnimalHungerManager.getAssignedZookeeper(entity);
                    net.zootycoon.animal.ZooAnimalHungerManager.setAssignedZookeeper(entity, null);
                    if (zkUuid != null) {
                        for (net.minecraft.server.level.ServerLevel sl : entity.level().getServer().getAllLevels()) {
                            net.minecraft.world.entity.Entity zkEnt = sl.getEntity(zkUuid);
                            if (zkEnt instanceof net.zootycoon.entity.ZooZookeeperEntity keeper) {
                                keeper.setAssignedAnimalUuid(null);
                                break;
                            }
                        }
                    }
                }
            }
        });

        // 6c. Track seat block placement for visitor rest coordinates
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player != null) {
                net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof net.minecraft.world.item.BlockItem bi) {
                    BlockPos placedPos = hitResult.getBlockPos().relative(hitResult.getDirection());
                    if (net.zootycoon.util.ZooSeatHelper.isSeatableBlockState(bi.getBlock().defaultBlockState())) {
                        net.zootycoon.util.ZooSeatHelper.registerSeat(placedPos);
                    }
                }
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        // Knife on Leaves -> Green Tea Leaves drop event (20% chance on any leaves)
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            net.zootycoon.util.ZooSeatHelper.unregisterSeat(pos);
            if (level.isClientSide() || player == null || player.isCreative()) {
                return;
            }
            if (state.is(net.minecraft.tags.BlockTags.LEAVES)) {
                ItemStack held = player.getMainHandItem();
                if (isKnife(held)) {
                    if (level.random.nextFloat() < 0.20F) {
                        net.minecraft.world.level.block.Block.popResource(level, pos, new ItemStack(ZooItems.GREEN_TEA_LEAVES));
                    }
                }
            }
        });

        LOGGER.info("Zoo Tycoon Simulation initialized successfully.");
    }

    private static boolean isKnife(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var tagKey1 = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse("c:tools/knives"));
        var tagKey2 = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse("c:knives"));
        if (stack.is(tagKey1) || stack.is(tagKey2)) {
            return true;
        }
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null && key.getPath().toLowerCase(java.util.Locale.ROOT).contains("knife");
    }

    private static void removeNearbyVehicles(net.minecraft.server.level.ServerLevel sl, net.minecraft.world.phys.Vec3 center, net.minecraft.server.level.ServerPlayer sp) {
        net.minecraft.world.phys.AABB box = net.minecraft.world.phys.AABB.ofSize(center, 10.0, 6.0, 10.0);
        for (net.zootycoon.entity.ZooVehicleEntity veh : sl.getEntitiesOfClass(net.zootycoon.entity.ZooVehicleEntity.class, box)) {
            veh.removeAndDropVehicle(sp);
            return;
        }
        for (net.minecraft.world.entity.Entity e : sl.getEntities(null, box)) {
            if (e.getTags().contains("physical_vehicle") || e.getTags().contains("phys_veh.seat") || e.getTags().contains("phys_veh.main") || e.getTags().contains("phys_veh.spawn")) {
                e.discard();
            }
        }
    }
}

