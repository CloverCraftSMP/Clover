package com.clovercraftsmp.clover.event.feature;

import com.clovercraftsmp.clover.recipe.CauldronRecipe;
import com.clovercraftsmp.clover.recipe.CauldronRecipeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class CauldronRecipeTickHandler {
    private static final Set<UUID> PROCESSED = new HashSet<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PROCESSED.clear();
            for (ServerLevel level : server.getAllLevels()) {
                tickLevel(level);
            }
        });
    }

    private static void tickLevel(ServerLevel level) {
        level.getEntities(EntityType.ITEM, e -> !PROCESSED.contains(e.getUUID()))
                .forEach(entity -> {
                    if (entity.isRemoved()) return;

                    BlockPos pos = entity.blockPosition();
                    BlockState state = level.getBlockState(pos);

                    if (!isFilledCauldron(state)) return;

                    ItemStack stack = entity.getItem();
                    Optional<CauldronRecipe> recipe = CauldronRecipeRegistry.find(stack, state);
                    if (recipe.isEmpty()) return;

                    PROCESSED.add(entity.getUUID());
                    convertItem(level, entity, stack, recipe.get(), pos);
                });
    }

    private static void convertItem(
            ServerLevel level,
            ItemEntity entity,
            ItemStack stack,
            CauldronRecipe recipe,
            BlockPos pos
    ) {
        int totalCount = recipe.output().getCount() * stack.getCount();
        int maxStack = recipe.output().getItem().getDefaultMaxStackSize();

        entity.discard();

        while (totalCount > 0) {
            int spawnCount = Math.min(totalCount, maxStack);

            ItemStack output = recipe.output().copy();
            output.setCount(spawnCount);
            totalCount -= spawnCount;

            ItemEntity result = new ItemEntity(
                    level,
                    entity.getX(),
                    entity.getY() + 0.2,
                    entity.getZ(),
                    output
            );
            result.setPickUpDelay(20);
            level.addFreshEntity(result);

            level.playSound(
                    null, pos,
                    SoundEvents.AXE_SCRAPE,
                    SoundSource.BLOCKS,
                    0.5f, .2f
            );

            level.sendParticles(
                    ParticleTypes.SPLASH,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    10, 0.2, 0.1, 0.2, 0.05
            );
        }
    }

    private static boolean isFilledCauldron(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.LAVA_CAULDRON) return true;
        if (block == Blocks.WATER_CAULDRON || block == Blocks.POWDER_SNOW_CAULDRON)
            return state.getValue(LayeredCauldronBlock.LEVEL) > 0;
        return false;
    }
}
