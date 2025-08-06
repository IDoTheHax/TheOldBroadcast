package net.idothehax.theoldbroadcast.world.generation.noise;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

/**
 * Advanced noise management for organic variation in the Backrooms
 * Uses multiple noise layers for different aspects of generation
 */
public class NoiseManager {

    // Noise generators for different purposes
    private final SimplexNoise algorithmNoise;      // Algorithm selection
    private final SimplexNoise organicNoise;        // Organic variations
    private final SimplexNoise connectionNoise;     // Connection probability
    private final SimplexNoise modificationNoise;   // Cell modifications
    private final SimplexNoise ceilingNoise;        // Height variations
    private final SimplexNoise wallNoise;           // Wall irregularities
    private final SimplexNoise lightingNoise;       // Lighting variations
    private final SimplexNoise densityNoise;        // Room density
    private final SimplexNoise themeNoise;          // Theme selection
    private final SimplexNoise propNoise;           // Prop placement

    // Scale factors for different noise types
    private static final double ALGORITHM_SCALE = 0.001;    // Very large scale for algorithm regions
    private static final double ORGANIC_SCALE = 0.005;      // Large scale for organic features
    private static final double CONNECTION_SCALE = 0.01;    // Medium scale for connections
    private static final double MODIFICATION_SCALE = 0.02;  // Medium scale for modifications
    private static final double CEILING_SCALE = 0.05;       // Small scale for ceiling variation
    private static final double WALL_SCALE = 0.1;           // Small scale for wall details
    private static final double LIGHTING_SCALE = 0.03;      // Medium scale for lighting
    private static final double DENSITY_SCALE = 0.008;      // Large scale for density regions
    private static final double THEME_SCALE = 0.002;        // Very large scale for theme regions
    private static final double PROP_SCALE = 0.2;           // Fine scale for prop placement

    public NoiseManager(long seed) {
        // Initialize all noise generators with different seeds derived from main seed
        this.algorithmNoise = new SimplexNoise(new LegacyRandomSource(seed));
        this.organicNoise = new SimplexNoise(new LegacyRandomSource(seed + 1));
        this.connectionNoise = new SimplexNoise(new LegacyRandomSource(seed + 2));
        this.modificationNoise = new SimplexNoise(new LegacyRandomSource(seed + 3));
        this.ceilingNoise = new SimplexNoise(new LegacyRandomSource(seed + 4));
        this.wallNoise = new SimplexNoise(new LegacyRandomSource(seed + 5));
        this.lightingNoise = new SimplexNoise(new LegacyRandomSource(seed + 6));
        this.densityNoise = new SimplexNoise(new LegacyRandomSource(seed + 7));
        this.themeNoise = new SimplexNoise(new LegacyRandomSource(seed + 8));
        this.propNoise = new SimplexNoise(new LegacyRandomSource(seed + 9));
    }

    /**
     * Gets noise for algorithm selection (0.0 to 1.0)
     */
    public float getAlgorithmNoise(double x, double z) {
        return normalizeNoise(algorithmNoise.getValue(x * ALGORITHM_SCALE, z * ALGORITHM_SCALE));
    }

    /**
     * Gets organic variation noise (-1.0 to 1.0)
     */
    public float getOrganicNoise(double x, double z) {
        return (float) organicNoise.getValue(x * ORGANIC_SCALE, z * ORGANIC_SCALE);
    }

    /**
     * Gets connection probability noise (0.0 to 1.0)
     */
    public float getConnectionNoise(double x, double z) {
        return normalizeNoise(connectionNoise.getValue(x * CONNECTION_SCALE, z * CONNECTION_SCALE));
    }

    /**
     * Gets cell modification noise (0.0 to 1.0)
     */
    public float getModificationNoise(double x, double z) {
        return normalizeNoise(modificationNoise.getValue(x * MODIFICATION_SCALE, z * MODIFICATION_SCALE));
    }

    /**
     * Gets ceiling height variation noise (-1.0 to 1.0)
     */
    public float getCeilingNoise(double x, double z) {
        return (float) ceilingNoise.getValue(x * CEILING_SCALE, z * CEILING_SCALE);
    }

    /**
     * Gets wall irregularity noise (0.0 to 1.0)
     */
    public float getWallNoise(double x, double z) {
        return normalizeNoise(wallNoise.getValue(x * WALL_SCALE, z * WALL_SCALE));
    }

    /**
     * Gets lighting variation noise (0.0 to 1.0)
     */
    public float getLightingNoise(double x, double z) {
        return normalizeNoise(lightingNoise.getValue(x * LIGHTING_SCALE, z * LIGHTING_SCALE));
    }

    /**
     * Gets room density noise (0.0 to 1.0)
     */
    public float getDensityNoise(double x, double z) {
        return normalizeNoise(densityNoise.getValue(x * DENSITY_SCALE, z * DENSITY_SCALE));
    }

    /**
     * Gets theme selection noise (0.0 to 1.0)
     */
    public float getThemeNoise(double x, double z) {
        return normalizeNoise(themeNoise.getValue(x * THEME_SCALE, z * THEME_SCALE));
    }

    /**
     * Gets prop placement noise (0.0 to 1.0)
     */
    public float getPropNoise(double x, double z) {
        return normalizeNoise(propNoise.getValue(x * PROP_SCALE, z * PROP_SCALE));
    }

    /**
     * Gets layered noise for complex variations
     */
    public float getLayeredNoise(double x, double z, NoiseLayer... layers) {
        float result = 0.0f;
        float totalWeight = 0.0f;

        for (NoiseLayer layer : layers) {
            float noise = getNoiseByType(layer.type, x, z);
            result += noise * layer.weight;
            totalWeight += layer.weight;
        }

        return totalWeight > 0 ? result / totalWeight : 0.0f;
    }

    /**
     * Gets turbulence noise for chaotic variations
     */
    public float getTurbulenceNoise(double x, double z, int octaves) {
        float total = 0.0f;
        float frequency = 0.01f;
        float amplitude = 1.0f;
        float maxValue = 0.0f;

        for (int i = 0; i < octaves; i++) {
            total += organicNoise.getValue(x * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= 0.5f;
            frequency *= 2.0f;
        }

        return total / maxValue;
    }

    /**
     * Gets ridged noise for sharp features
     */
    public float getRidgedNoise(double x, double z) {
        float noise = (float) Math.abs(organicNoise.getValue(x * ORGANIC_SCALE, z * ORGANIC_SCALE));
        return 1.0f - noise; // Invert for ridged effect
    }

    /**
     * Gets billowed noise for cloud-like patterns
     */
    public float getBillowedNoise(double x, double z) {
        float noise = (float) organicNoise.getValue(x * ORGANIC_SCALE, z * ORGANIC_SCALE);
        return Math.abs(noise) * 2.0f - 1.0f;
    }

    /**
     * Gets cellular noise for blob-like patterns
     */
    public float getCellularNoise(double x, double z) {
        // Simple cellular automata-like noise
        float n1 = (float) organicNoise.getValue(x * 0.1, z * 0.1);
        float n2 = (float) organicNoise.getValue(x * 0.05, z * 0.05);
        float n3 = (float) organicNoise.getValue(x * 0.02, z * 0.02);

        return (n1 + n2 * 0.5f + n3 * 0.25f) / 1.75f;
    }

    /**
     * Gets domain warped noise for interesting distortions
     */
    public float getDomainWarpedNoise(double x, double z, float warpStrength) {
        // Get warp offsets
        float warpX = (float) organicNoise.getValue(x * 0.02, z * 0.02) * warpStrength;
        float warpZ = (float) organicNoise.getValue(x * 0.02 + 100, z * 0.02 + 100) * warpStrength;

        // Sample noise at warped coordinates
        return (float) organicNoise.getValue((x + warpX) * ORGANIC_SCALE, (z + warpZ) * ORGANIC_SCALE);
    }

    /**
     * Gets noise for specific room features
     */
    public float getFeatureNoise(double x, double z, FeatureType feature) {
        return switch (feature) {
            case FURNITURE_PLACEMENT -> getPropNoise(x, z);
            case WALL_DAMAGE -> getWallNoise(x * 2, z * 2); // Higher frequency for damage
            case LIGHTING_FLICKER -> getLightingNoise(x * 5, z * 5); // Very high frequency
            case FLOOR_WEAR -> normalizeNoise(organicNoise.getValue(x * 0.15, z * 0.15));
            case CEILING_STAINS -> normalizeNoise(modificationNoise.getValue(x * 0.08, z * 0.08));
            case MOLD_GROWTH -> getCellularNoise(x, z);
            case CABLE_PLACEMENT -> getTurbulenceNoise(x, z, 3);
            case DOOR_CONDITION -> normalizeNoise(wallNoise.getValue(x * 0.01, z * 0.01));
        };
    }

    /**
     * Checks if coordinates pass noise threshold
     */
    public boolean passesNoiseThreshold(double x, double z, NoiseType type, float threshold) {
        float noise = getNoiseByType(type, x, z);
        return noise > threshold;
    }

    /**
     * Gets interpolated noise between two points
     */
    public float getInterpolatedNoise(double x1, double z1, double x2, double z2, float t, NoiseType type) {
        float noise1 = getNoiseByType(type, x1, z1);
        float noise2 = getNoiseByType(type, x2, z2);
        return Mth.lerp(t, noise1, noise2);
    }

    // Utility methods
    private float normalizeNoise(double noise) {
        return (float) ((noise + 1.0) * 0.5); // Convert -1,1 to 0,1
    }

    private float getNoiseByType(NoiseType type, double x, double z) {
        return switch (type) {
            case ALGORITHM -> getAlgorithmNoise(x, z);
            case ORGANIC -> getOrganicNoise(x, z);
            case CONNECTION -> getConnectionNoise(x, z);
            case MODIFICATION -> getModificationNoise(x, z);
            case CEILING -> getCeilingNoise(x, z);
            case WALL -> getWallNoise(x, z);
            case LIGHTING -> getLightingNoise(x, z);
            case DENSITY -> getDensityNoise(x, z);
            case THEME -> getThemeNoise(x, z);
            case PROP -> getPropNoise(x, z);
        };
    }

    // Supporting classes and enums
    public enum NoiseType {
        ALGORITHM, ORGANIC, CONNECTION, MODIFICATION,
        CEILING, WALL, LIGHTING, DENSITY, THEME, PROP
    }

    public enum FeatureType {
        FURNITURE_PLACEMENT, WALL_DAMAGE, LIGHTING_FLICKER,
        FLOOR_WEAR, CEILING_STAINS, MOLD_GROWTH,
        CABLE_PLACEMENT, DOOR_CONDITION
    }

    public static class NoiseLayer {
        public final NoiseType type;
        public final float weight;

        public NoiseLayer(NoiseType type, float weight) {
            this.type = type;
            this.weight = weight;
        }

        public static NoiseLayer of(NoiseType type, float weight) {
            return new NoiseLayer(type, weight);
        }
    }
}
