package com.netheryx.item;

import com.netheryx.entity.EntityAshWyvern;
import com.netheryx.entity.TameHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Oeuf de wyverne de cendre : pose sur un bloc, il eclot aussitot en une wyverne apprivoisee par le joueur. */
public class ItemWyvernEgg extends ItemBasic {
    public ItemWyvernEgg(String name) {
        super(name);
        stack(1);
        rarity(EnumRarity.epic);
        tooltip();
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
        if (world.isRemote) {
            return true;
        }
        x += side == 4 ? -1 : side == 5 ? 1 : 0;
        y += side == 0 ? -1 : side == 1 ? 1 : 0;
        z += side == 2 ? -1 : side == 3 ? 1 : 0;
        EntityAshWyvern wyvern = new EntityAshWyvern(world);
        wyvern.setLocationAndAngles(x + 0.5D, y, z + 0.5D, player.rotationYaw + 180.0F, 0.0F);
        TameHelper.tame(wyvern, player);
        wyvern.setHealth(wyvern.getMaxHealth());
        if (stack.hasDisplayName()) {
            wyvern.setCustomNameTag(stack.getDisplayName());
        }
        world.spawnEntityInWorld(wyvern);
        world.playSoundAtEntity(wyvern, "mob.enderdragon.growl", 0.8F, 1.6F);
        for (int i = 0; i < 20; i++) {
            world.spawnParticle("largesmoke", x + world.rand.nextDouble(), y + world.rand.nextDouble() * 2.0D, z + world.rand.nextDouble(), 0, 0.05D, 0);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
        }
        return true;
    }
}
