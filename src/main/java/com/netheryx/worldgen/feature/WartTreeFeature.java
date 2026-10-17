package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.netheryx.block.WartvineBlock;
import com.netheryx.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Verrue geante : une tige de verrue porte une grosse boule de verrue parsemee de bulbes lumineux,
 * sous laquelle pendent des verrulianes chargees de fruits.
 */
public class WartTreeFeature extends Feature<NoneFeatureConfiguration> {
    public WartTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!level.getBlockState(origin.below()).isSolidRender(level, origin.below()) || !level.getBlockState(origin).isAir()) {
            return false;
        }
        int height = 4 + random.nextInt(6);
        int radius = 2 + random.nextInt(2);
        int ceiling = FeatureHelper.ceilingDistance(level, origin, height + radius * 2 + 2);
        if (ceiling > 0) {
            return false;
        }
        BlockState stem = ModBlocks.WARTSTALK_STEM.get().defaultBlockState();
        for (int y = 0; y < height; y++) {
            FeatureHelper.set(level, origin.above(y), stem);
        }
        BlockPos center = origin.above(height + radius - 1);
        BlockState wart = Blocks.NETHER_WART_BLOCK.defaultBlockState();
        BlockState bulb = ModBlocks.WART_BULB.get().defaultBlockState();
        int ry = Math.max(1, radius - 1);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -ry; dy <= ry; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double d = (dx * dx + dz * dz) / (double) (radius * radius) + (dy * dy) / (double) (ry * ry);
                    if (d > 1.05D) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    boolean shell = d > 0.45D;
                    FeatureHelper.set(level, pos, shell && random.nextInt(9) == 0 ? bulb : wart);
                    if (dy == -ry && random.nextInt(3) == 0) {
                        boolean fruit = random.nextInt(3) == 0;
                        FeatureHelper.hangVine(level, pos, 2 + random.nextInt(5), ModBlocks.WARTVINE.get(), ModBlocks.WARTVINE_PLANT.get(),
                                random, state -> state.setValue(WartvineBlock.FRUIT, fruit || random.nextInt(4) == 0));
                    }
                }
            }
        }
        return true;
    }
}
