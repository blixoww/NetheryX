package com.netheryx.compat.jei;

import com.netheryx.NetheryX;
import com.netheryx.registry.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Categorie JEI des infusions d'onyx (table de forgeron) : gabarit + objet + ingredient -> objet infuse. */
public class InfusionCategory implements IRecipeCategory<InfusionJeiRecipe> {
    public static final RecipeType<InfusionJeiRecipe> TYPE = RecipeType.create(NetheryX.MOD_ID, "infusion", InfusionJeiRecipe.class);
    private static final int WIDTH = 150;
    private static final int HEIGHT = 46;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public InfusionCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.ONYX_INFUSION_TEMPLATE.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<InfusionJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.netheryx.infusion");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InfusionJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 6).addIngredients(recipe.template()).setBackground(this.slot, -1, -1);
        IRecipeSlotBuilder base = builder.addSlot(RecipeIngredientRole.INPUT, 28, 6).addItemStacks(recipe.bases())
                .setBackground(this.slot, -1, -1);
        builder.addSlot(RecipeIngredientRole.INPUT, 50, 6).addIngredients(recipe.addition()).setBackground(this.slot, -1, -1);
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 6).addItemStacks(recipe.outputs())
                .setBackground(this.slot, -1, -1);
        builder.createFocusLink(base, output);
    }

    @Override
    public void draw(InfusionJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.fill(74, 13, 98, 15, 0xFF8B8B8B);
        for (int i = 0; i < 5; i++) {
            graphics.fill(98 + i, 9 + i, 99 + i, 19 - i, 0xFF8B8B8B);
        }
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("infusion.netheryx." + recipe.infusion().id()),
                0, 30, 0xFF5A2A8A, false);
    }
}
