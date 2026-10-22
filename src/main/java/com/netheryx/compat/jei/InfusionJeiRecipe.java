package com.netheryx.compat.jei;

import com.netheryx.util.Infusion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/** Une infusion d'onyx pour JEI : gabarit, objets d'exemple, ingredient, et les memes objets une fois infuses. */
public record InfusionJeiRecipe(Ingredient template, List<ItemStack> bases, Ingredient addition, Infusion infusion,
                                List<ItemStack> outputs) {
}
