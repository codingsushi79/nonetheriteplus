package com.codingsushi.nonetheriteplus.service;

import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public final class LimitsService {

    private final NoNetheritePlus plugin;
    private final ConfigService config;
    private FileConfiguration limitsConfig;
    private File limitsFile;

    public LimitsService(NoNetheritePlus plugin, ConfigService config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void load() {
        limitsFile = new File(plugin.getDataFolder(), "limits.yml");
        if (!limitsFile.exists()) {
            try {
                plugin.saveResource("limits.yml", false);
            } catch (IllegalArgumentException ignored) {
                try {
                    if (!limitsFile.createNewFile()) {
                        plugin.getLogger().warning("Could not create limits.yml");
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Could not create limits.yml: " + e.getMessage());
                }
            }
        }
        limitsConfig = YamlConfiguration.loadConfiguration(limitsFile);

        // Prefer limits.yml, fall back to config.yml limits section
        if (!limitsConfig.contains("limits.enabled") && config.getConfig().contains("limits")) {
            limitsConfig.set("limits.enabled", config.getBoolean("limits.enabled", false));
            limitsConfig.set("limits.max_netherite_items", config.getConfig().getInt("limits.max_netherite_items", 0));
            save();
        }
    }

    public void save() {
        if (limitsConfig == null || limitsFile == null) {
            return;
        }
        try {
            limitsConfig.save(limitsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save limits.yml: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return limitsConfig != null && limitsConfig.getBoolean("limits.enabled", false);
    }

    public int maxNetheriteItems() {
        if (limitsConfig == null) {
            return 0;
        }
        return Math.max(0, limitsConfig.getInt("limits.max_netherite_items", 0));
    }

    public boolean wouldExceedLimit(Player player, int currentCount, int adding) {
        if (!isEnabled()) {
            return false;
        }
        int max = maxNetheriteItems();
        if (max <= 0) {
            return false;
        }
        return currentCount + adding > max;
    }
}
