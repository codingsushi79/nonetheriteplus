package com.codingsushi.nonetheriteplus.config;

import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ConfigService {

    private final NoNetheritePlus plugin;
    private FileConfiguration config;
    private final Map<String, Map<String, Object>> worldConfigs = new HashMap<>();
    private boolean debugMode;

    public ConfigService(NoNetheritePlus plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        worldConfigs.clear();
        loadWorldConfigurations();
        debugMode = config.getBoolean("debug.enabled", false);
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        worldConfigs.clear();
        loadWorldConfigurations();
        debugMode = config.getBoolean("debug.enabled", false);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        plugin.saveConfig();
        config = plugin.getConfig();
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean enabled) {
        debugMode = enabled;
        config.set("debug.enabled", enabled);
        save();
    }

    public String colorize(String message) {
        if (message == null) {
            return "";
        }
        return message.replace('&', '§');
    }

    public String message(String key, String defaultValue) {
        return colorize(config.getString(key, defaultValue));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    public boolean getWorldBoolean(String key, World world, boolean defaultValue) {
        Boolean worldValue = getWorldBooleanOrNull(key, world);
        if (worldValue != null) {
            return worldValue;
        }
        return config.getBoolean(key, defaultValue);
    }

    public Boolean getWorldBooleanOrNull(String key, World world) {
        if (world == null) {
            return null;
        }
        Map<String, Object> worldConfig = worldConfigs.get(world.getName());
        if (worldConfig == null || !worldConfig.containsKey(key)) {
            return null;
        }
        Object value = worldConfig.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return null;
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public Set<String> presetNames() {
        ConfigurationSection section = config.getConfigurationSection("presets");
        if (section == null) {
            return Collections.emptySet();
        }
        return section.getKeys(false);
    }

    public void savePreset(String name) {
        Map<String, Object> presetData = new HashMap<>();
        for (String key : config.getKeys(true)) {
            if (!key.startsWith("presets")) {
                presetData.put(key, config.get(key));
            }
        }
        config.set("presets." + name, presetData);
        save();
    }

    public boolean loadPreset(String name) {
        if (!config.contains("presets." + name)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> presetData = (Map<String, Object>) config.get("presets." + name);
        if (presetData == null) {
            return false;
        }
        for (Map.Entry<String, Object> entry : presetData.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        save();
        worldConfigs.clear();
        loadWorldConfigurations();
        debugMode = config.getBoolean("debug.enabled", false);
        return true;
    }

    public boolean deletePreset(String name) {
        if (!config.contains("presets." + name)) {
            return false;
        }
        config.set("presets." + name, null);
        save();
        return true;
    }

    private void loadWorldConfigurations() {
        ConfigurationSection worlds = config.getConfigurationSection("worlds");
        if (worlds == null) {
            return;
        }

        for (String worldName : worlds.getKeys(false)) {
            ConfigurationSection worldSection = worlds.getConfigurationSection(worldName);
            if (worldSection == null) {
                continue;
            }
            Map<String, Object> worldConfig = new HashMap<>();
            for (String key : worldSection.getKeys(true)) {
                worldConfig.put(key, worldSection.get(key));
            }
            worldConfigs.put(worldName, worldConfig);
        }

        if (debugMode) {
            plugin.getLogger().info("Loaded configurations for " + worldConfigs.size() + " worlds");
        }
    }
}
