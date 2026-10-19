package net.zootycoon.mixin.compat;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.network.ZooNetworkPayloads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.evandev.fieldguide.client.scan.manager.FieldGuideScanManager")
public abstract class FieldGuideScanManagerMixin {

    @Inject(method = "completeScan", at = @At("HEAD"), remap = false, require = 0)
    private void zootycoon$onFieldGuideCompleteScan(Minecraft mc, Object entry, Object target, CallbackInfo ci) {
        Entity entity = null;
        if (target instanceof Entity e) {
            entity = e;
        } else if (entry instanceof Entity e) {
            entity = e;
        } else {
            try {
                Class<?> stateClass = Class.forName("com.evandev.fieldguide.client.scan.manager.FieldGuideScanState");
                Object state = stateClass.getMethod("getInstance").invoke(null);
                if (state != null) {
                    Object scanning = stateClass.getMethod("getScanningEntity").invoke(state);
                    if (scanning instanceof Entity e) {
                        entity = e;
                    } else {
                        Object fading = stateClass.getMethod("getFadingEntity").invoke(state);
                        if (fading instanceof Entity e) {
                            entity = e;
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        if (entity == null && mc != null && mc.crosshairPickEntity instanceof LivingEntity) {
            entity = mc.crosshairPickEntity;
        }

        if (entity instanceof LivingEntity living && living.isAlive()) {
            ClientPlayNetworking.send(new ZooNetworkPayloads.ScanAnimalPayload(living.getId()));
            living.addTag("zoo_registered");
            net.zootycoon.client.inspect.InspectionTargetTracker.markRegisteredLocally(living.getId());
        }
    }
}
