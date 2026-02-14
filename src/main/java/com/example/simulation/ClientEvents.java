package com.example.simulation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * ClientEvents — Registrasi event khusus client-side:
 * - Entity attributes (Staff & Visitor)
 * - GUI Overlay (HUD saldo)
 */
public class ClientEvents {

    /**
     * Mod Bus events — dipanggil selama loading mod.
     * Mendaftarkan atribut entity dan overlay GUI.
     */
    @Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(IndoZooTycoon.STAFF_ENTITY.get(), StaffEntity.createAttributes().build());
            event.put(IndoZooTycoon.VISITOR_ENTITY.get(), VisitorEntity.createAttributes().build());
        }
    }

    /**
     * Client-only Mod Bus events — mendaftarkan HUD overlay.
     */
    @Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents {

        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("zoo_hud", ZooOverlay.HUD_OVERLAY);
        }
    }
}
