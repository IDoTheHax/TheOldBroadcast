package net.idothehax.theoldbroadcast;

import net.idothehax.theoldbroadcast.client.ModShaders;
import net.idothehax.theoldbroadcast.client.StaticOverlayFramebuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client-side events for sanity visuals and audio using custom shaders.
 */
@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SanityClientEvents {
    private static int gameTick = 0;
    private static final StaticOverlayFramebuffer framebuffer = new StaticOverlayFramebuffer();
    private static boolean shouldApplyEffect = false;
    private static float currentOpacity = 0.0f;
    private static boolean screenCaptured = false;
    private static int clientSanity = 100;
    private static final Logger LOGGER = LogManager.getLogger();

    public static void setClientSanity(int sanity) {
        clientSanity = sanity;
        LOGGER.info("[SanityClientEvents] setClientSanity called: {}", sanity);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase == ClientTickEvent.Phase.END) {
            gameTick++;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            // Check if we should apply effects
            boolean prevShouldApply = shouldApplyEffect;
            shouldApplyEffect = false;
            currentOpacity = 0.0f;

            if (player != null && mc.level != null && mc.screen == null) {
                ResourceLocation dim = mc.level.dimension().location();
                if (dim.equals(ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "old_broadcast"))) {
                    int sanity = clientSanity;
                    if (sanity < 100) {
                        shouldApplyEffect = true;
                        // More dramatic opacity curve for better visual effect
                        float sanityRatio = sanity / 100.0f;
                        currentOpacity = (1.0f - sanityRatio) * (1.0f - sanityRatio); // Quadratic curve
                        currentOpacity = Math.min(currentOpacity, 0.8f); // Cap at 80% opacity
                    }
                }
            }

            // If effect just started or stopped, resize framebuffer and reset screenCaptured
            if (shouldApplyEffect != prevShouldApply && mc.getWindow() != null) {
                framebuffer.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                screenCaptured = false;
            }
            // If effect is toggled off, also reset screenCaptured
            if (!shouldApplyEffect) {
                screenCaptured = false;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Capture screen after world rendering but before translucent objects
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS && shouldApplyEffect && !screenCaptured) {
            // Capture the current frame
            framebuffer.captureScreen();
            screenCaptured = true;
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        // Apply distortion effect as a full-screen overlay after all GUI elements
        if (shouldApplyEffect && screenCaptured && ModShaders.getStaticOverlayShader() != null) {
            // Set up render state for overlay
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            // Apply the lens distortion and static overlay effect
            framebuffer.renderDistortedScreen(
                ModShaders.getStaticOverlayShader(),
                currentOpacity,
                gameTick
            );

            // Restore previous state
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
        // Only log when shader is null and we expect it to work (reduce spam)
        else if (shouldApplyEffect && screenCaptured && ModShaders.getStaticOverlayShader() == null) {
            if (gameTick % 60 == 0) { // Log once per second instead of every frame
                LOGGER.warn("[SanityClientEvents] Shader instance is null - static overlay effect disabled");
            }
        }
    }

    /**
     * Cleanup method called when the mod is shutting down
     */
    public static void cleanup() {
        framebuffer.cleanup();
    }
}
