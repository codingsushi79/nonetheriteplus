package com.codingsushi.nonetheriteplus.common;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Minimal dotted-path YAML config, mirroring the subset of Bukkit's
 * FileConfiguration API used by the original plugin (get/set/contains/getKeys
 * with dotted paths, nested sections as maps).
 */
public class YamlConfig {

    private static final Yaml YAML;

    static {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        YAML = new Yaml(options);
    }

    private Map<String, Object> root = new LinkedHashMap<>();

    public static YamlConfig loadFromResource(String resourcePath, ClassLoader classLoader) {
        YamlConfig config = new YamlConfig();
        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            if (in != null) {
                config.loadFrom(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
            // fall through with empty config
        }
        return config;
    }

    public static YamlConfig loadFromFile(Path path) {
        YamlConfig config = new YamlConfig();
        if (Files.exists(path)) {
            try (InputStream in = new FileInputStream(path.toFile())) {
                config.loadFrom(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (IOException ignored) {
                // fall through with empty config
            }
        }
        return config;
    }

    @SuppressWarnings("unchecked")
    private void loadFrom(InputStreamReader reader) {
        Object loaded = YAML.load(reader);
        if (loaded instanceof Map) {
            root = (Map<String, Object>) loaded;
        }
    }

    public void save(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
            YAML.dump(root, writer);
        }
    }

    public boolean contains(String path) {
        return resolve(path, false) != null;
    }

    public Object get(String path) {
        Map<String, Object> parent = resolve(path, false);
        if (parent == null) {
            return null;
        }
        return parent.get(lastKey(path));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSection(String path) {
        Object value = get(path);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    public void set(String path, Object value) {
        Map<String, Object> parent = resolve(path, true);
        String key = lastKey(path);
        if (value == null) {
            parent.remove(key);
        } else {
            parent.put(key, value);
        }
    }

    public boolean getBoolean(String path, boolean def) {
        Object value = get(path);
        return value instanceof Boolean ? (Boolean) value : def;
    }

    public long getLong(String path, long def) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).longValue() : def;
    }

    public int getInt(String path, int def) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).intValue() : def;
    }

    public String getString(String path, String def) {
        Object value = get(path);
        return value != null ? String.valueOf(value) : def;
    }

    /** Immediate (deep=false) or all nested (deep=true) keys under path, dotted-path style. */
    public Set<String> getKeys(String path, boolean deep) {
        Set<String> keys = new LinkedHashSet<>();
        Map<String, Object> section = path.isEmpty() ? root : getSection(path);
        if (section == null) {
            return keys;
        }
        collectKeys(section, "", deep, keys);
        return keys;
    }

    @SuppressWarnings("unchecked")
    private void collectKeys(Map<String, Object> section, String prefix, boolean deep, Set<String> keys) {
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            keys.add(key);
            if (deep && entry.getValue() instanceof Map) {
                collectKeys((Map<String, Object>) entry.getValue(), key, true, keys);
            }
        }
    }

    /** Flattened dotted-path -> value map for everything under path (deep leaves only), used for presets. */
    public Map<String, Object> flatten(String path) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> section = path.isEmpty() ? root : getSection(path);
        if (section == null) {
            return result;
        }
        flattenInto(section, "", result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private void flattenInto(Map<String, Object> section, String prefix, Map<String, Object> out) {
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenInto((Map<String, Object>) entry.getValue(), key, out);
            } else {
                out.put(key, entry.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolve(String path, boolean create) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else if (create) {
                Map<String, Object> newSection = new LinkedHashMap<>();
                current.put(parts[i], newSection);
                current = newSection;
            } else {
                return null;
            }
        }
        return current;
    }

    private String lastKey(String path) {
        int idx = path.lastIndexOf('.');
        return idx < 0 ? path : path.substring(idx + 1);
    }
}
