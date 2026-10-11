package net.zootycoon.schematic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZooSchematicRegistry {
    private static final Gson GSON = new Gson();
    private static final Map<String, ZooSchematic> SCHEMATICS = new ConcurrentHashMap<>();

    public static void init() {
        SCHEMATICS.clear();
        loadCustomSchematics();
    }

    public static Path getCustomSchematicsDir() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("zootycoon").resolve("schematics");
        try {
            Files.createDirectories(dir);
        } catch (Exception ignored) {}
        return dir;
    }

    public static void loadCustomSchematics() {
        Path dir = getCustomSchematicsDir();
        File folder = dir.toFile();
        File[] files = folder.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject obj = GSON.fromJson(reader, JsonObject.class);
                String id = obj.get("id").getAsString();
                String name = obj.get("name").getAsString();
                String category = obj.get("category").getAsString();
                String subcategory = obj.has("subcategory") ? obj.get("subcategory").getAsString() : "custom";
                String author = obj.has("author") ? obj.get("author").getAsString() : "Player";
                JsonObject dim = obj.getAsJsonObject("dimensions");
                Vec3i dimensions = new Vec3i(dim.get("x").getAsInt(), dim.get("y").getAsInt(), dim.get("z").getAsInt());
                String desc = obj.has("description") ? obj.get("description").getAsString() : "";
                int hiringCost = obj.has("builderHiringCost") ? obj.get("builderHiringCost").getAsInt() : 10;
                int price = obj.has("defaultPrice") ? obj.get("defaultPrice").getAsInt() : 20;

                String baseName = file.getName().replace(".json", "");
                ResourceLocation nbtLoc = ZooTycoonMod.id(baseName);
                ZooSchematic schem = new ZooSchematic(
                        id, name, category, subcategory, author, dimensions, desc, hiringCost, price, true, nbtLoc
                );
                register(schem);
                ZooTycoonMod.LOGGER.info("Loaded custom schematic: {}", name);
            } catch (Exception e) {
                ZooTycoonMod.LOGGER.error("Failed to load custom schematic from file: " + file.getName(), e);
            }
        }
    }

    public static CompoundTag loadSchematicTag(MinecraftServer server, ZooSchematic schem) {
        if (schem == null || server == null) return null;
        if (schem.isCustom()) {
            Path nbtFile = getCustomSchematicsDir().resolve(schem.getStructureLocation().getPath() + ".nbt");
            if (Files.exists(nbtFile)) {
                try {
                    return NbtIo.readCompressed(nbtFile, NbtAccounter.unlimitedHeap());
                } catch (Exception e) {
                    ZooTycoonMod.LOGGER.error("Failed to load custom schematic NBT tag: " + nbtFile, e);
                }
            }
        } else {
            try {
                ResourceLocation id = schem.getStructureLocation();
                ResourceLocation path = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "structure/" + id.getPath() + ".nbt");
                var resOpt = server.getResourceManager().getResource(path);
                if (resOpt.isPresent()) {
                    try (var is = resOpt.get().open()) {
                        return NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public static StructureTemplate loadTemplate(MinecraftServer server, ZooSchematic schem) {
        if (schem.isCustom()) {
            CompoundTag tag = loadSchematicTag(server, schem);
            if (tag != null) {
                try {
                    StructureTemplate template = new StructureTemplate();
                    template.load(server.registryAccess().lookupOrThrow(Registries.BLOCK), tag);
                    return template;
                } catch (Exception e) {
                    ZooTycoonMod.LOGGER.error("Failed to parse custom schematic template: " + schem.getName(), e);
                }
            }
        }
        return server.getStructureManager().getOrCreate(schem.getStructureLocation());
    }

    public static void register(ZooSchematic schematic) {
        SCHEMATICS.put(schematic.getId(), schematic);
    }

    public static Optional<ZooSchematic> get(String id) {
        return Optional.ofNullable(SCHEMATICS.get(id));
    }

    public static Collection<ZooSchematic> getAll() {
        return Collections.unmodifiableCollection(SCHEMATICS.values());
    }

    public static boolean deleteCustomSchematic(String id) {
        ZooSchematic schem = SCHEMATICS.remove(id);
        if (schem != null) {
            String baseName = schem.getStructureLocation().getPath();
            Path jsonFile = getCustomSchematicsDir().resolve(baseName + ".json");
            Path nbtFile = getCustomSchematicsDir().resolve(baseName + ".nbt");
            try {
                Files.deleteIfExists(jsonFile);
                Files.deleteIfExists(nbtFile);
                return true;
            } catch (Exception e) {
                ZooTycoonMod.LOGGER.error("Failed to delete schematic files for " + id, e);
            }
        }
        return false;
    }

    public static List<ZooSchematic> getByCategory(String category) {
        List<ZooSchematic> list = new ArrayList<>();
        for (ZooSchematic s : SCHEMATICS.values()) {
            if (s.getCategory().equalsIgnoreCase(category)) {
                list.add(s);
            }
        }
        return list;
    }
}
