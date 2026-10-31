package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Poule de cendre : pond des oeufs de braise (au lieu d'oeufs ordinaires) ; se nourrit de graines de ble de cendre. */
public class EntityCinderHen extends EntityChicken {
    private int nextEmberEgg;

    public EntityCinderHen(World world) {
        super(world);
        this.isImmuneToFire = true;
        this.nextEmberEgg = 6000 + this.rand.nextInt(6000);
    }

    @Override
    public void onLivingUpdate() {
        this.timeUntilNextEgg = 100000; // pas d'oeuf vanilla
        super.onLivingUpdate();
        if (!this.worldObj.isRemote && !isChild() && --this.nextEmberEgg <= 0) {
            playSound("mob.chicken.plop", 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            dropItem(ModItems.emberEgg, 1);
            this.nextEmberEgg = 6000 + this.rand.nextInt(6000);
        }
    }

    @Override
    public EntityChicken createChild(EntityAgeable partner) {
        return new EntityCinderHen(this.worldObj);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack != null && stack.getItem() == ModItems.cinderWheatSeeds;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
                && !this.worldObj.isAnyLiquid(this.boundingBox);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(Items.feather, this.rand.nextInt(3 + looting));
        dropItem(isBurning() ? ModItems.cookedInfernalChicken : ModItems.infernalChicken, 1);
    }
}
