package net.zootycoon.client.worldgen;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.zootycoon.animal.habitat.SpeciesBiomeResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Live World Generation & Biome Seed Preview engine.
 * Reads the exact active Overworld ChunkGenerator, BiomeSource, and NoiseGeneratorSettings
 * from WorldCreationUiState (automatically supporting Default, Large Biomes, Amplified, Single Biome,
 * and modded worldgen datapacks such as Terralith, Tectonic, Biomes O' Plenty, etc.).
 */
public class SeedPreviewController {

    public static final int MAP_SIZE = 160;
    private static final int[] ZOOM_SCALES = {2, 4, 8, 16, 32};

    private static final ExecutorService PREVIEW_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() / 2)),
            r -> {
                Thread t = new Thread(r, "ZooTycoon-SeedPreviewWorker");
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 1);
                return t;
            }
    );

    private static final Map<String, Integer> BIOME_PALETTE = new HashMap<>();

    static {
        // Oceans & Rivers
        BIOME_PALETTE.put("ocean", 0xFF1B4F9C);
        BIOME_PALETTE.put("deep_ocean", 0xFF10326B);
        BIOME_PALETTE.put("warm_ocean", 0xFF28A0C8);
        BIOME_PALETTE.put("lukewarm_ocean", 0xFF2078B0);
        BIOME_PALETTE.put("deep_lukewarm_ocean", 0xFF165588);
        BIOME_PALETTE.put("cold_ocean", 0xFF22427A);
        BIOME_PALETTE.put("deep_cold_ocean", 0xFF162C58);
        BIOME_PALETTE.put("frozen_ocean", 0xFF5A78B8);
        BIOME_PALETTE.put("deep_frozen_ocean", 0xFF3E5690);
        BIOME_PALETTE.put("river", 0xFF3377CC);
        BIOME_PALETTE.put("frozen_river", 0xFF7CA4E0);

        // Beaches & Shores
        BIOME_PALETTE.put("beach", 0xFFF2DC99);
        BIOME_PALETTE.put("snowy_beach", 0xFFEAE5D3);
        BIOME_PALETTE.put("stony_shore", 0xFF919187);

        // Plains & Grasslands
        BIOME_PALETTE.put("plains", 0xFF7DB853);
        BIOME_PALETTE.put("sunflower_plains", 0xFF96C949);
        BIOME_PALETTE.put("meadow", 0xFF68B56A);
        BIOME_PALETTE.put("snowy_plains", 0xFFECF4F6);
        BIOME_PALETTE.put("ice_spikes", 0xFFA5D5E8);

        // Forests
        BIOME_PALETTE.put("forest", 0xFF3A8936);
        BIOME_PALETTE.put("flower_forest", 0xFF56A84C);
        BIOME_PALETTE.put("birch_forest", 0xFF589C48);
        BIOME_PALETTE.put("old_growth_birch_forest", 0xFF67AA55);
        BIOME_PALETTE.put("dark_forest", 0xFF285920);
        BIOME_PALETTE.put("pale_garden", 0xFF7C8278);
        BIOME_PALETTE.put("cherry_grove", 0xFFF294B8);

        // Taigas & Cold Forests
        BIOME_PALETTE.put("taiga", 0xFF2B6B52);
        BIOME_PALETTE.put("snowy_taiga", 0xFF476E66);
        BIOME_PALETTE.put("old_growth_pine_taiga", 0xFF4E6241);
        BIOME_PALETTE.put("old_growth_spruce_taiga", 0xFF435E3E);
        BIOME_PALETTE.put("grove", 0xFFBFD4CD);

        // Jungles
        BIOME_PALETTE.put("jungle", 0xFF267D1E);
        BIOME_PALETTE.put("sparse_jungle", 0xFF48942E);
        BIOME_PALETTE.put("bamboo_jungle", 0xFF569E24);

        // Swamps
        BIOME_PALETTE.put("swamp", 0xFF4D663E);
        BIOME_PALETTE.put("mangrove_swamp", 0xFF3E6133);

        // Arid & Badlands
        BIOME_PALETTE.put("desert", 0xFFE6CA7E);
        BIOME_PALETTE.put("savanna", 0xFFADA24B);
        BIOME_PALETTE.put("savanna_plateau", 0xFF999248);
        BIOME_PALETTE.put("windswept_savanna", 0xFFB8A95C);
        BIOME_PALETTE.put("badlands", 0xFFD16D36);
        BIOME_PALETTE.put("wooded_badlands", 0xFFA87D4C);
        BIOME_PALETTE.put("eroded_badlands", 0xFFE38349);

        // Mountains & Hills
        BIOME_PALETTE.put("windswept_hills", 0xFF738C6E);
        BIOME_PALETTE.put("windswept_gravelly_hills", 0xFF888C84);
        BIOME_PALETTE.put("windswept_forest", 0xFF547D54);
        BIOME_PALETTE.put("stony_peaks", 0xFF9C9E9A);
        BIOME_PALETTE.put("jagged_peaks", 0xFFDDE6EB);
        BIOME_PALETTE.put("frozen_peaks", 0xFFC8DCE8);
        BIOME_PALETTE.put("snowy_slopes", 0xFFD5E6EE);

        // Special
        BIOME_PALETTE.put("mushroom_fields", 0xFF966E9E);
    }

    private final WorldCreationUiState uiState;
    private final int[] argbPixels = new int[MAP_SIZE * MAP_SIZE];
    private final String[] biomeIds = new String[MAP_SIZE * MAP_SIZE];
    private final AtomicBoolean textureDirty = new AtomicBoolean(false);
    private final AtomicInteger renderGeneration = new AtomicInteger(0);
    private final List<EditBox> syncedSeedBoxes = new ArrayList<>();

    private long fallbackRandomSeed;
    private long activeResolvedSeed;
    private int zoomIndex = 2; // Default 8 blocks per pixel
    private int centerX = 0;
    private int centerZ = 0;
    private volatile BlockPos estimatedSpawn = BlockPos.ZERO;
    private volatile String activePresetLabel = "Default";
    private volatile boolean rendering = false;
    private boolean updatingSeedBoxes = false;

    private String lastSeedInput = null;
    private ChunkGenerator lastGenerator = null;

    public SeedPreviewController(WorldCreationUiState uiState) {
        this.uiState = uiState;
        this.fallbackRandomSeed = WorldOptions.randomSeed();
        this.activeResolvedSeed = resolveSeed(uiState.getSeed());

        // Initialize checkerboard placeholder before async pass completes
        for (int i = 0; i < argbPixels.length; i++) {
            argbPixels[i] = 0xFF1A242D;
            biomeIds[i] = "minecraft:plains";
        }

        this.uiState.addListener(this::onUiStateChanged);
        triggerRender(true);
    }

    public void attachSeedEditBox(EditBox editBox) {
        if (editBox != null && !this.syncedSeedBoxes.contains(editBox)) {
            this.syncedSeedBoxes.add(editBox);
        }
    }

    public long getActiveResolvedSeed() {
        return activeResolvedSeed;
    }

    public int getBlocksPerPixel() {
        return ZOOM_SCALES[zoomIndex];
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public BlockPos getEstimatedSpawn() {
        return estimatedSpawn;
    }

    public String getActivePresetLabel() {
        return activePresetLabel;
    }

    public boolean isRendering() {
        return rendering;
    }

    public boolean consumeTextureDirty() {
        return textureDirty.compareAndSet(true, false);
    }

    public int getPixelArgb(int x, int y) {
        if (x < 0 || x >= MAP_SIZE || y < 0 || y >= MAP_SIZE) {
            return 0xFF000000;
        }
        return argbPixels[y * MAP_SIZE + x];
    }

    public String getBiomeIdAtPixel(int x, int y) {
        if (x < 0 || x >= MAP_SIZE || y < 0 || y >= MAP_SIZE) {
            return "";
        }
        String id = biomeIds[y * MAP_SIZE + x];
        return id != null ? id : "";
    }

    public String getBiomeEnglishNameAtPixel(int x, int y) {
        String id = getBiomeIdAtPixel(x, y);
        if (id.isEmpty()) {
            return "Unknown";
        }
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        String[] parts = path.replace('/', '_').split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append(' ');
                }
                sb.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) {
                    sb.append(p.substring(1).toLowerCase(Locale.ROOT));
                }
            }
        }
        return sb.toString();
    }

    public int pixelToBlockX(int px) {
        int scale = getBlocksPerPixel();
        return centerX + (px - MAP_SIZE / 2) * scale;
    }

    public int pixelToBlockZ(int py) {
        int scale = getBlocksPerPixel();
        return centerZ + (py - MAP_SIZE / 2) * scale;
    }

    /**
     * Generates a new random seed, updates WorldCreationUiState and all attached Seed EditBoxes,
     * and immediately refreshes the biome preview map.
     */
    public void randomizeSeed() {
        long newSeed = WorldOptions.randomSeed();
        this.fallbackRandomSeed = newSeed;
        this.activeResolvedSeed = newSeed;
        String seedStr = Long.toString(newSeed);
        this.lastSeedInput = seedStr;

        this.updatingSeedBoxes = true;
        try {
            this.uiState.setSeed(seedStr);
            for (EditBox box : this.syncedSeedBoxes) {
                if (box != null && !seedStr.equals(box.getValue())) {
                    box.setValue(seedStr);
                }
            }
        } finally {
            this.updatingSeedBoxes = false;
        }

        triggerRender(true);
    }

    /**
     * Ensures that if the user left the seed box blank and clicks "Create New World",
     * the world is created with the exact seed shown in the preview.
     */
    public void syncSeedBeforeWorldCreate() {
        String raw = this.uiState.getSeed();
        if (raw == null || raw.trim().isEmpty()) {
            this.uiState.setSeed(Long.toString(this.activeResolvedSeed));
        }
    }

    public void zoomIn() {
        if (zoomIndex > 0) {
            zoomIndex--;
            triggerRender(false);
        }
    }

    public void zoomOut() {
        if (zoomIndex < ZOOM_SCALES.length - 1) {
            zoomIndex++;
            triggerRender(false);
        }
    }

    public void resetToSpawn() {
        BlockPos sp = this.estimatedSpawn;
        this.centerX = sp != null ? sp.getX() : 0;
        this.centerZ = sp != null ? sp.getZ() : 0;
        triggerRender(false);
    }

    public void panByPixels(double deltaPxX, double deltaPxZ) {
        int scale = getBlocksPerPixel();
        int moveX = (int) Math.round(deltaPxX * scale);
        int moveZ = (int) Math.round(deltaPxZ * scale);
        if (moveX != 0 || moveZ != 0) {
            this.centerX -= moveX;
            this.centerZ -= moveZ;
            triggerRender(false);
        }
    }

    private long resolveSeed(String seedInput) {
        if (seedInput == null || seedInput.trim().isEmpty()) {
            return this.fallbackRandomSeed;
        }
        OptionalLong parsed = WorldOptions.parseSeed(seedInput);
        return parsed.isPresent() ? parsed.getAsLong() : seedInput.hashCode();
    }

    private void onUiStateChanged(WorldCreationUiState state) {
        String currentSeedStr = state.getSeed();
        ChunkGenerator currentGen = extractOverworldGenerator(state.getSettings());

        if (!updatingSeedBoxes) {
            for (EditBox box : this.syncedSeedBoxes) {
                if (box != null && !box.isFocused() && !box.getValue().equals(currentSeedStr)) {
                    updatingSeedBoxes = true;
                    try {
                        box.setValue(currentSeedStr);
                    } finally {
                        updatingSeedBoxes = false;
                    }
                }
            }
        }

        boolean seedChanged = (lastSeedInput == null) || !lastSeedInput.equals(currentSeedStr);
        boolean genChanged = (lastGenerator != currentGen);

        if (seedChanged || genChanged) {
            this.lastSeedInput = currentSeedStr;
            this.lastGenerator = currentGen;
            this.activeResolvedSeed = resolveSeed(currentSeedStr);
            triggerRender(true);
        }
    }

    private static ChunkGenerator extractOverworldGenerator(WorldCreationContext context) {
        if (context == null || context.selectedDimensions() == null) {
            return null;
        }
        try {
            return context.selectedDimensions().overworld();
        } catch (Throwable ignored) {
            Optional<LevelStem> stem = context.selectedDimensions().get(LevelStem.OVERWORLD);
            return stem.map(LevelStem::generator).orElse(null);
        }
    }

    public void triggerRender(boolean recenterOnSpawn) {
        final int genId = this.renderGeneration.incrementAndGet();
        final WorldCreationContext context = this.uiState.getSettings();
        final ChunkGenerator generator = extractOverworldGenerator(context);
        if (generator == null || context == null) {
            return;
        }

        final long seed = this.activeResolvedSeed;
        final int scale = getBlocksPerPixel();
        final String presetName = resolvePresetName(this.uiState, generator);
        this.activePresetLabel = presetName;
        this.rendering = true;

        CompletableFuture.runAsync(() -> {
            try {
                if (this.renderGeneration.get() != genId) {
                    return;
                }

                BiomeSource biomeSource = generator.getBiomeSource();
                Climate.Sampler sampler;

                if (generator instanceof NoiseBasedChunkGenerator noiseGen) {
                    NoiseGeneratorSettings noiseSettings = noiseGen.generatorSettings().value();
                    HolderGetter<NormalNoise.NoiseParameters> noises = context.worldgenLoadContext()
                            .lookupOrThrow(Registries.NOISE);
                    RandomState randomState = RandomState.create(noiseSettings, noises, seed);
                    sampler = randomState.sampler();
                } else {
                    sampler = Climate.empty();
                }

                if (recenterOnSpawn) {
                    BlockPos spawn = BlockPos.ZERO;
                    try {
                        spawn = sampler.findSpawnPosition();
                    } catch (Throwable ignored) {
                    }
                    this.estimatedSpawn = spawn;
                    this.centerX = spawn.getX();
                    this.centerZ = spawn.getZ();
                }

                final int cX = this.centerX;
                final int cZ = this.centerZ;

                // Pass 1: Fast coarse 4x4 preview for instant interactive feedback
                renderPass(genId, biomeSource, sampler, cX, cZ, scale, 4, false);
                if (this.renderGeneration.get() != genId) {
                    return;
                }

                // Pass 2: Medium 2x2 preview
                renderPass(genId, biomeSource, sampler, cX, cZ, scale, 2, false);
                if (this.renderGeneration.get() != genId) {
                    return;
                }

                // Pass 3: Full 1x1 crisp biome + topographic hillshading pass
                renderPass(genId, biomeSource, sampler, cX, cZ, scale, 1, true);
            } catch (Throwable ignored) {
            } finally {
                if (this.renderGeneration.get() == genId) {
                    this.rendering = false;
                }
            }
        }, PREVIEW_EXECUTOR);
    }

    private void renderPass(
            int genId,
            BiomeSource biomeSource,
            Climate.Sampler sampler,
            int cX,
            int cZ,
            int scale,
            int step,
            boolean hillshade
    ) {
        final int quartY = QuartPos.fromBlock(64);
        final float[] heights = hillshade ? new float[MAP_SIZE * MAP_SIZE] : null;
        final boolean[] waterMask = hillshade ? new boolean[MAP_SIZE * MAP_SIZE] : null;
        final int[] localColors = new int[MAP_SIZE * MAP_SIZE];
        final String[] localBiomes = new String[MAP_SIZE * MAP_SIZE];

        DensityFunction contFn = sampler.continentalness();
        DensityFunction weirdFn = sampler.weirdness();

        for (int py = 0; py < MAP_SIZE; py += step) {
            if (this.renderGeneration.get() != genId) {
                return;
            }
            int blockZ = cZ + (py - MAP_SIZE / 2) * scale;
            int quartZ = QuartPos.fromBlock(blockZ);

            for (int px = 0; px < MAP_SIZE; px += step) {
                int blockX = cX + (px - MAP_SIZE / 2) * scale;
                int quartX = QuartPos.fromBlock(blockX);

                Holder<Biome> holder = biomeSource.getNoiseBiome(quartX, quartY, quartZ, sampler);
                String biomeId = holder.unwrapKey()
                        .map(ResourceKey::location)
                        .map(ResourceLocation::toString)
                        .orElse("minecraft:plains");

                int color = resolveBiomeColor(biomeId, holder);
                float hVal = 0.0F;
                boolean isWater = isAquaticBiome(biomeId);

                if (hillshade && contFn != null && weirdFn != null) {
                    try {
                        DensityFunction.SinglePointContext pt = new DensityFunction.SinglePointContext(blockX, 64, blockZ);
                        double c = contFn.compute(pt);
                        double w = weirdFn.compute(pt);
                        hVal = (float) (c * 1.15D + Math.abs(w) * 0.35D);
                    } catch (Throwable ignored) {
                    }
                }

                for (int dy = 0; dy < step && (py + dy) < MAP_SIZE; dy++) {
                    int rowOffset = (py + dy) * MAP_SIZE;
                    for (int dx = 0; dx < step && (px + dx) < MAP_SIZE; dx++) {
                        int idx = rowOffset + px + dx;
                        localColors[idx] = color;
                        localBiomes[idx] = biomeId;
                        if (hillshade) {
                            heights[idx] = hVal;
                            waterMask[idx] = isWater;
                        }
                    }
                }
            }
        }

        if (hillshade) {
            for (int py = 0; py < MAP_SIZE; py++) {
                int row = py * MAP_SIZE;
                for (int px = 0; px < MAP_SIZE; px++) {
                    int idx = row + px;
                    int baseColor = localColors[idx];
                    boolean isWater = waterMask[idx];

                    int prevX = Math.max(0, px - 1);
                    int prevY = Math.max(0, py - 1);
                    float dhx = heights[idx] - heights[row + prevX];
                    float dhy = heights[idx] - heights[prevY * MAP_SIZE + px];

                    // North-West directional hillshading
                    float shade = 1.0F + Mth.clamp((dhx + dhy) * 1.45F, -0.22F, 0.22F);

                    // Crisp shoreline contour where land meets ocean/river
                    if (!isWater) {
                        boolean coast = (px > 0 && waterMask[idx - 1])
                                || (px + 1 < MAP_SIZE && waterMask[idx + 1])
                                || (py > 0 && waterMask[idx - MAP_SIZE])
                                || (py + 1 < MAP_SIZE && waterMask[idx + MAP_SIZE]);
                        if (coast) {
                            shade *= 0.80F;
                        }
                    } else {
                        shade = 1.0F + Mth.clamp(heights[idx] * 0.12F, -0.15F, 0.10F);
                    }

                    localColors[idx] = multiplyColor(baseColor, shade);
                }
            }
        }

        if (this.renderGeneration.get() != genId) {
            return;
        }

        synchronized (this.argbPixels) {
            System.arraycopy(localColors, 0, this.argbPixels, 0, argbPixels.length);
            System.arraycopy(localBiomes, 0, this.biomeIds, 0, biomeIds.length);
        }
        this.textureDirty.set(true);
    }

    private static boolean isAquaticBiome(String biomeId) {
        String path = biomeId.contains(":") ? biomeId.substring(biomeId.indexOf(':') + 1) : biomeId;
        return path.contains("ocean") || path.contains("river") || path.contains("sea") || path.contains("trench");
    }

    public static int resolveBiomeColor(String biomeId, Holder<Biome> holder) {
        String path = biomeId.contains(":")
                ? biomeId.substring(biomeId.indexOf(':') + 1).toLowerCase(Locale.ROOT)
                : biomeId.toLowerCase(Locale.ROOT);

        Integer known = BIOME_PALETTE.get(path);
        if (known != null) {
            return known;
        }

        // Heuristic + BiomeSpecialEffects lookup for modded biomes (Terralith, Biomes O' Plenty, etc.)
        if (holder != null && holder.isBound()) {
            try {
                Biome b = holder.value();
                Optional<Integer> grass = b.getSpecialEffects().getGrassColorOverride();
                if (grass.isPresent()) {
                    return 0xFF000000 | grass.get();
                }
                Optional<Integer> foliage = b.getSpecialEffects().getFoliageColorOverride();
                if (foliage.isPresent()) {
                    return 0xFF000000 | foliage.get();
                }
                if (path.contains("ocean") || path.contains("river") || path.contains("lake") || path.contains("bay")) {
                    return 0xFF000000 | b.getSpecialEffects().getWaterColor();
                }

                float temp = b.getBaseTemperature();
                if (temp < 0.15F || path.contains("snow") || path.contains("ice") || path.contains("frozen") || path.contains("glacier") || path.contains("alpine")) {
                    return varyColorByHash(0xFFDCEBF0, path, 14);
                }
                if (temp >= 1.5F || path.contains("desert") || path.contains("dune") || path.contains("oasis") || path.contains("sahara")) {
                    return varyColorByHash(0xFFE2C478, path, 18);
                }
                if (path.contains("badlands") || path.contains("canyon") || path.contains("mesa") || path.contains("red_rock") || path.contains("volcan")) {
                    return varyColorByHash(0xFFCF6E3A, path, 22);
                }
                if (path.contains("savanna") || path.contains("steppe") || path.contains("shrub") || path.contains("outback")) {
                    return varyColorByHash(0xFFABA04E, path, 18);
                }
                if (path.contains("jungle") || path.contains("rainforest") || path.contains("tropics")) {
                    return varyColorByHash(0xFF2B8222, path, 20);
                }
                if (path.contains("swamp") || path.contains("marsh") || path.contains("bog") || path.contains("bayou") || path.contains("fen")) {
                    return varyColorByHash(0xFF4B663D, path, 18);
                }
                if (path.contains("cherry") || path.contains("Sakura") || path.contains("blossom") || path.contains("lavender")) {
                    return varyColorByHash(0xFFE88FB4, path, 22);
                }
                if (path.contains("peak") || path.contains("mountain") || path.contains("cliff") || path.contains("crag") || path.contains("highland")) {
                    return varyColorByHash(0xFF8C948E, path, 20);
                }
                if (path.contains("taiga") || path.contains("pine") || path.contains("spruce") || path.contains("boreal") || path.contains("conifer")) {
                    return varyColorByHash(0xFF336B52, path, 18);
                }
                if (path.contains("forest") || path.contains("woods") || path.contains("grove")) {
                    return varyColorByHash(0xFF3E8B38, path, 22);
                }
            } catch (Throwable ignored) {
            }
        }

        return varyColorByHash(0xFF6BA84F, path, 28);
    }

    private static int varyColorByHash(int baseArgb, String key, int maxDelta) {
        int h = key.hashCode();
        int dr = ((h & 0xFF) % (maxDelta * 2 + 1)) - maxDelta;
        int dg = (((h >> 8) & 0xFF) % (maxDelta * 2 + 1)) - maxDelta;
        int db = (((h >> 16) & 0xFF) % (maxDelta * 2 + 1)) - maxDelta;
        int r = Mth.clamp(((baseArgb >> 16) & 0xFF) + dr, 0, 255);
        int g = Mth.clamp(((baseArgb >> 8) & 0xFF) + dg, 0, 255);
        int b = Mth.clamp((baseArgb & 0xFF) + db, 0, 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int multiplyColor(int argb, float factor) {
        int r = Mth.clamp((int) (((argb >> 16) & 0xFF) * factor), 0, 255);
        int g = Mth.clamp((int) (((argb >> 8) & 0xFF) * factor), 0, 255);
        int b = Mth.clamp((int) ((argb & 0xFF) * factor), 0, 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static String resolvePresetName(WorldCreationUiState state, ChunkGenerator generator) {
        try {
            WorldCreationUiState.WorldTypeEntry entry = state.getWorldType();
            if (entry != null && entry.describePreset() != null) {
                String desc = entry.describePreset().getString();
                if (desc != null && !desc.isBlank()) {
                    return desc;
                }
            }
        } catch (Throwable ignored) {
        }
        return generator.getClass().getSimpleName().replace("ChunkGenerator", "");
    }
}
