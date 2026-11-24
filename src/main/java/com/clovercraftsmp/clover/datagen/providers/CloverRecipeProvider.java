package com.clovercraftsmp.clover.datagen.providers;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.util.filter.Filter;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//? if >= 26.1 {
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
//? }

//? if <= 1.21.1 {
/*import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.ItemStack;
*///? }

@SuppressWarnings("CommentedOutCode")
public class CloverRecipeProvider extends FabricRecipeProvider {
    public static final Identifier FILTER_VERIFICATION = Clover.id("filters/filter_verification");
    public static final Identifier FILTER_COMBINATION = Clover.id("filters/filter_combination");
    public static final Identifier FILTER_TUTORIAL = Clover.id("filters/item_filter_tutorial");

    public static final Identifier FILTER_TUTORIAL_ITEM = Clover.id("item_filter_tutorial_item");

    @SuppressWarnings({"AccessStaticViaInstance", "RedundantSuppression"})
    private static void generate(HolderLookup.Provider registries, RecipeOutput output, @Nullable RecipeOutput dropsBackportOutput, RecipeProvider provider) {
        Recipes r = new Recipes(output, provider);

        // Filters
        for (Filter filter : Filter.craftableFilters())
            smithing(filter.getSmithingItem(), toResult(createFilterPatch(filter), filter.formatPatch()), filterRecipeId(filter), output, provider);

        smithing(Items.WHITE_DYE, loadResult(registries, FILTER_TUTORIAL_ITEM), FILTER_TUTORIAL, output, provider);

        Input book = Input.of(Items.WRITABLE_BOOK);

        ShapelessRecipeBuilder verification = shapeless(provider, Items.WRITABLE_BOOK, 1).requires(book.ingredient(provider));
        book.unlock(verification, provider);
        verification.save(output, key(FILTER_VERIFICATION));

        ShapedRecipeBuilder combination = shaped(provider, Items.WRITABLE_BOOK, 1).pattern("##").define('#', book.ingredient(provider));
        book.unlock(combination, provider);
        combination.save(output, key(FILTER_COMBINATION));

        // Stonecutting
        Recipes stonecutting = r.in("stonecutting");
        stonecutting.addStonecutter(Input.of(Items.COBBLESTONE), Items.GRAVEL, 1);
        stonecutting.addStonecutter(Input.of(Items.GRAVEL), Items.SAND, 1);
        stonecutting.addStonecutter(Input.of(Items.COARSE_DIRT), Items.DIRT, 1);
        stonecutting.addStonecutter(Input.of(ItemTags.WOOL), Items.STRING, 4);

        // Dyes
        Recipes dyes = r.in("dyes");
        for (DyeColor color : DyeColor.values()) {
            if (color == DyeColor.WHITE) continue;
            Item dye = colored(color, "_dye");
            dyes.addSurrounding(Input.of(Items.WHITE_DYE), Input.of(dye), Input.of(dye), dye, 9,
                    RecipeProvider.getItemName(dye) + "_duplication");
        }

        dyes.addShapeless(Items.BLACK_DYE, 1, Input.of(ItemTags.COALS));
        dyes.addShapeless(Items.GREEN_DYE, 2, Input.of(Items.YELLOW_DYE), Input.of(Items.BLUE_DYE));
        dyes.addShapeless(Items.BROWN_DYE, 2, Input.of(Items.ORANGE_DYE), Input.of(Items.BLACK_DYE));

        // Water bucket crafting
        Recipes buckets = r.in("bucket_crafting");
        for (DyeColor color : DyeColor.values())
            buckets.addBucket(Input.of(colored(color, "_concrete_powder")), colored(color, "_concrete"));

        buckets.addBucket(Input.of(Items.DIRT), Items.MUD);

        // Tall flowers
        Recipes tall_flowers = r.in("tall_flowers");
        tall_flowers.add2x2(Input.of(Items.POPPY), Items.ROSE_BUSH, 1);
        tall_flowers.add2x2(Input.of(Items.PINK_TULIP), Items.PEONY, 1);
        tall_flowers.add2x2(Input.of(Items.ALLIUM), Items.LILAC, 1);
        tall_flowers.add2x2(Input.of(Items.DANDELION), Items.SUNFLOWER, 1);

        // Stick recipe
        r.in("crafting").add1x2(Input.of(ItemTags.LOGS), Items.STICK, 16);

        // Wood stonecutting
        List<Family> families = new ArrayList<>();
        for (String material : List.of("acacia", "oak", "dark_oak", "spruce", "birch", "jungle", "cherry", "mangrove"))
            families.add(Family.wood(material));

        //? if >= 26.1
        families.add(Family.wood("pale_oak"));
        families.add(Family.nether("crimson"));
        families.add(Family.nether("warped"));
        families.add(Family.BAMBOO);

        List<String> logList = List.of("{MAT}_{LOG}", "stripped_{MAT}_{LOG}", "{MAT}_{WOOD}", "stripped_{MAT}_{WOOD}");
        List<StonecuttingRule> rules = new ArrayList<>();

        // Plank-tier rules
        rules.add(new StonecuttingRule(List.of("{MAT}_planks", "{MAT}_stairs", "{MAT}_fence", "{MAT}_pressure_plate", "{MAT}_sign"), "{MAT}_button", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_door", "{MAT}_fence_gate"), "{MAT}_button", 2));
        rules.add(new StonecuttingRule(List.of("{MAT}_planks", "{MAT}_stairs"), "{MAT}_fence", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_door"), "{MAT}_fence", 2));
        rules.add(new StonecuttingRule(List.of("{MAT}_stairs"), "{MAT}_planks", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_sign"), "{MAT}_planks", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_door", "{MAT}_sign", "{MAT}_trapdoor"), "{MAT}_pressure_plate", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_pressure_plate", "{MAT}_door"), "{MAT}_sign", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_planks"), "{MAT}_slab", 2));
        rules.add(new StonecuttingRule(List.of("{MAT}_door", "{MAT}_stairs"), "{MAT}_slab", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_planks"), "{MAT}_stairs", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_planks", "{MAT}_stairs"), "{MAT}_trapdoor", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_door"), "{MAT}_trapdoor", 2));

        // Log-tier rules
        rules.add(new StonecuttingRule(logList, "{MAT}_button", 4, 2));
        rules.add(new StonecuttingRule(logList, "{MAT}_door", 2, 1));
        rules.add(new StonecuttingRule(logList, "{MAT}_fence", 4, 2));
        rules.add(new StonecuttingRule(logList, "{MAT}_fence_gate", 1, 1));
        rules.add(new StonecuttingRule(logList, "{MAT}_planks", 4, 2));
        rules.add(new StonecuttingRule(logList, "{MAT}_pressure_plate", 2, 2));
        //? if >= 26.1
        rules.add(new StonecuttingRule(logList, "{MAT}_shelf", 1, 1));
        rules.add(new StonecuttingRule(logList, "{MAT}_sign", 2, 1));
        rules.add(new StonecuttingRule(logList, "{MAT}_slab", 8, 4));
        rules.add(new StonecuttingRule(logList, "{MAT}_stairs", 4, 2));
        rules.add(new StonecuttingRule(logList, "{MAT}_trapdoor", 4, 2));

        // Log / wood conversions
        rules.add(new StonecuttingRule(List.of("{MAT}_{WOOD}"), "{MAT}_{LOG}", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_{LOG}", "{MAT}_{WOOD}", "stripped_{MAT}_{WOOD}"), "stripped_{MAT}_{LOG}", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_hanging_sign"), "stripped_{MAT}_{LOG}", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_{WOOD}"), "stripped_{MAT}_{WOOD}", 1));
        rules.add(new StonecuttingRule(List.of("{MAT}_{LOG}"), "{MAT}_{WOOD}", 1));
        rules.add(new StonecuttingRule(List.of("stripped_{MAT}_{LOG}"), "stripped_{MAT}_{WOOD}", 1));

        //? if >= 26.1 {
        Family poplar = Family.wood("poplar");
        Recipes poplarOut = new Recipes(dropsBackportOutput, provider).in("wood").in("poplar");
        for (StonecuttingRule rule : rules) rule.addRecipes(poplarOut, poplar);
        //? }

        // Bamboo mosaic outputs
        List<StonecuttingRule> bambooRules = List.of(
                new StonecuttingRule(logList, "bamboo_mosaic", 2),
                new StonecuttingRule(logList, "bamboo_mosaic_slab", 4),
                new StonecuttingRule(logList, "bamboo_mosaic_stairs", 2),
                new StonecuttingRule(List.of("{MAT}_planks"), "bamboo_mosaic_slab", 2),
                new StonecuttingRule(List.of("{MAT}_planks"), "bamboo_mosaic_stairs", 1),
                new StonecuttingRule(List.of("{MAT}_planks", "{MAT}_stairs"), "bamboo_mosaic", 1),
                new StonecuttingRule(List.of("{MAT}_stairs"), "bamboo_mosaic_slab", 1),
                new StonecuttingRule(List.of("bamboo_mosaic"), "{MAT}_planks", 1)
        );

        Recipes wood = r.in("wood");
        for (Family type : families) {
            Recipes inMaterial = wood.in(type.name());
            for (StonecuttingRule rule : rules) rule.addRecipes(inMaterial, type);
            if (type == Family.BAMBOO)
                for (StonecuttingRule rule : bambooRules) rule.addRecipes(inMaterial, type);
        }

        // Metal: copper
        Recipes metal = r.in("metal");
        List<Family> copper = List.of(
                Family.of("copper", "P", "", "BLOCK", "copper_block"),
                Family.of("exposed_copper", "P", "exposed_", "BLOCK", "exposed_copper"),
                Family.of("weathered_copper", "P", "weathered_", "BLOCK", "weathered_copper"),
                Family.of("oxidized_copper", "P", "oxidized_", "BLOCK", "oxidized_copper"),
                Family.of("waxed_copper", "P", "waxed_", "BLOCK", "waxed_copper_block"),
                Family.of("waxed_exposed_copper", "P", "waxed_exposed_", "BLOCK", "waxed_exposed_copper"),
                Family.of("waxed_weathered_copper", "P", "waxed_weathered_", "BLOCK", "waxed_weathered_copper"),
                Family.of("waxed_oxidized_copper", "P", "waxed_oxidized_", "BLOCK", "waxed_oxidized_copper"));

        List<String> cutCopper = List.of("{P}cut_copper", "{P}cut_copper_stairs", "{P}chiseled_copper");
        List<String> cutCopperAndGrate = List.of("{P}cut_copper", "{P}cut_copper_stairs", "{P}chiseled_copper", "{P}copper_grate");

        List<StonecuttingRule> copperRules = new ArrayList<>(List.of(
                new StonecuttingRule(List.of("{BLOCK}"), "{P}copper_door", 4),
                new StonecuttingRule(List.of("{BLOCK}"), "{P}copper_trapdoor", 4),
                new StonecuttingRule(List.of("{P}copper_door"), "{P}copper_trapdoor", 2),
                new StonecuttingRule(cutCopperAndGrate, "{P}copper_trapdoor", 1),
                new StonecuttingRule(List.of("{P}chiseled_copper"), "{P}cut_copper", 1),
                new StonecuttingRule(List.of("{P}chiseled_copper"), "{P}cut_copper_slab", 2),
                new StonecuttingRule(List.of("{P}chiseled_copper"), "{P}cut_copper_stairs", 1),
                new StonecuttingRule(List.of("{P}cut_copper_stairs"), "{P}chiseled_copper", 1),
                new StonecuttingRule(List.of("{P}cut_copper_stairs"), "{P}cut_copper", 1),
                new StonecuttingRule(List.of("{P}cut_copper_stairs"), "{P}cut_copper_slab", 1)));
        //? if >= 26.1 {
        copperRules.add(new StonecuttingRule(List.of("{BLOCK}"), "{P}copper_bars", 24));
        copperRules.add(new StonecuttingRule(List.of("{BLOCK}"), "{P}copper_chain", 8));
        copperRules.add(new StonecuttingRule(List.of("{BLOCK}"), "{P}lightning_rod", 4));
        copperRules.add(new StonecuttingRule(cutCopperAndGrate, "{P}copper_bars", 6));
        copperRules.add(new StonecuttingRule(cutCopperAndGrate, "{P}copper_chain", 2));
        copperRules.add(new StonecuttingRule(cutCopper, "{P}lightning_rod", 1));
        //? }

        applyRules(metal, copper, copperRules);

        // Metal: iron
        Recipes iron = metal.in("iron");
        iron.cut("heavy_weighted_pressure_plate", 1, "iron_trapdoor");
        iron.cut("iron_bars", 24, "iron_block");
        //? if >= 26.1
        iron.cut("iron_chain", 8, "iron_block");
        //? if <= 1.21.1
        //iron.cut("chain", 8, "iron_block");
        iron.cut("iron_door", 4, "iron_block");
        iron.cut("iron_ingot", 3, "bucket");
        iron.cut("iron_ingot", 5, "minecart", "hopper");
        iron.cut("iron_ingot", 7, "cauldron");
        iron.cut("iron_ingot", 9, "iron_block");
        iron.addStonecutter(Input.of(ItemTags.HANGING_SIGNS), Items.IRON_NUGGET, 3);
        iron.cut("iron_nugget", 3, "rail");
        iron.cut("iron_nugget", 5, "activator_rail", "detector_rail");
        iron.cut("iron_trapdoor", 2, "iron_door");
        iron.cut("iron_trapdoor", 4, "iron_block");

        // Metal: gold
        Recipes gold = metal.in("gold");
        gold.cut("gold_nugget", 5, "powered_rail");

        // Stone: granite, diorite, andesite
        Recipes stone = r.in("stone");

        List<StonecuttingRule> polishableStone = List.of(
                new StonecuttingRule(List.of("{S}_stairs", "polished_{S}", "polished_{S}_stairs"), "{S}_wall", 1),
                new StonecuttingRule(List.of("{S}_stairs"), "polished_{S}_stairs", 1),
                new StonecuttingRule(List.of("{S}_stairs", "polished_{S}", "polished_{S}_stairs"), "{S}", 1),
                new StonecuttingRule(List.of("{S}_stairs", "polished_{S}_stairs"), "polished_{S}", 1),
                new StonecuttingRule(List.of("{S}_stairs", "polished_{S}_stairs", "{S}_slab"), "polished_{S}_slab", 1),
                new StonecuttingRule(List.of("{S}_stairs", "polished_{S}_stairs", "polished_{S}_slab"), "{S}_slab", 1),
                new StonecuttingRule(List.of("polished_{S}"), "{S}_slab", 2),
                new StonecuttingRule(List.of("polished_{S}", "polished_{S}_stairs"), "{S}_stairs", 1));

        applyRules(stone, List.of(
                Family.of("granite", "S", "granite"),
                Family.of("diorite", "S", "diorite"),
                Family.of("andesite", "S", "andesite")), polishableStone);

        // Stone: sandstone, red sandstone
        List<StonecuttingRule> sandstoneRules = List.of(
                new StonecuttingRule(List.of("chiseled_{S}", "cut_{S}", "smooth_{S}"), "{S}_slab", 2),
                new StonecuttingRule(List.of("chiseled_{S}", "cut_{S}", "smooth_{S}", "{S}_stairs", "smooth_{S}_stairs"), "{S}_wall", 1),
                new StonecuttingRule(List.of("chiseled_{S}", "cut_{S}", "smooth_{S}", "smooth_{S}_stairs"), "{S}_stairs", 1),
                new StonecuttingRule(List.of("cut_{S}", "{S}_stairs", "smooth_{S}"), "{S}", 1),
                new StonecuttingRule(List.of("{S}", "{S}_stairs", "smooth_{S}", "smooth_{S}_stairs", "cut_{S}", "chiseled_{S}"), "{SAND}", 4),
                new StonecuttingRule(List.of("{S}_stairs", "smooth_{S}_stairs"), "{S}_slab", 1),
                new StonecuttingRule(List.of("smooth_{S}_stairs"), "smooth_{S}", 1),
                new StonecuttingRule(List.of("smooth_{S}_stairs"), "smooth_{S}_slab", 1));

        applyRules(stone, List.of(
                Family.of("sandstone", "S", "sandstone", "SAND", "sand"),
                Family.of("red_sandstone", "S", "red_sandstone", "SAND", "red_sand")), sandstoneRules);

        // Stone: simple stair families (stairs -> block, slab, wall)
        List<StonecuttingRule> stairFamily = List.of(
                new StonecuttingRule(List.of("{X}_stairs"), "{B}", 1),
                new StonecuttingRule(List.of("{X}_stairs"), "{X}_slab", 1),
                new StonecuttingRule(List.of("{X}_stairs"), "{X}_wall", 1));

        applyRules(stone, List.of(
                Family.of("bricks", "X", "brick", "B", "bricks"),
                Family.of("mud_bricks", "X", "mud_brick", "B", "mud_bricks"),
                Family.of("end_stone_bricks", "X", "end_stone_brick", "B", "end_stone_bricks"),
                Family.of("red_nether_bricks", "X", "red_nether_brick", "B", "red_nether_bricks"),
                Family.of("mossy_stone_bricks", "X", "mossy_stone_brick", "B", "mossy_stone_bricks"),
                Family.of("mossy_cobblestone", "X", "mossy_cobblestone", "B", "mossy_cobblestone")), stairFamily);

        // Stone: extras for the stair families
        Recipes bricks = stone.in("bricks");
        bricks.cut("brick", 3, "flower_pot");
        bricks.cut("brick", 4, "bricks");

        Recipes mudBricks = stone.in("mud_bricks");
        mudBricks.cut("mud_brick_slab", 2, "packed_mud");
        mudBricks.cut("mud_brick_stairs", 1, "packed_mud");
        mudBricks.cut("mud_brick_wall", 1, "packed_mud");
        mudBricks.cut("mud_bricks", 1, "packed_mud");
        mudBricks.cut("packed_mud", 1, "mud_bricks");

        stone.in("end_stone_bricks").cut("end_stone", 1, "end_stone_bricks");
        stone.in("red_nether_bricks").cut("nether_brick", 2, "red_nether_bricks");

        // Stone: basalt
        stone.in("basalt").cut("basalt", 1, "polished_basalt", "smooth_basalt");

        // Stone: blackstone
        Recipes blackstone = stone.in("blackstone");
        blackstone.cut("blackstone", 1, "blackstone_stairs", "polished_blackstone", "polished_blackstone_stairs");
        blackstone.cut("blackstone_slab", 1, "blackstone_stairs", "polished_blackstone", "polished_blackstone_stairs", "polished_blackstone_slab");
        blackstone.cut("blackstone_stairs", 1, "polished_blackstone", "polished_blackstone_stairs");
        blackstone.cut("blackstone_wall", 1, "blackstone_stairs", "polished_blackstone", "polished_blackstone_stairs", "polished_blackstone_wall");
        blackstone.cut("cracked_polished_blackstone_bricks", 1, "blackstone", "polished_blackstone_brick_stairs");
        blackstone.cut("polished_blackstone", 1, "chiseled_polished_blackstone", "polished_blackstone_bricks", "polished_blackstone_stairs");
        blackstone.cut("polished_blackstone_brick_slab", 1, "polished_blackstone_stairs", "polished_blackstone_slab", "polished_blackstone_brick_stairs");
        blackstone.cut("polished_blackstone_brick_stairs", 1, "polished_blackstone_stairs");
        blackstone.cut("polished_blackstone_brick_wall", 1, "polished_blackstone_stairs", "polished_blackstone_wall", "polished_blackstone_brick_stairs");
        blackstone.cut("polished_blackstone_bricks", 1, "polished_blackstone_brick_stairs", "polished_blackstone_stairs");
        blackstone.cut("polished_blackstone_button", 2, "blackstone_stairs", "polished_blackstone_stairs", "polished_blackstone_brick_stairs");
        blackstone.cut("polished_blackstone_button", 4, "blackstone", "polished_blackstone", "polished_blackstone_bricks");
        blackstone.cut("polished_blackstone_pressure_plate", 1, "blackstone", "polished_blackstone_stairs");
        blackstone.cut("polished_blackstone_slab", 1, "polished_blackstone_stairs");
        blackstone.cut("polished_blackstone_slab", 2, "chiseled_polished_blackstone");
        blackstone.cut("polished_blackstone_stairs", 1, "chiseled_polished_blackstone");
        blackstone.cut("polished_blackstone_wall", 1, "chiseled_polished_blackstone", "polished_blackstone_stairs");

        // Stone: deepslate
        Recipes deepslate = stone.in("deepslate");
        deepslate.cut("cobbled_deepslate", 1, "cobbled_deepslate_stairs", "chiseled_deepslate", "polished_deepslate_stairs");
        deepslate.cut("cobbled_deepslate_slab", 1, "cobbled_deepslate_stairs", "polished_deepslate_stairs", "polished_deepslate_slab");
        deepslate.cut("cobbled_deepslate_slab", 2, "polished_deepslate", "chiseled_deepslate");
        deepslate.cut("cobbled_deepslate_stairs", 1, "chiseled_deepslate", "polished_deepslate", "polished_deepslate_stairs");
        deepslate.cut("cobbled_deepslate_wall", 1, "cobbled_deepslate_stairs", "polished_deepslate_stairs", "chiseled_deepslate", "polished_deepslate", "polished_deepslate_wall");
        deepslate.cut("cracked_deepslate_bricks", 1, "cobbled_deepslate", "deepslate", "deepslate_brick_stairs");
        deepslate.cut("cracked_deepslate_tiles", 1, "cobbled_deepslate", "deepslate", "deepslate_tile_stairs");
        deepslate.cut("deepslate_brick_slab", 1, "cobbled_deepslate_slab", "cobbled_deepslate_stairs", "deepslate_brick_stairs", "polished_deepslate_stairs", "polished_deepslate_slab");
        deepslate.cut("deepslate_brick_stairs", 1, "cobbled_deepslate_stairs", "polished_deepslate_stairs");
        deepslate.cut("deepslate_brick_wall", 1, "deepslate_brick_stairs", "cobbled_deepslate_stairs", "cobbled_deepslate_wall", "polished_deepslate_stairs", "polished_deepslate_wall");
        deepslate.cut("deepslate_bricks", 1, "deepslate_brick_stairs", "cobbled_deepslate_stairs", "polished_deepslate_stairs");
        deepslate.cut("deepslate_tile_slab", 1, "cobbled_deepslate_slab", "deepslate_tile_stairs", "deepslate_brick_slab", "polished_deepslate_slab", "cobbled_deepslate_stairs", "polished_deepslate_stairs", "deepslate_brick_stairs");
        deepslate.cut("deepslate_tile_stairs", 1, "cobbled_deepslate_stairs", "polished_deepslate_stairs", "deepslate_brick_stairs");
        deepslate.cut("deepslate_tile_wall", 1, "cobbled_deepslate_stairs", "polished_deepslate_stairs", "deepslate_brick_stairs", "cobbled_deepslate_wall", "polished_deepslate_wall", "deepslate_brick_wall", "deepslate_tile_stairs");
        deepslate.cut("deepslate_tiles", 1, "cobbled_deepslate_stairs", "polished_deepslate_stairs", "deepslate_brick_stairs", "deepslate_tile_stairs");
        deepslate.cut("polished_deepslate", 1, "polished_deepslate_stairs", "cobbled_deepslate_stairs");
        deepslate.cut("polished_deepslate_slab", 1, "polished_deepslate_stairs", "cobbled_deepslate_stairs", "cobbled_deepslate_slab");
        deepslate.cut("polished_deepslate_stairs", 1, "cobbled_deepslate_stairs");
        deepslate.cut("polished_deepslate_wall", 1, "polished_deepslate_stairs", "cobbled_deepslate_stairs", "cobbled_deepslate_wall");

        // Stone: nether bricks
        Recipes netherBricks = stone.in("nether_bricks");
        netherBricks.cut("cracked_nether_bricks", 1, "nether_bricks");
        netherBricks.cut("nether_brick", 4, "nether_bricks");
        netherBricks.cut("nether_brick_fence", 1, "chiseled_nether_bricks", "nether_brick_stairs");
        netherBricks.cut("nether_brick_slab", 1, "nether_brick_stairs");
        netherBricks.cut("nether_brick_slab", 2, "chiseled_nether_bricks");
        netherBricks.cut("nether_brick_stairs", 1, "chiseled_nether_bricks");
        netherBricks.cut("nether_brick_wall", 1, "chiseled_nether_bricks", "nether_brick_stairs");
        netherBricks.cut("nether_bricks", 1, "chiseled_nether_bricks", "nether_brick_stairs");

        // Stone: prismarine (stairs give 2 slabs here, so not a simple stair family)
        Recipes prismarine = stone.in("prismarine");
        prismarine.cut("prismarine", 1, "prismarine_stairs");
        prismarine.cut("prismarine_shard", 4, "prismarine");
        prismarine.cut("prismarine_slab", 2, "prismarine_stairs");
        prismarine.cut("prismarine_wall", 1, "prismarine_stairs");

        Recipes prismarineBricks = stone.in("prismarine_bricks");
        prismarineBricks.cut("prismarine_brick_slab", 2, "prismarine_brick_stairs");
        prismarineBricks.cut("prismarine_bricks", 1, "prismarine_brick_stairs");
        prismarineBricks.cut("prismarine_shard", 9, "prismarine_bricks");

        Recipes darkPrismarine = stone.in("dark_prismarine");
        darkPrismarine.cut("dark_prismarine", 1, "dark_prismarine_stairs");
        darkPrismarine.cut("dark_prismarine_slab", 2, "dark_prismarine_stairs");
        darkPrismarine.cut("prismarine_shard", 8, "dark_prismarine");

        // Stone: purpur
        Recipes purpur = stone.in("purpur");
        purpur.cut("popped_chorus_fruit", 4, "purpur_block", "purpur_pillar");
        purpur.cut("purpur_block", 1, "purpur_pillar", "purpur_stairs");
        purpur.cut("purpur_slab", 1, "purpur_stairs");
        purpur.cut("purpur_slab", 2, "purpur_pillar");

        // Stone: stone and cobblestone
        Recipes stoneFamily = stone.in("stone");
        stoneFamily.cut("cobblestone", 1, "stone_stairs", "cobblestone_stairs", "mossy_cobblestone", "mossy_cobblestone_stairs");
        stoneFamily.cut("cobblestone_slab", 1, "stone_stairs", "stone_slab", "cobblestone_stairs", "mossy_cobblestone_stairs", "mossy_cobblestone_slab");
        stoneFamily.cut("cobblestone_slab", 2, "mossy_cobblestone");
        stoneFamily.cut("cobblestone_stairs", 1, "stone_stairs", "mossy_cobblestone", "mossy_cobblestone_stairs");
        stoneFamily.cut("cobblestone_wall", 1, "stone_stairs", "cobblestone_stairs", "mossy_cobblestone", "mossy_cobblestone_stairs", "mossy_cobblestone_wall");
        stoneFamily.cut("stone", 1, "smooth_stone", "stone_stairs");
        stoneFamily.cut("stone_button", 1, "stone_stairs");
        stoneFamily.cut("stone_button", 4, "stone");
        stoneFamily.cut("stone_pressure_plate", 1, "stone", "stone_stairs");
        stoneFamily.cut("stone_slab", 1, "stone_stairs");

        // Stone: stone bricks
        Recipes stoneBricks = stone.in("stone_bricks");
        stoneBricks.cut("chiseled_stone_bricks", 1, "stone_stairs");
        stoneBricks.cut("stone_brick_slab", 1, "stone_stairs", "stone_slab", "stone_brick_stairs", "mossy_stone_brick_stairs", "mossy_stone_brick_slab");
        stoneBricks.cut("stone_brick_slab", 2, "chiseled_stone_bricks", "mossy_stone_bricks");
        stoneBricks.cut("stone_brick_stairs", 1, "stone_stairs", "chiseled_stone_bricks", "mossy_stone_bricks", "mossy_stone_brick_stairs");
        stoneBricks.cut("stone_brick_wall", 1, "stone_stairs", "stone_brick_stairs", "mossy_stone_brick_stairs", "mossy_stone_bricks", "mossy_stone_brick_wall", "chiseled_stone_bricks");
        stoneBricks.cut("stone_bricks", 1, "stone_stairs", "stone_brick_stairs", "chiseled_stone_bricks", "mossy_stone_bricks", "mossy_stone_brick_stairs");

        // Stone: tuff
        Recipes tuff = stone.in("tuff");
        tuff.cut("chiseled_tuff", 1, "tuff_stairs", "polished_tuff", "polished_tuff_stairs");
        tuff.cut("chiseled_tuff_bricks", 1, "tuff_stairs", "tuff_brick_stairs", "polished_tuff_stairs");
        tuff.cut("polished_tuff", 1, "tuff_stairs", "polished_tuff_stairs");
        tuff.cut("polished_tuff_slab", 1, "tuff_stairs", "tuff_slab", "polished_tuff_stairs");
        tuff.cut("polished_tuff_stairs", 1, "tuff_stairs");
        tuff.cut("polished_tuff_wall", 1, "tuff_stairs", "tuff_wall", "polished_tuff_stairs");
        tuff.cut("tuff", 1, "tuff_stairs", "polished_tuff", "polished_tuff_stairs", "chiseled_tuff");
        tuff.cut("tuff_brick_slab", 1, "tuff_stairs", "tuff_slab", "polished_tuff_stairs", "tuff_brick_stairs", "polished_tuff_slab");
        tuff.cut("tuff_brick_slab", 2, "chiseled_tuff_bricks");
        tuff.cut("tuff_brick_stairs", 1, "tuff_stairs", "polished_tuff_stairs", "chiseled_tuff_bricks");
        tuff.cut("tuff_brick_wall", 1, "tuff_stairs", "tuff_wall", "tuff_brick_stairs", "chiseled_tuff_bricks", "polished_tuff_stairs", "polished_tuff_wall");
        tuff.cut("tuff_bricks", 1, "tuff_stairs", "tuff_brick_stairs", "chiseled_tuff_bricks", "polished_tuff_stairs");
        tuff.cut("tuff_slab", 1, "tuff_stairs", "polished_tuff_slab", "polished_tuff_stairs");
        tuff.cut("tuff_slab", 2, "polished_tuff", "chiseled_tuff");
        tuff.cut("tuff_stairs", 1, "polished_tuff", "polished_tuff_stairs", "chiseled_tuff");
        tuff.cut("tuff_wall", 1, "tuff_stairs", "polished_tuff", "chiseled_tuff", "polished_tuff_wall", "polished_tuff_stairs");

        // Quartz
        Recipes quartz = r.in("quartz");
        quartz.in("amethyst").cut("amethyst_shard", 4, "amethyst_block");

        Recipes netherQuartz = quartz.in("nether_quartz");
        netherQuartz.cut("quartz", 4, "quartz_block", "smooth_quartz", "chiseled_quartz_block", "quartz_bricks", "quartz_pillar");
        netherQuartz.cut("quartz_block", 1, "smooth_quartz", "chiseled_quartz_block", "quartz_bricks", "quartz_pillar", "quartz_stairs", "smooth_quartz_stairs");
        netherQuartz.cut("quartz_slab", 1, "quartz_stairs", "smooth_quartz_stairs");
        netherQuartz.cut("quartz_slab", 2, "chiseled_quartz_block", "quartz_bricks", "quartz_pillar", "smooth_quartz");
        netherQuartz.cut("quartz_stairs", 1, "chiseled_quartz_block", "quartz_bricks", "quartz_pillar", "smooth_quartz");
        netherQuartz.cut("smooth_quartz", 1, "smooth_quartz_stairs");
        netherQuartz.cut("smooth_quartz_slab", 1, "smooth_quartz_stairs");

        // Organic: resin (resin bricks were added in 1.21.4)
        //? if >= 26.1 {
        Recipes resin = r.in("organic").in("resin");
        resin.cut("chiseled_resin_bricks", 1, "resin_brick_stairs");
        resin.cut("resin_brick_slab", 1, "resin_brick_stairs");
        resin.cut("resin_brick_slab", 2, "chiseled_resin_bricks");
        resin.cut("resin_brick_stairs", 1, "chiseled_resin_bricks");
        resin.cut("resin_brick_wall", 1, "resin_brick_stairs", "chiseled_resin_bricks");
        resin.cut("resin_bricks", 1, "resin_brick_stairs", "chiseled_resin_bricks");
        //? }

        // Misc
        r.in("misc").cut("string", 9, "cobweb");
    }

    private record Family(String name, String namespace, Map<String, String> vars, boolean blockTier, Map<String, List<String>> inputAliases) {
        private static final Pattern VAR = Pattern.compile("\\{([A-Z]+)}");

        static Family of(String name, String... kv) {
            Map<String, String> vars = new HashMap<>();
            for (int i = 0; i < kv.length; i += 2) vars.put(kv[i], kv[i+1]);
            return new Family(name, "minecraft", vars, false, Map.of());
        }

        Family inNamespace(String namespace) {
            return new Family(name, namespace, vars, blockTier, inputAliases);
        }

        static Family wood(String name)   { return of(name, "MAT", name, "LOG", "log", "WOOD", "wood"); }
        static Family nether(String name) { return of(name, "MAT", name, "LOG", "stem", "WOOD", "hyphae"); }

        static final Family BAMBOO = new Family("bamboo", "minecraft", Map.of("MAT", "bamboo", "LOG", "block"), true, Map.of(
                "bamboo_planks", List.of("bamboo_planks", "bamboo_mosaic"),
                "bamboo_stairs", List.of("bamboo_stairs", "bamboo_mosaic_stairs")));

        Optional<String> resolve(String template) {
            Matcher m = VAR.matcher(template);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String value = vars.get(m.group(1));
                if (value == null) return Optional.empty();
                m.appendReplacement(sb, value);
            }
            m.appendTail(sb);
            return Optional.of(sb.toString());
        }

        List<String> resolveInput(String template) {
            return resolve(template).map(s -> inputAliases.getOrDefault(s, List.of(s))).orElse(List.of());
        }
    }

    private record StonecuttingRule(List<String> inputs, String output, int count, int blockCount) {
        StonecuttingRule(List<String> inputs, String output, int count) {
            this(inputs, output, count, count);
        }

        void addRecipes(Recipes r, Family type) {
            Optional<String> out = type.resolve(output);
            if (out.isEmpty()) return;
            Item outputItem = find(type.namespace(), out.get());
            int n = type.blockTier() ? blockCount : count;

            for (String template : inputs)
                for (String in : type.resolveInput(template))
                    if (!in.equals(out.get()))
                        r.addStonecutter(Input.of(find(type.namespace(), in)), outputItem, n);
        }
    }

    private record Input(@Nullable Item item, @Nullable TagKey<Item> tag) {
        static Input of(Item item) { return new Input(item, null); }
        static Input of(TagKey<Item> tag) { return new Input(null, tag); }

        String name() {
            return item != null ? RecipeProvider.getItemName(item) : tag.location().getPath().replace('/','_');
        }

        Ingredient ingredient(RecipeProvider provider) {
            if (item != null) return Ingredient.of(item);
            //? if >= 26.1
            return provider.tag(tag);
            //? if <= 1.21.1
            //return Ingredient.of(tag);
        }

        void unlock(RecipeBuilder builder, RecipeProvider provider) {
           builder.unlockedBy("has_" + name(), item != null ? provider.has(item) : provider.has(tag));
        }
    }

    private record Recipes(RecipeOutput output, RecipeProvider provider, String dir) {
        Recipes(RecipeOutput output, RecipeProvider provider) {
            this(output, provider, "");
        }

        Recipes in(String sub) {
            return new Recipes(output, provider, resolve(sub));
        }

        private String resolve(String name) {
            return dir.isEmpty() ? name : dir + "/" + name;
        }

        void addShapeless(Item out, int count, Input first, Input... rest) {
            ShapelessRecipeBuilder builder = shapeless(provider, out, count).requires(first.ingredient(provider));
            for (Input input : rest) builder.requires(input.ingredient(provider));

            Set<Input> distinct = new LinkedHashSet<>();
            distinct.add(first);
            distinct.addAll(Arrays.asList(rest));

            save(builder, from(out, null, distinct.toArray(Input[]::new)), distinct.toArray(Input[]::new));
        }

        void add2x2(Input in, Item out, int count) {
            ShapedRecipeBuilder builder = shaped(provider, out, count)
                    .pattern("##")
                    .pattern("##")
                    .define('#', in.ingredient(provider));
            save(builder, from(out, "2x2", in), in);
        }

        void add1x2(Input in, Item out, int count) {
            ShapedRecipeBuilder builder = shaped(provider, out, count)
                    .pattern("#")
                    .pattern("#")
                    .define('#', in.ingredient(provider));
            save(builder, from(out, "1x2", in), in);
        }

        void addStonecutter(Input in, Item out, int count) {
            SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(in.ingredient(provider), RecipeCategory.BUILDING_BLOCKS, out, count);
            save(builder, from(out, "stonecutting", in), in);
        }

        void cut(String out, int count, String... inputs) {
            Item outItem = find(out);
            for (String in : inputs) addStonecutter(Input.of(find(in)), outItem, count);
        }

        void addSurrounding(Input outer, Input inner, Input unlocks, Item out, int count, String path) {
            ShapedRecipeBuilder builder = shaped(provider, out, count)
                    .pattern("AAA")
                    .pattern("ABA")
                    .pattern("AAA")
                    .define('A', outer.ingredient(provider))
                    .define('B', inner.ingredient(provider));
            save(builder, path, unlocks);
        }

        void addBucket(Input in, Item out) {
            addSurrounding(in, Input.of(Items.WATER_BUCKET), in, out, 8, RecipeProvider.getItemName(out) + "_bucket_crafting");
        }

        private void save(RecipeBuilder builder, String name, Input... unlocks) {
            for (Input input : unlocks) input.unlock(builder, provider);
            builder.save(output, key(Clover.id(resolve(name))));
        }
    }

    private static Item colored(DyeColor color, String suffix) {
        return find(color.getSerializedName() + suffix);
    }

    private static Item find(String path) {
        return find("minecraft", path);
    }

    private static Item find(String namespace, String path) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        return BuiltInRegistries.ITEM.getOptional(id)
                .orElseThrow(() -> new IllegalStateException("No such item: " + id));
    }

    private static void applyRules(Recipes base, List<Family> families, List<StonecuttingRule> rules) {
        for (Family f : families) {
            Recipes in = base.in(f.name());
            for (StonecuttingRule rule : rules) rule.addRecipes(in, f);
        }
    }

    private static String from(Item out, @Nullable String suffix, Input... in) {
        String path = RecipeProvider.getItemName(out) + "_from_" +
                Arrays.stream(in).distinct().map(Input::name).collect(Collectors.joining("_and_"));
        return suffix == null ? path : path + "_" + suffix;
    }

    private static Identifier filterRecipeId(Filter filter) {
        return Clover.id("filters/item_filter_" + filter.getFilterName().toLowerCase().replace(' ', '_'));
    }

    private static DataComponentPatch createFilterPatch(Filter filter) {
        CompoundTag tag = new CompoundTag();
        tag.put(Filter.FILTER_PATH, filter.toCompound());
        return DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
                .build();
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
        RecipeOutput dropsBackport = withConditions(output, ResourceConditions.allModsLoaded("drops_backport"));
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                generate(registries, output, dropsBackport, this);
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

    private static ShapelessRecipeBuilder shapeless(RecipeProvider provider, Item result, int count) {
        return provider.shapeless(RecipeCategory.MISC, result, count);
    }

    private static ShapedRecipeBuilder shaped(RecipeProvider provider, Item result, int count) {
        return provider.shaped(RecipeCategory.MISC, result, count);
    }

    private static ItemStackTemplate toResult(DataComponentPatch data, DataComponentPatch format) {
        DataComponentPatch.Builder b = DataComponentPatch.builder();
        for (DataComponentPatch p : List.of(data, format))
            p.entrySet().forEach(e -> e.getValue().ifPresentOrElse(v -> b.set((DataComponentType) e.getKey(), v), () -> b.remove(e.getKey())));
        return new ItemStackTemplate(Items.WRITABLE_BOOK, b.build());
    }

    private static final int ITEM_NBT_DATA_VERSION = 3955;

    @SuppressWarnings("SameParameterValue")
    private static ItemStackTemplate loadResult(HolderLookup.Provider registries, Identifier id) {
        Dynamic<Tag> upgraded = DataFixers.getDataFixer().update(
                References.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, readItemNBT(id)),
                ITEM_NBT_DATA_VERSION,
                SharedConstants.getCurrentVersion().dataVersion().version()
        );

        return ItemStackTemplate.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), upgraded.getValue())
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
        generate(registriesFuture.join(), output, null, this);
    }

    private static Identifier key(Identifier id) {
        return id;
    }

    private static ShapelessRecipeBuilder shapeless(@SuppressWarnings("unused") RecipeProvider provider, Item result, int count) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result, count);
    }

    @SuppressWarnings("SameParameterValue")
    private static ShapedRecipeBuilder shaped(@SuppressWarnings("unused") RecipeProvider provider, Item result, int count) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, count);
    }

    private static ItemStack toResult(DataComponentPatch data, DataComponentPatch format) {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);
        stack.applyComponents(data);
        stack.applyComponents(format);
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
                .addCriterion(RecipeProvider.getHasName(Items.HOPPER), RecipeProvider.has(Items.HOPPER))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/misc/")));
    }
    *///? }
}
