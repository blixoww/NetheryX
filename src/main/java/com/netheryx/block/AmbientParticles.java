package com.netheryx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Particules d'ambiance des blocs (appelees depuis {@code animateTick}, donc uniquement cote client).
 *
 * @param particle type de particule (fournisseur, car les particules sont inscrites apres les blocs)
 * @param chance   une emission sur {@code chance} appels
 * @param mode     zone d'emission
 */
public record AmbientParticles(Supplier<? extends ParticleOptions> particle, int chance, Mode mode) {
    public enum Mode {
        /** Juste au-dessus du bloc (sols, nylium). */
        ABOVE,
        /** Sous le bloc (verrues, lampes suspendues). */
        BELOW,
        /** Sur une face exposee a l'air (minerais, briques). */
        FACES,
        /** Dans le volume du bloc (plantes, amas). */
        INSIDE
    }

    public static AmbientParticles of(Supplier<? extends ParticleOptions> particle, int chance, Mode mode) {
        return new AmbientParticles(particle, chance, mode);
    }

    public void tick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(this.chance) != 0) {
            return;
        }
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        switch (this.mode) {
            case ABOVE -> {
                if (!level.getBlockState(pos.above()).isSolidRender(level, pos.above())) {
                    level.addParticle(this.particle.get(), x + random.nextDouble(), y + 1.05D, z + random.nextDouble(), 0.0D, 0.0D, 0.0D);
                }
            }
            case BELOW -> {
                if (level.getBlockState(pos.below()).isAir()) {
                    level.addParticle(this.particle.get(), x + random.nextDouble(), y - 0.05D, z + random.nextDouble(), 0.0D, 0.0D, 0.0D);
                }
            }
            case FACES -> {
                Direction face = Direction.getRandom(random);
                BlockPos side = pos.relative(face);
                if (level.getBlockState(side).isSolidRender(level, side)) {
                    return;
                }
                double ox = face.getStepX() == 0 ? random.nextDouble() : 0.5D + face.getStepX() * 0.55D;
                double oy = face.getStepY() == 0 ? random.nextDouble() : 0.5D + face.getStepY() * 0.55D;
                double oz = face.getStepZ() == 0 ? random.nextDouble() : 0.5D + face.getStepZ() * 0.55D;
                level.addParticle(this.particle.get(), x + ox, y + oy, z + oz, 0.0D, 0.0D, 0.0D);
            }
            case INSIDE -> level.addParticle(this.particle.get(), x + 0.2D + random.nextDouble() * 0.6D,
                    y + 0.2D + random.nextDouble() * 0.6D, z + 0.2D + random.nextDouble() * 0.6D, 0.0D, 0.0D, 0.0D);
        }
    }
}
