package net.zootycoon.mixin;

import net.minecraft.world.level.biome.Climate;
import net.zootycoon.worldgen.FastWorldGenOptimizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Climate.ParameterList.class)
public class ClimateRTreeFastSearchMixin<T> {

    @Unique
    private static final int zootycoon$CACHE_SIZE = 512;

    @Unique
    private static final int zootycoon$CACHE_MASK = zootycoon$CACHE_SIZE - 1;

    @Unique
    private final ThreadLocal<long[]> zootycoon$keys = ThreadLocal.withInitial(() -> new long[zootycoon$CACHE_SIZE]);

    @Unique
    private final ThreadLocal<Object[]> zootycoon$vals = ThreadLocal.withInitial(() -> new Object[zootycoon$CACHE_SIZE]);

    @SuppressWarnings("unchecked")
    @Inject(method = "findValue", at = @At("HEAD"), cancellable = true)
    private void zootycoon$fastCacheLookup(
            Climate.TargetPoint point,
            CallbackInfoReturnable<T> cir
    ) {
        if (!FastWorldGenOptimizer.isEnabled() || point == null) {
            return;
        }
        long key = FastWorldGenOptimizer.packClimateKey(point);
        int slot = FastWorldGenOptimizer.hashSlot(key, zootycoon$CACHE_MASK);
        long[] keys = this.zootycoon$keys.get();
        if (keys[slot] == key) {
            Object cached = this.zootycoon$vals.get()[slot];
            if (cached != null) {
                FastWorldGenOptimizer.recordCacheHit();
                cir.setReturnValue((T) cached);
            }
        }
    }

    @Inject(method = "findValue", at = @At("RETURN"))
    private void zootycoon$fastCacheStore(
            Climate.TargetPoint point,
            CallbackInfoReturnable<T> cir
    ) {
        if (!FastWorldGenOptimizer.isEnabled() || point == null) {
            return;
        }
        T result = cir.getReturnValue();
        if (result != null) {
            long key = FastWorldGenOptimizer.packClimateKey(point);
            int slot = FastWorldGenOptimizer.hashSlot(key, zootycoon$CACHE_MASK);
            this.zootycoon$keys.get()[slot] = key;
            this.zootycoon$vals.get()[slot] = result;
            FastWorldGenOptimizer.recordCacheMiss();
        }
    }
}
