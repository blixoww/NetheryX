package com.netheryx.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.netheryx.util.Infusion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import java.util.Optional;

/** Infusion de fonte : les blocs mines avec un outil infuse sont directement fondus (minerais, sable, etc.). */
public class InfusionSmeltingModifier extends LootModifier {
    public static final Codec<InfusionSmeltingModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance)
            .apply(instance, InfusionSmeltingModifier::new));

    public InfusionSmeltingModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || !Infusion.SMELTING.isOn(tool)) {
            return generatedLoot;
        }
        ObjectArrayList<ItemStack> smelted = new ObjectArrayList<>();
        for (ItemStack stack : generatedLoot) {
            Optional<SmeltingRecipe> recipe = context.getLevel().getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), context.getLevel());
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().getResultItem(context.getLevel().registryAccess()).copy();
                result.setCount(result.getCount() * stack.getCount());
                smelted.add(result);
            } else {
                smelted.add(stack);
            }
        }
        return smelted;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
