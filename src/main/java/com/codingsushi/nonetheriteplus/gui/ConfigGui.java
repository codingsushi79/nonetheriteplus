package com.codingsushi.nonetheriteplus.gui;

import com.codingsushi.nonetheriteplus.NetheriteMaterials;
import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import com.codingsushi.nonetheriteplus.policy.RestrictionPolicy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigGui implements Listener {

    private static final String TITLE = "NoNetheritePlus Config";

    private final NoNetheritePlus plugin;
    private final ConfigService config;
    private final RestrictionPolicy policy;
    private final Map<Integer, ToggleBinding> bindings = new HashMap<>();

    public ConfigGui(NoNetheritePlus plugin, ConfigService config, RestrictionPolicy policy) {
        this.plugin = plugin;
        this.config = config;
        this.policy = policy;
        buildBindings();
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 54, Component.text(TITLE));
        for (Map.Entry<Integer, ToggleBinding> entry : bindings.entrySet()) {
            inventory.setItem(entry.getKey(), createToggleItem(entry.getValue()));
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = plainTitle(event.getView().title());
        if (!TITLE.equals(title)) {
            return;
        }

        event.setCancelled(true);
        ToggleBinding binding = bindings.get(event.getRawSlot());
        if (binding == null) {
            return;
        }

        boolean current = config.getBoolean(binding.configKey(), binding.defaultValue());
        config.set(binding.configKey(), !current);
        config.save();
        plugin.applyRuntimeReload(false);
        event.getInventory().setItem(event.getRawSlot(), createToggleItem(binding));
        player.sendMessage("§aToggled §f" + binding.label() + " §ato §f" + (!current));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (TITLE.equals(plainTitle(event.getView().title()))) {
            event.setCancelled(true);
        }
    }

    private static String plainTitle(Component component) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component);
    }

    private ItemStack createToggleItem(ToggleBinding binding) {
        boolean enabled = config.getBoolean(binding.configKey(), binding.defaultValue());
        ItemStack item = new ItemStack(binding.icon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(binding.label(), enabled ? NamedTextColor.RED : NamedTextColor.GREEN));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Path: " + binding.configKey(), NamedTextColor.GRAY));
            lore.add(Component.text("Currently: " + (enabled ? "ON (restricted)" : "OFF"), NamedTextColor.YELLOW));
            lore.add(Component.text("Click to toggle", NamedTextColor.DARK_GRAY));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void buildBindings() {
        int slot = 0;
        for (Material material : NetheriteMaterials.gearAndTools()) {
            String key = "disable_" + material.name().toLowerCase();
            boolean defaultValue = policy.isDisabledNetheriteItem(material);
            bindings.put(slot++, new ToggleBinding(
                key,
                material.name().replace("NETHERITE_", ""),
                material,
                defaultValue
            ));
        }

        bindings.put(45, new ToggleBinding("enforcement.block_ancient_debris", "Block Ancient Debris", Material.ANCIENT_DEBRIS, false));
        bindings.put(46, new ToggleBinding("enforcement.block_scrap_and_ingot_craft", "Block Scrap/Ingot Craft", Material.NETHERITE_SCRAP, false));
        bindings.put(47, new ToggleBinding("enforcement.block_piglin_barter", "Block Piglin Barter", Material.GOLD_INGOT, false));
        bindings.put(48, new ToggleBinding("enforcement.block_pickup", "Block Pickup", Material.HOPPER, false));
        bindings.put(49, new ToggleBinding("enforcement.strip_disabled_on_join", "Strip on Join", Material.BARRIER, false));
        bindings.put(50, new ToggleBinding("enforcement.block_equip_disabled", "Block Equip", Material.SHIELD, false));
        bindings.put(51, new ToggleBinding("enforcement.convert_on_strip", "Convert on Strip", Material.DIAMOND, false));
        bindings.put(52, new ToggleBinding("disable_netherite_trims", "Disable Trims", Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, false));
        bindings.put(53, new ToggleBinding("disable_upgrade_templates", "Disable Templates", Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, false));
    }

    private record ToggleBinding(String configKey, String label, Material icon, boolean defaultValue) {
    }
}
