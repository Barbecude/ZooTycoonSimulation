package net.zootycoon.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.zootycoon.worldgen.FastWorldGenOptimizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunkSection.class)
public class LevelChunkSectionFastBiomeMixin {

    @Shadow
    private PalettedContainerRO<Holder<Biome>> biomes;

    @Inject(method = "fillBiomesFromNoise", at = @At("HEAD"), cancellable = true)
    private void zootycoon$fastFillBiomesFromNoise(
            BiomeResolver resolver,
            Climate.Sampler sampler,
            int quartMinX,
            int quartMinY,
            int quartMinZ,
            CallbackInfo ci
    ) {
        if (!FastWorldGenOptimizer.isEnabled()) {
            return;
        }

        // Sample the 8 corners of the 4x4x4 biome section first
        Holder<Biome> c000 = resolver.getNoiseBiome(quartMinX, quartMinY, quartMinZ, sampler);
        Holder<Biome> c300 = resolver.getNoiseBiome(quartMinX + 3, quartMinY, quartMinZ, sampler);
        Holder<Biome> c030 = resolver.getNoiseBiome(quartMinX, quartMinY + 3, quartMinZ, sampler);
        Holder<Biome> c330 = resolver.getNoiseBiome(quartMinX + 3, quartMinY + 3, quartMinZ, sampler);
        Holder<Biome> c003 = resolver.getNoiseBiome(quartMinX, quartMinY, quartMinZ + 3, sampler);
        Holder<Biome> c303 = resolver.getNoiseBiome(quartMinX + 3, quartMinY, quartMinZ + 3, sampler);
        Holder<Biome> c033 = resolver.getNoiseBiome(quartMinX, quartMinY + 3, quartMinZ + 3, sampler);
        Holder<Biome> c333 = resolver.getNoiseBiome(quartMinX + 3, quartMinY + 3, quartMinZ + 3, sampler);

        PalettedContainer<Holder<Biome>> container = this.biomes.recreate();

        // If all 8 corners match, fill the 4x4x4 section without 56 redundant noise evaluations (87.5% savings)
        if (c000 == c300 && c000 == c030 && c000 == c330
                && c000 == c003 && c000 == c303 && c000 == c033 && c000 == c333) {
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    for (int z = 0; z < 4; z++) {
                        container.getAndSetUnchecked(x, y, z, c000);
                    }
                }
            }
            this.biomes = container;
            FastWorldGenOptimizer.recordSectionFastFill();
            ci.cancel();
            return;
        }

        // Otherwise, reuse the 8 already-computed corners and evaluate only the remaining 56 cells
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    Holder<Biome> b;
                    if ((x == 0 || x == 3) && (y == 0 || y == 3) && (z == 0 || z == 3)) {
                        if (x == 0) {
                            b = (y == 0) ? (z == 0 ? c000 : c003) : (z == 0 ? c030 : c033);
                        } else {
                            b = (y == 0) ? (z == 0 ? c300 : c303) : (z == 0 ? c330 : c333);
                        }
                    } else {
                        b = resolver.getNoiseBiome(quartMinX + x, quartMinY + y, quartMinZ + z, sampler);
                    }
                    container.getAndSetUnchecked(x, y, z, b);
                }
            }
        }
        this.biomes = container;
        ci.cancel();
    }
}
