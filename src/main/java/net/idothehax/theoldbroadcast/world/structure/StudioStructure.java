package net.idothehax.theoldbroadcast.world.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.idothehax.theoldbroadcast.Theoldbroadcast;

import java.util.Optional;

public class StudioStructure extends Structure {

    public static final Codec<StudioStructure> CODEC = simpleCodec(StudioStructure::new);

    public StudioStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return StudioStructures.STUDIO_TYPE.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();

        // Determine room type based on chunk coordinates
        RoomType roomType = determineRoomType(chunkPos.x, chunkPos.z);

        BlockPos pos = new BlockPos(chunkPos.getMinBlockX(), 65, chunkPos.getMinBlockZ());
        return Optional.of(new GenerationStub(pos, (builder) -> {
            builder.addPiece(new StudioRoomPiece(pos, roomType));
        }));
    }


    private RoomType determineRoomType(int chunkX, int chunkZ) {
        // Create varied room distribution
        int hash = (chunkX * 31 + chunkZ) * 17;
        int type = Math.abs(hash) % 7;

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

    public enum RoomType {
        CORRIDOR, CINEMA, BROADCAST_ROOM, EDITING_LAB, SOUNDSTAGE, STORAGE, ANTENNA_ROOM
    }

    public static class StudioRoomPiece extends StructurePiece {
        private final RoomType roomType;

        public StudioRoomPiece(BlockPos pos, RoomType roomType) {
            super(StudioStructures.STUDIO_ROOM_PIECE.get(), 0, BoundingBox.fromCorners(pos, pos.offset(31, 15, 31)));
            this.roomType = roomType;
        }

        public StudioRoomPiece(CompoundTag tag) {
            super(StudioStructures.STUDIO_ROOM_PIECE.get(), tag);
            this.roomType = RoomType.valueOf(tag.getString("RoomType"));
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putString("RoomType", roomType.name());
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
            generateRoom(level, random, box);
        }

        private void generateRoom(WorldGenLevel level, RandomSource random, BoundingBox box) {
            switch (roomType) {
                case CINEMA -> generateCinema(level, random, box);
                case BROADCAST_ROOM -> generateBroadcastRoom(level, random, box);
                case EDITING_LAB -> generateEditingLab(level, random, box);
                case SOUNDSTAGE -> generateSoundstage(level, random, box);
                case STORAGE -> generateStorage(level, random, box);
                case ANTENNA_ROOM -> generateAntennaRoom(level, random, box);
                default -> generateCorridor(level, random, box);
            }
        }

        private void generateCinema(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Generate rows of seats
            for (int x = box.minX() + 2; x < box.maxX() - 2; x += 4) {
                for (int z = box.minZ() + 5; z < box.maxZ() - 10; z += 2) {
                    placeBlock(level, Blocks.BLACK_WOOL.defaultBlockState(), x, 65, z, box);
                }
            }
            // Generate screen at front
            for (int x = box.minX() + 5; x < box.maxX() - 5; x++) {
                for (int y = 66; y < 75; y++) {
                    placeBlock(level, Blocks.WHITE_WOOL.defaultBlockState(), x, y, box.maxZ() - 3, box);
                }
            }
            // Add projector
            placeBlock(level, Blocks.OBSERVER.defaultBlockState(), box.minX() + 15, 70, box.minZ() + 5, box);
            // Add flickering light (simulate with redstone lamp and random lever)
            placeBlock(level, Blocks.REDSTONE_LAMP.defaultBlockState(), box.minX() + 10, 72, box.maxZ() - 6, box);
            if (random.nextBoolean()) {
                placeBlock(level, Blocks.LEVER.defaultBlockState(), box.minX() + 10, 73, box.maxZ() - 6, box);
            }
            // Place a VHS tape as a clue
            if (random.nextInt(3) == 0) {
                placeBlock(level, net.idothehax.theoldbroadcast.Theoldbroadcast.VHS_TAPE_BLOCK.get().defaultBlockState(), box.minX() + 8, 65, box.maxZ() - 8, box);
            }
        }

        private void generateBroadcastRoom(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Generate control desks
            for (int x = box.minX() + 3; x < box.maxX() - 3; x += 3) {
                for (int z = box.minZ() + 3; z < box.maxZ() - 3; z += 3) {
                    placeBlock(level, Blocks.IRON_BLOCK.defaultBlockState(), x, 65, z, box);
                    placeBlock(level, Blocks.OBSERVER.defaultBlockState(), x, 66, z, box);
                }
            }
            // TV screens on walls
            for (int i = 0; i < 5; i++) {
                int x = box.minX() + 2 + i * 5;
                placeBlock(level, Blocks.BLACK_STAINED_GLASS.defaultBlockState(), x, 68, box.minZ() + 1, box);
            }
            // Add static overlay prop (simulate with glass panes)
            for (int i = 0; i < 3; i++) {
                int x = box.minX() + 6 + i * 7;
                placeBlock(level, Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(), x, 69, box.minZ() + 2, box);
            }
            // Place a VHS tape as a clue
            if (random.nextInt(4) == 0) {
                placeBlock(level, net.idothehax.theoldbroadcast.Theoldbroadcast.VHS_TAPE_BLOCK.get().defaultBlockState(), box.maxX() - 6, 65, box.minZ() + 4, box);
            }
        }

        private void generateEditingLab(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Workstations
            for (int x = box.minX() + 4; x < box.maxX() - 4; x += 6) {
                for (int z = box.minZ() + 4; z < box.maxZ() - 4; z += 6) {
                    // Desk
                    placeBlock(level, Blocks.SPRUCE_PLANKS.defaultBlockState(), x, 65, z, box);
                    placeBlock(level, Blocks.SPRUCE_PLANKS.defaultBlockState(), x + 1, 65, z, box);
                    // Equipment
                    placeBlock(level, Blocks.DISPENSER.defaultBlockState(), x, 66, z, box);
                    placeBlock(level, Blocks.REPEATER.defaultBlockState(), x + 1, 66, z, box);
                }
            }
            // Film reels scattered around (using barrels as placeholders)
            for (int i = 0; i < 8; i++) {
                int x = box.minX() + 2 + random.nextInt(box.getXSpan() - 4);
                int z = box.minZ() + 2 + random.nextInt(box.getZSpan() - 4);
                placeBlock(level, Blocks.BARREL.defaultBlockState(), x, 66, z, box);
            }
            // Place a VHS tape as a clue
            if (random.nextInt(5) == 0) {
                placeBlock(level, net.idothehax.theoldbroadcast.Theoldbroadcast.VHS_TAPE_BLOCK.get().defaultBlockState(), box.maxX() - 7, 65, box.maxZ() - 7, box);
            }
        }

        private void generateSoundstage(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Large open space with set pieces
            int setType = random.nextInt(3);

            switch (setType) {
                case 0 -> generateHauntedHouseSet(level, random, box);
                case 1 -> generateSciFiSet(level, random, box);
                case 2 -> generateChildrenShowSet(level, random, box);
            }
            // Place a VHS tape as a clue
            if (random.nextInt(6) == 0) {
                placeBlock(level, net.idothehax.theoldbroadcast.Theoldbroadcast.VHS_TAPE_BLOCK.get().defaultBlockState(), box.minX() + 6, 65, box.minZ() + 6, box);
            }
        }

        private void generateHauntedHouseSet(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Fake house structure
            for (int x = box.minX() + 8; x < box.maxX() - 8; x++) {
                for (int z = box.minZ() + 8; z < box.maxZ() - 8; z++) {
                    if (x == box.minX() + 8 || x == box.maxX() - 9 || z == box.minZ() + 8 || z == box.maxZ() - 9) {
                        placeBlock(level, Blocks.DARK_OAK_PLANKS.defaultBlockState(), x, 65, z, box);
                        if (random.nextFloat() < 0.7f) {
                            placeBlock(level, Blocks.DARK_OAK_PLANKS.defaultBlockState(), x, 66, z, box);
                        }
                    }
                }
            }
        }

        private void generateSciFiSet(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Futuristic panels
            for (int x = box.minX() + 5; x < box.maxX() - 5; x += 3) {
                for (int z = box.minZ() + 5; z < box.maxZ() - 5; z += 3) {
                    placeBlock(level, Blocks.IRON_BLOCK.defaultBlockState(), x, 65, z, box);
                    placeBlock(level, Blocks.REDSTONE_LAMP.defaultBlockState(), x, 66, z, box);
                }
            }
        }

        private void generateChildrenShowSet(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Colorful but decaying set
            BlockState[] colors = {
                Blocks.RED_WOOL.defaultBlockState(),
                Blocks.BLUE_WOOL.defaultBlockState(),
                Blocks.YELLOW_WOOL.defaultBlockState(),
                Blocks.GREEN_WOOL.defaultBlockState()
            };

            for (int i = 0; i < 10; i++) {
                int x = box.minX() + 3 + random.nextInt(box.getXSpan() - 6);
                int z = box.minZ() + 3 + random.nextInt(box.getZSpan() - 6);
                BlockState color = colors[random.nextInt(colors.length)];
                placeBlock(level, color, x, 65, z, box);
                if (random.nextFloat() < 0.5f) {
                    placeBlock(level, color, x, 66, z, box);
                }
            }
        }

        private void generateStorage(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Shelves and storage
            for (int x = box.minX() + 2; x < box.maxX() - 2; x += 4) {
                for (int y = 65; y < 75; y += 2) {
                    placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), x, y, box.minZ() + 2, box);
                    placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), x, y, box.maxZ() - 3, box);
                }
            }
            // Props scattered around (using blocks since armor stands are entities)
            for (int i = 0; i < 15; i++) {
                int x = box.minX() + 2 + random.nextInt(box.getXSpan() - 4);
                int z = box.minZ() + 2 + random.nextInt(box.getZSpan() - 4);
                BlockState prop = random.nextFloat() < 0.5f ?
                    Blocks.SKELETON_SKULL.defaultBlockState() :
                    Blocks.CHEST.defaultBlockState();
                placeBlock(level, prop, x, 66, z, box);
            }
            // Place a VHS tape as a clue
            if (random.nextInt(4) == 0) {
                placeBlock(level, net.idothehax.theoldbroadcast.Theoldbroadcast.VHS_TAPE_BLOCK.get().defaultBlockState(), box.maxX() - 5, 65, box.minZ() + 5, box);
            }
        }

        private void generateAntennaRoom(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Antenna structures
            int centerX = (box.minX() + box.maxX()) / 2;
            int centerZ = (box.minZ() + box.maxZ()) / 2;

            // Main antenna tower
            for (int y = 65; y < 90; y++) {
                placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), centerX, y, centerZ, box);
                if (y % 5 == 0) {
                    placeBlock(level, Blocks.REDSTONE_BLOCK.defaultBlockState(), centerX - 1, y, centerZ, box);
                    placeBlock(level, Blocks.REDSTONE_BLOCK.defaultBlockState(), centerX + 1, y, centerZ, box);
                    placeBlock(level, Blocks.REDSTONE_BLOCK.defaultBlockState(), centerX, y, centerZ - 1, box);
                    placeBlock(level, Blocks.REDSTONE_BLOCK.defaultBlockState(), centerX, y, centerZ + 1, box);
                }
            }
        }

        private void generateCorridor(WorldGenLevel level, RandomSource random, BoundingBox box) {
            // Sparse furniture and cables
            for (int i = 0; i < 5; i++) {
                int x = box.minX() + 2 + random.nextInt(box.getXSpan() - 4);
                int z = box.minZ() + 2 + random.nextInt(box.getZSpan() - 4);
                placeBlock(level, Blocks.TRIPWIRE.defaultBlockState(), x, 65, z, box);
            }
            // Place a VHS tape as a clue
            if (random.nextInt(8) == 0) {
                placeBlock(level, net.idothehax.theoldbroadcast.Theoldbroadcast.VHS_TAPE_BLOCK.get().defaultBlockState(), box.minX() + 3, 65, box.maxZ() - 3, box);
            }
        }
    }
}
