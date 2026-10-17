package com.netheryx.block;

import com.netheryx.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Lys des ames : fleur lumineuse du Marais des ames, qui laisse echapper des flammes bleues. */
public class SoulLilyBlock extends NetherPlantBlock {
    public SoulLilyBlock(Properties properties) {
        super(properties, AmbientParticles.of(ModParticles.SOUL_WISP, 6, AmbientParticles.Mode.INSIDE));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.3D + random.nextDouble() * 0.4D,
                    pos.getY() + 0.7D, pos.getZ() + 0.3D + random.nextDouble() * 0.4D, 0.0D, 0.02D, 0.0D);
        }
    }
}
