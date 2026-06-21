package com.clovercraftsmp.clover.minigame.backrooms;

import com.clovercraftsmp.clover.Clover;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class BackroomsChunkGenerator extends ChunkGenerator {

    private static final int FLOOR_HEIGHT = 8;
    private static final int TOTAL_FLOORS = 48;
    private static final int WORLD_BOTTOM_Y = -64;

    public static final MapCodec<BackroomsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, instance.stable(BackroomsChunkGenerator::new))
    );

    public BackroomsChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();
        StructureTemplateManager templateManager = level.getLevel().getServer().getStructureManager();

        long pillarSeed = (long) chunkPos.x * 341873128712L + (long) chunkPos.z * 132897987541L;
        Random pillarRandom = new Random(pillarSeed);

        int playableBottomFloor = 5 + pillarRandom.nextInt(10);
        int playableTopFloor = 25 + pillarRandom.nextInt(15);

        for (int floor = 0; floor < TOTAL_FLOORS; floor++) {
            int currentY = WORLD_BOTTOM_Y + (floor * FLOOR_HEIGHT);

            if (floor < playableBottomFloor || floor > playableTopFloor) {
                placeTemplate(level, templateManager, Clover.id("solid_cap"), startX, currentY, startZ, Rotation.NONE);
                continue;
            }

            long floorSeed = pillarSeed + (floor * 31L);
            Random floorRandom = new Random(floorSeed);
            ResourceLocation roomId;

            Rotation[] rotations = Rotation.values();
            Rotation rotation = rotations[level.getRandom().nextInt(rotations.length)];

            if (floorRandom.nextFloat() < 0.10f && floor < playableTopFloor) {
                roomId = Clover.id("level_0_staircase");
                rotation = Rotation.NONE;
            } else {
                roomId = Clover.id( "level_0_hallway");
            }

            placeTemplate(level, templateManager, roomId, startX, currentY, startZ, rotation);
        }
    }

    private void placeTemplate(WorldGenLevel level, StructureTemplateManager manager, ResourceLocation id, int x, int y, int z, Rotation rotation) {
        Optional<StructureTemplate> templateOpt = manager.get(id);
        if (templateOpt.isPresent()) {
            StructurePlaceSettings placementData = new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).setIgnoreEntities(false);
            BlockPos placePos = new BlockPos(x, y, z);
            templateOpt.get().placeInWorld(level, placePos, placePos, placementData, level.getRandom(), 2);
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.completedFuture(chunkAccess);
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return new NoiseColumn(levelHeightAccessor.getMinBuildHeight(), new BlockState[0]);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) { }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) { }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) { }

    @Override
    public int getGenDepth() { return 384; }

    @Override
    public int getSeaLevel() { return 0; }

    @Override
    public int getMinY() { return WORLD_BOTTOM_Y; }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) { return 0; }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) { }
}