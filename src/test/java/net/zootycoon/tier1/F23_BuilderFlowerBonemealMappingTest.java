package net.zootycoon.tier1;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.zootycoon.TestHelper;
import net.zootycoon.construction.ConstructionSite;
import net.zootycoon.schematic.SchematicScanner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
