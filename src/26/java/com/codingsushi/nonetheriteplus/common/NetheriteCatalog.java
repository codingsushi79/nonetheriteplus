package com.codingsushi.nonetheriteplus.common;

import java.util.List;

/**
 * MC 26.x-exclusive netherite items. Item/recipe ids verified against the
 * 26.3 server jar's data/minecraft/recipe folder.
 */
public final class NetheriteCatalog {

    private NetheriteCatalog() {
    }

    public static List<NetheriteEntry> extraEntries() {
        return List.of(
            new NetheriteEntry("minecraft:netherite_spear", "disable_netherite_spear", false, "minecraft:netherite_spear_smithing"),
            new NetheriteEntry("minecraft:netherite_horse_armor", "disable_netherite_horse_armor", true, "minecraft:netherite_horse_armor_smithing")
        );
    }
}
