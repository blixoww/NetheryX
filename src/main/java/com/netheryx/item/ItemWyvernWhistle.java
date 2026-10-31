package com.netheryx.item;

import com.netheryx.entity.EntityAshWyvern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;

/** Sifflet de wyverne : rappelle aupres du joueur toutes ses wyvernes chargees dans la dimension (assises comprises). */
public class ItemWyvernWhistle extends ItemBasic {
    public ItemWyvernWhistle(String name) {
        super(name);
        stack(1);
        rarity(EnumRarity.rare);
        tooltip();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        world.playSoundAtEntity(player, "mob.enderdragon.growl", 0.5F, 2.0F);
        if (world.isRemote) {
            return stack;
        }
        int count = 0;
        for (Object object : new ArrayList<Object>(world.loadedEntityList)) {
            if (object instanceof EntityAshWyvern) {
                EntityAshWyvern wyvern = (EntityAshWyvern) object;
                if (wyvern.isTamed() && wyvern.getOwner() == player && wyvern.riddenByEntity == null) {
                    wyvern.stand();
                    wyvern.setLocationAndAngles(player.posX + world.rand.nextGaussian() * 2.0D, player.posY + 1.0D,
                            player.posZ + world.rand.nextGaussian() * 2.0D, wyvern.rotationYaw, 0.0F);
                    wyvern.getNavigator().clearPathEntity();
                    count++;
                }
            }
        }
        player.addChatMessage(new ChatComponentTranslation(count > 0 ? "message.netheryx.whistle.called" : "message.netheryx.whistle.none", count));
        return stack;
    }

}
