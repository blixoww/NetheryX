package com.netheryx.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Outils communs aux features de NetheryX. */
final class FeatureHelper {
    private FeatureHelper() {
    }

    /** Air ou vegetation remplacable (jamais de fluide ni de bloc solide). */
    static boolean replaceable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || (state.canBeReplaced() && state.getFluidState().isEmpty());
    }

    static boolean set(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (replaceable(level, pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            return true;
        }
        return false;
    }

    /** Distance jusqu'au premier bloc non vide au-dessus, ou -1 s'il n'y en a pas dans {@code max} blocs. */
    static int ceilingDistance(WorldGenLevel level, BlockPos origin, int max) {
        BlockPos.MutableBlockPos pos = origin.mutable();
        for (int dy = 1; dy <= max; dy++) {
            pos.move(Direction.UP);
            if (!level.getBlockState(pos).isAir()) {
                return dy;
            }
        }
        return -1;
    }

    /** Fait pendre une liane (corps puis extremite) sous {@code top}. */
    static void hangVine(WorldGenLevel level, BlockPos top, int length, Block head, Block body, RandomSource random,
                         java.util.function.UnaryOperator<BlockState> decorate) {
        BlockPos.MutableBlockPos pos = top.mutable();
        int placed = 0;
        while (placed < length && replaceable(level, pos.below()) && level.getBlockState(pos.below()).isAir()) {
            pos.move(Direction.DOWN);
            placed++;
        }
        if (placed == 0) {
            return;
        }
        BlockPos.MutableBlockPos cursor = top.mutable();
        for (int i = 1; i <= placed; i++) {
            cursor.move(Direction.DOWN);
            BlockState state = i == placed
                    ? head.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, 15 + random.nextInt(10))
                    : body.defaultBlockState();
            level.setBlock(cursor, decorate.apply(state), Block.UPDATE_CLIENTS);
        }
    }
}
