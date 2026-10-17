package com.netheryx.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/** Corps d'une liane pendante. */
public class HangingVinesPlantBlock extends GrowingPlantBodyBlock {
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private final Supplier<? extends Block> head;

    public HangingVinesPlantBlock(Properties properties, Supplier<? extends Block> head) {
        super(properties, Direction.DOWN, SHAPE, false);
        this.head = head;
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return (GrowingPlantHeadBlock) this.head.get();
    }
}
