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
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class RecipeProvider extends FabricRecipeProvider {
    public RecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    public static final Identifier FILTER_VERIFICATION = Clover.id("filter_verification");
    public static final Identifier FILTER_COMBINATION = Clover.id("filter_combination");

    @Override
    public void buildRecipes(RecipeOutput recipeOutput) {
        for (Filter filter : Filter.craftableFilters()) addFilterRecipe(recipeOutput, filter);
        addFilterAdjusters(recipeOutput);
        addFilterTutorialBook(recipeOutput);
    }

    private void addFilterAdjusters(RecipeOutput recipeOutput) {
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

    private void addFilterTutorialBook(RecipeOutput recipeOutput) {
        ItemStack result = loadItemStack(Clover.id("item_filter_tutorial_item"));

        Identifier id = Clover.id("item_filter_tutorial");
        SmithingTransformRecipe recipe = new SmithingTransformRecipe(Ingredient.of(Items.HOPPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.WHITE_DYE), result);

        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        recipeOutput.accept(id, recipe, advancement.build(id.withPrefix("recipes/misc/")));
    }

    private static ItemStack loadItemStack(Identifier id) {
        try (InputStream stream = RecipeProvider.class.getResourceAsStream(String.format("/data/%s/item/%s.nbt", id.getNamespace(), id.getPath()))) {
            if (stream == null) throw new RuntimeException("Missing item resource: " + id);
            CompoundTag tag = NbtIo.read(new DataInputStream(stream));
            return ItemStack.OPTIONAL_CODEC
                    .decode(NbtOps.INSTANCE, tag)
                    .getOrThrow(e -> new IllegalStateException("Invalid item compound read from " + id + ": " + e))
                    .getFirst();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load item from resources: " + id, e);
        }
    }

    private static void addFilterRecipe(RecipeOutput recipeOutput, Filter filter) {
        ItemStack result = new ItemStack(Items.WRITABLE_BOOK, 1);
        CompoundTag tag = new CompoundTag();
        tag.put(Filter.FILTER_PATH, filter.toCompound());
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        filter.formatItem(result);

        Item dyeItem = filter.getSmithingItem();
        Identifier id = Clover.id("item_filter_" + filter.getFilterName().toLowerCase().replace(' ', '_'));

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
