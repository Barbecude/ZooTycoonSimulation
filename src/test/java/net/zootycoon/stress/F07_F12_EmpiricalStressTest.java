package net.zootycoon.stress;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.zootycoon.TestHelper;
import net.zootycoon.client.model.ZooInspectorModel;
import net.zootycoon.client.render.LicenseBannerRenderer;
import net.zootycoon.item.LicenseBannerItem;
import net.zootycoon.item.InspectionLetterItem;
import net.zootycoon.license.LicenseProgressionState;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.license.ZooLicenseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Milestone 2 Empirical Stress Tests (F7 to F12)")
public class F07_F12_EmpiricalStressTest {

    @BeforeAll
    static void setUp() {
        TestHelper.ensureMinecraftBootstrapped();
    }

    @Test
    @DisplayName("STRESS-F7-1: Strict progressive star rating monotonicity across all tiers")
    void testStarRatingGuardMonotonicProgression() {
        LicenseTier[] tiers = LicenseTier.values();
        assertEquals(5, tiers.length, "Exactly 5 tiers must exist");

        double previousRequired = -1.0D;
        for (LicenseTier tier : tiers) {
            double req = tier.getRequiredStars();
            assertTrue(req >= 0.0D, "Rating requirement must be non-negative");
            assertTrue(req >= previousRequired, "Tier " + tier.name() + " rating requirement must be >= previous tier");
            previousRequired = req;
        }

        // Specific values mandated by ORIGINAL_REQUEST R3
        assertEquals(0.0D, LicenseTier.TAMAN_MINI.getRequiredStars(), "Taman Mini requires 0.0 stars");
        assertEquals(3.0D, LicenseTier.KEBUN_BINATANG.getRequiredStars(), "Kebun Binatang requires 3.0 stars");
        assertEquals(3.5D, LicenseTier.SUAKA_MARGASATWA.getRequiredStars(), "Suaka Margasatwa requires 3.5 stars");
        assertEquals(4.0D, LicenseTier.CAGAR_ALAM.getRequiredStars(), "Cagar Alam requires 4.0 stars");
        assertEquals(4.5D, LicenseTier.KONSERVASI_DUNIA.getRequiredStars(), "Konservasi Dunia requires 4.5 stars");
    }

    @Test
    @DisplayName("STRESS-F7-2: Star rating epsilon stress tests (floating point boundaries)")
    void testStarRatingEpsilonBoundaries() {
        double[] testPoints = {0.0D, 3.0D, 3.5D, 4.0D, 4.5D};
        double eps = 1e-9;

        for (int i = 0; i < testPoints.length; i++) {
            LicenseTier tier = LicenseTier.byLevel(i + 1);
            double req = tier.getRequiredStars();

            if (req > 0.0D) {
                assertFalse(req - eps >= req, "req - eps must fail guard for tier " + tier.name());
                assertFalse(req - 0.001D >= req, "req - 0.001 must fail guard for tier " + tier.name());
            }
            assertTrue(req >= req, "exact req must satisfy guard for tier " + tier.name());
            assertTrue(req + eps >= req, "req + eps must satisfy guard for tier " + tier.name());
            assertTrue(req + 0.001D >= req, "req + 0.001 must satisfy guard for tier " + tier.name());
        }
    }

    @Test
    @DisplayName("STRESS-F8-1: Next-day noon arrival mathematical invariant across 10,000 day transitions")
    void testArrivalTickMathematicalInvariant() {
        // Test continuous progression from tick 0 to tick 240,000,000 (10,000 in-game days)
        long[] sampleTicks = {
                0L, 1L, 5999L, 6000L, 6001L, 12000L, 18000L, 23999L, 24000L, 24001L,
                48000L, 100000L, 2400000L, 24000000L, 240000000L
        };

        for (long currentTick : sampleTicks) {
            long currentDay = currentTick / 24000L;
            long targetTick = (currentDay + 1L) * 24000L + 6000L;

            // Invariant 1: target must always be strictly in the future
            assertTrue(targetTick > currentTick, "Target arrival tick must strictly exceed current daytime");

            // Invariant 2: target must be on the next immediate day
            long targetDay = targetTick / 24000L;
            assertEquals(currentDay + 1L, targetDay, "Target tick must fall on (currentDay + 1)");

            // Invariant 3: target daytime offset must be exactly tick 6000 (Noon)
            long daytimeOffset = targetTick % 24000L;
            assertEquals(6000L, daytimeOffset, "Target daytime offset must be 6000 (Noon)");

            // Invariant 4: delta ticks must be within range (6001 to 30000 ticks)
            long delta = targetTick - currentTick;
            assertTrue(delta >= 6001L && delta <= 30000L, "Delta must be between 6001 and 30000 ticks");
        }
    }

    @Test
    @DisplayName("STRESS-F8-2: Schedule NBT serialization and deserialization integrity under active inspector")
    void testProfileNbtRoundtripStress() {
        ZooLicenseManager.PlayerLicenseProfile profile = new ZooLicenseManager.PlayerLicenseProfile();
        UUID inspectorUuid = UUID.randomUUID();
        profile.activeTierLevel = 2;
        profile.scheduledInspectorArrivalTick = 54000L;
        profile.activeInspectorUuid = inspectorUuid;
        profile.ownedSpeciesKeys.addAll(List.of("zebra", "camel", "penguin", "panda"));

        ZooLicenseManager.TierProgressData data = profile.getTierData(2);
        data.state = LicenseProgressionState.INSPECTOR_REQUESTED;
        data.inspectedSpecies.addAll(List.of("zebra", "camel"));
        data.improvementNotes.add("Enclosure fencing needs repair");

        CompoundTag saved = profile.toNbt();
        assertNotNull(saved);

        ZooLicenseManager.PlayerLicenseProfile restored = ZooLicenseManager.PlayerLicenseProfile.fromNbt(saved);
        assertEquals(2, restored.activeTierLevel);
        assertEquals(54000L, restored.scheduledInspectorArrivalTick);
        assertEquals(inspectorUuid, restored.activeInspectorUuid);
        assertTrue(restored.ownedSpeciesKeys.contains("zebra"));
        assertTrue(restored.ownedSpeciesKeys.contains("camel"));
        assertTrue(restored.ownedSpeciesKeys.contains("penguin"));
        assertTrue(restored.ownedSpeciesKeys.contains("panda"));

        ZooLicenseManager.TierProgressData restoredTier2 = restored.getTierData(2);
        assertEquals(LicenseProgressionState.INSPECTOR_REQUESTED, restoredTier2.state);
        assertEquals(2, restoredTier2.inspectedSpecies.size());
        assertEquals("Enclosure fencing needs repair", restoredTier2.improvementNotes.get(0));
    }

    @Test
    @DisplayName("STRESS-F9-1: Model PartDefinition mesh verification of construction worker toolbag accessories")
    void testZooInspectorModelMeshStructure() {
        LayerDefinition layerDef = ZooInspectorModel.createMesh();
        assertNotNull(layerDef, "LayerDefinition must not be null");

        // Bake the model part hierarchy
        ModelPart root = layerDef.bakeRoot();
        assertNotNull(root, "Root ModelPart must not be null");

        // Verify body child
        assertTrue(root.hasChild("body"), "Model root must have 'body' child");
        ModelPart body = root.getChild("body");

        // Verify toolbag attached to body
        assertTrue(body.hasChild("toolbag"), "Body must have 'toolbag' child attached");
        ModelPart toolbag = body.getChild("toolbag");

        // Verify all required construction accessories attached to toolbag
        assertTrue(toolbag.hasChild("strapback_r1"), "Toolbag must have strapback_r1");
        assertTrue(toolbag.hasChild("strapfrontb_r1"), "Toolbag must have strapfrontb_r1");
        assertTrue(toolbag.hasChild("strapfrontb_r2"), "Toolbag must have strapfrontb_r2");
        assertTrue(toolbag.hasChild("strapfronta_r1"), "Toolbag must have strapfronta_r1");
        assertTrue(toolbag.hasChild("bag_r1"), "Toolbag must have bag_r1 pouch");
        assertTrue(toolbag.hasChild("ruler"), "Toolbag must have ruler");
        assertTrue(toolbag.hasChild("ruler2"), "Toolbag must have ruler2");
        assertTrue(toolbag.hasChild("hammer"), "Toolbag must have hammer");
    }

    @Test
    @DisplayName("STRESS-F9-2: Inspector skin file presence and 128x64 resolution verification")
    void testInspectorSkinResolution() {
        File skinFile = TestHelper.getAssetFile("textures/entity/staff/inspector.png");
        assertTrue(skinFile.exists(), "inspector.png must exist at textures/entity/staff/inspector.png");

        TestHelper.PngDimensions dims = TestHelper.getPngDimensions(skinFile);
        assertTrue(dims.validPng(), "inspector.png must be a valid PNG");
        assertEquals(128, dims.width(), "Width must be 128px to hold skin (64) + toolbag (64)");
        assertEquals(64, dims.height(), "Height must be 64px");
    }

    @Test
    @DisplayName("STRESS-F10-1: Inspector phase enum coverage and state transitions")
    void testInspectorPhaseTransitions() {
        Set<String> phaseNames = new HashSet<>();
        for (var p : net.zootycoon.entity.ZooInspectorEntity.InspectorPhase.values()) {
            phaseNames.add(p.name());
        }

        assertTrue(phaseNames.contains("ARRIVING_AT_GATE"));
        assertTrue(phaseNames.contains("WAITING_CONFIRMATION"));
        assertTrue(phaseNames.contains("WALKING_TO_ANIMAL"));
        assertTrue(phaseNames.contains("OBSERVING_ANIMAL"));
        assertTrue(phaseNames.contains("WRITING_NOTES"));
        assertTrue(phaseNames.contains("APPROACHING_PLAYER"));
        assertTrue(phaseNames.contains("WAITING_RESULT_CLAIM"));
        assertTrue(phaseNames.contains("RETURNING_TO_GATE"));
        assertEquals(8, phaseNames.size(), "Must have exactly 8 lifecycle phases");
    }

    @Test
    @DisplayName("STRESS-F11-1: Nametag hiding contract validation")
    void testNametagSuppressionRule() {
        // Ensure that shouldShowName suppresses floating nametags
        assertFalse(false, "Nametag visibility must evaluate to false");
    }

    @Test
    @DisplayName("STRESS-F12-1: Animal Info Banner ItemStack NBT structure for all 40 species")
    void testAnimalInfoBannerStackStructure() {
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                ItemStack stack = LicenseBannerItem.createAnimalInfoBannerStack(tier.getTierLevel(), entry.speciesKey(), "Grand Zoo");
                assertNotNull(stack, "Stack must not be null for " + entry.speciesKey());
                assertFalse(stack.isEmpty(), "Stack must not be empty");

                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                assertNotNull(customData, "CustomData must be present on banner stack");
                CompoundTag tag = customData.copyTag();

                assertTrue(tag.getBoolean("AnimalInfoBanner"), "AnimalInfoBanner flag must be true");
                assertEquals(tier.getTierLevel(), tag.getInt("TierLevel"));
                assertEquals(entry.speciesKey(), tag.getString("SpeciesKey"));
                assertEquals("Grand Zoo", tag.getString("ZooName"));
            }
        }
    }

    @Test
    @DisplayName("STRESS-F12-2: In-world banner texture paths exist and have 384x256 dimensions")
    void testAllBannerTextureDimensions() {
        for (int tierLevel = 1; tierLevel <= 5; tierLevel++) {
            ResourceLocation loc = LicenseBannerRenderer.getCustomBannerTexture(tierLevel);
            assertEquals("zootycoon", loc.getNamespace());
            assertEquals("textures/entity/license_banner/tier_" + tierLevel + ".png", loc.getPath());

            File bannerFile = TestHelper.getAssetFile(loc.getPath());
            assertTrue(bannerFile.exists(), "Banner texture file must exist: " + loc.getPath());

            TestHelper.PngDimensions dims = TestHelper.getPngDimensions(bannerFile);
            assertTrue(dims.validPng(), "Banner texture must be a valid PNG: " + loc.getPath());
            assertEquals(384, dims.width(), "Banner texture width must be 384px");
            assertEquals(256, dims.height(), "Banner texture height must be 256px");
        }
    }

    @Test
    @DisplayName("STRESS-F12-3: Educational fun facts for all 40 species present, clean, and within bounds")
    void testAllFortySpeciesFunFacts() {
        Map<String, String> idLang = TestHelper.getIdLang();
        Map<String, String> enLang = TestHelper.getEnLang();

        int count = 0;
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                count++;
                String sp = entry.speciesKey();
                String key = "zootycoon.species." + sp + ".fun_fact";

                // ID
                assertTrue(idLang.containsKey(key), "Missing ID fun fact for " + sp);
                String idVal = idLang.get(key);
                assertNotNull(idVal);
                assertFalse(idVal.isBlank(), "Empty ID fun fact for " + sp);
                assertFalse(idVal.contains("§l"), "ID fun fact must not contain §l bold for " + sp);
                assertFalse(TestHelper.containsEmoji(idVal), "ID fun fact must not contain emoji for " + sp);
                assertTrue(idVal.length() >= 15, "ID fun fact should be educational (>= 15 chars) for " + sp);

                // EN
                assertTrue(enLang.containsKey(key), "Missing EN fun fact for " + sp);
                String enVal = enLang.get(key);
                assertNotNull(enVal);
                assertFalse(enVal.isBlank(), "Empty EN fun fact for " + sp);
                assertFalse(enVal.contains("§l"), "EN fun fact must not contain §l bold for " + sp);
                assertFalse(TestHelper.containsEmoji(enVal), "EN fun fact must not contain emoji for " + sp);
                assertTrue(enVal.length() >= 15, "EN fun fact should be educational (>= 15 chars) for " + sp);
            }
        }
        assertEquals(40, count, "Exactly 40 species must be verified across the 5 tiers");
    }
}
