package com.netheryx.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Fruit d'un arbre du Nether : il se mange (et peut guerir un effet), et se plante sous un bloc de houppier pour faire
 * pousser une nouvelle grappe.
 */
public class FruitItem extends ItemNameBlockItem {
    @Nullable
    private final Supplier<MobEffect> cures;

    public FruitItem(Block cluster, Properties properties, @Nullable Supplier<MobEffect> cures) {
        super(cluster, properties);
        this.cures = cures;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && this.cures != null) {
            entity.removeEffect(this.cures.get());
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
}
