package net.zootycoon.mixin;

import net.minecraft.world.entity.Mob;
import net.zootycoon.animal.ZooAnimalHungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ensures any animal registered in the zoo never despawns.
 * Cancels checkDespawn, overrides removeWhenFarAway, and enforces custom persistence.
 */
@Mixin(Mob.class)
public abstract class ZooAnimalDespawnMixin {

    @Shadow
    public abstract void setPersistenceRequired();

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void zootycoon$preventRegisteredAnimalDespawn(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (ZooAnimalHungerManager.isTagged(mob)) {
            ((LivingEntityAccessor) mob).zootycoon$setNoActionTime(0);
            if (!mob.isPersistenceRequired()) {
                this.setPersistenceRequired();
            }
            ci.cancel();
        }
    }

    @Inject(method = "removeWhenFarAway", at = @At("HEAD"), cancellable = true)
    private void zootycoon$neverRemoveRegisteredAnimalWhenFarAway(double distanceToClosestPlayer, CallbackInfoReturnable<Boolean> cir) {
        Mob mob = (Mob) (Object) this;
        if (ZooAnimalHungerManager.isTagged(mob)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "requiresCustomPersistence", at = @At("HEAD"), cancellable = true)
    private void zootycoon$registeredAnimalRequiresCustomPersistence(CallbackInfoReturnable<Boolean> cir) {
        Mob mob = (Mob) (Object) this;
        if (ZooAnimalHungerManager.isTagged(mob)) {
            cir.setReturnValue(true);
        }
    }
}

