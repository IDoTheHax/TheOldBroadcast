package net.idothehax.theoldbroadcast;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side events for sanity visuals and audio.
 */
@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SanityClientEvents {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "textures/overlay/static.png");

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !mc.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "old_broadcast"))) return;

        int sanity = player.getPersistentData().getInt(SanityHandler.SANITY_TAG);
        if (sanity >= SanityHandler.MAX_SANITY) return;

        float opacity = 1.0F - (sanity / (float)SanityHandler.MAX_SANITY);
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        GuiGraphics graphics = event.getGuiGraphics();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, OVERLAY);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);

        PoseStack poseStack = graphics.pose();
        // draw full-screen overlay
        graphics.blit(OVERLAY, 0, 0, width, height, 0, 0, width, height);

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        // TODO play audio when sanity drops below threshold
        // Implementation omitted for now
    }
}
