package com.clovercraftsmp.clover.structure;

import com.clovercraftsmp.clover.Clover;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public record StructureEntry(
        String name,
        TagKey<Biome> biomeTag,
        TerrainAdjustment terrainAdjustment,
        int spacing, // average distance between structures (chunks)
        int separation, // minimum distance (chunks), must be < spacing
        int salt // unique per structure to avoid clustering with vanilla
) {
    public ResourceKey<Structure> structureKey() {
        return ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(Clover.MOD_ID, name));
    }

    public ResourceKey<StructureSet> structureSetKey() {
        return ResourceKey.create(Registries.STRUCTURE_SET, ResourceLocation.fromNamespaceAndPath(Clover.MOD_ID, name));
    }

    public ResourceKey<StructureTemplatePool> templatePoolKey() {
        return ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.fromNamespaceAndPath(Clover.MOD_ID, name));
    }

    public ResourceLocation nbtId() {
        return ResourceLocation.fromNamespaceAndPath(Clover.MOD_ID, name);
    }
}
