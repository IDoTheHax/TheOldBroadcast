package net.idothehax.theoldbroadcast.client;

import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastDimensions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.idothehax.theoldbroadcast.Theoldbroadcast;

@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, value = Dist.CLIENT)
public class BroadcastAtmosphereClient {
    private static double originalGamma = -1.0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.ClientTickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            if (mc.level.dimension().equals(OldBroadcastDimensions.OLD_BROADCAST_LEVEL)) {
                // Store original gamma when first entering dimension
                if (originalGamma < 0) {
                    originalGamma = mc.options.gamma().get();
                }
                // Set dark atmosphere
                mc.options.gamma().set(0.2);
            } else if (originalGamma >= 0) {
                // Restore original gamma when leaving dimension
                mc.options.gamma().set(originalGamma);
                originalGamma = -1.0;
            }
        }
    }
}
