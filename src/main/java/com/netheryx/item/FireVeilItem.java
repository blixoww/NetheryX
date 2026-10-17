package com.netheryx.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Voile ignifuge : tant qu'il est dans l'inventaire (ou equipe dans un emplacement « breloque » de Curios API),
 * les flammes qui couvrent l'ecran quand on brule ne sont plus affichees. Purement visuel : on brule toujours.
 * L'effet est gere cote client dans {@code ClientForgeEvents}.
 */
public class FireVeilItem extends Item {
    public FireVeilItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.fire_veil.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.netheryx.fire_veil.tooltip2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.netheryx.fire_veil.tooltip3").withStyle(ChatFormatting.DARK_GRAY));
    }
}
