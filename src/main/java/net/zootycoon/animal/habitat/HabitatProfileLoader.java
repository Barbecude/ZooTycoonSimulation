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

        return DEFAULT_SPECIES.get(pathKey);
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
                List.of(new HabitatProfile.BlockRequirement("trees", 6, "Pepohonan"))
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
                        new HabitatProfile.BlockRequirement("trees", 6, "Pepohonan"),
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
                List.of(new HabitatProfile.BlockRequirement("snow", 6, "Salju/Es"))
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
                List.of(new HabitatProfile.BlockRequirement("sand", 8, "Pasir"))
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
                List.of(new HabitatProfile.BlockRequirement("grass", 8, "Rumput"))
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

        // Map species to default templates
        // Savanna
        registerDefaultSpecies(savanna,
                "alexsmobs:lion", "naturalist:lion",
                "alexsmobs:elephant", "naturalist:elephant",
                "naturalist:zebra", "naturalist:rhino", "alexsmobs:gazelle",
                "alexsmobs:kangaroo", "naturalist:hyena", "naturalist:leopard",
                "minecraft:llama", "minecraft:trader_llama");

        // Giraffe needs acacia
        HabitatProfile giraffeProfile = new HabitatProfile(
                ZooTycoonMod.id("giraffe"),
                "savanna",
                "Savana",
                savanna.biomeTags(),
                savanna.biomeIds(),
                savanna.minTemperature(),
                savanna.maxTemperature(),
                List.of(
                        new HabitatProfile.BlockRequirement("water", 4, "Air"),
                        new HabitatProfile.BlockRequirement("trees", 4, "Pohon Akasia")
                )
        );
        registerDefaultSpecies(giraffeProfile, "naturalist:giraffe");

        // Jungle
        registerDefaultSpecies(jungle,
                "alexsmobs:tiger", "naturalist:tiger",
                "alexsmobs:gorilla", "alexsmobs:capuchin_monkey", "alexsmobs:gelada_monkey",
                "minecraft:ocelot", "alexsmobs:hummingbird", "naturalist:canary");

        // Panda needs bamboo
        HabitatProfile pandaProfile = new HabitatProfile(
                ZooTycoonMod.id("panda"),
                "jungle",
                "Hutan Tropis",
                jungle.biomeTags(),
                jungle.biomeIds(),
                jungle.minTemperature(),
                jungle.maxTemperature(),
                List.of(
                        new HabitatProfile.BlockRequirement("trees", 6, "Pepohonan"),
                        new HabitatProfile.BlockRequirement("bamboo", 4, "Bambu")
                )
        );
        registerDefaultSpecies(pandaProfile, "minecraft:panda");

        // Forest
        registerDefaultSpecies(forest,
                "minecraft:wolf", "naturalist:wolf", "minecraft:fox",
                "naturalist:bear", "alexsmobs:grizzly_bear",
                "naturalist:deer", "alexsmobs:moose", "alexsmobs:raccoon",
                "alexsmobs:bald_eagle", "naturalist:boar");

        // Snow / Arctic
        registerDefaultSpecies(snow,
                "minecraft:polar_bear", "minecraft:goat");

        // Desert
        registerDefaultSpecies(desert,
                "minecraft:camel", "alexsmobs:roadrunner", "alexsmobs:rattlesnake",
                "alexsmobs:komodo_dragon", "naturalist:tortoise", "minecraft:strider");

        // Plains
        registerDefaultSpecies(plains,
                "minecraft:cow", "minecraft:sheep", "minecraft:pig", "minecraft:chicken",
                "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:rabbit",
                "minecraft:sniffer", "minecraft:cat", "naturalist:sparrow", "alexsmobs:emu");

        // Freshwater
        registerDefaultSpecies(freshwater,
                "naturalist:duck", "alexsmobs:platypus");

        // Ocean
        registerDefaultSpecies(ocean,
                "minecraft:dolphin", "alexsmobs:orca", "alexsmobs:seagull");

        // Swamp
        registerDefaultSpecies(swamp,
                "alexsmobs:crocodile", "naturalist:crocodile",
                "naturalist:alligator", "alexsmobs:alligator_snapping_turtle",
                "naturalist:hippo");
    }

    private static void registerDefaultSpecies(HabitatProfile profile, String... entityIds) {
        for (String id : entityIds) {
            DEFAULT_SPECIES.put(id.toLowerCase(Locale.ROOT), profile);
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

