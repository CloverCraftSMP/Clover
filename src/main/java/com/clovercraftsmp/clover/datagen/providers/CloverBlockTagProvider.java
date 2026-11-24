package com.clovercraftsmp.clover.datagen.providers;

//? if >= 26.1 {
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
//? }

//? if <= 1.21.1 {
/*import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
*///? }

import com.clovercraftsmp.clover.tags.CloverTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

//? if >= 26.1 {
public class CloverBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public CloverBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }
//? }

//? if <= 1.21.1 {
 /*public class CloverBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public CloverBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }
*///? }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        //? if >= 26.1
        valueLookupBuilder(CloverTags.DUPLICABLE_FLOWERS)
        //? if <= 1.21.1
        //getOrCreateTagBuilder(CloverTags.DUPLICABLE_FLOWERS)
                .add(Blocks.DANDELION)
                .add(Blocks.POPPY)
                .add(Blocks.BLUE_ORCHID)
                .add(Blocks.ALLIUM)
                .add(Blocks.AZURE_BLUET)
                .add(Blocks.RED_TULIP)
                .add(Blocks.ORANGE_TULIP)
                .add(Blocks.WHITE_TULIP)
                .add(Blocks.PINK_TULIP)
                .add(Blocks.OXEYE_DAISY)
                .add(Blocks.CORNFLOWER)
                .add(Blocks.LILY_OF_THE_VALLEY)
                .add(Blocks.LILY_PAD)
                //? if >= 26.1 {
                .add(Blocks.OPEN_EYEBLOSSOM)
                .add(Blocks.CLOSED_EYEBLOSSOM)
                //? }
                ;
    }
}