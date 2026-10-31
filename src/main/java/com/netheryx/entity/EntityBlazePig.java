package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Cochon de blaze : donne des cotelettes de blaze et un peu de poudre de blaze ; se nourrit de baies de feu. */
public class EntityBlazePig extends EntityPig {
    public EntityBlazePig(World world) {
        super(world);
        this.isImmuneToFire = true;
    }

    @Override
    public EntityPig createChild(EntityAgeable partner) {
        return new EntityBlazePig(this.worldObj);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack != null && stack.getItem() == ModItems.fireBerries;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
                && !this.worldObj.isAnyLiquid(this.boundingBox);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(isBurning() ? ModItems.cookedBlazePork : ModItems.blazePork, 1 + this.rand.nextInt(2 + looting));
        if (this.rand.nextBoolean()) {
            dropItem(Items.blaze_powder, 1);
        }
    }
}
