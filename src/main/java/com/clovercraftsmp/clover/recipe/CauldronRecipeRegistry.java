package com.clovercraftsmp.clover.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class CauldronRecipeRegistry {
    private static final Map<String, CauldronRecipe> RECIPES = new LinkedHashMap<>();

    public static void register(CauldronRecipe recipe) {
        RECIPES.put(recipe.id(), recipe);
    }

    public static void remove(String id) {
        RECIPES.remove(id);
    }

    public static Optional<CauldronRecipe> find(ItemStack stack, BlockState cauldron) {
        CauldronRecipe.CauldronFluid fluidType = getFluidType(cauldron);
        if (fluidType == null) return Optional.empty();

        return RECIPES.values().stream()
                .filter(r -> r.input() == stack.getItem())
                .filter(r -> r.fluid() == CauldronRecipe.CauldronFluid.ANY
                            || r.fluid() == fluidType)
                .findFirst();
    }

    public static CauldronRecipe.CauldronFluid getFluidType(BlockState state) {
        if (state.getBlock() instanceof AbstractCauldronBlock) {
            Block block = state.getBlock();
            if (block == Blocks.WATER_CAULDRON) return CauldronRecipe.CauldronFluid.WATER;
            if (block == Blocks.LAVA_CAULDRON) return CauldronRecipe.CauldronFluid.LAVA;
            if (block == Blocks.POWDER_SNOW_CAULDRON) return CauldronRecipe.CauldronFluid.POWDER_SNOW;
        }
        return null;
    }

    public static Collection<CauldronRecipe> getAll() {
        return Collections.unmodifiableCollection(RECIPES.values());
    }
}
