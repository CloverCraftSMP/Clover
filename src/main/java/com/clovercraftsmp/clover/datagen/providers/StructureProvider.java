package com.clovercraftsmp.clover.datagen.providers;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

import java.util.concurrent.CompletableFuture;

public class StructureProvider extends FabricDynamicRegistryProvider {

    public StructureProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.TEMPLATE_POOL));
        entries.addAll(registries.lookupOrThrow(Registries.STRUCTURE));
        entries.addAll(registries.lookupOrThrow(Registries.STRUCTURE_SET));
    }

    @Override
    public String getName() { return "Clover Structures"; }
}