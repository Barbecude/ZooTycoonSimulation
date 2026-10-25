package net.zootycoon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.construction.ConstructionManager;
import net.zootycoon.economy.EconomyConfig;
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
            }
            net.zootycoon.animal.ZooAnimalHungerManager.tickServer(level);
            if (level.getServer() != null) {
                var eco = net.zootycoon.economy.ZooEconomyManager.get(level.getServer());
                for (var player : level.players()) {
                    eco.absorbPlayerEmeralds(player);
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

        // 6b. Physical Vehicles Placement Interceptor: Spawns vehicle facing player with valid quaternion (Never a Pig!)
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
            if (!world.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp && stack.is(net.minecraft.world.item.Items.DOLPHIN_SPAWN_EGG)) {
                net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.ENTITY_DATA);
                if (customData != null && customData.contains("Tags")) {
                    net.minecraft.nbt.CompoundTag nbt = customData.copyTag();
                    net.minecraft.nbt.ListTag tags = nbt.getList("Tags", net.minecraft.nbt.Tag.TAG_STRING);
                    boolean isVehicleEgg = false;
                    for (int i = 0; i < tags.size(); i++) {
                        if ("phys_veh.spawn".equals(tags.getString(i))) {
                            isVehicleEgg = true;
                            break;
                        }
                    }
                    if (isVehicleEgg) {
                        net.minecraft.server.level.ServerLevel sl = sp.serverLevel();
                        net.minecraft.world.phys.Vec3 spawnPos = hitResult.getBlockPos().relative(hitResult.getDirection()).getBottomCenter();

                        // Tag player for orientation calculation in spawn_data.mcfunction
                        sp.addTag("midwut.used.dolphin_spawn_egg.selected_player");

                        net.minecraft.world.entity.animal.Pig pig = net.minecraft.world.entity.EntityType.PIG.create(sl);
                        if (pig != null) {
                            pig.moveTo(spawnPos.x, spawnPos.y + 0.25, spawnPos.z, sp.getYRot(), 0.0f);
                            customData.loadInto(pig);
                            pig.setSilent(true);
                            pig.setNoAi(true);
                            pig.setInvulnerable(true);
                            sl.addFreshEntity(pig);

                            sl.getServer().getCommands().performPrefixedCommand(
                                pig.createCommandSourceStack().withSuppressedOutput().withPermission(2),
                                "function physical_vehicles:zzz/summon_data/summon"
                            );

                            if (pig.isAlive()) {
                                pig.discard();
                            }
                        }

                        sp.removeTag("midwut.used.dolphin_spawn_egg.selected_player");

                        if (!sp.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        sl.playSound(null, net.minecraft.core.BlockPos.containing(spawnPos), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                }
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        // 6c. Fallback Pig safety net: Convert any stray vehicle spawn pigs immediately
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, serverLevel) -> {
            if (entity instanceof net.minecraft.world.entity.animal.Pig pig && pig.getTags().contains("phys_veh.spawn")) {
                net.minecraft.world.entity.player.Player nearest = serverLevel.getNearestPlayer(pig, 32.0);
                if (nearest != null) {
                    nearest.addTag("midwut.used.dolphin_spawn_egg.selected_player");
                }
                serverLevel.getServer().getCommands().performPrefixedCommand(
                    pig.createCommandSourceStack().withSuppressedOutput().withPermission(2),
                    "function physical_vehicles:zzz/summon_data/summon"
                );
                if (nearest != null) {
                    nearest.removeTag("midwut.used.dolphin_spawn_egg.selected_player");
                }
                if (pig.isAlive()) {
                    pig.discard();
                }
            }
        });

        // 6d. Bridge Player Vehicle Inputs in MC 1.21.1 to Physical Vehicles Scoreboard
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            net.zootycoon.animal.ZooInspectionService.tickServer(server);
            var scoreboard = server.getScoreboard();
            var mainScore = scoreboard.getObjective("main_score");
            if (mainScore == null) return;

            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                net.minecraft.world.entity.Entity vehicle = player.getVehicle();
                if (vehicle != null && vehicle.getTags().contains("phys_veh.seat")) {
                    scoreboard.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly("#afk_timer"), mainScore).set(118);

                    if (player.zza > 0.05f) {
                        scoreboard.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly("#throttle_pedal"), mainScore).set(1);
                    }
                    if (player.zza < -0.05f) {
                        scoreboard.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly("#brake_pedal"), mainScore).set(1);
                    }

                    if (player.xxa > 0.05f) { // Left (A)
                        var angleScore = scoreboard.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly("#wheel_angle"), mainScore);
                        angleScore.set(angleScore.get() + 1);
                    }
                    if (player.xxa < -0.05f) { // Right (D)
                        var angleScore = scoreboard.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly("#wheel_angle"), mainScore);
                        angleScore.set(angleScore.get() - 1);
                    }

                    if (((net.zootycoon.mixin.LivingEntityAccessor) player).isJumping()) {
                        scoreboard.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly("#hand_brake"), mainScore).set(1);
                    }

                    if (player.isSprinting()) {
                        scoreboard.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly("#clutch_pedal"), mainScore).set(1);
                    }
                }
            }
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

        LOGGER.info("Zoo Tycoon Simulation initialized successfully.");
    }
}

