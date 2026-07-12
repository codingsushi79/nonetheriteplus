package com.codingsushi.nonetheriteplus.service;

import com.codingsushi.nonetheriteplus.config.ConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public final class ItemRefundService {

    private final ConfigService config;

    public ItemRefundService(ConfigService config) {
        this.config = config;
    }

    public void giveOrDrop(Player player, ItemStack item) {
        if (player == null || item == null || item.getType().isAir()) {
            return;
        }

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    public void giveOrDropAll(Player player, ItemStack... items) {
        for (ItemStack item : items) {
            if (item != null) {
                giveOrDrop(player, item.clone());
            }
        }
    }

    public String refundMessage() {
        return config.message(
            "messages.blocked_craft",
            config.getConfig().getString(
                "refund_message",
                "&cNetherite crafting is disabled for this item. Your materials have been refunded."
            )
        );
    }
}
