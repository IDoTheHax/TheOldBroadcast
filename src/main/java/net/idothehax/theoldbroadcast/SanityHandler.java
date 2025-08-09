package net.idothehax.theoldbroadcast;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.idothehax.theoldbroadcast.network.ModNetwork;
import net.idothehax.theoldbroadcast.network.SanitySyncPacket;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Handles player sanity in the Old Broadcast dimension.
 */
@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SanityHandler {
    public static final String SANITY_TAG = "OldBroadcastSanity";
    public static final int MAX_SANITY = 100;
    private static final int MIN_SANITY = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        Level level = player.level();
        ResourceLocation dim = level.dimension().location();

        // Only process in Old Broadcast dimension
        if (!dim.equals(ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "old_broadcast"))) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        int sanity = data.contains(SANITY_TAG) ? data.getInt(SANITY_TAG) : MAX_SANITY;
        int oldSanity = sanity;

        // Only decrease sanity every 20 ticks (1 second) using player's tick count
        if (player.tickCount % 20 == 0) {
            // Decrease sanity over time in the dimension
            sanity = Math.max(MIN_SANITY, sanity - 1);

            // Restore sanity in high-light safe zones (light level 12+)
            BlockPos pos = player.blockPosition();
            int light = level.getMaxLocalRawBrightness(pos);
            if (light >= 12) {
                sanity = Math.min(MAX_SANITY, sanity + 2); // Restore faster than decay
            }

            // Clamp sanity values
            sanity = Mth.clamp(sanity, MIN_SANITY, MAX_SANITY);
            data.putInt(SANITY_TAG, sanity);

            // Apply effects based on sanity level
            applySanityEffects(player, sanity);
        }

        // Sync to client if sanity changed or every 5 seconds
        if (oldSanity != sanity || (player.tickCount % 100 == 0)) {
            syncSanityToClient((ServerPlayer) player, sanity);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CompoundTag data = serverPlayer.getPersistentData();
            int sanity = data.contains(SANITY_TAG) ? data.getInt(SANITY_TAG) : MAX_SANITY;
            syncSanityToClient(serverPlayer, sanity);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CompoundTag data = serverPlayer.getPersistentData();
            int sanity = data.contains(SANITY_TAG) ? data.getInt(SANITY_TAG) : MAX_SANITY;
            syncSanityToClient(serverPlayer, sanity);
        }
    }

    private static void applySanityEffects(Player player, int sanity) {
        // Low sanity effects
        if (sanity <= 20) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false));
        }

        if (sanity <= 10) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
        }

        // Critical sanity - damage player
        if (sanity <= 5) {
            DamageSource damageSource = new DamageSource(
                player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC)
            );
            player.hurt(damageSource, 1.0f);
        }
    }

    private static void syncSanityToClient(ServerPlayer player, int sanity) {
        ModNetwork.INSTANCE.send(
            PacketDistributor.PLAYER.with(() -> player),
            new SanitySyncPacket(sanity)
        );
    }

    public static int getSanity(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.contains(SANITY_TAG) ? data.getInt(SANITY_TAG) : MAX_SANITY;
    }

    public static void setSanity(Player player, int sanity) {
        sanity = Mth.clamp(sanity, MIN_SANITY, MAX_SANITY);
        player.getPersistentData().putInt(SANITY_TAG, sanity);

        if (player instanceof ServerPlayer serverPlayer) {
            syncSanityToClient(serverPlayer, sanity);
        }
    }
}
