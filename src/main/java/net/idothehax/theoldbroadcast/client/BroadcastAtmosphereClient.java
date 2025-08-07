package net.idothehax.theoldbroadcast.client;

import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastDimensions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class BroadcastAtmosphereClient {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null && mc.level.dimension().equals(OldBroadcastDimensions.OLD_BROADCAST_LEVEL)) {
            mc.options.gamma().set(0.2);
        }
    }
}
