package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import com.netheryx.registry.ModItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;

import java.util.List;

/** Arc en onyx : fleches 50 % plus puissantes et enflammees, tres grande durabilite, repare avec de l'onyx. */
public class ItemOnyxBow extends ItemBow {
    @SideOnly(Side.CLIENT)
    private IIcon[] pulling;

    public ItemOnyxBow(String name) {
        setMaxDamage(2400);
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining) {
        int charge = getMaxItemUseDuration(stack) - remaining;
        ArrowLooseEvent event = new ArrowLooseEvent(player, stack, charge);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        charge = event.charge;
        boolean infinite = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) > 0;
        if (!infinite && !player.inventory.hasItem(Items.arrow)) {
            return;
        }
        float power = charge / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        if (power < 0.1D) {
            return;
        }
        power = Math.min(power, 1.0F);
        EntityArrow arrow = new EntityArrow(world, player, power * 2.0F);
        if (power == 1.0F) {
            arrow.setIsCritical(true);
        }
        arrow.setDamage(arrow.getDamage() * 1.5D);
        int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
        if (powerLevel > 0) {
            arrow.setDamage(arrow.getDamage() + powerLevel * 0.5D + 0.5D);
        }
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
        if (punch > 0) {
            arrow.setKnockbackStrength(punch);
        }
        arrow.setFire(100);
        stack.damageItem(1, player);
        world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        if (infinite) {
            arrow.canBePickedUp = 2;
        } else {
            player.inventory.consumeInventoryItem(Items.arrow);
        }
        if (!world.isRemote) {
            world.spawnEntityInWorld(arrow);
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack stack, ItemStack material) {
        return material.getItem() == ModItems.onyx || super.getIsRepairable(stack, material);
    }

    @Override
    public int getItemEnchantability() {
        return 12;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.itemIcon = register.registerIcon(getIconString());
        this.pulling = new IIcon[3];
        for (int i = 0; i < 3; i++) {
            this.pulling[i] = register.registerIcon(getIconString() + "_pulling_" + i);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass, EntityPlayer player, ItemStack using, int remaining) {
        if (using == null || using != stack) {
            return this.itemIcon;
        }
        int charge = stack.getMaxItemUseDuration() - remaining;
        return charge >= 18 ? this.pulling[2] : charge > 13 ? this.pulling[1] : charge > 0 ? this.pulling[0] : this.itemIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        ItemBasic.addTooltip(lines, getUnlocalizedName() + ".tooltip");
    }
}
