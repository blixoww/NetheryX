package com.netheryx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Amas de cristaux qui scintille. */
public class AmbientClusterBlock extends AmethystClusterBlock {
    private final AmbientParticles particles;

    public AmbientClusterBlock(int height, int offset, Properties properties, AmbientParticles particles) {
        super(height, offset, properties);
        this.particles = particles;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        this.particles.tick(state, level, pos, random);
    }
}
