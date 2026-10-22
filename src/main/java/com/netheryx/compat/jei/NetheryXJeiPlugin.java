package com.netheryx.compat.jei;

import com.netheryx.NetheryX;
import com.netheryx.blockentity.SoulAltarBlockEntity;
import com.netheryx.recipe.InfusionRecipe;
import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration JEI (facultative : charge seulement si JEI est installe). Ajoute les categories « Autel des ames » et
 * « Infusion d'onyx », retire les infusions de la categorie vanilla de la table de forgeron (elles n'y ont pas de
 * resultat affichable), et des pages d'information sur les objets qui ne se comprennent pas d'apres leur recette.
 */
@JeiPlugin
public class NetheryXJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(NetheryX.MOD_ID, "jei");
    /** Objets sans recette ou a usage particulier : une page d'information chacun. */
    private static final String[] INFO = {"ash_wyvern_egg", "wyvern_whistle", "wyvern_crystal", "sealed_heart", "basalt_heartstone",
            "ember_fig", "withered_plum", "fire_veil", "onyx_infusion_template", "soul_altar", "sentinel_core"};

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new SoulAltarCategory(guiHelper), new InfusionCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(SoulAltarCategory.TYPE, altarRecipes());
        registration.addRecipes(InfusionCategory.TYPE, infusionRecipes());
        for (String name : INFO) {
            Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(NetheryX.MOD_ID, name));
            if (item != null && item != Items.AIR) {
                registration.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM_STACK, Component.translatable("jei.netheryx.info." + name));
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SOUL_ALTAR.get()), SoulAltarCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.SMITHING_TABLE), InfusionCategory.TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        List<SmithingRecipe> infusions = new ArrayList<>(smithingInfusions());
        if (!infusions.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(RecipeTypes.SMITHING, infusions);
        }
    }

    private static List<SoulAltarJeiRecipe> altarRecipes() {
        List<SoulAltarJeiRecipe> recipes = new ArrayList<>();
        for (SoulAltarBlockEntity.Offering offering : SoulAltarBlockEntity.OFFERINGS) {
            Enchantment enchantment = offering.enchantment().get();
            List<ItemStack> inputs = new ArrayList<>();
            List<ItemStack> outputs = new ArrayList<>();
            for (ItemLike example : examples(offering.targets())) {
                ItemStack stack = new ItemStack(example);
                if (stack.is(Items.BOOK)) {
                    inputs.add(stack);
                    outputs.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, 1)));
                } else if (enchantment.canEnchant(stack)) {
                    inputs.add(stack);
                    ItemStack result = stack.copy();
                    result.enchant(enchantment, 1);
                    outputs.add(result);
                }
            }
            recipes.add(new SoulAltarJeiRecipe(inputs, new ItemStack(offering.item().get(), offering.count()), enchantment, outputs));
        }
        return recipes;
    }

    private static List<ItemLike> examples(String targets) {
        return switch (targets) {
            case "boots" -> List.of(ModItems.ONYX_BOOTS.get(), Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS, Items.BOOK);
            case "weapons" -> List.of(ModItems.ONYX_SWORD.get(), Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD, Items.BOOK);
            default -> List.of(ModItems.ONYX_CHESTPLATE.get(), ModItems.ONYX_LEGGINGS.get(), Items.DIAMOND_CHESTPLATE,
                    Items.NETHERITE_LEGGINGS, Items.BOOK);
        };
    }

    private static List<InfusionRecipe> smithingInfusions() {
        List<InfusionRecipe> infusions = new ArrayList<>();
        if (Minecraft.getInstance().level == null) {
            return infusions;
        }
        for (SmithingRecipe recipe : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING)) {
            if (recipe instanceof InfusionRecipe infusion) {
                infusions.add(infusion);
            }
        }
        return infusions;
    }

    private static List<InfusionJeiRecipe> infusionRecipes() {
        List<ItemLike> examples = List.of(ModItems.ONYX_PICKAXE.get(), ModItems.ONYX_SWORD.get(), ModItems.ONYX_CHESTPLATE.get(),
                Items.DIAMOND_PICKAXE, Items.NETHERITE_AXE, Items.IRON_SHOVEL, Items.BOW, Items.DIAMOND_BOOTS);
        List<InfusionJeiRecipe> recipes = new ArrayList<>();
        for (InfusionRecipe recipe : smithingInfusions()) {
            List<ItemStack> bases = new ArrayList<>();
            List<ItemStack> outputs = new ArrayList<>();
            for (ItemLike example : examples) {
                ItemStack stack = new ItemStack(example);
                if (recipe.isBaseIngredient(stack)) {
                    bases.add(stack);
                    outputs.add(recipe.infusion().applyTo(stack.copy()));
                }
            }
            recipes.add(new InfusionJeiRecipe(recipe.template(), bases, recipe.addition(), recipe.infusion(), outputs));
        }
        return recipes;
    }
}
