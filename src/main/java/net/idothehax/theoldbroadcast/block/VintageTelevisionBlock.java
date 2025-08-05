package net.idothehax.theoldbroadcast.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastTeleporter;
import net.idothehax.theoldbroadcast.Theoldbroadcast;

import javax.annotation.Nonnull;

public class VintageTelevisionBlock extends Block {

    public VintageTelevisionBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(3.0f)
                .lightLevel((state) -> 2)
                .noOcclusion());
    }

    @Override
    @Nonnull
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            // Check if player is holding a VHS tape (now a block item)
            if (player.getItemInHand(hand).is(Theoldbroadcast.VHS_TAPE.get())) {
                if (isValidPortalStructure(level, pos)) {
                    activatePortal(level, pos, player);
                    return InteractionResult.SUCCESS;
                } else {
                    // Debug message when portal structure is invalid
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("Portal structure is invalid! Need 3x3 TVs with observers in corners."));
                    }
                }
            } else {
                // Debug message when not holding VHS tape
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("You need a VHS tape to activate the portal!"));
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

        // Check for observers in corners (representing vintage cameras)
        BlockPos[] cameraPositions = {
            center.offset(-2, 0, -2),
            center.offset(2, 0, -2),
            center.offset(-2, 0, 2),
            center.offset(2, 0, 2)
        };

        for (BlockPos cameraPos : cameraPositions) {
            if (!level.getBlockState(cameraPos).is(Blocks.OBSERVER)) {
                return false;
            }
        }

        return true;
    }

    private void activatePortal(Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
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

            // Consume the VHS tape
            player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
        }
    }
}
