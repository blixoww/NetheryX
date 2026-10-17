package com.netheryx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Minerai qui scintille ou fume sur ses faces exposees : plus facile a reperer dans une galerie. */
public class AmbientOreBlock extends DropExperienceBlock {
    private final AmbientParticles particles;

    public AmbientOreBlock(Properties properties, IntProvider xp, AmbientParticles particles) {
        super(properties, xp);
        this.particles = particles;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        this.particles.tick(state, level, pos, random);
    }
}
