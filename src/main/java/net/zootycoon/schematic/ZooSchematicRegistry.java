package net.zootycoon.schematic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZooSchematicRegistry {
    private static final Gson GSON = new Gson();
    private static final Map<String, ZooSchematic> SCHEMATICS = new ConcurrentHashMap<>();

    public static void init() {
        SCHEMATICS.clear();
        registerDefaultExamples();
        loadCustomSchematics();
    }

    private static void registerDefaultExamples() {
        // Essentials
        register(new ZooSchematic(
                "zootycoon:builders_hut", "Builder's Hut", "essentials", "construction",
                "Zoo Management", new Vec3i(7, 6, 7),
                "Kantor dan bengkel kerja kontraktor kebun binatang.",
                10, 20, false, ZooTycoonMod.id("builders_hut")
        ));

        register(new ZooSchematic(
                "zootycoon:zoo_office", "Zoo Office", "essentials", "management",
                "Zoo Management", new Vec3i(9, 7, 9),
                "Kantor administrasi zoo, pelayanan informasi, dan keuangan.",
                15, 35, false, ZooTycoonMod.id("zoo_office")
        ));

        register(new ZooSchematic(
                "zootycoon:ice_cream_stall", "Ice Cream Stall", "essentials", "food",
                "Zoo Management", new Vec3i(5, 5, 5),
                "Kios es krim menyegarkan untuk pengunjung kebun binatang.",
                8, 16, false, ZooTycoonMod.id("ice_cream_stall")
        ));

        register(new ZooSchematic(
                "zootycoon:drinks_stall", "Drinks Stall", "essentials", "food",
                "Zoo Management", new Vec3i(5, 5, 5),
                "Kios aneka minuman dingin dan jus buah segar.",
                8, 16, false, ZooTycoonMod.id("drinks_stall")
        ));

        register(new ZooSchematic(
                "zootycoon:burger_stall", "Burger Stall", "essentials", "food",
                "Zoo Management", new Vec3i(6, 5, 6),
                "Kios burger dan makanan cepat saji untuk mengisi tenaga turis.",
                8, 18, false, ZooTycoonMod.id("burger_stall")
        ));

        // Enclosures
        register(new ZooSchematic(
                "zootycoon:savanna_exhibit", "Kandang Sabana Terbuka", "enclosures", "savanna",
                "Zoo Architecture", new Vec3i(16, 6, 16),
                "Kandang sabana luas berpagar ganda dengan kolam minum alami.",
                25, 50, false, ZooTycoonMod.id("savanna_exhibit")
        ));

        register(new ZooSchematic(
                "zootycoon:aviary_dome", "Kubah Aviari Burung", "enclosures", "aviary",
                "Zoo Architecture", new Vec3i(14, 10, 14),
                "Kubah kaca tinggi untuk aneka burung tropis.",
                30, 65, false, ZooTycoonMod.id("aviary_dome")
        ));
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

                ResourceLocation nbtLoc = ZooTycoonMod.id("custom_" + file.getName().replace(".json", ""));
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

    public static void register(ZooSchematic schematic) {
        SCHEMATICS.put(schematic.getId(), schematic);
    }

    public static Optional<ZooSchematic> get(String id) {
        return Optional.ofNullable(SCHEMATICS.get(id));
    }

    public static Collection<ZooSchematic> getAll() {
        return Collections.unmodifiableCollection(SCHEMATICS.values());
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

