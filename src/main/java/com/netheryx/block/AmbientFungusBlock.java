package com.netheryx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/** Champignon geant (pousse a la poudre d'os) qui emet des particules. */
public class AmbientFungusBlock extends FungusBlock {
    private final AmbientParticles particles;

    public AmbientFungusBlock(Properties properties, ResourceKey<ConfiguredFeature<?, ?>> feature, Block soil,
                              AmbientParticles particles) {
        super(properties, feature, soil);
        this.particles = particles;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        this.particles.tick(state, level, pos, random);
    }
}
