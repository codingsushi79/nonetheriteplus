package com.codingsushi.nonetheriteplus.forge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import java.util.function.BiPredicate;

/**
 * Forge Global Loot Modifier stripping netherite upgrade templates from
 * generated drops, mirroring StripUpgradeTemplateFunction on the Fabric
 * branch. The per-world disable check happens per-drop against LootContext,
 * same reasoning as the Fabric version.
 */
public class StripUpgradeTemplateModifier extends LootModifier {

    public static volatile BiPredicate<String, LootContext> shouldStrip = (id, ctx) -> false;

    public static final MapCodec<StripUpgradeTemplateModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
        codecStart(inst).apply(inst, StripUpgradeTemplateModifier::new));

    public StripUpgradeTemplateModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(LootTable table, ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
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
