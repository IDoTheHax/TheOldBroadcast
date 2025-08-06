//package net.idothehax.theoldbroadcast.world.feature;
//
//import com.mojang.serialization.Codec;
//import net.minecraft.core.BlockPos;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.level.levelgen.placement.PlacementContext;
//import net.minecraft.world.level.levelgen.placement.PlacementModifier;
//import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Stream;
//
//public class StudioMazeGridPlacement extends PlacementModifier {
//    public static final Codec<StudioMazeGridPlacement> CODEC = Codec.unit(StudioMazeGridPlacement::new);
//
//    private static final int GRID_SIZE = 80; // 5 chunks = 80 blocks
//
//    @Override
//    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
//        List<BlockPos> positions = new ArrayList<>();
//
//        // Generate maze grids every 80 blocks (5x5 chunks)
//        int worldX = pos.getX();
//        int worldZ = pos.getZ();
//
//        // Snap to 80-block grid
//        int gridX = (worldX / GRID_SIZE) * GRID_SIZE;
//        int gridZ = (worldZ / GRID_SIZE) * GRID_SIZE;
//
//        // Only place if we're at the grid position
//        if (worldX == gridX && worldZ == gridZ) {
//            positions.add(new BlockPos(gridX, 64, gridZ));
//        }
//
//        return positions.stream();
//    }
//
//    @Override
//    public PlacementModifierType<?> type() {
//        //return StudioPlacementTypes.STUDIO_MAZE_GRID.get();
//    }
//}
//