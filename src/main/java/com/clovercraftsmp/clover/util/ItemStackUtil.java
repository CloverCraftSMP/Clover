package com.clovercraftsmp.clover.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

//? if >=26.1
import net.minecraft.nbt.StringTag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ItemStackUtil {
    private static final Map<Identifier, Holder.Reference<Enchantment>> ENCHANTMENT_LOOKUP = new HashMap<>();
    public static Holder.Reference<Enchantment> MENDING;
    private static HolderLookup.Provider lastSeenProvider;

    public static void onEnchantmentRegistryReady(HolderLookup.Provider wrapper) {
        if (lastSeenProvider == wrapper) return;
        lastSeenProvider = wrapper;

        ENCHANTMENT_LOOKUP.clear();
        wrapper.lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .forEach(holder -> ENCHANTMENT_LOOKUP.put(identifierOf(holder.key()), holder));

        MENDING = ENCHANTMENT_LOOKUP.get(identifierOf(Enchantments.MENDING));
    }

    public static boolean hasMending(ItemStack stack) {
        return MENDING != null &&
                Optional.ofNullable(stack.get(DataComponents.STORED_ENCHANTMENTS))
                        .map(e -> e.keySet().contains(MENDING))
                        .orElse(false);
    }

    public static @Nullable Holder.Reference<Enchantment> resolveEnchantment(String id) {
        Identifier loc = Identifier.tryParse(id);
        if (loc == null) return null;
        return ENCHANTMENT_LOOKUP.get(loc);
    }

    public static Identifier identifierOf(ResourceKey<?> key) {
        //? if <=1.21.1 {
        /*return key.location();
        *///? }

        //? if >=26.1 {
        return key.identifier();
        //? }
    }

    public static Item getFromBuiltin(Identifier loc) {
        //? if <=1.21.1 {
        /*return BuiltInRegistries.ITEM.get(loc);
        *///? }

        //? if >=26.1 {
        return BuiltInRegistries.ITEM.getValue(loc);
        //? }
    }

    public static Optional<HolderSet.Named<Item>> getFromBuiltin(TagKey<Item> tag) {
        //? if <=1.21.1 {
        /*return BuiltInRegistries.ITEM.getTag(tag);
        *///? }

        //? if >=26.1 {
        return BuiltInRegistries.ITEM.get(tag);
        //? }
    }

    public static ListTag getStringListOrEmpty(CompoundTag base, String name) {
        //? if <=1.21.1 {
        /*return base.getList(name, 8);
        *///? }

        //? if >=26.1 {
        return base.getList(name)
                .filter(e -> e.stream().allMatch(StringTag.class::isInstance))
                .orElse(new ListTag());
        //? }
    }

    public static ListTag getCompoundListOrEmpty(CompoundTag base, String name) {
        //? if <=1.21.1 {
        /*return base.getList(name, 10);
        *///? }

        //? if >=26.1 {
        return base.getList(name)
                .filter(e -> e.stream().allMatch(CompoundTag.class::isInstance))
                .orElse(new ListTag());
        //? }
    }

    public static int getIntOrDefault(CompoundTag base, String name) {
        //? if <=1.21.1 {
        /*return base.getInt(name);
        *///? }

        //? if >=26.1 {
        return base.getIntOr(name, 0);
        //? }
    }

    public static String getStringOrDefault(CompoundTag base, String name) {
        //? if <=1.21.1 {
        /*return base.getString(name);
        *///? }

        //? if >=26.1 {
        return base.getStringOr(name, "");
        //? }
    }

    public static String getStringOrDefault(ListTag base, int i) {
        //? if <=1.21.1 {
        /*return base.getString(i);
        *///? }

        //? if >=26.1 {
        return base.getStringOr(i, "");
        //? }
    }

    public static boolean getBooleanOrDefault(CompoundTag base, String name) {
        //? if <=1.21.1 {
        /*return base.getBoolean(name);
        *///? }

        //? if >=26.1 {
        return base.getBooleanOr(name, false);
        //? }
    }

    public static double getDoubleOrDefault(CompoundTag base, String name) {
        //? if <=1.21.1 {
        /*return base.getDouble(name);
        *///? }

        //? if >=26.1 {
        return base.getDoubleOr(name, 0.0);
        //? }
    }

    public static Optional<CompoundTag> getCompound(CompoundTag base, String name) {
        //? if <=1.21.1 {
        /*return base.contains(name, 10) ? Optional.of(base.getCompound(name)) : Optional.empty();
        *///? }

        //? if >=26.1 {
        return base.getCompound(name);
        //? }
    }

    public static Stream<ItemStack> allItems(ItemContainerContents contents) {
        //? if <=1.21.1 {
        /*return contents.stream();
        *///? }

        //? if >=26.1 {
        return contents.allItemsCopyStream();
        //? }
    }

    public static Stream<ItemStack> nonEmptyItems(ItemContainerContents contents) {
        //? if <=1.21.1 {
        /*return contents.nonEmptyStream();
        *///? }

        //? if >=26.1 {
        return contents.nonEmptyItemCopyStream();
        //? }
    }
}
