package com.netheryx.block;

import com.netheryx.blockentity.WardLanternBlockEntity;
import com.netheryx.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Lanterne de garde : aucune creature hostile n'apparait naturellement a moins de 24 blocs.
 * Ideale pour securiser une base ou une ferme dans le Nether.
 */
public class WardLanternBlock extends LanternBlock implements EntityBlock {
    public WardLanternBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardLanternBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(ModParticles.SOUL_WISP.get(), pos.getX() + 0.3D + random.nextDouble() * 0.4D, pos.getY() + 0.5D,
                    pos.getZ() + 0.3D + random.nextDouble() * 0.4D, 0.0D, 0.01D, 0.0D);
        }
    }
}
