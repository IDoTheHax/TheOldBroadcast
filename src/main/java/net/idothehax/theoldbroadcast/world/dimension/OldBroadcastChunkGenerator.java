package net.idothehax.theoldbroadcast.world.dimension;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class OldBroadcastChunkGenerator extends ChunkGenerator {
    public static final Codec<OldBroadcastChunkGenerator> CODEC = BiomeSource.CODEC.fieldOf("biome_source")
            .xmap(OldBroadcastChunkGenerator::new, ChunkGenerator::getBiomeSource).codec();

    private static final BlockState CONCRETE = Blocks.GRAY_CONCRETE.defaultBlockState();
    private static final BlockState WOOL = Blocks.BLACK_WOOL.defaultBlockState();
    private static final BlockState GLASS = Blocks.BLACK_STAINED_GLASS.defaultBlockState();

    public OldBroadcastChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        // No carvers in the studio dimension
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
        // Surface handled in fillFromNoise
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        // Custom mob spawning handled elsewhere
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(() -> {
            generateStudioStructure(chunk);
            return chunk;
        }, executor);
    }

    private void generateStudioStructure(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Generate floor
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                pos.set(x, 64, z);
                chunk.setBlockState(pos, CONCRETE, false);

                // Generate walls based on room layout
                if (shouldGenerateWall(chunk.getPos().x * 16 + x, chunk.getPos().z * 16 + z)) {
                    for (int y = 65; y < 80; y++) {
                        pos.set(x, y, z);
                        chunk.setBlockState(pos, WOOL, false);
                    }
                    // Ceiling
                    pos.set(x, 80, z);
                    chunk.setBlockState(pos, CONCRETE, false);
                }

                // Generate lighting fixtures
                if ((x + z) % 8 == 0 && !shouldGenerateWall(chunk.getPos().x * 16 + x, chunk.getPos().z * 16 + z)) {
                    pos.set(x, 79, z);
                    chunk.setBlockState(pos, Blocks.REDSTONE_LAMP.defaultBlockState(), false);
                }
            }
        }
    }

    private boolean shouldGenerateWall(int worldX, int worldZ) {
        // Create maze-like structure with connected rooms
        int roomSize = 32;
        int localX = Math.floorMod(worldX, roomSize);
        int localZ = Math.floorMod(worldZ, roomSize);

        // Room boundaries
        if (localX == 0 || localX == roomSize - 1 || localZ == 0 || localZ == roomSize - 1) {
            // Create doorways
            if ((localX == 0 && localZ >= 14 && localZ <= 18) ||
                (localX == roomSize - 1 && localZ >= 14 && localZ <= 18) ||
                (localZ == 0 && localX >= 14 && localX <= 18) ||
                (localZ == roomSize - 1 && localX >= 14 && localX <= 18)) {
                return false; // Doorway
            }
            return true; // Wall
        }

        // Interior walls for maze effect
        if (localX % 8 == 0 && localZ % 4 != 0) return true;
        if (localZ % 8 == 0 && localX % 4 != 0) return true;

        return false;
    }

    @Override
    public int getSeaLevel() {
        return 64;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType, LevelHeightAccessor level, RandomState randomState) {
        return 80;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        BlockState[] states = new BlockState[level.getHeight()];
        for (int i = 0; i < 64; i++) {
            states[i] = Blocks.STONE.defaultBlockState();
        }
        states[64] = CONCRETE;
        for (int i = 65; i < 80; i++) {
            states[i] = Blocks.AIR.defaultBlockState();
        }
        states[80] = CONCRETE;
        for (int i = 81; i < states.length; i++) {
            states[i] = Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(level.getMinBuildHeight(), states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Old Broadcast Studio Dimension");
    }
}
