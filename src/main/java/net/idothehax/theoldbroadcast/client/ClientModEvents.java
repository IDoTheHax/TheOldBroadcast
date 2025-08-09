package net.idothehax.theoldbroadcast.client;

import net.idothehax.theoldbroadcast.Theoldbroadcast;
import net.idothehax.theoldbroadcast.client.renderer.entity.ModModelLayers;
import net.idothehax.theoldbroadcast.client.renderer.entity.ShadowGhostModel;
import net.idothehax.theoldbroadcast.client.renderer.entity.ShadowGhostRenderer;
import net.idothehax.theoldbroadcast.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SHADOW_GHOST.get(), ShadowGhostRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.SHADOW_GHOST_LAYER, ShadowGhostModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FlashlightClientHandler.getFlashlightKey());
    }
}
