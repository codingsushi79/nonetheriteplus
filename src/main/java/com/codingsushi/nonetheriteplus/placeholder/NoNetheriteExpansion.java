package com.codingsushi.nonetheriteplus.placeholder;

import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.policy.RestrictionPolicy;
import com.codingsushi.nonetheriteplus.stats.StatisticsService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NoNetheriteExpansion extends PlaceholderExpansion {

    private final NoNetheritePlus plugin;
    private final RestrictionPolicy policy;
    private final StatisticsService stats;

    public NoNetheriteExpansion(NoNetheritePlus plugin, RestrictionPolicy policy, StatisticsService stats) {
        this.plugin = plugin;
        this.policy = policy;
        this.stats = stats;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "nonetherite";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("version")) {
            return plugin.getPluginMeta().getVersion();
        }
        if (params.equalsIgnoreCase("blocked_crafts")) {
            return String.valueOf(stats.get("total_blocked_crafts"));
        }
        if (params.toLowerCase().startsWith("disabled_")) {
            String item = params.substring("disabled_".length()).toUpperCase();
            if (!item.startsWith("NETHERITE_")) {
                item = "NETHERITE_" + item;
            }
            try {
                Material material = Material.valueOf(item);
                return String.valueOf(policy.isDisabledNetheriteItem(material));
            } catch (IllegalArgumentException e) {
                return "false";
            }
        }
        return null;
    }
}
