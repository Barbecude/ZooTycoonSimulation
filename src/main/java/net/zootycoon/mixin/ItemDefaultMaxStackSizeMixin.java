package net.zootycoon.mixin;

import net.minecraft.world.item.Item;
import net.zootycoon.entity.ZooCashierEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemDefaultMaxStackSizeMixin {
    @Inject(method = "getDefaultMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void zt_plushieDefaultMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        Item item = (Item) (Object) this;
        if (ZooCashierEntity.isPlushieItem(item)) {
            cir.setReturnValue(64);
        }
    }
}

