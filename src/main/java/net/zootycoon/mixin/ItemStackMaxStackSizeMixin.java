package net.zootycoon.mixin;

import net.minecraft.world.item.ItemStack;
import net.zootycoon.entity.ZooCashierEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMaxStackSizeMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void zt_plushieMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ZooCashierEntity.isPlushieItem(stack.getItem())) {
            cir.setReturnValue(64);
        }
    }
}

