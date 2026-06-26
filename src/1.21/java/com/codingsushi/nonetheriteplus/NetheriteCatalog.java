package com.codingsushi.nonetheriteplus;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public final class NetheriteCatalog {

    private NetheriteCatalog() {
    }

    public static Material[] extraNetheriteMaterials() {
        return new Material[0];
    }

    public static Material[] extraStatisticsMaterials() {
        return new Material[0];
    }

    public static Boolean getDisabledOverride(Material material, FileConfiguration config) {
        return null;
    }

    public static Material getExpectedInputOverride(Material netheriteItem) {
        return null;
    }
}
