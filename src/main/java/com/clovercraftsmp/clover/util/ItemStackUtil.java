package com.clovercraftsmp.clover.util;

import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Optional;

public class ItemStackUtil {
    public static Holder<Enchantment> MENDING;

    public static void mendingRegistry(ResourceKey<LootTable> registryKey, LootTable.Builder builder, LootTableSource source, HolderLookup.Provider wrapper) {
        if (MENDING == null)
            MENDING = wrapper.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING);
    }

    public static boolean hasMending(ItemStack stack) {
        return MENDING != null &&
                Optional.ofNullable(stack.get(DataComponents.STORED_ENCHANTMENTS))
                        .map(e -> e.keySet().contains(MENDING))
                        .orElse(false);
    }
}