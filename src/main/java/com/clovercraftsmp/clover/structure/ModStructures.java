package com.clovercraftsmp.clover.structure;

import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.List;

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
}
