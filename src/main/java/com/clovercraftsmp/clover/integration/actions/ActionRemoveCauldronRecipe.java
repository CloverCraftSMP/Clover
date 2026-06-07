package com.clovercraftsmp.clover.integration.actions;

import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.clovercraftsmp.clover.recipe.CauldronRecipe;
import com.clovercraftsmp.clover.recipe.CauldronRecipeRegistry;

public class ActionRemoveCauldronRecipe implements IRuntimeAction {
    private final String id;
    private CauldronRecipe removed;

    public ActionRemoveCauldronRecipe(String id) {
        this.id = id;
    }

    @Override
    public void apply() {
        removed = CauldronRecipeRegistry.getAll()
                .stream()
                .filter(r -> r.id().equals(id))
                .findFirst()
                .orElse(null);
        CauldronRecipeRegistry.remove(id);
    }

    @Override
    public String describe() {
        return "Removing recipe from cauldron converter";
    }

    @Override
    public String systemName() {
        return "Cauldron Recipe Remove";
    }
}
