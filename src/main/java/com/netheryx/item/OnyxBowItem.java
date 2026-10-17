package com.netheryx.item;

import com.netheryx.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Arc en onyx : fleches 50 % plus puissantes et enflammees, tres grande durabilite, enchantable comme l'onyx. */
public class OnyxBowItem extends BowItem {
    public OnyxBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        arrow.setBaseDamage(arrow.getBaseDamage() * 1.5D + 1.0D);
        arrow.setSecondsOnFire(100);
        return arrow;
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(ModItems.ONYX.get()) || super.isValidRepairItem(stack, repair);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.onyx_bow.tooltip").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
