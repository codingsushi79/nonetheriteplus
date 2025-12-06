package com.example.nonetheriteplus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class NoNetheritePlus extends JavaPlugin implements Listener, CommandExecutor {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Create config if it doesn't exist
        saveDefaultConfig();
        config = getConfig();

        // Register the event listener
        getServer().getPluginManager().registerEvents(this, this);

        // Register the command executor
        getCommand("nonetherite").setExecutor(this);

        // Remove disabled netherite recipes
        removeDisabledNetheriteRecipes();

        getLogger().info("NoNetheritePlus plugin enabled!");
    }

    @Override
    public void onDisable() {
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
        if (event.getResult() != null && isDisabledNetheriteItem(event.getResult().getType())) {
            // Cancel the smithing and refund materials if enabled
            event.setResult(null);

            if (!config.getBoolean("refund_materials", true)) {
                return;
            }

            // Get the inventory and refund items
            var inventory = event.getInventory();

            // The smithing table has slots: 0=input1, 1=input2, 2=output
            ItemStack input1 = inventory.getItem(0);
            ItemStack input2 = inventory.getItem(1);

            if (input1 != null && input2 != null) {
                // Check if this was an attempt to make a disabled netherite item
                Material expectedInput = getExpectedInputForNetheriteItem(event.getResult().getType());

                if ((input1.getType() == expectedInput && input2.getType() == Material.NETHERITE_INGOT) ||
                    (input2.getType() == expectedInput && input1.getType() == Material.NETHERITE_INGOT)) {

                    // Drop the items back to the player
                    var player = (org.bukkit.entity.Player) event.getView().getPlayer();
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
            default:
                return null;
        }
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (config.getBoolean("disable_upgrade_templates", false)) {
            // Remove netherite upgrade templates from loot
            event.getLoot().removeIf(itemStack ->
                itemStack.getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE
            );
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nonetherite")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                // Check permission for reload command
                if (!sender.hasPermission("nonetherite.reload")) {
                    sender.sendMessage("§cYou don't have permission to reload the configuration.");
                    return true;
                }

                // Reload configuration
                reloadConfig();
                config = getConfig();

                // Re-remove recipes based on new config
                removeDisabledNetheriteRecipes();

                sender.sendMessage("§aNoNetheritePlus configuration reloaded successfully!");
                return true;
            } else {
                // Show plugin info
                sender.sendMessage("§6=== NoNetheritePlus ===");
                sender.sendMessage("§eVersion: §f" + getDescription().getVersion());
                sender.sendMessage("§eAuthor: §f" + String.join(", ", getDescription().getAuthors()));
                sender.sendMessage("§eDescription: §f" + getDescription().getDescription());
                sender.sendMessage("§7Use §f/nonetherite reload §7to reload configuration (requires permission)");
                return true;
            }
        }
        return false;
    }
}
