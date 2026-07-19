package com.codingsushi.nonetheriteplus.common;

/**
 * A netherite item that can be individually toggled off.
 *
 * @param itemId         namespaced vanilla item id, e.g. "minecraft:netherite_helmet"
 * @param configKey      config.yml boolean key controlling whether it's disabled
 * @param defaultDisabled default value for configKey if unset
 * @param recipeId       namespaced id of the smithing_transform recipe that produces this item
 */
public record NetheriteEntry(String itemId, String configKey, boolean defaultDisabled, String recipeId) {
}
