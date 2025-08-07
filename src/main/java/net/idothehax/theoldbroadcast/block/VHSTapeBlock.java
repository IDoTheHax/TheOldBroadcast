package net.idothehax.theoldbroadcast.block;

import net.idothehax.theoldbroadcast.Theoldbroadcast;
import net.idothehax.theoldbroadcast.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.SoundType;

import javax.annotation.Nullable;

public class VHSTapeBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D);

    public VHSTapeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(0.5f)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.Entity entity) {
        if (!level.isClientSide && entity instanceof Player) {
            level.playSound(null, pos, ModSounds.VHS_STEP.get(), SoundSource.BLOCKS, 0.5f, 1.2f);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            level.playSound(null, pos, ModSounds.VHS_STEP.get(), SoundSource.BLOCKS, 0.7f, 1.0f);
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.isClientSide) {
            Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4.0, false);
            if (player != null && random.nextInt(40) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.VHS_STEP.get(), SoundSource.BLOCKS, 0.25f, 0.7f + random.nextFloat() * 0.2f, false);
            }
        }
    }

    // Add this to allow VHS tape item to light a portal frame
    public static class Item extends net.minecraft.world.item.BlockItem {
        public Item(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            // Check for a valid frame (e.g., vintage television blocks in a 4x5 rectangle)
            if (isValidPortalFrame(level, pos)) {
                if (level.isClientSide) return InteractionResult.SUCCESS;
                level.setBlock(pos, Theoldbroadcast.BROADCAST_PORTAL_BLOCK.get().defaultBlockState(), 3);
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), (p) -> p.broadcastBreakEvent(context.getHand()));
                return InteractionResult.SUCCESS;
            }
            return super.useOn(context);
        }

        private boolean isValidPortalFrame(Level level, BlockPos pos) {
            // Simple check: require a 4x5 frame of vintage televisions around the target pos
            for (int y = -1; y <= 3; y++) {
                for (int x = -1; x <= 2; x++) {
                    boolean edge = (y == -1 || y == 3 || x == -1 || x == 2);
                    BlockPos check = pos.offset(x, y, 0);
                    if (edge) {
                        if (level.getBlockState(check).getBlock() != Theoldbroadcast.VINTAGE_TELEVISION.get()) return false;
                    } else {
                        if (!level.getBlockState(check).isAir()) return false;
                    }
                }
            }
            return true;
        }
    }
}
