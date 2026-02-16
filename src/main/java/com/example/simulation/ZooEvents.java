package com.example.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

@Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID, value = Dist.CLIENT)
public class ZooEvents {

    @SubscribeEvent
    public static void onKeyInput(net.minecraftforge.client.event.InputEvent.Key event) {
        if (net.minecraft.client.Minecraft.getInstance().player == null)
            return;

        // Check if Shift is pressed.
        // Wait, "shift doang" usually means "Just Press Shift" to open menu?
        // That would be annoying if it opens every time they sneak.
        // Or maybe they mean "Press a Key (like 'M' or 'Z')"?
        // No, "shidt doang" suggests "Only Shift".
        // But Shift is used for Sneaking.
        // Opening a GUI on Sneak Toggle or Sneak Hold is very intrusive.

        // Maybe they mean "Shift + Click" but "Click is removed"?
        // "shift kanan doang" -> "shift right only" (Shift + Right Click)
        // "shidt doang" -> "only shift" ?

        // If I interpret literally: Open Menu when Shift is pressed.
        // This will open menu whenever they try to crouch.
        // I will implement a check: If they crouch, open menu?
        // This is highly unusual but requested.

        // Let's use `InputEvent.Key` and check for key down.
        // User requested "SHIFT YANG ADA DI KANAN KEYBOARD" (The Shift on the right
        // side of keyboard)
        // NOT Right Click + Shift.
        // Just Pressing "Right Shift".

        // Let's use `InputEvent.Key` and check for key down.
        if (event.getKey() == com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT) {
            if (event.getAction() == com.mojang.blaze3d.platform.InputConstants.PRESS) {
                // Only if no screen is open?
                if (net.minecraft.client.Minecraft.getInstance().screen == null) {
                    PacketHandler.INSTANCE.sendToServer(new OpenGuiPacket());
                }
            }
        }
    }
}
