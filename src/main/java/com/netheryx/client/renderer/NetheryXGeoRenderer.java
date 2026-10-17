package com.netheryx.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.netheryx.NetheryX;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Rendu GeckoLib generique des creatures NetheryX. Les ressources sont resolues par convention :
 * {@code geo/entity/<nom>.geo.json}, {@code animations/entity/<nom>.animation.json} et
 * {@code textures/entity/<nom>.png} (+ {@code <nom>_glowmask.png} pour les parties lumineuses).
 */
public class NetheryXGeoRenderer<T extends LivingEntity & GeoAnimatable> extends GeoEntityRenderer<T> {
    private boolean translucent;

    public NetheryXGeoRenderer(EntityRendererProvider.Context context, String name, float shadowRadius, float scale) {
        this(context, new DefaultedEntityGeoModel<>(NetheryX.id(name), true), shadowRadius, scale);
    }

    /** Variante avec un modele personnalise (parties masquees selon l'etat, etc.). */
    public NetheryXGeoRenderer(EntityRendererProvider.Context context, GeoModel<T> model, float shadowRadius, float scale) {
        super(context, model);
        this.shadowRadius = shadowRadius;
        this.withScale(scale);
    }

    /** Ajoute une couche emissive basee sur la texture {@code _glowmask}. */
    public NetheryXGeoRenderer<T> glowing() {
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        return this;
    }

    /** Rend la creature semi-transparente (spectres). */
    public NetheryXGeoRenderer<T> translucent() {
        this.translucent = true;
        return this;
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return this.translucent ? RenderType.entityTranslucent(texture) : super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
