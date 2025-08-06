package net.idothehax.theoldbroadcast.world.generation.maze;

import net.minecraft.util.RandomSource;
import net.idothehax.theoldbroadcast.world.generation.noise.NoiseManager;

import java.util.*;

/**
 * Advanced maze generator using multiple algorithms for variety
 * Combines depth-first search, Prim's algorithm, and cellular automata
 */
public class MazeGenerator {

    private final long seed;
    private final Random seedRandom;
    private final NoiseManager noiseManager;

    // Algorithm weights for hybrid generation
    private static final float DEPTH_FIRST_WEIGHT = 0.4f;
    private static final float PRIMS_WEIGHT = 0.3f;
    private static final float CELLULAR_WEIGHT = 0.2f;
    private static final float ORGANIC_WEIGHT = 0.1f;

    public MazeGenerator(long seed) {
        this.seed = seed;
        this.seedRandom = new Random(seed);
        this.noiseManager = new NoiseManager(seed);
    }

    /**
     * Generates a maze cell using hybrid algorithms
     */
    public MazeCell generateCell(int x, int z, RandomSource random) {
        // Use position-based seeded random for consistency
        Random cellRandom = new Random(seed ^ (((long) x << 32) | (z & 0xFFFFFFFFL)));

        // Determine cell type based on noise and algorithm selection
        float algorithmNoise = noiseManager.getAlgorithmNoise(x * 32, z * 32);
        MazeGenerationAlgorithm algorithm = selectAlgorithm(algorithmNoise);

        // Generate base cell
        MazeCell cell = new MazeCell(x, z);

        // Apply algorithm-specific generation
        switch (algorithm) {
            case DEPTH_FIRST -> generateDepthFirst(cell, cellRandom);
            case PRIMS -> generatePrims(cell, cellRandom);
            case CELLULAR -> generateCellular(cell, cellRandom);
            case ORGANIC -> generateOrganic(cell, cellRandom);
        }

        // Apply noise-based modifications
        applyNoiseModifications(cell, cellRandom);

        return cell;
    }

    /**
     * Selects generation algorithm based on noise value
     */
    private MazeGenerationAlgorithm selectAlgorithm(float noise) {
        if (noise < DEPTH_FIRST_WEIGHT) {
            return MazeGenerationAlgorithm.DEPTH_FIRST;
        } else if (noise < DEPTH_FIRST_WEIGHT + PRIMS_WEIGHT) {
            return MazeGenerationAlgorithm.PRIMS;
        } else if (noise < DEPTH_FIRST_WEIGHT + PRIMS_WEIGHT + CELLULAR_WEIGHT) {
            return MazeGenerationAlgorithm.CELLULAR;
        } else {
            return MazeGenerationAlgorithm.ORGANIC;
        }
    }

    /**
     * Depth-first search algorithm - creates winding paths
     */
    private void generateDepthFirst(MazeCell cell, Random random) {
        // Bias towards creating rooms in depth-first areas
        if (random.nextFloat() < 0.6f) {
            cell.setCellType(MazeCell.CellType.ROOM);
            cell.setRoomSize(8 + random.nextInt(16)); // 8-24 block rooms
        } else {
            cell.setCellType(MazeCell.CellType.CORRIDOR);
        }

        // Depth-first tends to create fewer but longer connections
        int connectionCount = random.nextFloat() < 0.7f ? 1 + random.nextInt(2) : 2 + random.nextInt(2);
        generateRandomConnections(cell, connectionCount, random);
    }

    /**
     * Prim's algorithm - creates more uniform maze structure
     */
    private void generatePrims(MazeCell cell, Random random) {
        // Prim's algorithm creates more balanced room/corridor distribution
        if (random.nextFloat() < 0.5f) {
            cell.setCellType(MazeCell.CellType.ROOM);
            cell.setRoomSize(12 + random.nextInt(12)); // 12-24 block rooms
        } else {
            cell.setCellType(MazeCell.CellType.CORRIDOR);
        }

        // More uniform connection distribution
        int connectionCount = 1 + random.nextInt(3);
        generateRandomConnections(cell, connectionCount, random);
    }

    /**
     * Cellular automata - creates organic, cave-like structures
     */
    private void generateCellular(MazeCell cell, Random random) {
        // Cellular automata creates larger, more open spaces
        if (random.nextFloat() < 0.7f) {
            cell.setCellType(MazeCell.CellType.ROOM);
            cell.setRoomSize(16 + random.nextInt(16)); // 16-32 block rooms
        } else {
            cell.setCellType(MazeCell.CellType.CORRIDOR);
        }

        // Cellular tends to create more connections for open feel
        int connectionCount = 2 + random.nextInt(3);
        generateRandomConnections(cell, connectionCount, random);
    }

    /**
     * Organic generation - uses noise for natural variation
     */
    private void generateOrganic(MazeCell cell, Random random) {
        float organicNoise = noiseManager.getOrganicNoise(cell.getX() * 32, cell.getZ() * 32);

        // Noise determines room vs corridor
        if (organicNoise > 0.3f) {
            cell.setCellType(MazeCell.CellType.ROOM);
            // Noise-based size variation
            int baseSize = 8 + Math.round(organicNoise * 20);
            cell.setRoomSize(Math.max(8, Math.min(28, baseSize)));
        } else {
            cell.setCellType(MazeCell.CellType.CORRIDOR);
        }

        // Organic connection count based on noise
        int connectionCount = Math.max(1, Math.round(organicNoise * 4));
        generateRandomConnections(cell, connectionCount, random);
    }

    /**
     * Applies noise-based modifications to generated cells
     */
    private void applyNoiseModifications(MazeCell cell, Random random) {
        float modificationNoise = noiseManager.getModificationNoise(cell.getX() * 32, cell.getZ() * 32);

        // Chance for special room types
        if (modificationNoise > 0.8f && random.nextFloat() < 0.1f) {
            cell.setSpecialType(selectSpecialType(random));
        }

        // Chance for dead end creation
        if (modificationNoise < 0.2f && random.nextFloat() < 0.15f) {
            cell.clearConnections();
            generateRandomConnections(cell, 1, random); // Force single connection for dead end
            cell.setDeadEnd(true);
        }

        // Variation in room themes
        if (cell.isRoom()) {
            cell.setTheme(selectRoomTheme(modificationNoise, random));
        }
    }

    /**
     * Generates random connections for a cell
     */
    private void generateRandomConnections(MazeCell cell, int count, Random random) {
        List<MazeCell.Direction> availableDirections = new ArrayList<>(Arrays.asList(MazeCell.Direction.values()));
        Collections.shuffle(availableDirections, random);

        for (int i = 0; i < Math.min(count, availableDirections.size()); i++) {
            cell.addConnection(availableDirections.get(i));
        }
    }

    /**
     * Selects special room types for variety
     */
    private MazeCell.SpecialType selectSpecialType(Random random) {
        MazeCell.SpecialType[] types = MazeCell.SpecialType.values();
        return types[random.nextInt(types.length)];
    }

    /**
     * Selects room theme based on noise
     */
    private MazeCell.RoomTheme selectRoomTheme(float noise, Random random) {
        if (noise > 0.7f) return MazeCell.RoomTheme.INDUSTRIAL;
        if (noise > 0.4f) return MazeCell.RoomTheme.OFFICE;
        if (noise > 0.1f) return MazeCell.RoomTheme.STORAGE;
        return MazeCell.RoomTheme.ABANDONED;
    }

    /**
     * Generates maze connections between adjacent cells using advanced algorithms
     */
    public void generateConnections(Map<Long, MazeCell> grid, int startX, int startZ, int endX, int endZ, RandomSource random) {
        // Use minimum spanning tree for Prim's areas
        generateMSTConnections(grid, startX, startZ, endX, endZ, random);

        // Add additional connections for loops and variety
        addSecondaryConnections(grid, startX, startZ, endX, endZ, random);

        // Ensure no isolated areas
        ensureConnectivity(grid, startX, startZ, endX, endZ, random);
    }

    private void generateMSTConnections(Map<Long, MazeCell> grid, int startX, int startZ, int endX, int endZ, RandomSource random) {
        // Implement minimum spanning tree for guaranteed connectivity
        Set<Long> visited = new HashSet<>();
        PriorityQueue<Connection> edges = new PriorityQueue<>();

        // Start from random cell
        long startKey = getCellKey(startX, startZ);
        visited.add(startKey);
        addAdjacentEdges(grid, startX, startZ, edges, visited, random);

        while (!edges.isEmpty() && visited.size() < (endX - startX + 1) * (endZ - startZ + 1)) {
            Connection edge = edges.poll();
            if (!visited.contains(edge.to)) {
                visited.add(edge.to);
                createConnection(grid, edge);

                // Add new edges from this cell
                int[] coords = getCellCoords(edge.to);
                addAdjacentEdges(grid, coords[0], coords[1], edges, visited, random);
            }
        }
    }

    private void addSecondaryConnections(Map<Long, MazeCell> grid, int startX, int startZ, int endX, int endZ, RandomSource random) {
        // Add random connections to create loops and alternative paths
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                if (random.nextFloat() < 0.3f) { // 30% chance for additional connection
                    MazeCell cell = grid.get(getCellKey(x, z));
                    if (cell != null && cell.getConnections().size() < 3) {
                        addRandomConnection(grid, cell, x, z, random);
                    }
                }
            }
        }
    }

    private void ensureConnectivity(Map<Long, MazeCell> grid, int startX, int startZ, int endX, int endZ, RandomSource random) {
        // Use flood fill to find disconnected components and connect them
        Set<Long> visited = new HashSet<>();
        List<Set<Long>> components = new ArrayList<>();

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                long key = getCellKey(x, z);
                if (!visited.contains(key) && grid.containsKey(key)) {
                    Set<Long> component = new HashSet<>();
                    floodFill(grid, key, visited, component);
                    components.add(component);
                }
            }
        }

        // Connect all components to the largest one
        if (components.size() > 1) {
            Set<Long> mainComponent = components.stream().max(Comparator.comparing(Set::size)).orElse(new HashSet<>());
            for (Set<Long> component : components) {
                if (component != mainComponent) {
                    connectComponents(grid, mainComponent, component, random);
                }
            }
        }
    }

    // Utility methods
    private long getCellKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private int[] getCellCoords(long key) {
        return new int[]{(int) (key >> 32), (int) key};
    }

    private void addAdjacentEdges(Map<Long, MazeCell> grid, int x, int z, PriorityQueue<Connection> edges, Set<Long> visited, RandomSource random) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            long key = getCellKey(x + dir[0], z + dir[1]);
            if (grid.containsKey(key) && !visited.contains(key)) {
                edges.add(new Connection(getCellKey(x, z), key, random.nextFloat()));
            }
        }
    }

    private void createConnection(Map<Long, MazeCell> grid, Connection connection) {
        int[] fromCoords = getCellCoords(connection.from);
        int[] toCoords = getCellCoords(connection.to);

        MazeCell fromCell = grid.get(connection.from);
        MazeCell toCell = grid.get(connection.to);

        if (fromCell != null && toCell != null) {
            MazeCell.Direction fromDir = getDirection(fromCoords[0], fromCoords[1], toCoords[0], toCoords[1]);
            MazeCell.Direction toDir = getDirection(toCoords[0], toCoords[1], fromCoords[0], fromCoords[1]);

            fromCell.addConnection(fromDir);
            toCell.addConnection(toDir);
        }
    }

    private MazeCell.Direction getDirection(int x1, int z1, int x2, int z2) {
        if (x2 > x1) return MazeCell.Direction.EAST;
        if (x2 < x1) return MazeCell.Direction.WEST;
        if (z2 > z1) return MazeCell.Direction.SOUTH;
        return MazeCell.Direction.NORTH;
    }

    private void addRandomConnection(Map<Long, MazeCell> grid, MazeCell cell, int x, int z, RandomSource random) {
        List<MazeCell.Direction> available = new ArrayList<>();
        for (MazeCell.Direction dir : MazeCell.Direction.values()) {
            if (!cell.hasConnection(dir)) {
                available.add(dir);
            }
        }

        if (!available.isEmpty()) {
            MazeCell.Direction newDir = available.get(random.nextInt(available.size()));
            cell.addConnection(newDir);
        }
    }

    private void floodFill(Map<Long, MazeCell> grid, long start, Set<Long> globalVisited, Set<Long> component) {
        Stack<Long> stack = new Stack<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            long current = stack.pop();
            if (globalVisited.contains(current)) continue;

            globalVisited.add(current);
            component.add(current);

            MazeCell cell = grid.get(current);
            if (cell != null) {
                int[] coords = getCellCoords(current);
                for (MazeCell.Direction dir : cell.getConnections()) {
                    long neighbor = getNeighborKey(coords[0], coords[1], dir);
                    if (grid.containsKey(neighbor) && !globalVisited.contains(neighbor)) {
                        stack.push(neighbor);
                    }
                }
            }
        }
    }

    private void connectComponents(Map<Long, MazeCell> grid, Set<Long> main, Set<Long> other, RandomSource random) {
        // Find closest cells between components and connect them
        long mainCell = main.stream().findFirst().orElse(0L);
        long otherCell = other.stream().findFirst().orElse(0L);

        createConnection(grid, new Connection(mainCell, otherCell, 0));
    }

    private long getNeighborKey(int x, int z, MazeCell.Direction dir) {
        return switch (dir) {
            case NORTH -> getCellKey(x, z - 1);
            case SOUTH -> getCellKey(x, z + 1);
            case EAST -> getCellKey(x + 1, z);
            case WEST -> getCellKey(x - 1, z);
        };
    }

    // Supporting classes
    private enum MazeGenerationAlgorithm {
        DEPTH_FIRST, PRIMS, CELLULAR, ORGANIC
    }

    private static class Connection implements Comparable<Connection> {
        final long from, to;
        final float weight;

        Connection(long from, long to, float weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        @Override
        public int compareTo(Connection other) {
            return Float.compare(this.weight, other.weight);
        }
    }
}
