package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

/** Baton du sorcier : lance une salve de trois boules de feu (recharge d'une seconde). Butin du Sorcier infernal. */
public class ItemWarlockStaff extends Item {
    public ItemWarlockStaff(String name) {
        setMaxStackSize(1);
        setMaxDamage(256);
        setFull3D();
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        long now = world.getTotalWorldTime();
        if (now < tag.getLong("NetheryXCast")) {
            return stack;
        }
        tag.setLong("NetheryXCast", now + 20L);
        stack.setTagCompound(tag);
        player.swingItem();
        world.playSoundAtEntity(player, "mob.ghast.fireball", 0.8F, 1.2F);
        if (!world.isRemote) {
            Vec3 look = player.getLookVec();
            for (int i = 0; i < 3; i++) {
                double spread = (i - 1) * 0.08D;
                EntitySmallFireball ball = new EntitySmallFireball(world, player, look.xCoord + spread * look.zCoord, look.yCoord,
                        look.zCoord - spread * look.xCoord);
                ball.setPosition(player.posX + look.xCoord, player.posY + player.getEyeHeight() + look.yCoord * 0.5D - 0.1D,
                        player.posZ + look.zCoord);
                double length = Math.sqrt(ball.accelerationX * ball.accelerationX + ball.accelerationY * ball.accelerationY
                        + ball.accelerationZ * ball.accelerationZ);
                if (length > 0.0D) {
                    ball.accelerationX = ball.accelerationX / length * 0.15D;
                    ball.accelerationY = ball.accelerationY / length * 0.15D;
                    ball.accelerationZ = ball.accelerationZ / length * 0.15D;
                }
                world.spawnEntityInWorld(ball);
            }
            stack.damageItem(1, player);
        }
        return stack;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.epic;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        ItemBasic.addTooltip(lines, getUnlocalizedName() + ".tooltip");
    }
}
