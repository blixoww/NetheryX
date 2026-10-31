package com.netheryx.entity;

import com.netheryx.registry.ModBlocks;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Porteur de lanterne : petit compagnon timide des marais des ames ; s'apprivoise avec des luminelles et vous suit. */
public class EntityLanternBearer extends EntityTameable implements IAnimatedMob {
    public EntityLanternBearer(World world) {
        super(world);
        setSize(0.6F, 1.2F);
        this.isImmuneToFire = true;
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.0D, 6.0F, 2.0F));
        this.tasks.addTask(5, new EntityAIWander(this, 0.8D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.28D);
    }

    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    @Override
    public boolean interact(EntityPlayer player) {
        ItemStack stack = player.inventory.getCurrentItem();
        Item glowcap = Item.getItemFromBlock(ModBlocks.glowcap);
        if (!isTamed() && stack != null && stack.getItem() == glowcap) {
            if (!player.capabilities.isCreativeMode) {
                stack.stackSize--;
            }
            if (!this.worldObj.isRemote) {
                if (this.rand.nextInt(3) == 0) {
                    TameHelper.tame(this, player);
                } else {
                    playTameEffect(false);
                    this.worldObj.setEntityState(this, (byte) 6);
                }
            }
            return true;
        }
        if (isTamed() && getOwner() == player && !this.worldObj.isRemote) {
            this.aiSit.setSitting(!isSitting());
            return true;
        }
        return super.interact(player);
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
