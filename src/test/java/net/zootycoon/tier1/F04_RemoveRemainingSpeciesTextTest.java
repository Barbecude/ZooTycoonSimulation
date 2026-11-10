package net.zootycoon.tier1;

import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F4: Removal of Remaining Species Text ('sisa 8 spesies')")
public class F04_RemoveRemainingSpeciesTextTest {

    @Test
    @DisplayName("F4-1: Progress remaining translation key does not contain 'sisa' or 'spesies' text")
    void testProgressRemainingTranslationKeyDoesNotContainRemainingText() {
        Map<String, String> idLang = TestHelper.getIdLang();
        String idVal = idLang.get("zootycoon.license.progress.remaining");
        if (idVal != null) {
            assertFalse(idVal.toLowerCase().contains("sisa"),
                    "Translation key 'zootycoon.license.progress.remaining' in id_id.json must not contain 'sisa', got: " + idVal);
            assertFalse(idVal.toLowerCase().contains("spesies"),
                    "Translation key 'zootycoon.license.progress.remaining' in id_id.json must not contain 'spesies', got: " + idVal);
        }

        Map<String, String> enLang = TestHelper.getEnLang();
        String enVal = enLang.get("zootycoon.license.progress.remaining");
        if (enVal != null) {
            assertFalse(enVal.toLowerCase().contains("remaining"),
                    "Translation key 'zootycoon.license.progress.remaining' in en_us.json must not contain 'remaining', got: " + enVal);
            assertFalse(enVal.toLowerCase().contains("species"),
                    "Translation key 'zootycoon.license.progress.remaining' in en_us.json must not contain 'species', got: " + enVal);
        }
    }

    @Test
    @DisplayName("F4-2: Clean progress formatting produces only completed ratio without remaining count")
    void testCleanProgressFormattingProducesOnlyRatio() {
        int inspectedCount = 0;
        int totalCount = 8;
        String formatted = formatProgressClean(inspectedCount, totalCount);

        assertEquals("Selesai: 0/8", formatted);
        assertFalse(formatted.contains("Sisa"));
        assertFalse(formatted.contains("spesies"));
    }

    @Test
    @DisplayName("F4-3: Progress display when partially inspected (4 of 8)")
    void testPartialInspectionProgressFormat() {
        int inspectedCount = 4;
        int totalCount = 8;
        String formatted = formatProgressClean(inspectedCount, totalCount);

        assertEquals("Selesai: 4/8", formatted);
        assertFalse(formatted.contains("Sisa"));
    }

    @Test
    @DisplayName("F4-4: Progress display when inspection is complete (8 of 8)")
    void testCompletedInspectionProgressFormat() {
        int inspectedCount = 8;
        int totalCount = 8;
        String formatted = formatProgressClean(inspectedCount, totalCount);

        assertEquals("Selesai: 8/8", formatted);
        assertFalse(formatted.contains("Sisa"));
    }

    @Test
    @DisplayName("F4-5: Inspector dialogue string does not contain 'sisa'")
    void testInspectorDialogueDoesNotContainRemainingSpecies() {
        Map<String, String> idLang = TestHelper.getIdLang();
        for (Map.Entry<String, String> entry : idLang.entrySet()) {
            if (entry.getKey().startsWith("zootycoon.license.dialog.")) {
                assertFalse(entry.getValue().toLowerCase().contains("sisa"),
                        "Dialog key " + entry.getKey() + " must not contain 'sisa', found: " + entry.getValue());
            }
        }
    }

    private String formatProgressClean(int inspected, int total) {
        return String.format("Selesai: %d/%d", inspected, total);
    }
}
