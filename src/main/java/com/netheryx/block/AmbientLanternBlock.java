package com.netheryx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Lanterne qui emet des particules. */
public class AmbientLanternBlock extends LanternBlock {
    private final AmbientParticles particles;

    public AmbientLanternBlock(Properties properties, AmbientParticles particles) {
        super(properties);
        this.particles = particles;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        this.particles.tick(state, level, pos, random);
    }
}
