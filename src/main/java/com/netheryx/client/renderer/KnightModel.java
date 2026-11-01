package com.netheryx.client.renderer;

import com.netheryx.NetheryX;
import com.netheryx.entity.WitheredKnight;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Modele des chevaliers fletris : le capitaine et le Gardien des runes ont leur propre forme et leur propre texture, mais
 * partagent les animations du chevalier.
 */
public class KnightModel<T extends WitheredKnight> extends GeoModel<T> {
    private final String name;

    public KnightModel(String name) {
        this.name = name;
    }

    private String shape(T knight) {
        return knight.isCaptain() ? "withered_knight_captain" : this.name;
    }

    @Override
    public ResourceLocation getModelResource(T knight) {
        return NetheryX.id("geo/entity/" + this.shape(knight) + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T knight) {
        return NetheryX.id("textures/entity/" + this.shape(knight) + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T knight) {
        return NetheryX.id("animations/entity/withered_knight.animation.json");
    }
}
