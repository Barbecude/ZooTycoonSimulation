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

        // 5b. Initialize Zoo Animal Hunger, Hunter incursions, and Hostile Mob Claims Barrier
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(level -> {
            net.zootycoon.claim.ZooClaimManager.tickServer(level);
            net.zootycoon.animal.ZooAnimalHungerManager.tickServer(level);
            net.zootycoon.hunter.ZooHunterManager.tickServer(level);
        });

        // 5c. Spyglass right-click animal registration
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                net.minecraft.world.item.ItemStack held = player.getItemInHand(hand);
                if (held.is(Items.SPYGLASS)) {
                    if (net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(living)) {
                        boolean ok = net.zootycoon.animal.ZooAnimalHungerManager.registerAnimalFromSpyglass(sp, living);
                        if (ok) {
                            return net.minecraft.world.InteractionResult.SUCCESS;
                        }
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

                // Give Starter Emeralds
                int starterEmeralds = EconomyConfig.getStarterEmeralds();
                ItemStack emeralds = new ItemStack(Items.EMERALD, starterEmeralds);
                if (!player.getInventory().add(emeralds)) {
                    player.drop(emeralds, false);
                }

                player.sendSystemMessage(Component.translatable("zootycoon.welcome.message"));
                LOGGER.info("Gave starter pack to player {}", player.getName().getString());
            }

            // Sync claims to player
            ZooClaimManager.get((net.minecraft.server.level.ServerLevel) player.level()).syncToPlayer(player);
            // Sync staff to player
            net.zootycoon.economy.ZooStaffManager.get((net.minecraft.server.level.ServerLevel) player.level()).syncToPlayer(player);
        });

        LOGGER.info("Zoo Tycoon Simulation initialized successfully.");
    }
}

