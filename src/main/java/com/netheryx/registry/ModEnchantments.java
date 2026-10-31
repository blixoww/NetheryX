package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXConfig;
import com.netheryx.enchantment.EnchantmentNether;
import net.minecraft.enchantment.Enchantment;

/** Les trois enchantements de l'autel des ames (identifiants a partir de {@code enchantmentIdStart}). */
public final class ModEnchantments {
    public static Enchantment emberStride;
    public static Enchantment soulHarvest;
    public static Enchantment anchoring;

    private ModEnchantments() {
    }

    public static void register() {
        int id = NetheryXConfig.enchantmentIdStart;
        emberStride = create(id, "ember_stride", 2, EnchantmentNether.Target.BOOTS);
        soulHarvest = create(id + 1, "soul_harvest", 3, EnchantmentNether.Target.WEAPONS);
        anchoring = create(id + 2, "anchoring", 2, EnchantmentNether.Target.CHEST_LEGS);
    }

    private static Enchantment create(int id, String name, int maxLevel, EnchantmentNether.Target target) {
        if (id < 0 || id >= Enchantment.enchantmentsList.length || Enchantment.enchantmentsList[id] != null) {
            NetheryX.LOG.error("Identifiant d'enchantement {} deja pris : changez enchantmentIdStart dans la configuration", id);
            return null;
        }
        return new EnchantmentNether(id, name, maxLevel, target);
    }
}
