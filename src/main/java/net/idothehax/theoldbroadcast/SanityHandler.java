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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles player sanity in the Old Broadcast dimension.
 */
@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class SanityHandler {
    public static final String SANITY_TAG = "OldBroadcastSanity";
    public static final int MAX_SANITY = 100;
    private static final int MIN_SANITY = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        Level level = player.level();
        if (level.isClientSide) return;
        ResourceLocation dim = level.dimension().location();
        if (!dim.equals(ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "old_broadcast"))) return;

        CompoundTag data = player.getPersistentData();
        int sanity = data.contains(SANITY_TAG) ? data.getInt(SANITY_TAG) : MAX_SANITY;

        // Decrease sanity over time
        sanity -= 1;

        // Restore sanity in high-light safe zones
        BlockPos pos = player.blockPosition();
        int light = level.getMaxLocalRawBrightness(pos);
        if (light > 8) {
            sanity += 2;
        }

        sanity = Mth.clamp(sanity, MIN_SANITY, MAX_SANITY);
        data.putInt(SANITY_TAG, sanity);

        // Apply negative effects based on sanity thresholds
        if (sanity < 50 && !player.hasEffect(MobEffects.CONFUSION)) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false));
        }
        if (sanity < 25 && !player.hasEffect(MobEffects.POISON)) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0, false, false));
        }

        // Kill the player if sanity hits zero
        if (sanity <= MIN_SANITY && player instanceof ServerPlayer) {
            DamageSource sanityDamage = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC));
            player.hurt(sanityDamage, 2.0F);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo().location().equals(ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "old_broadcast"))) {
            event.getEntity().getPersistentData().putInt(SANITY_TAG, MAX_SANITY);
        }
    }
}
