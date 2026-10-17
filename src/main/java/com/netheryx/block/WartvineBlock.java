package com.netheryx.block;

import com.netheryx.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

/**
 * Extremite de la verruliane : liane pendante qui porte des fruits de verrue lumineux, a cueillir a la main.
 * Les fruits repoussent tout seuls : c'est la nourriture renouvelable de l'Enchevetrement de verrues.
 */
public class WartvineBlock extends HangingVinesBlock {
    public static final BooleanProperty FRUIT = BooleanProperty.create("fruit");

    public WartvineBlock(Properties properties, Supplier<? extends Block> body) {
        super(properties, body);
        this.registerDefaultState(this.defaultBlockState().setValue(FRUIT, false));
    }

    public static int lightLevel(BlockState state) {
        return state.getValue(FRUIT) ? 11 : 3;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FRUIT);
    }

    @Override
    protected BlockState getGrowIntoState(BlockState state, RandomSource random) {
        return super.getGrowIntoState(state, random).setValue(FRUIT, random.nextFloat() < 0.15F);
    }

    @Override
    protected BlockState updateBodyAfterConvertedFromHead(BlockState head, BlockState body) {
        return body.setValue(FRUIT, head.getValue(FRUIT));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        ripen(level.getBlockState(pos), level, pos, random);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return harvest(player, state, level, pos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.WARTFRUIT.get());
    }

    /** Cueillette commune a la tete et au corps de la liane. */
    static InteractionResult harvest(Player player, BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(FRUIT)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Block.popResource(level, pos, new ItemStack(ModItems.WARTFRUIT.get(), 1 + level.random.nextInt(2)));
            level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(FRUIT, false), Block.UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static void ripen(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.hasProperty(FRUIT) && !state.getValue(FRUIT) && random.nextInt(8) == 0) {
            level.setBlock(pos, state.setValue(FRUIT, true), Block.UPDATE_CLIENTS);
        }
    }
}
