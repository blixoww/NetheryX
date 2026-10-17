package com.netheryx.block;

import com.netheryx.worldgen.ModFeatureKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Sol vegetal du Nether (nylium de braise, herbe du Nether) : redevient de la netherrack s'il est recouvert
 * et fait pousser sa flore a la poudre d'os.
 */
public class EmberNyliumBlock extends Block implements BonemealableBlock {
    private final ResourceKey<ConfiguredFeature<?, ?>> bonemealFeature;
    @Nullable
    private final AmbientParticles particles;

    public EmberNyliumBlock(Properties properties, @Nullable AmbientParticles particles) {
        this(properties, ModFeatureKeys.EMBER_VEGETATION_BONEMEAL, particles);
    }

    public EmberNyliumBlock(Properties properties, ResourceKey<ConfiguredFeature<?, ?>> bonemealFeature,
                            @Nullable AmbientParticles particles) {
        super(properties);
        this.bonemealFeature = bonemealFeature;
        this.particles = particles;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.particles != null) {
            this.particles.tick(state, level, pos, random);
        }
    }

    private static boolean canStayNylium(LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return !level.getBlockState(above).isSolidRender(level, above);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canStayNylium(level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.NETHERRACK.defaultBlockState());
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolder(this.bonemealFeature)
                .ifPresent(feature -> feature.value().place(level, level.getChunkSource().getGenerator(), random, pos.above()));
    }
}
