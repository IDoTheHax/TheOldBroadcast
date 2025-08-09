package net.idothehax.theoldbroadcast.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

/**
 * Lightweight framebuffer for handling static overlay rendering without GL errors.
 */
public class StaticOverlayFramebuffer {
    private RenderTarget screenCapture;
    private final Minecraft minecraft;

    public StaticOverlayFramebuffer() {
        this.minecraft = Minecraft.getInstance();
    }

    public void resize(int width, int height) {
        if (screenCapture != null) {
            screenCapture.destroyBuffers();
        }
        // Create a concrete RenderTarget implementation
        screenCapture = new RenderTarget(true) {
            @Override
            public void createBuffers(int width, int height, boolean clearError) {
                super.createBuffers(width, height, clearError);
            }
        };
        screenCapture.resize(width, height, Minecraft.ON_OSX);
        screenCapture.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
    }

    public void captureScreen() {
        if (screenCapture == null) {
            resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        }

        // Store current render state
        RenderTarget currentTarget = minecraft.getMainRenderTarget();

        // Bind our framebuffer
        screenCapture.bindWrite(false);
        screenCapture.clear(Minecraft.ON_OSX);

        // Set up orthographic projection for screen copy
        RenderSystem.setProjectionMatrix(new Matrix4f().ortho(0.0F,
                screenCapture.width, screenCapture.height, 0.0F, 1000.0F, 3000.0F),
                VertexSorting.ORTHOGRAPHIC_Z);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, currentTarget.getColorTextureId());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        // Render full screen quad to copy content
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(0, screenCapture.height, 0).uv(0.0F, 0.0F).endVertex();
        builder.vertex(screenCapture.width, screenCapture.height, 0).uv(1.0F, 0.0F).endVertex();
        builder.vertex(screenCapture.width, 0, 0).uv(1.0F, 1.0F).endVertex();
        builder.vertex(0, 0, 0).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        // Restore main framebuffer
        currentTarget.bindWrite(false);
    }

    public void renderDistortedScreen(ShaderInstance shader, float opacity, int gameTick) {
        if (screenCapture == null || shader == null) return;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Calculate 4:3 area
        int targetWidth = screenWidth;
        int targetHeight = (int) (screenWidth * 3.0 / 4.0);
        if (targetHeight > screenHeight) {
            targetHeight = screenHeight;
            targetWidth = (int) (screenHeight * 4.0 / 3.0);
        }
        int x = (screenWidth - targetWidth) / 2;
        int y = (screenHeight - targetHeight) / 2;

        // Set up rendering state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        // Set projection matrix
        Matrix4f projection = new Matrix4f().ortho(0.0F, screenWidth, screenHeight, 0.0F, 1000.0F, 3000.0F);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

        // Draw black bars first
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (x > 0) {
            fillRect(0, 0, x, screenHeight, 0xFF000000);
            fillRect(screenWidth - x, 0, x, screenHeight, 0xFF000000);
        }
        if (y > 0) {
            fillRect(0, 0, screenWidth, y, 0xFF000000);
            fillRect(0, screenHeight - y, screenWidth, y, 0xFF000000);
        }

        // Now apply our distortion shader
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, screenCapture.getColorTextureId());

        // Set shader uniforms with null safety
        var timeUniform = shader.getUniform("Time");
        if (timeUniform != null) {
            timeUniform.set((float) gameTick / 20.0f);
        }

        var opacityUniform = shader.getUniform("Opacity");
        if (opacityUniform != null) {
            opacityUniform.set(opacity);
        }

        var screenSizeUniform = shader.getUniform("ScreenSize");
        if (screenSizeUniform != null) {
            screenSizeUniform.set((float) targetWidth, (float) targetHeight);
        }

        // Render the distorted screen
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(x, y + targetHeight, 0).uv(0.0F, 0.0F).endVertex();
        builder.vertex(x + targetWidth, y + targetHeight, 0).uv(1.0F, 0.0F).endVertex();
        builder.vertex(x + targetWidth, y, 0).uv(1.0F, 1.0F).endVertex();
        builder.vertex(x, y, 0).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(builder.end());

        // Restore state
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

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

    public void cleanup() {
        if (screenCapture != null) {
            screenCapture.destroyBuffers();
            screenCapture = null;
        }
    }
}
