package com.netheryx.block;

import com.netheryx.registry.ModItems;
import com.netheryx.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

/** Ble de cendre : cereale du Nether (pain de cendre, nourriture des vaches de braise). */
public class CinderWheatCropBlock extends NetherCropBlock {
    public CinderWheatCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModTags.Blocks.CINDER_WHEAT_SOIL);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.CINDER_WHEAT_SEEDS.get();
    }
}
