package com.codingsushi.nonetheriteplus.listener;

import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.audit.AuditLog;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import com.codingsushi.nonetheriteplus.policy.RestrictionPolicy;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;
import java.util.List;

public final class AcquisitionListener implements Listener {

    private final NoNetheritePlus plugin;
    private final ConfigService config;
    private final RestrictionPolicy policy;
    private final AuditLog audit;

    public AcquisitionListener(
        NoNetheritePlus plugin,
        ConfigService config,
        RestrictionPolicy policy,
        AuditLog audit
    ) {
        this.plugin = plugin;
        this.config = config;
        this.policy = policy;
        this.audit = audit;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.ANCIENT_DEBRIS) {
            return;
        }
        Player player = event.getPlayer();
        if (policy.hasBypass(player) || !policy.blockAncientDebris(player.getWorld())) {
            return;
        }
        event.setDropItems(false);
        audit.log("DEBRIS_BREAK", player.getName() + " world=" + player.getWorld().getName());
        if (config.isDebugMode()) {
            plugin.getLogger().info("Blocked ancient debris break by " + player.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (policy.hasBypass(player) || !policy.blockAncientDebris(player.getWorld())) {
            return;
        }
        event.getItems().removeIf(item -> item.getItemStack().getType() == Material.ANCIENT_DEBRIS);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) {
            return;
        }
        if (policy.hasBypass(player) || !policy.blockScrapAndIngotCraft(player.getWorld())) {
            return;
        }

        Recipe recipe = event.getRecipe();
        if (recipe == null) {
            return;
        }
        Material result = recipe.getResult().getType();
        if (result == Material.NETHERITE_INGOT || result == Material.NETHERITE_BLOCK || result == Material.NETHERITE_SCRAP) {
            event.getInventory().setResult(null);
            audit.log("CRAFT_MATERIAL_BLOCKED", player.getName() + " item=" + result);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack result = event.getResult();
        if (result.getType() != Material.NETHERITE_SCRAP) {
            return;
        }

        World world = event.getBlock().getWorld();
        if (!policy.blockScrapAndIngotCraft(world)) {
            return;
        }

        event.setCancelled(true);
        audit.log("SMELT_BLOCKED", "world=" + world.getName() + " item=NETHERITE_SCRAP");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPiglinBarter(PiglinBarterEvent event) {
        World world = event.getEntity().getWorld();
        if (!policy.blockPiglinBarter(world)) {
            return;
        }
        List<ItemStack> outcome = event.getOutcome();
        boolean changed = false;
        for (Iterator<ItemStack> it = outcome.iterator(); it.hasNext();) {
            ItemStack stack = it.next();
            if (stack != null && isBarterNetherite(stack.getType())) {
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            audit.log("BARTER_BLOCKED", "world=" + world.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDropItem(EntityDropItemEvent event) {
        if (!(event.getEntity() instanceof Piglin)) {
            return;
        }
        World world = event.getEntity().getWorld();
        if (!policy.blockPiglinBarter(world)) {
            return;
        }
        ItemStack stack = event.getItemDrop().getItemStack();
        if (isBarterNetherite(stack.getType())) {
            event.setCancelled(true);
            audit.log("BARTER_BLOCKED", "world=" + world.getName() + " item=" + stack.getType());
        }
    }

    private boolean isBarterNetherite(Material material) {
        return material == Material.NETHERITE_SCRAP
            || material == Material.NETHERITE_INGOT
            || material == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
    }
}
