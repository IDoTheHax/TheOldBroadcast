package net.idothehax.theoldbroadcast.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.util.RandomSource;
import java.util.stream.Stream;

/**
 * Placement modifier for infinite The Old Studio generation.
 * Used for procedural maze-like structure placement.
 */
public class TheOldStudioPlacement extends PlacementModifier {
    public static final Codec<TheOldStudioPlacement> CODEC = Codec.unit(new TheOldStudioPlacement());

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        // Custom placement logic for The Old Studio infinite maze
        // For demonstration, just return the origin position
        return Stream.of(pos);
    }

    @Override
    public PlacementModifierType<?> type() {
        // This should match the registration in StudioPlacementTypes
        return StudioPlacementTypes.THE_OLD_STUDIO_INFINITE.get();
    }
}
