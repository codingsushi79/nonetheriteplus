package com.codingsushi.nonetheriteplus.fabric;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.function.BiPredicate;

/**
 * Loot function attached to every loot table at load time that strips
 * netherite upgrade templates from generated drops when the per-world/global
 * config disables them. Stateless, so it's applied unconditionally; the actual
 * disable check happens per-drop against the world from LootContext.
 */
public final class StripUpgradeTemplateFunction implements LootItemFunction {

    public static final MapCodec<StripUpgradeTemplateFunction> CODEC =
        MapCodec.unit(StripUpgradeTemplateFunction::new);

    public static final StripUpgradeTemplateFunction INSTANCE = new StripUpgradeTemplateFunction();

    private static volatile BiPredicate<String, LootContext> shouldStrip = (id, ctx) -> false;

    public static void setPredicate(BiPredicate<String, LootContext> predicate) {
        shouldStrip = predicate;
    }

    public static void register() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE,
            Identifier.fromNamespaceAndPath("nonetheriteplus", "strip_upgrade_template"), CODEC);
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        if (stack.isEmpty()) {
            return stack;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!id.equals("minecraft:netherite_upgrade_smithing_template")) {
            return stack;
        }
        return shouldStrip.test(id, context) ? ItemStack.EMPTY : stack;
    }

    @Override
    public MapCodec<StripUpgradeTemplateFunction> codec() {
        return CODEC;
    }
}
