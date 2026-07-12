package com.codingsushi.nonetheriteplus.service;

import com.codingsushi.nonetheriteplus.NetheriteMaterials;
import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.audit.AuditLog;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import com.codingsushi.nonetheriteplus.policy.RestrictionPolicy;
import com.codingsushi.nonetheriteplus.stats.StatisticsService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class PurgeService {

    private final NoNetheritePlus plugin;
    private final ConfigService config;
    private final RestrictionPolicy policy;
    private final StatisticsService stats;
    private final AuditLog audit;
    private BukkitTask purgeTask;

    public PurgeService(
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

    public void startPeriodicPurge() {
        stopPeriodicPurge();

        if (!config.getBoolean("periodic_purge.enabled", false)) {
            return;
        }

        long intervalSeconds = Math.max(1, config.getConfig().getLong("periodic_purge.interval_seconds", 60));
        long intervalTicks = intervalSeconds * 20L;

        purgeTask = Bukkit.getScheduler().runTaskTimer(plugin, this::purgeNetheriteFromAllPlayers, intervalTicks, intervalTicks);

        if (config.isDebugMode()) {
            plugin.getLogger().info("Periodic netherite purge enabled (every " + intervalSeconds + " seconds)");
        }
    }

    public void stopPeriodicPurge() {
        if (purgeTask != null) {
            purgeTask.cancel();
            purgeTask = null;
        }
    }

    public void purgeNetheriteFromAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (policy.hasBypass(player)) {
                continue;
            }
            int removed = removeMatchingFromPlayer(player, NetheriteMaterials::isNetherite, true);
            if (removed > 0) {
                stats.increment("total_purged_items", removed);
                audit.log("PURGE", player.getName() + " removed=" + removed);
                if (config.isDebugMode()) {
                    plugin.getLogger().info("Removed " + removed + " netherite item(s) from " + player.getName());
                }
            }
        }
    }

    public int stripDisabledFromPlayer(Player player) {
        if (policy.hasBypass(player)) {
            return 0;
        }

        Predicate<Material> matcher = material ->
            NetheriteMaterials.isGearOrTool(material) && policy.isDisabledNetheriteItem(material, player.getWorld());

        int removed = removeMatchingFromPlayer(player, matcher, policy.convertOnStrip());
        if (removed > 0) {
            stats.increment("total_purged_items", removed);
            audit.log("JOIN_STRIP", player.getName() + " removed=" + removed);
        }
        return removed;
    }

    public int countNetheriteGear(Player player) {
        int count = 0;
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && NetheriteMaterials.isGearOrTool(item.getType())) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public Map<Material, Integer> countByMaterial(Player player) {
        Map<Material, Integer> counts = new HashMap<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !NetheriteMaterials.isNetherite(item.getType())) {
                continue;
            }
            counts.merge(item.getType(), item.getAmount(), Integer::sum);
        }
        return counts;
    }

    private int removeMatchingFromPlayer(Player player, Predicate<Material> matcher, boolean convert) {
        int removed = 0;
        PlayerInventory inventory = player.getInventory();

        ItemStack[] storage = inventory.getStorageContents();
        removed += clearMatching(storage, matcher, convert, player);
        inventory.setStorageContents(storage);

        ItemStack[] armor = inventory.getArmorContents();
        removed += clearMatching(armor, matcher, convert, player);
        inventory.setArmorContents(armor);

        ItemStack offHand = inventory.getItemInOffHand();
        if (offHand != null && matcher.test(offHand.getType())) {
            removed += offHand.getAmount();
            if (convert) {
                inventory.setItemInOffHand(convertOrNull(offHand));
            } else {
                inventory.setItemInOffHand(null);
            }
        }

        return removed;
    }

    private int clearMatching(ItemStack[] items, Predicate<Material> matcher, boolean convert, Player player) {
        int removed = 0;
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || !matcher.test(item.getType())) {
                continue;
            }
            removed += item.getAmount();
            if (convert) {
                items[i] = convertOrNull(item);
            } else {
                items[i] = null;
            }
        }
        return removed;
    }

    private ItemStack convertOrNull(ItemStack netheriteItem) {
        Material diamond = NetheriteMaterials.toDiamondEquivalent(netheriteItem.getType());
        if (diamond == null) {
            return null;
        }
        ItemStack converted = new ItemStack(diamond, netheriteItem.getAmount());
        ItemMeta meta = netheriteItem.getItemMeta();
        if (meta != null) {
            ItemMeta target = converted.getItemMeta();
            if (target != null) {
                if (meta.hasDisplayName()) {
                    target.displayName(meta.displayName());
                }
                if (meta.hasEnchants()) {
                    meta.getEnchants().forEach((enchantment, level) -> target.addEnchant(enchantment, level, true));
                }
                if (meta instanceof Damageable damageable && target instanceof Damageable targetDamage) {
                    targetDamage.setDamage(damageable.getDamage());
                }
                converted.setItemMeta(target);
            }
        }
        return converted;
    }
}
