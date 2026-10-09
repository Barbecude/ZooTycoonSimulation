package net.zootycoon.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.render.ClaimBoundaryRenderer;
import net.zootycoon.client.render.PlacementPreviewRenderer;
import net.zootycoon.client.ui.MainManagementScreen;
import net.zootycoon.item.BuildToolItem;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.schematic.ZooSchematic;

public class ZooTycoonClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ZooTycoonMod.LOGGER.info("Initializing Zoo Tycoon Client for Fabric 26.3...");

        // 1. Hook into BuildToolItem client actions
        BuildToolItem.clientHook = new BuildToolItem.ClientScreenOpener() {
            @Override
            public void openMainScreen() {
                Minecraft.getInstance().setScreen(new MainManagementScreen());
            }

            @Override
            public void handleBlockClick(UseOnContext context) {
                ZooSchematic active = PlacementPreviewRenderer.getActiveSchematic();
                BlockPos targetPos = PlacementPreviewRenderer.getTargetPlacementPos();
                if (active != null && targetPos != null) {
                    // Send build request to server
                    ClientPlayNetworking.send(new ZooNetworkPayloads.RequestBuildPayload(targetPos, active.getId()));
                    PlacementPreviewRenderer.clear();
                }
            }

            @Override
            public void cancelPlacement() {
                PlacementPreviewRenderer.clear();
            }

            @Override
            public boolean isPlacingSchematic() {
                return PlacementPreviewRenderer.getActiveSchematic() != null;
            }
        };

        // 2. Register in-world render event for boundaries & pulsating preview
        WorldRenderEvents.LAST.register(context -> {
            ClaimBoundaryRenderer.render(context.matrixStack(), context.camera(), context.tickDelta());
            PlacementPreviewRenderer.render(context.matrixStack(), context.camera(), context.tickDelta());
        });

        ZooTycoonMod.LOGGER.info("Zoo Tycoon Client initialized successfully.");
    }
}
