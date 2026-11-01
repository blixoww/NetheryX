package com.netheryx.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Clef runique : gagnee au fond d'une aile de donjon, elle allume un verrou de la rotonde. Elle ne se perd pas au sol. */
public class RuneKeyItem extends Item {
    public RuneKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.rune_key.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
