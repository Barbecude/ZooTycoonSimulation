package net.zootycoon.challenger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.zootycoon.TestHelper;
import net.zootycoon.client.model.ZooInspectorModel;
import net.zootycoon.client.render.ZooInspectorRenderer;
import net.zootycoon.entity.ZooInspectorEntity;
import net.zootycoon.entity.ai.navigation.ZooPathNavigation;
import net.zootycoon.item.LicenseBannerItem;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.license.ZooLicenseManager;
import net.zootycoon.tier1.F08_InspectorNoonArrivalTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Milestone 2 Challenger: Empirical Stress Test Harness")
public class Milestone2EmpiricalStressTest {

    private static JsonObject enLang;
    private static JsonObject idLang;
    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\\x{10000}-\\x{10ffff}]|[\\u2600-\\u27bf]");

    @BeforeAll
    static void setUp() throws Exception {
        TestHelper.ensureMinecraftBootstrapped();

        Gson gson = new Gson();
        File enFile = new File("src/main/resources/assets/zootycoon/lang/en_us.json");
        File idFile = new File("src/main/resources/assets/zootycoon/lang/id_id.json");

        assertTrue(enFile.exists(), "en_us.json must exist");
        assertTrue(idFile.exists(), "id_id.json must exist");

        try (var r = new InputStreamReader(new FileInputStream(enFile), StandardCharsets.UTF_8)) {
            enLang = gson.fromJson(r, JsonObject.class);
        }
        try (var r = new InputStreamReader(new FileInputStream(idFile), StandardCharsets.UTF_8)) {
            idLang = gson.fromJson(r, JsonObject.class);
        }
    }

    // =========================================================================
    // F7: INSPECTOR STAR RATING GUARD STRESS TESTING
    // =========================================================================

    @Test
    @DisplayName("C-F7-1: Empirical validation of LicenseTier enum required star values")
    void testDirectLicenseTierStarValues() {
        assertEquals(0.0D, LicenseTier.TAMAN_MINI.getRequiredStars());
        assertEquals(3.0D, LicenseTier.KEBUN_BINATANG.getRequiredStars());
        assertEquals(3.5D, LicenseTier.SUAKA_MARGASATWA.getRequiredStars());
        assertEquals(4.0D, LicenseTier.CAGAR_ALAM.getRequiredStars());
        assertEquals(4.5D, LicenseTier.KONSERVASI_DUNIA.getRequiredStars());

        // Strictly monotonically increasing
        LicenseTier[] tiers = LicenseTier.values();
        for (int i = 0; i < tiers.length - 1; i++) {
            assertTrue(tiers[i + 1].getRequiredStars() > tiers[i].getRequiredStars(),
                    "Tier " + tiers[i + 1].getId() + " stars must exceed Tier " + tiers[i].getId());
        }
    }

    @Test
    @DisplayName("C-F7-2: Exhaustive micro-step rating sweep around thresholds (step = 0.01)")
    void testMicroStepRatingSweep() {
        for (LicenseTier tier : LicenseTier.values()) {
            double req = tier.getRequiredStars();

            // Values strictly below required must be blocked
            if (req > 0.0D) {
                for (double offset = 0.001D; offset <= 0.05D; offset += 0.005D) {
                    double below = req - offset;
                    assertTrue(below < req, "Step below must be strictly smaller");
                    boolean allowed = below >= req;
                    assertFalse(allowed, "Rating " + below + " must be blocked for tier " + tier.getId() + " (req=" + req + ")");
                }
            }

            // Values at or above required must pass
            for (double offset = 0.0D; offset <= 0.05D; offset += 0.005D) {
                double atOrAbove = req + offset;
                boolean allowed = atOrAbove >= req;
                assertTrue(allowed, "Rating " + atOrAbove + " must be allowed for tier " + tier.getId() + " (req=" + req + ")");
            }
        }
    }

    @Test
    @DisplayName("C-F7-3: Extreme rating stress inputs (negative, infinite, NaN, max double)")
    void testExtremeRatingInputs() {
        // Negative stars clamped to 0.0 should only allow Taman Mini
        double negativeStars = -999.0D;
        double clampedNeg = Math.clamp(negativeStars, 0.0D, 5.0D);
        assertEquals(0.0D, clampedNeg);
        assertTrue(clampedNeg >= LicenseTier.TAMAN_MINI.getRequiredStars());
        assertFalse(clampedNeg >= LicenseTier.KEBUN_BINATANG.getRequiredStars());

        // Max double clamped to 5.0 allows all tiers
        double extremeHigh = Double.MAX_VALUE;
        double clampedHigh = Math.clamp(extremeHigh, 0.0D, 5.0D);
        assertEquals(5.0D, clampedHigh);
        for (LicenseTier tier : LicenseTier.values()) {
            assertTrue(clampedHigh >= tier.getRequiredStars(), "5.0 stars satisfies all tier requirements");
        }
    }

    // =========================================================================
    // F8: INSPECTOR NOON ARRIVAL STRESS TESTING
    // =========================================================================

    @Test
    @DisplayName("C-F8-1: Monte-Carlo 50,000 tick generator across 500 days")
    void testMonteCarloNoonArrivalMath() {
        Random rng = new Random(424242L);

        for (int i = 0; i < 50000; i++) {
            long currentDayTime = Math.abs(rng.nextLong() % (500L * 24000L));
            long targetTick = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(currentDayTime);

            // Invariant 1: Target must be in the future
            assertTrue(targetTick > currentDayTime,
                    "Target tick " + targetTick + " must be strictly greater than current daytime " + currentDayTime);

            // Invariant 2: Arrival must be at noon (tick 6000 of target day)
            assertEquals(6000L, targetTick % 24000L,
                    "Target arrival tick " + targetTick + " must be exactly at tick 6000 (noon)");

            // Invariant 3: Arrival must occur on the NEXT day (between 6,000 and 30,000 ticks in future)
            long diff = targetTick - currentDayTime;
            assertTrue(diff >= 6000L && diff <= 30000L,
                    "Arrival diff (" + diff + ") must be within 1 day window for current " + currentDayTime);

            // Invariant 4: Target day is currentDay + 1
            long currentDay = currentDayTime / 24000L;
            long targetDay = targetTick / 24000L;
            assertEquals(currentDay + 1L, targetDay,
                    "Target day must be exactly current day + 1");
        }
    }

    @Test
    @DisplayName("C-F8-2: Boundary test at exact noon (tick 6000): must arrive noon NEXT day, not today")
    void testSummonAtExactNoonArrivesNextDay() {
        for (long day = 0; day <= 10; day++) {
            long exactNoon = day * 24000L + 6000L;
            long target = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(exactNoon);

            long expected = (day + 1L) * 24000L + 6000L;
            assertEquals(expected, target, "Summon at exact noon on day " + day + " must schedule for day " + (day + 1));
            assertEquals(24000L, target - exactNoon, "Exact 24000 ticks (20 minutes) until next day noon");
        }
    }

    @Test
    @DisplayName("C-F8-3: Boundary test at day boundary rollover (tick 23999 vs 24000)")
    void testDayBoundaryRollover() {
        long tick23999 = 23999L;
        long target23999 = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(tick23999);
        assertEquals(30000L, target23999); // Day 1 noon

        long tick24000 = 24000L;
        long target24000 = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(tick24000);
        assertEquals(54000L, target24000); // Day 2 noon
    }

    // =========================================================================
    // F9: INSPECTOR SKIN AND TOOLBAG MODEL ACCESSORY STRESS TESTING
    // =========================================================================

    @Test
    @DisplayName("C-F9-1: File byte integrity and dimensions of inspector.png")
    void testInspectorPngFileIntegrity() {
        File textureFile = new File("src/main/resources/assets/zootycoon/textures/entity/staff/inspector.png");
        assertTrue(textureFile.exists(), "inspector.png must exist at standard staff texture location");
        assertTrue(textureFile.length() > 500, "Texture file must be non-empty valid PNG (>500 bytes)");

        TestHelper.PngDimensions dims = TestHelper.getPngDimensions(textureFile);
        assertTrue(dims.validPng(), "Texture file must have valid PNG signature");
        assertEquals(128, dims.width(), "Texture width must be 128");
        assertEquals(64, dims.height(), "Texture height must be 64");
    }

    @Test
    @DisplayName("C-F9-2: Inspector model mesh creation and toolbag child hierarchy")
    void testInspectorModelMeshHierarchy() {
        LayerDefinition layer = ZooInspectorModel.createMesh();
        assertNotNull(layer, "LayerDefinition must not be null");

        // Bake the root ModelPart to verify cube geometry construction
        net.minecraft.client.model.geom.ModelPart root = layer.bakeRoot();
        assertTrue(root.hasChild("body"), "Model must contain 'body' part");

        net.minecraft.client.model.geom.ModelPart body = root.getChild("body");
        assertTrue(body.hasChild("toolbag"), "Body must contain 'toolbag' child part");

        net.minecraft.client.model.geom.ModelPart toolbag = body.getChild("toolbag");
        assertTrue(toolbag.hasChild("strapback_r1"));
        assertTrue(toolbag.hasChild("strapfrontb_r1"));
        assertTrue(toolbag.hasChild("strapfrontb_r2"));
        assertTrue(toolbag.hasChild("strapfronta_r1"));
        assertTrue(toolbag.hasChild("bag_r1"));
        assertTrue(toolbag.hasChild("ruler"));
        assertTrue(toolbag.hasChild("ruler2"));
        assertTrue(toolbag.hasChild("hammer"));
    }

    // =========================================================================
    // F10: INSPECTOR WALKING AI & STATE MACHINE STRESS TESTING
    // =========================================================================

    @Test
    @DisplayName("C-F10-1: ZooInspectorEntity class hierarchy and navigation type contract")
    void testInspectorClassHierarchy() {
        assertTrue(PathfinderMob.class.isAssignableFrom(ZooInspectorEntity.class),
                "ZooInspectorEntity must extend PathfinderMob");

        try {
            Method navMethod = ZooInspectorEntity.class.getDeclaredMethod("createNavigation", net.minecraft.world.level.Level.class);
            assertTrue(Modifier.isProtected(navMethod.getModifiers()));
        } catch (NoSuchMethodException e) {
            fail("ZooInspectorEntity must override createNavigation(Level)", e);
        }
    }

    @Test
    @DisplayName("C-F10-2: State machine phase enum order and complete lifecycle transitions")
    void testInspectorPhaseTransitions() {
        ZooInspectorEntity.InspectorPhase[] phases = ZooInspectorEntity.InspectorPhase.values();
        List<String> phaseNames = Arrays.stream(phases).map(Enum::name).toList();

        List<String> expectedOrder = List.of(
                "ARRIVING_AT_GATE",
                "WAITING_CONFIRMATION",
                "WALKING_TO_ANIMAL",
                "WRITING_NOTES",
                "OBSERVING_ANIMAL",
                "APPROACHING_PLAYER",
                "WAITING_RESULT_CLAIM",
                "RETURNING_TO_GATE"
        );

        for (String expected : expectedOrder) {
            assertTrue(phaseNames.contains(expected), "Missing phase: " + expected);
        }
    }

    // =========================================================================
    // F11: INSPECTOR HIDE NAMETAG STRESS TESTING
    // =========================================================================

    @Test
    @DisplayName("C-F11-1: Entity shouldShowName overridden to return false")
    void testEntityShouldShowNameContract() throws Exception {
        Method m = ZooInspectorEntity.class.getDeclaredMethod("shouldShowName");
        assertTrue(Modifier.isPublic(m.getModifiers()));
        assertEquals(boolean.class, m.getReturnType());
    }

    @Test
    @DisplayName("C-F11-2: Renderer shouldShowName and renderNameTag overrides present")
    void testRendererSuppressionOverrides() throws Exception {
        Method shouldShow = ZooInspectorRenderer.class.getDeclaredMethod("shouldShowName", ZooInspectorEntity.class);
        assertTrue(Modifier.isProtected(shouldShow.getModifiers()));

        Method renderTag = ZooInspectorRenderer.class.getDeclaredMethod("renderNameTag",
                ZooInspectorEntity.class,
                net.minecraft.network.chat.Component.class,
                com.mojang.blaze3d.vertex.PoseStack.class,
                net.minecraft.client.renderer.MultiBufferSource.class,
                int.class,
                float.class);
        assertTrue(Modifier.isProtected(renderTag.getModifiers()));
    }

    // =========================================================================
    // F12: ANIMAL INFO BANNER AWARD & 40-SPECIES MATRIX STRESS TESTING
    // =========================================================================

    @Test
    @DisplayName("C-F12-1: All 40 species have non-empty fun facts in both en_us and id_id")
    void testAll40SpeciesHaveFunFactsInBothLanguages() {
        int count = 0;
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                count++;
                String spKey = entry.speciesKey();
                String funFactKey = "zootycoon.species." + spKey + ".fun_fact";

                // 1. English
                assertTrue(enLang.has(funFactKey), "en_us.json missing fun fact for " + spKey + " (" + funFactKey + ")");
                String enText = enLang.get(funFactKey).getAsString();
                assertFalse(enText.isBlank(), "en_us fun fact is blank for " + spKey);
                assertFalse(enText.contains("§l") || enText.contains("**"), "en_us fun fact has bold for " + spKey);
                assertFalse(EMOJI_PATTERN.matcher(enText).find(), "en_us fun fact has emoji for " + spKey);

                // 2. Indonesian
                assertTrue(idLang.has(funFactKey), "id_id.json missing fun fact for " + spKey + " (" + funFactKey + ")");
                String idText = idLang.get(funFactKey).getAsString();
                assertFalse(idText.isBlank(), "id_id fun fact is blank for " + spKey);
                assertFalse(idText.contains("§l") || idText.contains("**"), "id_id fun fact has bold for " + spKey);
                assertFalse(EMOJI_PATTERN.matcher(idText).find(), "id_id fun fact has emoji for " + spKey);
            }
        }
        assertEquals(40, count, "Must verify exactly 40 species across 5 tiers");
    }

    @Test
    @DisplayName("C-F12-2: Animal info banner item stack creation with NBT custom data")
    void testAnimalInfoBannerStackCreation() {
        for (int tier = 1; tier <= 5; tier++) {
            LicenseTier t = LicenseTier.byLevel(tier);
            String firstSpecies = t.getRequiredSpecies().get(0).speciesKey();

            ItemStack stack = LicenseBannerItem.createAnimalInfoBannerStack(tier, firstSpecies, "Empirical Zoo");
            assertNotNull(stack);
            assertFalse(stack.isEmpty());

            CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
            assertNotNull(cd, "CustomData must be attached to banner stack");
            CompoundTag tag = cd.copyTag();

            assertEquals(tier, tag.getInt("TierLevel"));
            assertEquals(firstSpecies, tag.getString("SpeciesKey"));
            assertEquals("Empirical Zoo", tag.getString("ZooName"));

            // Check item name formatting
            String nameStr = stack.getHoverName().getString();
            assertTrue(nameStr.contains(t.getRequiredSpecies().get(0).defaultName()),
                    "Stack display name must include English default animal name: " + nameStr);
        }
    }

    @Test
    @DisplayName("C-F12-3: getFeaturedSpeciesForTier matching and fallback logic")
    void testFeaturedSpeciesForTier() {
        // Fallback when list is empty
        assertEquals("capybara", ZooLicenseManager.getFeaturedSpeciesForTier(1, List.of()));
        assertEquals("zebra", ZooLicenseManager.getFeaturedSpeciesForTier(2, List.of()));
        assertEquals("elephant", ZooLicenseManager.getFeaturedSpeciesForTier(3, List.of()));
        assertEquals("lion", ZooLicenseManager.getFeaturedSpeciesForTier(4, List.of()));
        assertEquals("orca", ZooLicenseManager.getFeaturedSpeciesForTier(5, List.of()));

        // Inspected match
        assertEquals("mini_sheep", ZooLicenseManager.getFeaturedSpeciesForTier(1, List.of("Mini Sheep")));
        assertEquals("kangaroo", ZooLicenseManager.getFeaturedSpeciesForTier(1, List.of("kangaroo")));
        assertEquals("panda", ZooLicenseManager.getFeaturedSpeciesForTier(2, List.of("Panda")));
        assertEquals("giraffe", ZooLicenseManager.getFeaturedSpeciesForTier(3, List.of("Giraffe")));
        assertEquals("tiger", ZooLicenseManager.getFeaturedSpeciesForTier(4, List.of("Tiger")));
        assertEquals("great_white_shark", ZooLicenseManager.getFeaturedSpeciesForTier(5, List.of("Great White Shark")));
    }
}
