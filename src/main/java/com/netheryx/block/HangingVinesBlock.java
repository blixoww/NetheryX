package com.netheryx.block;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NetherVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/** Extremite d'une liane qui pend du plafond (lianes de moelle). Grimpable via le tag {@code minecraft:climbable}. */
public class HangingVinesBlock extends GrowingPlantHeadBlock {
    private static final VoxelShape SHAPE = Block.box(4.0D, 9.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    private final Supplier<? extends Block> body;

    public HangingVinesBlock(Properties properties, Supplier<? extends Block> body) {
        super(properties, Direction.DOWN, SHAPE, false, 0.1D);
        this.body = body;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return NetherVines.getBlocksToGrowWhenBonemealed(random);
    }

    @Override
    protected Block getBodyBlock() {
        return this.body.get();
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return NetherVines.isValidGrowthState(state);
    }
}
