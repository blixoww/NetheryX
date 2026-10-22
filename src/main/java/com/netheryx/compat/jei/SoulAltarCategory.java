package com.netheryx.compat.jei;

import com.netheryx.NetheryX;
import com.netheryx.blockentity.SoulAltarBlockEntity;
import com.netheryx.registry.ModBlocks;
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

/**
 * Categorie JEI de l'Autel des ames : objet (ou livre) + offrande, avec le cout en experience et le nombre de
 * flammes des ames requis.
 */
public class SoulAltarCategory implements IRecipeCategory<SoulAltarJeiRecipe> {
    public static final RecipeType<SoulAltarJeiRecipe> TYPE = RecipeType.create(NetheryX.MOD_ID, "soul_altar", SoulAltarJeiRecipe.class);
    private static final int WIDTH = 150;
    private static final int HEIGHT = 58;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public SoulAltarCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SOUL_ALTAR.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<SoulAltarJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.netheryx.soul_altar");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SoulAltarJeiRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 6, 6).addItemStacks(recipe.inputs())
                .setBackground(this.slot, -1, -1);
        builder.addSlot(RecipeIngredientRole.INPUT, 34, 6).addItemStack(recipe.offering()).setBackground(this.slot, -1, -1);
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 96, 6).addItemStacks(recipe.outputs())
                .setBackground(this.slot, -1, -1);
        builder.createFocusLink(input, output);
    }

    @Override
    public void draw(SoulAltarJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        // Fleche
        graphics.fill(58, 13, 82, 15, 0xFF8B8B8B);
        for (int i = 0; i < 5; i++) {
            graphics.fill(82 + i, 9 + i, 83 + i, 19 - i, 0xFF8B8B8B);
        }
        graphics.drawString(font, Component.translatable("jei.netheryx.soul_altar.cost", SoulAltarBlockEntity.LEVELS_PER_ENCHANT_LEVEL),
                0, 30, 0xFF5A2A8A, false);
        graphics.drawString(font, Component.translatable("jei.netheryx.soul_altar.flames", SoulAltarBlockEntity.REQUIRED_FLAMES),
                0, 42, 0xFF2A6A7A, false);
    }
}
