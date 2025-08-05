package net.idothehax.theoldbroadcast.world.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class OldBroadcastTeleporter implements ITeleporter {

    public static void teleportToOldBroadcast(ServerLevel level, ServerPlayer player) {
        ServerLevel targetLevel = level.getServer().getLevel(OldBroadcastDimensions.OLD_BROADCAST_LEVEL);
        if (targetLevel != null) {
            player.changeDimension(targetLevel, new OldBroadcastTeleporter());
        }
    }

    public static void teleportFromOldBroadcast(ServerLevel level, ServerPlayer player) {
        ServerLevel overworld = level.getServer().overworld();
        player.changeDimension(overworld, new OldBroadcastTeleporter());
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        if (destWorld.dimension().equals(OldBroadcastDimensions.OLD_BROADCAST_LEVEL)) {
            // Teleport to studio entrance
            return new PortalInfo(new Vec3(8, 66, 8), Vec3.ZERO, entity.getYRot(), entity.getXRot());
        } else {
            // Return to overworld spawn
            BlockPos spawnPos = destWorld.getSharedSpawnPos();
            return new PortalInfo(new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5),
                    Vec3.ZERO, entity.getYRot(), entity.getXRot());
        }
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        Entity repositionedEntity = repositionEntity.apply(false);

        // Add entrance effects
        if (destWorld.dimension().equals(OldBroadcastDimensions.OLD_BROADCAST_LEVEL)) {
            // Player entered the Old Broadcast dimension
            destWorld.playSound(null, repositionedEntity.blockPosition(),
                    net.minecraft.sounds.SoundEvents.PORTAL_TRAVEL,
                    net.minecraft.sounds.SoundSource.AMBIENT, 1.0f, 0.5f);
        }

        return repositionedEntity;
    }
}
