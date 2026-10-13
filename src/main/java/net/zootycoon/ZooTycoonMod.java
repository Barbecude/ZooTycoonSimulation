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

        // 1. Register items, blocks and entities
        ZooItems.register();
        net.zootycoon.block.ZooBlocks.register();
        net.zootycoon.entity.ZooEntities.register();

        // 2. Initialize schematic registry & scan default catalog
        ZooSchematicRegistry.init();

        // 3. Register server networking payloads & handlers
        ZooNetworkPayloads.registerServerReceivers();

        // 4. Initialize Claim Manager & Protection hooks
        ZooClaimManager.initProtectionHooks();

        // 5. Initialize Construction Manager ticks
        ConstructionManager.initServerEvents();

        // 5b. Initialize Zoo Animal Hunger, Hunter incursions, Hostile Mob Claims Barrier, and Economy absorption
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(level -> {
            net.zootycoon.claim.ZooClaimManager.tickServer(level);
            net.zootycoon.animal.ZooAnimalHungerManager.tickServer(level);
            net.zootycoon.hunter.ZooHunterManager.tickServer(level);
            if (level.getServer() != null) {
                var eco = net.zootycoon.economy.ZooEconomyManager.get(level.getServer());
                for (var player : level.players()) {
                    eco.absorbPlayerEmeralds(player);
                }
            }
        });

        // 5c. Spyglass/Field Guide right-click animal registration & direct player feeding with love particles
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                net.minecraft.world.item.ItemStack held = player.getItemInHand(hand);
                if (net.zootycoon.animal.ZooAnimalHungerManager.isScanningItem(held)) {
                    if (net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(living)) {
                        boolean ok = net.zootycoon.animal.ZooAnimalHungerManager.registerAnimalFromSpyglass(sp, living);
                        if (ok) {
                            return net.minecraft.world.InteractionResult.SUCCESS;
                        }
                    }
                } else if (net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(living)
                        && net.zootycoon.animal.AnimalDietRegistry.isFoodValid(living, held)) {
                    boolean fed = net.zootycoon.animal.ZooAnimalHungerManager.feedAnimal(living, held, sp);
                    if (fed) {
                        ZooNetworkPayloads.sendAnimalSync(sp);
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                }
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        // 6. Initialize Creative Studio Dimension management
        StudioDimensionManager.initServerEvents();

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

            // Sync claims, staff and emerald wallet balance to player
            ZooClaimManager.get((net.minecraft.server.level.ServerLevel) player.level()).syncToPlayer(player);
            net.zootycoon.economy.ZooStaffManager.get((net.minecraft.server.level.ServerLevel) player.level()).syncToPlayer(player);
            net.zootycoon.economy.ZooEconomyManager.get(server).syncToPlayer(player);

            // Unlock Jake's Build Tools recipes if present
            try {
                var funcOpt = server.getFunctions().get(ResourceLocation.fromNamespaceAndPath("jbt", "give_recipes"));
                funcOpt.ifPresent(func -> server.getFunctions().execute(func, player.createCommandSourceStack().withSuppressedOutput().withPermission(2)));
            } catch (Exception ignored) {}
        });

        LOGGER.info("Zoo Tycoon Simulation initialized successfully.");
    }
}

