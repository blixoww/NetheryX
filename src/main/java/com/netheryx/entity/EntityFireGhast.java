package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.Items;
import net.minecraft.world.World;

/** Ghast de feu : variante plus resistante du ghast, qui lache des carapaces de magma. */
public class EntityFireGhast extends EntityGhast {
    public EntityFireGhast(World world) {
        super(world);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(24.0D);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (this.rand.nextInt(2) == 0) {
            dropItem(Items.ghast_tear, 1);
        }
        dropItem(ModItems.magmaShell, 1 + this.rand.nextInt(1 + looting));
    }
}
