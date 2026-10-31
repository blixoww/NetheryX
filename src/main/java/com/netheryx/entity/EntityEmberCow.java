package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Vache de braise : se trait comme une vache, donne du boeuf infernal ; se nourrit de ble de cendre. */
public class EntityEmberCow extends EntityCow {
    public EntityEmberCow(World world) {
        super(world);
        this.isImmuneToFire = true;
    }

    @Override
    public EntityCow createChild(EntityAgeable partner) {
        return new EntityEmberCow(this.worldObj);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack != null && stack.getItem() == ModItems.cinderWheat;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
                && !this.worldObj.isAnyLiquid(this.boundingBox);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        int count = 1 + this.rand.nextInt(3 + looting);
        dropItem(isBurning() ? ModItems.cookedInfernalBeef : ModItems.infernalBeef, count);
        if (this.rand.nextBoolean()) {
            dropItem(Items.leather, 1);
        }
    }
}
