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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.List;
import java.util.Map;

public class ModStructures {
    private static final int SALT_BASE = 420100;
    
    private static final int COMMON_SPACING = 72;
    private static final int COMMON_SEPARATION = 16;
    
    private static final int UNCOMMON_SPACING = 120;
    private static final int UNCOMMON_SEPARATION = 24;
    
    private static final int RARE_SPACING = 160;
    private static final int RARE_SEPARATION = 20;

    public static final List<StructureEntry> ALL = List.of(
            // common structures
            new StructureEntry("wagon", BiomeTags.IS_FOREST, TerrainAdjustment.NONE, COMMON_SPACING, COMMON_SEPARATION, SALT_BASE + 1),
            new StructureEntry("campsite", BiomeTags.IS_TAIGA, TerrainAdjustment.NONE, COMMON_SPACING, COMMON_SEPARATION, SALT_BASE + 2),
            new StructureEntry("haystorage", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, COMMON_SPACING, COMMON_SEPARATION, SALT_BASE + 3),

            // uncommon structures
            new StructureEntry("barn", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_BOX, UNCOMMON_SPACING, UNCOMMON_SEPARATION, SALT_BASE + 4),
            new StructureEntry("cabin", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, UNCOMMON_SPACING, UNCOMMON_SEPARATION, SALT_BASE + 5),
            new StructureEntry("greenhouse", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_BOX, UNCOMMON_SPACING, UNCOMMON_SEPARATION, SALT_BASE + 6),
            new StructureEntry("berrycabin", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, UNCOMMON_SPACING, UNCOMMON_SEPARATION, SALT_BASE + 7),
            new StructureEntry("desertarch", BiomeTags.HAS_DESERT_PYRAMID, TerrainAdjustment.BEARD_THIN, UNCOMMON_SPACING, UNCOMMON_SEPARATION, SALT_BASE + 8),
            new StructureEntry("deserthouse", BiomeTags.HAS_DESERT_PYRAMID, TerrainAdjustment.BEARD_THIN, UNCOMMON_SPACING, UNCOMMON_SEPARATION, SALT_BASE + 9),
            new StructureEntry("wizardtower", BiomeTags.IS_TAIGA, TerrainAdjustment.BEARD_THIN, UNCOMMON_SPACING, UNCOMMON_SEPARATION, SALT_BASE + 13),

            // rare structures
            new StructureEntry("lightningtree", BiomeTags.IS_OVERWORLD, TerrainAdjustment.BEARD_THIN, RARE_SPACING, RARE_SEPARATION, SALT_BASE + 10),
            new StructureEntry("stonehenge", BiomeTags.HAS_VILLAGE_PLAINS, TerrainAdjustment.BEARD_THIN, RARE_SPACING, RARE_SEPARATION, SALT_BASE + 11),
            new StructureEntry("statue", BiomeTags.IS_FOREST, TerrainAdjustment.BEARD_THIN, RARE_SPACING, RARE_SEPARATION, SALT_BASE + 12)
    );

    public static void bootstrapTemplatePools(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> poolRegistry = context.lookup(Registries.TEMPLATE_POOL);
        HolderGetter<StructureProcessorList> processorRegistry = context.lookup(Registries.PROCESSOR_LIST);

        Holder.Reference<StructureTemplatePool> emptyPool = poolRegistry.getOrThrow(Pools.EMPTY);
        Holder<StructureProcessorList> clearVegetation = processorRegistry.getOrThrow(ModProcessorLists.VEGETATION_CLEARANCE);

        for (StructureEntry s : ALL) {
            context.register(s.templatePoolKey(), new StructureTemplatePool(
                    emptyPool,
                    List.of(Pair.of(
                            StructurePoolElement.single(s.nbtId().toString(), clearVegetation)
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
