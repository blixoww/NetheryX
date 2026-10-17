package com.netheryx.item;

import com.netheryx.registry.ModArmorMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OnyxArmorItem extends ArmorItem {
    public OnyxArmorItem(Type type, Properties properties) {
        super(ModArmorMaterials.ONYX, type, properties);
    }

    /** @return {@code true} si l'entite porte les quatre pieces d'armure en Onyx. */
    public static boolean hasFullSet(LivingEntity entity) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof OnyxArmorItem)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.onyx_armor.set_bonus").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
