package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.world.World;

/** Scarabee de basalte : insecte cuirasse des terres cendrees ; donne des carapaces de basalte. */
public class EntityBasaltBeetle extends EntityNetheryXAnimal {
    public EntityBasaltBeetle(World world) {
        super(world, null);
        setSize(1.0F, 0.7F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(20.0D, 0.18D);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable partner) {
        return new EntityBasaltBeetle(this.worldObj);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        drop(ModItems.basaltCarapace, 1, 2, looting);
    }
}
