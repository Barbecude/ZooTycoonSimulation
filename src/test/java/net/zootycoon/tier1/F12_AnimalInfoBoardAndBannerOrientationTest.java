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
    }
}
