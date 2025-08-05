package net.idothehax.theoldbroadcast.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.idothehax.theoldbroadcast.Theoldbroadcast;
import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastDimensions;

@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StudioRoomGenerator {

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo().equals(OldBroadcastDimensions.OLD_BROADCAST_LEVEL)) {
            Player player = event.getEntity();
            if (player.level() instanceof ServerLevel serverLevel) {
                generateInitialRoom(serverLevel, player.blockPosition());
            }
        }
    }

    public static void generateInitialRoom(ServerLevel level, BlockPos playerPos) {
        // Generate a starting room and connected maze
        generateMazeSection(level, playerPos, 0, 0);

        // Generate adjacent maze sections for seamless exploration
        generateMazeSection(level, playerPos.offset(160, 0, 0), 1, 0);
        generateMazeSection(level, playerPos.offset(-160, 0, 0), -1, 0);
        generateMazeSection(level, playerPos.offset(0, 0, 160), 0, 1);
        generateMazeSection(level, playerPos.offset(0, 0, -160), 0, -1);
    }

    private static void generateMazeSection(ServerLevel level, BlockPos centerPos, int sectionX, int sectionZ) {
        // Create a 5x5 grid of 32x32 rooms (160x160 total)
        int gridSize = 5;
        int roomSize = 32;

        // Generate maze layout for this section
        boolean[][] maze = generateMazeLayout(sectionX, sectionZ, gridSize);

        BlockPos sectionStart = new BlockPos(
            centerPos.getX() - 80,
            64,
            centerPos.getZ() - 80
        );

        for (int gridX = 0; gridX < gridSize; gridX++) {
            for (int gridZ = 0; gridZ < gridSize; gridZ++) {
                BlockPos roomPos = sectionStart.offset(gridX * roomSize, 0, gridZ * roomSize);

                if (!maze[gridX][gridZ]) { // Open space
                    generateStudioRoom(level, roomPos, roomSize, gridX, gridZ, maze);
                } else {
                    // Fill with walls/void space
                    generateWallSection(level, roomPos, roomSize);
                }
            }
        }
    }

    private static boolean[][] generateMazeLayout(int sectionX, int sectionZ, int size) {
        // Use section coordinates as seed for deterministic generation
        long seed = (long)sectionX * 31 + (long)sectionZ * 17;
        java.util.Random random = new java.util.Random(seed);

        boolean[][] maze = new boolean[size][size];

        // Create maze using cellular automata for organic feel
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                maze[x][z] = random.nextFloat() < 0.4f; // 40% walls initially
            }
        }

        // Smooth the maze with cellular automata rules
        for (int iteration = 0; iteration < 3; iteration++) {
            boolean[][] newMaze = new boolean[size][size];
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    int wallCount = countWallNeighbors(maze, x, z, size);
                    newMaze[x][z] = wallCount >= 4;
                }
            }
            maze = newMaze;
        }

        // Ensure connectivity - carve paths between isolated areas
        ensureConnectivity(maze, size, random);

        return maze;
    }

    private static int countWallNeighbors(boolean[][] maze, int x, int z, int size) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int nx = x + dx;
                int nz = z + dz;
                if (nx < 0 || nx >= size || nz < 0 || nz >= size || maze[nx][nz]) {
                    count++;
                }
            }
        }
        return count;
    }

    private static void ensureConnectivity(boolean[][] maze, int size, java.util.Random random) {
        // Force center area to be open for player spawning
        int center = size / 2;
        maze[center][center] = false;
        maze[center-1][center] = false;
        maze[center+1][center] = false;
        maze[center][center-1] = false;
        maze[center][center+1] = false;

        // Add connecting corridors
        for (int i = 0; i < size; i++) {
            if (random.nextFloat() < 0.3f) {
                maze[i][center] = false; // Horizontal corridor
                maze[center][i] = false; // Vertical corridor
            }
        }
    }

    private static void generateStudioRoom(ServerLevel level, BlockPos pos, int roomSize, int gridX, int gridZ, boolean[][] maze) {
        // Generate floor
        for (int x = 0; x < roomSize; x++) {
            for (int z = 0; z < roomSize; z++) {
                level.setBlock(pos.offset(x, 0, z), Blocks.GRAY_CONCRETE.defaultBlockState(), 3);
            }
        }

        // Generate walls with doorways to adjacent open rooms
        for (int x = 0; x < roomSize; x++) {
            for (int z = 0; z < roomSize; z++) {
                for (int y = 1; y < 15; y++) {
                    boolean isWall = false;

                    // Check each wall face
                    if (x == 0) { // West wall
                        if (gridX == 0 || maze[gridX-1][gridZ]) {
                            isWall = true; // Wall if at edge or adjacent to wall
                        } else if (z < 12 || z > 20) {
                            isWall = true; // Partial wall with doorway
                        }
                    } else if (x == roomSize-1) { // East wall
                        if (gridX == maze.length-1 || maze[gridX+1][gridZ]) {
                            isWall = true;
                        } else if (z < 12 || z > 20) {
                            isWall = true;
                        }
                    } else if (z == 0) { // North wall
                        if (gridZ == 0 || maze[gridX][gridZ-1]) {
                            isWall = true;
                        } else if (x < 12 || x > 20) {
                            isWall = true;
                        }
                    } else if (z == roomSize-1) { // South wall
                        if (gridZ == maze[0].length-1 || maze[gridX][gridZ+1]) {
                            isWall = true;
                        } else if (x < 12 || x > 20) {
                            isWall = true;
                        }
                    }

                    if (isWall) {
                        level.setBlock(pos.offset(x, y, z), Blocks.BLACK_WOOL.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Generate ceiling
        for (int x = 0; x < roomSize; x++) {
            for (int z = 0; z < roomSize; z++) {
                level.setBlock(pos.offset(x, 15, z), Blocks.GRAY_CONCRETE.defaultBlockState(), 3);
            }
        }

        // Add room content based on position
        RoomType roomType = determineRoomType(gridX, gridZ);
        generateRoomContent(level, pos, roomSize, roomType);

        // Add lighting
        for (int x = 8; x < roomSize; x += 12) {
            for (int z = 8; z < roomSize; z += 12) {
                level.setBlock(pos.offset(x, 14, z), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
            }
        }
    }

    private static void generateWallSection(ServerLevel level, BlockPos pos, int roomSize) {
        // Fill with walls/structural elements
        for (int x = 0; x < roomSize; x++) {
            for (int z = 0; z < roomSize; z++) {
                level.setBlock(pos.offset(x, 0, z), Blocks.GRAY_CONCRETE.defaultBlockState(), 3);
                for (int y = 1; y < 15; y++) {
                    level.setBlock(pos.offset(x, y, z), Blocks.BLACK_WOOL.defaultBlockState(), 3);
                }
                level.setBlock(pos.offset(x, 15, z), Blocks.GRAY_CONCRETE.defaultBlockState(), 3);
            }
        }
    }

    private static RoomType determineRoomType(int gridX, int gridZ) {
        int hash = (gridX * 31 + gridZ * 17) % 7;
        return switch (Math.abs(hash)) {
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

    private static void generateRoomContent(ServerLevel level, BlockPos pos, int roomSize, RoomType roomType) {
        switch (roomType) {
            case CINEMA -> generateCinemaContent(level, pos, roomSize);
            case BROADCAST_ROOM -> generateBroadcastContent(level, pos, roomSize);
            case EDITING_LAB -> generateEditingContent(level, pos, roomSize);
            case SOUNDSTAGE -> generateSoundstageContent(level, pos, roomSize);
            case STORAGE -> generateStorageContent(level, pos, roomSize);
            case ANTENNA_ROOM -> generateAntennaContent(level, pos, roomSize);
            case CORRIDOR -> generateCorridorContent(level, pos, roomSize);
        }
    }

    private static void generateBroadcastContent(ServerLevel level, BlockPos pos, int roomSize) {
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

    private static void generateEditingContent(ServerLevel level, BlockPos pos, int roomSize) {
        // Workstations
        for (int x = 4; x < roomSize - 4; x += 6) {
            for (int z = 4; z < roomSize - 4; z += 6) {
                level.setBlock(pos.offset(x, 1, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(pos.offset(x + 1, 1, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);

                level.setBlock(pos.offset(x, 2, z), Blocks.DISPENSER.defaultBlockState(), 3);
                level.setBlock(pos.offset(x + 1, 2, z), Blocks.REPEATER.defaultBlockState(), 3);
            }
        }

        // Film reels
        for (int i = 0; i < 8; i++) {
            int x = 2 + (int)(Math.random() * (roomSize - 4));
            int z = 2 + (int)(Math.random() * (roomSize - 4));
            level.setBlock(pos.offset(x, 2, z), Blocks.BARREL.defaultBlockState(), 3);
        }
    }

    private static void generateSoundstageContent(ServerLevel level, BlockPos pos, int roomSize) {
        int setType = (int)(Math.random() * 3);

        switch (setType) {
            case 0 -> {
                // Haunted house set
                for (int x = 8; x < roomSize - 8; x++) {
                    for (int z = 8; z < roomSize - 8; z++) {
                        if (x == 8 || x == roomSize - 9 || z == 8 || z == roomSize - 9) {
                            level.setBlock(pos.offset(x, 1, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
                            if (Math.random() < 0.7) {
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
                net.minecraft.world.level.block.Block[] colors = {
                    Blocks.RED_WOOL, Blocks.BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.GREEN_WOOL
                };

                for (int i = 0; i < 10; i++) {
                    int x = 3 + (int)(Math.random() * (roomSize - 6));
                    int z = 3 + (int)(Math.random() * (roomSize - 6));
                    net.minecraft.world.level.block.Block color = colors[(int)(Math.random() * colors.length)];
                    level.setBlock(pos.offset(x, 1, z), color.defaultBlockState(), 3);
                    if (Math.random() < 0.5) {
                        level.setBlock(pos.offset(x, 2, z), color.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void generateStorageContent(ServerLevel level, BlockPos pos, int roomSize) {
        // Shelves
        for (int x = 2; x < roomSize - 2; x += 4) {
            for (int y = 1; y < 8; y += 2) {
                level.setBlock(pos.offset(x, y, 2), Blocks.BOOKSHELF.defaultBlockState(), 3);
                level.setBlock(pos.offset(x, y, roomSize - 3), Blocks.BOOKSHELF.defaultBlockState(), 3);
            }
        }

        // Props
        for (int i = 0; i < 15; i++) {
            int x = 2 + (int)(Math.random() * (roomSize - 4));
            int z = 2 + (int)(Math.random() * (roomSize - 4));
            net.minecraft.world.level.block.state.BlockState prop = Math.random() < 0.5 ?
                Blocks.SKELETON_SKULL.defaultBlockState() :
                Blocks.CHEST.defaultBlockState();
            level.setBlock(pos.offset(x, 2, z), prop, 3);
        }
    }

    private static void generateAntennaContent(ServerLevel level, BlockPos pos, int roomSize) {
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

    private static void generateCorridorContent(ServerLevel level, BlockPos pos, int roomSize) {
        // Sparse cables and furniture
        for (int i = 0; i < 8; i++) {
            int x = 2 + (int)(Math.random() * (roomSize - 4));
            int z = 2 + (int)(Math.random() * (roomSize - 4));
            level.setBlock(pos.offset(x, 1, z), Blocks.TRIPWIRE.defaultBlockState(), 3);
        }

        for (int i = 0; i < 3; i++) {
            int x = 4 + (int)(Math.random() * (roomSize - 8));
            int z = 4 + (int)(Math.random() * (roomSize - 8));
            level.setBlock(pos.offset(x, 1, z), Blocks.BARREL.defaultBlockState(), 3);
        }
    }

    private static void generateCinemaContent(ServerLevel level, BlockPos pos, int roomSize) {
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

    public enum RoomType {
        CORRIDOR, CINEMA, BROADCAST_ROOM, EDITING_LAB, SOUNDSTAGE, STORAGE, ANTENNA_ROOM
    }
}
