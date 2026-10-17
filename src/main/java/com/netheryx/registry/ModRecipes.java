package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.recipe.InfusionRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Types de recettes du mod. */
public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, NetheryX.MOD_ID);

    public static final RegistryObject<RecipeSerializer<InfusionRecipe>> INFUSION = SERIALIZERS.register("infusion", InfusionRecipe.Serializer::new);

    private ModRecipes() {
    }
}
