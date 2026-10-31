package com.netheryx.entity;

import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

/** Marchand piglin : commerce en or et en onyx (graines rares, gabarits, objets du Nether). */
public class EntityPiglinMerchant extends EntityVillager {
    private MerchantRecipeList trades;

    public EntityPiglinMerchant(World world) {
        super(world);
        this.isImmuneToFire = true;
    }

    @Override
    public MerchantRecipeList getRecipes(EntityPlayer player) {
        if (this.trades == null) {
            this.trades = new MerchantRecipeList();
            add(new ItemStack(Items.gold_ingot, 4), new ItemStack(ModItems.blazePepperSeeds, 4));
            add(new ItemStack(Items.gold_ingot, 4), new ItemStack(ModItems.cinderWheatSeeds, 4));
            add(new ItemStack(Items.gold_ingot, 3), new ItemStack(ModItems.fireBerries, 6));
            add(new ItemStack(Items.gold_ingot, 6), new ItemStack(Item.getItemFromBlock(ModBlocks.glowcap), 4));
            add(new ItemStack(ModItems.onyx, 3), new ItemStack(ModItems.onyxInfusionTemplate));
            add(new ItemStack(ModItems.onyx, 2), new ItemStack(ModItems.soulCompass));
            add(new ItemStack(ModItems.onyx, 2), new ItemStack(ModItems.recallStone));
            add(new ItemStack(Items.gold_ingot, 8), new ItemStack(ModItems.glimmerElixir, 2));
            add(new ItemStack(Items.gold_ingot, 5), new ItemStack(ModItems.sulfurBomb, 4));
            add(new ItemStack(ModItems.magmaShell, 4), new ItemStack(Items.gold_ingot, 3));
            add(new ItemStack(ModItems.basaltCarapace, 4), new ItemStack(Items.gold_ingot, 3));
        }
        return this.trades;
    }

    @SuppressWarnings("unchecked")
    private void add(ItemStack price, ItemStack sale) {
        MerchantRecipe recipe = new MerchantRecipe(price, sale);
        recipe.func_82783_a(1000); // presque illimite
        this.trades.add(recipe);
    }

    @Override
    public EntityVillager createChild(EntityAgeable partner) {
        return new EntityPiglinMerchant(this.worldObj);
    }

    @Override
    protected String getLivingSound() {
        return "mob.zombiepig.zpig";
    }

    @Override
    protected String getHurtSound() {
        return "mob.zombiepig.zpighurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.zombiepig.zpigdeath";
    }
}
