package net.idothehax.theoldbroadcast.world.generation.config;

/**
 * Configuration class for The Old Studio generation parameters
 * Allows customization of generation settings
 */
public class TheOldStudioConfig {

    // Core generation parameters
    public static float ROOM_DENSITY = 0.7f;
    public static float MEGA_ROOM_CHANCE = 0.02f;
    public static float CONNECTION_PROBABILITY = 0.6f;
    public static float DEAD_END_CHANCE = 0.15f;

    // Size parameters
    public static int GRID_SIZE = 32;
    public static int MIN_ROOM_SIZE = 8;
    public static int MAX_ROOM_SIZE = 32;
    public static int ROOM_HEIGHT = 12;
    public static int CORRIDOR_WIDTH = 4;

    // Noise parameters
    public static float NOISE_SCALE_MULTIPLIER = 1.0f;
    public static float ORGANIC_VARIATION_STRENGTH = 0.3f;
    public static float WALL_IRREGULARITY_CHANCE = 0.3f;
    public static float LIGHTING_VARIATION_CHANCE = 0.1f;

    // Prop generation
    public static float PROP_DENSITY = 0.15f;
    public static float FURNITURE_CHANCE = 0.7f;
    public static float DECAY_CHANCE = 0.4f;

    // Performance settings
    public static int CHUNK_CACHE_SIZE = 256;
    public static boolean ENABLE_LOD = true;
    public static int LOD_DISTANCE = 3; // chunks

    // Navigation aids
    public static boolean ENABLE_NAVIGATION_AIDS = true;
    public static float SOUND_CUE_CHANCE = 0.05f;
    public static float VISUAL_CUE_CHANCE = 0.08f;

    /**
     * Validates and clamps configuration values
     */
    public static void validateConfig() {
        ROOM_DENSITY = Math.max(0.1f, Math.min(1.0f, ROOM_DENSITY));
        MEGA_ROOM_CHANCE = Math.max(0.0f, Math.min(0.1f, MEGA_ROOM_CHANCE));
        CONNECTION_PROBABILITY = Math.max(0.1f, Math.min(0.9f, CONNECTION_PROBABILITY));
        DEAD_END_CHANCE = Math.max(0.0f, Math.min(0.5f, DEAD_END_CHANCE));

        GRID_SIZE = Math.max(16, Math.min(64, GRID_SIZE));
        MIN_ROOM_SIZE = Math.max(8, Math.min(16, MIN_ROOM_SIZE));
        MAX_ROOM_SIZE = Math.max(MIN_ROOM_SIZE, Math.min(64, MAX_ROOM_SIZE));
        ROOM_HEIGHT = Math.max(8, Math.min(20, ROOM_HEIGHT));
        CORRIDOR_WIDTH = Math.max(3, Math.min(8, CORRIDOR_WIDTH));

        PROP_DENSITY = Math.max(0.0f, Math.min(1.0f, PROP_DENSITY));
        CHUNK_CACHE_SIZE = Math.max(64, Math.min(1024, CHUNK_CACHE_SIZE));
        LOD_DISTANCE = Math.max(1, Math.min(8, LOD_DISTANCE));
    }

    /**
     * Resets all values to defaults
     */
    public static void resetToDefaults() {
        ROOM_DENSITY = 0.7f;
        MEGA_ROOM_CHANCE = 0.02f;
        CONNECTION_PROBABILITY = 0.6f;
        DEAD_END_CHANCE = 0.15f;
        GRID_SIZE = 32;
        MIN_ROOM_SIZE = 8;
        MAX_ROOM_SIZE = 32;
        ROOM_HEIGHT = 12;
        CORRIDOR_WIDTH = 4;
        PROP_DENSITY = 0.15f;
        CHUNK_CACHE_SIZE = 256;
        LOD_DISTANCE = 3;
        ENABLE_NAVIGATION_AIDS = true;
    }
}
