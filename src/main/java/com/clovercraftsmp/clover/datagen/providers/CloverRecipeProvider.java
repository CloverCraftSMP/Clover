package com.clovercraftsmp.clover.datagen.providers;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.util.filter.Filter;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.concurrent.CompletableFuture;

//? if >= 26.1 {
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
//? }

//? if <= 1.21.1 {
/*import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
*///? }

@SuppressWarnings("CommentedOutCode")
public class CloverRecipeProvider extends FabricRecipeProvider {
    public static final Identifier FILTER_VERIFICATION = Clover.id("filter_verification");
    public static final Identifier FILTER_COMBINATION = Clover.id("filter_combination");
    public static final Identifier FILTER_TUTORIAL_ITEM = Clover.id("item_filter_tutorial_item");
    public static final Identifier FILTER_TUTORIAL = Clover.id("item_filter_tutorial");

    @SuppressWarnings({"AccessStaticViaInstance", "RedundantSuppression"})
    private static void generate(HolderLookup.Provider registries, RecipeOutput output, RecipeProvider provider) {
        for (Filter filter : Filter.craftableFilters()) {
            smithing(filter.getSmithingItem(), toResult(createFilterStack(filter)), filterRecipeId(filter), output, provider);
        }

        smithing(Items.WHITE_DYE, loadResult(registries, FILTER_TUTORIAL_ITEM), FILTER_TUTORIAL, output, provider);

        shapeless(provider, Items.WRITABLE_BOOK)
                .requires(Items.WRITABLE_BOOK)
                .unlockedBy(RecipeProvider.getHasName(Items.WRITABLE_BOOK), provider.has(Items.WRITABLE_BOOK))
                .save(output, key(FILTER_VERIFICATION));

        shaped(provider, Items.WRITABLE_BOOK)
                .pattern("##")
                .define('#', Items.WRITABLE_BOOK)
                .unlockedBy(RecipeProvider.getHasName(Items.WRITABLE_BOOK), provider.has(Items.WRITABLE_BOOK))
                .save(output, key(FILTER_COMBINATION));
    }

    private static Identifier filterRecipeId(Filter filter) {
        return Clover.id("item_filter_" + filter.getFilterName().toLowerCase().replace(' ','_'));
    }

    private static ItemStack createFilterStack(Filter filter) {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);
        CompoundTag tag = new CompoundTag();
        tag.put(Filter.FILTER_PATH, filter.toCompound());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        filter.formatItem(stack);
        return stack;
    }

    private static CompoundTag readItemNBT(Identifier id) {
        String path = String.format("/data/%s/item/%s.nbt", id.getNamespace(), id.getPath());
        try (InputStream stream = CloverRecipeProvider.class.getResourceAsStream(path)) {
            if (stream == null) throw new RuntimeException("Missing item resource: " + id);
            return NbtIo.read(new DataInputStream(stream));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load item from resources: " + id, e);
        }
    }

    //? if >= 26.1 {
    public CloverRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override @NotNull
    protected RecipeProvider createRecipeProvider(@NotNull HolderLookup.Provider registries, @NotNull RecipeOutput output) {
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                generate(registries, output, this);
            }
        };
    }

    @Override @NotNull
    public String getName() {
        return "Clover Recipes";
    }

    private static ResourceKey<Recipe<?>> key(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }

    @SuppressWarnings("SameParameterValue")
    private static ShapelessRecipeBuilder shapeless(RecipeProvider provider, Item result) {
        return provider.shapeless(RecipeCategory.MISC, result);
    }

    @SuppressWarnings("SameParameterValue")
    private static ShapedRecipeBuilder shaped(RecipeProvider provider, Item result) {
        return provider.shaped(RecipeCategory.MISC, result);
    }

    private static ItemStackTemplate toResult(ItemStack stack) {
        return ItemStackTemplate.fromNonEmptyStack(stack);
    }

    @SuppressWarnings("SameParameterValue")
    private static ItemStackTemplate loadResult(HolderLookup.Provider registries, Identifier id) {
        return ItemStackTemplate.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), readItemNBT(id))
                .getOrThrow(e -> new IllegalStateException("Invalid item compound read from " + id + ": " + e));
    }

    private static void smithing(Item addition, ItemStackTemplate result, Identifier id, RecipeOutput output, RecipeProvider provider) {
        new SmithingTransformRecipeBuilder(
                Ingredient.of(Items.HOPPER),
                Ingredient.of(Items.PAPER),
                Ingredient.of(addition),
                RecipeCategory.MISC,
                result)
                .unlocks(RecipeProvider.getHasName(Items.HOPPER), provider.has(Items.HOPPER))
                .save(output, key(id));
    }
    //? }

    //? if <= 1.21.1 {
    /*private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    public CloverRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
        this.registriesFuture = registriesFuture;
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        generate(registriesFuture.join(), output, this);
    }

    private static Identifier key(Identifier id) {
        return id;
    }

    @SuppressWarnings("SameParameterValue")
    private static ShapelessRecipeBuilder shapeless(@SuppressWarnings("unused") RecipeProvider provider, Item result) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result);
    }

    @SuppressWarnings("SameParameterValue")
    private static ShapedRecipeBuilder shaped(@SuppressWarnings("unused") RecipeProvider provider, Item result) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result);
    }

    private static ItemStack toResult(ItemStack stack) {
        return stack;
    }

    @SuppressWarnings("SameParameterValue")
    private static ItemStack loadResult(HolderLookup.Provider registries, Identifier id) {
        return ItemStack.OPTIONAL_CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), readItemNBT(id))
                .getOrThrow(e -> new IllegalStateException("Invalid item compound read from " + id + ": " + e));
    }

    private static void smithing(Item addition, ItemStack result, Identifier id, RecipeOutput output, @SuppressWarnings("unused") RecipeProvider provider) {
        SmithingTransformRecipe recipe = new SmithingTransformRecipe(
                Ingredient.of(Items.HOPPER), Ingredient.of(Items.PAPER), Ingredient.of(addition), result);

        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/misc/")));
    }
    *///? }
}
