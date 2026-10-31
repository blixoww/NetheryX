package com.netheryx.item;

import com.netheryx.entity.EntityAshWyvern;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

/**
 * Cristal de wyverne : range sa wyverne pour l'emporter partout. Clic droit sur sa wyverne apprivoisee : elle entre
 * dans le cristal (avec son nom, sa vie et son maitre). Clic droit sur un bloc : elle en ressort. Au sol, un cristal
 * plein ne disparait jamais, resiste au feu et aux explosions, et remonte du vide au point d'apparition.
 */
public class ItemWyvernCrystal extends ItemBasic {
    private static final String TAG = "StoredWyvern";

    public ItemWyvernCrystal(String name) {
        super(name);
        stack(1);
        rarity(EnumRarity.epic);
    }

    public static boolean isFull(ItemStack stack) {
        return stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack ignored, EntityPlayer player, EntityLivingBase target) {
        // En creatif, la pile recue est une copie : on travaille sur celle de la main
        ItemStack stack = player.getCurrentEquippedItem();
        if (!(target instanceof EntityAshWyvern) || stack == null || stack.getItem() != this || isFull(stack)) {
            return false;
        }
        EntityAshWyvern wyvern = (EntityAshWyvern) target;
        if (player.worldObj.isRemote) {
            return true;
        }
        if (!wyvern.isTamed() || wyvern.getOwner() != player) {
            player.addChatMessage(new ChatComponentTranslation("message.netheryx.wyvern_crystal_not_yours"));
            return true;
        }
        if (wyvern.riddenByEntity != null) {
            wyvern.riddenByEntity.mountEntity(null);
        }
        NBTTagCompound data = new NBTTagCompound();
        if (!wyvern.writeToNBTOptional(data)) {
            return false;
        }
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setTag(TAG, data);
        for (int i = 0; i < 40; i++) {
            wyvern.worldObj.spawnParticle("portal", wyvern.posX + wyvern.worldObj.rand.nextGaussian(), wyvern.posY + 1.0D,
                    wyvern.posZ + wyvern.worldObj.rand.nextGaussian(), 0, 0, 0);
        }
        player.worldObj.playSoundAtEntity(player, "mob.endermen.portal", 0.8F, 1.4F);
        wyvern.setDead();
        return true;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
        if (!isFull(stack)) {
            return false;
        }
        if (!world.isRemote) {
            x += side == 4 ? -1 : side == 5 ? 1 : 0;
            y += side == 0 ? -1 : side == 1 ? 1 : 0;
            z += side == 2 ? -1 : side == 3 ? 1 : 0;
            if (release(world, stack, x + 0.5D, y, z + 0.5D, player.rotationYaw + 180.0F)) {
                world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.glass", 1.0F, 0.7F);
            }
        }
        return true;
    }

    /** Fait sortir la wyverne du cristal ; le cristal redevient vide. */
    static boolean release(World world, ItemStack stack, double x, double y, double z, float yaw) {
        NBTTagCompound data = stack.getTagCompound().getCompoundTag(TAG);
        Entity entity = EntityList.createEntityFromNBT(data, world);
        if (entity == null) {
            return false;
        }
        entity.setLocationAndAngles(x, y, z, yaw, 0.0F);
        entity.fallDistance = 0.0F;
        world.spawnEntityInWorld(entity);
        stack.getTagCompound().removeTag(TAG);
        if (entity instanceof EntityAshWyvern) {
            ((EntityAshWyvern) entity).stand();
        }
        return true;
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return isFull(stack) ? Integer.MAX_VALUE : super.getEntityLifespan(stack, world);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return isFull(stack);
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        SafeItem item = new SafeItem(world, location.posX, location.posY, location.posZ, stack);
        item.motionX = location.motionX;
        item.motionY = location.motionY;
        item.motionZ = location.motionZ;
        item.delayBeforeCanPickup = 40;
        return item;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack, int pass) {
        return isFull(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        if (!isFull(stack)) {
            addTooltip(lines, "item.netheryx.wyvern_crystal.empty");
            return;
        }
        NBTTagCompound data = stack.getTagCompound().getCompoundTag(TAG);
        String name = data.getString("CustomName");
        addTooltip(lines, "item.netheryx.wyvern_crystal.full", name.isEmpty() ? StatCollector.translateToLocal("entity.netheryx.ash_wyvern.name") : name);
        addTooltip(lines, "item.netheryx.wyvern_crystal.health", Math.round(data.getFloat("HealF")));
        addTooltip(lines, "item.netheryx.wyvern_crystal.release");
    }

    /** Cristal plein au sol : indestructible, et remonte du vide. */
    public static class SafeItem extends EntityItem {
        public SafeItem(World world) {
            super(world);
            this.isImmuneToFire = true;
        }

        public SafeItem(World world, double x, double y, double z, ItemStack stack) {
            super(world, x, y, z, stack);
            this.isImmuneToFire = true;
        }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            return false;
        }

        @Override
        public void onUpdate() {
            if (this.posY < 1.0D && !this.worldObj.isRemote) {
                ChunkCoordinates spawn = this.worldObj.getSpawnPoint();
                setPosition(spawn.posX + 0.5D, this.worldObj.getTopSolidOrLiquidBlock(spawn.posX, spawn.posZ) + 1.0D, spawn.posZ + 0.5D);
                this.motionX = this.motionY = this.motionZ = 0.0D;
            }
            super.onUpdate();
        }
    }
}
