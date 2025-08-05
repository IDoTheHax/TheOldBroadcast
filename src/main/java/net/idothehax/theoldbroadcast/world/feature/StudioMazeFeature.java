package net.idothehax.theoldbroadcast.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.*;

public class StudioMazeFeature extends Feature<NoneFeatureConfiguration> {

    private static final int GRID_SIZE = 80; // 5 chunks x 16 blocks = 80 blocks
    private static final int CELL_SIZE = 16; // Each maze cell is 16 blocks
    private static final int MAZE_CELLS = GRID_SIZE / CELL_SIZE; // 5x5 maze grid

    public StudioMazeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // Generate maze for this 5x5 chunk grid
        boolean[][] maze = generateMaze(random);

        // Place the maze structure
        generateMazeStructure(level, pos, maze, random);

        return true;
    }

    private boolean[][] generateMaze(RandomSource random) {
        // Create a 5x5 grid where true = wall, false = open space
        boolean[][] maze = new boolean[MAZE_CELLS][MAZE_CELLS];

        // Initialize all as walls
        for (int x = 0; x < MAZE_CELLS; x++) {
            for (int z = 0; z < MAZE_CELLS; z++) {
                maze[x][z] = true;
            }
        }

        // Generate maze using recursive backtracking
        Stack<int[]> stack = new Stack<>();
        List<int[]> visited = new ArrayList<>();

        // Start at random position
        int startX = random.nextInt(MAZE_CELLS);
        int startZ = random.nextInt(MAZE_CELLS);

        maze[startX][startZ] = false; // Open starting cell
        stack.push(new int[]{startX, startZ});
        visited.add(new int[]{startX, startZ});

        // Direction vectors (N, S, E, W)
        int[][] directions = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0];
            int z = current[1];

            List<int[]> neighbors = new ArrayList<>();

            // Find unvisited neighbors
            for (int[] dir : directions) {
                int nx = x + dir[0];
                int nz = z + dir[1];

                if (nx >= 0 && nx < MAZE_CELLS && nz >= 0 && nz < MAZE_CELLS) {
                    boolean isVisited = visited.stream().anyMatch(v -> v[0] == nx && v[1] == nz);
                    if (!isVisited) {
                        neighbors.add(new int[]{nx, nz});
                    }
                }
            }

            if (!neighbors.isEmpty()) {
                // Choose random neighbor
                int[] next = neighbors.get(random.nextInt(neighbors.size()));
                maze[next[0]][next[1]] = false; // Open the cell

                stack.push(next);
                visited.add(next);
            } else {
                stack.pop();
            }
        }

        // Add some random openings for variety (15% chance per wall cell)
        for (int x = 0; x < MAZE_CELLS; x++) {
            for (int z = 0; z < MAZE_CELLS; z++) {
                if (maze[x][z] && random.nextFloat() < 0.15f) {
                    maze[x][z] = false;
                }
            }
        }

        return maze;
    }

    private void generateMazeStructure(WorldGenLevel level, BlockPos startPos, boolean[][] maze, RandomSource random) {
        for (int mazeX = 0; mazeX < MAZE_CELLS; mazeX++) {
            for (int mazeZ = 0; mazeZ < MAZE_CELLS; mazeZ++) {
                BlockPos cellPos = startPos.offset(mazeX * CELL_SIZE, 0, mazeZ * CELL_SIZE);

                if (maze[mazeX][mazeZ]) {
                    // Generate wall cell with slight randomization
                    generateWallCell(level, cellPos, random);
                } else {
                    // Generate open cell (studio room)
                    generateOpenCell(level, cellPos, random);
                }
            }
        }

        // Add mega rooms occasionally
        if (random.nextFloat() < 0.3f) {
            generateMegaRoom(level, startPos, maze, random);
        }
    }

    private void generateWallCell(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Fill most of the cell with walls, but add some variation
        for (int x = 0; x < CELL_SIZE; x++) {
            for (int z = 0; z < CELL_SIZE; z++) {
                for (int y = 65; y < 80; y++) {
                    // Add some randomness to wall placement for organic feel
                    if (x == 0 || x == CELL_SIZE - 1 || z == 0 || z == CELL_SIZE - 1 ||
                        random.nextFloat() < 0.8f) {
                        level.setBlock(pos.offset(x, y, z), Blocks.BLACK_WOOL.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void generateOpenCell(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Clear the cell and add room content
        for (int x = 0; x < CELL_SIZE; x++) {
            for (int z = 0; z < CELL_SIZE; z++) {
                // Floor
                level.setBlock(pos.offset(x, 64, z), Blocks.GRAY_CONCRETE.defaultBlockState(), 3);

                // Clear air space
                for (int y = 65; y < 80; y++) {
                    level.setBlock(pos.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }

                // Ceiling
                level.setBlock(pos.offset(x, 80, z), Blocks.GRAY_CONCRETE.defaultBlockState(), 3);
            }
        }

        // Add room-specific content
        StudioRoomType roomType = determineRoomType(pos.getX(), pos.getZ(), random);
        generateRoomContent(level, pos, roomType, random);

        // Add some interior walls for complexity
        generateInteriorWalls(level, pos, random);

        // Add lighting
        generateCellLighting(level, pos, random);
    }

    private void generateInteriorWalls(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Add some random interior walls to break up large spaces
        for (int i = 0; i < 3; i++) {
            if (random.nextFloat() < 0.4f) {
                int wallX = 2 + random.nextInt(CELL_SIZE - 4);
                int wallStart = 2 + random.nextInt(6);
                int wallEnd = wallStart + 4 + random.nextInt(4);

                for (int z = wallStart; z < Math.min(wallEnd, CELL_SIZE - 2); z++) {
                    for (int y = 65; y < 75; y++) {
                        level.setBlock(pos.offset(wallX, y, z), Blocks.BLACK_WOOL.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void generateMegaRoom(WorldGenLevel level, BlockPos startPos, boolean[][] maze, RandomSource random) {
        // Find a 2x2 area of open cells and create a mega room
        for (int x = 0; x < MAZE_CELLS - 1; x++) {
            for (int z = 0; z < MAZE_CELLS - 1; z++) {
                if (!maze[x][z] && !maze[x+1][z] && !maze[x][z+1] && !maze[x+1][z+1]) {
                    if (random.nextFloat() < 0.7f) {
                        generateLargeCinema(level, startPos.offset(x * CELL_SIZE, 0, z * CELL_SIZE), random);
                        return;
                    }
                }
            }
        }
    }

    private void generateLargeCinema(WorldGenLevel level, BlockPos pos, RandomSource random) {
        int megaSize = CELL_SIZE * 2;

        // Clear the mega room
        for (int x = 0; x < megaSize; x++) {
            for (int z = 0; z < megaSize; z++) {
                for (int y = 65; y < 85; y++) {
                    level.setBlock(pos.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
                // Higher ceiling for mega rooms
                level.setBlock(pos.offset(x, 85, z), Blocks.GRAY_CONCRETE.defaultBlockState(), 3);
            }
        }

        // Generate large cinema with tiered seating
        for (int row = 5; row < megaSize - 5; row += 2) {
            int seatHeight = 65 + (row / 8); // Tiered seating
            for (int seat = 3; seat < megaSize - 3; seat += 2) {
                level.setBlock(pos.offset(seat, seatHeight, row), Blocks.BLACK_WOOL.defaultBlockState(), 3);
            }
        }

        // Large screen
        for (int x = 4; x < megaSize - 4; x++) {
            for (int y = 66; y < 80; y++) {
                level.setBlock(pos.offset(x, y, megaSize - 2), Blocks.WHITE_WOOL.defaultBlockState(), 3);
            }
        }

        // Multiple projectors
        level.setBlock(pos.offset(megaSize/2 - 2, 75, 5), Blocks.OBSERVER.defaultBlockState(), 3);
        level.setBlock(pos.offset(megaSize/2 + 2, 75, 5), Blocks.OBSERVER.defaultBlockState(), 3);
    }

    private StudioRoomType determineRoomType(int x, int z, RandomSource random) {
        int hash = (x / CELL_SIZE * 31 + z / CELL_SIZE) * 17;
        int type = Math.abs(hash + random.nextInt(3)) % 7;

        return switch (type) {
            case 0 -> StudioRoomType.CORRIDOR;
            case 1 -> StudioRoomType.CINEMA;
            case 2 -> StudioRoomType.BROADCAST_ROOM;
            case 3 -> StudioRoomType.EDITING_LAB;
            case 4 -> StudioRoomType.SOUNDSTAGE;
            case 5 -> StudioRoomType.STORAGE;
            case 6 -> StudioRoomType.ANTENNA_ROOM;
            default -> StudioRoomType.CORRIDOR;
        };
    }

    private void generateRoomContent(WorldGenLevel level, BlockPos pos, StudioRoomType roomType, RandomSource random) {
        switch (roomType) {
            case CINEMA -> generateSmallCinema(level, pos, random);
            case BROADCAST_ROOM -> generateBroadcastContent(level, pos, random);
            case EDITING_LAB -> generateEditingContent(level, pos, random);
            case SOUNDSTAGE -> generateSoundstageContent(level, pos, random);
            case STORAGE -> generateStorageContent(level, pos, random);
            case ANTENNA_ROOM -> generateAntennaContent(level, pos, random);
            case CORRIDOR -> generateCorridorContent(level, pos, random);
        }
    }

    private void generateSmallCinema(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Compact cinema for single cell
        for (int row = 3; row < CELL_SIZE - 5; row += 2) {
            for (int seat = 2; seat < CELL_SIZE - 2; seat += 2) {
                level.setBlock(pos.offset(seat, 65, row), Blocks.BLACK_WOOL.defaultBlockState(), 3);
            }
        }

        // Small screen
        for (int x = 3; x < CELL_SIZE - 3; x++) {
            for (int y = 66; y < 72; y++) {
                level.setBlock(pos.offset(x, y, CELL_SIZE - 2), Blocks.WHITE_WOOL.defaultBlockState(), 3);
            }
        }

        level.setBlock(pos.offset(CELL_SIZE/2, 70, 2), Blocks.OBSERVER.defaultBlockState(), 3);
    }

    private void generateBroadcastContent(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Control desks
        for (int x = 3; x < CELL_SIZE - 3; x += 3) {
            for (int z = 3; z < CELL_SIZE - 3; z += 3) {
                level.setBlock(pos.offset(x, 65, z), Blocks.IRON_BLOCK.defaultBlockState(), 3);
                level.setBlock(pos.offset(x, 66, z), Blocks.OBSERVER.defaultBlockState(), 3);
            }
        }

        // TV screens
        for (int i = 0; i < 4; i++) {
            int x = 2 + i * 3;
            if (x < CELL_SIZE - 2) {
                level.setBlock(pos.offset(x, 68, 1), Blocks.BLACK_STAINED_GLASS.defaultBlockState(), 3);
            }
        }
    }

    private void generateEditingContent(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Workstations
        for (int x = 2; x < CELL_SIZE - 2; x += 4) {
            for (int z = 2; z < CELL_SIZE - 2; z += 4) {
                level.setBlock(pos.offset(x, 65, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(pos.offset(x, 66, z), Blocks.DISPENSER.defaultBlockState(), 3);
            }
        }

        // Film reels
        for (int i = 0; i < 4; i++) {
            int x = 1 + random.nextInt(CELL_SIZE - 2);
            int z = 1 + random.nextInt(CELL_SIZE - 2);
            level.setBlock(pos.offset(x, 66, z), Blocks.BARREL.defaultBlockState(), 3);
        }
    }

    private void generateSoundstageContent(WorldGenLevel level, BlockPos pos, RandomSource random) {
        int setType = random.nextInt(3);

        switch (setType) {
            case 0 -> {
                // Mini haunted house
                for (int x = 4; x < CELL_SIZE - 4; x++) {
                    for (int z = 4; z < CELL_SIZE - 4; z++) {
                        if (x == 4 || x == CELL_SIZE - 5 || z == 4 || z == CELL_SIZE - 5) {
                            level.setBlock(pos.offset(x, 65, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
                            if (random.nextFloat() < 0.6f) {
                                level.setBlock(pos.offset(x, 66, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
            case 1 -> {
                // Sci-fi set
                for (int x = 3; x < CELL_SIZE - 3; x += 2) {
                    for (int z = 3; z < CELL_SIZE - 3; z += 2) {
                        level.setBlock(pos.offset(x, 65, z), Blocks.IRON_BLOCK.defaultBlockState(), 3);
                        level.setBlock(pos.offset(x, 66, z), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
                    }
                }
            }
            case 2 -> {
                // Children's show
                BlockState[] colors = {
                    Blocks.RED_WOOL.defaultBlockState(),
                    Blocks.BLUE_WOOL.defaultBlockState(),
                    Blocks.YELLOW_WOOL.defaultBlockState(),
                    Blocks.GREEN_WOOL.defaultBlockState()
                };

                for (int i = 0; i < 6; i++) {
                    int x = 2 + random.nextInt(CELL_SIZE - 4);
                    int z = 2 + random.nextInt(CELL_SIZE - 4);
                    BlockState color = colors[random.nextInt(colors.length)];
                    level.setBlock(pos.offset(x, 65, z), color, 3);
                }
            }
        }
    }

    private void generateStorageContent(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Shelves along walls
        for (int x = 1; x < CELL_SIZE - 1; x += 3) {
            for (int y = 65; y < 70; y += 2) {
                level.setBlock(pos.offset(x, y, 1), Blocks.BOOKSHELF.defaultBlockState(), 3);
                level.setBlock(pos.offset(x, y, CELL_SIZE - 2), Blocks.BOOKSHELF.defaultBlockState(), 3);
            }
        }

        // Props
        for (int i = 0; i < 8; i++) {
            int x = 1 + random.nextInt(CELL_SIZE - 2);
            int z = 1 + random.nextInt(CELL_SIZE - 2);
            BlockState prop = random.nextFloat() < 0.5f ?
                Blocks.SKELETON_SKULL.defaultBlockState() :
                Blocks.CHEST.defaultBlockState();
            level.setBlock(pos.offset(x, 66, z), prop, 3);
        }
    }

    private void generateAntennaContent(WorldGenLevel level, BlockPos pos, RandomSource random) {
        int centerX = CELL_SIZE / 2;
        int centerZ = CELL_SIZE / 2;

        // Antenna tower
        for (int y = 65; y < 90; y++) {
            level.setBlock(pos.offset(centerX, y, centerZ), Blocks.IRON_BARS.defaultBlockState(), 3);
            if (y % 3 == 0) {
                level.setBlock(pos.offset(centerX - 1, y, centerZ), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
                level.setBlock(pos.offset(centerX + 1, y, centerZ), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    private void generateCorridorContent(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Minimal decoration - just some cables and sparse furniture
        for (int i = 0; i < 4; i++) {
            int x = 1 + random.nextInt(CELL_SIZE - 2);
            int z = 1 + random.nextInt(CELL_SIZE - 2);
            level.setBlock(pos.offset(x, 65, z), Blocks.TRIPWIRE.defaultBlockState(), 3);
        }

        if (random.nextFloat() < 0.3f) {
            int x = 2 + random.nextInt(CELL_SIZE - 4);
            int z = 2 + random.nextInt(CELL_SIZE - 4);
            level.setBlock(pos.offset(x, 65, z), Blocks.BARREL.defaultBlockState(), 3);
        }
    }

    private void generateCellLighting(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Sparse, atmospheric lighting
        for (int x = 4; x < CELL_SIZE; x += 6) {
            for (int z = 4; z < CELL_SIZE; z += 6) {
                if (random.nextFloat() < 0.6f) {
                    level.setBlock(pos.offset(x, 78, z), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
                }
            }
        }
    }

    public enum StudioRoomType {
        CORRIDOR, CINEMA, BROADCAST_ROOM, EDITING_LAB, SOUNDSTAGE, STORAGE, ANTENNA_ROOM
    }
}
