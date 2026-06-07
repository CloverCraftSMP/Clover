package com.clovercraftsmp.clover.util;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.mixin.loot.EnchantRandomlyFunctionAccessor;
import com.clovercraftsmp.clover.mixin.loot.LootPoolAccessor;
import com.clovercraftsmp.clover.mixin.loot.LootPoolSingletonContainerAccessor;
import com.clovercraftsmp.clover.mixin.loot.LootTableBuilderAccessor;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.*;

public class LootTableUtil {
    private static final ResourceLocation END_VAULT = ResourceLocation.fromNamespaceAndPath("enderscape", "end_city/vault");

    public static void fixLootTables(ResourceKey<LootTable> resourceKey, LootTable.Builder builder) {
        LootTableBuilderAccessor builderAccessor = (LootTableBuilderAccessor) builder;

        if (resourceKey.location().equals(END_VAULT)) {
            ArrayList<LootPool> pools = new ArrayList<>(builderAccessor.getPools().build());

            LootPoolAccessor pool = accessorAt(pools, 4);
            ArrayList<LootPoolEntryContainer> entries = new ArrayList<>(pool.getEntries());

            // Decrease the weight of empty from 192 to 104
            LootPoolSingletonContainerAccessor emptyPool = accessorAt(entries, 0);
            emptyPool.setWeight(104);

            // Remove punch, silk touch, and knockback, and add mending. Decrease weight from 48 to 32.
            LootPoolSingletonContainerAccessor bookEntry = accessorAt(entries, 1);
            List<LootItemFunction> functions = new ArrayList<>(bookEntry.getFunctions());
            EnchantRandomlyFunctionAccessor enchantFunction = accessorAt(functions, 0);
            HolderSet<Enchantment> holderSet = enchantFunction.getOptions().orElseThrow();
            enchantFunction.setOptions(Optional.of(add(remove(holderSet, 4, 8, 11), ItemStackUtil.MENDING)));
            bookEntry.setFunctions(functions);
            bookEntry.setWeight(32);

            LootPoolSingletonContainerAccessor otherBookEntry = accessorAt(entries, 2);
            List<LootItemFunction> otherBookFunctions = new ArrayList<>(otherBookEntry.getFunctions());
            EnchantRandomlyFunctionAccessor otherEnchantFunction = accessorAt(otherBookFunctions, 0);
            HolderSet<Enchantment> otherHolderSet = otherEnchantFunction.getOptions().orElseThrow();
            otherEnchantFunction.setOptions(Optional.of(add(otherHolderSet, ItemStackUtil.MENDING)));
            otherBookEntry.setFunctions(otherBookFunctions);

            pool.setEntries(entries);
            pools.set(4, (LootPool) pool);
            ((LootTableBuilderAccessor) builder).setPools(
                    ImmutableList.<LootPool>builder().addAll(pools)
            );

            Clover.LOGGER.info("Adjusted enderscape end vault loot pool!");
        }
    }

    @SafeVarargs
    private static HolderSet<Enchantment> add(HolderSet<Enchantment> original, Holder<Enchantment>... added) {
        ArrayList<Holder<Enchantment>> edited = new ArrayList<>(original.stream().toList());
        edited.addAll(Arrays.asList(added));
        return HolderSet.direct(edited);
    }

    @SuppressWarnings("SameParameterValue")
    private static HolderSet<Enchantment> remove(HolderSet<Enchantment> original, int... indices) {
        ArrayList<Holder<Enchantment>> edited = new ArrayList<>(original.stream().toList());
        int[] sorted = Arrays.stream(indices).boxed().sorted(Comparator.reverseOrder()).mapToInt(Integer::intValue).toArray();
        for (int i : sorted) edited.remove(i);
        return HolderSet.direct(edited);
    }

    @SuppressWarnings("unchecked")
    private static <T> T accessorAt(List<?> list, int index) {
        return (T) list.get(index);
    }
}
