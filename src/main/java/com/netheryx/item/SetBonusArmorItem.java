package com.netheryx.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Piece d'armure thematique dont l'ensemble complet donne un bonus (gere dans {@code ArmorSetEffects}).
 * L'infobulle affiche {@code item.netheryx.<materiau>_armor.set_bonus}.
 */
public class SetBonusArmorItem extends ArmorItem {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final String bonusKey;

    public SetBonusArmorItem(ArmorMaterial material, Type type, Properties properties, String setName) {
        super(material, type, properties);
        this.bonusKey = "item.netheryx." + setName + "_armor.set_bonus";
    }

    /** Vrai si l'entite porte quatre pieces du materiau donne. */
    public static boolean hasFullSet(LivingEntity entity, ArmorMaterial material) {
        for (EquipmentSlot slot : SLOTS) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof ArmorItem armor) || armor.getMaterial() != material) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.bonusKey).withStyle(ChatFormatting.GOLD));
    }
}
