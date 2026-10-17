package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.netheryx.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Arbre de moelle : grand tronc blanc ramifie, coiffe de chapeaux de moelle d'ou pendent des lianes lumineuses.
 * Quand le plafond est proche, le tronc s'y soude et forme un pilier naturel reliant sol et voute.
 */
public class MarrowTreeFeature extends Feature<NoneFeatureConfiguration> {
    public MarrowTreeFeature(Codec<NoneFeatureConfiguration> codec) {
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
        BlockState stem = ModBlocks.MARROW_STEM.get().defaultBlockState();
        BlockState hyphae = ModBlocks.MARROW_HYPHAE.get().defaultBlockState();
        int ceiling = FeatureHelper.ceilingDistance(level, origin, 26);
        boolean pillar = ceiling > 8 && random.nextInt(3) == 0;
        int height = pillar ? ceiling : 7 + random.nextInt(8);
        if (!pillar && ceiling > 0 && ceiling < height + 3) {
            height = ceiling - 3;
        }
        if (height < 5) {
            return false;
        }
        // Racines evasees
        for (Direction side : Direction.Plane.HORIZONTAL) {
            int rootHeight = 1 + random.nextInt(pillar ? 3 : 2);
            for (int y = 0; y < rootHeight; y++) {
                FeatureHelper.set(level, origin.relative(side).above(y), hyphae);
            }
        }
        for (int y = 0; y < height; y++) {
            FeatureHelper.set(level, origin.above(y), stem);
        }
        if (pillar) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                for (int y = 1; y <= 1 + random.nextInt(3); y++) {
                    FeatureHelper.set(level, origin.relative(side).above(height - y), hyphae);
                }
            }
        } else {
            this.cap(level, origin.above(height), 2 + random.nextInt(2), random);
        }
        // Branches : un chapeau au bout de chacune
        int branches = 1 + random.nextInt(3);
        for (int i = 0; i < branches; i++) {
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int start = height / 3 + random.nextInt(Math.max(1, height / 3));
            int length = 2 + random.nextInt(3);
            BlockPos cursor = origin.above(start);
            for (int k = 1; k <= length; k++) {
                cursor = cursor.relative(side);
                if (k % 2 == 0) {
                    cursor = cursor.above();
                }
                FeatureHelper.set(level, cursor, stem.setValue(RotatedPillarBlock.AXIS, side.getAxis()));
            }
            this.cap(level, cursor.above(), 1, random);
        }
        return true;
    }

    private void cap(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        BlockState cap = ModBlocks.MARROW_CAP.get().defaultBlockState();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distance = dx * dx + dz * dz;
                if (distance > radius * radius + 1) {
                    continue;
                }
                BlockPos pos = center.offset(dx, 0, dz);
                FeatureHelper.set(level, pos, cap);
                if (distance <= (radius - 1) * (radius - 1)) {
                    FeatureHelper.set(level, pos.above(), cap);
                }
                if (distance >= radius * radius - 1 && random.nextInt(3) == 0) {
                    FeatureHelper.hangVine(level, pos, 1 + random.nextInt(6), ModBlocks.MARROW_VINES.get(),
                            ModBlocks.MARROW_VINES_PLANT.get(), random, state -> state);
                }
            }
        }
    }
}
