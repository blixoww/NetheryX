package com.netheryx.recipe;

import com.google.gson.JsonObject;
import com.netheryx.registry.ModRecipes;
import com.netheryx.util.Infusion;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;

/**
 * Recette de table de forgeron : gabarit d'infusion + n'importe quel objet a durabilite + ingredient
 * -> le meme objet portant l'infusion (un seul type d'infusion par objet).
 */
public class InfusionRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    private final Ingredient template;
    private final Ingredient addition;
    private final Infusion infusion;

    public InfusionRecipe(ResourceLocation id, Ingredient template, Ingredient addition, Infusion infusion) {
        this.id = id;
        this.template = template;
        this.addition = addition;
        this.infusion = infusion;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return stack.isDamageableItem() && stack.getCount() == 1 && Infusion.of(stack) != this.infusion;
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return this.isTemplateIngredient(container.getItem(0)) && this.isBaseIngredient(container.getItem(1))
                && this.isAdditionIngredient(container.getItem(2));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return this.infusion.applyTo(container.getItem(1).copy());
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    public Ingredient template() {
        return this.template;
    }

    public Ingredient addition() {
        return this.addition;
    }

    public Infusion infusion() {
        return this.infusion;
    }

    @Override
    public boolean isIncomplete() {
        return this.template.isEmpty() || this.addition.isEmpty();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.INFUSION.get();
    }

    public static class Serializer implements RecipeSerializer<InfusionRecipe> {
        @Override
        public InfusionRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient template = Ingredient.fromJson(GsonHelper.getNonNull(json, "template"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getNonNull(json, "addition"));
            Infusion infusion = Infusion.byId(GsonHelper.getAsString(json, "infusion"));
            if (infusion == null) {
                throw new IllegalArgumentException("Infusion inconnue dans " + id);
            }
            return new InfusionRecipe(id, template, addition, infusion);
        }

        @Override
        public InfusionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new InfusionRecipe(id, Ingredient.fromNetwork(buf), Ingredient.fromNetwork(buf), buf.readEnum(Infusion.class));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, InfusionRecipe recipe) {
            recipe.template.toNetwork(buf);
            recipe.addition.toNetwork(buf);
            buf.writeEnum(recipe.infusion);
        }
    }
}
