package com.netheryx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Bloc plein qui emet des particules d'ambiance (briques de magma, lampe de braise, verrues...). */
public class AmbientBlock extends Block {
    private final AmbientParticles particles;

    public AmbientBlock(Properties properties, AmbientParticles particles) {
        super(properties);
        this.particles = particles;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        this.particles.tick(state, level, pos, random);
    }
}
