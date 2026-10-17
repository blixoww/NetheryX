package com.netheryx.block;

import com.netheryx.registry.ModItems;
import com.netheryx.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

public class SoulTuberCropBlock extends NetherCropBlock {
    public SoulTuberCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModTags.Blocks.SOUL_TUBER_SOIL);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.SOUL_TUBER.get();
    }
}
