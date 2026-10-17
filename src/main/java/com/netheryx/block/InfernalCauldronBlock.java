package com.netheryx.block;

import com.netheryx.blockentity.InfernalCauldronBlockEntity;
import com.netheryx.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Chaudron infernal : un chaudron de potions en masse.
 * <ol>
 *     <li>verser une potion (jusqu'a 9 doses d'une meme potion) ;</li>
 *     <li>chauffer le chaudron par dessous (feu, lave, magma, feu de camp) ;</li>
 *     <li>ajouter du <b>soufre</b> : chaque soufre ajoute 2 doses de la potion presente ;</li>
 *     <li>remplir des fioles vides pour recuperer les potions.</li>
 * </ol>
 */
public class InfernalCauldronBlock extends BaseEntityBlock {
    public static final int MAX_DOSES = 9;
    public static final IntegerProperty LEVEL = IntegerProperty.create("doses", 0, MAX_DOSES);
    private static final VoxelShape INSIDE = box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
            box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE), BooleanOp.ONLY_FIRST);

    public InfernalCauldronBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    public static boolean isHeated(Level level, BlockPos pos) {
        return RimeIceBlock.isHeatSource(level.getBlockState(pos.below()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return INSIDE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfernalCauldronBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof InfernalCauldronBlockEntity cauldron)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        int doses = state.getValue(LEVEL);
        if (held.is(Items.POTION)) {
            Potion potion = PotionUtils.getPotion(held);
            if (potion == Potions.EMPTY || doses >= MAX_DOSES || (doses > 0 && cauldron.getPotion() != potion)) {
                return InteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                cauldron.setPotion(potion);
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, doses + 1));
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.GLASS_BOTTLE)));
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(Items.GLASS_BOTTLE) && doses > 0) {
            if (!level.isClientSide) {
                ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.POTION), cauldron.getPotion());
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, potion));
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, doses - 1));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(ModItems.SULFUR.get()) && doses > 0) {
            if (!isHeated(level, pos)) {
                if (!level.isClientSide) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("block.netheryx.infernal_cauldron.cold"), true);
                }
                return InteractionResult.CONSUME;
            }
            if (doses >= MAX_DOSES) {
                return InteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, Math.min(MAX_DOSES, doses + 2)));
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
                ((ServerLevel) level).sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                        12, 0.25D, 0.1D, 0.25D, 0.02D);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LEVEL) > 0 && isHeated(level, pos) && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + 0.25D + random.nextDouble() * 0.5D,
                    pos.getY() + 0.4D + state.getValue(LEVEL) * 0.055D, pos.getZ() + 0.25D + random.nextDouble() * 0.5D, 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LEVEL);
    }
}
