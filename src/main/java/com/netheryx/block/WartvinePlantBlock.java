package com.netheryx.block;

import com.netheryx.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

/** Corps de la verruliane (porte aussi des fruits). */
public class WartvinePlantBlock extends HangingVinesPlantBlock {
    public WartvinePlantBlock(Properties properties, Supplier<? extends Block> head) {
        super(properties, head);
        this.registerDefaultState(this.stateDefinition.any().setValue(WartvineBlock.FRUIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WartvineBlock.FRUIT);
    }

    @Override
    protected BlockState updateHeadAfterConvertedFromBody(BlockState body, BlockState head) {
        return head.setValue(WartvineBlock.FRUIT, body.getValue(WartvineBlock.FRUIT));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        WartvineBlock.ripen(state, level, pos, random);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return WartvineBlock.harvest(player, state, level, pos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.WARTFRUIT.get());
    }
}
