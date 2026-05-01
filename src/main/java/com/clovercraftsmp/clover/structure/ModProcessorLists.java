package com.clovercraftsmp.clover.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

import java.util.List;

import static com.clovercraftsmp.clover.Clover.id;

public class ModProcessorLists {
    public static final ResourceKey<StructureProcessorList> VEGETATION_CLEARANCE = ResourceKey.create(
            Registries.PROCESSOR_LIST,
            id("vegetation_clearance")
    );

    public static void bootstrap(BootstrapContext<StructureProcessorList> context) {
        context.register(VEGETATION_CLEARANCE, new StructureProcessorList(List.of(
                new RuleProcessor(List.of(
                        new ProcessorRule(new TagMatchTest(BlockTags.LEAVES), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new TagMatchTest(BlockTags.LOGS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new TagMatchTest(BlockTags.REPLACEABLE), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
                ))
        )));
    }
}
