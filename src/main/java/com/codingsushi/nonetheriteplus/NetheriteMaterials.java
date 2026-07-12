package com.codingsushi.nonetheriteplus;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public final class NetheriteMaterials {

    private static final Material[] BASE_NETHERITE_MATERIALS = {
        Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_BOOTS, Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE,
        Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE,
        Material.NETHERITE_INGOT, Material.NETHERITE_SCRAP, Material.NETHERITE_BLOCK,
        Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE
    };

    private static final Material[] ALL = combine(BASE_NETHERITE_MATERIALS, NetheriteCatalog.extraNetheriteMaterials());
    private static final Set<Material> ALL_SET = EnumSet.copyOf(Arrays.asList(ALL));

    private static final Material[] GEAR = combine(new Material[] {
        Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_BOOTS, Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE,
        Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE
    }, NetheriteCatalog.extraStatisticsMaterials());

    private NetheriteMaterials() {
    }

    public static Material[] all() {
        return ALL.clone();
    }

    public static Material[] gearAndTools() {
        return GEAR.clone();
    }

    public static boolean isNetherite(ItemStack item) {
        return item != null && isNetherite(item.getType());
    }

    public static boolean isNetherite(Material material) {
        return material != null && ALL_SET.contains(material);
    }

    public static boolean isNetheriteArmor(Material material) {
        return material == Material.NETHERITE_BOOTS
            || material == Material.NETHERITE_LEGGINGS
            || material == Material.NETHERITE_CHESTPLATE
            || material == Material.NETHERITE_HELMET;
    }

    public static boolean isGearOrTool(Material material) {
        for (Material gear : GEAR) {
            if (gear == material) {
                return true;
            }
        }
        return false;
    }

    public static Material toDiamondEquivalent(Material netherite) {
        Material override = NetheriteCatalog.getExpectedInputOverride(netherite);
        if (override != null) {
            return override;
        }

        return switch (netherite) {
            case NETHERITE_HELMET -> Material.DIAMOND_HELMET;
            case NETHERITE_CHESTPLATE -> Material.DIAMOND_CHESTPLATE;
            case NETHERITE_LEGGINGS -> Material.DIAMOND_LEGGINGS;
            case NETHERITE_BOOTS -> Material.DIAMOND_BOOTS;
            case NETHERITE_SWORD -> Material.DIAMOND_SWORD;
            case NETHERITE_PICKAXE -> Material.DIAMOND_PICKAXE;
            case NETHERITE_AXE -> Material.DIAMOND_AXE;
            case NETHERITE_SHOVEL -> Material.DIAMOND_SHOVEL;
            case NETHERITE_HOE -> Material.DIAMOND_HOE;
            default -> null;
        };
    }

    public static Material getExpectedInputForNetheriteItem(Material netheriteItem) {
        return toDiamondEquivalent(netheriteItem);
    }

    private static Material[] combine(Material[] base, Material[] extra) {
        Material[] combined = Arrays.copyOf(base, base.length + extra.length);
        System.arraycopy(extra, 0, combined, base.length, extra.length);
        return combined;
    }
}
