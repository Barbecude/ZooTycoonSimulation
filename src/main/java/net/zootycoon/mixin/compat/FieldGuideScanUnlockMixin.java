package net.zootycoon.mixin.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.animal.ZooAnimalHungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.evandev.fieldguide.network.ScanUnlockPacket")
public abstract class FieldGuideScanUnlockMixin {

    @Shadow(remap = false)
    private int targetEntityId;

    @Inject(method = "handleServer", at = @At("TAIL"), remap = false, require = 0)
    private void zootycoon$onFieldGuideScanServer(ServerPlayer player, CallbackInfo ci) {
        if (player == null || player.level() == null) return;
        if (this.targetEntityId <= 0) return;

        Entity entity = player.level().getEntity(this.targetEntityId);
        if (entity instanceof LivingEntity living) {
            ZooAnimalHungerManager.registerAnimalFromSpyglass(player, living);
        }
    }
}
