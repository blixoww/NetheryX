package com.netheryx.block;

import com.netheryx.blockentity.OnyxFurnaceBlockEntity;
import com.netheryx.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Four en onyx : cuit deux fois plus vite que le four vanilla, pour toutes les recettes de cuisson. */
public class OnyxFurnaceBlock extends AbstractFurnaceBlock {
    public OnyxFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OnyxFurnaceBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.ONYX_FURNACE.get(), (tickLevel, pos, tickState, furnace) -> {
            // Deux cycles par tick : cuisson deux fois plus rapide (le combustible dure autant par objet cuit).
            AbstractFurnaceBlockEntity.serverTick(tickLevel, pos, tickState, furnace);
            AbstractFurnaceBlockEntity.serverTick(tickLevel, pos, tickLevel.getBlockState(pos), furnace);
        });
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof OnyxFurnaceBlockEntity furnace) {
            player.openMenu(furnace);
            player.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        if (random.nextDouble() < 0.1D) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        Direction facing = state.getValue(FACING);
        double offset = random.nextDouble() * 0.6D - 0.3D;
        double dx = facing.getAxis() == Direction.Axis.X ? facing.getStepX() * 0.52D : offset;
        double dz = facing.getAxis() == Direction.Axis.Z ? facing.getStepZ() * 0.52D : offset;
        double dy = random.nextDouble() * 6.0D / 16.0D;
        level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + dx, y + dy, z + dz, 0.0D, 0.0D, 0.0D);
    }
}
