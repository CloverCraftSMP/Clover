package com.clovercraftsmp.clover.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import static com.clovercraftsmp.clover.Clover.id;

public record StructureEntry(
        String name,
        TagKey<Biome> biomeTag,
        TerrainAdjustment terrainAdjustment,
        int spacing, // average distance between structures (chunks)
        int separation, // minimum distance (chunks), must be < spacing
        int salt // unique per structure to avoid clustering with vanilla
) {
    public ResourceKey<Structure> structureKey() {
        return ResourceKey.create(Registries.STRUCTURE, id(name));
    }

    public ResourceKey<StructureSet> structureSetKey() {
        return ResourceKey.create(Registries.STRUCTURE_SET, id(name));
    }

    public ResourceKey<StructureTemplatePool> templatePoolKey() {
        return ResourceKey.create(Registries.TEMPLATE_POOL, id(name));
    }

    public ResourceLocation nbtId() {
        return id(name);
    }
}
