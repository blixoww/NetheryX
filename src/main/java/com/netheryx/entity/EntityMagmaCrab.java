package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.world.World;

/** Crabe de magma : arpente les sources sulfureuses ; donne des pattes de crabe et des carapaces de magma. */
public class EntityMagmaCrab extends EntityNetheryXAnimal {
    public EntityMagmaCrab(World world) {
        super(world, ModItems.fireBerries);
        setSize(0.9F, 0.6F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(16.0D, 0.22D);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable partner) {
        return new EntityMagmaCrab(this.worldObj);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        drop(isBurning() ? ModItems.cookedCrabLeg : ModItems.crabLeg, 1, 2, looting);
        drop(ModItems.magmaShell, 0, 1, looting);
    }
}
