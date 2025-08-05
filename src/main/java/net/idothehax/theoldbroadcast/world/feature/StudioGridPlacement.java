package net.idothehax.theoldbroadcast.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StudioGridPlacement extends PlacementModifier {
    public static final Codec<StudioGridPlacement> CODEC = Codec.unit(StudioGridPlacement::new);

    private static final int ROOM_SIZE = 32;

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        List<BlockPos> positions = new ArrayList<>();

        // Generate studio rooms every 32 blocks
        int worldX = pos.getX();
        int worldZ = pos.getZ();

        // Snap to 32-block grid
        int gridX = (worldX / 32) * 32;
        int gridZ = (worldZ / 32) * 32;

        // Only place if we're at the grid position
        if (worldX == gridX && worldZ == gridZ) {
            positions.add(new BlockPos(gridX, 65, gridZ));
        }

        return positions.stream();
    }

    @Override
    public PlacementModifierType<?> type() {
        return StudioPlacementTypes.STUDIO_GRID.get();
    }
}
