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
        if (target instanceof Entity entity && entity instanceof LivingEntity) {
            ClientPlayNetworking.send(new ZooNetworkPayloads.ScanAnimalPayload(entity.getId()));
        } else if (entry instanceof Entity entity && entity instanceof LivingEntity) {
            ClientPlayNetworking.send(new ZooNetworkPayloads.ScanAnimalPayload(entity.getId()));
        }
    }
}
