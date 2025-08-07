package net.idothehax.theoldbroadcast.entity;

import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class GhostSpawnHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide() || event.level.dimension() != OldBroadcastDimensions.OLD_BROADCAST_LEVEL) return;
        Level level = event.level;
        for (Player player : level.players()) {
            if (RANDOM.nextInt(600) == 0) { // ~Every 30 seconds per player
                int x = (int) (player.getX() + RANDOM.nextInt(32) - 16);
                int y = (int) player.getY();
                int z = (int) (player.getZ() + RANDOM.nextInt(32) - 16);
                if (level instanceof ServerLevel serverLevel) {
                    var entityType = ModEntities.SHADOW_GHOST.get();
                    var ghost = entityType.create(serverLevel);
                    if (ghost != null) {
                        ghost.moveTo(x + 0.5, y, z + 0.5, serverLevel.random.nextFloat() * 360F, 0);
                        serverLevel.addFreshEntity(ghost);
                    }
                }
            }
        }
    }
}
