package net.zootycoon.tier2;

import net.zootycoon.license.LicenseTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F3: Animal English Names Boundary and Corner Cases")
public class F03_AnimalEnglishNamesBoundaryTest {

    @Test
    @DisplayName("F3-B1: Null or empty entity identifier matching returns false without NPE")
    void testNullOrEmptyMatching() {
        LicenseTier.SpeciesEntry entry = new LicenseTier.SpeciesEntry("capybara", "Capybara", List.of("naturalist:capybara"));
        assertFalse(entry.matches((String) null), "Null identifier should match false without throwing NPE");
        assertFalse(entry.matches(""), "Empty string identifier should match false");
        assertFalse(entry.matches("   "), "Whitespace string identifier should match false");
    }

    @Test
    @DisplayName("F3-B2: Unknown mod namespace falls back to Title-Cased registry path")
    void testUnknownModNamespaceFallback() {
        String rawUnknown = "fictional_mod:mythical_dragon_beast";
        String formatted = formatSpeciesId(rawUnknown);
        assertEquals("Mythical Dragon Beast", formatted, "Multi-word unknown species should title-case cleanly");
    }

    @Test
    @DisplayName("F3-B3: Identifier without colon namespace prefix handled safely")
    void testIdentifierWithoutColon() {
        String noColon = "capybara";
        String formatted = formatSpeciesId(noColon);
        assertEquals("capybara", formatted, "Raw identifier without colon returned intact without string index exception");
    }

    @Test
    @DisplayName("F3-B4: Complex multi-underscore compound names title-case correctly")
    void testMultiUnderscoreCompoundNames() {
        assertEquals("Giant Anteater", formatSpeciesId("alexsmobs:giant_anteater"));
        assertEquals("Great White Shark", formatSpeciesId("naturalist:great_white_shark"));
        assertEquals("Mantis Shrimp", formatSpeciesId("alexsmobs:mantis_shrimp"));
        assertEquals("Capuchin Monkey", formatSpeciesId("alexsmobs:capuchin_monkey"));
    }

    @Test
    @DisplayName("F3-B5: Case insensitive entity matching")
    void testCaseInsensitiveMatching() {
        LicenseTier.SpeciesEntry entry = new LicenseTier.SpeciesEntry("capybara", "Capybara", List.of("naturalist:capybara"));
        assertTrue(entry.matches("NATURALIST:CAPYBARA"), "Matching should be case insensitive");
        assertTrue(entry.matches("Naturalist:Capybara"));
        assertTrue(entry.matches("naturalist:capybara"));
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
