package net.zootycoon.tier1;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.zootycoon.TestHelper;
import net.zootycoon.animal.habitat.HabitatEvaluator;
import net.zootycoon.client.render.LicenseBannerRenderer;
import net.zootycoon.item.LicenseBannerItem;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.network.ZooNetworkPayloads;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F12: Animal Info Board Model, Creative Items & Wall Orientation")
public class F12_AnimalInfoBoardAndBannerOrientationTest {

    @Test
    @DisplayName("Wall plaque yaw aligns +Z front face outward for all four cardinal directions")
    void testBannerYawFacesOutwardForAllDirections() {
        assertEquals(0.0F, LicenseBannerRenderer.getYawForDirection(Direction.SOUTH), 0.001F);
        assertEquals(180.0F, LicenseBannerRenderer.getYawForDirection(Direction.NORTH), 0.001F);
        assertEquals(-90.0F, LicenseBannerRenderer.getYawForDirection(Direction.WEST), 0.001F);
        assertEquals(90.0F, LicenseBannerRenderer.getYawForDirection(Direction.EAST), 0.001F);
    }

    @Test
    @DisplayName("All 40 species info board stacks carry CustomModelData and fun fact translation keys")
    void testAllSpeciesInfoBoardStacksForCreativeTab() {
        TestHelper.ensureMinecraftBootstrapped();
        int total = 0;
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                total++;
                ItemStack stack = LicenseBannerItem.createAnimalInfoBannerStack(tier.getTierLevel(), entry.speciesKey(), "");
                assertFalse(stack.isEmpty());
                CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
                assertNotNull(cmd, "Info board stack must set CustomModelData for standing board icon");
                assertEquals(LicenseBannerItem.ANIMAL_INFO_BOARD_CUSTOM_MODEL_DATA, cmd.value());
            }
        }
        assertEquals(40, total);
    }

    @Test
    @DisplayName("Standing animal info board assets and tier model overrides exist")
    void testAnimalInfoBoardAssetsAndModelOverridesExist() throws Exception {
        File entityTex = TestHelper.getAssetFile("textures/entity/animal_info_board.png");
        assertTrue(entityTex.exists(), "Fallback 64x64 bounty board entity texture must exist");
        TestHelper.PngDimensions entityDims = TestHelper.getPngDimensions(entityTex);
        assertTrue(entityDims.validPng());
        assertEquals(64, entityDims.width());
        assertEquals(64, entityDims.height());

        ResourceLocation parchmentLoc = LicenseBannerRenderer.getAnimalInfoParchmentTexture();
        File parchmentTex = TestHelper.getAssetFile(parchmentLoc.getPath());
        assertTrue(parchmentTex.exists(), "Medal-free parchment texture must exist");
        TestHelper.PngDimensions parchmentDims = TestHelper.getPngDimensions(parchmentTex);
        assertTrue(parchmentDims.validPng());
        assertEquals(384, parchmentDims.width());
        assertEquals(256, parchmentDims.height());

        File itemTex = TestHelper.getAssetFile("textures/item/animal_info_board.png");
        assertTrue(itemTex.exists(), "Item icon texture for standing info board must exist");

        File itemModel = TestHelper.getAssetFile("models/item/animal_info_board.json");
        assertTrue(itemModel.exists(), "Item model for standing info board must exist");

        for (int tier = 1; tier <= 5; tier++) {
            File tierModel = TestHelper.getAssetFile("models/item/license_banner_tier_" + tier + ".json");
            assertTrue(tierModel.exists());
            String json = Files.readString(tierModel.toPath());
            assertTrue(json.contains("\"custom_model_data\": 9001"), "Tier " + tier + " model must override custom_model_data 9001");
            assertTrue(json.contains("zootycoon:item/animal_info_board"), "Tier " + tier + " model must point to animal_info_board");
        }
    }

    @Test
    @DisplayName("Menu labels, Creative summon keys, and Creative perfect tag match plan")
    void testMenuLabelsAndCreativeSummonContracts() {
        Map<String, String> idLang = TestHelper.getIdLang();
        Map<String, String> enLang = TestHelper.getEnLang();

        assertEquals("Lisensi", idLang.get("zootycoon.ui.card.claim"));
        assertEquals("Lisensi", idLang.get("zootycoon.license.collection.title"));
        assertEquals("Bangunan", idLang.get("zootycoon.ui.card.buildings"));

        assertEquals("Licenses", enLang.get("zootycoon.ui.card.claim"));
        assertEquals("Licenses", enLang.get("zootycoon.license.collection.title"));
        assertEquals("Buildings", enLang.get("zootycoon.ui.card.buildings"));

        assertTrue(idLang.containsKey("zootycoon.license.btn.summon_tier"));
        assertTrue(idLang.containsKey("zootycoon.license.btn.summon_tier_tooltip"));
        assertTrue(idLang.containsKey("zootycoon.license.toast.summon_tier_done"));
        assertTrue(enLang.containsKey("zootycoon.license.btn.summon_tier"));
        assertTrue(enLang.containsKey("zootycoon.license.btn.summon_tier_tooltip"));
        assertTrue(enLang.containsKey("zootycoon.license.toast.summon_tier_done"));

        assertEquals("zoo_creative_perfect", HabitatEvaluator.CREATIVE_PERFECT_TAG);
        var payload = new ZooNetworkPayloads.SummonTierAnimalsPayload(3);
        assertEquals(3, payload.tierLevel());
    }

    @Test
    @DisplayName("English advancement texts and certificate-only place_banner rule match config")
    void testAdvancementsEnglishAndCertificateOnly() throws Exception {
        Map<String, String> idLang = TestHelper.getIdLang();
        Map<String, String> enLang = TestHelper.getEnLang();

        assertEquals("Wall of Honor", enLang.get("advancement.zootycoon.place_banner.title"));
        assertEquals("Mount a License Certificate on the wall", enLang.get("advancement.zootycoon.place_banner.desc"));
        assertEquals("First Plot", enLang.get("advancement.zootycoon.first_claim.title"));
        assertEquals("Five-Star Zoo", enLang.get("advancement.zootycoon.five_star_zoo.title"));

        String idPlaceDesc = idLang.get("advancement.zootycoon.place_banner.desc");
        assertTrue(idPlaceDesc.contains("Sertifikat Lisensi"));
        assertFalse(idPlaceDesc.contains("Papan Informasi Satwa"));

        String itemSource = Files.readString(new File("src/main/java/net/zootycoon/item/LicenseBannerItem.java").toPath());
        assertTrue(itemSource.contains("!isInfoBoard && player instanceof net.minecraft.server.level.ServerPlayer"));

        assertEquals("Beli Papan Info", idLang.get("zootycoon.license.btn.buy_info_board"));
        assertEquals("Beli Papan Info", enLang.get("zootycoon.license.btn.buy_info_board"));
        String modalSource = Files.readString(new File("src/main/java/net/zootycoon/client/ui/AnimalCollectionModal.java").toPath());
        assertTrue(modalSource.contains("Items.EMERALD"));
        assertTrue(modalSource.contains("drawSlateButtonBox"));

        String catalogSource = Files.readString(new File("src/main/java/net/zootycoon/client/ui/SchematicCatalogTab.java").toPath());
        assertTrue(catalogSource.contains("boolean isCreative = mc.player != null && mc.player.isCreative();"));
        int newBtnIdx = catalogSource.indexOf("btn_new_studio");
        int editBtnIdx = catalogSource.indexOf("btn_edit");
        assertTrue(newBtnIdx > 0 && catalogSource.substring(Math.max(0, newBtnIdx - 180), newBtnIdx).contains("if (isCreative)"));
        assertTrue(editBtnIdx > 0 && catalogSource.substring(Math.max(0, editBtnIdx - 180), editBtnIdx).contains("if (isCreative)"));
    }

    @Test
    @DisplayName("Cumulative certificate species counts, updated Tier 2 species, Tortoise label, and multipart segment guard")
    void testCumulativeCountsTier2ReplacementsAndAnacondaPartFix() throws Exception {
        assertEquals(8, LicenseTier.TAMAN_MINI.getCumulativeSpeciesCount());
        assertEquals(16, LicenseTier.KEBUN_BINATANG.getCumulativeSpeciesCount());
        assertEquals(24, LicenseTier.SUAKA_MARGASATWA.getCumulativeSpeciesCount());
        assertEquals(32, LicenseTier.CAGAR_ALAM.getCumulativeSpeciesCount());
        assertEquals(40, LicenseTier.KONSERVASI_DUNIA.getCumulativeSpeciesCount());

        String rendererSrc = Files.readString(new File("src/main/java/net/zootycoon/client/render/LicenseBannerRenderer.java").toPath());
        assertTrue(rendererSrc.contains("tier.getCumulativeSpeciesCount()"));

        assertNotNull(LicenseTier.findSpeciesEntry("black_bear"));
        assertNotNull(LicenseTier.findSpeciesEntry("naturalist:black_bear"));
        assertNotNull(LicenseTier.findSpeciesEntry("rhinoceros"));
        assertNotNull(LicenseTier.findSpeciesEntry("naturalist:rhino"));
        assertNotNull(LicenseTier.findSpeciesEntry("toucan"));
        assertNotNull(LicenseTier.findSpeciesEntry("gazelle"));
        assertNull(LicenseTier.findSpeciesEntry("penguin"));
        assertNull(LicenseTier.findSpeciesEntry("cassowary"));
        assertNull(LicenseTier.findSpeciesEntry("bald_eagle"));
        assertNull(LicenseTier.findSpeciesEntry("vulture"));
        assertEquals("Tortoise", LicenseTier.findSpeciesEntry("tortoise").defaultName());

        Map<String, String> idLang = TestHelper.getIdLang();
        Map<String, String> enLang = TestHelper.getEnLang();
        for (String key : new String[]{"black_bear", "rhinoceros", "toucan", "gazelle"}) {
            assertTrue(idLang.containsKey("zootycoon.species." + key + ".fun_fact"), "Missing id fun fact for " + key);
            assertTrue(enLang.containsKey("zootycoon.species." + key + ".fun_fact"), "Missing en fun fact for " + key);
        }

        String hungerSrc = Files.readString(new File("src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java").toPath());
        assertTrue(hungerSrc.contains("isMultipartSegmentEntity"));
        assertTrue(hungerSrc.contains("resolveRootAnimalEntity"));
    }

    @Test
    @DisplayName("Anaconda predator exclusion, extreme-biome-only thermal comfort, and persistent inspector follow")
    void testThermalRangesPredatorExclusionAndInspectorFollow() throws Exception {
        String evalSrc = Files.readString(new File("src/main/java/net/zootycoon/animal/habitat/HabitatEvaluator.java").toPath());
        assertTrue(evalSrc.contains("!net.zootycoon.animal.ZooAnimalHungerManager.isMultipartSegmentEntity(e)"));

        net.zootycoon.animal.habitat.HabitatProfileLoader.initDefaults();

        // Hot-only specialists get cold outside hot biomes (< 1.25) and never overheat in hot biomes
        for (String id : new String[]{"naturalist:giraffe", "minecraft:camel", "wildernature:lion", "wildernature:scorpion", "alexsmobs:guster"}) {
            var p = net.zootycoon.animal.habitat.HabitatProfileLoader.getDynamicFallbackProfile(ResourceLocation.parse(id));
            assertNotNull(p, "Fallback missing for " + id);
            assertTrue(p.minTemperature() >= 1.25f, id + " minTemperature should require hot climate");
            assertTrue(p.maxTemperature() >= 10.0f, id + " maxTemperature should stay open");
        }

        // Cold-only specialists overheat outside cold biomes (> 0.25)
        for (String id : new String[]{"minecraft:polar_bear", "alexsmobs:froststalker", "alexsmobs:snow_leopard", "alexsmobs:seal"}) {
            var p = net.zootycoon.animal.habitat.HabitatProfileLoader.getDynamicFallbackProfile(ResourceLocation.parse(id));
            assertNotNull(p, "Fallback missing for " + id);
            assertTrue(p.minTemperature() <= -10.0f, id + " minTemperature should handle freezing");
            assertTrue(p.maxTemperature() <= 0.25f, id + " maxTemperature should overheat outside snow biomes");
        }

        // Temperate/moderate species across all 3 mods stay comfortable across all seasons (-10..10)
        for (String id : new String[]{"naturalist:deer", "wildernature:minisheep", "alexsmobs:raccoon", "naturalist:black_bear", "alexsmobs:toucan", "alexsmobs:anaconda", "naturalist:tortoise", "wildernature:bison", "alexsmobs:orca"}) {
            var p = net.zootycoon.animal.habitat.HabitatProfileLoader.getDynamicFallbackProfile(ResourceLocation.parse(id));
            assertNotNull(p, "Fallback missing for " + id);
            assertTrue(p.minTemperature() <= -10.0f, id + " should not require heater across seasons");
            assertTrue(p.maxTemperature() >= 10.0f, id + " should not require cooler across seasons");
        }

        // Persistent inspector follow until result dialog is opened
        String insSrc = Files.readString(new File("src/main/java/net/zootycoon/entity/ZooInspectorEntity.java").toPath());
        assertTrue(insSrc.contains("resultDialogOpened"));
        assertTrue(insSrc.contains("markResultDialogOpened"));
        String mgrSrc = Files.readString(new File("src/main/java/net/zootycoon/license/ZooLicenseManager.java").toPath());
        assertTrue(mgrSrc.contains("inspector.markResultDialogOpened()"));

        File ticketTex = TestHelper.getAssetFile("textures/entity/staff/cashier_ticket.png");
        assertTrue(ticketTex.exists(), "Ticket cashier skin texture must exist");
        TestHelper.PngDimensions ticketDims = TestHelper.getPngDimensions(ticketTex);
        assertTrue(ticketDims.validPng());
        assertEquals(64, ticketDims.width());
        assertEquals(64, ticketDims.height());

        String cashierRendererSrc = Files.readString(new File("src/main/java/net/zootycoon/client/render/ZooCashierRenderer.java").toPath());
        assertTrue(cashierRendererSrc.contains("TEXTURE_TICKET"));
        assertTrue(cashierRendererSrc.contains("\"TICKET\".equalsIgnoreCase(role)"));

        String vehicleSrc = Files.readString(new File("src/main/java/net/zootycoon/entity/ZooVehicleEntity.java").toPath());
        assertTrue(vehicleSrc.contains("return this.getDriver() != null && this.level().isNight() && this.entityData.get(DATA_HEADLIGHTS);"));
    }
}
