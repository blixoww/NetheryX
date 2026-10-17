package com.netheryx.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Vision de lave : voir clairement sous la lave et y nager plus vite (voir {@code ArmorSetEffects} et
 * {@code ClientForgeEvents}). Obtenue avec la potion brassee a partir d'ecailles de poisson de magme.
 */
public class LavaVisionEffect extends MobEffect {
    public LavaVisionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF6A1A);
    }
}
