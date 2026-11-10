package net.zootycoon.tier1;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.TestHelper;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.entity.ZooZookeeperEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F20: Grazing Animal Bone Meal Supply Request & Direct Feeding Fix")
public class F20_GrazingAnimalBoneMealRequestTest {

    @BeforeAll
    static void setup() {
        TestHelper.ensureMinecraftBootstrapped();
    }

    @Test
    @DisplayName("Kangaroo and other grazing species diets specify correct reforestationItem (Bone Meal for grass eaters)")
    void testGrazingDietReforestationItems() {
        var kangarooDiet = AnimalDietRegistry.getDiet("alexsmobs:kangaroo");
        assertNotNull(kangarooDiet, "Kangaroo diet must be registered");
        assertEquals(AnimalDietRegistry.FeedingType.GRAZING, kangarooDiet.feedingType, "Kangaroo must be GRAZING feeding type");
        assertEquals(Items.BONE_MEAL, kangarooDiet.reforestationItem, "Kangaroo reforestationItem must be Bone Meal");
        assertEquals(16, kangarooDiet.reforestationCount, "Kangaroo reforestation count must be 16");
        assertTrue(kangarooDiet.acceptedFoods.contains(Items.WHEAT), "Kangaroo accepts wheat");
        assertTrue(kangarooDiet.acceptedFoods.contains(Items.CARROT), "Kangaroo accepts carrot");
        assertTrue(kangarooDiet.acceptedFoods.contains(Items.APPLE), "Kangaroo accepts apple");

        // Other grass eaters
        var sheepDiet = AnimalDietRegistry.getDiet("minecraft:sheep");
        assertEquals(Items.BONE_MEAL, sheepDiet.reforestationItem, "Sheep uses Bone Meal");

        var zebraDiet = AnimalDietRegistry.getDiet("naturalist:zebra");
        assertEquals(Items.BONE_MEAL, zebraDiet.reforestationItem, "Zebra uses Bone Meal");

        var miniSheepDiet = AnimalDietRegistry.getDiet("wildernature:minisheep");
        assertEquals(Items.BONE_MEAL, miniSheepDiet.reforestationItem, "Mini sheep uses Bone Meal");

        var capybaraDiet = AnimalDietRegistry.getDiet("naturalist:capybara");
        assertEquals(Items.BONE_MEAL, capybaraDiet.reforestationItem, "Capybara uses Bone Meal");

        // Specialized block eaters
        var pandaDiet = AnimalDietRegistry.getDiet("minecraft:panda");
        assertEquals(Items.BAMBOO, pandaDiet.reforestationItem, "Panda uses Bamboo");

        var camelDiet = AnimalDietRegistry.getDiet("minecraft:camel");
        assertEquals(Items.CACTUS, camelDiet.reforestationItem, "Camel uses Cactus");

        var bunfungusDiet = AnimalDietRegistry.getDiet("alexsmobs:bunfungus");
        assertEquals(Items.RED_MUSHROOM, bunfungusDiet.reforestationItem, "Bunfungus uses Red Mushroom");

        var mungusDiet = AnimalDietRegistry.getDiet("alexsmobs:mungus");
        assertEquals(Items.BROWN_MUSHROOM, mungusDiet.reforestationItem, "Mungus uses Brown Mushroom");
    }

    @Test
    @DisplayName("Foods for grazing species includes reforestation item (Bone Meal) for UI and transfer acceptance")
    void testGrazingFoodsIncludeReforestationItem() {
        List<ItemStack> foods = AnimalDietRegistry.getFoodsForSpecies("alexsmobs:kangaroo");
        assertFalse(foods.isEmpty(), "Foods list for Kangaroo must not be empty");
        boolean hasBoneMeal = foods.stream().anyMatch(st -> st.is(Items.BONE_MEAL));
        assertTrue(hasBoneMeal, "Foods list for Kangaroo must include Bone Meal");

        // Verify alternative foods also present
        boolean hasWheat = foods.stream().anyMatch(st -> st.is(Items.WHEAT));
        boolean hasCarrot = foods.stream().anyMatch(st -> st.is(Items.CARROT));
        assertTrue(hasWheat, "Kangaroo foods list includes wheat");
        assertTrue(hasCarrot, "Kangaroo foods list includes carrot");
    }

    @Test
    @DisplayName("WarehouseSupplyManager requests Bone Meal and alternative foods for grazing animals without dropping when hungry")
    void testWarehouseSupplyManagerGrazingLogic() throws Exception {
        Path supplyManagerPath = Path.of("src/main/java/net/zootycoon/warehouse/WarehouseSupplyManager.java");
        assertTrue(Files.exists(supplyManagerPath), "WarehouseSupplyManager.java must exist");
        String src = Files.readString(supplyManagerPath).replace("\r\n", "\n");

        // Verify primary target is reforestationItem (Bone Meal)
        assertTrue(src.contains("req.targetItems.add(new ItemStack(diet.reforestationItem, diet.reforestationCount));"),
                "Supply request must add reforestationItem with proper count as primary target");

        // Verify subtitle indicates Bone Meal or block label
        assertTrue(src.contains("String label = (diet.reforestationItem == Items.BONE_MEAL) ? \"Bone Meal\" : diet.missingBlockLabel;"),
                "Subtitle must explicitly display 'Bone Meal' for grass grazers");

        // Verify hunger check prevents premature request removal
        assertTrue(src.contains("isAnyFamilyHungry"),
                "WarehouseSupplyManager must evaluate isAnyFamilyHungry before canceling grazing supply requests");
    }

    @Test
    @DisplayName("ZooZookeeperEntity directly feeds hungry grazing animals and plants bone meal on dirt or grass")
    void testZookeeperDirectFeedingAndBoneMealPlanting() throws Exception {
        Path zookeeperPath = Path.of("src/main/java/net/zootycoon/entity/ZooZookeeperEntity.java");
        assertTrue(Files.exists(zookeeperPath), "ZooZookeeperEntity.java must exist");
        String src = Files.readString(zookeeperPath).replace("\r\n", "\n");

        // Priority 2b direct feeding
        assertTrue(src.contains("peekFoodForFamily"), "ZooZookeeperEntity must peek food for family when feeding grazing animals");
        assertTrue(src.contains("ZooAnimalHungerManager.feedAnimal(hungryTarget, feedStack, null)"),
                "Zookeeper must directly hand-feed hungry grazing target when in range");

        // Bone meal planting spot accepts dirt and converts to grass
        assertTrue(src.contains("groundState.is(Blocks.DIRT) || groundState.is(Blocks.COARSE_DIRT) || groundState.is(Blocks.ROOTED_DIRT)"),
                "findValidPlantingSpotNearAnimal must accept dirt blocks for bone meal application");
        assertTrue(src.contains("sLevel.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());"),
                "plantVegetationBlock must convert dirt to grass block when bone meal is used");

        // Food transfer accepts bone meal / matching vegetation
        assertTrue(src.contains("isMatchingVegetationItem(st, diet)"),
                "handleFoodTransfer must accept matching vegetation and bone meal for the animal's diet");
    }
}
