package net.zootycoon.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.zootycoon.ZooTycoonMod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

/**
 * Loads strings directly from assets/zootycoon/lang so that
 * any edit to lang files is always respected on both client and server
 * without any hardcoded strings in Java code.
 */
public final class ZooTranslations {
    private static final Map<String, String> TRANSLATIONS = new HashMap<>();
    private static boolean loaded = false;

    private ZooTranslations() {}

    private static void loadLangFile(String resourcePath) {
        String cleanPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        InputStream stream = ZooTranslations.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            stream = ZooTranslations.class.getClassLoader().getResourceAsStream(cleanPath);
        }
        if (stream == null) {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null) {
                stream = ccl.getResourceAsStream(cleanPath);
            }
        }
        try {
            if (stream != null) {
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        if (entry.getValue().isJsonPrimitive()) {
                            TRANSLATIONS.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                }
            } else {
                ZooTycoonMod.LOGGER.warn("Could not find lang file resource: " + resourcePath);
            }
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to load translations from " + resourcePath, e);
        }
    }

    public static synchronized void ensureLoaded() {
        if (loaded && !TRANSLATIONS.isEmpty()) return;
        loadLangFile("/assets/zootycoon/lang/en_us.json");
        loadLangFile("/assets/zootycoon/lang/id_id.json");
        loaded = !TRANSLATIONS.isEmpty();
    }

    public static boolean has(String key) {
        if (key == null) return false;
        ensureLoaded();
        if (TRANSLATIONS.containsKey(key)) return true;
        try {
            return net.minecraft.locale.Language.getInstance().has(key);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static String get(String key, Object... args) {
        ensureLoaded();
        String raw = TRANSLATIONS.getOrDefault(key, key);
        if (raw.equals(key)) {
            try {
                if (net.minecraft.locale.Language.getInstance().has(key)) {
                    raw = net.minecraft.locale.Language.getInstance().getOrDefault(key);
                }
            } catch (Throwable ignored) {}
        }
        if (raw.equals(key)) {
            try {
                String translated = Component.translatable(key).getString();
                if (!translated.equals(key)) {
                    raw = translated;
                }
            } catch (Throwable ignored) {}
        }
        if (args == null || args.length == 0) {
            return raw.replace("%%", "%");
        }
        try {
            return String.format(raw, args);
        } catch (IllegalFormatException e) {
            return raw;
        }
    }

    public static MutableComponent component(String key, Object... args) {
        return Component.literal(get(key, args));
    }
}

