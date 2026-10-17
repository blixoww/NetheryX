package com.netheryx.block;

import com.netheryx.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glace de givre : ne fond jamais a la lumiere ni dans le Nether. Placee contre une source de chaleur
 * (lave, feu, bloc de magma, feu de camp allume), elle fond lentement en une <b>source d'eau</b> :
 * c'est le seul moyen d'obtenir de l'eau dans le Nether.
 */
public class RimeIceBlock extends Block {
    public RimeIceBlock(Properties properties) {
        super(properties);
    }

    public static boolean isHeatSource(BlockState state) {
        return state.getFluidState().is(FluidTags.LAVA) || state.is(BlockTags.FIRE) || state.is(Blocks.MAGMA_BLOCK)
                || (state.is(BlockTags.CAMPFIRES) && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT));
    }

    private static boolean nearHeat(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (isHeatSource(level.getBlockState(pos.relative(direction)))) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (nearHeat(level, pos) && random.nextInt(3) == 0) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.6F);
            level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 8, 0.3D, 0.1D, 0.3D, 0.01D);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(nearHeat(level, pos) ? 2 : 14) == 0) {
            Direction face = Direction.getRandom(random);
            if (!level.getBlockState(pos.relative(face)).isSolidRender(level, pos.relative(face))) {
                level.addParticle(nearHeat(level, pos) ? ParticleTypes.DRIPPING_WATER : ModParticles.FROST_MOTE.get(),
                        pos.getX() + 0.5D + face.getStepX() * 0.55D + (random.nextDouble() - 0.5D) * 0.8D * (1 - Math.abs(face.getStepX())),
                        pos.getY() + 0.5D + face.getStepY() * 0.55D + (random.nextDouble() - 0.5D) * 0.8D * (1 - Math.abs(face.getStepY())),
                        pos.getZ() + 0.5D + face.getStepZ() * 0.55D + (random.nextDouble() - 0.5D) * 0.8D * (1 - Math.abs(face.getStepZ())),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }
}
