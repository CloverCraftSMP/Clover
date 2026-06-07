package com.clovercraftsmp.clover.integration.actions;

import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.clovercraftsmp.clover.recipe.CauldronRecipe;
import com.clovercraftsmp.clover.recipe.CauldronRecipeRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ActionAddCauldronRecipe(
        String id, IItemStack input, IItemStack output, String fluid
) implements IUndoableAction {
    @Override
    public void apply() {
        CauldronRecipe.CauldronFluid fluidType = switch (fluid.toLowerCase()) {
            case "lava" -> CauldronRecipe.CauldronFluid.LAVA;
            case "powder_snow" -> CauldronRecipe.CauldronFluid.POWDER_SNOW;
            case "any" -> CauldronRecipe.CauldronFluid.ANY;
            default -> CauldronRecipe.CauldronFluid.WATER;
        };

        Item inputItem = input.getInternal().getItem();
        ItemStack outputStack = output.getInternal().copy();

        CauldronRecipeRegistry.register(
                new CauldronRecipe(id, inputItem, outputStack, fluidType)
        );
    }

    @Override
    public String describe() {
        return "Adding cauldron recipe";
    }

    @Override
    public String systemName() {
        return "Cauldron Recipe";
    }

    @Override
    public void undo() {
        CauldronRecipeRegistry.remove(id);
    }

    @Override
    public String describeUndo() {
        return "Remove cauldron recipe";
    }
}
