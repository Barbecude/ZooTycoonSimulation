package net.zootycoon.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.claim.ClaimTier;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.client.render.ClaimBoundaryRenderer;
import net.zootycoon.client.render.PlacementPreviewRenderer;
import net.zootycoon.client.ui.MainManagementScreen;
import net.zootycoon.client.ui.StudioControlScreen;
import net.zootycoon.network.ZooNetworkPayloads;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class ZooTycoonClient implements ClientModInitializer {

    public static final KeyMapping OPEN_MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.zootycoon.open_menu",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_RSHIFT,
            KeyMapping.CATEGORY_MISC
    ));
    private static boolean lastRShiftDown = false;

    public static boolean inStudio = false;
    public static String studioCategory = "enclosures";

    public static int clientBuilders = 1;
    public static int clientActiveBuilders = 0;
    public static int clientZookeepers = 0;
    public static int clientSecurity = 0;
    public static java.util.List<ZooNetworkPayloads.AnimalSyncData> clientAnimals = new java.util.ArrayList<>();
    public static final java.util.Map<Integer, ZooNetworkPayloads.AnimalSyncData> clientAnimalMap = new java.util.concurrent.ConcurrentHashMap<>();

    private static int clientScopeTicks = 0;
    private static int clientLastTargetId = -1;
    private static int clientSpyglassCooldown = 0;

    public static ZooNetworkPayloads.AnimalSyncData getAnimalData(int entityId) {
        return clientAnimalMap.get(entityId);
    }

    @Override
    public void onInitializeClient() {
        ZooTycoonMod.LOGGER.info("Initializing Zoo Tycoon Client for Fabric 26.3...");

        // 1. Right-click actions with Stick (minecraft:stick)
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (level.isClientSide() && stack.is(net.minecraft.world.item.Items.STICK)) {
                // If placing schematic, shift-click cancels, normal click builds
                if (PlacementPreviewRenderer.getActiveSchematic() != null) {
                    if (player.isShiftKeyDown()) {
                        PlacementPreviewRenderer.clear();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Penempatan bangunan dibatalkan.").withStyle(net.minecraft.ChatFormatting.YELLOW), true);
                    } else {
                        PlacementPreviewRenderer.confirmBuild(null);
                    }
                    return InteractionResultHolder.success(stack);
                }

                // If in studio, open studio control screen
                if (inStudio) {
                    Minecraft.getInstance().setScreen(new StudioControlScreen(studioCategory));
                    return InteractionResultHolder.success(stack);
                }

                // Land selection preview cancellation / confirmation
                if (ClaimBoundaryRenderer.isBoxReady()) {
                    if (player.isShiftKeyDown()) {
                        ClaimBoundaryRenderer.clearClaimPreview();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Seleksi beli tanah dibatalkan.").withStyle(net.minecraft.ChatFormatting.YELLOW), true);
                    } else {
                        ClaimBoundaryRenderer.confirmActiveClaim();
                    }
                    return InteractionResultHolder.success(stack);
                }
            }
            return InteractionResultHolder.pass(stack);
        });

        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide() && player.getItemInHand(hand).is(net.minecraft.world.item.Items.STICK)) {
                if (PlacementPreviewRenderer.getActiveSchematic() != null) {
                    if (player.isShiftKeyDown()) {
                        PlacementPreviewRenderer.clear();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Penempatan bangunan dibatalkan.").withStyle(net.minecraft.ChatFormatting.YELLOW), true);
                    } else {
                        PlacementPreviewRenderer.confirmBuild(hitResult.getBlockPos());
                    }
                    return InteractionResult.SUCCESS;
                }

                if (ClaimBoundaryRenderer.isBoxReady()) {
                    if (player.isShiftKeyDown()) {
                        ClaimBoundaryRenderer.clearClaimPreview();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Seleksi beli tanah dibatalkan.").withStyle(net.minecraft.ChatFormatting.YELLOW), true);
                    } else {
                        ClaimBoundaryRenderer.confirmActiveClaim();
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        // 2. Intercept left-click block selection when holding Stick
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (level.isClientSide() && player.getMainHandItem().is(net.minecraft.world.item.Items.STICK)) {
                ClaimBoundaryRenderer.handleLeftClick(pos);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        // 3. Register Network Receivers for Syncing Claims & Studio State
        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.SyncClaimsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClaimBoundaryRenderer.CLIENT_CLAIMS.clear();
                for (var d : payload.claims()) {
                    BoundingBox box = new BoundingBox(d.minX(), d.minY(), d.minZ(), d.maxX(), d.maxY(), d.maxZ());
                    ClaimTier tier = ClaimTier.valueOf(d.tierName());
                    ZooClaim claim = new ZooClaim(d.claimId(), d.ownerUuid(), d.ownerName(), box, tier, 0L);
                    ClaimBoundaryRenderer.CLIENT_CLAIMS.add(claim);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.SyncStudioStatePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                inStudio = payload.inStudio();
                studioCategory = payload.category();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.SyncStaffPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                clientBuilders = payload.builders();
                clientActiveBuilders = payload.activeBuilders();
                clientZookeepers = payload.zookeepers();
                clientSecurity = payload.security();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.SyncAnimalsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                clientAnimals = new java.util.ArrayList<>(payload.animals());
                clientAnimalMap.clear();
                for (var a : payload.animals()) {
                    clientAnimalMap.put(a.entityId(), a);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.OpenBuilderScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                java.util.Map<net.minecraft.world.item.Item, Integer> missingMap = new java.util.HashMap<>();
                for (var entry : payload.missing()) {
                    try {
                        net.minecraft.resources.ResourceLocation key = net.minecraft.resources.ResourceLocation.parse(entry.itemId());
                        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(key);
                        if (item != null && item != net.minecraft.world.item.Items.AIR) {
                            missingMap.put(item, entry.count());
                        }
                    } catch (Exception ignored) {}
                }
                Minecraft.getInstance().setScreen(new net.zootycoon.client.ui.BuilderInspectionScreen(
                        payload.siteId(), payload.schematicName(), payload.progress(),
                        payload.placed(), payload.total(), missingMap, payload.builderName(),
                        payload.phase(), payload.obstructingCount(),
                        payload.needsShovel(), payload.hasShovel(),
                        payload.needsPickaxe(), payload.hasPickaxe(),
                        payload.needsAxe(), payload.hasAxe(),
                        payload.wageDue(), payload.builderUuid()
                ));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.OpenZookeeperScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new net.zootycoon.client.ui.ZookeeperDialogScreen(
                        payload.keeperUuid(),
                        payload.keeperName(),
                        payload.hasAnimal(),
                        payload.animalName(),
                        payload.hunger(),
                        payload.foodStock(),
                        payload.dialogText(),
                        payload.acceptedFoods()
                ));
            });
        });

        // 3b. Register Entity Renderer & Model Layer for Humanoid Builder NPC
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooBuilderModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooBuilderModel::createMesh
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.BUILDER,
                net.zootycoon.client.render.ZooBuilderRenderer::new
        );

        // 3c. Register Entity Renderer & Model Layer for Humanoid Zookeeper NPC
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooZookeeperModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooZookeeperModel::createMesh
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.ZOOKEEPER,
                net.zootycoon.client.render.ZooZookeeperRenderer::new
        );

        // 3d. Register Entity Renderer & Model Layer for Security Guard NPC
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooSecurityModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooSecurityModel::createMesh
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.SECURITY,
                net.zootycoon.client.render.ZooSecurityRenderer::new
        );

        // 3e. Register Entity Renderer & Model Layer for Hunter Mob
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooHunterModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooHunterModel::createMesh
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.HUNTER,
                net.zootycoon.client.render.ZooHunterRenderer::new
        );

        // 3f. Register Entity Renderer & Model Layer for Zoo Visitor NPC
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooVisitorModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooVisitorModel::createMesh
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.VISITOR,
                net.zootycoon.client.render.ZooVisitorRenderer::new
        );

        // 3g. Register Entity Renderer for Toilet Seat
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.TOILET_SEAT,
                net.zootycoon.client.render.ToiletSeatRenderer::new
        );

        // 4. Register in-world render event for boundaries & Litematica-style preview
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            ClaimBoundaryRenderer.render(context);
            PlacementPreviewRenderer.render(context);
            net.zootycoon.client.render.ZooWorldIndicatorRenderer.render(context);
        });

        // 5. Register client-tick particles and overlays + Right Shift menu opening
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClaimBoundaryRenderer.clientTick(client);
            PlacementPreviewRenderer.clientTick(client);

            // Open Zoo Management Menu on Right Shift (no stick required)
            boolean rShiftDown = (client.getWindow() != null) && InputConstants.isKeyDown(client.getWindow().getWindow(), InputConstants.KEY_RSHIFT);
            boolean keyClicked = OPEN_MENU_KEY.consumeClick();
            if ((keyClicked || (rShiftDown && !lastRShiftDown)) && client.screen == null && client.player != null) {
                client.setScreen(new MainManagementScreen());
            }
            lastRShiftDown = rShiftDown;

            // Client-side Spyglass scoping animal registration
            if (client.player != null && client.level != null) {
                boolean scoping = client.player.isScoping()
                        || (client.player.isUsingItem() && client.player.getUseItem().is(net.minecraft.world.item.Items.SPYGLASS))
                        || client.player.getMainHandItem().is(net.minecraft.world.item.Items.SPYGLASS)
                        || client.player.getOffhandItem().is(net.minecraft.world.item.Items.SPYGLASS);

                if (scoping && (client.player.isScoping() || client.player.isUsingItem())) {
                    if (clientSpyglassCooldown > 0) {
                        clientSpyglassCooldown--;
                    } else {
                        var eyePos = client.player.getEyePosition();
                        var lookVec = client.player.getViewVector(1.0f);
                        var reachVec = eyePos.add(lookVec.scale(64.0));
                        var box = client.player.getBoundingBox().expandTowards(lookVec.scale(64.0)).inflate(4.0);

                        var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                                client.player, eyePos, reachVec, box,
                                e -> e instanceof net.minecraft.world.entity.LivingEntity living && living.isAlive() && net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(living),
                                0.5
                        );

                        if (hit != null && hit.getEntity() instanceof net.minecraft.world.entity.LivingEntity target) {
                            if (target.getId() == clientLastTargetId) {
                                clientScopeTicks++;
                                if (clientScopeTicks >= 6) { // ~0.3s of scoping
                                    ClientPlayNetworking.send(new ZooNetworkPayloads.ScanAnimalPayload(target.getId()));
                                    clientSpyglassCooldown = 40; // 2s cooldown
                                    clientScopeTicks = 0;
                                }
                            } else {
                                clientLastTargetId = target.getId();
                                clientScopeTicks = 1;
                            }
                        } else {
                            if (clientScopeTicks > 0) clientScopeTicks--;
                        }
                    }
                } else {
                    clientScopeTicks = 0;
                    clientLastTargetId = -1;
                    if (clientSpyglassCooldown > 0) clientSpyglassCooldown--;
                }
            }
        });

        // 6. Register sleek top-left HUD overlay
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(
                net.zootycoon.client.ui.ZooHudOverlay::render
        );

        ZooTycoonMod.LOGGER.info("Zoo Tycoon Client initialized successfully.");
    }
}
