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

        // Calculate 4:3 area
        int targetWidth = screenWidth;
        int targetHeight = (int) (screenWidth * 3.0 / 4.0);
        if (targetHeight > screenHeight) {
            targetHeight = screenHeight;
            targetWidth = (int) (screenHeight * 4.0 / 3.0);
        }
        int x = (screenWidth - targetWidth) / 2;
        int y = (screenHeight - targetHeight) / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);

        if (shader.getUniform("Time") != null) {
            shader.getUniform("Time").set((float) gameTick / 20.0f);
        }
        if (shader.getUniform("Opacity") != null) {
            shader.getUniform("Opacity").set(opacity);
        }
        if (shader.getUniform("ScreenSize") != null) {
            shader.getUniform("ScreenSize").set((float) targetWidth, (float) targetHeight);
        }

        Matrix4f modelView = new Matrix4f();
        Matrix4f projection = new Matrix4f().ortho(0.0F, screenWidth, screenHeight, 0.0F, 1000.0F, 3000.0F);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);
        if (shader.getUniform("ModelViewMat") != null) {
            shader.getUniform("ModelViewMat").set(modelView);
        }
        if (shader.getUniform("ProjMat") != null) {
            shader.getUniform("ProjMat").set(projection);
        }

        // Draw black bars (letterbox/pillarbox)
        if (x > 0) {
            fillRect(0, 0, x, screenHeight, 0xFF000000);
            fillRect(screenWidth - x, 0, x, screenHeight, 0xFF000000);
        }
        if (y > 0) {
            fillRect(0, 0, screenWidth, y, 0xFF000000);
            fillRect(0, screenHeight - y, screenWidth, y, 0xFF000000);
        }

        // Render 4:3 overlay
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        // Pass 4:3 UVs to shader (0,0)-(1,1) always covers the 4:3 area, regardless of screen size)
        bufferBuilder.vertex(x, y + targetHeight, 0).uv(0.0F, 1.0F).endVertex();
        bufferBuilder.vertex(x + targetWidth, y + targetHeight, 0).uv(1.0F, 1.0F).endVertex();
        bufferBuilder.vertex(x + targetWidth, y, 0).uv(1.0F, 0.0F).endVertex();
        bufferBuilder.vertex(x, y, 0).uv(0.0F, 0.0F).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        // Restore projection matrix after custom overlay
        RenderSystem.setProjectionMatrix(new Matrix4f().ortho(0.0F, screenWidth, screenHeight, 0.0F, 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // Helper to draw a solid color rectangle (ARGB)
    private static void fillRect(int x, int y, int w, int h, int color) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float a = ((color >> 24) & 0xFF) / 255.0F;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        buffer.vertex(x, y + h, 0).color(r, g, b, a).endVertex();
        buffer.vertex(x + w, y + h, 0).color(r, g, b, a).endVertex();
        buffer.vertex(x + w, y, 0).color(r, g, b, a).endVertex();
        buffer.vertex(x, y, 0).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }
}
