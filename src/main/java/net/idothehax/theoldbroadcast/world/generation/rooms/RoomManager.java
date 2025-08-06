package net.idothehax.theoldbroadcast.world.generation.rooms;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.idothehax.theoldbroadcast.world.generation.maze.MazeCell;
import net.idothehax.theoldbroadcast.world.generation.noise.NoiseManager;

import java.util.*;

/**
 * Manages room templates and procedural room generation
 * Supports dynamic room creation with noise-based variations
 */
public class RoomManager {

    private final long seed;
    private final Random seedRandom;
    private final List<RoomTemplate> standardTemplates;
    private final List<RoomTemplate> megaRoomTemplates;
    private final Map<MazeCell.RoomTheme, List<RoomTemplate>> themeTemplates;

    // Block palettes for different themes
    private final Map<MazeCell.RoomTheme, BlockPalette> themePalettes;

    public RoomManager(long seed) {
        this.seed = seed;
        this.seedRandom = new Random(seed);
        this.standardTemplates = new ArrayList<>();
        this.megaRoomTemplates = new ArrayList<>();
        this.themeTemplates = new EnumMap<>(MazeCell.RoomTheme.class);
        this.themePalettes = new EnumMap<>(MazeCell.RoomTheme.class);

        initializeTemplates();
        initializePalettes();
    }

    /**
     * Selects appropriate room template based on cell properties
     */
    public RoomTemplate selectTemplate(MazeCell cell, RandomSource random) {
        List<RoomTemplate> candidates = new ArrayList<>();

        // Add theme-specific templates
        if (themeTemplates.containsKey(cell.getTheme())) {
            candidates.addAll(themeTemplates.get(cell.getTheme()));
        }

        // Add standard templates as fallback
        candidates.addAll(standardTemplates);

        // Filter by size constraints
        candidates.removeIf(template -> !template.fitsSize(cell.getRoomSize()));

        // Select weighted random template
        return selectWeightedTemplate(candidates, random);
    }

    /**
     * Gets mega room template for large special areas
     */
    public RoomTemplate getMegaRoomTemplate(RandomSource random) {
        if (megaRoomTemplates.isEmpty()) {
            return new StandardRoomTemplate(); // Fallback
        }
        return megaRoomTemplates.get(random.nextInt(megaRoomTemplates.size()));
    }

    /**
     * Generates base corridor structure
     */
    public void generateBaseCorridor(WorldGenLevel level, BlockPos pos, int width, RandomSource random) {
        BlockPalette palette = themePalettes.get(MazeCell.RoomTheme.STANDARD);

        // Generate corridor floor and ceiling
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos floorPos = pos.offset(x, 0, z);

                // Floor
                level.setBlock(floorPos, palette.floorBlock, 3);

                // Clear air space
                for (int y = 1; y < 12; y++) {
                    level.setBlock(floorPos.above(y), Blocks.AIR.defaultBlockState(), 3);
                }

                // Ceiling
                level.setBlock(floorPos.above(12), palette.ceilingBlock, 3);
            }
        }

        // Generate corridor walls
        generateCorridorWalls(level, pos, width, palette);
    }

    /**
     * Creates connection between rooms/corridors
     */
    public void createConnection(WorldGenLevel level, BlockPos pos, MazeCell.Direction direction, RandomSource random) {
        int doorWidth = 2 + random.nextInt(2); // 2-3 block wide doors
        int doorHeight = 3;

        BlockPos doorPos = pos.relative(direction.getDeltaX() > 0 ? net.minecraft.core.Direction.EAST :
                                       direction.getDeltaX() < 0 ? net.minecraft.core.Direction.WEST :
                                       direction.getDeltaZ() > 0 ? net.minecraft.core.Direction.SOUTH :
                                       net.minecraft.core.Direction.NORTH,
                                       direction.getDeltaX() != 0 ? 32 : direction.getDeltaZ() != 0 ? 32 : 0);

        // Clear doorway
        for (int w = 0; w < doorWidth; w++) {
            for (int h = 1; h <= doorHeight; h++) {
                BlockPos clearPos = doorPos.offset(w, h, 0);
                level.setBlock(clearPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Scatters props throughout generated areas
     */
    public void scatterProps(WorldGenLevel level, ChunkPos chunkPos, RandomSource random, NoiseManager noiseManager) {
        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                float propNoise = noiseManager.getPropNoise(worldX, worldZ);

                if (propNoise > 0.85f) {
                    placeProp(level, new BlockPos(worldX, 66, worldZ), random, noiseManager);
                }
            }
        }
    }

    /**
     * Gets appropriate ceiling block based on position
     */
    public BlockState getCeilingBlock(int x, int z) {
        // Use position-based variation for ceiling blocks
        long hash = ((long) x << 32) | (z & 0xFFFFFFFFL);
        Random posRandom = new Random(seed ^ hash);

        return switch (posRandom.nextInt(10)) {
            case 0, 1 -> Blocks.YELLOW_CONCRETE.defaultBlockState();
            case 2 -> Blocks.WHITE_CONCRETE.defaultBlockState();
            default -> Blocks.GRAY_CONCRETE.defaultBlockState();
        };
    }

    /**
     * Gets wall variation block for organic feel
     */
    public BlockState getWallVariationBlock(RandomSource random) {
        return switch (random.nextInt(5)) {
            case 0 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            case 1 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            case 2 -> Blocks.COBWEB.defaultBlockState();
            default -> Blocks.GRAY_CONCRETE.defaultBlockState();
        };
    }

    /**
     * Gets light block for atmospheric lighting
     */
    public BlockState getLightBlock(RandomSource random) {
        return switch (random.nextInt(4)) {
            case 0 -> Blocks.REDSTONE_TORCH.defaultBlockState();
            case 1 -> Blocks.LANTERN.defaultBlockState();
            case 2 -> Blocks.GLOWSTONE.defaultBlockState();
            default -> Blocks.TORCH.defaultBlockState();
        };
    }

    // Private helper methods
    private void initializeTemplates() {
        // Standard room templates
        standardTemplates.add(new StandardRoomTemplate());
        standardTemplates.add(new OfficeRoomTemplate());
        standardTemplates.add(new StorageRoomTemplate());
        standardTemplates.add(new BrokenRoomTemplate());

        // Mega room templates
        megaRoomTemplates.add(new MegaOfficeTemplate());
        megaRoomTemplates.add(new MegaStorageTemplate());
        megaRoomTemplates.add(new ConferenceRoomTemplate());

        // Theme-specific templates
        for (MazeCell.RoomTheme theme : MazeCell.RoomTheme.values()) {
            themeTemplates.put(theme, new ArrayList<>());
        }

        // Populate theme templates
        themeTemplates.get(MazeCell.RoomTheme.OFFICE).add(new OfficeRoomTemplate());
        themeTemplates.get(MazeCell.RoomTheme.INDUSTRIAL).add(new IndustrialRoomTemplate());
        themeTemplates.get(MazeCell.RoomTheme.STORAGE).add(new StorageRoomTemplate());
        themeTemplates.get(MazeCell.RoomTheme.ABANDONED).add(new BrokenRoomTemplate());
    }

    private void initializePalettes() {
        // Standard backrooms palette
        themePalettes.put(MazeCell.RoomTheme.STANDARD, new BlockPalette(
            Blocks.YELLOW_CONCRETE.defaultBlockState(),
            Blocks.GRAY_CONCRETE.defaultBlockState(),
            Blocks.YELLOW_CONCRETE.defaultBlockState(),
            Blocks.GLOWSTONE.defaultBlockState()
        ));

        // Office palette
        themePalettes.put(MazeCell.RoomTheme.OFFICE, new BlockPalette(
            Blocks.WHITE_CONCRETE.defaultBlockState(),
            Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),
            Blocks.WHITE_CONCRETE.defaultBlockState(),
            Blocks.SEA_LANTERN.defaultBlockState()
        ));

        // Industrial palette
        themePalettes.put(MazeCell.RoomTheme.INDUSTRIAL, new BlockPalette(
            Blocks.GRAY_CONCRETE.defaultBlockState(),
            Blocks.IRON_BLOCK.defaultBlockState(),
            Blocks.GRAY_CONCRETE.defaultBlockState(),
            Blocks.REDSTONE_LAMP.defaultBlockState()
        ));

        // Add more palettes...
        initializeRemainingPalettes();
    }

    private void initializeRemainingPalettes() {
        // Storage palette
        themePalettes.put(MazeCell.RoomTheme.STORAGE, new BlockPalette(
            Blocks.BROWN_CONCRETE.defaultBlockState(),
            Blocks.DARK_OAK_PLANKS.defaultBlockState(),
            Blocks.BROWN_CONCRETE.defaultBlockState(),
            Blocks.TORCH.defaultBlockState()
        ));

        // Abandoned palette
        themePalettes.put(MazeCell.RoomTheme.ABANDONED, new BlockPalette(
            Blocks.BLACK_CONCRETE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.BLACK_CONCRETE.defaultBlockState(),
            Blocks.SOUL_TORCH.defaultBlockState()
        ));

        // Clinical palette
        themePalettes.put(MazeCell.RoomTheme.CLINICAL, new BlockPalette(
            Blocks.WHITE_CONCRETE.defaultBlockState(),
            Blocks.QUARTZ_BLOCK.defaultBlockState(),
            Blocks.WHITE_CONCRETE.defaultBlockState(),
            Blocks.END_ROD.defaultBlockState()
        ));

        // Residential palette
        themePalettes.put(MazeCell.RoomTheme.RESIDENTIAL, new BlockPalette(
            Blocks.OAK_PLANKS.defaultBlockState(),
            Blocks.BRICK_WALL.defaultBlockState(),
            Blocks.OAK_PLANKS.defaultBlockState(),
            Blocks.LANTERN.defaultBlockState()
        ));

        // Technical palette
        themePalettes.put(MazeCell.RoomTheme.TECHNICAL, new BlockPalette(
            Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(),
            Blocks.IRON_BLOCK.defaultBlockState(),
            Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(),
            Blocks.REDSTONE_LAMP.defaultBlockState()
        ));
    }

    private void generateCorridorWalls(WorldGenLevel level, BlockPos pos, int width, BlockPalette palette) {
        // North and South walls
        for (int x = 0; x < 32; x++) {
            for (int y = 1; y < 12; y++) {
                level.setBlock(pos.offset(x, y, -1), palette.wallBlock, 3);
                level.setBlock(pos.offset(x, y, width), palette.wallBlock, 3);
            }
        }

        // East and West walls
        for (int z = 0; z < width; z++) {
            for (int y = 1; y < 12; y++) {
                level.setBlock(pos.offset(-1, y, z), palette.wallBlock, 3);
                level.setBlock(pos.offset(32, y, z), palette.wallBlock, 3);
            }
        }
    }

    private RoomTemplate selectWeightedTemplate(List<RoomTemplate> templates, RandomSource random) {
        if (templates.isEmpty()) {
            return new StandardRoomTemplate(); // Fallback
        }

        float totalWeight = templates.stream()
            .map(RoomTemplate::getWeight)
            .reduce(0.0f, Float::sum);

        float randomValue = random.nextFloat() * totalWeight;
        float currentWeight = 0.0f;

        for (RoomTemplate template : templates) {
            currentWeight += template.getWeight();
            if (randomValue <= currentWeight) {
                return template;
            }
        }

        return templates.get(templates.size() - 1); // Fallback to last template
    }

    private void placeProp(WorldGenLevel level, BlockPos pos, RandomSource random, NoiseManager noiseManager) {
        // Check if position is valid for prop placement
        if (!level.getBlockState(pos.below()).isSolid() || !level.getBlockState(pos).isAir()) {
            return;
        }

        float propTypeNoise = noiseManager.getFeatureNoise(pos.getX(), pos.getZ(), NoiseManager.FeatureType.FURNITURE_PLACEMENT);

        int type = (int) (propTypeNoise * 10) % 8;
        switch (type) {
            case 0 -> level.setBlock(pos, Blocks.BARREL.defaultBlockState(), 3);
            case 1 -> level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
            case 2 -> level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
            case 3 -> level.setBlock(pos, Blocks.FLOWER_POT.defaultBlockState(), 3);
            case 4 -> spawnItemFrame(level, pos, random);
            case 5 -> spawnPainting(level, pos, random);
            case 6 -> level.setBlock(pos, Blocks.SKELETON_SKULL.defaultBlockState(), 3);
            default -> level.setBlock(pos, Blocks.BROWN_MUSHROOM.defaultBlockState(), 3);
        }
    }

    // Helper to spawn an ItemFrame entity
    private void spawnItemFrame(WorldGenLevel level, BlockPos pos, RandomSource random) {
        if (level.getLevel().isClientSide) return;
        net.minecraft.core.Direction direction = net.minecraft.core.Direction.from2DDataValue(random.nextInt(4));
        net.minecraft.world.entity.decoration.ItemFrame itemFrame = new net.minecraft.world.entity.decoration.ItemFrame(
            level.getLevel(),
            pos,
            direction
        );
        level.getLevel().addFreshEntity(itemFrame);
    }

    // Helper to spawn a Painting entity
    private void spawnPainting(WorldGenLevel level, BlockPos pos, RandomSource random) {
        if (level.getLevel().isClientSide) return;
        net.minecraft.core.Direction direction = net.minecraft.core.Direction.from2DDataValue(random.nextInt(4));
        net.minecraft.world.entity.decoration.Painting painting = new net.minecraft.world.entity.decoration.Painting(
            net.minecraft.world.entity.EntityType.PAINTING,
            level.getLevel()
        );
        // Set position and facing
        painting.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
            direction.toYRot(), 0.0F);
        // Set a random variant, fallback to KEBAB if not found
        var variants = net.minecraft.core.registries.BuiltInRegistries.PAINTING_VARIANT;
        var variantHolder = variants.getRandom(random).orElse(variants.getHolderOrThrow(net.minecraft.world.entity.decoration.PaintingVariants.KEBAB));
        painting.setVariant(variantHolder);
        level.getLevel().addFreshEntity(painting);
    }

    // Supporting classes
    public static class BlockPalette {
        public final BlockState floorBlock;
        public final BlockState wallBlock;
        public final BlockState ceilingBlock;
        public final BlockState lightBlock;

        public BlockPalette(BlockState floor, BlockState wall, BlockState ceiling, BlockState light) {
            this.floorBlock = floor;
            this.wallBlock = wall;
            this.ceilingBlock = ceiling;
            this.lightBlock = light;
        }
    }
}
