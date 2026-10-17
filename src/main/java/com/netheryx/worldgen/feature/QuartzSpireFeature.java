package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.netheryx.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Aiguille de quartz : cone de quartz naturel, legerement penche, avec des veines de minerai de quartz riche.
 * Les Recifs de quartz en sont herisses.
 */
public class QuartzSpireFeature extends Feature<NoneFeatureConfiguration> {
    public QuartzSpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!level.getBlockState(origin.below()).isSolidRender(level, origin.below())) {
            return false;
        }
        int height = 6 + random.nextInt(15);
        int ceiling = FeatureHelper.ceilingDistance(level, origin, height + 2);
        if (ceiling > 0) {
            height = Math.max(3, ceiling - 2);
        }
        float baseRadius = 1.2F + height / 7.0F;
        float leanX = (random.nextFloat() - 0.5F) * 0.5F;
        float leanZ = (random.nextFloat() - 0.5F) * 0.5F;
        for (int y = -2; y < height; y++) {
            float t = Math.max(0, y) / (float) height;
            float radius = baseRadius * (1.0F - t) + 0.3F;
            int cx = Mth.floor(leanX * Math.max(0, y));
            int cz = Mth.floor(leanZ * Math.max(0, y));
            int r = Mth.ceil(radius);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }
                    BlockPos pos = origin.offset(cx + dx, y, cz + dz);
                    BlockState state = this.pick(random, dx == 0 && dz == 0);
                    if (y < 0) {
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                        }
                    } else {
                        FeatureHelper.set(level, pos, state);
                    }
                }
            }
        }
        return true;
    }

    private BlockState pick(RandomSource random, boolean core) {
        if (core) {
            return Blocks.QUARTZ_PILLAR.defaultBlockState();
        }
        int roll = random.nextInt(100);
        if (roll < 10) {
            return ModBlocks.RICH_QUARTZ_ORE.get().defaultBlockState();
        }
        if (roll < 25) {
            return Blocks.SMOOTH_QUARTZ.defaultBlockState();
        }
        if (roll < 32) {
            return Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
        }
        return Blocks.QUARTZ_BLOCK.defaultBlockState();
    }
}
