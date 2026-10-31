package com.netheryx.item;

import com.netheryx.entity.EntitySulfurBomb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Bombe de soufre : se lance comme une boule de neige et explose a l'impact (sans detruire de blocs). */
public class ItemSulfurBomb extends ItemBasic {
    public ItemSulfurBomb(String name) {
        super(name);
        stack(16);
        tooltip();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
        }
        world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        if (!world.isRemote) {
            world.spawnEntityInWorld(new EntitySulfurBomb(world, player));
        }
        return stack;
    }
}
