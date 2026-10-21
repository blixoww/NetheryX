package com.netheryx.block;

import com.netheryx.blockentity.SoulAltarBlockEntity;
import com.netheryx.registry.ModBlockEntities;
import com.netheryx.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Autel des ames : son interface accueille un objet et une offrande du Nether pour lui conferer un enchantement
 * unique (Pas de braise, Recolte d'ames, Ancrage). L'autel doit etre entoure d'au moins deux flammes des ames
 * (feu des ames, torches, lanternes ou feux de camp des ames) et l'infusion coute de l'experience.
 */
public class SoulAltarBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(box(1.0D, 0.0D, 1.0D, 15.0D, 4.0D, 15.0D),
            box(3.0D, 4.0D, 3.0D, 13.0D, 10.0D, 13.0D), box(0.0D, 10.0D, 0.0D, 16.0D, 13.0D, 16.0D));

    public SoulAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulAltarBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof SoulAltarBlockEntity altar) {
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, altar, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.SOUL_ALTAR.get(), SoulAltarBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            level.addParticle(ModParticles.SOUL_WISP.get(), pos.getX() + 0.5D + Math.cos(angle) * 0.6D, pos.getY() + 0.9D,
                    pos.getZ() + 0.5D + Math.sin(angle) * 0.6D, 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof SoulAltarBlockEntity altar) {
                Containers.dropContents(level, pos, altar.getInventory());
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    /** Nombre de flammes des ames dans un rayon de 3 blocs. */
    public static int countSoulFlames(Level level, BlockPos center) {
        int flames = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 2, 3))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.SOUL_FIRE) || state.is(Blocks.SOUL_TORCH)
                    || state.is(Blocks.SOUL_WALL_TORCH) || state.is(Blocks.SOUL_LANTERN)
                    || (state.is(Blocks.SOUL_CAMPFIRE)
                    && state.getValue(CampfireBlock.LIT))) {
                flames++;
            }
        }
        return flames;
    }
}
