package com.clovercraftsmp.clover.mixin.loot;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(EnchantRandomlyFunction.class)
public interface EnchantRandomlyFunctionAccessor {
    @Accessor("options")
    Optional<HolderSet<Enchantment>> getOptions();

    @Accessor("options") @Mutable
    void setOptions(Optional<HolderSet<Enchantment>> options);
}
