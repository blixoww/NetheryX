package com.netheryx.item;

import com.netheryx.util.WyvernCompanions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Sifflet de wyverne : rappelle aupres du joueur toutes ses wyvernes, ou qu'elles soient (autre dimension comprise). */
public class WyvernWhistleItem extends Item {
    public WyvernWhistleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.blockPosition(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(2).value(), SoundSource.PLAYERS, 1.0F, 1.6F);
        if (player instanceof ServerPlayer serverPlayer) {
            int count = WyvernCompanions.summon(serverPlayer);
            player.displayClientMessage(count == 0
                    ? Component.translatable("message.netheryx.wyvern_none").withStyle(ChatFormatting.GRAY)
                    : Component.translatable("message.netheryx.wyvern_summoned", count).withStyle(ChatFormatting.GOLD), true);
        }
        player.getCooldowns().addCooldown(this, 100);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.wyvern_whistle.tooltip").withStyle(ChatFormatting.GOLD));
    }
}
