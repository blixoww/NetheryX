package com.netheryx.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;

/** Particules d'ambiance des creatures (appelees cote client depuis {@code aiStep}). */
public final class MobParticles {
    private MobParticles() {
    }

    /**
     * Emet, une fois sur {@code chance}, une particule dans le volume de la creature.
     *
     * @param spread largeur relative de la zone d'emission (1.0 = toute la largeur de la hitbox)
     * @param vy     vitesse verticale initiale
     */
    public static void ambient(Entity entity, ParticleOptions particle, int chance, double spread, double vy) {
        if (entity.level().isClientSide && !entity.isInvisible() && entity.level().getRandom().nextInt(chance) == 0) {
            entity.level().addParticle(particle, entity.getRandomX(spread), entity.getRandomY(), entity.getRandomZ(spread), 0.0D, vy, 0.0D);
        }
    }
}
