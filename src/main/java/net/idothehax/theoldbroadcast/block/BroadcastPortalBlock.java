package net.idothehax.theoldbroadcast.block;

import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastDimensions;
import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BroadcastPortalBlock extends Block {
    public BroadcastPortalBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.5f).noOcclusion().lightLevel(state -> 7));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player.getVehicle() == null && player.getPassengers().isEmpty()) {
            if (level instanceof ServerLevel serverLevel && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                OldBroadcastTeleporter.teleportToOldBroadcast(serverLevel, serverPlayer);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }
}
