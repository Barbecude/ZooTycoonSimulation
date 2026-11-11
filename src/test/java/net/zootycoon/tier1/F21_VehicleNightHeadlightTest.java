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
    @DisplayName("hasHeadlightsOn requires driver and nighttime and synced DATA_HEADLIGHTS")
    void testVehicleHeadlightConditions() throws Exception {
        String vehicleSrc = Files.readString(new File("src/main/java/net/zootycoon/entity/ZooVehicleEntity.java").toPath());
        
        // Ensure hasHeadlightsOn requires driver, night time, and DATA_HEADLIGHTS
        assertTrue(vehicleSrc.contains("return this.getDriver() != null && this.level().isNight() && this.entityData.get(DATA_HEADLIGHTS);"),
                "hasHeadlightsOn must check getDriver() != null, level().isNight(), and DATA_HEADLIGHTS");

        // Ensure server sync sets DATA_HEADLIGHTS based on isNight() when driver is present
        assertTrue(vehicleSrc.contains("boolean isNight = this.level().isNight();"),
                "ZooVehicleEntity.tick must check level().isNight() for headlights");
        assertTrue(vehicleSrc.contains("this.entityData.set(DATA_HEADLIGHTS, isNight);"),
                "DATA_HEADLIGHTS must be set to isNight when driven");
        assertTrue(vehicleSrc.contains("this.entityData.set(DATA_HEADLIGHTS, false);"),
                "DATA_HEADLIGHTS must be set to false when driver is null");
    }

    @Test
    @DisplayName("ZooVehicleHeadlightRenderer aborts rendering when hasHeadlightsOn is false")
    void testHeadlightRendererGuards() throws Exception {
        String rendererSrc = Files.readString(new File("src/main/java/net/zootycoon/client/render/ZooVehicleHeadlightRenderer.java").toPath());
        assertTrue(rendererSrc.contains("if (!entity.hasHeadlightsOn()) {"),
                "Headlight renderer must early-return if hasHeadlightsOn() returns false");
    }
}
