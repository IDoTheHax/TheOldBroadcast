package net.idothehax.theoldbroadcast.world.generation.rooms;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.idothehax.theoldbroadcast.world.generation.noise.NoiseManager;

/**
 * Base class for room templates with procedural generation capabilities
 */
public abstract class RoomTemplate {

    protected final float weight;
    protected final int minSize;
    protected final int maxSize;

    public RoomTemplate(float weight, int minSize, int maxSize) {
        this.weight = weight;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public abstract void generate(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager);

    public float getWeight() { return weight; }
    public boolean fitsSize(int size) { return size >= minSize && size <= maxSize; }

    protected void generateBasicRoom(WorldGenLevel level, BlockPos pos, int size, BlockState floor, BlockState wall, BlockState ceiling) {
        // Generate floor
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                BlockPos floorPos = pos.offset(x, 0, z);
                level.setBlock(floorPos, floor, 3);

                // Clear air space
                for (int y = 1; y < 12; y++) {
                    level.setBlock(floorPos.above(y), Blocks.AIR.defaultBlockState(), 3);
                }

                // Place ceiling
                level.setBlock(floorPos.above(12), ceiling, 3);
            }
        }

        // Generate walls
        for (int i = 0; i < size; i++) {
            for (int y = 1; y < 12; y++) {
                // North wall
                level.setBlock(pos.offset(i, y, 0), wall, 3);
                // South wall
                level.setBlock(pos.offset(i, y, size - 1), wall, 3);
                // West wall
                level.setBlock(pos.offset(0, y, i), wall, 3);
                // East wall
                level.setBlock(pos.offset(size - 1, y, i), wall, 3);
            }
        }
    }
}

/**
 * Standard backrooms office room
 */
class StandardRoomTemplate extends RoomTemplate {
    public StandardRoomTemplate() {
        super(1.0f, 8, 24);
    }

    @Override
    public void generate(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        int size = 16 + random.nextInt(9); // 16-24 blocks
        generateBasicRoom(level, pos, size,
            Blocks.YELLOW_CONCRETE.defaultBlockState(),
            Blocks.GRAY_CONCRETE.defaultBlockState(),
            Blocks.YELLOW_CONCRETE.defaultBlockState());

        // Add fluorescent lighting
        for (int x = 4; x < size - 4; x += 8) {
            for (int z = 4; z < size - 4; z += 8) {
                level.setBlock(pos.offset(x, 11, z), Blocks.GLOWSTONE.defaultBlockState(), 3);
            }
        }
    }
}

/**
 * Office-themed room with furniture
 */
class OfficeRoomTemplate extends RoomTemplate {
    public OfficeRoomTemplate() {
        super(0.8f, 12, 28);
    }

    @Override
    public void generate(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        int size = 16 + random.nextInt(13); // 16-28 blocks
        generateBasicRoom(level, pos, size,
            Blocks.WHITE_CONCRETE.defaultBlockState(),
            Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),
            Blocks.WHITE_CONCRETE.defaultBlockState());

        // Add desks and chairs
        for (int x = 3; x < size - 3; x += 6) {
            for (int z = 3; z < size - 3; z += 6) {
                // Desk
                level.setBlock(pos.offset(x, 1, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(pos.offset(x + 1, 1, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);

                // Chair
                level.setBlock(pos.offset(x, 1, z + 2), Blocks.DARK_OAK_STAIRS.defaultBlockState(), 3);

                // Computer monitor
                if (random.nextFloat() < 0.7f) {
                    level.setBlock(pos.offset(x, 2, z), Blocks.BLACK_STAINED_GLASS.defaultBlockState(), 3);
                }
            }
        }

        // Overhead lighting
        for (int x = 6; x < size - 6; x += 12) {
            for (int z = 6; z < size - 6; z += 12) {
                level.setBlock(pos.offset(x, 11, z), Blocks.SEA_LANTERN.defaultBlockState(), 3);
            }
        }
    }
}

/**
 * Storage/warehouse room
 */
class StorageRoomTemplate extends RoomTemplate {
    public StorageRoomTemplate() {
        super(0.6f, 10, 32);
    }

    @Override
    public void generate(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        int size = 20 + random.nextInt(13); // 20-32 blocks
        generateBasicRoom(level, pos, size,
            Blocks.BROWN_CONCRETE.defaultBlockState(),
            Blocks.DARK_OAK_PLANKS.defaultBlockState(),
            Blocks.BROWN_CONCRETE.defaultBlockState());

        // Add storage shelves
        for (int x = 2; x < size - 2; x += 4) {
            for (int z = 2; z < size - 2; z += 8) {
                // Shelf structure
                for (int y = 1; y <= 6; y += 2) {
                    level.setBlock(pos.offset(x, y, z), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                    level.setBlock(pos.offset(x + 1, y, z), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                    level.setBlock(pos.offset(x, y, z + 1), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                    level.setBlock(pos.offset(x + 1, y, z + 1), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                }

                // Support pillars
                for (int y = 1; y < 7; y++) {
                    level.setBlock(pos.offset(x, y, z + 2), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
                    level.setBlock(pos.offset(x + 1, y, z + 2), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
                }

                // Boxes and barrels
                if (random.nextFloat() < 0.6f) {
                    level.setBlock(pos.offset(x, 2, z), Blocks.BARREL.defaultBlockState(), 3);
                }
                if (random.nextFloat() < 0.4f) {
                    level.setBlock(pos.offset(x + 1, 4, z), Blocks.CHEST.defaultBlockState(), 3);
                }
            }
        }

        // Sparse lighting
        for (int x = 8; x < size - 8; x += 16) {
            for (int z = 8; z < size - 8; z += 16) {
                level.setBlock(pos.offset(x, 10, z), Blocks.TORCH.defaultBlockState(), 3);
            }
        }
    }
}

/**
 * Damaged/abandoned room variant
 */
class BrokenRoomTemplate extends RoomTemplate {
    public BrokenRoomTemplate() {
        super(0.4f, 8, 20);
    }

    @Override
    public void generate(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        int size = 12 + random.nextInt(9); // 12-20 blocks
        generateBasicRoom(level, pos, size,
            Blocks.BLACK_CONCRETE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.BLACK_CONCRETE.defaultBlockState());

        // Add damage and decay
        for (int x = 1; x < size - 1; x++) {
            for (int z = 1; z < size - 1; z++) {
                float damageNoise = noiseManager.getFeatureNoise(pos.getX() + x, pos.getZ() + z,
                    NoiseManager.FeatureType.WALL_DAMAGE);

                if (damageNoise > 0.7f) {
                    // Floor damage
                    if (random.nextFloat() < 0.3f) {
                        level.setBlock(pos.offset(x, 0, z), Blocks.GRAVEL.defaultBlockState(), 3);
                    }

                    // Wall holes
                    if (random.nextFloat() < 0.2f && (x == 1 || x == size - 2 || z == 1 || z == size - 2)) {
                        for (int y = 1; y < 4; y++) {
                            level.setBlock(pos.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }

                // Add mold and cobwebs
                float moldNoise = noiseManager.getFeatureNoise(pos.getX() + x, pos.getZ() + z,
                    NoiseManager.FeatureType.MOLD_GROWTH);

                if (moldNoise > 0.8f) {
                    if (random.nextFloat() < 0.4f) {
                        level.setBlock(pos.offset(x, 1 + random.nextInt(3), z), Blocks.COBWEB.defaultBlockState(), 3);
                    }
                    if (random.nextFloat() < 0.3f) {
                        level.setBlock(pos.offset(x, 0, z), Blocks.MYCELIUM.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Flickering lights
        for (int x = 6; x < size - 6; x += 12) {
            for (int z = 6; z < size - 6; z += 12) {
                if (random.nextFloat() < 0.6f) {
                    level.setBlock(pos.offset(x, 11, z), Blocks.SOUL_TORCH.defaultBlockState(), 3);
                }
            }
        }
    }
}

/**
 * Industrial/mechanical room
 */
class IndustrialRoomTemplate extends RoomTemplate {
    public IndustrialRoomTemplate() {
        super(0.5f, 16, 32);
    }

    @Override
    public void generate(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        int size = 20 + random.nextInt(13); // 20-32 blocks
        generateBasicRoom(level, pos, size,
            Blocks.GRAY_CONCRETE.defaultBlockState(),
            Blocks.IRON_BLOCK.defaultBlockState(),
            Blocks.GRAY_CONCRETE.defaultBlockState());

        // Add machinery and pipes
        for (int x = 4; x < size - 4; x += 8) {
            for (int z = 4; z < size - 4; z += 8) {
                // Generator/machine
                level.setBlock(pos.offset(x, 1, z), Blocks.BLAST_FURNACE.defaultBlockState(), 3);
                level.setBlock(pos.offset(x + 1, 1, z), Blocks.SMOKER.defaultBlockState(), 3);
                level.setBlock(pos.offset(x, 1, z + 1), Blocks.DISPENSER.defaultBlockState(), 3);
                level.setBlock(pos.offset(x + 1, 1, z + 1), Blocks.OBSERVER.defaultBlockState(), 3);

                // Pipes (using iron bars)
                for (int y = 2; y < 8; y++) {
                    if (random.nextFloat() < 0.7f) {
                        level.setBlock(pos.offset(x + 2, y, z), Blocks.IRON_BARS.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Industrial lighting
        for (int x = 6; x < size - 6; x += 8) {
            for (int z = 6; z < size - 6; z += 8) {
                level.setBlock(pos.offset(x, 10, z), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
            }
        }
    }
}

/**
 * Large conference/meeting room
 */
class ConferenceRoomTemplate extends RoomTemplate {
    public ConferenceRoomTemplate() {
        super(0.3f, 24, 48);
    }

    @Override
    public void generate(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        int size = 32 + random.nextInt(17); // 32-48 blocks
        generateBasicRoom(level, pos, size,
            Blocks.QUARTZ_BLOCK.defaultBlockState(),
            Blocks.WHITE_CONCRETE.defaultBlockState(),
            Blocks.QUARTZ_BLOCK.defaultBlockState());

        // Central conference table
        int centerX = size / 2;
        int centerZ = size / 2;
        int tableSize = Math.min(12, size / 3);

        for (int x = centerX - tableSize/2; x < centerX + tableSize/2; x++) {
            for (int z = centerZ - tableSize/2; z < centerZ + tableSize/2; z++) {
                level.setBlock(pos.offset(x, 1, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
            }
        }

        // Chairs around table
        for (int x = centerX - tableSize/2 - 1; x <= centerX + tableSize/2; x += 2) {
            level.setBlock(pos.offset(x, 1, centerZ - tableSize/2 - 1), Blocks.DARK_OAK_STAIRS.defaultBlockState(), 3);
            level.setBlock(pos.offset(x, 1, centerZ + tableSize/2 + 1), Blocks.DARK_OAK_STAIRS.defaultBlockState(), 3);
        }

        for (int z = centerZ - tableSize/2 - 1; z <= centerZ + tableSize/2; z += 2) {
            level.setBlock(pos.offset(centerX - tableSize/2 - 1, 1, z), Blocks.DARK_OAK_STAIRS.defaultBlockState(), 3);
            level.setBlock(pos.offset(centerX + tableSize/2 + 1, 1, z), Blocks.DARK_OAK_STAIRS.defaultBlockState(), 3);
        }

        // Presentation screen
        for (int x = centerX - 4; x < centerX + 4; x++) {
            for (int y = 2; y < 8; y++) {
                level.setBlock(pos.offset(x, y, 2), Blocks.BLACK_STAINED_GLASS.defaultBlockState(), 3);
            }
        }

        // Premium lighting
        for (int x = 8; x < size - 8; x += 8) {
            for (int z = 8; z < size - 8; z += 8) {
                level.setBlock(pos.offset(x, 11, z), Blocks.SEA_LANTERN.defaultBlockState(), 3);
            }
        }
    }
}

/**
 * Large open office space
 */
class MegaOfficeTemplate extends RoomTemplate {
    public MegaOfficeTemplate() {
        super(0.7f, 32, 64);
    }

    @Override
    public void generate(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        int size = 48 + random.nextInt(17); // 48-64 blocks
        generateBasicRoom(level, pos, size,
            Blocks.WHITE_CONCRETE.defaultBlockState(),
            Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),
            Blocks.WHITE_CONCRETE.defaultBlockState());

        // Cubicle grid
        for (int x = 4; x < size - 4; x += 8) {
            for (int z = 4; z < size - 4; z += 8) {
                // Cubicle walls
                for (int w = 0; w < 6; w++) {
                    for (int h = 1; h < 4; h++) {
                        if (w == 0 || w == 5) {
                            level.setBlock(pos.offset(x + w, h, z), Blocks.GRAY_WOOL.defaultBlockState(), 3);
                            level.setBlock(pos.offset(x + w, h, z + 5), Blocks.GRAY_WOOL.defaultBlockState(), 3);
                        }
                        if (w > 0 && w < 5) {
                            level.setBlock(pos.offset(x, h, z + w), Blocks.GRAY_WOOL.defaultBlockState(), 3);
                            level.setBlock(pos.offset(x + 5, h, z + w), Blocks.GRAY_WOOL.defaultBlockState(), 3);
                        }
                    }
                }

                // Desk and chair in each cubicle
                level.setBlock(pos.offset(x + 1, 1, z + 1), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(pos.offset(x + 2, 1, z + 1), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(pos.offset(x + 1, 1, z + 3), Blocks.DARK_OAK_STAIRS.defaultBlockState(), 3);

                // Computer
                if (random.nextFloat() < 0.8f) {
                    level.setBlock(pos.offset(x + 1, 2, z + 1), Blocks.BLACK_STAINED_GLASS.defaultBlockState(), 3);
                }
            }
        }

        // Main aisles
        for (int x = 0; x < size; x += 16) {
            for (int z = 1; z < size - 1; z++) {
                level.setBlock(pos.offset(x, 0, z), Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(), 3);
            }
        }

        for (int z = 0; z < size; z += 16) {
            for (int x = 1; x < size - 1; x++) {
                level.setBlock(pos.offset(x, 0, z), Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(), 3);
            }
        }

        // Extensive lighting grid
        for (int x = 8; x < size - 8; x += 8) {
            for (int z = 8; z < size - 8; z += 8) {
                level.setBlock(pos.offset(x, 11, z), Blocks.SEA_LANTERN.defaultBlockState(), 3);
            }
        }
    }
}

/**
 * Large warehouse/storage facility
 */
class MegaStorageTemplate extends RoomTemplate {
    public MegaStorageTemplate() {
        super(0.5f, 32, 64);
    }

    @Override
    public void generate(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        int size = 40 + random.nextInt(25); // 40-64 blocks
        generateBasicRoom(level, pos, size,
            Blocks.BROWN_CONCRETE.defaultBlockState(),
            Blocks.DARK_OAK_PLANKS.defaultBlockState(),
            Blocks.BROWN_CONCRETE.defaultBlockState());

        // Massive storage racks
        for (int x = 3; x < size - 3; x += 6) {
            for (int z = 3; z < size - 3; z += 12) {
                // Tall storage structure
                for (int y = 1; y <= 10; y += 3) {
                    // Shelf platforms
                    for (int w = 0; w < 4; w++) {
                        for (int d = 0; d < 8; d++) {
                            level.setBlock(pos.offset(x + w, y, z + d), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                        }
                    }

                    // Support structure
                    level.setBlock(pos.offset(x, y + 1, z), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
                    level.setBlock(pos.offset(x + 3, y + 1, z), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
                    level.setBlock(pos.offset(x, y + 1, z + 7), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
                    level.setBlock(pos.offset(x + 3, y + 1, z + 7), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
                }

                // Crates and containers
                for (int y = 2; y <= 8; y += 3) {
                    for (int w = 0; w < 4; w++) {
                        for (int d = 0; d < 8; d += 2) {
                            if (random.nextFloat() < 0.7f) {
                                level.setBlock(pos.offset(x + w, y, z + d),
                                    random.nextBoolean() ? Blocks.BARREL.defaultBlockState() : Blocks.CHEST.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }

        // Forklift paths
        for (int x = 6; x < size - 6; x += 12) {
            for (int z = 1; z < size - 1; z++) {
                level.setBlock(pos.offset(x, 0, z), Blocks.YELLOW_CONCRETE.defaultBlockState(), 3);
            }
        }

        // High-bay lighting
        for (int x = 12; x < size - 12; x += 12) {
            for (int z = 12; z < size - 12; z += 12) {
                level.setBlock(pos.offset(x, 10, z), Blocks.GLOWSTONE.defaultBlockState(), 3);
            }
        }
    }
}
