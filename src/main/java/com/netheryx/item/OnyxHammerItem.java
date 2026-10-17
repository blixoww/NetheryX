package com.netheryx.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Marteau en Onyx : mine une zone de 3x3. La logique de minage de zone se trouve dans
 * {@link com.netheryx.event.HammerMiningHandler} afin de passer par les evenements Forge
 * (et donc de respecter les protections de terrain des autres mods).
 */
public class OnyxHammerItem extends PickaxeItem {
    public OnyxHammerItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.onyx_hammer.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
