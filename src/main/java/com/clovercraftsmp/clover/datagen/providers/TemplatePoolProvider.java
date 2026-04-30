package com.clovercraftsmp.clover.datagen.providers;

import com.clovercraftsmp.clover.structure.ModStructures;
import com.clovercraftsmp.clover.structure.StructureEntry;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TemplatePoolProvider extends FabricDynamicRegistryProvider {

    public TemplatePoolProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        HolderLookup.RegistryLookup<StructureTemplatePool> poolRegistry  = registries.lookupOrThrow(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> emptyPool = poolRegistry.getOrThrow(Pools.EMPTY);

        for (StructureEntry structure : ModStructures.ALL) {
            entries.add(structure.templatePoolKey(), new StructureTemplatePool(
                    emptyPool,
                    List.of(Pair.of(
                            StructurePoolElement.single(structure.nbtId().toString())
                                    .apply(StructureTemplatePool.Projection.RIGID),
                            1
                    ))
            ));
        }
    }

    @Override
    public String getName() { return "Clover Template Pools"; }
}
