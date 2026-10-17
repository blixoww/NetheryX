package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/** Rocher : amas irregulier (2 ou 3 boules qui se chevauchent) a demi enfonce dans le sol. Rend les biomes moins plats. */
public class BoulderFeature extends Feature<BlockStateConfiguration> {
    public BoulderFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!level.getBlockState(origin.below()).isSolidRender(level, origin.below())) {
            return false;
        }
        BlockPos center = origin.below();
        for (int ball = 0; ball < 2 + random.nextInt(2); ball++) {
            float radius = 1.2F + random.nextFloat() * 1.3F;
            int r = (int) Math.ceil(radius);
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
                if (pos.distSqr(center) <= radius * radius && !level.getBlockState(pos).hasBlockEntity()) {
                    level.setBlock(pos, context.config().state, Block.UPDATE_CLIENTS);
                }
            }
            center = center.offset(random.nextInt(3) - 1, random.nextInt(2), random.nextInt(3) - 1);
        }
        return true;
    }
}
