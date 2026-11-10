package net.zootycoon.tier1;

import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F15: Localization Integrity & Clean Text Guidelines")
public class F15_LocalizationAndCleanTextTest {

    private static final Pattern BOLD_PATTERN = Pattern.compile("(?i)§l");

    @Test
    @DisplayName("F15-1: Language files id_id.json and en_us.json both load cleanly")
    void testLanguageFilesLoadSuccessfully() {
        Map<String, String> idLang = TestHelper.getIdLang();
        assertNotNull(idLang, "id_id.json must load successfully");
        assertFalse(idLang.isEmpty(), "id_id.json must not be empty");

        Map<String, String> enLang = TestHelper.getEnLang();
        assertNotNull(enLang, "en_us.json must load successfully");
        assertFalse(enLang.isEmpty(), "en_us.json must not be empty");
    }

    @Test
    @DisplayName("F15-2: No bold font control codes (§l) exist in Indonesian translations")
    void testNoBoldControlCodesInIndonesianTranslations() {
        Map<String, String> idLang = TestHelper.getIdLang();
        int boldCount = 0;
        StringBuilder offendingKeys = new StringBuilder();

        for (Map.Entry<String, String> entry : idLang.entrySet()) {
            if (BOLD_PATTERN.matcher(entry.getValue()).find()) {
                boldCount++;
                offendingKeys.append(entry.getKey()).append(", ");
            }
        }
        assertEquals(0, boldCount, "Found bold codes in id_id.json keys: " + offendingKeys);
    }

    @Test
    @DisplayName("F15-3: No bold font control codes (§l) exist in English translations")
    void testNoBoldControlCodesInEnglishTranslations() {
        Map<String, String> enLang = TestHelper.getEnLang();
        int boldCount = 0;
        StringBuilder offendingKeys = new StringBuilder();

        for (Map.Entry<String, String> entry : enLang.entrySet()) {
            if (BOLD_PATTERN.matcher(entry.getValue()).find()) {
                boldCount++;
                offendingKeys.append(entry.getKey()).append(", ");
            }
        }
        assertEquals(0, boldCount, "Found bold codes in en_us.json keys: " + offendingKeys);
    }

    @Test
    @DisplayName("F15-4: No emoji characters exist in Zoo Tycoon translation values (Strict user rule)")
    void testNoEmojisInTranslationValues() {
        Map<String, String> idLang = TestHelper.getIdLang();
        for (Map.Entry<String, String> entry : idLang.entrySet()) {
            if (entry.getKey().contains("zootycoon")) {
                assertFalse(TestHelper.containsEmoji(entry.getValue()),
                        "Key '" + entry.getKey() + "' in id_id.json contains emoji: " + entry.getValue());
            }
        }

        Map<String, String> enLang = TestHelper.getEnLang();
        for (Map.Entry<String, String> entry : enLang.entrySet()) {
            if (entry.getKey().contains("zootycoon")) {
                assertFalse(TestHelper.containsEmoji(entry.getValue()),
                        "Key '" + entry.getKey() + "' in en_us.json contains emoji: " + entry.getValue());
            }
        }
    }

    @Test
    @DisplayName("F15-5: No empty or whitespace-only translation strings")
    void testNoEmptyTranslationValues() {
        Map<String, String> idLang = TestHelper.getIdLang();
        for (Map.Entry<String, String> entry : idLang.entrySet()) {
            assertFalse(entry.getValue().trim().isEmpty(),
                    "Key '" + entry.getKey() + "' in id_id.json has empty value");
        }

        Map<String, String> enLang = TestHelper.getEnLang();
        for (Map.Entry<String, String> entry : enLang.entrySet()) {
            assertFalse(entry.getValue().trim().isEmpty(),
                    "Key '" + entry.getKey() + "' in en_us.json has empty value");
        }
    }
}
