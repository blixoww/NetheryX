package com.netheryx.block;

import com.netheryx.registry.ModParticles;
import com.netheryx.registry.ModItems;
import com.netheryx.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

public class BlazePepperCropBlock extends NetherCropBlock {
    private static final AmbientParticles MATURE_PARTICLES = AmbientParticles.of(ModParticles.EMBER_SPARK, 6, AmbientParticles.Mode.INSIDE);

    public BlazePepperCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModTags.Blocks.BLAZE_PEPPER_SOIL);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.BLAZE_PEPPER_SEEDS.get();
    }

    @Override
    protected AmbientParticles matureParticles() {
        return MATURE_PARTICLES;
    }
}
