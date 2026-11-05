package net.zootycoon.mixin.client;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Worlds that use modded worldgen (custom biomes/noise from datapacks) are flagged with an experimental
 * lifecycle, which makes vanilla show the "Worlds using Experimental Settings are not supported" backup
 * prompt on every load. Reporting a stable lifecycle skips that prompt.
 */
@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {

    @Redirect(method = "openWorldCheckWorldStemCompatibility",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/WorldData;worldGenSettingsLifecycle()Lcom/mojang/serialization/Lifecycle;"),
            require = 0)
    private Lifecycle zootycoon$skipExperimentalWarning(WorldData data) {
        return Lifecycle.stable();
    }
}
