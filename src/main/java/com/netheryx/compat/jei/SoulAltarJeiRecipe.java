package com.netheryx.compat.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * Une offrande de l'Autel des ames pour JEI : objets acceptes (exemples), offrande, et les memes objets une fois
 * enchantes (au niveau 1).
 */
public record SoulAltarJeiRecipe(List<ItemStack> inputs, ItemStack offering, Enchantment enchantment, List<ItemStack> outputs) {
}
