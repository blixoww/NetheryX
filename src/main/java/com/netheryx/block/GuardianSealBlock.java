package com.netheryx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sceau du gardien : bloc indestructible qui ferme la salle au tresor d'un donjon.
 * Il se dissout quand le boss du donjon meurt (voir {@link com.netheryx.util.GuardianSeals}).
 */
public class GuardianSealBlock extends Block {
    public GuardianSealBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }
}
