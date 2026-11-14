package net.zootycoon.tier1;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.TestHelper;
import net.zootycoon.construction.ConstructionSite;
import net.zootycoon.schematic.SchematicScanner;
import net.zootycoon.schematic.ZooSchematic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F23: Builder Flower to Bone Meal Mapping")
public class F23_BuilderFlowerBonemealMappingTest {

    @BeforeAll
    static void setup() {
        TestHelper.ensureMinecraftBootstrapped();
    }

    @Test
    @DisplayName("F23-1: Vanilla flowers and petals map builder material requirement to Bone Meal")
    void testVanillaFlowersRequireBonemeal() {
        assertEquals(Items.BONE_MEAL, SchematicScanner.getRequiredItemForState(Blocks.DANDELION.defaultBlockState()));
        assertEquals(Items.BONE_MEAL, SchematicScanner.getRequiredItemForState(Blocks.SUNFLOWER.defaultBlockState()));
        assertEquals(Items.BONE_MEAL, SchematicScanner.getRequiredItemForState(Blocks.PINK_PETALS.defaultBlockState()));
        assertTrue(ConstructionSite.isGrassOrFlowerItem(new ItemStack(Items.POPPY)));
        assertTrue(ConstructionSite.isGrassOrFlowerItem(new ItemStack(Items.PINK_PETALS)));
    }

    @Test
    @DisplayName("F23-2: BloomingNature, Farm & Charm, Farmer's Delight, and generic modded flowers match")
    void testModdedFlowerRulesMatchAndExcludeNonFlowers() {
        // BloomingNature & Let's Do wildflowers
        assertTrue(SchematicScanner.matchesModdedFlowerRule("bloomingnature", "bluebell", "CustomBushBlock", true, false));
        assertTrue(SchematicScanner.matchesModdedFlowerRule("bloomingnature", "freesia", "FlowerVariantBlock", true, false));
        assertTrue(SchematicScanner.matchesModdedFlowerRule("farm_and_charm", "wild_ribwort", "WildPlantBlock", true, false));
        assertTrue(SchematicScanner.matchesModdedFlowerRule("farmersdelight", "wild_cabbages", "WildCropBlock", true, false));
        assertTrue(SchematicScanner.matchesModdedFlowerRule("anyothermod", "blue_orchid_wildflower", "ModdedBush", true, false));

        // Non-flower exclusions (pots, boxes, crates, cauliflower, saplings, solid blocks)
        assertFalse(SchematicScanner.matchesModdedFlowerRule("bloomingnature", "flower_box", "Block", false, false));
        assertFalse(SchematicScanner.matchesModdedFlowerRule("farm_and_charm", "cauliflower_crate", "Block", false, true));
        assertFalse(SchematicScanner.matchesModdedFlowerRule("bloomingnature", "aspen_sapling", "SaplingBlock", true, false));
        assertFalse(SchematicScanner.matchesModdedFlowerRule("farmersdelight", "onion_crate", "Block", false, true));
        assertNotEquals(Items.BONE_MEAL, SchematicScanner.getRequiredItemForState(Blocks.OAK_SAPLING.defaultBlockState()));
        assertNotEquals(Items.BONE_MEAL, SchematicScanner.getRequiredItemForState(Blocks.FLOWER_POT.defaultBlockState()));
        assertNotEquals(Items.BONE_MEAL, SchematicScanner.getRequiredItemForState(Blocks.OAK_PLANKS.defaultBlockState()));
    }

    @Test
    @DisplayName("F23-3: Large Bone Meal counts (>=200) cap deterministically to non-round random [101..199] and build completely")
    void testBoneMealCapNonRoundAndSiteConsumption() {
        assertEquals(0, SchematicScanner.capBoneMealRequirement(0, 12345L));
        assertEquals(47, SchematicScanner.capBoneMealRequirement(47, 12345L));
        assertEquals(199, SchematicScanner.capBoneMealRequirement(199, 12345L));

        Set<Integer> observed = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            int capped = SchematicScanner.capBoneMealRequirement(200 + i * 37, 987654321L * (i + 1));
            assertTrue(capped > 100 && capped < 200, "Capped Bone Meal must be strictly in 101..199, got " + capped);
            assertNotEquals(0, capped % 10, "Capped Bone Meal must not end in 0: " + capped);
            assertNotEquals(0, capped % 5, "Capped Bone Meal must not be round (divisible by 5): " + capped);
            observed.add(capped);
        }
        assertTrue(observed.size() > 20, "Capped Bone Meal should vary naturally across structures");
        assertEquals(
                SchematicScanner.capBoneMealRequirement(2500, 77777L),
                SchematicScanner.capBoneMealRequirement(2500, 77777L),
                "Same schematic signature must remain deterministic across catalog and reloads"
        );

        // ConstructionSite should only demand the capped amount (e.g. 137) even with 300 grass/flower blocks
        List<StructureTemplate.StructureBlockInfo> blocks = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            blocks.add(new StructureTemplate.StructureBlockInfo(
                    new BlockPos(i % 20, 0, i / 20),
                    Blocks.POPPY.defaultBlockState(),
                    null
            ));
        }
        ZooSchematic schem = new ZooSchematic(
                "test_flower_field", "Test Field", "enclosures", "Test", "Builder",
                new net.minecraft.core.Vec3i(20, 1, 15), "Test", 150, 0, true, null
        );
        ConstructionSite site = new ConstructionSite(
                UUID.randomUUID(), UUID.randomUUID(), BlockPos.ZERO, Level.OVERWORLD,
                schem, Map.<Item, Integer>of(Items.BONE_MEAL, 137), blocks
        );

        assertEquals(137, site.getMissingMaterials().getOrDefault(Items.BONE_MEAL, 0));
        int delivered = site.deliverMaterial(new ItemStack(Items.BONE_MEAL, 200));
        assertEquals(137, delivered, "Should accept only up to the 137 capped Bone Meal budget");
        assertTrue(site.getMissingMaterials().isEmpty(), "No materials should be missing once 137 Bone Meal are delivered");

        for (var block : blocks) {
            assertTrue(site.hasAvailableItemFor(block));
            assertTrue(site.consumeItemFor(block));
        }
        assertTrue(site.getMissingMaterials().isEmpty());
    }
}
