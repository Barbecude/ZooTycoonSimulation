package net.zootycoon.tier1;

import net.zootycoon.license.LicenseTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F3: Animal English Names Without Translation Mapping")
public class F03_AnimalEnglishNamesTest {

    @Test
    @DisplayName("F3-1: All 5 license tiers contain exactly 8 species entries with non-empty default names")
    void testAllTiersHaveEightSpeciesWithEnglishNames() {
        LicenseTier[] tiers = LicenseTier.values();
        assertEquals(5, tiers.length, "Must have exactly 5 license tiers");

        int totalSpecies = 0;
        for (LicenseTier tier : tiers) {
            List<LicenseTier.SpeciesEntry> entries = tier.getRequiredSpecies();
            assertEquals(8, entries.size(), "Tier " + tier.name() + " must have exactly 8 species");
            for (LicenseTier.SpeciesEntry entry : entries) {
                assertNotNull(entry.defaultName(), "Default name must not be null for " + entry.speciesKey());
                assertFalse(entry.defaultName().isBlank(), "Default name must not be blank for " + entry.speciesKey());
                totalSpecies++;
            }
        }
        assertEquals(40, totalSpecies, "Total species across all 5 tiers must be 40");
    }

    @Test
    @DisplayName("F3-2: Capybara, Tortoise, and Deer have standard English default names")
    void testSpecificSpeciesHaveEnglishDefaultNames() {
        LicenseTier.SpeciesEntry capybara = findSpecies("capybara");
        assertNotNull(capybara);
        assertEquals("Capybara", capybara.defaultName(), "Default name must be 'Capybara', not 'Kapibara'");

        LicenseTier.SpeciesEntry tortoise = findSpecies("tortoise");
        assertNotNull(tortoise);
        assertEquals("Tortoise", tortoise.defaultName(), "Default name must be 'Tortoise', not 'Kura-kura Darat'");

        LicenseTier.SpeciesEntry deer = findSpecies("deer");
        assertNotNull(deer);
        assertEquals("Deer", deer.defaultName(), "Default name must be 'Deer', not 'Rusa'");
    }

    @Test
    @DisplayName("F3-3: Apex tier species (Tiers 4 & 5) have standard English names")
    void testApexTierSpeciesHaveEnglishDefaultNames() {
        LicenseTier.SpeciesEntry bear = findSpecies("grizzly_bear");
        assertNotNull(bear);
        assertEquals("Grizzly Bear", bear.defaultName());

        LicenseTier.SpeciesEntry anteater = findSpecies("giant_anteater");
        assertNotNull(anteater);
        assertEquals("Giant Anteater", anteater.defaultName(), "Must be 'Giant Anteater', not 'Trenggiling Raksasa'");

        LicenseTier.SpeciesEntry whale = findSpecies("cachalot_whale");
        assertNotNull(whale);
        assertEquals("Cachalot Whale", whale.defaultName());
    }

    @Test
    @DisplayName("F3-4: Species matching works reliably with registry identifiers")
    void testSpeciesMatchingWithRegistryIds() {
        LicenseTier.SpeciesEntry capybara = findSpecies("capybara");
        assertNotNull(capybara);
        assertTrue(capybara.matches("naturalist:capybara"));
        assertFalse(capybara.matches("minecraft:pig"));

        LicenseTier.SpeciesEntry kangaroo = findSpecies("kangaroo");
        assertNotNull(kangaroo);
        assertTrue(kangaroo.matches("alexsmobs:kangaroo"));
    }

    @Test
    @DisplayName("F3-5: Registry path formatting title-cases raw IDs as clean English names")
    void testRegistryPathTitleCasing() {
        assertEquals("Kangaroo", formatSpeciesId("alexsmobs:kangaroo"));
        assertEquals("Giant Tortoise", formatSpeciesId("naturalist:giant_tortoise"));
        assertEquals("Bald Eagle", formatSpeciesId("alexsmobs:bald_eagle"));
        assertEquals("Polar Bear", formatSpeciesId("minecraft:polar_bear"));
    }

    private LicenseTier.SpeciesEntry findSpecies(String key) {
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                if (entry.speciesKey().equals(key)) {
                    return entry;
                }
            }
        }
        return null;
    }

    private String formatSpeciesId(String rawId) {
        if (rawId == null || !rawId.contains(":")) return rawId;
        String path = rawId.substring(rawId.indexOf(':') + 1);
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            }
        }
        return sb.toString();
    }
}
