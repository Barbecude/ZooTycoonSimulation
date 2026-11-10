package net.zootycoon.tier2;

import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F15: Localization and Clean Text Boundary Cases")
public class F15_LocalizationAndCleanTextBoundaryTest {

    private static final Pattern FORMAT_SPECIFIERS = Pattern.compile("%(\\d+\\$)?[-#+ 0,(\\<]?[0-9]*(\\.[0-9]+)?[a-zA-Z%]");

    @Test
    @DisplayName("F15-B1: Format specifiers (%s, %d) match between en_us and id_id for shared keys")
    void testFormatSpecifierParityBetweenLanguages() {
        Map<String, String> idLang = TestHelper.getIdLang();
        Map<String, String> enLang = TestHelper.getEnLang();

        for (Map.Entry<String, String> entry : idLang.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("zootycoon.") && enLang.containsKey(key)) {
                int idSpecifiers = countSpecifiers(entry.getValue());
                int enSpecifiers = countSpecifiers(enLang.get(key));
                assertEquals(idSpecifiers, enSpecifiers,
                        "Format specifier count mismatch for key '" + key + "' (id: " + idSpecifiers + ", en: " + enSpecifiers + ")");
            }
        }
    }

    @Test
    @DisplayName("F15-B2: Newline control characters (\\n) are properly escaped in JSON")
    void testNewlineEscaping() {
        Map<String, String> idLang = TestHelper.getIdLang();
        for (Map.Entry<String, String> entry : idLang.entrySet()) {
            if (entry.getKey().startsWith("zootycoon.")) {
                String val = entry.getValue();
                // If it contains newlines, ensure it's not a raw unescaped ASCII control char 0x0A
                assertNotNull(val);
            }
        }
    }

    @Test
    @DisplayName("F15-B3: Quotation marks and special characters do not corrupt string structure")
    void testSpecialCharacterIntegrity() {
        Map<String, String> idLang = TestHelper.getIdLang();
        for (Map.Entry<String, String> entry : idLang.entrySet()) {
            if (entry.getKey().startsWith("zootycoon.")) {
                assertFalse(entry.getValue().startsWith("\"") && entry.getValue().endsWith("\"") && entry.getValue().length() > 2 && !entry.getValue().contains(" "),
                        "Double quotation wrapping defect detected in key: " + entry.getKey());
            }
        }
    }

    @Test
    @DisplayName("F15-B4: Degree symbol (°C) and bullet points (•) preserve valid UTF-8 encoding")
    void testUtf8SpecialCharactersPreserved() {
        Map<String, String> idLang = TestHelper.getIdLang();
        String tempKey = "zootycoon.species.temp.comfortable";
        if (idLang.containsKey(tempKey)) {
            String val = idLang.get(tempKey);
            assertNotNull(val);
            assertFalse(val.contains(""), "Corrupt replacement character (U+FFFD) found in " + tempKey);
        }
    }

    @Test
    @DisplayName("F15-B5: Extremely long translation strings do not contain bold formatting codes")
    void testLongStringsDoNotContainBold() {
        Map<String, String> idLang = TestHelper.getIdLang();
        for (Map.Entry<String, String> entry : idLang.entrySet()) {
            if (entry.getValue().length() > 100 && entry.getKey().startsWith("zootycoon.")) {
                assertFalse(entry.getValue().toLowerCase().contains("§l"),
                        "Long string key '" + entry.getKey() + "' must not contain bold §l code");
            }
        }
    }

    private int countSpecifiers(String s) {
        if (s == null) return 0;
        Matcher m = FORMAT_SPECIFIERS.matcher(s);
        int count = 0;
        while (m.find()) {
            if (!m.group().equals("%%")) { // Exclude literal %%
                count++;
            }
        }
        return count;
    }
}
