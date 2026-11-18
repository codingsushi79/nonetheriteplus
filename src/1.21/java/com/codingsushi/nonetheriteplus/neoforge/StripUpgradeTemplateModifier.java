package com.codingsushi.nonetheriteplus.neoforge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.function.BiPredicate;

/**
 * NeoForge Global Loot Modifier stripping netherite upgrade templates from
 * generated drops. Same design as the Forge branch's version, just moved to
 * the net.neoforged.* package.
 */
public class StripUpgradeTemplateModifier extends LootModifier {

    public static volatile BiPredicate<String, LootContext> shouldStrip = (id, ctx) -> false;

    public static final MapCodec<StripUpgradeTemplateModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
        codecStart(inst).apply(inst, StripUpgradeTemplateModifier::new));

    public StripUpgradeTemplateModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.removeIf(stack -> {
            if (stack.isEmpty()) {
                return false;
            }
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            return id.equals("minecraft:netherite_upgrade_smithing_template") && shouldStrip.test(id, context);
        });
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
