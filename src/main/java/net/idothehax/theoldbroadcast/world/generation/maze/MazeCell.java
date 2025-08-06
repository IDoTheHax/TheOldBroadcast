package net.idothehax.theoldbroadcast.world.generation.maze;

import java.util.*;

/**
 * Represents a single cell in the maze grid with advanced properties
 * Supports dynamic connections, special types, and thematic variations
 */
public class MazeCell {

    private final int x, z;
    private CellType cellType;
    private Set<Direction> connections;
    private int roomSize;
    private boolean occupied;
    private boolean deadEnd;
    private SpecialType specialType;
    private RoomTheme theme;
    private Map<String, Object> properties;

    public MazeCell(int x, int z) {
        this.x = x;
        this.z = z;
        this.cellType = CellType.CORRIDOR;
        this.connections = EnumSet.noneOf(Direction.class);
        this.roomSize = 16;
        this.occupied = false;
        this.deadEnd = false;
        this.specialType = SpecialType.NONE;
        this.theme = RoomTheme.STANDARD;
        this.properties = new HashMap<>();
    }

    // Core properties
    public int getX() { return x; }
    public int getZ() { return z; }

    public CellType getCellType() { return cellType; }
    public void setCellType(CellType cellType) { this.cellType = cellType; }

    public boolean isRoom() { return cellType == CellType.ROOM; }
    public boolean isCorridor() { return cellType == CellType.CORRIDOR; }

    // Connection management
    public Set<Direction> getConnections() { return EnumSet.copyOf(connections); }
    public void addConnection(Direction direction) { connections.add(direction); }
    public void removeConnection(Direction direction) { connections.remove(direction); }
    public void clearConnections() { connections.clear(); }
    public boolean hasConnection(Direction direction) { return connections.contains(direction); }
    public int getConnectionCount() { return connections.size(); }

    // Room properties
    public int getRoomSize() { return roomSize; }
    public void setRoomSize(int roomSize) { this.roomSize = Math.max(8, Math.min(32, roomSize)); }

    // Occupancy and state
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }

    public boolean isDeadEnd() { return deadEnd; }
    public void setDeadEnd(boolean deadEnd) { this.deadEnd = deadEnd; }

    // Special features
    public SpecialType getSpecialType() { return specialType; }
    public void setSpecialType(SpecialType specialType) { this.specialType = specialType; }
    public boolean hasSpecialType() { return specialType != SpecialType.NONE; }

    public RoomTheme getTheme() { return theme; }
    public void setTheme(RoomTheme theme) { this.theme = theme; }

    // Custom properties for extensibility
    public void setProperty(String key, Object value) { properties.put(key, value); }
    public Object getProperty(String key) { return properties.get(key); }
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    // Utility methods
    public boolean canConnect(Direction direction) {
        // Check if connection is possible (not blocked by special conditions)
        return !occupied || connections.size() < 4;
    }

    public Direction getOppositeDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }

    public List<Direction> getAvailableDirections() {
        List<Direction> available = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (!connections.contains(dir)) {
                available.add(dir);
            }
        }
        return available;
    }

    public boolean isIsolated() {
        return connections.isEmpty();
    }

    public boolean isJunction() {
        return connections.size() >= 3;
    }

    public boolean isLinear() {
        return connections.size() == 2;
    }

    // Advanced maze analysis
    public float getConnectivityDensity() {
        return connections.size() / 4.0f; // Max 4 connections
    }

    public boolean shouldHaveDoor(Direction direction) {
        // Logic for determining if a door should be placed
        if (!hasConnection(direction)) return false;
        if (cellType == CellType.ROOM && specialType != SpecialType.NONE) return true;
        if (deadEnd) return false;
        return theme == RoomTheme.OFFICE || theme == RoomTheme.INDUSTRIAL;
    }

    public boolean shouldHaveWindow(Direction direction) {
        // Logic for window placement
        return isRoom() && !hasConnection(direction) &&
               (theme == RoomTheme.OFFICE || specialType == SpecialType.OBSERVATION);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MazeCell mazeCell = (MazeCell) obj;
        return x == mazeCell.x && z == mazeCell.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return String.format("MazeCell{pos=(%d,%d), type=%s, connections=%s, size=%d, special=%s, theme=%s}",
                x, z, cellType, connections, roomSize, specialType, theme);
    }

    // Enums for cell properties
    public enum CellType {
        CORRIDOR,
        ROOM,
        JUNCTION,
        SPECIAL_AREA
    }

    public enum Direction {
        NORTH(0, -1),
        SOUTH(0, 1),
        EAST(1, 0),
        WEST(-1, 0);

        private final int deltaX, deltaZ;

        Direction(int deltaX, int deltaZ) {
            this.deltaX = deltaX;
            this.deltaZ = deltaZ;
        }

        public int getDeltaX() { return deltaX; }
        public int getDeltaZ() { return deltaZ; }

        public Direction getOpposite() {
            return switch (this) {
                case NORTH -> SOUTH;
                case SOUTH -> NORTH;
                case EAST -> WEST;
                case WEST -> EAST;
            };
        }

        public Direction rotateClockwise() {
            return switch (this) {
                case NORTH -> EAST;
                case EAST -> SOUTH;
                case SOUTH -> WEST;
                case WEST -> NORTH;
            };
        }

        public Direction rotateCounterClockwise() {
            return switch (this) {
                case NORTH -> WEST;
                case WEST -> SOUTH;
                case SOUTH -> EAST;
                case EAST -> NORTH;
            };
        }
    }

    public enum SpecialType {
        NONE,
        MEGA_ROOM,           // Large multi-cell room
        OBSERVATION,         // Room with windows/viewing areas
        GENERATOR_ROOM,      // Contains machinery/generators
        STORAGE_VAULT,       // Secure storage area
        BROADCAST_STATION,   // Radio/TV equipment
        ABANDONED_SECTION,   // Heavily damaged/decayed area
        FLOODED_AREA,        // Partially flooded
        DARK_ZONE,           // No lighting, enhanced atmosphere
        MAINTENANCE_SHAFT,   // Narrow technical corridors
        ELEVATOR_SHAFT,      // Vertical connection point
        STAIRS,              // Multi-level connection
        ARCHIVE_ROOM,        // Document/tape storage
        SECURITY_STATION,    // Monitoring equipment
        BREAK_ROOM,          // Staff amenities
        CONFERENCE_ROOM      // Meeting space
    }

    public enum RoomTheme {
        STANDARD(0.5f, 0.3f),        // Default backrooms appearance
        OFFICE(0.7f, 0.6f),          // Corporate office environment
        INDUSTRIAL(0.3f, 0.2f),      // Factory/mechanical theme
        STORAGE(0.4f, 0.1f),         // Warehouse/storage theme
        ABANDONED(0.1f, 0.05f),      // Heavily decayed/damaged
        CLINICAL(0.8f, 0.8f),        // Medical/laboratory theme
        RESIDENTIAL(0.6f, 0.5f),     // Home-like environment
        TECHNICAL(0.5f, 0.4f),       // Server rooms/technical areas
        GENERATOR_ROOM(0.4f, 0.3f);  // Generator/power room theme

        private final float lightLevel;      // 0.0 to 1.0
        private final float cleanlinessLevel; // 0.0 to 1.0

        RoomTheme(float lightLevel, float cleanlinessLevel) {
            this.lightLevel = lightLevel;
            this.cleanlinessLevel = cleanlinessLevel;
        }

        public float getLightLevel() { return lightLevel; }
        public float getCleanlinessLevel() { return cleanlinessLevel; }

        public boolean shouldHaveFurniture() {
            return this == OFFICE || this == RESIDENTIAL || this == ABANDONED;
        }

        public boolean shouldHaveMachinery() {
            return this == INDUSTRIAL || this == TECHNICAL || this == GENERATOR_ROOM;
        }

        public boolean shouldHaveDecay() {
            return this == ABANDONED || cleanlinessLevel < 0.3f;
        }
    }
}
