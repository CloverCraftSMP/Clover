package com.clovercraftsmp.clover.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemStackUtil {
    private static final Map<ResourceLocation, Holder.Reference<Enchantment>> ENCHANTMENT_LOOKUP = new HashMap<>();
    public static Holder.Reference<Enchantment> MENDING;
    private static HolderLookup.Provider lastSeenProvider;

    public static void onEnchantmentRegistryReady(HolderLookup.Provider wrapper) {
        if (lastSeenProvider == wrapper) return;
        lastSeenProvider = wrapper;

        ENCHANTMENT_LOOKUP.clear();
        wrapper.lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .forEach(holder -> ENCHANTMENT_LOOKUP.put(holder.key().location(), holder));

        MENDING = ENCHANTMENT_LOOKUP.get(Enchantments.MENDING.location());
    }

    public static boolean hasMending(ItemStack stack) {
        return MENDING != null &&
                Optional.ofNullable(stack.get(DataComponents.STORED_ENCHANTMENTS))
                        .map(e -> e.keySet().contains(MENDING))
                        .orElse(false);
    }

    public static @Nullable Holder.Reference<Enchantment> resolveEnchantment(String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) return null;
        return ENCHANTMENT_LOOKUP.get(loc);
    }
}