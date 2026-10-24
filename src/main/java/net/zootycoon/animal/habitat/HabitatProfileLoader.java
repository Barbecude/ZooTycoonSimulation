package net.zootycoon.animal.habitat;

import com.google.gson.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.zootycoon.ZooTycoonMod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data-driven loader for habitat profiles and ecosystem templates.
 * Loads JSON files from data pack directory 'data/<namespace>/zoo_habitat/'.
 */
public class HabitatProfileLoader extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final ResourceLocation FABRIC_ID = ZooTycoonMod.id("habitat_profile_loader");

    private static final Map<ResourceLocation, HabitatProfile> TEMPLATES = new ConcurrentHashMap<>();
    private static final Map<String, HabitatProfile> SPECIES_PROFILES = new ConcurrentHashMap<>();
    private static final Map<String, HabitatProfile> DEFAULT_SPECIES = new ConcurrentHashMap<>();

    static {
        initDefaults();
    }

    public HabitatProfileLoader() {
        super(GSON, "zoo_habitat");
    }

    @Override
    public ResourceLocation getFabricId() {
        return FABRIC_ID;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ZooTycoonMod.LOGGER.info("[ZooTycoon] Loading data-driven habitat profiles from data packs (found {} entries)...", entries.size());

        Map<ResourceLocation, HabitatProfile> newTemplates = new HashMap<>();
        Map<String, HabitatProfile> newSpecies = new HashMap<>();

        // 1. First pass: load ecosystem templates
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            ResourceLocation id = entry.getKey();
            String path = id.getPath();

            if (path.startsWith("templates/")) {
                try {
                    HabitatProfile template = parseTemplate(id, entry.getValue().getAsJsonObject());
                    if (template != null) {
                        newTemplates.put(template.id(), template);
                        // Also store shorthand without "templates/"
                        String shortName = path.substring("templates/".length());
                        newTemplates.put(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), shortName), template);
                    }
                } catch (Exception e) {
                    ZooTycoonMod.LOGGER.error("[ZooTycoon] Failed to parse habitat template {}: {}", id, e.getMessage());
                }
            }
        }

        // 2. Second pass: load species profiles (referencing templates)
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            ResourceLocation id = entry.getKey();
            String path = id.getPath();

            if (path.startsWith("species/")) {
                try {
                    parseSpecies(id, entry.getValue().getAsJsonObject(), newTemplates, newSpecies);
                } catch (Exception e) {
                    ZooTycoonMod.LOGGER.error("[ZooTycoon] Failed to parse habitat species {}: {}", id, e.getMessage());
                }
            }
        }

        // Atomically replace loaded mappings
        TEMPLATES.clear();
        TEMPLATES.putAll(newTemplates);

        SPECIES_PROFILES.clear();
        SPECIES_PROFILES.putAll(newSpecies);

        ZooTycoonMod.LOGGER.info("[ZooTycoon] Loaded {} habitat templates and {} species mappings.", TEMPLATES.size(), SPECIES_PROFILES.size());
    }

    private static HabitatProfile parseTemplate(ResourceLocation id, JsonObject obj) {
        String name = obj.has("name") ? obj.get("name").getAsString() : id.getPath();
        String displayName = obj.has("display_name") ? obj.get("display_name").getAsString() : capitalize(name);

        List<String> biomeTags = new ArrayList<>();
        List<ResourceLocation> biomeIds = new ArrayList<>();
        if (obj.has("biomes") && obj.get("biomes").isJsonArray()) {
            for (JsonElement elem : obj.getAsJsonArray("biomes")) {
                String str = elem.getAsString().trim();
                if (str.startsWith("#")) {
                    biomeTags.add(str);
                } else {
                    ResourceLocation biomeLoc = ResourceLocation.tryParse(str);
                    if (biomeLoc != null) {
                        biomeIds.add(biomeLoc);
                    }
                }
            }
        }

        float minTemp = obj.has("min_temperature") ? obj.get("min_temperature").getAsFloat() : -100.0f;
        float maxTemp = obj.has("max_temperature") ? obj.get("max_temperature").getAsFloat() : 100.0f;

        List<HabitatProfile.BlockRequirement> blocks = new ArrayList<>();
        if (obj.has("required_blocks") && obj.get("required_blocks").isJsonArray()) {
            for (JsonElement elem : obj.getAsJsonArray("required_blocks")) {
                if (elem.isJsonObject()) {
                    JsonObject bObj = elem.getAsJsonObject();
                    String type = bObj.has("type") ? bObj.get("type").getAsString() : "custom";
                    String tag = bObj.has("tag") ? bObj.get("tag").getAsString() : null;
                    int minCount = bObj.has("min_count") ? bObj.get("min_count").getAsInt() : 1;
                    String label = bObj.has("label") ? bObj.get("label").getAsString() : capitalize(type);

                    List<ResourceLocation> blockList = new ArrayList<>();
                    if (bObj.has("blocks") && bObj.get("blocks").isJsonArray()) {
                        for (JsonElement bElem : bObj.getAsJsonArray("blocks")) {
                            ResourceLocation bLoc = ResourceLocation.tryParse(bElem.getAsString());
                            if (bLoc != null) blockList.add(bLoc);
                        }
                    }

                    blocks.add(new HabitatProfile.BlockRequirement(type, tag, blockList, minCount, label));
                    if (blocks.size() >= 3) break; // Max 3 requirements
                }
            }
        }

        return new HabitatProfile(id, name, displayName, biomeTags, biomeIds, minTemp, maxTemp, blocks);
    }

    private static void parseSpecies(ResourceLocation id, JsonObject obj, Map<ResourceLocation, HabitatProfile> templates, Map<String, HabitatProfile> speciesMap) {
        String templateStr = obj.has("template") ? obj.get("template").getAsString() : "";
        ResourceLocation templateLoc = ResourceLocation.tryParse(templateStr);
        HabitatProfile baseTemplate = null;
        if (templateLoc != null) {
            baseTemplate = templates.get(templateLoc);
            if (baseTemplate == null) {
                // Try with templates/ prefix
                baseTemplate = templates.get(ResourceLocation.fromNamespaceAndPath(templateLoc.getNamespace(), "templates/" + templateLoc.getPath()));
            }
        }

        if (baseTemplate == null) {
            // Fallback to built-in template
            baseTemplate = TEMPLATES.get(templateLoc);
        }

        if (baseTemplate == null) {
            ZooTycoonMod.LOGGER.warn("[ZooTycoon] Species profile {} references unknown template '{}'", id, templateStr);
            return;
        }

        // Check for extra blocks
        List<HabitatProfile.BlockRequirement> finalBlocks = new ArrayList<>(baseTemplate.requiredBlocks());
        if (obj.has("extra_blocks") && obj.get("extra_blocks").isJsonArray()) {
            for (JsonElement elem : obj.getAsJsonArray("extra_blocks")) {
                if (elem.isJsonObject()) {
                    JsonObject bObj = elem.getAsJsonObject();
                    String type = bObj.has("type") ? bObj.get("type").getAsString() : "custom";
                    String tag = bObj.has("tag") ? bObj.get("tag").getAsString() : null;
                    int minCount = bObj.has("min_count") ? bObj.get("min_count").getAsInt() : 1;
                    String label = bObj.has("label") ? bObj.get("label").getAsString() : capitalize(type);

                    List<ResourceLocation> blockList = new ArrayList<>();
                    if (bObj.has("blocks") && bObj.get("blocks").isJsonArray()) {
                        for (JsonElement bElem : bObj.getAsJsonArray("blocks")) {
                            ResourceLocation bLoc = ResourceLocation.tryParse(bElem.getAsString());
                            if (bLoc != null) blockList.add(bLoc);
                        }
                    }

                    finalBlocks.add(new HabitatProfile.BlockRequirement(type, tag, blockList, minCount, label));
                    if (finalBlocks.size() >= 3) break;
                }
            }
        }

        HabitatProfile profile = new HabitatProfile(
                id,
                baseTemplate.name(),
                baseTemplate.displayName(),
                baseTemplate.biomeTags(),
                baseTemplate.biomeIds(),
                baseTemplate.minTemperature(),
                baseTemplate.maxTemperature(),
                finalBlocks
        );

        List<String> targetEntities = new ArrayList<>();
        if (obj.has("entity")) {
            targetEntities.add(obj.get("entity").getAsString().trim().toLowerCase(Locale.ROOT));
        }
        if (obj.has("entities") && obj.get("entities").isJsonArray()) {
            for (JsonElement elem : obj.getAsJsonArray("entities")) {
                targetEntities.add(elem.getAsString().trim().toLowerCase(Locale.ROOT));
            }
        }
        if (obj.has("entity_types") && obj.get("entity_types").isJsonArray()) {
            for (JsonElement elem : obj.getAsJsonArray("entity_types")) {
                targetEntities.add(elem.getAsString().trim().toLowerCase(Locale.ROOT));
            }
        }

        for (String entityKey : targetEntities) {
            speciesMap.put(entityKey, profile);
        }
    }

    /**
     * Retrieves the habitat profile for the given living entity.
     */
    public static HabitatProfile getProfile(Entity entity) {
        if (entity == null) return null;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return null;

        String fullKey = id.toString().toLowerCase(Locale.ROOT);
        HabitatProfile profile = SPECIES_PROFILES.get(fullKey);
        if (profile != null) return profile;

        String pathKey = id.getPath().toLowerCase(Locale.ROOT);
        profile = SPECIES_PROFILES.get(pathKey);
        if (profile != null) return profile;

        // Fallback to default in-code mapping
        profile = DEFAULT_SPECIES.get(fullKey);
        if (profile != null) return profile;

        profile = DEFAULT_SPECIES.get(pathKey);
        if (profile != null) return profile;

        return getDynamicFallbackProfile(id);
    }

    public static HabitatProfile getTemplate(ResourceLocation templateId) {
        if (templateId == null) return null;
        return TEMPLATES.get(templateId);
    }

    /**
     * Initializes default in-code templates and species profiles so the system
     * works immediately even before any reload listener is dispatched.
     */
    public static void initDefaults() {
        // 1. Savanna
        HabitatProfile savanna = new HabitatProfile(
                ZooTycoonMod.id("savanna"),
                "savanna",
                "Savana",
                List.of("#minecraft:is_savanna"),
                List.of(ResourceLocation.parse("minecraft:savanna"), ResourceLocation.parse("minecraft:savanna_plateau"), ResourceLocation.parse("minecraft:windswept_savanna")),
                0.70f,
                2.5f,
                List.of(new HabitatProfile.BlockRequirement("water", 4, "Air"))
        );
        TEMPLATES.put(ZooTycoonMod.id("savanna"), savanna);
        TEMPLATES.put(ZooTycoonMod.id("templates/savanna"), savanna);

        // 2. Forest
        HabitatProfile forest = new HabitatProfile(
                ZooTycoonMod.id("forest"),
                "forest",
                "Hutan",
                List.of("#minecraft:is_forest"),
                List.of(ResourceLocation.parse("minecraft:forest"), ResourceLocation.parse("minecraft:flower_forest"), ResourceLocation.parse("minecraft:birch_forest"), ResourceLocation.parse("minecraft:dark_forest")),
                0.25f,
                1.8f,
                List.of(new HabitatProfile.BlockRequirement("trees", 6, "Pohon Oak / Birch"))
        );
        TEMPLATES.put(ZooTycoonMod.id("forest"), forest);
        TEMPLATES.put(ZooTycoonMod.id("templates/forest"), forest);

        // 3. Jungle / Hutan Tropis
        HabitatProfile jungle = new HabitatProfile(
                ZooTycoonMod.id("jungle"),
                "jungle",
                "Hutan Tropis",
                List.of("#minecraft:is_jungle"),
                List.of(ResourceLocation.parse("minecraft:jungle"), ResourceLocation.parse("minecraft:sparse_jungle"), ResourceLocation.parse("minecraft:bamboo_jungle")),
                0.70f,
                2.5f,
                List.of(
                        new HabitatProfile.BlockRequirement("trees", 6, "Pohon Oak / Birch"),
                        new HabitatProfile.BlockRequirement("water", 4, "Air")
                )
        );
        TEMPLATES.put(ZooTycoonMod.id("jungle"), jungle);
        TEMPLATES.put(ZooTycoonMod.id("templates/jungle"), jungle);

        // 4. Arctic / Snow
        HabitatProfile snow = new HabitatProfile(
                ZooTycoonMod.id("snow"),
                "snow",
                "Kutub/Salju",
                List.of("#minecraft:spawns_snow_foxes", "#minecraft:spawns_white_rabbits"),
                List.of(ResourceLocation.parse("minecraft:snowy_plains"), ResourceLocation.parse("minecraft:ice_spikes"), ResourceLocation.parse("minecraft:snowy_taiga"), ResourceLocation.parse("minecraft:snowy_slopes"), ResourceLocation.parse("minecraft:frozen_peaks"), ResourceLocation.parse("minecraft:jagged_peaks"), ResourceLocation.parse("minecraft:grove"), ResourceLocation.parse("minecraft:frozen_ocean"), ResourceLocation.parse("minecraft:deep_frozen_ocean")),
                -10.0f,
                0.25f,
                List.of(new HabitatProfile.BlockRequirement("snow", 6, "Blok Salju / Es"))
        );
        TEMPLATES.put(ZooTycoonMod.id("snow"), snow);
        TEMPLATES.put(ZooTycoonMod.id("templates/snow"), snow);

        // 5. Desert / Gurun
        HabitatProfile desert = new HabitatProfile(
                ZooTycoonMod.id("desert"),
                "desert",
                "Gurun",
                List.of("#minecraft:is_badlands"),
                List.of(ResourceLocation.parse("minecraft:desert"), ResourceLocation.parse("minecraft:badlands"), ResourceLocation.parse("minecraft:eroded_badlands"), ResourceLocation.parse("minecraft:wooded_badlands")),
                1.1f,
                2.5f,
                List.of(new HabitatProfile.BlockRequirement("sand", 8, "Blok Pasir"))
        );
        TEMPLATES.put(ZooTycoonMod.id("desert"), desert);
        TEMPLATES.put(ZooTycoonMod.id("templates/desert"), desert);

        // 6. Plains / Padang Rumput
        HabitatProfile plains = new HabitatProfile(
                ZooTycoonMod.id("plains"),
                "plains",
                "Padang Rumput",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:plains"), ResourceLocation.parse("minecraft:sunflower_plains"), ResourceLocation.parse("minecraft:meadow"), ResourceLocation.parse("minecraft:cherry_grove")),
                0.25f,
                1.8f,
                List.of(new HabitatProfile.BlockRequirement("grass", 8, "Blok Rumput"))
        );
        TEMPLATES.put(ZooTycoonMod.id("plains"), plains);
        TEMPLATES.put(ZooTycoonMod.id("templates/plains"), plains);

        // 7. Freshwater / Perairan Tawar
        HabitatProfile freshwater = new HabitatProfile(
                ZooTycoonMod.id("freshwater"),
                "freshwater",
                "Perairan Tawar",
                List.of("#minecraft:is_river"),
                List.of(ResourceLocation.parse("minecraft:river"), ResourceLocation.parse("minecraft:swamp"), ResourceLocation.parse("minecraft:mangrove_swamp")),
                0.2f,
                1.8f,
                List.of(new HabitatProfile.BlockRequirement("water", 12, "Air"))
        );
        TEMPLATES.put(ZooTycoonMod.id("freshwater"), freshwater);
        TEMPLATES.put(ZooTycoonMod.id("templates/freshwater"), freshwater);

        // 8. Ocean / Lautan
        HabitatProfile ocean = new HabitatProfile(
                ZooTycoonMod.id("ocean"),
                "ocean",
                "Lautan",
                List.of("#minecraft:is_ocean", "#minecraft:is_deep_ocean"),
                List.of(ResourceLocation.parse("minecraft:ocean"), ResourceLocation.parse("minecraft:deep_ocean"), ResourceLocation.parse("minecraft:warm_ocean"), ResourceLocation.parse("minecraft:lukewarm_ocean"), ResourceLocation.parse("minecraft:deep_lukewarm_ocean"), ResourceLocation.parse("minecraft:cold_ocean"), ResourceLocation.parse("minecraft:deep_cold_ocean"), ResourceLocation.parse("minecraft:frozen_ocean"), ResourceLocation.parse("minecraft:deep_frozen_ocean")),
                -0.5f,
                2.0f,
                List.of(new HabitatProfile.BlockRequirement("water", 16, "Air"))
        );
        TEMPLATES.put(ZooTycoonMod.id("ocean"), ocean);
        TEMPLATES.put(ZooTycoonMod.id("templates/ocean"), ocean);

        // 9. Swamp / Rawa
        HabitatProfile swamp = new HabitatProfile(
                ZooTycoonMod.id("swamp"),
                "swamp",
                "Rawa",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:swamp"), ResourceLocation.parse("minecraft:mangrove_swamp")),
                0.5f,
                1.0f,
                List.of(new HabitatProfile.BlockRequirement("water", 6, "Air"))
        );
        TEMPLATES.put(ZooTycoonMod.id("swamp"), swamp);
        TEMPLATES.put(ZooTycoonMod.id("templates/swamp"), swamp);

        // Map species to default templates based on Zoo Tycoon Ecosystem Guide (Bagian 1, 2, 3)

        // Zona 1: Savanna Safari (Elephant, Giraffe, Zebra, Ostrich)
        HabitatProfile savannaSafari = new HabitatProfile(
                ZooTycoonMod.id("savanna_safari"),
                "savanna",
                "Savana Safari",
                List.of("#minecraft:is_savanna"),
                List.of(ResourceLocation.parse("minecraft:savanna"), ResourceLocation.parse("minecraft:savanna_plateau"), ResourceLocation.parse("minecraft:windswept_savanna"), ResourceLocation.parse("minecraft:plains")),
                0.70f,
                2.5f,
                List.of(
                        new HabitatProfile.BlockRequirement("water", 2, "Air"),
                        new HabitatProfile.BlockRequirement("acacia", 2, "Pohon Akasia"),
                        new HabitatProfile.BlockRequirement("grass", 4, "Blok Rumput"),
                        new HabitatProfile.BlockRequirement("mud", 1, "Blok Lumpur")
                )
        );
        registerDefaultSpecies(savannaSafari,
                "alexsmobs:elephant", "naturalist:elephant",
                "naturalist:giraffe",
                "naturalist:zebra",
                "naturalist:ostrich",
                "naturalist:rhino", "alexsmobs:gazelle");

        // Zona 2: Apex Predators (Lion, Tiger, Snow Leopard)
        HabitatProfile lionProfile = new HabitatProfile(
                ZooTycoonMod.id("lion"),
                "savanna",
                "Savana (Singa)",
                List.of("#minecraft:is_savanna"),
                List.of(ResourceLocation.parse("minecraft:savanna"), ResourceLocation.parse("minecraft:windswept_savanna")),
                0.70f,
                2.5f,
                List.of(
                        new HabitatProfile.BlockRequirement("water", 2, "Air"),
                        new HabitatProfile.BlockRequirement("stone", 2, "Batu Tebing (Smooth Stone)"),
                        new HabitatProfile.BlockRequirement("sandstone", 2, "Batu Pasir (Sandstone)")
                )
        );
        registerDefaultSpecies(lionProfile, "alexsmobs:lion", "naturalist:lion", "naturalist:hyena", "naturalist:leopard");

        HabitatProfile tigerProfile = new HabitatProfile(
                ZooTycoonMod.id("tiger"),
                "jungle",
                "Hutan Tropis (Harimau)",
                List.of("#minecraft:is_jungle"),
                List.of(ResourceLocation.parse("minecraft:jungle"), ResourceLocation.parse("minecraft:sparse_jungle"), ResourceLocation.parse("minecraft:bamboo_jungle")),
                0.70f,
                2.5f,
                List.of(
                        new HabitatProfile.BlockRequirement("water", 2, "Air"),
                        new HabitatProfile.BlockRequirement("jungle_wood", 4, "Pohon Rimba"),
                        new HabitatProfile.BlockRequirement("bamboo", 2, "Batang Bambu")
                )
        );
        registerDefaultSpecies(tigerProfile, "alexsmobs:tiger", "naturalist:tiger");

        HabitatProfile snowLeopardProfile = new HabitatProfile(
                ZooTycoonMod.id("snow_leopard"),
                "snow",
                "Puncak Salju (Macan Tutul Salju)",
                List.of("#minecraft:spawns_snow_foxes"),
                List.of(ResourceLocation.parse("minecraft:snowy_slopes"), ResourceLocation.parse("minecraft:jagged_peaks"), ResourceLocation.parse("minecraft:frozen_peaks")),
                -10.0f,
                0.25f,
                List.of(
                        new HabitatProfile.BlockRequirement("ice", 2, "Blok Es (Packed/Blue)"),
                        new HabitatProfile.BlockRequirement("snow", 4, "Blok Salju"),
                        new HabitatProfile.BlockRequirement("stone", 2, "Batu Tebing (Smooth Stone)")
                )
        );
        registerDefaultSpecies(snowLeopardProfile, "alexsmobs:snow_leopard");

        // Zona 3: Wetland Semi-Aquatic (Hippo, Alligator, Anaconda)
        HabitatProfile wetlandProfile = new HabitatProfile(
                ZooTycoonMod.id("wetland"),
                "swamp",
                "Lahan Basah / Rawa",
                List.of("#minecraft:is_river"),
                List.of(ResourceLocation.parse("minecraft:swamp"), ResourceLocation.parse("minecraft:mangrove_swamp"), ResourceLocation.parse("minecraft:river"), ResourceLocation.parse("minecraft:jungle")),
                0.40f,
                2.0f,
                List.of(
                        new HabitatProfile.BlockRequirement("water", 4, "Air Kolam"),
                        new HabitatProfile.BlockRequirement("mud", 2, "Blok Lumpur"),
                        new HabitatProfile.BlockRequirement("lily_pad", 1, "Tanaman Lily Pad")
                )
        );
        registerDefaultSpecies(wetlandProfile,
                "naturalist:hippo",
                "naturalist:alligator", "alexsmobs:crocodile", "naturalist:crocodile", "alexsmobs:alligator_snapping_turtle",
                "alexsmobs:anaconda");

        // Zona 4: Boreal Forest (Deer, Grizzly Bear, Raccoon)
        HabitatProfile borealForest = new HabitatProfile(
                ZooTycoonMod.id("boreal_forest"),
                "forest",
                "Hutan Taiga",
                List.of("#minecraft:is_forest", "#minecraft:is_taiga"),
                List.of(ResourceLocation.parse("minecraft:forest"), ResourceLocation.parse("minecraft:birch_forest"), ResourceLocation.parse("minecraft:dark_forest"), ResourceLocation.parse("minecraft:taiga"), ResourceLocation.parse("minecraft:old_growth_pine_taiga")),
                0.20f,
                1.5f,
                List.of(
                        new HabitatProfile.BlockRequirement("spruce", 4, "Pohon Spruce"),
                        new HabitatProfile.BlockRequirement("podzol", 2, "Blok Podzol"),
                        new HabitatProfile.BlockRequirement("water", 2, "Air"),
                        new HabitatProfile.BlockRequirement("sweet_berries", 1, "Semak Sweet Berry")
                )
        );
        registerDefaultSpecies(borealForest,
                "naturalist:deer", "alexsmobs:moose",
                "alexsmobs:grizzly_bear", "naturalist:bear",
                "alexsmobs:raccoon");

        // Zona 5: Jungle Canopy (Gorilla, Capuchin Monkey)
        HabitatProfile jungleCanopy = new HabitatProfile(
                ZooTycoonMod.id("jungle_canopy"),
                "jungle",
                "Kanopi Rimba",
                List.of("#minecraft:is_jungle"),
                List.of(ResourceLocation.parse("minecraft:jungle"), ResourceLocation.parse("minecraft:bamboo_jungle"), ResourceLocation.parse("minecraft:sparse_jungle")),
                0.70f,
                2.5f,
                List.of(
                        new HabitatProfile.BlockRequirement("jungle_wood", 4, "Pohon Rimba"),
                        new HabitatProfile.BlockRequirement("bamboo", 2, "Batang Bambu"),
                        new HabitatProfile.BlockRequirement("vines", 2, "Tanaman Rambat (Vines)"),
                        new HabitatProfile.BlockRequirement("mud", 1, "Blok Lumpur")
                )
        );
        registerDefaultSpecies(jungleCanopy,
                "alexsmobs:gorilla", "alexsmobs:capuchin_monkey", "alexsmobs:gelada_monkey");

        // Zona 6: Walk-in Petting (Capybara, Giant Tortoise)
        HabitatProfile walkInPetting = new HabitatProfile(
                ZooTycoonMod.id("walk_in_petting"),
                "freshwater",
                "Taman Satwa Jinak",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:swamp"), ResourceLocation.parse("minecraft:river"), ResourceLocation.parse("minecraft:plains"), ResourceLocation.parse("minecraft:meadow")),
                0.30f,
                2.0f,
                List.of(
                        new HabitatProfile.BlockRequirement("water", 2, "Air"),
                        new HabitatProfile.BlockRequirement("mud", 1, "Blok Lumpur"),
                        new HabitatProfile.BlockRequirement("lily_pad", 1, "Tanaman Lily Pad"),
                        new HabitatProfile.BlockRequirement("flowers", 2, "Tanaman Bunga")
                )
        );
        registerDefaultSpecies(walkInPetting,
                "naturalist:capybara",
                "naturalist:giant_tortoise", "naturalist:tortoise");

        // Zona 7: Arid Scrubland (Kangaroo, Giant Anteater)
        HabitatProfile aridScrubland = new HabitatProfile(
                ZooTycoonMod.id("arid_scrubland"),
                "savanna",
                "Semak Kering Arid",
                List.of("#minecraft:is_badlands"),
                List.of(ResourceLocation.parse("minecraft:savanna"), ResourceLocation.parse("minecraft:badlands"), ResourceLocation.parse("minecraft:sparse_jungle")),
                0.80f,
                2.5f,
                List.of(
                        new HabitatProfile.BlockRequirement("red_sand", 2, "Blok Pasir Merah"),
                        new HabitatProfile.BlockRequirement("terracotta", 2, "Blok Terracotta"),
                        new HabitatProfile.BlockRequirement("water", 2, "Air")
                )
        );
        registerDefaultSpecies(aridScrubland,
                "alexsmobs:kangaroo", "alexsmobs:giant_anteater");

        // Zona 8: Prehistoric / Dangerous (Cassowary, Komodo Dragon)
        HabitatProfile prehistoricProfile = new HabitatProfile(
                ZooTycoonMod.id("prehistoric_zone"),
                "savanna",
                "Kandang Prasejarah",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:jungle"), ResourceLocation.parse("minecraft:savanna"), ResourceLocation.parse("minecraft:badlands"), ResourceLocation.parse("minecraft:desert")),
                0.80f,
                2.5f,
                List.of(
                        new HabitatProfile.BlockRequirement("sandstone", 2, "Batu Pasir (Sandstone)"),
                        new HabitatProfile.BlockRequirement("bamboo", 2, "Batang Bambu"),
                        new HabitatProfile.BlockRequirement("bone", 1, "Blok Tulang")
                )
        );
        registerDefaultSpecies(prehistoricProfile,
                "wildernature:cassowary", "alexsmobs:emu",
                "naturalist:komodo_dragon", "alexsmobs:komodo_dragon");

        // Zona 9: Desert Oasis (Camel)
        HabitatProfile camelProfile = new HabitatProfile(
                ZooTycoonMod.id("desert_oasis"),
                "desert",
                "Oasis Gurun",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:desert")),
                1.1f,
                2.5f,
                List.of(
                        new HabitatProfile.BlockRequirement("sand", 6, "Blok Pasir"),
                        new HabitatProfile.BlockRequirement("sandstone", 2, "Batu Pasir (Sandstone)"),
                        new HabitatProfile.BlockRequirement("dead_bush", 1, "Semak Kering (Dead Bush)")
                )
        );
        registerDefaultSpecies(camelProfile, "minecraft:camel");

        // Zona 10: Bamboo Garden (Panda)
        HabitatProfile pandaProfile = new HabitatProfile(
                ZooTycoonMod.id("panda_garden"),
                "jungle",
                "Taman Bambu Panda",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:bamboo_jungle"), ResourceLocation.parse("minecraft:sparse_jungle")),
                0.60f,
                2.0f,
                List.of(
                        new HabitatProfile.BlockRequirement("bamboo", 6, "Batang Bambu"),
                        new HabitatProfile.BlockRequirement("jungle_wood", 2, "Pohon Rimba"),
                        new HabitatProfile.BlockRequirement("water", 2, "Air")
                )
        );
        registerDefaultSpecies(pandaProfile, "minecraft:panda");

        // Zona 11: Clean Ice Basin (Penguin)
        HabitatProfile penguinProfile = new HabitatProfile(
                ZooTycoonMod.id("clean_ice_basin"),
                "snow",
                "Kolam Es Pesisir",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:snowy_beach"), ResourceLocation.parse("minecraft:frozen_ocean"), ResourceLocation.parse("minecraft:stony_shore")),
                -10.0f,
                0.35f,
                List.of(
                        new HabitatProfile.BlockRequirement("ice", 4, "Blok Es (Packed/Blue)"),
                        new HabitatProfile.BlockRequirement("snow", 4, "Blok Salju"),
                        new HabitatProfile.BlockRequirement("stone", 2, "Batu Halus (Smooth Stone)"),
                        new HabitatProfile.BlockRequirement("water", 4, "Air Kolam")
                )
        );
        registerDefaultSpecies(penguinProfile, "alexsmobs:penguin");

        // Zona 12: Glacial Dome (Polar Bear, Mammoth, Froststalker)
        HabitatProfile glacialDome = new HabitatProfile(
                ZooTycoonMod.id("glacial_dome"),
                "snow",
                "Kubah Es Abadi",
                List.of("#minecraft:spawns_snow_foxes"),
                List.of(ResourceLocation.parse("minecraft:snowy_plains"), ResourceLocation.parse("minecraft:ice_spikes"), ResourceLocation.parse("minecraft:frozen_peaks"), ResourceLocation.parse("minecraft:grove")),
                -10.0f,
                0.20f,
                List.of(
                        new HabitatProfile.BlockRequirement("ice", 4, "Blok Es (Packed/Blue)"),
                        new HabitatProfile.BlockRequirement("snow", 6, "Blok Salju"),
                        new HabitatProfile.BlockRequirement("water", 2, "Air")
                )
        );
        registerDefaultSpecies(glacialDome,
                "minecraft:polar_bear", "alexsmobs:mammoth", "alexsmobs:froststalker");

        // Zona 13-16: Pelagic & Oceanarium (Orca, Whale, Shark, Squid, Mantis Shrimp)
        HabitatProfile pelagicProfile = new HabitatProfile(
                ZooTycoonMod.id("ocean_pelagic"),
                "ocean",
                "Oseanarium",
                List.of("#minecraft:is_ocean", "#minecraft:is_deep_ocean"),
                List.of(ResourceLocation.parse("minecraft:deep_ocean"), ResourceLocation.parse("minecraft:deep_cold_ocean"), ResourceLocation.parse("minecraft:deep_frozen_ocean"), ResourceLocation.parse("minecraft:deep_lukewarm_ocean"), ResourceLocation.parse("minecraft:ocean"), ResourceLocation.parse("minecraft:warm_ocean")),
                -0.5f,
                2.0f,
                List.of(
                        new HabitatProfile.BlockRequirement("water", 16, "Air Laut"),
                        new HabitatProfile.BlockRequirement("coral", 4, "Blok Prismarine / Karang")
                )
        );
        registerDefaultSpecies(pelagicProfile,
                "alexsmobs:orca", "alexsmobs:cachalot_whale", "naturalist:shark",
                "alexsmobs:giant_squid", "alexsmobs:mantis_shrimp", "minecraft:dolphin");

        // Zona 17: High-Wire Aviary (Bald Eagle, Vulture)
        HabitatProfile aviaryProfile = new HabitatProfile(
                ZooTycoonMod.id("aviary"),
                "forest",
                "Kubah Aviari",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:windswept_forest"), ResourceLocation.parse("minecraft:taiga"), ResourceLocation.parse("minecraft:desert"), ResourceLocation.parse("minecraft:badlands")),
                0.20f,
                2.2f,
                List.of(
                        new HabitatProfile.BlockRequirement("spruce", 4, "Pohon Spruce / Cemara"),
                        new HabitatProfile.BlockRequirement("stone", 2, "Batu Tebing (Smooth Stone)"),
                        new HabitatProfile.BlockRequirement("water", 2, "Air")
                )
        );
        registerDefaultSpecies(aviaryProfile,
                "alexsmobs:bald_eagle", "naturalist:vulture");

        // Zona 18: Petting Farm (Mini Sheep)
        HabitatProfile pettingFarm = new HabitatProfile(
                ZooTycoonMod.id("petting_farm"),
                "plains",
                "Kandang Domba Mini",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:plains"), ResourceLocation.parse("minecraft:meadow")),
                0.25f,
                1.8f,
                List.of(
                        new HabitatProfile.BlockRequirement("grass", 6, "Blok Rumput"),
                        new HabitatProfile.BlockRequirement("hay", 2, "Bale Jerami (Hay Block)"),
                        new HabitatProfile.BlockRequirement("flowers", 2, "Tanaman Bunga")
                )
        );
        registerDefaultSpecies(pettingFarm,
                "wildernature:mini_sheep", "minecraft:sheep", "minecraft:cow", "minecraft:pig", "minecraft:chicken", "minecraft:rabbit");

        // Zona 19: Fantasy Dome (Guster, Bunfungus, Mungus, Rocky Roller, Flutter)
        HabitatProfile fantasyDome = new HabitatProfile(
                ZooTycoonMod.id("fantasy_dome"),
                "fantasy",
                "Kubah Jamur Fantasi",
                Collections.emptyList(),
                List.of(ResourceLocation.parse("minecraft:mushroom_fields"), ResourceLocation.parse("minecraft:lush_caves"), ResourceLocation.parse("minecraft:cherry_grove"), ResourceLocation.parse("minecraft:desert"), ResourceLocation.parse("minecraft:badlands")),
                0.20f,
                2.2f,
                List.of(
                        new HabitatProfile.BlockRequirement("mushrooms", 2, "Blok Jamur / Miselium"),
                        new HabitatProfile.BlockRequirement("podzol", 2, "Blok Podzol / Miselium"),
                        new HabitatProfile.BlockRequirement("terracotta", 2, "Blok Terracotta / Pasir")
                )
        );
        registerDefaultSpecies(fantasyDome,
                "alexsmobs:guster", "alexsmobs:bunfungus", "alexsmobs:mungus",
                "alexsmobs:rocky_roller", "alexsmobs:flutter");
    }

    private static void registerDefaultSpecies(HabitatProfile profile, String... entityIds) {
        for (String id : entityIds) {
            DEFAULT_SPECIES.put(id.toLowerCase(Locale.ROOT), profile);
        }
    }

    public static HabitatProfile getDynamicFallbackProfile(ResourceLocation id) {
        if (id == null) return TEMPLATES.get(ZooTycoonMod.id("plains"));
        String path = id.getPath().toLowerCase(Locale.ROOT);

        // 1. Jungle species
        if (path.contains("tiger") || path.contains("panther") || path.contains("jaguar")
                || path.contains("gorilla") || path.contains("monkey") || path.contains("chimpanzee")
                || path.contains("parrot") || path.contains("toucan") || path.contains("ocelot")
                || path.contains("leopard") || path.contains("hummingbird") || path.contains("canary")) {
            HabitatProfile t = TEMPLATES.get(ZooTycoonMod.id("jungle"));
            if (t != null) return t;
        }

        // 2. Savanna species
        if (path.contains("lion") || path.contains("elephant") || path.contains("zebra")
                || path.contains("rhino") || path.contains("giraffe") || path.contains("hyena")
                || path.contains("gazelle") || path.contains("antelope") || path.contains("kangaroo")
                || path.contains("ostrich") || path.contains("warthog") || path.contains("meerkat")
                || path.contains("llama")) {
            HabitatProfile t = TEMPLATES.get(ZooTycoonMod.id("savanna"));
            if (t != null) return t;
        }

        // 3. Arctic / Snow species
        if (path.contains("polar_bear") || path.contains("penguin") || path.contains("seal")
                || path.contains("walrus") || path.contains("arctic") || path.contains("snow")
                || path.contains("reindeer") || path.contains("yak") || path.contains("goat")) {
            HabitatProfile t = TEMPLATES.get(ZooTycoonMod.id("snow"));
            if (t != null) return t;
        }

        // 4. Desert species
        if (path.contains("camel") || path.contains("rattlesnake") || path.contains("scorpion")
                || path.contains("fennec") || path.contains("roadrunner") || path.contains("armadillo")
                || path.contains("tortoise") || path.contains("komodo") || path.contains("lizard")
                || path.contains("vulture") || path.contains("strider")) {
            HabitatProfile t = TEMPLATES.get(ZooTycoonMod.id("desert"));
            if (t != null) return t;
        }

        // 5. Freshwater & Swamp species
        if (path.contains("hippo") || path.contains("crocodile") || path.contains("alligator")
                || path.contains("duck") || path.contains("platypus") || path.contains("frog")
                || path.contains("toad") || path.contains("turtle") || path.contains("beaver")
                || path.contains("otter") || path.contains("mudskipper") || path.contains("caiman")) {
            HabitatProfile t = TEMPLATES.get(ZooTycoonMod.id("swamp"));
            if (t != null) return t;
        }

        // 6. Ocean species
        if (path.contains("dolphin") || path.contains("orca") || path.contains("whale")
                || path.contains("shark") || path.contains("seagull") || path.contains("squid")) {
            HabitatProfile t = TEMPLATES.get(ZooTycoonMod.id("ocean"));
            if (t != null) return t;
        }

        // 7. Forest species
        if (path.contains("bison") || path.contains("deer") || path.contains("boar")
                || path.contains("wolf") || path.contains("fox") || path.contains("bear")
                || path.contains("moose") || path.contains("raccoon") || path.contains("badger")
                || path.contains("squirrel") || path.contains("skunk") || path.contains("crow")
                || path.contains("eagle") || path.contains("robin") || path.contains("cardinal")
                || path.contains("bluejay") || path.contains("finch")) {
            HabitatProfile t = TEMPLATES.get(ZooTycoonMod.id("forest"));
            if (t != null) return t;
        }

        // 8. Default fallback: Plains (Padang Rumput)
        HabitatProfile fallback = TEMPLATES.get(ZooTycoonMod.id("plains"));
        return fallback != null ? fallback : TEMPLATES.values().stream().findFirst().orElse(null);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

