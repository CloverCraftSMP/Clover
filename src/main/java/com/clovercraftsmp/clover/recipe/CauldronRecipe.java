package com.clovercraftsmp.clover.recipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record CauldronRecipe(
        String id,
        Item input,
        ItemStack output,
        CauldronFluid fluid
) {
    public enum CauldronFluid {
        WATER, LAVA, POWDER_SNOW, ANY
    }
}
