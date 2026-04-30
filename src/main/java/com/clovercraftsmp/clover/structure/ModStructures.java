package com.clovercraftsmp.clover.structure;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.List;
import java.util.Map;

public class ModStructures {
    public static final List<StructureEntry> ALL = List.of(
            new StructureEntry("barn", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, 24, 6, 4201),
            new StructureEntry("berrycabin", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, 24, 6, 4203),
            new StructureEntry("cabin", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, 24, 6, 4204),
            new StructureEntry("campsite", BiomeTags.IS_TAIGA, TerrainAdjustment.BEARD_THIN, 24, 6, 4205),
            new StructureEntry("desertarch", BiomeTags.HAS_DESERT_PYRAMID, TerrainAdjustment.BEARD_THIN, 24, 6, 4206),
            new StructureEntry("deserthouse", BiomeTags.HAS_DESERT_PYRAMID, TerrainAdjustment.BEARD_THIN, 24, 6, 4207),
            new StructureEntry("greenhouse", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, 24, 6, 4208),
            new StructureEntry("haystorage", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, 24, 6, 4209),
            new StructureEntry("lightning_tree", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, 24, 6, 4210),
            new StructureEntry("statue", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, 24, 6, 4211),
            new StructureEntry("stonehenge", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, 24, 6, 4212),
            new StructureEntry("wagon", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, 24, 6, 4213),
            new StructureEntry("wizardtower", BiomeTags.IS_TAIGA, TerrainAdjustment.BEARD_THIN, 24, 6, 4214)
    );

    public static void bootstrapTemplatePools(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> poolRegistry = context.lookup(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> emptyPool = poolRegistry.getOrThrow(Pools.EMPTY);

        for (StructureEntry s : ALL) {
            context.register(s.templatePoolKey(), new StructureTemplatePool(
                    emptyPool,
                    List.of(Pair.of(
                            StructurePoolElement.single(s.nbtId().toString())
                                    .apply(StructureTemplatePool.Projection.RIGID),
                            1
                    ))
            ));
        }
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> poolRegistry = context.lookup(Registries.TEMPLATE_POOL);

        for (StructureEntry s : ALL) {
            context.register(s.structureKey(), new JigsawStructure(
                    new Structure.StructureSettings(
                            biomeRegistry.getOrThrow(s.biomeTag()),
                            Map.of(),
                            GenerationStep.Decoration.SURFACE_STRUCTURES,
                            s.terrainAdjustment()
                    ),
                    poolRegistry.getOrThrow(s.templatePoolKey()),
                    1,
                    ConstantHeight.of(VerticalAnchor.absolute(0)),
                    false,
                    Heightmap.Types.WORLD_SURFACE_WG
            ));
        }
    }

    public static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structureRegistry = context.lookup(Registries.STRUCTURE);

        for (StructureEntry s : ALL) {
            context.register(s.structureSetKey(), new StructureSet(
                    List.of(StructureSet.entry(structureRegistry.getOrThrow(s.structureKey()))),
                    new RandomSpreadStructurePlacement(
                            s.spacing(),
                            s.separation(),
                            RandomSpreadType.LINEAR,
                            s.salt()
                    )
            ));
        }
    }
}
