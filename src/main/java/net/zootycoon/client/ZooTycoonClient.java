package net.zootycoon.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.claim.ClaimTier;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.client.render.ClaimBoundaryRenderer;
import net.zootycoon.client.render.PlacementPreviewRenderer;
import net.zootycoon.client.ui.MainManagementScreen;
import net.zootycoon.client.ui.StudioControlScreen;
import net.zootycoon.client.ui.ZooHudOverlay;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.lang.reflect.Method;

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
    public static int clientCashiers = 0;
    public static int clientEmeraldBalance = 0;
    public static int clientZooRating = 50;
    public static java.util.List<ZooNetworkPayloads.AnimalSyncData> clientAnimals = new java.util.ArrayList<>();
    public static java.util.List<ZooNetworkPayloads.ZookeeperSyncData> clientZookeeperList = new java.util.ArrayList<>();
    public static final java.util.Map<Integer, ZooNetworkPayloads.AnimalSyncData> clientAnimalMap = new java.util.concurrent.ConcurrentHashMap<>();

    private static int clientScopeTicks = 0;
    private static int clientLastTargetId = -1;
    private static int clientSpyglassCooldown = 0;

    // Reflection cache for Field Guide integration
    private static boolean fieldGuideChecked = false;
    private static Method fgGetInstance = null;
    private static Method fgGetScanningTarget = null;
    private static Method fgGetFadingTarget = null;

    public static ZooNetworkPayloads.AnimalSyncData getAnimalData(int entityId) {
        return clientAnimalMap.get(entityId);
    }

    @Override
    public void onInitializeClient() {
        ZooTycoonMod.LOGGER.info("Initializing Zoo Tycoon Client for Fabric 26.3...");

        // 0. Register Spawn Egg color providers for Visitor and Cashier spawn eggs
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (stack.getItem() instanceof SpawnEggItem egg) {
                return 0xFF000000 | egg.getColor(tintIndex);
            }
            return -1;
        }, ZooItems.VISITOR_SPAWN_EGG, ZooItems.CASHIER_SPAWN_EGG, ZooItems.HUNTER_SPAWN_EGG);

        // 1. Right-click actions with Stick (minecraft:stick)
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (level.isClientSide() && stack.is(net.minecraft.world.item.Items.STICK)) {
                // If placing schematic, shift-click cancels, normal click builds
                if (PlacementPreviewRenderer.getActiveSchematic() != null) {
                    if (player.isShiftKeyDown()) {
                        PlacementPreviewRenderer.clear();
                        ZooHudOverlay.showToast("zootycoon.toast.title.info", "zootycoon.placement.cancelled", 2);
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
                        ZooHudOverlay.showToast("zootycoon.toast.title.claim", "zootycoon.claim.selection_cancelled", 2);
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
                if (inStudio) {
                    Minecraft.getInstance().setScreen(new StudioControlScreen(studioCategory));
                    return InteractionResult.SUCCESS;
                }

                if (PlacementPreviewRenderer.getActiveSchematic() != null) {
                    if (player.isShiftKeyDown()) {
                        PlacementPreviewRenderer.clear();
                        ZooHudOverlay.showToast("zootycoon.toast.title.info", "zootycoon.placement.cancelled", 2);
                    } else {
                        PlacementPreviewRenderer.confirmBuild(hitResult.getBlockPos());
                    }
                    return InteractionResult.SUCCESS;
                }

                if (ClaimBoundaryRenderer.isBoxReady()) {
                    if (player.isShiftKeyDown()) {
                        ClaimBoundaryRenderer.clearClaimPreview();
                        ZooHudOverlay.showToast("zootycoon.toast.title.claim", "zootycoon.claim.selection_cancelled", 2);
                    } else {
                        ClaimBoundaryRenderer.confirmActiveClaim();
                    }
                    return InteractionResult.SUCCESS;
                } else if (ClaimBoundaryRenderer.hasSelection()) {
                    if (player.isShiftKeyDown()) {
                        ClaimBoundaryRenderer.clearClaimPreview();
                        ZooHudOverlay.showToast("zootycoon.toast.title.claim", "zootycoon.claim.selection_cancelled", 2);
                    } else {
                        boolean ok = ClaimBoundaryRenderer.handleRightClickBlock(hitResult.getBlockPos());
                        if (ok) return InteractionResult.SUCCESS;
                    }
                } else {
                    ClaimBoundaryRenderer.handleLeftClick(hitResult.getBlockPos());
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        // 2. Intercept left-click block selection when holding Stick
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (inStudio) {
                return InteractionResult.PASS;
            }
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
                clientCashiers = payload.cashiers();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.SyncEmeraldBalancePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                clientEmeraldBalance = payload.balance();
                net.zootycoon.economy.ZooEconomyManager.setClientEmeraldBalance(payload.balance());
                if (payload.delta() != 0) {
                    ZooHudOverlay.addDelta(payload.delta());
                }
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

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.SyncZookeepersPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                clientZookeeperList = new java.util.ArrayList<>(payload.zookeepers());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.PlayBrushAnimationPayload.TYPE, (payload, context) -> {
            context.client().execute(net.zootycoon.client.animation.PlayerBrushAnimationManager::startBrushing);
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.ShowToastPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Object[] fmtArgs = payload.args() != null ? payload.args().toArray() : new Object[0];
                ZooHudOverlay.showToast(payload.titleKey(), payload.msgKey(), payload.toastType(), fmtArgs);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.RequestClientSelectionSavePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (ClaimBoundaryRenderer.isBoxReady()) {
                    BlockPos p1 = ClaimBoundaryRenderer.getPos1();
                    BlockPos p2 = ClaimBoundaryRenderer.getPos2();
                    ClientPlayNetworking.send(new ZooNetworkPayloads.SaveCustomSchematicPayload(
                            p1, p2, payload.name(), payload.category(),
                            ZooTranslations.get("zootycoon.studio.custom_desc")
                    ));
                    ClaimBoundaryRenderer.clearClaimPreview();
                } else if (inStudio) {
                    if (payload.name().trim().isEmpty()) {
                        Minecraft.getInstance().setScreen(new StudioControlScreen(payload.category()));
                        return;
                    }
                    ClientPlayNetworking.send(new ZooNetworkPayloads.SaveCustomSchematicPayload(
                            BlockPos.ZERO, BlockPos.ZERO, payload.name(), payload.category(),
                            ZooTranslations.get("zootycoon.studio.custom_desc")
                    ));
                } else {
                    ZooHudOverlay.showToast(
                            "zootycoon.toast.title.warning",
                            "zootycoon.schematic.select_area_first",
                            2
                    );
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

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.OpenSecurityScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new net.zootycoon.client.ui.SecurityDialogScreen(
                        payload.entityId(),
                        payload.securityUuid(),
                        payload.securityName(),
                        payload.isAlert(),
                        payload.targetName(),
                        payload.health(),
                        payload.maxHealth()
                ));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.OpenVisitorScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new net.zootycoon.client.ui.VisitorDialogScreen(
                        payload.entityId(),
                        payload.visitorUuid(),
                        payload.variant(),
                        payload.isChild(),
                        payload.moodName(),
                        payload.moodLabel(),
                        payload.hunger(),
                        payload.thirst(),
                        payload.needsToilet(),
                        payload.hasTrash()
                ));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.OpenCashierScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof net.zootycoon.client.ui.CashierDialogScreen existing
                        && existing.getEntityId() == payload.entityId()
                        && existing.getCurrentRole().equalsIgnoreCase(payload.currentRole())) {
                    existing.updateStockItems(payload.stockItems());
                    existing.updateZooOpenState(payload.isZooOpen());
                } else {
                    mc.setScreen(new net.zootycoon.client.ui.CashierDialogScreen(
                            payload.entityId(),
                            payload.currentRole(),
                            payload.cashierName(),
                            payload.stockItems(),
                            payload.isZooOpen()
                    ));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.OpenFeedingTroughScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new net.zootycoon.client.ui.FeedingTroughDialogScreen(
                        payload.pos(),
                        payload.assignedAnimalId(),
                        payload.assignedAnimalUuid(),
                        payload.assignedAnimalName(),
                        payload.assignedAnimalSpecies(),
                        payload.assignedFoodItemId(),
                        payload.foodCount(),
                        payload.foodItemKey(),
                        payload.waterLevel()
                ));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.OpenGachaMachineScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (Minecraft.getInstance().screen instanceof net.zootycoon.client.ui.GachaMachineDialogScreen existingScreen) {
                    existingScreen.updateData(payload.totalStock(), payload.slots());
                } else {
                    Minecraft.getInstance().setScreen(new net.zootycoon.client.ui.GachaMachineDialogScreen(
                            payload.pos(),
                            payload.totalStock(),
                            payload.slots()
                    ));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ZooNetworkPayloads.SyncRatingPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                clientZooRating = payload.rating();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(net.zootycoon.network.InspectionPayloads.InspectionSnapshotPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                net.zootycoon.client.inspect.InspectionClientCache.update(payload);
            });
        });

        // Reset client static state when disconnecting from world or joining a new server
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClaimBoundaryRenderer.CLIENT_CLAIMS.clear();
            ClaimBoundaryRenderer.clearClaimPreview();
            clientZooRating = 50;
            clientAnimals.clear();
            clientAnimalMap.clear();
            clientZookeeperList.clear();
            clientEmeraldBalance = 0;
            clientBuilders = 1;
            clientActiveBuilders = 0;
            clientZookeepers = 0;
            clientSecurity = 0;
            clientCashiers = 0;
            inStudio = false;
            net.zootycoon.client.render.ZooAnimalHologramRenderer.reset();
            net.zootycoon.client.inspect.InspectionTargetTracker.reset();
            net.zootycoon.client.inspect.InspectionClientCache.clear();
        });

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClaimBoundaryRenderer.CLIENT_CLAIMS.clear();
            ClaimBoundaryRenderer.clearClaimPreview();
            clientAnimals.clear();
            clientAnimalMap.clear();
            clientZookeeperList.clear();
            clientEmeraldBalance = 0;
            net.zootycoon.client.inspect.InspectionTargetTracker.reset();
            net.zootycoon.client.inspect.InspectionClientCache.clear();
        });

        // 3b. Register Entity Renderer & Model Layer for Humanoid Builder NPC
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooBuilderModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooBuilderModel::createMesh
        );
        EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.BUILDER,
                net.zootycoon.client.render.ZooBuilderRenderer::new
        );

        // 3c. Register Entity Renderer & Model Layer for Humanoid Zookeeper NPC
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooZookeeperModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooZookeeperModel::createMesh
        );
        EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.ZOOKEEPER,
                net.zootycoon.client.render.ZooZookeeperRenderer::new
        );

        // 3d. Register Entity Renderer & Model Layer for Security Guard NPC
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooSecurityModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooSecurityModel::createMesh
        );
        EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.SECURITY,
                net.zootycoon.client.render.ZooSecurityRenderer::new
        );

        // 3e. Register Entity Renderer & Model Layer for Hunter Mob
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooHunterModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooHunterModel::createMesh
        );
        EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.HUNTER,
                net.zootycoon.client.render.ZooHunterRenderer::new
        );

        // 3f. Register Entity Renderer & Model Layer for Zoo Visitor NPC & Cashier NPC
        EntityModelLayerRegistry.registerModelLayer(
                net.zootycoon.client.model.ZooVisitorModel.LAYER_LOCATION,
                net.zootycoon.client.model.ZooVisitorModel::createMesh
        );
        EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.VISITOR,
                net.zootycoon.client.render.ZooVisitorRenderer::new
        );
        EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.CASHIER,
                net.zootycoon.client.render.ZooCashierRenderer::new
        );

        // 3g. Register Entity Renderer for Toilet Seat
        EntityRendererRegistry.register(
                net.zootycoon.entity.ZooEntities.TOILET_SEAT,
                net.zootycoon.client.render.ToiletSeatRenderer::new
        );

        // 3h. Register transparent cutout and translucent render layers for blocks (Trash, Feeding Trough, Trash Can, Gacha Machine, Heater, Cooler Box)
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(net.zootycoon.block.ZooBlocks.TRASH, net.minecraft.client.renderer.RenderType.cutout());
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(net.zootycoon.block.ZooBlocks.TRASH_CAN, net.minecraft.client.renderer.RenderType.cutout());
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(net.zootycoon.block.ZooBlocks.FEEDING_TROUGH, net.minecraft.client.renderer.RenderType.cutout());
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(net.zootycoon.block.ZooBlocks.HEATER, net.minecraft.client.renderer.RenderType.cutout());
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(net.zootycoon.block.ZooBlocks.COOLER, net.minecraft.client.renderer.RenderType.cutout());
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(net.zootycoon.block.ZooBlocks.GACHA_MACHINE, net.minecraft.client.renderer.RenderType.translucent());

        // 3h. Register Block Entity Renderer for Feeding Trough (Dynamic 3D food items) and Gacha Machine (Dynamic 3D plushies)
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                net.zootycoon.block.ZooBlocks.FEEDING_TROUGH_BLOCK_ENTITY,
                net.zootycoon.client.render.ZooFeedingTroughRenderer::new
        );
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                net.zootycoon.block.ZooBlocks.GACHA_MACHINE_BLOCK_ENTITY,
                net.zootycoon.client.render.ZooGachaMachineRenderer::new
        );

        // 4. Register in-world render event for boundaries & Litematica-style preview & Holograms
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            ClaimBoundaryRenderer.render(context);
            PlacementPreviewRenderer.render(context);
            net.zootycoon.client.render.ZooWorldIndicatorRenderer.render(context);
            net.zootycoon.client.render.ZooAnimalHologramRenderer.render(context);
            net.zootycoon.client.render.PlayerBathingHologramRenderer.render(context);
        });

        // 5. Register client-tick particles and overlays + Right Shift menu opening
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClaimBoundaryRenderer.clientTick(client);
            PlacementPreviewRenderer.clientTick(client);
            net.zootycoon.client.inspect.InspectionTargetTracker.tick(client);
            net.zootycoon.client.animation.PlayerBrushAnimationManager.clientTick();
            net.zootycoon.client.bathing.PlayerBathingClientController.clientTick(client);

            // Open Zoo Management Menu on Right Shift (no stick required)
            boolean rShiftDown = (client.getWindow() != null) && InputConstants.isKeyDown(client.getWindow().getWindow(), InputConstants.KEY_RSHIFT);
            boolean keyClicked = OPEN_MENU_KEY.consumeClick();
            if ((keyClicked || (rShiftDown && !lastRShiftDown)) && client.screen == null && client.player != null) {
                client.setScreen(new MainManagementScreen());
            }
            lastRShiftDown = rShiftDown;

            // Client-side Spyglass & Field Guide long-range animal registration
            if (client.player != null && client.level != null) {
                if (clientSpyglassCooldown > 0) {
                    clientSpyglassCooldown--;
                }

                // 5a. Poll FieldGuideScanState directly if Field Guide mod is installed
                pollFieldGuideScanTarget();

                // 5b. Long-range 64-block raycast while scoping or using Spyglass / Field Guide
                boolean hasScanItem = ZooAnimalHungerManager.isScanningItem(client.player.getMainHandItem())
                        || ZooAnimalHungerManager.isScanningItem(client.player.getOffhandItem())
                        || ZooAnimalHungerManager.isScanningItem(client.player.getUseItem());
                boolean activelyScanning = client.player.isScoping()
                        || (hasScanItem && client.player.isUsingItem());

                if (activelyScanning) {
                    if (clientSpyglassCooldown <= 0) {
                        var eyePos = client.player.getEyePosition();
                        var lookVec = client.player.getViewVector(1.0f);
                        var reachVec = eyePos.add(lookVec.scale(64.0));
                        var box = client.player.getBoundingBox().expandTowards(lookVec.scale(64.0)).inflate(4.0);

                        var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                                client.player, eyePos, reachVec, box,
                                e -> e instanceof LivingEntity living && living.isAlive() && ZooAnimalHungerManager.isZooAnimalCandidate(living),
                                64.0 * 64.0
                        );

                        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
                            if (target.getId() == clientLastTargetId) {
                                clientScopeTicks++;
                                if (clientScopeTicks >= 6) {
                                    ClientPlayNetworking.send(new ZooNetworkPayloads.ScanAnimalPayload(target.getId()));
                                    clientSpyglassCooldown = 30;
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
                }
            }
        });

        // 6. Register sleek top-left HUD overlay + Essential-Mod style Toasts
        HudRenderCallback.EVENT.register(ZooHudOverlay::render);

        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() && hand == net.minecraft.world.InteractionHand.MAIN_HAND && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                net.minecraft.world.item.ItemStack held = player.getItemInHand(hand);
                if (held.is(net.minecraft.world.item.Items.BRUSH)) {
                    if (net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(living)
                            && net.zootycoon.animal.ZooAnimalHungerManager.isTagged(living)
                            && net.zootycoon.animal.AnimalBathingManager.isWet(living)) {
                        net.zootycoon.client.animation.PlayerBrushAnimationManager.startBrushing();
                    }
                }
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        ZooTycoonMod.LOGGER.info("Zoo Tycoon Client initialized successfully.");
    }

    private static void pollFieldGuideScanTarget() {
        try {
            if (!fieldGuideChecked) {
                fieldGuideChecked = true;
                Class<?> stateClass = Class.forName("com.evandev.fieldguide.client.scan.FieldGuideScanState");
                fgGetInstance = stateClass.getMethod("getInstance");
                fgGetScanningTarget = stateClass.getMethod("getScanningTarget");
                fgGetFadingTarget = stateClass.getMethod("getFadingTarget");
            }
            if (fgGetInstance != null) {
                Object state = fgGetInstance.invoke(null);
                if (state != null) {
                    Object fading = fgGetFadingTarget != null ? fgGetFadingTarget.invoke(state) : null;
                    Object scanning = fgGetScanningTarget != null ? fgGetScanningTarget.invoke(state) : null;

                    Object candidate = fading != null ? fading : scanning;
                    if (candidate instanceof LivingEntity living && living.isAlive() && ZooAnimalHungerManager.isZooAnimalCandidate(living)) {
                        if (!clientAnimalMap.containsKey(living.getId()) && clientSpyglassCooldown <= 0) {
                            if (fading != null) {
                                ClientPlayNetworking.send(new ZooNetworkPayloads.ScanAnimalPayload(living.getId()));
                                clientSpyglassCooldown = 30;
                            } else {
                                if (living.getId() == clientLastTargetId) {
                                    clientScopeTicks++;
                                    if (clientScopeTicks >= 8) {
                                        ClientPlayNetworking.send(new ZooNetworkPayloads.ScanAnimalPayload(living.getId()));
                                        clientSpyglassCooldown = 30;
                                        clientScopeTicks = 0;
                                    }
                                } else {
                                    clientLastTargetId = living.getId();
                                    clientScopeTicks = 1;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
            // Field Guide not installed or class structure differs
        }
    }
}
