package net.zootycoon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Common test helper providing Minecraft bootstrap, JSON loading,
 * image verification, and layout calculation utilities for E2E tests.
 */
public class TestHelper {

    private static boolean bootstrapped = false;
    private static Map<String, String> cachedIdLang = null;
    private static Map<String, String> cachedEnLang = null;
    private static final Gson GSON = new Gson();

    public static synchronized void ensureMinecraftBootstrapped() {
        if (!bootstrapped) {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
            unfreezeRegistry(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE);
            unfreezeRegistry(net.minecraft.core.registries.BuiltInRegistries.ITEM);
            unfreezeRegistry(net.minecraft.core.registries.BuiltInRegistries.BLOCK);
            bootstrapped = true;
        }
    }

    private static void unfreezeRegistry(net.minecraft.core.Registry<?> registry) {
        try {
            Class<?> clazz = registry.getClass();
            while (clazz != null && clazz != Object.class) {
                try {
                    java.lang.reflect.Field frozenField = clazz.getDeclaredField("frozen");
                    frozenField.setAccessible(true);
                    frozenField.set(registry, false);
                } catch (NoSuchFieldException ignored) {}

                try {
                    java.lang.reflect.Field intrusiveField = clazz.getDeclaredField("unregisteredIntrusiveHolders");
                    intrusiveField.setAccessible(true);
                    intrusiveField.set(registry, new java.util.IdentityHashMap<>());
                } catch (NoSuchFieldException ignored) {}

                try {
                    java.lang.reflect.Field intrusiveField = clazz.getDeclaredField("intrusiveHolderCache");
                    intrusiveField.setAccessible(true);
                    intrusiveField.set(registry, new java.util.IdentityHashMap<>());
                } catch (NoSuchFieldException ignored) {}

                clazz = clazz.getSuperclass();
            }
        } catch (Exception ignored) {}
    }

    public static synchronized Map<String, String> getIdLang() {
        if (cachedIdLang == null) {
            cachedIdLang = loadLangFile("src/main/resources/assets/zootycoon/lang/id_id.json");
        }
        return cachedIdLang;
    }

    public static synchronized Map<String, String> getEnLang() {
        if (cachedEnLang == null) {
            cachedEnLang = loadLangFile("src/main/resources/assets/zootycoon/lang/en_us.json");
        }
        return cachedEnLang;
    }

    private static Map<String, String> loadLangFile(String relativePath) {
        try {
            File f = new File(relativePath);
            if (!f.exists()) {
                f = new File(System.getProperty("user.dir"), relativePath);
            }
            try (InputStream is = new FileInputStream(f);
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                return GSON.fromJson(reader, type);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load lang file: " + relativePath, e);
        }
    }

    public static File getAssetFile(String relativePath) {
        File f = new File("src/main/resources/assets/zootycoon/" + relativePath);
        if (!f.exists()) {
            f = new File(System.getProperty("user.dir"), "src/main/resources/assets/zootycoon/" + relativePath);
        }
        return f;
    }

    /**
     * Checks if a file is a valid PNG and extracts width and height from IHDR.
     */
    public static PngDimensions getPngDimensions(File file) {
        if (!file.exists() || !file.isFile()) {
            return new PngDimensions(false, 0, 0);
        }
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            if (bytes.length < 24) {
                return new PngDimensions(false, 0, 0);
            }
            // Check PNG signature: 89 50 4E 47 0D 0A 1A 0A
            if ((bytes[0] & 0xFF) != 0x89 ||
                bytes[1] != 'P' || bytes[2] != 'N' || bytes[3] != 'G' ||
                bytes[4] != 0x0D || bytes[5] != 0x0A || bytes[6] != 0x1A || bytes[7] != 0x0A) {
                return new PngDimensions(false, 0, 0);
            }
            // IHDR width at offset 16 (4 bytes big-endian)
            int width = ((bytes[16] & 0xFF) << 24) | ((bytes[17] & 0xFF) << 16) |
                        ((bytes[18] & 0xFF) << 8)  | (bytes[19] & 0xFF);
            // IHDR height at offset 20 (4 bytes big-endian)
            int height = ((bytes[20] & 0xFF) << 24) | ((bytes[21] & 0xFF) << 16) |
                         ((bytes[22] & 0xFF) << 8)  | (bytes[23] & 0xFF);
            return new PngDimensions(true, width, height);
        } catch (Exception e) {
            return new PngDimensions(false, 0, 0);
        }
    }

    public static boolean containsEmoji(String s) {
        if (s == null) return false;
        int len = s.length();
        for (int i = 0; i < len; ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (cp >= 0x1F000 && cp <= 0x1FAFF) return true;
            if (cp >= 0x2600 && cp <= 0x27BF) return true;
            if (cp >= 0x2B50 && cp <= 0x2B55) return true;
        }
        return false;
    }

    public record PngDimensions(boolean validPng, int width, int height) {}

    /**
     * Layout calculation simulator for AnimalManagementTab across viewport dimensions.
     */
    public static LayoutResult calculateAnimalManagementLayout(int screenWidth, int screenHeight) {
        int sidebarX = 14;
        int sidebarW = (screenWidth < 540) ? 130 : 160;
        int sidebarY = 48;
        int sidebarH = screenHeight - sidebarY - 14;

        int contentX = sidebarX + sidebarW + 16;
        int contentY = 48;
        int contentW = screenWidth - contentX - 14;
        int contentH = screenHeight - contentY - 14;

        int previewX = contentX + 14;
        int previewW = (screenWidth < 540) ? 100 : 140;
        int previewH = (screenHeight < 300) ? 100 : 140;

        int infoX = previewX + previewW + 16;
        int infoW = contentW - (infoX - contentX) - 14;

        // Button layout logic
        int removeBtnW = 100;
        int sellBtnW = 126;
        boolean stacked = infoW < 240;

        int removeBtnX = infoX;
        int removeBtnY = contentY + 65;

        int sellBtnX;
        int sellBtnY;
        int effectiveSellBtnW;

        if (stacked) {
            sellBtnX = infoX;
            sellBtnY = contentY + 87;
            effectiveSellBtnW = Math.min(sellBtnW, infoW);
        } else {
            sellBtnX = infoX + removeBtnW + 8;
            sellBtnY = contentY + 65;
            effectiveSellBtnW = sellBtnW;
        }

        // Stat text position (relative to infoX)
        int statTextX = infoX + Math.min(106, Math.max(70, infoW - 35));

        return new LayoutResult(
                sidebarX, sidebarW, sidebarY, sidebarH,
                contentX, contentW, contentY, contentH,
                previewX, previewW, previewH,
                infoX, infoW,
                removeBtnX, removeBtnY, removeBtnW,
                sellBtnX, sellBtnY, effectiveSellBtnW,
                statTextX, stacked
        );
    }

    public record LayoutResult(
            int sidebarX, int sidebarW, int sidebarY, int sidebarH,
            int contentX, int contentW, int contentY, int contentH,
            int previewX, int previewW, int previewH,
            int infoX, int infoW,
            int removeBtnX, int removeBtnY, int removeBtnW,
            int sellBtnX, int sellBtnY, int sellBtnW,
            int statTextX, boolean buttonsStacked
    ) {}
}
