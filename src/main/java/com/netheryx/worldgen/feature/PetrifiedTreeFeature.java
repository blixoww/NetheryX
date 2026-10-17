package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.netheryx.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Arbre petrifie de la Foret d'onyx : tronc de bois petrifie tordu, branches nues terminees par des grappes de
 * feuillage cristallise et des amas d'onyx.
 */
public class PetrifiedTreeFeature extends Feature<NoneFeatureConfiguration> {
    public PetrifiedTreeFeature(Codec<NoneFeatureConfiguration> codec) {
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
        int height = 5 + random.nextInt(6);
        int ceiling = FeatureHelper.ceilingDistance(level, origin, height + 4);
        if (ceiling > 0) {
            height = ceiling - 4;
        }
        if (height < 4) {
            return false;
        }
        BlockState log = ModBlocks.PETRIFIED_LOG.get().defaultBlockState();
        BlockPos cursor = origin;
        for (int y = 0; y < height; y++) {
            FeatureHelper.set(level, cursor, log);
            // Le tronc se tord un peu en montant
            if (y > 1 && random.nextInt(4) == 0) {
                Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                cursor = cursor.relative(side);
                FeatureHelper.set(level, cursor, log.setValue(RotatedPillarBlock.AXIS, side.getAxis()));
            }
            cursor = cursor.above();
        }
        this.crown(level, cursor, 2, random);
        for (int i = 0; i < 2 + random.nextInt(3); i++) {
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos branch = origin.above(height / 2 + random.nextInt(Math.max(1, height / 2)));
            int length = 2 + random.nextInt(3);
            for (int k = 0; k < length; k++) {
                branch = branch.relative(side).above(random.nextInt(2));
                FeatureHelper.set(level, branch, log.setValue(RotatedPillarBlock.AXIS, side.getAxis()));
            }
            this.crown(level, branch.above(), 1, random);
        }
        return true;
    }

    private void crown(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        BlockState leaves = ModBlocks.ONYX_CRYSTAL_LEAVES.get().defaultBlockState();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radius * radius + 1 || random.nextInt(5) == 0) {
                        continue;
                    }
                    FeatureHelper.set(level, center.offset(dx, dy, dz), leaves);
                }
            }
        }
        BlockPos top = center.above(radius + 1);
        if (level.getBlockState(top).isAir() && !level.getBlockState(top.below()).isAir()) {
            level.setBlock(top, ModBlocks.ONYX_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP), 2);
        }
    }
}
