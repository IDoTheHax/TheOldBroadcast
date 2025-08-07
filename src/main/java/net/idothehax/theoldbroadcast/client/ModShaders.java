package net.idothehax.theoldbroadcast.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.idothehax.theoldbroadcast.Theoldbroadcast;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModShaders {
    private static ShaderInstance staticOverlayShader;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
            new ShaderInstance(
                event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "static_overlay"),
                DefaultVertexFormat.POSITION_TEX
            ),
            shader -> staticOverlayShader = shader
        );
    }

    public static ShaderInstance getStaticOverlayShader() {
        return staticOverlayShader;
    }
}
