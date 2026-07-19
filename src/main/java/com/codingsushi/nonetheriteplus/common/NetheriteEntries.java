package com.codingsushi.nonetheriteplus.common;

import java.util.ArrayList;
import java.util.List;

public final class NetheriteEntries {

    private NetheriteEntries() {
    }

    public static final List<NetheriteEntry> BASE = List.of(
        new NetheriteEntry("minecraft:netherite_helmet", "disable_netherite_helmet", true, "minecraft:netherite_helmet_smithing"),
        new NetheriteEntry("minecraft:netherite_chestplate", "disable_netherite_chestplate", true, "minecraft:netherite_chestplate_smithing"),
        new NetheriteEntry("minecraft:netherite_leggings", "disable_netherite_leggings", true, "minecraft:netherite_leggings_smithing"),
        new NetheriteEntry("minecraft:netherite_boots", "disable_netherite_boots", true, "minecraft:netherite_boots_smithing"),
        new NetheriteEntry("minecraft:netherite_sword", "disable_netherite_sword", false, "minecraft:netherite_sword_smithing"),
        new NetheriteEntry("minecraft:netherite_pickaxe", "disable_netherite_pickaxe", false, "minecraft:netherite_pickaxe_smithing"),
        new NetheriteEntry("minecraft:netherite_axe", "disable_netherite_axe", false, "minecraft:netherite_axe_smithing"),
        new NetheriteEntry("minecraft:netherite_shovel", "disable_netherite_shovel", false, "minecraft:netherite_shovel_smithing"),
        new NetheriteEntry("minecraft:netherite_hoe", "disable_netherite_hoe", false, "minecraft:netherite_hoe_smithing")
    );

    /** Plain netherite materials that aren't craftable items with a toggle, but should still be swept by the purge task. */
    public static final List<String> PURGE_ONLY_ITEMS = List.of(
        "minecraft:netherite_ingot",
        "minecraft:netherite_scrap",
        "minecraft:netherite_block",
        "minecraft:netherite_upgrade_smithing_template"
    );

    /** BASE plus this version's NetheriteCatalog.extraEntries(). */
    public static List<NetheriteEntry> all() {
        List<NetheriteEntry> all = new ArrayList<>(BASE);
        all.addAll(NetheriteCatalog.extraEntries());
        return all;
    }
}
