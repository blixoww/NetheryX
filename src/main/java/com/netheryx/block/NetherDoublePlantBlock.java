package com.netheryx.block;

import com.netheryx.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.PlantType;

/** Plante haute (2 blocs) du Nether, posee sur les sols du tag {@code netheryx:nether_plant_soil}. */
public class NetherDoublePlantBlock extends DoublePlantBlock {
    public NetherDoublePlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModTags.Blocks.NETHER_PLANT_SOIL) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    public PlantType getPlantType(BlockGetter level, BlockPos pos) {
        return PlantType.NETHER;
    }
}
