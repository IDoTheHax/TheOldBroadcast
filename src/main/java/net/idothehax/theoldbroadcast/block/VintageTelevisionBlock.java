package net.idothehax.theoldbroadcast.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastTeleporter;

public class VintageTelevisionBlock extends Block {

    public VintageTelevisionBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(3.0f)
                .lightLevel((state) -> 2)
                .noOcclusion());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            // Check if player is holding a VHS tape (we'll use a book as placeholder)
            if (player.getItemInHand(hand).is(Items.BOOK)) {
                if (isValidPortalStructure(level, pos)) {
                    activatePortal(level, pos, player);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private boolean isValidPortalStructure(Level level, BlockPos center) {
        // Check for 3x3 TV arrangement
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = center.offset(x, 0, z);
                if (!level.getBlockState(checkPos).getBlock().equals(this)) {
                    return false;
                }
            }
        }

        // Check for vintage cameras in corners (we'll use observers as placeholders)
        BlockPos[] cameraPositions = {
            center.offset(-2, 0, -2),
            center.offset(2, 0, -2),
            center.offset(-2, 0, 2),
            center.offset(2, 0, 2)
        };

        for (BlockPos cameraPos : cameraPositions) {
            if (!level.getBlockState(cameraPos).is(net.minecraft.world.level.block.Blocks.OBSERVER)) {
                return false;
            }
        }

        return true;
    }

    private void activatePortal(Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel serverLevel && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0f, 0.8f);

            // Teleport player to Old Broadcast dimension
            OldBroadcastTeleporter.teleportToOldBroadcast(serverLevel, serverPlayer);

            // Create static effect
            for (int i = 0; i < 20; i++) {
                double x = pos.getX() + level.random.nextGaussian() * 2;
                double y = pos.getY() + level.random.nextGaussian() * 2;
                double z = pos.getZ() + level.random.nextGaussian() * 2;
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        x, y, z, 1, 0, 0, 0, 0.1);
            }
        }
    }
}
