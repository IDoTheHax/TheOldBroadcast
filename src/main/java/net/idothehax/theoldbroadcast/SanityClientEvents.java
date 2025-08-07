package net.idothehax.theoldbroadcast;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.idothehax.theoldbroadcast.client.ModShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Client-side events for sanity visuals and audio using custom shaders.
 */
@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SanityClientEvents {
    private static int gameTick = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase == ClientTickEvent.Phase.END) {
            gameTick++;
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        // Only render on specific overlays, not over menus/GUIs
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // Don't render if in menu/GUI or not in our dimension
        if (player == null || mc.screen != null ||
            !mc.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "old_broadcast"))) {
            return;
        }

        int sanity = player.getPersistentData().getInt(SanityHandler.SANITY_TAG);
        if (sanity >= SanityHandler.MAX_SANITY) return;

        float opacity = 1.0F - (sanity / (float)SanityHandler.MAX_SANITY);
        renderStaticOverlay(opacity, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    private static void renderStaticOverlay(float opacity, int screenWidth, int screenHeight) {
        ShaderInstance shader = ModShaders.getStaticOverlayShader();
        if (shader == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        // Apply our custom shader
        RenderSystem.setShader(() -> shader);

        // Set shader uniforms
        if (shader.getUniform("Time") != null) {
            shader.getUniform("Time").set((float) gameTick / 20.0f);
        }
        if (shader.getUniform("Opacity") != null) {
            shader.getUniform("Opacity").set(opacity);
        }
        if (shader.getUniform("ScreenSize") != null) {
            shader.getUniform("ScreenSize").set((float) screenWidth, (float) screenHeight);
        }

        // Setup matrices
        Matrix4f modelView = new Matrix4f();
        Matrix4f projection = new Matrix4f().ortho(0.0F, screenWidth, screenHeight, 0.0F, 1000.0F, 3000.0F);

        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

        if (shader.getUniform("ModelViewMat") != null) {
            shader.getUniform("ModelViewMat").set(modelView);
        }
        if (shader.getUniform("ProjMat") != null) {
            shader.getUniform("ProjMat").set(projection);
        }

        // Render fullscreen quad
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferBuilder.vertex(0, screenHeight, 0).uv(0.0F, 1.0F).endVertex();
        bufferBuilder.vertex(screenWidth, screenHeight, 0).uv(1.0F, 1.0F).endVertex();
        bufferBuilder.vertex(screenWidth, 0, 0).uv(1.0F, 0.0F).endVertex();
        bufferBuilder.vertex(0, 0, 0).uv(0.0F, 0.0F).endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());

        // Cleanup
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
