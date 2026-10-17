package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/** Tronc tombe : quelques blocs de tige couches sur le sol, dans une direction au hasard. */
public class FallenLogFeature extends Feature<BlockStateConfiguration> {
    public FallenLogFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int length = 3 + random.nextInt(4);
        BlockState log = context.config().state;
        if (log.hasProperty(RotatedPillarBlock.AXIS)) {
            log = log.setValue(RotatedPillarBlock.AXIS, direction.getAxis());
        }
        for (int i = 0; i < length; i++) {
            BlockPos pos = origin.relative(direction, i);
            if (!FeatureHelper.replaceable(level, pos) || !level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                return i > 1;
            }
            level.setBlock(pos, log, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        return true;
    }
}
