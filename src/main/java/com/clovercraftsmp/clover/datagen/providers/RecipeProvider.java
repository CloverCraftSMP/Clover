package com.clovercraftsmp.clover.datagen.providers;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.util.filter.Filter;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class RecipeProvider extends FabricRecipeProvider {
    public RecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    public static final ResourceLocation FILTER_VERIFICATION = Clover.id("filter_verification");
    public static final ResourceLocation FILTER_COMBINATION = Clover.id("filter_combination");

    @Override
    public void buildRecipes(RecipeOutput recipeOutput) {
        for (Filter filter : Filter.craftableFilters()) addFilterRecipe(recipeOutput, filter);
        addFilterAdjusters(recipeOutput);
        addFilterTutorialBook(recipeOutput);
    }

    private static void addFilterAdjusters(RecipeOutput recipeOutput) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WRITABLE_BOOK)
                .requires(Items.WRITABLE_BOOK)
                .unlockedBy(getHasName(Items.WRITABLE_BOOK), has(Items.WRITABLE_BOOK))
                .save(recipeOutput, FILTER_VERIFICATION);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.WRITABLE_BOOK)
                .pattern("##")
                .define('#', Items.WRITABLE_BOOK)
                .unlockedBy(getHasName(Items.WRITABLE_BOOK), has(Items.WRITABLE_BOOK))
                .save(recipeOutput, FILTER_COMBINATION);
    }

    private static void addFilterTutorialBook(RecipeOutput recipeOutput) {
        ItemStack result = new ItemStack(Items.WRITTEN_BOOK, 1);

        List<Filterable<Component>> pages = Stream.of(
                "\n\n\n        How to use\n        Item Filters\n\n            v1.0\n\n         by Colonel",
                "Table of Contents\n\nIntroduction.......................4\nFilter Addition...................6\nField Adjustment............7\nUnpacking..........................10\nApplying..............................11\nFinalizing............................12\nFilter Types....................13",
                "Table of Contents\n\nContent Filter................14\nDurability Filter............17\nEnchantment Filter....18\nGroup Filter....................20\nName Filter.......................21\nTag Filter..........................23\nType Filter.......................24\nTips and Tricks............25\nConclusion........................26",
                "Introduction\n\nHello! This book acts as a guide for the item filter system.\n\nFirstly, filters are crafted through a smithing table. You need 1 hopper, 1 paper, and 1 dye. Use a recipe viewer for more info!",
                "Introduction\n\nThere are two ways in which you can update a filter:\n• Filter Addition\n• Field Adjustment \n\nTo put it simply: If you're editing some filter value, it's a field adjustment. If you're adding a sub-filter, it's an addition. ",
                "Filter Addition\n\nTake one filter (the base) and add a second filter (the sacrifice) to the crafting grid in the following shape:\n\n██ \n\nThe base goes on the left, the sacrifice on the right. ",
                "Field Adjustment\n\nField adjustment is a two-step process:\n\n1) Add the field to the filter through the book editor (write it in the book).\n\n2) Put the filter through a crafting table to apply the field adjustment.",
                "Field Adjustment\n\nThe fields are name-value pairs joined by '='. Ex: The following sets the mode to \"None\":\nmode=none\n\nNote: All modes are specified like that. \n\nFields are case-insensitive.",
                "Field Adjustment\n\nYou can specify multiple fields at once. Add a blank line between each field you want to enter.\n\nYou can use multiple pages to enter fields, but a field cannot span multiple pages.",
                "Unpacking\n\nIf you add a sub-filter to a filter and later want to undo that, you can sneak while right clicking with the filter to unpack it.\n\nThis is required if you want to replace a sub-filter with a different one.",
                "Applying\n\nTo apply a filter to a copper pipe, simply right click the pipe while holding the filter in your main hand slot.\n\nThis works for finalized as well as unfinalized filters.",
                "Finalizing\n\nWhen you're done modifying your filter, you have the option to finalize it by signing it and giving it a name.\n\nNote: If you do this, you can no longer edit the filter, so be careful! Also, this prevents unpacking!",
                "Filter Types\n\nThere are 7 types of filter as of v1.0:\n• Content............................14\n• Durability........................17\n• Enchantment................18\n• Group................................20\n• Name...................................21\n• Tag......................................23\n• Type...................................24",
                "Content Filter\n\nFilters shulkers and bundles based on their contents.\n\n'slot' is a whole number greater than 0. Examples:\n• slot=1\n• slot=27\n\nModes are specified on the next page.",
                "Content Filter\n\n'Any', 'All', and 'None' pass based on if any, all, or none of the container items match the sub-filter.\n\n'Empty' passes if the container is empty. 'Specific': Passes if the nth slot (starting at 1) passes the sub-filter.",
                "Content Filter\n\nShulker boxes include empty slots i.e slot=27 points to the last slot.\n\nThe 'All' mode fails on empty containers. \n\nUnspecified sub-filter always passes.\n\nSwapping sub-filter requires unpacking.",
                "Durability Filter\n\nFilters based on durability as a percentage. Ex:\nvalue=12.3%\nvalue=26\n\nAllowed modes are:\n• At least\n• At most\n\nItems without durability always fail.",
                "Enchantment Filter\n\nFilters based on enchantments. Also works with enchanted books!\n\nAllowed modes are:\n• At least\n• At most\n• Exact",
                "Enchantment Filter\n\nTo specify the enchantment, you need the target ID. Ex:\nenchant=mending\nenchant=minecraft:wow\nenchant=enderscape:rebound\n\nLevel is specified as a whole number. Ex:\nlevel=0\nlevel=4",
                "Group Filter\n\nCombines multiple filters into one.\n\nAllowed modes are:\n• Any\n• All\n• None\n\nSub-filters are added through filter addition and can later be all unpacked at once.",
                "Name Filter\n\nFilters based on normal or renamed item name.\n\nAllowed modes are:\n• Contains\n• Starts with\n• Ends with\n• Exact",
                "Name Filter\n\nSet the searched value as a string, i.e:\nvalue=red \nvalue=pickaxe\n\nOptionally, toggle case sensitivity with:\nsensitive=true\nsensitive=false",
                "Tag Filter\n\nFilters based on which tags item has. Use an item viewer for tags!\n\nAllowed modes are:\n• Any\n• All\n• None\n\nTo add/remove tags:\ntag=dirt\ntag=#c:buckets",
                "Type Filter\n\nFilters based on item type. Allowed modes:\n• Any\n• None\n\nTo add/remove types, use the identifier, i.e:\nitem=stone\nitem=minecraft:dirt",
                "Tips and Tricks\n\nA finalized filter can be duplicated just like a normal book.\n\nGroup / Content filters can be nested for more combinations.\n\nA group filter with mode 'none' works as an inverter for one supplied sub-filter.\n",
                "Conclusion\n\nI hope this book was helpful to you.\n\nIf you have any questions, comments, or concerns, let me know, and I'll try to improve in the next version!\n\n-Colonel"
                ).map(e -> Filterable.passThrough((Component) Component.literal(e)))
                .toList();

        result.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough("Filters v1.0"), "thecolonel63", 0, pages, true));

        ResourceLocation id = Clover.id("item_filter_tutorial");
        SmithingTransformRecipe recipe = new SmithingTransformRecipe(Ingredient.of(Items.HOPPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.WHITE_DYE), result);

        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        recipeOutput.accept(id, recipe, advancement.build(id.withPrefix("recipes/misc/")));
    }

    private static void addFilterRecipe(RecipeOutput recipeOutput, Filter filter) {
        ItemStack result = new ItemStack(Items.WRITABLE_BOOK, 1);
        CompoundTag tag = new CompoundTag();
        tag.put(Filter.FILTER_PATH, filter.toCompound());
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        filter.formatItem(result);

        Item dyeItem = filter.getSmithingItem();
        ResourceLocation id = Clover.id("item_filter_" + filter.getFilterName().toLowerCase().replace(' ', '_'));

        SmithingTransformRecipe recipe = new SmithingTransformRecipe(Ingredient.of(Items.HOPPER), Ingredient.of(Items.PAPER), Ingredient.of(dyeItem), result);

        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        recipeOutput.accept(
                id,
                recipe,
                advancement.build(id.withPrefix("recipes/misc/"))
        );
    }
}
