package com.codingsushi.nonetheriteplus.listener;

import com.codingsushi.nonetheriteplus.NetheriteMaterials;
import com.codingsushi.nonetheriteplus.audit.AuditLog;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import com.codingsushi.nonetheriteplus.policy.RestrictionPolicy;
import com.codingsushi.nonetheriteplus.service.LimitsService;
import com.codingsushi.nonetheriteplus.service.PurgeService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class PossessionListener implements Listener {

    private final ConfigService config;
    private final RestrictionPolicy policy;
    private final AuditLog audit;
    private final PurgeService purge;
    private final LimitsService limits;

    public PossessionListener(
        ConfigService config,
        RestrictionPolicy policy,
        AuditLog audit,
        PurgeService purge,
        LimitsService limits
    ) {
        this.config = config;
        this.policy = policy;
        this.audit = audit;
        this.purge = purge;
        this.limits = limits;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = event.getItem().getItemStack();
        Material type = stack.getType();

        if (policy.shouldBlockPickup(type, player)) {
            event.setCancelled(true);
            player.sendMessage(config.message(
                "messages.blocked_pickup",
                "&cYou cannot pick up this netherite item."
            ));
            audit.log("PICKUP_BLOCKED", player.getName() + " item=" + type);
            return;
        }

        if (NetheriteMaterials.isGearOrTool(type) && !policy.hasBypass(player)) {
            int current = purge.countNetheriteGear(player);
            if (limits.wouldExceedLimit(player, current, stack.getAmount())) {
                event.setCancelled(true);
                player.sendMessage(config.message(
                    "messages.blocked_pickup",
                    "&cYou cannot pick up this netherite item."
                ));
                audit.log("PICKUP_LIMIT", player.getName() + " item=" + type);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!policy.stripDisabledOnJoin()) {
            return;
        }
        Player player = event.getPlayer();
        int removed = purge.stripDisabledFromPlayer(player);
        if (removed > 0) {
            player.sendMessage(config.message(
                "messages.stripped_on_join",
                "&cDisabled netherite items were removed from your inventory."
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!policy.blockEquipDisabled()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player) || policy.hasBypass(player)) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Shift-click equip into armor slots
        if (event.isShiftClick() && current != null && NetheriteMaterials.isNetheriteArmor(current.getType())) {
            if (policy.isDisabledNetheriteItem(current.getType(), player.getWorld())) {
                event.setCancelled(true);
                player.sendMessage(config.message(
                    "messages.blocked_equip",
                    "&cEquipping this netherite armor is disabled."
                ));
                audit.log("EQUIP_BLOCKED", player.getName() + " item=" + current.getType());
            }
            return;
        }

        // Direct click into armor slot
        if (event.getSlotType() == InventoryType.SlotType.ARMOR && cursor != null
            && NetheriteMaterials.isNetheriteArmor(cursor.getType())
            && policy.isDisabledNetheriteItem(cursor.getType(), player.getWorld())) {
            event.setCancelled(true);
            player.sendMessage(config.message(
                "messages.blocked_equip",
                "&cEquipping this netherite armor is disabled."
            ));
            audit.log("EQUIP_BLOCKED", player.getName() + " item=" + cursor.getType());
        }

        // Number-key equip
        if (event.getClick().isKeyboardClick() && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            int hotbar = event.getHotbarButton();
            if (hotbar < 0) {
                return;
            }
            PlayerInventory inventory = player.getInventory();
            ItemStack hotbarItem = inventory.getItem(hotbar);
            if (hotbarItem != null && NetheriteMaterials.isNetheriteArmor(hotbarItem.getType())
                && policy.isDisabledNetheriteItem(hotbarItem.getType(), player.getWorld())) {
                event.setCancelled(true);
                player.sendMessage(config.message(
                    "messages.blocked_equip",
                    "&cEquipping this netherite armor is disabled."
                ));
                audit.log("EQUIP_BLOCKED", player.getName() + " item=" + hotbarItem.getType());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!policy.blockEquipDisabled()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player) || policy.hasBypass(player)) {
            return;
        }

        ItemStack old = event.getOldCursor();
        if (old == null || !NetheriteMaterials.isNetheriteArmor(old.getType())) {
            return;
        }
        if (!policy.isDisabledNetheriteItem(old.getType(), player.getWorld())) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= inventory.getSize()) {
                continue;
            }
            // Armor slots in player inventory are 36-39 in storage indexing for raw slots varies;
            // cancel any drag that touches armor slot types via inventory slots 5-8 in crafting view
            if (rawSlot >= 5 && rawSlot <= 8) {
                event.setCancelled(true);
                player.sendMessage(config.message(
                    "messages.blocked_equip",
                    "&cEquipping this netherite armor is disabled."
                ));
                audit.log("EQUIP_BLOCKED", player.getName() + " item=" + old.getType());
                return;
            }
        }
    }
}
