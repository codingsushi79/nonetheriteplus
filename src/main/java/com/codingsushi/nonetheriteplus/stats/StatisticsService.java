package com.codingsushi.nonetheriteplus.stats;

import com.codingsushi.nonetheriteplus.NetheriteMaterials;
import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StatisticsService {

    private final NoNetheritePlus plugin;
    private final ConfigService config;
    private final Map<String, Integer> statistics = new ConcurrentHashMap<>();
    private FileConfiguration statsConfig;
    private File statsFile;

    public StatisticsService(NoNetheritePlus plugin, ConfigService config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void load() {
        statistics.clear();
        if (!config.getBoolean("statistics.enabled", true)) {
            return;
        }

        statsFile = new File(plugin.getDataFolder(), "statistics.yml");
        if (!statsFile.exists()) {
            try {
                if (!statsFile.createNewFile()) {
                    plugin.getLogger().warning("Could not create statistics file");
                    return;
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create statistics file: " + e.getMessage());
                return;
            }
        }

        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        statistics.put("total_refunds", statsConfig.getInt("total_refunds", 0));
        statistics.put("total_blocked_crafts", statsConfig.getInt("total_blocked_crafts", 0));
        statistics.put("total_template_removals", statsConfig.getInt("total_template_removals", 0));
        statistics.put("total_purged_items", statsConfig.getInt("total_purged_items", 0));

        for (Material material : NetheriteMaterials.gearAndTools()) {
            String key = material.name().toLowerCase() + "_blocked";
            statistics.put(key, statsConfig.getInt(key, 0));
        }
    }

    public void save() {
        if (statsConfig == null || !config.getBoolean("statistics.enabled", true)) {
            return;
        }

        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            statsConfig.set(entry.getKey(), entry.getValue());
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save statistics: " + e.getMessage());
        }
    }

    public void increment(String key) {
        increment(key, 1);
    }

    public void increment(String key, int amount) {
        if (!config.getBoolean("statistics.enabled", true)) {
            return;
        }

        if (key.equals("total_purged_items") && !config.getBoolean("statistics.track_purged_items", true)) {
            return;
        }

        statistics.merge(key, amount, Integer::sum);

        if (config.getBoolean("statistics.log_to_file", false)) {
            save();
        }

        if (config.isDebugMode() && config.getBoolean("debug.log_craft_events", false)) {
            plugin.getLogger().info("Statistic incremented: " + key + " = " + statistics.get(key));
        }
    }

    public int get(String key) {
        return statistics.getOrDefault(key, 0);
    }

    public Map<String, Integer> snapshot() {
        return Map.copyOf(statistics);
    }
}
