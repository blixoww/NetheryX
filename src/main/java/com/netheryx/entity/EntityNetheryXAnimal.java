package com.netheryx.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Animal du Nether : insensible au feu, se reproduit avec sa nourriture, apparait sur les sols du Nether (pas besoin
 * d'herbe ni de lumiere comme les animaux vanilla).
 */
public abstract class EntityNetheryXAnimal extends EntityAnimal implements IAnimatedMob {
    private final Item food;

    public EntityNetheryXAnimal(World world, Item food) {
        super(world);
        this.food = food;
        this.isImmuneToFire = true;
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.3D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        if (food != null) {
            this.tasks.addTask(3, new EntityAITempt(this, 1.1D, food, false));
        }
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    protected void stats(double health, double speed) {
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(health);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(speed);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack != null && this.food != null && stack.getItem() == this.food;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.worldObj.checkNoEntityCollision(this.boundingBox)
                && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
                && !this.worldObj.isAnyLiquid(this.boundingBox);
    }

    protected void drop(Item item, int min, int max, int looting) {
        int count = min + this.rand.nextInt(max - min + 1) + (looting > 0 ? this.rand.nextInt(looting + 1) : 0);
        if (count > 0) {
            dropItem(item, count);
        }
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
