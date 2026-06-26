package com.codingsushi.nonetheriteplus;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public final class NetheriteCatalog {

    private NetheriteCatalog() {
    }

    public static Material[] extraNetheriteMaterials() {
        return new Material[] {
            Material.NETHERITE_SPEAR,
            Material.NETHERITE_HORSE_ARMOR
        };
    }

    public static Material[] extraStatisticsMaterials() {
        return extraNetheriteMaterials();
    }

    public static Boolean getDisabledOverride(Material material, FileConfiguration config) {
        switch (material) {
            case NETHERITE_SPEAR:
                return config.getBoolean("disable_netherite_spear", false);
            case NETHERITE_HORSE_ARMOR:
                return config.getBoolean("disable_netherite_horse_armor", true);
            default:
                return null;
        }
    }

    public static Material getExpectedInputOverride(Material netheriteItem) {
        if (netheriteItem == Material.NETHERITE_HORSE_ARMOR) {
            return Material.DIAMOND_HORSE_ARMOR;
        }
        return null;
    }
}
