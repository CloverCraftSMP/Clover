package com.clovercraftsmp.clover.tags;

import com.clovercraftsmp.clover.Clover;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class CloverTags {
    public static final TagKey<Block> DUPLICABLE_FLOWERS = TagKey.create(Registries.BLOCK, Clover.id("duplicable_flowers"));
}
