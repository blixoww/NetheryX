package com.netheryx.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Enchantement propre au Nether : il ne s'obtient ni a la table d'enchantement ni dans les coffres,
 * uniquement a l'Autel des ames (et chez le marchand piglin, en livre).
 */
public class NetherEnchantment extends Enchantment {
    private final int maxLevel;

    public NetherEnchantment(Rarity rarity, EnchantmentCategory category, int maxLevel, EquipmentSlot... slots) {
        super(rarity, category, slots);
        this.maxLevel = maxLevel;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 30;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }
}
