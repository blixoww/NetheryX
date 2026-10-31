package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import com.netheryx.registry.ModEntities;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

/** Oeufs d'apparition de toutes les creatures du mod (une meta par creature, couleurs de {@link ModEntities#EGGS}). */
public class ItemNetheryXEgg extends Item {
    @SideOnly(Side.CLIENT)
    private IIcon overlay;

    public ItemNetheryXEgg() {
        setHasSubtypes(true);
        setUnlocalizedName(NetheryX.MOD_ID + ".spawn_egg");
        setTextureName("spawn_egg");
        setCreativeTab(NetheryXTab.TAB);
    }

    private static ModEntities.Info info(ItemStack stack) {
        int meta = stack.getItemDamage();
        return meta >= 0 && meta < ModEntities.EGGS.size() ? ModEntities.EGGS.get(meta) : null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        ModEntities.Info info = info(stack);
        String name = info == null ? "?" : StatCollector.translateToLocal("entity.netheryx." + info.name + ".name");
        return StatCollector.translateToLocalFormatted("item.netheryx.spawn_egg.name", name);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
        if (world.isRemote) {
            return true;
        }
        ModEntities.Info info = info(stack);
        if (info == null) {
            return false;
        }
        x += net.minecraft.util.Facing.offsetsXForSide[side];
        y += net.minecraft.util.Facing.offsetsYForSide[side];
        z += net.minecraft.util.Facing.offsetsZForSide[side];
        Entity entity = EntityList.createEntityByName(NetheryX.MOD_ID + "." + info.name, world);
        if (entity == null) {
            return false;
        }
        entity.setLocationAndAngles(x + 0.5D, y, z + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.rotationYawHead = living.rotationYaw;
            living.renderYawOffset = living.rotationYaw;
            living.onSpawnWithEgg((IEntityLivingData) null);
            if (stack.hasDisplayName()) {
                living.setCustomNameTag(stack.getDisplayName());
            }
        }
        world.spawnEntityInWorld(entity);
        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).playLivingSound();
        }
        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        ModEntities.Info info = info(stack);
        return info == null ? 0xFFFFFF : pass == 0 ? info.primary : info.secondary;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
        return pass > 0 ? this.overlay : this.itemIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.itemIcon = register.registerIcon("spawn_egg");
        this.overlay = register.registerIcon("spawn_egg_overlay");
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < ModEntities.EGGS.size(); i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }
}
