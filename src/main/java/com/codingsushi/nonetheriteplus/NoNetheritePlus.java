package com.codingsushi.nonetheriteplus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NoNetheritePlus extends JavaPlugin implements Listener, CommandExecutor {

    private FileConfiguration config;
    private FileConfiguration statsConfig;
    private File statsFile;

    // Statistics tracking
    private final Map<String, Integer> statistics = new ConcurrentHashMap<>();

    // Cached world configurations for performance
    private final Map<String, Map<String, Object>> worldConfigs = new HashMap<>();

    // Debug mode flag
    private boolean debugMode = false;

    @Override
    public void onEnable() {
        // Create config if it doesn't exist
        saveDefaultConfig();
        config = getConfig();

        // Initialize statistics
        initializeStatistics();

        // Load world configurations
        loadWorldConfigurations();

        // Set debug mode
        debugMode = config.getBoolean("debug.enabled", false);

        // Register the event listener
        getServer().getPluginManager().registerEvents(this, this);

        // Register the command executor
        getCommand("nonetherite").setExecutor(this);

        // Remove disabled netherite recipes
        removeDisabledNetheriteRecipes();

        if (debugMode) {
            getLogger().info("Debug mode enabled");
        }

        getLogger().info("NoNetheritePlus plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Save statistics before shutdown
        saveStatistics();
        getLogger().info("NoNetheritePlus plugin disabled!");
    }

    private void removeDisabledNetheriteRecipes() {
        Iterator<Recipe> iterator = getServer().recipeIterator();

        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();

            if (recipe instanceof SmithingRecipe smithingRecipe) {
                ItemStack result = smithingRecipe.getResult();

                // Check if the result is a disabled netherite item
                if (isDisabledNetheriteItem(result.getType())) {
                    iterator.remove();
                    getLogger().info("Removed netherite recipe for: " + result.getType());
                }
            }
        }
    }

    private boolean isDisabledNetheriteItem(Material material) {
        switch (material) {
            case NETHERITE_HELMET:
                return config.getBoolean("disable_netherite_helmet", true);
            case NETHERITE_CHESTPLATE:
                return config.getBoolean("disable_netherite_chestplate", true);
            case NETHERITE_LEGGINGS:
                return config.getBoolean("disable_netherite_leggings", true);
            case NETHERITE_BOOTS:
                return config.getBoolean("disable_netherite_boots", true);
            case NETHERITE_SWORD:
                return config.getBoolean("disable_netherite_sword", false);
            case NETHERITE_PICKAXE:
                return config.getBoolean("disable_netherite_pickaxe", false);
            case NETHERITE_AXE:
                return config.getBoolean("disable_netherite_axe", false);
            case NETHERITE_SHOVEL:
                return config.getBoolean("disable_netherite_shovel", false);
            case NETHERITE_HOE:
                return config.getBoolean("disable_netherite_hoe", false);
            case NETHERITE_SPEAR:
                return config.getBoolean("disable_netherite_spear", false);
            case NETHERITE_HORSE_ARMOR:
                return config.getBoolean("disable_netherite_horse_armor", true);
            default:
                return false;
        }
    }

    private boolean isNetheriteArmor(Material material) {
        return material == Material.NETHERITE_BOOTS ||
               material == Material.NETHERITE_LEGGINGS ||
               material == Material.NETHERITE_CHESTPLATE ||
               material == Material.NETHERITE_HELMET;
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (event.getResult() != null) {
            Player player = (Player) event.getView().getPlayer();
            Material resultType = event.getResult().getType();

            if (isDisabledNetheriteItem(resultType, player.getWorld())) {
                // Track blocked craft attempt
                incrementStatistic("total_blocked_crafts");
                incrementStatistic(resultType.name().toLowerCase() + "_blocked");

                if (debugMode && config.getBoolean("debug.log_craft_events", false)) {
                    getLogger().info("Blocked " + resultType + " craft attempt by " + player.getName() + " in world " + player.getWorld().getName());
                }

                // Cancel the smithing and refund materials if enabled
                event.setResult(null);

                if (!getWorldConfigValue("refund_materials", player.getWorld(), true)) {
                    return;
                }

                // Get the inventory and refund items
                var inventory = event.getInventory();

                // The smithing table has slots: 0=input1, 1=input2, 2=output
                ItemStack input1 = inventory.getItem(0);
                ItemStack input2 = inventory.getItem(1);

                if (input1 != null && input2 != null) {
                    // Check if this was an attempt to make a disabled netherite item
                    Material expectedInput = getExpectedInputForNetheriteItem(resultType);

                    if ((input1.getType() == expectedInput && input2.getType() == Material.NETHERITE_INGOT) ||
                        (input2.getType() == expectedInput && input1.getType() == Material.NETHERITE_INGOT)) {

                        // Track refund
                        incrementStatistic("total_refunds");

                        // Drop the items back to the player
                        var location = player.getLocation();

                        // Refund the base item (diamond tool/armor)
                        ItemStack baseItem = input1.getType() == expectedInput ? input1 : input2;
                        player.getWorld().dropItemNaturally(location, baseItem.clone());

                        // Refund the netherite ingot
                        player.getWorld().dropItemNaturally(location, new ItemStack(Material.NETHERITE_INGOT, 1));

                        // Clear the input slots
                        inventory.setItem(0, null);
                        inventory.setItem(1, null);

                        String message = config.getString("refund_message", "&cNetherite crafting is disabled for this item. Your materials have been refunded.");
                        player.sendMessage(message.replace("&", "§"));
                    }
                }
            }
        }
    }

    private Material getExpectedInputForNetheriteItem(Material netheriteItem) {
        switch (netheriteItem) {
            case NETHERITE_HELMET:
                return Material.DIAMOND_HELMET;
            case NETHERITE_CHESTPLATE:
                return Material.DIAMOND_CHESTPLATE;
            case NETHERITE_LEGGINGS:
                return Material.DIAMOND_LEGGINGS;
            case NETHERITE_BOOTS:
                return Material.DIAMOND_BOOTS;
            case NETHERITE_SWORD:
                return Material.DIAMOND_SWORD;
            case NETHERITE_PICKAXE:
                return Material.DIAMOND_PICKAXE;
            case NETHERITE_AXE:
                return Material.DIAMOND_AXE;
            case NETHERITE_SHOVEL:
                return Material.DIAMOND_SHOVEL;
            case NETHERITE_HOE:
                return Material.DIAMOND_HOE;
            case NETHERITE_HORSE_ARMOR:
                return Material.DIAMOND_HORSE_ARMOR;
            default:
                return null;
        }
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (getWorldConfigValue("disable_upgrade_templates", event.getWorld(), false)) {
            int templatesRemoved = 0;
            // Remove netherite upgrade templates from loot
            for (Iterator<ItemStack> iterator = event.getLoot().iterator(); iterator.hasNext();) {
                ItemStack item = iterator.next();
                if (item.getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
                    iterator.remove();
                    templatesRemoved++;
                }
            }

            if (templatesRemoved > 0) {
                incrementStatistic("total_template_removals");

                if (debugMode && config.getBoolean("debug.log_loot_events", false)) {
                    getLogger().info("Removed " + templatesRemoved + " netherite upgrade templates from loot in world " + event.getWorld().getName());
                }
            }
        }
    }

    private void initializeStatistics() {
        if (!config.getBoolean("statistics.enabled", true)) {
            return;
        }

        statsFile = new File(getDataFolder(), "statistics.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Could not create statistics file: " + e.getMessage());
                return;
            }
        }

        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        // Load existing statistics or initialize defaults
        statistics.put("total_refunds", statsConfig.getInt("total_refunds", 0));
        statistics.put("total_blocked_crafts", statsConfig.getInt("total_blocked_crafts", 0));
        statistics.put("total_template_removals", statsConfig.getInt("total_template_removals", 0));

        // Load item-specific statistics
        for (Material material : Arrays.asList(
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS, Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE,
            Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE,
            Material.NETHERITE_SPEAR, Material.NETHERITE_HORSE_ARMOR)) {
            String key = material.name().toLowerCase() + "_blocked";
            statistics.put(key, statsConfig.getInt(key, 0));
        }
    }

    private void saveStatistics() {
        if (statsConfig == null || !config.getBoolean("statistics.enabled", true)) {
            return;
        }

        // Save statistics to config
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            statsConfig.set(entry.getKey(), entry.getValue());
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            getLogger().warning("Could not save statistics: " + e.getMessage());
        }
    }

    private void incrementStatistic(String key) {
        if (!config.getBoolean("statistics.enabled", true)) {
            return;
        }

        statistics.merge(key, 1, Integer::sum);

        if (config.getBoolean("statistics.log_to_file", false)) {
            saveStatistics();
        }

        if (debugMode && config.getBoolean("debug.log_craft_events", false)) {
            getLogger().info("Statistic incremented: " + key + " = " + statistics.get(key));
        }
    }

    private void loadWorldConfigurations() {
        if (!config.contains("worlds")) {
            return;
        }

        for (String worldName : config.getConfigurationSection("worlds").getKeys(false)) {
            Map<String, Object> worldConfig = new HashMap<>();
            for (String key : config.getConfigurationSection("worlds." + worldName).getKeys(true)) {
                worldConfig.put(key, config.get("worlds." + worldName + "." + key));
            }
            worldConfigs.put(worldName, worldConfig);
        }

        if (debugMode) {
            getLogger().info("Loaded configurations for " + worldConfigs.size() + " worlds");
        }
    }

    private boolean isDisabledNetheriteItem(Material material, World world) {
        // Check world-specific config first
        String worldName = world.getName();
        if (worldConfigs.containsKey(worldName)) {
            Map<String, Object> worldConfig = worldConfigs.get(worldName);
            String configKey = "disable_" + material.name().toLowerCase();
            if (worldConfig.containsKey(configKey)) {
                return (Boolean) worldConfig.get(configKey);
            }
        }

        // Fall back to global config

        // Fall back to global config
        return isDisabledNetheriteItem(material);
    }

    private boolean getWorldConfigValue(String key, World world, boolean defaultValue) {
        // Check world-specific config first
        String worldName = world.getName();
        if (worldConfigs.containsKey(worldName)) {
            Map<String, Object> worldConfig = worldConfigs.get(worldName);
            if (worldConfig.containsKey(key)) {
                return (Boolean) worldConfig.get(key);
            }
        }

        // Fall back to global config

        // Fall back to global config
        return config.getBoolean(key, defaultValue);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nonetherite")) {
            if (args.length == 0) {
                // Show plugin info
                sender.sendMessage("§6=== No Netherite+ ===");
                sender.sendMessage("§eVersion: §f" + getDescription().getVersion());
                sender.sendMessage("§eAuthor: §f" + String.join(", ", getDescription().getAuthors()));
                sender.sendMessage("§eDescription: §f" + getDescription().getDescription());
                sender.sendMessage("§7Commands: §freload§7, §fstats§7, §fdebug§7, §fpreset");
                return true;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "reload":
                    return handleReloadCommand(sender);
                case "stats":
                case "statistics":
                    return handleStatsCommand(sender);
                case "debug":
                    return handleDebugCommand(sender, args);
                case "preset":
                    return handlePresetCommand(sender, args);
                default:
                    sender.sendMessage("§cUnknown subcommand. Use §f/nonetherite §cfor help.");
                    return true;
            }
        }
        return false;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("nonetherite.reload")) {
            sender.sendMessage("§cYou don't have permission to reload the configuration.");
            return true;
        }

        // Save statistics before reload
        saveStatistics();

        // Reload configuration
        reloadConfig();
        config = getConfig();

        // Reload world configurations
        loadWorldConfigurations();

        // Update debug mode
        debugMode = config.getBoolean("debug.enabled", false);

        // Re-remove recipes based on new config
        removeDisabledNetheriteRecipes();

        sender.sendMessage("§aNoNetheritePlus configuration reloaded successfully!");
        return true;
    }

    private boolean handleStatsCommand(CommandSender sender) {
        if (!sender.hasPermission("nonetherite.stats")) {
            sender.sendMessage("§cYou don't have permission to view statistics.");
            return true;
        }

        if (!config.getBoolean("statistics.enabled", true)) {
            sender.sendMessage("§cStatistics tracking is disabled.");
            return true;
        }

        sender.sendMessage("§6=== NoNetheritePlus Statistics ===");
        sender.sendMessage("§eTotal Refunds: §f" + statistics.getOrDefault("total_refunds", 0));
        sender.sendMessage("§eTotal Blocked Crafts: §f" + statistics.getOrDefault("total_blocked_crafts", 0));
        sender.sendMessage("§eTemplates Removed: §f" + statistics.getOrDefault("total_template_removals", 0));

        sender.sendMessage("§7--- Item Breakdown ---");
        for (Material material : Arrays.asList(
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS, Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE,
            Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE,
            Material.NETHERITE_SPEAR, Material.NETHERITE_HORSE_ARMOR)) {
            String key = material.name().toLowerCase() + "_blocked";
            int count = statistics.getOrDefault(key, 0);
            if (count > 0) {
                sender.sendMessage("§7" + material.name().replace("NETHERITE_", "") + ": §f" + count);
            }
        }

        return true;
    }

    private boolean handleDebugCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nonetherite.debug")) {
            sender.sendMessage("§cYou don't have permission to toggle debug mode.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§eDebug mode is currently: §f" + (debugMode ? "ON" : "OFF"));
            sender.sendMessage("§7Use §f/nonetherite debug <on|off> §7to toggle");
            return true;
        }

        String toggle = args[1].toLowerCase();
        if (toggle.equals("on")) {
            debugMode = true;
            config.set("debug.enabled", true);
            saveConfig();
            sender.sendMessage("§aDebug mode enabled");
            getLogger().info("Debug mode enabled by " + sender.getName());
        } else if (toggle.equals("off")) {
            debugMode = false;
            config.set("debug.enabled", false);
            saveConfig();
            sender.sendMessage("§aDebug mode disabled");
            getLogger().info("Debug mode disabled by " + sender.getName());
        } else {
            sender.sendMessage("§cUse §f/nonetherite debug <on|off>");
        }

        return true;
    }

    private boolean handlePresetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nonetherite.preset")) {
            sender.sendMessage("§cYou don't have permission to manage presets.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§7Usage: §f/nonetherite preset <save|load|list|delete> [name]");
            return true;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "save":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: §f/nonetherite preset save <name>");
                    return true;
                }
                return handlePresetSave(sender, args[2]);

            case "load":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: §f/nonetherite preset load <name>");
                    return true;
                }
                return handlePresetLoad(sender, args[2]);

            case "list":
                return handlePresetList(sender);

            case "delete":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: §f/nonetherite preset delete <name>");
                    return true;
                }
                return handlePresetDelete(sender, args[2]);

            default:
                sender.sendMessage("§cUnknown preset action. Use save, load, list, or delete.");
                return true;
        }
    }

    private boolean handlePresetSave(CommandSender sender, String name) {
        // Save current config as preset
        Map<String, Object> presetData = new HashMap<>();
        for (String key : config.getKeys(true)) {
            if (!key.startsWith("presets")) {
                presetData.put(key, config.get(key));
            }
        }

        config.set("presets." + name, presetData);
        saveConfig();

        sender.sendMessage("§aPreset '" + name + "' saved successfully!");
        return true;
    }

    private boolean handlePresetLoad(CommandSender sender, String name) {
        if (!config.contains("presets." + name)) {
            sender.sendMessage("§cPreset '" + name + "' does not exist.");
            return true;
        }

        // Load preset data
        Map<String, Object> presetData = (Map<String, Object>) config.get("presets." + name);

        // Apply preset data (excluding presets section itself)
        for (Map.Entry<String, Object> entry : presetData.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }

        saveConfig();

        // Reload configurations
        loadWorldConfigurations();
        debugMode = config.getBoolean("debug.enabled", false);
        removeDisabledNetheriteRecipes();

        sender.sendMessage("§aPreset '" + name + "' loaded successfully!");
        sender.sendMessage("§7Use §f/nonetherite reload §7to apply changes.");
        return true;
    }

    private boolean handlePresetList(CommandSender sender) {
        if (!config.contains("presets")) {
            sender.sendMessage("§7No presets saved yet.");
            return true;
        }

        sender.sendMessage("§6=== Available Presets ===");
        for (String presetName : config.getConfigurationSection("presets").getKeys(false)) {
            sender.sendMessage("§e- §f" + presetName);
        }
        return true;
    }

    private boolean handlePresetDelete(CommandSender sender, String name) {
        if (!config.contains("presets." + name)) {
            sender.sendMessage("§cPreset '" + name + "' does not exist.");
            return true;
        }

        config.set("presets." + name, null);
        saveConfig();

        sender.sendMessage("§aPreset '" + name + "' deleted successfully!");
        return true;
    }
}
