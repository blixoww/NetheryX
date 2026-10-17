package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.netheryx.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Pic de givre : une colonne de glace de givre sur socle de pierre de givre, couronnee d'un cristal ;
 * et, si la voute est proche, une stalactite de givre suspendue au-dessus.
 */
public class RimeSpikeFeature extends Feature<NoneFeatureConfiguration> {
    public RimeSpikeFeature(Codec<NoneFeatureConfiguration> codec) {
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
        int height = 3 + random.nextInt(9);
        int ceiling = FeatureHelper.ceilingDistance(level, origin, 30);
        if (ceiling > 0 && ceiling < height + 2) {
            height = Math.max(2, ceiling - 2);
        }
        this.cone(level, origin, height, Direction.UP, random);
        BlockPos tip = origin.above(height);
        FeatureHelper.set(level, tip, ModBlocks.RIME_CRYSTAL.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
        if (ceiling > height + 6) {
            int length = 2 + random.nextInt(Math.min(8, ceiling - height - 3));
            this.cone(level, origin.above(ceiling - 1), length, Direction.DOWN, random);
        }
        return true;
    }

    private void cone(WorldGenLevel level, BlockPos base, int height, Direction direction, RandomSource random) {
        BlockState ice = ModBlocks.RIME_ICE.get().defaultBlockState();
        BlockState stone = ModBlocks.RIMESTONE.get().defaultBlockState();
        float baseRadius = 0.8F + height / 5.0F;
        for (int i = 0; i < height; i++) {
            float radius = baseRadius * (1.0F - i / (float) height) + 0.2F;
            int r = Mth.ceil(radius);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        boolean shell = i < 2 && direction == Direction.UP && dx * dx + dz * dz > (radius - 1) * (radius - 1);
                        FeatureHelper.set(level, base.relative(direction, i).offset(dx, 0, dz), shell || random.nextInt(8) == 0 ? stone : ice);
                    }
                }
            }
        }
    }
}
