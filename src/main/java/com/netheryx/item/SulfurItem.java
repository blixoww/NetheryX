package com.netheryx.item;

import com.netheryx.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LevelEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Soufre : sert d'engrais pour les cultures du Nether (tag {@code netheryx:sulfur_fertilizable}),
 * y compris les verrues du Nether que la poudre d'os n'affecte pas.
 */
public class SulfurItem extends Item {
    public SulfurItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        if (player == null || !state.is(ModTags.Blocks.SULFUR_FERTILIZABLE)) {
            return InteractionResult.PASS;
        }

        if (state.getBlock() instanceof NetherWartBlock) {
            int age = state.getValue(NetherWartBlock.AGE);
            if (age >= NetherWartBlock.MAX_AGE) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(NetherWartBlock.AGE, age + 1), Block.UPDATE_CLIENTS);
                level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
                if (!player.getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (BoneMealItem.applyBonemeal(context.getItemInHand(), level, pos, player)) {
            if (!level.isClientSide) {
                level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.sulfur.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}
