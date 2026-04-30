package com.clovercraftsmp.clover.datagen.providers;

import com.clovercraftsmp.clover.structure.ModStructures;
import com.clovercraftsmp.clover.structure.StructureEntry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StructureSetProvider extends FabricDynamicRegistryProvider {

    public StructureSetProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        var structureRegistry = registries.lookupOrThrow(Registries.STRUCTURE);

        for (StructureEntry structure : ModStructures.ALL) {
            entries.add(structure.structureSetKey(), new StructureSet(
                    List.of(StructureSet.entry(structureRegistry.getOrThrow(structure.structureKey()))),
                    new RandomSpreadStructurePlacement(
                            structure.spacing(),
                            structure.separation(),
                            RandomSpreadType.LINEAR,
                            structure.salt()
                    )
            ));
        }
    }

    @Override
    public String getName() { return "Clover Structure Sets"; }
}