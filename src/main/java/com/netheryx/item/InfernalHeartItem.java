package com.netheryx.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Coeur infernal : tant qu'il est dans l'inventaire, le joueur resiste au feu dans le Nether. */
public class InfernalHeartItem extends Item {
    public InfernalHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || level.dimension() != Level.NETHER || level.getGameTime() % 40 != 0
                || !(entity instanceof Player player)) {
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.FIRE_RESISTANCE);
        if (current == null || current.getDuration() < 100) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 220, 0, true, false, true));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.infernal_heart.tooltip").withStyle(ChatFormatting.RED));
    }
}
