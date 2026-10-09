package net.zootycoon.studio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

public class SchematicCaptureService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean captureAndSave(ServerPlayer player, BlockPos pos1, BlockPos pos2,
                                         String schematicName, String category, String description) {
        ServerLevel level = player.serverLevel();

        BlockPos min = new BlockPos(
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ())
        );
        BlockPos max = new BlockPos(
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ())
        );

        Vec3i size = new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);

        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, min, size, true, Blocks.BEDROCK);

        String safeId = "custom_" + schematicName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        Path baseDir = ZooSchematicRegistry.getCustomSchematicsDir();

        try {
            // 1. Save .nbt
            File nbtFile = baseDir.resolve(safeId + ".nbt").toFile();
            CompoundTag nbtData = template.save(new CompoundTag());
            NbtIo.writeCompressed(nbtData, nbtFile.toPath());

            // 2. Save .json metadata
            File jsonFile = baseDir.resolve(safeId + ".json").toFile();
            JsonObject obj = new JsonObject();
            obj.addProperty("id", "zootycoon:" + safeId);
            obj.addProperty("name", schematicName);
            obj.addProperty("category", category);
            obj.addProperty("subcategory", "custom");
            obj.addProperty("author", player.getName().getString());
            obj.addProperty("description", description);
            obj.addProperty("builderHiringCost", Math.max(10, (size.getX() * size.getZ()) / 20));
            obj.addProperty("defaultPrice", Math.max(15, (size.getX() * size.getZ()) / 10));

            JsonObject dim = new JsonObject();
            dim.addProperty("x", size.getX());
            dim.addProperty("y", size.getY());
            dim.addProperty("z", size.getZ());
            obj.add("dimensions", dim);

            try (FileWriter writer = new FileWriter(jsonFile)) {
                GSON.toJson(obj, writer);
            }

            // 3. Register into memory
            ZooSchematic customSchem = new ZooSchematic(
                    "zootycoon:" + safeId, schematicName, category, "custom",
                    player.getName().getString(), size, description,
                    obj.get("builderHiringCost").getAsInt(),
                    obj.get("defaultPrice").getAsInt(),
                    true, ZooTycoonMod.id(safeId)
            );
            ZooSchematicRegistry.register(customSchem);

            ZooTycoonMod.LOGGER.info("Captured and saved custom schematic: {} (Size: {}x{}x{})", schematicName, size.getX(), size.getY(), size.getZ());
            return true;
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to capture schematic", e);
            return false;
        }
    }
}

