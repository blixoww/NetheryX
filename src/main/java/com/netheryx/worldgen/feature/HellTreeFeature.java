package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.netheryx.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Arbre de la Jungle infernale : tronc de braise epais a contreforts, branches, large canopee de verrue de braise
 * piquee de champilampes, et lianes de braise qui pendent jusqu'au sol.
 */
public class HellTreeFeature extends Feature<NoneFeatureConfiguration> {
    public HellTreeFeature(Codec<NoneFeatureConfiguration> codec) {
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
        int height = 9 + random.nextInt(8);
        int ceiling = FeatureHelper.ceilingDistance(level, origin, height + 6);
        if (ceiling > 0) {
            height = ceiling - 5;
        }
        if (height < 6) {
            return false;
        }
        BlockState stem = ModBlocks.EMBER_STEM.get().defaultBlockState();
        boolean thick = height > 12;
        for (int y = 0; y < height; y++) {
            FeatureHelper.set(level, origin.above(y), stem);
            if (thick && y < height - 3) {
                FeatureHelper.set(level, origin.offset(1, y, 0), stem);
                FeatureHelper.set(level, origin.offset(0, y, 1), stem);
                FeatureHelper.set(level, origin.offset(1, y, 1), stem);
            }
        }
        // Contreforts
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos root = origin.relative(side, thick ? 2 : 1);
            for (int y = 0; y < 1 + random.nextInt(3); y++) {
                FeatureHelper.set(level, root.above(y), ModBlocks.EMBER_HYPHAE.get().defaultBlockState());
            }
        }
        BlockPos top = origin.above(height);
        this.canopy(level, top, thick ? 5 : 4, random);
        // Branches avec petites canopees
        for (int i = 0; i < 2 + random.nextInt(2); i++) {
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos cursor = origin.above(height / 2 + random.nextInt(Math.max(1, height / 3)));
            for (int k = 0; k < 3; k++) {
                cursor = cursor.relative(side).above(k % 2);
                FeatureHelper.set(level, cursor, stem.setValue(RotatedPillarBlock.AXIS, side.getAxis()));
            }
            this.canopy(level, cursor.above(), 2, random);
        }
        return true;
    }

    private void canopy(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        BlockState wart = ModBlocks.EMBER_WART_BLOCK.get().defaultBlockState();
        for (int dy = -1; dy <= 2; dy++) {
            int r = dy <= 0 ? radius : radius - dy;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    if (distance > r + 0.3D || (dy == -1 && distance < r - 1.5D)) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    FeatureHelper.set(level, pos, random.nextInt(14) == 0 ? Blocks.SHROOMLIGHT.defaultBlockState() : wart);
                    if (dy == -1 && random.nextInt(4) == 0) {
                        FeatureHelper.hangVine(level, pos, 2 + random.nextInt(9), ModBlocks.EMBER_VINES.get(),
                                ModBlocks.EMBER_VINES_PLANT.get(), random, state -> state);
                    }
                }
            }
        }
    }
}
