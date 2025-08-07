package net.idothehax.theoldbroadcast.client.renderer.entity;

import net.idothehax.theoldbroadcast.entity.ShadowGhostEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShadowGhostRenderer extends MobRenderer<ShadowGhostEntity, ShadowGhostModel<ShadowGhostEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("theoldbroadcast", "textures/entity/shadow_ghost.png");

    public ShadowGhostRenderer(EntityRendererProvider.Context context) {
        super(context, new ShadowGhostModel<>(context.bakeLayer(ModModelLayers.SHADOW_GHOST_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowGhostEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean isBodyVisible(ShadowGhostEntity entity) {
        return !entity.isInvisible();
    }
}
