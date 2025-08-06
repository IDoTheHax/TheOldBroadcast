package net.idothehax.theoldbroadcast.world.generation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.idothehax.theoldbroadcast.world.generation.maze.MazeGenerator;
import net.idothehax.theoldbroadcast.world.generation.maze.MazeCell;
import net.idothehax.theoldbroadcast.world.generation.rooms.RoomTemplate;
import net.idothehax.theoldbroadcast.world.generation.rooms.RoomManager;
import net.idothehax.theoldbroadcast.world.generation.noise.NoiseManager;
import net.idothehax.theoldbroadcast.world.generation.performance.PerformanceManager;
import net.idothehax.theoldbroadcast.world.generation.navigation.NavigationAids;
import net.idothehax.theoldbroadcast.world.generation.config.TheOldStudioConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced procedural generation system for infinite Backrooms dimension
 * Combines maze generation, room templates, and noise-based variation
 */
public class TheOldStudioGenerator {

    // Core generation parameters
    public static final int CHUNK_SIZE = 16;
    public static final int BASE_HEIGHT = 66; // Set fixed Y for all generation

    // Noise generators for organic variation
    private final NoiseManager noiseManager;
    private final MazeGenerator mazeGenerator;
    private final RoomManager roomManager;
    private final PerformanceManager performanceManager;

    // Chunk generation cache for performance
    private final Map<Long, MazeCell> mazeGrid = new ConcurrentHashMap<>();

    public TheOldStudioGenerator(long seed) {
        TheOldStudioConfig.validateConfig(); // Ensure valid configuration

        this.noiseManager = new NoiseManager(seed);
        this.mazeGenerator = new MazeGenerator(seed);
        this.roomManager = new RoomManager(seed);
        this.performanceManager = new PerformanceManager();
    }

    /**
     * Main generation entry point for chunk loading
     */
    public void generateChunk(WorldGenLevel level, ChunkAccess chunk, RandomSource random) {
        ChunkPos chunkPos = chunk.getPos();
        if (!performanceManager.shouldGenerateChunk(chunkPos, level)) {
            return;
        }
        long startTime = System.currentTimeMillis();
        performanceManager.markChunkGenerationStart(chunkPos);
        try {
            // 1. Generate maze grid for corridors using Prim's algorithm (no rooms for now)
            boolean[][] mazeGrid = generatePrimMazeGrid(chunkPos, random);
            // 2. Carve corridors (air, not solid, no pillars)
            carveCorridors(level, chunkPos, mazeGrid);
            // 3. Add extra corridor loops for connectivity
            addCorridorLoops(level, chunkPos, mazeGrid, random);
            // 4. Remove stray pillars (isolated stone bricks)
            removeStrayPillars(level, chunkPos);
            // 5. Add props and details
            generateProps(level, chunkPos, random);
            // 6. Add navigation aids
            NavigationAids.placeNavigationAids(level, new BlockPos(chunkPos.getMinBlockX(), BASE_HEIGHT, chunkPos.getMinBlockZ()), random, noiseManager);
        } finally {
            long generationTime = System.currentTimeMillis() - startTime;
            performanceManager.markChunkGenerationComplete(chunkPos, generationTime);
        }
    }

    /**
     * Generates a maze grid using Prim's algorithm, no rooms
     */
    private boolean[][] generatePrimMazeGrid(ChunkPos chunkPos, RandomSource random) {
        int gridSize = CHUNK_SIZE / 2;
        boolean[][] maze = new boolean[gridSize][gridSize];
        List<int[]> frontier = new ArrayList<>();
        int sx = random.nextInt(gridSize);
        int sz = random.nextInt(gridSize);
        maze[sx][sz] = true;
        addFrontierCells(sx, sz, gridSize, maze, frontier);
        while (!frontier.isEmpty()) {
            int[] cell = frontier.remove(random.nextInt(frontier.size()));
            int cx = cell[0], cz = cell[1];
            List<int[]> neighbors = getMazeNeighbors(cx, cz, maze);
            if (!neighbors.isEmpty()) {
                int[] neighbor = neighbors.get(random.nextInt(neighbors.size()));
                maze[cx][cz] = true;
                addFrontierCells(cx, cz, gridSize, maze, frontier);
            }
        }
        return maze;
    }

    /**
     * Carves corridors as air blocks, no pillars, no solid walls, and removes any wool structures
     */
    private void carveCorridors(WorldGenLevel level, ChunkPos chunkPos, boolean[][] maze) {
        int gridSize = maze.length;
        for (int x = 0; x < gridSize; x++) {
            for (int z = 0; z < gridSize; z++) {
                if (maze[x][z]) {
                    int wx = chunkPos.getMinBlockX() + x * 2;
                    int wz = chunkPos.getMinBlockZ() + z * 2;
                    // Carve a 3x3 air corridor at BASE_HEIGHT
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            for (int y = 0; y <= 3; y++) {
                                BlockPos pos = new BlockPos(wx + dx, BASE_HEIGHT + y, wz + dz);
                                level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }
        // Remove all wool blocks in the chunk area
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = BASE_HEIGHT; y <= BASE_HEIGHT + 3; y++) {
                    BlockPos pos = new BlockPos(minX + x, y, minZ + z);
                    if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.WHITE_WOOL) ||
                        level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.LIGHT_GRAY_WOOL) ||
                        level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.GRAY_WOOL) ||
                        level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BLACK_WOOL) ||
                        level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BROWN_WOOL) ||
                        level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.YELLOW_WOOL)) {
                        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    /**
     * Removes stray pillars by filling any isolated stone bricks with air
     */
    private void removeStrayPillars(WorldGenLevel level, ChunkPos chunkPos) {
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = BASE_HEIGHT; y <= BASE_HEIGHT + 3; y++) {
                    BlockPos pos = new BlockPos(minX + x, y, minZ + z);
                    if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.STONE_BRICKS)) {
                        boolean surroundedByAir = true;
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                if (dx == 0 && dz == 0) continue;
                                BlockPos neighbor = pos.offset(dx, 0, dz);
                                if (!level.getBlockState(neighbor).isAir()) {
                                    surroundedByAir = false;
                                    break;
                                }
                            }
                            if (!surroundedByAir) break;
                        }
                        if (surroundedByAir) {
                            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds extra corridor connections (loops) for better navigation
     */
    private void addCorridorLoops(WorldGenLevel level, ChunkPos chunkPos, boolean[][] maze, RandomSource random) {
        int gridSize = maze.length;
        int loops = 1 + random.nextInt(2);
        for (int i = 0; i < loops; i++) {
            int x = random.nextInt(gridSize);
            int z = random.nextInt(gridSize);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) != 1) continue;
                    int nx = x + dx, nz = z + dz;
                    if (nx >= 0 && nz >= 0 && nx < gridSize && nz < gridSize && maze[x][z] && maze[nx][nz]) {
                        int wx = chunkPos.getMinBlockX() + x * 2;
                        int wz = chunkPos.getMinBlockZ() + z * 2;
                        for (int y = 0; y <= 3; y++) {
                            level.setBlock(new BlockPos(wx, BASE_HEIGHT + y, wz), net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Room area helper for corridor avoidance
     */
    private static class RoomArea {
        public final int x, z, size;
        public RoomArea(int x, int z, int size) {
            this.x = x;
            this.z = z;
            this.size = size;
        }
        public boolean contains(int wx, int wz) {
            return Math.abs(wx - x) <= size && Math.abs(wz - z) <= size;
        }
    }

    /**
     * Generates the underlying maze structure using advanced algorithms
     */
    private void generateMazeStructure(ChunkPos chunkPos, RandomSource random) {
        int startX = chunkPos.getMinBlockX() / TheOldStudioConfig.GRID_SIZE;
        int startZ = chunkPos.getMinBlockZ() / TheOldStudioConfig.GRID_SIZE;
        int endX = chunkPos.getMaxBlockX() / TheOldStudioConfig.GRID_SIZE;
        int endZ = chunkPos.getMaxBlockZ() / TheOldStudioConfig.GRID_SIZE;

        // Generate maze cells for chunk area with buffer for connectivity
        for (int x = startX - 1; x <= endX + 1; x++) {
            for (int z = startZ - 1; z <= endZ + 1; z++) {
                long cellKey = getCellKey(x, z);

                if (!mazeGrid.containsKey(cellKey)) {
                    MazeCell cell = mazeGenerator.generateCell(x, z, random);
                    mazeGrid.put(cellKey, cell);
                }
            }
        }

        // Connect adjacent cells using dynamic connectivity
        connectAdjacentCells(startX - 1, startZ - 1, endX + 1, endZ + 1, random);
    }

    /**
     * Generates rooms and corridors based on maze structure
     */
    private void generateRoomsAndCorridors(WorldGenLevel level, ChunkPos chunkPos, RandomSource random) {
        int startX = chunkPos.getMinBlockX() / TheOldStudioConfig.GRID_SIZE;
        int startZ = chunkPos.getMinBlockZ() / TheOldStudioConfig.GRID_SIZE;
        int endX = chunkPos.getMaxBlockX() / TheOldStudioConfig.GRID_SIZE;
        int endZ = chunkPos.getMaxBlockZ() / TheOldStudioConfig.GRID_SIZE;

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                long cellKey = getCellKey(x, z);
                MazeCell cell = mazeGrid.get(cellKey);

                if (cell != null) {
                    BlockPos cellPos = new BlockPos(x * TheOldStudioConfig.GRID_SIZE, BASE_HEIGHT, z * TheOldStudioConfig.GRID_SIZE);

                    // Determine if this should be a mega room
                    if (random.nextFloat() < TheOldStudioConfig.MEGA_ROOM_CHANCE && canPlaceMegaRoom(x, z)) {
                        generateMegaRoom(level, cellPos, random);
                    } else if (cell.isRoom()) {
                        // Generate regular room with template variation
                        RoomTemplate template = roomManager.selectTemplate(cell, random);
                        template.generate(level, cellPos, random, noiseManager);
                    } else {
                        // Generate corridor
                        generateCorridor(level, cellPos, cell, random);
                    }
                }
            }
        }
    }

    /**
     * Applies noise-based variations for organic feel
     */
    private void applyNoiseVariations(WorldGenLevel level, ChunkPos chunkPos, RandomSource random) {
        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                // Apply ceiling height variation
                applyCeilingVariation(level, worldX, worldZ);

                // Apply wall irregularities
                applyWallVariation(level, worldX, worldZ, random);

                // Apply lighting variations
                applyLightingVariation(level, worldX, worldZ, random);
            }
        }
    }

    /**
     * Connects adjacent maze cells with dynamic probability
     */
    private void connectAdjacentCells(int startX, int startZ, int endX, int endZ, RandomSource random) {
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                MazeCell current = mazeGrid.get(getCellKey(x, z));
                if (current == null) continue;

                // Check all four directions
                checkAndCreateConnection(current, x, z, x + 1, z, random); // East
                checkAndCreateConnection(current, x, z, x, z + 1, random); // South
                checkAndCreateConnection(current, x, z, x - 1, z, random); // West
                checkAndCreateConnection(current, x, z, x, z - 1, random); // North
            }
        }
    }

    private void checkAndCreateConnection(MazeCell current, int x1, int z1, int x2, int z2, RandomSource random) {
        MazeCell neighbor = mazeGrid.get(getCellKey(x2, z2));
        if (neighbor == null) return;

        // Dynamic connection probability based on noise and distance
        float connectionNoise = noiseManager.getConnectionNoise((double)(x1 * TheOldStudioConfig.GRID_SIZE), (double)(z1 * TheOldStudioConfig.GRID_SIZE));
        float adjustedProbability = TheOldStudioConfig.CONNECTION_PROBABILITY + (connectionNoise * 0.3f);

        if (random.nextFloat() < adjustedProbability) {
            current.addConnection(getDirection(x1, z1, x2, z2));
            neighbor.addConnection(getDirection(x2, z2, x1, z1));
        }
    }

    /**
     * Generates mega rooms for variety
     */
    private void generateMegaRoom(WorldGenLevel level, BlockPos pos, RandomSource random) {
        int megaSize = TheOldStudioConfig.GRID_SIZE * (2 + random.nextInt(3)); // 2x2 to 4x4 grid size
        RoomTemplate megaTemplate = roomManager.getMegaRoomTemplate(random);
        megaTemplate.generate(level, pos, random, noiseManager);

        // Mark surrounding cells as occupied
        markMegaRoomCells(pos.getX() / TheOldStudioConfig.GRID_SIZE, pos.getZ() / TheOldStudioConfig.GRID_SIZE, megaSize / TheOldStudioConfig.GRID_SIZE);
    }

    /**
     * Generates corridors with dynamic width and connections
     */
    private void generateCorridor(WorldGenLevel level, BlockPos pos, MazeCell cell, RandomSource random) {
        // Base corridor generation
        roomManager.generateBaseCorridor(level, pos, TheOldStudioConfig.CORRIDOR_WIDTH, random);

        // Add connections based on cell data
        for (MazeCell.Direction direction : cell.getConnections()) {
            roomManager.createConnection(level, pos, direction, random);
        }
    }

    /**
     * Applies ceiling height variation using noise
     */
    private void applyCeilingVariation(WorldGenLevel level, int x, int z) {
        float heightNoise = noiseManager.getCeilingNoise((double)x, (double)z);
        float wallNoise = noiseManager.getWallNoise((double)x, (double)z);
        float lightNoise = noiseManager.getLightingNoise((double)x, (double)z);

        // Calculate ceiling height
        int finalHeight = BASE_HEIGHT + TheOldStudioConfig.ROOM_HEIGHT;
        BlockPos ceilingPos = new BlockPos(x, finalHeight, z);
        if (level.getBlockState(ceilingPos).isAir()) {
            level.setBlock(ceilingPos, roomManager.getCeilingBlock(x, z), 3);
        }
    }

    /**
     * Applies wall irregularities for organic feel
     */
    private void applyWallVariation(WorldGenLevel level, int x, int z, RandomSource random) {
        float wallNoise = noiseManager.getWallNoise(x, z);

        if (wallNoise > 0.7f && random.nextFloat() < TheOldStudioConfig.WALL_IRREGULARITY_CHANCE) {
            // Add wall protrusion or damage
            for (int y = BASE_HEIGHT + 1; y < BASE_HEIGHT + TheOldStudioConfig.ROOM_HEIGHT; y++) {
                BlockPos wallPos = new BlockPos(x, y, z);
                if (level.getBlockState(wallPos).isAir()) {
                    level.setBlock(wallPos, roomManager.getWallVariationBlock(random), 3);
                }
            }
        }
    }

    /**
     * Applies lighting variation for atmosphere
     */
    private void applyLightingVariation(WorldGenLevel level, int x, int z, RandomSource random) {
        float lightNoise = noiseManager.getLightingNoise(x, z);

        if (lightNoise > 0.8f && random.nextFloat() < TheOldStudioConfig.LIGHTING_VARIATION_CHANCE) {
            // Place flickering light source
            BlockPos lightPos = new BlockPos(x, BASE_HEIGHT + TheOldStudioConfig.ROOM_HEIGHT - 1, z);
            level.setBlock(lightPos, roomManager.getLightBlock(random), 3);
        }
    }

    /**
     * Generates procedural props and details
     */
    private void generateProps(WorldGenLevel level, ChunkPos chunkPos, RandomSource random) {
        roomManager.scatterProps(level, chunkPos, random, noiseManager);
    }

    // Helper for Prim's algorithm: add frontier cells
    private void addFrontierCells(int x, int z, int gridSize, boolean[][] maze, List<int[]> frontier) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) + Math.abs(dz) != 1) continue;
                int nx = x + dx, nz = z + dz;
                if (nx >= 0 && nz >= 0 && nx < gridSize && nz < gridSize && !maze[nx][nz]) {
                    frontier.add(new int[]{nx, nz});
                }
            }
        }
    }

    // Helper for Prim's algorithm: get maze neighbors
    private List<int[]> getMazeNeighbors(int x, int z, boolean[][] maze) {
        List<int[]> neighbors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) + Math.abs(dz) != 1) continue;
                int nx = x + dx, nz = z + dz;
                if (nx >= 0 && nz >= 0 && nx < maze.length && nz < maze[0].length && maze[nx][nz]) {
                    neighbors.add(new int[]{nx, nz});
                }
            }
        }
        return neighbors;
    }

    // Utility methods
    private long getCellKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private MazeCell.Direction getDirection(int x1, int z1, int x2, int z2) {
        if (x2 > x1) return MazeCell.Direction.EAST;
        if (x2 < x1) return MazeCell.Direction.WEST;
        if (z2 > z1) return MazeCell.Direction.SOUTH;
        return MazeCell.Direction.NORTH;
    }

    private boolean canPlaceMegaRoom(int x, int z) {
        // Check if surrounding cells are available for mega room
        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                long key = getCellKey(x + dx, z + dz);
                MazeCell cell = mazeGrid.get(key);
                if (cell != null && cell.isOccupied()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void markMegaRoomCells(int x, int z, int size) {
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                long key = getCellKey(x + dx, z + dz);
                MazeCell cell = mazeGrid.get(key);
                if (cell != null) {
                    cell.setOccupied(true);
                }
            }
        }
    }

    // Performance and debugging methods
    public PerformanceManager getPerformanceManager() {
        return performanceManager;
    }

    public int getMazeGridSize() {
        return mazeGrid.size();
    }

    public void clearCache() {
        mazeGrid.clear();
        performanceManager.clearCache();
    }

    // Data classes
    public static class GeneratedChunkData {
        private final long generationTime;

        public GeneratedChunkData(long generationTime) {
            this.generationTime = generationTime;
        }

        public long getGenerationTime() {
            return generationTime;
        }
    }
}
