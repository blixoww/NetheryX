package com.netheryx.util;

import com.netheryx.entity.SulfurBombEntity;
import com.netheryx.registry.ModEffects;
import com.netheryx.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraft.Util;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.netheryx.registry.ModBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.DispenserBlock;

/** Integrations avec les mecaniques vanilla (composteur, distributeur). Appele pendant le common setup. */
public final class CompatSetup {
    private CompatSetup() {
    }

    public static void registerVanillaHooks() {
        ComposterBlock.COMPOSTABLES.put(ModItems.FIRE_BERRIES.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.BLAZE_PEPPER_SEEDS.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.BLAZE_PEPPER.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.SOUL_TUBER.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.BAKED_SOUL_TUBER.get(), 0.85F);
        ComposterBlock.COMPOSTABLES.put(com.netheryx.registry.ModBlocks.EMBER_FUNGUS.get().asItem(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(com.netheryx.registry.ModBlocks.EMBER_ROOTS.get().asItem(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(com.netheryx.registry.ModBlocks.EMBER_WART_BLOCK.get().asItem(), 0.85F);

        ComposterBlock.COMPOSTABLES.put(ModItems.CINDER_WHEAT.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.CINDER_WHEAT_SEEDS.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.GLOWCAP.get(), 0.65F);
        for (var flower : java.util.List.of(ModBlocks.NETHER_GRASS, ModBlocks.EMBER_BLOOM, ModBlocks.ASH_DAISY,
                ModBlocks.CRIMSON_BELLFLOWER, ModBlocks.SULFUR_TULIP, ModBlocks.SOUL_LILY)) {
            ComposterBlock.COMPOSTABLES.put(flower.get().asItem(), 0.65F);
        }

        // Plantes en pot (le pot vanilla doit connaitre chaque plante)
        FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
        pot.addPlant(ModBlocks.EMBER_BLOOM.getId(), ModBlocks.POTTED_EMBER_BLOOM);
        pot.addPlant(ModBlocks.ASH_DAISY.getId(), ModBlocks.POTTED_ASH_DAISY);
        pot.addPlant(ModBlocks.CRIMSON_BELLFLOWER.getId(), ModBlocks.POTTED_CRIMSON_BELLFLOWER);
        pot.addPlant(ModBlocks.SULFUR_TULIP.getId(), ModBlocks.POTTED_SULFUR_TULIP);
        pot.addPlant(ModBlocks.SOUL_LILY.getId(), ModBlocks.POTTED_SOUL_LILY);
        pot.addPlant(ModBlocks.GLOWCAP.getId(), ModBlocks.POTTED_GLOWCAP);
        pot.addPlant(ModBlocks.EMBER_FUNGUS.getId(), ModBlocks.POTTED_EMBER_FUNGUS);
        pot.addPlant(ModBlocks.WITHERED_FUNGUS.getId(), ModBlocks.POTTED_WITHERED_FUNGUS);

        // 1.2.0 : compost, potions
        ComposterBlock.COMPOSTABLES.put(ModItems.WARTFRUIT.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.MARROW_SPROUTS.get().asItem(), 0.5F);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.MARROW_VINES.get().asItem(), 0.5F);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.MARROW_CAP.get().asItem(), 0.85F);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.WART_BULB.get().asItem(), 0.65F);
        registerBrewing();

        DispenserBlock.registerBehavior(ModItems.SULFUR_BOMB.get(), new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
                return Util.make(new SulfurBombEntity(level, position.x(), position.y(), position.z()),
                        bomb -> bomb.setItem(stack));
            }
        });
    }

    /** Potion de plongee en lave : potion etrange + ecaille de poisson de magme, prolongee a la redstone. */
    private static void registerBrewing() {
        for (Item container : new Item[]{Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION}) {
            BrewingRecipeRegistry.addRecipe(StrictNBTIngredient.of(PotionUtils.setPotion(new ItemStack(container), Potions.AWKWARD)),
                    Ingredient.of(ModItems.MAGMAFISH_SCALE.get()),
                    PotionUtils.setPotion(new ItemStack(container), ModEffects.LAVA_DIVING.get()));
            BrewingRecipeRegistry.addRecipe(StrictNBTIngredient.of(PotionUtils.setPotion(new ItemStack(container), ModEffects.LAVA_DIVING.get())),
                    Ingredient.of(Items.REDSTONE),
                    PotionUtils.setPotion(new ItemStack(container), ModEffects.LONG_LAVA_DIVING.get()));
        }
    }
}
