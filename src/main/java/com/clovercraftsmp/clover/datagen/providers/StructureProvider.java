package com.clovercraftsmp.clover.datagen.providers;

import com.clovercraftsmp.clover.structure.ModStructures;
import com.clovercraftsmp.clover.structure.StructureEntry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StructureProvider extends FabricDynamicRegistryProvider {

    public StructureProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        var biomeRegistry = registries.lookupOrThrow(Registries.BIOME);
        var poolRegistry  = registries.lookupOrThrow(Registries.TEMPLATE_POOL);

        for (StructureEntry structure : ModStructures.ALL) {
            entries.add(structure.structureKey(), new JigsawStructure(
                    new Structure.StructureSettings(
                            biomeRegistry.getOrThrow(structure.biomeTag()),
                            Map.of(),
                            GenerationStep.Decoration.SURFACE_STRUCTURES,
                            structure.terrainAdjustment()
                    ),
                    poolRegistry.getOrThrow(structure.templatePoolKey()),
                    1,
                    ConstantHeight.of(VerticalAnchor.absolute(0)),
                    false
            ));
        }
    }

    @Override
    public String getName() { return "Clover Structures"; }
}
