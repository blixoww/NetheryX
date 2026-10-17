package com.netheryx.client.renderer;

import com.netheryx.NetheryX;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.MagmaCube;

public class OnyxCubeRenderer extends MagmaCubeRenderer {
    private static final ResourceLocation TEXTURE = NetheryX.id("textures/entity/onyx_cube.png");

    public OnyxCubeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(MagmaCube cube) {
        return TEXTURE;
    }
}
