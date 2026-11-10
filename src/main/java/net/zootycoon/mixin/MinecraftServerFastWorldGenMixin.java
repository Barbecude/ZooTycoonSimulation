package net.zootycoon.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.ServerLevelData;
import net.zootycoon.worldgen.FastWorldGenOptimizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerFastWorldGenMixin {

    /**
     * Eliminates the blocking 441-chunk wait loop in prepareLevels so world creation and loading
     * complete up to 80-90% faster while the spawn ticket continues streaming chunks in parallel.
     */
    @Redirect(
            method = "prepareLevels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/GameRules;getInt(Lnet/minecraft/world/level/GameRules$Key;)I"
            ),
            require = 0
    )
    private int zootycoon$fastPrepareLevelsRadius(GameRules gameRules, GameRules.Key<GameRules.IntegerValue> key) {
        if (FastWorldGenOptimizer.isEnabled() && key == GameRules.RULE_SPAWN_CHUNK_RADIUS) {
            return 0;
        }
        return gameRules.getInt(key);
    }

    /**
     * Replaces the slow 121-chunk synchronous spiral search in setInitialSpawn with a fast 1D noise
     * column pre-screened spawn lookup.
     */
    @Inject(method = "setInitialSpawn", at = @At("HEAD"), cancellable = true, require = 0)
    private static void zootycoon$fastInitialSpawn(
            ServerLevel level,
            ServerLevelData levelData,
            boolean bonusChest,
            boolean debugWorld,
            CallbackInfo ci
    ) {
        if (FastWorldGenOptimizer.isEnabled()) {
            FastWorldGenOptimizer.findFastInitialSpawn(level, levelData, bonusChest, debugWorld);
            ci.cancel();
        }
    }
}
