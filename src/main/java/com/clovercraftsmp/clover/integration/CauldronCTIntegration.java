package com.clovercraftsmp.clover.integration;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.clovercraftsmp.clover.integration.actions.ActionAddCauldronRecipe;
import com.clovercraftsmp.clover.integration.actions.ActionRemoveCauldronRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.clover.CauldronManager")
public class CauldronCTIntegration {
    @ZenCodeType.Method
    public static void addRecipe(
            String id,
            IItemStack input,
            IItemStack output,
            @ZenCodeType.OptionalString("water") String fluid
    ) {
        CraftTweakerAPI.apply(new ActionAddCauldronRecipe(id, input, output, fluid));
    }

    @ZenCodeType.Method
    public static void removeRecipe(String id) {
        CraftTweakerAPI.apply(new ActionRemoveCauldronRecipe(id));
    }
}
