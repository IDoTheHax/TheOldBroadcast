package net.idothehax.theoldbroadcast.world.generation.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.idothehax.theoldbroadcast.world.generation.config.TheOldStudioConfig;
import net.idothehax.theoldbroadcast.world.generation.noise.NoiseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Backrooms Mod Incomplete Features & Tasks
 *
 * - Sound Effects:
 *   - Add custom ambient sounds for electrical hum, ventilation, dripping, machinery.
 *   - Implement dynamic sound placement and volume control based on player proximity.
 *
 * - Shaders & Visual Effects:
 *   - Integrate custom shaders for lighting, fog, and atmosphere.
 *   - Add subtle visual cues (flickering lights, color grading) for navigation.
 *
 * - Navigation Aids:
 *   - Expand visual cue variety (more wall markings, unique floor patterns).
 *   - Improve placement logic for cues to avoid clutter/repetition.
 *
 * - Room Generation:
 *   - Add more room types/themes (generator rooms, break rooms, security stations).
 *   - Implement furniture/machinery placement logic for special rooms.
 *
 * - Performance Optimization:
 *   - Profile and optimize maze/navigation aid generation for large worlds.
 *
 * - Config & Customization:
 *   - Expose more settings for navigation aids, sound cues, room features in config files.
 *
 * - Testing & Polish:
 *   - Playtest navigation/room generation for edge cases and bugs.
 *   - Add missing documentation and code comments for maintainability.
 */

/**
 * Navigation aids system for helping players navigate the infinite The Old Studio
 * Provides subtle environmental cues without breaking immersion
 */
public class NavigationAids {

    /**
     * Places navigation aids throughout the generated area
     */
    public static void placeNavigationAids(WorldGenLevel level, BlockPos chunkStart, RandomSource random, NoiseManager noiseManager) {
        if (!TheOldStudioConfig.ENABLE_NAVIGATION_AIDS) {
            return;
        }

        List<BlockPos> potentialSoundCues = new ArrayList<>();
        List<BlockPos> potentialVisualCues = new ArrayList<>();

        // Scan chunk area for suitable navigation aid placement
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos = chunkStart.offset(x, 66, z);

                // Check if this is a good location for navigation aids
                if (isGoodNavigationSpot(level, pos, random, noiseManager)) {
                    float navigationNoise = noiseManager.getFeatureNoise(pos.getX(), pos.getZ(),
                        NoiseManager.FeatureType.CABLE_PLACEMENT);

                    if (navigationNoise > 0.7f) {
                        if (random.nextFloat() < TheOldStudioConfig.SOUND_CUE_CHANCE) {
                            potentialSoundCues.add(pos);
                        }
                        if (random.nextFloat() < TheOldStudioConfig.VISUAL_CUE_CHANCE) {
                            potentialVisualCues.add(pos);
                        }
                    }
                }
            }
        }

        // Place selected navigation aids
        placeSoundCues(level, potentialSoundCues, random);
        placeVisualCues(level, potentialVisualCues, random, noiseManager);
    }

    /**
     * Places subtle sound-generating elements
     */
    private static void placeSoundCues(WorldGenLevel level, List<BlockPos> positions, RandomSource random) {
        for (BlockPos pos : positions) {
            NavigationCueType cueType = selectSoundCueType(random);

            switch (cueType) {
                case ELECTRICAL_HUM -> placeElectricalHum(level, pos, random);
                case VENTILATION -> placeVentilation(level, pos, random);
                case DRIPPING -> placeDripping(level, pos, random);
                case MACHINERY -> placeMachinery(level, pos, random);
            }
        }
    }

    /**
     * Places subtle visual navigation cues
     */
    private static void placeVisualCues(WorldGenLevel level, List<BlockPos> positions, RandomSource random, NoiseManager noiseManager) {
        for (BlockPos pos : positions) {
            VisualCueType cueType = selectVisualCueType(random);

            switch (cueType) {
                case DIRECTIONAL_LIGHTING -> placeDirectionalLighting(level, pos, random);
                case COLOR_VARIATION -> placeColorVariation(level, pos, random, noiseManager);
                case FLOOR_PATTERNS -> placeFloorPattern(level, pos, random);
                case WALL_MARKINGS -> placeWallMarkings(level, pos, random);
                case CEILING_INDICATORS -> placeCeilingIndicators(level, pos, random);
            }
        }
    }

    private static boolean isGoodNavigationSpot(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        // Check if position is in a corridor or room entrance
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }

        // Prefer junction areas and corridor intersections
        int nearbyWalls = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (level.getBlockState(pos.offset(dx, 0, dz)).isSolid()) {
                    nearbyWalls++;
                }
            }
        }

        // Good spots have moderate wall density (not too open, not too enclosed)
        return nearbyWalls >= 8 && nearbyWalls <= 16;
    }

    private static NavigationCueType selectSoundCueType(RandomSource random) {
        NavigationCueType[] types = NavigationCueType.values();
        return types[random.nextInt(types.length)];
    }

    private static VisualCueType selectVisualCueType(RandomSource random) {
        VisualCueType[] types = VisualCueType.values();
        return types[random.nextInt(types.length)];
    }

    // Sound cue placement methods
    private static void placeElectricalHum(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Place redstone dust in walls to suggest electrical activity
        for (int y = 1; y < 4; y++) {
            BlockPos wallPos = findNearbyWall(level, pos.above(y));
            if (wallPos != null && random.nextFloat() < 0.3f) {
                level.setBlock(wallPos, Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
            }
        }
    }

    private static void placeVentilation(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Place iron bars in ceiling to suggest air vents
        BlockPos ceilingPos = pos.above(11);
        if (level.getBlockState(ceilingPos).isSolid()) {
            level.setBlock(ceilingPos, Blocks.IRON_BARS.defaultBlockState(), 3);

            // Add some air flow indicators
            if (random.nextFloat() < 0.5f) {
                level.setBlock(pos.above(10), Blocks.COBWEB.defaultBlockState(), 3);
            }
        }
    }

    private static void placeDripping(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Place water source in ceiling to create dripping sound
        BlockPos ceilingPos = pos.above(12);
        if (level.getBlockState(ceilingPos).isSolid() && random.nextFloat() < 0.2f) {
            level.setBlock(ceilingPos.below(), Blocks.WATER.defaultBlockState(), 3);
        }
    }

    private static void placeMachinery(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Place dispenser or observer blocks to suggest machinery
        BlockPos wallPos = findNearbyWall(level, pos);
        if (wallPos != null) {
            BlockState machinery = random.nextBoolean() ?
                Blocks.DISPENSER.defaultBlockState() :
                Blocks.OBSERVER.defaultBlockState();
            level.setBlock(wallPos, machinery, 3);
        }
    }

    // Visual cue placement methods
    private static void placeDirectionalLighting(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Create subtle directional lighting with different intensities
        BlockPos lightPos = pos.above(10);

        BlockState lightBlock = switch (random.nextInt(4)) {
            case 0 -> Blocks.TORCH.defaultBlockState();
            case 1 -> Blocks.REDSTONE_TORCH.defaultBlockState();
            case 2 -> Blocks.SOUL_TORCH.defaultBlockState();
            default -> Blocks.LANTERN.defaultBlockState();
        };

        level.setBlock(lightPos, lightBlock, 3);
    }

    private static void placeColorVariation(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        // Add subtle color variations to nearby blocks
        float colorNoise = noiseManager.getFeatureNoise(pos.getX(), pos.getZ(),
            NoiseManager.FeatureType.WALL_DAMAGE);

        BlockState colorBlock = switch ((int)(colorNoise * 6) % 6) {
            case 0 -> Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
            case 1 -> Blocks.WHITE_CONCRETE.defaultBlockState();
            case 2 -> Blocks.YELLOW_CONCRETE.defaultBlockState();
            case 3 -> Blocks.LIME_CONCRETE.defaultBlockState();
            case 4 -> Blocks.CYAN_CONCRETE.defaultBlockState();
            default -> Blocks.GRAY_CONCRETE.defaultBlockState();
        };

        // Apply to nearby wall
        BlockPos wallPos = findNearbyWall(level, pos);
        if (wallPos != null && random.nextFloat() < 0.4f) {
            level.setBlock(wallPos, colorBlock, 3);
        }
    }

    private static void placeFloorPattern(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Create subtle floor patterns for navigation
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floorPos = pos.offset(dx, -1, dz);
                if (level.getBlockState(floorPos).isSolid() && random.nextFloat() < 0.3f) {
                    BlockState patternBlock = switch (random.nextInt(3)) {
                        case 0 -> Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
                        case 1 -> Blocks.WHITE_CONCRETE.defaultBlockState();
                        default -> Blocks.YELLOW_CONCRETE.defaultBlockState();
                    };
                    level.setBlock(floorPos, patternBlock, 3);
                }
            }
        }
    }

    private static void placeWallMarkings(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Add item frames or signs as subtle markers
        BlockPos wallPos = findNearbyWall(level, pos.above(random.nextInt(3) + 1));
        if (wallPos != null) {
            if (random.nextBoolean()) {
                // Place an item frame entity on the wall
                var itemFrame = EntityType.ITEM_FRAME.create(level.getLevel());
                if (itemFrame != null) {
                    itemFrame.moveTo(wallPos.getX() + 0.5, wallPos.getY() + 0.5, wallPos.getZ() + 0.5, 0, 0);
                    level.addFreshEntity(itemFrame);
                }
            } else {
                level.setBlock(wallPos, Blocks.OAK_SIGN.defaultBlockState(), 3);
            }
        }
    }

    private static void placeCeilingIndicators(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Place subtle ceiling variations
        BlockPos ceilingPos = pos.above(11);
        if (level.getBlockState(ceilingPos).isSolid() && random.nextFloat() < 0.3f) {
            BlockState indicator = switch (random.nextInt(3)) {
                case 0 -> Blocks.IRON_TRAPDOOR.defaultBlockState();
                case 1 -> Blocks.STONE_BUTTON.defaultBlockState();
                default -> Blocks.LEVER.defaultBlockState();
            };
            level.setBlock(ceilingPos, indicator, 3);
        }
    }

    private static BlockPos findNearbyWall(WorldGenLevel level, BlockPos center) {
        // Find the nearest wall block around the center position
        for (int radius = 1; radius <= 3; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        BlockPos checkPos = center.offset(dx, 0, dz);
                        if (level.getBlockState(checkPos).isSolid()) {
                            return checkPos;
                        }
                    }
                }
            }
        }
        return null;
    }

    // Enums for navigation cue types
    private enum NavigationCueType {
        ELECTRICAL_HUM,
        VENTILATION,
        DRIPPING,
        MACHINERY
    }

    private enum VisualCueType {
        DIRECTIONAL_LIGHTING,
        COLOR_VARIATION,
        FLOOR_PATTERNS,
        WALL_MARKINGS,
        CEILING_INDICATORS
    }
}
