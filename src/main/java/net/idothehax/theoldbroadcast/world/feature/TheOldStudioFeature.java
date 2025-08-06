package net.idothehax.theoldbroadcast.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.idothehax.theoldbroadcast.world.generation.TheOldStudioGenerator;

/**
 * Advanced The Old Studio feature that generates infinite maze-like structures
 * Replaces the simple grid-based generation with sophisticated procedural algorithms
 */
public class TheOldStudioFeature extends Feature<NoneFeatureConfiguration> {
    private static TheOldStudioGenerator generator;
    private static long lastSeed = 0;
    public TheOldStudioFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        // Initialize or update generator if needed
        long currentSeed = level.getSeed();
        if (generator == null || lastSeed != currentSeed) {
            generator = new TheOldStudioGenerator(currentSeed);
            lastSeed = currentSeed;
        }
        // Get the chunk we're generating
        ChunkAccess chunk = level.getChunk(pos);
        // Generate the infinite The Old Studio structure for this chunk
        generator.generateChunk(level, chunk, random);
        return true;
    }
}
