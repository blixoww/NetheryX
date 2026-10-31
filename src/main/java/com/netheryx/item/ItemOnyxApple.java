package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Pomme en onyx : se mange meme rassasie ; resistance au feu, resistance et regeneration. */
public class ItemOnyxApple extends ItemFood {
    public ItemOnyxApple(String name) {
        super(6, 1.2F, false);
        setAlwaysEdible();
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 2400, 0));
            player.addPotionEffect(new PotionEffect(Potion.resistance.id, 1200, 0));
            player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 1));
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.rare;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
