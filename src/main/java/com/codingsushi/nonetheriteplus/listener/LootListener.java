package com.codingsushi.nonetheriteplus.listener;

import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.audit.AuditLog;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import com.codingsushi.nonetheriteplus.policy.RestrictionPolicy;
import com.codingsushi.nonetheriteplus.stats.StatisticsService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public final class LootListener implements Listener {

    private final NoNetheritePlus plugin;
    private final ConfigService config;
    private final RestrictionPolicy policy;
    private final StatisticsService stats;
    private final AuditLog audit;

    public LootListener(
        NoNetheritePlus plugin,
        ConfigService config,
        RestrictionPolicy policy,
        StatisticsService stats,
        AuditLog audit
    ) {
        this.plugin = plugin;
        this.config = config;
        this.policy = policy;
        this.stats = stats;
        this.audit = audit;
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (!policy.disableUpgradeTemplates(event.getWorld())) {
            return;
        }

        if (event.getEntity() instanceof Player player && policy.hasBypass(player)) {
            return;
        }

        int templatesRemoved = 0;
        for (Iterator<ItemStack> iterator = event.getLoot().iterator(); iterator.hasNext();) {
            ItemStack item = iterator.next();
            if (item.getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
                iterator.remove();
                templatesRemoved++;
            }
        }

        if (templatesRemoved > 0) {
            stats.increment("total_template_removals", templatesRemoved);
            audit.log("TEMPLATE_REMOVED", "world=" + event.getWorld().getName() + " count=" + templatesRemoved);
            if (config.isDebugMode() && config.getBoolean("debug.log_loot_events", false)) {
                plugin.getLogger().info("Removed " + templatesRemoved
                    + " netherite upgrade templates from loot in world " + event.getWorld().getName());
            }
        }
    }
}
