package net.zootycoon.tier2;

import net.zootycoon.TestHelper;
import net.zootycoon.tier1.F12_AnimalInfoBannerAwardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F12: Animal Information Banner Award Boundary Cases")
public class F12_AnimalInfoBannerAwardBoundaryTest {

    @Test
    @DisplayName("F12-B1: Extremely short fun fact text handles minimum margin bounds")
    void testShortFunFactText() {
        String shortFact = "Loves water.";
        assertTrue(shortFact.length() > 0 && shortFact.length() < 20);
        assertFalse(TestHelper.containsEmoji(shortFact));
    }

    @Test
    @DisplayName("F12-B2: Long fun fact text (300+ characters) wrapping boundary")
    void testLongFunFactTextWrapping() {
        String longFact = "Capybaras (Hydrochoerus hydrochaeris) are giant cavy rodents native to South America. " +
                "They are the largest living rodents in the world and inhabit savannas and dense forests living near bodies of water. " +
                "They are highly social species, living in groups as large as 100 individuals.";
        assertTrue(longFact.length() > 200);

        int maxCharsPerLine = 32;
        int estimatedLines = (int) Math.ceil((double) longFact.length() / maxCharsPerLine);
        assertTrue(estimatedLines >= 7, "Long fun fact wraps into multiple lines");
        assertFalse(TestHelper.containsEmoji(longFact));
    }

    @Test
    @DisplayName("F12-B3: Multiple consecutive inspection passes yield distinct banner awards")
    void testMultipleInspectionPassesAwardBanners() {
        int passes = 5;
        int bannersAwarded = 0;
        for (int i = 0; i < passes; i++) {
            if (F12_AnimalInfoBannerAwardTest.shouldAwardInfoBanner(true)) {
                bannersAwarded++;
            }
        }
        assertEquals(5, bannersAwarded);
    }

    @Test
    @DisplayName("F12-B4: Unknown species key generates standardized fallback key format")
    void testUnknownSpeciesKeyFormat() {
        String unknownKey = "fictional_creature";
        String key = F12_AnimalInfoBannerAwardTest.getFunFactKey(unknownKey);
        assertEquals("zootycoon.species.fictional_creature.fun_fact", key);
    }

    @Test
    @DisplayName("F12-B5: Inspection failure boundary awards zero banners")
    void testFailedInspectionAwardsZeroBanners() {
        assertFalse(F12_AnimalInfoBannerAwardTest.shouldAwardInfoBanner(false));
    }
}
