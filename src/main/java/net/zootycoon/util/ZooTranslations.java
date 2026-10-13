package net.zootycoon.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.zootycoon.ZooTycoonMod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

/**
 * Loads strings directly from assets/zootycoon/lang/id_id.json so that
 * any edit to id_id.json is always respected on both client and server
 * without any hardcoded strings in Java code.
 */
public final class ZooTranslations {
    private static final Map<String, String> TRANSLATIONS = new HashMap<>();
    private static boolean loaded = false;

    private ZooTranslations() {}

    public static synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try (InputStream stream = ZooTranslations.class.getResourceAsStream("/assets/zootycoon/lang/id_id.json")) {
            if (stream != null) {
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> parsed = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), type);
                if (parsed != null) {
                    TRANSLATIONS.putAll(parsed);
                }
            }
        } catch (Exception e) {
            ZooTycoonMod.LOGGER.error("Failed to load id_id.json translations", e);
        }
    }

    public static String get(String key, Object... args) {
        ensureLoaded();
        String raw = TRANSLATIONS.getOrDefault(key, key);
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

