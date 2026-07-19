package com.codingsushi.nonetheriteplus.common;

import java.util.List;

/**
 * MC 26.x-exclusive netherite items. Item/recipe ids here are inferred from
 * Mojang's standard naming convention (not independently confirmed against a
 * live 26.x server) -- verify against an actual game before relying on them.
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
