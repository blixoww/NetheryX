package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.netheryx.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Ilot d'obsidienne pose sur la mer de lave : une galette irreguliere d'obsidienne et de verre en fusion,
 * avec quelques cristaux en fusion. Ne fait rien si l'origine n'est pas a la surface de la lave.
 */
public class LavaIslandFeature extends Feature<NoneFeatureConfiguration> {
    public LavaIslandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!level.getFluidState(origin).is(FluidTags.LAVA) || !level.getBlockState(origin.above()).isAir()) {
            return false;
        }
        int radius = 3 + random.nextInt(5);
        boolean placed = false;
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz) + random.nextDouble() * 1.2D;
                if (distance > radius) {
                    continue;
                }
                int depth = 1 + (int) ((radius - distance) * 0.8D);
                for (int dy = -depth; dy <= 0; dy++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (level.getBlockState(pos).isAir() || level.getFluidState(pos).is(FluidTags.LAVA)) {
                        boolean top = dy == 0;
                        level.setBlock(pos, top && random.nextInt(6) == 0 ? ModBlocks.MOLTEN_GLASS.get().defaultBlockState()
                                : top && random.nextInt(10) == 0 ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                                : Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_CLIENTS);
                        placed = true;
                    }
                }
                BlockPos above = origin.offset(dx, 1, dz);
                if (distance < radius - 1 && random.nextInt(14) == 0 && level.getBlockState(above).isAir()) {
                    level.setBlock(above, ModBlocks.MOLTEN_CRYSTAL.get().defaultBlockState()
                            .setValue(AmethystClusterBlock.FACING, Direction.UP), Block.UPDATE_CLIENTS);
                }
            }
        }
        return placed;
    }
}
