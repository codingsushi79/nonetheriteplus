package com.codingsushi.nonetheriteplus;

import com.codingsushi.nonetheriteplus.audit.AuditLog;
import com.codingsushi.nonetheriteplus.command.NoNetheriteCommand;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import com.codingsushi.nonetheriteplus.gui.ConfigGui;
import com.codingsushi.nonetheriteplus.listener.AcquisitionListener;
import com.codingsushi.nonetheriteplus.listener.LootListener;
import com.codingsushi.nonetheriteplus.listener.PossessionListener;
import com.codingsushi.nonetheriteplus.listener.SmithingListener;
import com.codingsushi.nonetheriteplus.placeholder.NoNetheriteExpansion;
import com.codingsushi.nonetheriteplus.policy.RestrictionPolicy;
import com.codingsushi.nonetheriteplus.service.ItemRefundService;
import com.codingsushi.nonetheriteplus.service.LimitsService;
import com.codingsushi.nonetheriteplus.service.PurgeService;
import com.codingsushi.nonetheriteplus.stats.StatisticsService;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

public final class NoNetheritePlus extends JavaPlugin {

    private ConfigService configService;
    private RestrictionPolicy policy;
    private StatisticsService statistics;
    private AuditLog auditLog;
    private PurgeService purgeService;
    private LimitsService limitsService;
    private ItemRefundService refundService;
    private ConfigGui configGui;

    @Override
    public void onEnable() {
        configService = new ConfigService(this);
        configService.load();

        policy = new RestrictionPolicy(configService);
        statistics = new StatisticsService(this, configService);
        statistics.load();
        auditLog = new AuditLog(this, configService);
        refundService = new ItemRefundService(configService);
        limitsService = new LimitsService(this, configService);
        limitsService.load();
        purgeService = new PurgeService(this, configService, policy, statistics, auditLog);
        configGui = new ConfigGui(this, configService, policy);

        getServer().getPluginManager().registerEvents(
            new SmithingListener(this, configService, policy, statistics, auditLog, refundService, limitsService, purgeService),
            this
        );
        getServer().getPluginManager().registerEvents(
            new LootListener(this, configService, policy, statistics, auditLog),
            this
        );
        getServer().getPluginManager().registerEvents(
            new AcquisitionListener(this, configService, policy, auditLog),
            this
        );
        getServer().getPluginManager().registerEvents(
            new PossessionListener(configService, policy, auditLog, purgeService, limitsService),
            this
        );
        getServer().getPluginManager().registerEvents(configGui, this);

        NoNetheriteCommand command = new NoNetheriteCommand(
            this, configService, policy, statistics, auditLog, purgeService, limitsService, configGui
        );
        var pluginCommand = getCommand("nonetherite");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        removeDisabledNetheriteRecipes();
        purgeService.startPeriodicPurge();
        registerPlaceholderApi();

        if (configService.isDebugMode()) {
            getLogger().info("Debug mode enabled");
        }
        getLogger().info("NoNetheritePlus plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (purgeService != null) {
            purgeService.stopPeriodicPurge();
        }
        if (statistics != null) {
            statistics.save();
        }
        getLogger().info("NoNetheritePlus plugin disabled!");
    }

    public void applyRuntimeReload(boolean saveStatsFirst) {
        if (saveStatsFirst && statistics != null) {
            statistics.save();
        }
        configService.reload();
        limitsService.load();
        removeDisabledNetheriteRecipes();
        purgeService.startPeriodicPurge();
    }

    public RestrictionPolicy getPolicy() {
        return policy;
    }

    public StatisticsService getStatistics() {
        return statistics;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    void removeDisabledNetheriteRecipes() {
        Iterator<Recipe> iterator = getServer().recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            ItemStack result = null;

            if (recipe instanceof SmithingTransformRecipe transformRecipe) {
                result = transformRecipe.getResult();
            } else if (recipe instanceof SmithingRecipe smithingRecipe) {
                result = smithingRecipe.getResult();
            }

            if (result != null && policy.isDisabledNetheriteItem(result.getType())) {
                iterator.remove();
                getLogger().info("Removed netherite upgrade recipe for: " + result.getType());
            }
        }
    }

    private void registerPlaceholderApi() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }
        try {
            NoNetheriteExpansion expansion = new NoNetheriteExpansion(this, policy, statistics);
            if (expansion.register()) {
                getLogger().info("Registered PlaceholderAPI expansion");
            }
        } catch (NoClassDefFoundError | Exception e) {
            getLogger().warning("Could not register PlaceholderAPI expansion: " + e.getMessage());
        }
    }
}
