package com.clovercraftsmp.clover.mixin.feature.filter;

import com.clovercraftsmp.clover.datagen.providers.RecipeProvider;
import com.clovercraftsmp.clover.util.filter.Filter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
    @Inject(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeHolder;value()Lnet/minecraft/world/item/crafting/Recipe;"))
    private static void shareRecipeID(
            AbstractContainerMenu abstractContainerMenu,
            Level level,
            Player player,
            CraftingContainer craftingContainer,
            ResultContainer resultContainer,
            @Nullable RecipeHolder<CraftingRecipe> recipeHolder,
            CallbackInfo ci,
            @Local(ordinal = 1) RecipeHolder<CraftingRecipe> recipeHolder2,
            @Share("recipe") LocalRef<ResourceLocation> recipeId,
            @Share("player") LocalRef<Player> playerShare
    ) {
        recipeId.set(recipeHolder2.id());
        playerShare.set(player);
    }

    @WrapOperation(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;assemble(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack wrapOutput(
            CraftingRecipe instance,
            RecipeInput recipeInput,
            HolderLookup.Provider provider,
            Operation<ItemStack> original,
            @Share("recipe") LocalRef<ResourceLocation> share,
            @Share("player") LocalRef<Player> playerShare
    ) {
        Player player = playerShare.get();
        if (!(player instanceof ServerPlayer)) return original.call(instance, recipeInput, provider);
        if (share.get().equals(RecipeProvider.FILTER_VERIFICATION)) return filterVerification(recipeInput);
        if (share.get().equals(RecipeProvider.FILTER_COMBINATION)) return filterCombination(recipeInput);
        return original.call(instance, recipeInput, provider);
    }

    @Unique
    private static ItemStack filterVerification(RecipeInput recipeInput) {
        ItemStack out = recipeInput.getItem(0).copy();
        if (!(Filter.isFilter(out) && out.has(DataComponents.WRITABLE_BOOK_CONTENT))) return ItemStack.EMPTY;

        CompoundTag customData = Objects.requireNonNull(out.get(DataComponents.CUSTOM_DATA)).copyTag();
        Filter filter = Filter.fromCompound(customData.getCompound(Filter.FILTER_PATH));
        List<String> entries = new java.util.ArrayList<>(Objects.requireNonNull(out.get(DataComponents.WRITABLE_BOOK_CONTENT))
                .getPages(false)
                .flatMap(e -> Arrays.stream(e.replaceAll("§.", "").split("\n")).map(String::trim))
                .toList());

        entries.removeIf(String::isEmpty);

        if (entries.stream().anyMatch(filter::changeFailed)) {
            return ItemStack.EMPTY;
        }

        customData.put(Filter.FILTER_PATH, filter.toCompound());
        out.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
        filter.formatItem(out);
        return out;
    }

    @Unique
    private static ItemStack filterCombination(RecipeInput recipeInput) {
        ItemStack base = recipeInput.getItem(0).copy();
        ItemStack sacrifice = recipeInput.getItem(1).copy();
        if (!(Filter.isFilter(base) && Filter.isFilter(sacrifice))) return ItemStack.EMPTY;

        Filter baseFilter = Objects.requireNonNull(Filter.fromItem(base));
        Filter sacrificeFilter = Objects.requireNonNull(Filter.fromItem(sacrifice));

        if (baseFilter.add(sacrificeFilter)) {
            CompoundTag tag = Objects.requireNonNull(base.get(DataComponents.CUSTOM_DATA)).copyTag();
            tag.put(Filter.FILTER_PATH, baseFilter.toCompound());
            base.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            baseFilter.formatItem(base);
            return base;
        }

        return ItemStack.EMPTY;
    }
}