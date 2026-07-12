package com.codingsushi.nonetheriteplus.listener;

import com.codingsushi.nonetheriteplus.NetheriteMaterials;
import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.audit.AuditLog;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import com.codingsushi.nonetheriteplus.policy.RestrictionPolicy;
import com.codingsushi.nonetheriteplus.service.ItemRefundService;
import com.codingsushi.nonetheriteplus.service.LimitsService;
import com.codingsushi.nonetheriteplus.service.PurgeService;
import com.codingsushi.nonetheriteplus.stats.StatisticsService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public final class SmithingListener implements Listener {

    private final NoNetheritePlus plugin;
    private final ConfigService config;
    private final RestrictionPolicy policy;
    private final StatisticsService stats;
    private final AuditLog audit;
    private final ItemRefundService refunds;
    private final LimitsService limits;
    private final PurgeService purge;

    public SmithingListener(
        NoNetheritePlus plugin,
        ConfigService config,
        RestrictionPolicy policy,
        StatisticsService stats,
        AuditLog audit,
        ItemRefundService refunds,
        LimitsService limits,
        PurgeService purge
    ) {
        this.plugin = plugin;
        this.config = config;
        this.policy = policy;
        this.stats = stats;
        this.audit = audit;
        this.refunds = refunds;
        this.limits = limits;
        this.purge = purge;
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (event.getResult() == null) {
            return;
        }

        Player player = (Player) event.getView().getPlayer();
        Material resultType = event.getResult().getType();

        if (policy.hasBypass(player)) {
            return;
        }

        if (isNetheriteArmorTrimOperation(event)) {
            if (policy.disableTrims(player.getWorld())) {
                event.setResult(null);
                if (config.isDebugMode() && config.getBoolean("debug.log_craft_events", false)) {
                    plugin.getLogger().info("Blocked netherite trim on " + resultType + " by " + player.getName()
                        + " in world " + player.getWorld().getName());
                }
                audit.log("TRIM_BLOCKED", player.getName() + " item=" + resultType);
            }
            return;
        }

        boolean disabled = policy.isDisabledNetheriteItem(resultType, player.getWorld());
        boolean overLimit = false;
        if (!disabled && NetheriteMaterials.isGearOrTool(resultType)) {
            int current = purge.countNetheriteGear(player);
            overLimit = limits.wouldExceedLimit(player, current, 1);
        }

        if (!disabled && !overLimit) {
            return;
        }

        stats.increment("total_blocked_crafts");
        stats.increment(resultType.name().toLowerCase() + "_blocked");
        audit.log("CRAFT_BLOCKED", player.getName() + " item=" + resultType + (overLimit ? " reason=limit" : ""));

        if (config.isDebugMode() && config.getBoolean("debug.log_craft_events", false)) {
            plugin.getLogger().info("Blocked " + resultType + " craft attempt by " + player.getName()
                + " in world " + player.getWorld().getName());
        }

        event.setResult(null);

        if (!policy.refundMaterials(player.getWorld())) {
            return;
        }

        if (!(event.getInventory() instanceof SmithingInventory inventory)) {
            return;
        }

        ItemStack template = inventory.getInputTemplate();
        ItemStack equipment = inventory.getInputEquipment();
        ItemStack ingredient = inventory.getInputMineral();

        Material expectedInput = NetheriteMaterials.getExpectedInputForNetheriteItem(resultType);
        if (expectedInput == null || equipment == null || ingredient == null) {
            return;
        }

        boolean validUpgrade =
            equipment.getType() == expectedInput && ingredient.getType() == Material.NETHERITE_INGOT;

        if (!validUpgrade) {
            return;
        }

        stats.increment("total_refunds");

        refunds.giveOrDrop(player, equipment.clone());
        refunds.giveOrDrop(player, new ItemStack(Material.NETHERITE_INGOT, 1));
        if (template != null && !template.getType().isAir()) {
            refunds.giveOrDrop(player, template.clone());
        }

        inventory.setInputTemplate(null);
        inventory.setInputEquipment(null);
        inventory.setInputMineral(null);

        player.sendMessage(refunds.refundMessage());
    }

    private boolean isNetheriteArmorTrimOperation(PrepareSmithingEvent event) {
        if (event.getResult() == null || !NetheriteMaterials.isNetheriteArmor(event.getResult().getType())) {
            return false;
        }

        if (!(event.getInventory() instanceof SmithingInventory smithingInventory)) {
            return false;
        }

        ItemStack template = smithingInventory.getInputTemplate();
        ItemStack base = smithingInventory.getInputEquipment();

        if (template == null || base == null) {
            return false;
        }

        if (template.getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
            return false;
        }

        return base.getType() == event.getResult().getType();
    }
}
