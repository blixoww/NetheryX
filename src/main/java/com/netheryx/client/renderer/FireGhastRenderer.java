package com.netheryx.client.renderer;

import com.netheryx.NetheryX;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;

public class FireGhastRenderer extends GhastRenderer {
    private static final ResourceLocation TEXTURE = NetheryX.id("textures/entity/fire_ghast.png");
    private static final ResourceLocation SHOOTING = NetheryX.id("textures/entity/fire_ghast_shooting.png");

    public FireGhastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Ghast ghast) {
        return ghast.isCharging() ? SHOOTING : TEXTURE;
    }
}
