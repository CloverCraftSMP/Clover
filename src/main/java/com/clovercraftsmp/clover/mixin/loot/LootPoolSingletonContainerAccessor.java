package com.clovercraftsmp.clover.mixin.loot;

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootPoolSingletonContainer.class)
public interface LootPoolSingletonContainerAccessor {
    @Accessor("weight") @Mutable
    void setWeight(int weight);

    @Accessor("functions")
    List<LootItemFunction> getFunctions();

    @Accessor("functions") @Mutable
    void setFunctions(List<LootItemFunction> functions);
}
