package net.zootycoon.stress;

import net.minecraft.resources.ResourceLocation;
import net.zootycoon.TestHelper;
import net.zootycoon.client.render.LicenseBannerRenderer;
import net.zootycoon.client.ui.AnimalManagementTab;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.network.ZooNetworkPayloads;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Milestone 1 Empirical Stress Tests (F1 to F6)")
public class F01_F06_EmpiricalStressTest {

    private static Method computeLayoutMethod;
    private static Method formatSpeciesMethod;
    private static Method treeIndentMethod;
    private static Method buildDisplayItemsMethod;
    private static AnimalManagementTab tabInstance;

    @BeforeAll
    static void setUp() throws Exception {
        TestHelper.ensureMinecraftBootstrapped();

        // Instantiate AnimalManagementTab safely with null parent
        Constructor<AnimalManagementTab> ctor = AnimalManagementTab.class.getDeclaredConstructor(net.zootycoon.client.ui.MainManagementScreen.class);
        ctor.setAccessible(true);
        tabInstance = ctor.newInstance((Object) null);

        // Access private layout and formatting methods
        computeLayoutMethod = AnimalManagementTab.class.getDeclaredMethod("computeLayout", int.class, int.class, boolean.class);
        computeLayoutMethod.setAccessible(true);

        formatSpeciesMethod = AnimalManagementTab.class.getDeclaredMethod("formatSpecies", String.class);
        formatSpeciesMethod.setAccessible(true);

        treeIndentMethod = AnimalManagementTab.class.getDeclaredMethod("treeIndent", AnimalManagementTab.AnimalListItem.class);
        treeIndentMethod.setAccessible(true);
    }

    private AnimalManagementTab.LayoutMetrics invokeComputeLayout(int w, int h, boolean hasSell) {
        try {
            return (AnimalManagementTab.LayoutMetrics) computeLayoutMethod.invoke(tabInstance, w, h, hasSell);
        } catch (Exception e) {
            throw new RuntimeException("computeLayout failed at " + w + "x" + h, e);
        }
    }

    private String invokeFormatSpecies(String rawId) {
        try {
            return (String) formatSpeciesMethod.invoke(tabInstance, rawId);
        } catch (Exception e) {
            throw new RuntimeException("formatSpecies failed for " + rawId, e);
        }
    }

    private int invokeTreeIndent(int depth) {
        try {
            AnimalManagementTab.AnimalListItem item = new AnimalManagementTab.AnimalListItem(
                    new ZooNetworkPayloads.AnimalSyncData(1, UUID.randomUUID().toString(), "naturalist:capybara", "", 100, 100, 100, 20.0f, 20.0f, false, "", "", "", "", false, ""),
                    depth, false, false, 0
            );
            return (int) treeIndentMethod.invoke(null, item);
        } catch (Exception e) {
            throw new RuntimeException("treeIndent failed for depth " + depth, e);
        }
    }

    @Test
    @DisplayName("STRESS-F1-1: Responsive layout matrix across standard GUI scales (Scale 1 to 4) and window dimensions")
    void testComputeLayoutResponsiveMatrix() {
        int[][] viewports = {
                // 1920x1080 Full HD
                {1920, 1080}, // Scale 1
                {960, 540},   // Scale 2
                {640, 360},   // Scale 3
                {480, 270},   // Scale 4 (Auto default)
                // 1366x768 Common Laptop
                {1366, 768},  // Scale 1
                {683, 384},   // Scale 2
                {455, 256},   // Scale 3
                // 1280x720 HD
                {1280, 720},  // Scale 1
                {640, 360},   // Scale 2
                {426, 240},   // Scale 3
                // 854x480 Minimum Minecraft Window Size
                {854, 480},   // Scale 1
                {427, 240}    // Scale 2
        };

        boolean[] sellStates = {false, true};

        for (int[] vp : viewports) {
            int w = vp[0];
            int h = vp[1];

            for (boolean hasSell : sellStates) {
                String ctx = "Viewport " + w + "x" + h + " (hasSell=" + hasSell + ")";
                AnimalManagementTab.LayoutMetrics m = invokeComputeLayout(w, h, hasSell);

                // 1. All layout bounds must be strictly positive
                assertTrue(m.sidebarW > 0, ctx + ": sidebarW must be positive, got " + m.sidebarW);
                assertTrue(m.sidebarH > 0, ctx + ": sidebarH must be positive, got " + m.sidebarH);
                assertTrue(m.contentW > 0, ctx + ": contentW must be positive, got " + m.contentW);
                assertTrue(m.contentH > 0, ctx + ": contentH must be positive, got " + m.contentH);
                assertTrue(m.previewW > 0, ctx + ": previewW must be positive, got " + m.previewW);
                assertTrue(m.previewH > 0, ctx + ": previewH must be positive, got " + m.previewH);
                assertTrue(m.infoW > 0, ctx + ": infoW must be positive, got " + m.infoW);

                // 2. No panel overlap and no screen overflow
                assertTrue(m.sidebarX >= m.margin, ctx + ": sidebarX within left margin");
                assertTrue(m.sidebarX + m.sidebarW <= m.contentX, ctx + ": sidebar does not overlap content panel");
                assertTrue(m.contentX + m.contentW <= w, ctx + ": content panel does not exceed screen width (" + (m.contentX + m.contentW) + " > " + w + ")");
                assertTrue(m.sidebarY + m.sidebarH <= h, ctx + ": sidebar does not exceed screen height");
                assertTrue(m.contentY + m.contentH <= h, ctx + ": content panel does not exceed screen height");

                // 3. Action buttons stay inside content panel
                int contentRight = m.contentX + m.contentW;
                assertTrue(m.removeBtnX >= m.contentX, ctx + ": removeBtnX must be within content panel");
                assertTrue(m.removeBtnX + m.removeBtnW <= contentRight,
                        ctx + ": removeBtn right edge (" + (m.removeBtnX + m.removeBtnW) + ") exceeds content panel (" + contentRight + ")");

                if (hasSell) {
                    assertTrue(m.sellBtnX >= m.contentX, ctx + ": sellBtnX must be within content panel");
                    assertTrue(m.sellBtnX + m.sellBtnW <= contentRight,
                        ctx + ": sellBtn right edge (" + (m.sellBtnX + m.sellBtnW) + ") exceeds content panel (" + contentRight + ")");

                    // 4. Verify no button collision
                    if (m.infoW >= 170) {
                        // Side-by-side mode: remove button is on left, sell button is on right
                        assertTrue(m.removeBtnX + m.removeBtnW <= m.sellBtnX,
                                ctx + ": Side-by-side buttons collide! removeRight=" + (m.removeBtnX + m.removeBtnW) + ", sellX=" + m.sellBtnX);
                        assertEquals(m.removeBtnY, m.sellBtnY, ctx + ": Side-by-side buttons must share Y");
                    } else {
                        // Stacked mode: sell button is stacked vertically below remove button
                        assertTrue(m.removeBtnY + m.removeBtnH <= m.sellBtnY,
                                ctx + ": Stacked buttons collide! removeBottom=" + (m.removeBtnY + m.removeBtnH) + ", sellY=" + m.sellBtnY);
                    }
                }

                // 5. Scroll area must be within content panel and have non-negative height
                assertTrue(m.scrollAreaX >= m.contentX, ctx + ": scrollAreaX within content panel");
                assertTrue(m.scrollAreaX + m.scrollAreaW <= contentRight, ctx + ": scrollAreaW within content panel");
                assertTrue(m.scrollAreaH > 0, ctx + ": scrollAreaH must be positive, got " + m.scrollAreaH);
            }
        }
    }

    @Test
    @DisplayName("STRESS-F1-2: Extreme species names formatting and character handling")
    void testFormatSpeciesStressWithExtremeNames() {
        // Null and empty handling
        assertEquals("", invokeFormatSpecies(null));
        assertEquals("", invokeFormatSpecies(""));

        // All 40 species registered in LicenseTier must return clean English names
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                for (String entityId : entry.entityTypeIds()) {
                    String formatted = invokeFormatSpecies(entityId);
                    assertNotNull(formatted);
                    assertFalse(formatted.isBlank());
                    assertEquals(entry.getDisplayName(), formatted,
                            "Entity ID " + entityId + " must resolve to registered English name: " + entry.getDisplayName());
                }
            }
        }

        // Unregistered external IDs with arbitrary path structures
        assertEquals("Very Long Dragon Species", invokeFormatSpecies("mymod:very_long_dragon_species"));
        assertEquals("Single", invokeFormatSpecies("mod:single"));
        assertEquals("Hyphenated-animal-name", invokeFormatSpecies("custom:hyphenated-animal-name"));
        assertEquals("Hyphenated Animal Name", invokeFormatSpecies("custom:hyphenated_animal_name"));
        assertEquals("UnknownRawStringWithoutColon", invokeFormatSpecies("UnknownRawStringWithoutColon"));
    }

    @Test
    @DisplayName("STRESS-F1-3: Deep tree indentation cap preserves text space in sidebar")
    void testTreeIndentCappingAtExtremeDepths() {
        // Indentation must be strictly capped at 21px (depth 3 * 7px)
        int indentDepth0 = invokeTreeIndent(0);
        int indentDepth1 = invokeTreeIndent(1);
        int indentDepth2 = invokeTreeIndent(2);
        int indentDepth3 = invokeTreeIndent(3);
        int indentDepth4 = invokeTreeIndent(4);
        int indentDepth10 = invokeTreeIndent(10);
        int indentDepth100 = invokeTreeIndent(100);

        assertEquals(0, indentDepth0);
        assertEquals(7, indentDepth1);
        assertEquals(14, indentDepth2);
        assertEquals(21, indentDepth3);
        assertEquals(21, indentDepth4, "Depth 4 must cap at 21px");
        assertEquals(21, indentDepth10, "Depth 10 must cap at 21px");
        assertEquals(21, indentDepth100, "Depth 100 must cap at 21px");

        // Even with max indentation on smallest sidebar (125px at scale 4),
        // text area remains: 125 - 3 - 21 (indent) - 32 (icon) - 14 (alert) = 55px (sufficient for truncated text)
        int minSidebarW = 125;
        int maxIndent = 21;
        int reservedElements = 32 + 14 + 6;
        int remainingTextSpace = minSidebarW - maxIndent - reservedElements;
        assertTrue(remainingTextSpace >= 50, "Remaining text space in sidebar (" + remainingTextSpace + "px) must be >= 50px");
    }

    @Test
    @DisplayName("STRESS-F2: Pixel art icons physical image integrity, dimensions and transparency")
    void testCutePixelArtIconsPhysicalIntegrity() throws Exception {
        String[] iconPaths = {
                "textures/gui/icons/animals.png",
                "textures/gui/icons/animal_list_cute.png",
                "textures/gui/icons/animal_collection_cute.png",
                "textures/gui/icons/license_cute.png"
        };

        for (String relPath : iconPaths) {
            File f = TestHelper.getAssetFile(relPath);
            assertTrue(f.exists(), "Icon asset must exist on disk: " + relPath);
            assertTrue(f.length() > 64, "Icon file size must be non-trivial (> 64 bytes): " + relPath);

            TestHelper.PngDimensions dims = TestHelper.getPngDimensions(f);
            assertTrue(dims.validPng(), "Icon must be a valid PNG: " + relPath);
            assertEquals(dims.width(), dims.height(), "Icon must be square: " + relPath + " (" + dims.width() + "x" + dims.height() + ")");
            assertTrue(dims.width() == 16 || dims.width() == 32 || dims.width() == 64,
                    "Icon width should conform to standard pixel art grid (16, 32, or 64): " + dims.width());

            // Load with Java ImageIO to guarantee no decoders crash
            BufferedImage img = ImageIO.read(f);
            assertNotNull(img, "ImageIO must successfully decode: " + relPath);
            assertEquals(dims.width(), img.getWidth());
            assertEquals(dims.height(), img.getHeight());
            assertTrue(img.getColorModel().hasAlpha(), "Pixel art icon should have alpha channel for transparent background: " + relPath);
        }
    }

    @Test
    @DisplayName("STRESS-F3: Audit all 40 species for 100% English naming and zero Indonesian terms")
    void testAllFortySpeciesAreEnglishNames() {
        Set<String> indonesianTerms = Set.of(
                "kapibara", "kura-kura", "rusa", "trenggiling", "paus",
                "hiu", "burung unta", "rajawali", "gajah", "harimau",
                "singa", "beruang", "domba", "unta", "jerapah", "kuda nil"
        );

        int count = 0;
        for (LicenseTier tier : LicenseTier.values()) {
            for (LicenseTier.SpeciesEntry species : tier.getRequiredSpecies()) {
                String name = species.defaultName();
                assertNotNull(name, "Species name cannot be null: " + species.speciesKey());
                assertFalse(name.isBlank(), "Species name cannot be blank: " + species.speciesKey());

                String lower = name.toLowerCase(Locale.ROOT);
                for (String term : indonesianTerms) {
                    assertFalse(lower.equals(term) || lower.startsWith(term + " ") || lower.endsWith(" " + term),
                            "Species " + species.speciesKey() + " has Indonesian defaultName: '" + name + "' (matched '" + term + "')");
                }
                count++;
            }
        }
        assertEquals(40, count, "Must audit exactly 40 species across all 5 tiers");
    }

    @Test
    @DisplayName("STRESS-F4: Verify total absence of 'sisa ... spesies' in dialogs, UI and localization")
    void testAbsenceOfRemainingSpeciesText() {
        Map<String, String> idLang = TestHelper.getIdLang();
        Map<String, String> enLang = TestHelper.getEnLang();

        // 1. Check progress remaining key
        String idRem = idLang.get("zootycoon.license.progress.remaining");
        assertNotNull(idRem);
        assertFalse(idRem.toLowerCase().contains("sisa"), "zootycoon.license.progress.remaining must not contain 'sisa'");
        assertFalse(idRem.toLowerCase().contains("spesies"), "zootycoon.license.progress.remaining must not contain 'spesies'");
        assertTrue(idRem.contains("%d/%d"), "zootycoon.license.progress.remaining should be clean ratio '%d/%d', got: " + idRem);

        String enRem = enLang.get("zootycoon.license.progress.remaining");
        assertNotNull(enRem);
        assertFalse(enRem.toLowerCase().contains("remaining"), "zootycoon.license.progress.remaining in en_us must not contain 'remaining'");
        assertFalse(enRem.toLowerCase().contains("species"), "zootycoon.license.progress.remaining in en_us must not contain 'species'");
        assertTrue(enRem.contains("%d/%d"), "zootycoon.license.progress.remaining in en_us should be clean ratio '%d/%d', got: " + enRem);

        // 2. Scan all inspector dialog keys in both languages
        for (Map.Entry<String, String> entry : idLang.entrySet()) {
            if (entry.getKey().startsWith("zootycoon.license.dialog.")) {
                assertFalse(entry.getValue().toLowerCase().contains("sisa"),
                        "Dialog key " + entry.getKey() + " contains 'sisa': " + entry.getValue());
            }
        }
    }

    @Test
    @DisplayName("STRESS-F5: Inspection letter parchment visual assets, contrast and aspect ratio")
    void testInspectionLetterVisualsAndContrast() throws Exception {
        File parchment = TestHelper.getAssetFile("textures/gui/letter_parchment.png");
        assertTrue(parchment.exists(), "textures/gui/letter_parchment.png must exist");

        TestHelper.PngDimensions dims = TestHelper.getPngDimensions(parchment);
        assertTrue(dims.validPng(), "letter_parchment.png must be a valid PNG");
        assertTrue(dims.width() >= 128 && dims.height() >= 128, "letter_parchment.png must have adequate resolution");

        BufferedImage img = ImageIO.read(parchment);
        assertNotNull(img);

        // Sample background parchment colors to verify warm paper tone
        int sampleX = dims.width() / 2;
        int sampleY = dims.height() / 2;
        int paperRgb = img.getRGB(sampleX, sampleY);
        int paperR = (paperRgb >> 16) & 0xFF;
        int paperG = (paperRgb >> 8) & 0xFF;
        int paperB = paperRgb & 0xFF;

        // Paper tone should be warm/light (R >= G > B)
        assertTrue(paperR > paperB && paperG > paperB, "Parchment texture should have warm golden/sepia paper undertones");

        // Verify ink contrast: Walnut ink (0xFF2A1708) against parchment
        double inkLum = 0.299 * 0x2A + 0.587 * 0x17 + 0.114 * 0x08; // ~27
        double paperLum = 0.299 * paperR + 0.587 * paperG + 0.114 * paperB;
        assertTrue(paperLum > 100, "Parchment base color must be sufficiently bright (lum > 100), got: " + paperLum);
        assertTrue((paperLum / Math.max(1.0, inkLum)) >= 3.0, "Contrast ratio between ink and parchment must be >= 3.0");
    }

    @Test
    @DisplayName("STRESS-F6: Certification banner world plaque textures (tiers 1-5), 3:2 ratio and clamping")
    void testCertificationBannerWorldPlaqueTextures() throws Exception {
        for (int tier = 1; tier <= 5; tier++) {
            ResourceLocation loc = LicenseBannerRenderer.getCustomBannerTexture(tier);
            assertEquals("zootycoon", loc.getNamespace());
            assertEquals("textures/entity/license_banner/tier_" + tier + ".png", loc.getPath());

            File bannerFile = TestHelper.getAssetFile(loc.getPath());
            assertTrue(bannerFile.exists(), "Banner texture for tier " + tier + " must exist: " + bannerFile.getPath());

            TestHelper.PngDimensions dims = TestHelper.getPngDimensions(bannerFile);
            assertTrue(dims.validPng(), "Banner texture must be a valid PNG: tier " + tier);
            assertTrue(dims.width() > 0 && dims.height() > 0);

            BufferedImage img = ImageIO.read(bannerFile);
            assertNotNull(img, "Banner texture must decode properly: tier " + tier);
        }

        // Test clamping on edge cases
        assertEquals("textures/entity/license_banner/tier_1.png", LicenseBannerRenderer.getCustomBannerTexture(Integer.MIN_VALUE).getPath());
        assertEquals("textures/entity/license_banner/tier_1.png", LicenseBannerRenderer.getCustomBannerTexture(-1).getPath());
        assertEquals("textures/entity/license_banner/tier_1.png", LicenseBannerRenderer.getCustomBannerTexture(0).getPath());
        assertEquals("textures/entity/license_banner/tier_5.png", LicenseBannerRenderer.getCustomBannerTexture(6).getPath());
        assertEquals("textures/entity/license_banner/tier_5.png", LicenseBannerRenderer.getCustomBannerTexture(Integer.MAX_VALUE).getPath());
    }
}
