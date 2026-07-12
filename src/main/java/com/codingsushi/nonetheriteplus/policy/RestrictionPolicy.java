package com.codingsushi.nonetheriteplus.policy;

import com.codingsushi.nonetheriteplus.NetheriteCatalog;
import com.codingsushi.nonetheriteplus.NetheriteMaterials;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class RestrictionPolicy {

    public static final String BYPASS_PERMISSION = "nonetherite.bypass";

    private final ConfigService config;

    public RestrictionPolicy(ConfigService config) {
        this.config = config;
    }

    public boolean hasBypass(Player player) {
        return player != null && player.hasPermission(BYPASS_PERMISSION);
    }

    public boolean isDisabledNetheriteItem(Material material) {
        Boolean versionSpecific = NetheriteCatalog.getDisabledOverride(material, config.getConfig());
        if (versionSpecific != null) {
            return versionSpecific;
        }

        return switch (material) {
            case NETHERITE_HELMET -> config.getBoolean("disable_netherite_helmet", true);
            case NETHERITE_CHESTPLATE -> config.getBoolean("disable_netherite_chestplate", true);
            case NETHERITE_LEGGINGS -> config.getBoolean("disable_netherite_leggings", true);
            case NETHERITE_BOOTS -> config.getBoolean("disable_netherite_boots", true);
            case NETHERITE_SWORD -> config.getBoolean("disable_netherite_sword", false);
            case NETHERITE_PICKAXE -> config.getBoolean("disable_netherite_pickaxe", false);
            case NETHERITE_AXE -> config.getBoolean("disable_netherite_axe", false);
            case NETHERITE_SHOVEL -> config.getBoolean("disable_netherite_shovel", false);
            case NETHERITE_HOE -> config.getBoolean("disable_netherite_hoe", false);
            default -> false;
        };
    }

    public boolean isDisabledNetheriteItem(Material material, World world) {
        String configKey = "disable_" + material.name().toLowerCase();
        Boolean worldOverride = config.getWorldBooleanOrNull(configKey, world);
        if (worldOverride != null) {
            return worldOverride;
        }
        return isDisabledNetheriteItem(material);
    }

    public boolean isDisabledForPlayer(Material material, Player player) {
        if (hasBypass(player)) {
            return false;
        }
        World world = player != null ? player.getWorld() : null;
        return isDisabledNetheriteItem(material, world);
    }

    public boolean disableTrims(World world) {
        return config.getWorldBoolean("disable_netherite_trims", world, false);
    }

    public boolean refundMaterials(World world) {
        return config.getWorldBoolean("refund_materials", world, true);
    }

    public boolean disableUpgradeTemplates(World world) {
        return config.getWorldBoolean("disable_upgrade_templates", world, false);
    }

    public boolean blockAncientDebris(World world) {
        return config.getWorldBoolean("enforcement.block_ancient_debris", world, false);
    }

    public boolean blockScrapAndIngotCraft(World world) {
        return config.getWorldBoolean("enforcement.block_scrap_and_ingot_craft", world, false);
    }

    public boolean blockPiglinBarter(World world) {
        return config.getWorldBoolean("enforcement.block_piglin_barter", world, false);
    }

    public boolean blockPickup(World world) {
        return config.getWorldBoolean("enforcement.block_pickup", world, false);
    }

    public boolean stripDisabledOnJoin() {
        return config.getBoolean("enforcement.strip_disabled_on_join", false);
    }

    public boolean blockEquipDisabled() {
        return config.getBoolean("enforcement.block_equip_disabled", false);
    }

    public boolean convertOnStrip() {
        return config.getBoolean("enforcement.convert_on_strip", false);
    }

    public boolean shouldBlockPickup(Material material, Player player) {
        if (hasBypass(player) || !blockPickup(player.getWorld())) {
            return false;
        }
        if (NetheriteMaterials.isGearOrTool(material)) {
            return isDisabledNetheriteItem(material, player.getWorld());
        }
        return material == Material.NETHERITE_INGOT
            || material == Material.NETHERITE_SCRAP
            || material == Material.NETHERITE_BLOCK
            || material == Material.ANCIENT_DEBRIS
            || material == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
    }
}
