package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/**
 * Lezard de cendre : monture du Nether. On l'apprivoise avec des baies de feu, on le selle, puis on le dirige comme
 * un cheval. Son cavalier ne craint pas le feu tant qu'il est en selle.
 */
public class EntityCinderLizard extends EntityTameable implements IAnimatedMob {
    public EntityCinderLizard(World world) {
        super(world);
        setSize(1.3F, 1.0F);
        this.isImmuneToFire = true;
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.4D));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.1D, 10.0F, 3.0F));
        this.tasks.addTask(5, new EntityAIWander(this, 0.9D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(20, Byte.valueOf((byte) 0));
    }

    public boolean isSaddled() {
        return this.dataWatcher.getWatchableObjectByte(20) == 1;
    }

    public void setSaddled(boolean saddled) {
        this.dataWatcher.updateObject(20, Byte.valueOf((byte) (saddled ? 1 : 0)));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
    }

    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    @Override
    public boolean interact(EntityPlayer player) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (!isTamed() && stack != null && stack.getItem() == ModItems.fireBerries) {
            if (!player.capabilities.isCreativeMode) {
                stack.stackSize--;
            }
            if (!this.worldObj.isRemote) {
                if (this.rand.nextInt(3) == 0) {
                    TameHelper.tame(this, player);
                    this.aiSit.setSitting(false);
                } else {
                    this.worldObj.setEntityState(this, (byte) 6);
                }
            }
            return true;
        }
        if (isTamed() && !isSaddled() && stack != null && stack.getItem() == Items.saddle) {
            if (!this.worldObj.isRemote) {
                setSaddled(true);
                playSound("mob.horse.leather", 0.5F, 1.0F);
                if (!player.capabilities.isCreativeMode) {
                    stack.stackSize--;
                }
            }
            return true;
        }
        if (isTamed() && isSaddled() && this.riddenByEntity == null && !player.isSneaking()) {
            if (!this.worldObj.isRemote) {
                this.aiSit.setSitting(false);
                player.mountEntity(this);
            }
            return true;
        }
        if (isTamed() && getOwner() == player && player.isSneaking() && !this.worldObj.isRemote) {
            this.aiSit.setSitting(!isSitting());
            return true;
        }
        return super.interact(player);
    }

    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        if (this.riddenByEntity instanceof EntityLivingBase && isSaddled()) {
            EntityLivingBase rider = (EntityLivingBase) this.riddenByEntity;
            this.prevRotationYaw = this.rotationYaw = rider.rotationYaw;
            this.rotationPitch = rider.rotationPitch * 0.5F;
            setRotation(this.rotationYaw, this.rotationPitch);
            this.rotationYawHead = this.renderYawOffset = this.rotationYaw;
            strafe = rider.moveStrafing * 0.5F;
            forward = rider.moveForward;
            if (forward <= 0.0F) {
                forward *= 0.25F;
            }
            this.stepHeight = 1.0F;
            this.jumpMovementFactor = getAIMoveSpeed() * 0.1F;
            if (!this.worldObj.isRemote) {
                setAIMoveSpeed((float) getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 1.3F);
                super.moveEntityWithHeading(strafe, forward);
            }
            this.prevLimbSwingAmount = this.limbSwingAmount;
            double dx = this.posX - this.prevPosX;
            double dz = this.posZ - this.prevPosZ;
            float speed = Math.min(1.0F, (float) Math.sqrt(dx * dx + dz * dz) * 4.0F);
            this.limbSwingAmount += (speed - this.limbSwingAmount) * 0.4F;
            this.limbSwing += this.limbSwingAmount;
        } else {
            this.stepHeight = 0.5F;
            this.jumpMovementFactor = 0.02F;
            super.moveEntityWithHeading(strafe, forward);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.worldObj.isRemote && this.riddenByEntity instanceof EntityPlayer && this.ticksExisted % 20 == 0) {
            ((EntityPlayer) this.riddenByEntity).addPotionEffect(new PotionEffect(Potion.fireResistance.id, 60, 0, true));
            this.riddenByEntity.extinguish();
        }
    }

    @Override
    public double getMountedYOffset() {
        return this.height * 0.85D;
    }

    @Override
    protected void dropEquipment(boolean hit, int looting) {
        if (isSaddled()) {
            dropItem(Items.saddle, 1);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("Saddled", isSaddled());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setSaddled(tag.getBoolean("Saddled"));
    }

    @Override
    public EntityAgeable createChild(EntityAgeable partner) {
        return null;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
                && !this.worldObj.isAnyLiquid(this.boundingBox);
    }

    @Override
    public boolean isFlyingAnimation() {
        return false;
    }

    @Override
    public float getActionProgress() {
        return 0.0F;
    }
}
