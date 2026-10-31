package com.netheryx.enchantment;

import com.netheryx.NetheryX;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

/**
 * Enchantements de l'autel des ames : jamais proposes par la table d'enchantement ni trouves dans les butins vanilla.
 * <ul>
 *     <li>foulee de braise (bottes) : plus rapide sur le sol du Nether, insensible aux blocs brulants ;</li>
 *     <li>moisson des ames (armes) : les creatures tuees lachent plus d'experience, parfois de la poussiere d'ame ;</li>
 *     <li>ancrage (plastron, jambieres) : resistance au recul.</li>
 * </ul>
 */
public class EnchantmentNether extends Enchantment {
    public enum Target {
        BOOTS, WEAPONS, CHEST_LEGS
    }

    private final int maxLevel;
    private final Target target;

    public EnchantmentNether(int id, String name, int maxLevel, Target target) {
        super(id, 2, target == Target.BOOTS ? EnumEnchantmentType.armor_feet : target == Target.WEAPONS ? EnumEnchantmentType.weapon
                : EnumEnchantmentType.armor);
        this.maxLevel = maxLevel;
        this.target = target;
        setName(NetheryX.MOD_ID + "." + name);
    }

    public Target getTarget() {
        return this.target;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public int getMinEnchantability(int level) {
        return 15 + level * 10;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 30;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        switch (this.target) {
            case BOOTS:
                return stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).armorType == 3;
            case WEAPONS:
                return stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemAxe;
            default:
                return stack.getItem() instanceof ItemArmor && (((ItemArmor) stack.getItem()).armorType == 1
                        || ((ItemArmor) stack.getItem()).armorType == 2);
        }
    }

    /** Reserve a l'autel des ames : la table d'enchantement ne le propose jamais. */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }
}
