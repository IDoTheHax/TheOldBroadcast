package net.idothehax.theoldbroadcast.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class StudioRoomFeature extends Feature<NoneFeatureConfiguration> {

    public StudioRoomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // Generate a 32x32 studio room
        int roomSize = 32;
        int roomHeight = 15;

        // Determine room type based on position
        RoomType roomType = determineRoomType(pos.getX(), pos.getZ(), random);

        // Clear area and place floor
        for (int x = 0; x < roomSize; x++) {
            for (int z = 0; z < roomSize; z++) {
                BlockPos floorPos = pos.offset(x, 0, z);
                level.setBlock(floorPos, Blocks.GRAY_CONCRETE.defaultBlockState(), 3);

                // Clear air space
                for (int y = 1; y < roomHeight; y++) {
                    level.setBlock(floorPos.above(y), Blocks.AIR.defaultBlockState(), 3);
                }

                // Place ceiling
                level.setBlock(floorPos.above(roomHeight), Blocks.GRAY_CONCRETE.defaultBlockState(), 3);
            }
        }

        // Generate walls with doorways
        generateWalls(level, pos, roomSize, roomHeight);

        // Generate room-specific content
        generateRoomContent(level, pos, roomSize, roomType, random);

        // Add lighting
        generateLighting(level, pos, roomSize, random);

        return true;
    }

    private RoomType determineRoomType(int x, int z, RandomSource random) {
        int hash = (x / 32 * 31 + z / 32) * 17;
        int type = Math.abs(hash + random.nextInt(3)) % 7;

        return switch (type) {
            case 0 -> RoomType.CORRIDOR;
            case 1 -> RoomType.CINEMA;
            case 2 -> RoomType.BROADCAST_ROOM;
            case 3 -> RoomType.EDITING_LAB;
            case 4 -> RoomType.SOUNDSTAGE;
            case 5 -> RoomType.STORAGE;
            case 6 -> RoomType.ANTENNA_ROOM;
            default -> RoomType.CORRIDOR;
        };
    }

    private void generateWalls(WorldGenLevel level, BlockPos pos, int roomSize, int roomHeight) {
        // Generate perimeter walls with doorways
        for (int i = 0; i < roomSize; i++) {
            for (int y = 1; y < roomHeight; y++) {
                // North wall
                if (!(i >= 14 && i <= 18)) { // Doorway
                    level.setBlock(pos.offset(i, y, 0), Blocks.BLACK_WOOL.defaultBlockState(), 3);
                }
                // South wall
                if (!(i >= 14 && i <= 18)) { // Doorway
                    level.setBlock(pos.offset(i, y, roomSize - 1), Blocks.BLACK_WOOL.defaultBlockState(), 3);
                }
                // West wall
                if (!(i >= 14 && i <= 18)) { // Doorway
                    level.setBlock(pos.offset(0, y, i), Blocks.BLACK_WOOL.defaultBlockState(), 3);
                }
                // East wall
                if (!(i >= 14 && i <= 18)) { // Doorway
                    level.setBlock(pos.offset(roomSize - 1, y, i), Blocks.BLACK_WOOL.defaultBlockState(), 3);
                }
            }
        }
    }

    private void generateRoomContent(WorldGenLevel level, BlockPos pos, int roomSize, RoomType roomType, RandomSource random) {
        switch (roomType) {
            case CINEMA -> generateCinemaContent(level, pos, roomSize, random);
            case BROADCAST_ROOM -> generateBroadcastContent(level, pos, roomSize, random);
            case EDITING_LAB -> generateEditingContent(level, pos, roomSize, random);
            case SOUNDSTAGE -> generateSoundstageContent(level, pos, roomSize, random);
            case STORAGE -> generateStorageContent(level, pos, roomSize, random);
            case ANTENNA_ROOM -> generateAntennaContent(level, pos, roomSize, random);
            case CORRIDOR -> generateCorridorContent(level, pos, roomSize, random);
        }
    }

    private void generateCinemaContent(WorldGenLevel level, BlockPos pos, int roomSize, RandomSource random) {
        // Rows of seats
        for (int row = 5; row < roomSize - 10; row += 3) {
            for (int seat = 3; seat < roomSize - 3; seat += 2) {
                level.setBlock(pos.offset(seat, 1, row), Blocks.BLACK_WOOL.defaultBlockState(), 3);
            }
        }

        // Screen at front
        for (int x = 5; x < roomSize - 5; x++) {
            for (int y = 2; y < 8; y++) {
                level.setBlock(pos.offset(x, y, roomSize - 3), Blocks.WHITE_WOOL.defaultBlockState(), 3);
            }
        }

        // Projector
        level.setBlock(pos.offset(roomSize / 2, 6, 5), Blocks.OBSERVER.defaultBlockState(), 3);
    }

    private void generateBroadcastContent(WorldGenLevel level, BlockPos pos, int roomSize, RandomSource random) {
        // Control desks
        for (int x = 5; x < roomSize - 5; x += 4) {
            for (int z = 5; z < roomSize - 5; z += 4) {
                level.setBlock(pos.offset(x, 1, z), Blocks.IRON_BLOCK.defaultBlockState(), 3);
                level.setBlock(pos.offset(x, 2, z), Blocks.OBSERVER.defaultBlockState(), 3);
            }
        }

        // TV screens on walls
        for (int i = 0; i < 6; i++) {
            int x = 3 + i * 4;
            if (x < roomSize - 3) {
                level.setBlock(pos.offset(x, 4, 1), Blocks.BLACK_STAINED_GLASS.defaultBlockState(), 3);
            }
        }
    }

    private void generateEditingContent(WorldGenLevel level, BlockPos pos, int roomSize, RandomSource random) {
        // Workstations
        for (int x = 4; x < roomSize - 4; x += 6) {
            for (int z = 4; z < roomSize - 4; z += 6) {
                // Desk
                level.setBlock(pos.offset(x, 1, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(pos.offset(x + 1, 1, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);

                // Equipment
                level.setBlock(pos.offset(x, 2, z), Blocks.DISPENSER.defaultBlockState(), 3);
                level.setBlock(pos.offset(x + 1, 2, z), Blocks.REPEATER.defaultBlockState(), 3);
            }
        }

        // Film reels
        for (int i = 0; i < 8; i++) {
            int x = 2 + random.nextInt(roomSize - 4);
            int z = 2 + random.nextInt(roomSize - 4);
            level.setBlock(pos.offset(x, 2, z), Blocks.BARREL.defaultBlockState(), 3);
        }
    }

    private void generateSoundstageContent(WorldGenLevel level, BlockPos pos, int roomSize, RandomSource random) {
        int setType = random.nextInt(3);

        switch (setType) {
            case 0 -> {
                // Haunted house set
                for (int x = 8; x < roomSize - 8; x++) {
                    for (int z = 8; z < roomSize - 8; z++) {
                        if (x == 8 || x == roomSize - 9 || z == 8 || z == roomSize - 9) {
                            level.setBlock(pos.offset(x, 1, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
                            if (random.nextFloat() < 0.7f) {
                                level.setBlock(pos.offset(x, 2, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
            case 1 -> {
                // Sci-fi set
                for (int x = 5; x < roomSize - 5; x += 3) {
                    for (int z = 5; z < roomSize - 5; z += 3) {
                        level.setBlock(pos.offset(x, 1, z), Blocks.IRON_BLOCK.defaultBlockState(), 3);
                        level.setBlock(pos.offset(x, 2, z), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
                    }
                }
            }
            case 2 -> {
                // Children's show set
                // Children's show set
                Block[] colors = new Block[]{
                    Blocks.RED_WOOL, Blocks.BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.GREEN_WOOL
                };

                for (int i = 0; i < 10; i++) {
                    int x = 3 + random.nextInt(roomSize - 6);
                    int z = 3 + random.nextInt(roomSize - 6);
                    Block color = colors[random.nextInt(colors.length)];
                    level.setBlock(pos.offset(x, 1, z), color.defaultBlockState(), 3);
                    if (random.nextFloat() < 0.5f) {
                        level.setBlock(pos.offset(x, 2, z), color.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void generateStorageContent(WorldGenLevel level, BlockPos pos, int roomSize, RandomSource random) {
        // Shelves
        for (int x = 2; x < roomSize - 2; x += 4) {
            for (int y = 1; y < 8; y += 2) {
                level.setBlock(pos.offset(x, y, 2), Blocks.BOOKSHELF.defaultBlockState(), 3);
                level.setBlock(pos.offset(x, y, roomSize - 3), Blocks.BOOKSHELF.defaultBlockState(), 3);
            }
        }

        // Props scattered around
        for (int i = 0; i < 15; i++) {
            int x = 2 + random.nextInt(roomSize - 4);
            int z = 2 + random.nextInt(roomSize - 4);
            BlockState prop = random.nextFloat() < 0.5f ?
                Blocks.SKELETON_SKULL.defaultBlockState() :
                Blocks.CHEST.defaultBlockState();
            level.setBlock(pos.offset(x, 2, z), prop, 3);
        }
    }

    private void generateAntennaContent(WorldGenLevel level, BlockPos pos, int roomSize, RandomSource random) {
        int centerX = roomSize / 2;
        int centerZ = roomSize / 2;

        // Main antenna tower
        for (int y = 1; y < 25; y++) {
            level.setBlock(pos.offset(centerX, y, centerZ), Blocks.IRON_BARS.defaultBlockState(), 3);
            if (y % 5 == 0) {
                level.setBlock(pos.offset(centerX - 1, y, centerZ), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
                level.setBlock(pos.offset(centerX + 1, y, centerZ), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
                level.setBlock(pos.offset(centerX, y, centerZ - 1), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
                level.setBlock(pos.offset(centerX, y, centerZ + 1), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    private void generateCorridorContent(WorldGenLevel level, BlockPos pos, int roomSize, RandomSource random) {
        // Sparse cables and furniture
        for (int i = 0; i < 8; i++) {
            int x = 2 + random.nextInt(roomSize - 4);
            int z = 2 + random.nextInt(roomSize - 4);
            level.setBlock(pos.offset(x, 1, z), Blocks.TRIPWIRE.defaultBlockState(), 3);
        }

        // Occasional furniture
        for (int i = 0; i < 3; i++) {
            int x = 4 + random.nextInt(roomSize - 8);
            int z = 4 + random.nextInt(roomSize - 8);
            level.setBlock(pos.offset(x, 1, z), Blocks.BARREL.defaultBlockState(), 3);
        }
    }

    private void generateLighting(WorldGenLevel level, BlockPos pos, int roomSize, RandomSource random) {
        // Sparse, eerie lighting
        for (int x = 8; x < roomSize; x += 8) {
            for (int z = 8; z < roomSize; z += 8) {
                if (random.nextFloat() < 0.7f) {
                    level.setBlock(pos.offset(x, roomSize - 2, z), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
                }
            }
        }
    }

    public enum RoomType {
        CORRIDOR, CINEMA, BROADCAST_ROOM, EDITING_LAB, SOUNDSTAGE, STORAGE, ANTENNA_ROOM
    }
}
