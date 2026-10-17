package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.netheryx.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Aiguille d'obsidienne du Lac de verre en fusion : cone d'obsidienne veine d'obsidienne pleureuse et de verre en
 * fusion, herisse de cristaux en fusion.
 */
public class ObsidianSpireFeature extends Feature<NoneFeatureConfiguration> {
    public ObsidianSpireFeature(Codec<NoneFeatureConfiguration> codec) {
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
        int height = 5 + random.nextInt(12);
        int ceiling = FeatureHelper.ceilingDistance(level, origin, height + 2);
        if (ceiling > 0) {
            height = Math.max(3, ceiling - 2);
        }
        float baseRadius = 1.0F + height / 6.0F;
        for (int y = -2; y < height; y++) {
            float t = Math.max(0, y) / (float) height;
            float radius = baseRadius * (1.0F - t) + 0.3F;
            int r = Mth.ceil(radius);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }
                    BlockPos pos = origin.offset(dx, y, dz);
                    BlockState state = this.pick(random);
                    if (y < 0) {
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                        }
                    } else if (FeatureHelper.set(level, pos, state) && random.nextInt(9) == 0) {
                        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                        BlockPos crystal = pos.relative(side);
                        if (level.getBlockState(crystal).isAir()) {
                            level.setBlock(crystal, ModBlocks.MOLTEN_CRYSTAL.get().defaultBlockState()
                                    .setValue(AmethystClusterBlock.FACING, side), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
        return true;
    }

    private BlockState pick(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 12) {
            return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        }
        if (roll < 22) {
            return ModBlocks.MOLTEN_GLASS.get().defaultBlockState();
        }
        return Blocks.OBSIDIAN.defaultBlockState();
    }
}
