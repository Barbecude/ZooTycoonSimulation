package net.zootycoon.tier1;

import net.zootycoon.TestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tier 1 - F21: Vehicle Night-Only & Mounted-Only Headlights")
public class F21_VehicleNightHeadlightTest {

    @BeforeAll
    static void setup() {
        TestHelper.ensureMinecraftBootstrapped();
    }

    @Test
    @DisplayName("hasHeadlightsOn uses persistent synced DATA_HEADLIGHTS toggled by keybind")
    void testVehicleHeadlightConditions() throws Exception {
        String vehicleSrc = Files.readString(new File("src/main/java/net/zootycoon/entity/ZooVehicleEntity.java").toPath());

        // Ensure hasHeadlightsOn reads synced DATA_HEADLIGHTS and supports persistent keybind toggling
        assertTrue(vehicleSrc.contains("return this.entityData.get(DATA_HEADLIGHTS);"),
                "hasHeadlightsOn must return synced DATA_HEADLIGHTS");
        assertTrue(vehicleSrc.contains("toggleHeadlights"),
                "ZooVehicleEntity must provide toggleHeadlights");
        assertTrue(vehicleSrc.contains("tag.putBoolean(\"HeadlightsOn\", hasHeadlightsOn());"),
                "ZooVehicleEntity must persist HeadlightsOn in NBT");
    }

    @Test
    @DisplayName("ZooVehicleHeadlightRenderer aborts rendering when hasHeadlightsOn is false")
    void testHeadlightRendererGuards() throws Exception {
        String rendererSrc = Files.readString(new File("src/main/java/net/zootycoon/client/render/ZooVehicleHeadlightRenderer.java").toPath());
        assertTrue(rendererSrc.contains("if (!entity.hasHeadlightsOn()) {"),
                "Headlight renderer must early-return if hasHeadlightsOn() returns false");
    }
}
