package net.zootycoon.mixin;

import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.zootycoon.animal.ZooAnimalHungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks breeding parents as paired even for egg-laying species (e.g. Naturalist Ostrich)
 * that bypass Animal.spawnChildFromBreeding and trigger CriteriaTriggers.BRED_ANIMALS directly.
 */
@Mixin(BredAnimalsTrigger.class)
public class BredAnimalsTriggerMixin {

    @Inject(method = "trigger", at = @At("HEAD"))
    private void zooTycoon$onBredTrigger(
            ServerPlayer player,
            Animal parent,
            Animal partner,
            AgeableMob child,
            CallbackInfo ci
    ) {
        ZooAnimalHungerManager.markParentsPaired(parent, partner);
    }
}
